package com.example.contractfarmingapp.models;

import java.io.Serializable;

public class ContractModel implements Serializable {

    private String id;
    private String oftakerId;
    private String namaPerusahaan;
    private String kebutuhan;
    private String jumlahkebutuhan;
    private String satuan;
    private String hargaPerKg;
    private String waktuDibutuhkan;
    private String persyaratan;
    private String jumlahKontrak;
    private String timeUpload;
    private String rating;
    private String lokasi;
    private String deskripsi;
    private String logoUrl;

    // Constructor lengkap
    public ContractModel(String id,
                         String oftakerId,
                         String namaPerusahaan,
                         String kebutuhan,
                         String jumlahkebutuhan,
                         String satuan,
                         String hargaPerKg,
                         String waktuDibutuhkan,
                         String persyaratan,
                         String jumlahKontrak,
                         String timeUpload,
                         String rating,
                         String lokasi,
                         String deskripsi,
                         String logoUrl) {
        this.id = id;
        this.oftakerId = oftakerId;
        this.namaPerusahaan = namaPerusahaan;
        this.kebutuhan = kebutuhan;
        this.jumlahkebutuhan = jumlahkebutuhan;
        this.satuan = satuan;
        this.hargaPerKg = hargaPerKg;
        this.waktuDibutuhkan = waktuDibutuhkan;
        this.persyaratan = persyaratan;
        this.jumlahKontrak = jumlahKontrak;
        this.timeUpload = timeUpload;
        this.rating = rating;
        this.lokasi = lokasi;
        this.deskripsi = deskripsi;
        this.logoUrl = logoUrl;
    }

    // Getter
    public String getId() { return id; }
    public String getOftakerId() { return oftakerId; }
    public String getNamaPerusahaan() { return namaPerusahaan; }
    public String getKebutuhan() { return kebutuhan; }
    public String getJumlahkebutuhan() { return jumlahkebutuhan; }
    public String getSatuan() { return satuan; }
    public String getHargaPerKg() { return hargaPerKg; }
    public String getWaktuDibutuhkan() { return waktuDibutuhkan; }
    public String getPersyaratan() { return persyaratan; }
    public String getJumlahKontrak() { return jumlahKontrak; }
    public String getTimeUpload() { return timeUpload; }
    public String getRating() { return rating; }
    public String getLokasi() { return lokasi; }
    public String getDeskripsi() { return deskripsi; }
    public String getLogoUrl() { return logoUrl; }

    // Setter
    public void setId(String id) { this.id = id; }
    public void setOftakerId(String oftakerId) { this.oftakerId = oftakerId; }
    public void setNamaPerusahaan(String namaPerusahaan) { this.namaPerusahaan = namaPerusahaan; }
    public void setKebutuhan(String kebutuhan) { this.kebutuhan = kebutuhan; }
    public void setJumlahkebutuhan(String jumlahkebutuhan) { this.jumlahkebutuhan = jumlahkebutuhan; }
    public void setSatuan(String satuan) { this.satuan = satuan; }
    public void setHargaPerKg(String hargaPerKg) { this.hargaPerKg = hargaPerKg; }
    public void setWaktuDibutuhkan(String waktuDibutuhkan) { this.waktuDibutuhkan = waktuDibutuhkan; }
    public void setPersyaratan(String persyaratan) { this.persyaratan = persyaratan; }
    public void setJumlahKontrak(String jumlahKontrak) { this.jumlahKontrak = jumlahKontrak; }
    public void setTimeUpload(String timeUpload) { this.timeUpload = timeUpload; }
    public void setRating(String rating) { this.rating = rating; }
    public void setLokasi(String lokasi) { this.lokasi = lokasi; }
    public void setDeskripsi(String deskripsi) { this.deskripsi = deskripsi; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }

    // toString() untuk debug
    @Override
    public String toString() {
        return "ContractModel{" +
                "id='" + id + '\'' +
                ", oftakerId='" + oftakerId + '\'' +
                ", namaPerusahaan='" + namaPerusahaan + '\'' +
                ", kebutuhan='" + kebutuhan + '\'' +
                ", jumlahkebutuhan='" + jumlahkebutuhan + '\'' +
                ", satuan='" + satuan + '\'' +
                ", hargaPerKg='" + hargaPerKg + '\'' +
                ", waktuDibutuhkan='" + waktuDibutuhkan + '\'' +
                ", persyaratan='" + persyaratan + '\'' +
                ", jumlahKontrak='" + jumlahKontrak + '\'' +
                ", timeUpload='" + timeUpload + '\'' +
                ", rating='" + rating + '\'' +
                ", lokasi='" + lokasi + '\'' +
                ", deskripsi='" + deskripsi + '\'' +
                ", logoUrl='" + logoUrl + '\'' +
                '}';
    }

    // equals & hashCode opsional (untuk membandingkan object)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ContractModel)) return false;
        ContractModel that = (ContractModel) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
