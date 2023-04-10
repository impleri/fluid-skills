package net.impleri.fluidskills.integrations.kubejs;

import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.ScriptType;
import net.impleri.fluidskills.integrations.kubejs.events.EventsBinding;
import net.impleri.fluidskills.integrations.kubejs.events.RestrictionsRegistrationEventJS;
import net.minecraft.server.MinecraftServer;

public class FluidSkillsPlugin extends KubeJSPlugin {
    public static void onStartup(MinecraftServer minecraftServer) {
        new RestrictionsRegistrationEventJS(minecraftServer).post(ScriptType.SERVER, EventsBinding.RESTRICTIONS);
    }
}
