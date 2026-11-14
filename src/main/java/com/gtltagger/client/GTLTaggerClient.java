package com.gtltagger.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import com.gtltagger.GTLTaggerMod;
import com.gtltagger.client.gui.GTLTaggerSettingsScreen;
import com.gtltagger.client.gui.PlayerSearchScreen;
import com.gtltagger.client.hud.GTLTaggerHud;
import com.gtltagger.config.GTLTaggerConfig;
import com.gtltagger.data.TierDatabase;
import com.gtltagger.data.TierEntry;
import com.gtltagger.detect.KitDetector;
import com.gtltagger.gamemode.Gamemodes;
import com.gtltagger.tag.TierTagGenerator;

/**
 * Client entrypoint. GTLTagger is a client-only mod, so all logic
 * (config, HUD, keybinds, commands) is wired up here.
 *
 * Design note / assumption: the spec doesn't name an explicit keybind
 * for "generate my tag", only for switching gamemode. This mod treats
 * the TierTag as being about the LOCAL player's own tier (the thing
 * you paste in chat when someone asks "what's your tier?"), generated
 * for whichever gamemode is currently active (detected kit, falling
 * back to gamemode1). It's available two ways: an optional "Copy My
 * TierTag" keybind (unbound by default — bind it in Controls if you
 * want one) and the "/gtltagger tag" command. The TAB display is the
 * separate, already-fully-specified feature that shows OTHER players'
 * tested tiers next to their name.
 *
 * Left and right now each cycle their own gamemode independently
 * (cycleLeftGamemodeKey / cycleRightGamemodeKey) - which surface(s)
 * each side shows on is a settings-screen choice (DisplaySurface),
 * not a keybind, since a 4-state cycle per side would need its own
 * pair of keys anyway. H, previously "switch gamemode", now opens the
 * player-search screen instead (see PlayerSearchScreen) - that's a
 * more frequent action than gamemode switching for most sessions.
 */
public class GTLTaggerClient implements ClientModInitializer {

    private static KeyBinding cycleLeftGamemodeKey;
    private static KeyBinding cycleRightGamemodeKey;
    private static KeyBinding openPlayerSearchKey;
    private static KeyBinding copyTagKey;
    private static KeyBinding openSettingsKey;

    @Override
    public void onInitializeClient() {
        TierDatabase.load();
        GTLTaggerConfig config = GTLTaggerConfig.get();

        registerKeyBindings();
        registerTickHandler(config);
        registerHud();
        registerCommands();
    }

    private void registerKeyBindings() {
        cycleLeftGamemodeKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.gtltagger.cycle_left_gamemode",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                "key.categories.gtltagger"
        ));

        cycleRightGamemodeKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.gtltagger.cycle_right_gamemode",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_K,
                "key.categories.gtltagger"
        ));

        openPlayerSearchKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.gtltagger.open_player_search",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_H, // spec section 3 default - repurposed from "switch gamemode"
                "key.categories.gtltagger"
        ));

        copyTagKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.gtltagger.copy_tag",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN, // unbound by default, bind it in Controls if wanted
                "key.categories.gtltagger"
        ));

        openSettingsKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.gtltagger.open_settings",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                "key.categories.gtltagger"
        ));
    }

    private void registerTickHandler(GTLTaggerConfig config) {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (cycleLeftGamemodeKey.wasPressed()) {
                config.gamemode1 = Gamemodes.next(config.gamemode1);
                config.save();
                feedback(client, "Left gamemode: " + config.gamemode1);
            }

            while (cycleRightGamemodeKey.wasPressed()) {
                config.gamemode2 = Gamemodes.next(config.gamemode2);
                config.save();
                feedback(client, "Right gamemode: " + config.gamemode2);
            }

            while (openPlayerSearchKey.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new PlayerSearchScreen(null));
                }
            }

            while (copyTagKey.wasPressed()) {
                copyMyTagToClipboard(client, config);
            }

            while (openSettingsKey.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new GTLTaggerSettingsScreen(null, config));
                }
            }
        });
    }

    private void registerHud() {
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            GTLTaggerConfig config = GTLTaggerConfig.get();
            if (config.hudEnabled) {
                GTLTaggerHud.render(drawContext, config);
            }
        });
    }

    private void registerCommands() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(
                ClientCommandManager.literal("gtltagger")
                        .executes(ctx -> {
                            MinecraftClient.getInstance().setScreen(
                                    new GTLTaggerSettingsScreen(null, GTLTaggerConfig.get()));
                            return 1;
                        })
                        .then(ClientCommandManager.literal("settings").executes(ctx -> {
                            MinecraftClient.getInstance().setScreen(
                                    new GTLTaggerSettingsScreen(null, GTLTaggerConfig.get()));
                            return 1;
                        }))
                        .then(ClientCommandManager.literal("reload").executes(ctx -> {
                            feedback(MinecraftClient.getInstance(), "Refreshing tier data from GlobalTierlist...");
                            TierDatabase.reload(success -> MinecraftClient.getInstance().execute(() ->
                                    feedback(MinecraftClient.getInstance(), success
                                            ? "Tier data refreshed"
                                            : "Refresh failed, see log (kept previous data)")));
                            return 1;
                        }))
                        .then(ClientCommandManager.literal("tag").executes(ctx -> {
                            copyMyTagToClipboard(MinecraftClient.getInstance(), GTLTaggerConfig.get());
                            return 1;
                        }))
        ));
    }

    private void copyMyTagToClipboard(MinecraftClient client, GTLTaggerConfig config) {
        if (client.player == null) return;

        String ign = client.player.getGameProfile().getName();
        String gamemode = config.automaticDetectionEnabled ? KitDetector.detect() : null;
        if (gamemode == null) {
            gamemode = config.gamemode1;
        }

        TierEntry entry = TierDatabase.get(ign, gamemode);
        String tag = TierTagGenerator.generate(ign, gamemode, entry != null ? entry.tier : null,
                config.leftSurface.enabled(), config.rightSurface.enabled());

        client.keyboard.setClipboard(tag);
        feedback(client, "Copied: " + tag);
    }

    private void feedback(MinecraftClient client, String message) {
        if (client.player != null) {
            client.player.sendMessage(Text.literal("[GTLTagger] " + message), true);
        }
    }
}
