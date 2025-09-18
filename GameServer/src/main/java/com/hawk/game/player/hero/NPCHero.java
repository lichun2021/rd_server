package com.hawk.game.player.hero;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.hawk.config.HawkConfigManager;
import org.hawk.db.entifytype.EntityType;
import org.hawk.tuple.HawkTuple2;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.hawk.game.battle.NpcPlayer;
import com.hawk.game.config.FoggyHeroCfg;
import com.hawk.game.config.HeroCfg;
import com.hawk.game.entity.HeroEntity;
import com.hawk.game.player.Player;
import com.hawk.game.player.hero.skill.IHeroSkill;
import com.hawk.game.player.hero.skill.NpcHeroSkill;
import com.hawk.game.protocol.Army.ArmyHeroPB;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Hero.PBHeroAttr;
import com.hawk.game.protocol.Hero.PBHeroInfo;
import com.hawk.game.protocol.Hero.PBHeroState;
import com.hawk.game.util.EffectParams;
import com.hawk.game.world.march.IWorldMarch;

public class NPCHero extends PlayerHero {
	private NpcPlayer parent = NpcPlayer.DEFAULT_INSTANCE;
	private FoggyHeroCfg foCfg;
	private PBHeroState state = PBHeroState.HERO_STATE_FREE;
	private NPCHero(HeroEntity heroEntity) {
		super(heroEntity);
	}

	public static NPCHero create(FoggyHeroCfg cfg) {
		HeroEntity entity = new HeroEntity();
		entity.setHeroId(cfg.getHeroId());
		entity.setStar(cfg.getStarLevel());
		entity.setPersistable(false);
		entity.setEntityType(EntityType.TEMPORARY);
		NPCHero hero = new NPCHero(entity);
		hero.foCfg = cfg;
		hero.init();
		hero.loadEffVal();
		return hero;
	}

	@Override
	protected void init() {
		super.init();
		getHeroCollect().setActive(false);
		ImmutableList<SkillSlot> skillList = this.getSkillSlots();
		ImmutableList<HawkTuple2<Integer, Integer>> skillIdLev = foCfg.getSkillList();
		for (int i = 0; i < skillIdLev.size(); i++) {
			SkillSlot slot = skillList.get(i);
			HawkTuple2<Integer, Integer> idLev = skillIdLev.get(i);
			NpcHeroSkill skill = new NpcHeroSkill();
			skill.setSkillId(idLev.first);
			skill.setLevel(idLev.second);
			slot.setSkill(skill);
		}
	}
	
	public Optional<HeroSkin> getSkin(int cfgId) {
		return Optional.empty();
	}

	@Override
	public int getStep() {
		return 0;
	}

	@Override
	public int getShowSkin() {
		return 0;
	}

	@Override
	public int getCityDefense() {
		return 0;
	}

	@Override
	public IHeroSkill getSkillById(int skillID) {
		return null;
	}

	@Override
	public Map<Integer, PBHeroAttr> attrs() {
		return super.attrs();
	}

	@Override
	public ArmyHeroPB toArmyHeroPb() {
		ArmyHeroPB.Builder heroInfo = ArmyHeroPB.newBuilder();
		heroInfo.setHeroId(getCfgId());
		heroInfo.setLevel(this.getLevel());
		heroInfo.setStar(this.getStar());
		return heroInfo.build();
	}

	@Override
	public void changeSkin(int skinId) {
	}

	@Override
	public void cityDef(int office) {
	}

	@Override
	public void incShare() {
	}

	@Override
	public int getShareCount() {
		return 0;
	}

	@Override
	public int power() {
		return super.power();
	}

	@Override
	public int getStar() {
		return getFoggyHeroCfg().getStarLevel();
	}

	@Override
	public int getLevel() {
		return getFoggyHeroCfg().getLevel();
	}

	@Override
	public HeroCfg getConfig() {
		return HawkConfigManager.getInstance().getConfigByKey(HeroCfg.class, foCfg.getHeroId());
	}

	@Override
	public int getOffice() {
		return 0;
	}

	@Override
	public void loadEffVal() {
		super.loadEffVal();
	}

	@Override
	public int getOfficeEffVal(EffType effType , EffectParams effParams){
		return 0;
	}

	@Override
	public void castSkill(int skillID) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean goMarch(IWorldMarch march) {
		setState(PBHeroState.HERO_STATE_MARCH);
		return true;
	}

	@Override
	public boolean backFromMarch(IWorldMarch march) {
		setState(PBHeroState.HERO_STATE_FREE);
		return true;
	}

	@Override
	public void notifyChange() {
		throw new UnsupportedOperationException();
	}

	@Override
	public PBHeroState getState() {
		return state;
	}

	@Override
	public String serializPassiveSkill() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String serializSkill() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setSkills(List<IHeroSkill> skills) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addExp(int exp) {
	}

	@Override
	public int showExp() {
		return 0;
	}

	@Override
	public int levelExp() {
		return super.levelExp();
	}

	@Override
	public int getCfgId() {
		return foCfg.getHeroId();
	}

	@Override
	public Player getParent() {
		return parent;
	}

	@Override
	public PBHeroInfo toPBobj() {
		return super.toPBobj();
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("heroId", getCfgId())
				.add("star", getStar())
				.add("level", getLevel())
				.toString();
	}

	@Override
	public void starUp(int toStar,int toStep) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void officeAppoint(int office) {
		throw new UnsupportedOperationException();
	}

	public FoggyHeroCfg getFoggyHeroCfg() {
		return foCfg;
	}

	public void setState(PBHeroState state) {
		this.state = state;
	}
	
	
}
