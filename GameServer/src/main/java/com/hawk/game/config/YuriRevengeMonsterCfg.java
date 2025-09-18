package com.hawk.game.config;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.os.HawkOSOperator;

import com.hawk.game.march.ArmyInfo;

/**
 * 尤里复仇怪表
 * @author zhenyu.shang
 * @since 2017年9月21日
 */
@HawkConfigManager.XmlResource(file = "xml/yuriRevenge_monster.xml")
@HawkConfigBase.CombineId(fields = {"wave", "openServiceStartDay", "openServiceEndDay"})
public class YuriRevengeMonsterCfg extends HawkConfigBase{
	
	//波次
	protected final int wave;
	//开服时间区间-开始
	protected final int openServiceStartDay;
	//开服时间区间-结束
	protected final int openServiceEndDay;
	//怪物数据
	protected final String monster;
	
	// 尤里部队列表
	private List<ArmyInfo> armyList;

	public YuriRevengeMonsterCfg() {
		this.wave = 0;
		this.openServiceStartDay = 0;
		this.openServiceEndDay = 0;
		this.monster = "";
	}

	public int getWave() {
		return wave;
	}

	public int getOpenServiceStartDay() {
		return openServiceStartDay;
	}

	public int getOpenServiceEndDay() {
		return openServiceEndDay;
	}

	public String getMonster() {
		return monster;
	}
	
	public List<ArmyInfo> getArmyList(){
		List<ArmyInfo> list = new ArrayList<>();
		armyList.forEach(e -> list.add(e.getCopy()));
		return list;
	}
	
	@Override
	protected boolean assemble() {
		if(HawkOSOperator.isEmptyString(monster)){
			logger.error("yuriRevenge_monster.xml error, monster is null");
			return false;
		}
		armyList = new ArrayList<>();
		String[] marr = monster.split("\\|");
		for (String monsterStr : marr) {
			String[] monsters = monsterStr.split("_");
			armyList.add(new ArmyInfo(Integer.parseInt(monsters[0]), Integer.parseInt(monsters[1])));
		}
		return true;
	}
	
	
	@Override
	protected boolean checkValid() {
		Set<Integer> startSet = new HashSet<Integer>();
		Set<Integer> endSet = new HashSet<Integer>();
		ConfigIterator<YuriRevengeMonsterCfg> it = HawkConfigManager.getInstance().getConfigIterator(YuriRevengeMonsterCfg.class);
		for (YuriRevengeMonsterCfg yuriRevengeMonsterCfg : it) {
			startSet.add(yuriRevengeMonsterCfg.getOpenServiceStartDay());
			endSet.add(yuriRevengeMonsterCfg.getOpenServiceEndDay());
		}
		if(!startSet.contains(1)){
			logger.error("yuriRevenge_monster.xml error, start day must be start with 1.................");
			return false;
		}
		for (Integer endDay : endSet) {
			if(endDay == 9999){
				continue;
			}
			if(!startSet.contains(endDay + 1)){
				logger.error("yuriRevenge_monster.xml error, end day must be countine start day, endDay : {}", endDay);
				return false;
			}
		}
		return true;
	}
}
