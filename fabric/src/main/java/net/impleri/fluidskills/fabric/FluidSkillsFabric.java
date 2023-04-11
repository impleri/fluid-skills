package net.impleri.fluidskills.fabric;

import net.fabricmc.api.ModInitializer;
import net.impleri.fluidskills.FluidSkills;

public class FluidSkillsFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        FluidSkills.init();
    }
}
