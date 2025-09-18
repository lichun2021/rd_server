package com.hawk.game.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;
import org.hawk.tuple.HawkTuple5;
import org.hawk.tuple.HawkTuples;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.hawk.game.protocol.Const.EffType;

@HawkConfigManager.XmlResource(file = "xml/lmjy_npc.xml")
public class LMJYNpcCfg extends HawkConfigBase {

	@Id
	protected final int id;
	protected final int cityLevel;
	protected final int power;// ="88888"
	protected final int icon;
	protected final String playerName;
	protected final String guildName;
	protected final String guildTag;
	protected final int maxMarchNum;

	protected final String effectList;// ="1003_3000|1011_2000|1019_1000"

	protected final String heroList;

	protected final String trapNumSec;

	protected final String trapList;

	protected final String soldierNumSec;

	protected final String soldierList;
	
	protected final int massMarchWait;//集结行军等待时间
	protected final int marchNotMarch;

	/** 决定NPC本次攻打时打哪个目标 最强_最弱_随机 */
	protected final String atkPrefer;// ="100_50_50"
	protected final String atkCD;// ="120_180" 出征CD为120到180秒间随机，副本到进行阶段时先走CD，然后再出征；
	protected final String atkToConcentrate;// ="20_80" 单人出征还是集结 20_80 代表 20%的概率单人出征，80%概率集结；
	protected final String atkTeamNumber;// ="1_2" 每次进攻，会同时出征1到2个出征队列；
	protected final int atkArmyNumber;// ="20000" 每次进攻出征士兵数;
	////////////////////////////////////
	// 特点：独立CD ，每次可以多队列侦查
	protected final String spyCD;// ="120_180" 出征CD 为120到180秒之间随机，副本进行时不走CD，先侦查后再走CD；
	protected final String spyTeamNumber;// ="1_2" 每次侦查，会同时出征1到2个侦查队列；
	////////
	protected final String asstanPrefer;// ="20_80" 每次有80%的概率向盟友发启援助
	protected final double asstanR;// ="0.8" 当自己守城兵力战力小于对方进攻兵力战力0.8倍时，要求盟友向自己援助，发启概率为80%（asstanPrefer="80"）
	protected final int asstanArmyNubmer;// ="20000" 每次援助会派遣部队数量，当剩余部队小于该值时不会援助。

	protected final String superSoldierList;//="1003_4,1004_5,1005_6" 
	protected final String armourList;//="103_10,203_20,303_30,403_40,503_10,603_20,703_30"
	////////////////////

	private List<Integer> heroIds;
	private List<NpcArmyRandInfo> foggyTrapRandInfos;
	private List<NpcArmyRandInfo> foggySoldierRandInfos;

	private ImmutableMap<EffType, Integer> effectmap;
	
	private ImmutableMap<Integer, Integer> superSoldiermap;
	private ImmutableMap<Integer, Integer> armourmap;

	public LMJYNpcCfg() {
		asstanR = 0.6;
		asstanArmyNubmer = 20000;
		asstanPrefer = "20_80";
		this.id = 0;
		this.heroList = "";
		this.trapNumSec = "";
		this.trapList = "";
		this.soldierNumSec = "";
		this.soldierList = "";
		this.cityLevel = 1;
		this.icon = 0;
		this.guildName = "NPC_GUILD";
		this.guildTag = "电脑";
		this.playerName = "帅哥";
		effectList = "";
		maxMarchNum = 5;
		atkCD = "120_180";
		atkPrefer = "100_50_50";
		atkToConcentrate = "20_80";
		atkTeamNumber = "1_2";
		atkArmyNumber = 20000;
		spyCD = "120_180";
		spyTeamNumber = "1_2";
		power = 13579;
		massMarchWait = 60;
		marchNotMarch = 10;
		superSoldierList = "";
		armourList = "";
	}

	/** 获取随机陷阱数量
	 * 
	 * @return */
	public int getRandTrapNum() {
		String[] minMax = trapNumSec.split("_");
		return HawkRand.randInt(Integer.parseInt(minMax[0]), Integer.parseInt(minMax[1]));
	}

	/** 获取随机陷阱数量
	 * 
	 * @return */
	public int getRandSoldierNum() {
		String[] minMax = soldierNumSec.split("_");
		return HawkRand.randInt(Integer.parseInt(minMax[0]), Integer.parseInt(minMax[1]));
	}

	/** 获取陷阱信息
	 * 
	 * @return */
	public Map<Integer, Integer> getRandTrapInfo() {
		Map<Integer, Integer> trapInfo = new HashMap<Integer, Integer>();
		int totalWeight = 0;
		// 首先算出命中ID
		for (NpcArmyRandInfo foggyRandInfo : foggyTrapRandInfos) {
			if (HawkRand.randPercentRate(foggyRandInfo.hitRate)) {
				// 随机权重
				int weight = HawkRand.randInt(foggyRandInfo.minWeight, foggyRandInfo.maxWeight);
				totalWeight += weight;
				// 这里先放入权重，方便后面计算
				trapInfo.put(foggyRandInfo.id, weight);
			}
		}
		// 判断空
		if (trapInfo.isEmpty()) {
			// 重新随机一个
			NpcArmyRandInfo info = HawkRand.randomObject(foggyTrapRandInfos);
			int num = getRandTrapNum();
			if (num > 0) {
				trapInfo.put(info.id, num);
			}
		} else {
			// 计算数量
			long totalNum = getRandTrapNum();
			Iterator<Entry<Integer, Integer>> it = trapInfo.entrySet().iterator();
			while (it.hasNext()) {
				Entry<Integer, Integer> entry = it.next();
				Long num = totalNum * entry.getValue() / totalWeight;
				if (num > 0) {
					trapInfo.put(entry.getKey(), num.intValue());
				} else {
					it.remove();
				}
			}
		}
		return trapInfo;
	}

	/** 获取士兵信息
	 * 
	 * @return */
	public Map<Integer, Integer> getRandSoldierInfo() {
		Map<Integer, Integer> soldierInfo = new HashMap<Integer, Integer>();
		int totalWeight = 0;
		// 首先算出命中ID
		for (NpcArmyRandInfo foggyRandInfo : foggySoldierRandInfos) {
			if (HawkRand.randPercentRate(foggyRandInfo.hitRate)) {
				// 随机权重
				int weight = HawkRand.randInt(foggyRandInfo.minWeight, foggyRandInfo.maxWeight);
				totalWeight += weight;
				// 这里先放入权重，方便后面计算
				soldierInfo.put(foggyRandInfo.id, weight);
			}
		}
		// 判断空
		if (soldierInfo.isEmpty()) {
			// 重新随机一个
			NpcArmyRandInfo info = HawkRand.randomObject(foggySoldierRandInfos);
			int num = getRandSoldierNum();
			if (num > 0) {
				soldierInfo.put(info.id, num);
			}
		} else {
			// 计算数量
			long totalNum = getRandSoldierNum();
			Iterator<Entry<Integer, Integer>> it = soldierInfo.entrySet().iterator();
			while (it.hasNext()) {
				Entry<Integer, Integer> entry = it.next();
				Long num = totalNum * entry.getValue() / totalWeight;
				if (num > 0) {
					soldierInfo.put(entry.getKey(), num.intValue());
				} else {
					it.remove();
				}
			}
		}
		return soldierInfo;
	}

	public HawkTuple5<Boolean, Boolean, Boolean, Boolean, Boolean> randA_B_C_D_E(String a_b_c_d_e) {
		String[] x_y = a_b_c_d_e.split("_");
		int[] arr = new int[5];
		int sum = 0;
		for (int i = 0; i < x_y.length; i++) {
			arr[i] = NumberUtils.toInt(x_y[i]);
			sum += arr[i];
		}
		int rand = RandomUtils.nextInt(sum);
		for (int i = 0; i < arr.length; i++) {
			rand -= arr[i];
			if (rand <= 0) {
				arr[i] = -1;
				break;
			}
		}

		return HawkTuples.tuple(arr[0] < 0, arr[1] < 0, arr[2] < 0, arr[3] < 0, arr[4] < 0);
	}

	public int randNum(String a_b) {
		String[] x_y = a_b.split("_");
		int[] arr = new int[5];
		for (int i = 0; i < x_y.length; i++) {
			arr[i] = NumberUtils.toInt(x_y[i]);
		}
		return HawkRand.randInt(arr[0], arr[1]);
	}

	@Override
	protected boolean assemble() {

		heroIds = new ArrayList<Integer>();
		if (!HawkOSOperator.isEmptyString(heroList)) {
			String[] array = heroList.split("\\|");
			for (String val : array) {
				heroIds.add(NumberUtils.toInt(val));
			}
		}

		foggyTrapRandInfos = new ArrayList<NpcArmyRandInfo>();
		if (!HawkOSOperator.isEmptyString(trapList)) {
			String[] list = trapList.split("\\|");
			for (String val : list) {
				String[] info = val.split("_");
				NpcArmyRandInfo fInfo = new NpcArmyRandInfo();
				fInfo.id = Integer.parseInt(info[0]);
				fInfo.hitRate = Integer.parseInt(info[1]);
				fInfo.minWeight = Integer.parseInt(info[2]);
				fInfo.maxWeight = Integer.parseInt(info[3]);
				foggyTrapRandInfos.add(fInfo);
			}
		}

		foggySoldierRandInfos = new ArrayList<NpcArmyRandInfo>();
		if (!HawkOSOperator.isEmptyString(soldierList)) {
			String[] list = soldierList.split("\\|");
			for (String val : list) {
				String[] info = val.split("_");
				NpcArmyRandInfo fInfo = new NpcArmyRandInfo();
				fInfo.id = Integer.parseInt(info[0]);
				fInfo.hitRate = Integer.parseInt(info[1]);
				fInfo.minWeight = Integer.parseInt(info[2]);
				fInfo.maxWeight = Integer.parseInt(info[3]);
				foggySoldierRandInfos.add(fInfo);
			}
		}

		{
			Map<EffType, Integer> map = new HashMap<>();
			List<String> effkv = Splitter.on("|").omitEmptyStrings().splitToList(getEffectList());
			for (String kv : effkv) {
				String[] kvArr = kv.split("_");
				EffType key = EffType.valueOf(NumberUtils.toInt(kvArr[0]));
				int val = NumberUtils.toInt(kvArr[1]);
				map.put(key, val);
			}
			effectmap = ImmutableMap.copyOf(map);
		}
		
		{
			Map<Integer, Integer> map = Maps.newLinkedHashMap();
			List<String> effkv = Splitter.on(",").omitEmptyStrings().splitToList(superSoldierList);
			for (String kv : effkv) {
				String[] kvArr = kv.split("_");
				int key = NumberUtils.toInt(kvArr[0]);
				int val = NumberUtils.toInt(kvArr[1]);
				map.put(key, val);
			}
			superSoldiermap = ImmutableMap.copyOf(map);
		}
		
		{
			Map<Integer, Integer> map = new HashMap<>();
			List<String> effkv = Splitter.on(",").omitEmptyStrings().splitToList(armourList);
			for (String kv : effkv) {
				String[] kvArr = kv.split("_");
				int key = NumberUtils.toInt(kvArr[0]);
				int val = NumberUtils.toInt(kvArr[1]);
				map.put(key, val);
			}
			armourmap = ImmutableMap.copyOf(map);
		}

		return true;
	}

	public ImmutableMap<EffType, Integer> getEffectmap() {
		return effectmap;
	}

	@Override
	protected boolean checkValid() {
		if (!HawkOSOperator.isEmptyString(heroList)) {
			for (Integer heroId : heroIds) {
				FoggyHeroCfg cfg = HawkConfigManager.getInstance().getConfigByKey(FoggyHeroCfg.class, heroId);
				if (cfg == null) {
					return false;
				}
			}
		}

		if (!HawkOSOperator.isEmptyString(trapList)) {
			for (NpcArmyRandInfo info : foggyTrapRandInfos) {
				BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, info.id);
				if (cfg == null) {
					return false;
				}
			}
		}

		if (!HawkOSOperator.isEmptyString(soldierList)) {
			for (NpcArmyRandInfo info : foggySoldierRandInfos) {
				BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, info.id);
				if (cfg == null) {
					return false;
				}
			}
		}

		return super.checkValid();
	}

	public String getGuildName() {
		return guildName;
	}

	public String getGuildTag() {
		return guildTag;
	}

	public int getCityLevel() {
		return cityLevel;
	}

	public String getPlayerName() {
		return playerName;
	}

	/** 随机信息类 */
	public static class NpcArmyRandInfo {

		public int id;

		public int hitRate;

		public int minWeight;

		public int maxWeight;
	}

	public int getIcon() {
		return icon;
	}

	public String getEffectList() {
		return effectList;
	}

	public List<NpcArmyRandInfo> getFoggyTrapRandInfos() {
		return foggyTrapRandInfos;
	}

	public void setFoggyTrapRandInfos(List<NpcArmyRandInfo> foggyTrapRandInfos) {
		this.foggyTrapRandInfos = foggyTrapRandInfos;
	}

	public List<NpcArmyRandInfo> getFoggySoldierRandInfos() {
		return foggySoldierRandInfos;
	}

	public void setFoggySoldierRandInfos(List<NpcArmyRandInfo> foggySoldierRandInfos) {
		this.foggySoldierRandInfos = foggySoldierRandInfos;
	}

	public void setHeroIds(List<Integer> heroIds) {
		this.heroIds = heroIds;
	}

	public void setEffectmap(ImmutableMap<EffType, Integer> effectmap) {
		this.effectmap = effectmap;
	}

	public int getMaxMarchNum() {
		return maxMarchNum;
	}

	public String getAtkPrefer() {
		return atkPrefer;
	}

	public String getAtkCD() {
		return atkCD;
	}

	public int getId() {
		return id;
	}

	public String getHeroList() {
		return heroList;
	}

	public String getTrapNumSec() {
		return trapNumSec;
	}

	public String getTrapList() {
		return trapList;
	}

	public String getSoldierNumSec() {
		return soldierNumSec;
	}

	public String getSoldierList() {
		return soldierList;
	}

	public List<Integer> getHeroIds() {
		return new ArrayList<>(heroIds);
	}

	public String getAtkToConcentrate() {
		return atkToConcentrate;
	}

	public String getAtkTeamNumber() {
		return atkTeamNumber;
	}

	public int getAtkArmyNumber() {
		return atkArmyNumber;
	}

	public String getSpyCD() {
		return spyCD;
	}

	public String getSpyTeamNumber() {
		return spyTeamNumber;
	}

	public String getAsstanPrefer() {
		return asstanPrefer;
	}

	public double getAsstanR() {
		return asstanR;
	}

	public int getAsstanArmyNubmer() {
		return asstanArmyNubmer;
	}

	public int getPower() {
		return power;
	}

	public int getMassMarchWait() {
		return massMarchWait;
	}

	public ImmutableMap<Integer, Integer> getSuperSoldiermap() {
		return superSoldiermap;
	}

	public ImmutableMap<Integer, Integer> getArmourmap() {
		return armourmap;
	}

	public int getMarchNotMarch() {
		return marchNotMarch;
	}

}
