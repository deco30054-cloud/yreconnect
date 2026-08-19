package com.yreconnect;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class YReconnectCommand {
    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
            dispatcher.register(ClientCommandManager.literal("yreconnect")
                .then(ClientCommandManager.literal("status").executes(ctx -> {
                    YReconnectConfig cfg = YReconnectConfig.get();
                    ctx.getSource().sendFeedback(Text.literal("§b[YReconnect] §fEnabled=§e" + cfg.enabled + " §f| Y=§e" + cfg.triggerY + " §f| Direction=§e" + (cfg.triggerAbove ? "above" : "below") + " §f| Delay=§e" + cfg.reconnectDelayTicks + " ticks"));
                    return 1;
                }))
                .then(ClientCommandManager.literal("config").executes(ctx -> {
                    MinecraftClient client = MinecraftClient.getInstance();
                    client.send(() -> client.setScreen(new YReconnectConfigScreen(null)));
                    return 1;
                }))
                .then(ClientCommandManager.literal("set")
                    .then(ClientCommandManager.argument("y", DoubleArgumentType.doubleArg())
                        .executes(ctx -> {
                            double y = DoubleArgumentType.getDouble(ctx, "y");
                            YReconnectConfig cfg = YReconnectConfig.get(); cfg.triggerY = y; cfg.triggerAbove = false; YReconnectConfig.save();
                            ctx.getSource().sendFeedback(Text.literal("§a[YReconnect] §fDisconnect when Y < §e" + y)); return 1;
                        })
                        .then(ClientCommandManager.argument("above", BoolArgumentType.bool()).executes(ctx -> {
                            double y = DoubleArgumentType.getDouble(ctx, "y"); boolean above = BoolArgumentType.getBool(ctx, "above");
                            YReconnectConfig cfg = YReconnectConfig.get(); cfg.triggerY = y; cfg.triggerAbove = above; YReconnectConfig.save();
                            ctx.getSource().sendFeedback(Text.literal("§a[YReconnect] §fDisconnect when Y " + (above ? "above" : "below") + " §e" + y)); return 1;
                        }))
                    )
                )
                .then(ClientCommandManager.literal("delay")
                    .then(ClientCommandManager.argument("ticks", IntegerArgumentType.integer(0, 200)).executes(ctx -> {
                        int ticks = IntegerArgumentType.getInteger(ctx, "ticks");
                        YReconnectConfig.get().reconnectDelayTicks = ticks; YReconnectConfig.save();
                        ctx.getSource().sendFeedback(Text.literal("§a[YReconnect] §fDelay set to §e" + ticks + " ticks")); return 1;
                    }))
                )
                .then(ClientCommandManager.literal("toggle").executes(ctx -> {
                    YReconnectConfig cfg = YReconnectConfig.get(); cfg.enabled = !cfg.enabled; YReconnectConfig.save();
                    ctx.getSource().sendFeedback(Text.literal(cfg.enabled ? "§a[YReconnect] §fEnabled." : "§c[YReconnect] §fDisabled.")); return 1;
                }))
            )
        );
    }
}
