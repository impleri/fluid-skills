package net.impleri.fluidskills.utils;

import net.impleri.fluidskills.FluidHelper;
import net.minecraft.world.level.material.Fluid;

import java.util.ArrayList;
import java.util.List;

public class ListDiff {
    public static boolean contains(List<Fluid> a, List<Fluid> b) {
        return getMissing(a, b).size() == 0;
    }

    public static List<Fluid> getMissing(List<Fluid> a, List<Fluid> b) {
        if (a.size() == 0 && b.size() == 0) {
            return new ArrayList<>();
        }

        var bStrings = b.stream()
                .map(FluidHelper::getFluidName)
                .toList();

        return a.stream()
                .filter(existing -> !bStrings.contains(FluidHelper.getFluidName(existing)))
                .toList();
    }
}
