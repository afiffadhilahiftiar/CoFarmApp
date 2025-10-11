package com.example.contractfarmingapp.models;

public class TraktorModel {
    private String id;
    private String jenisTraktor;
    private String companyId;
    private String kapasitas;
    private String namaOperator;
    private String noHp;
    private String fotoTraktor;
    private String createdAt;

    // Konstruktor
    public TraktorModel(String id, String jenisTraktor, String companyId, String kapasitas,
                        String namaOperator, String noHp, String fotoTraktor, String createdAt) {
        this.id = id;
        this.jenisTraktor = jenisTraktor;
        this.companyId = companyId;
        this.kapasitas = kapasitas;
        this.namaOperator = namaOperator;
        this.noHp = noHp;
        this.fotoTraktor = fotoTraktor;
        this.createdAt = createdAt;
    }

    // Getter dan Setter
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getJenisTraktor() {
        return jenisTraktor;
    }

    public void setJenisTraktor(String jenisTraktor) {
        this.jenisTraktor = jenisTraktor;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public String getKapasitas() {
        return kapasitas;
    }

    public void setKapasitas(String kapasitas) {
        this.kapasitas = kapasitas;
    }

    public String getNamaOperator() {
        return namaOperator;
    }

    public void setNamaOperator(String namaOperator) {
        this.namaOperator = namaOperator;
    }

    public String getNoHp() {
        return noHp;
    }

    public void setNoHp(String noHp) {
        this.noHp = noHp;
    }

    public String getFotoTraktor() {
        return fotoTraktor;
    }

    public void setFotoTraktor(String fotoTraktor) {
        this.fotoTraktor = fotoTraktor;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
