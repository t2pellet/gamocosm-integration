package com.t2pellet.gamocosm.ui;

public class GamocosmServerInfo {
    private final String address;
    private final String name;

    public GamocosmServerInfo(String address, String name) {
        this.address = address;
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public String getName() {
        return name;
    }
}
