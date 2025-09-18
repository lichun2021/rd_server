package com.hawk.game.config;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.math.NumberUtils;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.tuple.HawkTuple3;
import org.hawk.tuple.HawkTuple4;
import org.hawk.tuple.HawkTuples;

import com.google.common.base.Splitter;
import com.hawk.game.item.ItemInfo;

@HawkConfigManager.KVResource(file = "xml/laboratory_const.xml")
public class LaboratoryKVCfg extends HawkConfigBase {
	protected final int blockOpen; // 核心是否开放
	protected final String lockOneCost; // 锁一个消耗
	protected final String lockTwoCost; // 锁二个消耗 不叠加
	protected final String lockThreeCost; //锁3个消耗 不叠加
	protected final String remakeCost; // 改造消耗
	protected final String remakeGoldCost; // 改造消耗
	// # 解锁等级
	protected final String blockUnlock;// = 120_150_180

	// # 最多页数
	protected final int maxPage;// = 10
	protected final String lockItemId;

	private HawkTuple4<Integer, Integer, Integer, Integer> unlockLevel;
	private ItemInfo lockItem;
	public LaboratoryKVCfg() {
		blockOpen = 0;
		lockOneCost = "";
		lockTwoCost = "";
		lockThreeCost = "";
		remakeCost = "";
		blockUnlock = "";
		maxPage = 10;
		remakeGoldCost = "10000_1001_0,10000_1001_10,10000_1001_20,10000_1001_30,10000_1001_40";
		lockItemId = "30000_1480001_1";
	}

	public boolean isBlockOpen() {
		return blockOpen == 1;
	}

	@Override
	protected boolean assemble() {
		int[] arr = Splitter.on("_").splitToList(blockUnlock).stream().mapToInt(NumberUtils::toInt).toArray();
		int[] list = {Integer.MAX_VALUE,Integer.MAX_VALUE,Integer.MAX_VALUE,Integer.MAX_VALUE};
		System.arraycopy(arr, 0, list, 0, arr.length);
		unlockLevel = HawkTuples.tuple(list[0], list[1], list[2],list[3]);

		lockItem = ItemInfo.valueOf(lockItemId);
		return super.assemble();
	}

	public int getBlockOpen() {
		return blockOpen;
	}

	public String getLockOneCost() {
		return lockOneCost;
	}

	public String getLockTwoCost() {
		return lockTwoCost;
	}

	public String getRemakeCost() {
		return remakeCost;
	}

	public String getBlockUnlock() {
		return blockUnlock;
	}

	public int getMaxPage() {
		return maxPage;
	}

	public int getLockItemId() {
		return lockItem.getItemId();
	}

	public HawkTuple4<Integer, Integer, Integer, Integer> getUnlockLevel() {
		return unlockLevel;
	}

	public String getRemakeGoldCost() {
		return remakeGoldCost;
	}

	public String getLockThreeCost() {
		return lockThreeCost;
	}

}
