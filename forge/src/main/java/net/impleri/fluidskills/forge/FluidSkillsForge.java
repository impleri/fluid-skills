package net.impleri.fluidskills.forge;

import dev.architectury.platform.forge.EventBuses;
import net.impleri.fluidskills.FluidSkills;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(FluidSkills.MOD_ID)
public class FluidSkillsForge {
    public FluidSkillsForge() {
        // Submit our event bus to let architectury register our content on the right time
        EventBuses.registerModEventBus(FluidSkills.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        FluidSkills.init();
    }
}
