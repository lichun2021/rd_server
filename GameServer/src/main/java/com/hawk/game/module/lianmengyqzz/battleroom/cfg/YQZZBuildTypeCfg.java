package com.hawk.game.module.lianmengyqzz.battleroom.cfg;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.math.NumberUtils;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.tuple.HawkTuple2;
import org.hawk.tuple.HawkTuples;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.hawk.game.protocol.Const.EffType;

@HawkConfigManager.XmlResource(file = "xml/moon_war_buildType.xml")
public class YQZZBuildTypeCfg extends HawkConfigBase {

	@Id
	private final int buildTypeId;
	private final int gridCnt;
	private final double nationScore;
	private final double allianceScore;
	private final double playerScore;
	private final double nationFirstScore;
	private final double nationLastScore;
	private final double allianceFirstScore;
	private final double playerFirstScore;
	private final int declareCost;
	private final int declareTime;
	private final int occupyTime;// 持续占领后开罩 ="1800"
	private final int occupyProTime;
	private final int foggyFortressId;// ="202";
	private final int battleTime;
	private final int heal;
	private final String buildEffect;
	private final String allianceReward;
	private ImmutableList<HawkTuple2<EffType, Integer>> controleBuffList;

	public YQZZBuildTypeCfg() {
		nationLastScore = 0;
		buildTypeId = 0;
		nationScore = 0;
		allianceScore = 0;
		playerScore = 0;
		nationFirstScore = 0;
		allianceFirstScore = 0;
		playerFirstScore = 0;
		declareCost = 0;
		declareTime = 0;
		occupyTime = Integer.MAX_VALUE;// 持续占领后开罩 ="1800"
		occupyProTime = Integer.MAX_VALUE;
		buildEffect = "";
		gridCnt = 3;
		foggyFortressId = 202;
		battleTime = 600;
		heal = 0;
		allianceReward = "";
	}

	@Override
	protected boolean assemble() {

		{
			List<HawkTuple2<EffType, Integer>> lsit = new ArrayList<>();
			for (String xy : Splitter.on(",").omitEmptyStrings().splitToList(buildEffect)) {
				String[] x_y = xy.split("_");
				int[] pos = new int[2];
				pos[0] = NumberUtils.toInt(x_y[0]);
				pos[1] = NumberUtils.toInt(x_y[1]);
				lsit.add(HawkTuples.tuple(EffType.valueOf(pos[0]), pos[1]));
			}
			controleBuffList = ImmutableList.copyOf(lsit);
		}
		return super.assemble();
	}

	public ImmutableList<HawkTuple2<EffType, Integer>> getControlebuffList() {
		return controleBuffList;
	}

	public void setControlebuffList(ImmutableList<HawkTuple2<EffType, Integer>> controlebuffList) {
		this.controleBuffList = controlebuffList;
	}

	public String getBuffList() {
		return buildEffect;
	}

	public ImmutableList<HawkTuple2<EffType, Integer>> getControleBuffList() {
		return controleBuffList;
	}

	public void setControleBuffList(ImmutableList<HawkTuple2<EffType, Integer>> controleBuffList) {
		this.controleBuffList = controleBuffList;
	}

	/**
	 * 建筑类型
	1.月球空间站
	2.月球心灵要塞
	3.月球军备要塞
	4.月球军火要塞
	5.月球指挥要塞
	6.月球军事中心
	7.月球司令部
	8.国家飞船
	9.月球关隘
	 */
	public int getBuildTypeId() {
		return buildTypeId;
	}

	public double getNationScore() {
		return nationScore;
	}

	public double getAllianceScore() {
		return allianceScore;
	}

	public double getPlayerScore() {
		return playerScore;
	}

	public double getNationFirstScore() {
		return nationFirstScore;
	}

	public double getAllianceFirstScore() {
		return allianceFirstScore;
	}

	public double getPlayerFirstScore() {
		return playerFirstScore;
	}

	public int getDeclareCost() {
		return declareCost;
	}

	public int getDeclareTime() {
		return declareTime;
	}

	public int getOccupyTime() {
		return occupyTime;
	}

	public int getOccupyProTime() {
		return occupyProTime;
	}

	public String getBuildEffect() {
		return buildEffect;
	}

	public int getGridCnt() {
		return gridCnt;
	}

	public int getFoggyFortressId() {
		return foggyFortressId;
	}

	public int getBattleTime() {
		return battleTime;
	}

	public int getHeal() {
		return heal;
	}

	public String getAllianceReward() {
		return allianceReward;
	}

	public double getNationLastScore() {
		return nationLastScore;
	}

}
