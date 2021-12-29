//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.t2pellet.gamocosm;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import com.t2pellet.gamocosm.rest.GamocosmServer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConnectScreen;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.Address;
import net.minecraft.client.network.AllowedAddressResolver;
import net.minecraft.client.network.ClientLoginNetworkHandler;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkState;
import net.minecraft.network.packet.c2s.handshake.HandshakeC2SPacket;
import net.minecraft.network.packet.c2s.login.LoginHelloC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Util;
import net.minecraft.util.logging.UncaughtExceptionLogger;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import javax.net.SocketFactory;

// Same as ConnectScreen, except for gamocosm instance
// For gamocosm instance, if its off, waits for it to turn on with visual indication

@Environment(EnvType.CLIENT)
public class GamocosmScreen extends Screen {
    private static final AtomicInteger CONNECTOR_THREADS_COUNT = new AtomicInteger(0);
    static final Logger LOGGER = LogManager.getLogger();
    private static final long NARRATOR_INTERVAL = 2000L;
    public static final Text BLOCKED_HOST_TEXT = new TranslatableText("disconnect.genericReason", new Object[]{new TranslatableText("disconnect.unknownHost")});
    @Nullable
    volatile ClientConnection connection;
    volatile boolean connectingCancelled;
    final Screen parent;
    private Text status = new TranslatableText("gamocosm.connect.checking");
    private long lastNarrationTime = -1L;

    public GamocosmScreen(Screen parent) {
        super(NarratorManager.EMPTY);
        this.parent = parent;
    }

    public static void connect(Screen screen, MinecraftClient client, ServerAddress address, @Nullable ServerInfo info) {
        try {
            var serverStatus = GamocosmServer.get();
            if (address.getAddress().equals(serverStatus.address)) {
                GamocosmScreen connectScreen = new GamocosmScreen(screen);
                client.disconnect();
                client.loadBlockList();
                client.setCurrentServerEntry(info);
                client.setScreen(connectScreen);
                connectScreen.connect(client, address);
            } else {
                ConnectScreen.connect(screen, client, address, info);
            }
        } catch (Exception ex) {
            ConnectScreen.connect(screen, client, address, info);
        }
    }

    private void connect(MinecraftClient client, ServerAddress address) {
        LOGGER.info("Connecting to {}, {}", address.getAddress(), address.getPort());
        Thread thread = new Thread("Server Connector #" + CONNECTOR_THREADS_COUNT.incrementAndGet()) {
            public void run() {
                InetSocketAddress inetSocketAddress = null;
                try {
                    if (GamocosmScreen.this.connectingCancelled) {
                        return;
                    }

                    // Start host if off
                    var server = GamocosmServer.get();
                    server.startHost();
                    if (server.getStatus(false) == GamocosmServer.Status.OFF) {
                        status = new TranslatableText("gamocosm.connect.hosting");
                    }

                    if (GamocosmScreen.this.connectingCancelled) {
                        return;
                    }

                    // Wait for host to turn on
                    while (server.getStatus(false) == GamocosmServer.Status.OFF) {
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
                    while (server.getStatus(false) != GamocosmServer.Status.ON) {
                        server.updateStatus();
                    }

                    if (GamocosmScreen.this.connectingCancelled) {
                        return;
                    }

                    //Connect
                    MinecraftClient.getInstance().execute(() -> {
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
                        client.setScreen(new DisconnectedScreen(GamocosmScreen.this.parent, ScreenTexts.CONNECT_FAILED, new TranslatableText("disconnect.genericReason", new Object[]{exception})));
                    });
                }

            }
        };
        thread.setUncaughtExceptionHandler(new UncaughtExceptionLogger(LOGGER));
        thread.start();
    }

    private void setStatus(Text status) {
        this.status = status;
    }

    public void tick() {
        if (this.connection != null) {
            if (this.connection.isOpen()) {
                this.connection.tick();
            } else {
                this.connection.handleDisconnection();
            }
        }

    }

    public boolean shouldCloseOnEsc() {
        return false;
    }

    protected void init() {
        this.addDrawableChild(new ButtonWidget(this.width / 2 - 100, this.height / 4 + 120 + 12, 200, 20, ScreenTexts.CANCEL, (button) -> {
            this.connectingCancelled = true;
            if (this.connection != null) {
                this.connection.disconnect(new TranslatableText("connect.aborted"));
            }

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
        super.render(matrices, mouseX, mouseY, delta);
    }
}
