package com.hawk.game.cfgElement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.hawk.game.config.ArmourStarExploreNodeCfg;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.config.ArmourStarExploreCfg;
import com.hawk.game.config.ArmourStarExploreUpgradeCfg;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Armour.ArmourStarExploreAttrInfo;
import com.hawk.game.protocol.Armour.ArmourStarExploreInfo;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.util.GameUtil;
import com.hawk.log.Action;

public class ArmourStarExploreObj {
	
	/**
	 * 7个球,这个是球的ID
	 */
	private int starId;
	
	/**
	 * 进度
	 */
	private Map<Integer, Integer> progressMap;
	
	
	/**
	 * 构造方法(反序列化调用这个)
	 */
	private ArmourStarExploreObj() {};

	/**
	 * 构造方法(初始化调用这个)
	 * @param starId
	 */
	public ArmourStarExploreObj(int starId) {
		this.starId = starId;
		Map<Integer, Integer> progressMap = new HashMap<>();
		for (int progressId = 1; progressId <= ArmourStarExploreCfg.getStarAttrCount(); progressId++) {
			progressMap.put(progressId, 0);
		}
		this.progressMap = progressMap;
	}
	
	
	/**
	 * 序列化
	 * @param str
	 */
	public JSONObject serialize() {
		JSONObject obj = new JSONObject();
		obj.put("starId", starId);
		
		JSONArray progress = new JSONArray();
		for (Entry<Integer, Integer> info : progressMap.entrySet()) {
			JSONObject infoObj = new JSONObject();
			infoObj.put("protressId", info.getKey());
			infoObj.put("progressVal", info.getValue());
			progress.add(infoObj);
		}
		obj.put("progress", progress);
		return obj;
	}
	
	/**
	 * 反序列化
	 * @param dataArray
	 */
	public static ArmourStarExploreObj unSerialize(JSONObject json) {
		ArmourStarExploreObj obj = new ArmourStarExploreObj();
		obj.setStarId(json.getIntValue("starId"));
		
		Map<Integer, Integer> progressMap = new HashMap<>();
		JSONArray progress = json.getJSONArray("progress");
		for (int i = 0; i < progress.size(); i++) {
			JSONObject progressJson = progress.getJSONObject(i);
			int progressId = progressJson.getIntValue("protressId");
			int progressVal = progressJson.getIntValue("progressVal");
			progressMap.put(progressId, progressVal);
		}
		obj.setProgressMap(progressMap);
		return obj;
		
	}

	/**
	 * toPB
	 * @return
	 */
	public ArmourStarExploreInfo.Builder toPB() {
		ArmourStarExploreInfo.Builder builder = ArmourStarExploreInfo.newBuilder();
		builder.setStarId(starId);
		for (Entry<Integer, Integer> attrInfo : progressMap.entrySet()) {
			ArmourStarExploreAttrInfo.Builder attrBuilder = ArmourStarExploreAttrInfo.newBuilder();
			attrBuilder.setAttrIndex(attrInfo.getKey());
			attrBuilder.setAttrValue(attrInfo.getValue());
			builder.addAttrInfo(attrBuilder);
		}
		return builder;
	}
	
	public int getStarId() {
		return starId;
	}

	public Map<Integer, Integer> getProgressMap() {
		return progressMap;
	}

	public void setStarId(int id) {
		this.starId = id;
	}

	public void setProgressMap(Map<Integer, Integer> progressMap) {
		this.progressMap = progressMap;
	}
	
	/**
	 * 获取基础配置
	 * @return
	 */
	private ArmourStarExploreCfg getBaseCfg() {
		return HawkConfigManager.getInstance().getConfigByKey(ArmourStarExploreCfg.class, starId);
	}
	
	/**
	 * 获取当前等级升级配置
	 * @param currentLevel
	 * @return
	 */
	private ArmourStarExploreUpgradeCfg getLevelUpCfg(int currentLevel) {
		ConfigIterator<ArmourStarExploreUpgradeCfg> iter = HawkConfigManager.getInstance().getConfigIterator(ArmourStarExploreUpgradeCfg.class);
		while(iter.hasNext()) {
			ArmourStarExploreUpgradeCfg cfg = iter.next();
			if (cfg.getStarId() == starId && cfg.getLevel() == currentLevel + 1) {
				return cfg;
			}
		}
		return null;
	}
	
	/**
	 * 当前等级
	 * @return
	 */
	public int getCurrentLevel() {
		return progressMap.values().stream().mapToInt(Integer::intValue).sum();
	}
	
	/**
	 * 获取当前等级升级消耗
	 */
	private ConsumeItems getLevelOnceConsume(Player player) {
		// 当前等级
		int currentLevel = getCurrentLevel();
		// 等级配置
		ArmourStarExploreUpgradeCfg levelUpCfg = getLevelUpCfg(currentLevel);
		if (levelUpCfg == null) {
			return null;
		}
		List<ItemInfo> resItems = ItemInfo.valueListOf(levelUpCfg.getConsume());
		final int XINGNENG = 802005; //星能id
		GameUtil.reduceByEffect(resItems, XINGNENG, player.getEffect().getEffValArr(EffType.EFF_367818));
		ConsumeItems consume = ConsumeItems.valueOf();
		consume.addConsumeInfo(resItems);
		return consume;
	}
	
	/**
	 * 获取当前等级到满级消耗
	 */
	private ConsumeItems getLevelMaxConsume() {
		// 当前等级
		int currentLevel = getCurrentLevel();
		
		boolean hasConsume = false;
		ConsumeItems consume = ConsumeItems.valueOf();
		ConfigIterator<ArmourStarExploreUpgradeCfg> iter = HawkConfigManager.getInstance().getConfigIterator(ArmourStarExploreUpgradeCfg.class);
		while(iter.hasNext()) {
			ArmourStarExploreUpgradeCfg cfg = iter.next();
			if (cfg.getStarId() == starId && cfg.getLevel() > currentLevel) {
				consume.addConsumeInfo(ItemInfo.valueListOf(cfg.getConsume()));
				hasConsume = true;
			}
		}
		if (!hasConsume) {
			return null;
		}
		
		return consume;
	}
	
	/**
	 * 升级
	 * @param player
	 * @param once
	 */
	public void upLevel(Player player, int times) {
		times = Math.max(times, 1);
		ArmourStarExploreUpgradeCfg cfg = ArmourStarExploreUpgradeCfg.getLevelCfg(starId, getCurrentLevel() + 1);
		for (int i = 0; i < times && cfg != null; i++) {
			if (!upLevelOnce(player)) {
				return;
			}
			cfg = ArmourStarExploreUpgradeCfg.getLevelCfg(starId, getCurrentLevel() + 1);
		}
	}
	
	/**
	 * 升级一次
	 * @param player
	 */
	public boolean upLevelOnce(Player player) {
		// 消耗
		ConsumeItems consume = getLevelOnceConsume(player);
		if (consume == null || !consume.checkConsume(player)) {
			return false;
		}
		consume.consumeAndPush(player, Action.ARMOUR_STAR_EXPLORE_UP_ONCE);
				
		// 算下都哪些属性可以进入随机升级列表
		ArmourStarExploreCfg cfg = getBaseCfg();
		List<Integer> randStarIds = new ArrayList<>();
		for (Entry<Integer, Integer> progress : progressMap.entrySet()) {
			int attrId = progress.getKey();
			int currRate = progress.getValue();
			int maxRate = cfg.getRate(attrId);
			if (currRate < maxRate) {
				randStarIds.add(attrId);
			}
		}
		
		// 乱序后取第一个
		Collections.shuffle(randStarIds);
		int attrId = randStarIds.get(0);
		
		// 升级
		int beforeVal = progressMap.get(attrId);
		progressMap.put(attrId, beforeVal + 1);
		return true;
	}
	
	/**
	 * 升到满级
	 * @param player
	 */
	public void upLevelMax(Player player) {
		ArmourStarExploreUpgradeCfg cfg = ArmourStarExploreUpgradeCfg.getLevelCfg(starId, getCurrentLevel() + 1);
		while (cfg != null){
			if(!upLevelOnce(player)){
				return;
			}
			cfg = ArmourStarExploreUpgradeCfg.getLevelCfg(starId, getCurrentLevel() + 1);
		}

//		ArmourStarExploreCfg cfg = getBaseCfg();
//
//		// 检测消耗
//		ConsumeItems consume = getLevelMaxConsume();
//		if (consume == null) {
//			return;
//		}
//		if (!consume.checkConsume(player)) {
//			return;
//		}
//		// 消耗
//		consume.consumeAndPush(player, Action.ARMOUR_STAR_EXPLORE_UP_ALL);
//
//		// 都升到满级
//		progressMap.clear();
//		for (int progressId = 1; progressId <= ArmourStarExploreCfg.getStarAttrCount(); progressId++) {
//			int maxRate = cfg.getRate(progressId);
//			progressMap.put(progressId, maxRate);
//		}
	}

	public boolean isMax(){
		ArmourStarExploreCfg cfg = getBaseCfg();
		for (int progressId = 1; progressId <= ArmourStarExploreCfg.getStarAttrCount(); progressId++) {
			int maxRate = cfg.getRate(progressId);
			int value = progressMap.get(progressId);
			if(value < maxRate){
				return false;
			}
		}
		return true;
	}

	public Map<Integer, Integer> getEffectMap(){
		Map<Integer, Integer> effMap = new HashMap<>();
		ArmourStarExploreCfg cfg = getBaseCfg();
		int total = 0;
		for (int progressId = 1; progressId <= ArmourStarExploreCfg.getStarAttrCount(); progressId++) {
			int value = progressMap.get(progressId);
			total += value * 100;
			EffectObject effectObject = null;
			if (progressId == 1) {
				effectObject = cfg.getFirstEff();
			}
			if (progressId == 2) {
				effectObject = cfg.getSecondEff();
			}
			if (progressId == 3) {
				effectObject = cfg.getThirdEff();
			}
			if(effectObject != null){
				effMap.put(effectObject.getEffectType(), effectObject.getEffectValue() * value);
			}
		}
		mergeMap(effMap, getNodeEff(total));
		return effMap;
	}

	private void mergeMap(Map<Integer, Integer> mainMap, Map<Integer, Integer> slaveMap){
		for (Map.Entry<Integer, Integer> entry : slaveMap.entrySet()) {
			if(mainMap.containsKey(entry.getKey())){
				mainMap.put(entry.getKey(), mainMap.get(entry.getKey()) + entry.getValue());
			}else {
				mainMap.put(entry.getKey(), entry.getValue());
			}
		}
	}

	public Map<Integer, Integer> getNodeEff(int schedule){
		Map<Integer, Integer> effMap = new HashMap<>();
		ConfigIterator<ArmourStarExploreNodeCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(ArmourStarExploreNodeCfg.class);
		for(ArmourStarExploreNodeCfg cfg : iterator){
			if(cfg.getStarId() == starId && cfg.getExploreSchedule() <= schedule && cfg.getEff() != null){
				if(effMap.containsKey(cfg.getEff().getEffectType())){
					effMap.put(cfg.getEff().getEffectType(), effMap.get(cfg.getEff().getEffectType()) + cfg.getEff().getEffectValue());
				}else {
					effMap.put(cfg.getEff().getEffectType(), cfg.getEff().getEffectValue());
				}
			}
		}
		return effMap;
	}

	public int power(){
		ArmourStarExploreUpgradeCfg cfg = ArmourStarExploreUpgradeCfg.getLevelCfg(starId, getCurrentLevel());
		if (cfg == null) {
			return 0;
		}
		return cfg.getPower();
	}
}
