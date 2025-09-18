package com.hawk.robot.config.element;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.hawk.os.HawkOSOperator;
/**
 * 装备位解锁条件
 * @author admin
 *
 */
public class MaterialSlotLimit {
	/** 位置id */
	private int index;

	/** VIP等级限制 */
	private int vipLimit;

	/** 所需水晶 */
	private int needGold;

	public int getIndex() {
		return index;
	}

	public int getVipLimit() {
		return vipLimit;
	}

	public int getNeedGold() {
		return needGold;
	}

	public static MaterialSlotLimit valueOf(String info) {
		MaterialSlotLimit limit = new MaterialSlotLimit();
		if (limit.init(info)) {
			return limit;
		}
		return null;
	}

	public boolean init(String info) {
		if (HawkOSOperator.isEmptyString(info)) {
			return false;
		}
		if (info.contains("_")) {
			String[] strs = info.split("_");
			if (strs.length != 3) {
				return false;
			}
			index = Integer.parseInt(strs[0]);
			vipLimit = Integer.parseInt(strs[1]);
			needGold = Integer.parseInt(strs[2]);
		} else {
			index = Integer.parseInt(info);
			vipLimit = 0;
			needGold = 0;
		}
		return true;
	}

	public static List<MaterialSlotLimit> valueListOf(String info) {
		if (HawkOSOperator.isEmptyString(info)) {
			return Collections.emptyList();
		}

		List<MaterialSlotLimit> itemList = new LinkedList<>();
		String[] limitArr = info.split(",");
		for (String limitStr : limitArr) {
			MaterialSlotLimit limitInfo = MaterialSlotLimit.valueOf(limitStr);
			if (limitInfo != null) {
				itemList.add(limitInfo);
			} else {
				return null;
			}
		}
		return itemList;
	}
}
