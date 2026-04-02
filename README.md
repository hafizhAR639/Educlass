# EduClass - Android Application 📚

EduClass adalah aplikasi manajemen pembelajaran berbasis Android yang dirancang untuk memudahkan Siswa SMA dalam mengelola aktivitas akademik dengan Gaya belajar terpersonalisasi. Saat ini, aplikasi telah mengimplementasikan sistem autentikasi dasar menggunakan database lokal.

---

## ⚙️ Fitur Saat Ini
* **Splash Screen**: Tampilan pembuka interaktif dengan durasi 3 detik sebelum masuk ke aplikasi.
* **Registrasi Akun**: Fitur untuk mendaftarkan user baru dengan validasi input (Nama, Email, Password).
* **Login System**: Autentikasi keamanan untuk masuk ke aplikasi menggunakan data yang tersimpan.
* **Local Storage**: Penyimpanan data pengguna yang aman di dalam perangkat menggunakan SQLite.
* **Toggle Password**: Fitur *Show/Hide* password untuk meningkatkan kenyamanan pengguna saat mengetik.

---

## 📂 Struktur Folder & File Utama

| Lokasi / File | Fungsi |
| :--- | :--- |
| `java/com.belajar.myapplication/` | Pusat logika program menggunakan bahasa Java. |
| `DatabaseHelper.java` | *Backend* lokal: Mengatur pembuatan tabel, proses simpan (*Insert*), dan pencarian data (*Query*). |
| `LoginActivity.java` | Mengatur alur masuk, validasi email, dan perpindahan ke halaman register. |
| `RegisterActivity.java` | Mengatur pendaftaran user baru dan memastikan data input sudah sesuai kriteria. |
| `res/layout/` | Kumpulan file XML yang mengatur tata letak (UI) setiap halaman. |
| `res/drawable/` | Tempat penyimpanan aset visual seperti logo, icon mata, dan background. |
| `AndroidManifest.xml` | "KTP" Aplikasi: Mendaftarkan semua Activity agar bisa dijalankan oleh sistem Android. |

---

## 🚀 Cara Kerja Aplikasi (Alur Kerja)

1.  **Awal (Launcher)**: Aplikasi dimulai dari `SplashActivity`.
2.  **Validasi Login**: User memasukkan kredensial di `LoginActivity`. Data akan dicocokkan dengan tabel `users` di SQLite.
3.  **Proses Daftar**: Jika belum punya akun, user diarahkan ke `RegisterActivity`. 
    * Sistem mengecek apakah email sudah terdaftar (**UNIQUE Constraint**).
    * Sistem memastikan *Confirm Password* sama dengan *Password* utama.
4.  **Sesi Berhasil**: Jika login sukses, user diarahkan ke `HomepageActivity`.

---

## 🛠️ Cara Menjalankan Project

Ikuti panduan ini untuk menjalankan EduClass di perangkat kamu:

### 1. Persiapan Environment
* Buka **Android Studio** (Versi terbaru disarankan).
* Pastikan koneksi internet aktif untuk proses *Gradle Build* pertama kali.

### 2. Impor Project
* Pilih **File > Open** dan arahkan ke folder project ini.
* Tunggu hingga status "Gradle Sync Finished" muncul di bagian bawah.

### 3. Menjalankan di Emulator (Virtual Device)
1.  Klik ikon **Device Manager** (pojok kanan atas).
2.  Pilih **Create Device** > Pilih tipe HP (contoh: Pixel 6) > **Next**.
3.  Pilih System Image (minimal API 24/Android 7.0).
4.  Setelah Emulator menyala, klik tombol **Run App** (ikon segitiga hijau ▷).

### 4. Menjalankan di HP Fisik (USB Debugging)
1.  Sambungkan HP ke Laptop dengan kabel USB.
2.  Aktifkan **Developer Options** di HP (Ketuk *Build Number* 7x di pengaturan ponsel).
3.  Aktifkan **USB Debugging**.
4.  Pilih nama HP kamu di Android Studio, lalu klik **Run App**.

---

## 🔍 Cara Debugging Data (Cek Database)
Untuk melihat data user yang masuk tanpa perlu kode tambahan:
1.  Jalankan aplikasi di Emulator/HP.
2.  Di Android Studio, klik menu **App Inspection** (biasanya di panel bawah).
3.  Pilih tab **Database Inspector**.
4.  Pilih proses aplikasi `com.belajar.myapplication` dan buka tabel `users`.

---

## 🛠️ Tech Stack
* **Bahasa**: Java
* **Database**: SQLite (Local)
* **Design**: XML Layouts
* **Minimum SDK**: API 24 (Android 7.0 "Nougat")
