package com.hawk.game.config;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import com.hawk.game.util.GsConst;

/**
 * 大R复仇死兵返还配置
 *
 * @author lating
 *
 */
@HawkConfigManager.XmlResource(file = "xml/revenge_store_army.xml")
public class RevengeStoreArmyCfg extends HawkConfigBase {
    @Id
    protected final int id;
    // 损失兵力数值
    protected final int lossTroopsNum;
    // 返还兵比例（万分比）
    protected final int returnTroopsPercent;
    
    private static Map<Integer, RevengeStoreArmyCfg> lossTroopsCfgMap = new HashMap<Integer, RevengeStoreArmyCfg>();
    
    private static Integer[] lossTroopsArray;

	public RevengeStoreArmyCfg() {
        id = 0;
        lossTroopsNum = 0;
        returnTroopsPercent = 0;
    }

    public int getId() {
        return id;
    }

    public int getLossTroopsNum() {
		return lossTroopsNum;
	}

	public int getReturnTroopsPercent() {
		return returnTroopsPercent;
	}

	@Override
    protected boolean assemble() {
		lossTroopsCfgMap.put(lossTroopsNum, this);
        return true;
    }

    @Override
    protected boolean checkValid() {
    	if (returnTroopsPercent > GsConst.RANDOM_MYRIABIT_BASE) {
    		return false;
    	}
    	
        return true;
    }
    
    public static RevengeStoreArmyCfg getCfgByLossTroopCount(int count) {
    	if (lossTroopsArray == null) {
    		lossTroopsArray = lossTroopsCfgMap.keySet().toArray(new Integer[lossTroopsCfgMap.size()]);
    		Arrays.sort(lossTroopsArray);
    	}
    	
    	int lossTroopCnt = 0;
    	for (int i = 0; i < lossTroopsArray.length; i++) {
    		if (count >= lossTroopsArray[i]) {
    			lossTroopCnt = lossTroopsArray[i];
    		}
    	}
    	
    	return lossTroopsCfgMap.get(lossTroopCnt);
    } 

}
