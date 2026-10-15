package dev.armorskin;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArmorSkinClient implements ClientModInitializer {
    public static final String MOD_ID = "armorskin";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        ArmorSkinCommands.register();
        LOGGER.info("ArmorSkin success!");
    }
}
