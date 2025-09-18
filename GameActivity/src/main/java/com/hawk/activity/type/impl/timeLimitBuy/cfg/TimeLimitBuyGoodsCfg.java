package com.hawk.activity.type.impl.timeLimitBuy.cfg;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;

/**
 * 限时抢购
 * @author Golden
 *
 */
@HawkConfigManager.XmlResource(file = "activity/time_limit_buy/time_limit_buy_goods.xml")
public class TimeLimitBuyGoodsCfg extends HawkConfigBase {
	
	/**
	 * 商品id
	 */
	@Id
	private final int goodsId;
	
	/**
	 * 大区id
	 */
	private final String areaId;
	
	/**
	 * 期数
	 */
	private final int turn;
	
	/**
	 * 商品列表
	 */
	private final String goodsList;
	
	/**
	 * 商品价格
	 */
	private final String buyPrice;
	
	/**
	 * 全服购买限制次数
	 */
	private final int awardBuyLimit;
	
	/**
	 * 单人购买限制次数
	 */
	private final int personalBuyLimit;
	
	/**
	 * 注水
	 */
	private final String waterFlood;
	
	/**
	 * 奖励
	 */
	private List<RewardItem.Builder> reward;

	/**
	 * 消耗
	 */
	private List<RewardItem.Builder> consume;
	
	/**
	 * 注水
	 */
	private List<Long> waterFloodTime;
	private List<Integer> waterFloodValue;
	
	public TimeLimitBuyGoodsCfg() {
		goodsId = 0;
		turn = 0;
		goodsList = "";
		buyPrice = "";
		awardBuyLimit = 0;
		personalBuyLimit = 0;
		waterFlood = "";
		areaId = "";
	}

	public int getGoodsId() {
		return goodsId;
	}

	public int getTurn() {
		return turn;
	}

	public String getGoodsList() {
		return goodsList;
	}

	public String getBuyPrice() {
		return buyPrice;
	}

	public int getAwardBuyLimit() {
		return awardBuyLimit;
	}

	public int getPersonalBuyLimit() {
		return personalBuyLimit;
	}
	
	public String getAreaId() {
		return areaId;
	}

	public List<RewardItem.Builder> getReward() {
		return new ArrayList<>(reward);
	}

	public List<RewardItem.Builder> getConsume() {
		return new ArrayList<>(consume);
	}

	public List<Long> getWaterFloodTime() {
		return waterFloodTime;
	}

	public List<Integer> getWaterFloodValue() {
		return waterFloodValue;
	}

	@Override
	protected boolean assemble() {
		try {
			reward = RewardHelper.toRewardItemImmutableList(goodsList);
			consume = RewardHelper.toRewardItemImmutableList(buyPrice);
			
			// 注水配置
			List<Long> waterFloodTime = new ArrayList<>();
			List<Integer> waterFloodValue = new ArrayList<>();
			if (!HawkOSOperator.isEmptyString(waterFlood)) {
				String[] flood = waterFlood.split(",");
				for (int i = 0; i < flood.length; i++) {
					String[] split = flood[i].split("_");
					waterFloodTime.add(Integer.parseInt(split[0]) * 60 * 1000L);
					waterFloodValue.add(Integer.valueOf(split[1]));
				}
			}
			this.waterFloodTime = waterFloodTime;
			this.waterFloodValue = waterFloodValue;
			
		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}
		return true;
	}
}
