package net.dreemurr.paperdoll;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class PaperDoll implements ClientModInitializer {

    public static final Logger LOGGER = LogManager.getLogger();

    public static int x;
    public static int y;
    public static float scale;
    public static int rotation;
    public static boolean fponly;
    public static boolean alwayson;
    public static long delay;
    public static int bounds;

    private static final File file = new File(FabricLoader.getInstance().getConfigDir().resolve("paperdoll.properties").toString());

    @Override
    public void onInitializeClient() {
        setDefaults();
        loadConfig();
        saveConfig();
    }

    public static void loadConfig() {
        try {
            if(file.exists()) {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line = br.readLine();
                do {
                    String[] content = line.split("=");

                    if (content.length >= 2 && line.charAt(0) != '#') {
                        switch (content[0]) {
                            case "x":
                                x = Integer.parseInt(content[1]);
                                break;
                            case "y":
                                y = Integer.parseInt(content[1]);
                                break;
                            case "scale":
                                scale = Float.parseFloat(content[1]);
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
                            case "delay":
                                delay = Long.parseLong(content[1]);
                                break;
                            case "bounds":
                                bounds = Integer.parseInt(content[1]);
                                break;
                        }
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

            writer.write("### X Offset ### - default 20\n");
            writer.write("x=" + x + "\n\n");

            writer.write("### Y Offset ### - default 20\n");
            writer.write("y=" + y + "\n\n");

            writer.write("### Scale ### - default 1.0\n");
            writer.write("scale=" + scale + "\n\n");

            writer.write("### Rotation ### - default 20\n");
            writer.write("rotation=" + rotation + "\n\n");

            writer.write("### Render only when in First Person ### - default true\n");
            writer.write("fponly=" + fponly + "\n\n");

            writer.write("### Render ALWAYS instead of only when doing special actions ### - default false\n");
            writer.write("alwayson=" + alwayson + "\n\n");

            writer.write("### Delay to hide the PaperDoll when set to only render during special actions ### - default 1000\n");
            writer.write("delay=" + delay + "\n\n");

            writer.write("### Space that the PaperDoll can render ### - default 150\n");
            writer.write("bounds=" + bounds);

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
        scale = 1.0F;
        rotation = 20;
        fponly = true;
        alwayson = false;
        delay = 1000;
        bounds = 150;
    }
}