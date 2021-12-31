package com.t2pellet.gamocosm.config;

import com.t2pellet.gamocosm.Gamocosm;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

@Config(name = Gamocosm.ID)
public class GamocosmConfig implements ConfigData {

    @ConfigEntry.Gui.Excluded
    private static GamocosmConfig instance;

    public static GamocosmConfig getInstance() {
        if (instance == null) {
            AutoConfig.register(GamocosmConfig.class, JanksonConfigSerializer::new);
            instance = AutoConfig.getConfigHolder(GamocosmConfig.class).getConfig();
        }
        return instance;
    }

    @Comment("Desired name for Gamocosm server in servers tab")
    public String name = "Gamocosm Server";

    @Comment("Gamocosm server ID. Found in URL for your gamocosm server")
    public String id = "";

    @Comment("Gamocosm server API key. Found in advanced settings for your gamocosm server")
    public String key = "";

    public String getURL() {
        return "https://gamocosm.com/servers/" + id + "/api/" + key + "/";
    }
}
