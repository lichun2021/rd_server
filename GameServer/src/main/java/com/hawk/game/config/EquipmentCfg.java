package com.hawk.game.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkOSOperator;

import com.hawk.game.item.ItemInfo;

/**
 * 装备配置
 *
 * @author Jesse
 *
 */
@HawkConfigManager.XmlResource(file = "xml/equipment.xml")
public class EquipmentCfg extends HawkConfigBase {
	
	@Id
	protected final int id;
	
	/** 装备模版id */
	protected final int mouldId;
	
	/** 装备位置 */
	protected final int pos;

	/** 装备品质 */
	protected final int quality;

	/** 装备等级限制 */
	protected final int level;

	/** 装备战力 */
	protected final int power;

	/** 装备属性 */
	protected final String attr;

	/** 装备打造道具消耗 */
	protected final String forgeMaterial;
	
	/** 装备打造耗时 */
	protected final int forgeTime;
	
	/** 装备分解道具返还 */
	protected final String resolveMaterial;
	
	/** 装备分解耗时 */
	protected final int resolveTime;
	
	/**
	 * 装备打造道具消耗列表
	 */
	private List<ItemInfo> forgeMaterialList;
	
	/**
	 * 装备分解道具返还列表
	 */
	private List<ItemInfo> resolveMaterialList;

	/**
	 * 装备属性
	 */
	private Map<Integer, Integer> attrMap;

	public EquipmentCfg() {
		id = 0;
		mouldId = 0;
		pos = 0;
		quality = 0;
		level = 0;
		power = 0;
		attr = "";
		forgeMaterial = "";
		forgeTime = 0;
		resolveMaterial = "";
		resolveTime = 0;
	}

	public int getId() {
		return id;
	}

	public int getPos() {
		return pos;
	}

	public int getMouldId() {
		return mouldId;
	}

	public int getQuality() {
		return quality;
	}

	public int getLevel() {
		return level;
	}

	public int getPower() {
		return power;
	}

	public String getAttr() {
		return attr;
	}

	public long getForgeTime() {
		return forgeTime * 1000l;
	}

	public long getResolveTime() {
		return resolveTime * 1000l;
	}
	
	public List<ItemInfo> getForgeMaterialList() {
		List<ItemInfo> copy = new ArrayList<>();
		forgeMaterialList.forEach(e->copy.add(e.clone()));
		return copy;
	}

	public List<ItemInfo> getResolveMaterialList() {
		List<ItemInfo> copy = new ArrayList<>();
		resolveMaterialList.forEach(e->copy.add(e.clone()));
		return copy;
	}

	public Map<Integer, Integer> getAttrMap() {
		Map<Integer, Integer> cloneMap = new HashMap<>();
		for (Entry<Integer, Integer> entry : attrMap.entrySet()) {
			cloneMap.put(entry.getKey(), entry.getValue());
		}
		return cloneMap;
	}
	
	public static boolean isCfgExist(int itemId) {
		return Objects.nonNull(HawkConfigManager.getInstance().getConfigByKey(EquipmentCfg.class, itemId));
	}

	@Override
	protected boolean assemble() {
		forgeMaterialList = ItemInfo.valueListOf(forgeMaterial);
		
		resolveMaterialList = ItemInfo.valueListOf(resolveMaterial);

		attrMap = new HashMap<>();
		if (!HawkOSOperator.isEmptyString(attr)) {
			String[] array = attr.split(",");
			for (String val : array) {
				String[] info = val.split("_");
				int attType = Integer.parseInt(info[0]);
				int attVal = Integer.parseInt(info[1]);
				if (attrMap.containsKey(attType)) {
					attrMap.put(attType, attrMap.get(attType) + attVal);
				} else {
					attrMap.put(attType, attVal);
				}
			}
		}
		return super.assemble();
	}
	
	@Override
	protected boolean checkValid() {
		for (ItemInfo itemInfo : forgeMaterialList) {
			//校验物品信息是否正确
			ItemCfg itemCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, itemInfo.getItemId());
	        if (itemCfg == null) {
	        	HawkLog.errPrintln("it is not have this forgeMaterial, itemId : {}", itemInfo);
	            return false;
	        }
		}
		
		for (ItemInfo itemInfo : resolveMaterialList) {
			//校验物品信息是否正确
			ItemCfg itemCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, itemInfo.getItemId());
	        if (itemCfg == null) {
	        	HawkLog.errPrintln("it is not have this resolveMaterial, itemId : {}", itemInfo);
	            return false;
	        }
		}
		return true;
	}
}
