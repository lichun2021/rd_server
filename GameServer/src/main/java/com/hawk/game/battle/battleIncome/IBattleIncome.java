package com.hawk.game.battle.battleIncome;

import java.util.Collections;
import java.util.List;

import com.hawk.game.battle.Battle;
import com.hawk.game.battle.BattleCalcParames;
import com.hawk.game.battle.BattleOutcome;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Armour.ArmourSuitType;
import com.hawk.game.protocol.MechaCore.MechaCoreSuitType;
import com.hawk.game.protocol.World.PresetMarchManhattan;
import com.hawk.game.service.mail.DungeonMailType;
import com.hawk.game.util.EffectParams;

public interface IBattleIncome {
	
	/**
	 * 是否掠夺资源
	 * @return
	 */
	default boolean isGrabResMarch(){
		return false;
	}
	
	/**
	 * 是否集结进攻
	 * @return
	 */
	boolean isMassAtk();
	
	default void calNationMilitary(BattleOutcome battleOutcome){}
	
	/**
	 * 是否援助防守
	 * @return
	 */
	boolean isAssitanceDef();
	
	/**
	 * 获取战斗类
	 * @return
	 */
	Battle getBattle();
	
	/**
	 * 获取进攻方玩家列表
	 * @return
	 */
	List<Player> getAtkPlayers();
	
	/**
	 * 获取防御方玩家列表
	 * @return
	 */
	List<Player> getDefPlayers();
	
	default Player getPlayer(String playerId){
		if(getAtkCalcParames().playerBattleUnity.containsKey(playerId)){
			return getAtkCalcParames().playerBattleUnity.get(playerId).getPlayer();
		}
		if(getDefCalcParames().playerBattleUnity.containsKey(playerId)){
			return getAtkCalcParames().playerBattleUnity.get(playerId).getPlayer();
		}
		return null;
	}
	/**
	 * 战斗结果统计
	 * @return
	 */
	BattleOutcome gatherBattleResult();
	
	/**取得英雄Id*/
	default List<Integer> getAtkPlayerHeros(String playerId) {
		if(getAtkCalcParames().playerBattleUnity.containsKey(playerId)){
			return getAtkCalcParames().playerBattleUnity.get(playerId).getEffectParams().getHeroIds();
		}
		return Collections.emptyList();
	}

	default List<Integer> getDefPlayerHeros(String playerId) {
		if(getDefCalcParames().playerBattleUnity.containsKey(playerId)){
			return getDefCalcParames().playerBattleUnity.get(playerId).getEffectParams().getHeroIds();
		}
		return Collections.emptyList();
	}
	/**超级兵 */
	default int getAtkPlayerSuperSoldier(String playerId) {
		if(getAtkCalcParames().playerBattleUnity.containsKey(playerId)){
			return getAtkCalcParames().playerBattleUnity.get(playerId).getEffectParams().getSuperSoliderId();
		}
		return 0;
	}

	default int getDefPlayerSuperSoldier(String playerId) {
		if(getDefCalcParames().playerBattleUnity.containsKey(playerId)){
			return getDefCalcParames().playerBattleUnity.get(playerId).getEffectParams().getSuperSoliderId();
		}
		return 0;
	}
	
	/**装备*/
	default int getAtkPlayerArmourSuitId(String playerId) {
		if(getAtkCalcParames().playerBattleUnity.containsKey(playerId)){
			ArmourSuitType armourSuit = getAtkCalcParames().playerBattleUnity.get(playerId).getEffectParams().getArmourSuit();
			if (armourSuit == null) {
				return 0;
			}
			return armourSuit.getNumber();
		}
		return 0;
	}

	default int getDefPlayerArmourSuitId(String playerId) {
		if(getDefCalcParames().playerBattleUnity.containsKey(playerId)){
			ArmourSuitType armourSuit = getDefCalcParames().playerBattleUnity.get(playerId).getEffectParams().getArmourSuit();
			if (armourSuit == null) {
				return 0;
			}
			return armourSuit.getNumber();
		}
		return 0;
	}
	
	default MechaCoreSuitType getAtkPlayerMechacoreSuit(String playerId) {
		if(getAtkCalcParames().playerBattleUnity.containsKey(playerId)){
			return getAtkCalcParames().playerBattleUnity.get(playerId).getEffectParams().getMechacoreSuit();
		}
		return null;
	}

	default MechaCoreSuitType getDefPlayerMechacoreSuit(String playerId) {
		if(getDefCalcParames().playerBattleUnity.containsKey(playerId)){
			return getDefCalcParames().playerBattleUnity.get(playerId).getEffectParams().getMechacoreSuit();
		}
		return null;
	}
	default PresetMarchManhattan getAtkPlayerManhattan(String playerId) {
		if (getAtkCalcParames().playerBattleUnity.containsKey(playerId)) {
			PresetMarchManhattan.Builder builder = PresetMarchManhattan.newBuilder();
			EffectParams params = getAtkCalcParames().playerBattleUnity.get(playerId).getEffectParams();
			int atkSwId = params.getManhattanAtkSwId();
			int defSwId = params.getManhattanDefSwId();
			builder.setManhattanAtkSwId(atkSwId);
			builder.setManhattanDefSwId(defSwId);
			return builder.build();
		}
		return null;
	}

	default PresetMarchManhattan getDefPlayerManhattan(String playerId) {
		if (getDefCalcParames().playerBattleUnity.containsKey(playerId)) {
			PresetMarchManhattan.Builder builder = PresetMarchManhattan.newBuilder();
			EffectParams params = getDefCalcParames().playerBattleUnity.get(playerId).getEffectParams();
			int atkSwId = params.getManhattanAtkSwId();
			int defSwId = params.getManhattanDefSwId();
			builder.setManhattanAtkSwId(atkSwId);
			builder.setManhattanDefSwId(defSwId);
			return builder.build();
		}
		return null;
	}
	
	/**
	 * 获取怪物id
	 * @return
	 */
	default int getMonsterId(){
		return 0;
	}
	
	/**
	 * 获取进攻玩家计算相关信息
	 * @return
	 */
	public BattleCalcParames getAtkCalcParames();
	
	/**
	 * 获取防御玩家计算相关信息
	 * @return
	 */
	public BattleCalcParames getDefCalcParames();

	default String getDungeon(){
		return "";
	}

	default String getDungeonId(){
		return "";
	}

	default int getIsLeaguaWar(){
		return 0;
	}

	default int getSeason(){
		return 0;
	}
	
	default DungeonMailType getDuntype() {
		return DungeonMailType.NONE;
	}
}
