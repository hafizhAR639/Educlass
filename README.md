# EduClass App - Firebase Migration & Dynamic Content

## Deskripsi Perubahan Terbaru
Aplikasi telah dimigrasi dari sistem penyimpanan lokal statis ke ekosistem **Firebase** untuk autentikasi dan database materi yang dinamis.

## Perubahan Utama (Migration & Features)

### 1. Autentikasi Firebase
- Menggantikan sistem login SQLite ke **Firebase Authentication**.
- **Login & Register**: Sekarang terhubung langsung ke server Firebase.
- **Data User**: Data tambahan (Nama Lengkap) disimpan secara otomatis ke **Cloud Firestore** saat registrasi.

### 2. Database Materi Dinamis (Cloud Firestore)
- Daftar Mata Pelajaran (Subjects) dan Materi (Topics) kini ditarik secara *real-time* dari Firestore.
- **Field yang Digunakan**:
  - `subjects`: `nama`, `color_hex`, `icon_name`, `total_moduls`, `order`.
  - `topics`: `judul`, `subject_id`, `views_count`.

### 3. Pembaruan Navigasi & UI
- **Homepage**: Menambahkan `RecyclerView` horizontal untuk "Mata Pelajaran Terpopuler" yang dapat di-scroll.
- **Modul Page**: Menampilkan grid mata pelajaran yang diambil secara dinamis dari database.
- **Materi List**: Implementasi `MateriFragment` yang menampilkan daftar topik spesifik berdasarkan mata pelajaran yang diklik.
- **Content Page**: Navigasi akhir dari topik ke `ContentFragment` yang menampilkan materi berdasarkan gaya belajar (Visual, Auditory, Kinesthetic).

### 4. Integrasi Ikon & Gambar Dinamis
- Menggunakan library **Glide** untuk optimasi pemuatan gambar.
- **Ikon Lokal Dinamis**: Sistem secara otomatis mencari ikon di folder `drawable` berdasarkan field `icon_name` dari database (Contoh: `"ic_matematika"`).

### 5. Peningkatan Struktur & Penamaan Kode
- **Refactoring Model**: Struktur model data (`ModelSubject`, `ModelTopic`) telah diperbaiki untuk konsistensi penamaan dan kemudahan integrasi.
- **Robustness**: Model kini menggunakan penanganan tipe data dinamis untuk mencegah error `Could not deserialize` jika terdapat perbedaan tipe data (String/Long) di Firestore.

## Alur Navigasi Baru
1. **Login/Register** (Firebase Auth) -> Masuk ke **Homepage**.
2. **Homepage/Modul** -> Klik Mata Pelajaran (Ditarik dari koleksi `subjects`).
3. **MateriFragment** -> Menampilkan List Topik (Ditarik dari koleksi `topics` filter by `subject_id`).
4. **ContentFragment** -> Konten Detail Materi.

## Persyaratan Teknis Baru
- File `google-services.json` harus berada di folder `app/`.
- SHA-1 Fingerprint PC/Laptop pengembang harus terdaftar di Firebase Console untuk menghindari `DEVELOPER_ERROR`.
- Library Baru: `firebase-auth`, `firebase-firestore`, `glide`.

---
*Terakhir diupdate: 27 April 2026*