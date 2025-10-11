package com.example.contractfarmingapp.models;

public class AnggotaModel {
    private int id;                 // ubah ke int
    private String nama;
    private String peran;
    private String areaSize;
    private String fotoProfile;
    private int jumlahKontrak;

    public AnggotaModel(int id, String nama, String peran, String areaSize, String fotoProfile, int jumlahKontrak) {
        this.id = id;
        this.nama = nama;
        this.peran = peran;
        this.areaSize = areaSize;
        this.fotoProfile = fotoProfile;
        this.jumlahKontrak = jumlahKontrak;
    }

    public int getId() {
        return id;
    }

    public String getNama() {
        return nama;
    }

    public String getPeran() {
        return peran;
    }

    public String getAreaSize() {
        return areaSize;
    }

    public String getFotoProfile() {
        return fotoProfile;
    }

    public int getJumlahKontrak() {
        return jumlahKontrak;
    }
}
