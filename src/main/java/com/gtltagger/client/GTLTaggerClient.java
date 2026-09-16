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
 * for "generate my tag", only for switching gamemode and toggling
 * left/right. This mod treats the TierTag as being about the LOCAL
 * player's own tier (the thing you paste in chat when someone asks
 * "what's your tier?"), generated for whichever gamemode is currently
 * active (detected kit, falling back to gamemode1). It's available two
 * ways: an optional "Copy My TierTag" keybind (unbound by default —
 * bind it in Controls if you want one) and the "/gtltagger tag"
 * command. The TAB display is the separate, already-fully-specified
 * feature that shows OTHER players' tested tiers next to their name.
 */
public class GTLTaggerClient implements ClientModInitializer {

    private static KeyBinding switchGamemodeKey;
    private static KeyBinding toggleLeftRightKey;
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
        switchGamemodeKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.gtltagger.switch_gamemode",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_H, // spec section 3 default
                "key.categories.gtltagger"
        ));

        toggleLeftRightKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.gtltagger.toggle_left_right",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_J, // no default specified in spec; chosen to avoid common conflicts
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
            while (switchGamemodeKey.wasPressed()) {
                config.gamemode1 = Gamemodes.next(config.gamemode1);
                config.save();
                feedback(client, "Gamemode: " + config.gamemode1);
            }

            while (toggleLeftRightKey.wasPressed()) {
                config.tierMode = config.tierMode.next();
                config.save();
                feedback(client, "Tier display: " + config.tierMode.label);
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
        String tag = TierTagGenerator.generate(ign, entry != null ? entry.tier : null, config.tierMode);

        client.keyboard.setClipboard(tag);
        feedback(client, "Copied: " + tag);
    }

    private void feedback(MinecraftClient client, String message) {
        if (client.player != null) {
            client.player.sendMessage(Text.literal("[GTLTagger] " + message), true);
        }
    }
}
