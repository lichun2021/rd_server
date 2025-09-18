package com.hawk.activity.type.impl.submarineWar.cfg;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.serialize.string.SerializeHelper;

/**
 *  中部养成计划
 * 
 * @author che
 *
 */
@HawkConfigManager.XmlResource(file = "activity/submarine_war/submarine_war_level.xml")
public class SubmarineWarLevelCfg extends HawkConfigBase {
	// 奖励ID
	@Id
	private final int id;
	private final int time;
	private final String addMon;
	private final String minTime;
	private final String maxTime;
	private final int quickScore;
	
	
	public SubmarineWarLevelCfg() {
		id = 0;
		time = 0;
		addMon = "";
		minTime = "";
		maxTime = "";
		quickScore = 0;
	}
	
	@Override
	protected boolean assemble() {
		return true;
	}

	public int getId() {
		return id;
	}
	
	public String getAddMon() {
		return addMon;
	}


	public String getMaxTime() {
		return maxTime;
	}
	public String getMinTime() {
		return minTime;
	}
	
	public int getQuickScore() {
		return quickScore;
	}
	
	public int getTime() {
		return time;
	}

	@Override
	protected final boolean checkValid() {
		return super.checkValid();
	}
	
	
	public Map<Integer,Integer> getMonsterScore(){
		Map<Integer,Integer> monsterMap = new HashMap<>();
		String[] arr = this.addMon.split(SerializeHelper.BETWEEN_ITEMS);
		for(String param : arr){
			String[] paramArr = param.split(SerializeHelper.ATTRIBUTE_SPLIT);
			int monsterId = Integer.parseInt(paramArr[0]);
			SubmarineWarMonsterCfg monsterCfg =  HawkConfigManager.getInstance().getConfigByKey(SubmarineWarMonsterCfg.class, monsterId);
			if(Objects.isNull(monsterCfg)){
				continue;
			}
			float min = Float.parseFloat(paramArr[1]);
			float timeMax = Float.valueOf(this.time);
			int loop = Integer.parseInt(paramArr[3]);
			int count = 1;
			if(loop > 0){
				count = (int) (timeMax / min +1);
			}
			int monsterCount = monsterMap.getOrDefault(monsterId,0) + count;
			monsterMap.put(monsterId, monsterCount);
		}
		return monsterMap;
	}
	

}
