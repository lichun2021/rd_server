package com.hawk.game.module.lianmengXianquhx.cfg;

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

@HawkConfigManager.XmlResource(file = "xml/xqhx_buildType.xml")
public class XQHXBuildTypeCfg extends HawkConfigBase {

	@Id
	private final int buildTypeId;
	private final int gridCnt;
	private final double allianceScore;
	private final double allianceScoreControl;
	private final double allianceOrder;
	private final double playerScore;
	private final double allianceFirstScore;
	private final double playerFirstScore;
	private final int occupyTime;// 持续占领后开罩 ="1800"
	private final int peaceTime;
	private final int collectArmyMin;
	private final String buildEffect;
	private ImmutableList<HawkTuple2<EffType, Integer>> controleBuffList;

	public XQHXBuildTypeCfg() {
		buildTypeId = 0;
		allianceScoreControl = 0;
		allianceScore = 0;
		playerScore = 0;
		allianceOrder = 0;
		allianceFirstScore = 0;
		playerFirstScore = 0;
		occupyTime = Integer.MAX_VALUE;// 持续占领后开罩 ="1800"
		peaceTime = Integer.MAX_VALUE;
		gridCnt = 3;
		collectArmyMin = 1;
		buildEffect = "";
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

	public double getAllianceScoreControl() {
		return allianceScoreControl;
	}

	public String getBuildEffect() {
		return buildEffect;
	}

	public double getAllianceScore() {
		return allianceScore;
	}

	public double getPlayerScore() {
		return playerScore;
	}

	public double getAllianceFirstScore() {
		return allianceFirstScore;
	}

	public double getPlayerFirstScore() {
		return playerFirstScore;
	}

	public int getOccupyTime() {
		return occupyTime;
	}

	public int getGridCnt() {
		return gridCnt;
	}

	public int getPeaceTime() {
		return peaceTime;
	}

	public int getCollectArmyMin() {
		return collectArmyMin;
	}

	public double getAllianceOrder() {
		return allianceOrder;
	}

}
