package com.hawk.game.guild.manor;

import java.util.List;

import org.hawk.enums.EnumUtil;
import org.hawk.enums.IndexedEnum;

/**
 * 联盟领地大本状态机
 * 
 * @author zhenyu.shang
 * @since 2017年7月7日
 */
public enum ManorBastionStat implements IndexedEnum {

	/** 未解锁 */
	LOCKED(1),
	/** 未放置 */
	OPENED(2),
	/** 建造中 */
	BUILDING(3),
	/** 未完成 */
	UNCOMPELETE(4),
	/** 未驻防 */
	UNGARRISON(5),
	/** 已驻防 */
	GARRISON(6),
	/** 损毁 */
	DAMAGED(7),
	/** 修理中 */
	REPAIRING(8),
	/** 摧毁中 */
	BREAKING(9),
	/** 未完成摧毁中 */
	UN_BREAKING(10);

	private ManorBastionStat(int index) {
		this.index = index;
	}

	public final int index;

	private ManorBastionStat[] prevStates;

	static {// 设置每个状态合法的前置状态，如果不设置，则任何状态都可以进入此状态
		BUILDING.setPrevStates(UNCOMPELETE, BREAKING, GARRISON, UNGARRISON, DAMAGED, UN_BREAKING);
		UNCOMPELETE.setPrevStates(OPENED, BUILDING, UN_BREAKING);
		GARRISON.setPrevStates(UNGARRISON, BREAKING, DAMAGED, REPAIRING, BUILDING, UN_BREAKING, UNCOMPELETE);
		UNGARRISON.setPrevStates(BUILDING, GARRISON);
		DAMAGED.setPrevStates(BREAKING, REPAIRING);
		REPAIRING.setPrevStates(BREAKING, DAMAGED, GARRISON, UNGARRISON);
		BREAKING.setPrevStates(GARRISON, UNGARRISON, DAMAGED, BUILDING, UNCOMPELETE, REPAIRING);
		UN_BREAKING.setPrevStates(UNCOMPELETE, BUILDING);
	}

	private void setPrevStates(ManorBastionStat... prevStates) {
		this.prevStates = prevStates;
	}

	@Override
	public int getIndex() {
		return index;
	}

	private static final List<ManorBastionStat> values = IndexedEnumUtil.toIndexes(ManorBastionStat.values());

	public static ManorBastionStat valueOf(int index) {
		return EnumUtil.valueOf(values, index);
	}

	/**
	 * 判断是否能进入此状态
	 * @param targetState
	 * @return
	 */
	public boolean canEnter(ManorBastionStat targetState) {
		//如果是本身或者前置状态为空都可以进入
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
