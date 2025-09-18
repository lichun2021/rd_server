package com.hawk.game.module.toucai.cfg;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.google.common.collect.ImmutableMap;
import com.hawk.game.item.ItemInfo;

/**
 * 铁血军团活动配置
 * @author PhilChen
 *
 */
@HawkConfigManager.KVResource(file = "xml/medal_factory_const.xml")
public class MedalFactoryConstCfg extends HawkConfigBase {
	// # 功能开启等级，基地30级
	private final int openLevel;// = 30
	//
	// # 每日偷取道具数量上限
	private final String daylyMax;// = 30000_15900001_1000;30000_15900002_200
	//
	// # 每日刷新次数
	private final int refreshNum;// = 5
	//
	// # 刷新CD，单位秒
	private final int refreshTime;// = 300
	//
	// # 每次偷取比例，万分比
	private final int stealRatioOnce;// = 1500
	//
	// # 最大偷取比例，万分比
	private final int stealRatioMax;// = 6000
	// # 生产线总量
	private final int maxProductionNum;

	private Map<Integer, Long> itemCntMap;

	public MedalFactoryConstCfg() {
		openLevel = 0;
		daylyMax = "";
		refreshNum = 0;
		refreshTime = 0;
		stealRatioOnce = 0;
		stealRatioMax = 0;
		maxProductionNum = 0;
	}

	@Override
	protected boolean assemble() {
		List<ItemInfo> list = ItemInfo.valueListOf(daylyMax);
		Map<Integer, Long> map = new HashMap<>();
		for (ItemInfo item : list) {
			map.put(item.getItemId(), item.getCount());
		}
		itemCntMap = ImmutableMap.copyOf(map);

		return super.assemble();
	}

	public int getOpenLevel() {
		return openLevel;
	}

	public String getDaylyMax() {
		return daylyMax;
	}

	public int getRefreshNum() {
		return refreshNum;
	}

	public int getRefreshTime() {
		return refreshTime;
	}

	private int getStealRatioOnce() {
		return stealRatioOnce;
	}

	private int getStealRatioMax() {
		return stealRatioMax;
	}

	public int getMaxProductionNum() {
		return maxProductionNum;
	}

	public Map<Integer, Long> getItemCntMap() {
		return itemCntMap;
	}

	public void setItemCntMap(Map<Integer, Long> itemCntMap) {
		this.itemCntMap = itemCntMap;
	}

}
