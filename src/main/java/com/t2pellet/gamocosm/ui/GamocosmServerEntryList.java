package com.t2pellet.gamocosm.ui;

import com.t2pellet.gamocosm.network.GamocosmServer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.commons.compress.utils.Lists;

import java.util.Collections;
import java.util.List;

@Environment(EnvType.CLIENT)
public class GamocosmServerEntryList {
    private final List<GamocosmServer> serverEntries = Lists.newArrayList();
    private boolean dirty;

    public GamocosmServerEntryList() {
    }

    public synchronized boolean needsUpdate() {
        return this.dirty;
    }

    public synchronized void markClean() {
        this.dirty = false;
    }

    public synchronized List<GamocosmServer> getServers() {
        return Collections.unmodifiableList(this.serverEntries);
    }

    public synchronized void addServer(GamocosmServer server) {
        if (!serverEntries.contains(server)) {
            serverEntries.add(server);
            this.dirty = true;
        }
    }
}
