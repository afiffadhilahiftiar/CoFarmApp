package com.example.contractfarmingapp;

import java.util.ArrayList;
import java.util.List;

public class ServiceDataHolderPetani {

    private static ServiceDataHolderPetani instance;

    private List<MapPetaniAllActivity.GeofenceArea> geofenceAreas = new ArrayList<>();
    private List<String> emails = new ArrayList<>();
    private String companyId = "";
    private String companyName = "";

    // Private constructor agar tidak bisa diinstansiasi luar
    private ServiceDataHolderPetani() { }

    // Singleton instance
    public static synchronized ServiceDataHolderPetani getInstance() {
        if (instance == null) {
            instance = new ServiceDataHolderPetani();
        }
        return instance;
    }

    // Set data sekaligus
    public synchronized void setData(List<MapPetaniAllActivity.GeofenceArea> geofenceAreas,
                                     List<String> emails,
                                     String companyId,
                                     String companyName) {
        this.geofenceAreas = geofenceAreas != null ? geofenceAreas : new ArrayList<>();
        this.emails = emails != null ? emails : new ArrayList<>();
        this.companyId = companyId != null ? companyId : "";
        this.companyName = companyName != null ? companyName : "";
    }

    public synchronized List<MapPetaniAllActivity.GeofenceArea> getGeofenceAreas() {
        return geofenceAreas;
    }

    public synchronized List<String> getEmails() {
        return emails;
    }

    public synchronized String getCompanyId() {
        return companyId;
    }

    public synchronized String getCompanyName() {
        return companyName;
    }

    // Optional: reset data
    public synchronized void clear() {
        geofenceAreas.clear();
        emails.clear();
        companyId = "";
        companyName = "";
    }
}
