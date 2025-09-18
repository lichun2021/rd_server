package com.hawk.game.guild.manor;

import java.util.List;

import org.hawk.enums.EnumUtil;
import org.hawk.enums.IndexedEnum;

/**
 * 联盟建筑通用状态
 * @author zhenyu.shang
 * @since 2017年7月7日
 */
public enum GuildBuildingStat implements IndexedEnum {

	/** 未解锁 */
	LOCKED(1),
	/** 未放置 */
	OPENED(2),
	/** 建造中 */
	BUILDING(3),
	/** 未完成 */
	UNCOMPELETE(4),
	/** 已完成 */
	COMPELETE(5),
	/** 采集中, 防御中, 存储中 */
	COLLECT(6),	
	;

	private GuildBuildingStat(int index) {
		this.index = index;
	}

	public final int index;

	private GuildBuildingStat[] prevStates;

	static {// 设置每个状态合法的前置状态，如果不设置，则任何状态都可以进入此状态
		BUILDING.setPrevStates(UNCOMPELETE);
		UNCOMPELETE.setPrevStates(OPENED, BUILDING);
		COMPELETE.setPrevStates(BUILDING,COLLECT);
		COLLECT.setPrevStates(COMPELETE, BUILDING);
	}

	private void setPrevStates(GuildBuildingStat... prevStates) {
		this.prevStates = prevStates;
	}

	@Override
	public int getIndex() {
		return index;
	}

	private static final List<GuildBuildingStat> values = IndexedEnumUtil.toIndexes(GuildBuildingStat.values());

	public static GuildBuildingStat valueOf(int index) {
		return EnumUtil.valueOf(values, index);
	}

	/**
	 * 判断是否能进入此状态
	 * @param targetState
	 * @return
	 */
	public boolean canEnter(GuildBuildingStat targetState) {
		if (targetState == this || targetState.prevStates == null) {
			return true;
		}
		for (int i = 0; i < targetState.prevStates.length; i++) {
			if (targetState.prevStates[i] == this) {
				return true;
			}
		}
		return false;
	}
}