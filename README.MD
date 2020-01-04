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

## Build the code

You will need to create an account of [Sypht.com](https://sypht.com) and get a "Client ID" and a "Client Secret"
for your account. These can either but put in `local.properties` for your project or
if you are building from the command line you can add them to your environment variables.

```
SYPHT_CLIENT_ID=Abc...
SYPHT_CLIENT_SECRET=xyz...
```

## Notes

`SYPHT_CLIENT_ID` and `SYPHT_CLIENT_SECRET` are required to run the code, the code will successfully
build if you don't add them, however the project will not be able to upload files to Sypht. If you
run the code without correct `SYPHT_CLIENT_ID` and `SYPHT_CLIENT_SECRET`, you will have to sign-out
of the app and sign back in (or uninstall and reinstall) before the code will work as expected.

You will need a Outlook.com or Hotmail account to use with this app. When the app is launched,
click on "Sign-in" in the nav bar and you will be taken to a webpage to sign-in to your account.
On successful sign-in, the app will download 1 page of emails with attachments. Only emails with
attachments will be fetched. All emails with PDF or Image attachments will have their attachments
upload to Sypht and parsed as an invoice.

For the purpose of this project, any field that ends with "\.[0-9]{0,2}" is considered a
currency field, all currencies are displayed as dollars.







