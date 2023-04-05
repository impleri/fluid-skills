package net.impleri.fluidskills.integrations.jei.fabric;

import me.shedaniel.rei.plugincompatibilities.api.REIPluginCompatIgnore;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.fabric.ingredients.fluids.IJeiFluidIngredient;
import net.impleri.fluidskills.integrations.jei.FluidSkillsJeiPlugin;

@REIPluginCompatIgnore()
@JeiPlugin()
public class FluidSkillsJeiPluginFabric extends FluidSkillsJeiPlugin<IJeiFluidIngredient> {
}
