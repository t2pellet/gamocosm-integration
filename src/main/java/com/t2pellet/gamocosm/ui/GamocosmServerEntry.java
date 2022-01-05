package com.t2pellet.gamocosm.ui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.t2pellet.gamocosm.network.GamocosmServer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;

import java.awt.*;

@Environment(EnvType.CLIENT)
public class GamocosmServerEntry extends MultiplayerServerListWidget.Entry {

    final MinecraftClient client;
    protected final GamocosmServer server;
    private final MultiplayerScreen screen;
    private long time;

    public GamocosmServerEntry(MultiplayerScreen screen, GamocosmServer server) {
        this.screen = screen;
        this.server = server;
        this.client = MinecraftClient.getInstance();
    }

    public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
        // bg, shamelessly stolen from EntryListWidget::renderList
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionShader);
        float f = 0.25F;
        RenderSystem.setShaderColor(f, f, f, 0.8F);
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
        bufferBuilder.vertex(x, y + entryHeight, 0.0D).next();
        bufferBuilder.vertex(x + entryWidth - 5, y + entryHeight, 0.0D).next();
        bufferBuilder.vertex(x + entryWidth - 5, y, 0.0D).next();
        bufferBuilder.vertex(x, y, 0.0D).next();
        tessellator.draw();
        RenderSystem.setShaderColor(0.05F, 0.05F, 0.05F, 0.8F);
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
        bufferBuilder.vertex(x + 1, y + entryHeight - 1, 0.0D).next();
        bufferBuilder.vertex(x + entryWidth - 6, y + entryHeight - 1, 0.0D).next();
        bufferBuilder.vertex(x + entryWidth - 6, y + 1, 0.0D).next();
        bufferBuilder.vertex(x + 1, y + 1, 0.0D).next();
        tessellator.draw();
        RenderSystem.disableBlend();
        RenderSystem.enableTexture();

        // Title
        var title = Text.of(Formatting.BOLD + this.server.getName());
        DrawableHelper.drawCenteredText(matrices, this.client.textRenderer, title, this.screen.width / 2, y + 5, 16777215);
        // Loading
        if (getServer().isUpdatingStatus()) {
            String string = switch ((int) (Util.getMeasuringTimeMs() / 300L % 4L)) {
                case 1, 3 -> "o O o";
                case 2 -> "o o O";
                default -> "O o o";
            };
            DrawableHelper.drawCenteredText(matrices, this.client.textRenderer, string, this.screen.width / 2, y + 18, 8421504);
        } // Address
        else {
            int colourIdx;
            switch (server.getStatus()) {
                case OFF -> colourIdx = 0xc;
                case HOSTED -> colourIdx = 0xe;
                default -> colourIdx = 0xa;
            }
            var address = Text.of(Formatting.byColorIndex(colourIdx) + this.server.getAddress());
            DrawableHelper.drawCenteredText(matrices, this.client.textRenderer, address, this.screen.width / 2, y + 18, 16777215);
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.screen.select(this);
        if (Util.getMeasuringTimeMs() - this.time < 250L) {
            this.screen.connect();
        }

        this.time = Util.getMeasuringTimeMs();
        return false;
    }

    public GamocosmServer getServer() {
        return this.server;
    }

    public Text getNarration() {
        return new TranslatableText("narrator.select", (new LiteralText("")).append(server.getName()));
    }
}

