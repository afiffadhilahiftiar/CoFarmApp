package com.example.contractfarmingapp.models;

public class Petani {
    public int id;
    public int user_id;
    public String nama;
    public String harga;
    public String lahan;
    public String progres;
    public String catatan;
    public String companyName;
    public int companyId;
    public int contract_id;
    public String status;
    public String statusLahan;
    public int oftaker_id;

    // Field tambahan sesuai API baru
    public String kebutuhan;
    public String satuan;
    public String waktuDibutuhkan;
    public String ikutAsuransi;
    public String jumlahKebutuhan;
    public String tanggalAjukan;

    public Petani(int id, int user_id, String nama, String harga, String lahan, String progres,
                  String catatan, String companyName, int companyId, int contract_id, int oftaker_id,
                  String status, String statusLahan, String kebutuhan, String satuan, String waktuDibutuhkan,
                  String ikutAsuransi, String jumlahKebutuhan, String tanggalAjukan) {
        this.id = id;
        this.user_id = user_id;
        this.nama = nama;
        this.harga = harga;
        this.lahan = lahan;
        this.progres = progres;
        this.catatan = catatan;
        this.companyName = companyName;
        this.companyId = companyId;
        this.contract_id = contract_id;
        this.oftaker_id = oftaker_id;
        this.status = status;
        this.statusLahan = statusLahan;
        this.kebutuhan = kebutuhan;
        this.satuan = satuan;
        this.waktuDibutuhkan = waktuDibutuhkan;
        this.ikutAsuransi = ikutAsuransi;
        this.jumlahKebutuhan = jumlahKebutuhan;
        this.tanggalAjukan = tanggalAjukan;
    }
}
