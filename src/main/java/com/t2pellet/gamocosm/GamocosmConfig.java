package com.t2pellet.gamocosm;

import com.t2pellet.gamocosm.io.Config;
import net.minecraft.client.network.ServerInfo;

import java.io.IOException;

public class GamocosmConfig extends Config {

    private static GamocosmConfig instance;

    public static GamocosmConfig getInstance() {
        if (instance == null) {
            try {
                instance = new GamocosmConfig();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return instance;
    }

    private GamocosmConfig() throws IOException, IllegalAccessException {
        super("gamocosm");
    }

    @Section("core")
    public static class Core {
        @Section.Comment("Desired name for gamocosm server")
        private static String name;
        @Section.Comment("Your gamocosm server ID")
        private static String id;
        @Section.Comment("Your gamocosm server API key")
        private static String key;

        public static String getName() {
            return name;
        }

        public static String getURL() {
            return "https://gamocosm.com/servers/" + id + "/api/" + key + "/";
        }
    }
}
