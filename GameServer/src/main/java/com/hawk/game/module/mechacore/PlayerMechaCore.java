package com.hawk.game.module.mechacore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.hawk.collection.ConcurrentHashSet;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;
import org.hawk.thread.HawkThreadPool;
import org.hawk.uuid.HawkUUIDGenerator;
import com.google.common.collect.ImmutableMap;
import com.hawk.game.config.GachaCfg;
import com.hawk.game.config.ItemCfg;
import com.hawk.game.entity.PlayerGachaEntity;
import com.hawk.game.gacha.CheckAndConsumResult;
import com.hawk.game.gacha.GachaOprator;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisKey;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.module.mechacore.cfg.MechaCoreAddtionalPoolCfg;
import com.hawk.game.module.mechacore.cfg.MechaCoreConstCfg;
import com.hawk.game.module.mechacore.cfg.MechaCoreRankLevelCfg;
import com.hawk.game.module.mechacore.cfg.MechaCoreShowCfg;
import com.hawk.game.module.mechacore.cfg.MechaCoreTabCfg;
import com.hawk.game.module.mechacore.cfg.MechaCoreModuleAddtionalCfg;
import com.hawk.game.module.mechacore.cfg.MechaCoreModuleCfg;
import com.hawk.game.module.mechacore.cfg.MechaCoreModuleSlotCfg;
import com.hawk.game.module.mechacore.cfg.MechaCoreModuleSlotLimitCfg;
import com.hawk.game.module.mechacore.cfg.MechaCoreTechLevelCfg;
import com.hawk.game.module.mechacore.entity.MechaCoreEntity;
import com.hawk.game.module.mechacore.entity.MechaCoreModuleEffObject;
import com.hawk.game.module.mechacore.entity.MechaCoreModuleEntity;
import com.hawk.game.module.mechacore.entity.MechaCoreSlotObject;
import com.hawk.game.module.mechacore.entity.MechaCoreSuitObject;
import com.hawk.game.module.mechacore.obj.MechaAddProductInfo;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.MechaCore.MechaCoreLevelPB;
import com.hawk.game.protocol.MechaCore.MechaCoreInfoSync;
import com.hawk.game.protocol.MechaCore.MechaCoreModuleSync;
import com.hawk.game.protocol.MechaCore.MechaCoreSlotPB;
import com.hawk.game.protocol.MechaCore.MechaCoreSuit;
import com.hawk.game.protocol.MechaCore.MechaCoreSuitResp;
import com.hawk.game.protocol.MechaCore.MechaCoreSuitType;
import com.hawk.game.protocol.MechaCore.PBMechaCoreInfo;
import com.hawk.game.util.EffectParams;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.LogUtil;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.service.WorldPlayerService;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.log.Action;
import com.hawk.log.LogConst.LogInfoType;
import com.hawk.log.LogConst.PowerChangeReason;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.GachaType;
import com.hawk.game.protocol.Item.HPGachaReq;
import com.hawk.game.protocol.Item.HPGachaResp;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 机甲核心功能
 * @author lating
 */
public class PlayerMechaCore {
	/**
	 * db实体
	 */
	private MechaCoreEntity entity;
	/**
	 * 科技Map<类型,cfgId>
	 */
	private Map<Integer, Integer> techLevelCfgMap = new ConcurrentHashMap<>();
	/**
	 * 槽位信息（当前生效套装下的槽位）
	 */
	private Map<Integer, MechaCoreSlotObject> slotInfoMap = new ConcurrentHashMap<>();
	/**
	 * 套装信息
	 */
	private Map<Integer, MechaCoreSuitObject> suitInfoMap = new ConcurrentHashMap<>();
	
	/**
	 * 抽卡抽出的模块
	 */
	private List<MechaCoreModuleEntity> gachaModules = new ArrayList<>();
	
	/**
	 * 已解锁的外显
	 */
	private Set<Integer> unlockShowSet = new ConcurrentHashSet<>();
	
	/** 
	 * 作用号 
	 */
	private ImmutableMap<Integer, Integer> effValMap;
	/**
	 * 是否加载过作用号
	 */
	private boolean efvalLoad;
	/**
	 * 模块增产信息
	 */
	private MechaAddProductInfo addProductInfo;
	

	public static PlayerMechaCore create(MechaCoreEntity entity) {
		PlayerMechaCore mechaCore = new PlayerMechaCore(); 
		mechaCore.init(entity);
		entity.recordMechaCoreObj(mechaCore);
		return mechaCore;
	}
	
	private void init(MechaCoreEntity entity) {
		this.effValMap = ImmutableMap.of();
		this.entity = entity;
		try {
			//科技信息
			if (!HawkOSOperator.isEmptyString(entity.getTechInfo())) {
				Map<Integer, Integer> map = SerializeHelper.stringToMap(entity.getTechInfo(), Integer.class, Integer.class);
				techLevelCfgMap.putAll(map);
			} else {
				for(int type : MechaCoreTechLevelCfg.getTypeTechs()) {
					techLevelCfgMap.put(type, 0);
				}
				entity.notifyUpdate();
			}
			
			//槽位信息
			if (!HawkOSOperator.isEmptyString(entity.getSlotInfo())) {
				Map<Integer, MechaCoreSlotObject> map = SerializeHelper.stringToMap(entity.getSlotInfo(), Integer.class, MechaCoreSlotObject.class);
				slotInfoMap.putAll(map);
			} else {
				for(int type : MechaCoreModuleSlotCfg.getSlotTypes()) {
					MechaCoreSlotObject slot = MechaCoreSlotObject.valueOf(type);
					slotInfoMap.put(type, slot);
				}
				entity.notifyUpdate();
			}
			
			//套装信息
			if (!HawkOSOperator.isEmptyString(entity.getSuitInfo())) {
				Map<Integer, MechaCoreSuitObject> map = MechaCoreSuitObject.stringToMap(entity.getSuitInfo());
				suitInfoMap.putAll(map);
			} else {
				ConfigIterator<MechaCoreTabCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(MechaCoreTabCfg.class);
				while (iterator.hasNext()) {
					MechaCoreTabCfg cfg = iterator.next();
					if (HawkOSOperator.isEmptyString(cfg.getUnlockTabNeedItem())) {
						MechaCoreSuitObject obj = MechaCoreSuitObject.valueOf(cfg.getId());
						suitInfoMap.put(obj.getSuitId(), obj);
						entity.setSuitCount(entity.getSuitCount() + 1);
					}
				}
				entity.notifyUpdate();
			}
			
			//外显
			if (!HawkOSOperator.isEmptyString(entity.getUnlockedCityShow())) {
				Set<Integer> set = SerializeHelper.stringToSet(Integer.class, entity.getUnlockedCityShow(), ",");
				unlockShowSet.addAll(set);
			}
			
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 科技等级数据序列化
	 * @return
	 */
	public String serializeTechInfo() {
		return SerializeHelper.mapToString(techLevelCfgMap);
	}
	
	public String serializeSlotInfo() {
		return SerializeHelper.mapToString(slotInfoMap);
	}
	
	public String serializeSuitInfo() {
		return SerializeHelper.mapToString(suitInfoMap);
	}
	
	public String serializeUnlockedCityShow() {
		return SerializeHelper.collectionToString(unlockShowSet, ",");
	}
	
	public Set<Integer> getUnlockShowSet() {
		return unlockShowSet;
	}
	
	/**
	 * 加载作用号
	 */
	public void loadEffVal() {
		if (efvalLoad) {
			return;
		}
		efvalLoad = true;
		Map<Integer, Integer> effVals = loadEffValMap();
		effValMap = ImmutableMap.copyOf(effVals);
	}
	
	public boolean isEfvalLoad() {
		return efvalLoad;
	}

	public ImmutableMap<Integer, Integer> getEffValMap() {
		return effValMap;
	}
	
	public int getEffVal(int effId, EffectParams effParams) {
		int effVal = effValMap.getOrDefault(effId, 0);
		MechaCoreSuitType mechaSuit = effParams.getMechacoreSuit();
		if (mechaSuit != null && isSuitUnlocked(mechaSuit.getNumber())) {
			effVal += getSuitObj(mechaSuit.getNumber()).getEffVal(effId);
		} else {
			effVal += getSuitObj(getWorkSuit()).getEffVal(effId);
		}
		return effVal;
	}
	
	/**
	 * 变化通知
	 */
	public void notifyChange(PowerChangeReason reason) {
		Player player = getParent();
		Set<EffType> allEff = new HashSet<>();
		efvalLoad = false;
		loadEffVal();
		effValMap.keySet().forEach(e -> allEff.add(EffType.valueOf(e)));
		player.getEffect().syncEffect(player, allEff.toArray(new EffType[allEff.size()]));
		refreshPower(reason);
	}
	
	/**
	 * 通知变化
	 */
	public void refreshPower(PowerChangeReason reason) {
		if (reason != null) {
			getParent().refreshPowerElectric(reason);
		}
	}
	
	/**
	 * 加载作用号数值
	 * @return
	 */
	private Map<Integer, Integer> loadEffValMap() {
		//核心突破等级、核心科技等级、槽位等级、模块
		Map<Integer, Integer> map = new HashMap<>();
		
		//核心突破等级
		MechaCoreRankLevelCfg rankLvCfg = MechaCoreRankLevelCfg.getCfgByLevel(entity.getRankLevel());
		if (rankLvCfg != null) {
			map.putAll(rankLvCfg.getAttrMap());
			for(Entry<Integer, Integer> attrEntry : rankLvCfg.getSkillAttrMap().entrySet()) {
				map.merge(attrEntry.getKey(), attrEntry.getValue(), (v1, v2) -> v1 + v2);
			}
		}
		
		//核心科技等级
		for(Entry<Integer, Integer> entry : techLevelCfgMap.entrySet()) {
			MechaCoreTechLevelCfg cfg = HawkConfigManager.getInstance().getConfigByKey(MechaCoreTechLevelCfg.class, entry.getValue());
			if (cfg == null) {
				continue;
			}
			for(Entry<Integer, Integer> attrEntry : cfg.getAttrMap().entrySet()) {
				map.merge(attrEntry.getKey(), attrEntry.getValue(), (v1, v2) -> v1 + v2);
			}
		}
		
		Player player = getParent();
		for (MechaCoreSuitObject suit : suitInfoMap.values()) {
			suit.loadMoudleEffect(player);
		}
		
		return map;
	}
	
	/**
	 * 获取战力值
	 * @return
	 */
	public int getPower() {
		int power = getTechPower() + getModulePower();
		return power;
	}
	
	/**
	 * 获取科技战力
	 * @return
	 */
	public int getTechPower() {
		int power = 0;
		//机甲核心突破等级
		MechaCoreRankLevelCfg breakCfg = MechaCoreRankLevelCfg.getCfgByLevel(entity.getRankLevel());
		if (breakCfg != null) {
			power += breakCfg.getPower();
		}
		
		//科技等级
		for(Entry<Integer, Integer> entry : techLevelCfgMap.entrySet()) {
			MechaCoreTechLevelCfg cfg = HawkConfigManager.getInstance().getConfigByKey(MechaCoreTechLevelCfg.class, entry.getValue());
			if (cfg != null) {
				power += cfg.getPower();
			}
		}
		
		return power;
	}
	
	/**
	 * 获取模块战力
	 * @return
	 */
	public int getModulePower() {
		double modulePower = 0D;
		Player player = getParent();
		//装载的模块信息
		for (MechaCoreSlotObject slot : slotInfoMap.values()) {
			if (HawkOSOperator.isEmptyString(slot.getModuleUuid())) {
				continue;
			}
			
			MechaCoreModuleSlotCfg slotCfg = HawkConfigManager.getInstance().getConfigByKey(MechaCoreModuleSlotCfg.class, slot.getSlotId());
			MechaCoreModuleEntity moduleEntity = player.getMechaCoreModuleEntity(slot.getModuleUuid());
			if (moduleEntity == null) {
				continue;
			}
			
			MechaCoreModuleCfg moduleCfg = HawkConfigManager.getInstance().getConfigByKey(MechaCoreModuleCfg.class, moduleEntity.getCfgId());
			int factor = slotCfg.getQualityUpFactorMap().getOrDefault(moduleCfg.getModuleQuality(), 1);
			modulePower += moduleCfg.getPower() * 1D * factor;
			HawkLog.debugPrintln("playerMechacore getModulePower, playerId: {}, slot: {}, moduleId: {}, modulePower: {}, factor: {}, resultVal: {}", 
					entity.getPlayerId(), slot.getSlotId(), moduleEntity.getCfgId(), moduleCfg.getPower(), factor, modulePower);
			for (MechaCoreModuleEffObject attrObj : moduleEntity.getRandomAttrEff()) {
				MechaCoreModuleAddtionalCfg attrCfg = HawkConfigManager.getInstance().getConfigByKey(MechaCoreModuleAddtionalCfg.class, attrObj.getAttrId());
				modulePower += (attrCfg.getPower() * 1D * attrObj.getEffectValue()) / 1000;
				HawkLog.debugPrintln("playerMechacore getModulePower, playerId: {}, slot: {}, moduleId: {}, attrPower: {}, attrVal: {}, resultVal: {}", 
						entity.getPlayerId(), slot.getSlotId(), moduleEntity.getCfgId(), attrCfg.getPower(), attrObj.getEffectValue(), modulePower);
			}
		}
		
		return (int)Math.floor(modulePower); //【module表的power】 * 【slot表的upFactor字段中取对应模块品质的factor】 + 【addtional表的power】 * 【随机属性的属性值】
	}
	
	/**
	 * 获取单个模块的战力
	 * @param moduleEntity
	 * @param slotCfg
	 * @return
	 */
	public float getModulePower(MechaCoreModuleEntity moduleEntity, MechaCoreModuleSlotCfg slotCfg) {
		MechaCoreModuleCfg moduleCfg = HawkConfigManager.getInstance().getConfigByKey(MechaCoreModuleCfg.class, moduleEntity.getCfgId());
		float modulePower = moduleCfg.getPower() * 1.0f;
		if (slotCfg != null) {
			int factor = slotCfg.getQualityUpFactorMap().getOrDefault(moduleCfg.getModuleQuality(), 1);
			modulePower = modulePower * factor;
		}
		for (MechaCoreModuleEffObject attrObj : moduleEntity.getRandomAttrEff()) {
			MechaCoreModuleAddtionalCfg attrCfg = HawkConfigManager.getInstance().getConfigByKey(MechaCoreModuleAddtionalCfg.class, attrObj.getAttrId());
			modulePower += (attrCfg.getPower() * 1.0f * attrObj.getEffectValue()) / 1000;
		}
		return modulePower;
	} 
	
	public MechaCoreEntity getEntity() {
		return entity;
	}
	
	public int getRankLevel() {
		return entity.getRankLevel();
	}
	
	protected void updateRankLevel(int level) {
		entity.setRankLevel(level);
	}
	
	public int getWorkSuit() {
		return entity.getWorkSuit();
	}
	
	protected void updateWorkSuit(int workSuit) {
		entity.setWorkSuit(workSuit);
	}
	
	public int getUnlockedSuitCount() {
		return entity.getSuitCount();
	}
	
	public Player getParent() {
		return GlobalData.getInstance().makesurePlayer(entity.getPlayerId());
	}
	
	public Map<Integer, Integer> getTechLevelCfgMap() {
		return techLevelCfgMap; 
	}
	
	/**
	 * 获取科技等级
	 * @param type
	 * @return
	 */
	public int getTechLevel(int type) {
		int cfgId = techLevelCfgMap.get(type);
		MechaCoreTechLevelCfg cfg = HawkConfigManager.getInstance().getConfigByKey(MechaCoreTechLevelCfg.class, cfgId);
		return cfg == null ? 0 : cfg.getCoreLevels();
	}
	
	/**
	 * 判断套装是否已解锁出来了
	 * @param suit
	 * @return
	 */
	public MechaCoreSuitObject getSuitObj(int suit) {
		return suitInfoMap.get(suit);
	}
	
	public boolean isSuitUnlocked(int suit) {
		return getSuitObj(suit) != null;
	}
	
	/**
	 * 判断槽位是否已解锁
	 * @param slotType
	 * @return
	 */
	public boolean checkSlotUnlocked(int slotType) {
		MechaCoreSlotObject slotObj = getSlotObj(slotType);
		return slotAutoUnlock(slotObj) > 0;
	}
	
	/**
	 * 获取槽位信息对象
	 * @param slotType
	 * @return
	 */
	public MechaCoreSlotObject getSlotObj(int slotType) {
		return slotInfoMap.get(slotType);
	}
	
	/**
	 * 获取当前等级的槽位信息配置
	 * @param slotType
	 * @return
	 */
	public MechaCoreModuleSlotCfg getSlotCfg(int slotType) {
		MechaCoreSlotObject slotObj = getSlotObj(slotType);
		return MechaCoreModuleSlotCfg.getConfig(slotType, slotObj.getLevel());
	}
	
	/**
	 * 获取槽位中已经装载的模块
	 * @param slotType
	 * @return
	 */
	public MechaCoreModuleEntity getLoadedModule(int suit, int slotType) {
		MechaCoreSuitObject suitObj = getSuitObj(suit);
		if (suitObj == null) {
			return null;
		}
		String moduleId = suitObj.getSlotModuleInfo().get(slotType);
		if (HawkOSOperator.isEmptyString(moduleId)) {
			return null;
		}
		return getParent().getMechaCoreModuleEntity(moduleId);
	}
	
	/**
	 * 科技升级
	 * @param type 科技类型
	 * @return
	 */
	public int techLevelUp(int techType, int protocol) {
		Player player = getParent();
		int oldLevel = getTechLevel(techType);
		int newLevel = oldLevel + 1;
		MechaCoreTechLevelCfg config = MechaCoreTechLevelCfg.getCfgByLevel(techType, newLevel);
		if (config == null) {
			HawkLog.errPrintln("mechaCore tech levelup failed, config not exist, playerId: {}, techType: {}, newLevel: {}", player.getId(), techType, newLevel);
			return Status.Error.MECHA_CORE_TECH_MAXLV_VALUE;
		}
		
		//核心突破等级判断
		if (getRankLevel() < config.getRankLevel()) {
			HawkLog.errPrintln("mechaCore tech levelup failed, rankLevel error, playerId: {}, techType: {}, newLevel: {}", player.getId(), techType, newLevel);
			return Status.Error.MECHA_CORE_RANKLV_ERROR_VALUE;
		}
		
		ConsumeItems consumeItems = ConsumeItems.valueOf();
		consumeItems.addConsumeInfo(ItemInfo.valueListOf(config.getNeedItem()));
		if (!consumeItems.checkConsume(player, protocol)) {
			HawkLog.errPrintln("mechaCore tech levelup failed, consume break, playerId: {}, techType: {}, newLevel: {}", player.getId(), techType, newLevel);
			return -1;
		}
		
		consumeItems.consumeAndPush(player, Action.MECHA_CORE_TECH_LEVEL);
		techLevelCfgMap.put(techType, config.getId());
		entity.notifyUpdate();
		this.logMechcoreTechFlow(player, 0, techType, oldLevel, newLevel);
		HawkLog.logPrintln("mechaCore tech levelup success, playerId: {}, techType: {}, techLevel: {} -> {}", player.getId(), techType, oldLevel, newLevel);
		return 0;
	}
	
	/**
	 * 核心突破
	 * @return
	 */
	public int breakthrough(int protocol) {
		Player player = getParent();
		int oldLevel = getRankLevel();
		int newLevel = oldLevel + 1;
		MechaCoreRankLevelCfg config = MechaCoreRankLevelCfg.getCfgByLevel(newLevel);
		if (config == null) {
			HawkLog.errPrintln("mechaCore breakthrough failed, breakthrough config not exist, playerId: {}, newLevel: {}", player.getId(), newLevel);
			return Status.Error.MECHA_CORE_RANKLV_MAX_VALUE;
		}
		
		//前置科技等级判断
		for(int techType : MechaCoreTechLevelCfg.getTypeTechs()) {
			if (getTechLevel(techType) >= config.getTechLevelLimit()) {
				continue;
			}
			HawkLog.errPrintln("mechaCore breakthrough failed, tech level error, playerId: {}, newLevel: {}, techType: {}, level now: {}", player.getId(), newLevel, techType, getTechLevel(techType));
			return Status.Error.MECHA_CORE_PRE_TECH_ERROR_VALUE;
		}
		
		ConsumeItems consumeItems = ConsumeItems.valueOf();
		consumeItems.addConsumeInfo(ItemInfo.valueListOf(config.getNeedItem()));
		if (!consumeItems.checkConsume(player, protocol)) {
			HawkLog.errPrintln("mechaCore breakthrough failed, consume break, playerId: {}, newStage: {}", player.getId(), newLevel);
			return -1;
		}

		consumeItems.consumeAndPush(player, Action.MECHA_CORE_BREAKTHROUGH);
		updateRankLevel(newLevel);
		
		boolean update = false;
		ConfigIterator<MechaCoreShowCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(MechaCoreShowCfg.class);
		while (iterator.hasNext()) {
			MechaCoreShowCfg showCfg = iterator.next();
			if (unlockShowSet.contains(showCfg.getId())) {
				continue;
			}
			if (showCfg.getRankLevelLimit() > 0 && showCfg.getRankLevelLimit() <= newLevel) {
				update = true;
				unlockShowSet.add(showCfg.getId());
				HawkLog.logPrintln("mechaCore unlock show breakthrough, playerId: {}, showId: {}, unlocked: {}", player.getId(), showCfg.getId(), unlockShowSet);
			}
		}
		if (update) {
			entity.notifyUpdate();
			syncWorldPoint();
		}
		
		this.logMechcoreTechFlow(player, 1, 0, oldLevel, newLevel);
		HawkLog.logPrintln("mechaCore breakthrough success, playerId: {}, rankLevel: {} -> {}", player.getId(), oldLevel, newLevel);
		return 0;
	}
	
	/**
	 * 槽位升级
	 * @param slotType
	 */
	public int slotLevelup(int slotType, int protocol) {
		if (!checkSlotUnlocked(slotType)) {
			HawkLog.logPrintln("mechaCore slot levelup failed, playerId: {}, need unlock before levelup, slotType: {}", entity.getPlayerId(), slotType);
			return Status.Error.MECHA_CORE_SLOT_LOCKED_VALUE;
		}
		
		MechaCoreModuleEntity moduleEntity = getLoadedModule(getWorkSuit(), slotType);
		if (moduleEntity == null) {
			return Status.Error.MECHA_CORE_SLOT_NOT_LOAD_VALUE; //未装载模块的清空下，不能对槽位进行升级
		}

		MechaCoreModuleSlotCfg slotCfg = getSlotCfg(slotType);
		MechaCoreModuleSlotCfg newSlotCfg = MechaCoreModuleSlotCfg.getConfig(slotType, slotCfg.getSlotLevel() + 1);
		if (newSlotCfg == null) {
			HawkLog.logPrintln("mechaCore slot levelup failed, playerId: {}, config error slotType: {}", entity.getPlayerId(), slotType);
			return Status.Error.MECHA_CORE_SLOTLV_MAX_VALUE;
		}
		
		Player player = getParent();
		ConsumeItems consumeItems = ConsumeItems.valueOf();
		consumeItems.addConsumeInfo(ItemInfo.valueListOf(newSlotCfg.getNeedItem()));
		if (!consumeItems.checkConsume(player, protocol)) {
			HawkLog.errPrintln("mechaCore slot levelup failed, consume break, playerId: {}, slotType: {}, newLevel: {}", entity.getPlayerId(), slotType, newSlotCfg.getSlotLevel());
			return -1;
		}

		consumeItems.consumeAndPush(player, Action.MECHA_CORE_SLOT_LEVEL);
		MechaCoreSlotObject slotObj = getSlotObj(slotType);
		slotObj.setSlotId(newSlotCfg.getId());
		slotObj.setLevel(newSlotCfg.getSlotLevel());
		entity.notifyUpdate();
		this.logMechcoreSlotFlow(player, slotType, slotCfg.getSlotLevel(), newSlotCfg.getSlotLevel());
		HawkLog.logPrintln("mechaCore slot levelup success, playerId: {}, slotType: {}, level: {} -> {}", entity.getPlayerId(), slotType, slotCfg.getSlotLevel(), newSlotCfg.getSlotLevel());
		return 0;
	}
	
	/**
	 * 装载模块
	 * @param slotType
	 * @param moduleId
	 */
	public int loadModule(int suit, int slotType, String moduleId) {
		if (getSuitObj(suit) == null) {
			return Status.Error.MECHA_CORE_SUIT_LOCK_ERR_VALUE;
		}
		
		if (!checkSlotUnlocked(slotType)) {
			HawkLog.logPrintln("mechaCore load module failed, playerId: {}, need unlock slot before load, slotType: {}", entity.getPlayerId(), slotType);
			return Status.Error.MECHA_CORE_SLOT_LOCKED_VALUE;
		}
		
		MechaCoreModuleEntity newModuleEntity = getParent().getMechaCoreModuleEntity(moduleId);
		if (newModuleEntity == null) {
			return Status.Error.MECHA_CORE_MODULE_NOT_EXIST_VALUE;
		}
		
		MechaCoreModuleCfg moduleCfg = HawkConfigManager.getInstance().getConfigByKey(MechaCoreModuleCfg.class, newModuleEntity.getCfgId());
		if (moduleCfg.getModuleType() != slotType) {
			return Status.Error.MECHA_CORE_SLOT_MODULE_TYPE_ERR_VALUE;
		}
		
		MechaCoreModuleEntity moduleEntity = getLoadedModule(suit, slotType);
		//为空装载，不为空替换
		if (moduleEntity != null) {
			if (moduleEntity.getId().equals(moduleId)) {
				return Status.Error.MECHA_CORE_SLOT_LOADED_VALUE;
			}
			moduleEntity.removeSuit(suit);
		}
		
		newModuleEntity.addSuit(suit);
		slotModuleUpdate(suit, slotType, moduleId);
		entity.notifyUpdate();
		int oldCfgId = 0;
		String oldUuid = "";
		if (moduleEntity != null) {
			oldCfgId = moduleEntity.getCfgId();
			oldUuid = moduleEntity.getId();
			syncModuleInfo(moduleEntity);
		}
		syncModuleInfo(newModuleEntity);
		
		this.logModuleLoadFlow(getParent(), 0, newModuleEntity.getCfgId(), newModuleEntity.getId(), slotType, suit, oldCfgId, oldUuid);
		HawkLog.logPrintln("mechaCore module load success, playerId: {}, slotType: {}, moduleUuid: {}, cfgId: {}", entity.getPlayerId(), slotType, moduleId, newModuleEntity.getCfgId());
		
		//检测是否解锁新的外显
		unlockCityShow(suit, newModuleEntity);
		return 0;
	}
	
	/**
	 * 解锁外显
	 * @param suit
	 * @param moduleCfg
	 */
	private void unlockCityShow(int suit, MechaCoreModuleEntity moduleEntity, boolean moduleCheck) {
		try {
			int configSize = HawkConfigManager.getInstance().getConfigSize(MechaCoreShowCfg.class);
			if (unlockShowSet.size() >= configSize) {
				return;
			}
			
			List<MechaCoreModuleEntity> moduleList = new ArrayList<>();
			for (int slot : slotInfoMap.keySet()) {
				MechaCoreModuleEntity entity = getLoadedModule(suit, slot);
				if (entity != null) {
					moduleList.add(entity);
				}
			}
			
			if (moduleCheck) {
				unlockShowByModuleQuality(moduleList, moduleEntity.getQuality());
			}
			for (MechaCoreModuleEffObject moduleAttrObj : moduleEntity.getRandomAttrEff()) {
				unlockShowByModuleAttrQuality(moduleList, moduleAttrObj.getQuality());
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 解锁外显
	 * @param suit
	 * @param moduleEntity
	 */
	private void unlockCityShow(int suit, MechaCoreModuleEntity moduleEntity) {
		unlockCityShow(suit, moduleEntity, true);
	}
	
	/**
	 * 卸载模块
	 * @param slotType
	 */
	public int unloadModule(int suit, int slotType) {
		if (getSuitObj(suit) == null) {
			return Status.Error.MECHA_CORE_SUIT_LOCK_ERR_VALUE;
		}
		
		if (!checkSlotUnlocked(slotType)) {
			HawkLog.logPrintln("mechaCore module unload failed, playerId: {}, need unlock slot before load, slotType: {}", entity.getPlayerId(), slotType);
			return Status.Error.MECHA_CORE_SLOT_LOCKED_VALUE;
		}
		
		MechaCoreModuleEntity moduleEntity = getLoadedModule(suit, slotType);
		if (moduleEntity == null) {
			HawkLog.logPrintln("mechaCore module unload failed, slot empty, playerId: {}, slotType: {}", entity.getPlayerId(), slotType);
			return Status.Error.MECHA_CORE_SLOT_NOT_LOAD_VALUE;
		}
		
		moduleEntity.removeSuit(suit);
		slotModuleUpdate(suit, slotType, "");
		entity.notifyUpdate();
		syncModuleInfo(moduleEntity);
		this.logModuleLoadFlow(getParent(), 1, moduleEntity.getCfgId(), moduleEntity.getId(), slotType, suit, 0, "");
		HawkLog.logPrintln("mechaCore module unload success, playerId: {}, slotType: {}, moduleUuid: {}, cfgId: {}", entity.getPlayerId(), slotType, moduleEntity.getId(), moduleEntity.getCfgId());
		return 0;
	}
	
	/**
	 * 更新槽位中装载的模块信息
	 * @param suit
	 * @param slotType
	 * @param moduleId
	 */
	private void slotModuleUpdate(int suit, int slotType, String moduleId) {
		getSuitObj(suit).slotModuleUpdate(slotType, moduleId);
		if (suit == getWorkSuit()) {
			MechaCoreSlotObject slotObj = getSlotObj(slotType);
			slotObj.setModuleUuid(moduleId);
		}
	}
	
	/**
	 * 登录修复数据
	 */
	public void loginFix() {
		try {
			ConfigIterator<MechaCoreTabCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(MechaCoreTabCfg.class);
			while (iterator.hasNext()) {
				MechaCoreTabCfg cfg = iterator.next();
				if (HawkOSOperator.isEmptyString(cfg.getUnlockTabNeedItem()) && !suitInfoMap.containsKey(cfg.getId())) {
					MechaCoreSuitObject obj = MechaCoreSuitObject.valueOf(cfg.getId());
					suitInfoMap.put(obj.getSuitId(), obj);
					entity.setSuitCount(entity.getSuitCount() + 1);
					entity.notifyUpdate();
				}
			}
			
			int workSuit = getWorkSuit();
			for(MechaCoreSlotObject slotObj : slotInfoMap.values()){
				getSuitObj(workSuit).slotModuleUpdate(slotObj.getSlotType(), slotObj.getModuleUuid());
			}
			entity.notifyUpdate();
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 模块属性词条替换
	 * @param fromModule
	 * @param toModule
	 * @param fromAttrId
	 * @param toAttrId
	 */
	public int replaceModuleAttr(String fromModuleId, String toModuleId, int fromAttrId, int toAttrId, int protocol) {
		Player player = getParent();
		MechaCoreModuleEntity fromModule = player.getMechaCoreModuleEntity(fromModuleId);
		MechaCoreModuleEntity toModule = player.getMechaCoreModuleEntity(toModuleId);
		if (fromModule == null || toModule == null) {
			HawkLog.errPrintln("mechaCore moduleAttr replace failed, playerId: {}, fromModule exist: {}, moduleId: {} {}", player.getId(), fromModule != null, fromModuleId, toModuleId);
			return Status.Error.MECHA_CORE_MODULE_NOT_EXIST_VALUE;
		}
		
		MechaCoreModuleCfg fromModuleCfg = HawkConfigManager.getInstance().getConfigByKey(MechaCoreModuleCfg.class, fromModule.getCfgId());
		MechaCoreModuleCfg toModuleCfg = HawkConfigManager.getInstance().getConfigByKey(MechaCoreModuleCfg.class, toModule.getCfgId());
		/** 交换属性需要同种类型的模块，不同类型的模块之间不可交换 */
		if (fromModuleCfg.getModuleType() != toModuleCfg.getModuleType()) {
			HawkLog.errPrintln("mechaCore moduleAttr replace failed, moduleType error, playerId: {}, module cfgId: {} {}", player.getId(), fromModule.getCfgId(), toModule.getCfgId());
			return Status.Error.MECHA_CORE_MODULE_TYPE_ERROR_VALUE;
		}
		
		MechaCoreModuleEffObject fromAttrObj = fromModule.getRandomAttr(fromAttrId);
		MechaCoreModuleEffObject toAttrObj = toModule.getRandomAttr(toAttrId);
		if (fromAttrObj == null || (toAttrId > 0 && toAttrObj == null)) {
			HawkLog.errPrintln("mechaCore moduleAttr replace failed, playerId: {}, moduleId: {} {}, attrId: {} {}", player.getId(), fromModuleId, toModuleId, fromAttrId, toAttrId);
			return Status.Error.MECHA_CORE_MODULE_ATTR_ERROR_VALUE;
		}
		
		MechaCoreAddtionalPoolCfg attrPoolCfg = HawkConfigManager.getInstance().getConfigByKey(MechaCoreAddtionalPoolCfg.class, toModuleCfg.getRandomAttrPool());
		if (toAttrObj == null && toModule.getRandomAttrEff().size() >= attrPoolCfg.getValue()) {
			HawkLog.errPrintln("mechaCore moduleAttr replace failed, playerId: {}, moduleId: {} {}, attrId: {} {}, attr size: {}", player.getId(), fromModuleId, toModuleId, fromAttrId, toAttrId, toModule.getRandomAttrEff().size());
			return Status.Error.MECHA_CORE_ATTR_ENOUGH_VALUE;
		}
		
		/** 交换同种属性词条如果属性低于目标属性不可交换 */
		if (toAttrObj != null && fromAttrObj.getEffectType() == toAttrObj.getEffectType() && fromAttrObj.getEffectValue() <= toAttrObj.getEffectValue()) {
			HawkLog.errPrintln("mechaCore moduleAttr replace failed, playerId: {}, moduleId: {} {}, attrId: {} {}, effType: {}, effVal: {} {}", player.getId(), fromModuleId, toModuleId, fromAttrId, 
					toAttrId, fromAttrObj.getEffectType(), fromAttrObj.getEffectValue(), toAttrObj.getEffectValue());
			return Status.Error.MECHA_CORE_MODULE_ATTR_ERROR1_VALUE;
		}
		
		/** 相同作用号的词条不能同时随机到2条及以上 */
		Optional<MechaCoreModuleEffObject> sameAttrObjOp = toModule.getRandomAttrEff().stream().filter(e -> e.getEffectType() == fromAttrObj.getEffectType()).findAny();
		if (sameAttrObjOp.isPresent() && (toAttrObj == null || sameAttrObjOp.get().getAttrId() != toAttrObj.getAttrId())) {
			HawkLog.errPrintln("mechaCore moduleAttr replace failed, playerId: {}, moduleId: {} {}, attrId: {} {}, same effType: {}", player.getId(), fromModuleId, toModuleId, fromAttrId, toAttrId, fromAttrObj.getEffectType());
			return Status.Error.MECHA_CORE_MODULE_ATTR_ERROR2_VALUE;
		}
		
		//消耗
		MechaCoreModuleAddtionalCfg fromAttrCfg = HawkConfigManager.getInstance().getConfigByKey(MechaCoreModuleAddtionalCfg.class, fromAttrId);
		ConsumeItems consumeItems = ConsumeItems.valueOf();
		consumeItems.addConsumeInfo(ItemInfo.valueListOf(fromAttrCfg.getInheritConsume()));
		if (!consumeItems.checkConsume(player, protocol)) {
			HawkLog.errPrintln("mechaCore moduleAttr replace failed, consume break, playerId: {}, toModuleId: {}, fromAttrId: {}", entity.getPlayerId(), toModuleId, fromAttrId);
			return -1;
		}
		
		consumeItems.consumeAndPush(player, Action.MECHA_CORE_REPLACE_ATTR);
		List<MechaCoreModuleEntity> moduleList = new ArrayList<>();
		moduleList.add(fromModule);
		moduleList.add(toModule);
		fromModule.removeRandomAttr(fromAttrObj); //从源模块上移除
		if (toAttrObj != null) {
			toModule.removeRandomAttr(toAttrObj); //从目标模块上移除
		}
		toModule.addRandomAttrEff(fromAttrObj); //将原模块上移除的属性，添加到目标模块上
		
		syncModuleInfo(moduleList);
		this.logModuleAttrReplace(player, fromModuleId, fromModuleCfg.getId(), toModuleId, toModuleCfg.getId(), fromAttrId, toAttrId);
		
		//当前预设下的模块属性传承，检测是否解锁新的外显
		List<Integer> suitList = toModule.getSuitList();
		if (suitList.contains(this.getWorkSuit())) {
			unlockCityShow(this.getWorkSuit(), toModule, false);
		}
		return 0;
	}
	
	/**
	 * 解锁机甲核心套装
	 * @param protocol
	 * @return
	 */
	public int suitUnlock(int protocol) {
		//当前解锁到第几套了
		int current = getUnlockedSuitCount();
		int suitMaxCount = HawkConfigManager.getInstance().getConfigSize(MechaCoreTabCfg.class);
		if (current >= suitMaxCount) {
			return Status.Error.MECHA_CORE_SUIT_UNLOCKED_ERR_VALUE;
		}

		Player player = getParent();
		MechaCoreTabCfg tabCfg = HawkConfigManager.getInstance().getConfigByKey(MechaCoreTabCfg.class, current + 1);
		List<ItemInfo> cost = ItemInfo.valueListOf(tabCfg.getUnlockTabNeedItem());
		ConsumeItems consum = ConsumeItems.valueOf();
		consum.addConsumeInfo(cost);
		if (!consum.checkConsume(player, protocol)) {
			return -1;
		}
		
		consum.consumeAndPush(player, Action.MECHA_CORE_SUIT_UNLOCK);
		entity.setSuitCount(current + 1);
		MechaCoreSuitObject obj = MechaCoreSuitObject.valueOf(entity.getSuitCount());
		suitInfoMap.put(obj.getSuitId(), obj);
		entity.notifyUpdate();
		this.logMechacoreSuitChange(getParent(), 0, 0, obj.getSuitId());
		HawkLog.logPrintln("mechaCore suitUnlock, playerId: {}, current: {}", entity.getPlayerId(), getUnlockedSuitCount());
		return 0;
	}
	
	/**
	 * 切换套装
	 * @return
	 */
	public int suitSwitch(int suit) {
		if (suit > getUnlockedSuitCount() || suit <= 0) {
			return Status.Error.MECHA_CORE_SUIT_LOCK_ERR_VALUE;
		}
		
		int workSuit = getWorkSuit();
		if (suit == workSuit) {
			HawkLog.logPrintln("mechaCore suitSwitch break, playerId: {}, workSuit: {}, switch to: {}", entity.getPlayerId(), workSuit, suit);
			return Status.Error.MECHA_CORE_SUIT_SWITCH_ERR_VALUE;
		}

		updateWorkSuit(suit);
		MechaCoreSuitObject suitObj = getSuitObj(suit);
		//所有套装下的槽位基本信息都是共享的，只是每个套装下各槽位中装入的模块可能不一样
		for (Entry<Integer, MechaCoreSlotObject> entry : slotInfoMap.entrySet()) {
			int slotType = entry.getKey();
			MechaCoreSlotObject slotObj = entry.getValue();
			String moduleUuid = suitObj.getSlotModuleInfo().getOrDefault(slotType, "");
			slotObj.setModuleUuid(moduleUuid);
		}
		
		this.logMechacoreSuitChange(getParent(), 1, workSuit, suit);
		HawkLog.logPrintln("mechaCore suitSwitch success, playerId: {}, old: {}, current: {}", entity.getPlayerId(), workSuit, getWorkSuit());
		
		//检测是否解锁新的外显
		for (Entry<Integer, MechaCoreSlotObject> entry : slotInfoMap.entrySet()) {
			MechaCoreModuleEntity entity = this.getLoadedModule(suit, entry.getKey());
			if (entity != null) {
				unlockCityShow(suit, entity);
			}
		}
		return 0;
	}
	
	/**
	 * 修改套装名字
	 * @param suitType
	 * @param name
	 */
	public void changeSuitName(int suitType, String name) {
		MechaCoreSuitObject suitObj = suitInfoMap.get(suitType);
		suitObj.setSuitName(name);
		entity.notifyUpdate();
		this.syncMechaCoreInfo(true);
		this.notifyChange(null);
		getParent().responseSuccess(HP.code2.MECHACORE_SUIT_CHANGENAME_C_VALUE);
	}
	
	/**
	 * 同步机甲核心信息
	 */
	public void syncMechaCoreInfo(boolean funcUnlocked) {
		funcUnlocked = funcUnlocked && MechaCoreConstCfg.getInstance().isMechaCoreOpen();
		MechaCoreInfoSync.Builder builder = MechaCoreInfoSync.newBuilder();
		builder.setFuncUnlocked(funcUnlocked ? 1 : 0);
		if (funcUnlocked) {
			this.genMechaCoreBuilder(builder, getWorkSuit());
		}
		
		GachaCfg gachaCfg = HawkConfigManager.getInstance().getConfigByKey(GachaCfg.class, GachaType.MODULE_ONE_VALUE);
		builder.setDrawFloorTimes(gachaCfg.getPseudoDropTimes());
		getParent().sendProtocol(HawkProtocol.valueOf(HP.code2.MECHACORE_INFO_SYNC, builder));
	}
	
	/**
	 * 同步所有模块信息
	 */
	public void syncAllModuleInfo() {
		Player player = getParent();
		MechaCoreModuleSync.Builder builder = MechaCoreModuleSync.newBuilder();
		builder.setAll(1);
		for (MechaCoreModuleEntity module : player.getData().getMechaCoreModuleEntityList()) {
			builder.addModule(module.toBuilder());
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.MECHACORE_MODULE_SYNC, builder));
	}
	
	/**
	 * 同步模块信息
	 */
	public void syncModuleInfo(MechaCoreModuleEntity module) {
		MechaCoreModuleSync.Builder builder = MechaCoreModuleSync.newBuilder();
		builder.setAll(0);
		builder.addModule(module.toBuilder());
		getParent().sendProtocol(HawkProtocol.valueOf(HP.code2.MECHACORE_MODULE_SYNC, builder));
	}
	
	/**
	 * 同步模块信息
	 */
	public void syncModuleInfo(List<MechaCoreModuleEntity> moduleList) {
		MechaCoreModuleSync.Builder builder = MechaCoreModuleSync.newBuilder();
		builder.setAll(0);
		for (MechaCoreModuleEntity module : moduleList) {
			builder.addModule(module.toBuilder());
		}
		getParent().sendProtocol(HawkProtocol.valueOf(HP.code2.MECHACORE_MODULE_SYNC, builder));
	}
	
	/**
	 * 核心模块抽取功能是否已解锁
	 * @return
	 */
	public boolean isModuleGachaUnlock() {
		if (!MechaCoreConstCfg.getInstance().isMechaCoreOpen()) {
			return false;
		}
		if (!MechaCoreConstCfg.getInstance().isMechaCoreDrawOpen()) {
			return false;
		}
		return entity.getRankLevel() >= MechaCoreConstCfg.getInstance().getDrawRankLimit();
	}
	
	/**
	 * 核心装配功能是否已解锁
	 * @return
	 */
	public boolean isModuleLoadUnlock() {
		if (!MechaCoreConstCfg.getInstance().isMechaCoreOpen()) {
			return false;
		}
		if (!MechaCoreConstCfg.getInstance().isMechaModuleOpen()) {
			return false;
		}
		return entity.getRankLevel() >= MechaCoreConstCfg.getInstance().getModuleLoadRankLimit();
	}
	
	/**
	 * 构建机甲核心信息
	 * @param builder
	 */
	public void genMechaCoreBuilder(MechaCoreInfoSync.Builder builder, int vsuit) {
		builder.setBreakthroughLv(entity.getRankLevel());
		builder.setModuleLoadUnlock(this.isModuleLoadUnlock() ? 1 : 0);
		builder.setModuleGachaUnlock(this.isModuleGachaUnlock() ? 1 : 0);
		for (Entry<Integer, Integer> entry : techLevelCfgMap.entrySet()) {
			MechaCoreLevelPB.Builder techBuilder = MechaCoreLevelPB.newBuilder();
			techBuilder.setType(entry.getKey());
			MechaCoreTechLevelCfg cfg = HawkConfigManager.getInstance().getConfigByKey(MechaCoreTechLevelCfg.class, entry.getValue());
			techBuilder.setLevel(cfg == null ? 0 : cfg.getCoreLevels());
			builder.addTechLvInfo(techBuilder);
		}
		
		for (MechaCoreSuitObject suit : suitInfoMap.values()) {
			MechaCoreSuit.Builder suitBuilder = suit.toBuilder();
			builder.addSuitInfo(suitBuilder);
		}
		
		builder.setCurrSuit(MechaCoreSuitType.valueOf(getWorkSuit()));
		if (vsuit == getWorkSuit()) {
			for (MechaCoreSlotObject slot : slotInfoMap.values()) {
				MechaCoreSlotPB.Builder slotBuilder = slot.toBuilder(this);
				builder.addSlotInfo(slotBuilder);
			}
		}
		builder.setMechacoreShowInfo(this.serializeUnlockedCityShow());
	}
	
	/**
	 * 构建套装详细信息
	 * @param builder
	 */
	public void genSuitDetailBuilder(MechaCoreSuitResp.Builder builder) {
		for (MechaCoreSuitObject suit : suitInfoMap.values()) {
			MechaCoreSuit.Builder suitBuilder = suit.toBuilder(this, true);
			builder.addSuitInfo(suitBuilder);
		}
	}
	
	/**
	 * 槽位自动解锁
	 * @param slotObj
	 */
	public int slotAutoUnlock(MechaCoreSlotObject slotObj) {
		if (slotObj.getUnlocked() > 0) {
			return slotObj.getUnlocked();
		}
		MechaCoreModuleSlotLimitCfg cfg = MechaCoreModuleSlotLimitCfg.getConfigByType(slotObj.getSlotType());
		if (getParent().getCityLevel() < cfg.getUnlockBaseLevelLimit()) {
			return 0;
		}
		
		if (getRankLevel() < cfg.getUnlockRankLimit()) {
			return 0;
		}
		
		int slotId = MechaCoreModuleSlotCfg.getInitId(slotObj.getSlotType());
		slotObj.setUnlocked(1);
		slotObj.setSlotId(slotId);
		entity.notifyUpdate();
		MechaCoreModuleSlotCfg slotCfg = HawkConfigManager.getInstance().getConfigByKey(MechaCoreModuleSlotCfg.class, slotId);
		this.logMechcoreSlotFlow(getParent(), slotObj.getSlotType(), 0, slotCfg.getSlotLevel());
		return slotObj.getUnlocked();
	}
	
	/**
	 * 构建邮件信息
	 * @return
	 */
	public PBMechaCoreInfo.Builder builderMailInfo(MechaCoreSuitType suit) {
		if (suit == null || suit == MechaCoreSuitType.MECHA_NONE) {
			suit = MechaCoreSuitType.valueOf(getWorkSuit());
		}
		
		PBMechaCoreInfo.Builder builder = PBMechaCoreInfo.newBuilder();
		builder.setBreakthroughLv(entity.getRankLevel());
		for (Entry<Integer, Integer> entry : techLevelCfgMap.entrySet()) {
			MechaCoreLevelPB.Builder techBuilder = MechaCoreLevelPB.newBuilder();
			techBuilder.setType(entry.getKey());
			MechaCoreTechLevelCfg cfg = HawkConfigManager.getInstance().getConfigByKey(MechaCoreTechLevelCfg.class, entry.getValue());
			techBuilder.setLevel(cfg == null ? 0 : cfg.getCoreLevels());
			builder.addTechLvInfo(techBuilder);
		}
		
		for (MechaCoreSuitObject suitObj : suitInfoMap.values()) {
			if (suitObj.getSuitId() != suit.getNumber()) {
				continue;
			}
			for (Entry<Integer, String> entry : suitObj.getSlotModuleInfo().entrySet()) {
				MechaCoreSlotObject slotObj = this.getSlotObj(entry.getKey());
				MechaCoreSlotPB.Builder slotBuilder = slotObj.toBuilder(this, entry.getValue());
				builder.addSlotInfo(slotBuilder);
			}
		}

		builder.setCurrSuit(suit);
		return builder;
	}
	
	/**
	 * 模块抽取开始
	 */
	public void gachaStart() {
		gachaModules.clear();
	}

	/**
	 * 模块抽取结束
	 * @return
	 */
	public List<String> gachaEnd() {
		if (!gachaModules.isEmpty()) {
			try {
				syncModuleInfo(gachaModules);
				List<String> uuidList = gachaModules.stream().map(e -> e.getId()).collect(Collectors.toList());
				gachaModules.clear();
				return uuidList;
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		return Collections.emptyList();
	}
	
	/**
	 * 机甲核心模块抽卡
	 * @param gachaType
	 * @param req
	 * @return
	 */
	public boolean gachaModule(Player player, GachaType gachaType, HPGachaReq req) {
		if(!this.isModuleGachaUnlock()) {
			player.sendError(HP.code.GACHA_C_VALUE, Status.Error.MECHA_CORE_FUNC_LOCK_VALUE, 0);
			return false;
		}
		GachaCfg gachaCfg = HawkConfigManager.getInstance().getConfigByKey(GachaCfg.class, gachaType.getNumber());
		PlayerGachaEntity gachaEntity = player.getData().getGachaEntityByType(gachaType);
		GachaOprator gachaOprator = GachaOprator.of(gachaType);
		boolean batchable = gachaOprator.getGachaCount() == GachaOprator.DEFAULT_BATCH;
		int batchGachaCnt = req.getBatchGachaCnt();
		if (batchGachaCnt > 0 && batchable) {
			if (gachaType == GachaType.MODULE_TEN) {
				batchGachaCnt = Math.min(batchGachaCnt, MechaCoreConstCfg.getInstance().getBatchDrawMax());
			}
			gachaOprator.setGachaCount(batchGachaCnt);
		}
		
		// 验证消耗
		CheckAndConsumResult checkAndConsumResult = gachaOprator.checkAndConsum(gachaCfg, gachaEntity, player);
		if (!checkAndConsumResult.isSuccess()) {
			return false;
		}
		
		List<MechaCoreModuleEntity> moduleList = player.getData().getMechaCoreModuleEntityList();
		//判断模块背包中模块数量是否达到上限
		if (moduleList.size() >= MechaCoreConstCfg.getInstance().getModuleMaxCount()) {
			player.sendError(HP.code.GACHA_C_VALUE, Status.Error.MECHA_CORE_MODULE_MAX_LIMIT_VALUE, 0);
			return false;
		}

		// 先给默认奖励
		List<ItemInfo> gachaAwardItems = new LinkedList<>();
		if (gachaCfg.getBuyItem().length() > 0) {
			ItemInfo buyItem = ItemInfo.valueOf(gachaCfg.getBuyItem());
			buyItem.setCount(gachaOprator.getGachaCount());
			gachaAwardItems.add(buyItem);
		}

		this.gachaStart();
		// 显示奖励
		List<String> rewardsShow = gachaOprator.gacha(gachaCfg, gachaEntity, player);
		rewardsShow.forEach(re -> gachaAwardItems.add(ItemInfo.valueOf(re)));
		player.getPush().syncGachaInfo();
		
		// 刷新任务
		//int gachaCount = gachaOprator.getGachaCount();
		//ActivityManager.getInstance().postEvent(new RandomHeroEvent(player.getId(), gachaCount, gachaType.getNumber()));
		//MissionManager.getInstance().postMsg(player, new EventGacha(gachaType.getNumber(), gachaCount));
		
		// 记录英雄抽卡打点日志
		LogUtil.logGacha(player, gachaType.getNumber(), checkAndConsumResult.getCost());
		LogUtil.logGachaItemsFlow(player, gachaType.getNumber(), gachaAwardItems);
		
		int resolveNum = 0;
		AwardItems resolveAward = AwardItems.valueOf();
		//多抽情况下处理模块的分解
		if (batchGachaCnt > 0 && gachaType == GachaType.MODULE_TEN && req.getArmourResolveQuality() > 0) {
			int resolveQuality = req.getArmourResolveQuality();
			List<ItemInfo> removeList =new ArrayList<>();
			for (ItemInfo item : gachaAwardItems) {
				if (item.getType() / GsConst.ITEM_TYPE_BASE != Const.ItemType.MECHA_CORE_MODULE_VALUE) {
					continue;
				}
				MechaCoreModuleCfg moduleCfg = HawkConfigManager.getInstance().getConfigByKey(MechaCoreModuleCfg.class, item.getItemId());
				if(moduleCfg.getModuleQuality() > resolveQuality){
					continue;
				}
				resolveAward.addItemInfos(ItemInfo.valueListOf(moduleCfg.getBreakDownGetItem()));
				removeList.add(item);
				resolveNum++;
			}
			
			gachaAwardItems.removeAll(removeList);
			gachaAwardItems.addAll(resolveAward.getAwardItems());
		}
		
		AwardItems awardItem = AwardItems.valueOf();
		awardItem.addItemInfos(gachaAwardItems);
		awardItem.rewardTakeAffectAndPush(player, Action.GACHA);

		List<MechaCoreModuleEntity> modules = new ArrayList<>();
		modules.addAll(gachaModules);
		HPGachaResp.Builder resp = HPGachaResp.newBuilder().setGachaType(gachaType.getNumber()).addAllRewards(rewardsShow).setBatchGachaCnt(batchGachaCnt);
		if (resolveNum > 0) {
			resp.setArmourResolveRewards(ItemInfo.toString(resolveAward.getAwardItems())).setArmourResolveNum(resolveNum).setArmourResolveQuality(req.getArmourResolveQuality());
		}
		List<String> mechaModuleUuidList = this.gachaEnd();
		if (!mechaModuleUuidList.isEmpty()) {
			resp.addAllModuleUuid(mechaModuleUuidList);
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code.GACHA_S, resp));
		this.logMechcoreModuleFlow(player, 0, modules);
		return true;
	}
	
	/**
	 * 添加模块
	 * @param poolId
	 */
	public void addModule(int moduleId) {
		MechaCoreModuleCfg moduleCfg = HawkConfigManager.getInstance().getConfigByKey(MechaCoreModuleCfg.class, moduleId);
		if (moduleCfg == null) {
			return;
		}

		try {
			MechaCoreModuleEntity moduleEntity = new MechaCoreModuleEntity();
			moduleEntity.setId(HawkUUIDGenerator.genUUID());
			moduleEntity.setPlayerId(entity.getPlayerId());
			moduleEntity.setCfgId(moduleCfg.getId());
			moduleEntity.setQuality(moduleCfg.getModuleQuality());
			
			List<MechaCoreModuleEffObject> randomAttrs = new ArrayList<>();
			List<Integer> randomList = new ArrayList<>(), qualityList = new ArrayList<>();
			MechaCoreAddtionalPoolCfg attrPoolCfg = HawkConfigManager.getInstance().getConfigByKey(MechaCoreAddtionalPoolCfg.class, moduleCfg.getRandomAttrPool());
			for (Map<Integer, Integer> qualityMap : attrPoolCfg.getAttrList()) {
				int quality = HawkRand.randomWeightObject(qualityMap);
				MechaCoreModuleAddtionalCfg cfg = MechaCoreModuleAddtionalCfg.randCfgByQuality(quality);
				if (cfg == null) {
					continue;
				}
				int i = 0;
				while (randomList.contains(cfg.getEffect()) && i < 10) {
					i++;
					cfg = MechaCoreModuleAddtionalCfg.randCfgByQuality(quality);
				}
				if (randomList.contains(cfg.getEffect())) {
					HawkLog.logPrintln("mechaCore addModule rand attr failed, playerId: {}, cfgId: {}, uuid: {}, quality: {}, attrCfgId: {}", entity.getPlayerId(), moduleId, moduleEntity.getId(), quality, cfg.getId());
					continue;
				}
				randomList.add(cfg.getEffect());
				randomAttrs.add(new MechaCoreModuleEffObject(cfg.getId(), cfg.getEffect(), HawkRand.randInt(cfg.getRandMin(), cfg.getRandMax()), cfg.getQuality()));
				qualityList.add(quality);
			}
			
			Collections.shuffle(randomAttrs);
			for (MechaCoreModuleEffObject attr : randomAttrs) {
				moduleEntity.addRandomAttrEff(attr);
			}
			
			moduleEntity.create(true);
			List<MechaCoreModuleEntity> moduleList = getParent().getData().getMechaCoreModuleEntityList(); 
			moduleList.add(moduleEntity);
			gachaModules.add(moduleEntity);
			
			HawkLog.logPrintln("mechaCore addModule, playerId: {}, cfgId: {}, uuid: {}, totalCount: {}", entity.getPlayerId(), moduleId, moduleEntity.getId(), moduleList.size());
		} catch (Exception e) {
			HawkLog.logPrintln("mechaCore addModule exception, playerId: {}, moduleCfgId: {}", entity.getPlayerId(), moduleId);
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 检测解锁新外显
	 * @param moduleList
	 * @param moduleQuality
	 */
	private boolean unlockShowByModuleQuality(List<MechaCoreModuleEntity> moduleList, int moduleQuality) {
		if (moduleQuality < MechaCoreShowCfg.getLowModuleQuality()) {
			return false;
		}
		
		try {
			int count = (int)moduleList.stream().filter(e -> e.getQuality() >= moduleQuality).count();
			boolean update = false;
			ConfigIterator<MechaCoreShowCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(MechaCoreShowCfg.class);
			while (iterator.hasNext()) {
				MechaCoreShowCfg showCfg = iterator.next();
				if (unlockShowSet.contains(showCfg.getId())) {
					continue;
				}
				if (showCfg.getModuleQuality() > 0 && showCfg.getModuleQuality() <= moduleQuality && count >= showCfg.getModuleCount()) {
					update = true;
					unlockShowSet.add(showCfg.getId());
					HawkLog.logPrintln("mechaCore unlock show moduleQuality, playerId: {}, showId: {}, unlocked: {}", entity.getPlayerId(), showCfg.getId(), unlockShowSet);
				}
			}
			
			if (update) {
				entity.notifyUpdate();
				syncWorldPoint();
				return true;
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return false;
	}
	
	/**
	 * 检测解锁新外显
	 * @param moduleList
	 */
	private boolean unlockShowByModuleAttrQuality(List<MechaCoreModuleEntity> moduleList, int moduleAttrQuality) {
		if (moduleAttrQuality < MechaCoreShowCfg.getLowAttrQuality()) {
			return false;
		}
		try {
			int count = 0;
			for (MechaCoreModuleEntity entity : moduleList) {
				count += (int)entity.getRandomAttrEff().stream().filter(e -> e.getQuality() >= moduleAttrQuality).count();
			}
			
			boolean update = false;
			ConfigIterator<MechaCoreShowCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(MechaCoreShowCfg.class);
			while (iterator.hasNext()) {
				MechaCoreShowCfg showCfg = iterator.next();
				if (unlockShowSet.contains(showCfg.getId())) {
					continue;
				}
				if (showCfg.getAttrQuality() > 0 && showCfg.getAttrQuality() <= moduleAttrQuality && count >= showCfg.getAttrCount()) {
					update = true;
					unlockShowSet.add(showCfg.getId());
					HawkLog.logPrintln("mechaCore unlock show moduleAttrQuality, playerId: {}, showId: {}, unlocked: {}", entity.getPlayerId(), showCfg.getId(), unlockShowSet);
				}
			}

			if (update) {
				entity.notifyUpdate();
				syncWorldPoint();
				return true;
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		return false;
	}
	
	/**
	 * 同步世界点信息
	 * @param point
	 */
	public void syncWorldPoint() {
		try {
			WorldPoint point = WorldPlayerService.getInstance().getPlayerWorldPoint(entity.getPlayerId());
			if (point == null) {
				return;
			}
			point.setMechaCoreShow(serializeUnlockedCityShow());
			WorldPointService.getInstance().notifyPointUpdate(point.getX(), point.getY());
			point.notifyUpdate();
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 科技升级或升阶
	 * @param player
	 * @param oper  操作类型：0-升级，1-升阶
	 */
	public void logMechcoreTechFlow(Player player, int oper, int techId, int beforeLevel, int afterLevel) {
		try {
			Map<String, Object> param = new HashMap<>();
	        param.put("oper", oper);          //操作类型：0-升级，1-升阶
	        param.put("techId", techId);           //科技id（升阶情况下，科技id为0）
	        param.put("beforeLevel", beforeLevel); //操作之前的等级（升阶情况下，对应的是品阶）
	        param.put("afterLevel", afterLevel);   //操作之后的等级（升阶情况下，对应的是品阶）
	        LogUtil.logActivityCommon(player, LogInfoType.mecha_core_tech_flow, param);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 模块槽位解锁、升级
	 * @param player
	 */
	public void logMechcoreSlotFlow(Player player, int slotId, int beforeLevel, int afterLevel) {
		try {
			Map<String, Object> param = new HashMap<>();
	        param.put("slotId", slotId);           //槽位id
	        param.put("beforeLevel", beforeLevel); //操作前的等级（为0表示解锁）
	        param.put("afterLevel", afterLevel);   //操作后的等级
	        LogUtil.logActivityCommon(player, LogInfoType.mecha_core_slot_flow, param);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 模块添加或分解
	 * @param player
	 * @param oper  操作类型：0-添加，1-分解
	 */
	public void logMechcoreModuleFlow(Player player, int oper, List<MechaCoreModuleEntity> modules) {
		if (modules.isEmpty()) {
			return;
		}
		
		HawkThreadPool threadPool = HawkTaskManager.getInstance().getThreadPool("task");
		if (modules.size() <= 10 || threadPool == null) {
			logMechcoreModuleBatch(player, oper, modules);
		} else {
			threadPool.addTask(new HawkTask() {
				@Override
				public Object run() {
					logMechcoreModuleBatch(player, oper, modules);
					return null;
				}
			});
		}
	}
	
	/**
	 * 批量上报
	 * @param player
	 * @param oper  操作类型：0-添加，1-分解
	 */
	private void logMechcoreModuleBatch(Player player, int oper, List<MechaCoreModuleEntity> modules) {
		for (MechaCoreModuleEntity module : modules) {
			try {
				Map<String, Object> param = new HashMap<>();
				param.put("oper", oper);                     //操作类型：0-添加，1-分解
				param.put("moduleCfgId", module.getCfgId()); //模块配置id
				param.put("moduleUuid", module.getId());     //模块uuid
				param.put("quality", module.getQuality());   //模块品质
				LogUtil.logActivityCommon(player, LogInfoType.mecha_core_module_flow, param);
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
	}
	
	/**
	 * 模块装载或卸载
	 * @param player
	 * @param oper  操作类型：0-装载，1-卸载
	 */
	public void logModuleLoadFlow(Player player, int oper, int cfgId, String uuid, int slotType, int suitType, int oldCfgId, String oldUuid) {
		try {
			Map<String, Object> param = new HashMap<>();
	        param.put("oper", oper);           //操作类型：0-装载，1-卸载
	        param.put("moduleId", cfgId);      //模块的配置id
	        param.put("moduleUuid", uuid);     //模块uuid
	        param.put("oldModuleId", oldCfgId); //原有模块的配置id
	        param.put("oldModuleUuid", oldUuid); //原有模块的uuid
	        param.put("slotType", slotType);   //操作对应的槽位
	        param.put("suitType", suitType);   //操作对应的套装
	        LogUtil.logActivityCommon(player, LogInfoType.mecha_core_module_oper, param);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 模块属性传承（从A传承到B）
	 * @param player
	 */
	public void logModuleAttrReplace(Player player, String fromModuleUuid, int fromModuleCfgId, String toModuleUuid, int toModuleCfgId, int fromAttrId, int toAttrId) {
		try {
			Map<String, Object> param = new HashMap<>();
	        param.put("fromModuleId", fromModuleCfgId);  //A模块配置ID
	        param.put("fromModuleUuid", fromModuleUuid); //A模块uuid
	        param.put("toModuleId", toModuleCfgId);      //B模块配置ID
	        param.put("toModuleUuid", toModuleUuid);     //B模块uuid
	        param.put("fromAttrId", fromAttrId);         //A模块上的属性ID
	        param.put("toAttrId", toAttrId);             //B模块上的属性（为空时值为0）
	        LogUtil.logActivityCommon(player, LogInfoType.mecha_core_module_attr_replace, param);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 套装解锁或切换
	 * @param player
	 * @param oper  操作类型：0-解锁，1-切换
	 */
	public void logMechacoreSuitChange(Player player, int oper, int oldSuit, int newSuit) {
		try {
			Map<String, Object> param = new HashMap<>();
	        param.put("oper", oper);           //操作类型：0-解锁，1-切换
	        param.put("oldSuit", oldSuit);     //原套装（解锁时此项为0）
	        param.put("newSuit", newSuit);     //新套装
	        LogUtil.logActivityCommon(player, LogInfoType.mecha_core_suit_flow, param);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 获取增产数量
	 * @return
	 */
	public int getGachaAddProductCount() {
		return this.getAddProductInfo().getProductAddCount();
	}

	public MechaAddProductInfo getAddProductInfo() {
		if (addProductInfo == null || addProductInfo.getLoadTime() < getParent().getLoginTime()) {
			String key = RedisKey.GACHA_MODULE_ADD_PRODUCT + ":" + entity.getPlayerId();
			String info  = RedisProxy.getInstance().getRedisSession().getString(key);
			addProductInfo = MechaAddProductInfo.toObject(info);
		}
		return addProductInfo;
	}
	
	/**
	 * 更新增产数量
	 * @param count
	 */
	public void decGachaAddProduct(int count) {
		if(this.getAddProductInfo() != null || count != 0) {
			addProductInfo.incUseCountDaily(count);
			updateAddProductInfo();
		}
	}
	
	/**
	 * 跨天更新数据
	 */
	public void updateGachaAddProductCrossDay() {
		if(this.getAddProductInfo() != null) {
			addProductInfo.updateProductAddCount(0 - addProductInfo.getUseCountDaily());
			addProductInfo.setUseCountDaily(0);
			updateAddProductInfo();
		}
	}

	/**
	 * 使用道具增产
	 * @param itemCfg
	 */
	public void useAddProductItemEffect(ItemCfg itemCfg, int itemCount) {
		this.getAddProductInfo().setUseItemTime(HawkTime.getMillisecond());
		addProductInfo.setUseItemId(itemCfg.getId());
		addProductInfo.updateProductAddCount(itemCfg.getNum() * itemCount);
		updateAddProductInfo();
		getParent().getPush().syncGachaInfo();
	}
	
	private void updateAddProductInfo() {
		String key = RedisKey.GACHA_MODULE_ADD_PRODUCT + ":" + entity.getPlayerId();
		RedisProxy.getInstance().getRedisSession().setString(key, addProductInfo.toString());
	}
	
}
