package com.t2pellet.gamocosm;

import com.t2pellet.gamocosm.config.GamocosmConfig;
import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Gamocosm implements ModInitializer {

	public static final String ID = "gamocosm";
	public static final Logger LOGGER = LogManager.getLogger(ID);
	public static final GamocosmConfig CONFIG = GamocosmConfig.getInstance();

	@Override
	public void onInitialize() {
		LOGGER.info("Gamocosm integration is here! :)");
	}
}
