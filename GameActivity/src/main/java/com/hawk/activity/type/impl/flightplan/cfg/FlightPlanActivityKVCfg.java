package com.hawk.activity.type.impl.flightplan.cfg;

import java.util.ArrayList;
import java.util.List;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;

/**
 * 威龙庆典-飞行计划活动配置
 * 
 * @author lating
 *
 */
@HawkConfigManager.KVResource(file = "activity/j20_celebration/j20_celebration_activity_cfg.xml")
public class FlightPlanActivityKVCfg extends HawkConfigBase {
	/**
	 * 服务器开服延时开启活动时间
	 */
	private final int serverDelay;
	/**
	 * 单次抽奖消耗
	 */
	private final String singlePrice;
	/**
	 * 奖励倍率
	 */
	private final String magnification;
	/**
	 * 购买骰子道具的消耗
	 */
	private final String itemPrice;

	/**
	 * 购买单个骰子道具额外获得的奖励
	 */
	private final String extReward;
	
	/**
	 * 飞行计划一次行走的最大步数
	 */
	private final int stepOnceMax;
	
	// 飞行消耗
	private int flightItemId;
	
	private List<Integer> weights = new ArrayList<Integer>();
	
	private List<Integer> ratioList = new ArrayList<Integer>();

	public FlightPlanActivityKVCfg() {
		serverDelay = 0;
		singlePrice = "";
		magnification = "";
		itemPrice = "";
		extReward = "";
		stepOnceMax = 6;
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}

	public String getSinglePrice() {
		return singlePrice;
	}
	
	public String getItemPrice() {
		return itemPrice;
	}

	public String getExtReward() {
		return extReward;
	}

	@Override
	protected boolean assemble() {
		RewardItem.Builder flightConsumeItem = RewardHelper.toRewardItem(singlePrice);
		flightItemId = flightConsumeItem.getItemId();
		if (!HawkOSOperator.isEmptyString(magnification)) {
			String[] arr = magnification.split(",");
			for (String element : arr) {
				String[] str = element.split("_");
				if (str.length >= 2) {
					ratioList.add(Integer.parseInt(str[0]));
					weights.add(Integer.parseInt(str[1]));
				}
			}
		}
		
		return true;
	}
	
	public int getFlightItemId() {
		return flightItemId;
	}

	public int getStepOnceMax() {
		return stepOnceMax;
	}

	public String getMagnification() {
		return magnification;
	}
	
	public int getCellAwardRatio() {
		if (weights.isEmpty() || ratioList.isEmpty()) {
			return 2;
		}
		
		return HawkRand.randomWeightObject(ratioList, weights);
	}

}
