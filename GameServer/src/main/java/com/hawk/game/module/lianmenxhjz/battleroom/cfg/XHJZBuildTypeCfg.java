package com.hawk.game.module.lianmenxhjz.battleroom.cfg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.math.NumberUtils;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;
import org.hawk.tuple.HawkTuple2;
import org.hawk.tuple.HawkTuple3;
import org.hawk.tuple.HawkTuples;

import com.google.common.base.Splitter;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import com.hawk.game.module.lianmenxhjz.battleroom.worldpoint.XHJZBuildType;
import com.hawk.game.protocol.Const.EffType;

@HawkConfigManager.XmlResource(file = "xml/xhjz_buildType.xml")
public class XHJZBuildTypeCfg extends HawkConfigBase {

	@Id
	private final int buildTypeId;
	private final int gridCnt;
	private final double allianceScore;
	private final double allianceScoreAdd;
	private final String buildEffect;
	private final String specialEffect;// 5_100_20000,5_103_20000,5_102_20000

	private final int occupyTime;
	private final int peaceTime;
	private final double playerFuelAdd;// ="0.016667"
	private final double allianceFuelAdd;// ="0.5"
	private final int occupyMarchFree; // 占领后向该点行军,可直接到达, 不消耗燃油
	private final int occupyMarchSpeedUp; // 占领后向该点行军 百分比加速
	private final int occupyMarchFuel;
	private final int towerAtkPeriod;
	private final int towerAtk;
	private final int coolDownReducePercentage;

	private Map<EffType, Integer> controleBuffList;
	private List<HawkTuple3<XHJZBuildType, EffType, Integer>> specialEffectTable;

	public XHJZBuildTypeCfg() {
		buildTypeId = 0;
		allianceScore = 0;
		allianceScoreAdd = 0;
		occupyTime = Integer.MAX_VALUE;// 持续占领后开罩 ="1800"
		buildEffect = "";
		specialEffect = "";
		gridCnt = 3;
		peaceTime = 0;
		playerFuelAdd = 0;
		allianceFuelAdd = 0;
		occupyMarchFree = 0;
		occupyMarchSpeedUp = 0;
		towerAtkPeriod = Integer.MAX_VALUE;
		towerAtk = 0;
		coolDownReducePercentage = 0;
		occupyMarchFuel = 0;
	}

	@Override
	protected boolean assemble() {

		{
			controleBuffList = new HashMap<>();
			if (!HawkOSOperator.isEmptyString(buildEffect)) {
				String[] array = buildEffect.split(",");
				for (String val : array) {
					String[] info = val.split("_");
					controleBuffList.put(EffType.valueOf(Integer.parseInt(info[0])), Integer.parseInt(info[1]));
				}
			}
			controleBuffList = ImmutableMap.copyOf(controleBuffList);

		}

		{
			specialEffectTable = new ArrayList<>();
			if (!HawkOSOperator.isEmptyString(specialEffect)) {
				String[] array = specialEffect.split(",");
				for (String val : array) {
					String[] info = val.split("_");
					HawkTuple3<XHJZBuildType, EffType, Integer> tuple = HawkTuples.tuple(XHJZBuildType.valueOf(Integer.parseInt(info[0])),
							EffType.valueOf(Integer.parseInt(info[1])), Integer.parseInt(info[2]));
					specialEffectTable.add(tuple);
				}
			}
			specialEffectTable = ImmutableList.copyOf(specialEffectTable);

		}
		return super.assemble();
	}

	public Map<EffType, Integer> getControleBuffList() {
		return controleBuffList;
	}

	public void setControleBuffList(Map<EffType, Integer> controleBuffList) {
		this.controleBuffList = controleBuffList;
	}

	public int getEffectVal(EffType eff) {
		return controleBuffList.getOrDefault(eff, 0);
	}

	public String getSpecialEffect() {
		return specialEffect;
	}

	public List<HawkTuple3<XHJZBuildType, EffType, Integer>> getSpecialEffectTable() {
		return specialEffectTable;
	}

	public String getBuffList() {
		return buildEffect;
	}

	public int getBuildTypeId() {
		return buildTypeId;
	}

	public int getOccupyTime() {
		return occupyTime;
	}

	public String getBuildEffect() {
		return buildEffect;
	}

	public int getGridCnt() {
		return gridCnt;
	}

	public double getAllianceScore() {
		return allianceScore;
	}

	public double getAllianceScoreAdd() {
		return allianceScoreAdd;
	}

	public int getPeaceTime() {
		return peaceTime;
	}

	public double getPlayerFuelAdd() {
		return playerFuelAdd;
	}

	public double getAllianceFuelAdd() {
		return allianceFuelAdd;
	}

	public int getOccupyMarchFree() {
		return occupyMarchFree;
	}

	public int getOccupyMarchSpeedUp() {
		return occupyMarchSpeedUp;
	}

	public int getTowerAtkPeriod() {
		return towerAtkPeriod;
	}

	public int getTowerAtk() {
		return towerAtk;
	}

	public int getCoolDownReducePercentage() {
		return coolDownReducePercentage;
	}

	public int getOccupyMarchFuel() {
		return occupyMarchFuel;
	}

}
