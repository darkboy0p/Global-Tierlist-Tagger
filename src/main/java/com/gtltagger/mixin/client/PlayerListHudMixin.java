package com.gtltagger.mixin.client;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.gtltagger.config.LeftRightMode;
import com.gtltagger.config.GTLTaggerConfig;
import com.gtltagger.data.TierDatabase;
import com.gtltagger.data.TierEntry;
import com.gtltagger.detect.KitDetector;

import java.util.Map;

/**
 * Appends a TierTag to each name shown in the TAB player list
 * (spec section 5). Uses the player's actual tested gamemode data —
 * NOT whatever gamemode the user currently has selected — per the
 * spec's "important logic" note.
 */
@Mixin(PlayerListHud.class)
public abstract class PlayerListHudMixin {

    @Inject(method = "getPlayerName", at = @At("RETURN"), cancellable = true)
    private void gtltagger$appendTierTag(PlayerListEntry entry, CallbackInfoReturnable<Text> cir) {
        GTLTaggerConfig config = GTLTaggerConfig.get();
        if (!config.tabTiersEnabled) {
            return;
        }

        GameProfile profile = entry.getProfile();
        if (profile == null || profile.getName() == null) {
            return;
        }

        TierEntry tierEntry = resolveTierForTab(profile.getName(), config);
        if (tierEntry == null || tierEntry.tier == null) {
            return; // never tested — spec: do not invent a tier
        }

        LeftRightMode mode = config.tierMode;
        if (!mode.left && !mode.right) {
            return;
        }

        MutableText icon = Text.literal("[:icon:" + tierEntry.tier + "]").formatted(Formatting.GRAY);
        MutableText result = Text.empty();
        if (mode.left) {
            result.append(icon.copy()).append(Text.literal(" "));
        }
        result.append(cir.getReturnValue());
        if (mode.right) {
            result.append(Text.literal(" ")).append(icon.copy());
        }
        cir.setReturnValue(result);
    }

    /**
     * Priority: currently detected kit (if enabled + tested) -> gamemode1
     * (if tested) -> gamemode2 (if enabled + tested) -> any other tested
     * gamemode. Returns null if the player has never been tested at all.
     */
    private static TierEntry resolveTierForTab(String ign, GTLTaggerConfig config) {
        Map<String, TierEntry> tested = TierDatabase.getAll(ign);
        if (tested.isEmpty()) {
            return null;
        }

        if (config.automaticDetectionEnabled) {
            String detected = KitDetector.detect();
            if (detected != null && tested.containsKey(detected)) {
                return tested.get(detected);
            }
        }

        if (tested.containsKey(config.gamemode1)) {
            return tested.get(config.gamemode1);
        }
        if (config.secondGamemodeEnabled && tested.containsKey(config.gamemode2)) {
            return tested.get(config.gamemode2);
        }

        return tested.values().iterator().next();
    }
}
