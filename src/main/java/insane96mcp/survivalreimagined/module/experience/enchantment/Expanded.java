package insane96mcp.survivalreimagined.module.experience.enchantment;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import insane96mcp.survivalreimagined.setup.SREnchantments;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.BlockDestructionProgress;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.enchantment.DiggingEnchantment;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.model.data.ModelData;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Expanded extends Enchantment {
    public Expanded() {
        super(Rarity.RARE, EnchantmentCategory.DIGGER, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override
    public int getMaxLevel() {
        return 2;
    }

    @Override
    public int getMinCost(int level) {
        return 22 * level;
    }

    @Override
    public int getMaxCost(int level) {
        return this.getMinCost(level) + 22;
    }

    public boolean checkCompatibility(Enchantment other) {
        return !(other instanceof DiggingEnchantment) && !(other instanceof Blasting) && super.checkCompatibility(other);
    }

    public static void apply(LivingEntity entity, Level level, BlockPos pos, Direction face, BlockState state) {
        ItemStack heldStack = entity.getMainHandItem();
        if (!heldStack.isCorrectToolForDrops(state))
            return;
        int enchLevel = heldStack.getEnchantmentLevel(SREnchantments.EXPANDED.get());
        if (enchLevel == 0)
            return;
        List<BlockPos> minedBlocks = getMinedBlocks(enchLevel, heldStack.getItem() instanceof PickaxeItem || heldStack.getItem() instanceof ShovelItem, level, entity, pos, face);
        for (BlockPos minedBlock : minedBlocks) {
            if (level instanceof ServerLevel) {
                BlockState minedBlockState = level.getBlockState(minedBlock);
                BlockEntity blockEntity = state.hasBlockEntity() ? level.getBlockEntity(minedBlock) : null;
                LootContext.Builder lootcontext$builder = (new LootContext.Builder((ServerLevel) level)).withRandom(level.getRandom()).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(minedBlock)).withParameter(LootContextParams.TOOL, entity.getMainHandItem()).withOptionalParameter(LootContextParams.BLOCK_ENTITY, blockEntity).withOptionalParameter(LootContextParams.THIS_ENTITY, entity);
                minedBlockState.getDrops(lootcontext$builder).forEach((stack) -> {
                    ItemEntity drop = new ItemEntity(level, minedBlock.getX() + 0.5d, minedBlock.getY() + 0.5d, minedBlock.getZ() + 0.5d, stack);
                    drop.setDefaultPickUpDelay();
                    level.addFreshEntity(drop);
                });
                heldStack.hurtAndBreak(1, entity, l -> {
                    l.broadcastBreakEvent(InteractionHand.MAIN_HAND);
                });
            }
            level.destroyBlock(minedBlock, false, entity);
            if (heldStack.isEmpty())
                break;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void applyDestroyAnimation(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_SOLID_BLOCKS)
            return;
        // validate required variables are set
        MultiPlayerGameMode controller = Minecraft.getInstance().gameMode;
        if (controller == null || !controller.isDestroying()) {
            return;
        }
        Level level = Minecraft.getInstance().level;
        Player player = Minecraft.getInstance().player;
        if (level == null || player == null || Minecraft.getInstance().getCameraEntity() == null) {
            return;
        }
        // must have the enchantment
        int enchLevel = player.getMainHandItem().getEnchantmentLevel(SREnchantments.EXPANDED.get());
        if (enchLevel == 0)
            return;
        // must be targeting a block
        HitResult result = Minecraft.getInstance().hitResult;
        if (result == null || result.getType() != HitResult.Type.BLOCK) {
            return;
        }
        // find breaking progress
        BlockHitResult blockTrace = (BlockHitResult)result;
        BlockPos targetPos = blockTrace.getBlockPos();
        BlockState targetState = level.getBlockState(targetPos);
        ItemStack heldStack = player.getMainHandItem();
        if (!heldStack.isCorrectToolForDrops(targetState))
            return;
        BlockDestructionProgress progress = null;
        for (Int2ObjectMap.Entry<BlockDestructionProgress> entry : Minecraft.getInstance().levelRenderer.destroyingBlocks.int2ObjectEntrySet()) {
            if (entry.getValue().getPos().equals(targetPos)) {
                progress = entry.getValue();
                break;
            }
        }
        if (progress == null) {
            return;
        }
        // determine extra blocks to highlight
        List<BlockPos> minedBlocks = getMinedBlocks(enchLevel, heldStack.getItem() instanceof PickaxeItem || heldStack.getItem() instanceof ShovelItem, level, player, targetPos, blockTrace.getDirection());
        if (minedBlocks.isEmpty()) {
            return;
        }

        // set up buffers
        PoseStack matrices = event.getPoseStack();
        matrices.pushPose();
        MultiBufferSource.BufferSource vertices = event.getLevelRenderer().renderBuffers.crumblingBufferSource();
        VertexConsumer vertexBuilder = vertices.getBuffer(ModelBakery.DESTROY_TYPES.get(progress.getProgress()));

        // finally, render the blocks
        Camera renderInfo = Minecraft.getInstance().gameRenderer.getMainCamera();
        double x = renderInfo.getPosition().x;
        double y = renderInfo.getPosition().y;
        double z = renderInfo.getPosition().z;
        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
        for (BlockPos minedPos : minedBlocks) {
            matrices.pushPose();
            matrices.translate(minedPos.getX() - x, minedPos.getY() - y, minedPos.getZ() - z);
            PoseStack.Pose entry = matrices.last();
            VertexConsumer blockBuilder = new SheetedDecalTextureGenerator(vertexBuilder, entry.pose(), entry.normal(), 1f);
            dispatcher.renderBreakingTexture(level.getBlockState(minedPos), minedPos, level, matrices, blockBuilder, ModelData.EMPTY);
            matrices.popPose();
        }
        // finish rendering
        matrices.popPose();
        vertices.endBatch();
    }

    public static List<BlockPos> getMinedBlocks(int expansion, boolean square, Level level, LivingEntity entity, BlockPos targetPos, Direction face) {
        List<BlockPos> minedBlocks = new ArrayList<>();
        boolean upDown = false;
        if (face == Direction.UP || face == Direction.DOWN) {
            face = entity.getDirection();
            upDown = true;
        }

        if (square) {
            if (!upDown) {
                if (expansion >= 1) {
                    addIfCanBeMined(minedBlocks, level, targetPos, targetPos.below());
                    addIfCanBeMined(minedBlocks, level, targetPos, targetPos.above());
                }
                if (expansion >= 2) {
                    addIfCanBeMined(minedBlocks, level, targetPos, targetPos.relative(face.getClockWise()).above());
                    addIfCanBeMined(minedBlocks, level, targetPos, targetPos.relative(face.getCounterClockWise()).above());
                    addIfCanBeMined(minedBlocks, level, targetPos, targetPos.relative(face.getClockWise()).below());
                    addIfCanBeMined(minedBlocks, level, targetPos, targetPos.relative(face.getCounterClockWise()).below());
                }
            }
            else {
                if (expansion >= 1) {
                    addIfCanBeMined(minedBlocks, level, targetPos, targetPos.relative(face));
                    addIfCanBeMined(minedBlocks, level, targetPos, targetPos.relative(face.getOpposite()));
                }
                if (expansion >= 2) {
                    addIfCanBeMined(minedBlocks, level, targetPos, targetPos.relative(face.getClockWise()).relative(face));
                    addIfCanBeMined(minedBlocks, level, targetPos, targetPos.relative(face.getCounterClockWise()).relative(face));
                    addIfCanBeMined(minedBlocks, level, targetPos, targetPos.relative(face.getClockWise()).relative(face.getOpposite()));
                    addIfCanBeMined(minedBlocks, level, targetPos, targetPos.relative(face.getCounterClockWise()).relative(face.getOpposite()));
                }
            }
            if (expansion >= 2) {
                addIfCanBeMined(minedBlocks, level, targetPos, targetPos.relative(face.getClockWise()));
                addIfCanBeMined(minedBlocks, level, targetPos, targetPos.relative(face.getCounterClockWise()));
            }
        }
        else {
            List<BlockPos> posToCheck = new ArrayList<>();
            posToCheck.add(targetPos);
            List<BlockPos> explored = new ArrayList<>();
            AtomicInteger toMine = new AtomicInteger(0);
            while (!posToCheck.isEmpty() && toMine.intValue() < (expansion == 1 ? 3 - 1 : 9 - 1)) {
                List<BlockPos> posToCheckTmp = new ArrayList<>();
                for (BlockPos pos : posToCheck) {
                    Direction.stream().forEach(direction -> {
                        BlockPos relativePos = pos.relative(direction);
                        if (explored.contains(relativePos))
                            return;
                        if (addIfCanBeMined(minedBlocks, level, targetPos, relativePos)) {
                            toMine.incrementAndGet();
                            posToCheckTmp.add(relativePos);
                        }
                        explored.add(relativePos);
                    });
                    if (toMine.intValue() >= (expansion == 1 ? 3 - 1 : 9 - 1)) {
                        posToCheckTmp.clear();
                        break;
                    }
                }
                posToCheck.clear();
                posToCheck.addAll(posToCheckTmp);
            }
        }

        return minedBlocks;
    }

    private static boolean addIfCanBeMined(List<BlockPos> blockPos, Level level, BlockPos targetPos, BlockPos minedPos) {
        BlockState targetState = level.getBlockState(targetPos);
        BlockState minedState = level.getBlockState(minedPos);
        if (targetState.getMaterial() == minedState.getMaterial()
                && targetState.getDestroySpeed(level, targetPos) >= minedState.getDestroySpeed(level, minedPos) - 0.5d) {
            blockPos.add(minedPos);
            return true;
        }
        return false;
    }
}
