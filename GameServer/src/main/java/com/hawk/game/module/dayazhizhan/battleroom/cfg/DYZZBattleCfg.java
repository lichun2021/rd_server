package com.hawk.game.module.dayazhizhan.battleroom.cfg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.tuple.HawkTuple2;
import org.hawk.tuple.HawkTuple4;
import org.hawk.tuple.HawkTuples;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.serialize.string.SerializeHelper;

@HawkConfigManager.KVResource(file = "xml/dyzz_battle.xml")
public class DYZZBattleCfg extends HawkConfigBase {

	protected final int prepairTime;// = 180
	protected final int collectTime;// = 600
	protected final int battleTime;// = 1200
	protected final int fireSpeed;
	protected final String bornPointA;// ="24,25|26,27|22,22"
	protected final String bornPointB;// ="24,25|20,18|18,16"
	protected final double playerMarchSpeedUp;// ="1.5"
	protected final int mapX;// = 140;
	protected final int mapY;// = 140;
	protected final int moveCityCd;
	protected final double cureSpeedUp;

	// # 玩家城防耐久值
	protected final int citydefense;// = 17500

	// # 玩家队列数
	protected final int queue;// = 5

	// # 玩家单次出征基础上限
	protected final int singlebattle;// = 400000

	// # 玩家单次集结基础上限
	protected final int massbattle;// = 2000000

	// # 玩家副本基础加成
	protected final String baseaddition;// = 1701_1500,1702_1500,1704_1500,1707_1500

	// # 进入鏖战阶段触发能源井伤害加成
	protected final double damage;// = 1.5

	// # 玩家战斗评分=A*玩家副本击杀数+B*玩家副本陨晶矿获取数+C*玩家防御坦克阵亡数（基地攻防不计数）
	protected final String scoreparameter;// = 5,300,5

	// # rogue机会刷新时间
	protected final String rogueSelectTimes;// = 30,60,90,120

	// # rogue机会 基地血量百分比
	protected final String rogueSelectBaseHp;// = 60,30,10

	protected final long noticeCd;
//	# 加速道具
	protected final int speedupItem;// = 820005
//	# 免费获得
	protected final int speedupItemFree;// = 10
//	# 付费购买
	protected final String speedupItemCost;// = 10000_1007_1
//	# 付费购买数
	protected final int speedupItemCnt;// = 10

	private HawkTuple4<Double, Double, Double,Double> scoreparameter3;
	private List<HawkTuple2<Integer, Integer>> bornPointAList = new ArrayList<>();
	private List<HawkTuple2<Integer, Integer>> bornPointBList = new ArrayList<>();
	private ImmutableMap<EffType, Integer> baseadditionBuffMap;
	private List<Integer> rogueSelectTimesList = ImmutableList.of();
	private List<Integer> rogueSelectBaseHpList = ImmutableList.of();

	public DYZZBattleCfg() {
		this.bornPointA = "";
		this.bornPointB = "";
		prepairTime = 30;
		collectTime = 600;
		battleTime = 1200;
		fireSpeed = 10;
		playerMarchSpeedUp = 1.5;
		mapX = 140;
		mapY = 140;
		moveCityCd = 30;
		cureSpeedUp = 1;
		citydefense = 17500;
		queue = 5;
		singlebattle = 400000;
		massbattle = 2000000;
		baseaddition = "1701_1500,1702_1500,1704_1500,1707_1500";
		damage = 1.5;
		scoreparameter = "5,300,5";
		rogueSelectTimes = "";
		rogueSelectBaseHp = "";
		noticeCd = 5;
		speedupItem = 0;
		speedupItemFree = 0;
		speedupItemCost = "";
		speedupItemCnt = 0;
	}

	@Override
	protected boolean assemble() {
		for (String xy : getBornPointA().trim().split("\\,")) {
			String[] x_y = xy.split("_");
			if (x_y.length != 2) {
				continue;
			}
			int[] pos = new int[2];
			pos[0] = NumberUtils.toInt(x_y[0]);
			pos[1] = NumberUtils.toInt(x_y[1]);
			bornPointAList.add(HawkTuples.tuple(pos[0], pos[1]));
		}
		for (String xy : getBornPointB().trim().split("\\,")) {
			String[] x_y = xy.split("_");
			if (x_y.length != 2) {
				continue;
			}
			int[] pos = new int[2];
			pos[0] = NumberUtils.toInt(x_y[0]);
			pos[1] = NumberUtils.toInt(x_y[1]);
			bornPointBList.add(HawkTuples.tuple(pos[0], pos[1]));
		}

		if (bornPointAList.size() < 5 || bornPointBList.size() < 5) {
			throw new RuntimeException("BornPoint error " + bornPointA);
		}

		{
			String[] x_y_z = scoreparameter.split(",");
			scoreparameter3 = HawkTuples.tuple(Double.valueOf(x_y_z[0]), Double.valueOf(x_y_z[1]), Double.valueOf(x_y_z[2]),Double.valueOf(x_y_z[3]));
		}

		{
			Map<EffType, Integer> lsit = new HashMap<>();
			for (String xy : Splitter.on(",").omitEmptyStrings().splitToList(baseaddition)) {
				String[] x_y = xy.split("_");
				int[] pos = new int[2];
				pos[0] = NumberUtils.toInt(x_y[0]);
				pos[1] = NumberUtils.toInt(x_y[1]);
				lsit.put(EffType.valueOf(pos[0]), pos[1]);
			}
			baseadditionBuffMap = ImmutableMap.copyOf(lsit);
		}
		
		if (StringUtils.isNotEmpty(rogueSelectTimes)) {
			rogueSelectTimesList = ImmutableList.copyOf(SerializeHelper.stringToList(Integer.class, rogueSelectTimes, SerializeHelper.BETWEEN_ITEMS));
		}
		
		if (StringUtils.isNotEmpty(rogueSelectBaseHp)) {
			rogueSelectBaseHpList = ImmutableList.copyOf(SerializeHelper.stringToList(Integer.class, rogueSelectBaseHp, SerializeHelper.BETWEEN_ITEMS));
		}

		return super.assemble();
	}

	public List<int[]> copyOfbornPointAList() {
		List<int[]> result = new LinkedList<int[]>();
		for (HawkTuple2<Integer, Integer> ht : bornPointAList) {
			result.add(new int[] { ht.first, ht.second });
		}
		return result;
	}

	public List<int[]> copyOfbornPointBList() {
		List<int[]> result = new LinkedList<int[]>();
		for (HawkTuple2<Integer, Integer> ht : bornPointBList) {
			result.add(new int[] { ht.first, ht.second });
		}
		return result;
	}

	public String getBornPointA() {
		return bornPointA;
	}

	public String getBornPointB() {
		return bornPointB;
	}

	public int getPrepairTime() {
		return prepairTime;
	}

	public int getFireSpeed() {
		return fireSpeed;
	}

	public double getPlayerMarchSpeedUp() {
		return playerMarchSpeedUp;
	}

	public int getMapX() {
		return mapX;
	}

	public int getMapY() {
		return mapY;
	}

	public int getMoveCityCd() {
		return moveCityCd;
	}

	public double getCureSpeedUp() {
		return cureSpeedUp;
	}

	public List<HawkTuple2<Integer, Integer>> getBornPointAList() {
		return bornPointAList;
	}

	public void setBornPointAList(List<HawkTuple2<Integer, Integer>> bornPointAList) {
		this.bornPointAList = bornPointAList;
	}

	public List<HawkTuple2<Integer, Integer>> getBornPointBList() {
		return bornPointBList;
	}

	public void setBornPointBList(List<HawkTuple2<Integer, Integer>> bornPointBList) {
		this.bornPointBList = bornPointBList;
	}

	public int getCollectTime() {
		return collectTime;
	}

	public int getBattleTime() {
		return battleTime;
	}

	public int getCitydefense() {
		return citydefense;
	}

	public int getQueue() {
		return queue;
	}

	public int getSinglebattle() {
		return singlebattle;
	}

	public int getMassbattle() {
		return massbattle;
	}

	public String getBaseaddition() {
		return baseaddition;
	}

	public double getDamage() {
		return damage;
	}

	public String getScoreparameter() {
		return scoreparameter;
	}

	public HawkTuple4<Double, Double, Double,Double> getScoreparameter3() {
		return scoreparameter3;
	}

	public ImmutableMap<EffType, Integer> getBaseadditionBuffMap() {
		return baseadditionBuffMap;
	}

	public List<Integer> getRogueSelectTimesList() {
		return rogueSelectTimesList;
	}

	public void setRogueSelectTimesList(List<Integer> rogueSelectTimesList) {
		this.rogueSelectTimesList = rogueSelectTimesList;
	}

	public List<Integer> getRogueSelectBaseHpList() {
		return rogueSelectBaseHpList;
	}

	public void setRogueSelectBaseHpList(List<Integer> rogueSelectBaseHpList) {
		this.rogueSelectBaseHpList = rogueSelectBaseHpList;
	}

	public String getRogueSelectTimes() {
		return rogueSelectTimes;
	}

	public String getRogueSelectBaseHp() {
		return rogueSelectBaseHp;
	}

	public void setScoreparameter3(HawkTuple4<Double, Double, Double,Double> scoreparameter3) {
		this.scoreparameter3 = scoreparameter3;
	}

	public void setBaseadditionBuffMap(ImmutableMap<EffType, Integer> baseadditionBuffMap) {
		this.baseadditionBuffMap = baseadditionBuffMap;
	}

	public long getNoticeCd() {
		return noticeCd * 1000l;
	}

	public int getSpeedupItem() {
		return speedupItem;
	}

	public int getSpeedupItemFree() {
		return speedupItemFree;
	}

	public String getSpeedupItemCost() {
		return speedupItemCost;
	}

	public int getSpeedupItemCnt() {
		return speedupItemCnt;
	}
	
	
}
