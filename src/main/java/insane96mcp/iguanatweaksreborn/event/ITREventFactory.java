package insane96mcp.iguanatweaksreborn.event;

import insane96mcp.iguanatweaksreborn.module.world.explosionoverhaul.ITRExplosion;
import insane96mcp.iguanatweaksreborn.module.world.explosionoverhaul.ITRExplosionCreatedEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraftforge.common.MinecraftForge;

public class ITREventFactory {
    /**
     * Returns true if the event is canceled
     */
    public static boolean onSRExplosionCreated(ITRExplosion explosion)
    {
        ITRExplosionCreatedEvent event = new ITRExplosionCreatedEvent(explosion);
        MinecraftForge.EVENT_BUS.post(event);
        return event.isCanceled();
    }

    /**
     * Apply changes to damage amount after damage absorb but before absorption reduction
     */
    public static float onLivingHurtPreAbsorption(LivingEntity livingEntity, DamageSource source, float amount)
    {
        LivingHurtPreAbsorptionEvent event = new LivingHurtPreAbsorptionEvent(livingEntity, source, amount);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getAmount();
    }

    /**
     * Apply changes to the ticks that will be removed from the hook to lure and hook
     */
    public static int onHookTickToHookLure(FishingHook hook, int tick)
    {
        HookTickToHookLureEvent event = new HookTickToHookLureEvent(hook, tick);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getTick();
    }

    public static float onLivingAttack(LivingEntity entity, DamageSource src, float amount)
    {
        if (entity instanceof Player)
            return amount;
        ITRLivingAttackEvent event = new ITRLivingAttackEvent(entity, src, amount);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getAmount();
    }

    public static float onPlayerAttack(LivingEntity entity, DamageSource src, float amount)
    {
        ITRLivingAttackEvent event = new ITRLivingAttackEvent(entity, src, amount);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getAmount();
    }

    public static float onStaminaConsumed(Player player, float amount) {
        StaminaEvent.Consumed event = new StaminaEvent.Consumed(player, amount);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getAmount();
    }

    public static float onStaminaRegenerated(Player player, float amount) {
        StaminaEvent.Regenerated event = new StaminaEvent.Regenerated(player, amount);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getAmount();
    }
}
