package com.hawk.game.battle;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.hawk.collection.ConcurrentHashTable;
import org.hawk.os.HawkException;

import com.google.common.collect.Table;
import com.google.common.util.concurrent.AtomicLongMap;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;

public class BattleUnityStatistics {
	/** 所有出战兵数量, 集结是整支部队 */
	private double totalCount;
	private AtomicLongMap<String> playerArmyCountMap;
	private AtomicLongMap<SoldierType> armyCountMap;
	private Table<String, SoldierType, Integer> playerSoldierCount;
	
	/** 出征数量, 无影子兵 无防御武器 光陵塔*/
	private double totalCountMarch;
	private AtomicLongMap<String> playerArmyCountMapMarch;
	private AtomicLongMap<SoldierType> armyCountMapMarch;
	private Table<String, SoldierType, Integer> playerSoldierCountMarch;

	/**玩家,兵种类型, 兵种 (free 倒序)*/
	private Table<String, SoldierType, List<BattleUnity>> playerSoldierTable;
	/**全体成员士兵类型 不包括城武*/
	private Set<SoldierType> soldierTypeAll;

	/** 已方所有参战玩家 */
	private List<ArmyInfo> armyList;
	/** 已方所有参战玩家 */
	private List<BattleUnity> unityList;
	private BattleUnity leaderUnity;

	private BattleUnityPlantStatistics plantUnityStatistics;
	/** 近战数量最多*/
	private BattleUnity maxJinZhan;
	private BattleUnity maxJinZhanMarch;
	/** 远程数量最多*/
	private BattleUnity maxYuanCheng;
	private BattleUnity maxYuanChengMarch;
	/** 参谋点*/
	private int staffOfficePoint;
	protected BattleUnityStatistics() {
	}

	public boolean isMass(){
		return getPlayerArmyCountMap().size() > 1;
	}
	
	public boolean isNotMass(){
		return !isMass();
	}
	
	public static BattleUnityStatistics create(List<BattleUnity> unitList) {
		List<String> playerIds = unitList.stream().map(BattleUnity::getPlayerId).distinct().collect(Collectors.toList());
		BattleUnityStatistics result = fill(new BattleUnityStatistics(), playerIds, unitList);
		List<BattleUnity> plantUList = new ArrayList<>();
		for (BattleUnity unity : unitList) {
			if (unity.getArmyInfo().isPlant()) {
				plantUList.add(unity);
			}
		}
		result.plantUnityStatistics = fill(new BattleUnityPlantStatistics(), playerIds, plantUList);
		return result;
	}

	private static <T extends BattleUnityStatistics> T fill(T result,List<String> playerIds, List<BattleUnity> unitList) {
		List<ArmyInfo> armyList = unitList.stream().map(BattleUnity::getArmyInfo).collect(Collectors.toList());
		/** 所有出战兵数量, 集结是整支部队 */
		double totalCount = 0;
		double totalCountMarch = 0;
		Set<SoldierType> soldierTypeAll = new HashSet<>();
		AtomicLongMap<String> playerArmyCountMap = AtomicLongMap.create();
		AtomicLongMap<SoldierType> armyCountMap = AtomicLongMap.create();
		Table<String, SoldierType, Integer> playerSoldierCount = ConcurrentHashTable.create();
		
		AtomicLongMap<String> playerArmyCountMap2 = AtomicLongMap.create();
		AtomicLongMap<SoldierType> armyCountMap2 = AtomicLongMap.create();
		Table<String, SoldierType, Integer> playerSoldierCount2 = ConcurrentHashTable.create();
		
		for(SoldierType stype : SoldierType .values()){
			armyCountMap.put(stype, 0);
			armyCountMap2.put(stype, 0);
		}
		
		for(String playerId: playerIds){
			playerArmyCountMap.put(playerId, 0);
			playerArmyCountMap2.put(playerId, 0);
			for(SoldierType stype : SoldierType .values()){
				playerSoldierCount.put(playerId, stype, 0);
				playerSoldierCount2.put(playerId, stype, 0);
			}
		}
		
		Map<String,Integer> staffOfficePointMap = new HashMap<>();
		
		Table<String, SoldierType, List<BattleUnity>> playerSoldierTable = ConcurrentHashTable.create();
		for (BattleUnity unit : unitList) {
			ArmyInfo ar = unit.getArmyInfo();
			totalCount += ar.getFreeCnt();
			if (unit.getSolider().isJinZhan() || unit.getSolider().isYuanCheng()) {
				totalCountMarch += ar.getFreeCnt() - ar.getShadowCnt();
				playerArmyCountMap2.getAndAdd(ar.getPlayerId(), ar.getFreeCnt() - ar.getShadowCnt());
			}
			playerArmyCountMap.getAndAdd(ar.getPlayerId(), ar.getFreeCnt());
			armyCountMap.getAndAdd(ar.getType(), ar.getFreeCnt());
			armyCountMap2.getAndAdd(ar.getType(), ar.getFreeCnt() - ar.getShadowCnt());
			if (ar.getType().getNumber() <= 8) {
				soldierTypeAll.add(ar.getType());
			}
			if (playerSoldierCount.contains(ar.getPlayerId(), ar.getType())) {
				Integer count = playerSoldierCount.get(ar.getPlayerId(), ar.getType());
				playerSoldierCount.put(ar.getPlayerId(), ar.getType(), count + ar.getFreeCnt());
			} else {
				playerSoldierCount.put(ar.getPlayerId(), ar.getType(), ar.getFreeCnt());
			}
			if (playerSoldierCount2.contains(ar.getPlayerId(), ar.getType())) {
				Integer count = playerSoldierCount2.get(ar.getPlayerId(), ar.getType());
				playerSoldierCount2.put(ar.getPlayerId(), ar.getType(), count + ar.getFreeCnt() - ar.getShadowCnt());
			} else {
				playerSoldierCount2.put(ar.getPlayerId(), ar.getType(), ar.getFreeCnt() - ar.getShadowCnt());
			}
			/////////////////////////////////////////
			if (!playerSoldierTable.contains(ar.getPlayerId(), ar.getType())) {
				playerSoldierTable.put(ar.getPlayerId(), ar.getType(), new ArrayList<>());
			}
			playerSoldierTable.get(ar.getPlayerId(), ar.getType()).add(unit);
			
			if(!staffOfficePointMap.containsKey(ar.getPlayerId())){
				staffOfficePointMap.put(ar.getPlayerId(), unit.getPlayer().getStaffOffic().getStaffVal());
			}
			//////////////////////////////////
		}
		int stpoint = staffOfficePointMap.values().stream().sorted(Comparator.comparingInt(Integer::intValue).reversed()).mapToInt(Integer::intValue).limit(ConstProperty.getInstance().getStaffOffice4Max()).sum();
		result.setStaffOfficePoint(stpoint);
		
		playerSoldierTable.values().forEach(list -> list.sort(Comparator.comparingInt(BattleUnity::getFreeCnt).thenComparingInt(BattleUnity::getSoldierLevel).reversed().thenComparing(BattleUnity::getBuildingWeight)));

		try {
			List<BattleUnity> maxnumList = playerSoldierTable.values().stream().filter(v-> !v.isEmpty())
					.map(v -> v.get(0))
					.sorted(Comparator.comparingInt(BattleUnity::getSoldierLevel).reversed().thenComparing(BattleUnity::getBuildingWeight))
					.collect(Collectors.toList());
			for (BattleUnity unit : maxnumList) {
				if (unit.getSolider().isJinZhan()) {
					if (result.getMaxJinZhan() == null) {
						result.setMaxJinZhan(unit);
						result.setMaxJinZhanMarch(unit);
					}
					if (unit.getFreeCnt() > result.getMaxJinZhan().getFreeCnt()) {
						result.setMaxJinZhan(unit);
					}
					if (unit.getMarchCnt() > result.getMaxJinZhan().getMarchCnt()) {
						result.setMaxJinZhanMarch(unit);
					}
				}
				if (unit.getSolider().isYuanCheng()) {
					if (result.getMaxYuanCheng() == null) {
						result.setMaxYuanCheng(unit);
						result.setMaxYuanChengMarch(unit);
					}
					if (unit.getFreeCnt() > result.getMaxYuanCheng().getFreeCnt()) {
						result.setMaxYuanCheng(unit);
					}
					if (unit.getMarchCnt() > result.getMaxYuanCheng().getMarchCnt()) {
						result.setMaxYuanChengMarch(unit);
					}
				}
			}
//			if (result.getMaxJinZhan() != null) {
//				result.getMaxJinZhan().getSolider().addDebugLog("本方近战最多 {}", result.getMaxJinZhan().getSolider().getUUID());
//			}
//			if (result.getMaxYuanCheng() != null) {
//				result.getMaxYuanCheng().getSolider().addDebugLog("本方远程最多 {}", result.getMaxYuanCheng().getSolider().getUUID());
//			}

		} catch (Exception e) {
			HawkException.catchException(e);
		}
		result.setTotalCount(totalCount);
		result.setTotalCountMarch(totalCountMarch);
		result.setPlayerArmyCountMap(playerArmyCountMap);
		result.setPlayerArmyCountMapMarch(playerArmyCountMap2);
		result.setPlayerSoldierCount(playerSoldierCount);
		result.setPlayerSoldierCountMarch(playerSoldierCount2);
		result.setArmyList(armyList);
		result.setUnityList(unitList);
		result.setSoldierTypeAll(soldierTypeAll);
		if (!unitList.isEmpty()) {
			result.setLeaderUnity(unitList.get(0));
		}
		result.setPlayerSoldierTable(playerSoldierTable);
		result.setArmyCountMap(armyCountMap);
		result.setArmyCountMapMarch(armyCountMap2);
		return result;
	}
	
	public int getEffValSOType4(EffType effType){
		if(leaderUnity.getEffectParams().isStaffPointGreat()){
			return leaderUnity.getPlayer().getStaffOffic().getEffValSOType4(effType);
		}
		return 0;
	}

	public double getTotalCount() {
		return totalCount;
	}

	public AtomicLongMap<String> getPlayerArmyCountMap() {
		return playerArmyCountMap;
	}

	public Table<String, SoldierType, Integer> getPlayerSoldierCount() {
		return playerSoldierCount;
	}

	public List<ArmyInfo> getArmyList() {
		return armyList;
	}

	public List<BattleUnity> getUnityList() {
		return unityList;
	}

	public void setTotalCount(double totalCount) {
		this.totalCount = totalCount;
	}

	public void setPlayerArmyCountMap(AtomicLongMap<String> playerArmyCountMap) {
		this.playerArmyCountMap = playerArmyCountMap;
	}

	public void setPlayerSoldierCount(Table<String, SoldierType, Integer> playerSoldierCount) {
		this.playerSoldierCount = playerSoldierCount;
	}

	public void setArmyList(List<ArmyInfo> armyList) {
		this.armyList = armyList;
	}

	public void setUnityList(List<BattleUnity> unityList) {
		this.unityList = unityList;
	}

	public BattleUnity getLeaderUnity() {
		return leaderUnity;
	}

	public void setLeaderUnity(BattleUnity leaderUnity) {
		this.leaderUnity = leaderUnity;
	}

	public Table<String, SoldierType, List<BattleUnity>> getPlayerSoldierTable() {
		return playerSoldierTable;
	}

	public void setPlayerSoldierTable(Table<String, SoldierType, List<BattleUnity>> playerSoldierTable) {
		this.playerSoldierTable = playerSoldierTable;
	}

	public AtomicLongMap<SoldierType> getArmyCountMap() {
		return armyCountMap;
	}

	public void setArmyCountMap(AtomicLongMap<SoldierType> armyCountMap) {
		this.armyCountMap = armyCountMap;
	}

	public Set<SoldierType> getSoldierTypeAll() {
		return soldierTypeAll;
	}

	public void setSoldierTypeAll(Set<SoldierType> soldierTypeAll) {
		this.soldierTypeAll = soldierTypeAll;
	}

	public final BattleUnityPlantStatistics getPlantUnityStatistics() {
		return plantUnityStatistics;
	}

	public final void setPlantUnityStatistics(BattleUnityPlantStatistics plantUnityStatistics) {
		this.plantUnityStatistics = plantUnityStatistics;
	}

	public AtomicLongMap<String> getPlayerArmyCountMapMarch() {
		return playerArmyCountMapMarch;
	}

	public void setPlayerArmyCountMapMarch(AtomicLongMap<String> playerArmyCountMapMarch) {
		this.playerArmyCountMapMarch = playerArmyCountMapMarch;
	}

	public AtomicLongMap<SoldierType> getArmyCountMapMarch() {
		return armyCountMapMarch;
	}

	public void setArmyCountMapMarch(AtomicLongMap<SoldierType> armyCountMapMarch) {
		this.armyCountMapMarch = armyCountMapMarch;
	}

	public Table<String, SoldierType, Integer> getPlayerSoldierCountMarch() {
		return playerSoldierCountMarch;
	}

	public void setPlayerSoldierCountMarch(Table<String, SoldierType, Integer> playerSoldierCountMarch) {
		this.playerSoldierCountMarch = playerSoldierCountMarch;
	}

	public double getTotalCountMarch() {
		return totalCountMarch;
	}

	public void setTotalCountMarch(double totalCountMarch) {
		this.totalCountMarch = totalCountMarch;
	}

	public BattleUnity getMaxJinZhan() {
		return maxJinZhan;
	}

	public void setMaxJinZhan(BattleUnity maxJinZhan) {
		this.maxJinZhan = maxJinZhan;
	}

	public BattleUnity getMaxJinZhanMarch() {
		return maxJinZhanMarch;
	}

	public void setMaxJinZhanMarch(BattleUnity maxJinZhanMarch) {
		this.maxJinZhanMarch = maxJinZhanMarch;
	}

	public BattleUnity getMaxYuanCheng() {
		return maxYuanCheng;
	}

	public void setMaxYuanCheng(BattleUnity maxYuanCheng) {
		this.maxYuanCheng = maxYuanCheng;
	}

	public BattleUnity getMaxYuanChengMarch() {
		return maxYuanChengMarch;
	}

	public void setMaxYuanChengMarch(BattleUnity maxYuanChengMarch) {
		this.maxYuanChengMarch = maxYuanChengMarch;
	}

	public int getStaffOfficePoint() {
		return staffOfficePoint;
	}

	public void setStaffOfficePoint(int staffOfficePoint) {
		this.staffOfficePoint = staffOfficePoint;
	}
	
	public BattleUnity getMaxSoldier() {
		if (maxJinZhan == null) {
			return maxYuanCheng;
		}
		if (maxYuanCheng == null) {
			return maxJinZhan;
		}

		if (maxJinZhan.getFreeCnt() > maxYuanCheng.getFreeCnt()) {
			return maxJinZhan;
		}
		return maxYuanCheng;
	}
	
	public BattleUnity getMaxSoldierMarch() {
		if (maxJinZhanMarch == null) {
			return maxYuanChengMarch;
		}
		if (maxYuanChengMarch == null) {
			return maxJinZhanMarch;
		}

		if (maxJinZhanMarch.getFreeCnt() > maxYuanChengMarch.getFreeCnt()) {
			return maxJinZhanMarch;
		}
		return maxYuanChengMarch;
	}

}
