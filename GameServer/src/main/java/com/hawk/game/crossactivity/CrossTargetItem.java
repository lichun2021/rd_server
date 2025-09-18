package com.hawk.game.crossactivity;

import java.util.ArrayList;
import java.util.List;

/**
 * 跨服活动任务配置单元
 * 
 * @author jesse
 *
 */
public class CrossTargetItem {

	/** 配置id */
	private int cfgId;

	/** 任务类型 */
	private CrossTargetType type;

	/** 条件id */
	private List<Integer> conditionList;

	/** 完成值 */
	private int value;

	public CrossTargetItem() {
	}

	public CrossTargetItem(int cfgId, int type, String condition) {
		this.cfgId = cfgId;
		this.type = CrossTargetType.getType(type);
		if (this.type == null) {
			throw new NullPointerException();
		}

		this.conditionList = new ArrayList<Integer>();

		String[] strArr = condition.split("_");
		if (strArr.length > 1) {
			for (int i = 0; i < strArr.length - 1; i++) {
				conditionList.add(Integer.parseInt(strArr[i]));
			}
		}
		this.value = Integer.parseInt(strArr[strArr.length - 1]);
	}

	public int getCfgId() {
		return cfgId;
	}

	public CrossTargetType getType() {
		return type;
	}

	public List<Integer> getIds() {
		return new ArrayList<>(conditionList);
	}

	public int getValue() {
		return value;
	}
	
	public CrossTargetItem getCopy(){
		CrossTargetItem copy = new CrossTargetItem();
		copy.cfgId = this.cfgId;
		copy.type = this.type;
		copy.value = this.value;
		List<Integer> conditions = new ArrayList<>();
		for(Integer condition : this.conditionList){
			conditions.add(condition);
		}
		copy.conditionList = conditions;
		return copy;
	}
}
