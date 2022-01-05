package com.t2pellet.gamocosm.ui;

import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import com.t2pellet.gamocosm.network.GamocosmServer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.ConnectScreen;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Util;
import net.minecraft.util.logging.UncaughtExceptionLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

// Same as ConnectScreen, except for gamocosm instance
// For gamocosm instance, if its off, waits for it to turn on with visual indication

@Environment(EnvType.CLIENT)
public class GamocosmScreen extends Screen {
    private static final AtomicInteger CONNECTOR_THREADS_COUNT = new AtomicInteger(0);
    static final Logger LOGGER = LogManager.getLogger();
    volatile boolean connectingCancelled;
    final Screen parent;
    private Text status = new TranslatableText("gamocosm.connect.checking");
    private long lastNarrationTime = -1L;

    public GamocosmScreen(Screen parent) {
        super(NarratorManager.EMPTY);
        this.parent = parent;
    }

    public static void connect(Screen screen, MinecraftClient client, GamocosmServer server) {
        try {
            GamocosmScreen connectScreen = new GamocosmScreen(screen);
            client.disconnect();
            client.loadBlockList();
            client.setCurrentServerEntry(new ServerInfo(server.getName(), server.getAddress(), false));
            client.setScreen(connectScreen);
            connectScreen.connect(client, server);
        } catch (Exception ex) {
            LOGGER.error("Error connecting to gamocosm server: " + ex.getMessage());
        }
    }

    private void connect(MinecraftClient client, GamocosmServer server) {
        ServerAddress address = ServerAddress.parse(server.getAddress());
        LOGGER.info("Connecting to {}, {}", address.getAddress(), address.getPort());
        Thread thread = new Thread("Server Connector #" + CONNECTOR_THREADS_COUNT.incrementAndGet()) {
            public void run() {
                try {
                    if (GamocosmScreen.this.connectingCancelled) {
                        return;
                    }

                    // Start host if off
                    server.startHost();
                    if (server.getStatus() == GamocosmServer.Status.OFF) {
                        status = new TranslatableText("gamocosm.connect.hosting");
                    }

                    if (GamocosmScreen.this.connectingCancelled) {
                        return;
                    }

                    // Wait for host to turn on
                    while (server.getStatus() == GamocosmServer.Status.OFF) {
                        Thread.sleep(1000);
                        if (GamocosmScreen.this.connectingCancelled) {
                            return;
                        }
                        server.updateStatus();
                    }

                    if (GamocosmScreen.this.connectingCancelled) {
                        return;
                    }

                    // Start server if off
                    server.startGame();

                    // Wait for server start
                    status = new TranslatableText("gamocosm.connect.starting");
                    while (server.getStatus() != GamocosmServer.Status.ON) {
                        server.updateStatus();
                    }

                    if (GamocosmScreen.this.connectingCancelled) {
                        return;
                    }

                    //Connect
                    client.execute(() -> {
                        ConnectScreen.connect(parent, MinecraftClient.getInstance(), address, MinecraftClient.getInstance().getCurrentServerEntry());
                    });
                } catch (Exception var6) {
                    if (GamocosmScreen.this.connectingCancelled) {
                        return;
                    }

                    Throwable var5 = var6.getCause();
                    Exception exception2;
                    if (var5 instanceof Exception) {
                        Exception exceptionx = (Exception)var5;
                        exception2 = exceptionx;
                    } else {
                        exception2 = var6;
                    }

                    GamocosmScreen.LOGGER.error("Couldn't connect to server", var6);
                    String exception = exception2.getMessage();
                    client.execute(() -> {
                        client.setScreen(new DisconnectedScreen(GamocosmScreen.this.parent, ScreenTexts.CONNECT_FAILED, new TranslatableText("disconnect.genericReason", exception)));
                    });
                }

            }
        };
        thread.setUncaughtExceptionHandler(new UncaughtExceptionLogger(LOGGER));
        thread.start();
    }

    public boolean shouldCloseOnEsc() {
        return false;
    }

    protected void init() {
        this.addDrawableChild(new ButtonWidget(this.width / 2 - 100, this.height / 4 + 120 + 12, 200, 20, ScreenTexts.CANCEL, (button) -> {
            this.connectingCancelled = true;
            this.client.setScreen(this.parent);
        }));
    }

    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        long l = Util.getMeasuringTimeMs();
        if (l - this.lastNarrationTime > 2000L) {
            this.lastNarrationTime = l;
            NarratorManager.INSTANCE.narrate(new TranslatableText("narrator.joining"));
        }

        drawCenteredText(matrices, this.textRenderer, this.status, this.width / 2, this.height / 2 - 50, 16777215);

        // Loading anim
        String string = switch ((int) (Util.getMeasuringTimeMs() / 300L % 4L)) {
            case 1, 3 -> "o O o";
            case 2 -> "o o O";
            default -> "O o o";
        };
        drawCenteredText(matrices, this.textRenderer, string, this.width / 2, this.height / 2, 8421504);

        super.render(matrices, mouseX, mouseY, delta);
    }
}
