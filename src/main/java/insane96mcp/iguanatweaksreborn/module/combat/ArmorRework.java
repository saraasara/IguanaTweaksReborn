package insane96mcp.iguanatweaksreborn.module.combat;

import com.ezylang.evalex.Expression;
import com.ezylang.evalex.data.EvaluationValue;
import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.LoadFeature;
import insane96mcp.insanelib.util.LogHelper;

@Label(name = "Armor Rework", description = "Changes how armor and toughness work. Armor reduces armor by a percentage and Toughness reduces damage by a flat amount")
@LoadFeature(module = Modules.Ids.COMBAT)
public class ArmorRework extends Feature {

    @Config
    @Label(name = "Damage Reduction formula", description = "Vanilla formula is 'damage * (1 - ((MIN(20, MAX(armor / 5, armor - ((4 * damage) / (toughness + 8)))))) / 25))'")
    public static String formula = "MAX(0, damage - (0.8 * toughness / 10 * ((toughness + 4) / (toughness + 1)))) * (1 - 1.25 * (armor / (armor + 30)))";

    public ArmorRework(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super(module, enabledByDefault, canBeDisabled);
    }

    public static float getCalculatedDamage(float damage, float armor, float toughness) {
        Expression expression = new Expression(formula);
        try {
            //noinspection ConstantConditions
            EvaluationValue result = expression
                    .with("damage", damage)
                    .and("armor", armor)
                    .and("toughness", toughness)
                    .evaluate();
            return result.getNumberValue().floatValue();
        }
        catch (Exception ex) {
            LogHelper.error("Failed to evaluate armor formula: %s\n%s", formula, ex);
            return -1f;
        }
    }
}
