package com.hawk.activity.type.impl.groupBuy.cfg;

import java.util.ArrayList;
import java.util.List;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.serialize.string.SerializeHelper;

@HawkConfigManager.XmlResource(file = "activity/group_buy/group_buying_goods.xml")
public class GroupBuyGoodsCfg extends HawkConfigBase {
	@Id
	private final int id;
	//奖励
	private final String rewards;
	//购买次数
	private final int buyTimes;
	//微信注水条件
	private final String waterLimitWX;
	//微信注水参数
	private final String waterScaleWX;
	//QQ注水条件
	private final String waterLimitQQ;
	//QQ注水参数
	private final String waterScaleQQ;
	//定时WX注水参数
	private final String buyCountAssistWX;
	//定时QQ注水参数
	private final String buyCountAssistQQ;
	
	//商品类型(是否为热销商品)
	private final int hotSell;
	//热销商品免费积分
	private final int points;
	
	private List<RewardItem.Builder> rewardList;
	
	private List<int[]> buyCountAssistWXList = new ArrayList<>();
	
	private List<int[]> buyCountAssistQQList = new ArrayList<>();
	
	public GroupBuyGoodsCfg() {
		this.id = 0;
		this.rewards = "";
		this.buyTimes = 0;
		this.waterLimitWX = "";
		this.waterScaleWX = "";
		this.waterLimitQQ = "";
		this.waterScaleQQ = "";
		this.buyCountAssistWX = "";
		this.buyCountAssistQQ = "";
		this.hotSell = 0;
		this.points = 0;
	}
	
	@Override
	protected boolean assemble() {
		try {
			rewardList = RewardHelper.toRewardItemImmutableList(rewards);
			buyCountAssistWXList = SerializeHelper.str2intList(this.buyCountAssistWX);
			buyCountAssistQQList = SerializeHelper.str2intList(this.buyCountAssistQQ);
			if (hotSell > 0 && points <= 0) {
				HawkLog.errPrintln("activity/group_buy/group_buying_goods.xml hotSell config error, id: {}, points: {}", id, points);
				return false;
			}
		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}
		return true;
	}

	public int getId() {
		return id;
	}

	public String getRewards() {
		return rewards;
	}

	public int getBuyTimes() {
		return buyTimes;
	}

	public String getWaterLimitWX() {
		return waterLimitWX;
	}

	public String getWaterScaleWX() {
		return waterScaleWX;
	}

	public String getWaterLimitQQ() {
		return waterLimitQQ;
	}

	public String getWaterScaleQQ() {
		return waterScaleQQ;
	}

	public int getHotSell() {
		return hotSell;
	}

	public int getPoints() {
		return points;
	}

	public List<int[]> getBuyCountAssistWXList() {
		return buyCountAssistWXList;
	}


	public List<int[]> getBuyCountAssistQQList() {
		return buyCountAssistQQList;
	}


	public String getBuyCountAssistWX() {
		return buyCountAssistWX;
	}

	public String getBuyCountAssistQQ() {
		return buyCountAssistQQ;
	}

	public List<RewardItem.Builder> getRewardList() {
		return rewardList;
	}

	
}
