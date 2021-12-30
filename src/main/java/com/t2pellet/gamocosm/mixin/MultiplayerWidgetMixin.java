package com.t2pellet.gamocosm.mixin;

import com.google.common.collect.Lists;
import com.t2pellet.gamocosm.Gamocosm;
import com.t2pellet.gamocosm.ui.GamocosmServerEntry;
import com.t2pellet.gamocosm.ui.GamocosmServerInfo;
import com.t2pellet.gamocosm.ui.GamocosmWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;
import java.util.List;

@Mixin(MultiplayerServerListWidget.class)
public class MultiplayerWidgetMixin extends AlwaysSelectedEntryListWidget<MultiplayerServerListWidget.Entry> implements GamocosmWidget {

    @Unique
    private final List<GamocosmServerEntry> gamocosmServers = Lists.newArrayList();

    @Shadow
    @Final
    private MultiplayerScreen screen;

    @Shadow
    private void updateEntries() {
    }

    public MultiplayerWidgetMixin(MinecraftClient minecraftClient, int i, int j, int k, int l, int m) {
        super(minecraftClient, i, j, k, l, m);
    }

    @Override
    public void setGamocosmServers(List<GamocosmServerInfo> gamocosmServers) {
        this.gamocosmServers.clear();
        Iterator<GamocosmServerInfo> iterator = gamocosmServers.iterator();
        while (iterator.hasNext()) {
            GamocosmServerInfo gamocosmServerInfo = iterator.next();
            this.gamocosmServers.add(new GamocosmServerEntry(this.screen, gamocosmServerInfo));
        }

        this.updateEntries();
    }

    @Inject(at = @At(value = "INVOKE", target = "Ljava/util/List;forEach(Ljava/util/function/Consumer;)V", ordinal = 0), method = "updateEntries")
    private void updateEntriesMixin(CallbackInfo info) {
        this.gamocosmServers.forEach(gamocosmServer -> {
            this.addEntry(gamocosmServer);
            Gamocosm.LOGGER.info("Added gamocosm server entry: " + gamocosmServer.getServerEntry().getName());
        });
    }


}
