package com.t2pellet.gamocosm.ui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;

@Environment(EnvType.CLIENT)
public class GamocosmServerEntry extends MultiplayerServerListWidget.Entry {

    private final MultiplayerScreen screen;
    protected final MinecraftClient client;
    protected final GamocosmServerInfo server;
    private long time;

    public GamocosmServerEntry(MultiplayerScreen screen, GamocosmServerInfo server) {
        this.screen = screen;
        this.server = server;
        this.client = MinecraftClient.getInstance();
    }

    public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
        var title = Text.of(Formatting.BOLD + this.server.getName());
        DrawableHelper.drawCenteredText(matrices, this.client.textRenderer, title, this.screen.width / 2, y + 5, 16777215);
        DrawableHelper.drawCenteredText(matrices, this.client.textRenderer, Text.of(this.server.getAddress()), this.screen.width / 2, y + 20, 16777215);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.screen.select(this);
        if (Util.getMeasuringTimeMs() - this.time < 250L) {
            this.screen.connect();
        }

        this.time = Util.getMeasuringTimeMs();
        return false;
    }

    public GamocosmServerInfo getServerEntry() {
        return this.server;
    }

    public Text getNarration() {
        return new TranslatableText("narrator.select", (new LiteralText("")).append(server.getName()));
    }
}

