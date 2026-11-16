package com.hubspawn;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.level.ServerPlayer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class HubSpawnConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public boolean enabled = false;
    public String world = "minecraft:overworld";
    public double x = 0.5;
    public double y = 100.0;
    public double z = 0.5;
    public float yaw = 0.0f;
    public float pitch = 0.0f;

    private transient Path path;

    public static HubSpawnConfig load() {
        Path path = FabricLoader.getInstance().getConfigDir().resolve("hubspawn.json");
        HubSpawnConfig config = new HubSpawnConfig();
        config.path = path;
        if (Files.exists(path)) {
            try {
                HubSpawnConfig loaded = GSON.fromJson(Files.readString(path, StandardCharsets.UTF_8), HubSpawnConfig.class);
                if (loaded != null) {
                    config = loaded;
                    config.path = path;
                }
            } catch (Exception e) {
                HubSpawnMod.LOGGER.error("[HubSpawn] Failed to read {}", path, e);
            }
        }
        config.save();
        return config;
    }

    public void setFrom(ServerPlayer player) {
        this.enabled = true;
        this.world = player.level().dimension().identifier().toString();
        this.x = player.getX();
        this.y = player.getY();
        this.z = player.getZ();
        this.yaw = player.getYRot();
        this.pitch = player.getXRot();
        this.save();
    }

    public void save() {
        if (path == null) {
            path = FabricLoader.getInstance().getConfigDir().resolve("hubspawn.json");
        }
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, GSON.toJson(this), StandardCharsets.UTF_8);
        } catch (IOException e) {
            HubSpawnMod.LOGGER.error("[HubSpawn] Failed to write {}", path, e);
        }
    }
}
