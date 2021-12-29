package com.t2pellet.gamocosm.rest;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.t2pellet.gamocosm.GamocosmConfig;
import net.minecraft.client.network.Address;
import net.minecraft.client.network.AllowedAddressResolver;
import net.minecraft.client.network.ServerAddress;
import org.apache.http.client.HttpClient;
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
    private boolean hasGotStatus;

    private static GamocosmServer instance;

    public static GamocosmServer get() {
        if (instance == null) {
            try {
                var json = getJson();
                var address = json.get("domain").getAsString();
                var name = GamocosmConfig.Core.getName();
                instance = new GamocosmServer(address, name);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return instance;
    }

    private static JsonObject getJson() throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet get = new HttpGet(GamocosmConfig.Core.getURL() + "status");
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
            socket.connect(optional.get(), 5000);
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
        this.hasGotStatus = false;
    }

    public Status getStatus() throws IOException {
        return getStatus(true);
    }

    public Status getStatus(boolean shouldUpdate) throws IOException {
        if (shouldUpdate || !hasGotStatus) {
            updateStatus();
        }
        return gameStarted ? (canPing ? Status.ON : Status.HOSTED) : Status.OFF;
    }

    public void updateStatus() throws IOException {
        var json = GamocosmServer.getJson();
        gameStarted = json.get("server").getAsBoolean();
        canPing = gameStarted && getCanPing(address);
        hasGotStatus = true;
    }

    public void startHost() throws IOException {
      if (getStatus() == Status.OFF) {
          var client = HttpClients.createDefault();
          var post = new HttpPost(GamocosmConfig.Core.getURL() + "start");
          client.execute(post).close();
          client.close();
      }
    }

    public void startGame() throws IOException {
        var status = getStatus();
        if (status == Status.ON) return;

        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost post;
        if (status == Status.HOSTED) {
            post = new HttpPost(GamocosmConfig.Core.getURL() + "resume");
        } else  {
            post = new HttpPost(GamocosmConfig.Core.getURL() + "start");
        }
        client.execute(post).close();
        client.close();
    }

}
