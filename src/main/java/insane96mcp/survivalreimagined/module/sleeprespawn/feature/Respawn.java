package insane96mcp.survivalreimagined.module.sleeprespawn.feature;

import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.LoadFeature;
import insane96mcp.insanelib.util.IdTagMatcher;
import insane96mcp.survivalreimagined.SurvivalReimagined;
import insane96mcp.survivalreimagined.base.SRFeature;
import insane96mcp.survivalreimagined.module.Modules;
import insane96mcp.survivalreimagined.module.misc.utils.IdTagValue;
import insane96mcp.survivalreimagined.module.sleeprespawn.block.RespawnObeliskBlock;
import insane96mcp.survivalreimagined.setup.SRBlocks;
import insane96mcp.survivalreimagined.setup.SRItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerSetSpawnEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@Label(name = "Respawn", description = "Changes to respawning")
@LoadFeature(module = Modules.Ids.SLEEP_RESPAWN)
public class Respawn extends SRFeature {

	public static final String LOOSE_RESPAWN_POINT_SET = SurvivalReimagined.MOD_ID + ".loose_bed_respawn_point_set";

	@Config(min = 0)
	@Label(name = "Loose World Spawn Range", description = "The range from world spawn where players will respawn.")
	public static Double looseWorldSpawnRange = 256d;

	@Config(min = 0)
	@Label(name = "Loose Bed Spawn Range", description = "The range from beds where players will respawn.")
	public static Double looseBedSpawnRange = 256d;

	public static final RegistryObject<RespawnObeliskBlock> RESPAWN_OBELISK = SRBlocks.REGISTRY.register("respawn_obelisk", () -> new RespawnObeliskBlock(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.COLOR_LIGHT_BLUE).requiresCorrectToolForDrops().strength(50.0F, 1200.0F).lightLevel(RespawnObeliskBlock::lightLevel)));

	public static final RegistryObject<BlockItem> RESPAWN_OBELISK_ITEM = SRItems.REGISTRY.register("respawn_obelisk", () -> new BlockItem(RESPAWN_OBELISK.get(), new Item.Properties()));

	public static final ArrayList<IdTagValue> RESPAWN_OBELISK_CATALYSTS_DEFAULT = new ArrayList<>(List.of(
			new IdTagValue(IdTagMatcher.Type.ID, "minecraft:iron_block", 0.8d),
			new IdTagValue(IdTagMatcher.Type.ID, "minecraft:gold_block", 0.3d),
			new IdTagValue(IdTagMatcher.Type.ID, "survivalreimagined:mithril_block", 0.1d),
			new IdTagValue(IdTagMatcher.Type.ID, "minecraft:diamond_block", 0.1d),
			new IdTagValue(IdTagMatcher.Type.ID, "minecraft:emerald_block", 0.5d),
			new IdTagValue(IdTagMatcher.Type.ID, "minecraft:netherite_block", 0.01d)
	));

	public static final ArrayList<IdTagValue> respawnObeliskCatalysts = new ArrayList<>();

	public Respawn(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
		JSON_CONFIGS.add(new JsonConfig<>("respawn_obelisk_catalysts.json", respawnObeliskCatalysts, RESPAWN_OBELISK_CATALYSTS_DEFAULT, IdTagValue.LIST_TYPE));
	}

	@Override
	public void loadJsonConfigs() {
		if (!this.isEnabled())
			return;
		super.loadJsonConfigs();
	}

	@SubscribeEvent
	public void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
		if (!this.isEnabled()
				|| event.isEndConquered())
			return;

		BlockPos respawn = looseWorldSpawn(event);
		if (respawn == null) {
			respawn = looseBedSpawn(event);
		}

		if (respawn != null)
			event.getEntity().teleportToWithTicket(respawn.getX() + 0.5d, respawn.getY() + 0.5d, respawn.getZ() + 0.5d);

		tryRespawnObelisk(event);
	}

	@Nullable
	private BlockPos looseWorldSpawn(PlayerEvent.PlayerRespawnEvent event) {
		if (looseWorldSpawnRange == 0d
				|| event.getEntity().isSpectator())
			return null;
		ServerPlayer player = (ServerPlayer) event.getEntity();
		BlockPos pos = player.getRespawnPosition();
		if (pos != null)
			return null;

		BlockPos respawn = BlockPos.randomInCube(player.getLevel().getRandom(), 1, player.getLevel().getSharedSpawnPos(), looseWorldSpawnRange.intValue()).iterator().next();
		int y = player.getLevel().getMaxBuildHeight();
		while (y > player.getLevel().getMinBuildHeight()) {
			respawn = new BlockPos(respawn.getX(), y - 1, respawn.getZ());
			BlockState state = player.getLevel().getBlockState(respawn);
			if (state.getMaterial().blocksMotion() || !state.getFluidState().isEmpty()) {
				respawn = new BlockPos(respawn.getX(), y, respawn.getZ());
				break;
			}
			y--;
		}
		return respawn;
	}

	@Nullable
	private BlockPos looseBedSpawn(PlayerEvent.PlayerRespawnEvent event) {
		if (looseBedSpawnRange == 0d
				|| event.getEntity().isSpectator())
			return null;
		ServerPlayer player = (ServerPlayer) event.getEntity();
		BlockPos pos = player.getRespawnPosition();
		if (pos == null
				|| !event.getEntity().getLevel().getBlockState(pos).is(BlockTags.BEDS))
			return null;

		BlockPos respawn = BlockPos.randomInCube(player.getLevel().getRandom(), 1, pos, looseWorldSpawnRange.intValue()).iterator().next();
		int y = player.getLevel().getMaxBuildHeight();
		while (y > player.getLevel().getMinBuildHeight()) {
			respawn = new BlockPos(respawn.getX(), y - 1, respawn.getZ());
			BlockState state = player.getLevel().getBlockState(respawn);
			if (state.getMaterial().blocksMotion() || !state.getFluidState().isEmpty()) {
				respawn = new BlockPos(respawn.getX(), y, respawn.getZ());
				break;
			}
			y--;
		}
		return respawn;
	}

	private void tryRespawnObelisk(PlayerEvent.PlayerRespawnEvent event) {
		ServerPlayer player = (ServerPlayer) event.getEntity();
		BlockPos pos = player.getRespawnPosition();
		if (pos == null
				|| !event.getEntity().getLevel().getBlockState(pos).is(RESPAWN_OBELISK.get()))
			return;

		RespawnObeliskBlock.onObeliskRespawn(event.getEntity(), event.getEntity().level, pos);
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public void onSetSpawnLooseMessage(PlayerSetSpawnEvent event) {
		if (!this.isEnabled()
				|| event.isForced()
				|| looseBedSpawnRange == 0d
				|| event.getNewSpawn() == null
				|| !event.getEntity().getLevel().getBlockState(event.getNewSpawn()).is(BlockTags.BEDS))
			return;

		ServerPlayer player = (ServerPlayer) event.getEntity();
		if (event.getNewSpawn().equals(player.getRespawnPosition()))
			return;
		player.displayClientMessage(Component.translatable(LOOSE_RESPAWN_POINT_SET), false);
	}

	@SubscribeEvent
	public void onSetSpawnPreventObeliskOverwrite(PlayerSetSpawnEvent event) {
		if (!this.isEnabled()
				|| event.isForced()
				|| !(event.getEntity() instanceof ServerPlayer player))
			return;

		if (player.getRespawnPosition() != null && player.level.getBlockState(player.getRespawnPosition()).is(RESPAWN_OBELISK.get())
			&& event.getNewSpawn() != null && !player.level.getBlockState(event.getNewSpawn()).is(RESPAWN_OBELISK.get())) {
			event.setCanceled(true);
		}
	}
}