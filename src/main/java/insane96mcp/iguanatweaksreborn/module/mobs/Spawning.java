package insane96mcp.iguanatweaksreborn.module.mobs;

import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.LoadFeature;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Label(name = "Spawning", description = "Changes to mob spawn")
@LoadFeature(module = Modules.Ids.MOBS)
public class Spawning extends Feature {
    @Config
    @Label(name = "No Zombie Villagers", description = "Disables Zombie Villagers")
    public static Boolean noZombieVillagers = true;
    @Config
    @Label(name = "Allow world spawn spawn", description = "Allows mobs to spawn in the world spawn (in vanilla mobs can't spawn in a 24 blocks radius from world spawn)")
    public static Boolean allowWorldSpawnSpawn = true;

    public Spawning(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super(module, enabledByDefault, canBeDisabled);
    }

    @SubscribeEvent
    public void onZombieVillagerSpawn(MobSpawnEvent.FinalizeSpawn event) {
        if (!this.isEnabled()
                || !noZombieVillagers
                || event.getEntity().getType() != EntityType.ZOMBIE_VILLAGER
                || event.getEntity().isAddedToWorld())
            return;

        event.setSpawnCancelled(true);
        event.setCanceled(true);
    }
}
