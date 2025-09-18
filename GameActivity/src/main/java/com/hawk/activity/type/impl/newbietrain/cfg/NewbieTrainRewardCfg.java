package com.hawk.activity.type.impl.newbietrain.cfg;

import com.hawk.gamelib.activity.ConfigChecker;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkRand;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.security.InvalidParameterException;

/**
 * 新兵作训-作训奖励配置
 */
@HawkConfigManager.XmlResource(file = "activity/newbie_train/%s/newbie_train_lottery.xml", autoLoad=false, loadParams="334")
public class NewbieTrainRewardCfg extends HawkConfigBase {
    @Id
    private final int id;
    /**
     * 1-英雄；2-装备
     */
    private final int type;
    /**
     * 若type=1，则填写英雄ID，若type=2，则填写兵种ID
     */
    private final int typeValue;
    /**
     * 权重
     */
    private final int weight;
    
    private final String gainItem;
    
    private static Map<Integer, Set<Integer>> trainObjIdMap = new HashMap<>();
    
    private static Map<String, List<String>> trainRewardMap = new HashMap<>();
    private static Map<String, List<Integer>> trainRewardWeightMap = new HashMap<>();

    public NewbieTrainRewardCfg(){
        this.id = 0;
        this.type = 0;
        this.typeValue = 0;
        this.weight = 0;
        this.gainItem = "";
    }

    public int getId() {
        return id;
    }

	public int getType() {
		return type;
	}

	public int getTypeValue() {
		return typeValue;
	}

	public int getWeight() {
		return weight;
	}

	public String getGainItem() {
		return gainItem;
	}

	@Override
    protected boolean assemble() {
		Set<Integer> set = trainObjIdMap.get(type);
		if (set == null) {
			set = new HashSet<>();
			trainObjIdMap.put(type, set);
		}
		set.add(typeValue);
		
		String key = String.format("%d_%d", type, typeValue);
		List<String> rewardList = trainRewardMap.get(key);
		if (rewardList == null) {
			rewardList = new ArrayList<>();
			trainRewardMap.put(key, rewardList);
		}
		rewardList.add(gainItem);
		
		List<Integer> weightList = trainRewardWeightMap.get(key);
		if (weightList == null) {
			weightList = new ArrayList<>();
			trainRewardWeightMap.put(key, weightList);
		}
		weightList.add(weight);
		
		return true;
    }

    @Override
    protected boolean checkValid() {
        boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(gainItem);
        if (!valid) {
            throw new InvalidParameterException(String.format("NewbieTrainRewardCfg gainItem error, id: %s , gainItem: %s", id, gainItem));
        }
        return super.checkValid();
    }
    
    public static Set<Integer> getTrainObjIdSet(int trainType) {
    	return trainObjIdMap.getOrDefault(trainType, Collections.emptySet());
    }

    /**
     * 随机奖励
     * @param trainType
     * @param objId
     * @return
     */
    public static String randomAward(int trainType, int objId) {
    	String key = String.format("%d_%d", trainType, objId);
    	List<String> rewardList = trainRewardMap.get(key);
    	List<Integer> weightList = trainRewardWeightMap.get(key);
    	if (rewardList == null || trainRewardWeightMap == null) {
    		return null;
    	}
    	
    	return HawkRand.randomWeightObject(rewardList, weightList);
    }
}
