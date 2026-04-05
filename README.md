# Feature: Custom Floating Navbar

## Deskripsi
Implementasi Bottom Navigation Bar melayang dengan efek indikator lingkaran putih permanen saat menu aktif.

## Komponen yang Ditambahakan
- `activity_main.xml`: Menambahkan BottomNavigationView dengan background rounded.
- `MainActivity.java`: Menambahkan logika switch visibility antar layout.
- `nav_item_bg.xml`: Selector untuk lingkaran putih (menggunakan inset agar ukuran proporsional).
- `'bg_navbar_rounded.xml`: Backgorund untuk lingkaran putih saat aktif diklik.

## Cara Test
1. Jalankan aplikasi.
2. Klik ikon 'Modul', 'Chart', dan lain lain
3. Pastikan lingkaran putih muncul dan teks "Halaman ..." terlihat di layar.