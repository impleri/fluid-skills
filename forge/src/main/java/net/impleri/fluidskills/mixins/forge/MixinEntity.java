package net.impleri.fluidskills.mixins.forge;

import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import net.impleri.fluidskills.FluidHelper;
import net.impleri.fluidskills.FluidSkills;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.fluids.FluidType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Entity.class)
public class MixinEntity {
    @Shadow(remap = false)
    protected Object2DoubleMap<FluidType> forgeFluidTypeHeight;

    @Redirect(
            method = "updateFluidHeightAndDoFluidPushing()V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getFluidState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/material/FluidState;")
    )
    private FluidState onUpdateFluidHeight(Level instance, BlockPos blockPos) {
        return FluidHelper.replaceFluidStateForEntity((Entity) (Object) this, instance, blockPos);
    }

    // Just overwrite the existing method
    @Final()
    protected void setFluidTypeHeight(FluidType type, int height) {
        var entity = (Entity) (Object) this;

        var level = entity.getLevel();
        var pos = entity.getOnPos();
        var blockState = level.getBlockState(pos);
        var fluid = blockState.getFluidState().getType();

        if (type.equals(fluid.getFluidType())) {
            var replacement = FluidHelper.replaceFluidStateForEntity(entity, level, pos);
            FluidSkills.LOGGER.info(
                    "Setting fluid height (forge) for {} to be treated as {}",
                    FluidHelper.getFluidName(fluid),
                    FluidHelper.getFluidName(replacement)
            );
            forgeFluidTypeHeight.put(replacement.getFluidType(), height);
            return;
        }


        forgeFluidTypeHeight.put(type, height);
    }
}
