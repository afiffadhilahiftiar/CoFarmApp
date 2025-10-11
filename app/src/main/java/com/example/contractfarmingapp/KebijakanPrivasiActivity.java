package com.example.contractfarmingapp;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import com.example.contractfarmingapp.R;

public class KebijakanPrivasiActivity extends AppCompatActivity {

    private TextView txtKebijakanPrivasi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kebijakan_privasi);

        txtKebijakanPrivasi = findViewById(R.id.txtKebijakanPrivasi);

        String kebijakan = "Kebijakan Privasi Aplikasi CoFarm\n\n" +
                "1. Definisi Data Pribadi\n" +
                "Data pribadi adalah setiap informasi yang dapat mengidentifikasi pengguna, baik secara langsung maupun tidak langsung, termasuk namun tidak terbatas pada nama, alamat, nomor telepon, alamat email, foto profil, dan informasi kontrak pertanian.\n\n" +
                "2. Jenis Data Pribadi\n" +
                "- Nama lengkap, alamat, nomor telepon, dan alamat email.\n" +
                "- Data profil, termasuk foto profil dan identitas (KTP).\n" +
                "- Data transaksi, kontrak pertanian, dan aktivitas penggunaan aplikasi.\n" +
                "- Data teknis perangkat dan log penggunaan aplikasi.\n\n" +
                "3. Legalitas Pemrosesan Data Pribadi\n" +
                "Pemrosesan data pribadi dilakukan berdasarkan:\n" +
                "- Persetujuan pengguna;\n" +
                "- Kebutuhan untuk menjalankan kontrak dengan pengguna;\n" +
                "- Kepatuhan terhadap kewajiban hukum yang berlaku;\n" +
                "- Kepentingan sah kami dalam memberikan layanan dan meningkatkan pengalaman pengguna.\n\n" +
                "4. Tujuan Pemrosesan Data Pribadi Anda\n" +
                "- Mengelola akun pengguna dan autentikasi;\n" +
                "- Menyediakan layanan kontrak pertanian dan fitur aplikasi lainnya;\n" +
                "- Mengirim pemberitahuan, pembaruan, dan informasi terkait layanan;\n" +
                "- Mematuhi kewajiban hukum dan regulasi yang berlaku;\n" +
                "- Analisis dan perbaikan kualitas layanan.\n\n" +
                "5. Pengendalian dan Transfer Data Pribadi\n" +
                "Kami menjaga data pribadi Anda dengan standar keamanan yang sesuai. Data tidak akan dijual atau disewakan ke pihak ketiga. Dalam hal tertentu, data dapat dibagikan kepada:\n" +
                "- Penyedia layanan pihak ketiga yang membantu operasional aplikasi (misal server, notifikasi, pembayaran);\n" +
                "- Pihak berwenang sesuai permintaan hukum.\n\n" +
                "6. Hak Anda sebagai Subjek Data Pribadi\n" +
                "- Mengakses data pribadi yang kami simpan;\n" +
                "- Memperbaiki atau memperbarui data pribadi;\n" +
                "- Menghapus data pribadi;\n" +
                "- Menarik persetujuan pemrosesan data;\n" +
                "- Mengajukan keberatan terhadap pemrosesan data pribadi;\n" +
                "- Meminta pembatasan pemrosesan data.\n\n" +
                "7. Jangka Waktu Pemrosesan Data Pribadi\n" +
                "Data pribadi disimpan selama akun pengguna aktif atau sesuai kebutuhan pemenuhan tujuan pemrosesan. Data yang tidak lagi diperlukan akan dihapus atau dianonimkan secara aman.\n\n" +
                "8. Perubahan Kebijakan Privasi\n" +
                "Kami dapat memperbarui Kebijakan Privasi ini dari waktu ke waktu. Setiap perubahan akan diberitahukan melalui aplikasi atau email. Penggunaan aplikasi setelah perubahan dianggap menyetujui kebijakan yang diperbarui.\n\n" +
                "9. Hubungi Kami\n" +
                "Jika Anda memiliki pertanyaan atau permintaan terkait data pribadi, silakan hubungi kami melalui:\n" +
                "- Email: sistemcerdasindonesia@gmail.com\n";

        txtKebijakanPrivasi.setText(kebijakan);
    }
}
