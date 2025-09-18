package com.hawk.game.config;

import com.hawk.game.cfgElement.EffectObject;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

/**
 * 星能探索配置
 * 
 * @author golden
 *
 */
@HawkConfigManager.XmlResource(file = "xml/star_explore_node.xml")
public class ArmourStarExploreNodeCfg extends HawkConfigBase {
	@Id
	private final int id;

	private final int starId;
	// 星球探索进度万分比
	private final int exploreSchedule;
	// 进度奖励属性
	private final String scheduleAttribute;

	private EffectObject eff;

	public ArmourStarExploreNodeCfg() {
		id = 0;
		starId = 0;
		exploreSchedule = 0;
		scheduleAttribute = "";
	}

	@Override
	protected boolean assemble() {
		if(!HawkOSOperator.isEmptyString(scheduleAttribute)){
			String[] split = scheduleAttribute.split("_");
			eff = new EffectObject(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
		}
		return true;
	}

	public int getStarId() {
		return starId;
	}

	public int getExploreSchedule() {
		return exploreSchedule;
	}

	public String getScheduleAttribute() {
		return scheduleAttribute;
	}

	public EffectObject getEff() {
		return eff;
	}
}
