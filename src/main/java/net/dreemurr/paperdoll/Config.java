package net.dreemurr.paperdoll;

import net.fabricmc.loader.api.FabricLoader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

public class Config {
    public static final Map<String, ConfigEntry> entries = new HashMap<>();

    private static final File file = new File(FabricLoader.getInstance().getConfigDir().resolve("paperdoll.properties").toString());

    public static void initialize() {
        setDefaults();
        loadConfig();
        saveConfig();
    }

    public static void loadConfig() {
        try {
            if(file.exists()) {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line = br.readLine();

                while (line != null) {
                    String[] content = line.split("=");

                    if (content.length >= 2 && line.charAt(0) != '#') {
                        if (entries.containsKey(content[0])) {

                            ConfigEntry entry = entries.get(content[0]);
                            try {
                                if (entry.modValue != null) {
                                    int value = Integer.parseInt(content[1]) % (int) entry.modValue;
                                    if (value < 0) value += (int) entry.modValue;

                                    entry.setValue(String.valueOf(value));
                                } else {
                                    entry.setValue(content[1]);
                                }
                            } catch (Exception e) {
                                entry.value = entry.defaultValue;
                            }
                        }
                    }
                    line = br.readLine();
                }
                br.close();
            }
        }
        catch (Exception e) {
            PaperDoll.LOGGER.warn("Failed to load config file! Generating a new one...");
            e.printStackTrace();
            setDefaults();
            saveConfig();
        }
    }

    public static void saveConfig() {
        try {
            FileWriter writer = new FileWriter(file);

            writer.write("### Horizontal offset that the paperdoll should render ### - default 20\n");
            writer.write("x=" + entries.get("x").value + "\n\n");

            writer.write("### Vertical offset that the paperdoll should render ### - default 20\n");
            writer.write("y=" + entries.get("y").value + "\n\n");

            writer.write("### Paperdoll scale ### - default 1.0\n");
            writer.write("scale=" + entries.get("scale").value + "\n\n");

            writer.write("### Paperdoll rotation ### - default 20\n");
            writer.write("rotation=" + entries.get("rotation").value + "\n\n");

            writer.write("### Render only when in First Person ### - default false\n");
            writer.write("fponly=" + entries.get("fponly").value + "\n\n");

            writer.write("### Render ALWAYS instead of only when doing special actions ### - default false\n");
            writer.write("alwayson=" + entries.get("alwayson").value + "\n\n");

            writer.write("### Delay to hide the paperdoll when rendering set to only special actions ### - default 1000\n");
            writer.write("delay=" + entries.get("delay").value + "\n\n");

            writer.write("### Size of the paperdoll rendering \"box\" ### - default 150\n");
            writer.write("bounds=" + entries.get("bounds").value + "\n\n");

            writer.write("### Render your own nametag ### - default false\n");
            writer.write("nametag=" + entries.get("nametag").value + "\n\n");

            writer.write("### Render the paperdoll when the debug screen is open ### - default false\n");
            writer.write("debugRender=" + entries.get("debugRender").value + "\n\n");

            writer.write("### Fix elytra vertical offset ### - default true\n");
            writer.write("elytraOffset=" + entries.get("elytraOffset").value);

            writer.close();
        }
        catch (Exception e) {
            PaperDoll.LOGGER.error("Failed to save config file!");
            e.printStackTrace();
        }
    }

    public static void copyConfig() {
        entries.forEach((s, configEntry) -> configEntry.setValue(configEntry.configValue.toString()));
    }

    public static void discardConfig() {
        entries.forEach((s, configEntry) -> configEntry.configValue = configEntry.value);
    }

    public static void setDefaults() {
        entries.clear();
        entries.put("x", new ConfigEntry<>(20));
        entries.put("y", new ConfigEntry<>(20));
        entries.put("scale", new ConfigEntry<>(1.0F));
        entries.put("rotation", new ConfigEntry<>(20));
        entries.put("fponly", new ConfigEntry<>(false));
        entries.put("alwayson", new ConfigEntry<>(false));
        entries.put("delay", new ConfigEntry<>(1000L));
        entries.put("bounds", new ConfigEntry<>(150));
        entries.put("nametag", new ConfigEntry<>(false));
        entries.put("debugRender", new ConfigEntry<>(false));
        entries.put("elytraOffset", new ConfigEntry<>(true));
    }

    public static class ConfigEntry<T> {
        public T value;
        public T defaultValue;
        public T configValue;
        public T modValue;

        public ConfigEntry(T value) {
            this(value, null);
        }

        public ConfigEntry(T value, T modValue) {
            this.value = value;
            this.defaultValue = value;
            this.configValue = value;
            this.modValue = modValue;
        }

        @SuppressWarnings("unchecked")
        private void setValue(String text) {
            try {
                if (value instanceof String)
                    value = (T) text;
                else if (value instanceof Boolean)
                    value = (T) Boolean.valueOf(text);
                else if (value instanceof Integer)
                    value = (T) Integer.valueOf(text);
                else if (value instanceof Float)
                    value = (T) Float.valueOf(text);
                else if (value instanceof Long)
                    value = (T) Long.valueOf(text);
                else if (value instanceof Double)
                    value = (T) Double.valueOf(text);
                else if (value instanceof Byte)
                    value = (T) Byte.valueOf(text);
                else if (value instanceof Short)
                    value = (T) Short.valueOf(text);
            } catch (Exception e) {
                value = defaultValue;
            }

            configValue = value;
        }
    }
}