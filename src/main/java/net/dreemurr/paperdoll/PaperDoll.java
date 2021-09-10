package net.dreemurr.paperdoll;

import com.rits.cloning.Cloner;
import net.dreemurr.paperdoll.config.Config;
import net.fabricmc.api.ClientModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PaperDoll implements ClientModInitializer {

    public static final Logger LOGGER = LogManager.getLogger();
    public static final Cloner CLONER = new Cloner();
    public static boolean nameplate = true;

    @Override
    public void onInitializeClient() {
        Config.initialize();
    }
}