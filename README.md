# EduClass App - Personalized Learning Management System

EduClass adalah aplikasi pembelajaran adaptif berbasis Android yang menyesuaikan penyampaian materi berdasarkan gaya belajar pengguna (**Visual, Audio, atau Kinestetik**). Aplikasi ini dirancang untuk manajemen pembelajaran yang efisien dengan pemisahan peran Admin dan User.

## 🛠 Tech Stack & Versi
*   **Bahasa Pemrograman**: Java (JDK 11)
*   **Android Gradle Plugin (AGP)**: v9.2.1
*   **Target SDK**: 36 | **Min SDK**: 28
*   **Database Remote**: Google Cloud Firestore (Primary) & Rencana Transisi Supabase.
*   **Database Lokal**: Room Persistence Library (Rencana Intermediary).
*   **Authentication**: Firebase Auth (Email & Password).
*   **Library Utama**: Glide (Image), YouTube Player SDK, Material 3.

##  Struktur Folder
```text
app/src/main/java/com/belajar/myapplication/
├── admin/          -> Fitur khusus Admin (Manage User, Add Subject, dll)
├── auth/           -> Login, Register, & AuthManager (Logika Firebase Auth)
├── data/           ->
│   ├── models/     -> Model data (POJO) untuk mapping Firestore/Room
│   └── local/      -> Implementasi Room Database (DAO, Entity, Database) - [In Progress]
└── user/           -> UI & Fitur Utama User (Home, Modul, Materi, Content)
```

##  Fitur Terbaru & Perbaikan
1.  **Admin Panel (Materi & User)**:
    *   Tampilan Daftar Topik kini berbentuk List panjang (Full-width) yang konsisten dengan sisi User.
    *   Fitur Tambah Mata Pelajaran dengan pemilihan warna tema dan unggah ikon.
    *   Manajemen User dengan pengambilan data real-time dari Firestore.
    *   Auto-initials avatar untuk mempermudah identifikasi user.
2.  **Fix UI - Add Subject**: Perbaikan warna teks input menjadi hitam (`#101828`) agar terlihat jelas saat mengetik di background putih.

## 🏗 Perencanaan Room (Local Persistence)
Aplikasi sedang dikembangkan untuk menggunakan **Room Persistence Library** sebagai "penengah" (cache layer).
*   **Tujuan**: Menghemat kuota Firestore (Free Tier) dan memungkinkan akses offline.
*   **Object Buffering**: File ikon subjek (path/URL) dan metadata subjek akan disimpan di lokal terlebih dahulu.
*   **Offline First**: User tetap bisa melihat daftar mata pelajaran yang sudah terunduh tanpa koneksi internet. Proses upload subjek baru akan dicatat secara lokal sebelum disinkronkan ke remote.

---

## 💻 Panduan Instalasi Lengkap

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

### 3. Konfigurasi Firebase
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
*   **Build Error**: Jika muncul error terkait `ProgressBar`, pastikan import `android.widget.ProgressBar` sudah tersemat di class adapter terkait.

---
*Kontributor: [Nama Anda/Tim]*
