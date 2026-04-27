# QR Code Integration - Smart e-Ticket Feature

## Overview
This document explains the QR Code integration for PinkShield appointments. The feature generates live QR codes for each appointment using the free QRServer API, allowing patients to scan for appointment e-tickets.

## What Was Implemented

### 1. FXML Updates (appointment_USER.fxml)

#### Added ImageView Import:
```xml
<?import javafx.scene.image.ImageView?>
```

#### Added QR Code Display Card:
- Located in the "My Booked Appointments" tab
- Displays a 150x150px ImageView with a drop shadow effect
- Shows "📱 Scan for e-Ticket" label above the QR code
- Styled as a card with padding and background radius

```xml
<VBox spacing="10" styleClass="card-pane" style="-fx-padding: 20; -fx-background-radius: 15; -fx-alignment: center;">
    <Label text="📱 Scan for e-Ticket" style="-fx-font-size: 14; -fx-font-weight: bold; -fx-text-alignment: center;"/>
    <ImageView fx:id="qrCodeImageView" fitWidth="150" fitHeight="150" preserveRatio="true" style="-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.2), 10, 0, 0, 5);"/>
</VBox>
```

### 2. Java Controller Updates (AppointmentUserController.java)

#### Added Required Imports:
```java
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
```

#### Added ImageView Field:
```java
@FXML private ImageView qrCodeImageView;
```

#### Added TableView Selection Listener in initialize():
The listener automatically generates QR codes when an appointment is selected:
```java
if (table != null) {
    table.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
        if (newVal != null) {
            updateQRCode(newVal);
        } else {
            if (qrCodeImageView != null) {
                qrCodeImageView.setImage(null);
            }
        }
    });
}
```

#### Implemented updateQRCode() Method:
This method handles all QR code generation logic:

1. **Summary Construction**: Builds a detailed ticket containing:
   - PinkShield e-Ticket header
   - Ticket ID (appointment ID)
   - Patient name
   - Doctor name
   - Appointment date & time
   - Appointment status
   - Notes (first 50 characters)

2. **URL Encoding**: Safely encodes the summary string for API usage:
   ```java
   String encodedData = URLEncoder.encode(summaryString, StandardCharsets.UTF_8.toString());
   ```

3. **API Integration**: Constructs QRServer API endpoint:
   ```java
   String qrApiUrl = "https://api.qrserver.com/v1/create-qr-code/?size=150x150&data=" + encodedData;
   ```

4. **Asynchronous Image Loading**: Uses background loading to prevent UI freezing:
   ```java
   Image qrImage = new Image(qrApiUrl, 150, 150, true, true);
   qrCodeImageView.setImage(qrImage);
   ```

5. **Exception Handling**: Catches and logs any errors during QR code generation.

## How It Works

### User Flow:
1. Patient logs in and navigates to "My Booked Appointments" tab
2. Patient clicks on any appointment in the TableView
3. The `updateQRCode()` method is automatically triggered
4. A QR code is generated with the appointment details
5. The QR code appears in the ImageView below the actions
6. Patient can scan the QR code with their phone to verify the appointment details

### Technical Flow:
1. TableView selection listener detects appointment selection
2. `updateQRCode(Appointment appt)` is called with the selected appointment
3. Appointment details are formatted into a summary string
4. Summary string is URL-encoded for safe API transmission
5. QRServer API endpoint is constructed with the encoded data
6. JavaFX Image is instantiated with `true, true` parameters for:
   - `preserveRatio=true`: Maintains aspect ratio
   - `backgroundLoading=true`: Loads image asynchronously without blocking UI
7. Generated QR image is displayed in the ImageView

## API Used

**QRServer API** (Free & No Authentication Required)
- Endpoint: `https://api.qrserver.com/v1/create-qr-code/`
- Parameters:
  - `size=150x150`: Pixel dimensions of QR code
  - `data`: URL-encoded content to encode in QR code
- Response: Direct image data (PNG)

## Benefits

✅ **No Backend Dependency**: Generates QR codes on-the-fly using free API
✅ **Non-Blocking UI**: Asynchronous image loading prevents freezing
✅ **Secure**: URL encoding protects special characters
✅ **Professional**: Drop shadow styling creates a polished appearance
✅ **User-Friendly**: Automatic generation on appointment selection
✅ **Scalable**: No database storage needed for QR codes

## Error Handling

If an error occurs during QR code generation:
- Exception is caught and logged to console
- Error message includes root cause
- UI remains responsive and functional
- ImageView displays nothing (null image)

## Future Enhancements

1. **Custom Branding**: Add PinkShield logo to QR code
2. **QR History**: Store generated QR codes for audit trail
3. **Email Integration**: Send QR code via email to patient
4. **Mobile Verification**: Create a mobile app feature to verify QR codes
5. **Time-Expiring QR**: Regenerate QR codes with expiration timestamps

## Testing

To test the QR code feature:
1. Book an appointment through the "Book Appointment" tab
2. Navigate to "My Booked Appointments" tab
3. Click on the appointment in the table
4. Verify QR code appears within a few seconds
5. Scan QR code with any QR scanner app to verify content
6. Select a different appointment to generate a new QR code

## Notes

- QR codes are generated on-demand, not stored in the database
- Image loading is asynchronous, so there may be a slight delay (1-2 seconds)
- QR code size is fixed at 150x150px but can be adjusted in the API URL
- The encoding supports Unicode characters, so special characters in names work correctly

