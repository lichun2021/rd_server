package com.hawk.game.battle.effect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.hawk.game.battle.BattleSoldier;
import com.hawk.game.battle.BattleUnity;
import com.hawk.game.battle.BattleUnityStatistics;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;

public class CheckerParames {
	public final BattleTupleType.Type tupleType;
	public final int supersoldierId;
	/** 自身兵种类型 */
	public final SoldierType type;
	/** 目标兵种类型 */
	public final SoldierType tarType;
	public final BattleConst.WarEff troopEffType;
	public final List<Integer> heroId;
	/** 已方所有参战玩家 */
	public final List<ArmyInfo> armyList;
	/** 额外伤害加成(新版野怪) */
	public final int hurtPer;
	/** 所有出战兵数量, 集结是整支部队 */
	public final double totalCount;
	public final BattleUnity unity;
	/** 已方所有参战玩家 */
	public final List<BattleUnity> unityList;
	/**已方对长的第一个战斗单位. 多用于光环类计算结果缓存 队伍整体增益 额外参数存放*/
	private final BattleUnity leaderUnity;

	public final BattleUnityStatistics unitStatic;
	public final BattleUnityStatistics tarStatic;
	/** 慎用! 对应的战场, troop等都没有组装 */
	public final BattleSoldier solider;

	private CheckerParames(BattleSoldier solider, SoldierType type, SoldierType tarType, BattleConst.WarEff troopEffType, List<Integer> heroId,
			int supersoldierId, int hurtPer, BattleUnity unity,BattleTupleType.Type tupleType) {
		this.solider = solider;
		this.tarType = tarType;
		this.type = type;
		this.troopEffType = troopEffType;
		this.heroId = heroId;
		this.hurtPer = hurtPer;
		this.supersoldierId = supersoldierId;
		this.unity = unity;
		this.unitStatic = unity.getUnitStatic();
		this.tarStatic = unity.getTarStatic();
		this.armyList = unitStatic.getArmyList();
		this.unityList = unitStatic.getUnityList();
		this.totalCount = unitStatic.getTotalCount();
		this.leaderUnity = unitStatic.getLeaderUnity();
		this.tupleType = tupleType;
	}
	
	public void addDebugLog(final String messagePattern, final Object... arguments){
		leaderUnity.getSolider().addDebugLog(messagePattern, arguments);
	}
	
	
	public int getEffVal(String playerId , EffType effType){
		Optional<BattleUnity> unity = findFirstUnity(playerId);
		if(unity.isPresent()){
			return unity.get().getEffVal(effType);
		}
		return 0;
	}
	
	public Optional<BattleUnity> findFirstUnity(String playerId){
		return unityList.stream().filter(u -> Objects.equals(playerId, u.getPlayer().getId())).findFirst();
	}

	public double getPlayerArmyCount(String playerId) {
		return unitStatic.getPlayerArmyCountMap().get(playerId);
	}

	public List<String> getAllPlayer() {
		return new ArrayList<>(unitStatic.getPlayerArmyCountMap().asMap().keySet());
	}

	public double getPlayerArmyCount(String playerId, SoldierType type) {
		return Optional.ofNullable(unitStatic.getPlayerSoldierCount().get(playerId, type)).orElse(0);
	}

	/**集结队伍中type的总量*/
	public double getArmyTypeCount(SoldierType type) {
		return unitStatic.getArmyCountMap().get(type);
	}

	public int getPlayerBattleEffVal(String playerId, EffType eff) {
		for (BattleUnity unit : unityList) {
			if (Objects.equals(unit.getPlayer().getId(), playerId)) {
				return unit.getEffVal(eff);
			}
		}
		return 0;
	}

	/**指定兵种中出征数量最多的*/
	public ArmyInfo getMaxFreeArmy(SoldierType type) {
		ArmyInfo result = null;
		for (BattleUnity unit : unityList) {
			if (unit.getArmyInfo().getType() == type) {
				if (Objects.isNull(result) || result.getFreeCnt() < unit.getArmyInfo().getFreeCnt()) {
					result = unit.getArmyInfo();
				}
			}
		}
		return result;
	}

	/**指定兵种中出征数量最多的*/
	public BattleUnity getPlayerMaxFreeArmy(String playerId, SoldierType type) {
		if (unitStatic.getPlayerSoldierTable().contains(playerId, type)) {
			return unitStatic.getPlayerSoldierTable().get(playerId, type).get(0);
		}
		return null;
	}
	
	public BattleUnity getPlayeMaxMarchArmy(String playerId) {
		String key = "getPlayeMaxMarchArmy:" + playerId;
		BattleUnity max = (BattleUnity) getLeaderExtryParam(key);
		if (max != null) {
			return max;
		}
		for (SoldierType type : SoldierType.values()) {
			if (!BattleSoldier.isSoldier(type)) {
				continue;
			}
			BattleUnity ts = getPlayerMaxFreeArmy(playerId, type);
			if (ts == null) {
				continue;
			}
			if (max == null) {
				max = ts;
				continue;
			}
			if (ts.getMarchCnt() > max.getMarchCnt()) {
				max = ts;
				continue;
			}
			if (ts.getSoldierLevel() > max.getSoldierLevel()) {
				max = ts;
				continue;
			}
		}
		putLeaderExtryParam(key, max);
		return max;
	}

	/**在队长身上存放参数, 本次战斗所有玩家共享*/
	public void putLeaderExtryParam(String key, Object value) {
		leaderUnity.getExtryParam().put(key, value);
	}

	/**在队长身上存放参数, 本次战斗所有玩家共享*/
	public Object getLeaderExtryParam(String key) {
		return leaderUnity.getExtryParam().get(key);
	}

	/**存放本人参数, 本次战斗个人共享*/
	public void putPlayerExtryParam(String key, Object value) {
		String pkey = unity.getPlayer().getName();
		if (!leaderUnity.getExtryParam().containsKey(pkey)) {
			leaderUnity.getExtryParam().put(pkey, new HashMap<>());
		}
		@SuppressWarnings("unchecked")
		Map<String, Object> playerExtryMap = (Map<String, Object>) leaderUnity.getExtryParam().get(pkey);
		playerExtryMap.put(key, value);
	}

	/**存放本人参数, 本次战斗个人共享*/
	public Object getPlayerExtryParam(String key) {
		String pkey = unity.getPlayer().getName();
		if (!leaderUnity.getExtryParam().containsKey(pkey)) {
			return null;
		}
		@SuppressWarnings("unchecked")
		Map<String, Object> playerExtryMap = (Map<String, Object>) leaderUnity.getExtryParam().get(pkey);
		return playerExtryMap.get(key);
	}

	public static Builder newBuilder() {
		return new Builder();
	}

	public static class Builder {
		private BattleSoldier solider;
		private SoldierType tarType;
		private BattleConst.WarEff troopEffType;
		private List<Integer> heroId;
		private int supersoldierId;
		private int hurtPer;
		private BattleUnity unity;
		private BattleTupleType.Type tupleType;
		public CheckerParames build() {
			CheckerParames pa = new CheckerParames(solider, solider.getType(), tarType, troopEffType, heroId, supersoldierId, hurtPer, unity,tupleType);
			return pa;
		}

		public Builder setSolider(BattleSoldier solider) {
			this.solider = solider;
			return this;
		}

		public SoldierType getTarType() {
			return tarType;
		}

		public BattleConst.WarEff getTroopEffType() {
			return troopEffType;
		}

		public List<Integer> getHeroId() {
			return heroId;
		}

		public Builder setTarType(SoldierType tarType) {
			this.tarType = tarType;
			return this;
		}

		public Builder setTroopEffType(BattleConst.WarEff troopEffType) {
			this.troopEffType = troopEffType;
			return this;
		}

		public Builder setHeroId(List<Integer> heroId) {
			this.heroId = heroId;
			return this;
		}

		public Builder setHurtPer(int hurtPer) {
			this.hurtPer = hurtPer;
			return this;
		}

		public Builder setSupersoldierId(int supersoldierId) {
			this.supersoldierId = supersoldierId;
			return this;
		}

		public Builder setUnity(BattleUnity unity) {
			this.unity = unity;
			return this;
		}

		public Builder setTupleType(BattleTupleType.Type tupleType) {
			this.tupleType = tupleType;
			return this;
		}

	}

}
