package net.impleri.fluidskills.client;

import net.impleri.fluidskills.FluidSkills;
import net.impleri.fluidskills.restrictions.Restriction;
import net.impleri.fluidskills.restrictions.Restrictions;
import net.impleri.playerskills.client.RestrictionsClient;
import net.impleri.playerskills.restrictions.Registry;
import net.minecraft.world.level.material.Fluid;

import java.util.List;

public class ClientRestrictions extends RestrictionsClient<Fluid, Restriction, Restrictions> {
    public static final ClientRestrictions INSTANCE = new ClientRestrictions(FluidSkills.RESTRICTIONS, Restrictions.INSTANCE);

    private ClientRestrictions(Registry<Restriction> registry, Restrictions serverApi) {
        super(registry, serverApi);
    }

    private List<Fluid> pluckTarget(List<Restriction> list) {
        return list.stream().map(r -> r.target).toList();
    }

    public List<Fluid> getHidden() {
        return pluckTarget(getFiltered(r -> !r.producible && !r.consumable));
    }

    public List<Fluid> getUnproducible() {
        return pluckTarget(getFiltered(r -> !r.producible));
    }

    public List<Fluid> getUnconsumable() {
        return pluckTarget(getFiltered(r -> !r.consumable));
    }

    public boolean isProducible(Fluid fluid) {
        return serverApi.isProducible(getPlayer(), fluid);
    }

    public boolean isConsumable(Fluid fluid) {
        return serverApi.isConsumable(getPlayer(), fluid);
    }

    public boolean isIdentifiable(Fluid fluid) {
        return serverApi.isIdentifiable(getPlayer(), fluid);
    }
}
