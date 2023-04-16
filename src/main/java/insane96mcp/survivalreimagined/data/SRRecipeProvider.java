package insane96mcp.survivalreimagined.data;

import insane96mcp.survivalreimagined.SurvivalReimagined;
import insane96mcp.survivalreimagined.module.experience.feature.EnchantmentsFeature;
import insane96mcp.survivalreimagined.module.items.feature.*;
import insane96mcp.survivalreimagined.module.movement.feature.Minecarts;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.ConditionalRecipe;
import net.minecraftforge.common.crafting.conditions.IConditionBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class SRRecipeProvider extends RecipeProvider implements IConditionBuilder {
    public SRRecipeProvider(PackOutput p_248933_) {
        super(p_248933_);
    }

    @Override
    protected void buildRecipes(@NotNull Consumer<FinishedRecipe> writer) {
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, FlintTools.AXE.get())
                .pattern("ff")
                .pattern("fs")
                .pattern(" s")
                .define('f', Items.FLINT)
                .define('s', Items.STICK)
                .unlockedBy("has_stick", has(Items.STICK))
                .unlockedBy("has_flint", has(Items.FLINT))
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, FlintTools.SHOVEL.get())
                .pattern("f")
                .pattern("s")
                .pattern("s")
                .define('f', Items.FLINT)
                .define('s', Items.STICK)
                .unlockedBy("has_stick", has(Items.STICK))
                .unlockedBy("has_flint", has(Items.FLINT))
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, FlintTools.PICKAXE.get())
                .pattern("fff")
                .pattern(" s ")
                .pattern(" s ")
                .define('f', Items.FLINT)
                .define('s', Items.STICK)
                .unlockedBy("has_stick", has(Items.STICK))
                .unlockedBy("has_flint", has(Items.FLINT))
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, FlintTools.HOE.get())
                .pattern("ff")
                .pattern(" s")
                .pattern(" s")
                .define('f', Items.FLINT)
                .define('s', Items.STICK)
                .unlockedBy("has_stick", has(Items.STICK))
                .unlockedBy("has_flint", has(Items.FLINT))
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, FlintTools.SWORD.get())
                .pattern("f")
                .pattern("f")
                .pattern("s")
                .define('f', Items.FLINT)
                .define('s', Items.STICK)
                .unlockedBy("has_stick", has(Items.STICK))
                .unlockedBy("has_flint", has(Items.FLINT))
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, FlintTools.SHIELD.get())
                .pattern(" f ")
                .pattern("fLf")
                .pattern(" f ")
                .define('f', Items.FLINT)
                .define('L', ItemTags.LOGS)
                .unlockedBy("has_flint", has(Items.FLINT))
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, BoneClub.BONE_CLUB.get())
                .pattern("  b")
                .pattern(" b ")
                .pattern("b  ")
                .define('b', Items.BONE)
                .unlockedBy("has_bone", has(Items.BONE))
                .save(writer);

        ConditionalRecipe.builder()
                .addCondition(not(modLoaded("tconstruct")))
                .addRecipe(
                    ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.FLINT)
                            .requires(Items.GRAVEL, 3)
                            .unlockedBy("has_gravel", has(Items.GRAVEL))
                            ::save
                )
                .build(writer, SurvivalReimagined.MOD_ID, "flint_from_gravel");

        //Chained Copper Armor
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ChainedCopperArmor.HELMET.get())
                .pattern("cmc")
                .pattern("m m")
                .define('c', Items.CHAIN)
                .define('m', Items.COPPER_INGOT)
                .unlockedBy("has_copper_ingot", has(Items.COPPER_INGOT))
                .unlockedBy("has_chain", has(Items.CHAIN))
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ChainedCopperArmor.CHESTPLATE.get())
                .pattern("c c")
                .pattern("mcm")
                .pattern("mmm")
                .define('c', Items.CHAIN)
                .define('m', Items.COPPER_INGOT)
                .unlockedBy("has_copper_ingot", has(Items.COPPER_INGOT))
                .unlockedBy("has_chain", has(Items.CHAIN))
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ChainedCopperArmor.LEGGINGS.get())
                .pattern("ccc")
                .pattern("m m")
                .pattern("m m")
                .define('c', Items.CHAIN)
                .define('m', Items.COPPER_INGOT)
                .unlockedBy("has_copper_ingot", has(Items.COPPER_INGOT))
                .unlockedBy("has_chain", has(Items.CHAIN))
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ChainedCopperArmor.BOOTS.get())
                .pattern("c c")
                .pattern("m m")
                .define('c', Items.CHAIN)
                .define('m', Items.COPPER_INGOT)
                .unlockedBy("has_copper_ingot", has(Items.COPPER_INGOT))
                .unlockedBy("has_chain", has(Items.CHAIN))
                .save(writer);

        ShapedRecipeBuilder.shaped(RecipeCategory.TRANSPORTATION, Crate.BLOCK_ITEM.get())
                .pattern("nnn")
                .pattern("ibi")
                .pattern("nnn")
                .define('n', Florpium.NUGGET.get())
                .define('i', Items.IRON_INGOT)
                .define('b', Items.BARREL)
                .unlockedBy("has_florpium_nugget", has(Florpium.NUGGET.get()))
                .unlockedBy("has_barrel", has(Items.BARREL))
                .save(writer);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.TRANSPORTATION, Minecarts.NETHER_INFUSED_POWERED_RAIL_ITEM.get(), 4)
                .requires(Items.POWERED_RAIL, 4)
                .requires(Items.NETHER_BRICK, 4)
                .requires(Items.FIRE_CHARGE, 1)
                .unlockedBy("has_powered_rail", has(Items.POWERED_RAIL))
                .unlockedBy("has_nether_brick", has(Items.NETHER_BRICK))
                .save(writer);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, AncientLapis.ANCIENT_LAPIS.get(), 1)
                .requires(EnchantmentsFeature.CLEANSED_LAPIS.get(), 1)
                .requires(Items.NETHERITE_SCRAP, 1)
                .requires(Items.EXPERIENCE_BOTTLE, 1)
                .unlockedBy("has_netherite_scrap", has(Items.NETHERITE_SCRAP))
                .unlockedBy("has_ancient_lapis", has(AncientLapis.ANCIENT_LAPIS.get()))
                .save(writer);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, Florpium.BLOCK_ITEM.get(), 1)
                .requires(Florpium.INGOT.get(), 9)
                .unlockedBy("has_ingot", has(Florpium.INGOT.get()))
                .save(writer);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Florpium.INGOT.get(), 9)
                .requires(Florpium.BLOCK_ITEM.get(), 1)
                .unlockedBy("has_ingot", has(Florpium.INGOT.get()))
                .save(writer, SurvivalReimagined.RESOURCE_PREFIX + "florpium_ingot_from_block");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Florpium.INGOT.get(), 1)
                .requires(Florpium.NUGGET.get(), 9)
                .unlockedBy("has_nuggets", has(Florpium.NUGGET.get()))
                .save(writer, SurvivalReimagined.RESOURCE_PREFIX + "florpium_ingot_from_nuggets");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Florpium.NUGGET.get(), 9)
                .requires(Florpium.INGOT.get(), 1)
                .unlockedBy("has_ingot", has(Florpium.INGOT.get()))
                .save(writer);

        SimpleCookingRecipeBuilder.smelting(
                        Ingredient.of(Florpium.ORE.get()),
                        RecipeCategory.MISC,
                        Florpium.INGOT.get(),
                        0.8f,
                        800
                )
                .unlockedBy("has_florpium_ore", has(Florpium.ORE.get()))
                .save(writer, SurvivalReimagined.RESOURCE_PREFIX + "florpium_ingot_from_smelting_ore");

        SimpleCookingRecipeBuilder.smelting(
                        Ingredient.of(Florpium.DEEPSLATE_ORE.get()),
                        RecipeCategory.MISC,
                        Florpium.INGOT.get(),
                        0.8f,
                        800
                )
                .unlockedBy("has_florpium_ore", has(Florpium.DEEPSLATE_ORE.get()))
                .save(writer, SurvivalReimagined.RESOURCE_PREFIX + "florpium_ingot_from_smelting_deepslate_ore");

        SimpleCookingRecipeBuilder.blasting(
                        Ingredient.of(Florpium.ORE.get()),
                        RecipeCategory.MISC,
                        Florpium.INGOT.get(),
                        0.8f,
                        200
                )
                .unlockedBy("has_florpium_ore", has(Florpium.ORE.get()))
                .save(writer, SurvivalReimagined.RESOURCE_PREFIX + "florpium_ingot_from_blasting_ore");

        SimpleCookingRecipeBuilder.blasting(
                        Ingredient.of(Florpium.DEEPSLATE_ORE.get()),
                        RecipeCategory.MISC,
                        Florpium.INGOT.get(),
                        0.8f,
                        200
                )
                .unlockedBy("has_florpium_ore", has(Florpium.DEEPSLATE_ORE.get()))
                .save(writer, SurvivalReimagined.RESOURCE_PREFIX + "florpium_ingot_from_blasting_deepslate_ore");

        LegacyUpgradeRecipeBuilder.smithing(Ingredient.of(Items.IRON_AXE), Ingredient.of(Florpium.INGOT.get()), RecipeCategory.TOOLS, Florpium.AXE.get())
                .unlocks("has_florpium", has(Florpium.INGOT.get()))
                .save(writer, SurvivalReimagined.RESOURCE_PREFIX + "florpium_axe");
        LegacyUpgradeRecipeBuilder.smithing(Ingredient.of(Items.IRON_PICKAXE), Ingredient.of(Florpium.INGOT.get()), RecipeCategory.TOOLS, Florpium.PICKAXE.get())
                .unlocks("has_florpium", has(Florpium.INGOT.get()))
                .save(writer, SurvivalReimagined.RESOURCE_PREFIX + "florpium_pickaxe");
        LegacyUpgradeRecipeBuilder.smithing(Ingredient.of(Items.IRON_SHOVEL), Ingredient.of(Florpium.INGOT.get()), RecipeCategory.TOOLS, Florpium.SHOVEL.get())
                .unlocks("has_florpium", has(Florpium.INGOT.get()))
                .save(writer, SurvivalReimagined.RESOURCE_PREFIX + "florpium_shovel");
        LegacyUpgradeRecipeBuilder.smithing(Ingredient.of(Items.IRON_HOE), Ingredient.of(Florpium.INGOT.get()), RecipeCategory.TOOLS, Florpium.HOE.get())
                .unlocks("has_florpium", has(Florpium.INGOT.get()))
                .save(writer, SurvivalReimagined.RESOURCE_PREFIX + "florpium_how");
        LegacyUpgradeRecipeBuilder.smithing(Ingredient.of(Items.IRON_SWORD), Ingredient.of(Florpium.INGOT.get()), RecipeCategory.COMBAT, Florpium.SWORD.get())
                .unlocks("has_florpium", has(Florpium.INGOT.get()))
                .save(writer, SurvivalReimagined.RESOURCE_PREFIX + "florpium_sword");
        LegacyUpgradeRecipeBuilder.smithing(Ingredient.of(Items.SHIELD), Ingredient.of(Florpium.INGOT.get()), RecipeCategory.COMBAT, Florpium.SHIELD.get())
                .unlocks("has_florpium", has(Florpium.INGOT.get()))
                .save(writer, SurvivalReimagined.RESOURCE_PREFIX + "florpium_shield");
        LegacyUpgradeRecipeBuilder.smithing(Ingredient.of(Items.IRON_HELMET), Ingredient.of(Florpium.INGOT.get()), RecipeCategory.COMBAT, Florpium.HELMET.get())
                .unlocks("has_florpium", has(Florpium.INGOT.get()))
                .save(writer, SurvivalReimagined.RESOURCE_PREFIX + "florpium_helmet");
        LegacyUpgradeRecipeBuilder.smithing(Ingredient.of(Items.IRON_CHESTPLATE), Ingredient.of(Florpium.INGOT.get()), RecipeCategory.COMBAT, Florpium.CHESTPLATE.get())
                .unlocks("has_florpium", has(Florpium.INGOT.get()))
                .save(writer, SurvivalReimagined.RESOURCE_PREFIX + "florpium_chestplate");
        LegacyUpgradeRecipeBuilder.smithing(Ingredient.of(Items.IRON_LEGGINGS), Ingredient.of(Florpium.INGOT.get()), RecipeCategory.COMBAT, Florpium.LEGGINGS.get())
                .unlocks("has_florpium", has(Florpium.INGOT.get()))
                .save(writer, SurvivalReimagined.RESOURCE_PREFIX + "florpium_leggings");
        LegacyUpgradeRecipeBuilder.smithing(Ingredient.of(Items.IRON_BOOTS), Ingredient.of(Florpium.INGOT.get()), RecipeCategory.COMBAT, Florpium.BOOTS.get())
                .unlocks("has_florpium", has(Florpium.INGOT.get()))
                .save(writer, SurvivalReimagined.RESOURCE_PREFIX + "florpium_boots");
    }


}
