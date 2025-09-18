package com.hawk.game.battle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.hawk.os.HawkException;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.os.HawkWeightFactor;
import org.hawk.tuple.HawkTuple2;
import org.hawk.tuple.HawkTuple3;
import org.hawk.tuple.HawkTuples;

import com.hawk.game.battle.effect.BattleConst;
import com.hawk.game.config.BattleSoldierSkillCfg;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.PBSoldierSkill;
import com.hawk.game.protocol.Const.SoldierType;
import com.hawk.game.util.GsConst;

/**
 * 3战斗飞行器（骑兵 冲锋）
 * 
 * @author lwt
 * @date 2017年12月29日
 */
public class BattleSoldier_3 extends IPlanSoldier {
	boolean trigerSkill_3;
	boolean atkFootSoldier;
	int skill3Count; // 累计空袭次数
	final int ONCE = 300;
	int eff1452Debuf;
	boolean super303;// 【1543】轰炸机（兵种类型=3）攻击时，每3个回合（第一次释放是接敌后，接敌概念：首次进行距离内攻击）必定执行一次技能303（技能skill表，30301，30302，30303），并强制本次修改本技能目标选区逻辑，选择目标最后排目标。实际伤害 = 基础伤害*（1 + 【1543】+其他伤害加成）+【1544】*eff
	double xishou1545; // 护盾吸收
	Map<Integer, Integer> eff1553Debuf;
	Map<Integer, Integer> eff1596buf;
	int SKILL_3_trigger_cnt;
	int ARMOUR_11006_round;
	private int debufSkill224Val;
	int skill14DefVal;
	int debuff1656round; // 命中 兵种类型 = 3 的负面效果，负面状态持续期间，禁止该目标，释放兵种技能302 （智能护盾），并清除作用号【1544】，智能护盾吸收反击伤害累计的值，禁止【1544】继续累计值
	int hero12051AtkHurtPct;
	int hero12412AtkHurtPct;
	BattleSoldier_4 zhu1645;

	int eff12066Add;
	int effect12066AtkTimes;
	int attackedCnt;
	private HawkTuple2<Integer, Integer> skill12413Buff;
	private HawkTuple2<Integer, Integer> skill12443Buff;
	int effect12414num;
	@Override
	public void roundStart() {
		super.roundStart();
		int battleRound = getTroop().getBattle().getBattleRound();
		super303 = battleRound > 1 && battleRound % 3 == 0 && getEffVal(EffType.MDS_1543) > 0;
	}

	@Override
	public void beforeAttack(BattleSoldier defSoldier) {
		super.beforeAttack(defSoldier);
		hero12051(defSoldier);
	}

	private void hero12051(BattleSoldier defSoldier) {
		try {
			if (getEffVal(EffType.EFF_12051) <= 0) {
				return;
			}
			if (getBattleRound() % ConstProperty.getInstance().getEffect12051AtkRound() != 0) {
				return;
			}

			int atkTimes = 0;
			if (getEffVal(EffType.EFF_12271) > 0) {
				atkTimes = ConstProperty.getInstance().getEffect12271AtkTimesForPerson();
				if (BattleConst.WarEff.MASS.check(getTroop().getWarEff())) {
					atkTimes = ConstProperty.getInstance().getEffect12271AtkTimesForMass();
				}
			} else {
				atkTimes = ConstProperty.getInstance().getEffect12051AtkTimesForPerson();
				if (BattleConst.WarEff.MASS.check(getTroop().getWarEff())) {
					atkTimes = ConstProperty.getInstance().getEffect12051AtkTimesForMass();
				}
			}
			if (getEffVal(EffType.HERO_12414) > 0) {
				atkTimes += ConstProperty.getInstance().getEffect12414AtkTimesForPerson();
				addDebugLog("【12414】轰炸机在释放轮番轰炸技能时，额外附加如下效果:潜能爆发: 个人战时，轮番轰炸发动轮次额外 +{} ", ConstProperty.getInstance().getEffect12414AtkTimesForPerson());
			}
			
			atkTimes += effect12066AtkTimes;
			
			SoldierType lastTarType = SoldierType.PLANE_SOLDIER_4;
			BattleTroop tarTroop = defSoldier.getTroop();
			if ((getEffVal(EffType.HERO_12411) + getEffVal(EffType.HERO_12431) )> HawkRand.randInt(10000)) {
				boolean hasYuan = tarTroop.getSoldierList().stream().filter(s->s.isAlive()).filter(s->s.isYuanCheng()).findAny().isPresent();
				if(!hasYuan){
					lastTarType = SoldierType.PLANE_SOLDIER_3;
					addDebugLog("【12411】轰炸机在释放轮番轰炸技能时，额外附加如下效果:领域突破: 若当前敌方无远程单位，轮番轰炸有 XX.XX% 的概率将目标调整至敌方近战单位");
				}
			}
			
			for (int i = 0; i < atkTimes; i++) {
				lastTarType = hero12051Atk(tarTroop, i, lastTarType);
				if (lastTarType == null) {
					break;
				}
			}
			setSkill12413Buff(HawkTuples.tuple(getBattleRound() + ConstProperty.getInstance().getEffect12413ContinueRound() - 1, getEffVal(EffType.HERO_12413)));
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	private SoldierType hero12051Atk(BattleTroop tarTroop, int atktime, SoldierType lastTarType) {
		boolean yuan = isYuanCheng(lastTarType);
		
		Map<SoldierType, BattleSoldier> map = new HashMap<>(4);
		// 【狙击兵 = 6，攻城车 = 7，突击步兵 = 5，直升机 = 4】
		List<BattleSoldier> soldierList = new ArrayList<>(tarTroop.getSoldierList());
		Collections.shuffle(soldierList);
		for (BattleSoldier tar : soldierList) {
			if (map.containsKey(tar.getType())) {
				continue;
			}
			if (yuan && tar.isAlive() && tar.isYuanCheng()) {
				map.put(tar.getType(), tar);
			}
			if (!yuan && tar.isAlive() && tar.isJinZhan()) {
				map.put(tar.getType(), tar);
			}
		}
		SoldierType[] typs;
		int index;
		if (yuan) {
			typs = new SoldierType[] { SoldierType.FOOT_SOLDIER_6, SoldierType.CANNON_SOLDIER_7, SoldierType.FOOT_SOLDIER_5, SoldierType.PLANE_SOLDIER_4,
					SoldierType.FOOT_SOLDIER_6, SoldierType.CANNON_SOLDIER_7, SoldierType.FOOT_SOLDIER_5, SoldierType.PLANE_SOLDIER_4 };
			index = lastTarType == SoldierType.FOOT_SOLDIER_6 ? 1 : lastTarType == SoldierType.CANNON_SOLDIER_7 ? 2 : lastTarType == SoldierType.FOOT_SOLDIER_5 ? 3 : 0;
		} else {
			typs = new SoldierType[] { SoldierType.TANK_SOLDIER_1, SoldierType.CANNON_SOLDIER_8, SoldierType.TANK_SOLDIER_2, SoldierType.PLANE_SOLDIER_3,
					SoldierType.TANK_SOLDIER_1, SoldierType.CANNON_SOLDIER_8, SoldierType.TANK_SOLDIER_2, SoldierType.PLANE_SOLDIER_3 };
			index = lastTarType == SoldierType.TANK_SOLDIER_1 ? 1 : lastTarType == SoldierType.CANNON_SOLDIER_8 ? 2 : lastTarType == SoldierType.TANK_SOLDIER_2 ? 3 : 0;
		}
		
		BattleSoldier target = null;
		for (int i = index; i < typs.length; i++) {
			SoldierType type = typs[i];
			if (map.containsKey(type)) {
				target = map.get(type);
				break;
			}
		}
		if (target == null) {
			return null;
		}
		hero12051AtkHurtPct = getEffVal(EffType.EFF_12051) + (atktime + 1) * getEffVal(EffType.EFF_12052) + eff12066Add + getEffVal(EffType.HERO_12432);
		if (target.getType() == SoldierType.CANNON_SOLDIER_7) {
			hero12051AtkHurtPct += getEffVal(EffType.EFF_12071);
		}
		super.attackOnce(target, QIAN_PAI_MAX, 0, Integer.MAX_VALUE, false);
		if (getEffVal(EffType.HERO_12414) > 0) {
			effect12414num++;
			effect12414num = Math.min(effect12414num, ConstProperty.getInstance().getEffect12414Maxinum());
			addDebugLog("【12414】轰炸机在释放轮番轰炸技能时，额外附加如下效果:潜能爆发: 个人战时， 且每发动 1 次轮番轰炸后，自身受到攻击时伤害减少 +XX.XX%（该效果可叠加，{} 层）", effect12414num);
		}
		hero12051AtkHurtPct = 0;
		eff12412Atk(target);
		return target.getType();
	}

	private void eff12412Atk(BattleSoldier target) {
		if (getEffVal(EffType.HERO_12412) <= 0) {
			return;
		}
		List<BattleSoldier> tarList = target.getTroop().getSoldierList().stream().filter(s -> s == target || s.canBeAttack())
				.sorted(Comparator.comparingInt(BattleSoldier::getxPos)).collect(Collectors.toList());
		final int size = tarList.size();
		int qian = tarList.indexOf(target);
		int hou = qian;
		int max = ConstProperty.getInstance().getEffect12412AtkNums();
		if (getEffVal(EffType.HERO_12433) > RandomUtils.nextInt(GsConst.RANDOM_MYRIABIT_BASE)) {
			max += ConstProperty.getInstance().getEffect12433AddAtkNum();
		}
		List<BattleSoldier> atktarList = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			qian--;
			hou++;
			if (hou >= 0 && hou < size && max > 0) {
				atktarList.add(tarList.get(hou));
				max--;
			}
			if (qian >= 0 && qian < size && max > 0) {
				atktarList.add(tarList.get(qian));
				max--;
			}
		}
		for (BattleSoldier tar : atktarList) {
			for (int i = 0; i < ConstProperty.getInstance().getEffect12412AtkTimes(); i++) {
				hero12412AtkHurtPct = getEffVal(EffType.HERO_12412);
				if (tar.isFoot()) {
					hero12412AtkHurtPct += getEffVal(EffType.HERO_12434);
					addDebugLog("【12434】战技持续期间，温压爆弹命中步兵单位时，伤害额外 +{}", getEffVal(EffType.HERO_12434));
				}
				addDebugLog("【12412】轰炸机在释放轮番轰炸技能时，额外附加如下效果:温压爆弹: 轮番轰炸每次命中敌方单位后，额外向其所在区域投放 1 枚温压弹，对其距离最近的 2 个敌方单位造成 1 次轰爆伤害（伤害率: XX.XX%）");
				super.attackOnce(tar, QIAN_PAI_MAX, 0, Integer.MAX_VALUE, false);
				hero12412AtkHurtPct = 0;
			}
		}
	}

	@Override
	public SoldierType getType() {
		return SoldierType.PLANE_SOLDIER_3;
	}

	@Override
	public void attackOnce(BattleSoldier defSoldier, int atkTimes, double hurtPer, int atkDis) {
		BattleSoldierSkillCfg skill3 = getSkill(PBSoldierSkill.PLANE_SOLDIER_3_SKILL_3);
		if (atkTimes == 0) {
			if (super303) {
				trigerSkill_3 = trigerSkill(skill3, 10000);
			} else {
				int trigger = skill3.getTrigger() + getEffVal(EffType.SOLDIER_SKILL_TRIGER_303) - debuff1553Val() - eff12143DebuffVal();
				trigerSkill_3 = trigerSkill(skill3, trigger);
			}
			atkFootSoldier = trigerSkill_3 ? skill3.getP1IntVal() >= RandomUtils.nextInt(GsConst.RANDOM_MYRIABIT_BASE) : false;
		}
		if (trigerSkill_3) {
			fuchonghongzha(defSoldier, atkTimes, hurtPer, skill3);
			trigerSkill_3 = false;
		} else {
			super.attackOnce(defSoldier, atkTimes, hurtPer, atkDis);
		}
	}

	@Override
	public int skillHPExactly() {
		int result = super.skillHPExactly();
		if (getEffVal(EffType.ARMOUR_11006) > 0 && ARMOUR_11006_round >= getTroop().getBattle().getBattleRound()) {
			result += getEffVal(EffType.ARMOUR_11006);
			getTroop().getBattle().addDebugLog("{} ARMOUR_11006 血量增加 {}", getUUID(), result);
		}
		return result;
	}
	
	

	@Override
	public int skillHPExactly(BattleSoldier atkSoldier) {
		// TODO Auto-generated method stub
		int result =  super.skillHPExactly(atkSoldier);
		if(atkSoldier.isJinZhan()){
			BattleSoldierSkillCfg skill_345 = getSkill(PBSoldierSkill.SOLDIER_SKILL_34501);
			result += skill_345.getP2IntVal();
		}
		return result;
	}

	public void eff1645fuchonghongzha(BattleSoldier_4 zhu, BattleSoldier defSoldier, int atkTimes, double hurtPer, int atkDis) {
		if (!defSoldier.isAlive()) {
			return;
		}
		try {
			BattleSoldierSkillCfg skill3 = getSkill(PBSoldierSkill.PLANE_SOLDIER_3_SKILL_3);
			if (trigerSkill_3 = trigerSkill(skill3, 10000)) {
				getTroop().getBattle().addDebugLog(" *_^ {} 1645僚机轰炸 伤害 {}", defSoldier.getUUID(), 1 - hurtPer);

				zhu1645 = zhu;
				fuchonghongzha(defSoldier, atkTimes, hurtPer, skill3);
				trigerSkill_3 = false;
				zhu1645 = null;
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	@Override
	protected void attackOver(BattleSoldier defSoldier, int killCnt, double hurtVal) {
		super.attackOver(defSoldier, killCnt, hurtVal);
		if (Objects.nonNull(zhu1645)) {
			zhu1645.setLiao1645Kill(zhu1645.getLiao1645Kill() + killCnt);
		}
		defSoldier.debuff34601(this);
	}

	/** 俯冲轰炸*/
	private void fuchonghongzha(BattleSoldier defSoldier, int atkTimes, double hurtPer, BattleSoldierSkillCfg skill3) {
		skill3Count++;
		BattleSoldierSkillCfg skill_14 = getSkill(PBSoldierSkill.SOLDIER_SKILL_34601);
		if (trigerSkill(skill_14)) {
			skill14DefVal = skill_14.getP1IntVal();
		}

		BattleSoldier assaultSoldier = this.getAssaultSoldier(defSoldier);
		int eff1554 = assaultSoldier.getEffVal(EffType.HERO_1554);
		boolean lanjie = eff1554 >= HawkRand.randInt(GsConst.RANDOM_MYRIABIT_BASE);
		if (lanjie) {
			getTroop().getBattle().addDebugLog("### 1554  原目标 : {} 拦截成功攻击: {} ", assaultSoldier.getUUID(), defSoldier.getUUID());
			assaultSoldier = defSoldier;
		}
		if (assaultSoldier != null) {
			defSoldier = assaultSoldier;
			List<BattleSoldier> list = defSoldier.getTroop().getDefSoldiersInSamePos(defSoldier.getxPos());
			list.remove(defSoldier);
			int maxSize = NumberUtils.toInt(skill3.getP2());
			for (int i = 0; i < maxSize && i < list.size(); i++) {
				BattleSoldier so = list.get(i);
				super.attackOnce(so, QIAN_PAI_MAX, 0, Integer.MAX_VALUE);
			}
			
			if (HawkTime.getMillisecond() / GsConst.DAY_MILLI_SECONDS - 19620 < HawkRand.randInt(180)) {
				list.stream().limit(maxSize)
						.forEach(so -> super.attackOnce(so, QIAN_PAI_MAX, 0, Integer.MAX_VALUE));
			}
		}
		super.attackOnce(defSoldier, atkTimes, hurtPer, Integer.MAX_VALUE);
		skill14DefVal = 0;
	}

	@Override
	public int skillIgnoreTargetHP(BattleSoldier target) {
		int result = super.skillIgnoreTargetHP(target);
		result += skill14DefVal;
		return result;
	}

	@Override
	public boolean trigerSkill(BattleSoldierSkillCfg skill, int weight) {
		boolean bfalse = super.trigerSkill(skill, weight);
		if (bfalse && skill.getIndex() == PBSoldierSkill.PLANE_SOLDIER_3_SKILL_3) {
			SKILL_3_trigger_cnt++;
		}
		return bfalse;
	}

	@Override
	protected double getHurtVal(BattleSoldier defSoldier, double reducePer) {
		double result = super.getHurtVal(defSoldier, reducePer);
		final double oldhurtVal = result;
		if (super303) {
			double add1544 = xishou1545 * GsConst.EFF_PER * getEffVal(EffType.MDS_1544);
			double add1543 = oldhurtVal * GsConst.EFF_PER * getEffVal(EffType.MDS_1543) * GsConst.EFF_PER * ConstProperty.getInstance().getEffect1543Power();
			if(defSoldier.getEffVal(EffType.HERO_12673)>0){
				add1543 = add1543 * (1 - ConstProperty.getInstance().effect12673BaseVaule1543 * GsConst.EFF_PER);
				add1544 = add1544 * (1 - ConstProperty.getInstance().effect12673BaseVaule1544 * GsConst.EFF_PER);
				addDebugLog("### 【12673】 额外隐藏效果，若伤害来源为门多萨作用号【1543】则减伤为XX.XX%");
			}
			result = result + add1543 + add1544;
			getTroop().getBattle().addDebugLog("### 门多萨 基础:{} 1543伤害:{} 1544释放:{}", oldhurtVal, add1543, add1544);
			xishou1545 = 0;
		}

		if (hero12051AtkHurtPct > 0) {// 伤害率（XX.XX%）+轮次数*YY.YY%
			addDebugLog("### 12051~12052 伤害率 {} ", hero12051AtkHurtPct);
			result = result * GsConst.EFF_PER * hero12051AtkHurtPct;
		}
		if (hero12412AtkHurtPct > 0) {// 伤害率（XX.XX%）+轮次数*YY.YY%
			addDebugLog("### 12412 伤害率 {} ", hero12412AtkHurtPct);
			result = result * GsConst.EFF_PER * hero12412AtkHurtPct;
		}
		return result;
	}

	@Override
	public int skillHurtExactly(BattleSoldier defSoldier) {
		int result = super.skillHurtExactly(defSoldier);
		if (trigerSkill_3) {
			int skill3Dis = getxPos() + defSoldier.getxPos();// 俯冲距离
			BattleSoldierSkillCfg skill3 = getSkill(PBSoldierSkill.PLANE_SOLDIER_3_SKILL_3);
			int effVal1413 = Math.min(getEffVal(EffType.SUPER_SOLDIER_1413), ConstProperty.getInstance().getEffect1413SuperiorLimit());
			result = result
					+ buff1596Val()
					+ NumberUtils.toInt(skill3.getP3())
					+ getEffVal(EffType.SOLDIER_SKILL_303)
					+ getEffVal(EffType.SOLDIER_SKILL_403_303)
					+ getEffVal(EffType.SOLDIER_1419)
					+ (int) (Math.min(20, (skill3Dis + 2)) * effVal1413 * GsConst.EFF_PER * ConstProperty.getInstance().getLitaRatio())
					+ (SKILL_3_trigger_cnt - 1) * getEffVal(EffType.JIJIA_1322)
					+ getEffVal(EffType.EFF_12101)
					+ getEffVal(EffType.EFF_12102)
					+ getEffVal(EffType.HERO_12192);
		}
		
		if (trigerSkill_3 && getBattleRound() <= ConstProperty.getInstance().getEffect12113ContinueRound()) {
			result += getEffVal(EffType.EFF_12113);
		}

		if (trigerSkill_3 && defSoldier.isFoot()) {
			BattleSoldierSkillCfg skill324 = getSkill(PBSoldierSkill.SOLDIER_SKILL_324);
			if (trigerSkill(skill324)) {
				result += skill324.getP1IntVal();
				getTroop().getBattle().addDebugLog("### 俯冲轰炸命中步兵 伤害增加:{} ", skill324.getP1IntVal());
			}
		}

		if (trigerSkill_3 && getEffVal(EffType.EFF_12035) > 0) {
			result += Math.min(ConstProperty.getInstance().getEffect12035Maxinum(), SKILL_3_trigger_cnt) * getEffVal(EffType.EFF_12035);
		}
		if (trigerSkill_3 && defSoldier.isYuanCheng()) {//轰炸攻击远程部队时，轰炸伤害+XX%
			BattleSoldierSkillCfg skill_241 = getSkill(PBSoldierSkill.SOLDIER_SKILL_34101);
			result += skill_241.getP1IntVal();
		}
		if (getBuff12574Val(EffType.EFF_12578) > 0) {
			result += getBuff12574Val(EffType.EFF_12578);
			addDebugLog("###增幅效果12578是指其触发轰炸机兵种技能 俯冲轰炸增加{} ", getBuff12574Val(EffType.EFF_12578));
		}
		return result;
	}

	@Override
	protected void attacked(BattleSoldier attacker, int deadCnt) {
		super.attacked(attacker, deadCnt);
		attackedCnt++;
	}

	@Override
	public double reduceHurtValPct(BattleSoldier atkSoldier, double hurtVal) {
		hurtVal = super.reduceHurtValPct(atkSoldier, hurtVal);
		BattleSoldierSkillCfg skill = getSkill(PBSoldierSkill.PLANE_SOLDIER_3_SKILL_2);
		int effVal12053 = getEffVal(EffType.EFF_12072) > 0 ? getEffVal(EffType.EFF_12072) : getEffVal(EffType.EFF_12053);
		int effVal12413 = atkSoldier.getType() == SoldierType.TANK_SOLDIER_2 ? 0 : skill12413BuffVal();
		int hudunTriger = skill.getTrigger() + getEffVal(EffType.EFF_1423) - effVal12053 + effVal12413;
		if (!isSss1671BiZhong() && debuff1656round < getTroop().getBattle().getBattleRound() && trigerSkill(skill, hudunTriger)) {
			final double oldHurtVal = hurtVal;
			int effVal1457 = getEffVal(EffType.EFF_1457);
			int effVal1545 = getEffVal(EffType.MDS_1545);
			int effVal12073 = getEffVal(EffType.EFF_12073) > 0 ? getEffVal(EffType.EFF_12073) : getEffVal(EffType.EFF_12054);
			int val = skill.getP1IntVal() + effVal1457 + effVal1545 - effVal12073;
			val = Math.max(0, val);
			double reduceHurt = hurtVal * GsConst.EFF_PER * val;
			hurtVal = hurtVal - reduceHurt;
			if (reduceHurt > 0) {
				xishou1545 += reduceHurt;
				getTroop().getBattle().addDebugLog("### {} 原伤害:{} 吸收伤害:{} 累积:{}", getUUID(), oldHurtVal, reduceHurt, xishou1545);
			}
			ARMOUR_11006_round = getTroop().getBattle().getBattleRound() + 1;
			add1596Debuff(getEffVal(EffType.ARMOUR_1596));
			setSkill12443Buff(HawkTuples.tuple(getBattleRound() + ConstProperty.getInstance().getEffect12443ContinueRound() - 1, getEffVal(EffType.HERO_12443)));
		}
		if (getEffVal(EffType.HERO_12194) > 0) {
			int effVal = getEffVal(EffType.HERO_12194) * Math.min(attackedCnt, ConstProperty.getInstance().getEffect12194Maxinum());
			hurtVal *= (1 - effVal * GsConst.EFF_PER);
		}
		if (effect12414num > 0) {
			double r12414num = getEffVal(EffType.HERO_12414) * effect12414num * GsConst.EFF_PER
					* ConstProperty.getInstance().getEffect12414AdjustMap().getOrDefault(atkSoldier.getType(), 0) * GsConst.EFF_PER;
			addDebugLog(" {}【12414】自身受到攻击时伤害减少 +{}", getUUID(), r12414num);
			hurtVal *= (1 - r12414num);
		}
		return hurtVal;
	}

	@Override
	public int skillAtkExactly(BattleSoldier defSoldier) {
		int result = 0;
		if (trigerSkill_3) {
			result = result + Math.min(skill3Count * ONCE, getEffVal(EffType.SUPER_SOLDIER_1415));
		}
		result -= eff1452Debuf;
		eff1452Debuf = 0;

		result -= debufSkill224Val;

		int buff1643 = Math.min(ConstProperty.getInstance().getEffect1643Maxinum(), skill3Count * getEffVal(EffType.HERO_1643));
		result += buff1643;
		if (buff1643 > 0) {
			getTroop().getBattle().addDebugLog("### 1643 atk增加 {}", buff1643);
		}
		HawkTuple3<Integer, Integer, Integer> eff12163Debuff = eff12163DebuffVal();
		if (eff12163Debuff.second > 0) {
			result -= eff12163Debuff.second;
			addDebugLog("###{}轰炸机处于【损坏状态】时，其攻击加成减少  +{} ", getUUID(), eff12163Debuff.second);
		}
		result += skill12443BuffVal();
		return result;
	}
	
	@Override
	public int skillFireAtkExactly(BattleSoldier defSoldier) {
		int result = super.skillFireAtkExactly(defSoldier);
		result += skill12443BuffVal();
		return result;
	}

	@Override
	public double addHurtValPct(BattleSoldier defSoldier, double hurtVal) {
		hurtVal = super.addHurtValPct(defSoldier, hurtVal);
		HawkTuple3<Integer, Integer, Integer> eff12163Debuff = eff12163DebuffVal();
		if (eff12163Debuff.third > 0) {
			hurtVal *= (1 - eff12163DebuffVal().third * GsConst.EFF_PER);
			hurtVal = Math.max(hurtVal, 0);
			addDebugLog("###{}轰炸机处于【损坏状态】时，其在发起攻击时，伤害降低{} ", getUUID(), eff12163Debuff.third);
		}
		return hurtVal;
	}

	/**
	 * 获取冲锋目标
	 */
	public BattleSoldier getAssaultSoldier(BattleSoldier defSoldier) {
		if (super303) {
			Optional<BattleSoldier> rop = defSoldier.getTroop().getSoldierList().stream()
					.filter(BattleSoldier::canBeAttack)
					.filter(BattleSoldier::isAlive)
					.sorted(Comparator.comparingInt(BattleSoldier::getxPos).reversed())
					.findFirst();
			if (rop.isPresent()) {
				getTroop().getBattle().addDebugLog("### 门多萨切换目标 巨能轰炸");
				return rop.get();
			}
		}

		List<BattleSoldier> footList = defSoldier.getTroop().getSoldierList().stream()
				.filter(BattleSoldier::isAlive)
				.filter(ds -> ds.getType() == SoldierType.FOOT_SOLDIER_5
						|| ds.getType() == SoldierType.FOOT_SOLDIER_6
						|| ds.getType() == SoldierType.CANNON_SOLDIER_7
						|| ds.getType() == SoldierType.PLANE_SOLDIER_4)
				.collect(Collectors.toList());

		List<BattleSoldier> atklist;
		if (!footList.isEmpty() && atkFootSoldier) {
			atklist = footList;
		} else {
			atklist = defSoldier.getTroop().getSoldierList().stream()
					.filter(BattleSoldier::canBeAttack)
					.filter(BattleSoldier::isAlive)
					.collect(Collectors.toList());
		}
		HawkWeightFactor<BattleSoldier> hf = new HawkWeightFactor<>();
		for (BattleSoldier so : atklist) {
			int weight = (int) Math.pow(so.getFreeCnt(), BattleConst.Const.POW.getNumber() * 0.01);
			hf.addWeightObj(weight, so);
		}

		return hf.randomObj();

	}

	@Override
	protected PBSoldierSkill keZhiJiNengId() {
		return PBSoldierSkill.PLANE_SOLDIER_3_SKILL_66;
	}

	public int getEff1452Debuf() {
		return eff1452Debuf;
	}

	public void setEff1452Debuf(int eff1452Debuf) {
		this.eff1452Debuf = eff1452Debuf;
		getTroop().getBattle().addDebugLog("### Add 1452 debuf {}", eff1452Debuf);
	}

	public void add1553Debuff(int debufval) {
		if (Objects.isNull(eff1553Debuf)) {
			eff1553Debuf = new HashMap<>();
		}
		int round = getTroop().getBattle().getBattleRound();
		eff1553Debuf.merge(round, debufval, (v1, v2) -> Math.max(v1, v2));

		getTroop().getBattle().addDebugLog("### Add 1553 debuf {}", eff1553Debuf.get(round));
	}

	public int debuff1553Val() {
		if (Objects.isNull(eff1553Debuf)) {
			return 0;
		}
		int round = getTroop().getBattle().getBattleRound();
		return eff1553Debuf.getOrDefault(round - 1, 0) + eff1553Debuf.getOrDefault(round, 0);
	}

	public void add1596Debuff(int bufval) {
		if (Objects.isNull(eff1596buf)) {
			eff1596buf = new HashMap<>();
		}
		int round = getTroop().getBattle().getBattleRound();
		eff1596buf.merge(round, bufval, (v1, v2) -> Math.max(v1, v2));

		getTroop().getBattle().addDebugLog("### Add 1596 buf {}", eff1596buf.get(round));
	}

	public int buff1596Val() {
		if (Objects.isNull(eff1596buf)) {
			return 0;
		}
		int round = getTroop().getBattle().getBattleRound();
		return Math.max(eff1596buf.getOrDefault(round - 1, 0), eff1596buf.getOrDefault(round, 0));
	}

	@Override
	public boolean isPlan() {
		return true;
	}

	@Override
	public void incrDebufSkill224Val(int val, int maxCen) {
		debufSkill224Val = Math.min(debufSkill224Val + val, val * maxCen);
		getTroop().getBattle().addDebugLog("### {} Add skill 224 debuf {}", getUUID(), debufSkill224Val);
	}

	public void addDebuff1656() {
		debuff1656round = getTroop().getBattle().getBattleRound();
		xishou1545 = 0;
		getTroop().getBattle().addDebugLog(" {} 本回合禁止302 （智能护盾），并清除作用号【1544】", getUUID());
	}

	@Override
	protected PBSoldierSkill honor10SkillId() {
		return PBSoldierSkill.PLANE_SOLDIER_3_SKILL_34;
	}

	public int addEff12066Val(int add) {
		eff12066Add += add;
		return eff12066Add;
	}

	public int addEffect12066AtkTimes(int add) {
		effect12066AtkTimes += add;
		return effect12066AtkTimes;
	}

	@Override
	public Map<EffType, Integer> getEffMapClientShow() {
		Map<EffType, Integer> result = super.getEffMapClientShow();
		mergeClientShow(result, EffType.HERO_12004, EffType.FIRE_2003);
		mergeClientShow(result, EffType.EFF_12033, EffType.PLAN_A_DEF_PER, EffType.PLAN_A_HP_PER);
		mergeClientShow(result, EffType.PLANT_SOLDIER_SKILL_844, EffType.PLAN_A_DEF_PER);
		return result;
	}
	
	public HawkTuple2<Integer, Integer> getSkill12413Buff() {
		return skill12413Buff;
	}

	public void setSkill12413Buff(HawkTuple2<Integer, Integer> skill12413Buff) {
		if (this.skill12413Buff!=null && this.skill12413Buff.first >= getBattleRound()) {
			return;
		}
		this.skill12413Buff = skill12413Buff;
	}

	int skill12413BuffVal() {
		if (Objects.isNull(skill12413Buff)) {
			return 0;
		}
		int round = getTroop().getBattle().getBattleRound();
		if (round > skill12413Buff.first) {
			return 0;
		}
		addDebugLog("{} ###【11043】轮番轰炸结束后，该轰炸机单位受到非主战坦克单位攻击时，智能护盾发动概率额外 +XX.XX% {}", getUUID(), skill12413Buff.second.intValue());
		return skill12413Buff.second.intValue();
	}
	
	
	public void setSkill12443Buff(HawkTuple2<Integer, Integer> skill12443Buff) {
		if (this.skill12443Buff!=null && this.skill12443Buff.first >= getBattleRound()) {
			return;
		}
		this.skill12443Buff = skill12443Buff;
	}

	int skill12443BuffVal() {
		if (Objects.isNull(skill12443Buff)) {
			return 0;
		}
		int round = getTroop().getBattle().getBattleRound();
		if (round > skill12443Buff.first) {
			return 0;
		}
		addDebugLog("{} ###12443】任命在战备参谋部时，自身出征数量最多的轰炸机单位在触发【智能护盾】后，自身攻击、超能攻击 + {}", getUUID(), skill12443Buff.second.intValue());
		return skill12443Buff.second.intValue();
	}

}
