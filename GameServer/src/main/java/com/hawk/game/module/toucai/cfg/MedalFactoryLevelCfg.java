package com.hawk.game.module.toucai.cfg;

import java.util.HashMap;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.google.common.collect.ImmutableMap;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.YQZZ.PBYQZZOrderType;

/**
 * 铁血军团活动时间配置
 * @author Jesse
 *
 */
@HawkConfigManager.XmlResource(file = "xml/medal_factory_level.xml")
public class MedalFactoryLevelCfg extends HawkConfigBase {
	private final int id;// ="10101"
	@Id
	private final int level;// ="1"
	private final int exp;// ="10"
	private final String dailyReward;// ="2"
	private final String monCardReward;
	private final int productionNum;// ="1"
	private final String effect;// ="1577_3000,1578_3000"

	private Map<EffType, Integer> effectList;
	public MedalFactoryLevelCfg() {
		id = 0;
		level = 0;
		exp = 0;
		dailyReward = "";
		productionNum = 0;
		effect = "";
		monCardReward="";
	}
	@Override
	protected boolean assemble() {
		effectList = new HashMap<>();
		if (!HawkOSOperator.isEmptyString(effect)) {
			String[] array = effect.split(",");
			for (String val : array) {
				String[] info = val.split("_");
				effectList.put(EffType.valueOf(Integer.parseInt(info[0])), Integer.parseInt(info[1]));
			}
		}
		effectList = ImmutableMap.copyOf(effectList);

		return true;
	}
	
	public int getEffect(EffType eff){
		return effectList.getOrDefault(eff, 0);
	}
	
	public int getId() {
		return id;
	}

	public int getLevel() {
		return level;
	}

	public int getExp() {
		return exp;
	}

	public String getDailyReward() {
		return dailyReward;
	}

	public int getProductionNum() {
		return productionNum;
	}

	public String getEffect() {
		return effect;
	}

	public String getMonCardReward() {
		return monCardReward;
	}

}
