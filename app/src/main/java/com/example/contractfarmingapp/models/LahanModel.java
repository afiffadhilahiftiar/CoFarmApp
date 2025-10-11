package com.example.contractfarmingapp.models;

public class LahanModel {
    private final String id, company_id, nama, perusahaan, lokasi, status, komoditas, luas, email;

    public LahanModel(String id, String company_id, String nama, String perusahaan, String email, String lokasi, String status, String komoditas, String luas) {
        this.id = id;
        this.company_id = company_id;
        this.nama = nama;
        this.perusahaan = perusahaan;
        this.email = email;
        this.lokasi = lokasi;
        this.status = status;
        this.komoditas = komoditas;
        this.luas = luas;
    }



    public String getId() { return id; }
    public String getCompany_id() { return company_id; }
    public String getNama() { return nama; }
    public String getPerusahaan() { return perusahaan; }
    public String getEmail() { return email; }
    public String getLokasi() { return lokasi; }
    public String getStatus() { return status; }
    public String getKomoditas() { return komoditas; }
    public String getLuas() { return luas; }
}
