package com.hawk.activity.type.impl.medalAction.cfg;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.HawkConfigBase;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.gamelib.activity.ConfigChecker;


/**勋章宝藏kv配置表
 * @author Winder
 *
 */
@HawkConfigManager.KVResource(file = "activity/medal_action/medal_action_activity_cfg.xml")
public class MedalActionActivityKVCfg extends HawkConfigBase {
	/**
	 * 服务器开服延时开启活动时间
	 */
	private final int serverDelay;
	/**
	 * 是否重置
	 */
	private final int isDailyReset;
	
	/** 单抽消耗*/
	private final String singlePrice;
	
	/** 十连抽消耗*/
	private final String tenPrice;
	
	/** 单个道具价格*/
	private final String itemPrice;
	
	/** 购买1次获得固定奖励*/
	private final String extReward;
	/** 每日抽卡限制次数*/
	private final int dailyTimes;
	
	private RewardItem.Builder singleConsume;
	
	private RewardItem.Builder tenConsume;
	
	private RewardItem.Builder itemConsume;
	
	private List<RewardItem.Builder> extRewardItems;
	
	public MedalActionActivityKVCfg() {
		serverDelay = 0;
		isDailyReset = 0;
		singlePrice = "";
		tenPrice = "";
		itemPrice = "";
		extReward = "";
		dailyTimes = 0;
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}
	
	public boolean isDailyReset() {
		return isDailyReset == 1;
	}

	public String getSinglePrice() {
		return singlePrice;
	}

	public String getTenPrice() {
		return tenPrice;
	}
	
	public String getItemPrice() {
		return itemPrice;
	}
	
	public RewardItem.Builder getSingleConsume() {
		return singleConsume.clone();
	}

	public RewardItem.Builder getTenConsume() {
		return tenConsume.clone();
	}

	public RewardItem.Builder getItemConsume() {
		return itemConsume.clone();
	}
	
	public int getDailyTimes() {
		return dailyTimes;
	}

	public List<RewardItem.Builder> getExtRewardItems() {
		List<RewardItem.Builder> copy = new ArrayList<>();
		for(RewardItem.Builder builder : extRewardItems){
			copy.add(builder.clone());
		}
		return copy;
	}

	@Override
	protected boolean assemble() {
		singleConsume = RewardHelper.toRewardItem(singlePrice);
		tenConsume = RewardHelper.toRewardItem(tenPrice);
		itemConsume = RewardHelper.toRewardItem(itemPrice);
		extRewardItems = RewardHelper.toRewardItemImmutableList(extReward);
		if (singleConsume == null || tenConsume == null || itemConsume == null) {
			return false;
		}
		return true;
	}

	@Override
	protected boolean checkValid() {
		boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(singlePrice);
		if (!valid) {
			throw new InvalidParameterException(String.format("MedalTreasureActivityKVCfg reward error, singlePrice: %s", singlePrice));
		}
		valid = ConfigChecker.getDefaultChecker().checkAwardsValid(tenPrice);
		if (!valid) {
			throw new InvalidParameterException(String.format("MedalTreasureActivityKVCfg reward error, tenPrice: %s", tenPrice));
		}
		valid = ConfigChecker.getDefaultChecker().checkAwardsValid(itemPrice);
		if (!valid) {
			throw new InvalidParameterException(String.format("MedalTreasureActivityKVCfg reward error, itemPrice: %s", itemPrice));
		}
		return super.checkValid();
	}
	
}