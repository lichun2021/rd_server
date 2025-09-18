package com.hawk.game.player.hero;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.hawk.app.HawkApp;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.helper.HawkAssert;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.task.HawkTaskManager;
import org.hawk.tuple.HawkTuple2;
import org.hawk.tuple.HawkTuple3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.HeroChangeEvent;
import com.hawk.activity.event.impl.HeroLevelUpEvent;
import com.hawk.game.battle.BattleOutcome;
import com.hawk.game.battle.battleIncome.IBattleIncome;
import com.hawk.game.config.HeroAttrCfg;
import com.hawk.game.config.HeroCfg;
import com.hawk.game.config.HeroLevelCfg;
import com.hawk.game.config.HeroOfficeCfg;
import com.hawk.game.config.HeroStarLevelCfg;
import com.hawk.game.entity.HeroArchivesEntity;
import com.hawk.game.entity.HeroEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.msg.HeroChangedMsg;
import com.hawk.game.msg.HeroLevelUpMsg;
import com.hawk.game.player.Player;
import com.hawk.game.player.hero.collect.PlayerHeroCollect;
import com.hawk.game.player.hero.skill.HeroSkillFactory;
import com.hawk.game.player.hero.skill.IHeroSkill;
import com.hawk.game.protocol.Army.ArmyHeroPB;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Hero.PBHeroAttr;
import com.hawk.game.protocol.Hero.PBHeroEffect;
import com.hawk.game.protocol.Hero.PBHeroInfo;
import com.hawk.game.protocol.Hero.PBHeroState;
import com.hawk.game.service.mssion.MissionManager;
import com.hawk.game.service.mssion.event.EventHeroChange;
import com.hawk.game.service.mssion.event.EventHeroUpgrade;
import com.hawk.game.strengthenguide.StrengthenGuideManager;
import com.hawk.game.strengthenguide.msg.SGHeroLevelUpMsg;
import com.hawk.game.util.EffectParams;
import com.hawk.game.util.GsConst;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.log.LogConst.PowerChangeReason;

/**
 * 
 * @author lwt
 * @date 2017年7月19日
 */
public class PlayerHero {
	static Logger logger = LoggerFactory.getLogger("Server");
	private PBHeroInfo heroInfoPB;
	private final HeroEntity heroEntity;
	private ImmutableList<SkillSlot> skillSlots = ImmutableList.of();
	private ImmutableList<SkillSlot> passiveSkillSlots = ImmutableList.of();
	private ImmutableList<TalentSlot> talentSlots = ImmutableList.of();
	private List<HeroSkin> skins = ImmutableList.of();
	/** 官职做用号 */
	private ImmutableMap<EffType, Integer> officeEffVal = ImmutableMap.of();
	/** 出征做用号 */
	private ImmutableMap<EffType, Integer> battleEffVal = ImmutableMap.of();
	private boolean efvalLoad;
	/** 英雄羁绊 */
	private PlayerHeroCollect heroCollect;
	private SSSSoul soul;
	/** 当前所在地图*/
	private String dungeonMap = "";

	protected PlayerHero(HeroEntity heroEntity) {
		this.heroEntity = heroEntity;
	}

	public void tick() {
		Optional<IHeroSkill> proSkill = getProficiencySkill();
		if (heroInfoPB != null && proSkill.isPresent() && !Objects.equals(dungeonMap, getParent().getDungeonMap())) {
			this.notifyChange();
		}
		proSkill.ifPresent(skill -> skill.tick());
	}
	
	public int staffVal(){
		HeroOfficeCfg heroofficeCfg = HawkConfigManager.getInstance().getConfigByKey(HeroOfficeCfg.class, getOffice());
		if (heroofficeCfg == null) {
			return 0;
		}
		HeroCfg herocfg = getConfig();
		if (herocfg.getStaffOfficer() == 1 && heroofficeCfg.getStaffOfficer() == 1) {
			return attrVale(101);
		}
		return 0;
	}
	
	/**
	 * 获取英雄档案等级
	 */
	public int getArchiveLevel() {
		try {
			HeroArchivesEntity entity = getParent().getData().getHeroArchivesEntity();
			if (Objects.nonNull(entity)) {
				return entity.getArchiveLevel(getCfgId());
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return 0;
	}
	

	/** 以上作用号不允许出现在英雄身上 */
	public static boolean isForbidEff(EffType effType) {
		if (effType == EffType.T3_1401 || effType == EffType.T3_1402 || effType == EffType.T3_1403 ||
				effType == EffType.T3_1405 || effType == EffType.T3_1406 || effType == EffType.T3_1407 ||
				effType == EffType.T3_1409 || effType == EffType.T3_1410 || effType == EffType.T3_1411) {
			return true; // 以上作用号不允许出现在英雄身上
		}
		return false;
	}

	/** 按index取天赋卡槽 */
	public Optional<TalentSlot> getTalentSlotByIndex(int index) {
		for (TalentSlot slot : talentSlots) {
			if (slot.getIndex() == index) {
				return Optional.of(slot);
			}
		}
		return Optional.empty();
	}

	public int getStar() {
		return heroEntity.getStar();
	}

	public int getStep() {
		return heroEntity.getStep();
	}

	public int getOffice() {
		return heroEntity.getOffice();
	}

	public int getShowSkin() {
		return heroEntity.getSkin();
	}

	public int getCityDefense() {
		return heroEntity.getCityDefense();
	}

	public HeroCfg getConfig() {
		HeroCfg cfg = HawkConfigManager.getInstance().getConfigByKey(HeroCfg.class, heroEntity.getHeroId());
		if (Objects.isNull(cfg)) {
			throw new NullPointerException("英雄配置缺失 heroid = " + heroEntity.getHeroId());
		}
		return cfg;
	}

	public void loadEffVal() {
		if (efvalLoad) {
			return;
		}
		efvalLoad = true;
		HeroOfficeCfg officeCfg = HawkConfigManager.getInstance().getConfigByKey(HeroOfficeCfg.class, getOffice());
		Map<EffType, Integer> __officeEffVal = new HashMap<>(10);
		Map<EffType, Integer> __battleEffVal = new HashMap<>(10);
		List<SkillSlot> allSkillSlots = new ArrayList<>();
		allSkillSlots.addAll(skillSlots);
		allSkillSlots.addAll(passiveSkillSlots);
		for (SkillSlot slot : allSkillSlots) {
			if (!slot.isUnLock()) {
				continue;
			}
			if (Objects.isNull(slot.getSkill())) {
				continue;
			}
			IHeroSkill skill = slot.getSkill();
			if (skill.getCfg().getMarchUsed() == 1) {
				mergeEffval(__battleEffVal, skill.effectVal());
				continue;
			}
			// 是否官职生效
			if (Objects.nonNull(officeCfg) && skill.getCfg().getOfficeIdList().contains(getOffice())) {
				mergeEffval(__officeEffVal, skill.effectVal());
			}
		}
		for (TalentSlot slot : talentSlots) {
			if (!slot.isUnLock()) {
				continue;
			}
			if (Objects.isNull(slot.getTalent())) {
				continue;
			}
			HeroTalent talent = slot.getTalent();
			if (talent.getCfg().getMarchUsed() == 1) {
				mergeEffval(__battleEffVal, talent.effectVal());
				continue;
			}
			// 是否官职生效
			if (Objects.nonNull(officeCfg) && talent.getCfg().getOfficeIdList().contains(getOffice())) {
				mergeEffval(__officeEffVal, talent.effectVal());
			}
		}
		// 属性
		for (PBHeroAttr pbattr : attrs().values()) {
			int effVal = 0;
			switch (pbattr.getAttrId()) {
			case 101:
				effVal = getParent().getEffect().getEffectTech(EffType.T3_1410.getNumber());
				break;
			case 102:
				effVal = getParent().getEffect().getEffectTech(EffType.T3_1409.getNumber());
				break;
			case 103:
				effVal = getParent().getEffect().getEffectTech(EffType.T3_1411.getNumber());
				break;
			default:
				break;
			}
			final double pct = 1 + effVal / GsConst.EFF_RATE;

			{// 出征
				HeroAttrCfg attrCfg = HawkConfigManager.getInstance().getConfigByKey(HeroAttrCfg.class, pbattr.getAttrId());
				List<PBHeroEffect> attrEffList = attrCfg.getAttrMarchEffectList().stream().map(tup2 -> PBHeroEffect.newBuilder()
						.setEffectId(tup2.first)
						.setValue((int) Math.ceil(tup2.second * pct * pbattr.getNumber()))
						.build())
						.collect(Collectors.toList());
				mergeEffval(__battleEffVal, attrEffList);
			}
			{
				// 是否官职生效
				if (Objects.nonNull(officeCfg) && officeCfg.getAttrId() == pbattr.getAttrId()) {
					List<PBHeroEffect> attrEffList = officeCfg.getAttrOfficeEffectList().stream().map(tup2 -> PBHeroEffect.newBuilder()
							.setEffectId(tup2.first)
							.setValue((int) Math.ceil(tup2.second * pct * pbattr.getNumber()))
							.build())
							.collect(Collectors.toList());
					mergeEffval(__officeEffVal, attrEffList);
				}
			}
		}

		// 羁绊
		if (this.heroCollect.isActive()) {
			this.heroCollect.checkCollectEffect();
		}
		
		mergeEffval(__battleEffVal, soul.effectVal());

		officeEffVal = ImmutableMap.copyOf(__officeEffVal);
		battleEffVal = ImmutableMap.copyOf(__battleEffVal);
	}

	private void mergeEffval(Map<EffType, Integer> __effVal, List<PBHeroEffect> effValList) {
		for (PBHeroEffect effVal : effValList) {
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
		HeroStarLevelCfg starLevelCfg = HawkConfigManager.getInstance().getCombineConfig(HeroStarLevelCfg.class, getCfgId(), getStar(), getStep());
		HeroCfg herocfg = getConfig();
		double heropow = herocfg.getPowerCoe().first + herocfg.getPowerCoe().second * getLevel();
		double starPow = starLevelCfg.getStarPower();
		double skillpow = passiveSkillSlots.stream().mapToDouble(SkillSlot::power).sum() + skillSlots.stream().mapToDouble(SkillSlot::power).sum();
		double talentPow = talentSlots.stream().mapToDouble(TalentSlot::power).sum();
		double skinPow = 0;
		Optional<HeroSkin> skinOp = getSkin(getShowSkin());
		if (skinOp.isPresent()) {
			HeroSkin skin = skinOp.get();
			if (skin.isUnlock()) {
				skinPow = skin.getCfg().getPowerSkin();
			}
		}

		return (int) (heropow + starPow + skillpow + skinPow + talentPow + soul.power());
	}

	public String talentPowerStr() {
		List<String> list = new ArrayList<>(talentSlots.size());
		for (TalentSlot slot : talentSlots) {
			int talentId = 0;
			if (slot.getTalent() != null) {
				talentId = slot.getTalent().getSkillID();
			}
			String str = slot.getIndex() + "_" + talentId + "_" + slot.power();
			list.add(str);
		}
		return Joiner.on(",").join(list);
	}

	/**
	 * 取得英雄身上所有作用号
	 * 
	 * @return
	 */
	public Map<EffType, Integer> battleEffectMap() {

		return battleEffVal;
	}

	/**取得SSS战技*/
	public Optional<IHeroSkill> getProficiencySkill() {
		if (passiveSkillSlots == null || passiveSkillSlots.isEmpty()) {
			return Optional.empty();
		}
		IHeroSkill skill = passiveSkillSlots.get(0).getSkill();
		if (skill.isProficiencySkill()) {
			return Optional.of(skill);
		}
		return Optional.empty();
	}

	/**行军特效*/
	public int getShowProficiencyEffect() {
		try {
			Optional<IHeroSkill> proficiencySkill = getProficiencySkill();
			if (proficiencySkill.isPresent()) {
				return proficiencySkill.get().getShowProficiencyEffect();
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return 0;
	}

	/**
	 * 取得英雄身上作用号
	 * 
	 * @return
	 */
	public int getBattleEffect(EffType effType, EffectParams effParams) {
		int result = battleEffVal.getOrDefault(effType, 0);
		Optional<IHeroSkill> proficiencySkill = getProficiencySkill();
		if (proficiencySkill.isPresent()) {
			result += proficiencySkill.get().getProficiencyEffect(effType, effParams);
		}
		return result;
	}

	/** 委任中的英雄增益 */
	public int getOfficeEffVal(EffType effType, EffectParams effParams) {
		return officeEffVal.getOrDefault(effType, 0);
	}

	/**
	 * 释放技能
	 * 
	 * @param skillID
	 */
	public void castSkill(int skillID) {
		IHeroSkill skill = getSkillById(skillID);
		if (Objects.isNull(skill)) {
			return;
		}

		if (skill.isCooling()) {
			skill.cast();
		}
		this.notifyChange();
	}

	public IHeroSkill getSkillById(int skillID) {
		List<SkillSlot> allSkill = new ArrayList<>(skillSlots);
		allSkill.addAll(passiveSkillSlots);
		Optional<IHeroSkill> skillOP = allSkill.stream()
				.filter(SkillSlot::isUnLock)
				.map(SkillSlot::getSkill)
				.filter(Objects::nonNull)
				.filter(s -> s.skillID() == skillID)
				.findAny();
		return skillOP.orElse(null);
	}

	/**
	 * 设置英雄状态
	 */
	private synchronized void setState(PBHeroState state) {
		heroEntity.setState(state.getNumber());
	}

	/**
	 * 出征
	 */
	public boolean goMarch(IWorldMarch march) {
		setState(PBHeroState.HERO_STATE_MARCH);
		for (SkillSlot slot : this.passiveSkillSlots) {
			slot.getSkill().goMarch(march);
		}
		if (heroInfoPB != null) {
			heroInfoPB = heroInfoPB.toBuilder().setState(PBHeroState.HERO_STATE_MARCH).build();
		}
		getParent().sendProtocol(HawkProtocol.valueOf(HP.code.UPDATE_HERO_INFO, this.toPBobj().toBuilder()));
		return true;
	}

	/**
	 * 单场战斗结束
	 */
	public void afterBattle(IBattleIncome income, BattleOutcome battleOutcome) {
		for (SkillSlot slot : this.passiveSkillSlots) {
			slot.getSkill().afterBattle(income, battleOutcome);
		}
	}

	/**
	 * 出征归来
	 */
	public boolean backFromMarch(IWorldMarch march) {
		try {
			setState(PBHeroState.HERO_STATE_FREE);
			for (SkillSlot slot : this.passiveSkillSlots) {
				slot.getSkill().backFromMarch(march);
			}
			if (heroInfoPB != null) {
				heroInfoPB = heroInfoPB.toBuilder().setState(PBHeroState.HERO_STATE_FREE).build();
			}

		} catch (Exception e) {
			HawkException.catchException(e);
		}
		getParent().sendProtocol(HawkProtocol.valueOf(HP.code.UPDATE_HERO_INFO, this.toPBobj().toBuilder()));
		return true;
	}

	/**
	 * 通知英雄数据有变化
	 */
	public void notifyChange() {
		heroEntity.setChanged(true);
		heroInfoPB = null;
		dungeonMap = getParent().getDungeonMap();
		// 重新推送所有做用号
		Set<EffType> allEff = new HashSet<>();
		allEff.addAll(this.battleEffVal.keySet());
		allEff.addAll(this.officeEffVal.keySet());
		allEff.addAll(this.heroCollect.getEffects().keySet());
		efvalLoad = false;
		this.loadEffVal(); // 做号用变更,如删除技能

		allEff.addAll(this.battleEffVal.keySet());
		allEff.addAll(this.officeEffVal.keySet());
		// 羁绊作用号
		allEff.addAll(this.heroCollect.getEffects().keySet());

		getParent().getEffect().syncEffect(getParent(), allEff.toArray(new EffType[allEff.size()]));
		getParent().sendProtocol(HawkProtocol.valueOf(HP.code.UPDATE_HERO_INFO, this.toPBobj().toBuilder()));

		getParent().refreshPowerElectric(PowerChangeReason.HERO_ATTR_CHANGE);
		HawkApp.getInstance().postMsg(getParent(), new HeroChangedMsg(this));
	}

	/**
	 * @return 英雄状态
	 */
	public synchronized PBHeroState getState() {
		return PBHeroState.valueOf(heroEntity.getState());
	}

	public static PlayerHero create(HeroEntity heroEntity) {
		PlayerHero hero = new PlayerHero(heroEntity);
		hero.init();
		heroEntity.recordHeroObj(hero);
		return hero;
	}

	protected void init() {
		this.battleEffVal = ImmutableMap.of();
		this.officeEffVal = ImmutableMap.of();
		this.skillSlots = ImmutableList.copyOf(loadSkill());
		this.passiveSkillSlots = ImmutableList.copyOf(loadPassiveSkill());
		this.talentSlots = ImmutableList.copyOf(loadTalent());
		this.skins = loadSkin();
		this.heroCollect = new PlayerHeroCollect(this);
		this.soul = SSSSoul.create(this);
		
		checkColorUp();
	}

	/**英雄品质调整 */
	private void checkColorUp() {
		try {
			HeroCfg heroCfg = getConfig();
			if (heroCfg.getQualityColor() == 6) {
				if (heroEntity.getExp() < 0 || heroEntity.getExp() > 32497300) {
					heroEntity.setExp(32497300);
				}
			}

			IHeroSkill pskill = passiveSkillSlots.get(0).getSkill();
			int passiveSkill = heroCfg.getPassiveSkillList().get(0);// ="11101"
			if (heroCfg.getQualityColor() < 6 && pskill.skillID() != passiveSkill) {
				SkillSlot pslot = passiveSkillSlots.get(0);
				IHeroSkill skill = HeroSkillFactory.getInstance().createEmptySkill(passiveSkill);
				pslot.setSkill(skill);

				int color = heroCfg.getQualityColor();
				// 已经改好了passiveSkill字段变的就把英雄经验乘一下，乘经验的时候需要读一下hero表qualityColor字段，字段为4就乘1.25倍（蓝变紫），5就乘1.4倍（紫变金），6就乘2倍（金变红）
				int exp = (int) (heroEntity.getExp() * (color <= 4 ? 1.25 : color == 5 ? 1.4 : 2));
				heroEntity.setExp(exp);
				heroEntity.setChanged(true);
			}

			Optional<TalentSlot> pto = talentSlots.stream()
					.filter(slot -> slot.getIndex() == 0 && slot.isUnlock() && slot.getTalent().getSkillID() != getConfig().getPassiveTalent())
					.findAny();
			if (pto.isPresent()) {
				pto.get().getTalent().setSkillID(getConfig().getPassiveTalent());
				for (TalentSlot slot : talentSlots) {
					if(slot.getTalent()!=null){
						slot.getTalent().setExp(0);
					}
				}
				heroEntity.setChanged(true);
			}
			
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	private List<SkillSlot> loadPassiveSkill() {
		if (StringUtils.isEmpty(heroEntity.getPassiveSkillSerialized())) {// 新英雄
			return initPassiveSkill();
		}

		List<SkillSlot> list = new ArrayList<>();
		JSONArray arr = JSONArray.parseArray(heroEntity.getPassiveSkillSerialized());
		arr.forEach(str -> {
			SkillSlot slot = new SkillSlot(this);
			slot.mergeFrom(str.toString());
			if (Objects.nonNull(slot.getSkill())) {
				HawkAssert.notNull(slot.getSkill().getCfg());// 如果属性配置文件出问题,
																// 就重新初始化
			}
			list.add(slot);
		});
		return list;
	}

	private List<SkillSlot> initPassiveSkill() {
		List<SkillSlot> list = new ArrayList<>();
		HeroCfg heroCfg = getConfig();
		int passiveSkill = heroCfg.getPassiveSkillList().get(0);// ="11101"

		SkillSlot slot = new SkillSlot(this);
		slot.setIndex(0);
		slot.setLevel(1);
		IHeroSkill skill = HeroSkillFactory.getInstance().createEmptySkill(passiveSkill);
		slot.setSkill(skill);
		list.add(slot);
		return list;
	}

	public String serializPassiveSkill() {
		JSONArray arr = new JSONArray();
		passiveSkillSlots.stream().map(SkillSlot::serializ).forEach(arr::add);
		return arr.toJSONString();
	}

	private List<SkillSlot> loadSkill() {
		if (StringUtils.isEmpty(heroEntity.getSkillSerialized())) {// 新英雄
			return initSkill();
		}

		List<SkillSlot> list = new ArrayList<>();
		JSONArray arr = JSONArray.parseArray(heroEntity.getSkillSerialized());
		arr.forEach(str -> {
			SkillSlot slot = new SkillSlot(this);
			slot.mergeFrom(str.toString());
			if (Objects.nonNull(slot.getSkill())) {
				HawkAssert.notNull(slot.getSkill().getCfg());// 如果属性配置文件出问题,
																// 就重新初始化
			}
			list.add(slot);
		});
		return list;
	}

	private List<SkillSlot> initSkill() {
		List<SkillSlot> list = new ArrayList<>();
		HeroCfg heroCfg = getConfig();
		for (HawkTuple3<Integer, Integer, Integer> tup : heroCfg.getSkillBlankList()) {
			SkillSlot slot = new SkillSlot(this);
			slot.setIndex(tup.first);
			slot.setLevel(tup.second);
			
			if(tup.third>0){
				IHeroSkill skill = HeroSkillFactory.getInstance().createEmptySkill(tup.third);
				slot.setSkill(skill);
			}
			
			list.add(slot);
		}
		return list;
	}

	/** 取出英雄皮肤 包含未解锁 */
	public Optional<HeroSkin> getSkin(int cfgId) {
		if (cfgId == 0) {
			return Optional.empty();
		}
		Optional<HeroSkin> skinOp = skins.stream().filter(s -> s.getCfgId() == cfgId).findAny();
		if (skinOp.isPresent()) {
			return skinOp;
		}

		HeroSkin skin = new HeroSkin(this);
		skin.setCfgId(cfgId);
		skin.setStep(1);
		if (skin.getCfg() != null) {
			skins.add(skin);
			return Optional.of(skin);
		}

		return Optional.empty();
	}

	/** 序列化skill */
	public String serializSkill() {
		JSONArray arr = new JSONArray();
		skillSlots.stream().map(SkillSlot::serializ).forEach(arr::add);
		return arr.toJSONString();
	}

	private List<HeroSkin> loadSkin() {
		if (StringUtils.isEmpty(heroEntity.getSkinSerialized())) {
			return new ArrayList<>();
		}

		List<HeroSkin> list = new ArrayList<>();
		JSONArray arr = JSONArray.parseArray(heroEntity.getSkinSerialized());
		arr.forEach(str -> {
			HeroSkin slot = new HeroSkin(this);
			slot.mergeFrom(str.toString());
			list.add(slot);
		});
		return list;
	}

	public String serializSkin() {
		JSONArray arr = new JSONArray();
		skins.stream().map(HeroSkin::serializ).forEach(arr::add);
		return arr.toJSONString();
	}

	// ------------------
	private List<TalentSlot> loadTalent() {
		if (StringUtils.isEmpty(getConfig().getMaxTalentBlanks())) {
			return new ArrayList<>();
		}

		if (StringUtils.isEmpty(heroEntity.getTalentSerialized())) {
			return initTalent();
		}

		List<TalentSlot> list = new ArrayList<>();
		JSONArray arr = JSONArray.parseArray(heroEntity.getTalentSerialized());
		arr.forEach(str -> {
			TalentSlot slot = new TalentSlot(this);
			slot.mergeFrom(str.toString());
			if (Objects.nonNull(slot.getTalent())) {
				HawkAssert.notNull(slot.getTalent().getCfg());// 如果属性配置文件出问题,
																// 就重新初始化
			}
			list.add(slot);
		});
		return list;
	}

	private List<TalentSlot> initTalent() {
		List<TalentSlot> list = new ArrayList<>();
		HeroCfg heroCfg = getConfig();
		for (HawkTuple2<Integer, String> tup : heroCfg.getTalentBlankList()) {
			TalentSlot slot = new TalentSlot(this);
			slot.setIndex(tup.first);
			list.add(slot);
		}
		return list;
	}

	/** 天赋序列化 */
	public String serializTalent() {
		if (talentSlots.isEmpty()) {
			return "";
		}
		JSONArray arr = new JSONArray();
		talentSlots.stream().map(TalentSlot::serializ).forEach(arr::add);
		return arr.toJSONString();
	}

	public void setSkills(List<IHeroSkill> skills) {
		throw new UnsupportedOperationException();
	}

	public int getLevel() {

		HeroStarLevelCfg plcfg = HawkConfigManager.getInstance().getCombineConfig(HeroStarLevelCfg.class, getCfgId(), getStar(), getStep());
		int maxLevel = 120;
		if (Objects.nonNull(plcfg)) {
			maxLevel = plcfg.getMaxLevel();
		} else {
			logger.error("HeroStarLevelCfg is null, heroId: {}, star: {}, step: {}", getCfgId(), getStar(), getStep());
		}

		HeroCfg heroCfg = getConfig();
		ConfigIterator<HeroLevelCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(HeroLevelCfg.class);
		long level = configIterator.stream()
				.filter(cfg -> cfg.getHeroQuality() == heroCfg.getQualityColor())
				.filter(cfg -> cfg.getLevelUpExp() <= heroEntity.getExp()).count() + 1;
		return (int) Math.min(maxLevel, level);
	}

	/**
	 * 增加英雄经验值
	 * 
	 * @param exp
	 */
	public void addExp(int exp) {
		int oldLvl = getLevel();
		int oldExp = heroEntity.getExp();
		heroEntity.setExp(exp + oldExp);
		notifyChange();
		int newLevel = getLevel();
		if (newLevel > oldLvl) {
			// 抛出活动事件
			ActivityManager.getInstance().postEvent(new HeroChangeEvent(getParent().getId()));
			ActivityManager.getInstance().postEvent(new HeroLevelUpEvent(getParent().getId(), heroEntity.getHeroId(), newLevel));
			MissionManager.getInstance().postMsg(getParent(), new EventHeroChange());
		}
		MissionManager.getInstance().postMsg(getParent(), new EventHeroUpgrade(heroEntity.getHeroId(), oldLvl, newLevel));
		// 推送礼包
		HawkTaskManager.getInstance().postMsg(this.getParent().getXid(), new HeroLevelUpMsg(heroEntity.getHeroId(), oldLvl, newLevel));

		// 我要变强
		StrengthenGuideManager.getInstance().postMsg(new SGHeroLevelUpMsg(getParent()));
	}

	/**
	 * 显示经验条
	 */
	public int showExp() {
		int heroLevel = getLevel();
		if (heroLevel == 1) {
			return heroEntity.getExp();
		}
		HeroLevelCfg precfg = HawkConfigManager.getInstance().getCombineConfig(HeroLevelCfg.class, heroLevel - 1, getConfig().getQualityColor());
		return Math.min(levelExp(), heroEntity.getExp() - precfg.getLevelUpExp());
	}

	/**
	 * 当前等级全部经验
	 */
	public int levelExp() {
		int heroLevel = getLevel();
		HeroLevelCfg cfg = HawkConfigManager.getInstance().getCombineConfig(HeroLevelCfg.class, heroLevel, getConfig().getQualityColor());
		if (heroLevel == 1) {
			return cfg.getLevelUpExp();
		}
		HeroLevelCfg precfg = HawkConfigManager.getInstance().getCombineConfig(HeroLevelCfg.class, heroLevel - 1, getConfig().getQualityColor());

		return cfg.getLevelUpExp() - precfg.getLevelUpExp();

	}

	/**
	 * 四属性. 时时计算. attrVale() 方法优先
	 */
	public Map<Integer, PBHeroAttr> attrs() {
		ImmutableMap<Integer, Double> starAttrMap = HawkConfigManager.getInstance().getCombineConfig(HeroStarLevelCfg.class, getCfgId(), getStar(), getStep()).getStarAttrMap();
		Map<Integer, PBHeroAttr> result = Maps.newHashMapWithExpectedSize(4);
		for (HawkTuple3<Integer, Double, Double> tup : getConfig().getAttrList()) {
			double val = tup.second + tup.third * getLevel() // 初值加成长
					+ starAttrMap.getOrDefault(tup.first, 0D); // 星级增加值
			Optional<HeroSkin> skinOp = getSkin(getShowSkin());
			if (skinOp.isPresent()) {
				HeroSkin skin = skinOp.get();
				if (skin.isUnlock()) {
					val = val + skin.getCfg().getAttrMap().getOrDefault(tup.first, 0);
				}
			}
			switch (tup.first) {
			case 101:
				if (getConfig().getStaffOfficer() != 1) {
					val = val + getParent().getEffect().getEffectTech(EffType.T3_1402.getNumber());
				}
				break;
			case 102:
				val = val + getParent().getEffect().getEffectTech(EffType.T3_1401.getNumber());
				break;
			case 103:
				val = val + getParent().getEffect().getEffectTech(EffType.T3_1403.getNumber());
				break;

			default:
				break;
			}

			PBHeroAttr pbattr = PBHeroAttr.newBuilder()
					.setAttrId(tup.first)
					.setNumber((int) Math.ceil(val))
					.build();
			result.put(tup.first, pbattr);
		}
		return result;
	}

	/** 四属性,缓存值. 注意不在用在Hero对象初始化过程中即可.*/
	public int attrVale(int attrId) {
		if (Objects.isNull(heroInfoPB)) {
			toPBobj();
		}
		for (PBHeroAttr attr : heroInfoPB.getAttrList()) {
			if (attr.getAttrId() == attrId) {
				return attr.getNumber();
			}
		}
		return 0;

		// PBHeroAttr attr = attrs().get(attrId);
		// if (Objects.isNull(attr)) {
		// return 0;
		// }
		// return attr.getNumber();
	}

	public int getCfgId() {
		return heroEntity.getHeroId();
	}

	public Player getParent() {
		return GlobalData.getInstance().makesurePlayer(heroEntity.getPlayerId());
	}

	/**
	 * 组织数据传输协议
	 */
	public PBHeroInfo toPBobj() {
		if (Objects.nonNull(heroInfoPB)) {
			return heroInfoPB;
		}

		PBHeroInfo.Builder result = PBHeroInfo.newBuilder()
				.setHeroId(getCfgId())
				.setLevel(getLevel())
				.setTotalExp(heroEntity.getExp())
				.setStar(this.getStar())
				.setStep(getStep())
				.setOffice(this.getOffice())
				.setState(getState())
				.setSkinUse(getShowSkin())
				.setTalentOpen(heroEntity.getTalentOpen() == 1)
				.setShareCount(heroEntity.getShareCount())
				.addAllPassiveSkillSlot(this.passiveSkillSlots.stream().map(SkillSlot::toPBObj).collect(Collectors.toList()))
				.addAllSkillSlot(this.skillSlots.stream().map(SkillSlot::toPBObj).collect(Collectors.toList()))
				.addAllTalentSlot(this.talentSlots.stream().map(TalentSlot::toPBObj).collect(Collectors.toList()))
				.setCityDefense(this.heroEntity.getCityDefense())
				.addAllAttr(attrs().values())
				.setSssSoul(soul.toPbObj())
				.setPower(this.power());

		battleEffVal.forEach((k, v) -> {
			PBHeroEffect effect = PBHeroEffect.newBuilder().setEffectId(k.getNumber()).setValue(v).build();
			result.addMarchEffect(effect);
		});

		// 皮肤
		for (int skinId : HeroSkin.heroSkinAll(getCfgId())) {
			Optional<HeroSkin> skinOp = getSkin(skinId);
			if (skinOp.isPresent()) {
				HeroSkin skin = skinOp.get();
				if (skin.isUnlock()) {
					result.addSkinList(skin.toPBobj());
				}
			}
		}
		heroInfoPB = result.build();
		return heroInfoPB;
	}

	public ArmyHeroPB toArmyHeroPb() {
		ArmyHeroPB.Builder heroInfo = ArmyHeroPB.newBuilder();
		heroInfo.setHeroId(getCfgId());
		heroInfo.setLevel(this.getLevel());
		heroInfo.setStar(this.getStar());
		return heroInfo.build();
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("playerId", getParent().getId())
				.add("heroId", getCfgId())
				.add("star", getStar())
				.add("level", getLevel())
				.add("state", getState())
				.add("defense", getCityDefense())
				.add("attr101", attrVale(101))
				.toString();

	}

	public void starUp(int toStar, int toStep) {
		this.heroEntity.setStar(toStar);
		this.heroEntity.setStep(toStep);
		this.notifyChange();
	}

	public ImmutableList<SkillSlot> getSkillSlots() {
		return skillSlots;
	}

	public ImmutableList<SkillSlot> getPassiveSkillSlots() {
		return passiveSkillSlots;
	}

	public ImmutableList<TalentSlot> getTalentSlots() {
		return talentSlots;
	}

	public void officeAppoint(int office) {
		if (heroEntity.getOffice() != office) {
			heroEntity.setOffice(office);
			this.notifyChange();
		}
	}

	public void changeSkin(int skinId) {
		if (heroEntity.getSkin() != skinId) {
			heroEntity.setSkin(skinId);
			this.notifyChange();
		}
	}

	public void cityDef(int office) {
		if (heroEntity.getCityDefense() != office) {
			heroEntity.setCityDefense(office);
			this.notifyChange();
		}
	}

	public void incShare() {
		int cc = heroEntity.getShareCount();
		heroEntity.setShareCount(cc + 1);
		this.notifyChange();
	}

	public int getShareCount() {
		return heroEntity.getShareCount();
	}

	public boolean isEfvalLoad() {
		return efvalLoad;
	}

	public void setTalentOpen() {
		heroEntity.setTalentOpen(1);
	}

	public PlayerHeroCollect getHeroCollect() {
		return heroCollect;
	}

	public HeroEntity getHeroEntity() {
		return heroEntity;
	}

	public SSSSoul getSoul() {
		return soul;
	}

}
