package com.gtltagger.client.hud;

import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

/**
 * Version-specific half of {@link GTLTaggerHud}'s icon drawing.
 *
 * On Minecraft 1.21.6+, {@code DrawContext.drawTexture} picks its
 * RenderPipeline explicitly - {@code RenderPipelines.GUI_TEXTURED} for
 * a plain textured quad:
 * drawTexture(RenderPipeline, Identifier, x, y, u, v, width, height,
 * regionWidth, regionHeight, textureWidth, textureHeight).
 *
 * This file is only added to the compile classpath for the 1.21.6+
 * matrix legs (see the sourceSets block in build.gradle) - the
 * 1.21/1.21.1 and 1.21.2-1.21.5 variants live alongside it in sibling
 * java_* directories and are never compiled together. Also used as
 * the fallback for any future Minecraft version outside the declared
 * 1.21.1-1.21.7 range, on the assumption that newer versions keep this
 * shape rather than reverting to an older one.
 */
final class HudIconRenderer {

    private HudIconRenderer() {
    }

    static void drawIcon(DrawContext context, Identifier texture, int x, int y, int size, int sourcePx) {
        context.drawTexture(RenderPipelines.GUI_TEXTURED, texture, x, y, 0, 0, size, size, sourcePx, sourcePx, sourcePx, sourcePx);
    }
}
