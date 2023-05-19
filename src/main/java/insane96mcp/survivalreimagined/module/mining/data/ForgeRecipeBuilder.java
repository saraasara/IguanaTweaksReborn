package insane96mcp.survivalreimagined.module.mining.data;

import com.google.gson.JsonObject;
import insane96mcp.survivalreimagined.module.mining.crafting.ForgeRecipe;
import insane96mcp.survivalreimagined.module.mining.feature.Forging;
import insane96mcp.survivalreimagined.setup.client.SRBookCategory;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class ForgeRecipeBuilder implements RecipeBuilder {
    private final RecipeCategory category;
    private final SRBookCategory bookCategory;
    final Ingredient ingredient;
    final int ingredientAmount;
    final Ingredient gear;
    private final Item result;
    protected final int smashesRequired;
    private final RecipeSerializer<? extends ForgeRecipe> serializer;
    private final Advancement.Builder advancement = Advancement.Builder.advancement();

    public ForgeRecipeBuilder(RecipeCategory pCategory, SRBookCategory pBookCategory, ItemLike pResult, Ingredient ingredient, int ingredientAmount, Ingredient gear, int smashesRequired, RecipeSerializer<? extends ForgeRecipe> pSerializer) {
        this.category = pCategory;
        this.bookCategory = pBookCategory;
        this.ingredient = ingredient;
        this.ingredientAmount = ingredientAmount;
        this.gear = gear;
        this.result = pResult.asItem();
        this.smashesRequired = smashesRequired;
        this.serializer = pSerializer;
    }

    public static ForgeRecipeBuilder forging(RecipeCategory pCategory, ItemLike pResult, Ingredient ingredient, int ingredientAmount, Ingredient gear, int smashesRequired) {
        return new ForgeRecipeBuilder(pCategory, SRBookCategory.FORGE_MISC, pResult, ingredient, ingredientAmount, gear, smashesRequired, Forging.FORGE_RECIPE_SERIALIZER.get());
    }

    @Override
    public ForgeRecipeBuilder unlockedBy(String pCriterionName, CriterionTriggerInstance pCriterionTrigger) {
        this.advancement.addCriterion(pCriterionName, pCriterionTrigger);
        return this;
    }

    @Override
    public RecipeBuilder group(@Nullable String pGroupName) {
        return this;
    }

    @Override
    public Item getResult() {
        return this.result;
    }

    @Override
    public void save(Consumer<FinishedRecipe> pFinishedRecipeConsumer, ResourceLocation pRecipeId) {
        this.ensureValid(pRecipeId);
        this.advancement.parent(ROOT_RECIPE_ADVANCEMENT).addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(pRecipeId)).rewards(AdvancementRewards.Builder.recipe(pRecipeId)).requirements(RequirementsStrategy.OR);
        pFinishedRecipeConsumer.accept(new ForgeRecipeBuilder.Result(pRecipeId, this.bookCategory, this.ingredient, this.ingredientAmount, this.gear, this.result, this.smashesRequired, this.advancement, pRecipeId.withPrefix("recipes/" + this.category.getFolderName() + "/"), this.serializer));
    }

    /**
     * Makes sure that this obtainable.
     */
    private void ensureValid(ResourceLocation pId) {
        if (this.advancement.getCriteria().isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe " + pId);
        }
    }

    static class Result implements FinishedRecipe {
        private final ResourceLocation id;
        private final SRBookCategory category;
        private final Ingredient ingredient;
        private final int ingredientAmount;
        private final Ingredient gear;
        private final Item result;
        private final int smashesRequired;
        private final Advancement.Builder advancement;
        private final ResourceLocation advancementId;
        private final RecipeSerializer<? extends ForgeRecipe> serializer;

        public Result(ResourceLocation id, SRBookCategory bookCategory, Ingredient ingredient, int ingredientAmount, Ingredient gear, ItemLike pResult, int smashesRequired, Advancement.Builder pAdvancement, ResourceLocation pAdvancementId, RecipeSerializer<? extends ForgeRecipe> pSerializer) {
            this.id = id;
            this.category = bookCategory;
            this.ingredient = ingredient;
            this.ingredientAmount = ingredientAmount;
            this.gear = gear;
            this.result = pResult.asItem();
            this.smashesRequired = smashesRequired;
            this.serializer = pSerializer;
            this.advancement = pAdvancement;
            this.advancementId = pAdvancementId;
        }

        public void serializeRecipeData(JsonObject pJson) {
            pJson.addProperty("category", this.category.getSerializedName());
            pJson.add("ingredient", this.ingredient.toJson());
            pJson.add("ingredient", this.ingredient.toJson());
            pJson.add("gear", this.gear.toJson());
            pJson.addProperty("result", ForgeRegistries.ITEMS.getKey(this.result).toString());
            pJson.addProperty("smashes_required", this.smashesRequired);
        }

        public RecipeSerializer<?> getType() {
            return this.serializer;
        }

        /**
         * Gets the ID for the recipe.
         */
        public ResourceLocation getId() {
            return this.id;
        }

        /**
         * Gets the JSON for the advancement that unlocks this recipe. Null if there is no advancement.
         */
        @javax.annotation.Nullable
        public JsonObject serializeAdvancement() {
            return this.advancement.serializeToJson();
        }

        @javax.annotation.Nullable
        public ResourceLocation getAdvancementId() {
            return this.advancementId;
        }
    }
}