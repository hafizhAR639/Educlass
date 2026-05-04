# EduClass App - Firebase Migration & Dynamic Content

## 🛠 Tech Stack
*   **Language**: Java
*   **Database**: Google Cloud Firestore (NoSQL)
*   **Authentication**: Firebase Auth
*   **Image Loading**: Glide
*   **Video Player**: Android YouTube Player SDK (PierfrancescoSoffritti)
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
    └── PageContent...  -> Fragment isi materi (Personalized Content)
```

## 🔄 Alur Aplikasi (User Flow)
1.  **Splash Screen**: Cek status login aplikasi.
2.  **Auth (Login/Regis)**: Masuk atau daftar akun via Firebase. Data profil (Gaya Belajar) disimpan di Firestore.
3.  **Home Page**: Melihat ringkasan & mata pelajaran populer.
4.  **Modul Page**: Memilih mata pelajaran (e.g., Matematika).
5.  **Materi Page**: Memilih bab/topik spesifik.
6.  **Content Page**: Membaca materi yang **otomatis menyesuaikan** dengan Gaya Belajar user.

## 📡 Alur Data (Data Flow)
1.  **Auth Profile**: Aplikasi mengambil `gaya_belajar` dari koleksi `users`.
2.  **Smart Content Loading**: Di `PageContentFragment`, aplikasi menggunakan gaya belajar tersebut untuk memilih sub-data di Firestore (`visual`, `audio`, atau `kinestetik`).
3.  **Robust Handling**: Menggunakan `String.valueOf()` di model dan pengecekan `null` di UI untuk mencegah crash jika data tidak lengkap.
4.  **Video Extraction**: Sistem secara otomatis mengekstrak ID video dari URL YouTube lengkap (watch?v=...) agar bisa diputar di player native.

## 📝 Catatan Perubahan & Robustness
- **Clean Code**: Semua warning mayor telah dibersihkan (unused imports, unchecked casts, null safety).
- **Auto-Lifecycle**: Video player otomatis berhenti/lepas saat fragment ditutup (mencegah memory leak).
- **Personalized UI**: Label header di halaman konten berubah secara dinamis sesuai profil user.

---
*Terakhir diupdate: 27 April 2026*
