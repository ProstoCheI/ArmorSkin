package dev.armorskin;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.text.Text;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public final class ArmorSkinCommands {
    private ArmorSkinCommands() {
    }

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(literal("armorskin")
                        .then(literal("reload").executes(context -> {
                            int count = PlayerSkinCache.reload();
                            context.getSource().sendFeedback(Text.translatable("armorskin.command.reload", count));
                            return 1;
                        }))));
    }
}
