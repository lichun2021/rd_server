package com.hawk.game.config;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;

import com.hawk.game.item.ItemInfo;

/**
 * 装备材料配置
 *
 * @author Jesse
 *
 */
@HawkConfigManager.XmlResource(file = "xml/equipment_material.xml")
public class EquipMaterialCfg extends HawkConfigBase {
	@Id
	protected final int id;

	/** 材料品质 */
	protected final int quality;

	/** 合成消耗 */
	protected final String compoundNeed;

	/** 分解产出*/
	protected final String resolveGain;
	
	/**
	 * 材料合成消耗列表
	 */
	private List<ItemInfo> consumeList;
	
	/**
	 * 材料分解产出列表
	 */
	private List<ItemInfo> resolveList;

	public EquipMaterialCfg() {
		id = 0;
		quality = 0;
		compoundNeed = "";
		resolveGain = "";
	}

	public int getId() {
		return id;
	}

	public int getQuality() {
		return quality;
	}

	public String getCompoundNeed() {
		return compoundNeed;
	}
	
	public String getResolveGain() {
		return resolveGain;
	}

	public List<ItemInfo> getConsumeList() {
		List<ItemInfo> copy = new ArrayList<>();
		consumeList.forEach(e->copy.add(e.clone()));
		return copy;
	}
	
	public List<ItemInfo> getResolveList() {
		List<ItemInfo> copy = new ArrayList<>();
		resolveList.forEach(e->copy.add(e.clone()));
		return copy;
	}

	@Override
	protected boolean assemble() {
		consumeList =  ItemInfo.valueListOf(compoundNeed);
		resolveList =  ItemInfo.valueListOf(resolveGain);
		return super.assemble();
	}

	@Override
	protected boolean checkValid() {
		for (ItemInfo itemInfo : consumeList) {
			//校验物品信息是否正确
			ItemCfg itemCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, itemInfo.getItemId());
	        if (itemCfg == null) {
	        	HawkLog.errPrintln("it is not have this compoundNeed, itemId : {}", itemInfo);
	            return false;
	        }
		}
		resolveList =  ItemInfo.valueListOf(resolveGain);
		for (ItemInfo itemInfo : resolveList) {
			//校验物品信息是否正确
			ItemCfg itemCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, itemInfo.getItemId());
	        if (itemCfg == null) {
	        	HawkLog.errPrintln("it is not have this resolveGain, itemId : {}", itemInfo);
	            return false;
	        }
		}
		return true;
	}
}
