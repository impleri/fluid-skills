package net.impleri.fluidskills.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.impleri.fluidskills.FluidSkills;
import net.impleri.playerskills.commands.PlayerSkillsCommands;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class FluidSkillsCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registry, Commands.CommandSelection selection) {
        PlayerSkillsCommands.registerDebug(dispatcher, "fluids", PlayerSkillsCommands.toggleDebug("Fluid Skills", FluidSkills::toggleDebug));
    }
}
