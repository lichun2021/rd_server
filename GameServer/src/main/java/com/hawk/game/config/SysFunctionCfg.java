package com.hawk.game.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 功能开启控制
 * @author golden
 *
 */
@HawkConfigManager.XmlResource(file = "xml/sys_function.xml")
public class SysFunctionCfg extends HawkConfigBase {
	
	/**
	 * 功能id
	 */
	private final int id;
	
	/**
	 * 解锁条件
	 */
	private final String unlockCondition;

	private Map<Integer, List<Integer>> conditions;
	
	public SysFunctionCfg() {
		id = 0;
		unlockCondition = "";
	}
	
	public int getId() {
		return id;
	}

	public String getUnlockCondition() {
		return unlockCondition;
	}

	
	public Map<Integer, List<Integer>> getConditions() {
		return conditions;
	}

	@Override
	protected boolean assemble() {
		
		conditions = new HashMap<Integer, List<Integer>>();
		
		String[] condition = unlockCondition.split(",");
		for (int i = 0; i < condition.length; i++) {
			String[] conditionVal = condition[i].split("_");
			
			Integer conId = Integer.valueOf(conditionVal[0]);
			
			List<Integer> conVal = new ArrayList<>();
			for (int j = 1; j < conditionVal.length; j++) {
				conVal.add(Integer.valueOf(conditionVal[j]));
			}
			
			conditions.put(conId, conVal);
		}
		return true;
	}
	
	@Override
	protected boolean checkValid() {
		
		return true;
	}
}
