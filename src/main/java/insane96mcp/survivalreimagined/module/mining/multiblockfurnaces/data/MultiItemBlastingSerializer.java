package insane96mcp.survivalreimagined.module.mining.multiblockfurnaces.data;

import insane96mcp.survivalreimagined.module.mining.multiblockfurnaces.crafting.MultiItemBlastingRecipe;
import insane96mcp.survivalreimagined.setup.client.SRBookCategory;

public class MultiItemBlastingSerializer extends AbstractMultiItemSmeltingSerializer {

    public MultiItemBlastingSerializer() {
        super(MultiItemBlastingRecipe::new);
    }

    @Override
    protected SRBookCategory getDefaultBookCategory() {
        return SRBookCategory.BLAST_FURNACE_MISC;
    }

    @Override
    int getIngredientSlotsCount() {
        return 4;
    }
}