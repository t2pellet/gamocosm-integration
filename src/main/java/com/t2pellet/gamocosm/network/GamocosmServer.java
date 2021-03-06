package com.t2pellet.gamocosm.network;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.t2pellet.gamocosm.Gamocosm;
import net.minecraft.client.network.Address;
import net.minecraft.client.network.AllowedAddressResolver;
import net.minecraft.client.network.ServerAddress;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import javax.net.SocketFactory;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public class GamocosmServer {

    public enum Status {
        OFF,
        HOSTED,
        ON
    }

    public final String address;
    public final String name;
    private boolean gameStarted;
    private boolean canPing;
    private AtomicBoolean isUpdatingStatus = new AtomicBoolean(false);

    private static GamocosmServer instance;

    public static GamocosmServer get() throws IOException {
        // Setup instance
        if (instance == null) {
            var json = getJson();
            var address = json.get("domain").getAsString();
            var name = Gamocosm.CONFIG.name;
            instance = new GamocosmServer(address, name);
        }
        // Update status
        Thread thread = new Thread("Gamocosm Status Update") {
            @Override
            public void run() {
                try {
                    instance.updateStatus();
                } catch (IOException ex) {
                    Gamocosm.LOGGER.error("Failed to update gamocosm server status");
                }
            }
        };
        thread.start();
        return instance;
    }

    private static JsonObject getJson() throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet get = new HttpGet(Gamocosm.CONFIG.getURL() + "status");
        var response = client.execute(get);
        var entity = response.getEntity();
        var string = EntityUtils.toString(entity);
        response.close();
        client.close();
        return JsonParser.parseString(string).getAsJsonObject();
    }

    private static boolean getCanPing(String address) {
        try {
            Socket socket = SocketFactory.getDefault().createSocket();
            Optional<InetSocketAddress> optional = AllowedAddressResolver.DEFAULT
                    .resolve(ServerAddress.parse(address))
                    .map(Address::getInetSocketAddress);
            socket.connect(optional.get(), 2000);
            socket.close();
            return true;
        } catch (Exception ignore) {
            return false;
        }
    }

    GamocosmServer(String address, String name) {
        this.address = address;
        this.name = name;
        this.gameStarted = false;
        this.canPing = false;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public Status getStatus() {
        return gameStarted ? (canPing ? Status.ON : Status.HOSTED) : Status.OFF;
    }

    public boolean isUpdatingStatus() {
        return isUpdatingStatus.get();
    }

    public Status getUpdatedStatus() throws IOException {
        updateStatus();
        return getStatus();
    }

    public void updateStatus() throws IOException {
        if (isUpdatingStatus.get()) return;
        isUpdatingStatus.set(true);
        var json = GamocosmServer.getJson();
        gameStarted = json.get("server").getAsBoolean() && json.get("status").isJsonNull();
        canPing = gameStarted && getCanPing(address);
        isUpdatingStatus.set(false);
    }

    public void startHost() throws IOException {
      if (getUpdatedStatus() == Status.OFF) {
          var client = HttpClients.createDefault();
          var post = new HttpPost(Gamocosm.CONFIG.getURL() + "start");
          client.execute(post).close();
          client.close();
      }
    }

    public void startGame() throws IOException {
        var status = getUpdatedStatus();
        if (status == Status.ON) return;

        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost post;
        if (status == Status.HOSTED) {
            post = new HttpPost(Gamocosm.CONFIG.getURL() + "resume");
        } else  {
            post = new HttpPost(Gamocosm.CONFIG.getURL() + "start");
        }
        client.execute(post).close();
        client.close();
    }

}
