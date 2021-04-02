package net.dreemurr.paperdoll;

import java.io.*;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PaperDoll implements ClientModInitializer {

    public static final Logger LOGGER = LogManager.getLogger();

    public static int x;
    public static int y;
    public static double scale;
    public static int rotation;
    public static boolean fponly;
    public static boolean alwayson;
    public static long delay;

    private static final File file = new File(FabricLoader.getInstance().getConfigDir().resolve("paperdoll.properties").toString());

    @Override
    public void onInitializeClient() {
        setDefaults();
        if (!file.exists())
            saveConfig();
        loadConfig();
    }

    public static void loadConfig() {
        try {
            if(file.exists()) {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line = br.readLine();
                do {
                    String[] content = line.split(":");

                    switch (content[0]) {
                        case "x":
                            x = Integer.parseInt(content[1]);
                            break;
                        case "y":
                            y = Integer.parseInt(content[1]);
                            break;
                        case "scale":
                            scale = Double.parseDouble(content[1]);
                            break;
                        case "rotation":
                            rotation = Integer.parseInt(content[1]);
                            break;
                        case "fponly":
                            fponly = Boolean.parseBoolean(content[1]);
                            break;
                        case "alwayson":
                            alwayson = Boolean.parseBoolean(content[1]);
                            break;
                        case "delayy":
                            delay = Long.parseLong(content[1]);
                            break;
                    }
                    line = br.readLine();
                } while (line != null);

                br.close();
            }
        }
        catch (Exception e) {
            LOGGER.error("Failed to load config file! Generating a new one...");
            e.printStackTrace();
            setDefaults();
            saveConfig();
        }
    }

    public static void saveConfig() {
        try {
            FileWriter writer = new FileWriter(file);

            writer.write("x:" + x + "\n");
            writer.write("y:" + y + "\n");
            writer.write("scale:" + scale + "\n");
            writer.write("rotation:" + rotation + "\n");
            writer.write("fponly:" + fponly + "\n");
            writer.write("alwayson:" + alwayson + "\n");
            writer.write("delay:" + delay + "\n");

            writer.close();
        }
        catch (Exception e) {
            LOGGER.error("Failed to save config file!");
            e.printStackTrace();
        }
    }

    public static void setDefaults() {
        x = 20;
        y = 20;
        scale = 1.0d;
        rotation = 20;
        fponly = true;
        alwayson = false;
        delay = 1000;
    }
}