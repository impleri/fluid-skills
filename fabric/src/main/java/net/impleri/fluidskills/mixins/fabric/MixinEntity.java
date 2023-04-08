package net.impleri.fluidskills.mixins.fabric;

import net.impleri.fluidskills.FluidHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Entity.class)
public class MixinEntity {
    @Redirect(method = "updateFluidHeightAndDoFluidPushing", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getFluidState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/material/FluidState;"))
    private FluidState onUpdateFluidHeight(Level instance, BlockPos blockPos) {
        return FluidHelper.replaceFluidStateForEntity((Entity) (Object) this, instance, blockPos);
    }
}
