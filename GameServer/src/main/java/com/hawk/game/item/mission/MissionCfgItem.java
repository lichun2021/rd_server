package com.hawk.game.item.mission;

import java.util.ArrayList;
import java.util.List;

import org.hawk.os.HawkOSOperator;

import com.hawk.game.service.mssion.MissionType;

/**
 * 任务配置结构体
 * 
 * @author golden
 *
 */
public class MissionCfgItem {

	/** 配置id */
	private int cfgId;

	/** 任务类型 */
	private MissionType type;

	/** 条件id */
	private List<Integer> ids;

	/** 完成值 */
	private int value;

	public MissionCfgItem(int cfgId, int type, String ids, int value) {
		this.cfgId = cfgId;
		this.type = MissionType.valueOf(type);
		if (this.type == null) {
			throw new NullPointerException();
		}
		
		this.value = value;
		this.ids = new ArrayList<Integer>();

		String[] idArr = ids.split(";");
		for (String id : idArr) {
			if (HawkOSOperator.isEmptyString(id)) {
				continue;
			}
			this.ids.add(Integer.parseInt(id));
		}
	}

	public int getCfgId() {
		return cfgId;
	}

	public MissionType getType() {
		return type;
	}

	public List<Integer> getIds() {
		return new ArrayList<>(ids);
	}

	public int getValue() {
		return value;
	}
}
