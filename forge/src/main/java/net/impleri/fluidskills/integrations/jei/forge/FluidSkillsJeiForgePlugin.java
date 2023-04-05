package net.impleri.fluidskills.integrations.jei.forge;

import me.shedaniel.rei.plugincompatibilities.api.REIPluginCompatIgnore;
import mezz.jei.api.JeiPlugin;
import net.impleri.fluidskills.integrations.jei.FluidSkillsJeiPlugin;
import net.minecraftforge.fluids.FluidStack;

@REIPluginCompatIgnore()
@JeiPlugin()
public class FluidSkillsJeiForgePlugin extends FluidSkillsJeiPlugin<FluidStack> {
}
