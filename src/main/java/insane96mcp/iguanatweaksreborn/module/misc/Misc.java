package insane96mcp.iguanatweaksreborn.module.misc;

import insane96mcp.iguanatweaksreborn.module.misc.feature.ExplosionOverhaul;
import insane96mcp.iguanatweaksreborn.module.misc.feature.ToolStats;
import insane96mcp.iguanatweaksreborn.setup.Config;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;

@Label(name = "Miscellaneous")
public class Misc extends Module {

	public ExplosionOverhaul explosionOverhaul;
	public ToolStats toolStats;
	//public DebuffFeature debuff;
	//public ToolNerfFeature toolNerf;
	//public TempSpawnerFeature tempSpawner;

	public Misc() {
		super(Config.builder);
		pushConfig(Config.builder);
		explosionOverhaul = new ExplosionOverhaul(this);
		toolStats = new ToolStats(this);
		//debuff = new DebuffFeature(this);
		//tempSpawner = new TempSpawnerFeature(this);
		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		explosionOverhaul.loadConfig();
		toolStats.loadConfig();
		//debuff.loadConfig();
		//tempSpawner.loadConfig();
	}
}
