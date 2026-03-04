<?php

namespace App\Service;

use Symfony\Component\HttpFoundation\File\UploadedFile;
use Symfony\Component\DependencyInjection\Attribute\Autowire;

/**
 * FaceRecognitionService — wraps Face++ API for registration and login.
 *
 *  Registration : processUploadedFace()  — detects a face and stores the image.
 *  Login        : compareFaces()         — compares the stored image against a live photo.
 */
class FaceRecognitionService
{
    private const DETECT_URL  = 'https://api-us.faceplusplus.com/facepp/v3/detect';
    private const COMPARE_URL = 'https://api-us.faceplusplus.com/facepp/v3/compare';

    public function __construct(
        #[Autowire('%kernel.project_dir%')] private string $projectDir,
        #[Autowire('%env(FACE_API_KEY)%')]  private string $apiKey,
        #[Autowire('%env(FACE_API_SECRET)%')] private string $apiSecret,
    ) {}

    // ─────────────────────────────────────────────────────────────────────────
    //  REGISTRATION — detect face, save image, return token info
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Process a face photo uploaded during registration.
     *
     * @return array{face_token: string|null, image_path: string|null}
     */
    public function processUploadedFace(UploadedFile $photo, string $userId): array
    {
        $result = ['face_token' => null, 'image_path' => null];

        try {
            // Store the image permanently
            $uploadDir  = $this->projectDir . '/public/uploads/faces/';
            if (!is_dir($uploadDir)) {
                mkdir($uploadDir, 0775, true);
            }
            $filename   = 'face_' . $userId . '_' . time() . '.jpg';
            $photo->move($uploadDir, $filename);
            $imagePath  = 'uploads/faces/' . $filename;
            $result['image_path'] = $imagePath;

            // Call Face++ Detect to get the face_token
            $absolutePath = $this->projectDir . '/public/' . $imagePath;
            $faceToken = $this->detectFace($absolutePath);
            $result['face_token'] = $faceToken;

        } catch (\Throwable $e) {
            // Non-blocking: log and continue without face data
            error_log('[FaceRecognitionService] processUploadedFace error: ' . $e->getMessage());
        }

        return $result;
    }

    /**
     * Call Face++ /detect on a local file and return the first face_token found.
     */
    private function detectFace(string $absoluteImagePath): ?string
    {
        if (!file_exists($absoluteImagePath)) {
            return null;
        }

        $ch = curl_init(self::DETECT_URL);
        curl_setopt_array($ch, [
            CURLOPT_POST           => true,
            CURLOPT_RETURNTRANSFER => true,
            CURLOPT_TIMEOUT        => 30,
            CURLOPT_POSTFIELDS     => [
                'api_key'    => $this->apiKey,
                'api_secret' => $this->apiSecret,
                'image_file' => new \CURLFile($absoluteImagePath, 'image/jpeg'),
            ],
        ]);

        $response = curl_exec($ch);
        curl_close($ch);

        if (!$response) {
            return null;
        }

        $data = json_decode($response, true);
        return $data['faces'][0]['face_token'] ?? null;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  LOGIN — compare stored image against live photo
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Compare a stored face image path against a freshly uploaded face photo.
     *
     * @param  string       $storedImagePath  Relative public path (e.g. "uploads/faces/face_xxx.jpg")
     * @param  UploadedFile $livePhoto        The photo captured/uploaded at login time
     * @return float  Confidence score 0–100. ≥ 70 is considered a match.
     */
    public function compareFaces(string $storedImagePath, UploadedFile $livePhoto): float
    {
        try {
            $storedAbsolute = $this->projectDir . '/public/' . $storedImagePath;

            if (!file_exists($storedAbsolute)) {
                return 0.0;
            }

            // Write the live photo to a temp file so cURL can read it
            $tmpFile = tempnam(sys_get_temp_dir(), 'face_login_') . '.jpg';
            copy($livePhoto->getPathname(), $tmpFile);

            $ch = curl_init(self::COMPARE_URL);
            curl_setopt_array($ch, [
                CURLOPT_POST           => true,
                CURLOPT_RETURNTRANSFER => true,
                CURLOPT_TIMEOUT        => 30,
                CURLOPT_POSTFIELDS     => [
                    'api_key'      => $this->apiKey,
                    'api_secret'   => $this->apiSecret,
                    'image_file1'  => new \CURLFile($storedAbsolute, 'image/jpeg'),
                    'image_file2'  => new \CURLFile($tmpFile, 'image/jpeg'),
                ],
            ]);

            $response = curl_exec($ch);
            curl_close($ch);

            if (file_exists($tmpFile)) {
                @unlink($tmpFile);
            }

            if (!$response) {
                return 0.0;
            }

            $data = json_decode($response, true);

            // Face++ returns a "confidence" field (0–100)
            return (float) ($data['confidence'] ?? 0.0);

        } catch (\Throwable $e) {
            error_log('[FaceRecognitionService] compareFaces error: ' . $e->getMessage());
            return 0.0;
        }
    }
}
