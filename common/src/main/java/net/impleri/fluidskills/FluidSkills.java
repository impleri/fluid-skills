package net.impleri.fluidskills;

import net.impleri.fluidskills.restrictions.Restriction;
import net.impleri.playerskills.restrictions.Registry;
import net.impleri.playerskills.utils.PlayerSkillsLogger;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class FluidSkills {
    public static final String MOD_ID = "fluidskills";

    public static final PlayerSkillsLogger LOGGER = PlayerSkillsLogger.create(MOD_ID, "FLUIDS");

    public static Registry<Restriction> RESTRICTIONS = new Registry<>(MOD_ID);

    private static final FluidEvents INSTANCE = new FluidEvents();

    public static void init() {
        LOGGER.info("Loaded Fluid Skills");
        INSTANCE.registerEventHandlers();
        INSTANCE.registerCommands();
    }

    public static void enableDebug() {
        LOGGER.enableDebug();
    }

    public static boolean toggleDebug() {
        return LOGGER.toggleDebug();
    }

    @Nullable
    public static BlockState maybeReplaceFluidBlock(Player player, BlockState original, BlockPos pos) {
        return INSTANCE.onReplaceBlock(player, original, pos);
    }
}
