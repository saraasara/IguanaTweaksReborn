package insane96mcp.survivalreimagined.data.lootmodifier;

import insane96mcp.survivalreimagined.module.experience.feature.EnchantmentsFeature;
import insane96mcp.survivalreimagined.module.farming.feature.Crops;
import insane96mcp.survivalreimagined.module.farming.feature.Livestock;
import insane96mcp.survivalreimagined.module.items.feature.FlintTools;
import insane96mcp.survivalreimagined.module.mining.feature.Iron;
import insane96mcp.survivalreimagined.module.mobs.feature.Equipment;
import insane96mcp.survivalreimagined.module.world.feature.Loot;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.GlobalLootModifierProvider;

public class SRGlobalLootModifierProvider extends GlobalLootModifierProvider {
    public SRGlobalLootModifierProvider(PackOutput output, String modid) {
        super(output, modid);
    }

    @Override
    protected void start() {
        Iron.addGlobalLoot(this);
        Crops.addGlobalLoot(this);
        EnchantmentsFeature.addGlobalLoot(this);
        Livestock.addGlobalLoot(this);
        Equipment.addGlobalLoot(this);
        Loot.addGlobalLoot(this);
        FlintTools.addGlobalLoot(this);
    }
}
