# EduClass App - Firebase Migration & Dynamic Content

## 🛠 Tech Stack
*   **Language**: Java
*   **Database**: Google Cloud Firestore (NoSQL)
*   **Authentication**: Firebase Auth
*   **Image Loading**: Glide
*   **UI Components**: Material Design, RecyclerView (Linear & Grid)

## 📂 Struktur Folder & Fungsi
```text
app/src/main/java/com/belajar/myapplication/
├── auth/           -> Logika Login, Register, & AuthManager
├── data/models/    -> Model data (POJO) untuk Firestore
└── user/           -> UI (Activity & Fragment) & Adapter untuk fitur User
    ├── MainActivity    -> Container utama (Bottom Nav)
    ├── PageHome...     -> Fragment beranda
    ├── PageModul...    -> Fragment daftar mata pelajaran
    ├── PageMateri...   -> Fragment daftar topik/bab
    └── PageContent...  -> Fragment isi materi detail
```

## 🔄 Alur Aplikasi (User Flow)
1.  **Splash Screen**: Cek status aplikasi.
2.  **Auth (Login/Regis)**: Masuk atau daftar akun via Firebase.
3.  **Home Page**: Melihat ringkasan & mata pelajaran populer.
4.  **Modul Page**: Memilih mata pelajaran yang ingin dipelajari.
5.  **Materi Page**: Memilih topik spesifik dari mata pelajaran tersebut.
6.  **Content Page**: Membaca/mempelajari isi materi detail.

## 📡 Alur Data (Data Flow)
1.  **Cloud Firestore**: Menyimpan data `subjects` (mapel) dan `topics` (bab).
2.  **Model (POJO)**: Mengambil data dari Firestore. Menggunakan prinsip **KISS** (simpel) dengan `String.valueOf()` agar aplikasi tidak crash jika ada tipe data yang tidak sesuai.
3.  **Adapter**: Menghubungkan data dari Model ke tampilan `RecyclerView` secara dinamis.
4.  **UI**: Menampilkan data akhir ke pengguna (Nama mapel, ikon, jumlah modul, dll).

## 📝 Catatan Perubahan Terbaru
- **Migrasi Firebase**: Transisi dari data lokal statis ke database real-time.
- **Robust Model**: Penanganan error `Could not deserialize` dengan menyederhanakan tipe data pada getter/setter model.
- **Ikon Dinamis**: Memanggil drawable berdasarkan string nama dari database.

---
*Terakhir diupdate: 27 April 2026*
