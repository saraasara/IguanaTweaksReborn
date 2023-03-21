package insane96mcp.survivalreimagined.module.hungerhealth.feature;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.LoadFeature;
import insane96mcp.insanelib.base.config.MinMax;
import insane96mcp.survivalreimagined.SurvivalReimagined;
import insane96mcp.survivalreimagined.module.Modules;
import insane96mcp.survivalreimagined.module.movement.feature.Stamina;
import insane96mcp.survivalreimagined.network.MessageFoodRegenSync;
import insane96mcp.survivalreimagined.network.NetworkHandler;
import insane96mcp.survivalreimagined.setup.SRMobEffects;
import insane96mcp.survivalreimagined.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.GuiOverlayManager;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.NetworkDirection;

@Label(name = "No Hunger", description = "Remove hunger and get back to the Beta 1.7.3 days.")
@LoadFeature(module = Modules.Ids.HUNGER_HEALTH)
public class NoHunger extends Feature {

    //TODO Add restore speed and amount of food in tooltip
    private static final String PASSIVE_REGEN_TICK = SurvivalReimagined.RESOURCE_PREFIX + "passive_regen_ticks";
    private static final String FOOD_REGEN_LEFT = SurvivalReimagined.RESOURCE_PREFIX + "food_regen_left";
    private static final String FOOD_REGEN_STRENGTH = SurvivalReimagined.RESOURCE_PREFIX + "food_regen_strength";

    private static final ResourceLocation RAW_FOOD = new ResourceLocation(SurvivalReimagined.MOD_ID, "raw_food");

    @Config
    @Label(name = "Disable Hunger", description = "Completely disables the entire hunger system, from the hunger bar, to the health regen that comes with it.")
    public static Boolean disableHunger = true;
    @Config
    @Label(name = "Passive Health Regen.Enable Passive Health Regen", description = "If true, Passive Regeneration is enabled")
    public static Boolean enablePassiveRegen = true;
    @Config
    @Label(name = "Passive Health Regen.Regen Speed", description = "Min represents how many seconds the regeneration of 1 HP takes when health is 100%, Max how many seconds when health is 0%")
    public static MinMax passiveRegenerationTime = new MinMax(40, 60);
    @Config(min = -1)
    @Label(name = "Food Gives Well Fed when Saturation Modifier >", description = "When saturation modifier of the food eaten is higher than this value, the Well Fed effect is given. Set to -1 to disable the effect.\n" +
            "Well Fed increases passive health regen speed by 40%")
    public static Double foodGivesWellFedWhenSaturationModifier = 0.5d;
    @Config(min = 0d)
    @Label(name = "Food Heal.Health Multiplier", description = "When eating you'll get healed by hunger restored multiplied by this percentage. (Set to 1 to have the same effect as pre-beta 1.8 food")
    public static Double foodHealHealthMultiplier = 1d;
    @Config
    @Label(name = "Raw food.Heal Multiplier", description = "If true, raw food will heal by this percentage. Raw food is defined in the survivalreimagined:raw_food tag")
    public static Double rawFoodHealPercentage = 1d;
    @Config(min = 0d, max = 1f)
    @Label(name = "Raw food.Poison Chance", description = "Raw food has this chance to poison the player. Raw food is defined in the survivalreimagined:raw_food tag")
    public static Double rawFoodPoisonChance = 0.8d;

    @Config
    @Label(name = "Convert Hunger to Weakness", description = "If true, Hunger effect is replaced by Weakness")
    public static Boolean convertHungerToWeakness = true;

    public NoHunger(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super(module, enabledByDefault, canBeDisabled);
    }

    private static final int PASSIVE_REGEN_TICK_RATE = 10;
    private static final int FOOD_REGEN_TICK_RATE = 10;

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (!this.isEnabled()
                || !disableHunger
                || event.player.level.isClientSide
                || event.phase.equals(TickEvent.Phase.START))
            return;

        event.player.getFoodData().foodLevel = 15;

        if (event.player.tickCount % PASSIVE_REGEN_TICK_RATE == 1 && enablePassiveRegen && event.player.isHurt()) {
            incrementPassiveRegenTick(event.player);
            int passiveRegen = getPassiveRegenSpeed(event.player);

            if (getPassiveRegenTick(event.player) > passiveRegen) {
                float heal = 1.0f;
                event.player.heal(heal);
                resetPassiveRegenTick(event.player);
            }
        }

        if (event.player.hasEffect(MobEffects.HUNGER) && convertHungerToWeakness) {
            MobEffectInstance effect = event.player.getEffect(MobEffects.HUNGER);
            //noinspection ConstantConditions; Checking with hasEffect
            event.player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, effect.getDuration(), effect.getAmplifier(), effect.isAmbient(), effect.isVisible(), effect.showIcon()));
            event.player.removeEffect(MobEffects.HUNGER);
        }

        if (event.player.tickCount % FOOD_REGEN_TICK_RATE == 0 && getFoodRegenLeft(event.player) > 0f) {
            consumeAndHealFromFoodRegen(event.player);
        }
    }

    @SubscribeEvent
    public void onPlayerEat(LivingEntityUseItemEvent.Finish event) {
        if (!this.isEnabled()
                || !event.getItem().isEdible()
                || !(event.getEntity() instanceof Player)
                || event.getEntity().level.isClientSide)
            return;

        //applyFedEffect(event);
        healOnEat(event);
    }

    public void applyFedEffect(LivingEntityUseItemEvent.Finish event) {
        if (foodGivesWellFedWhenSaturationModifier < 0d) return;
        FoodProperties food = event.getItem().getItem().getFoodProperties(event.getItem(), event.getEntity());
        //noinspection ConstantConditions
        if (food.saturationModifier < foodGivesWellFedWhenSaturationModifier)
            return;
        int duration = (int) (food.getNutrition() * food.getSaturationModifier() * 2 * 20 * 10);
        //int amplifier = (int) ((food.saturationModifier * 2 * food.nutrition) / 4 - 1);
        event.getEntity().addEffect(new MobEffectInstance(SRMobEffects.WELL_FED.get(), duration, 0, true, false, true));
    }

    @SuppressWarnings("ConstantConditions")
    public void healOnEat(LivingEntityUseItemEvent.Finish event) {
        if (foodHealHealthMultiplier == 0d
                || !(event.getEntity() instanceof Player player))
            return;
        FoodProperties food = event.getItem().getItem().getFoodProperties(event.getItem(), player);
        boolean isRawFood = isRawFood(event.getItem().getItem());
        if (player.getRandom().nextDouble() < rawFoodPoisonChance && isRawFood) {
            player.addEffect(new MobEffectInstance(MobEffects.POISON, food.getNutrition() * 20 * 4));
        }
        else {
            float heal = food.getNutrition() * foodHealHealthMultiplier.floatValue();
            //Half heart per second by default
            float strength = 0.5f * food.getSaturationModifier() / 20f;
            if (isRawFood && rawFoodHealPercentage != 1d)
                heal *= rawFoodHealPercentage;
            //event.getEntity().heal((float) heal);
            setFoodRegenLeft(player, heal);
            setFoodRegenStrength(player, strength);
        }
    }

    private static int getPassiveRegenSpeed(Player player) {
        float healthPerc = 1 - (player.getHealth() / player.getMaxHealth());
        int secs;
        secs = (int) ((passiveRegenerationTime.max - passiveRegenerationTime.min) * healthPerc + passiveRegenerationTime.min);
        if (player.hasEffect(SRMobEffects.WELL_FED.get())) {
            MobEffectInstance wellFed = player.getEffect(SRMobEffects.WELL_FED.get());
            //noinspection ConstantConditions
            secs *= 1 - (((wellFed.getAmplifier() + 1) * 0.4d));
        }
        return secs * 20;
    }

    private static int getPassiveRegenTick(Player player) {
        return player.getPersistentData().getInt(PASSIVE_REGEN_TICK);
    }

    private static void incrementPassiveRegenTick(Player player) {
        player.getPersistentData().putInt(PASSIVE_REGEN_TICK, getPassiveRegenTick(player) + FOOD_REGEN_TICK_RATE);
    }

    private static void resetPassiveRegenTick(Player player) {
        player.getPersistentData().putInt(PASSIVE_REGEN_TICK, 0);
    }

    private static float getFoodRegenLeft(Player player) {
        return player.getPersistentData().getFloat(FOOD_REGEN_LEFT);
    }

    private static void setFoodRegenLeft(Player player, float amount) {
        player.getPersistentData().putFloat(FOOD_REGEN_LEFT, amount);
    }

    private static void consumeAndHealFromFoodRegen(Player player) {
        float regenLeft = getFoodRegenLeft(player);
        float regenStrength = getFoodRegenStrength(player) * FOOD_REGEN_TICK_RATE;
        if (regenLeft < regenStrength)
            regenStrength = regenLeft;
        player.heal(regenStrength);
        regenLeft -= regenStrength;
        if (regenLeft < 0f){
            regenLeft = 0f;
        }
        setFoodRegenLeft(player, regenLeft);
        if (regenLeft == 0f)
            setFoodRegenStrength(player, 0f);
    }

    private static float getFoodRegenStrength(Player player) {
        return player.getPersistentData().getFloat(FOOD_REGEN_STRENGTH);
    }

    public static void setFoodRegenStrength(Player player, float amount) {
        player.getPersistentData().putFloat(FOOD_REGEN_STRENGTH, amount);
        if (player instanceof ServerPlayer serverPlayer) {
            Object msg = new MessageFoodRegenSync(amount);
            NetworkHandler.CHANNEL.sendTo(msg, serverPlayer.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
        }
    }

    public static boolean isRawFood(Item item) {
        return Utils.isItemInTag(item, RAW_FOOD);
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void removeFoodBar(final RenderGuiOverlayEvent.Pre event)
    {
        if (event.getOverlay().equals(VanillaGuiOverlay.FOOD_LEVEL.type()))
            event.setCanceled(true);
    }

    static ResourceLocation PLAYER_HEALTH_ELEMENT = new ResourceLocation("minecraft", "player_health");

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onRenderGuiOverlayPre(RenderGuiOverlayEvent.Post event) {
        if (!this.isEnabled())
            return;
        if (event.getOverlay() == GuiOverlayManager.findOverlay(PLAYER_HEALTH_ELEMENT)) {
            Minecraft mc = Minecraft.getInstance();
            ForgeGui gui = (ForgeGui) mc.gui;
            if (!mc.options.hideGui && gui.shouldDrawSurvivalElements()) {
                renderFoodRegen(gui, event.getPoseStack(), event.getPartialTick(), event.getWindow().getScreenWidth(), event.getWindow().getScreenHeight());
            }
        }
    }

    private static final Vec2 UV_ARROW = new Vec2(0, 18);

    @OnlyIn(Dist.CLIENT)
    public static void renderFoodRegen(ForgeGui gui, PoseStack poseStack, float partialTicks, int screenWidth, int screenHeight) {
        int healthIconsOffset = gui.leftHeight;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        assert player != null;

        int right = mc.getWindow().getGuiScaledWidth() / 2 - 94;
        int top = mc.getWindow().getGuiScaledHeight() - healthIconsOffset + 11;
        float saturationModifier = getFoodRegenStrength(player) * 20 * 2;
        if (saturationModifier == 0f)
            return;
        RenderSystem.setShaderTexture(0, SurvivalReimagined.GUI_ICONS);
        Stamina.setColor(1.2f - (saturationModifier / 1.2f), 0.78f, 0.17f, 1f);
        mc.gui.blit(poseStack, right, top, (int) UV_ARROW.x, (int) UV_ARROW.y, 9, 9);
        Stamina.resetColor();

        // rebind default icons
        RenderSystem.setShaderTexture(0, GuiComponent.GUI_ICONS_LOCATION);
    }
}