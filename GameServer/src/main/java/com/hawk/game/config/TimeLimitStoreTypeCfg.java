package com.hawk.game.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 限时商店触发类型配置信息
 *
 * @author lating
 *
 */
@HawkConfigManager.XmlResource(file = "xml/goods_group_timelimit_type.xml")
public class TimeLimitStoreTypeCfg extends HawkConfigBase {
    /**
     *  触发类型
     */
	@Id
    protected final int triggerType;
    /**
     *  触发达成条件所需的数值
     */
    protected final int num;
    /**
     *  触发达成条件的时间限制
     */
    protected final int triggerDuration;
    /**
     * 添加满足时上架优先级
     */
    protected final int priority;
    /**
     * 限时出售时长:单位s
     */
    protected final int shopDuration;
    
    /**
     * 所有的触发类型
     */
    private static List<Integer> triggerTypeList = new ArrayList<Integer>();
    /**
     * 商品出售时长<triggerType, shopDuration>
     */
    private static Map<Integer, Integer> storeDurationMap = new HashMap<Integer, Integer>();
    
	public TimeLimitStoreTypeCfg() {
		triggerType = 0;
		num = 0;
		triggerDuration = 0;
		priority = 0;
		shopDuration = 0;
    }

	public int getTriggerType() {
		return triggerType;
	}

	public int getNum() {
		return num;
	}

	public int getTriggerDuration() {
		return triggerDuration;
	}

	public int getPriority() {
		return priority;
	}

	public int getShopDuration() {
		return shopDuration;
	}

	@Override
    protected boolean assemble() {
		triggerTypeList.add(triggerType);
		storeDurationMap.put(triggerType, shopDuration);
        return true;
    }

    @Override
    protected boolean checkValid() {
    	if (triggerDuration <= 0 || shopDuration <= 0 || num <= 0) {
    		return false;
    	}
    	
        return true;
    }
    
    public static boolean checkTriggerType(Integer triggerType) {
    	return triggerTypeList.contains(triggerType);
    }
    
    public static int getShopDurationByType(int triggerType) {
    	if (storeDurationMap.containsKey(triggerType)) {
    		return storeDurationMap.get(triggerType);
    	}
    	
    	return 0;
    }
}
