package com.hawk.game.service.simulatewar.data;

import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.hawk.game.config.SuperSoldierCfg;
import com.hawk.game.config.SuperSoldierSkinCfg;
import com.hawk.game.entity.StatusDataEntity;
import com.hawk.game.entity.SuperSoldierEntity;
import com.hawk.game.player.Player;
import com.hawk.game.player.supersoldier.SuperSoldier;
import com.hawk.game.player.supersoldier.SuperSoldierSkillSlot;
import com.hawk.game.player.supersoldier.skill.ISuperSoldierSkill;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.SuperSoldier.PBSuperSoldierInfo;
import com.hawk.game.protocol.SuperSoldier.PBSuperSoldierState;
import com.hawk.game.world.march.IWorldMarch;

public class SimulateWarSuperSoldier extends SuperSoldier {
	private PBSuperSoldierInfo sourceData;

	private SimulateWarSuperSoldier(SuperSoldierEntity dbEntity) {
		super(dbEntity);
	}

	public static SimulateWarSuperSoldier create(PBSuperSoldierInfo data) {
		SimulateWarSuperSoldier result = new SimulateWarSuperSoldier(null);
		result.sourceData = data;
		return result;
	}

	@Override
	public int getCfgId() {
		return sourceData.getSuperSoldierId();
	}

	@Override
	public PBSuperSoldierInfo toPBobj() {
		return sourceData;
	}

	@Override
	public int getStar() {
		return sourceData.getStar();
	}

	@Override
	public int getStep() {
		return sourceData.getStep();
	}

	@Override
	public int getOffice() {
		throw new UnsupportedOperationException();
		// return // super.getOffice();
	}

	@Override
	public int getSkin() {
		throw new UnsupportedOperationException();
		// return // super.getSkin();
	}

	@Override
	public int getCityDefense() {
		throw new UnsupportedOperationException();
		// return // super.getCityDefense();
	}

	@Override
	public SuperSoldierCfg getConfig() {
		throw new UnsupportedOperationException();
		// return // super.getConfig();
	}

	@Override
	public void loadEffVal() {
		throw new UnsupportedOperationException();
		// super.loadEffVal();
	}

	@Override
	public int power() {
		throw new UnsupportedOperationException();
		// return // super.power();
	}

	@Override
	public Map<EffType, Integer> battleEffect() {
		throw new UnsupportedOperationException();
		// return // super.battleEffect();
	}

	@Override
	public ImmutableMap<EffType, Integer> getOfficeEffVal() {
		throw new UnsupportedOperationException();
		// return // super.getOfficeEffVal();
	}

	@Override
	public void castSkill(int skillID) {
		throw new UnsupportedOperationException();
		// super.castSkill(skillID);
	}

	@Override
	public ISuperSoldierSkill getSkillById(int skillID) {
		throw new UnsupportedOperationException();
		// return // super.getSkillById(skillID);
	}

	@Override
	public boolean goMarch(IWorldMarch march) {
		throw new UnsupportedOperationException();
		// return // super.goMarch();
	}

	@Override
	public boolean backFromMarch(IWorldMarch march) {
		throw new UnsupportedOperationException();
		// return // super.backFromMarch();
	}

	@Override
	public void notifyChange() {
		throw new UnsupportedOperationException();
		// super.notifyChange();
	}

	@Override
	public PBSuperSoldierState getState() {
		throw new UnsupportedOperationException();
		// return // super.getState();
	}

	@Override
	protected void init() {
		throw new UnsupportedOperationException();
		// super.init();
	}

	@Override
	public String serializPassiveSkill() {
		throw new UnsupportedOperationException();
		// return // super.serializPassiveSkill();
	}

	@Override
	public String serializSkill() {
		throw new UnsupportedOperationException();
		// return // super.serializSkill();
	}

	@Override
	public void setSkills(List<ISuperSoldierSkill> skills) {
		throw new UnsupportedOperationException();
		// super.setSkills(skills);
	}

	@Override
	public int getLevel() {
		throw new UnsupportedOperationException();
		// return // super.getLevel();
	}

	@Override
	public void addExp(int exp) {
		throw new UnsupportedOperationException();
		// super.addExp(exp);
	}

	@Override
	public int showExp() {
		throw new UnsupportedOperationException();
		// return // super.showExp();
	}

	@Override
	public int levelExp() {
		throw new UnsupportedOperationException();
		// return // super.levelExp();
	}

	@Override
	public Player getParent() {
		throw new UnsupportedOperationException();
		// return // super.getParent();
	}

	@Override
	protected StatusDataEntity getSkinBuff(SuperSoldierSkinCfg skinCfg) {
		throw new UnsupportedOperationException();
		// return // super.getSkinBuff(skinCfg);
	}

	@Override
	public String toString() {
		throw new UnsupportedOperationException();
		// return // super.toString();
	}

	@Override
	public void starUp(int toStar, int toStep) {
		throw new UnsupportedOperationException();
		// super.starUp(toStar, toStep);
	}

	@Override
	public ImmutableList<SuperSoldierSkillSlot> getSkillSlots() {
		throw new UnsupportedOperationException();
		// return // super.getSkillSlots();
	}

	@Override
	public ImmutableList<SuperSoldierSkillSlot> getPassiveSkillSlots() {
		throw new UnsupportedOperationException();
		// return // super.getPassiveSkillSlots();
	}

	@Override
	public void officeAppoint(int office) {
		throw new UnsupportedOperationException();
		// super.officeAppoint(office);
	}

	@Override
	public void changeSkin(int skinId) {
		throw new UnsupportedOperationException();
		// super.changeSkin(skinId);
	}

	@Override
	public void cityDef(int office) {
		throw new UnsupportedOperationException();
		// super.cityDef(office);
	}

	@Override
	public void incShare() {
		throw new UnsupportedOperationException();
		// super.incShare();
	}

	@Override
	public int getShareCount() {
		throw new UnsupportedOperationException();
		// return // super.getShareCount();
	}

	@Override
	public boolean isEfvalLoad() {
		throw new UnsupportedOperationException();
		// return // super.isEfvalLoad();
	}

}
