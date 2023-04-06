package net.impleri.fluidskills.restrictions;

import net.impleri.fluidskills.FluidHelper;
import net.impleri.fluidskills.FluidSkills;
import net.impleri.playerskills.restrictions.RestrictionsApi;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

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

    private boolean canHelper(@Nullable Player player, Fluid fluid, @Nullable BlockPos pos, String resource) {
        if (player == null) {
            return false;
        }

        var blockPos = pos == null ? player.getOnPos() : pos;
        var level = player.getLevel();
        var dimension = level.dimension().location();
        var biome = level.getBiome(blockPos).unwrapKey().orElseThrow().location();

        return canPlayer(player, fluid, dimension, biome, resource);
    }

    public boolean isBucketable(@Nullable Player player, Fluid fluid, @Nullable BlockPos pos) {
        return canHelper(player, fluid, pos, "bucketable");
    }

    public boolean isBucketable(@Nullable Player player, Fluid fluid) {
        return isBucketable(player, fluid, null);
    }

    public boolean isProducible(@Nullable Player player, Fluid fluid, @Nullable BlockPos pos) {
        FluidSkills.LOGGER.info("Checking if {} is producible for {}", FluidHelper.getFluidName(fluid), player == null ? "unknown" : player.getName().getString());
        return canHelper(player, fluid, pos, "producible");
    }

    public boolean isProducible(@Nullable Player player, Fluid fluid) {
        return isProducible(player, fluid, null);
    }

    public boolean isConsumable(@Nullable Player player, Fluid fluid, @Nullable BlockPos pos) {
        FluidSkills.LOGGER.info("Checking if {} is consumable for {}", FluidHelper.getFluidName(fluid), player == null ? "unknown" : player.getName().getString());
        return canHelper(player, fluid, pos, "consumable");
    }

    public boolean isConsumable(@Nullable Player player, Fluid fluid) {
        return isConsumable(player, fluid, null);
    }

    public boolean isIdentifiable(@Nullable Player player, Fluid fluid, @Nullable BlockPos pos) {
        return canHelper(player, fluid, pos, "identifiable");
    }

    public boolean isIdentifiable(@Nullable Player player, Fluid fluid) {
        return isIdentifiable(player, fluid, null);
    }

    // Second layer cache: Finite/infinite value for (fluid, dimension, biome)

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
                .map(restriction -> restriction.finiteMode)
                .toList();

        if (values.contains(FluidFiniteMode.FINITE)) {
            return FluidFiniteMode.FINITE;
        } else if (values.contains(FluidFiniteMode.INFINITE)) {
            return FluidFiniteMode.INFINITE;
        }

        return FluidFiniteMode.DEFAULT;
    }

    // First cache layer: all finite/infinite restrictions for the fluid
    private List<Restriction> getRestrictionsFor(Fluid fluid) {
        return fluidRestrictionsCache.computeIfAbsent(fluid, this::populateFluidRestrictions);
    }

    private final Map<Fluid, List<Restriction>> fluidRestrictionsCache = new HashMap<>();

    private List<Restriction> populateFluidRestrictions(Fluid fluid) {
        var isTargetingFluid = createPredicateFor(fluid);

        return registry.entries().stream()
                .filter(restriction -> isTargetingFluid.test(restriction.target))
                .filter(restriction -> restriction.finiteMode != FluidFiniteMode.DEFAULT)
                .toList();
    }
}
