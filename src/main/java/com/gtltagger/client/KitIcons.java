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
 * These textures are drawn on-screen by {@link com.gtltagger.client.hud.GTLTaggerHud}
 * (the HUD's kit lines) via {@code DrawContext.drawTexture(...)}. That
 * method's shape changed twice within the 1.21.1-1.21.7 range this
 * repo's CI matrix builds against (plain Identifier in 1.21/1.21.1,
 * Function&lt;Identifier,RenderLayer&gt; from 1.21.2, RenderPipeline from
 * 1.21.6) — see {@code com.gtltagger.client.hud.HudIconRenderer}, which
 * has one implementation per bucket in a sibling src/main/java_* dir,
 * with build.gradle adding the right one to the classpath per matrix leg.
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
