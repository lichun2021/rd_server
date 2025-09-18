package com.hawk.game.service.guildtask;

import java.util.ArrayList;
import java.util.List;

/**
 * 联盟任务配置单元
 * 
 * @author jesse
 *
 */
public class GuildTaskCfgItem {

	/** 配置id */
	private int cfgId;

	/** 任务类型 */
	private GuildTaskType type;

	/** 条件id */
	private List<Integer> conditionList;

	/** 完成值 */
	private int value;

	public GuildTaskCfgItem() {
	}

	public GuildTaskCfgItem(int cfgId, int type, String condition) {
		this.cfgId = cfgId;
		this.type = GuildTaskType.valueOf(type);
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

	public GuildTaskType getType() {
		return type;
	}

	public List<Integer> getIds() {
		return new ArrayList<>(conditionList);
	}

	public int getValue() {
		return value;
	}
	
	public GuildTaskCfgItem getCopy(){
		GuildTaskCfgItem copy = new GuildTaskCfgItem();
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
