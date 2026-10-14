package dev.playersarmor;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.text.Text;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public final class PlayerArmorCommands {
    private PlayerArmorCommands() {
    }

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(literal("playersarmor")
                        .then(literal("reload").executes(context -> {
                            int count = PlayerSkinCache.reload();
                            context.getSource().sendFeedback(Text.translatable("playersarmor.command.reload", count));
                            return 1;
                        }))));
    }
}
