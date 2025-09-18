package com.hawk.game.player.supersoldier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.hawk.game.entity.CommanderEntity;
import com.hawk.game.player.cache.PlayerDataKey;
import com.hawk.serialize.string.SerializeHelper;
import org.apache.commons.lang.StringUtils;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.helper.HawkAssert;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;
import org.hawk.tuple.HawkTuple3;

import com.alibaba.fastjson.JSONArray;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.hawk.game.config.SuperSoldierCfg;
import com.hawk.game.config.SuperSoldierLevelCfg;
import com.hawk.game.config.SuperSoldierOfficeCfg;
import com.hawk.game.config.SuperSoldierSkinCfg;
import com.hawk.game.config.SuperSoldierStarLevelCfg;
import com.hawk.game.entity.StatusDataEntity;
import com.hawk.game.entity.SuperSoldierEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.player.Player;
import com.hawk.game.player.supersoldier.skill.ISuperSoldierSkill;
import com.hawk.game.player.supersoldier.skill.SuperSoldierSkillFactory;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.SuperSoldier.PBSuperSoldierEffect;
import com.hawk.game.protocol.SuperSoldier.PBSuperSoldierInfo;
import com.hawk.game.protocol.SuperSoldier.PBSuperSoldierSkin;
import com.hawk.game.protocol.SuperSoldier.PBSuperSoldierState;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.log.LogConst.PowerChangeReason;
/**
 * 
 * @author lwt
 * @date 2017年7月19日
 */
public class SuperSoldier {

	private final SuperSoldierEntity superSoldierEntity;
	private ImmutableList<SuperSoldierSkillSlot> skillSlots;
	private ImmutableList<SuperSoldierSkillSlot> passiveSkillSlots;
	/** 官职做用号 */
	private ImmutableMap<EffType, Integer> officeEffVal;
	/** 出征做用号 */
	private ImmutableMap<EffType, Integer> battleEffVal;
	private boolean efvalLoad;
	private PBSuperSoldierInfo soldierInfoPB;
	/** 赋能 */
	private SuperSoldierEnergy soldierEnergy;

	private Set<Integer> unlockSkinSet;
	
	protected SuperSoldier(SuperSoldierEntity dbEntity) {
		this.superSoldierEntity = dbEntity;
	}
	
	public boolean isAnyWhereUnlock(boolean isNew) {
		if(isNew){
			if(superSoldierEntity.getAnyWhereUnlock() > 0){
				return true;
			}
			return !getPlayerUnlockSkinSet().isEmpty();
		}else {
			return superSoldierEntity.getAnyWhereUnlock() > 0;
		}

	}

	public int getStar() {
		return superSoldierEntity.getStar();
	}

	public int getStep() {
		return superSoldierEntity.getStep();
	}

	public int getOffice() {
		return superSoldierEntity.getOffice();
	}

	public int getSkin() {
		return superSoldierEntity.getSkin();
	}

	public int getCityDefense() {
		return superSoldierEntity.getCityDefense();
	}

	public SuperSoldierCfg getConfig() {
		SuperSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(SuperSoldierCfg.class, superSoldierEntity.getSoldierId());
		if (Objects.isNull(cfg)) {
			throw new NullPointerException("超级兵配置缺失 SuperSoldierCfg = " + superSoldierEntity.getSoldierId());
		}
		return cfg;
	}

	public void loadEffVal() {
		SuperSoldierOfficeCfg officeCfg = HawkConfigManager.getInstance().getConfigByKey(SuperSoldierOfficeCfg.class, getOffice());
		Map<EffType, Integer> __officeEffVal = new HashMap<>(10);
		Map<EffType, Integer> __battleEffVal = new HashMap<>(10);
		List<SuperSoldierSkillSlot> allSkillSlots = new ArrayList<>();
		allSkillSlots.addAll(skillSlots);
		allSkillSlots.addAll(passiveSkillSlots);
		for (SuperSoldierSkillSlot slot : allSkillSlots) {
			if (!slot.isUnLock()) {
				continue;
			}
			if (Objects.isNull(slot.getSkill())) {
				continue;
			}
			ISuperSoldierSkill skill = slot.getSkill();
			if (skill.getCfg().getMarchUsed() == 1) {
				mergeEffval(__battleEffVal, skill.effectVal());
				continue;
			}
			// 是否官职生效
			if (Objects.nonNull(officeCfg) && skill.getCfg().getOfficeIdList().contains(getOffice())) {
				mergeEffval(__officeEffVal, skill.effectVal());
			}
		}
		
		SuperSoldierCfg cfg = getConfig();
		if (isAnyWhereUnlock(false)) {
			if (cfg.getSupersoldierClass() == 1) {  // 出征型
				mergeEffval(__battleEffVal, cfg.getUnlockAnyWhereEffectList());
			} else if (Objects.nonNull(officeCfg)) {// 生产型
				mergeEffval(__officeEffVal, cfg.getUnlockAnyWhereEffectList());
			}
		}
		if(cfg.getPreSupersoldierId() > 0){
			for(int skinId : getPlayerUnlockSkinSet()){
				SuperSoldierSkinCfg skinCfg = HawkConfigManager.getInstance().getConfigByKey(SuperSoldierSkinCfg.class, skinId);
				if(skinCfg == null){
					continue;
				}
				if (cfg.getSupersoldierClass() == 1) {  // 出征型
					mergeEffval(__battleEffVal, skinCfg.getEffectList());
				} else if (Objects.nonNull(officeCfg)) {// 生产型
					mergeEffval(__officeEffVal, skinCfg.getEffectList());
				}
			}
		}

		if (cfg.getSupersoldierClass() == 1) {  // 出征型
			mergeEffval(__battleEffVal, soldierEnergy.getEffect());
		} else if (Objects.nonNull(officeCfg)) {// 生产型
			mergeEffval(__officeEffVal, soldierEnergy.getEffect());
		}
		
		officeEffVal = ImmutableMap.copyOf(__officeEffVal);
		battleEffVal = ImmutableMap.copyOf(__battleEffVal);
		efvalLoad = true;
	}

	private void mergeEffval(Map<EffType, Integer> __effVal, List<PBSuperSoldierEffect> effValList) {
		for (PBSuperSoldierEffect effVal : effValList) {
			EffType type = EffType.valueOf(effVal.getEffectId());
			if (type == null) {
				continue;
			}

			__effVal.merge(type, effVal.getValue(), (v1, v2) -> v1 + v2);
		}
	}

	/**
	 * 战斗力
	 * 
	 * @return
	 */
	public int power() {
		SuperSoldierStarLevelCfg starLevelCfg = HawkConfigManager.getInstance().getCombineConfig(SuperSoldierStarLevelCfg.class, getCfgId(), getStar(), getStep());
		SuperSoldierCfg soldiercfg = getConfig();
		double soldierpow = soldiercfg.getPowerCoe().first + soldiercfg.getPowerCoe().second * getLevel();
		double starPow = starLevelCfg.getStarPower();
		double skillpow = passiveSkillSlots.stream().mapToDouble(SuperSoldierSkillSlot::power).sum() + skillSlots.stream().mapToDouble(SuperSoldierSkillSlot::power).sum();
		double skinPow = 0;
		int unlockAnyWherePower = isAnyWhereUnlock(false) ? soldiercfg.getUnlockAnyWherePower() : 0;
//		if (getSkin() != 0) {
//			SuperSoldierSkinCfg skinCfg = HawkConfigManager.getInstance().getConfigByKey(SuperSoldierSkinCfg.class, getSkin());
//			skillpow = Objects.nonNull(skinCfg) ? skinCfg.getPowerSkin() : 0;
//		}
		int energyPower = soldierEnergy.getPower();
		int skinPower = 0;
		if(soldiercfg.getPreSupersoldierId() > 0){
			for(int skinId : getPlayerUnlockSkinSet()){
				SuperSoldierSkinCfg skinCfg = HawkConfigManager.getInstance().getConfigByKey(SuperSoldierSkinCfg.class, skinId);
				if(skinCfg == null){
					continue;
				}
				skinPower += skinCfg.getPowerSkin();
			}
		}
		
		return (int) (soldierpow + starPow + skillpow + skinPow + unlockAnyWherePower + energyPower + skinPower);
	}

	/**
	 * 取得超级兵身上所有作用号
	 * 
	 * @return
	 */
	public Map<EffType, Integer> battleEffect() {

		return battleEffVal;
	}

	/** 委任中的超级兵增益 */
	public ImmutableMap<EffType, Integer> getOfficeEffVal() {
		return officeEffVal;
	}

	/**
	 * 释放技能
	 * 
	 * @param skillID
	 */
	public void castSkill(int skillID) {
		ISuperSoldierSkill skill = getSkillById(skillID);
		if (Objects.isNull(skill)) {
			return;
		}

		if (skill.isCooling()) {
			skill.cast();
		}
		this.notifyChange();
	}

	public ISuperSoldierSkill getSkillById(int skillID) {
		List<SuperSoldierSkillSlot> allSkill = new ArrayList<>(skillSlots);
		allSkill.addAll(passiveSkillSlots);
		Optional<ISuperSoldierSkill> skillOP = allSkill.stream()
				.filter(SuperSoldierSkillSlot::isUnLock)
				.map(SuperSoldierSkillSlot::getSkill)
				.filter(Objects::nonNull)
				.filter(s -> s.skillID() == skillID)
				.findAny();
		return skillOP.orElse(null);
	}

	/**
	 * 设置超级兵状态
	 */
	private synchronized void setState(PBSuperSoldierState state) {
		superSoldierEntity.setState(state.getNumber());
	}

	/**
	 * 出征
	 */
	public boolean goMarch(IWorldMarch march) {
		if(isAnyWhereUnlock(true)){
			return true;
		}
		setState(PBSuperSoldierState.SUPER_SOLDIER_STATE_MARCH);
		if (soldierInfoPB == null) {
			toPBobj();
		}
		soldierInfoPB = soldierInfoPB.toBuilder().setState(PBSuperSoldierState.SUPER_SOLDIER_STATE_MARCH).build();
		getParent().sendProtocol(HawkProtocol.valueOf(HP.code.UPDATE_SUPER_SOLDIER_INFO, this.toPBobj().toBuilder()));
		return true;
	}

	/**
	 * 出征归来
	 */
	public boolean backFromMarch(IWorldMarch march) {
		setState(PBSuperSoldierState.SUPER_SOLDIER_STATE_FREE);
		if (soldierInfoPB == null) {
			toPBobj();
		}
		soldierInfoPB = soldierInfoPB.toBuilder().setState(PBSuperSoldierState.SUPER_SOLDIER_STATE_FREE).build();
		getParent().sendProtocol(HawkProtocol.valueOf(HP.code.UPDATE_SUPER_SOLDIER_INFO, this.toPBobj().toBuilder()));
		return true;
	}

	/**
	 * 通知超级兵数据有变化
	 */
	public void notifyChange() {
		superSoldierEntity.setChanged(true);
		soldierInfoPB = null;
		// 重新推送所有做用号
		Set<EffType> allEff = new HashSet<>();
		allEff.addAll(this.battleEffVal.keySet());
		allEff.addAll(this.officeEffVal.keySet());
		this.loadEffVal(); // 做号用变更,如删除技能

		allEff.addAll(this.battleEffVal.keySet());
		allEff.addAll(this.officeEffVal.keySet());
		getParent().getEffect().syncEffect(getParent(), allEff.toArray(new EffType[allEff.size()]));
		getParent().sendProtocol(HawkProtocol.valueOf(HP.code.UPDATE_SUPER_SOLDIER_INFO, this.toPBobj().toBuilder()));

		getParent().refreshPowerElectric(PowerChangeReason.SUPER_SOLDIER_ATTR_CHANGE);
		// HawkApp.getInstance().postMsg(getParent(), new HeroChangedMsg(this));
	}

	/**
	 * @return 超级兵状态
	 */
	public synchronized PBSuperSoldierState getState() {
		if(isAnyWhereUnlock(true)){
			return PBSuperSoldierState.SUPER_SOLDIER_STATE_FREE;
		}
		return PBSuperSoldierState.valueOf(superSoldierEntity.getState());
	}

	public static SuperSoldier create(SuperSoldierEntity dbEntity) {
		SuperSoldier soldier = new SuperSoldier(dbEntity);
		soldier.init();
		dbEntity.recordSObj(soldier);
		return soldier;
	}

	protected void init() {
		this.battleEffVal = ImmutableMap.of();
		this.officeEffVal = ImmutableMap.of();
		this.skillSlots = ImmutableList.copyOf(loadSkill());
		this.passiveSkillSlots = ImmutableList.copyOf(loadPassiveSkill());
		this.soldierEnergy = SuperSoldierEnergy.load(this, superSoldierEntity.getEnergySerialized());
		this.unlockSkinSet = SerializeHelper.stringToSet(Integer.class, superSoldierEntity.getSkinSerialized(), SerializeHelper.ATTRIBUTE_SPLIT,null, null);
	}

	private void fixSkin(SuperSoldierCfg spsCfg) {
		if (superSoldierEntity.getAnyWhereUnlock() > 0 && !unlockSkinSet.contains(spsCfg.getUnlockAnyWhereGetSkin()) && spsCfg.getUnlockAnyWhereGetSkin() > 0) {
			unlockSkin(spsCfg.getUnlockAnyWhereGetSkin());
			for (SuperSoldierCfg scfg : HawkConfigManager.getInstance().getConfigIterator(SuperSoldierCfg.class)) {
				if (unlockSkinSet.contains(scfg.getUnlockAnyWhereGetSkin()) && scfg.getSupersoldierId() != spsCfg.getSupersoldierId()) {
					unlockSkinSet.remove(scfg.getUnlockAnyWhereGetSkin());
				}
			}
		}
	}

	private List<SuperSoldierSkillSlot> loadPassiveSkill() {
		if (getConfig().getPassiveSkill() == 0) {
			return new ArrayList<>();
		}

		if (StringUtils.isEmpty(superSoldierEntity.getPassiveSkillSerialized())) {// 新超级兵
			return initPassiveSkill();
		}

		List<SuperSoldierSkillSlot> list = new ArrayList<>();
		JSONArray arr = JSONArray.parseArray(superSoldierEntity.getPassiveSkillSerialized());
		arr.forEach(str -> {
			SuperSoldierSkillSlot slot = new SuperSoldierSkillSlot(this);
			slot.mergeFrom(str.toString());
			if (Objects.nonNull(slot.getSkill())) {
				HawkAssert.notNull(slot.getSkill().getCfg());// 如果属性配置文件出问题,
																// 就重新初始化
			}
			list.add(slot);
		});
		return list;
	}

	private List<SuperSoldierSkillSlot> initPassiveSkill() {
		List<SuperSoldierSkillSlot> list = new ArrayList<>();
		SuperSoldierCfg spsCfg = getConfig();
		int passiveSkill = spsCfg.getPassiveSkill();// ="11101"

		SuperSoldierSkillSlot slot = new SuperSoldierSkillSlot(this);
		slot.setIndex(0);
		slot.setLevel(1);
		ISuperSoldierSkill skill = SuperSoldierSkillFactory.getInstance().createEmptySkill(passiveSkill);
		slot.setSkill(skill);
		list.add(slot);
		return list;
	}

	public String serializPassiveSkill() {
		JSONArray arr = new JSONArray();
		passiveSkillSlots.stream().map(SuperSoldierSkillSlot::serializ).forEach(arr::add);
		return arr.toJSONString();
	}

	private List<SuperSoldierSkillSlot> loadSkill() {
		if (StringUtils.isEmpty(superSoldierEntity.getSkillSerialized())) {// 新超级兵
			return initSkill();
		}

		List<SuperSoldierSkillSlot> list = new ArrayList<>();
		JSONArray arr = JSONArray.parseArray(superSoldierEntity.getSkillSerialized());
		arr.forEach(str -> {
			SuperSoldierSkillSlot slot = new SuperSoldierSkillSlot(this);
			slot.mergeFrom(str.toString());
			if (Objects.nonNull(slot.getSkill())) {
				HawkAssert.notNull(slot.getSkill().getCfg());// 如果属性配置文件出问题,
																// 就重新初始化
			}
			list.add(slot);
		});
		return list;
	}

	private List<SuperSoldierSkillSlot> initSkill() {
		List<SuperSoldierSkillSlot> list = new ArrayList<>();
		SuperSoldierCfg soldierCfg = getConfig();
		for (HawkTuple3<Integer, Integer, Integer> tup : soldierCfg.getSkillBlankList()) {
			SuperSoldierSkillSlot slot = new SuperSoldierSkillSlot(this);
			slot.setIndex(tup.first);
			slot.setLevel(tup.second);
			ISuperSoldierSkill skill = SuperSoldierSkillFactory.getInstance().createEmptySkill(tup.third);
			slot.setSkill(skill);
			list.add(slot);
		}
		return list;
	}

	/** 序列化skill */
	public String serializSkill() {
		JSONArray arr = new JSONArray();
		skillSlots.stream().map(SuperSoldierSkillSlot::serializ).forEach(arr::add);
		return arr.toJSONString();
	}

	public void setSkills(List<ISuperSoldierSkill> skills) {
		throw new UnsupportedOperationException();
	}

	public int getLevel() {
		SuperSoldierCfg supSoldierCfg = getConfig();

		SuperSoldierStarLevelCfg plcfg = HawkConfigManager.getInstance().getCombineConfig(SuperSoldierStarLevelCfg.class, getCfgId(), getStar(), getStep());
		int maxLevel = plcfg.getMaxLevel();

		ConfigIterator<SuperSoldierLevelCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(SuperSoldierLevelCfg.class);
		long level = configIterator.stream()
				.filter(cfg -> cfg.getSupersoldierQuality() == supSoldierCfg.getQualityColor())
				.filter(cfg -> cfg.getLevelUpExp() <= superSoldierEntity.getExp()).count() + 1;
		return (int) Math.min(maxLevel, level);
	}

	/**
	 * 增加超级兵经验值
	 * 
	 * @param exp
	 */
	public void addExp(int exp) {
		// int oldLvl = getLevel();
		int oldExp = superSoldierEntity.getExp();
		superSoldierEntity.setExp(exp + oldExp);
		notifyChange();
		// int newLevel = getLevel();
		// if (newLevel > oldLvl) {
		// // 抛出活动事件
		// ActivityManager.getInstance().postEvent(new HeroChangeEvent(getParent().getId()));
		// }
		// MissionManager.getInstance().postMsg(getParent(), new EventHeroUpgrade(superSoldierEntity.getSoldierId(), oldLvl, newLevel));
		// // 推送礼包
		// HawkTaskManager.getInstance().postMsg(this.getParent().getXid(), new HeroLevelUpMsg(superSoldierEntity.getSoldierId(), oldLvl, newLevel));
	}

	/**
	 * 显示经验条
	 */
	public int showExp() {
		int soldierLevel = getLevel();
		if (soldierLevel == 1) {
			return superSoldierEntity.getExp();
		}
		SuperSoldierLevelCfg precfg = HawkConfigManager.getInstance().getCombineConfig(SuperSoldierLevelCfg.class, soldierLevel - 1, getConfig().getQualityColor());
		return Math.min(levelExp(), superSoldierEntity.getExp() - precfg.getLevelUpExp());
	}

	/**
	 * 当前等级全部经验
	 */
	public int levelExp() {
		int supsLevel = getLevel();
		SuperSoldierLevelCfg cfg = HawkConfigManager.getInstance().getCombineConfig(SuperSoldierLevelCfg.class, supsLevel, getConfig().getQualityColor());
		if (supsLevel == 1) {
			return cfg.getLevelUpExp();
		}
		SuperSoldierLevelCfg precfg = HawkConfigManager.getInstance().getCombineConfig(SuperSoldierLevelCfg.class, supsLevel - 1, getConfig().getQualityColor());

		return cfg.getLevelUpExp() - precfg.getLevelUpExp();

	}

	public int getCfgId() {
		return superSoldierEntity.getSoldierId();
	}

	public Player getParent() {
		return GlobalData.getInstance().makesurePlayer(superSoldierEntity.getPlayerId());
	}

	/**
	 * 组织数据传输协议
	 */
	public PBSuperSoldierInfo toPBobj() {
		if (Objects.nonNull(soldierInfoPB)) {
			return soldierInfoPB;
		}

		PBSuperSoldierInfo.Builder result = PBSuperSoldierInfo.newBuilder()
				.setSuperSoldierId(getCfgId())
				.setLevel(getLevel())
				.setTotalExp(superSoldierEntity.getExp())
				.setStar(this.getStar())
				.setStep(getStep())
				.setOffice(this.getOffice())
				.setState(getState())
				.setSkinUse(getSkin())
				.addAllUnlockSkins(getUnlockSkinSet())
				.setUnlockAnyWhere(isAnyWhereUnlock(false))
				.setShareCount(superSoldierEntity.getShareCount())
				.addAllPassiveSkillSlot(this.passiveSkillSlots.stream().map(SuperSoldierSkillSlot
						
						::toPBObj).collect(Collectors.toList()))
				.addAllSkillSlot(this.skillSlots.stream().map(SuperSoldierSkillSlot::toPBObj).collect(Collectors.toList()))
				.setCityDefense(this.superSoldierEntity.getCityDefense())
				.setPower(this.power());

		battleEffVal.forEach((k, v) -> {
			PBSuperSoldierEffect effect = PBSuperSoldierEffect.newBuilder().setEffectId(k.getNumber()).setValue(v).build();
			result.addMarchEffect(effect);
		});

		// 皮肤
		ConfigIterator<SuperSoldierSkinCfg> skinIt = HawkConfigManager.getInstance().getConfigIterator(SuperSoldierSkinCfg.class);
		for (SuperSoldierSkinCfg skinCfg : skinIt) {
			if (getCfgId() != skinCfg.getSupersoldierId()) {
				continue;
			}
			StatusDataEntity entity = getSkinBuff(skinCfg);
			if (entity != null && entity.getEndTime() > HawkTime.getMillisecond()) {
				PBSuperSoldierSkin skin = PBSuperSoldierSkin.newBuilder()
						.setSkinId(skinCfg.getSkinId())
						.setStarTime(entity.getStartTime())
						.setEndTime(entity.getEndTime()).build();
				result.addSkinList(skin);
			}
		}
		
		// 赋能
		result.addAllEnergys(soldierEnergy.getAllEnergyIds());
		
		soldierInfoPB = result.build();
		return soldierInfoPB;
	}

	protected StatusDataEntity getSkinBuff(SuperSoldierSkinCfg skinCfg) {
		return getParent().getData().getStatusById(skinCfg.getSkinId());
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("playerId", getParent().getId())
				.add("soldierId", getCfgId())
				.add("star", getStar())
				.add("level", getLevel())
				.toString();

	}

	public void starUp(int toStar, int toStep) {
		this.superSoldierEntity.setStar(toStar);
		this.superSoldierEntity.setStep(toStep);
		this.notifyChange();
	}

	public ImmutableList<SuperSoldierSkillSlot> getSkillSlots() {
		return skillSlots;
	}

	public ImmutableList<SuperSoldierSkillSlot> getPassiveSkillSlots() {
		return passiveSkillSlots;
	}

	public void officeAppoint(int office) {
		if (superSoldierEntity.getOffice() != office) {
			superSoldierEntity.setOffice(office);
			this.notifyChange();
		}
	}

	public void changeSkin(int skinId) {
		if (superSoldierEntity.getSkin() != skinId) {
			superSoldierEntity.setSkin(skinId);
			this.notifyChange();
		}
	}

	public void cityDef(int office) {
		if (superSoldierEntity.getCityDefense() != office) {
			superSoldierEntity.setCityDefense(office);
			this.notifyChange();
		}
	}

	public void incShare() {
		int cc = superSoldierEntity.getShareCount();
		superSoldierEntity.setShareCount(cc + 1);
		this.notifyChange();
	}

	public int getShareCount() {
		return superSoldierEntity.getShareCount();
	}

	public boolean isEfvalLoad() {
		return efvalLoad;
	}

	public SuperSoldierEntity getDBEntity() {
		return superSoldierEntity;
	}

	public SuperSoldierEnergy getSoldierEnergy() {
		return soldierEnergy;
	}

	public String serializUnlockSkin() {
		return SerializeHelper.collectionToString(this.unlockSkinSet, SerializeHelper.ATTRIBUTE_SPLIT);
	}


	public Set<Integer> getUnlockSkinSet() {
		return unlockSkinSet;
	}

	public void unlockSkin(int skinId){
		unlockSkinSet.add(skinId);
		superSoldierEntity.setChanged(true);
		//superSoldierEntity.notifyUpdate();
	}

	public Set<Integer> getPlayerUnlockSkinSet() {
		try {
			if(getParent() == null
					|| getParent().getData() == null
					|| getParent().getData().getDataCache() == null){
				return new HashSet<>();
			}
			CommanderEntity entity = getParent().getData().getCommanderEntity();
			if(entity == null){
				return new HashSet<>();
			}
			return entity.getSuperSoldierSkins();
		}catch (Exception e){
			HawkException.catchException(e);
		}
		return new HashSet<>();
	}
}
