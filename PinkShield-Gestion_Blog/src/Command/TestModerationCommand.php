<?php

namespace App\Command;

use App\Service\CommentModerationService;
use Symfony\Component\Console\Attribute\AsCommand;
use Symfony\Component\Console\Command\Command;
use Symfony\Component\Console\Input\InputInterface;
use Symfony\Component\Console\Output\OutputInterface;
use Symfony\Component\Console\Style\SymfonyStyle;

#[AsCommand(
    name: 'app:test-moderation',
    description: 'Test the comment moderation service with various inputs',
)]
class TestModerationCommand extends Command
{
    public function __construct(
        private CommentModerationService $moderationService
    ) {
        parent::__construct();
    }

    protected function execute(InputInterface $input, OutputInterface $output): int
    {
        $io = new SymfonyStyle($input, $output);

        $io->title('Comment Moderation Service Test');

        // Test cases
        $testCases = [
            ['text' => 'This is a nice and friendly comment!', 'expected' => 'PASS'],
            ['text' => 'Great article, thanks for sharing!', 'expected' => 'PASS'],
            ['text' => 'I love this content', 'expected' => 'PASS'],
            ['text' => 'This is fucking terrible', 'expected' => 'FAIL'],
            ['text' => 'You are a stupid idiot', 'expected' => 'FAIL'],
            ['text' => 'What a shit post', 'expected' => 'FAIL'],
            ['text' => 'Go kill yourself', 'expected' => 'FAIL'],
            ['text' => 'I hate this damn thing', 'expected' => 'FAIL'],
        ];

        $io->section('Running Tests...');

        $passed = 0;
        $failed = 0;

        foreach ($testCases as $index => $testCase) {
            $text = $testCase['text'];
            $expected = $testCase['expected'];

            try {
                $result = $this->moderationService->isApproved($text);
                $actual = $result ? 'PASS' : 'FAIL';

                if ($actual === $expected) {
                    $io->success("Test #" . ($index + 1) . ": ✓ CORRECT");
                    $io->writeln("  Text: \"$text\"");
                    $io->writeln("  Expected: $expected | Got: $actual");
                    $passed++;
                } else {
                    $io->error("Test #" . ($index + 1) . ": ✗ WRONG");
                    $io->writeln("  Text: \"$text\"");
                    $io->writeln("  Expected: $expected | Got: $actual");
                    $failed++;
                }
            } catch (\Exception $e) {
                $io->error("Test #" . ($index + 1) . ": ✗ EXCEPTION");
                $io->writeln("  Text: \"$text\"");
                $io->writeln("  Error: " . $e->getMessage());
                $failed++;
            }

            $io->newLine();
        }

        // Summary
        $io->section('Test Summary');
        $io->writeln("Total Tests: " . count($testCases));
        $io->writeln("Passed: <fg=green>$passed</>");
        $io->writeln("Failed: <fg=red>$failed</>");

        if ($failed === 0) {
            $io->success('All tests passed! The moderation service is working correctly.');
            return Command::SUCCESS;
        } else {
            $io->warning("Some tests failed. Check the configuration or API key.");
            return Command::FAILURE;
        }
    }
}
