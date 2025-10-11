package com.example.contractfarmingapp.models;

public class RiwayatKontrakModel {
    private String contract_id;
    private String oftaker_id;
    private String kebutuhan;
    private String jumlahKebutuhan;
    private String satuan;
    private String namaPerusahaan;
    private String waktuDibutuhkan;
    private String lahan;
    private String statuslahan;
    private String status;
    private String catatan;
    private String tanggal;
    private String ikutAsuransi; // baru ditambahkan

    public RiwayatKontrakModel(String contract_id, String oftaker_id, String kebutuhan, String jumlahKebutuhan, String satuan,
                               String namaPerusahaan, String waktuDibutuhkan, String lahan, String statuslahan,
                               String status, String catatan, String tanggal, String ikutAsuransi) {
        this.contract_id = contract_id;
        this.oftaker_id = oftaker_id;
        this.kebutuhan = kebutuhan;
        this.jumlahKebutuhan = jumlahKebutuhan;
        this.satuan = satuan;
        this.namaPerusahaan = namaPerusahaan;
        this.waktuDibutuhkan = waktuDibutuhkan;
        this.lahan = lahan;
        this.statuslahan = statuslahan;
        this.status = status;
        this.catatan = catatan;
        this.tanggal = tanggal;
        this.ikutAsuransi = ikutAsuransi; // set dari constructor
    }

    public String getContract_id() { return contract_id; }
    public String getOftaker_id() { return oftaker_id; }
    public String getKebutuhan() { return kebutuhan; }
    public String getJumlahKebutuhan() { return jumlahKebutuhan; }
    public String getSatuan() { return satuan; }
    public String getNamaPerusahaan() { return namaPerusahaan; }
    public String getWaktuDibutuhkan() { return waktuDibutuhkan; }
    public String getLahan() { return lahan; }
    public String getStatuslahan() { return statuslahan; }
    public String getStatus() { return status; }
    public String getCatatan() { return catatan; }
    public String getTanggal() { return tanggal; }
    public String getIkutAsuransi() { return ikutAsuransi; } // getter baru
}
