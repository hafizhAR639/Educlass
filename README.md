# EduClass App - Personalized Learning Management System

EduClass adalah aplikasi pembelajaran adaptif berbasis Android yang menyesuaikan penyampaian materi berdasarkan gaya belajar pengguna (**Visual, Audio, atau Kinestetik**). Aplikasi ini menggunakan Firebase untuk autentikasi dan database Firestore secara real-time.

## 🛠 Tech Stack & Versi
*   **Bahasa Pemrograman**: Java (JDK 11)
*   **Android Gradle Plugin (AGP)**: v9.1.0
*   **Gradle Version**: v9.3.1
*   **Target SDK**: 36 | **Min SDK**: 28
*   **Database**: Google Cloud Firestore
*   **Authentication**: Firebase Auth (Email & Password)
*   **Library Utama**: Glide (Image), YouTube Player SDK, Material 3.

## 📂 Struktur Folder
```text
app/src/main/java/com/belajar/myapplication/
├── auth/           -> Login, Register, & AuthManager (Logika Firebase Auth)
├── data/models/    -> Model data (POJO) untuk mapping data Firestore
└── user/           -> UI & Fitur Utama (Home, Modul, Materi, Content, Profile)
```

---

## 💻 Panduan Instalasi Lengkap (Windows)

Ikuti panduan ini agar proyek berjalan lancar di komputer Anda.

### 1. Persiapan Software
Pastikan Anda sudah menginstall:
*   **Git**: [Download di sini](https://git-scm.com/download/win).
*   **Android Studio Ladybug (2024.2.1)** atau versi lebih baru.
*   **JDK 11 atau 17**: Konfigurasi di *File > Settings > Build, Execution, Deployment > Build Tools > Gradle > Gradle JDK*.

### 2. Cloning Project
1.  Buka folder tempat Anda ingin menyimpan proyek (misal: `D:\Projects`).
2.  Klik kanan, pilih **Git Bash Here**.
3.  Jalankan perintah:
    ```bash
    git clone https://github.com/username/repository-name.git
    ```
4.  Buka Android Studio, pilih **Open**, arahkan ke folder hasil clone.

### 3. Konfigurasi Firebase (Untuk Tim/Kolaborator)
Karena Anda sudah diundang ke project Firebase yang sama, **jangan membuat project baru**. Cukup unduh file konfigurasi yang sudah ada:

1.  Buka [Firebase Console](https://console.firebase.google.com/).
2.  Pilih project **EduClass** (atau nama project yang telah dibagikan).
3.  Klik ikon gerigi ⚙️ (**Project Settings**) di menu samping.
4.  Scroll ke bawah ke bagian **Your apps**.
5.  Cari aplikasi `com.belajar.myapplication`, lalu klik tombol **google-services.json**.
6.  **Pindahkan file tersebut** ke folder `MyApplication2/app/` (di dalam folder `app`).

### 4. Running & Troubleshooting
1.  **Sync Gradle**: Klik ikon gajah (**Sync Project with Gradle Files**) di pojok kanan atas.
2.  **Running**: Hubungkan HP (USB Debugging ON) atau gunakan Emulator API 28+, lalu klik tombol **Run** (Segitiga Hijau).

**Masalah Umum:**
*   **SDK Location**: Jika error, buat file `local.properties` di root folder dan isi: `sdk.dir=C\:\\Users\\NamaUser\\AppData\\Local\\Android\\Sdk`.
*   **Firebase Error**: Pastikan file `google-services.json` berada di folder `/app`, bukan di root project.

---

## 🔄 Alur Kerja Aplikasi
1.  **Auth**: Registrasi via `AuthRegisterActivity`. Data akun di Firebase Auth, profil di Firestore koleksi `users`.
2.  **Adaptif**: Di `PageContentFragment`, konten (Video/Gambar/Teks) akan otomatis berubah mengikuti field `gaya_belajar` di Firestore user tersebut.

---
*Kontributor: [Nama Anda/Tim]*
