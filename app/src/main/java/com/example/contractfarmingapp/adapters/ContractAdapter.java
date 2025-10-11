package com.example.contractfarmingapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.example.contractfarmingapp.ApiConfig;
import com.example.contractfarmingapp.R;
import com.example.contractfarmingapp.activities.ContractDetailActivity;
import com.example.contractfarmingapp.models.ContractModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContractAdapter extends RecyclerView.Adapter<ContractAdapter.ViewHolder> {

    private List<ContractModel> contractList;
    private Context context;


    public ContractAdapter(Context context, List<ContractModel> list) {
        this.context = context;
        this.contractList = list;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_contract, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ContractModel contract = contractList.get(position);

        holder.tvKebutuhan.setText(contract.getKebutuhan());
        String rawJumlah = String.valueOf(contract.getJumlahkebutuhan());
        String angkaJumlah = rawJumlah.replace(".", "").replace(",", "").replace(" ", "");
        int jumlahInt = 0;
        try {
            jumlahInt = Integer.parseInt(angkaJumlah);
        } catch (NumberFormatException e) {
            jumlahInt = 0;
        }
        String jumlahFormatted = String.format("%,d", jumlahInt).replace(',', '.');
        holder.tvJumlahKebutuhan.setText(String.format("%s %s", jumlahFormatted, contract.getSatuan()));
        holder.tvNamaPerusahaan.setText(contract.getNamaPerusahaan());
        String rawHarga = contract.getHargaPerKg(); // misal: "Rp 5.000" atau "5000"
        String angkaHarga = rawHarga.replace("Rp", "").replace(" ", "").replace(".", "");
        int hargaInt = 0;
        try {
            hargaInt = Integer.parseInt(angkaHarga);
        } catch (NumberFormatException e) {
            hargaInt = 0; // fallback jika gagal parse
        }
        String hargaFormatted = String.format("Rp%,d", hargaInt).replace(',', '.');
        holder.tvHarga.setText(String.format("Harga: %s/%s", hargaFormatted, contract.getSatuan()));
        holder.tvWaktu.setText("Waktu: " + contract.getWaktuDibutuhkan());
        holder.tvJumlahKontrak.setText(contract.getJumlahKontrak() + "peserta telah ajukan kontrak");
        holder.layoutSyaratTags.removeAllViews();
        String[] tags = contract.getPersyaratan().split(",");
        for (String tag : tags) {
            TextView tagView = new TextView(context);
            tagView.setText(tag.trim());
            tagView.setTextSize(12);
            tagView.setTextColor(context.getResources().getColor(R.color.white));
            tagView.setPadding(20, 10, 20, 10);

            GradientDrawable bg = new GradientDrawable();
            bg.setColor(context.getResources().getColor(R.color.teal_700));
            bg.setCornerRadius(40);
            tagView.setBackground(bg);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 16, 0);
            tagView.setLayoutParams(params);

            holder.layoutSyaratTags.addView(tagView);
        }
        holder.tvTimeUpload.setText("Diunggah: " + contract.getTimeUpload());
        holder.tvId.setText("ID: " + contract.getId());
        holder.itemView.setOnClickListener(v -> {
            Context context = holder.itemView.getContext();
            Intent intent = new Intent(context, ContractDetailActivity.class);
            intent.putExtra("contract_id", contract.getId());
            context.startActivity(intent);
        });
        holder.btnEdit.setOnClickListener(v -> {
            // Buat AlertDialog
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
            LayoutInflater inflater = LayoutInflater.from(context);
            View dialogView = inflater.inflate(R.layout.form_edit_kontrak, null);
            builder.setView(dialogView);

            // Ambil semua komponen di form_edit_kontrak
            EditText etNamaPerusahaan = dialogView.findViewById(R.id.etNamaPerusahaan);
            EditText etLokasi = dialogView.findViewById(R.id.etLokasi);
            EditText etRating = dialogView.findViewById(R.id.etRating);
            EditText etKebutuhan = dialogView.findViewById(R.id.etKebutuhan);
            EditText etJumlahKebutuhan = dialogView.findViewById(R.id.etJumlahKebutuhan);
            Spinner spinnerSatuan = dialogView.findViewById(R.id.spinnerSatuan);
            EditText etHargaPerKg = dialogView.findViewById(R.id.etHargaPerKg);
            EditText etWaktuDibutuhkan = dialogView.findViewById(R.id.etWaktuDibutuhkan);
            EditText etPersyaratan = dialogView.findViewById(R.id.etPersyaratan);
            EditText etDeskripsi = dialogView.findViewById(R.id.etDeskripsi);
            Button btnSubmitKontrak = dialogView.findViewById(R.id.btnSubmitKontrak);

            // Isi otomatis dari ContractModel
            etNamaPerusahaan.setText(contract.getNamaPerusahaan());
            etLokasi.setText(contract.getLokasi());
            etRating.setText(contract.getRating());
            etKebutuhan.setText(contract.getKebutuhan());
            etJumlahKebutuhan.setText(contract.getJumlahkebutuhan());

            // Spinner satuan
            ArrayAdapter<CharSequence> adapterSatuan = ArrayAdapter.createFromResource(
                    context, R.array.satuan, R.layout.spinner_item_3);
            adapterSatuan.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerSatuan.setAdapter(adapterSatuan);
            int spinnerPosition = adapterSatuan.getPosition(contract.getSatuan());
            spinnerSatuan.setSelection(spinnerPosition);

            etHargaPerKg.setText(contract.getHargaPerKg());
            etWaktuDibutuhkan.setText(contract.getWaktuDibutuhkan());
            etPersyaratan.setText(contract.getPersyaratan());
            etDeskripsi.setText(contract.getDeskripsi());

            // Tampilkan dialog
            android.app.AlertDialog dialog = builder.create();
            dialog.show();

            // Klik tombol submit → kirim update ke server
            btnSubmitKontrak.setOnClickListener(view -> {
                String namaPerusahaan = etNamaPerusahaan.getText().toString();
                String lokasi = etLokasi.getText().toString();
                String rating = etRating.getText().toString();
                String kebutuhan = etKebutuhan.getText().toString();
                String jumlahKebutuhan = etJumlahKebutuhan.getText().toString();
                String satuan = spinnerSatuan.getSelectedItem().toString();
                String hargaPerKg = etHargaPerKg.getText().toString();
                String waktuDibutuhkan = etWaktuDibutuhkan.getText().toString();
                String persyaratan = etPersyaratan.getText().toString();
                String deskripsi = etDeskripsi.getText().toString();

                // Volley POST request
                String url = ApiConfig.BASE_URL + "update_kontrak.php";
                RequestQueue queue = Volley.newRequestQueue(context);

                StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                        response -> {
                            try {
                                JSONObject json = new JSONObject(response);
                                boolean success = json.getBoolean("success");
                                String message = json.getString("message");

                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();

                                if (success) {
                                    // Update data lokal hanya jika sukses
                                    contract.setNamaPerusahaan(namaPerusahaan);
                                    contract.setLokasi(lokasi);
                                    contract.setRating(rating);
                                    contract.setKebutuhan(kebutuhan);
                                    contract.setJumlahkebutuhan(jumlahKebutuhan);
                                    contract.setSatuan(satuan);
                                    contract.setHargaPerKg(hargaPerKg);
                                    contract.setWaktuDibutuhkan(waktuDibutuhkan);
                                    contract.setPersyaratan(persyaratan);
                                    contract.setDeskripsi(deskripsi);
                                    notifyItemChanged(position);
                                    dialog.dismiss();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(context, "Error parsing server response", Toast.LENGTH_LONG).show();
                            }
                        },

                        error -> Toast.makeText(context, "Gagal update: " + error.getMessage(), Toast.LENGTH_LONG).show()
                ) {
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<>();
                        params.put("id", contract.getId());
                        params.put("namaPerusahaan", namaPerusahaan);
                        params.put("lokasi", lokasi);
                        params.put("rating", rating);
                        params.put("kebutuhan", kebutuhan);
                        params.put("jumlahkebutuhan", jumlahKebutuhan);
                        params.put("satuan", satuan);
                        params.put("hargaPerKg", hargaPerKg);
                        params.put("waktuDibutuhkan", waktuDibutuhkan);
                        params.put("persyaratan", persyaratan);
                        params.put("deskripsi", deskripsi);
                        return params;
                    }
                };
                queue.add(postRequest);
            });
        });
        holder.btnDelete.setBackgroundColor(Color.RED); // background merah
        holder.btnDelete.setTextColor(Color.WHITE);
        holder.btnDelete.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(context)
                    .setTitle("Hapus Kontrak")
                    .setMessage("Apakah Anda yakin ingin menghapus kontrak ini?")
                    .setPositiveButton("Hapus", (dialog, which) -> {
                        deleteContract(contract.getId(), position, holder);
                    })
                    .setNegativeButton("Batal", null)
                    .show();
        });

    }
    private void deleteContract(String contractId, int position, ViewHolder holder) {
        String url = ApiConfig.BASE_URL + "delete_kontrak.php";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        boolean success = json.getBoolean("success");
                        String message = json.getString("message");
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                        if (success) {
                            contractList.remove(position);
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position, contractList.size());
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(context, "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(context, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id", contractId);
                return params;
            }
        };

        Volley.newRequestQueue(context).add(request);
    }

    @Override
    public int getItemCount() {
        return contractList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvKebutuhan, tvJumlahKebutuhan, tvNamaPerusahaan, tvLokasi, tvRating;
        TextView tvHarga, tvWaktu, tvJumlahKontrak, tvTimeUpload, tvId;
        LinearLayout layoutSyaratTags; // TAMBAH INI
        Button btnEdit, btnDelete;

        public ViewHolder(View itemView) {
            super(itemView);
            tvKebutuhan = itemView.findViewById(R.id.tvKebutuhan);
            tvJumlahKebutuhan = itemView.findViewById(R.id.tvJumlahKebutuhan);
            tvNamaPerusahaan = itemView.findViewById(R.id.tvNamaPerusahaan);
            tvLokasi = itemView.findViewById(R.id.tvLokasi);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvHarga = itemView.findViewById(R.id.tvHarga);
            tvWaktu = itemView.findViewById(R.id.tvWaktu);
            tvJumlahKontrak = itemView.findViewById(R.id.tvJumlahKontrak);
            layoutSyaratTags = itemView.findViewById(R.id.layoutSyaratTags);
            tvTimeUpload = itemView.findViewById(R.id.tvTimeUpload);
            tvId = itemView.findViewById(R.id.tvId);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);

        }
    }
}
