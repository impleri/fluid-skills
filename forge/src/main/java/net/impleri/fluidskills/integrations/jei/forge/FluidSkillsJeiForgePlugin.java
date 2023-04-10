package net.impleri.fluidskills.integrations.jei.forge;

import me.shedaniel.rei.plugincompatibilities.api.REIPluginCompatIgnore;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IJeiRuntime;
import net.impleri.fluidskills.FluidHelper;
import net.impleri.fluidskills.FluidSkills;
import net.impleri.fluidskills.client.ClientRestrictions;
import net.impleri.fluidskills.utils.ListDiff;
import net.impleri.playerskills.client.events.ClientSkillsUpdatedEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@REIPluginCompatIgnore()
public class FluidSkillsJeiForgePlugin implements IModPlugin {
    private IJeiRuntime runtime;
    private final List<Fluid> unconsumables = new ArrayList<>();
    private final List<Fluid> unproducibles = new ArrayList<>();

    public FluidSkillsJeiForgePlugin() {
        ClientSkillsUpdatedEvent.EVENT.register(this::updateHidden);
    }

    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return new ResourceLocation(FluidSkills.MOD_ID, "jei_plugin");
    }

    @Override
    public void onRuntimeAvailable(@NotNull IJeiRuntime jeiRuntime) {
        runtime = jeiRuntime;

        processUnconsumables();
        processUnproducibles();
    }

    private void updateHidden(ClientSkillsUpdatedEvent event) {
        if (runtime == null) {
            FluidSkills.LOGGER.warn("JEI Runtime not yet available to update");
            return;
        }

        processUnconsumables();
        processUnproducibles();
    }

    private void processUnconsumables() {
        var manager = runtime.getIngredientManager();
        var next = ClientRestrictions.INSTANCE.getUnconsumable();

        // Nothing on either list, so don't bother
        if (unconsumables.size() == 0 && next.size() == 0) {
            FluidSkills.LOGGER.debug("No changes in restrictions");
            return;
        }

        FluidSkills.LOGGER.debug("Found {} unconsumable fluid(s)", next.size());

        var toShow = ListDiff.getMissing(unconsumables, next);
        if (toShow.size() > 0) {
            FluidSkills.LOGGER.debug(
                    "Showing {} fluid(s) based on consumables: {}",
                    toShow.size(),
                    toShow.stream()
                            .map(FluidHelper::getFluidName)
                            .toList()
                            .toString()
            );
            manager.addIngredientsAtRuntime(ForgeTypes.FLUID_STACK, getFluidStacks(toShow));
        }

        var toHide = ListDiff.getMissing(next, unconsumables);
        if (toHide.size() > 0) {
            FluidSkills.LOGGER.debug(
                    "Hiding {} fluid(s) based on consumables: {}",
                    toHide.size(),
                    toHide.stream()
                            .map(FluidHelper::getFluidName)
                            .toList()
                            .toString()
            );
            manager.removeIngredientsAtRuntime(ForgeTypes.FLUID_STACK, getFluidStacks(toHide));
        }

        unconsumables.clear();
        unconsumables.addAll(next);
    }

    private void processUnproducibles() {
        var next = ClientRestrictions.INSTANCE.getUnproducible();

        // Nothing on either list, so don't bother
        if (unproducibles.size() == 0 && next.size() == 0) {
            FluidSkills.LOGGER.debug("No changes in restrictions");
            return;
        }

        FluidSkills.LOGGER.debug("Found {} unproducible fluid(s)", next.size());

        var toShow = ListDiff.getMissing(unproducibles, next);

        if (toShow.size() > 0) {
            var foci = getFociFor(getFluidStacks(toShow));
            var types = getTypesFor(foci, true);

            FluidSkills.LOGGER.debug(
                    "Showing {} fluid(s) based on producibles: {}",
                    toShow.size(),
                    toShow.stream()
                            .map(FluidHelper::getFluidName)
                            .toList()
                            .toString()
            );

            types.forEach(type -> showRecipesForType(type, foci));
        }

        var toHide = ListDiff.getMissing(next, unproducibles);

        if (toHide.size() > 0) {
            var foci = getFociFor(getFluidStacks(toHide));
            var types = getTypesFor(foci, false);

            FluidSkills.LOGGER.debug(
                    "Hiding {} fluid(s) based on producibles: {}",
                    toHide.size(),
                    toHide.stream()
                            .map(FluidHelper::getFluidName)
                            .toList()
                            .toString()
            );

            types.forEach(type -> hideRecipesForType(type, foci));
        }

        unproducibles.clear();
        unproducibles.addAll(next);
    }

    private List<IFocus<FluidStack>> getFociFor(List<FluidStack> fluids) {
        var factory = runtime.getJeiHelpers().getFocusFactory();

        return fluids.stream()
                .map(fluid -> factory.createFocus(RecipeIngredientRole.OUTPUT, ForgeTypes.FLUID_STACK, fluid))
                .toList();
    }

    private Collection<RecipeType<FluidStack>> getTypesFor(List<IFocus<FluidStack>> foci, boolean includeHidden) {
        var lookup = runtime.getRecipeManager().createRecipeCategoryLookup()
                .limitFocus(foci);

        if (includeHidden) {
            lookup.includeHidden();
        }

        return lookup.get()
                .map(IRecipeCategory::getRecipeType)
                .map(r -> (RecipeType<FluidStack>) r)
                .toList();
    }

    private void hideRecipesForType(RecipeType<FluidStack> type, List<IFocus<FluidStack>> foci) {
        runtime.getRecipeManager().hideRecipes(type, getRecipesFor(type, foci, false));
    }

    private void showRecipesForType(RecipeType<FluidStack> type, List<IFocus<FluidStack>> foci) {
        runtime.getRecipeManager().unhideRecipes(type, getRecipesFor(type, foci, true));
    }

    private Collection<FluidStack> getRecipesFor(RecipeType<FluidStack> type, List<IFocus<FluidStack>> foci, boolean includeHidden) {
        var lookup = runtime.getRecipeManager()
                .createRecipeLookup(type)
                .limitFocus(foci);

        if (includeHidden) {
            lookup.includeHidden();
        }

        return lookup.get().toList();
    }

    private List<FluidStack> getFluidStacks(List<Fluid> fluids) {
        return fluids.stream()
                .map(this::getFluidStacksFor)
                .toList();
    }

    private FluidStack getFluidStacksFor(Fluid fluid) {
        return new FluidStack(fluid, FluidAttributes.BUCKET_VOLUME);
    }
}
