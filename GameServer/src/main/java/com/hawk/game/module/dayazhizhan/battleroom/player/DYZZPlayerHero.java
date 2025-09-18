package com.hawk.game.module.dayazhizhan.battleroom.player;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.hawk.config.HawkConfigManager;
import org.hawk.db.entifytype.EntityType;
import org.hawk.tuple.HawkTuple2;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.hawk.game.battle.NpcPlayer;
import com.hawk.game.config.FoggyHeroCfg;
import com.hawk.game.config.HeroCfg;
import com.hawk.game.entity.HeroEntity;
import com.hawk.game.player.Player;
import com.hawk.game.player.hero.HeroSkin;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.player.hero.SkillSlot;
import com.hawk.game.player.hero.skill.HeroSkillFactory;
import com.hawk.game.player.hero.skill.IHeroSkill;
import com.hawk.game.protocol.Army.ArmyHeroPB;
import com.hawk.game.protocol.Hero.PBHeroAttr;
import com.hawk.game.protocol.Hero.PBHeroInfo;
import com.hawk.game.protocol.Hero.PBHeroState;

public class DYZZPlayerHero extends PlayerHero {
	private Player parent = NpcPlayer.DEFAULT_INSTANCE; 
	private FoggyHeroCfg foCfg;
	private PBHeroState state = PBHeroState.HERO_STATE_FREE;
	protected DYZZPlayerHero(HeroEntity heroEntity) {
		super(heroEntity);
	}

	public static DYZZPlayerHero create(IDYZZPlayer player, FoggyHeroCfg cfg) {
		HeroEntity entity = new HeroEntity();
		entity.setHeroId(cfg.getHeroId());
		entity.setStar(cfg.getStarLevel());
		entity.setPersistable(false);
		entity.setEntityType(EntityType.TEMPORARY);

		DYZZPlayerHero hero = new DYZZPlayerHero(entity);
		hero.foCfg = cfg;
		hero.init();
		hero.loadEffVal();
		hero.parent = player;
		entity.recordHeroObj(hero);
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
			
			IHeroSkill skill = HeroSkillFactory.getInstance().createEmptySkill(idLev.first);
			skill.setExp(Integer.MAX_VALUE);
			slot.setSkill(skill);
		}
		ImmutableList<SkillSlot> passiveSkillSlots = this.getPassiveSkillSlots();
		for (int i = 0; i < passiveSkillSlots.size(); i++) {
			SkillSlot slot = passiveSkillSlots.get(i);
			int size = getConfig().getPassiveSkillList().size();
			if(size == 5){
				int passiveSkill = getConfig().getPassiveSkillList().get(4);
				IHeroSkill skill = HeroSkillFactory.getInstance().createEmptySkill(passiveSkill);
				slot.setSkill(skill);
			}
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

//	@Override
//	public IHeroSkill getSkillById(int skillID) {
//		return null;
//	}

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
		return super.getOffice();
	}

	@Override
	public void loadEffVal() {
		super.loadEffVal();
	}

//	@Override
//	public void castSkill(int skillID) {
//		throw new UnsupportedOperationException();
//	}

	@Override
	public void notifyChange() {
		super.notifyChange();
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
	public void starUp(int toStar, int toStep) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ImmutableList<SkillSlot> getSkillSlots() {
		return super.getSkillSlots();
	}

	@Override
	public void officeAppoint(int office) {
		super.officeAppoint(office);
	}

	public FoggyHeroCfg getFoggyHeroCfg() {
		return foCfg;
	}

	public void setState(PBHeroState state) {
		this.state = state;
	}

}
