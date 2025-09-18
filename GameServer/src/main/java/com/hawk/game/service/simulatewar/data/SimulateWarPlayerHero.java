package com.hawk.game.service.simulatewar.data;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.hawk.game.config.HeroCfg;
import com.hawk.game.entity.HeroEntity;
import com.hawk.game.player.Player;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.player.hero.SkillSlot;
import com.hawk.game.player.hero.TalentSlot;
import com.hawk.game.player.hero.skill.IHeroSkill;
import com.hawk.game.protocol.Army.ArmyHeroPB;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Hero.PBHeroAttr;
import com.hawk.game.protocol.Hero.PBHeroInfo;
import com.hawk.game.protocol.Hero.PBHeroState;
import com.hawk.game.util.EffectParams;
import com.hawk.game.world.march.IWorldMarch;

public class SimulateWarPlayerHero extends PlayerHero {
	private PBHeroInfo heroData;

	private SimulateWarPlayerHero(HeroEntity heroEntity) {
		super(heroEntity);
	}

	public static SimulateWarPlayerHero create(PBHeroInfo data) {
		SimulateWarPlayerHero result = new SimulateWarPlayerHero(null);
		result.heroData = data;
		return result;
	}

	@Override
	public int getStar() {
		return heroData.getStar();
	}

	@Override
	public int getStep() {
		return heroData.getStep();
	}

	@Override
	public int getOffice() {
		return heroData.getOffice();
	}

	@Override
	public PBHeroInfo toPBobj() {
		return heroData;
	}

	@Override
	public Optional<TalentSlot> getTalentSlotByIndex(int index) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getShowSkin() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getCityDefense() {
		throw new UnsupportedOperationException();
	}

	@Override
	public HeroCfg getConfig() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void loadEffVal() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int power() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getBattleEffect(EffType effType , EffectParams effectParams) {
		return 0;
	}

	@Override
	public int getOfficeEffVal(EffType effType , EffectParams effParams){
		throw new UnsupportedOperationException();
	}

	@Override
	public void castSkill(int skillID) {
		throw new UnsupportedOperationException();
	}

	@Override
	public IHeroSkill getSkillById(int skillID) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean goMarch(IWorldMarch march) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean backFromMarch(IWorldMarch march) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void notifyChange() {
		throw new UnsupportedOperationException();
	}

	@Override
	public PBHeroState getState() {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void init() {
		throw new UnsupportedOperationException();
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
	public String serializTalent() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setSkills(List<IHeroSkill> skills) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getLevel() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addExp(int exp) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int showExp() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int levelExp() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Map<Integer, PBHeroAttr> attrs() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getCfgId() {
		return heroData.getHeroId();
	}

	@Override
	public Player getParent() {
		throw new UnsupportedOperationException();
	}

	@Override
	public ArmyHeroPB toArmyHeroPb() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void starUp(int toStar, int toStep) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ImmutableList<TalentSlot> getTalentSlots() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void officeAppoint(int office) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void changeSkin(int skinId) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void cityDef(int office) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void incShare() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getShareCount() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isEfvalLoad() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setTalentOpen() {
		throw new UnsupportedOperationException();
	}

}
