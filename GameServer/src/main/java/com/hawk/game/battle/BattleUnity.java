package com.hawk.game.battle;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;

import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.config.BattleSoldierCfg;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.util.EffectParams;

public class BattleUnity {
	private ArmyInfo armyInfo;
	private BattleSoldier solider;
	private Player player;
	/** 额外伤害加成(新版野怪) */
	public int hurtPer;
	/** 行军绑定. 与行军一对一. 即一个玩家一个. 集结玩家各用各的*/
	private EffectParams effParams;

	private BattleUnityStatistics unitStatic;
	private BattleUnityStatistics tarStatic;

	/** 额外参数 */
	private Map<String, Object> extryParam;

	/**决斗*/
	private boolean isDuel;

	private Map<EffType, CheckerKVResult> tarTypeNotSensitiveCache = new HashMap<>();
	
	private BattleUnity() {
	}

	public static BattleUnity valueOf(Player player, ArmyInfo armyInfo, EffectParams effParams) {
		BattleUnity result = new BattleUnity();
		result.player = player;
		result.armyInfo = armyInfo;
		result.effParams = effParams;
		return result;
	}
	
	public void initSolder() {
		if (Objects.isNull(getSolider())) {
			ArmyInfo armyInfo = this.getArmyInfo();
			int armyId = armyInfo.getArmyId();
			BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, armyId);
			BattleSoldier solider = BattleSoldier.valueOf(this.getPlayer(), cfg, armyInfo.getTotalCount(), armyInfo.getShadowCnt());
			solider.setHeros(this.getEffectParams().getHeroIds());
			this.setSolider(solider);
		}
	}

	public int getBuildingWeight() {
		return armyInfo.getBuildingWeight();
	}
	
	public int getEffVal(EffType effType) {
		return effParams.getEffVal(player, effType) + solider.getHonor10EffVal(effType) + unitStatic.getEffValSOType4(effType);
	}

	public ArmyInfo getArmyInfo() {
		return armyInfo;
	}

	public void setArmyInfo(ArmyInfo armyInfo) {
		this.armyInfo = armyInfo;
	}

	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public int getHurtPer() {
		return hurtPer;
	}

	public void setHurtPer(int hurtPer) {
		this.hurtPer = hurtPer;
	}

	public EffectParams getEffectParams() {
		return effParams;
	}

	public void setEffectParams(EffectParams effParams) {
		this.effParams = effParams;
	}

	public void setEffParams(EffectParams effParams) {
		this.effParams = effParams;
	}

	public BattleUnityStatistics getUnitStatic() {
		return unitStatic;
	}

	public void setUnitStatic(BattleUnityStatistics unitStatic) {
		this.unitStatic = unitStatic;
	}

	public BattleUnityStatistics getTarStatic() {
		return tarStatic;
	}

	public void setTarStatic(BattleUnityStatistics tarStatic) {
		this.tarStatic = tarStatic;
	}

	public Map<String, Object> getExtryParam() {
		if (Objects.isNull(extryParam)) {
			extryParam = new HashMap<>();
		}
		return extryParam;
	}

	public void setExtryParam(Map<String, Object> extryParam) {
		this.extryParam = extryParam;
	}

	public boolean isDuel() {
		return isDuel;
	}

	public void setDuel(boolean isDuel) {
		this.isDuel = isDuel;
	}

	public BattleSoldier getSolider() {
		return solider;
	}

	public void setSolider(BattleSoldier solider) {
		this.solider = solider;
	}

	public int getArmyId() {
		return armyInfo.getArmyId();
	}
	
	public int getSoldierLevel(){
		return armyInfo.getLevel();
	}

	public int getFreeCnt() {
		return armyInfo.getFreeCnt();
	}

	public int getMarchCnt() {
		return armyInfo.getFreeCnt() - armyInfo.getShadowCnt();
	}

	public String getPlayerId() {
		return player.getId();
	}
	
	public String getPlayerName() {
		return player.getName();
	}
	
	public Map<EffType, CheckerKVResult> getTarTypeNotSensitiveCache() {
		return tarTypeNotSensitiveCache;
	}
}
