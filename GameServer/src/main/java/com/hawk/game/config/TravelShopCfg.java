package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.manager.AssembleDataManager;
import com.hawk.game.protocol.Const.BuildingType;


@HawkConfigManager.XmlResource(file = "xml/travel_shop.xml")
public class TravelShopCfg extends HawkConfigBase{
	
	/**
	 * 配置唯一ID
	 */
	@Id
	protected final int id;
	/**
	 * 等级最小
	 */
	protected final int lvMin;
	/**
	 * 等级最大
	 */
	protected final int lvMax;
	/**
	 * 权重
	 */
	protected final int rate;
	/**
	 * 组
	 */
	protected final int shopPool;
	/**
	 * 物品ID
	 */
	protected final int itemId;
	/**
	 * 数量
	 */
	protected final int num;
	/**
	 * 价格
	 */
	protected final String price;
	/**
	 * 价格.解析后的格式
	 */
	private ItemInfo itemPrice;
	/**
	 * 折扣价格
	 */
	protected final int discount;	
	/**
	 * 购买类型 1金币 2资源
	 */
	protected final int costType;
	/**
	 * 单次购买给rate提升比例，万分比
	 */
	private final int rise;
	/**
	 * 最大提升，万分比
	 */
	private final int riseMax;
	
	public TravelShopCfg(){
		id = 0;
		lvMax = 0;
		lvMin = 0;
		rate = 0;
		itemId = 0;
		price = "";
		discount = 0;
		shopPool = 0;
		num = 0;
		costType = 0;
		rise = 0;
		riseMax = 0;
	}
	
	public int getId() {
		return id;
	}
	public int getLvMin() {
		return lvMin;
	}
	public int getLvMax() {
		return lvMax;
	}
	public int getRate() {
		return rate;
	}
	public int getItemId() {
		return itemId;
	}
	
	public String getPrice() {
		return price;
	}
			
	public ItemInfo getItemPrice() {
		return itemPrice;
	}

	public void setItemPrice(ItemInfo itemPrice) {
		this.itemPrice = itemPrice;
	}

	public int getShopPool() {
		return shopPool;
	}
	
	public int getRise() {
		return rise;
	}

	public int getRiseMax() {
		return riseMax;
	}

	@Override
	public boolean assemble(){
		itemPrice = ItemInfo.valueOf(price);
		return true;
	}
	
	@Override
	public boolean checkValid(){
		if(itemPrice == null){
			return false;
		}
		
		RewardHelper.checkRewardItem(itemPrice.getType(), itemPrice.getItemId(), itemPrice.getCount());	
		if (!AssembleDataManager.getInstance().isValidBuildingLevel(BuildingType.CONSTRUCTION_FACTORY, lvMin)){
			return false;
		}
		if (!AssembleDataManager.getInstance().isValidBuildingLevel(BuildingType.CONSTRUCTION_FACTORY, lvMax)) {
			return false;
		}
		return true;
	}

	public int getDiscount() {
		return discount;
	}

	public int getNum() {
		return num;
	}

	public int getCostType() {
		return costType;
	}
}
