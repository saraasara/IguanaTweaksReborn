package insane96mcp.survivalreimagined.mixin;

import insane96mcp.survivalreimagined.module.farming.feature.Crops;
import net.minecraft.world.level.block.FarmBlock;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Debug(export = true)
@Mixin(FarmBlock.class)
public abstract class FarmBlockMixin {
    @ModifyConstant(method = "isNearWater", constant = {@Constant(intValue = 4), @Constant(intValue = -4)})
    private static int onWaterHydrationRadius(int radius) {
        return radius > 0 ? Crops.waterHydrationRadius : -Crops.waterHydrationRadius;
    }
}
