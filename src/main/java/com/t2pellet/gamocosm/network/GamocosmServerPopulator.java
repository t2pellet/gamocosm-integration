package com.t2pellet.gamocosm.network;

import com.t2pellet.gamocosm.ui.GamocosmServerEntryList;

public class GamocosmServerPopulator extends Thread {

    private GamocosmServerEntryList servers;

    public GamocosmServerPopulator(GamocosmServerEntryList servers) {
        super("Gamocosm Server Populator");
        this.servers = servers;
    }

    @Override
    public void run() {
        var server = GamocosmServer.get();
        servers.addServer(server.address, server.name);
    }
}
