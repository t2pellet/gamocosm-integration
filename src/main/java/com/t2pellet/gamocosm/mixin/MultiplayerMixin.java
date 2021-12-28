package com.t2pellet.gamocosm.mixin;

import com.t2pellet.gamocosm.Gamocosm;
import com.t2pellet.gamocosm.rest.GamocosmServer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.io.IOException;

@Mixin(MultiplayerScreen.class)
public class MultiplayerMixin extends Screen {

	protected MultiplayerMixin(Text title) {
		super(title);
	}

	@Shadow
	private void refresh() {
	}

	// Add to server list
	@Inject(at = @At("RETURN"), method = "init()V")
	private void init(CallbackInfo ci) {
		// Add first entry & save if not correct
		Thread thread = new Thread("Gamocosm Server Thread") {
			@Override
			public void run() {
				try {
					// Read file if exists, otherwise start with empty compound
					File serversFile = new File(MinecraftClient.getInstance().runDirectory, "servers.dat");
					var nbtCompound = NbtIo.read(serversFile);
					if (nbtCompound == null) nbtCompound = new NbtCompound();

					// Read list if exists, otherwise start with empty
					var nbtList = nbtCompound.getList("servers", 10);
					if (nbtList == null) nbtList = new NbtList();


					var server = GamocosmServer.get();
					NbtCompound info = new ServerInfo(server.name, server.address, false).toNbt();
					if (!nbtList.contains(info)) {
						Gamocosm.LOGGER.info("Gamocosm server was not in server list, adding it...");
						nbtList.add(0, info);
						nbtCompound.put("servers", nbtList);
						NbtIo.write(nbtCompound, serversFile);
						MinecraftClient.getInstance().execute(MultiplayerMixin.this::refresh);
					}
				} catch (IOException ex) {
					Gamocosm.LOGGER.error("Failed to add Gamocosm server to server list");
				}
			}
		};
		thread.start();
	}
}
