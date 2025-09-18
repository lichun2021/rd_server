package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.game.service.guildtask.GuildTaskCfgItem;

/**
 * 联盟任务配置
 * @author Jesse
 *
 */
@HawkConfigManager.XmlResource(file = "xml/alliance_task.xml")
public class AllianceTaskCfg extends HawkConfigBase {
	@Id
	/**
	 * 任务id
	 */
	protected final int id;
	/**
	 * 群组id
	 */
	private final int groupId;
	/**
	 * 任务类型
	 */
	private final int conditionType;
	/**
	 * 任务条件
	 */
	private final String conditionValue;
	
	/**
	 * 任务单元
	 */
	private GuildTaskCfgItem taskItem;
	

	public AllianceTaskCfg() {
		this.id = 0;
		this.groupId = 0;
		this.conditionType = 0;
		this.conditionValue = "";
	}

	public int getId() {
		return id;
	}

	public int getGroupId() {
		return groupId;
	}

	public int getConditionType() {
		return conditionType;
	}
	
	public GuildTaskCfgItem getTaskItem() {
		return taskItem.getCopy();
	}
	
	@Override
	protected boolean assemble() {
		this.taskItem = new GuildTaskCfgItem(this.id, this.conditionType, this.conditionValue); 
		return super.assemble();
	}

	@Override
	protected boolean checkValid() {
		// TODO
		return super.checkValid();
	}
	
	

}
