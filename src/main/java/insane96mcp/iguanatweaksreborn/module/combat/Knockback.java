package insane96mcp.iguanatweaksreborn.module.combat;

import com.google.common.collect.Multimap;
import insane96mcp.iguanatweaksreborn.IguanaTweaksReborn;
import insane96mcp.iguanatweaksreborn.data.generator.ITRItemTagsProvider;
import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.iguanatweaksreborn.module.items.itemstats.ItemStats;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.LoadFeature;
import insane96mcp.insanelib.data.IdTagValue;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;
import java.util.UUID;

@Label(name = "Knockback", description = "Player will deal reduced knockback if attacking with a non-weapon or spamming.")
@LoadFeature(module = Modules.Ids.COMBAT)
public class Knockback extends Feature {

	public static final TagKey<Item> REDUCED_KNOCKBACK = ITRItemTagsProvider.create("reduced_knockback");
	public static final String TIME_SINCE_LAST_SWING = IguanaTweaksReborn.RESOURCE_PREFIX + "ticks_since_last_swing";
	public static final String SHOULD_APPLY_NO_KNOCKBACK = IguanaTweaksReborn.RESOURCE_PREFIX + "should_apply_no_knockback";

	@Config
	@Label(name = "No Weapon Penalty.Enabled", description = "If true the player will deal reduced knockback when not using an item that doesn't have the attack damage attribute.")
	public static Boolean noItemNoKnockback = true;

	@Config(min = 0d, max = 1d)
	@Label(name = "No Weapon Penalty.Knockback reduction", description = "Percentage knockback dealt when conditions are met.")
	public static Double knockbackReduction = 0.35d;

	public static final UUID SHOVELS_KNOCKBACK_MODIFIER_UUID = UUID.fromString("80d9e6b1-c385-49d4-aac0-cb018f4f1b16");
	@Config(min = 0d)
	@Label(name = "Shovels damage to knockback ratio")
	public static Double shovelsKnockbackRatio = 0.4d;

	//Knockback multipliers for items
	public static final List<IdTagValue> KNOCKBACKS = List.of(
			IdTagValue.newTag("minecraft:pickaxes", 0.75d),
			IdTagValue.newTag("minecraft:axes", 0.85d),
			IdTagValue.newTag("minecraft:hoes", 0.3d),
			IdTagValue.newTag("iguanatweaksexpanded:forge_hammers", 0.85d)
	);

	public Knockback(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
	}

	@SubscribeEvent
	public void onPlayerAttackEvent(AttackEntityEvent event) {
		if (!this.isEnabled())
			return;
		Player player = event.getEntity();
		if (player.getAbilities().instabuild)
			return;
		player.getPersistentData().putInt(TIME_SINCE_LAST_SWING, player.attackStrengthTicker);
	}

	@SubscribeEvent
	public void onLivingHurtEvent(LivingHurtEvent event) {
		if (!this.isEnabled()
				|| !(event.getSource().getDirectEntity() instanceof Player player))
			return;
		player.getPersistentData().putBoolean(SHOULD_APPLY_NO_KNOCKBACK, true);
	}

	@SubscribeEvent
	public void onKnockback(LivingKnockBackEvent event) {
		if (!this.isEnabled())
			return;

		reducedKnockback(event);
		itemKnockbackReduction(event);
	}

	public void reducedKnockback(LivingKnockBackEvent event) {
		if (event.getEntity().lastHurtByPlayer == null
				|| event.getEntity().lastHurtByPlayerTime != 100
				|| !(event.getEntity().getLastHurtByMob() instanceof Player player)
				|| player.getAbilities().instabuild)
			return;

		if (!player.getPersistentData().getBoolean(SHOULD_APPLY_NO_KNOCKBACK))
			return;

		ItemStack itemStack = player.getMainHandItem();

		boolean reducedKnockback = false;
		Multimap<Attribute, AttributeModifier> attributeModifiers = itemStack.getAttributeModifiers(EquipmentSlot.MAINHAND);
		if ((!attributeModifiers.containsKey(Attributes.ATTACK_DAMAGE) && noItemNoKnockback) || itemStack.is(REDUCED_KNOCKBACK) || (isEnabled(ItemStats.class) && ItemStats.unbreakableItems && ItemStats.isBroken(itemStack)))
			reducedKnockback = true;

		int ticksSinceLastSwing = player.getPersistentData().getInt(TIME_SINCE_LAST_SWING);
		float cooldown = Mth.clamp((ticksSinceLastSwing + 0.5f) / player.getCurrentItemAttackStrengthDelay(), 0.0F, 1.0F);
		if (cooldown <= 0.9f)
			reducedKnockback = true;
		if (reducedKnockback)
			event.setStrength(event.getStrength() * knockbackReduction.floatValue());
		player.getPersistentData().putBoolean(SHOULD_APPLY_NO_KNOCKBACK, false);
	}

	@SubscribeEvent
	public void itemKnockbackReduction(LivingKnockBackEvent event) {
		if (event.getEntity().getLastHurtByMob() == null)
			return;
		float multiplier = 1f;
		for (IdTagValue idTagValue : KNOCKBACKS) {
			if (idTagValue.id.matchesItem(event.getEntity().getLastHurtByMob().getMainHandItem()))
				multiplier = (float) idTagValue.value;
		}
		event.setStrength(event.getStrength() * multiplier);
	}

	@SubscribeEvent
	public void addAttributeToShovels(ItemAttributeModifierEvent event) {
		if (!this.isEnabled()
				|| event.getSlotType() != EquipmentSlot.MAINHAND
				|| !(event.getItemStack().getItem() instanceof ShovelItem shovelItem)
				|| !(event.getItemStack().is(ItemTags.SHOVELS))
				|| (isEnabled(ItemStats.class) && ItemStats.unbreakableItems && ItemStats.isBroken(event.getItemStack())))
			return;

		event.addModifier(Attributes.ATTACK_KNOCKBACK, new AttributeModifier(SHOVELS_KNOCKBACK_MODIFIER_UUID, "Shovels Knockback modifier", shovelItem.getAttackDamage() * shovelsKnockbackRatio, AttributeModifier.Operation.ADDITION));
	}
}