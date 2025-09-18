package com.hawk.activity.type.impl.pandoraBox.cfg;

import java.security.InvalidParameterException;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager.XmlResource;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.gamelib.activity.ConfigChecker;

@XmlResource(file="activity/pandora_box/pandora_box_shop.xml")
public class PandoraStoreConfig extends HawkConfigBase {
		
	/** 序号 **/
	@Id
	private final int id;
	/** 商品(type_itemId_count) **/
	private final String goods;
	/** 总库存 **/
	private final int total;
	/** 售价 **/
	private final int price;
	/**
	 * goods 转换.
	 */
	private List<RewardItem.Builder> goodsList;
	public PandoraStoreConfig() {
		this.id = 0;
		this.goods = "";
		this.total = 0;
		this.price = 0;
	}
	public int getId() {
		return id;
	}
	public String getGoods() {
		return goods;
	}
	public int getTotal() {
		return total;
	}
	public int getPrice() {
		return price;
	}
	
	protected boolean assemble() {
		goodsList = RewardHelper.toRewardItemImmutableList(goods);
		return true;
	}
	
	@Override
	protected boolean checkValid() {
		boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(goods);
		if(!valid){
			throw new InvalidParameterException(String.format("goods error, id: %d, Class name: %s ", getId(), getClass().getName())); 
		}
		return super.checkValid();
	}
	
	public List<RewardItem.Builder> getGoodsList() {
		return goodsList;
	}	
}
