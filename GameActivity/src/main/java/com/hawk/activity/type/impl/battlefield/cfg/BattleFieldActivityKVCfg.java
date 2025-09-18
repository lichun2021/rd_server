package com.hawk.activity.type.impl.battlefield.cfg;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 战场寻宝活动配置
 * 
 * @author lating
 *
 */
@HawkConfigManager.KVResource(file = "activity/battlefield_treasure/battlefield_treasure_cfg.xml")
public class BattleFieldActivityKVCfg extends HawkConfigBase {
	/**
	 * 服务器开服延时开启活动时间
	 */
	private final int serverDelay;
	/**
	 * 随机点数的骰子投掷次数
	 */
	private final int ordinaryDiceLimit;
	/**
	 * 固定点数的骰子投掷次数
	 */
	private final int controlDiceLimit;
	/**
	 * 随机点数的骰子Id
	 */
	private final int ordinaryDiceId;
	/**
	 * 固定点数的骰子Id
	 */
	private final int controlDiceId;
	/**
	 * 投骰子投出点数上限
	 */
	private final int pointLimit;
	/**
	 * 转完一圈的最终奖励
	 */
	private final String finalAward;
	/**
	 * 随机点数的骰子的价格
	 */
	private final String ordinaryDicePrice;
	/**
	 * 固定点数骰子的价格
	 */
	private final String controlDicePrice;
	/**
	 * 通行证直购ID,安卓_苹果 490004_490009
	 */
	private final String payId;
	/**
	 * 奖励池显示顺序
	 */
	private final String displayOrder;

	private final String oneConsume;// = 30000_21061001_3,30000_21061002_3
	// # 一键跑图次数上限
	private final int oneTimeLimit;

	private List<Integer> awardPoolOrderList = new ArrayList<Integer>();

	public BattleFieldActivityKVCfg() {
		serverDelay = 0;
		ordinaryDiceLimit = 0;
		controlDiceLimit = 0;
		controlDiceId = 0;
		ordinaryDiceId = 0;
		pointLimit = 6;
		finalAward = "";
		ordinaryDicePrice = "";
		controlDicePrice = "";
		payId = "";
		displayOrder = "1_2_3_4_5_6";
		oneConsume = "";
		oneTimeLimit = 0;
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}

	public int getRandomDiceItemId() {
		return ordinaryDiceId;
	}

	public int getFixedDiceItemId() {
		return controlDiceId;
	}

	public int getPointLimit() {
		return pointLimit;
	}

	public String getFinalAward() {
		return finalAward;
	}

	public String getOrdinaryDicePrice() {
		return ordinaryDicePrice;
	}

	public String getControlDicePrice() {
		return controlDicePrice;
	}

	public String getPayId() {
		return payId;
	}

	public int getOrdinaryDiceLimit() {
		return ordinaryDiceLimit;
	}

	public int getControlDiceLimit() {
		return controlDiceLimit;
	}

	public boolean assemble() {
		String[] pools = displayOrder.split("_");
		for (String poolId : pools) {
			awardPoolOrderList.add(Integer.valueOf(poolId));
		}
		return true;
	}

	public List<Integer> getAwardPoolOrderList() {
		return awardPoolOrderList;
	}

	public String getOneConsume() {
		return oneConsume;
	}

	public int getOneTimeLimit() {
		return oneTimeLimit;
	}

}
