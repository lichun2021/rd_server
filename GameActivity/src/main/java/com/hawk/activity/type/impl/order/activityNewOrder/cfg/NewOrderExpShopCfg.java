package com.hawk.activity.type.impl.order.activityNewOrder.cfg;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;


/**
 * 战令经验商品配置
 * @author Jesse
 *
 */
@HawkConfigManager.XmlResource(file = "activity/order_two/%s/order_two_expshop.xml", autoLoad=false, loadParams="211")
public class NewOrderExpShopCfg extends HawkConfigBase {
	/** 成就id */
	@Id
	private final int id;
	/** 条件值 */
	private final String price;
	/** 经验值 */
	private final int exp;

	private List<RewardItem.Builder> costList;

	public NewOrderExpShopCfg() {
		id = 0;
		price = "";
		exp = 0;
	}

	public int getId() {
		return id;
	}

	public int getExp() {
		return exp;
	}

	public List<RewardItem.Builder> getCostList() {
		List<RewardItem.Builder> copyList = new ArrayList<>();
		for (RewardItem.Builder builder : costList) {
			copyList.add(builder.clone());
		}
		return copyList;
	}

	@Override
	protected boolean assemble() {
		try {
			costList = RewardHelper.toRewardItemImmutableList(price);
		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}
		return true;
	}

}
