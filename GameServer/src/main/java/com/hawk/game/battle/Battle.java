package com.hawk.game.battle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.logging.log4j.message.FormattedMessage;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.tuple.HawkTuple2;
import org.hawk.tuple.HawkTuples;

import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.AtomicLongMap;
import com.hawk.game.GsConfig;
import com.hawk.game.battle.effect.BattleConst;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.config.BattleProtectCancelCfg;
import com.hawk.game.config.BattleProtectLevelCfg;
import com.hawk.game.config.BattleSoldierSkillCfg;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.config.GameConstCfg;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.PBSoldierSkill;
import com.hawk.game.protocol.Const.SoldierType;
import com.hawk.game.protocol.Mail.PB12541Detail;
import com.hawk.game.service.mail.DungeonMailType;
import com.hawk.game.util.GsConst;

/** 战斗类，工作流程如下：
 * <ul>
 * <li>1. new 战斗对象</li>
 * <li>2. new 战斗单元，攻防双方添加数据（添加顺序决定出手顺序）</li>
 * <li>3. 立即战斗，立即出结果</li>
 * <li>4. 遍历战斗结果</li>
 * </ul>
*/
public class Battle {
	/** 战斗发生地点id */
	private int pointId;
	/** 战斗类型 */
	private BattleConst.BattleType type;
	/** 胜利方 */
	private BattleConst.Troop winTroop;
	/** 进攻方 */
	private BattleTroop attackerTroop;
	/** 防守方 */
	private BattleTroop defencerTroop;
	/** 战斗回合数 */
	private int battleRound;
	/** 战斗日志文件名 */
	private String playerId = "";
	boolean saveDebugLog;

	private DungeonMailType duntype = DungeonMailType.NONE;
	/** 战斗日志 */
	private StringBuilder logBuilder = new StringBuilder();
	private List<Integer> eff1535Weight = new ArrayList<>(6);
	private Map<String, PB12541Detail> eff12541map = new HashMap<>();

	public void addDebugLog(final String messagePattern, final Object... arguments) {
		if (!saveDebugLog) {
			return;
		}

		for (int i = 0; i < arguments.length; i++) {
			Object arg = arguments[i];
			if (arg instanceof Double || arg instanceof Float) {
				arguments[i] = String.format("%.5f", arg);
			}
		}
		String stackStr = "";// stackTraceStr();
		FormattedMessage msg = new FormattedMessage(stackStr + messagePattern, arguments);
		logBuilder.append(msg.getFormattedMessage()).append("<br>\n");
	}

	private static String stackTraceStr() {
		try {
			StackTraceElement st = Thread.currentThread().getStackTrace()[3];
			return "[" + Thread.currentThread().getName() + "]" + "[" + st.getFileName().split("\\.")[0] + ":" + st.getMethodName() + ":" + st.getLineNumber() + "]";

		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return "";
	}

	public String getDebugLog() {
		return logBuilder.toString();
	}

	public BattleConst.Troop getWinTroop() {
		return winTroop;
	}

	public int getPointId() {
		return pointId;
	}

	public void setPointId(int pointId) {
		this.pointId = pointId;
	}

	/** 构造函数
	 * 
	 * @param playerId
	 *            战斗发起人
	 * @param type
	 *            战斗类型 @see BattleConst.TYPE_OTHER
	 * @param pointId
	 *            战斗发生地点 */
	public Battle(String playerId, BattleConst.BattleType type, int pointId, String atkLeaderId, String defLeaderId) {
		this.saveDebugLog = GameConstCfg.getInstance().getKeepBattleLog() == 1 && GsConfig.getInstance().isDebug();
		BattleConst.TroopType atkType = BattleConst.TroopType.ALL;
		BattleConst.TroopType defType = BattleConst.TroopType.ALL;

		if (type == BattleConst.BattleType.ATTACK_RES) {
			atkType = BattleConst.TroopType.RES;
			defType = BattleConst.TroopType.RES;

		} else if (type == BattleConst.BattleType.ATTACK_CITY) {
			atkType = BattleConst.TroopType.ATK_CITY;
			defType = BattleConst.TroopType.DEF_CITY;
		} else if (type == BattleConst.BattleType.ATTACK_MONSTER) {
			atkType = BattleConst.TroopType.ATK_MONSTER;
		} else if (type == BattleConst.BattleType.YURI_YURIREVENGE) {
			defType = BattleConst.TroopType.DEF_YURI_REVENGE;
		}

		attackerTroop = new BattleTroop(this, atkType, BattleConst.Troop.ATTACKER, atkLeaderId);
		defencerTroop = new BattleTroop(this, defType, BattleConst.Troop.DEFENDER, defLeaderId);

		this.type = type;
		this.pointId = pointId;
		this.playerId = playerId;

		Splitter.on(",").split(ConstProperty.getInstance().getEffect1535mum()).forEach(str -> eff1535Weight.add(NumberUtils.toInt(str)));
	}

	/** 清理，销毁 */
	public void clear() {
		attackerTroop.clear();
		defencerTroop.clear();
	}

	/** 立即进行战斗
	 * 
	 * @return 获胜方 */
	public BattleConst.Troop warfare() {
		long startTime = HawkTime.getMillisecond();
		HawkLog.logPrintln("battle playerId : {} pointId : {}", playerId, pointId);

		// 取战斗相关常量
		final int MAX_ROUND = ConstProperty.getInstance().getBattleRoundMax(); // 战斗最大回合数
		int round = 1;
		int action = 1;
		this.battleRound = round;
		boolean needProtected = false;// 是否免保护
		BattleProtectLevelCfg protectCfg = null;
		if (BattleConst.WarEff.CITY_DEF.check(defencerTroop.getWarEff()) && duntype == DungeonMailType.NONE) {
			Player defPlayer = GlobalData.getInstance().makesurePlayer(defencerTroop.getLeaderId());
			Player atkPlayer = GlobalData.getInstance().makesurePlayer(attackerTroop.getLeaderId());
			if (Objects.nonNull(defPlayer) && Objects.nonNull(atkPlayer)) {
				BattleProtectCancelCfg cancelCfg = HawkConfigManager.getInstance().getConfigByKey(BattleProtectCancelCfg.class, defPlayer.getCityLevel());
				if (Objects.nonNull(cancelCfg) && defencerTroop.totalSoldier() > cancelCfg.getNum()) {
					protectCfg = HawkConfigManager.getInstance().getConfigByKey(BattleProtectLevelCfg.class, atkPlayer.getCityLevel() - defPlayer.getCityLevel());
					needProtected = Objects.nonNull(protectCfg) ? true : false;
				}
			}
		}
		boolean triggerWeakProtect = false;
		
		attackerTroop.calForceField();
		defencerTroop.calForceField();
		
		attackerTroop.getSoldierList().forEach(BattleSoldier::flushExtryLog);
		defencerTroop.getSoldierList().forEach(BattleSoldier::flushExtryLog);
		attackerTroop.getSoldierList().forEach(BattleSoldier::initKezhi);
		defencerTroop.getSoldierList().forEach(BattleSoldier::initKezhi);
		attackerTroop.getSoldierList().forEach(BattleSoldier::beforeWarfare);
		defencerTroop.getSoldierList().forEach(BattleSoldier::beforeWarfare);
		effect11041Damage(attackerTroop, defencerTroop);
		effect11042Debuf(attackerTroop, defencerTroop);
		effect12541Debuf(attackerTroop, defencerTroop);
		effect12641Damage(attackerTroop, defencerTroop);
		skill14601Buff(attackerTroop);
		skill14601Buff(defencerTroop);
		skill44601Debuff(attackerTroop, defencerTroop);
		skill44601Debuff(defencerTroop, attackerTroop);
		for (; round <= MAX_ROUND + 1; round++) {
			addDebugLog("<br>\nround={} action={}", round, action);
			this.battleRound = round;
			beforeRoundStart();
			beforeRoundStart2();
			beforeRoundStart3();
			// 取攻防双方的进攻战斗单元
			BattleSoldier atkAtkSoldier = attackerTroop.nextAtkSoldier();
			BattleSoldier defAtkSoldier = defencerTroop.nextAtkSoldier();

			if (atkAtkSoldier == null) {
				winTroop = BattleConst.Troop.DEFENDER;
				break;
			}

			if (atkAtkSoldier != null && defAtkSoldier == null) {
				winTroop = BattleConst.Troop.ATTACKER;
				break;
			}

			addExtHurt1305(attackerTroop);
			addExtHurt1305(defencerTroop);
			heroJiDiEnRoundStart(attackerTroop, defencerTroop);
			heroJiDiEnRoundStart(defencerTroop, attackerTroop);
			attackerTroop.bindBake1631();
			defencerTroop.bindBake1631();
			attackerTroop.jijia1633Dun();
			defencerTroop.jijia1633Dun();
			attackerTroop.eff12116();
			defencerTroop.eff12116();

			while (true) {
				// 攻方先行
				if (atkAtkSoldier != null) {
					if (atkAtkSoldier.isAlive() && round <= MAX_ROUND + atkAtkSoldier.extryAtkRount()) {
						atkAtkSoldier.tryMoveOrAttack(defencerTroop, action++);
					}
					atkAtkSoldier = attackerTroop.nextAtkSoldier();
				}

				// 攻防反转
				if (defAtkSoldier != null) {
					if (defAtkSoldier.isAlive() && round <= MAX_ROUND + defAtkSoldier.extryAtkRount()) {
						defAtkSoldier.tryMoveOrAttack(attackerTroop, action++);
					}
					defAtkSoldier = defencerTroop.nextAtkSoldier();
				}

				// 都打完了退出本回合
				if (atkAtkSoldier == null && defAtkSoldier == null) {
					break;
				}
			}

			roundEnd();
			heroJiDiEnRoundEnd(attackerTroop);
			heroJiDiEnRoundEnd(defencerTroop);
			attackerTroop.setBuff1652Cnt(0);
			defencerTroop.setBuff1652Cnt(0);
			// 判断防守兵
			if (attackerTroop.getDefSoldier() == null) {
				winTroop = BattleConst.Troop.DEFENDER;
				break;
			}

			if (defencerTroop.getDefSoldier() == null) {
				winTroop = BattleConst.Troop.ATTACKER;
				break;
			}

			if (needProtected) {
				if (!triggerWeakProtect) {
					if (protectCfg.getTriggerround() == 0) {
						triggerWeakProtect = true;
					}
					if (protectCfg.getTriggerround() >= round
							&& defencerTroop.lossRate() > protectCfg.getLossrate()
							&& defencerTroop.lossRate() > attackerTroop.lossRate()) {
						triggerWeakProtect = true;
					}
				}

				if (triggerWeakProtect
						&& attackerTroop.totalSoldier() > defencerTroop.totalSoldier() * 2
						&& defencerTroop.lossRate() > (1 - protectCfg.getPercent())) {
					addDebugLog("@@@@@@@@@@@@@@ 保护机制触发战斗结束 ");
					winTroop = BattleConst.Troop.ATTACKER;
					break;
				}
			}

		}

		// 最大回合数还未分出胜负，则比较双方剩余战力
		double atkLosePower = attackerTroop.calcLostPower();
		double defLosePower = defencerTroop.calcLostPower();

		if (winTroop == null) {
			winTroop = defLosePower > atkLosePower ? BattleConst.Troop.ATTACKER : BattleConst.Troop.DEFENDER;
		}

		int costtime = (int) (HawkTime.getMillisecond() - startTime);
		HawkLog.logPrintln("battle over winTroop {} costtime : {}ms", winTroop, costtime);

		addDebugLog("lost power {} VS {}", atkLosePower, defLosePower);
		// debug模式输出战斗日志到缓存

		saveDebugInfo(costtime);

		return winTroop;
	}

	private void skill44601Debuff(BattleTroop attackerTroop, BattleTroop defencerTroop) {
		if(!BattleConst.WarEff.MASS.check(attackerTroop.getWarEff()) ){
			return;
		}
		BattleSoldierSkillCfg scfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierSkillCfg.class, 44601);
		if (scfg == null) {
			return;
		}
		int effPer = attackerTroop.getSoldierList()
				.stream().map(u -> u.getSkill(PBSoldierSkill.SOLDIER_SKILL_44601).getP2IntVal())
				.sorted(Comparator.comparingInt(Integer::intValue).reversed())
				.limit(scfg.getP3IntVal())
				.mapToInt(Integer::intValue)
				.sum();
		if (effPer <= 0) {
			return;
		}
		for (BattleSoldier soldier : defencerTroop.getSoldierList()) {
			if (!scfg.getP1().contains(soldier.getType().getNumber() + "")) {
				continue;
			}
			addDebugLog("【44601】{} - 技能效果：集结战斗开始时喷射烟雾，使敌方全体单位攻击减少+{}%（该效果可加，至多X层）",soldier.getUUID(), effPer);
			soldier.setDebuff44601(effPer);
		}
		
	}

	private void skill14601Buff(BattleTroop troop) {
		BattleSoldierSkillCfg scfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierSkillCfg.class, 14601);
		if (scfg == null) {
			return;
		}
		int effPer = troop.getSoldierList()
				.stream().map(u -> u.getSkill(PBSoldierSkill.SOLDIER_SKILL_14601).getP2IntVal())
				.sorted(Comparator.comparingInt(Integer::intValue).reversed())
				.limit(scfg.getP3IntVal())
				.mapToInt(Integer::intValue)
				.sum();
		if (effPer <= 0) {
			return;
		}

		for (BattleSoldier soldier : troop.getSoldierList()) {
			if (!scfg.getP1().contains(soldier.getType().getNumber() + "")) {
				continue;
			}
			addDebugLog("【14601】{} 战斗开始时获得纳米护盾，使己方前排单位受到伤害减少 +{}%（该效果可叠加，至多 X 层，持续X回合）",soldier.getUUID(), effPer);
			HawkTuple2<Integer, Integer> skill14601Buff = HawkTuples.tuple(scfg.getP4IntVal(), effPer);
			soldier.setSkill14601Buff(skill14601Buff);
		}
	}

	private void effect12641Damage(BattleTroop attackerTroop, BattleTroop defencerTroop) {
		try {
			List<BattleSoldier> eff11041Soldiers = attackerTroop.getSoldierList().stream().filter(s -> s.getEffVal(EffType.EFF_12641) > 0).collect(Collectors.toList());
			if (eff11041Soldiers.isEmpty()) {
				return;
			}
			List<String> pids = defencerTroop.getSoldierList().stream().map(s -> s.getPlayerId()).distinct().collect(Collectors.toList());
			if (pids.isEmpty()) {
				return;
			}
			Collections.shuffle(pids);
			int i = 0;
			for (BattleSoldier soldier : eff11041Soldiers) {
				String tarpid = pids.get(i % pids.size());
				i++;
				addDebugLog("12641~12642】进攻战斗开始时，随机选中某指挥官，对其后排所有部队进行一轮轰炸");
				List<BattleSoldier> tarses = defencerTroop.getSoldierList(tarpid);
				for (BattleSoldier tar : tarses) {
					if (tar.isJinZhan()) {
						continue;
					}
					// 即实际伤害 = 伤害率 * 修正系数 *基础伤害 *（1 + 各类加成）
					double hurtPer = soldier.getEffVal(EffType.EFF_12641) * GsConst.EFF_PER;
					soldier.attackOnce(tar, BattleSoldier.QIAN_PAI_MAX, 1 - hurtPer, Integer.MAX_VALUE, false);
					double debufVal = soldier.getEffVal(EffType.EFF_12642) * GsConst.EFF_PER
							* ConstProperty.getInstance().effect12641SoldierAdjustMap.getOrDefault(tar.getType(), 10000);
					HawkTuple2<Integer, Double> debuf = HawkTuples.tuple(ConstProperty.getInstance().effect12641ContinueRound, debufVal);
					tar.setSkill12642Debuff(debuf);
				}
			}

		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	private void effect11041Damage(BattleTroop attackerTroop, BattleTroop defencerTroop) {
		try {
			List<BattleSoldier> eff11041Soldiers = attackerTroop.getSoldierList().stream().filter(s -> s.getEffVal(EffType.EFF_11041) > 0).collect(Collectors.toList());
			if (eff11041Soldiers.isEmpty()) {
				return;
			}
			List<String> pids = defencerTroop.getSoldierList().stream().map(s -> s.getPlayerId()).distinct().collect(Collectors.toList());
			if (pids.isEmpty()) {
				return;
			}
			Collections.shuffle(pids);
			int i = 0;
			for (BattleSoldier soldier : eff11041Soldiers) {
				String tarpid = pids.get(i % pids.size());
				i++;
				addDebugLog("【11041】进攻战斗时，在战斗开始时，随机选中敌方某指挥官的全部部队进行 1 轮核弹轰炸（伤害率：XX%；此效果对步兵 翻倍）");
				List<BattleSoldier> tarses = defencerTroop.getSoldierList(tarpid);
				for (BattleSoldier tar : tarses) {
					// 即实际伤害 = 伤害率 * 修正系数 *基础伤害 *（1 + 各类加成）
					double hurtPer = ConstProperty.getInstance().getEffect11041DamageAdjustMap().getOrDefault(tar.getType(), 0) * GsConst.EFF_PER
							* soldier.getEffVal(EffType.EFF_11041) * GsConst.EFF_PER;
					soldier.attackOnce(tar, BattleSoldier.QIAN_PAI_MAX, 1 - hurtPer, Integer.MAX_VALUE, false);
				}
			}

		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	private void effect11042Debuf(BattleTroop attackerTroop, BattleTroop defencerTroop) {
		try {
			List<BattleSoldier> eff11041Soldiers = attackerTroop.getSoldierList().stream().filter(s -> s.getEffVal(EffType.EFF_11042) > 0).collect(Collectors.toList());
			if (eff11041Soldiers.isEmpty()) {
				return;
			}
			List<String> pids = defencerTroop.getSoldierList().stream().map(s -> s.getPlayerId()).distinct().collect(Collectors.toList());
			if (pids.isEmpty()) {
				return;
			}
			Collections.shuffle(pids);
			int i = 0;
			for (BattleSoldier soldier : eff11041Soldiers) {
				String tarpid = pids.get(i % pids.size());
				i++;
				boolean type = HawkRand.randInt(1) > 0;
				addDebugLog("11042~11043】进攻战斗时，在战斗开始时，随机选中敌方某指挥官的全部部队进行 1 轮风暴干扰；使其随机陷入下述状态之一效果 {}", type ? "A" : "B");
				List<BattleSoldier> tarses = defencerTroop.getSoldierList(tarpid);
				for (BattleSoldier tar : tarses) {
					if (type) {
						double debufVal = soldier.getEffVal(EffType.EFF_11042) * GsConst.EFF_PER
								* ConstProperty.getInstance().getEffect11042AdjustMap().getOrDefault(tar.getType(), 0);
						HawkTuple2<Integer, Double> debuf = HawkTuples.tuple(ConstProperty.getInstance().getEffect11042ContinueRound(), debufVal);
						tar.setSkill11042Debuff(debuf);
					} else {
						double debufVal = soldier.getEffVal(EffType.EFF_11043) * GsConst.EFF_PER
								* ConstProperty.getInstance().getEffect11043AdjustMap().getOrDefault(tar.getType(), 0);
						HawkTuple2<Integer, Double> debuf = HawkTuples.tuple(ConstProperty.getInstance().getEffect11043ContinueRound(), debufVal);
						tar.setSkill11043Debuff(debuf);
					}
				}
			}

		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	private void effect12541Debuf(BattleTroop attackerTroop, BattleTroop defencerTroop) {
		try {
			List<BattleSoldier> eff12541Soldiers = attackerTroop.getSoldierList().stream().filter(s -> s.getEffVal(EffType.EFF_12541) > 0).collect(Collectors.toList());
			if (eff12541Soldiers.isEmpty()) {
				return;
			}
			List<String> pids = defencerTroop.getSoldierList().stream().map(s -> s.getPlayerId()).distinct().collect(Collectors.toList());
			if (pids.isEmpty()) {
				return;
			}
			Collections.shuffle(pids);

			Map<BattleSoldier, Integer> weightMap = new HashMap<>();
			for (BattleSoldier tar : defencerTroop.getSoldierList()) {
				weightMap.put(tar, tar.getFreeCnt());
			}

			for (int i = 0; i < eff12541Soldiers.size() && !weightMap.isEmpty(); i++) {
				BattleSoldier soldier = eff12541Soldiers.get(i);
				BattleSoldier tar12541 = HawkRand.randomWeightObject(weightMap);
				weightMap.remove(tar12541);
				soldier.setEff12541Soldier(tar12541);
				addDebugLog("【12541】战斗效果：进攻其他指挥官基地战斗开始时，{} 随机选中敌方{}", soldier.getUUID(), tar12541.getUUID());
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	private void beforeRoundStart() {
		for (BattleSoldier soldier : attackerTroop.getSoldierList()) {
			if (!soldier.isAlive()) {
				continue;
			}
			soldier.roundStart();
			if (soldier.getSolomonPet() != null) {
				soldier.getSolomonPet().roundStart();
			}
		}
		for (BattleSoldier soldier : defencerTroop.getSoldierList()) {
			if (!soldier.isAlive()) {
				continue;
			}
			soldier.roundStart();
			if (soldier.getSolomonPet() != null) {
				soldier.getSolomonPet().roundStart();
			}
		}
	}

	private void beforeRoundStart2() {
		for (BattleSoldier soldier : attackerTroop.getSoldierList()) {
			if (!soldier.isAlive()) {
				continue;
			}
			soldier.roundStart2();
		}
		for (BattleSoldier soldier : defencerTroop.getSoldierList()) {
			if (!soldier.isAlive()) {
				continue;
			}
			soldier.roundStart2();
		}
	}

	private void beforeRoundStart3() {
		for (BattleSoldier soldier : attackerTroop.getSoldierList()) {
			if (!soldier.isAlive()) {
				continue;
			}
			soldier.roundStart3();
			if (soldier.getSolomonPet() != null) {
				soldier.getSolomonPet().roundStart3();
			}
		}
		for (BattleSoldier soldier : defencerTroop.getSoldierList()) {
			if (!soldier.isAlive()) {
				continue;
			}
			soldier.roundStart3();
			if (soldier.getSolomonPet() != null) {
				soldier.getSolomonPet().roundStart3();
			}
		}
	}

	private void roundEnd() {
		for (BattleSoldier soldier : attackerTroop.getSoldierList()) {
			if (!soldier.isAlive()) {
				continue;
			}
			soldier.roundEnd();
		}
		for (BattleSoldier soldier : defencerTroop.getSoldierList()) {
			if (!soldier.isAlive()) {
				continue;
			}
			soldier.roundEnd();
		}
	}

	private void heroJiDiEnRoundStart(BattleTroop xi, BattleTroop beiXi) {
		if (xi.isHasNoJidien()) {
			return;
		}
		AtomicLongMap<String> totalPowMap = AtomicLongMap.create();
		Map<String, BattleSoldier> xiMap = new HashMap<>();
		for (BattleSoldier so : xi.getSoldierList()) {
			if (so.isAlive() && so.canBeAttack() && so.getEffVal(EffType.JIDIEN_1535) > 0) {
				double soPower = (so.getFreeCnt() + so.getJiDiEnXi()) * (1 - so.getShadowRate() * GsConst.EFF_PER) * so.getSoldierCfg().getPower();
				totalPowMap.addAndGet(so.getPlayerId(), (int) soPower);
				xiMap.put(so.getPlayerId(), so);
			}
		}
		if (xiMap.isEmpty()) {
			xi.setHasNoJidien(true);
		}

		for (BattleSoldier xiSo : xiMap.values()) {
			List<BattleSoldier> defList = beiXi.getSoldierList().stream()
					.filter(ds -> ds.canBeAttack())
					.filter(ds -> ds.isAlive())
					.filter(ds -> ds.getJiDiEnXi() == 0)
					.sorted(Comparator.comparingInt(BattleSoldier::getFreeCnt).reversed())
					.limit(eff1535Weight.size())
					.collect(Collectors.toList());
			if (defList.isEmpty()) {
				return;
			}
			Map<BattleSoldier, Integer> map = Maps.newHashMapWithExpectedSize(defList.size());
			for (int i = 0; i < defList.size(); i++) {
				map.put(defList.get(i), eff1535Weight.get(i));
			}
			BattleSoldier result = HawkRand.randomWeightObject(map);
			double eff1535Per = xiSo.getEffVal(EffType.JIDIEN_1535) * GsConst.EFF_PER;
			double eff1535power = ConstProperty.getInstance().getEffect1535power() * GsConst.EFF_PER;
			int xiNum = (int) Math.min(result.getFreeCnt() * eff1535Per, totalPowMap.get(xiSo.getPlayerId()) * eff1535power / result.getSoldierCfg().getPower());
			xiNum = Math.max(0, xiNum);
			result.setJiDiEnXi(xiSo.getPlayerId(), xiNum, xiSo.getEffVal(EffType.JIDIEN_1536), xiSo.getEffVal(EffType.JIDIEN_1537));

			addDebugLog("吉迪恩吸入 -> {} num = {}", result.getSoldierId(), xiNum);
		}

	}

	private void heroJiDiEnRoundEnd(BattleTroop troop) {
		for (BattleSoldier so : troop.getSoldierList()) {
			so.setJiDiEnXi("", 0, 0, 0);
		}

	}

	private void addExtHurt1305(BattleTroop troop) {
		if (this.battleRound == 1) {
			return;
		}
		for (BattleSoldier sd : troop.getSoldierList()) {
			sd.setExtryHurt(0);
		}
		SoldierType atkent = rountHurtMaxSoldierType(this.battleRound - 1, troop.getSoldierList());
		if (atkent == SoldierType.FOOT_SOLDIER_5) {
			for (BattleSoldier sd : troop.getSoldierList()) {
				sd.addExtryHurt(sd.getEffVal(EffType.SUPER_SOLDIER_1305));
				if (sd.getType() == SoldierType.FOOT_SOLDIER_5) {
					sd.addExtryHurt(sd.getEffVal(EffType.SUPER_SOLDIER_1304));
				}
			}
		} else if (atkent == SoldierType.FOOT_SOLDIER_6) {
			for (BattleSoldier sd : troop.getSoldierList()) {
				sd.addExtryHurt(sd.getEffVal(EffType.SOLIDER_6_1314));
			}
		}
	}

	public SoldierType rountHurtMaxSoldierType(int round, List<BattleSoldier> soldierlist) {
		TreeMap<Integer, SoldierType> smap = Maps.newTreeMap();
		for (BattleSoldier soldier : soldierlist) {
			int val = soldier.getRountHurtVal(round);
			if (val > 0) {
				smap.put(val, soldier.getType());
			}
		}

		if (smap.isEmpty()) {
			return SoldierType.XXXXXXXXXXXMAN;
		}

		return smap.lastEntry().getValue();
	}

	/** 攻方：添加战斗单元 */
	public void addAtkSoldier(List<BattleSoldier> soldier) {
		attackerTroop.addSoldier(soldier);
	}

	/** 防方：添加战斗单元 */
	public void addDefSoldier(List<BattleSoldier> soldier) {
		defencerTroop.addSoldier(soldier);
	}

	/** 攻方：按玩家ID获取战斗单元列表
	 * 
	 * @param playerId */
	public List<BattleSoldier> getAtkSoldierList(String playerId) {
		return attackerTroop.getSoldierList(playerId);
	}

	/** 防方：按玩家ID获取战斗单元列表
	 * 
	 * @param playerId */
	public List<BattleSoldier> getDefSoldierList(String playerId) {
		return defencerTroop.getSoldierList(playerId);
	}

	/** 获取进攻方类型
	 * 
	 * @return */
	public BattleConst.TroopType getAtkTroopType() {
		return attackerTroop.getTroopType();
	}

	/** 获取防御方类型
	 * 
	 * @return */
	public BattleConst.TroopType getDefTroopType() {
		return defencerTroop.getTroopType();
	}

	/** 获取战斗类型
	 * 
	 * @return */
	public BattleConst.BattleType getType() {
		return type;
	}

	/** 获取实际战斗回合数
	 * 
	 * @return */
	public int getBattleRound() {
		return battleRound;
	}

	private void saveDebugInfo(int costtime) {
		if (!saveDebugLog) {
			return;
		}

		addDebugLog("{} playerId={} pointId={} costtime={}", HawkTime.formatNowTime(), playerId, pointId, costtime);

		addDebugLog("atk {}", winTroop == BattleConst.Troop.ATTACKER ? "win" : "lose");
		for (BattleSoldier soldier : attackerTroop.getSoldierList()) {
			addDebugLog(soldier.toString());
			addDebugLog(
					"Effect : " + soldier.getEffMap().entrySet().stream().map(ent -> "[" + ent.getKey().getNumber() + "->" + ent.getValue() + "]").collect(Collectors.joining()));
		}

		addDebugLog("def");
		for (BattleSoldier soldier : defencerTroop.getSoldierList()) {
			addDebugLog(soldier.toString());
			addDebugLog(
					"Effect : " + soldier.getEffMap().entrySet().stream().map(ent -> "[" + ent.getKey().getNumber() + "->" + ent.getValue() + "]").collect(Collectors.joining()));
		}

		if (saveDebugLog) {
			LocalRedis.getInstance().updatePlayerBattleLog("LLL", logBuilder.toString());
		}
	}

	public BattleTroop getAttacker() {
		return attackerTroop;
	}

	public BattleTroop getDefencer() {
		return defencerTroop;
	}

	public void setSaveDebugLog(boolean saveDebugLog) {
		this.saveDebugLog = saveDebugLog;
	}

	public boolean isSaveDebugLog() {
		return saveDebugLog;
	}

	public DungeonMailType getDuntype() {
		return duntype;
	}

	public void setDuntype(DungeonMailType duntype) {
		this.duntype = duntype;
	}

	public Map<String, PB12541Detail> getEff12541map() {
		return eff12541map;
	}

}
