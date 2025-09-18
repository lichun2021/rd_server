package com.hawk.activity.type.impl.buildlevel.entity;

import java.util.ArrayList;
import java.util.List;

import com.hawk.game.protocol.Activity.AchieveState;
import com.hawk.serialize.string.SerializeHelper;

public class BuildLevelItem {

	/** 活动项id */
	private int itemId;

	/** 玩家建筑等级 */
	private int buildLevel;

	/** 活动项状态 */
	private int state;

	public static BuildLevelItem valueOf(int itemId, int buildLevel, int state) {
		BuildLevelItem item = new BuildLevelItem();
		item.itemId = itemId;
		item.buildLevel = buildLevel;
		item.state = state;
		return item;
	}

	public static BuildLevelItem valueOf(String data) {
		String[] array = SerializeHelper.split(data, SerializeHelper.ATTRIBUTE_SPLIT);
		String[] fillArray = SerializeHelper.fillStringArray(array, 3, "0");
		int index = 0;
		BuildLevelItem item = new BuildLevelItem();
		item.itemId = SerializeHelper.getInt(fillArray, index++);
		item.buildLevel = SerializeHelper.getInt(fillArray, index++);
		item.state = SerializeHelper.getInt(fillArray, index++);
		return item;
	}

	@Override
	public String toString() {
		List<Object> list = new ArrayList<>();
		list.add(itemId);
		list.add(buildLevel);
		list.add(state);
		return SerializeHelper.collectionToString(list, SerializeHelper.ATTRIBUTE_SPLIT);
	}

	public int getItemId() {
		return itemId;
	}

	public void setItemId(int itemId) {
		this.itemId = itemId;
	}

	public int getBuildLevel() {
		return buildLevel;
	}

	public void setBuildLevel(int buildLevel) {
		this.buildLevel = buildLevel;
	}

	public int getState() {
		return state;
	}

	public AchieveState getStateType() {
		for (AchieveState type : AchieveState.values()) {
			if (type.getNumber() == state) {
				return type;
			}
		}
		return null;
	}

	public void setState(int state) {
		this.state = state;
	}

}
