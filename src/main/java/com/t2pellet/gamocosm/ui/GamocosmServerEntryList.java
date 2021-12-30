package com.t2pellet.gamocosm.ui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.commons.compress.utils.Lists;

import java.util.Collections;
import java.util.List;

@Environment(EnvType.CLIENT)
public class GamocosmServerEntryList {
    private final List<GamocosmServerInfo> serverEntries = Lists.newArrayList();
    private boolean dirty;

    public GamocosmServerEntryList() {
    }

    public synchronized boolean needsUpdate() {
        return this.dirty;
    }

    public synchronized void markClean() {
        this.dirty = false;
    }

    public synchronized List<GamocosmServerInfo> getServers() {
        return Collections.unmodifiableList(this.serverEntries);
    }

    public synchronized void addServer(String address, String name) {
        var info = new GamocosmServerInfo(address, name);
        if (!serverEntries.contains(info)) {
            serverEntries.add(info);
            this.dirty = true;
        }
    }
}
