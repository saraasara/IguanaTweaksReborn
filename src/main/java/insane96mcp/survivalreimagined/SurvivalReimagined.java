package insane96mcp.survivalreimagined;

import insane96mcp.survivalreimagined.data.SRAnvilRecipeReloadListener;
import insane96mcp.survivalreimagined.data.SRDataReloadListener;
import insane96mcp.survivalreimagined.data.SRRecipeProvider;
import insane96mcp.survivalreimagined.data.lootmodifier.SRGlobalLootModifierProvider;
import insane96mcp.survivalreimagined.module.misc.capability.SpawnerProvider;
import insane96mcp.survivalreimagined.module.sleeprespawn.feature.Tiredness;
import insane96mcp.survivalreimagined.network.NetworkHandler;
import insane96mcp.survivalreimagined.setup.*;
import insane96mcp.survivalreimagined.setup.client.ClientSetup;
import insane96mcp.survivalreimagined.setup.client.SRClientConfig;
import insane96mcp.survivalreimagined.utils.Weights;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;

@Mod("survivalreimagined")
public class SurvivalReimagined
{
	public static final String MOD_ID = "survivalreimagined";
	public static final String RESOURCE_PREFIX = MOD_ID + ":";
    public static final Logger LOGGER = LogManager.getLogger();

    public static final String CONFIG_FOLDER = "config/" + MOD_ID;

    public static final ResourceLocation GUI_ICONS = new ResourceLocation(SurvivalReimagined.MOD_ID, "textures/gui/icons.png");

    public SurvivalReimagined() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, SRClientConfig.CONFIG_SPEC, MOD_ID + "/client.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SRCommonConfig.CONFIG_SPEC, MOD_ID + "/common.toml");
        MinecraftForge.EVENT_BUS.register(this);
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::gatherData);
        modEventBus.addListener(ClientSetup::creativeTabsBuildContents);
        modEventBus.addListener(this::addPackFinders);
        modEventBus.register(Tiredness.class);
        SRSoundEvents.SOUND_EVENTS.register(modEventBus);
        SRMobEffects.MOB_EFFECTS.register(modEventBus);
        SRItems.ITEMS.register(modEventBus);
        SRGlobalLootModifiers.LOOT_MODIFIERS.register(modEventBus);
        SRLootItemConditions.LOOT_ITEM_CONDITION_TYPES.register(modEventBus);
        Weights.initMaterialWeight();
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onAddReloadListener(AddReloadListenerEvent event) {
        SRDataReloadListener.reloadContext = event.getConditionContext();
        event.addListener(SRDataReloadListener.INSTANCE);
        event.addListener(SRAnvilRecipeReloadListener.INSTANCE);
    }

    @SubscribeEvent
    public void attachCapBlockEntity(final AttachCapabilitiesEvent<BlockEntity> event)
    {
        if (event.getObject() instanceof SpawnerBlockEntity)
            event.addCapability(SpawnerProvider.IDENTIFIER, new SpawnerProvider());
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void attachCapBlockEntity(final RenderGuiOverlayEvent.Pre event)
    {
        if (event.getOverlay().equals(VanillaGuiOverlay.FOOD_LEVEL.type()))
            event.setCanceled(true);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        NetworkHandler.init();
    }

    @SubscribeEvent
    public void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        generator.addProvider(event.includeServer(), new SRRecipeProvider(generator.getPackOutput()));
        generator.addProvider(event.includeServer(), new SRGlobalLootModifierProvider(generator.getPackOutput(), SurvivalReimagined.MOD_ID));
    }

    public void addPackFinders(AddPackFindersEvent event)
    {
        for (IntegratedDataPacks.IntegratedDataPack dataPack : IntegratedDataPacks.INTEGRATED_DATA_PACKS) {
            if (event.getPackType() != dataPack.getPackType())
                continue;

            Path resourcePath = ModList.get().getModFileById(MOD_ID).getFile().findResource("integrated_packs/" + dataPack.getPath());
            var pack = Pack.readMetaAndCreate(SurvivalReimagined.RESOURCE_PREFIX + dataPack.getPath(), dataPack.getDescription(), dataPack.shouldBeEnabled(),
                    (path) -> new PathPackResources(path, resourcePath, false), PackType.SERVER_DATA, Pack.Position.BOTTOM, dataPack.shouldBeEnabled() ? PackSource.DEFAULT : SRPackSource.DISABLED);
            event.addRepositorySource((packConsumer) -> packConsumer.accept(pack));
        }
    }


}