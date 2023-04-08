package net.impleri.fluidskills.restrictions;

import net.impleri.playerskills.restrictions.AbstractRestriction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

public class Restriction extends AbstractRestriction<Fluid> {
    public final boolean bucketable;
    public final boolean producible;
    public final boolean consumable;
    public final boolean identifiable;

    public final FluidFiniteMode finiteMode;

    public Restriction(
            Fluid target,
            @Nullable Predicate<Player> condition,
            @Nullable List<ResourceLocation> includeDimensions,
            @Nullable List<ResourceLocation> excludeDimensions,
            @Nullable List<ResourceLocation> includeBiomes,
            @Nullable List<ResourceLocation> excludeBiomes,
            @Nullable Boolean bucketable,
            @Nullable Boolean producible,
            @Nullable Boolean consumable,
            @Nullable Boolean identifiable,
            @Nullable FluidFiniteMode finiteMode,
            @NotNull Fluid replacement
    ) {
        super(target, condition, includeDimensions, excludeDimensions, includeBiomes, excludeBiomes, replacement);

        this.bucketable = Boolean.TRUE.equals(bucketable);
        this.producible = Boolean.TRUE.equals(producible);
        this.consumable = Boolean.TRUE.equals(consumable);
        this.identifiable = Boolean.TRUE.equals(identifiable);
        this.finiteMode = finiteMode == null ? FluidFiniteMode.DEFAULT : finiteMode;
    }
}
