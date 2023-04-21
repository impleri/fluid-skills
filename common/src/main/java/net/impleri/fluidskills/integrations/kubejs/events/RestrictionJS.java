package net.impleri.fluidskills.integrations.kubejs.events;

import dev.latvian.mods.kubejs.RegistryObjectBuilderTypes;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.impleri.fluidskills.FluidHelper;
import net.impleri.fluidskills.FluidSkills;
import net.impleri.fluidskills.restrictions.FluidFiniteMode;
import net.impleri.fluidskills.restrictions.Restriction;
import net.impleri.playerskills.integration.kubejs.api.AbstractRestrictionBuilder;
import net.impleri.playerskills.utils.SkillResourceLocation;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public class RestrictionJS extends Restriction {
    private static final ResourceKey<Registry<Restriction>> key = ResourceKey.createRegistryKey(SkillResourceLocation.of("fluid_restriction_builders_registry"));

    public static final RegistryObjectBuilderTypes<Restriction> registry = RegistryObjectBuilderTypes.add(key, Restriction.class);

    public RestrictionJS(Fluid fluid, Builder builder) {
        super(
                fluid,
                builder.condition,
                builder.includeDimensions,
                builder.excludeDimensions,
                builder.includeBiomes,
                builder.excludeBiomes,
                builder.bucketable,
                builder.producible,
                builder.consumable,
                builder.identifiable,
                builder.finiteMode,
                builder.replacement
        );
    }

    public static class Builder extends AbstractRestrictionBuilder<Restriction> {
        public Fluid replacement;

        public FluidFiniteMode finiteMode;
        public boolean bucketable = true;
        public boolean producible = true;
        public boolean consumable = true;
        public boolean identifiable = true;

        @HideFromJS
        public Builder(ResourceLocation id, MinecraftServer server) {
            super(id, server);
        }

        public Builder replaceWithFluid(String replacement) {
            var name = SkillResourceLocation.of(replacement);

            var fluid = FluidHelper.getFluid(name);
            if (FluidHelper.isEmptyFluid(fluid)) {
                FluidSkills.LOGGER.warn("Could not find any fluid named %s", name);
                return this;
            }

            this.replacement = fluid;

            return this;
        }

        public Builder replaceWithAir() {
            this.replacement = Fluids.EMPTY;

            return this;
        }

        public Builder bucketable() {
            this.bucketable = true;

            return this;
        }

        public Builder unbucketable() {
            this.bucketable = false;

            return this;
        }

        public Builder producible() {
            this.producible = true;

            return this;
        }

        public Builder unproducible() {
            this.producible = false;

            return this;
        }

        public Builder consumable() {
            this.consumable = true;

            return this;
        }

        public Builder unconsumable() {
            this.consumable = false;

            return this;
        }

        public Builder identifiable() {
            this.identifiable = true;

            return this;
        }

        public Builder unidentifiable() {
            this.identifiable = false;

            return this;
        }

        public Builder infinite() {
            this.finiteMode = FluidFiniteMode.INFINITE;

            return this;
        }

        public Builder finite() {
            this.finiteMode = FluidFiniteMode.FINITE;

            return this;
        }

        public Builder nothing() {
            bucketable = true;
            producible = true;
            consumable = true;
            identifiable = true;

            return this;
        }

        public Builder everything() {
            bucketable = false;
            producible = false;
            consumable = false;
            identifiable = false;

            return this;
        }

        @HideFromJS
        @Override
        public RegistryObjectBuilderTypes<Restriction> getRegistryType() {
            return registry;
        }

        @HideFromJS
        @Override
        public Restriction createObject() {
            return null;
        }

        @HideFromJS
        public Restriction createObject(Fluid fluid) {
            return new RestrictionJS(fluid, this);
        }
    }
}
