# EduClass App - Personalized Learning Management System

EduClass adalah aplikasi pembelajaran adaptif berbasis Android yang menyesuaikan penyampaian materi berdasarkan gaya belajar pengguna (**Visual, Audio, atau Kinestetik**). Aplikasi ini terintegrasi dengan Firebase untuk manajemen data dan autentikasi secara real-time.

## 🛠 Tech Stack & Versi
Aplikasi ini dibangun menggunakan teknologi terbaru untuk memastikan performa dan keamanan:

*   **Bahasa Pemrograman**: Java (JDK 11)
*   **Android Gradle Plugin (AGP)**: v9.1.0
*   **Gradle Version**: v9.3.1
*   **Minimum SDK**: 28 (Android 9.0 Pie)
*   **Target SDK**: 36
*   **Database**: Google Cloud Firestore (NoSQL)
*   **Storage**: Firebase Storage (untuk aset gambar/file)
*   **Authentication**: Firebase Auth
*   **Library Utama**:
    *   `Glide v4.16.0` - Image loading & caching.
    *   `YouTube Player SDK v12.1.0` - Pemutar video native YouTube.
    *   `Material Components v1.13.0` - UI modern dengan Material 3.
    *   `Firebase BOM v33.1.0` - Sinkronisasi versi layanan Firebase.

## 📂 Struktur Folder & Fungsi
```text
app/src/main/java/com/belajar/myapplication/
├── auth/           -> Logika Autentikasi (Login, Register, & AuthManager)
├── data/models/    -> Model data (POJO) untuk mapping Firestore
└── user/           -> Antarmuka Pengguna & Fitur Utama
    ├── MainActivity        -> Container utama dengan Bottom Navigation
    ├── PageHomeFragment    -> Beranda: Ringkasan progres & materi populer
    ├── PageModulFragment   -> Daftar mata pelajaran (Subjects)
    ├── PageMateriFragment  -> Daftar topik/bab per mata pelajaran
    ├── PageContentFragment -> Konten adaptif (Visual/Audio/Kinestetik)
    ├── PageProfileFragment -> Profil user & pengaturan gaya belajar
    ├── PagePomodoro        -> Fitur produktivitas (Timer belajar)
    └── Adapters/           -> Adapter untuk RecyclerView (Subject & Topic)

app/src/main/res/
├── layout/         -> Definisi UI dalam format XML
├── drawable/       -> Aset grafis, icon, dan custom backgrounds
└── values/         -> Definisi warna (colors), teks (strings), & tema (themes)
```

## 💻 Panduan Instalasi (Windows)

Ikuti langkah-langkah berikut untuk menjalankan proyek ini di perangkat lokal Anda:

### 1. Persiapan Lingkungan
*   **Install Git**: Unduh dan install dari [git-scm.com](https://git-scm.com/).
*   **Install Android Studio**: Gunakan versi **Ladybug (2024.2.1)** atau yang lebih baru untuk mendukung Gradle 9+.
*   **Java Development Kit (JDK)**: Pastikan JDK 11 atau 17 terpasang dan terkonfigurasi di Android Studio.

### 2. Langkah-langkah Cloning & Running
1.  **Clone Repository**:
    Buka CMD atau PowerShell, arahkan ke folder tujuan, lalu jalankan:
    ```bash
    git clone https://github.com/username/repository-name.git
    ```
2.  **Buka Proyek**:
    *   Buka Android Studio.
    *   Pilih **Open** dan pilih folder `MyApplication2`.
3.  **Konfigurasi Firebase (Wajib)**:
    *   Buka [Firebase Console](https://console.firebase.google.com/).
    *   Buat proyek baru dan tambahkan aplikasi Android dengan package name `com.belajar.myapplication`.
    *   Unduh file `google-services.json` dan letakkan di dalam folder `app/`.
4.  **Sync Gradle**:
    *   Android Studio akan otomatis mendownload library yang dibutuhkan.
    *   Jika muncul peringatan, klik **"Sync Project with Gradle Files"**.
5.  **Jalankan Aplikasi**:
    *   Gunakan Emulator (API 28+) atau HP Fisik dengan USB Debugging aktif.
    *   Klik tombol **Run** (Ikon Segitiga Hijau) di bagian atas Android Studio.

## 🔄 Fitur Utama
*   **Smart Content Delivery**: Konten otomatis berubah format (teks/video/aktivitas) berdasarkan field `gaya_belajar` di profil Firestore user.
*   **Progress Tracking**: Menampilkan persentase penyelesaian materi di halaman Home.
*   **Pomodoro Timer**: Membantu user tetap fokus saat mempelajari materi.

---
*Terakhir diupdate: Mei 2024*
