package net.impleri.fluidskills.integrations.jei;

import me.shedaniel.rei.plugincompatibilities.api.REIPluginCompatIgnore;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
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
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@REIPluginCompatIgnore()
public class FluidSkillsJeiPlugin<T> implements IModPlugin {
    private IJeiRuntime runtime;
    private final List<Fluid> unconsumables = new ArrayList<>();
    private final List<Fluid> unproducibles = new ArrayList<>();

    public FluidSkillsJeiPlugin() {
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

    private IIngredientTypeWithSubtypes<Fluid, T> getFluidType() {
        return (IIngredientTypeWithSubtypes<Fluid, T>) runtime.getJeiHelpers().getPlatformFluidHelper().getFluidIngredientType();
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
        var fluidType = getFluidType();
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
            manager.addIngredientsAtRuntime(fluidType, getFluidStacks(toShow));
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
            manager.removeIngredientsAtRuntime(fluidType, getFluidStacks(toHide));
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
                            .map(value -> (Fluid) value)
                            .map(FluidHelper::getFluidName)
                            .toList()
                            .toString()
            );

            types.forEach(type -> hideRecipesForType(type, foci));
        }

        unproducibles.clear();
        unproducibles.addAll(next);
    }

    private List<IFocus<T>> getFociFor(List<T> fluids) {
        var factory = runtime.getJeiHelpers().getFocusFactory();
        var fluidType = getFluidType();

        return fluids.stream()
                .map(fluid -> factory.createFocus(RecipeIngredientRole.OUTPUT, fluidType, fluid))
                .toList();
    }

    private Collection<RecipeType<T>> getTypesFor(List<IFocus<T>> foci, boolean includeHidden) {
        var lookup = runtime.getRecipeManager().createRecipeCategoryLookup()
                .limitFocus(foci);

        if (includeHidden) {
            lookup.includeHidden();
        }

        return lookup.get()
                .map(IRecipeCategory::getRecipeType)
                .map(r -> (RecipeType<T>) r)
                .toList();
    }

    private void hideRecipesForType(RecipeType<T> type, List<IFocus<T>> foci) {
        runtime.getRecipeManager().hideRecipes(type, getRecipesFor(type, foci, false));
    }

    private void showRecipesForType(RecipeType<T> type, List<IFocus<T>> foci) {
        runtime.getRecipeManager().unhideRecipes(type, getRecipesFor(type, foci, true));
    }

    private Collection<T> getRecipesFor(RecipeType<T> type, List<IFocus<T>> foci, boolean includeHidden) {
        var lookup = runtime.getRecipeManager()
                .createRecipeLookup(type)
                .limitFocus(foci);

        if (includeHidden) {
            lookup.includeHidden();
        }

        return lookup.get().toList();
    }

    private List<T> getFluidStacks(List<Fluid> fluids) {
        return fluids.stream()
                .map(this::getFluidStacksFor)
                .toList();
    }

    private T getFluidStacksFor(Fluid fluid) {
        var helper = runtime.getJeiHelpers().getPlatformFluidHelper();
        var bucket = helper.bucketVolume();

        return (T) helper.create(fluid, bucket);
    }
}
