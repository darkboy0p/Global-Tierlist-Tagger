package com.gtltagger.client;

import net.minecraft.util.Identifier;
import com.gtltagger.GTLTaggerMod;
import com.gtltagger.gamemode.Gamemodes;

import java.util.HashMap;
import java.util.Map;

/**
 * Maps each kit name (and "Overall") to the icon texture shipped at
 * assets/gtltagger/textures/icon/&lt;name&gt;.png.
 *
 * NOTE: this class only holds Identifier references — it does not draw
 * anything. Actually rendering these on-screen (in the HUD or the TAB
 * list) needs DrawContext.drawTexture(...), whose method signature has
 * changed twice within the 1.21.1–1.21.7 range this mod targets (plain
 * Identifier in 1.21.1, Function&lt;Identifier,RenderLayer&gt; from 1.21.2,
 * RenderPipeline from 1.21.6) — code compiled for one of those won't
 * run on a client using another, so wiring this up needs a decision on
 * which version(s) to support before it's added to the HUD/TAB mixin.
 */
public final class KitIcons {

    private static final Map<String, Identifier> ICONS = new HashMap<>();

    static {
        for (String kit : Gamemodes.ORDER) {
            register(kit);
        }
        register("Overall");
    }

    private KitIcons() {
    }

    private static void register(String kit) {
        String fileName = kit.equals("NethPot") ? "neth_pot" : kit.toLowerCase();
        ICONS.put(kit, Identifier.of(GTLTaggerMod.MOD_ID, "textures/icon/" + fileName + ".png"));
    }

    /** Returns the icon texture for {@code kit} (case-sensitive, matching {@link Gamemodes#ORDER} or "Overall"), or null if unknown. */
    public static Identifier get(String kit) {
        return ICONS.get(kit);
    }
}
