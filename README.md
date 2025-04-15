# 🍲 Kozinti – Kitchen Recipe Android App

**Kozinti** is an Android application that allows users to browse, create, and manage cooking recipes. It features user authentication, cloud data storage using Firebase, and full recipe management including ingredients, images, and instructions.

Built using **Android Studio**, this app leverages modern **Material3 design**, **Firebase Realtime Database**, and local device storage access for images.

---

## 📱 Features

- 🔐 **User Authentication** (Register/Login via Firebase)
- 📸 **Add Recipes** with images and detailed instructions
- 📝 **Edit & Delete Recipes**
- 👤 **Profile Management**
- 🌐 **Guest Browsing Mode**
- ☁️ **Firebase Integration** for cloud storage
- 🖼️ **Image Picker** with FileProvider for secure access
- 🎨 **Modern UI** using Material3 (Dark Theme)

---

## 🧱 Tech Stack

- **Language**: Java (or Kotlin if used)
- **IDE**: Android Studio
- **Database**: Firebase Realtime Database / Firebase Firestore
- **Storage**: Firebase Storage for images
- **Authentication**: Firebase Auth
- **Design**: Material 3 Theme (Dark)

---

## 🔐 Permissions

The following permissions are declared in `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```

> 🔒 Note: Starting Android 11, access to external storage must use **Storage Access Framework** or **MediaStore** unless you use scoped storage or target legacy APIs.

---

## 🧩 Firebase Setup

To connect with Firebase:

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create a new project (e.g. "Kozinti")
3. Add an Android app with your package name `com.example.kozinti`
4. Download the `google-services.json` file
5. Place it inside:  
   `app/` directory of your project
6. Add Firebase dependencies in `build.gradle`

Example:

```gradle
implementation 'com.google.firebase:firebase-auth'
implementation 'com.google.firebase:firebase-database'
implementation 'com.google.firebase:firebase-storage'
```

And apply the plugin:

```gradle
apply plugin: 'com.google.gms.google-services'
```

---

## 🏗️ Project Structure

```
Kozinti/
├── manifests/
│   └── AndroidManifest.xml
├── java/com/example/kozinti/
│   ├── Login.java
│   ├── Register.java
│   ├── MainActivity.java
│   ├── MainActivity_Guest.java
│   ├── addNewRecipe.java
│   ├── EditRecipe.java
│   ├── EditProfile.java
│   ├── Profile.java
│   ├── YourRecipes.java
│   └── RecipeDetails.java
├── res/
│   ├── drawable/         # App icons, images
│   ├── layout/           # XML UI Layouts
│   ├── values/           # Themes, strings, styles
│   └── xml/              # file_paths.xml, backup_rules.xml
└── google-services.json
```

---

## 🔍 Key Components

### `FileProvider` (Secure Image Sharing)
Declared in `AndroidManifest.xml`:

```xml
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.fileprovider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths" />
</provider>
```

> Required for securely accessing images taken or uploaded in the app.

---

## 🧪 How to Run the App

1. Clone the repository or import into Android Studio
2. Add your Firebase `google-services.json`
3. Sync Gradle and ensure dependencies are installed
4. Connect an Android device or emulator
5. Click **Run ▶️**

---

## ✨ UI & UX

- **Dark Mode** by default
- Material3 styling across all activities
- No Action Bars for a cleaner interface
- Soft keyboard managed with `adjustResize` where needed

---

## 🚀 Future Features

- [ ] Advanced search by ingredients
- [ ] Like & Save Recipes
- [ ] Firebase Firestore migration for better performance
- [ ] Multilingual Support (EN / AR / FR)
- [ ] Offline Mode

---

## 📄 License

This project is licensed under the MIT License.

---

## 🙌 Credits

- Developed using Android Studio
- Powered by Firebase
- Icons & UI inspired by Google Material Design

---

> “Cooking is an art, Kozinti makes it easy.”
