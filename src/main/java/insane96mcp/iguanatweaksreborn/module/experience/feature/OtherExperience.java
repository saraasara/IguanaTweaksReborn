package insane96mcp.iguanatweaksreborn.module.experience.feature;

import insane96mcp.iguanatweaksreborn.setup.Config;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.projectile.ThrownExperienceBottle;
import net.minecraftforge.common.ForgeConfigSpec;

@Label(name = "Other Experience", description = "Change other experience sources")
public class OtherExperience extends Feature {

	private final ForgeConfigSpec.IntValue xpBottleBonusConfig;
	private final ForgeConfigSpec.IntValue anvilRepairCapConfig;
	private final ForgeConfigSpec.BooleanValue freeRenamingConfig;

	public int xpBottleBonus = 18;
	public int anvilRepairCap = 2048;
	public boolean freeRenaming = true;

	public OtherExperience(Module module) {
		super(Config.builder, module, true);
		this.pushConfig(Config.builder);
		xpBottleBonusConfig = Config.builder
				.comment("Bottle o' enchanting will drop this more XP. Experience is still affected by 'Global Experience Multiplier'\nCan be set to 0 to make Bottle o' enchanting drop no experience")
				.defineInRange("Bottle o' Enchanting Bonus XP", this.xpBottleBonus, 0, 1024);
		anvilRepairCapConfig = Config.builder
				.comment("Set the cap for repairing items in the anvil (vanilla is 40)")
				.defineInRange("Anvil Repair Cap", this.anvilRepairCap, 1, Integer.MAX_VALUE);
		freeRenamingConfig = Config.builder
				.comment("Removes cost of renaming items in Anvil")
				.define("Remove rename cost", this.freeRenaming);
		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		this.xpBottleBonus = this.xpBottleBonusConfig.get();
		this.anvilRepairCap = this.anvilRepairCapConfig.get();
		this.freeRenaming = this.freeRenamingConfig.get();
	}

	public void onXpBottleHit(ThrownExperienceBottle xpBottle) {
		if (!this.isEnabled())
			return;

		if (this.xpBottleBonus == 0)
			return;

		if (xpBottle.level instanceof ServerLevel) {
			ExperienceOrb.award((ServerLevel)xpBottle.level, xpBottle.position(), this.xpBottleBonus);
		}
	}

	public boolean isFreeRenaming() {
		return this.isEnabled() && this.freeRenaming;
	}
}