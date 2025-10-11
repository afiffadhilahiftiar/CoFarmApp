package com.example.contractfarmingapp;

import java.util.ArrayList;
import java.util.List;

public class ServiceDataHolder {

    private static ServiceDataHolder instance;

    private List<MapActivity.GeofenceArea> geofenceAreas = new ArrayList<>();
    private List<String> emails = new ArrayList<>();
    private String companyId = "";
    private String companyName = "";

    // Private constructor agar tidak bisa diinstansiasi luar
    private ServiceDataHolder() { }

    // Singleton instance
    public static synchronized ServiceDataHolder getInstance() {
        if (instance == null) {
            instance = new ServiceDataHolder();
        }
        return instance;
    }

    // Set data sekaligus
    public synchronized void setData(List<MapActivity.GeofenceArea> geofenceAreas,
                                     List<String> emails,
                                     String companyId,
                                     String companyName) {
        this.geofenceAreas = geofenceAreas != null ? geofenceAreas : new ArrayList<>();
        this.emails = emails != null ? emails : new ArrayList<>();
        this.companyId = companyId != null ? companyId : "";
        this.companyName = companyName != null ? companyName : "";
    }

    public synchronized List<MapActivity.GeofenceArea> getGeofenceAreas() {
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
