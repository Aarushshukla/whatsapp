# Firebase setup for `com.example.whatsappcleaner`

This project requires a real Firebase Android configuration file.
The old placeholder `app/google-services.json` has been removed on purpose.

## 1) Add the Android app in Firebase

In Firebase Console:

1. Open the correct Firebase project.
2. Add Android app package name exactly as:
   - `com.example.whatsappcleaner`
3. Download the generated `google-services.json`.
4. Save it to:
   - `app/google-services.json`

> Do **not** commit your real `google-services.json` to git.

## 2) Verify package name matches exactly

- Firebase Android app package: `com.example.whatsappcleaner`
- Android app package:
  - `applicationId` in `app/build.gradle`
  - `namespace` in `app/build.gradle`
  - `package` in `app/src/main/AndroidManifest.xml`

## 3) Add SHA-1 certificate fingerprint

From project root, run:

```bash
./gradlew signingReport
```

Copy the `SHA1` for your debug/release variant and add it in:

- Firebase Console → Project settings → Your apps → Add fingerprint

## 4) Enable Authentication provider

In Firebase Console:

- Authentication → Sign-in method → Enable **Email/Password**

## 5) Clean + rebuild

```bash
./gradlew clean :app:assembleDebug
```

## 6) Dependencies/plugins required

Already configured in this repository:

- Google Services plugin (`com.google.gms.google-services`)
- Firebase Auth (`com.google.firebase:firebase-auth-ktx`)

## Expected result

- Firebase initializes with real config.
- Signup works with Email/Password auth.
- The placeholder-config error no longer appears.
