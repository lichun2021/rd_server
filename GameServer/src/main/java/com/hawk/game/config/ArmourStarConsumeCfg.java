package com.hawk.game.config;

import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.game.item.ItemInfo;
import com.hawk.game.item.ItemInfoCollection;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 装备升星消耗配置
 * @author Golden
 *
 */
@HawkConfigManager.XmlResource(file = "xml/armour_star_consume.xml")
public class ArmourStarConsumeCfg extends HawkConfigBase  {
	
	/**
	 * 装备星级
	 */
	@Id
	protected final int equipmentStar;
	
	/**
	 * 升级到本级的消耗
	 */
	protected final String starConsumption;

	/**
	 * 星级对应的战力
	 */
	protected final int starPower;

	/**
	 * 解锁充能位
	 */
	protected final int unlockCharging;
	
	/**
	 * 分解获取
	 */
	protected final String resolve;
	
	protected final String atkAttr;
	protected final String hpAttr;
	
	/**
	 * 分解获取
	 */
	private ItemInfoCollection reolveItems;
	
	public ArmourStarConsumeCfg() {
		equipmentStar = 0;
		starConsumption = "";
		starPower = 0;
		unlockCharging = 0;
		resolve = "";
		atkAttr = "";
		hpAttr = "";
	}

	public int getEquipmentStar() {
		return equipmentStar;
	}

	/**
	 * 升级到此星级需要的消耗
	 * @return
	 */
	public List<ItemInfo> getConsume() {
		return ItemInfo.valueListOf(starConsumption);
	}

	public int getStarPower() {
		return starPower;
	}

	public int getUnlockCharging() {
		return unlockCharging;
	}
	
	public ItemInfoCollection getReolveItems() {
		return reolveItems;
	}

	public int getAtkAttr(int soldierType) {
		return SerializeHelper.cfgStr2Map(atkAttr).getOrDefault(soldierType, 0);
	}

	public int getHpAttr(int soldierType) {
		return SerializeHelper.cfgStr2Map(hpAttr).getOrDefault(soldierType, 0);
	}
	
	@Override
	protected boolean assemble() {
		reolveItems = ItemInfoCollection.valueOf(resolve);
		return true;
	}
}
