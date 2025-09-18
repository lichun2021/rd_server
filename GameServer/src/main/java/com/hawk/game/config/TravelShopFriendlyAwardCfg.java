package com.hawk.game.config;

import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.game.item.ItemInfo;
import com.hawk.game.util.WeightAble;

/**
 * 黑市商店好友度奖励配置
 * @author Golden
 *
 */
@HawkConfigManager.XmlResource(file = "xml/travel_shop_friendly_award.xml")
public class TravelShopFriendlyAwardCfg extends HawkConfigBase implements WeightAble {
	
	/**
	 * 配置唯一ID
	 */
	@Id
	protected final int id;
	/**
	 * 类型1普通2高级
	 */
	protected final int type;
	
	/**
	 * 组别
	 */
	protected final int group;
	
	
	/**
	 * 权重
	 */
	protected final int weight;
	/**
	 * 奖励
	 */
	protected final String award;

	public TravelShopFriendlyAwardCfg() {
		this.id = 0;
		this.type = 1;
		group= 0;
		this.weight = 0;
		this.award = "";
	}

	public int getId() {
		return id;
	}

	public int getType() {
		return type;
	}
	
	public int getGroup() {
		return group;
	}

	public int getWeight() {
		return weight;
	}

	public List<ItemInfo> getAward() {
		return ItemInfo.valueListOf(award);
	}
}