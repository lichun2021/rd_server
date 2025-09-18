package com.hawk.activity.type.impl.order.activityOrderTwo.cfg;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;

/**
 * 战令经验商品配置
 * 
 * @author Jesse
 *
 */
@HawkConfigManager.XmlResource(file = "activity/new_order/new_order_shop.xml")
public class OrderTwoShopCfg extends HawkConfigBase {
	/** 成就id */
	@Id
	private final int id;

	/** 商品item */
	private final String item;

	/** 战令等级限制 */
	private final int level;

	/** 战令等阶限制 */
	private final int order;

	/** 购买消耗 */
	private final String price;

	/** 购买数量上限 */
	private final int num;

	private List<RewardItem.Builder> itemList;

	private List<RewardItem.Builder> costList;

	public OrderTwoShopCfg() {
		id = 0;
		item = "";
		level = 0;
		order = 0;
		price = "";
		num = 0;
	}

	public int getId() {
		return id;
	}

	public int getLevel() {
		return level;
	}

	public int getOrder() {
		return order;
	}

	public int getNum() {
		return num;
	}

	public void setCostList(List<RewardItem.Builder> costList) {
		this.costList = costList;
	}

	public List<RewardItem.Builder> getItemList() {
		List<RewardItem.Builder> copyList = new ArrayList<>();
		for (RewardItem.Builder builder : itemList) {
			copyList.add(builder.clone());
		}
		return copyList;
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
			itemList = RewardHelper.toRewardItemImmutableList(item);
			costList = RewardHelper.toRewardItemImmutableList(price);
		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}
		return true;
	}

}
