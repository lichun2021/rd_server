package com.hawk.game.battle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.hawk.os.HawkException;
import org.hawk.tuple.HawkTuple2;
import org.hawk.tuple.HawkTuples;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import com.hawk.game.battle.effect.BattleConst;
import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;

/**
 * 战斗方（攻击方、防守方）
 */
public class BattleTroop {
	/** 队长Id */
	private String leaderId;
	/**
	 * 功守
	 */
	private BattleConst.Troop troop;

	/**
	 * 战斗
	 */
	private Battle battle = null;
	/**
	 * 战斗方类型
	 */
	private BattleConst.TroopType troopType;

	/**
	 * 部队作用号类型
	 */
	private BattleConst.WarEff warEff;

	/**
	 * 进攻游标
	 */
	private int curAtkIdx = 0;

	/**
	 * 布阵游标
	 */
	private int curPos;

	private boolean soulLinkClose = true;
	/**兵种1-8参战总数 包含所罗门影子*/
	private int totalArmyCount;
	/** 兵种1-8死亡总数 */
	private int deadArmyCount;
	/**队伍中是否有吉迪恩这个东西*/
	private boolean hasNoJidien = true;

	private int maxSOLDIER_1633; // 多个1633 取最大值
	private int last1645round;
	private int buff1652Cnt;
	int buff12061Val;
	public int eff12086ZhuanAll;
	public int skill74501Buff;
	public Map<String,Integer> skill144Val = new HashMap<>();
	/**
	 * 战斗单元列表
	 */
	private List<BattleSoldier> soldierList = new ArrayList<BattleSoldier>();

	private Map<String, Object> extryParam = new HashMap<>();

	public int attackCnt;
	public int planAttackCnt;
	
	public long forceFieldMax;
	public long forceField;
	
	public void putExtryParam(String key, Object val) {
		extryParam.put(key, val);
	}

	public Optional<Object> getExtryParam(String key) {
		return Optional.ofNullable(extryParam.get(key));
	}

	/**
	 * 获取战斗单元列表
	 */
	public List<BattleSoldier> getSoldierList() {
		return soldierList;
	}

	/**
	 * 获取战斗方类型 @see BattleConst.TROOP_TYPE_
	 */
	public BattleConst.TroopType getTroopType() {
		return troopType;
	}

	/**
	 * 获取战斗
	 */
	public Battle getBattle() {
		return battle;
	}

	public BattleTroop getEnemyTroop() {
		if (this == getBattle().getAttacker()) {
			return getBattle().getDefencer();
		}
		return getBattle().getAttacker();
	}

	/**
	 * 按玩家ID获取战斗单元列表
	 * 
	 * @param playerId
	 */
	public List<BattleSoldier> getSoldierList(String playerId) {
		return soldierList.stream()
				.filter(e -> playerId.equals(e.getPlayerId()))
				.collect(Collectors.toList());
	}

	/** 剩余总数 */
	public int totalSoldier() {
		int result = 0;
		for (BattleSoldier solider : soldierList) {
			if (solider.canBeAttack()) {
				result += solider.getFreeCnt();
			}
		}
		return result;
	}

	/** 兵种1-8损失比例  0 到 1  */
	public double lossRate() {
		// double dead = 0;
		// double total = 0;
		// for (BattleSoldier solider : soldierList) {
		// if (solider.canBeAttack()) {
		// dead += solider.getDeadCnt();
		// total += solider.getOriCnt();
		// }
		// }
		// return dead / total;
		return deadArmyCount * 1.0 / totalArmyCount;
	}

	/** 兵种1-8损失 百分比 0 到 100  */
	public int lossRatePct() {
		return (int) (lossRate() * 100);
	}

	public int addArmyDeadCnt(int dead) {
		this.deadArmyCount += dead;
		return this.deadArmyCount;
	}

	/**
	 * 构造方法
	 * 
	 * @param troopType
	 *            战斗方类型 @see BattleConst.TROOP_TYPE_
	 */
	public BattleTroop(Battle battle, BattleConst.TroopType troopType, BattleConst.Troop troop, String leaderId) {
		this.leaderId = leaderId;
		this.battle = battle;
		this.troop = troop;
		this.troopType = troopType;
		this.curPos = BattleConst.Const.INIT_POS.getNumber();
	}

	/**
	 * 下一个进攻战斗单元
	 */
	public BattleSoldier nextAtkSoldier() {
		// 如果到达队末，重置游标，返回null
		if (curAtkIdx >= soldierList.size()) {
			curAtkIdx = 0;
			return null;
		}

		for (int i = curAtkIdx; i < soldierList.size(); i++) {
			BattleSoldier soldier = soldierList.get(i);
			if (soldier.isAlive()) {
				curAtkIdx = i + 1;
				return soldier;
			}
		}

		curAtkIdx = 0;
		return null;
	}

	/** 指定格子内所以兵 */
	public List<BattleSoldier> getDefSoldiersInSamePos(int pos) {
		return soldierList.stream()
				.filter(BattleSoldier::isAlive)
				.filter(BattleSoldier::canBeAttack)
				.filter(soldier -> soldier.getxPos() == pos)
				.collect(Collectors.toList());
	}

	/**
	 * 获取防守战斗单元
	 */
	public BattleSoldier getDefSoldier() {
		// 防守兵
		BattleSoldier defSoldier = null;
		for (int i = 0; i < soldierList.size(); i++) {
			BattleSoldier tmpSoldier = soldierList.get(i);
			if (!tmpSoldier.isAlive() || !tmpSoldier.canBeAttack()) {
				continue;
			}

			if (defSoldier == null || !defSoldier.isAlive()) {
				defSoldier = tmpSoldier;
				continue;
			}

			if (tmpSoldier.getxPos() < defSoldier.getxPos()) {
				defSoldier = tmpSoldier;
				continue;
			}

			if (tmpSoldier.getxPos() == defSoldier.getxPos() && tmpSoldier.getSoldierCfg().getPosPrior() < defSoldier.getSoldierCfg().getPosPrior()) {
				defSoldier = tmpSoldier;
				continue;
			}
		}

		// 找到防守兵返回
		if (defSoldier != null && defSoldier.isAlive()) {
			return defSoldier;
		}

		return null;
	}

	/**
	 * 损失战力
	 */
	public double calcLostPower() {
		double lostPower = 0;
		for (BattleSoldier soldier : soldierList) {
			lostPower += soldier.getLostPower();
		}

		return lostPower;
	}

	/**
	 * 剩余战力
	 */
	public double calcLeftPower() {
		double lostPower = 0;
		for (BattleSoldier soldier : soldierList) {
			lostPower += soldier.getLeftPower();
		}

		return lostPower;
	}

	// /**
	// * 添加战斗单元
	// */
	// public void addSoldier(BattleSoldier soldier) {
	// soldier.setTroop(this);
	//
	// if (soldier.getType() == SoldierType.BARTIZAN_100) {
	// soldier.setxPos(BattleConst.Const.BARTIZAN_POS.getNumber());
	// } else {
	// soldier.setxPos(curPos);
	// curPos++;
	// }
	//
	// soldierList.add(soldier);
	// }
	/**
	 * 添加战斗单元
	 */
	public void addSoldier(List<BattleSoldier> soldierList) {

		ListMultimap<Integer, BattleSoldier> map = Multimaps.newListMultimap(new TreeMap<>(), ArrayList::new);
		for (BattleSoldier soldier : soldierList) {
			map.put(soldier.getSoldierCfg().getPosPrior(), soldier);
		}

		Collection<Collection<BattleSoldier>> sortedLists = map.asMap().values();
		for (Collection<BattleSoldier> soldiers : sortedLists) {
			for (BattleSoldier soldier : soldiers) {
				soldier.setTroop(this);
				if (Objects.nonNull(soldier.getSolomonPet())) {
					soldier.getSolomonPet().setTroop(this);
				}
				soldier.setxPos(curPos);
				this.soldierList.add(soldier);
				if (soldier.canBeAttack()) {
					this.totalArmyCount += soldier.getOriCnt();
				}
			}
			curPos++;
		}

		for (int i = 0; i < this.soldierList.size(); i++) {
			BattleSoldier soldier = this.soldierList.get(i);
			if (soldier.getEffVal(EffType.EFF_1431) > 0) {
				setSoulLinkClose(false);
			}
			if (soldier.getEffVal(EffType.JIDIEN_1535) > 0) {
				setHasNoJidien(false);
			}

			if (soldier.getEffVal(EffType.HERO_1631) > 0) { // 是巴克用于承伤的坦克
				addBake1631((BattleSoldier_1) soldier);
			}

			if (soldier.getEffVal(EffType.SUPER_SOLDIER_1633) > 0) {
				maxSOLDIER_1633 = Math.max(maxSOLDIER_1633, soldier.getEffVal(EffType.SUPER_SOLDIER_1633));
				jijia1633.add((BattleSoldier_8) soldier);
			}

		}
		
	}

	public void jijia1633Dun() {
		if (jijia1633.isEmpty()) {
			return;
		}
		if (getBattle().getBattleRound() % ConstProperty.getInstance().getEffect1633TimesLimit() != 0) {
			return;
		}
		double dunNum = jijia1633.stream().mapToDouble(BattleSoldier_8::jijia1633DunNum).sum();
		if (dunNum <= 0) {
			return;
		}
		for (BattleSoldier soldier : soldierList) { // 之前的清0
			soldier.setJijia1633(0);
		}
		BattleSoldier defSoldier = getDefSoldier();
		if (Objects.nonNull(defSoldier)) {
			defSoldier.setJijia1633(dunNum);
			getBattle().addDebugLog("### 机甲1633 添加护盾值:   {} 目标-> {}  ", dunNum, defSoldier.getSoldierId());
		}
	}

	List<BattleSoldier_8> jijia1633 = new ArrayList<>();

	/**每个玩家可用的巴克正义拦截 的坦克*/
	ListMultimap<String, BattleSoldier_1> bake1631 = Multimaps.newListMultimap(new LinkedHashMap<>(), ArrayList::new);

	/***/
	public void addBake1631(BattleSoldier_1 soldier) {
		bake1631.put(soldier.getPlayerId(), soldier);
		Collections.sort(bake1631.get(soldier.getPlayerId()), Comparator.comparing(BattleSoldier::getFreeCnt).reversed());
	}

	public void bindBake1631() {
		if (bake1631.isEmpty()) {
			return;
		}
		List<BattleSoldier_1> allbake = new ArrayList<>(bake1631.values());
		for (BattleSoldier_1 soldier : allbake) {
			BattleSoldier target = soldier.getBake1631Protected();
			if (!soldier.isAlive()) { // 自己死掉
				if (Objects.nonNull(target)) {
					target.setBake1631Soldier(null);
				}
				soldier.setBake1631Protected(null);
				bake1631.remove(soldier.getPlayerId(), soldier);
			}
			if (Objects.nonNull(target) && !target.isAlive()) { // 被保护目标死
				soldier.setBake1631Protected(null);
				target.setBake1631Soldier(null);
			}
		}

		List<BattleSoldier> needProtected = new ArrayList<>();
		for (BattleSoldier houpai : soldierList) {
			if (houpai.getType() == SoldierType.TANK_SOLDIER_1) {
				continue;
			}
			if (!houpai.isAlive()) {
				continue;
			}
			if (!houpai.canBeAttack()) {
				continue;
			}
			if (Objects.nonNull(houpai.getBake1631Soldier())) {
				continue;
			}
			needProtected.add(houpai);
		}
		Collections.reverse(needProtected);

		List<String> bakePlayers = new ArrayList<>(bake1631.keySet());
		int effect1631Maxinum = ConstProperty.getInstance().getEffect1631Maxinum();
		for (int i = 0; i < effect1631Maxinum && i < bakePlayers.size(); i++) {
			if (needProtected.isEmpty()) {
				break;
			}

			BattleSoldier_1 soldier = bake1631.get(bakePlayers.get(i)).get(0);
			if (Objects.nonNull(soldier.getBake1631Protected())) {
				continue;
			}

			BattleSoldier target = needProtected.remove(0);
			soldier.setBake1631Protected(target);
			target.setBake1631Soldier(soldier);
			getBattle().addDebugLog("### 巴克   {} 目标绑定-> {}  ", soldier.getSoldierId(), target.getSoldierId());
		}

	}

	/**
	 * 清理，销毁
	 */
	public void clear() {
		for (BattleSoldier soldier : soldierList) {
			soldier.clear();
		}
	}

	public BattleConst.Troop getTroop() {
		return troop;
	}

	/**
	 * 获取参战作用号
	 * 
	 * @return
	 */
	public Map<EffType, Integer> getEffMap() {
		Map<EffType, Integer> effMap = new HashMap<>();
		for (BattleSoldier soldier : soldierList) {
			if (Objects.equals(soldier.getPlayerId(), leaderId)) {
				for (Entry<EffType, Integer> ent : soldier.getEffMapClientShow().entrySet()) {
					EffType eff = ent.getKey();
					if(eff.getNumber() > 1000000){	// 去除服务器自定义做用号
						continue;
					}
					int val = ent.getValue();
					effMap.merge(eff, val, (v1, v2) -> v1 > v2 ? v1 : v2);
				}
			}
		}
		return effMap;
	}

	public BattleConst.WarEff getWarEff() {
		return warEff;
	}

	public void setWarEff(BattleConst.WarEff warEff) {
		this.warEff = warEff;
	}

	public String getLeaderId() {
		return leaderId;
	}

	public void setLeaderId(String leaderId) {
		this.leaderId = leaderId;
	}

	public boolean isSoulLinkClose() {
		return soulLinkClose;
	}

	public void setSoulLinkClose(boolean soulLinkClose) {
		this.soulLinkClose = soulLinkClose;
	}

	public boolean isHasNoJidien() {
		return hasNoJidien;
	}

	public void setHasNoJidien(boolean hasNoJidien) {
		this.hasNoJidien = hasNoJidien;
	}

	public int getTotalArmyCount() {
		return totalArmyCount;
	}

	public int getDeadArmyCount() {
		return deadArmyCount;
	}

	public int getMaxSOLDIER_1633() {
		return maxSOLDIER_1633;
	}

	public int getLast1645round() {
		return last1645round;
	}

	public void setLast1645round(int last1645round) {
		this.last1645round = last1645round;
	}

	public int getBuff1652Cnt() {
		return buff1652Cnt;
	}

	public void setBuff1652Cnt(int buff1652Cnt) {
		this.buff1652Cnt = buff1652Cnt;
	}

	public void addBuff12061Val(int val) {
		buff12061Val += val;
		buff12061Val = Math.min(buff12061Val, ConstProperty.getInstance().getEffect12061MaxValue());
		battle.addDebugLog("空军双将 12061 add:{} , val:{}", val, buff12061Val);
	}

	public int getBuff12061Val() {
		return buff12061Val;
	}

	public void setBuff12061Val(int buff12061Val) {
		this.buff12061Val = buff12061Val;
	}

	public Map<String, Integer> getSkill144Val() {
		return skill144Val;
	}

	public void setSkill144Val(Map<String, Integer> skill144Val) {
		this.skill144Val = skill144Val;
	}

	public void eff12116() {
		try {
			if (getBattle().getBattleRound() % ConstProperty.getInstance().getEffect12116IntervalRound() != 0) {
				return;
			}
			int eff12116Val = soldierList.get(0).getEffVal(EffType.EFF_12116);
			int effect12116Maxinum = soldierList.get(0).getEffect12116Maxinum();

			if (eff12116Val == 0) {
				return;
			}

			List<BattleSoldier> tarList = soldierList.stream().filter(BattleSoldier::isSoldier).filter(BattleSoldier::isAlive)
					.sorted(Comparator.comparingDouble(BattleSoldier::lossRate).reversed().thenComparing(s -> s.getLevel() * -1)
							.thenComparing(BattleSoldier::getBuildingWeight))
					.limit(effect12116Maxinum).collect(Collectors.toList());

			for (BattleSoldier tar : tarList) {
				HawkTuple2<Integer, Integer> buff = HawkTuples.tuple(tar.getBattleRound() + ConstProperty.getInstance().getEffect12116ContinueRound() - 1, eff12116Val);
				tar.setSkill12116Buff(buff);
				tar.addDebugLog(" {}【12116】每第 5 回合开始时，为己方累计战损比率排序前 1 名的战斗单位提供战场保护，使其受到伤害减少 +XX.XX% {}", tar.getUUID(), eff12116Val);
			}
			
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 *   - 护盾承伤 = MIN（剩余盾值，伤害值*0.9）
	 */
	public double forceField(BattleSoldier atk, BattleSoldier def, double hurtVal) {
		if (forceField == 0) {
			return hurtVal;
		}
		double xishou = Math.min(forceField, hurtVal * 0.9);
		forceField -= xishou;
		getBattle().addDebugLog("星穹护盾 {} + {} total:{}", def.getUUID(), xishou, forceField);
		return hurtVal -= xishou;
	}

	public void calForceField() {
		for (BattleSoldier soldier : soldierList) {
			long first = soldier.tupleValue(BattleTupleType.Type.FORCE_FIELD, SoldierType.XXXXXXXXXXXMAN).first;
			if (first <= 0) {
				continue;
			}
			long add = (long) (1000 * first * Math.min(1, soldier.getForceFieldMarch() * 0.00001));
			this.forceFieldMax += add;
			getBattle().addDebugLog("星穹护盾 {} 出征 {} , 配置{} ,护盾 {} total:{}",soldier.getUUID(),soldier.getForceFieldMarch(), first, add, forceFieldMax);
		}
		this.forceField = forceFieldMax;
	}

	public long getForceFieldMax() {
		return forceFieldMax;
	}

	public void setForceFieldMax(int forceFieldMax) {
		this.forceFieldMax = forceFieldMax;
	}

	public long getForceField() {
		return forceField;
	}

	public void setForceField(int forceField) {
		this.forceField = forceField;
	}

}
