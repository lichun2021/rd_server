package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 联盟任务群组配置
 * 
 * @author Jesse
 *
 */
@HawkConfigManager.XmlResource(file = "xml/alliance_task_group.xml")
public class AllianceTaskGroupCfg extends HawkConfigBase {
	// <data groupId="14" type="1" weight="20" openDay="0" closeDay="60" />
	@Id
	protected final int groupId;

	/**
	 * 群组类别 0-日常,1-随机
	 */
	private final int type;
	/**
	 * 随机权重
	 */
	private final int weight;

	/**
	 * 任务开始刷新天数(距开服日期)
	 */
	private final int openDay;

	/**
	 * 任务停止刷新天数(距开服日期)
	 */
	private final int closeDay;

	public AllianceTaskGroupCfg() {
		this.groupId = 0;
		this.type = 0;
		this.weight = 0;
		this.openDay = 0;
		this.closeDay = 0;
	}

	public int getGroupId() {
		return groupId;
	}

	public int getType() {
		return type;
	}

	public int getWeight() {
		return weight;
	}

	public int getOpenDay() {
		return openDay;
	}

	public int getCloseDay() {
		return closeDay;
	}

	@Override
	protected boolean checkValid() {
		// TODO
		return super.checkValid();
	}

}
