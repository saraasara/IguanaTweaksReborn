package insane96mcp.survivalreimagined.module.mining.solarium;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;
import net.minecraft.world.level.material.Fluids;

import java.util.ArrayList;
import java.util.List;

public class SolariumBoudlerFeature extends Feature<BlockStateConfiguration> {

	public SolariumBoudlerFeature(Codec<BlockStateConfiguration> pCodec) {
		super(pCodec);
	}

	@Override
	public boolean place(FeaturePlaceContext<BlockStateConfiguration> context) {
		BlockPos blockPos = context.origin();
		WorldGenLevel worldgenlevel = context.level();
		RandomSource randomsource = context.random();
		List<BlockPos> bouldersPos = new ArrayList<>();
		for (int boulders = 0; boulders < 4; boulders++) {
			blockPos = blockPos.offset(randomsource.nextInt(6) - 1, 3, randomsource.nextInt(6) - 1);
			for(; blockPos.getY() > worldgenlevel.getMinBuildHeight() + 3; blockPos = blockPos.below()) {
				if (!worldgenlevel.isEmptyBlock(blockPos.below())) {
					BlockState blockstate = worldgenlevel.getBlockState(blockPos.below());
					if (isDirt(blockstate) || isStone(blockstate) || blockstate.is(BlockTags.SAND))
						break;
				}
			}
			blockPos = blockPos.below(2);
			bouldersPos.add(new BlockPos(blockPos));
			if (boulders >= 2 && randomsource.nextBoolean())
				break;
		}

		for (BlockPos pos : bouldersPos) {
			if (blockPos.getY() <= worldgenlevel.getMinBuildHeight() + 3)
				continue;

			int r = 3;
			float f = 2.5f + randomsource.nextFloat();
			for (BlockPos betweenClosedPos : BlockPos.betweenClosed(pos.offset(-r, -r, -r), pos.offset(r, r, r))) {
				if (betweenClosedPos.distSqr(pos) <= (double) (f * f)) {
					int b = randomsource.nextInt(100);
					if (b < 30)
						worldgenlevel.setBlock(betweenClosedPos, Blocks.STONE.defaultBlockState, 3);
					else if (b < 60)
						worldgenlevel.setBlock(betweenClosedPos, Blocks.COBBLESTONE.defaultBlockState, 3);
					else if (b < 80)
						worldgenlevel.setBlock(betweenClosedPos, Blocks.DIRT.defaultBlockState, 3);
					else
						worldgenlevel.setBlock(betweenClosedPos, Blocks.COARSE_DIRT.defaultBlockState, 3);
					if (randomsource.nextFloat() < 0.4f)
						tryPlaceSolium(worldgenlevel, betweenClosedPos);
				}
			}
		}

		return true;
	}

	public static void tryPlaceSolium(WorldGenLevel pLevel, BlockPos pPos) {
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos().set(pPos);
		for (Direction direction : Direction.values()) {
			mutableBlockPos.set(pPos.relative(direction));
			BlockState curBlockState = pLevel.getBlockState(mutableBlockPos);
			if (!curBlockState.canBeReplaced())
				continue;
			BlockState blockStateToPlace = Solarium.SOLIUM_MOSS.block().get().defaultBlockState;
			for (Direction mossDirection : Direction.values()) {
				if (!MultifaceBlock.canAttachTo(pLevel, mossDirection, mutableBlockPos, pLevel.getBlockState(mutableBlockPos.relative(mossDirection)))) {
					blockStateToPlace.setValue(MultifaceBlock.getFaceProperty(mossDirection), false);
					continue;
				}
				blockStateToPlace = blockStateToPlace.setValue(MultifaceBlock.getFaceProperty(mossDirection), true);
			}
			if (curBlockState.getFluidState().isSourceOfType(Fluids.WATER))
				blockStateToPlace = blockStateToPlace.setValue(BlockStateProperties.WATERLOGGED, Boolean.TRUE);
			pLevel.setBlock(mutableBlockPos, blockStateToPlace, 3);
			pLevel.getChunk(mutableBlockPos).markPosForPostprocessing(mutableBlockPos);
		}

	}
}
