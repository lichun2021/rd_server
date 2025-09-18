package com.hawk.game.battle.battleIncome.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hawk.game.battle.Battle;
import com.hawk.game.battle.BattleCalcParames;
import com.hawk.game.battle.BattleOutcome;
import com.hawk.game.battle.BattleService;
import com.hawk.game.battle.battleIncome.IBattleIncome;
import com.hawk.game.battle.effect.BattleConst;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.player.Player;

/**
 * 战前准备信息
 * @author admin
 */
public class PveBattleIncome implements IBattleIncome{
	/**
	 * 战斗数据
	 */
	private Battle battle;

	/**
	 * 据点Id 
	 */
	private int monsterId;

	/**
	 * 进攻方玩家列表
	 */
	private List<Player> atkPlayers = new ArrayList<>();
	
	/**
	 * 防御方玩家列表
	 */
	private List<Player> defPlayers = new ArrayList<>();

	/**
	 * 进攻方部队信息<playerId, armyList>
	 */
	private Map<String, List<ArmyInfo>> atkArmyMap = new HashMap<>();
	
	/**
	 * 防御方部队信息<playerId, armyList>
	 */
	private Map<String, List<ArmyInfo>> defArmyMap = new HashMap<>();
	
	/**
	 * 进攻玩家计算相关信息
	 */
	private BattleCalcParames atkCalcParames;
	
	/**
	 * 防御玩家计算相关信息
	 */
	private BattleCalcParames defCalcParames;
	
	public Battle getBattle() {
		return battle;
	}

	@Override
	public int getMonsterId() {
		return monsterId;
	}

	public List<Player> getAtkPlayers() {
		return atkPlayers;
	}
	
	public List<Player> getDefPlayers() {
		return defPlayers;
	}

	public Map<String, List<ArmyInfo>> getAtkArmyMap() {
		return atkArmyMap;
	}

	public Map<String, List<ArmyInfo>> getDefArmyMap() {
		return defArmyMap;
	}

	public BattleCalcParames getAtkCalcParames() {
		return atkCalcParames;
	}

	public BattleCalcParames getDefCalcParames() {
		return defCalcParames;
	}
	
	public PveBattleIncome setBattle(Battle battle) {
		this.battle = battle;
		return this;
	}

	public PveBattleIncome setMonsterId(int monsterId) {
		this.monsterId = monsterId;
		return this;
	}

	public PveBattleIncome setAtkCalcParames(BattleCalcParames atkCalcParames) {
		this.atkCalcParames = atkCalcParames;
		return this;
	}

	public PveBattleIncome setAtkPlayers(List<Player> atkPlayers) {
		this.atkPlayers = atkPlayers;
		return this;
	}
	
	public PveBattleIncome setDefPlayers(List<Player> defPlayers) {
		this.defPlayers = defPlayers;
		return this;
	}

	public PveBattleIncome setAtkArmyMap(Map<String, List<ArmyInfo>> atkArmyMap) {
		this.atkArmyMap = atkArmyMap;
		return this;
	}

	public PveBattleIncome setDefArmyMap(Map<String, List<ArmyInfo>> defArmyMap) {
		this.defArmyMap = defArmyMap;
		return this;
	}

	public PveBattleIncome setDefCalcParames(BattleCalcParames defCalcParames) {
		this.defCalcParames = defCalcParames;
		return this;
	}

	@Override
	public boolean isMassAtk() {
		return atkPlayers.size() > 1;
	}

	@Override
	public boolean isAssitanceDef() {
		return defPlayers.size() > 1;
	}

	@Override
	public BattleOutcome gatherBattleResult() {
		Battle battle = this.getBattle();
		boolean isWin = battle.getWinTroop() == BattleConst.Troop.ATTACKER;
		// 进攻方本次参战部队信息
		Map<String, List<ArmyInfo>> battleArmyMapAtk = new HashMap<>();
		// 进攻方战后剩余部队信息
		Map<String, List<ArmyInfo>> aftArmyMapAtk = new HashMap<>();
		BattleService.getInstance().calcArmyInfo(battleArmyMapAtk, aftArmyMapAtk, this.getAtkCalcParames(), this.getAtkArmyMap(), battle);
		
		// 防御方本次参战部队信息
		Map<String, List<ArmyInfo>> battleArmyMapDef = new HashMap<>();
		// 防御方战后剩余部队信息
		Map<String, List<ArmyInfo>> aftArmyMapDef = new HashMap<>();
		BattleService.getInstance().calcArmyInfo(battleArmyMapDef, aftArmyMapDef, this.getDefCalcParames(), this.getDefArmyMap(), battle);
		BattleOutcome battleOutcome = new BattleOutcome(battleArmyMapAtk, battleArmyMapDef, aftArmyMapAtk, aftArmyMapDef, isWin);
		return battleOutcome;
	}
	
}
