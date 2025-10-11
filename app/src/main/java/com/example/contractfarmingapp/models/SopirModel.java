package com.example.contractfarmingapp.models;

public class SopirModel {
    private String id;
    private String nama;
    private String noHp;
    private String kendaraan;
    private String platNomor;
    private String kapasitas;

    // Tambahan untuk foto-foto
    private String fotoSopir;
    private String fotoKendaraan;
    private String fotoSim;
    private String fotoStnk;

    public SopirModel(String id, String nama, String noHp, String kendaraan,
                      String platNomor, String kapasitas,
                      String fotoSopir, String fotoKendaraan,
                      String fotoSim, String fotoStnk) {
        this.id = id;
        this.nama = nama;
        this.noHp = noHp;
        this.kendaraan = kendaraan;
        this.platNomor = platNomor;
        this.kapasitas = kapasitas;
        this.fotoSopir = fotoSopir;
        this.fotoSim = fotoSim;
        this.fotoStnk = fotoStnk;
        this.fotoKendaraan = fotoKendaraan;

    }

    // Getter & Setter
    public String getId() { return id; }
    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }
    public String getNoHp() { return noHp; }
    public void setNoHp(String noHp) { this.noHp = noHp; }
    public String getKendaraan() { return kendaraan; }
    public void setKendaraan(String kendaraan) { this.kendaraan = kendaraan; }
    public String getPlatNomor() { return platNomor; }
    public void setPlatNomor(String platNomor) { this.platNomor = platNomor; }
    public String getKapasitas() { return kapasitas; }
    public void setKapasitas(String kapasitas) { this.kapasitas = kapasitas; }

    public String getFotoSopir() { return fotoSopir; }
    public void setFotoSopir(String fotoSopir) { this.fotoSopir = fotoSopir; }

    public String getFotoSim() { return fotoSim; }
    public void setFotoSim(String fotoSim) { this.fotoSim = fotoSim; }

    public String getFotoStnk() { return fotoStnk; }
    public void setFotoStnk(String fotoStnk) { this.fotoStnk = fotoStnk; }
    public String getFotoKendaraan() { return fotoKendaraan; }
    public void setFotoKendaraan(String fotoKendaraan) { this.fotoKendaraan = fotoKendaraan; }

    @Override
    public String toString() {
        return "Nama: " + nama + "\n" +
                "No HP: " + noHp + "\n" +
                "Kendaraan: " + kendaraan + "\n" +
                "Plat Nomor: " + platNomor + "\n" +
                "Kapasitas: " + kapasitas + " kg";
    }
}
