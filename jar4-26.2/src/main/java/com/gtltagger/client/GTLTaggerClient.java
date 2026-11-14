package com.gtltagger.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import com.gtltagger.GTLTaggerMod;
import com.gtltagger.client.compat.Mc;
import com.gtltagger.client.gui.GTLTaggerSettingsScreen;
import com.gtltagger.client.gui.PlayerSearchScreen;
import com.gtltagger.client.hud.GTLTaggerHud;
import com.gtltagger.config.GTLTaggerConfig;
import com.gtltagger.data.TierDatabase;
import com.gtltagger.data.TierEntry;
import com.gtltagger.detect.KitDetector;
import com.gtltagger.gamemode.Gamemodes;
import com.gtltagger.tag.TierTagGenerator;

import java.util.HashMap;
import java.util.Map;

/**
 * Client entrypoint. GTLTagger is client-only, so config, HUD, keybinds
 * and commands are all wired up here.
 *
 * The TierTag is about the LOCAL player's own tier (what you paste in
 * chat when asked "what's your tier?"), for the detected kit or else
 * gamemode1: "Copy My TierTag" keybind (unbound by default) or
 * "/gtltagger tag". Left and right each cycle their own gamemode
 * (cycleLeft/RightGamemodeKey); H opens the player-search screen.
 */
public class GTLTaggerClient implements ClientModInitializer {

    private static KeyMapping cycleLeftGamemodeKey;
    private static KeyMapping cycleRightGamemodeKey;
    private static KeyMapping openPlayerSearchKey;
    private static KeyMapping copyTagKey;
    private static KeyMapping openSettingsKey;

    private static final KeyMapping.Category CATEGORY =
            KeyMapping.Category.register(Identifier.fromNamespaceAndPath(GTLTaggerMod.MOD_ID, "main"));

    private static final Map<String, KeyMapping> BINDINGS = new HashMap<>();

    /** The registered keybinding for a translation key like "key.gtltagger.copy_tag", or null. */
    public static KeyMapping binding(String id) {
        return BINDINGS.get(id);
    }

    @Override
    public void onInitializeClient() {
        TierDatabase.load();
        GTLTaggerConfig config = GTLTaggerConfig.get();

        registerKeyBindings();
        registerTickHandler(config);
        registerHud();
        registerCommands();
    }

    private static KeyMapping register(String id, int defaultKey) {
        KeyMapping mapping = KeyMappingHelper.registerKeyMapping(Mc.newKey(id, defaultKey, CATEGORY));
        BINDINGS.put(id, mapping);
        return mapping;
    }

    private void registerKeyBindings() {
        int unbound = InputConstants.UNKNOWN.getValue();
        cycleLeftGamemodeKey = register("key.gtltagger.cycle_left_gamemode", InputConstants.KEY_G);
        cycleRightGamemodeKey = register("key.gtltagger.cycle_right_gamemode", InputConstants.KEY_K);
        openPlayerSearchKey = register("key.gtltagger.open_player_search", InputConstants.KEY_H);
        copyTagKey = register("key.gtltagger.copy_tag", unbound);
        openSettingsKey = register("key.gtltagger.open_settings", unbound);
    }

    private void registerTickHandler(GTLTaggerConfig config) {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (cycleLeftGamemodeKey.consumeClick()) {
                config.gamemode1 = Gamemodes.next(config.gamemode1);
                config.save();
                feedback(client, "Left gamemode: " + config.gamemode1);
            }

            while (cycleRightGamemodeKey.consumeClick()) {
                config.gamemode2 = Gamemodes.next(config.gamemode2);
                config.save();
                feedback(client, "Right gamemode: " + config.gamemode2);
            }

            while (openPlayerSearchKey.consumeClick()) {
                if (Mc.currentScreen(client) == null) {
                    Mc.openScreen(client, new PlayerSearchScreen(null));
                }
            }

            while (copyTagKey.consumeClick()) {
                copyMyTagToClipboard(client, config);
            }

            while (openSettingsKey.consumeClick()) {
                if (Mc.currentScreen(client) == null) {
                    Mc.openScreen(client, new GTLTaggerSettingsScreen(null, config));
                }
            }
        });
    }

    private void registerHud() {
        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath(GTLTaggerMod.MOD_ID, "tier_hud"),
                (graphics, deltaTracker) -> {
                    GTLTaggerConfig config = GTLTaggerConfig.get();
                    if (config.hudEnabled) {
                        GTLTaggerHud.render(graphics, config);
                    }
                });
    }

    private void registerCommands() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, buildContext) -> dispatcher.register(
                ClientCommands.literal("gtltagger")
                        .executes(ctx -> {
                            Minecraft mc = Minecraft.getInstance();
                            Mc.openScreen(mc, new GTLTaggerSettingsScreen(null, GTLTaggerConfig.get()));
                            return 1;
                        })
                        .then(ClientCommands.literal("settings").executes(ctx -> {
                            Minecraft mc = Minecraft.getInstance();
                            Mc.openScreen(mc, new GTLTaggerSettingsScreen(null, GTLTaggerConfig.get()));
                            return 1;
                        }))
                        .then(ClientCommands.literal("reload").executes(ctx -> {
                            feedback(Minecraft.getInstance(), "Refreshing tier data from GlobalTierlist...");
                            TierDatabase.reload(success -> Minecraft.getInstance().execute(() ->
                                    feedback(Minecraft.getInstance(), success
                                            ? "Tier data refreshed"
                                            : "Refresh failed, see log (kept previous data)")));
                            return 1;
                        }))
                        .then(ClientCommands.literal("tag").executes(ctx -> {
                            copyMyTagToClipboard(Minecraft.getInstance(), GTLTaggerConfig.get());
                            return 1;
                        }))
        ));
    }

    private void copyMyTagToClipboard(Minecraft client, GTLTaggerConfig config) {
        if (client.player == null) return;

        String ign = client.player.getName().getString();
        String gamemode = config.automaticDetectionEnabled ? KitDetector.detect() : null;
        if (gamemode == null) {
            gamemode = config.gamemode1;
        }

        TierEntry entry = TierDatabase.get(ign, gamemode);
        String tag = TierTagGenerator.generate(ign, gamemode, entry != null ? entry.tier : null,
                config.leftSurface.enabled(), config.rightSurface.enabled());

        client.keyboardHandler.setClipboard(tag);
        feedback(client, "Copied: " + tag);
    }

    private void feedback(Minecraft client, String message) {
        Mc.overlayMessage(client, Component.literal("[GTLTagger] " + message));
    }
}
