package com.yreconnect;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class YReconnect implements ClientModInitializer {
    public static final String MOD_ID = "yreconnect";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static KeyBinding openConfigKey;
    private static String lastServerIp = null;
    private static int lastServerPort = 25565;
    private static int reconnectCountdown = -1;
    private static boolean waitingToReconnect = false;
    private static boolean triggered = false;

    @Override
    public void onInitializeClient() {
        YReconnectConfig.load();
        openConfigKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.yreconnect.openConfig", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_K, "category.yreconnect"));
        YReconnectCommand.register();
        LOGGER.info("[YReconnect] Loaded for Minecraft 1.21.11");
        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
    }

    private void onTick(MinecraftClient client) {
        while (openConfigKey.wasPressed()) {
            if (client.currentScreen == null)
                client.setScreen(new YReconnectConfigScreen(null));
        }
        YReconnectConfig cfg = YReconnectConfig.get();
        if (cfg.enabled && client.player != null && client.world != null
                && client.getCurrentServerEntry() != null && !waitingToReconnect && !triggered) {
            double playerY = client.player.getY();
            boolean shouldDisconnect = cfg.triggerAbove ? playerY > cfg.triggerY : playerY < cfg.triggerY;
            if (shouldDisconnect) {
                ServerInfo info = client.getCurrentServerEntry();
                ServerAddress addr = ServerAddress.parse(info.address);
                lastServerIp = addr.getAddress();
                lastServerPort = addr.getPort();
                LOGGER.info("[YReconnect] Threshold crossed at Y={}. Disconnecting.", String.format("%.2f", playerY));
                client.player.sendMessage(Text.literal("§c[YReconnect] §fThreshold crossed! Reconnecting..."), true);
                client.world.disconnect();
                client.disconnect();
                triggered = true;
                waitingToReconnect = true;
                reconnectCountdown = cfg.reconnectDelayTicks;
            }
        }
        if (waitingToReconnect) {
            reconnectCountdown--;
            if (reconnectCountdown <= 0) {
                waitingToReconnect = false;
                reconnectCountdown = -1;
                triggered = false;
                attemptReconnect(client);
            }
        }
    }

    private void attemptReconnect(MinecraftClient client) {
        if (lastServerIp == null) return;
        LOGGER.info("[YReconnect] Reconnecting to {}:{}", lastServerIp, lastServerPort);
        ServerAddress address = new ServerAddress(lastServerIp, lastServerPort);
        ServerInfo serverInfo = new ServerInfo("YReconnect Server", lastServerIp + ":" + lastServerPort, ServerInfo.ServerType.OTHER);
        ConnectScreen.connect(new TitleScreen(), client, address, serverInfo, false, null);
    }
}
