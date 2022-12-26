package com.t2pellet.gamocosm.mixin;

import com.mojang.logging.LogUtils;
import com.t2pellet.gamocosm.network.GamocosmServer;
import com.t2pellet.gamocosm.network.GamocosmServerPopulator;
import com.t2pellet.gamocosm.ui.GamocosmScreen;
import com.t2pellet.gamocosm.ui.GamocosmServerEntry;
import com.t2pellet.gamocosm.ui.GamocosmServerEntryList;
import com.t2pellet.gamocosm.ui.GamocosmWidget;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(MultiplayerScreen.class)
public class MultiplayerMixin extends Screen {

    @Final
    @Shadow
    private static final Logger LOGGER = LogUtils.getLogger();

    @Unique
    private GamocosmServerEntryList gamocosmServers;

    @Shadow
    protected MultiplayerServerListWidget serverListWidget;

    @Shadow
    @Final
    private Screen parent;

    protected MultiplayerMixin(Text title) {
        super(title);
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/multiplayer/MultiplayerServerListWidget;setServers(Lnet/minecraft/client/option/ServerList;)V"), method = "init")
    private void init(CallbackInfo info)
    {
        gamocosmServers = new GamocosmServerEntryList();
        try {
            var gamocosmServerDetector = new GamocosmServerPopulator(gamocosmServers);
            gamocosmServerDetector.start();
        } catch (Exception ex) {
            LOGGER.error("Unable to start Gamocosm server detection: {}", ex.getMessage());
        }
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/MultiplayerServerListPinger;tick()V"), method = "tick")
    private void tick(CallbackInfo info) {
        if (this.gamocosmServers.needsUpdate()) {
            List<GamocosmServer> list = this.gamocosmServers.getServers();
            this.gamocosmServers.markClean();
            ((GamocosmWidget) this.serverListWidget).setGamocosmServers(list);
        }
    }

    @Inject(at = @At("TAIL"), method = "connect()V")
    public void connect(CallbackInfo info) {
        MultiplayerServerListWidget.Entry entry = this.serverListWidget.getSelectedOrNull();
        if (entry instanceof GamocosmServerEntry gamocosmEntry) {
            GamocosmServer gamocosmServer = gamocosmEntry.getServer();
            GamocosmScreen.connect(this, client, gamocosmServer);
        }
    }

}
