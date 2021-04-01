package net.dreemurr.paperdoll;

import java.io.*;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class PaperDoll implements ClientModInitializer {

    public static int x = 20;
    public static int y = 20;
    public static int scale = 1;
    public static int rotation = 20;
    public static boolean fponly = true;

    private static final File file = new File(FabricLoader.getInstance().getConfigDir().resolve("paperdoll.properties").toString());
    
    @Override
    public void onInitializeClient() {
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
                            scale = Integer.parseInt(content[1]);
                            break;
                        case "rotation":
                            rotation = Integer.parseInt(content[1]);
                            break;
                        case "fponly":
                            fponly = Boolean.parseBoolean(content[1]);
                            break;
                    }
                    line = br.readLine();
                } while (line != null);

                br.close();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
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

            writer.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}