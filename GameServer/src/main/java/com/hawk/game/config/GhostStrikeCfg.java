package com.hawk.game.config;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.math.NumberUtils;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkRand;
import org.hawk.tuple.HawkTuple3;
import org.hawk.tuple.HawkTuples;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.hawk.game.march.ArmyInfo;

@HawkConfigManager.XmlResource(file = "xml/ghost_Strikes.xml")
public class GhostStrikeCfg extends HawkConfigBase {
	@Id
	private final int id;// ="10001"
	private final int enemyHero;// ="0"
	private final String enemyArmy;// ="100301_1800_2000,100401_1800_2000"
	private final int wounded;// ="3000" 伤兵减免，万份比
	private final int enemyDistance;// ="50" 距离
	private final int enemyTime;// ="35" 行军时间

	private ImmutableList<HawkTuple3<Integer, Integer, Integer>> enemyArmyIdMinMax;

	public GhostStrikeCfg() {
		id = 0;
		enemyHero = 0;
		enemyArmy = "";
		wounded = 3000;
		enemyDistance = 50;
		enemyTime = 35;
	}

	@Override
	protected boolean assemble() {
		List<HawkTuple3<Integer, Integer, Integer>> result = new ArrayList<>();
		List<String> armyList = Splitter.on(",").omitEmptyStrings().splitToList(enemyArmy);
		for (String army : armyList) {
			String[] abc = army.split("_");
			if (abc.length != 3) {
				continue;
			}
			HawkTuple3<Integer, Integer, Integer> tt = HawkTuples.tuple(NumberUtils.toInt(abc[0]), NumberUtils.toInt(abc[1]), NumberUtils.toInt(abc[2]));
			result.add(tt);
		}
		enemyArmyIdMinMax = ImmutableList.copyOf(result);
		return super.assemble();
	}

	public List<ArmyInfo> getEnemyList() {
		List<ArmyInfo> enemyList = getArmyList(enemyArmyIdMinMax);
		return enemyList;
	}

	private List<ArmyInfo> getArmyList(List<HawkTuple3<Integer, Integer, Integer>> armyIdMinMax) {
		List<ArmyInfo> enemyList = new ArrayList<>();
		for (HawkTuple3<Integer, Integer, Integer> id_min_max : armyIdMinMax) {
			enemyList.add(new ArmyInfo(id_min_max.first, HawkRand.randInt(id_min_max.second, id_min_max.third)));
		}
		return enemyList;
	}

	public int getId() {
		return id;
	}

	public int getEnemyHero() {
		return enemyHero;
	}

	public String getEnemyArmy() {
		return enemyArmy;
	}

	public int getWounded() {
		return wounded;
	}

	public int getEnemyDistance() {
		return enemyDistance;
	}

	public int getEnemyTime() {
		return enemyTime;
	}

}
