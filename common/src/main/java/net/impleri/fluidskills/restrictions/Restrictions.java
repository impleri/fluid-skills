package net.impleri.fluidskills.restrictions;

import net.impleri.fluidskills.FluidHelper;
import net.impleri.fluidskills.FluidSkills;
import net.impleri.playerskills.restrictions.RestrictionsApi;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.Fluid;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class Restrictions extends RestrictionsApi<Fluid, Restriction> {
    private static final Field[] allRestrictionFields = Restriction.class.getDeclaredFields();
    public static Restrictions INSTANCE = new Restrictions(FluidSkills.RESTRICTIONS, allRestrictionFields);

    public Restrictions(net.impleri.playerskills.restrictions.Registry<Restriction> registry, Field[] fields) {
        super(registry, fields, FluidSkills.LOGGER);
    }

    @Override
    protected ResourceLocation getTargetName(Fluid target) {
        return Registry.FLUID.getKey(target);
    }

    @Override
    protected Predicate<Fluid> createPredicateFor(Fluid fluid) {
        return (Fluid target) -> target.isSame(fluid);
    }

    private boolean canHelper(Player player, Fluid fluid, BlockPos pos, String resource) {
        var level = player.getLevel();
        return canPlayer(player, fluid, level.dimension().location(), level.getBiome(pos).unwrapKey().orElseThrow().location(), resource);
    }

    public boolean isBucketable(Player player, Fluid fluid, BlockPos pos) {
        return canHelper(player, fluid, pos, "bucketable");
    }

    public FluidFiniteMode getFiniteModeFor(Fluid fluid, ResourceLocation dimension, ResourceLocation biome) {
        var fluidName = FluidHelper.getFluidName(fluid);
        var cacheKey = new FiniteCacheKey(fluidName, dimension, biome);
        return finiteCache.computeIfAbsent(cacheKey, (key) -> populateFiniteRestriction(fluid, dimension, biome));
    }

    private record FiniteCacheKey(ResourceLocation fluid, ResourceLocation dimension, ResourceLocation biome) {
    }

    private final Map<FiniteCacheKey, FluidFiniteMode> finiteCache = new HashMap<>();

    private FluidFiniteMode populateFiniteRestriction(Fluid fluid, ResourceLocation dimension, ResourceLocation biome) {
        var values = getRestrictionsFor(fluid).stream()
                .filter(inIncludedDimension(dimension))
                .filter(notInExcludedDimension(dimension))
                .filter(inIncludedBiome(biome))
                .filter(notInExcludedBiome(biome))
                .filter(restriction -> restriction.finiteMode != FluidFiniteMode.DEFAULT)
                .map(restriction -> restriction.finiteMode)
                .toList();

        if (values.contains(FluidFiniteMode.FINITE)) {
            return FluidFiniteMode.FINITE;
        } else if (values.contains(FluidFiniteMode.INFINITE)) {
            return FluidFiniteMode.INFINITE;
        }

        return FluidFiniteMode.DEFAULT;
    }

    private final Map<Fluid, List<Restriction>> fluidRestrictionsCache = new HashMap<>();

    private List<Restriction> getRestrictionsFor(Fluid fluid) {
        return fluidRestrictionsCache.computeIfAbsent(fluid, this::populateFluidRestrictions);
    }

    private List<Restriction> populateFluidRestrictions(Fluid fluid) {
        var isTargetingFluid = createPredicateFor(fluid);

        return registry.entries().stream()
                .filter(restriction -> isTargetingFluid.test(restriction.target))
                .toList();
    }
}
