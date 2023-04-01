package net.impleri.fluidskills;

import net.impleri.fluidskills.restrictions.FluidFiniteMode;
import net.impleri.fluidskills.restrictions.Restrictions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DispensibleContainerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public class FluidHelper {
    public static Fluid getFluid(FluidState fluidState) {
        return fluidState.getType();
    }

    public static Fluid getFluid(ResourceLocation fluid) {
        return Registry.FLUID.get(fluid);
    }

    public static boolean isEmptyFluid(Fluid fluid) {
        return fluid == null || fluid.isSame(Fluids.EMPTY);
    }

    public static boolean isEmptyFluid(FluidState fluidState) {
        return isEmptyFluid(fluidState.getType());
    }

    public static boolean isFluidBlock(Block block) {
        return block instanceof LiquidBlock;
    }

    public static boolean isFluidBlock(BlockState block) {
        return isFluidBlock(block.getBlock()) || !isEmptyFluid(block.getFluidState());
    }

    public static boolean isBucketLikeItem(ItemStack itemStack) {
        if (!itemStack.isEmpty()) {
            var item = itemStack.getItem();

            return (item instanceof DispensibleContainerItem bucketLike);
        }

        return false;
    }

    public static boolean isReplacedFluid(Fluid a, Fluid b) {
        return !b.isSame(a);
    }

    public static ResourceLocation getFluidName(Fluid fluid) {
        return Registry.FLUID.getKey(fluid);
    }

    public static ResourceLocation getFluidName(FluidState fluidState) {
        return getFluidName(getFluid(fluidState));
    }

    public static Fluid getReplacementFor(Player player, Fluid original, BlockPos pos) {
        var level = player.getLevel();
        var dimension = level.dimension().location();
        var biome = level.getBiome(pos).unwrapKey().orElseThrow().location();

        var replacement = Restrictions.INSTANCE.getReplacementFor(player, original, dimension, biome);

        if (isReplacedFluid(replacement, original)) {
            FluidSkills.LOGGER.debug("Replacing fluid {} in {}/{} with {}", getFluidName(original), dimension.getPath(), biome.getPath(), getFluidName(replacement));
        }

        return replacement;
    }

    public static BlockState getReplacementBlock(Player player, BlockState originalBlock, BlockPos pos) {
        var fluidState = originalBlock.getFluidState();
        var original = fluidState.getType();

        var replacement = getReplacementFor(player, original, pos);

        // We have a replacement fluid
        if (isReplacedFluid(original, replacement) && !isEmptyFluid(original)) {
            var isEmptyReplacement = isEmptyFluid(replacement);
            var replacedBlock = isEmptyReplacement ? Blocks.AIR.defaultBlockState() : Registry.BLOCK.get(getFluidName(replacement))
                    .defaultBlockState();

            if (!fluidState.isSource() && !isEmptyReplacement) {
                replacedBlock.setValue(LiquidBlock.LEVEL, originalBlock.getValue(LiquidBlock.LEVEL));
            }

            return replacedBlock;
        }

        return originalBlock;
    }

    public static FluidState replaceFluidStateForEntity(Entity entity, Level level, BlockPos blockPos) {
        Player nearestPlayer;
        if (entity instanceof Player player) {
            nearestPlayer = player;
        } else {
            // Use the nearest player within spawning distance to apply their restrictions
            nearestPlayer = level.getNearestPlayer(entity, entity.getType().getCategory().getDespawnDistance());
        }

        if (nearestPlayer != null) {
            var blockState = level.getBlockState(blockPos);
            var original = blockState.getFluidState().getType();

            if (!isEmptyFluid(original)) {
                var replacement = getReplacementBlock(nearestPlayer, blockState, blockPos).getFluidState();

                if (isReplacedFluid(replacement.getType(), original)) {
                    FluidSkills.LOGGER.debug(
                            "Replacing fluid {} in {}/{} for player interaction with {}",
                            getFluidName(original),
                            level.dimension().location().getPath(),
                            level.getBiome(blockPos).unwrapKey().orElseThrow().location().getPath(),
                            getFluidName(replacement)
                    );
                }

                return replacement;
            }
        }

        return level.getFluidState(blockPos);
    }

    public static boolean canBucket(Player player, Fluid fluid, BlockPos pos) {
        var actual = getReplacementFor(player, fluid, pos);
        return Restrictions.INSTANCE.isBucketable(player, actual, pos);
    }

    public static FluidFiniteMode getFiniteModeFor(Fluid fluid, ResourceLocation dimension, ResourceLocation biome) {
        return Restrictions.INSTANCE.getFiniteModeFor(fluid, dimension, biome);
    }
}
