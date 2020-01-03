# Sypht Kotlin SDK Demo

The purpose of this repo is to demonstrate how to use the Sypht Kotlin SDK in Android.

The code create a simple email client that lists all emails that have attachments in them.
If any of your emails have PDF or image invoices in them, taping the email will show you details 
of the invoice without opening the image or PDF.

## How dit works

The attachments are downloaded from Microsoft's Outlook.com using their Graph v1.0 API. The 
attachments are then sent to Sypht. If the attachment is a valid invoice, Sypht will parse 
the invoice and provide you with a JSON object that contains all the invoice details.

## Requirements

- Android Studio 3.5.3+
- JDK8
- Android device or emulator with API 23+






