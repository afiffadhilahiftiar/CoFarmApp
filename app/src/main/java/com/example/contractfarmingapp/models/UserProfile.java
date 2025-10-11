package com.example.contractfarmingapp.models;

public class UserProfile {
    private String nama;
    private String alamat;
    private String noHp;
    private String ktp;
    private String gender;
    private String tanggalLahir;
    private String fotoProfilUrl;
    private String fotoKtpUrl;

    // Diperlukan untuk Firebase
    public UserProfile() {
    }

    public UserProfile(String nama, String alamat, String noHp, String ktp,
                       String gender, String tanggalLahir,
                       String fotoProfilUrl, String fotoKtpUrl) {
        this.nama = nama;
        this.alamat = alamat;
        this.noHp = noHp;
        this.ktp = ktp;
        this.gender = gender;
        this.tanggalLahir = tanggalLahir;
        this.fotoProfilUrl = fotoProfilUrl;
        this.fotoKtpUrl = fotoKtpUrl;
    }

    // Getter dan Setter
    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getAlamat() {
        return alamat;
    }

    public void setAlamat(String alamat) {
        this.alamat = alamat;
    }

    public String getNoHp() {
        return noHp;
    }

    public void setNoHp(String noHp) {
        this.noHp = noHp;
    }

    public String getKtp() {
        return ktp;
    }

    public void setKtp(String ktp) {
        this.ktp = ktp;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getTanggalLahir() {
        return tanggalLahir;
    }

    public void setTanggalLahir(String tanggalLahir) {
        this.tanggalLahir = tanggalLahir;
    }

    public String getFotoProfilUrl() {
        return fotoProfilUrl;
    }

    public void setFotoProfilUrl(String fotoProfilUrl) {
        this.fotoProfilUrl = fotoProfilUrl;
    }

    public String getFotoKtpUrl() {
        return fotoKtpUrl;
    }

    public void setFotoKtpUrl(String fotoKtpUrl) {
        this.fotoKtpUrl = fotoKtpUrl;
    }
}