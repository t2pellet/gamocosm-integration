package com.t2pellet.gamocosm;

import com.t2pellet.gamocosm.io.Config;

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

    @Section("coreconfig")
    public static class CoreConfig {
        @Section.Comment("Your gamocosm domain")
        private static String ip = "txsksmqb.gamocosm.com";

        public static String getIP() {
            return ip;
        }
    }
}
