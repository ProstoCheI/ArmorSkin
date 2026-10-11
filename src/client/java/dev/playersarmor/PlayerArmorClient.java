package dev.playersarmor;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlayerArmorClient implements ClientModInitializer {
    public static final String MOD_ID = "playersarmor";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        LOGGER.info("Player Armor initialized");
    }
}
