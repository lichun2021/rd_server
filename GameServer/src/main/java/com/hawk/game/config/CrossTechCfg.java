package com.hawk.game.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.hawk.game.cfgElement.EffectObject;
import com.hawk.game.item.ItemInfo;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 远征科技功能配置
 * 
 * @author Jesse
 *
 */
@HawkConfigManager.XmlResource(file = "xml/cross_tech_level.xml")
public class CrossTechCfg extends HawkConfigBase {
	@Id
	protected final int id;
	/**
	 * 科技类型 
	 */
	protected final int techType;
	/**
	 * 科技等级
	 */
	protected final int techLevel;
	/**
	 * 科技类型
	 */
	protected final int techId;
	/**
	 * 科技研究耗时
	 */
	protected final long techTime;
	/**
	 * 科技研究远征合金消耗
	 */
	protected final String techItem;
	/**
	 * 科技研究资源消耗
	 */
	protected final String techCost;
	/**
	 * 解锁条件-前置科技
	 */
	protected final String frontTech;
	/**
	 * 科技作用号
	 */
	protected final String techEffect;
	/**
	 * 科技战力
	 */
	protected final int battlePoint;
	/**
	 * 战力属性计算加成
	 */
	protected final String atkAttr;
	protected final String hpAttr;
	
	/**
	 * 科技作用号属性
	 */
	private List<EffectObject> effectList;
	/**
	 * 远征合金资源列表 
	 */
	private List<ItemInfo> itemList;
	
	/**
	 * 资源消耗列表
	 */
	private List<ItemInfo> costList;
	
	/**
	 * 前置解锁科技列表A<B<>>, B内条件为&&, A内条件为||
	 */
	private List<List<Integer>> conditionTechList;
	
	private static Map<Integer, Integer> techIdLevelMaxMap = new HashMap<Integer, Integer>(); 

	public CrossTechCfg() {
		this.id = 0;
		this.techLevel = 0;
		this.techId = 0;
		this.techEffect = "";
		this.techTime = 0;
		this.techItem = "";
		this.techCost = "";
		this.frontTech = "";
		this.battlePoint = 0;
		this.techType = 0;
		this.atkAttr = "";
		this.hpAttr = "";
	}
	
	/**
	 * @return 科技Id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @return 科技等级
	 */
	public int getLevel() {
		return techLevel;
	}
	
	/**
	 * @return 科技类型Id
	 */
	public int getTechId() {
		return techId;
	}

	/**
	 * @return 升级所需时间
	 */
	public long getLevelUpTime() {
		return techTime * 1000;
	}

	/**
	 * @return 效果列表
	 */
	public List<EffectObject> getEffectList() {
		return Collections.unmodifiableList(effectList);
	}

	/**
	 * @return 升级科研石消耗列表
	 */
	public List<ItemInfo> getItemList() {
		List<ItemInfo> listCopy = new ArrayList<>();
		for(ItemInfo info : itemList){
			listCopy.add(info.clone());
		}
		return listCopy;
	}

	/**
	 * @return 升级资源消耗列表
	 */
	public List<ItemInfo> getCostList() {
		List<ItemInfo> listCopy = new ArrayList<>();
		for(ItemInfo info : costList){
			listCopy.add(info.clone());
		}
		return listCopy;
	}

	/**
	 * @return 升级所需科技列表
	 */
	public List<List<Integer>> getConditionTechList() {
		return Collections.unmodifiableList(conditionTechList);
	}

	/**
	 * @return 科技包含的战斗力
	 */
	public int getBattlePoint() {
		return battlePoint;
	}
	
	/**
	 * 科技类型
	 * @return
	 */
	public int getTechType() {
		return techType;
	}

	@Override
	protected boolean checkValid() {
		for(List<Integer> techIds : conditionTechList){
			for (Integer techId : techIds) {
				CrossTechCfg cfg = HawkConfigManager.getInstance().getConfigByKey(CrossTechCfg.class, techId);
				if (cfg == null) {
					return false;
				}
			}
		}
		return true;
	}

	@Override
	protected boolean assemble() {

		effectList = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(techEffect)) {
			String[] array = techEffect.split(",");
			for (String val : array) {
				String[] info = val.split("_");
				EffectObject effect = new EffectObject(Integer.parseInt(info[0]), Integer.parseInt(info[1]));
				effectList.add(effect);
			}
		}
		
		itemList = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(techItem)) {
			String[] array = techItem.split(",");
			for (String val : array) {
				ItemInfo item = ItemInfo.valueOf(val);
				if (item != null) {
					itemList.add(item);
				}
			}
		}

		costList = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(techCost)) {
			String[] array = techCost.split(",");
			for (String val : array) {
				ItemInfo item = ItemInfo.valueOf(val);
				if (item != null) {
					costList.add(item);
				}
			}
		}

		List<List<Integer>> orList = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(frontTech)) {
			String[] arrays = frontTech.split("|");
			for (String arrayStr : arrays) {
				if (HawkOSOperator.isEmptyString(arrayStr)) {
					continue;
				}
				List<Integer> andList = new ArrayList<>();
				String[] array = frontTech.split(";");
				for (String val : array) {
					andList.add(Integer.parseInt(val));
				}
				orList.add(andList);
			}
		}
		conditionTechList = orList;

		
		if (!techIdLevelMaxMap.containsKey(techId) || techLevel > techIdLevelMaxMap.get(techId)) {
			techIdLevelMaxMap.put(techId, techLevel);
		}
		
		return true;
	}

	public static Map<Integer, Integer> getTechIdLevelMaxMap() {
		return techIdLevelMaxMap;
	}
	
	public int getAtkAttr(int soldierType) {
		return SerializeHelper.cfgStr2Map(atkAttr).getOrDefault(soldierType, 0);
	}

	public int getHpAttr(int soldierType) {
		return SerializeHelper.cfgStr2Map(hpAttr).getOrDefault(soldierType, 0);
	}
}
