package com.hawk.game.battle;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hawk.os.HawkException;

import com.google.common.collect.ImmutableMap;
import com.hawk.game.battle.effect.BattleConst.WarEff;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.util.BattleUtil;

public class BattleCalcParames {
	/** 是否是进攻方 */
	public final boolean isAtk;
	/** 部队作用号类型 */
	public final WarEff warEff;

	public final boolean isSameServer;
	/** 是否在副本地图中 */
	public boolean isInDungeonMap = false;

	/** 是否开启救援 */
	public final boolean lifeSaving;
	/** 是否开启决斗 */
	public final boolean duel;
	/** 伤兵转化率减少万分比 */
	public final double disHurtRate;
	/** 是否是战争点拥有者(联盟领地,旗帜等) */
	public final boolean isOwner;
	public final int decDieBecomeInjury;
	public Map<String, BattleUnity> playerBattleUnity;
	private BattleUnity leaderUnit;
	private int convertRate;
	public BattleCalcParames(boolean isAtk, WarEff warEff, boolean isSameServer, boolean lifeSaving, boolean duel, double disHurtRate, Map<String, BattleUnity> playerBattleUnity, boolean isOwner, boolean isInDungeonMap,int decDieBecomeInjury) {
		this.isAtk = isAtk;
		this.isSameServer = isSameServer;
		this.lifeSaving = lifeSaving;
		this.duel = duel;
		this.disHurtRate = disHurtRate;
		this.warEff = warEff;
		this.playerBattleUnity = ImmutableMap.copyOf(playerBattleUnity);
		this.isOwner = isOwner;
		this.isInDungeonMap = isInDungeonMap;
		this.decDieBecomeInjury = decDieBecomeInjury;
	}
	
	public int getBattleEffVal(String playerId, EffType eff) {
		if (playerBattleUnity.containsKey(playerId)) {
			return playerBattleUnity.get(playerId).getEffVal(eff);
		}
		return 0;
	}
	
	public int getBattleSoldierEffVal(String playerId, EffType eff) {
		try {
			if (playerBattleUnity.containsKey(playerId)) {
				return playerBattleUnity.get(playerId).getSolider().getEffVal(eff);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return 0;
	}
	
	public static Builder newBuilder() {
		return new Builder();
	}

	public static class Builder {
		private boolean isAtk;
		public WarEff warEff = WarEff.NO_EFF;
		public boolean isSameServer = true;
		public boolean isInDungeonMap = false;
		private boolean lifeSaving = false;
		private boolean duel = false;
		private double disHurtRate = 0;
		private boolean isOwner = false;
		/**减少决斗中转伤*/
		private int decDieBecomeInjury;
		
		private Map<String, BattleUnity> playerBattleUnity = new HashMap<>();
		private BattleUnity leaderUnit;
		public BattleCalcParames build() {
			BattleCalcParames parames = new BattleCalcParames(isAtk, warEff, isSameServer, lifeSaving, duel, disHurtRate, playerBattleUnity, isOwner,
					isInDungeonMap,decDieBecomeInjury);
			parames.leaderUnit = leaderUnit;
			return parames;
		}

		public Builder addPlayerBattleUnity(List<BattleUnity> unityList) {
			if(!unityList.isEmpty()){
				leaderUnit = unityList.get(0);
			}
			for(BattleUnity unity: unityList){
				playerBattleUnity.put(unity.getPlayer().getId(), unity);
			}
			return this;
		}

		public Builder setWarEff(WarEff warEff) {
			this.warEff = warEff;
			return this;
		}

		public Builder setAtk(boolean isAtk) {
			this.isAtk = isAtk;
			return this;
		}

		public Builder setIsSameServer(boolean isSameServer) {
			this.isSameServer = isSameServer;
			return this;
		}

		public Builder setInDungeonMap(boolean isInDungeonMap) {
			this.isInDungeonMap = isInDungeonMap;
			return this;
		}

		public Builder setLifeSaving(boolean lifeSaving) {
			this.lifeSaving = lifeSaving;
			return this;
		}

		public Builder setDuel(boolean duel) {
			this.duel = duel;
			return this;
		}

		public Builder setDisHurtRate(double disHurtRate) {
			this.disHurtRate = disHurtRate;
			return this;
		}

		public Builder setIsOwner(boolean isOwner) {
			this.isOwner = isOwner;
			return this;
		}

		public Builder setDecDieBecomeInjury(int decDieBecomeInjury) {
			this.decDieBecomeInjury = decDieBecomeInjury;
			return this;
		}

	}

	public BattleUnity getleaderUnity() {
		return leaderUnit;
	}

	public int getConvertRate() {
		return convertRate;
	}

	public void setConvertRate(int convertRate) {
		this.convertRate = convertRate;
	}

}
