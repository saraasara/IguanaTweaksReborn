package insane96mcp.iguanatweaksreborn.module.farming.feature;

import insane96mcp.iguanatweaksreborn.IguanaTweaksReborn;
import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.iguanatweaksreborn.setup.Strings;
import insane96mcp.iguanatweaksreborn.utils.Utils;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.LoadFeature;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

@Label(name = "Livestock Slowdown", description = "Slower breeding, Growing, Egging and Milking")
@LoadFeature(module = Modules.Ids.FARMING)
public class LivestockSlowdown extends Feature {

	private static final ResourceLocation NO_LIVESTOCK_SLOWDOWN = new ResourceLocation(IguanaTweaksReborn.MOD_ID, "no_livestock_slowdown");

	@Config(min = 1d, max = 128d)
	@Label(name = "Childs Growth Multiplier", description = "Increases the time required for Baby Animals to grow (e.g. at 2.0 Animals will take twice to grow).\n1.0 will make Animals grow like normal.")
	public static Double childGrowthMultiplier = 3.0d;
	@Config
	@Label(name = "Childs Growth Villagers", description = "If true, 'Childs Growth Multiplier' will be applied to villagers too.")
	public static Boolean childGrowthVillagers = true;
	@Config(min = 1d, max = 128d)
	@Label(name = "Breeding Time Multiplier", description = "Increases the time required for Animals to breed again (e.g. at 2.0 Animals will take twice to be able to breed again).\n1.0 will make Animals breed like normal.")
	public static Double breedingMultiplier = 3.5d;
	@Config(min = 1d, max = 128d)
	@Label(name = "Egg Lay Multiplier", description = "Increases the time required for Chickens to lay an egg (e.g. at 2.0 Chickens will take twice the time to lay an egg).\n1.0 will make chickens lay eggs like normal.")
	public static Double eggLayMultiplier = 3.0d;
	@Config(min = 0)
	@Label(name = "Cow Milk Delay", description = "Seconds before a cow can be milked again. This applies to Mooshroom stew too.\n0 will disable this feature.")
	public static Integer cowMilkDelay = 1200;

	public LivestockSlowdown(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
	}

	@SubscribeEvent
	public void slowdownAnimalGrowth(LivingEvent.LivingTickEvent event) {
		if (!this.isEnabled()
				|| childGrowthMultiplier == 1d
				|| isEntityBlacklisted(event.getEntity()))
			return;
		if (!(event.getEntity() instanceof Animal) && !(event.getEntity() instanceof AbstractVillager))
			return;
		if (event.getEntity() instanceof AbstractVillager && !childGrowthVillagers)
			return;
		AgeableMob entity = (AgeableMob) event.getEntity();
		int growingAge = entity.getAge();
		if (growingAge >= 0)
			return;
		double chance = 1d / childGrowthMultiplier;
		if (entity.getRandom().nextFloat() > chance)
			entity.setAge(growingAge - 1);
	}

	@SubscribeEvent
	public void slowdownBreeding(LivingEvent.LivingTickEvent event) {
		if (!this.isEnabled()
				|| breedingMultiplier == 1d
				|| !(event.getEntity() instanceof Animal)
				|| isEntityBlacklisted(event.getEntity()))
			return;
		AgeableMob entity = (AgeableMob) event.getEntity();
		int growingAge = entity.getAge();
		if (growingAge <= 0)
			return;
		double chance = 1d / breedingMultiplier;
		if (entity.getRandom().nextFloat() > chance)
			entity.setAge(growingAge + 1);
	}

	@SubscribeEvent
	public void slowdownEggLay(LivingEvent.LivingTickEvent event) {
		if (!this.isEnabled()
				|| eggLayMultiplier == 1d
				|| !(event.getEntity() instanceof Chicken chicken)
				|| isEntityBlacklisted(chicken))
			return;
		int timeUntilNextEgg = chicken.eggTime;
		if (timeUntilNextEgg < 0)
			return;
		double chance = 1d / eggLayMultiplier;
		if (chicken.getRandom().nextFloat() > chance)
			chicken.eggTime += 1;
	}

	@SubscribeEvent
	public void cowMilkTick(LivingEvent.LivingTickEvent event) {
		if (!this.isEnabled()
				|| cowMilkDelay == 0
				|| event.getEntity().tickCount % 20 != 0
				|| !(event.getEntity() instanceof Cow cow)
				|| isEntityBlacklisted(cow))
			return;
		CompoundTag cowNBT = cow.getPersistentData();
		int milkCooldown = cowNBT.getInt(Strings.Tags.MILK_COOLDOWN);
		if (milkCooldown > 0)
			milkCooldown -= 20;
		cowNBT.putInt(Strings.Tags.MILK_COOLDOWN, milkCooldown);
	}

	@SubscribeEvent
	public void onCowMilk(PlayerInteractEvent.EntityInteract event) {
		if (!this.isEnabled()
				|| cowMilkDelay == 0
				|| !(event.getTarget() instanceof Cow cow)
				|| isEntityBlacklisted(cow)
				|| cow.getAge() < 0)
			return;
		Player player = event.getEntity();
		InteractionHand hand = event.getHand();
		ItemStack equipped = player.getItemInHand(hand);
		if (equipped.isEmpty() || equipped.getItem() == Items.AIR)
			return;
		Item item = equipped.getItem();
		if ((!FluidUtil.getFluidHandler(equipped).isPresent() || !FluidStack.loadFluidStackFromNBT(equipped.getTag()).isEmpty()) && (!(cow instanceof MushroomCow) || item != Items.BOWL))
			return;
		CompoundTag cowNBT = cow.getPersistentData();
		int milkCooldown = cowNBT.getInt(Strings.Tags.MILK_COOLDOWN);
		if (milkCooldown > 0) {
			event.setCanceled(true);
			if (!player.level.isClientSide) {
				cow.playSound(SoundEvents.COW_HURT, 0.4F, (event.getEntity().level.random.nextFloat() - event.getEntity().level.random.nextFloat()) * 0.2F + 1.2F);
				String animal = cow instanceof MushroomCow ? Strings.Translatable.MOOSHROOM_COOLDOWN : Strings.Translatable.COW_COOLDOWN;
				String yetReady = Strings.Translatable.YET_READY;
				MutableComponent message = Component.translatable(animal).append(" ").append(Component.translatable(yetReady));
				player.displayClientMessage(message, true);
			}
			else
				event.setCancellationResult(InteractionResult.SUCCESS);
		}
		else {
			milkCooldown = cowMilkDelay * 20;
			cowNBT.putInt(Strings.Tags.MILK_COOLDOWN, milkCooldown);
			player.swing(event.getHand());
		}
	}

	public static boolean isEntityBlacklisted(Entity entity) {
		return Utils.isEntityInTag(entity, NO_LIVESTOCK_SLOWDOWN);
	}
}