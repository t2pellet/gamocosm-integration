package com.t2pellet.gamocosm;

import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Gamocosm implements ModInitializer {

	public static final Logger LOGGER = LogManager.getLogger("modid");
	public static final GamocosmConfig CONFIG = GamocosmConfig.getInstance();

	@Override
	public void onInitialize() {
		LOGGER.info("Hello Fabric world!");
	}
}
