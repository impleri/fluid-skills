package net.impleri.fluidskills.mixins;

import net.impleri.fluidskills.FluidHelper;
import net.impleri.fluidskills.FluidSkills;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(FlowingFluid.class)
public abstract class MixinFlowingFluid {
    @Shadow
    protected abstract boolean isSourceBlockOfThisType(FluidState fluidState);

    @Shadow
    protected abstract boolean canPassThroughWall(Direction direction, BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, BlockPos blockPos2, BlockState blockState2);

    @Shadow
    protected abstract int getDropOff(LevelReader levelReader);

    @Inject(method = "getNewLiquid", at = @At("RETURN"), cancellable = true, locals = LocalCapture.CAPTURE_FAILSOFT)
    private void onGetNewLiquid(LevelReader levelReader, BlockPos blockPos, BlockState blockState, CallbackInfoReturnable<FluidState> cir, int i, int j) {
        var fluid = (FlowingFluid) (Object) this;

        ResourceLocation currentDimension;
        var currentBiome = levelReader.getBiome(blockPos).unwrapKey().orElseThrow().location();

        if (levelReader instanceof Level level) {
            currentDimension = level.dimension().location();
        } else {
            // This is pretty hacky and rests on the hope that the dimension name matches the dimension type
            currentDimension = BuiltinRegistries.DIMENSION_TYPE.getKey(levelReader.dimensionType());
        }

        var mode = FluidHelper.getFiniteModeFor(fluid, currentDimension, currentBiome);
        FluidSkills.LOGGER.info("Maybe altering fluid {} to be {}", FluidHelper.getFluidName(fluid), mode);

        switch (mode) {
            case DEFAULT -> {
                // do nothing
            }
            case FINITE -> {
                // Copy logic from FlowingFluid.getNewLiquid for non-source blocks
                var originalReturn = cir.getReturnValue();
                if (originalReturn.isSource()) {
                    var blockPosAbove = blockPos.above();
                    BlockState blockStateAbove = levelReader.getBlockState(blockPosAbove);
                    FluidState fluidStateAbove = blockStateAbove.getFluidState();
                    if (!fluidStateAbove.isEmpty() && fluidStateAbove.getType().isSame(fluid) && canPassThroughWall(Direction.UP, levelReader, blockPos, blockState, blockPosAbove, blockStateAbove)) {
                        cir.setReturnValue(fluid.getFlowing(8, true));
                    } else {
                        int k = i - getDropOff(levelReader);
                        cir.setReturnValue(k <= 0 ? Fluids.EMPTY.defaultFluidState() : fluid.getFlowing(k, false));
                    }
                }
            }
            case INFINITE -> {
                // Copy logic from FlowingFluid.getNewLiquid for creating new source blocks but exclude call to canConvertToSource
                if (j >= 2) {
                    BlockState blockStateBelow = levelReader.getBlockState(blockPos.below());
                    FluidState fluidStateBelow = blockStateBelow.getFluidState();
                    if (blockStateBelow.getMaterial().isSolid() || isSourceBlockOfThisType(fluidStateBelow)) {
                        cir.setReturnValue(fluid.getSource(false));
                    }
                }
            }
        }
    }
}
