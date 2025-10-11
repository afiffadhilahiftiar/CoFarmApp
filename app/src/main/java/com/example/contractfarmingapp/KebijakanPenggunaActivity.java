package com.example.contractfarmingapp;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class KebijakanPenggunaActivity extends AppCompatActivity {

    private TextView txtKebijakanPengguna;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kebijakan_pengguna);

        txtKebijakanPengguna = findViewById(R.id.txtKebijakanPengguna);

        String kebijakanPengguna = "Kebijakan Penggunaan CoFarm\n\n"

                + "1. Definisi\n"
                + "Kebijakan Penggunaan ini menetapkan aturan bagi semua pengguna aplikasi CoFarm "
                + "(“Aplikasi”) terkait akses, interaksi, dan penggunaan layanan, informasi, dan fitur yang tersedia. "
                + "Dengan menggunakan Aplikasi, pengguna setuju mematuhi ketentuan ini.\n\n"

                + "2. Prinsip Umum\n"
                + "- Gunakan Aplikasi secara sah dan etis.\n"
                + "- Hormati hak orang lain, termasuk hak privasi dan properti intelektual.\n"
                + "- Jangan menyalahgunakan sistem, data, atau fitur Aplikasi untuk tujuan ilegal atau merugikan pihak lain.\n\n"

                + "3. Larangan Khusus\n"
                + "a. Pelanggaran privasi atau pengumpulan data ilegal:\n"
                + "- Mengumpulkan, mengolah, atau mengungkapkan data pribadi tanpa izin.\n"
                + "- Meminta informasi sensitif seperti nomor kartu kredit, rekening bank, NIK, kata sandi, atau kunci API.\n"
                + "- Menggunakan identifikasi biometrik tanpa izin.\n"
                + "- Menyebarkan spyware atau pemantauan tidak sah.\n\n"

                + "b. Aktivitas yang membahayakan hak, keselamatan, atau kesejahteraan orang lain:\n"
                + "- Mengambil tindakan atas nama pengguna lain tanpa izin.\n"
                + "- Memberikan nasihat hukum, medis/kesehatan, atau finansial yang disesuaikan.\n"
                + "- Membuat keputusan otomatis yang memengaruhi hak atau kesejahteraan seseorang.\n"
                + "- Memfasilitasi perjudian uang nyata atau pinjaman potong gaji.\n"
                + "- Melakukan kampanye atau lobi politik yang dipersonalisasi.\n"
                + "- Menghalangi partisipasi masyarakat dalam proses demokrasi.\n\n"

                + "c. Keamanan dan integritas Aplikasi:\n"
                + "- Menyebarkan virus, malware, atau kode berbahaya.\n"
                + "- Mengganggu, merusak, atau memodifikasi sistem, server, atau jaringan Aplikasi.\n"
                + "- Mengakses akun orang lain tanpa izin.\n\n"

                + "4. Kewajiban Pengguna\n"
                + "- Menjaga kerahasiaan akun dan kata sandi.\n"
                + "- Menggunakan data yang diperoleh melalui Aplikasi hanya untuk tujuan sah.\n"
                + "- Melaporkan pelanggaran keamanan atau penyalahgunaan.\n\n"

                + "5. Penggunaan Layanan\n"
                + "- Semua interaksi melalui Aplikasi harus transparan dan sesuai hukum.\n"
                + "- Pengguna bertanggung jawab atas konten yang diunggah.\n"
                + "- Pengelola berhak menangguhkan akun yang melanggar kebijakan.\n\n"

                + "6. Perubahan Kebijakan\n"
                + "- Kebijakan ini dapat diperbarui dari waktu ke waktu.\n"
                + "- Perubahan akan diberitahukan melalui Aplikasi atau email.\n"
                + "- Pengguna yang terus menggunakan Aplikasi dianggap menyetujui perubahan.\n\n"

                + "7. Hubungi Kami\n"
                + "Jika ada pertanyaan, hubungi sistemcerdasindonesia@gmail.com atau telepon +62 895 0230 2050.";

        txtKebijakanPengguna.setText(kebijakanPengguna);
    }
}
