package com.hawk.activity.type.impl.bountyHunter.config;

import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.lang.math.NumberUtils;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.google.common.base.Splitter;

@HawkConfigManager.KVResource(file = "activity/bounty_hunter/bounty_hunter_activity_cfg.xml")
public class BountyHunterActivityKVCfg extends HawkConfigBase {

	private final long serverDelay;// = 0
	// # 第10次礼包
	private final String everyTenItem;// = 10000_1000_1;

	// # 攻击boss道具消耗
	private final String itemHitPrice;// =30000_1150072_1

	// #攻击boss道具价格
	private final String itemOnecePrice;// = 10000_1001_10

	// # 攻击一次固定奖励(防政策风险)
	private final String extReward;// =30000_840172_0

	// # 奖金池初始金额
	private final int initGold;// = 500;

	// #每次hit加金币
	private final int everyHitAdd;// =3;

	// #将池区间takeAl概率 2000_1000 代表当总量小于2000 有10%的概率得到大奖
	private final String takeAllRate;// =2000_1000,5000_4000,15000_6000,10000000_95000
	// #初始倍率, 消耗, 连续次数 对应bounty_hunter_hit.xml表
	private final int initHitId;// =7
	
//	# #每日免费
	private final String everyDayFree;// = 10000_1001_500

	private TreeMap<Integer, Double> allgoldMap = new TreeMap<>();

	public BountyHunterActivityKVCfg() {
		this.serverDelay = 0;
		this.everyTenItem = "";
		this.itemOnecePrice = "";
		this.extReward = "";
		this.initGold = 500;
		this.everyHitAdd = 3;
		this.takeAllRate = "2000_1000,5000_4000,15000_6000,10000000_95000";
		this.initHitId = 7;
		this.itemHitPrice = "";
		this.everyDayFree = "30000_9990009_10";
	}

	@Override
	protected boolean assemble() {
		// 2000_1000,5000_4000,15000_6000,10000000_95000
		Splitter.on(",").omitEmptyStrings().trimResults().split(takeAllRate).forEach(str -> {
			String[] wsarr = str.split("_");
			allgoldMap.put(NumberUtils.toInt(wsarr[0]), NumberUtils.toDouble(wsarr[1]));
		});
		return super.assemble();
	}

	/** 当前中奖率 */
	public double getTakeAllRate(int totalgold) {
		Entry<Integer, Double> higherEntry = allgoldMap.higherEntry(totalgold);
		if(higherEntry == null){
			return Double.MAX_VALUE;
		}
		return higherEntry.getValue();
	}

	public long getServerDelay() {
		return serverDelay * 1000L;
	}

	public String getEveryTenItem() {
		return everyTenItem;
	}

	public String getItemOnecePrice() {
		return itemOnecePrice;
	}

	public String getExtReward() {
		return extReward;
	}

	public String getItemHitPrice() {
		return itemHitPrice;
	}

	public int getInitGold() {
		return initGold;
	}

	public int getEveryHitAdd() {
		return everyHitAdd;
	}

	public String getTakeAllRate() {
		return takeAllRate;
	}

	public int getInitHitId() {
		return initHitId;
	}

	public String getEveryDayFree() {
		return everyDayFree;
	}

}
