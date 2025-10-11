package com.example.contractfarmingapp.models;

public class KontrakModel {
    private String id;
    private String namaPerusahaan;
    private String kebutuhan;
    private String jumlahkebutuhan;
    private String satuan;          // <-- tambahan
    private String hargaPerKg;
    private String waktuDibutuhkan;
    private String persyaratan;
    private String rating;
    private String jumlahKontrak;
    private String lokasi;
    private String timeUpload;
    private String logoUrl; // URL atau drawable name
    private String certificate;

    public KontrakModel(String id,
                        String kebutuhan,
                        String jumlahkebutuhan,
                        String satuan,                // <-- tambahan
                        String namaPerusahaan,
                        String rating,
                        String lokasi,
                        String hargaPerKg,
                        String waktuDibutuhkan,
                        String jumlahKontrak,
                        String persyaratan,
                        String timeUpload,
                        String logoUrl,
                        String certificate) {
        this.id = id;
        this.kebutuhan = kebutuhan;
        this.jumlahkebutuhan = jumlahkebutuhan;
        this.satuan = satuan;        // <-- set
        this.namaPerusahaan = namaPerusahaan;
        this.rating = rating;
        this.lokasi = lokasi;
        this.hargaPerKg = hargaPerKg;
        this.waktuDibutuhkan = waktuDibutuhkan;
        this.jumlahKontrak = jumlahKontrak;
        this.persyaratan = persyaratan;
        this.timeUpload = timeUpload;
        this.logoUrl = logoUrl;
        this.certificate = certificate;
    }

    public String getId() { return id; }
    public String getKebutuhan() { return kebutuhan; }
    public String getJumlahkebutuhan() { return jumlahkebutuhan; }
    public String getSatuan() { return satuan; }   // <-- getter
    public String getNamaPerusahaan() { return namaPerusahaan; }
    public String getHargaPerKg() { return hargaPerKg; }
    public String getWaktuDibutuhkan() { return waktuDibutuhkan; }
    public String getPersyaratan() { return persyaratan; }
    public String getRating() { return rating; }
    public String getJumlahKontrak() { return jumlahKontrak; }
    public String getLokasi() { return lokasi; }
    public String getTimeUpload() { return timeUpload; }
    public String getLogoUrl() { return logoUrl; }
    public String getCertificate() { return certificate; }
}
