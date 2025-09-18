package com.hawk.activity.type.impl.plantFortress.cfg;

import java.util.HashMap;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.hawk.gamelib.GameConst;

/**
 * 时空好礼时空之门直购礼包道具
 * 
 * @author che
 *
 */
@HawkConfigManager.XmlResource(file = "activity/tiberium_fortress/tiberium_fortress_stage.xml")
public class PlantFortressStage extends HawkConfigBase {
	// 奖励ID
	@Id
	private final int id;
	private final String commAwards;
	private final int bigAwardPool;
	private final int bigAwardWeight;
	
	private Map<Integer,Integer> rewardMap = new HashMap<Integer,Integer>();
	
	
	public static final int xLength = 4;
	public static final int yLength = 3;

	public PlantFortressStage() {
		id = 0;
		bigAwardWeight = 0;
		bigAwardPool = 0;
		commAwards = "";
	}
	
	@Override
	protected boolean assemble() {
		if(!HawkOSOperator.isEmptyString(commAwards)){
			String[] arr = commAwards.split(",");
			for(String str : arr){
				String[] param = str.split("_");
				if(param.length != 2){
					return false;
				}
				int p1 = Integer.parseInt(param[0]);
				int p2 = Integer.parseInt(param[1]);
				rewardMap.put(p1, p2);
			}
		}
		return true;
	}

	
	
	

	
	
	public int getId() {
		return id;
	}

	public String getCommAwards() {
		return commAwards;
	}

	public int getBigAwardPool() {
		return bigAwardPool;
	}

	public int getBigAwardWeight() {
		return bigAwardWeight;
	}

	public static int getXlength() {
		return xLength;
	}

	public static int getYlength() {
		return yLength;
	}

	

	public Map<Integer, Integer> getRewardMap() {
		return rewardMap;
	}

	
	public boolean valiteId(int id){
		if(id < 0){
			return false;
		}
		int x = id / GameConst.RANDOM_MYRIABIT_BASE;
		int y = id % GameConst.RANDOM_MYRIABIT_BASE;
		if(x <= 0){
			return false;
		}
		if(y <= 0){
			return false;
		}
		if(x > xLength){
			return false;
		}
		if(y > yLength){
			return false;
		}
		return true;
	}
	
	
	


}
