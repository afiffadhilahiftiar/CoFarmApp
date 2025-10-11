package com.example.contractfarmingapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.contractfarmingapp.activities.AturAkunActivity;
import com.example.contractfarmingapp.activities.EditProfileActivity;

public class SettingsFragment extends Fragment {

    Button btnAturAkun, btnEditProfil, btnHapusAkun, btnKebijakanPrivasi, btnKebijakanPenggunaan;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        btnAturAkun = view.findViewById(R.id.btnAturAkun);
        btnEditProfil = view.findViewById(R.id.btnEditProfil);
        btnHapusAkun = view.findViewById(R.id.btnHapusAkun);
        btnKebijakanPrivasi = view.findViewById(R.id.btnKebijakanPrivasi);
        btnKebijakanPenggunaan = view.findViewById(R.id.btnKebijakanPenggunaan);

        btnAturAkun.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AturAkunActivity.class);
            startActivity(intent);
        });

        btnEditProfil.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), EditProfileActivity.class);
            startActivity(intent);
        });

        btnHapusAkun.setOnClickListener(v -> {
            new AlertDialog.Builder(getActivity())
                    .setTitle("Hapus Akun")
                    .setMessage("Apakah Anda yakin ingin menghapus akun?")
                    .setPositiveButton("Ya", (dialog, which) -> {
                        // Tambahkan proses hapus akun (panggil API backend atau Firebase)
                        Toast.makeText(getActivity(), "Akun berhasil dihapus", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Batal", null)
                    .show();
        });

        btnKebijakanPrivasi.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), KebijakanPrivasiActivity.class);
            startActivity(intent);
        });


        btnKebijakanPenggunaan.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), KebijakanPenggunaActivity.class);
            startActivity(intent);
        });

        return view;
    }

    private void openWeb(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }
}
