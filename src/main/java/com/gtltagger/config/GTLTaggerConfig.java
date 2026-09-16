package com.gtltagger.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import com.gtltagger.GTLTaggerMod;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * All user-configurable settings (spec section 8), persisted to
 * <config>/gtltagger.json. Keybindings themselves are NOT stored
 * here — Minecraft's own KeyBinding system persists those to
 * options.txt automatically once registered.
 */
public class GTLTaggerConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("gtltagger.json");

    // --- Tier display (section 4) ---
    public LeftRightMode tierMode = LeftRightMode.BOTH;

    // --- Gamemodes (section 2 / 8) ---
    public String gamemode1 = "NethPot";
    public String gamemode2 = "Crystal";
    public boolean secondGamemodeEnabled = false;

    // --- TAB (section 5 / 8) ---
    public boolean tabTiersEnabled = true;

    // --- Automatic kit detection (section 6 / 8) ---
    public boolean automaticDetectionEnabled = true;

    // --- HUD (section 7 / 8) ---
    public boolean hudEnabled = true;
    public int hudX = 8;
    public int hudY = 8;

    private static GTLTaggerConfig instance;

    public static GTLTaggerConfig get() {
        if (instance == null) {
            instance = load();
        }
        return instance;
    }

    private static GTLTaggerConfig load() {
        if (Files.exists(PATH)) {
            try (Reader reader = Files.newBufferedReader(PATH, StandardCharsets.UTF_8)) {
                GTLTaggerConfig loaded = GSON.fromJson(reader, GTLTaggerConfig.class);
                if (loaded != null) {
                    return loaded;
                }
            } catch (IOException | RuntimeException e) {
                GTLTaggerMod.LOGGER.warn("Failed to read gtltagger.json, using defaults", e);
            }
        }
        return new GTLTaggerConfig();
    }

    public void save() {
        try {
            Files.createDirectories(PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(PATH, StandardCharsets.UTF_8)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException e) {
            GTLTaggerMod.LOGGER.warn("Failed to save gtltagger.json", e);
        }
    }
}
