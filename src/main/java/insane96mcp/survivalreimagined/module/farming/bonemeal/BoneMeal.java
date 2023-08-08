package insane96mcp.survivalreimagined.module.farming.bonemeal;

import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.LoadFeature;
import insane96mcp.survivalreimagined.base.SimpleBlockWithItem;
import insane96mcp.survivalreimagined.data.criterion.MakeRichFarmlandTrigger;
import insane96mcp.survivalreimagined.data.generator.SRBlockTagsProvider;
import insane96mcp.survivalreimagined.data.generator.SRItemTagsProvider;
import insane96mcp.survivalreimagined.module.Modules;
import insane96mcp.survivalreimagined.module.farming.crops.Crops;
import insane96mcp.survivalreimagined.utils.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.event.entity.player.BonemealEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.config.ModConfigEvent;

import java.util.Collections;

@Label(name = "Bone meal", description = "Bone meal is no longer so OP and also Rich Farmland")
@LoadFeature(module = Modules.Ids.FARMING)
public class BoneMeal extends Feature {

	public static final SimpleBlockWithItem RICH_FARMLAND = SimpleBlockWithItem.register("rich_farmland", () -> new RichFarmlandBlock(BlockBehaviour.Properties.of().mapColor(MapColor.DIRT).randomTicks().strength(0.6F).sound(SoundType.GRAVEL).isViewBlocking((state, blockGetter, pos) -> true).isSuffocating((state, blockGetter, pos) -> true)));

	public static final TagKey<Item> ITEM_BLACKLIST = SRItemTagsProvider.create("nerfed_bone_meal_blacklist");
	public static final TagKey<Block> BLOCK_BLACKLIST = SRBlockTagsProvider.create("nerfed_bone_meal_blacklist");
	@Config
	@Label(name = "Nerfed Bone Meal", description = "Makes more Bone Meal required for Crops. Valid Values are\nNO: No Bone Meal changes\nSLIGHT: Makes Bone Meal grow 1-2 crop stages\nNERFED: Makes Bone Meal grow only 1 Stage")
	public static BoneMealNerf nerfedBoneMeal = BoneMealNerf.NERFED;
	@Config(min = 0d, max = 1d)
	@Label(name = "Bone Meal Fail Chance", description = "Makes Bone Meal have a chance to fail to grow crops. 0 to disable, 1 to disable Bone Meal.")
	public static Double boneMealFailChance = 0d;

	@Config
	@Label(name = "Transform Farmland in Rich Farmland", description = "Bone meal used on Farmland transforms it into Rich Farmland.")
	public static Boolean farmlandToRich = true;
	@Config(min = 1)
	@Label(name = "Rich Farmland Extra Ticks", description = "How many extra random ticks does Rich Farmland give to the crop sitting on top?")
	public static Integer richFarmlandExtraTicks = 2;

	@Config(min = 0d, max = 1d)
	@Label(name = "Rich Farmland Chance to Decay", description = "Chance for a Rich farmland to decay back to farmland")
	public static Double richFarmlandChanceToDecay = 0.4d;

	@Config
	@Label(name = "Bone meal dirt to grass", description = "If true, you can bone meal dirt that's near a grass block to get grass block.")
	public static Boolean boneMealDirtToGrass = true;

	@Config
	@Label(name = "Compostable Rotten Flesh")
	public static Boolean compostableRottenFlesh = true;

	public BoneMeal(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
	}

	@Override
	public void readConfig(ModConfigEvent event) {
		super.readConfig(event);
		if (compostableRottenFlesh)
			ComposterBlock.COMPOSTABLES.put(Items.ROTTEN_FLESH, 0.5f);
		else
			ComposterBlock.COMPOSTABLES.removeFloat(Items.ROTTEN_FLESH);
	}

	/**
	 * Handles part of crops require water too
	 */
	@SubscribeEvent
	public void onBonemeal(BonemealEvent event) {
		if (event.isCanceled()
				|| event.getResult() == Event.Result.DENY
				|| event.isCanceled()
				|| !this.isEnabled()
				|| event.getLevel().isClientSide)
			return;
		if (farmlandToRich){
			BlockPos farmlandPos = null;
			if (event.getBlock().is(Blocks.FARMLAND))
				farmlandPos = event.getPos();
			else if (event.getLevel().getBlockState(event.getPos().below()).is(Blocks.FARMLAND) && event.getEntity().isCrouching())
				farmlandPos = event.getPos().below();
			if (farmlandPos != null) {
				event.getLevel().setBlockAndUpdate(farmlandPos, RICH_FARMLAND.block().get().defaultBlockState().setValue(FarmBlock.MOISTURE, event.getLevel().getBlockState(farmlandPos).getValue(FarmBlock.MOISTURE)));
				event.getEntity().swing(event.getEntity().getMainHandItem().getItem() == event.getStack().getItem() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND, true);
				event.setResult(Event.Result.ALLOW);
				MakeRichFarmlandTrigger.TRIGGER.trigger((ServerPlayer) event.getEntity());
			}
		}
		if (event.getResult() != Event.Result.ALLOW) {
			BoneMealResult result = applyBoneMeal(event.getLevel(), event.getStack(), event.getBlock(), event.getPos());
			if (result == BoneMealResult.ALLOW)
				event.setResult(Event.Result.ALLOW);
			else if (result == BoneMealResult.CANCEL)
				event.setCanceled(true);
		}

		if (event.getResult() != Event.Result.ALLOW) {
			tryBoneMealDirt(event, event.getLevel(), event.getBlock(), event.getPos());
		}
	}

	private void tryBoneMealDirt(BonemealEvent event, Level level, BlockState state, BlockPos pos) {
		if (!state.is(Blocks.DIRT)
				|| !level.getBlockState(pos.above()).isAir())
			return;

		for (Direction direction : Direction.values()) {
			if (direction == Direction.UP || direction == Direction.DOWN)
				continue;

			if (level.getBlockState(pos.relative(direction)).is(Blocks.GRASS_BLOCK)) {
				level.setBlockAndUpdate(pos, Blocks.GRASS_BLOCK.defaultBlockState());
				event.getEntity().swing(event.getEntity().getMainHandItem().getItem() == event.getStack().getItem() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND, true);
				event.setResult(Event.Result.ALLOW);
				break;
			}
		}
	}

	public enum BoneMealResult {
		NONE,
		CANCEL,
		ALLOW
	}



	public BoneMealResult applyBoneMeal(Level level, ItemStack stack, BlockState state, BlockPos pos) {
		if (Utils.isItemInTag(stack.getItem(), ITEM_BLACKLIST) || Utils.isBlockInTag(state.getBlock(), BLOCK_BLACKLIST))
			return BoneMealResult.NONE;

		//If farmland is dry and cropsRequireWater is enabled then cancel the event
		if (Crops.requiresWetFarmland(level, pos) && !Crops.hasWetFarmland(level, pos)) {
			return BoneMealResult.CANCEL;
		}

		if (nerfedBoneMeal.equals(BoneMealNerf.NO))
			return BoneMealResult.NONE;
		if (state.getBlock() instanceof CropBlock cropBlock) {
			int age = state.getValue(cropBlock.getAgeProperty());
			int maxAge = Collections.max(cropBlock.getAgeProperty().getPossibleValues());
			if (age == maxAge) {
				return BoneMealResult.NONE;
			}

			if (level.getRandom().nextDouble() < boneMealFailChance) {
				return BoneMealResult.ALLOW;
			} else if (nerfedBoneMeal.equals(BoneMealNerf.SLIGHT)) {
				age += Mth.nextInt(level.getRandom(), 1, 2);
			} else if (nerfedBoneMeal.equals(BoneMealNerf.NERFED)) {
				age++;
			}
			age = Mth.clamp(age, 0, maxAge);
			state = state.setValue(cropBlock.getAgeProperty(), age);
		} else if (state.getBlock() instanceof StemBlock) {
			int age = state.getValue(StemBlock.AGE);
			int maxAge = Collections.max(StemBlock.AGE.getPossibleValues());
			if (age == maxAge) {
				return BoneMealResult.NONE;
			}

			if (level.getRandom().nextDouble() < boneMealFailChance) {
				return BoneMealResult.ALLOW;
			} else if (nerfedBoneMeal.equals(BoneMealNerf.SLIGHT)) {
				age += Mth.nextInt(level.getRandom(), 1, 2);
			} else if (nerfedBoneMeal.equals(BoneMealNerf.NERFED)) {
				age++;
			}
			age = Mth.clamp(age, 0, maxAge);
			state = state.setValue(StemBlock.AGE, age);
		} else
			return BoneMealResult.NONE;
		level.setBlockAndUpdate(pos, state);
		return BoneMealResult.ALLOW;
	}

	public enum BoneMealNerf {
		NO,
		SLIGHT,
		NERFED
	}
}
