package com.t2pellet.gamocosm.network;

import com.t2pellet.gamocosm.Gamocosm;
import com.t2pellet.gamocosm.ui.GamocosmServerEntryList;

public class GamocosmServerPopulator extends Thread {

    private GamocosmServerEntryList servers;

    public GamocosmServerPopulator(GamocosmServerEntryList servers) {
        super("Gamocosm Server Populator");
        this.servers = servers;
    }

    @Override
    public void run() {
        while (!this.isInterrupted()) {
            try {
                var server = GamocosmServer.get();
                servers.addServer(server.address, server.name);
                Gamocosm.LOGGER.info("Successfully populated gamocosm server");
                break;
            } catch (Exception ex) {
                Gamocosm.LOGGER.warn("Failed getting gamocosm server. Retrying...");
            }
        }
    }
}
