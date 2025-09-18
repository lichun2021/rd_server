package com.hawk.game.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.os.HawkOSOperator;

import com.hawk.game.cfgElement.EffectObject;
import com.hawk.game.item.ItemInfo;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 科技功能配置
 * 
 * @author shadow
 *
 */
@HawkConfigManager.XmlResource(file = "xml/tech_level.xml")
public class TechnologyCfg extends HawkConfigBase {
	@Id
	protected final int id;
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
	 * 科技研究科研石消耗
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
	 * 解锁条件-前置建筑
	 */
	protected final String frontBuild;
	/**
	 * 解锁条件-VIP等级
	 */
	protected final int frontVip;
	/**
	 * 科技作用号
	 */
	protected final String techEffect;
	/**
	 * 科技技能
	 */
	protected final int techSkill;
	/**
	 * 科技战力
	 */
	protected final int battlePoint;
	/**
	 * 科技类型 
	 */
	protected final int techType;
	/**
	 * 战力计算属性加成
	 */
	protected final String atkAttr;
	
	/**
	 * 战力计算属性加成
	 */
	protected final String hpAttr;
	
	/**
	 * 科技作用号属性
	 */
	private List<EffectObject> effectList;
	/**
	 * 科研石资源列表 
	 */
	private List<ItemInfo> itemList;
	/**
	 * 资源消耗列表
	 */
	private List<ItemInfo> costList;
	/**
	 * 前置解锁科技列表
	 */
	private List<Integer> conditionTechList;
	/**
	 * 前置解锁建筑列表
	 */
	private List<Integer> conditionBuildList;
	
	private static Map<Integer, Integer> techIdLevelMaxMap = new HashMap<Integer, Integer>(); 

	public TechnologyCfg() {
		this.id = 0;
		this.techLevel = 0;
		this.techId = 0;
		this.techEffect = "";
		this.techTime = 0;
		this.techItem = "";
		this.techCost = "";
		this.frontTech = "";
		this.frontBuild = "";
		this.frontVip = 0;
		this.battlePoint = 0;
		this.techSkill = 0;
		this.techType = 0;
		atkAttr = "";
		hpAttr = "";
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
		return techTime;
	}
	
	/**
	 * @return 解锁技能
	 */
	public int getTechSkill() {
		return techSkill;
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
	public List<Integer> getConditionTechList() {
		return Collections.unmodifiableList(conditionTechList);
	}

	/**
	 * @return 升级所需科技建筑等级
	 */
	public List<Integer> getConditionBuildList() {
		return Collections.unmodifiableList(conditionBuildList);
	}
	
	/**
	 * @return 升级所需VIP等级
	 */
	public int getFrontVip() {
		return frontVip;
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
		for (Integer techId : conditionTechList) {
			TechnologyCfg cfg = HawkConfigManager.getInstance().getConfigByKey(TechnologyCfg.class, techId);
			if (cfg == null) {
				return false;
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

		conditionTechList = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(frontTech)) {
			String[] array = frontTech.split(";");
			for (String val : array) {
				conditionTechList.add(Integer.parseInt(val));
			}
		}

		conditionBuildList = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(frontBuild)) {
			String[] array = frontBuild.split("_");
			for (String val : array) {
				conditionBuildList.add(Integer.parseInt(val));
			}
		}
		
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

	private static Map<Integer, Map<Integer, TechnologyCfg>> levelMap = new ConcurrentHashMap<>();

	private static Map<Integer, Long> levelPowerMap = new ConcurrentHashMap<>();

	private static Map<Integer, Map<Integer, Integer>> levelTypeMap = new ConcurrentHashMap<>();

	private static Map<Integer, Integer> typeMaxMap = new ConcurrentHashMap<>();


	public static boolean doAssemble() {
		int MAX_LEVEL = 45;
		Map<Integer, Map<Integer, TechnologyCfg>> tmpLevelMap = new ConcurrentHashMap<>();
		Map<Integer, TechnologyCfg> techIdMap = new ConcurrentHashMap<>();
		ConfigIterator<TechnologyCfg> cfgs = HawkConfigManager.getInstance().getConfigIterator(TechnologyCfg.class);
		for(TechnologyCfg cfg : cfgs){
			int level = getCfgLevel(cfg);
			for(int i = MAX_LEVEL; i >= level; i--){
				if(!tmpLevelMap.containsKey(level)){
					tmpLevelMap.put(i, new ConcurrentHashMap<>());
				}
				Map<Integer, TechnologyCfg> techMap = tmpLevelMap.get(i);
				if(techMap.containsKey(cfg.getTechId())){
					TechnologyCfg curCfg = techMap.get(cfg.getTechId());
					if(curCfg.getLevel() < cfg.getLevel()){
						techMap.put(cfg.getTechId(), cfg);
					}
				}else {
					techMap.put(cfg.getTechId(), cfg);
				}
			}
			TechnologyCfg curCfg = techIdMap.get(cfg.getTechId());
			if(curCfg == null){
				techIdMap.put(cfg.getTechId(), cfg);
			}else {
				if(cfg.getLevel() > curCfg.getLevel()){
					techIdMap.put(cfg.getTechId(), cfg);
				}
			}
		}

		Map<Integer, Long> tmpLevelPowerMap = new ConcurrentHashMap<>();
		Map<Integer, Map<Integer, Integer>> tmpLevelTypeMap = new ConcurrentHashMap<>();
		for(int level : tmpLevelMap.keySet()){
			Map<Integer, TechnologyCfg> techMap = tmpLevelMap.get(level);
			long power = 0L;
			Map<Integer, Integer> typeMap = new ConcurrentHashMap<>();
			for(TechnologyCfg cfg : techMap.values()){
				power += cfg.getBattlePoint();
				int cur = typeMap.getOrDefault(cfg.getTechType(), 0);
				cur += cfg.getLevel();
				typeMap.put(cfg.getTechType(), cur);
			}
			tmpLevelPowerMap.put(level, power);
			tmpLevelTypeMap.put(level, typeMap);
		}
		Map<Integer, Integer> tmpTypeMaxMap = new ConcurrentHashMap<>();
		for(TechnologyCfg cfg : techIdMap.values()){
			int cur = tmpTypeMaxMap.getOrDefault(cfg.getTechType(), 0);
			cur += cfg.getLevel();
			tmpTypeMaxMap.put(cfg.getTechType(), cur);
		}
		levelMap = tmpLevelMap;
		levelPowerMap = tmpLevelPowerMap;
		levelTypeMap = tmpLevelTypeMap;
		typeMaxMap = tmpTypeMaxMap;
		return true;
	}

	private static int getCfgLevel(TechnologyCfg cfg){
		int level = 0;
		for (int condition : cfg.getConditionBuildList()) {
			BuildingCfg buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, condition);
			if (buildingCfg == null) {
				continue;
			}
			if(level == 0){
				level = buildingCfg.getLevel();
			}else {
				level = Math.min(level, buildingCfg.getLevel());
			}
		}
		return level;
	}

	public static Map<Integer, TechnologyCfg> getTechMap(int level){
		return levelMap.getOrDefault(level, new ConcurrentHashMap<>());
	}

	public static long getPower(int level){
		return levelPowerMap.getOrDefault(level, 0L);
	}

	public static Map<Integer, Integer> getTypeMap(int level){
		return levelTypeMap.getOrDefault(level, new ConcurrentHashMap<>());
	}

	public static Map<Integer, Integer> getTypeMaxMap() {
		return typeMaxMap;
	}
}
