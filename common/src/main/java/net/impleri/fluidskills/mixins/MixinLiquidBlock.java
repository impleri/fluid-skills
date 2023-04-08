package net.impleri.fluidskills.mixins;

import net.impleri.fluidskills.FluidHelper;
import net.impleri.fluidskills.FluidSkills;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LiquidBlock.class)
public class MixinLiquidBlock {
    @Shadow
    @Final
    protected FlowingFluid fluid;
    private static final double MAX_DISTANCE = 10.0;

    @Inject(method = "pickupBlock", at = @At("HEAD"), cancellable = true)
    private void onPickup(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState, CallbackInfoReturnable<ItemStack> cir) {
        // Action is targeting a non-fluid block, so nothing needs to be done here
        if (!FluidHelper.isFluidBlock(blockState)) {
            return;
        }

        // Action is targeting a non-source block, so it can't be picked up
        if (!blockState.getFluidState().isSource()) {
            return;
        }

        // We are assuming that the non-spectator player closed to the block being picked up is the one doing the action.
        // If enough mods extend the player's reach past 10 blocks, this will fail here.
        var player = levelAccessor.getNearestPlayer(blockPos.getX(), blockPos.getY(), blockPos.getZ(), MAX_DISTANCE, EntitySelector.NO_SPECTATORS);

        if (player == null) {
            FluidSkills.LOGGER.warn("Could not find a player within {} blocks of fluid {} being picked up", MAX_DISTANCE, FluidHelper.getFluidName(fluid));
            return;
        }

        var canPickup = FluidHelper.canBucket(player, fluid, blockPos);

        if (!canPickup) {
            FluidSkills.LOGGER.debug("Preventing pickup of {}", FluidHelper.getFluidName(fluid));
            cir.setReturnValue(ItemStack.EMPTY);
        }
    }
}
