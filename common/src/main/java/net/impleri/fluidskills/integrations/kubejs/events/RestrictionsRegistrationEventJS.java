package net.impleri.fluidskills.integrations.kubejs.events;

import dev.latvian.mods.kubejs.server.ServerEventJS;
import dev.latvian.mods.kubejs.util.ConsoleJS;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.impleri.fluidskills.FluidHelper;
import net.impleri.fluidskills.FluidSkills;
import net.impleri.playerskills.utils.RegistrationType;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class RestrictionsRegistrationEventJS extends ServerEventJS {
    public RestrictionsRegistrationEventJS(MinecraftServer s) {
        super(s);
    }

    public void restrict(String fluidName, @NotNull Consumer<RestrictionJS.Builder> consumer) {
        RegistrationType<Fluid> registrationType = new RegistrationType<Fluid>(fluidName, Registry.FLUID_REGISTRY);

        registrationType.ifNamespace(namespace -> restrictNamespace(namespace, consumer));
        registrationType.ifName(name -> restrictFluid(name, consumer));
        registrationType.ifTag(tag -> restrictTag(tag, consumer));
    }

    @HideFromJS
    public void restrictFluid(ResourceLocation name, @NotNull Consumer<RestrictionJS.Builder> consumer) {
        var builder = new RestrictionJS.Builder(name, server);

        consumer.accept(builder);

        var fluid = FluidHelper.getFluid(name);
        if (FluidHelper.isEmptyFluid(fluid)) {
            ConsoleJS.SERVER.warn("Could not find any fluid named " + name);
            return;
        }

        var restriction = builder.createObject(fluid);
        ConsoleJS.SERVER.info("Created fluid restriction for " + name);
        FluidSkills.RESTRICTIONS.add(name, restriction);
    }

    @HideFromJS
    private void restrictNamespace(String namespace, @NotNull Consumer<RestrictionJS.Builder> consumer) {
        ConsoleJS.SERVER.info("Creating fluid restrictions for namespace " + namespace);
        net.minecraft.core.Registry.FLUID.keySet()
                .stream()
                .filter(fluidName -> fluidName.getNamespace().equals(namespace))
                .forEach(fluidName -> restrictFluid(fluidName, consumer));
    }

    @HideFromJS
    private void restrictTag(TagKey<Fluid> tag, @NotNull Consumer<RestrictionJS.Builder> consumer) {
        ConsoleJS.SERVER.info("Creating block restrictions for tag " + tag.location());
        net.minecraft.core.Registry.FLUID.stream()
                .filter(fluid -> fluid.is(tag))
                .forEach(fluid -> restrictFluid(FluidHelper.getFluidName(fluid), consumer));
    }
}
