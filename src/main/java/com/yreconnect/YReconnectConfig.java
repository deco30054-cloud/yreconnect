package com.yreconnect;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import java.io.*;
import java.nio.file.*;

public class YReconnectConfig {
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("yreconnect.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static YReconnectConfig INSTANCE = new YReconnectConfig();

    public boolean enabled = true;
    public double  triggerY = 0.0;
    public boolean triggerAbove = false;
    public int     reconnectDelayTicks = 20;

    public static YReconnectConfig get() { return INSTANCE; }

    public static void load() {
        if (!Files.exists(CONFIG_PATH)) { save(); return; }
        try (Reader r = new FileReader(CONFIG_PATH.toFile())) {
            YReconnectConfig loaded = GSON.fromJson(r, YReconnectConfig.class);
            if (loaded != null) INSTANCE = loaded;
        } catch (Exception e) {
            YReconnect.LOGGER.error("[YReconnect] Failed to load config: {}", e.getMessage());
        }
    }

    public static void save() {
        try (Writer w = new FileWriter(CONFIG_PATH.toFile())) {
            GSON.toJson(INSTANCE, w);
        } catch (Exception e) {
            YReconnect.LOGGER.error("[YReconnect] Failed to save config: {}", e.getMessage());
        }
    }
}
