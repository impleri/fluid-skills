package net.impleri.fluidskills;

import com.mojang.brigadier.CommandDispatcher;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.platform.Platform;
import net.impleri.playerskills.commands.PlayerSkillsCommands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class FluidEvents {
    public void registerEventHandlers() {
        LifecycleEvent.SERVER_STARTING.register(this::onStartup);
    }

    public void registerCommands() {
        CommandRegistrationEvent.EVENT.register(this::registerDebugCommand);
    }

    private void registerDebugCommand(CommandDispatcher<CommandSourceStack> dispatcher, Commands.CommandSelection selection) {
        PlayerSkillsCommands.registerDebug(dispatcher, "fluidskills", PlayerSkillsCommands.toggleDebug("Fluid Skills", FluidSkills::toggleDebug));
    }

    private void onStartup(MinecraftServer minecraftServer) {
        if (Platform.isModLoaded("kubejs")) {
            net.impleri.fluidskills.integrations.kubejs.FluidSkillsPlugin.onStartup(minecraftServer);
        }
    }

    @Nullable
    public BlockState onReplaceBlock(Player player, BlockState original, BlockPos pos) {
        if (FluidHelper.isFluidBlock(original)) {
            return FluidHelper.getReplacementBlock(player, original, pos);
        }

        return null;
    }
}
