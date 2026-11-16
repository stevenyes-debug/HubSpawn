package com.hubspawn;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class HubSpawnMod implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("hubspawn");
    private static HubSpawnConfig config;

    @Override
    public void onInitialize() {
        config = HubSpawnConfig.load();
        registerCommands();
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.player;
            if (isFakePlayer(player)) {
                LOGGER.info("[HubSpawn] Skipped fake player {}", player.getScoreboardName());
                return;
            }
            server.execute(() -> teleportToHub(server, player));
        });
        LOGGER.info("[HubSpawn] Ready. Config: config/hubspawn.json");
    }

    private static boolean isFakePlayer(ServerPlayer player) {
        String className = player.getClass().getName();
        return className.contains("EntityPlayerMPFake")
                || className.contains("FakePlayer")
                || className.contains("FakeServerPlayer");
    }

    private static void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                register(dispatcher));
    }

    private static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("sethubspawn")
                .requires(HubSpawnMod::isOp)
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    config.setFrom(player);
                    context.getSource().sendSuccess(() -> Component.literal(
                            "[HubSpawn] Hub spawn set: " + config.world + " "
                                    + String.format("%.2f %.2f %.2f", config.x, config.y, config.z)), false);
                    return 1;
                }));
    }

    private static void teleportToHub(MinecraftServer server, ServerPlayer player) {
        if (config.enabled == false || player.isRemoved()) {
            return;
        }
        try {
            Identifier location = Identifier.parse(config.world);
            ResourceKey<Level> key = ResourceKey.create(Registries.DIMENSION, location);
            ServerLevel level = server.getLevel(key);
            if (level == null) {
                LOGGER.warn("[HubSpawn] World '{}' not found", config.world);
                return;
            }
            player.teleportTo(level, config.x, config.y, config.z, java.util.Set.of(), config.yaw, config.pitch, true);
        } catch (Exception e) {
            LOGGER.warn("[HubSpawn] Failed to teleport {}: {}", player.getScoreboardName(), e.toString());
        }
    }

    private static boolean isOp(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            return true;
        }
        return source.getServer().getPlayerList().isOp(player.nameAndId());
    }
}
