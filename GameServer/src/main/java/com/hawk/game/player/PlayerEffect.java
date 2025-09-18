package com.hawk.game.player;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.BlockingDeque;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.cfgElement.ArmourStarExplores;
import com.hawk.game.config.*;
import com.hawk.game.entity.*;
import com.hawk.game.util.MapUtil;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.hawk.common.AccountRoleInfo;
import com.hawk.game.GsApp;
import com.hawk.game.GsConfig;
import com.hawk.game.cfgElement.EffectObject;
import com.hawk.game.crossactivity.CrossActivityService;
import com.hawk.game.crossproxy.CrossService;
import com.hawk.game.crossproxy.CrossSkillService;
import com.hawk.game.entity.item.DressItem;
import com.hawk.game.global.GlobalData;
import com.hawk.game.lianmengxzq.XZQService;
import com.hawk.game.manager.AssembleDataManager;
import com.hawk.game.module.spacemecha.SpaceMechaService;
import com.hawk.game.nation.NationService;
import com.hawk.game.nation.NationalBuilding;
import com.hawk.game.nation.tech.NationTechCenter;
import com.hawk.game.player.cache.PlayerDataKey;
import com.hawk.game.player.equip.CommanderObject;
import com.hawk.game.player.equip.EquipSlot;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.player.supersoldier.SuperSoldier;
import com.hawk.game.president.PresidentBuff;
import com.hawk.game.president.PresidentOfficier;
import com.hawk.game.protocol.Armour.ArmourSuitType;
import com.hawk.game.protocol.Building.BuildingBuff;
import com.hawk.game.protocol.Building.ResOutputAddBuffPB;
import com.hawk.game.protocol.Building.ResOutputBuffNoId;
import com.hawk.game.protocol.Building.ResOutputBuffUnit;
import com.hawk.game.protocol.Building.ResOutputBuffUnitType;
import com.hawk.game.protocol.Building.ResOutputBuffWithId;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.National.NationbuildingType;
import com.hawk.game.protocol.Player.LoginWay;
import com.hawk.game.protocol.Talent.TalentType;
import com.hawk.game.service.BuffService;
import com.hawk.game.service.GuildManorService;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.RelationService;
import com.hawk.game.service.college.CollegeService;
import com.hawk.game.service.starwars.StarWarsOfficerService;
import com.hawk.game.superweapon.SuperWeaponService;
import com.hawk.game.superweapon.weapon.IWeapon;
import com.hawk.game.util.EffectParams;
import com.hawk.game.util.GameUtil;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.sdk.SDKConst.UserType;

/**
 * 管理玩家作用号
 *
 * @author 
 *
 */
public class PlayerEffect {

	private static final Logger logger = LoggerFactory.getLogger("Server");

	/**
	 * 挂载玩家数据管理集合
	 */
	private PlayerData playerData;

	/**
	 * 具体effect数据表
	 */
	private Map<Integer, Integer> effectVip = new HashMap<Integer, Integer>();
	private Map<Integer, Integer> effectPlat = new HashMap<Integer, Integer>();
	private Map<Integer, Integer> effectSVIP = new HashMap<Integer, Integer>();
	private Map<Integer, Integer> effectTech = new HashMap<Integer, Integer>();
	private Map<Integer, Integer> effectCrossTech = new HashMap<Integer, Integer>();
	
	private Map<Integer, Map<EffType, Integer>> effectTalent = new HashMap<Integer, Map<EffType, Integer>>();
	
	private Map<Integer, Integer> effectEquip = new HashMap<Integer, Integer>();
	private Map<Integer, Integer> effectBuilding = new HashMap<Integer, Integer>();
	private Map<Integer, Integer> effectManor = new HashMap<Integer, Integer>();
	
	/**
	 * 军衔作用号
	 */
	private Map<Integer, Integer> effectMilitaryRank = new HashMap<Integer, Integer>();
	/**
	 * 装扮作用号
	 */
	private Map<Integer, Integer> effectDress = new HashMap<Integer, Integer>();
	/**
	 * 超级武器作用号
	 */
	private Map<Integer, Integer> effectSuperWeapon = new HashMap<Integer, Integer>();
	/**
	 * 铠甲作用号
	 */
	private Map<Integer, Map<EffType, Integer>> effectArmour = new HashMap<Integer, Map<EffType, Integer>>();
	
	/**
	 * 已推送过的资源田增产作用号数据<BuildingType, buildId or buffId join with ResOutputBuffUnitType, effVal>
	 */
	private Table<BuildingType, String, Integer> pushedResOutputBuffData = HashBasedTable.create();
	
	/**
	 * 装备科技作用号
	 */
	private Map<Integer, Integer> equipResearch = new HashMap<Integer, Integer>();

	/**
	 * 终身卡作用号
	 */
	private Map<Integer,Integer> lifeTimeCard = new HashMap<>();

	/**
	 * 先驱回响
	 */
	Map<EffType, Integer> xqhxTalent = new HashMap<>();

	/**
	 * 是否初始化完成
	 */
	private boolean initOk;
	
	/**
	 * 构造
	 * 
	 * @param playerData
	 */
	public PlayerEffect(PlayerData playerData) {
		this.playerData = playerData;
	}

	/**
	 * 获取数据对应玩家对象
	 *
	 * @return
	 */
	public PlayerData getPlayerData() {
		return playerData;
	}

	/**
	 * 设置本数据实体的玩家对象
	 *
	 * @return
	 */
	public void setPlayerData(PlayerData playerData) {
		this.playerData = playerData;
	}
	
	/**
	 * 资源田增产作用号
	 */
	static class ResOutputBuffVal {
		int effId;
		int effVal;
		String buildId = "";
		
		ResOutputBuffVal(int effId, int effVal) {
			this.effId = effId;
			this.effVal = effVal;
		}
		
		ResOutputBuffVal(int effId, int effVal, String targetId) {
			this(effId, effVal);
			this.buildId = targetId;
		}
		
		String key() {
			if (!HawkOSOperator.isEmptyString(buildId)) {
				return buildId;
			}
			
			return String.valueOf(effId);
		}
	}

	/**
	 * 初始化作用号
	 *
	 * @return
	 */
	public void init() {
		if (GsApp.getInstance().isInitOK()) { 
			// 这段代码一定要放在前面, 不然会掉进无限递归
			initOk = true;
		}

		initEffectEquipResearch();
		initStarExplore();
		initEffectVip();
		initEffectPlat();
		initEffectStatus();
		initEffectTech();
		initEffectCrossTech();
		initEffectTalent();
		initEffectEquip();
		initEffectBuilding();
		initEffectManor();
		initEffectMilityRank();
		initEffectDress();
		initEffectSuperWeapon();
		initEffectArmour();
		initEffectHero();
		initEffectSuperSoldier();
		initLifeTimeCard();
		initEffectManhattan();
		initEffectMechaCore();
		initXqhxTalent();
	}
	public void initLifeTimeCard(){
		LifetimeCardEntity lifetimeCardEntity = playerData.getLifetimeCardEntity();
		LifetimeCardCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(LifetimeCardCfg.class);
		lifeTimeCard.clear();

		if (lifetimeCardEntity.isCommonUnlock()){
			List<EffectObject> commonEffList = kvCfg.getCommonEffList();
			for (EffectObject effectObject:commonEffList){
				lifeTimeCard.put(effectObject.getEffectType(),effectObject.getEffectValue());
			}
		}
		if (lifetimeCardEntity.getFreeEndTime() > HawkTime.getMillisecond()){
			List<EffectObject> commonEffList = kvCfg.getCommonEffList();
			for (EffectObject effectObject:commonEffList){
				lifeTimeCard.put(effectObject.getEffectType(),effectObject.getEffectValue());
			}
		}
		if (lifetimeCardEntity.getAdvancedEndTime() > HawkTime.getMillisecond()){
			List<EffectObject> advanceEffList = kvCfg.getAdvanceEffList();
			for (EffectObject effectObject:advanceEffList){
				lifeTimeCard.put(effectObject.getEffectType(),effectObject.getEffectValue());
			}
		}

	}

	public void resetLifeTimeCard(Player player){
		List<EffType> changeList = new ArrayList<>();
		for (int effId : lifeTimeCard.keySet()) {
			changeList.add(EffType.valueOf(effId));
		}

		initLifeTimeCard();

		for (int effId : lifeTimeCard.keySet()) {
			if (changeList.contains(EffType.valueOf(effId))) {
				continue;
			}
			changeList.add(EffType.valueOf(effId));
		}
		if (changeList.size() == 0){
			return;
		}
		syncEffect(player, changeList.toArray(new EffType[changeList.size()]));

	}
	public void initEffectVip() {
		effectVip.clear();
		int vipLevel = playerData.getPlayerEntity().getVipLevel();
		VipCfg vipCfg = HawkConfigManager.getInstance().getConfigByKey(VipCfg.class, vipLevel);
		if (vipCfg != null) {
			vipCfg.assembleEffectMap(effectVip);
		}
	}
	
	public void initEffectPlat() {
		String channel = playerData.getPlayerEntity().getChannel().toLowerCase();
		effectPlat.clear();
		ConstProperty.getInstance().assemblePlatEffectMap(effectPlat, channel);
		if (UserType.getByChannel(channel) == UserType.QQ) {
			ConstProperty.getInstance().assemblePlatEffectMap(effectSVIP, "svip");
		}
	}

	/**
	 * 初始化作用号
	 */
	public void initEffectVip(Player player) {
		initEffectVip();
		List<EffType> list = new ArrayList<EffType>();
		for (int effId : effectVip.keySet()) {
			list.add(EffType.valueOf(effId));
		}

		EffType[] types = list.toArray(new EffType[list.size()]);
		syncEffect(player, types);
	}
	
	/**
	 * 跨天重置启动特权加成
	 * 
	 * @param player
	 */
	public void clearEffectPlat(Player player) {
		Set<EffType> list = new HashSet<EffType>();
		for (int effId : effectPlat.keySet()) {
			list.add(EffType.valueOf(effId));
		}

		EffType[] types = list.toArray(new EffType[list.size()]);
		syncEffect(player, types);
	}

	/**
	 * 初始化状态作用号
	 */
	public void initEffectStatus() {
		// 不作处理
	}

	/**
	 * 天赋洗点
	 */
	public void clearEffectTalent(Player player) {
		Set<EffType> list = new HashSet<EffType>();
		for (int effId : effectTalent.keySet()) {
			list.add(EffType.valueOf(effId));
		}
		effectTalent.clear();

		EffType[] types = list.toArray(new EffType[list.size()]);
		syncEffect(player, types);
	}

	/**
	 * 初始化天赋作用号
	 */
	public Set<EffType> initEffectTalent() {
		Set<EffType> list = new HashSet<EffType>();
		for (int effId : effectTalent.keySet()) {
			list.add(EffType.valueOf(effId));
		}

		Map<Integer, Map<EffType, Integer>> effectTalent = new HashMap<Integer, Map<EffType, Integer>>();
		
		List<TalentEntity> talentEntities = playerData.getTalentEntities();
		for (TalentEntity talentEntity : talentEntities) {
			int talentId = talentEntity.getTalentId();
			int talentLvl = talentEntity.getLevel();
			if (talentId <= 0 || talentLvl <= 0) {
				continue;
			}
			
			TalentLevelCfg cfg = AssembleDataManager.getInstance().getTalentLevelCfg(talentId, talentLvl);
			if (cfg == null || HawkOSOperator.isEmptyString(cfg.getEffect())) {
				continue;
			}
			
			Map<EffType, Integer> effMap = effectTalent.get(talentEntity.getType());
			if (effMap == null) {
				effMap = new HashMap<>();
				effectTalent.put(talentEntity.getType(), effMap);
			}
			
			for (EffectObject eff : cfg.getEffList()) {
				EffType effId = EffType.valueOf(eff.getEffectType());
				int effVal = eff.getEffectValue();
				
				if (effMap.containsKey(effId)) {
					effVal += effMap.get(effId);
				}
				
				effMap.put(effId, effVal);
				list.add(effId);
			}
		}
		
		this.effectTalent = effectTalent;
		
		return list;
	}

	public void initEffectTalent(Player player) {
		Set<EffType> list = initEffectTalent();
		EffType[] types = list.toArray(new EffType[list.size()]);
		syncEffect(player, types);
	}

	public void initXqhxTalent(){
		Map<EffType, Integer> xqhxTalentTmp = new HashMap<>();
		for (PlayerXQHXTalentEntity entity : playerData.getXQHXTalentEntityList()){
			XQHXTalentLevelCfg cfg = entity.getCfg();
			if(cfg == null){
				continue;
			}
			MapUtil.mergeMap(xqhxTalentTmp, cfg.getEffectMap());
		}
		xqhxTalent = xqhxTalentTmp;
	}

	public void syncXqhxTalent(Player player){
        Set<EffType> effTypes = new HashSet<>(xqhxTalent.keySet());
		initXqhxTalent();
		effTypes.addAll(xqhxTalent.keySet());
		syncEffect(player, effTypes.toArray(new EffType[0]));
	}

	/**
	 * 初始化科技作用号
	 */
	public void initEffectTech() {
		effectTech.clear();

		List<TechnologyEntity> technologyEntities = playerData.getTechnologyEntities();
		for (TechnologyEntity entity : technologyEntities) {
			if (entity.getLevel() <= 0) {
				continue;
			}
			TechnologyCfg cfg = HawkConfigManager.getInstance().getConfigByKey(TechnologyCfg.class, entity.getCfgId());
			if (cfg == null) {
				continue;
			}
			for (EffectObject effect : cfg.getEffectList()) {
				int effId = effect.getEffectType();
				int effVal = effect.getEffectValue();
				if (effectTech.containsKey(effId)) {
					effVal += effectTech.get(effId);
				}
				effectTech.put(effId, effVal);
			}
		}
	}
	
	/**
	 * 初始化科技作用号
	 */
	public void initEffectCrossTech() {
		effectCrossTech.clear();
		
		List<CrossTechEntity> technologyEntities = playerData.getCrossTechEntities();
		for (CrossTechEntity entity : technologyEntities) {
			if (entity.getLevel() <= 0) {
				continue;
			}
			CrossTechCfg cfg = HawkConfigManager.getInstance().getConfigByKey(CrossTechCfg.class, entity.getCfgId());
			if (cfg == null) {
				continue;
			}
			for (EffectObject effect : cfg.getEffectList()) {
				int effId = effect.getEffectType();
				int effVal = effect.getEffectValue();
				if (effectCrossTech.containsKey(effId)) {
					effVal += effectCrossTech.get(effId);
				}
				effectCrossTech.put(effId, effVal);
			}
		}
	}

	/**
	 * 初始化领地作用号效果
	 */
	public void initEffectManor() {
		if (!GsApp.getInstance().isInitOK()) {
			return;
		}
		effectManor.clear();
		String playerId = playerData.getPlayerEntity().getId();
		boolean inSelfManor = GuildManorService.getInstance().isInOwnGuildManor(playerId);
		if (inSelfManor) {
			int manorLvl = 1;
			List<int[]> effectList = AssembleDataManager.getInstance().getManorBuff(manorLvl);
			addEffectList(effectManor, effectList);
			return;
		}
		// 判断在N个敌对领地,debuff叠加
		List<Integer> enemyManorLvls = GuildManorService.getInstance().getEnemyManorBuff(playerId);
		for (int lvl : enemyManorLvls) {
			List<int[]> effectList = AssembleDataManager.getInstance().getManorDeBuff(lvl);
			addEffectList(effectManor, effectList);
		}
	}

	private void addEffectList(Map<Integer, Integer> effectMap, List<int[]> effectList) {
		for (int[] effect : effectList) {
			int type = effect[0];
			int val = effect[1];
			if (effectMap.containsKey(type)) {
				effectMap.put(type, effectMap.get(type) + val);
			} else {
				effectMap.put(type, val);
			}
		}
	}

	/**
	 * 初始化装备作用号
	 */
	private void initEffectEquip() {
		effectEquip.clear();
		CommanderObject commander = playerData.getCommanderObject();
		List<EquipSlot> slotList = commander.getEquipSlots();
		for (EquipSlot slot : slotList) {
			Map<Integer, Integer> attrs = slot.getEquipAttr();
			for (Entry<Integer, Integer> entry : attrs.entrySet()) {
				int key = entry.getKey();
				int val = entry.getValue();
				if (effectEquip.containsKey(key)) {
					effectEquip.put(key, effectEquip.get(key) + val);
				} else {
					effectEquip.put(key, val);
				}
			}
		}
	}

	private void initEffectEquipResearch() {
		if (!GsApp.getInstance().isInitOK()) {
			return;
		}
		equipResearch = GameUtil.getEquipResearchEff(playerData);
		
	}
	
	private void initStarExplore(){
		if (!GsApp.getInstance().isInitOK()) {
			return;
		}
		try {
			CommanderEntity entity = playerData.getCommanderEntity();
			ArmourStarExplores starExplores = entity.getStarExplores();
			starExplores.loadEffMap();
		}catch (Exception e){
			HawkException.catchException(e);
		}
	}

	private void initEffectSuperWeapon() {
		if (!GsApp.getInstance().isInitOK()) {
			return;
		}
		effectSuperWeapon.clear();
		
		String guildId = GuildService.getInstance().getPlayerGuildId(playerData.getPlayerId());
		List<IWeapon> weapons = SuperWeaponService.getInstance().getGuildControlSuperWeapon(guildId);
		if (weapons.isEmpty()) {
			return;
		}
		
		for (IWeapon weapon : weapons) {
			SuperWeaponCfg superWeaponCfg = AssembleDataManager.getInstance().getSuperWeaponCfg(weapon.getPointId());
			if (superWeaponCfg == null) {
				logger.error("initEffectSuperWeapon error, superWeaponCfg not found, pointId:{}", weapon.getPointId());
				continue;
			}
			for (EffectObject eff : superWeaponCfg.getBuffList()) {
				int key = eff.getEffectType();
				int val = eff.getEffectValue();
				if (effectSuperWeapon.containsKey(key)) {
					effectSuperWeapon.put(key, effectSuperWeapon.get(key) + val);
				} else {
					effectSuperWeapon.put(key, val);
				}
			}
		}
	}
	
	/**
	 * 初始化军衔作用号
	 */
	private void initEffectMilityRank() {
		effectMilitaryRank.clear();
		int militaryExp = playerData.getPlayerEntity().getMilitaryExp();
		int militaryLvl = GameUtil.getMilitaryRankByExp(militaryExp);

		MilitaryRankCfg cfg = null;

		ConfigIterator<MilitaryRankCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(MilitaryRankCfg.class);
		while (configIterator.hasNext()) {
			MilitaryRankCfg thisCfg = configIterator.next();
			if (thisCfg.getRankLevel() == militaryLvl) {
				cfg = thisCfg;
				break;
			}
		}

		if (cfg == null) {
			logger.error("military rank cfg is null, level:{}", militaryLvl);
			return;
		}
		
		List<EffectObject> touchEffect = cfg.getTouchEffect();
		for (EffectObject eff : touchEffect) {
			int key = eff.getEffectType();
			int val = eff.getEffectValue();
			if (effectMilitaryRank.containsKey(key)) {
				effectMilitaryRank.put(key, effectMilitaryRank.get(key) + val);
			} else {
				effectMilitaryRank.put(key, val);
			}
		}
	}

	/**
	 * 初始化装扮作用号
	 */
	private void initEffectDress() {
		effectDress.clear();

		// 拥有装扮触发作用号
		BlockingDeque<DressItem> dressInfos = playerData.getDressEntity().getDressInfo();
		for (DressItem dressInfo : dressInfos) {
			DressCfg dressCfg = AssembleDataManager.getInstance().getDressCfg(dressInfo.getDressType(), dressInfo.getModelType());
			if (dressCfg == null) {
				logger.error("initEffectDress error, gain dress cfg null, dressType:{}, modelType:{}", dressInfo.getDressType(), dressInfo.getModelType());
				continue;
			}

			for (EffectObject eff : dressCfg.getGainEffectList()) {
				int key = eff.getEffectType();
				int val = eff.getEffectValue();
				if (effectDress.containsKey(key)) {
					effectDress.put(key, effectDress.get(key) + val);
				} else {
					effectDress.put(key, val);
				}
			}
		}

		// 使用装扮触发作用号
		Map<Integer, DressItem> showDresses = WorldPointService.getInstance().getShowDress(playerData.getPlayerBaseEntity().getPlayerId());
		for (Entry<Integer, DressItem> showDress : showDresses.entrySet()) {
			DressCfg dressCfg = AssembleDataManager.getInstance().getDressCfg(showDress.getKey(), showDress.getValue().getModelType());
			if (dressCfg == null) {
				logger.error("initEffectDress error, use dress cfg null, dressType:{}, modelType:{}", showDress.getKey(), showDress.getValue());
				continue;
			}

			for (EffectObject eff : dressCfg.getUseEffectList()) {
				int key = eff.getEffectType();
				int val = eff.getEffectValue();
				if (effectDress.containsKey(key)) {
					effectDress.put(key, effectDress.get(key) + val);
				} else {
					effectDress.put(key, val);
				}
			}
		}
		
		// 使用套装装扮触发作用号
		ConfigIterator<DressGroupCfg> dressGroupIter = HawkConfigManager.getInstance().getConfigIterator(DressGroupCfg.class);
		while (dressGroupIter.hasNext()) {
			
			DressGroupCfg dressGroupCfg = dressGroupIter.next();
			
			// 是否触发套装属性
			boolean touch = true;
			
			for (Integer dressId : dressGroupCfg.getDressIdList()) {
				DressCfg dressCfg = HawkConfigManager.getInstance().getConfigByKey(DressCfg.class, dressId);
				if (dressCfg == null) {
					touch = false;
				} else {
					DressItem show = showDresses.get(dressCfg.getDressType());
					if (show == null || show.getModelType() != dressCfg.getModelType()) {
						touch = false;
					}
				}
			}
			
			if (!touch) {
				continue;
			}
			
			for (EffectObject eff : dressGroupCfg.getEffectList()) {
				int key = eff.getEffectType();
				int val = eff.getEffectValue();
				if (effectDress.containsKey(key)) {
					effectDress.put(key, effectDress.get(key) + val);
				} else {
					effectDress.put(key, val);
				}
			}
		}
		
		// 装扮点加成作用号
		int dressPoint = WorldPointService.getInstance().getDressPoint(playerData);
		ConfigIterator<DressPointCfg> dressPointCfgIter = HawkConfigManager.getInstance().getConfigIterator(DressPointCfg.class);
		while (dressPointCfgIter.hasNext()) {
			DressPointCfg cfg = dressPointCfgIter.next();
			if (dressPoint < cfg.getNeedPoint()) {
				continue;
			}
			
			for (EffectObject eff : cfg.getEffectList()) {
				int key = eff.getEffectType();
				int val = eff.getEffectValue();
				if (effectDress.containsKey(key)) {
					effectDress.put(key, effectDress.get(key) + val);
				} else {
					effectDress.put(key, val);
				}
			}
		}
	}

	private void initEffectArmour() {
		if (!GsApp.getInstance().isInitOK()) {
			return;
		}
		
		Map<Integer, Map<EffType, Integer>> effectArmour = new HashMap<Integer, Map<EffType, Integer>>();
		int armourSuitCount = playerData.getPlayerEntity().getArmourSuitCount();
		int suitMaxCount = ArmourConstCfg.getInstance().getSuitMaxCount();
		for (int i = 0; i < suitMaxCount; i++) {
			if (armourSuitCount < i + 1) {
				effectArmour.put(i + 1, new HashMap<>());
			} else {
				effectArmour.put(i + 1, playerData.getArmourEffect(i + 1));
			}
		}
		this.effectArmour = effectArmour;
	}
	
	/**
	 * 英雄做用号初始 
	 */
	public void initEffectHero() {
		playerData.getHeroEntityList().stream()
				.map(HeroEntity::getHeroObj)
				.forEach(PlayerHero::loadEffVal);
	}
	
	/**
	 * 英雄做用号初始 
	 */
	public void initEffectSuperSoldier() {
		playerData.getSuperSoldierEntityList().stream()
				.map(SuperSoldierEntity::getSoldierObj)
				.forEach(SuperSoldier::loadEffVal);
	}
	
	/**
	 * 超武作用号初始化
	 */
	public void initEffectManhattan() {
		playerData.getManhattanEntityList().stream().map(e -> e.getManhattanObj()).forEach(e -> e.loadEffVal());
	}
	
	/**
	 * 机甲核心作用号初始化
	 */
	public void initEffectMechaCore() {
		try {
			playerData.getMechaCoreEntity().getMechaCoreObj().loadEffVal();
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 初始化建筑作用号
	 */
	public void initEffectBuilding() {
		List<BuildingBaseEntity> buildingEntities = playerData.getBuildingEntities();
		for (BuildingBaseEntity buildingEntity : buildingEntities) {
			BuildingCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, buildingEntity.getBuildingCfgId());
			if (cfg == null) {
				continue;
			}

			Map<Integer, Integer> effectMap = cfg.getBuildEffectMap();
			for (Entry<Integer, Integer> entry : effectMap.entrySet()) {
				resetEffectBuilding(entry.getKey(), entry.getValue());
			}
		}
	}

	// 以下几个add方法，用于单个作用号变化，比如vip升级科技升级。对于多条数据更新，建议使用init重新初始化数据。

	/**
	 * vip升级时添加作用号
	 */
	public void addEffectVip(Player player, int effId, int effVal) {
		if (effectVip.containsKey(effId)) {
			effVal += effectVip.get(effId);
		}
		effectVip.put(effId, effVal);

		syncEffect(player, EffType.valueOf(effId));
	}

	private void addEffectTech(Player player, int effId, int effVal) {
		if (effectTech.containsKey(effId)) {
			effVal += effectTech.get(effId);
		}
		effectTech.put(effId, effVal);

		syncEffect(player, EffType.valueOf(effId));
	}
	
	private void addCrossEffectTech(Player player, int effId, int effVal) {
		if (effectCrossTech.containsKey(effId)) {
			effVal += effectCrossTech.get(effId);
		}
		effectCrossTech.put(effId, effVal);
		
		syncEffect(player, EffType.valueOf(effId));
	}

	/**
	 * 建筑特效
	 * @param effId
	 * @param effVal
	 */
	public void addEffectBuilding(Player player, int effId, int effVal) {
		if (effectBuilding.containsKey(effId)) {
			effVal += effectBuilding.get(effId);
		}
		effectBuilding.put(effId, effVal);
		syncEffect(player, EffType.valueOf(effId));
	}

	/**
	 * 刷新指挥官装备作用号
	 * @param player
	 * @param effTypes
	 */
	public void resetEffectEquip(Player player, EffType[] effTypes) {
		initEffectEquip();
		syncEffect(player, effTypes);
	}

	public void resetEffectEquipResearch(Player player) {
		List<EffType> changeList = new ArrayList<>();
		for (int effId : equipResearch.keySet()) {
			changeList.add(EffType.valueOf(effId));
		}
		
		initEffectEquipResearch();
		
		for (int effId : equipResearch.keySet()) {
			if (changeList.contains(EffType.valueOf(effId))) {
				continue;
			}
			changeList.add(EffType.valueOf(effId));
		}

		syncEffect(player, changeList.toArray(new EffType[changeList.size()]));
	}
	

	public void syncStarExplore(Player player, ArmourStarExplores starExplores) {
		List<EffType> changeList = new ArrayList<>();
		for (int effId : starExplores.getEffMap().keySet()) {
			changeList.add(EffType.valueOf(effId));
		}
		syncEffect(player, changeList.toArray(new EffType[changeList.size()]));
	}

	/**
	 * 刷新军衔作用号
	 * @param player
	 * @param effTypes
	 */
	public void resetEffectMilitaryRank(Player player) {
		List<EffType> changeList = new ArrayList<>();
		for (int effId : effectMilitaryRank.keySet()) {
			changeList.add(EffType.valueOf(effId));
		}

		initEffectMilityRank();

		for (int effId : effectMilitaryRank.keySet()) {
			if (changeList.contains(EffType.valueOf(effId))) {
				continue;
			}
			changeList.add(EffType.valueOf(effId));
		}

		syncEffect(player, changeList.toArray(new EffType[changeList.size()]));
	}

	/**
	 * 刷新超级武器作用号
	 * @param player
	 * @param effTypes
	 */
	public void resetEffectSuperWeapon(Player player) {
		List<EffType> changeList = new ArrayList<>();
		for (int effId : effectSuperWeapon.keySet()) {
			changeList.add(EffType.valueOf(effId));
		}

		initEffectSuperWeapon();
		
		for (int effId : effectSuperWeapon.keySet()) {
			if (changeList.contains(EffType.valueOf(effId))) {
				continue;
			}
			changeList.add(EffType.valueOf(effId));
		}
		
		syncEffect(player, changeList.toArray(new EffType[changeList.size()]));
	}
	
	/**
	 * 刷新装扮作用号
	 * @param player
	 */
	public void resetEffectDress(Player player) {
		List<EffType> changeList = new ArrayList<>();
		for (int effId : effectDress.keySet()) {
			changeList.add(EffType.valueOf(effId));
		}

		initEffectDress();

		for (int effId : effectDress.keySet()) {
			if (changeList.contains(EffType.valueOf(effId))) {
				continue;
			}
			changeList.add(EffType.valueOf(effId));
		}

		syncEffect(player, changeList.toArray(new EffType[changeList.size()]));
	}

	/**
	 * 刷新铠甲作用号
	 */
	public void resetEffectArmour(Player player) {
		int armourSuit = player.getEntity().getArmourSuit();
		
		List<EffType> changeList = new ArrayList<>();
		
		if (effectArmour.containsKey(armourSuit)) {
			for (EffType effectType : effectArmour.get(armourSuit).keySet()) {
				changeList.add(effectType);
			}
		}

		initEffectArmour();

		if (effectArmour.containsKey(armourSuit)) {
			for (EffType effectType : effectArmour.get(armourSuit).keySet()) {
				if (changeList.contains(effectType)) {
					continue;
				}
				changeList.add(effectType);
			}
		}

		syncEffect(player, changeList.toArray(new EffType[changeList.size()]));
	}
	
	/**
	 * 科技升级添加对应作用号效果
	 * @param entity
	 */
	public void addEffectTech(Player player, TechnologyEntity entity) {
		if (entity == null) {
			return;
		}
		TechnologyCfg oldCfg = HawkConfigManager.getInstance().getConfigByKey(TechnologyCfg.class, entity.getCfgId());
		TechnologyCfg newCfg = HawkConfigManager.getInstance().getConfigByKey(TechnologyCfg.class, entity.getCfgId() + 1);
		if (oldCfg == null && newCfg == null) {
			return;
		}

		if (oldCfg == null) { // newCfg肯定不为null
			for (EffectObject effect : newCfg.getEffectList()) {
				addEffectTech(player, effect.getEffectType(), effect.getEffectValue());
			}
		} else { // oldCfg、newCfg都不为null，或newCfg为null oldCfg不为null
			Map<Integer, Integer> map = new HashMap<Integer, Integer>();
			if (newCfg != null) {
				for (EffectObject newEffTech : newCfg.getEffectList()) {
					map.put(newEffTech.getEffectType(), newEffTech.getEffectValue());
				}
			}

			for (EffectObject oldEffTech : oldCfg.getEffectList()) {
				int effId = oldEffTech.getEffectType();
				if (map.containsKey(effId)) {
					map.put(effId, map.get(effId) - oldEffTech.getEffectValue());
				} else {
					map.put(effId, -oldEffTech.getEffectValue());
				}
			}

			for (Map.Entry<Integer, Integer> tmp : map.entrySet()) {
				addEffectTech(player, tmp.getKey(), tmp.getValue());
			}
		}
	}
	
	/**
	 * 远征科技升级添加对应作用号效果 
	 * @param entity
	 */
	public void addEffectCrossTech(Player player, CrossTechEntity entity) {
		if (entity == null) {
			return;
		}
		CrossTechCfg oldCfg = HawkConfigManager.getInstance().getConfigByKey(CrossTechCfg.class, entity.getCfgId());
		CrossTechCfg newCfg = HawkConfigManager.getInstance().getConfigByKey(CrossTechCfg.class, entity.getCfgId() + 1);
		if (oldCfg == null && newCfg == null) {
			return;
		}

		if (oldCfg == null) { // newCfg肯定不为null
			for (EffectObject effect : newCfg.getEffectList()) {
				addCrossEffectTech(player, effect.getEffectType(), effect.getEffectValue());
			}
		} else { // oldCfg、newCfg都不为null，或newCfg为null oldCfg不为null
			Map<Integer, Integer> map = new HashMap<Integer, Integer>();
			if (newCfg != null) {
				for (EffectObject newEffTech : newCfg.getEffectList()) {
					map.put(newEffTech.getEffectType(), newEffTech.getEffectValue());
				}
			}

			for (EffectObject oldEffTech : oldCfg.getEffectList()) {
				int effId = oldEffTech.getEffectType();
				if (map.containsKey(effId)) {
					map.put(effId, map.get(effId) - oldEffTech.getEffectValue());
				} else {
					map.put(effId, -oldEffTech.getEffectValue());
				}
			}

			for (Map.Entry<Integer, Integer> tmp : map.entrySet()) {
				addCrossEffectTech(player, tmp.getKey(), tmp.getValue());
			}
		}
	}

	public void resetEffectBuilding(int effId, int effVal) {
		effectBuilding.put(effId, effVal);
	}

	public void resetEffectBuilding(Player player, int effId, int effVal) {
		resetEffectBuilding(effId, effVal);
		syncEffect(player, EffType.valueOf(effId));
	}

	/**
	 * 同步作用号数据
	 * 
	 * @param player
	 * @param types
	 */
	public void syncEffect(Player player, EffType... types) {
		if (player.isActiveOnline()) {
			player.getPush().syncPlayerEffect(types);
		}
	}

	public void syncEffect(Player player, Collection<Integer> collection){
		if(player.isActiveOnline()){
			List<EffType> types = new ArrayList<>();
			for(int effId : collection){
				EffType type = EffType.valueOf(effId);
				if(type != null){
					types.add(type);
				}
			}
			syncEffect(player, types.toArray(new EffType[0]));
		}
	}

	public int getEffVal(EffType effType, EffectParams effParams) {
		return getEffVal(effType, null, effParams);
	}

	/**
	 * 根据作用号取作用值
	 * 
	 * @param effType
	 * @return
	 */
	public int getEffVal(EffType effType) {
		return getEffVal(effType, null, EffectParams.getDefaultVal());
	}
	
	public int[] getEffValArr(EffType... effType) {
		int[] result = new int[effType.length];
		try {
			for (int i = 0; i < result.length; i++) {
				result[i] = getEffVal(effType[i], EffectParams.getDefaultVal());
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return result;
	}

	/**
	 * 根据作用号取作用值
	 *
	 * @return
	 */
	public int getEffVal(EffType effType, String targetId) {
		return getEffVal(effType, targetId, EffectParams.getDefaultVal());
	}

	/**
	 * 根据作用号取作用值
	 * 
	 * 注意！！！此方法会计算玩家身上的所有作用号，会有上百次调用，所以此处禁止有IO操作，切记
	 * 
	 * @return
	 */
	public int getEffVal(EffType effType, String targetId, EffectParams effParams) {
		if (!initOk && GsApp.getInstance().isInitOK()) {
			init();
		}
		
		// 判断作用号是否存在
		if (effType == null) {
			return 0;
		}
		
		if(effType.getNumber() >= EffType.EFF_1461_VALUE && effType.getNumber() <= EffType.EFF_1478_VALUE){
			//这个段是黑科技活动加的buff,在跨服情况下不生效
			String playerId = playerData.getPlayerId();
			if(CrossService.getInstance().isCrossPlayer(playerId)){
				return 0;
			}
			//用此判断在不在泰伯利亚
			if(playerData.getDataCache().isLockKey(PlayerDataKey.ArmyEntities)){
				return 0;
			}
		}
		
		int effId = effType.getNumber();
		String playerId = playerData.getPlayerEntity().getId();

		// 道具状态、城市增益
		int effState = getEffectStatus(effId, targetId);  // 5  道具
		
		// 跨服相关buff
		int crossBuff = getCrossBuff(effId);
		
		// 军事学院加成
		int collegeBuff = getCollegeEff(effType);
		
		// 全局
		int globalBuff = getGlobalEffect(effId);
		// VIP
		int effVip = getEffectVip(effId);            // 1  贵族
		// 平台启动特权
		int effStartUp = getEffectPlat(effId);    // 2  平台特权

		// 天赋
		int effTalent = getEffectTalent(effParams.getTalent(), effId);      // 3  天赋

		// 科技
		int effTech = getEffectTech(effId);         // 4  科技
		
		// 远征科技
		int effCrossTech = getEffectCrossTech(effId);

		// 装备
		int effEquip = getEffectEquip(effId);

		// 军衔
		int effMilitaryRank = getEffectMilitary(effId);

		// 装扮
		int effDress = getEffectDress(effId, effParams);
		
		// 建筑
		int effBuilding = getEffectBuilding(effId);

		// 官职
		int effOfficer = PresidentOfficier.getInstance().getEffectOfficer(playerId, effId);

		// 联盟相关
		int effGuild = GuildService.getInstance().getEffectGuild(playerId, effId);

		// 领地增益/减益
		int effGuildManor = getEffectManor(effId);   // 6 联盟领地
		
		// 超级武器
		int effSuperWeapon = getEffectSuperWeapon(effId);
		
		// 英雄官职增益
		int heroOffice = getHerosOfficeEffVal(effType, effParams);
		int planttech = getPlantTechEffVal(effType);
		int plantSchool =  getPlantSchoolEffVal(effType);
		int plantScience = getPlantScienceEffVal(effType);
		// 英雄出征
		int effHero = getHeroMarchEffVal(effType, effParams);
		// 英雄羁绊增益
		int effHeroCol = getHerosCollectEffVal(effType);

		// 神兽
		int soldierOffice = getSuperSoldierOfficeEffVal(effType);
		// 神兽出征
		int effSS = getSuperSoldierMarchEffVal(effType, effParams.getSuperSoliderId());
		//超级实验室
		int slab = getSuperLabVal(effType, effParams.getSuperLab());
		//跨服携带的作用号
		int crossEffValue = getEffectValueInCross(effId);
		// 要塞作用号
		int fortressEffValue = getFortressVal(effType);
		// 铠甲
		int armourEffValue = getEffectArmour(effParams.getArmourSuit(), effType);
		//总统开启的buff
		int presidentBuff = getPresidentBuff(playerId, effId);
		//守护的buff
		int guardBuff = this.getGuardBuff(playerId, effId);
		//星球大战.
		int starWarsEffect = this.getStarWarsEffect(playerId, effId);
		// 司令技能
		int crossSkillEft = CrossSkillService.getInstance().getEffectValIfContinue(playerData.getPlayerEntity().getServerId(), effType);
		// 装备科技
		int equipResearch = this.getEffectEquipResearch(effId);
		//小站区作用号
		int xzqEffValue = XZQService.getInstance().getXZQEffectVal(playerId, effId);

		// 国家科技作用号
		int nationTechEff = 0;
		try {
			NationTechCenter techCenter = (NationTechCenter) NationService.getInstance().getNationBuildingByType(NationbuildingType.NATION_TECH_CENTER);
			if (techCenter != null && !HawkOSOperator.isEmptyString(playerId)) {
				if (!CrossService.getInstance().isCrossPlayer(playerId)) {
					nationTechEff = techCenter.getEffValue(effId);
				} else {
					String serverId = playerData.getPlayerEntity().getServerId();
					serverId = GlobalData.getInstance().getMainServerId(serverId);
					nationTechEff = techCenter.getCrossEffValue(serverId, effId);
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		int nationalHopitalEffVal = getNationalHospitalEffVal(effId);
		
		int crossScoreRankBuff = CrossActivityService.getInstance().getCrossScoreRankBuff(playerId, effId);
		int crossPylonBuff = CrossActivityService.getInstance().getPylonBuff(playerId, effId);
		int crossTowerBuff = CrossActivityService.getInstance().getTowerBuff(playerId, effId);
		
		int heroArchiveEff = getHeroArchivesEffVal(effId);
		// 星甲召唤的buff加成
		int spaceMechaBuff = SpaceMechaService.getInstance().getPlayerEffVal(playerId, effId, effParams);
		int staffoffice = getStaffOfficEffVal(effType, effParams);

		//终身卡
		int lifeTimeCardBuff = getLifeTimeBuff(effId);
		int medalfVal = getMedalFactoryEff(effType);
		//星能探索
		int starExploreBuff = getStarExploreBuff(effId);
		//超武
		int manhattanEff = getManhattanEff(effId, effParams);
		//机甲核心
		int mechaCoreEff = getMechaCoreEff(effId, effParams);
		//活动作用号
		int activityEff = getActivityBuff(effId);
		//先驱回响作用号
//		int xqhxEff = getXqhxBuff(effType);
        //家园作用号
        int homeLandEff = getHomeLandBuff(effType);
		// TODO 注意！！！此方法会计算玩家身上的所有作用号，会有上百次调用，所以此处禁止有IO操作，切记

		int effVal = effState + crossBuff + effVip + effStartUp + effTalent + effTech + effCrossTech + effEquip
				+ effBuilding + effOfficer + effGuild + effGuildManor + globalBuff + heroOffice
				+ effHero + effMilitaryRank + effDress + effSuperWeapon + soldierOffice + effSS +
				slab + collegeBuff + crossEffValue + fortressEffValue + armourEffValue + presidentBuff + guardBuff 
				+ starWarsEffect + effHeroCol + crossSkillEft + equipResearch + planttech + xzqEffValue + plantSchool 
				+ plantScience + nationTechEff + nationalHopitalEffVal + crossScoreRankBuff + crossPylonBuff + crossTowerBuff
				+ heroArchiveEff + spaceMechaBuff + staffoffice + lifeTimeCardBuff + medalfVal + starExploreBuff + manhattanEff
				+ mechaCoreEff + activityEff + homeLandEff;

		return effVal;
	}
	
	/**
	 * 机甲核心
	 * @return
	 */
	private int getMechaCoreEff(int effId, EffectParams effParams) {
		try {
			return playerData.getMechaCoreEntity().getMechaCoreObj().getEffVal(effId, effParams);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return 0;
	}
	
	/**
	 * 超级武器
	 * @param effType
	 * @return
	 */
	private int getManhattanEff(int effId, EffectParams effParams) {
		int val = 0;
		for(ManhattanEntity entity : playerData.getManhattanEntityList()) {
			val += entity.getManhattanObj().getEffVal(effId, effParams); 
		}
		return val;
	}

	private int getMedalFactoryEff(EffType effType) {
		try {
			return getPlayerData().getMedalEntity().getFactoryObj().getEffect(effType);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return 0;
	}

	private int getStaffOfficEffVal(EffType effType,EffectParams effParams) {
		return playerData.getStaffOffic().getEffVal(effType, effParams.isStaffPointGreat());
	}

	/**
	 * 获取国家医院建筑所附带的相关作用号值
	 * @param effId
	 * @return
	 */
	private int getNationalHospitalEffVal(int effId) {
		NationbuildingType buildType = NationbuildingType.NATION_HOSPITAL;
		String serverId = playerData.getPlayerEntity().getServerId();
		serverId = GlobalData.getInstance().getMainServerId(serverId);
		if (!GsConfig.getInstance().getServerId().equals(serverId)) {
    		int level = NationService.getInstance().getBuildLevel(serverId, buildType.getNumber());
    		int baseId = buildType.getNumber() * 100 + level;
    		NationConstructionLevelCfg cfg = HawkConfigManager.getInstance().getConfigByKey(NationConstructionLevelCfg.class, baseId);
    		return cfg != null ? cfg.getHospitalPropVal(effId) : 0;
    	}
		
		NationalBuilding building = NationService.getInstance().getNationBuildingByType(buildType);
    	if (building == null) {
    		return 0;
    	}
    	
    	NationConstructionLevelCfg cfg = building.getCurrentLevelCfg();
    	return cfg != null ? cfg.getHospitalPropVal(effId) : 0;
	}
	
	private int getStarWarsEffect(String playerId, int effId) {
		if (!GsApp.getInstance().isInitOK()) {
			return 0;
		}
		
		if (HawkOSOperator.isEmptyString(playerId)) {
			return 0;
		}
		
		return StarWarsOfficerService.getInstance().getEffectValue(playerId, effId);
	}

	public int getGuardBuff(String playerId, int effectId) {
		if (!GsApp.getInstance().isInitOK()) {
			return 0;
		}
		
		if (HawkOSOperator.isEmptyString(playerId)) {
			return 0;
		}
		
		return RelationService.getInstance().getEffectValue(playerId, effectId);
	}
	public int getPresidentBuff(String playerId, int effectId) {
		if (!GsApp.getInstance().isInitOK()) {
			return 0;
		}
		
		return PresidentBuff.getInstance().getEffect(playerId, effectId);
	}
	
	public int getEffectValueInCross(int effId) {
		return playerData.getEffectValueInCross(effId);
	}
	/**
	 * 获取跨服活动相关的加成
	 * @param effId
	 * @return
	 */
	private int getCrossBuff(int effId) {
		if (!GsApp.getInstance().isInitOK()) {
			return 0;
		}
		return CrossActivityService.getInstance().getCrossBuff(playerData.getPlayerId(), effId);
	}
	
	
	/**
	 * 获取军事学院作用号加成
	 * @param effType
	 * @return
	 */
	private int getCollegeEff(EffType effType) {
		if (!GsApp.getInstance().isInitOK()) {
			return 0;
		}
		try {
			return CollegeService.getInstance().getCollegeEff(playerData.getPlayerId(), effType);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return 0;
	}

	private int getSuperLabVal(EffType effType, int superLab) {
		if (superLab == 0) {
			superLab = playerData.getPlayerEntity().getLaboratory();
		}
		try {
			for (LaboratoryEntity lab : playerData.getLaboratoryEntityList()) {
				if (lab.getPageIndex() == superLab) {
					return lab.getLabObj().getEffVal(effType);
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return 0;
	}

	/**
	 * 获取要塞作用号
	 */
	public int getFortressVal(EffType effType) {
		return playerData.getFortressEffect(effType.getNumber());
	}
	
	/**委任中的英雄增益*/
	protected int getHerosOfficeEffVal(EffType effType, EffectParams effParams) {
		if (!GsApp.getInstance().isInitOK()) {
			return 0;
		}
		if (PlayerHero.isForbidEff(effType)) {
			return 0; // 以上作用号不允许出现在英雄身上
		}
		
		try {
			int val = 0;
			for (HeroEntity hero : playerData.getHeroEntityList()) {
				PlayerHero heroObj = hero.getHeroObj();
				if (Objects.nonNull(heroObj)) {
					val = val + heroObj.getOfficeEffVal(effType, effParams);
				}
			}
			return val;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return 0;
	}
	
	private int getPlantTechEffVal(EffType effType) {
		try {
			int val = 0;
			for(PlantTechEntity enttiy: playerData.getPlantTechEntities()){
				val += enttiy.getTechObj().getEffValMap().getOrDefault(effType, 0);
			}
			return val;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return 0;
	}
	
	private int getPlantSchoolEffVal(EffType effType) {
		try {
			return playerData.getPlantSoldierSchoolEntity().getPlantSchoolObj().getEffVal(effType);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return 0;
	}
	
	private int getPlantScienceEffVal(EffType effType){
		try {
			return playerData.getPlantScienceEntity().getSciencObj().getEffValMap().getOrDefault(effType, 0);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return 0;
	}
	
	/**
	 * 英雄羁绊增益
	 */
	public int getHerosCollectEffVal(EffType effType) {
		if (!GsApp.getInstance().isInitOK()) {
			return 0;
		}
		if (PlayerHero.isForbidEff(effType)) {
			return 0; // 以上作用号不允许出现在英雄身上
		}
		try {
			int val = 0;
			for (HeroEntity hero : playerData.getHeroEntityList()) {
				PlayerHero heroObj = hero.getHeroObj();
				if (Objects.nonNull(heroObj) && heroObj.getHeroCollect().isActive()) {
					val = val + heroObj.getHeroCollect().getEffects().getOrDefault(effType, 0);
				}
			}
			return val;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return 0;
	}
	
	/**
	 * 获取英雄档案作用号
	 * @param effType
	 * @return
	 */
	public int getHeroArchivesEffVal(int effId) {
		int val = 0;
		try {
			HeroArchivesEntity entity = playerData.getHeroArchivesEntity();
			for (Entry<Integer, Integer> archive : entity.getArchiveInfo().entrySet()) {
				HeroArchivesContentCfg cfg = HawkConfigManager.getInstance().getConfigByKey(HeroArchivesContentCfg.class, archive.getKey());
				for (int i = 1; i <= archive.getValue(); i++) {
					val += cfg.getEff(i).getOrDefault(effId, 0);
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return val;
	}
	
	public int getHeroMarchEffVal(EffType effType, EffectParams effParams) {
		if (!GsApp.getInstance().isInitOK()) {
			return 0;
		}
		if (PlayerHero.isForbidEff(effType)) {
			return 0; // 以上作用号不允许出现在英雄身上
		}
		List<Integer> heroIds = effParams.getHeroIds();
		try {
			int effHero = 0;
			if (Objects.nonNull(heroIds) && !heroIds.isEmpty()) {
				List<HeroEntity> heros = playerData.getHeroEntityByCfgId(heroIds);
				for (HeroEntity heroEntity : heros) {
					effHero = effHero + heroEntity.getHeroObj().getBattleEffect(effType,effParams); // 7 英雄
				}
			}
			return effHero;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return 0;
	}
	
	/**委任中的超级兵增益*/
	private int getSuperSoldierOfficeEffVal(EffType effType) {
		try {
			int val = 0;
			for (SuperSoldierEntity ssentity : playerData.getSuperSoldierEntityList()) {
				SuperSoldier soldierObj = ssentity.getSoldierObj();
				if (Objects.nonNull(soldierObj)) {
					val = val + soldierObj.getOfficeEffVal().getOrDefault(effType, 0);
				}
			}
			return val;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return 0;
	}
	
	public int getSuperSoldierMarchEffVal(EffType effType, int ssoldierId) {
		try {
			int effSS = 0;
			if (ssoldierId > 0) {
				List<SuperSoldierEntity> ssoldierList = playerData.getSuperSoldierEntityList();
				for (SuperSoldierEntity soldier : ssoldierList) {
					if (soldier.getSoldierId() == ssoldierId) {
						effSS = effSS + soldier.getSoldierObj().battleEffect().getOrDefault(effType, 0);
						break;
					}
				}
			}
			return effSS;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return 0;
	}

	private int getGlobalEffect(int effId) {
		if (!GsApp.getInstance().isInitOK()) {
			return 0;
		}
		return BuffService.getInstance().getEffectValue(effId);
	}

	/**
	 * 获取buff值
	 * @param effId
	 * @param targetId
	 * @return
	 */
	private int getEffectStatus(int effId, String targetId) {
		StatusDataEntity playerStatusEntity = playerData.getStatusById(effId, targetId);
		if (playerStatusEntity != null && playerStatusEntity.getEndTime() > HawkTime.getMillisecond()) {
			return playerStatusEntity.getVal();
		}

		return 0;
	}

	private int getEffectVip(int effId) {
		// 检查VIP是否激活
		if (!playerData.getVipActivated()) {
			return 0;
		}
		if (!effectVip.containsKey(effId)) {
			return 0;
		}

		return effectVip.get(effId);
	}
	
	private int getEffectPlat(int effId) {
		if (HawkOSOperator.isEmptyString(playerData.getPlayerId())) {
			return 0;
		}
		
		AccountRoleInfo roleInfo = GlobalData.getInstance().getAccountRoleInfo(playerData.getPlayerId()); 
		// QQ超级会员
		if (roleInfo != null && roleInfo.getQqSVIPLevel() > 0) {
			int effSVIP = getEffectSVIP(effId);
			if (effSVIP > 0) {
				return effSVIP;
			}
		}
		
		// 不是由游戏中心启动
		if (playerData.getPlayerEntity().getLoginWay() == LoginWay.COMMON_LOGIN_VALUE) {
			return 0;
		}
		
		if (!effectPlat.containsKey(effId)) {
			return 0;
		}

		return effectPlat.get(effId);
	}
	
	private int getEffectSVIP(int effId) {
		if (!effectSVIP.containsKey(effId)) {
			return 0;
		}

		return effectSVIP.get(effId);
	}

	private int getEffectTalent(int talentType, int effId) {
		if (talentType == TalentType.TALENT_TYPE_DEFAULT_VALUE) {
			talentType = playerData.getPlayerEntity().getTalentType();
		}
		
		if (!effectTalent.containsKey(talentType)) {
			return 0;
		}

		Map<EffType, Integer> talentMap = effectTalent.get(talentType);
		if (talentMap == null) {
			return 0;
		}
		
		EffType effType = EffType.valueOf(effId);
		if (effType == null) {
			return 0;
		}
		
		if (!talentMap.containsKey(effType)) {
			return 0;
		}
		return talentMap.get(effType);
	}

	public int getEffectTech(int effId) {
		if (!effectTech.containsKey(effId)) {
			return 0;
		}

		return effectTech.get(effId);
	}
	
	public int getEffectCrossTech(int effId) {
		if (!effectCrossTech.containsKey(effId)) {
			return 0;
		}
		
		return effectCrossTech.get(effId);
	}

	private int getEffectEquip(int effId) {
		if (!effectEquip.containsKey(effId)) {
			return 0;
		}

		return effectEquip.get(effId);
	}

	public int getEffectEquipResearch(int effId) {
		return equipResearch.getOrDefault(effId, 0);
	}
	
	private int getEffectSuperWeapon(int effId) {
		if (!effectSuperWeapon.containsKey(effId)) {
			return 0;
		}

		return effectSuperWeapon.get(effId);		
	}
	
	private int getEffectMilitary(int effId) {
		if (!effectMilitaryRank.containsKey(effId)) {
			return 0;
		}
		return effectMilitaryRank.get(effId);
	}

	private int getEffectDress(int effId) {
		if (!effectDress.containsKey(effId)) {
			return 0;
		}
		return effectDress.get(effId);
	}

	/**
	 * 获取装扮作用号
	 * @param effId
	 * @param effParams
	 * @return
	 * 
	 * 20230607修改,行军出征可以携带自定义装扮,重载原getEffectDress(int effId)接口
	 */
	private int getEffectDress(int effId, EffectParams effParams) {
		try {
			// effParams为null,获取玩家身上的装扮做用号
			if (effParams == null) {
				return getEffectDress(effId);
			}
			// dressList为空,获取玩家身上的装扮做用号
			List<Integer> dressList = effParams.getDressList();
			if (dressList.isEmpty()) {
				return getEffectDress(effId);
			}
			
			Map<Integer, Integer> effectDress = new HashMap<Integer, Integer>();
			// 拥有装扮
			BlockingDeque<DressItem> dressInfos = playerData.getDressEntity().getDressInfo();
			for (DressItem dressInfo : dressInfos) {
				DressCfg dressCfg = AssembleDataManager.getInstance().getDressCfg(dressInfo.getDressType(), dressInfo.getModelType());
				if (dressCfg == null) {
					continue;
				}
				for (EffectObject eff : dressCfg.getGainEffectList()) {
					int key = eff.getEffectType();
					int val = eff.getEffectValue();
					if (effectDress.containsKey(key)) {
						effectDress.put(key, effectDress.get(key) + val);
					} else {
						effectDress.put(key, val);
					}
				}
			}
			// 使用装扮
			for (int dressId : dressList) {
				DressCfg dressCfg = HawkConfigManager.getInstance().getConfigByKey(DressCfg.class, dressId);
				if (dressCfg != null) {
					for (EffectObject eff : dressCfg.getUseEffectList()) {
						int key = eff.getEffectType();
						int val = eff.getEffectValue();
						if (effectDress.containsKey(key)) {
							effectDress.put(key, effectDress.get(key) + val);
						} else {
							effectDress.put(key, val);
						}
					}
				}
			}
			
			// 使用套装装扮触发作用号
			ConfigIterator<DressGroupCfg> dressGroupIter = HawkConfigManager.getInstance().getConfigIterator(DressGroupCfg.class);
			while (dressGroupIter.hasNext()) {
				DressGroupCfg dressGroupCfg = dressGroupIter.next();
				// 是否触发套装属性
				boolean touch = true;
				for (Integer dressId : dressGroupCfg.getDressIdList()) {
					DressCfg dressCfg = HawkConfigManager.getInstance().getConfigByKey(DressCfg.class, dressId);
					if (dressCfg == null) {
						touch = false;
					} else {
						boolean contains = dressList.contains(dressCfg.getDressId());
						if (!contains) {
							touch = false;
						}
					}
				}
				if (!touch) {
					continue;
				}
				for (EffectObject eff : dressGroupCfg.getEffectList()) {
					int key = eff.getEffectType();
					int val = eff.getEffectValue();
					if (effectDress.containsKey(key)) {
						effectDress.put(key, effectDress.get(key) + val);
					} else {
						effectDress.put(key, val);
					}
				}
			}
			// 装扮点加成作用号
			int dressPoint = WorldPointService.getInstance().getDressPoint(playerData);
			ConfigIterator<DressPointCfg> dressPointCfgIter = HawkConfigManager.getInstance().getConfigIterator(DressPointCfg.class);
			while (dressPointCfgIter.hasNext()) {
				DressPointCfg cfg = dressPointCfgIter.next();
				if (dressPoint < cfg.getNeedPoint()) {
					continue;
				}
				for (EffectObject eff : cfg.getEffectList()) {
					int key = eff.getEffectType();
					int val = eff.getEffectValue();
					if (effectDress.containsKey(key)) {
						effectDress.put(key, effectDress.get(key) + val);
					} else {
						effectDress.put(key, val);
					}
				}
			}
			return effectDress.getOrDefault(effId, 0);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return 0;
	}
	
	private int getEffectArmour(ArmourSuitType armourSuitType, EffType type) {
		if (!GsApp.getInstance().isInitOK()) {
			return 0;
		}
		
		if (type == null) {
			return 0;
		}
		
		int armourSuit = playerData.getPlayerEntity().getArmourSuit();
		if (armourSuitType != null && armourSuitType != ArmourSuitType.ARMOUR_NONE) {
			armourSuit = armourSuitType.getNumber();
		}
		
		Map<EffType, Integer> effectMap = effectArmour.get(armourSuit);
		if (effectMap == null) {
			return 0;
		}
		
		if (!effectMap.containsKey(type)) {
			return 0;
		}
		return effectMap.get(type);
	}
	
	private int getEffectBuilding(int effId) {
		if (!effectBuilding.containsKey(effId)) {
			return 0;
		}

		return effectBuilding.get(effId);
	}

	/**
	 * 获取联盟领地指定作用号的作用效果
	 * @param effId
	 * @return
	 */
	private int getEffectManor(int effId) {
		// 玩家不在线时加载玩家数据，不会主动加载effectManor数据，所以这里要做处理
		if (effectManor.isEmpty()) {
			initEffectManor();
			// 防止没完没了地重复加载，这里给一个无效值填充map
			if (effectManor.isEmpty()) {
				effectManor.put(0, 0);
			}
		}
		
		if (!effectManor.containsKey(effId)) {
			return 0;
		}
		return effectManor.get(effId);
	}

	/**
	 * 获取作用号结束时间
	 * @return 结束时间
	 */
	public long getStatusEndTime(EffType effType) {
		int effId = effType.getNumber();
		StatusDataEntity playerStatusEntity = playerData.getStatusById(effId);
		if (playerStatusEntity != null) {
			return playerStatusEntity.getEndTime();
		}
		return 0;
	}

	// ==============================其它常用封装==============================

	/**
	 * 获取采集作用号数据
	 * 
	 * @return 作用值
	 */
	public int getCollectEff() {
		int effVal1 = getEffVal(EffType.RES_COLLECT);
		int effVal2 = getEffVal(EffType.RES_COLLECT_BUF);
		int effVal3 = getEffVal(EffType.RES_COLLECT_SKILL);
		int effVal4 = getEffVal(EffType.RES_COLLECT_NEW_SERVER) + getEffVal(EffType.EFF_520);
		return effVal1 + effVal2 + effVal3 + effVal4;
	}
	
	/**
	 *  同步资源田增产专项作用号数据
	 *  
	 * @param player
	 * @param changedEffTypes 发生变化的作用号类型
	 */
	public void syncResOutputBuff(Player player, List<Integer> changedEffTypes) {
		if (changedEffTypes.isEmpty()) {
			return;
		}
		
		try {
			Set<Integer> allBuffTypeSet = ConstProperty.getInstance().getResOutputBuffAllSet();
			Set<Integer> buffToAllSet = new HashSet<>();
			// 求参数中指定作用号与全局作用号（对所有资源田生效）的交集 
			buffToAllSet.addAll(changedEffTypes);
			buffToAllSet.retainAll(allBuffTypeSet);

			Set<Integer> buildBuffSet = new HashSet<>();
			ResOutputAddBuffPB.Builder syncBuilder = ResOutputAddBuffPB.newBuilder();
			// 黄金田
			Set<Integer> goldoreBuffSet = ConstProperty.getInstance().getResOutputBuffGoldoreSet();
			boolean result = buildBuildingBuff(buildBuffSet, goldoreBuffSet, buffToAllSet, changedEffTypes, BuildingType.ORE_REFINING_PLANT, syncBuilder);
			
			// 油井
			Set<Integer> oilBuffSet = ConstProperty.getInstance().getResOutputBuffOilSet();
			result |= buildBuildingBuff(buildBuffSet, oilBuffSet, buffToAllSet, changedEffTypes, BuildingType.OIL_WELL, syncBuilder);
			
			// 铀矿厂 
			if (WorldMapConstProperty.getInstance().getCanCollectSteelLevel() <= playerData.getConstructionFactoryLevel()) {
				Set<Integer> steelBuffSet = ConstProperty.getInstance().getResOutputBuffSteelSet();
				result |= buildBuildingBuff(buildBuffSet, steelBuffSet, buffToAllSet, changedEffTypes, BuildingType.STEEL_PLANT, syncBuilder);
			}
			
			// 合金厂
			if (WorldMapConstProperty.getInstance().getCanCollectTombarthiteLevel() <= playerData.getConstructionFactoryLevel()) {
				Set<Integer> tombarBuffSet = ConstProperty.getInstance().getResOutputBuffTombarthiteSet();
				result |= buildBuildingBuff(buildBuffSet, tombarBuffSet, buffToAllSet, changedEffTypes, BuildingType.RARE_EARTH_SMELTER, syncBuilder);
			}
			
			if (result) {
				player.sendProtocol(HawkProtocol.valueOf(HP.code.RES_OUTPUT_BUFF_SYNC_S, syncBuilder));
			}
			
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 大本升级时处罚新解锁资源建筑的作用号加成
	 * 
	 * @param player
	 */
	public void syncNewUnlockedBuildingBuff(Player player) {
		int cityLevel = player.getCityLevel();
		
		if (cityLevel != WorldMapConstProperty.getInstance().getCanCollectSteelLevel() && 
				cityLevel != WorldMapConstProperty.getInstance().getCanCollectTombarthiteLevel()) {
			return;
		}
		
		Set<Integer> buffToAllSet = ConstProperty.getInstance().getResOutputBuffAllSet(); // 300,301
		
	    //  所有资源田建筑
		ResOutputAddBuffPB.Builder syncBuilder = ResOutputAddBuffPB.newBuilder();
		// 登录时同步所有增产作用号，不存在发生变化的作用号一说
		List<Integer> changedEffTypes = Collections.emptyList();
		
		Set<Integer> buildBuffSet = new HashSet<>();
				
		// 铀矿厂 
		if (WorldMapConstProperty.getInstance().getCanCollectSteelLevel() == cityLevel) {
			Set<Integer> steelBuffSet = ConstProperty.getInstance().getResOutputBuffSteelSet();
			buildBuildingBuff(buildBuffSet, steelBuffSet, buffToAllSet, changedEffTypes, BuildingType.STEEL_PLANT, syncBuilder);
		}
		
		// 合金厂
		if (WorldMapConstProperty.getInstance().getCanCollectTombarthiteLevel() == cityLevel) {
			Set<Integer> tombarBuffSet = ConstProperty.getInstance().getResOutputBuffTombarthiteSet();
			buildBuildingBuff(buildBuffSet, tombarBuffSet, buffToAllSet, changedEffTypes, BuildingType.RARE_EARTH_SMELTER, syncBuilder);
		}
		
		player.sendProtocol(HawkProtocol.valueOf(HP.code.RES_OUTPUT_BUFF_SYNC_S, syncBuilder));
	}
	
	/**
	 * 同步资源田增产专项作用号数据
	 * 
	 * @param player
	 */
	public void syncResOutputBuff(Player player) {
		
		pushedResOutputBuffData.clear();
		
		Set<Integer> buffToAllSet = ConstProperty.getInstance().getResOutputBuffAllSet(); // 300,301	
		try {
			//  所有资源田建筑
			ResOutputAddBuffPB.Builder syncBuilder = ResOutputAddBuffPB.newBuilder();
			// 登录时同步所有增产作用号，不存在发生变化的作用号一说
			List<Integer> changedEffTypes = Collections.emptyList();
			
			Set<Integer> buildBuffSet = new HashSet<>();
			
			// 黄金田  302,303
			Set<Integer> goldoreBuffSet = ConstProperty.getInstance().getResOutputBuffGoldoreSet();
			buildBuildingBuff(buildBuffSet, goldoreBuffSet, buffToAllSet, changedEffTypes, BuildingType.ORE_REFINING_PLANT, syncBuilder);
			
			// 油井 304,305
			Set<Integer> oilBuffSet = ConstProperty.getInstance().getResOutputBuffOilSet();
			buildBuildingBuff(buildBuffSet, oilBuffSet, buffToAllSet, changedEffTypes, BuildingType.OIL_WELL, syncBuilder);
			
			// 铀矿厂 
			if (WorldMapConstProperty.getInstance().getCanCollectSteelLevel() <= playerData.getConstructionFactoryLevel()) {
				Set<Integer> steelBuffSet = ConstProperty.getInstance().getResOutputBuffSteelSet();
				buildBuildingBuff(buildBuffSet, steelBuffSet, buffToAllSet, changedEffTypes, BuildingType.STEEL_PLANT, syncBuilder);
			}
			
			// 合金厂
			if (WorldMapConstProperty.getInstance().getCanCollectTombarthiteLevel() <= playerData.getConstructionFactoryLevel()) {
				Set<Integer> tombarBuffSet = ConstProperty.getInstance().getResOutputBuffTombarthiteSet();
				buildBuildingBuff(buildBuffSet, tombarBuffSet, buffToAllSet, changedEffTypes, BuildingType.RARE_EARTH_SMELTER, syncBuilder);
			}
			
			player.sendProtocol(HawkProtocol.valueOf(HP.code.RES_OUTPUT_BUFF_SYNC_S, syncBuilder));
			
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
	}
	
	/**
	 * 获取资源田建筑的增产作用号数据
	 * 
	 * @param buildBuffAllSet  存放对该类资源田本身有效的所有作用号（包括全局作用号），这里只是拿来做运算的容器
	 * @param buildBuffSet     只针对资源田类型本身的作用号（const.xml中配置）
	 * @param buffToAllSet     对所有类型资源田均有效的作用号
	 * @param effTypes         发生变化的作用号类型
	 * @param buildingType     建筑类型
	 * @param addBuffBuilder   最后要同步的资源田增产作用号数据
	 * @return
	 */
	private boolean buildBuildingBuff(Set<Integer> buildBuffAllSet, Set<Integer> buildBuffSet, Set<Integer> buffToAllSet, 
			List<Integer> effTypes, BuildingType buildingType, ResOutputAddBuffPB.Builder addBuffBuilder) {
		// 先要请款容器，防止污染
		buildBuffAllSet.clear();
		
		// 登录是算所有作用号，effTypes为空
		if (effTypes.isEmpty()) {
			buildBuffAllSet.addAll(buildBuffSet);
		} else {
			// 求参数中指定作用号与特定资源田类型作用号的交集 
			buildBuffAllSet.addAll(effTypes);
			buildBuffAllSet.retainAll(buildBuffSet);
		}
		
		// 再算上全局作用号
		buildBuffAllSet.addAll(buffToAllSet);
		BuildingBuff buildingBuff = buildBuildingBuffUnit(buildBuffAllSet, buildingType, effTypes.isEmpty());
		if (buildingBuff != null) {
			addBuffBuilder.addBuff(buildingBuff);
			return true;
		}
		
		return false;
	}
	
	/**
	 * 获取资源田建筑的增产作用号数据
	 * 
	 * @param allBuffTable
	 * @param buildingBuffSet
	 * @param building
	 * @return
	 */
	private BuildingBuff buildBuildingBuffUnit(Collection<Integer> buildingBuffSet, BuildingType building, boolean isLogin) {
		
		Table<ResOutputBuffUnitType, Object, ResOutputBuffVal> effData = HashBasedTable.create();
		for (int effId : buildingBuffSet) {
			getResOutputUnitBuff(effId, building, effData, isLogin);
		}
		
		if (effData.isEmpty()) {
			return null;
		}
		
		// 2 每种资源田建筑
		boolean result = false;
		BuildingBuff.Builder buildingBuff = BuildingBuff.newBuilder();
		
		for (ResOutputBuffUnitType unit : ResOutputBuffUnitType.values()) {
			if (!effData.containsRow(unit)) {
				continue;
			}
			
			ResOutputBuffUnit.Builder effUnitBuilder = ResOutputBuffUnit.newBuilder();
			boolean innerResult = false;
			for (ResOutputBuffVal buffVal : effData.row(unit).values()) {
				// 作用号的值为0，且为非登录状态，需要判断是否更新数据
				if (buffVal.effVal == 0 && !isLogin) {
					String columnKey = Joiner.on("_").join(unit.getNumber(), buffVal.key());
					if (!pushedResOutputBuffData.contains(building, columnKey)) {
						continue;
					}
					
					pushedResOutputBuffData.remove(building, columnKey);
				}
				
				// 作用号值大于0时，判断要不要更新数据
				if (buffVal.effVal > 0) {
					String columnKey = Joiner.on("_").join(unit.getNumber(), buffVal.key());
					if (pushedResOutputBuffData.contains(building, columnKey) && pushedResOutputBuffData.get(building, columnKey) == buffVal.effVal) {
						continue;
					}
					
					pushedResOutputBuffData.put(building, columnKey, buffVal.effVal);
				}
				
				innerResult = true;
				ResOutputBuffNoId.Builder buffNoId = ResOutputBuffNoId.newBuilder();
				buffNoId.setBuffType(buffVal.effId);
				buffNoId.setBuffVal(buffVal.effVal);
				
				if (HawkOSOperator.isEmptyString(buffVal.buildId)) {
					effUnitBuilder.addBuffDataNoId(buffNoId.build());
				} else {
					ResOutputBuffWithId.Builder buffWithId = ResOutputBuffWithId.newBuilder();
					buffWithId.setBuffVal(buffNoId.build());
					buffWithId.setTargetId(buffVal.buildId);
					effUnitBuilder.addBuffDataWithId(buffWithId.build());
				}
			}
			
			result |= innerResult;
			if (innerResult) {
				effUnitBuilder.setBuffUnit(unit);
				buildingBuff.addUnitBuff(effUnitBuilder.build());
			}
		}
		
		if (result) {
			buildingBuff.setBuilding(building);
			return buildingBuff.build();
		}
		
		return null;
	}
	
	/**
	 * 获取资源田增产专项的加成
	 * 
	 * @param index
	 * @param buffMap
	 */
	private void getResOutputUnitBuff(int effId, BuildingType buildingType, Table<ResOutputBuffUnitType, Object, ResOutputBuffVal> effData, boolean isLogin) {
		// 1、贵族加成
		int vipEffVal = getEffectVip(effId);
		if (isLogin || vipEffVal > 0) {
			effData.put(ResOutputBuffUnitType.VIP_BUFF, effId, new ResOutputBuffVal(effId, vipEffVal));
		}
		
		// 2、天赋加成
		int talentEffVal = getEffectTalent(TalentType.TALENT_TYPE_DEFAULT_VALUE, effId);
		if (isLogin || talentEffVal >= 0) {
			effData.put(ResOutputBuffUnitType.TALENT_BUFF, effId, new ResOutputBuffVal(effId, talentEffVal));
		}
		
		// 3、科技加成
		int techEffVal = getEffectTech(effId);
		if (isLogin || techEffVal > 0) {
			effData.put(ResOutputBuffUnitType.TECH_BUFF, effId, new ResOutputBuffVal(effId, techEffVal));
		}
		
		// 4、联盟领地加成
		int manorEffVal = getEffectManor(effId);
		if (isLogin || manorEffVal > 0 || pushedResOutputBuffData.contains(buildingType, Joiner.on("_").join(ResOutputBuffUnitType.GUILD_MANOR_BUFF_VALUE, effId))) {
			effData.put(ResOutputBuffUnitType.GUILD_MANOR_BUFF, effId, new ResOutputBuffVal(effId, manorEffVal));
		}
		
		// 5、平台特权加成
		int platEffVal = getEffectPlat(effId);
		if (isLogin || platEffVal >= 0 || pushedResOutputBuffData.contains(buildingType, Joiner.on("_").join(ResOutputBuffUnitType.PLAT_BUFF_VALUE, effId))) {
			effData.put(ResOutputBuffUnitType.PLAT_BUFF, effId, new ResOutputBuffVal(effId, platEffVal));
		}
		
		// 6、英雄加成
		int heroEffVal = getHerosOfficeEffVal(EffType.valueOf(effId),EffectParams.getDefaultVal());
		if (isLogin || heroEffVal >= 0 || pushedResOutputBuffData.contains(buildingType, Joiner.on("_").join(ResOutputBuffUnitType.HERO_BUFF_VALUE, effId))) {
			effData.put(ResOutputBuffUnitType.HERO_BUFF, effId, new ResOutputBuffVal(effId, heroEffVal));
		}
		
		// 7、道具加成
		int toolTotalEffVal = 0;
		boolean toolEffAdd = false;
		List<StatusDataEntity> list = playerData.getStatusListById(effId);
		for (StatusDataEntity entity : list) {
			int toolEffVal = getEffectStatus(effId, entity.getTargetId());
			toolTotalEffVal += toolEffVal;
			if (isLogin || toolEffVal >= 0 || pushedResOutputBuffData.contains(buildingType, Joiner.on("_").join(ResOutputBuffUnitType.TOOL_BUFF_VALUE, entity.getTargetId()))) {
				toolEffAdd = true;
				effData.put(ResOutputBuffUnitType.TOOL_BUFF, entity.getTargetId(), new ResOutputBuffVal(effId, toolEffVal, entity.getTargetId()));
			}
		}
		
		if (!toolEffAdd) {
			toolTotalEffVal = getEffectStatus(effId, null);
			if (isLogin || toolTotalEffVal >= 0 || pushedResOutputBuffData.contains(buildingType, Joiner.on("_").join(ResOutputBuffUnitType.TOOL_BUFF_VALUE, effId))) {
				effData.put(ResOutputBuffUnitType.TOOL_BUFF, effId, new ResOutputBuffVal(effId, toolTotalEffVal));
			}
		}
		
		// 8、其他加成
		int totalEffVal = 0;
		Set<Integer> buffToAll = ConstProperty.getInstance().getResOutputBuffAllSet();
		if (buffToAll.contains(effId)) {
			totalEffVal = getEffVal(EffType.valueOf(effId), null, EffectParams.getDefaultVal());
		} else {
			List<StatusDataEntity> entityList = playerData.getStatusListById(effId);
			if (entityList.isEmpty()) {
				totalEffVal = getEffVal(EffType.valueOf(effId), null, EffectParams.getDefaultVal());
			} else {
				for (StatusDataEntity entity : entityList) {
					totalEffVal += getEffVal(EffType.valueOf(effId), entity.getTargetId());
				}
			}
		}
		
		int otherEffVal = totalEffVal - vipEffVal - talentEffVal - techEffVal - manorEffVal - platEffVal - toolTotalEffVal - heroEffVal;
		if (isLogin || otherEffVal > 0 || pushedResOutputBuffData.contains(buildingType, Joiner.on("_").join(ResOutputBuffUnitType.OTHER_VALUE, effId))) {
			effData.put(ResOutputBuffUnitType.OTHER, effId, new ResOutputBuffVal(effId, otherEffVal));
		}
	}

	public int getLifeTimeBuff(int effId){

		return lifeTimeCard.getOrDefault(effId,0);
	}

	public int getStarExploreBuff(int effId){
		CommanderEntity entity = playerData.getCommanderEntity();
		ArmourStarExplores starExplores = entity.getStarExplores();
		return starExplores.getEffValue(effId);
	}

	public int getActivityBuff(int effId){
		String guildId = GuildService.getInstance().getPlayerGuildId(playerData.getPlayerId());
		if(!HawkOSOperator.isEmptyString(guildId)){
			return ActivityManager.getInstance().getBuff(ActivityType.GUILD_BACK.intValue(), guildId, effId);
		}
		return 0;
	}

	public int getXqhxBuff(EffType effId){
		return xqhxTalent.getOrDefault(effId, 0);
	}

	public int getHomeLandBuff(EffType effType) {
		try {
			return playerData.getHomeLandEntity().getComponent().getEffect(effType);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return 0;
	}
}
