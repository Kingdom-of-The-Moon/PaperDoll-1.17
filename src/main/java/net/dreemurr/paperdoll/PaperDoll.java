package net.dreemurr.paperdoll;

import net.fabricmc.api.ClientModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PaperDoll implements ClientModInitializer {

    public static final Logger LOGGER = LogManager.getLogger();

    @Override
    public void onInitializeClient() {
        Config.initialize();
    }
}