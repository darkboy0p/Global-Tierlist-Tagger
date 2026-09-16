package com.gtltagger.client.hud;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

/**
 * Version-specific half of {@link GTLTaggerHud}'s icon drawing.
 *
 * On Minecraft 1.21 / 1.21.1, {@code DrawContext.drawTexture} has no
 * RenderLayer/RenderPipeline selector argument yet - it's called
 * directly with the Identifier:
 * drawTexture(Identifier, x, y, u, v, width, height, regionWidth,
 * regionHeight, textureWidth, textureHeight).
 *
 * This file is only added to the compile classpath for the 1.21/1.21.1
 * matrix legs (see the sourceSets block in build.gradle) - the
 * 1.21.2-1.21.5 and 1.21.6+ variants live alongside it in sibling
 * java_* directories and are never compiled together.
 */
final class HudIconRenderer {

    private HudIconRenderer() {
    }

    static void drawIcon(DrawContext context, Identifier texture, int x, int y, int size, int sourcePx) {
        context.drawTexture(texture, x, y, 0, 0, size, size, sourcePx, sourcePx, sourcePx, sourcePx);
    }
}
