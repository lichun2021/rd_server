package com.hawk.game.battle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.hawk.os.HawkException;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkWeightFactor;
import org.hawk.tuple.HawkTuple3;
import org.hawk.tuple.HawkTuples;

import com.google.common.collect.ImmutableMap;
import com.hawk.game.battle.effect.BattleConst;
import com.hawk.game.battle.effect.impl.hero1110.Buff12574;
import com.hawk.game.config.BattleSoldierSkillCfg;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.PBSoldierSkill;
import com.hawk.game.protocol.Const.SoldierType;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.RandomContent;
import com.hawk.game.util.RandomUtil;

public class BattleSoldier_8 extends ICannonSoldier {
	private boolean triger1528;
	/**1528造成的伤害*/
	private double hurtVal1528;

	/**进行雷达干扰，被干扰的目标，下回合无法攻击*/
	private int round1639;
	private int round12162;
	private int round12163;
	private boolean attack12163;
	TreeMap<Integer, Integer> buff84601map ;

	@Override
	public SoldierType getType() {

		return SoldierType.CANNON_SOLDIER_8;
	}

	@Override
	public void beforeWarfare() {
		super.beforeWarfare();
		loadSkill84601();
	}

	private void loadSkill84601() {
		try {
			BattleSoldierSkillCfg scfg = getSkill(PBSoldierSkill.SOLDIER_SKILL_84601);
			if (StringUtils.isEmpty(scfg.getP1())) {
				return;
			}
			Map<Integer, Integer> lsit = new HashMap<>();
			for (String xy : scfg.getP1().trim().split("\\,")) {
				String[] x_y = xy.split("_");
				int[] pos = new int[2];
				pos[0] = NumberUtils.toInt(x_y[0]); //万分比
				pos[1] = NumberUtils.toInt(x_y[1]);
				lsit.put(pos[0], pos[1]);
			}
			buff84601map = new TreeMap<>(lsit);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	@Override
	public void roundStart() {
		super.roundStart();
		if (round1639 > 0 && !isAlive()) { // 如果本来自己释放1639. 结果自己已经死光了
			List<BattleSoldier> soldierList = getTroop().getSoldierList(getPlayerId());
			Optional<BattleSoldier> rop = soldierList.stream()
					.filter(so -> so.getType() == SoldierType.CANNON_SOLDIER_8)
					.filter(BattleSoldier::isAlive)
					.sorted(Comparator.comparingInt(BattleSoldier::getFreeCnt).reversed())
					.findFirst();
			if (rop.isPresent()) {
				BattleSoldier_8 next = (BattleSoldier_8) rop.get();
				next.setRound1639(round1639);
				getTroop().getBattle().addDebugLog("### {} 死亡,雷达干扰释放转移: {}", getUUID(), next.getUUID());
			}
			this.round1639 = 0;
		}
		jelani12005();
		levan12574();
	}

	@Override
	public void roundStart2() {
		jixika12161();
		jixika12162();
	}

	@Override
	public void roundEnd() {
		jixika12163();
		levan12571();
	}
	
	private void levan12574() {// 聚焦增幅
		if (getEffVal(EffType.EFF_12574) <= 0 || getBattleRound() % ConstProperty.getInstance().effect12574AtkRound != 0) {
			return;
		}
		List<RandomContent<BattleSoldier>> objList = getTroop().getSoldierList().stream().filter(BattleSoldier::isAlive)
				.filter(s -> s.getType() == SoldierType.TANK_SOLDIER_2 || s.getType() == SoldierType.PLANE_SOLDIER_3)
				.filter(s -> s.getBuff12574Val(EffType.EFF_12574) == 0)
				.map(s -> RandomContent.create(s, ConstProperty.getInstance().effect12574RoundWeightMap.getOrDefault(s.getType(), 100))).filter(r -> r.getWeight() > 0)
				.collect(Collectors.toList());
		objList = RandomUtil.randomWeightObject(objList, ConstProperty.getInstance().effect12574InitChooseNum);
		List<BattleSoldier> targets = objList.stream().map(RandomContent::getObj).collect(Collectors.toList());
		for (BattleSoldier tar : targets) {
			Buff12574 buff = new Buff12574();
			buff.round = getBattleRound() + ConstProperty.getInstance().effect12574ContinueRound - 1;
			buff.eff12574 = getEffVal(EffType.EFF_12574);
			buff.eff12575 = getEffVal(EffType.EFF_12575);
			buff.eff12576 = (int) (getEffVal(EffType.EFF_12576) + getEffVal(EffType.EFF_12592));
			buff.eff12577 = (int) ((getEffVal(EffType.EFF_12577) + ConstProperty.getInstance().effect12577BaseVaule) * GsConst.EFF_PER* ConstProperty.getInstance().effect12577Adjust);
			buff.eff12578 = (int) ((getEffVal(EffType.EFF_12578) + ConstProperty.getInstance().effect12578BaseVaule) * GsConst.EFF_PER* ConstProperty.getInstance().effect12578Adjust);
			buff.zhenSheNum = ConstProperty.getInstance().effect12576ReduceFirePoint;
			tar.setBuff12574(buff);
			addDebugLog("聚焦增幅{} -> {} 攻击、防御、血量增加12574:{} 超能攻击增加12575:{} 伤害吸收护盾 12576:{}, 12577:{} 12578:{} 结束回合:{}", getUUID(), tar.getUUID(), buff.eff12574, buff.eff12575, buff.eff12576,buff.eff12577,buff.eff12578, buff.round);
		}

	}

	private void levan12571() {
		if (getEffVal(EffType.EFF_12571) == 0) {
			return;
		}
//		 声波震慑：莱万布设声波发射器，使战斗中敌方全体近战单位受到声波震慑影响，每回合结束时受到一次震慑伤害（伤害率：XX.XX%（作用号12571）），且有【固定值(effect12571BaseVaule)+XX.XX%（作用号12572）*受声波震慑影响回合数】 的概率增加 1(effect12571AddFirePoint)  点震慑值；
//		在每回合开始时，当前震慑值不低于 10点 (effect12571AtkThresholdValue)的敌方单位会进入【损坏状态】，持续 X（effect12571ContinueRound） 回合，并且清空自身震慑值（不同采矿车单位施加的震慑值可叠加，【损坏状态】将会触发杰西卡对敌方施加的负面效果） 
		List<BattleSoldier> enemys = getTroop().getEnemyTroop().getSoldierList().stream().filter(BattleSoldier::isJinZhan).filter(BattleSoldier::isAlive).collect(Collectors.toList());
		int hurtRate = getEffVal(EffType.EFF_12571) + getEffVal(EffType.EFF_12591);//伤害率：XX.XX%（作用号12571）
		int rand12571 = ConstProperty.getInstance().effect12571BaseVaule + getEffVal(EffType.EFF_12572) * getBattleRound();
		for(BattleSoldier tar: enemys){
			addDebugLog("12571声波震慑 伤害率 {}  ",hurtRate);
			attackOnce(tar, QIAN_PAI_MAX, (10000 - hurtRate) * GsConst.EFF_PER, Integer.MAX_VALUE, false);
			if(HawkRand.randInt(GsConst.RANDOM_MYRIABIT_BASE) < rand12571){
				int add12571 = ConstProperty.getInstance().effect12571AddFirePoint;
				if(tar.buff12574.zhenSheNum >0 && tar.buff12574.round >= getBattleRound()){
					int xishou = Math.min(add12571, tar.buff12574.zhenSheNum);
					add12571 = add12571 - xishou;
					tar.buff12574.zhenSheNum -= xishou;
					addDebugLog("护盾生效期间可以抵消 {} 点震慑值叠加  ",add12571);
				}
				tar.debuff12571Num += add12571;
				addDebugLog("声波震慑 且有{} 的概率增加 1(effect12571AddFirePoint)点震慑值  {}",rand12571 , tar.debuff12571Num);
			}
		}
		
		int round = getBattleRound();
		for(BattleSoldier defSoldier: enemys){
			double hurtVal = defSoldier.getRountHurtedVal(round) * GsConst.EFF_PER * (ConstProperty.getInstance().effect12573BaseVaule + (getEffVal(EffType.EFF_12573) + getEffVal(EffType.EFF_12593))* defSoldier.eff12163DebuffCnt );
			if(hurtVal > 0 && defSoldier.eff12163DebuffCnt > 0 && round > defSoldier.getEff12163Debuff().first  && round <= defSoldier.getEff12163Debuff().first + ConstProperty.getInstance().effect12573ContinueRound){
				hurtVal = defSoldier.forceField(this, hurtVal);
				int curCnt = defSoldier.getFreeCnt();
				int maxKillCnt = (int) Math.ceil(4.0f * hurtVal / defSoldier.getHpVal(this));
				maxKillCnt = Math.max(1, maxKillCnt);
				int killCnt = Math.min(maxKillCnt, curCnt);

				defSoldier.addDeadCnt(killCnt);
				addKillCnt(defSoldier, killCnt);
				getTroop().getBattle().addDebugLog("损坏追惩：当敌方【损坏状态】结束后，在接下来 3 回合中，回合结束时会受到一次附加伤害 击杀 {} count{} hurtVal {}", defSoldier.getUUID(), killCnt, hurtVal);
			}
		}
		
		
//		在每回合开始时，当前震慑值不低于 10点
		for(BattleSoldier tar: enemys){
			if(tar.isAlive() && tar.debuff12571Num>= ConstProperty.getInstance().effect12571AtkThresholdValue){
				addDebugLog("声波震慑 在每回合开始时，当前震慑值不低于 10点的敌方单位会进入【损坏状态】");
				tar.debuff12571Num = 0;
				add12163Debuff(tar, ConstProperty.getInstance().effect12571ContinueRound);
			}
		}
	}

	private void jixika12163() {
		if (getEffVal(EffType.HERO_12163) == 0||getEffVal(EffType.HERO_12005)==0) {
			return;
		}
		if (round12163 != getBattleRound()) {
			return;
		}
		addDebugLog("### {} 【发动引爆】{} {}", getUUID(), round12162, getBattleRound());
		// 点燃状态的
		List<BattleSoldier> dianRans = getTroop().getEnemyTroop().getSoldierList().stream().filter(BattleSoldier::isAlive).filter(BattleSoldier::isDianran)
				.collect(Collectors.toList());
		Collections.shuffle(dianRans);
		attack12163 = true;
		for (int i = 0; i < ConstProperty.getInstance().getEffect12163AtkNum() && i < dianRans.size(); i++) {
			BattleSoldier defSoldier = dianRans.get(i);
			super.attackOnce(defSoldier, QIAN_PAI_MAX, 0, Integer.MAX_VALUE, false);
			defSoldier.clearDianRan();
			add12163Debuff(defSoldier,0);
		}
		attack12163 = false;
	}

	/**
	 * - 【12164】主战坦克处于【损坏状态】时，其攻击加成减少 +XX.XX%
        - 此为外围属性加成减少效果；即实际攻击 = 基础攻击*（1 + 其他加成 - 【本作用值】）
      - 【12165】主战坦克处于【损坏状态】时，其在发起攻击时，伤害降低 +XX.XX%
        - 此为外围累乘减免效果（同作用号【12153】），即 敌方实际伤害 = 基础伤害*（1 + 敌方各类伤害加成）*（1 - 某作用值伤害减免）*（1 - 【本作用值】）
    - 轰炸机（兵种类型 = 3）
      - 【12166】轰炸机处于【损坏状态】时，其攻击加成减少 +XX.XX%
        - 此为外围属性加成减少效果；即实际攻击 = 基础攻击*（1 + 其他加成 - 【本作用值】）
      - 【12167】轰炸机处于【损坏状态】时，其在发起攻击时，伤害降低 +XX.XX%
        - 此为外围累乘减免效果（同作用号【12153】），即 敌方实际伤害 = 基础伤害*（1 + 敌方各类伤害加成）*（1 - 某作用值伤害减免）*（1 - 【本作用值】）
    - 防御坦克（兵种类型 = 1）
      - 【12168】防御坦克处于【损坏状态】时，其防御、生命加成减少 +XX.XX%
        - 此为外围属性加成减少效果；即实际防御 = 基础防御*（1 + 其他加成 - 【本作用值】）
      - 【12169】防御坦克处于【损坏状态】时，其在受到攻击时，伤害额外 +XX.XX%
        - 此为外围伤害加成效果，与其他作用号累乘计算；即实际伤害 = 基础伤害*（1 + 其他加成）*（1 +  【本作用值】）
    - 采矿车（兵种类型 = 8）
      - 【12170】采矿车处于【损坏状态】时，其防御、生命加成减少 +XX.XX%
        - 此为外围属性加成减少效果；即实际防御 = 基础防御*（1 + 其他加成 - 【本作用值】）
      - 【12171】采矿车处于【损坏状态】时，其在受到攻击时，伤害额外 +XX.XX%
        - 此为外围伤害加成效果，与其他作用号累乘计算；即实际伤害 = 基础伤害*（1 + 其他加成）*（1 +  【本作用值】
	 * @param defSoldier
	 */
	private void add12163Debuff(BattleSoldier defSoldier,int continueRound) {
		if(defSoldier instanceof BattleSoldier_2){
			continueRound = Math.max(continueRound, ConstProperty.getInstance().getEffect12164ContinueRound());
			int round = getTroop().getBattle().getBattleRound() + continueRound;
			HawkTuple3<Integer, Integer, Integer> tuple = HawkTuples.tuple(round, getEffVal(EffType.HERO_12164), getEffVal(EffType.HERO_12165));
			defSoldier.setEff12163Debuff(tuple);
			getTroop().getBattle().addDebugLog("### {} 【损坏状态】{}  12164: {} 12165: {} 结束回合 {}", getUUID(), defSoldier.getUUID(), tuple.second,tuple.third, round);
		}
		if(defSoldier instanceof BattleSoldier_3){
			continueRound = Math.max(continueRound, ConstProperty.getInstance().getEffect12166ContinueRound());
			int round = getTroop().getBattle().getBattleRound() + continueRound;
			HawkTuple3<Integer, Integer, Integer> tuple = HawkTuples.tuple(round, getEffVal(EffType.HERO_12166), getEffVal(EffType.HERO_12167));
			defSoldier.setEff12163Debuff(tuple);
			getTroop().getBattle().addDebugLog("### {} 【损坏状态】 {} 12166: {} 12167: {} 结束回合 {}", getUUID(), defSoldier.getUUID(), tuple.second,tuple.third, round);
		}
		if(defSoldier instanceof BattleSoldier_1){
			continueRound = Math.max(continueRound, ConstProperty.getInstance().getEffect12168ContinueRound());
			int round = getTroop().getBattle().getBattleRound() + continueRound;
			HawkTuple3<Integer, Integer, Integer> tuple = HawkTuples.tuple(round, getEffVal(EffType.HERO_12168), getEffVal(EffType.HERO_12169));
			defSoldier.setEff12163Debuff(tuple);
			getTroop().getBattle().addDebugLog("### {} 【损坏状态】 {} 12168: {} 12169: {} 结束回合 {}", getUUID(), defSoldier.getUUID(), tuple.second,tuple.third, round);
		}
		if(defSoldier instanceof BattleSoldier_8){
			continueRound = Math.max(continueRound, ConstProperty.getInstance().getEffect12170ContinueRound());
			int round = getTroop().getBattle().getBattleRound() + continueRound;
			HawkTuple3<Integer, Integer, Integer> tuple = HawkTuples.tuple(round, getEffVal(EffType.HERO_12170), getEffVal(EffType.HERO_12171));
			defSoldier.setEff12163Debuff(tuple);
			getTroop().getBattle().addDebugLog("### {} 【损坏状态】 {} 12170: {} 12171: {} 结束回合 {}", getUUID(), defSoldier.getUUID(), tuple.second,tuple.third, round);
		}
		
	}

	private void jixika12162() {
		if (getEffVal(EffType.HERO_12162) == 0) {
			return;
		}
		if (round12162 < getBattleRound() && getBattleRound() <= round12163) {
			boolean has12161 = getTroop().getEnemyTroop().getSoldierList().stream().filter(BattleSoldier::isAlive)
					.filter(enemy -> enemy.getDebuff12161ContinueRound() >= getBattleRound())
					.findAny().isPresent();
			if (has12161) {
				for (int i = 0; i < ConstProperty.getInstance().getEffect12162EffectiveUnit(); i++) {
					BattleSoldier target = get12162TargetSoldier();
					if (target != null) {
						target.setDebuff12161ContinueRound(getBattleRound() + ConstProperty.getInstance().getEffect12162ContinueRound() - 1);
						addDebugLog("-杰西卡 {} 鼓风 -> {} 结束{}", getUUID(), target.getUUID(), target.getDebuff12161ContinueRound());
					}
				}
			}
		}
	}

	private BattleSoldier get12162TargetSoldier() {
		// 防守兵
		BattleSoldier defSoldier = null;
		for (BattleSoldier tmpSoldier : getTroop().getEnemyTroop().getSoldierList()) {
			if (!tmpSoldier.isAlive() || !tmpSoldier.canBeAttack()) {
				continue;
			}
			if (tmpSoldier.getDebuff12161ContinueRound() >= getBattleRound()) {
				continue;
			}
			if (tmpSoldier.isYuanCheng()) {
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

	private void jixika12161() {
		if (getEffVal(EffType.HERO_12161) == 0 || getBattleRound() % ConstProperty.getInstance().getEffect12005AtkRound() != 0) {
			return;
		}
		round12162 = getBattleRound();
		round12163 =  getBattleRound() + ConstProperty.getInstance().getEffect12162NextRound();
		boolean has12005 = getTroop().getEnemyTroop().getSoldierList().stream().filter(BattleSoldier::isAlive)
				.filter(enemy -> enemy.getDebuff12005ContinueRound() >= getBattleRound())
				.findAny().isPresent();
		if (has12005) {
			BattleSoldier target = get12161TargetSoldier();
			if (target != null) {
				target.setDebuff12161ContinueRound(getBattleRound() + ConstProperty.getInstance().getEffect12161ContinueRound() - 1);
				addDebugLog("-杰西卡 {} 汲火 -> {}  结束{}", getUUID(), target.getUUID(), target.getDebuff12161ContinueRound());
			}
		}

	}

	/**
	 * 获取防守战斗单元
	 */
	public BattleSoldier get12161TargetSoldier() {
		// 防守兵
		BattleSoldier defSoldier = null;
		for (BattleSoldier tmpSoldier : getTroop().getEnemyTroop().getSoldierList()) {
			if (!tmpSoldier.isAlive() || !tmpSoldier.canBeAttack()) {
				continue;
			}
			if (tmpSoldier.getDebuff12161ContinueRound() > getBattleRound()) {
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

	@Override
	public void attackOnce(BattleSoldier defSoldier, int atkTimes, double hurtPer, int atkDis) {
		super.attackOnce(defSoldier, atkTimes, hurtPer, atkDis);
		chuantouAttack(defSoldier);
		round1639(defSoldier);
	}

	@Override
	public void beforeAttack(BattleSoldier defSoldier) {
		super.beforeAttack(defSoldier);
	}

	private boolean jelani12005AOE;

	private void jelani12005() {
		try {
			jelani12005AOE = false;
			if (getEffVal(EffType.HERO_12005) <= 0 || getBattleRound() % ConstProperty.getInstance().getEffect12005AtkRound() != 0) {
				return;
			}
			List<BattleSoldier> footList = getTroop().getEnemyTroop().getSoldierList().stream()
					.filter(BattleSoldier::isAlive)
					.filter(BattleSoldier::canBeAttack)
					.collect(Collectors.toCollection(ArrayList::new));
			Collections.shuffle(footList);
			footList = footList.stream().limit(ConstProperty.getInstance().getEffect12005AtkNum()).collect(Collectors.toList());

			addDebugLog("-Jelani {} 每第 5 回合，额外向敌方随机 5 个单位进行 1 轮攻击（伤害率XX.XX%）", ConstProperty.getInstance().getEffect12005AtkNum(), getUUID());
			jelani12005AOE = true;
			for (BattleSoldier tar : footList) {
				super.attackOnce(tar, QIAN_PAI_MAX, 0, Integer.MAX_VALUE, false);
				tar.setDebuff12005ContinueRound(getBattleRound() + ConstProperty.getInstance().getEffect12005ContinueRound() - 1);
				tar.setDebuff12006Atk(Math.min(ConstProperty.getInstance().getEffect12006MaxValue(), getEffVal(EffType.HERO_12006) + tar.getDebuff12006Atk()));
				tar.setDebuff12007Def(Math.min(ConstProperty.getInstance().getEffect12007MaxValue(), getEffVal(EffType.HERO_12007) + tar.getDebuff12007Def()));
				tar.setDebuff12008HP(Math.min(ConstProperty.getInstance().getEffect12008MaxValue(), getEffVal(EffType.HERO_12008) + tar.getDebuff12008HP()));

				addDebugLog("-Jelani {} 12005点燃 {} round {} 12006:{} 12007:{} 12008:{}",
						getUUID(), tar.getUUID(), tar.getDebuff12005ContinueRound(), tar.getDebuff12006Atk(),
						tar.getDebuff12007Def(), tar.getDebuff12008HP());
				addDebugLog("");
			}
			jelani12005AOE = false;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/** 尝试释放雷达干扰*/
	private void round1639(BattleSoldier defSoldier) {
		if (round1639 == 0) {
			return;
		}
		boolean bfalse = getTroop().getBattle().getBattleRound() % round1639 == 0;
		if (!bfalse) {
			return;
		}
		// 选取被眩晕的目标 effect1639SoldierPer控制
		// 例：1_5,2_15,3_75,8_5 即只有1排轰炸，主战，防御，采矿车，轰炸被选中概率75%，主战15%，防坦，采矿车为5%
		Map<SoldierType, Integer> perMap = ConstProperty.getInstance().getEffect1639SoldierPerMap();

		List<BattleSoldier> footList = defSoldier.getTroop().getSoldierList().stream()
				.filter(BattleSoldier::isAlive)
				.filter(ds -> perMap.containsKey(ds.getType()))
				.filter(BattleSoldier::canBeAttack)
				.collect(Collectors.toList());

		// 排除已经干扰的 最多干扰数也要限制
		int defbuff1639Round = getTroop().getBattle().getBattleRound() + 1;
		int hasDebuf1639Cnt = (int) footList.stream().filter(ds -> ds.isDebuff1639Round(defbuff1639Round)).count();
		if (hasDebuf1639Cnt >= ConstProperty.getInstance().getEffect1639Maxinum()) {
			return;
		}

		footList = footList.stream().filter(ds -> !ds.isDebuff1639Round(defbuff1639Round)).collect(Collectors.toList());

		if (footList.isEmpty()) {
			return;
		}

		HawkWeightFactor<BattleSoldier> hf = new HawkWeightFactor<>();
		for (BattleSoldier so : footList) {
			hf.addWeightObj(perMap.get(so.getType()), so);
		}

		BattleSoldier tar = hf.randomObj();
		tar.setDebuff1639Round(defbuff1639Round, getEffVal(EffType.HERO_1639));
		getTroop().getBattle().addDebugLog("### {} 雷达干扰: {} ,{}", getUUID(), tar.getUUID(), hasDebuf1639Cnt);
	}

	@Override
	protected void attacked(BattleSoldier attacker, int killCnt) {
		super.attacked(attacker, killCnt);
		BattleSoldierSkillCfg skill_14 = getSkill(PBSoldierSkill.SOLDIER_SKILL_814);
		if (trigerSkill(skill_14)) {
			attacker.incrDebfuSkill814Val(skill_14.getP1IntVal(), NumberUtils.toInt(skill_14.getP2()));
		}

		if (getEffVal(EffType.EFF_12039) > 0) {
			attacker.addDebuff12039(getEffVal(EffType.EFF_12039));
		}
		if (getEffVal(EffType.EFF_12040) > 0) {
			addBuff12040(getEffVal(EffType.EFF_12040));
			getTroop().getSoldierList().stream().filter(BattleSoldier::isAlive).filter(BattleSoldier::canBeAttack).filter(s -> s.getxPos() > getxPos())
					.filter(s -> s.buff12040cen < ConstProperty.getInstance().getEffect12040Maxinum()).limit(ConstProperty.getInstance().getEffect12040Nums())
					.forEach(s -> s.addBuff12040(getEffVal(EffType.EFF_12040)));
		}
		
		if (getEffVal(EffType.EFF_12302) > 0) {
			List<BattleSoldier> jinList = getTroop().getSoldierList().stream().filter(BattleSoldier::canAddBuff12302).collect(Collectors.toList());
			List<BattleSoldier> tarList = RandomUtil.randomWeightObject(jinList, ConstProperty.getInstance().getEffect12302AffectNum());
			for (BattleSoldier jin : tarList) {
				jin.addBuff12302(getEffVal(EffType.EFF_12302));
			}
		}

	}

	@Override
	public int skillHPExactly() {
		int result = super.skillHPExactly();
		if (getEffVal(EffType.EFF_1150) > 0) {
			int pct = (int) (getDeadCnt() * 10D / getOriCnt()) + 1;
			result = result + pct * getEffVal(EffType.EFF_1150);
		}
		
		HawkTuple3<Integer, Integer, Integer> eff12163Debuff = eff12163DebuffVal();
		if (eff12163Debuff.second > 0) {
			result -= eff12163Debuff.second;
			addDebugLog("###{}采矿车处于【损坏状态】时，其防御、生命加成减少+{} ", getUUID(), eff12163Debuff.second);
		}
		result += buff84601Val();
		return result;
	}
	
	@Override
	public double reduceHurtValPct(BattleSoldier atkSoldier, double hurtVal) {
		hurtVal = super.reduceHurtValPct(atkSoldier, hurtVal);
		if (getEffVal(EffType.HERO_12172) > 0 || getEffVal(EffType.HERO_12173) > 0) {
			// 点燃状态的
			long dianRans = getTroop().getEnemyTroop().getSoldierList().stream().filter(BattleSoldier::isAlive).filter(BattleSoldier::isDianran).count();
			double eff12173 = getEffVal(EffType.HERO_12173)*Math.min(ConstProperty.getInstance().getEffect12173MaxNum(),dianRans);
			hurtVal *= (1 - getEffVal(EffType.HERO_12172) * GsConst.EFF_PER - eff12173 * GsConst.EFF_PER);
		}
		
		HawkTuple3<Integer, Integer, Integer> eff12163Debuff = eff12163DebuffVal();
		if (eff12163Debuff.third > 0) {
			hurtVal *= (1 + eff12163DebuffVal().third * GsConst.EFF_PER);
			hurtVal = Math.max(hurtVal, 0);
			addDebugLog("###{}采矿车处于【损坏状态】时，其在受到攻击时，伤害额外 +{} ", getUUID(), eff12163Debuff.third);
		}
		return hurtVal;
	}


	private void chuantouAttack(BattleSoldier defSoldier) {
		// 【芯片设计概述】：采矿车攻击时，有概率额外进行一次自毁攻击，对敌军最多 3 个目标造成采矿车部队生命 XXX% 的爆炸伤害（爆炸伤害无视目标防御），采矿车也会受到同等伤害的反噬。
		// 单人出征或集结时，如果采矿车超过部队总数的 10%，当采矿车触发自毁攻击时，将减少目标 XX% 的部队生命和 XX% 的部队防御，最多可叠加 N 层。
		//
		// 【专属芯片】
		// 【1528】采矿车（兵种类型=8）攻击时，有概率额外触发AOE攻击，触发AOE攻击时最多可命中3个目标，由前至后（同希伯特 贯穿弹选取目标的逻辑一样，只是无伤害衰减）,造成XX%伤害，在触发AOE时，采矿车自身并受到XX%反噬伤害
		// 注：不同等级不同兵种类型均视为不同目标。
		// 注：当敌方目标小于等于3个时，全中（各攻击1次），当敌方目标大于3个时，由前至后站位3个目标受到1次AOE攻击
		// 注：此AOE触发是额外追加的伤害，并不是代替普攻。
		// 注：AOE 实际伤害= 采矿车当前的部队生命总值^Eff1*【1528】* Eff2 输出是根据采矿的生命计算的，且无需计算对面单位防御
		// 采矿受到反噬伤害=采矿车当前的部队生命总值^Eff1*【1528】*Eff2 （1-【1531】）
		// 触发概率读取const表，字段effect1528Prob（万分比数值 ，填5000，即50%）
		// Eff1参数读取const表，字段effect1528Power1（万分比数值 ，填7000，即70%）
		// Eff2参数读取const表，字段effect1528Power2（万分比数值 ，填7000，即70%）
		// 【1529】单人出征或集结时，如果采矿车超过部队总数的10%，当采矿车触发AOE时，将减少AOE打击的所有目标 XX% 的部队生命，最多可叠加 N 层（持续至战斗结束）。
		// 【目标受击需要判定】读取目标敌方部队减益层数，作为参数A（初始为0），上限记为参数B（Const表字段，effect1529Maximum）
		// Eff = 【1529】*min（参数A，参数B）
		// 受击者实际生命=受击者基础生命*（1+其他生命加成-Eff）
		// 注： 【1529】有触发条件，不满足条件，不触发此减益，满足条件，则100%可给目标挂上减益
		// 【1530】单人出征或集结时，如果采矿车超过部队总数的10%，当采矿车触发AOE时，将减少AOE打击的所有目标 XX% 的部队防御，最多可叠加 N 层（持续至战斗结束）。
		// 【目标受击需要判定】读取目标敌方部队减益层数，作为参数A（初始为0），上限记为参数B（Const表字段，effect1530Maximum）
		// Eff = 【1530】*min（参数A，参数B）
		// 受击者实际防御=受击者基础防御*（1+其他防御加成-Eff）
		// 注： 【1530】有触发条件，不满足条件，不触发此减益，满足条件，则100%可给目标挂上减益
		//
		// 【专属天赋】
		// 【1531】减少【1528】导致的对自身矿车的反噬伤害 （万分比数值 ，填5000，即50%
		// 采矿受到反噬伤害=采矿车当前的部队生命总值^Eff1*【1528】*Eff2 （1-【1531】）
		if (getEffVal(EffType.HERO_1528) > 0 && HawkRand.randInt(GsConst.RANDOM_MYRIABIT_BASE) < ConstProperty.getInstance().getEffect1528Prob()) {
			triger1528 = true;

			// 注：AOE 实际伤害= 采矿车当前的部队生命总值^Eff1*【1528】* Eff2 输出是根据采矿的生命计算的，且无需计算对面单位防御
			hurtVal1528 = Math.pow(getHpVal() * getFreeCnt(), ConstProperty.getInstance().getEffect1528Power1() * 0.0001)
					* getEffVal(EffType.HERO_1528) * GsConst.EFF_PER
					* ConstProperty.getInstance().getEffect1528Power2() * GsConst.EFF_PER;
			// 防守兵
			List<BattleSoldier> soldierList = defSoldier.getTroop().getSoldierList();
			int atkcount = 0;
			for (BattleSoldier defsold : soldierList) {
				if (defsold.getxPos() < defSoldier.getxPos() || !defsold.canBeAttack() || !defsold.isAlive()) {
					continue;
				}
				super.attackOnce(defsold, QIAN_PAI_MAX, 0, Integer.MAX_VALUE, false);
				// 叠减血, 防debuff
				if (this.getEffVal(EffType.HERO_1529) > 0) {
					defsold.incrDebuf1528Val(this.getEffVal(EffType.HERO_1529), this.getEffVal(EffType.HERO_1530));
				}

				atkcount++;
				if (atkcount == 3) {
					break;
				}
			}

			// 采矿受到反噬伤害=采矿车当前的部队生命总值^Eff1*【1528】*Eff2 （1-【1531】）
			int curCnt = this.getFreeCnt();
			double selfHurlt = hurtVal1528 * (1 - getEffVal(EffType.HERO_1531) * GsConst.EFF_PER);
			selfHurlt = this.forceField(defSoldier, selfHurlt);
			int maxKillCnt = (int) Math.ceil(4.0f * selfHurlt / this.getHpVal());
			maxKillCnt = Math.max(1, maxKillCnt);
			int killCnt = Math.min(maxKillCnt, curCnt);
			getTroop().getBattle().addDebugLog("### 触发1528 自爆伤害: {} ,自杀公式: {} ,实际死亡: {}", selfHurlt, "Math.ceil(4.0f * selfHurlt / this.getHpVal())", killCnt);
			this.addDeadCnt(killCnt);
			defSoldier.addKillCnt(this, killCnt);
			// 被攻击
			this.attacked(defSoldier, killCnt);
		}
		triger1528 = false;
	}

	@Override
	protected double getHurtVal(BattleSoldier defSoldier, double reducePer) {
		if (triger1528) {
			getTroop().getBattle().addDebugLog("### 触发1528 造成伤害 {}", hurtVal1528);
			return hurtVal1528;
		}

		double result = super.getHurtVal(defSoldier, reducePer);
		if (jelani12005AOE) {
			result = result * GsConst.EFF_PER * getEffVal(EffType.HERO_12005);
			addDebugLog("-Jelani {} 12005AOE（伤害率 {}）hurt {} ", getUUID(), getEffVal(EffType.HERO_12005), result);
		}
		if (attack12163) {
			result = result * GsConst.EFF_PER * getEffVal(EffType.HERO_12163);
			addDebugLog("-杰西卡 {} 引爆 {} 伤害率 {} hurt {} ", getUUID(), defSoldier.getUUID(), getEffVal(EffType.HERO_12163), result);
		}
		return result;
	}

	@Override
	protected void attackOver(BattleSoldier defSoldier, int killCnt, double hurtVal) {
		super.attackOver(defSoldier, killCnt, hurtVal);
		if (getEffVal(EffType.HERO_1681) > 0) {
			defSoldier.addDebuff1681(getEffVal(EffType.HERO_1681));
			getTroop().getBattle().addDebugLog("### {} vs {} 1681 debuf {} ", getUUID(), defSoldier.getUUID(), defSoldier.debuff1681Val());
		}
		if (getEffVal(EffType.EFF_12554) > 0 && defSoldier.isJinZhan()) {
			int debuffEffect12554Value = defSoldier.debuffEffect12554Value + getEffVal(EffType.EFF_12554);
			debuffEffect12554Value = Math.min(debuffEffect12554Value, ConstProperty.getInstance().effect12554MaxValue);
			defSoldier.debuffEffect12554Value = debuffEffect12554Value;
			addDebugLog(" 12554 降低 {} 攻击、防御、生命  {}", getUUID(), debuffEffect12554Value);
		}
	}

	@Override
	public int skillDefExactly(BattleSoldier atkSoldier) {
		int result = super.skillDefExactly(atkSoldier);
		BattleSoldierSkillCfg skill = getSkill(PBSoldierSkill.CANNON_SOLDIER_8_SKILL_3);
		if (BattleConst.WarEff.CITY_ATK.check(getTroop().getWarEff()) && trigerSkill(skill, GsConst.RANDOM_MYRIABIT_BASE)) {
			result += skill.getP1IntVal();
		}
		
		HawkTuple3<Integer, Integer, Integer> eff12163Debuff = eff12163DebuffVal();
		if (eff12163Debuff.second > 0) {
			result -= eff12163Debuff.second;
			addDebugLog("###{}采矿车处于【损坏状态】时，其防御、生命加成减少+{} ", getUUID(), eff12163Debuff.second);
		}
		return result;
	}

	@Override
	public int skillAtkExactly(BattleSoldier defSoldier) {
		return getEffVal(EffType.CANNON_ATK_CITY_HERT);
	}

	@Override
	protected PBSoldierSkill keZhiJiNengId() {
		return PBSoldierSkill.CANNON_SOLDIER_8_SKILL_66;
	}

	/** 机甲1633的套值*/
	public double jijia1633DunNum() {
		return getFreeCnt() * getHpVal() * GsConst.EFF_PER * getTroop().getMaxSOLDIER_1633();
	}

	public int getRound1639() {
		return round1639;
	}

	public void setRound1639(int round1639) {
		this.round1639 = round1639;
	}

	@Override
	protected PBSoldierSkill honor10SkillId() {
		return PBSoldierSkill.CANNON_SOLDIER_8_SKILL_34;
	}

	@Override
	public Map<EffType, Integer> getEffMapClientShow() {
		Map<EffType, Integer> result = super.getEffMapClientShow();
		mergeClientShow(result, EffType.HERO_12004, EffType.FIRE_2008);
		mergeClientShow(result, EffType.EFF_12033, EffType.CANNON_B_DEF_PER, EffType.CANNON_B_HP_PER);
		mergeClientShow(result, EffType.PLANT_SOLDIER_SKILL_844, EffType.CANNON_B_DEF_PER);
		return result;
	}
	
	@Override
	public int skillIgnoreTargetHP(BattleSoldier target) {
		int result = super.skillIgnoreTargetHP(target);
		if (target.isJinZhan()) {//采矿车攻击近战部队时，减少目标生命加成+XX%
			BattleSoldierSkillCfg skill_241 = getSkill(PBSoldierSkill.SOLDIER_SKILL_84101);
			result += skill_241.getP1IntVal();
		}
		return result;
	}
	
	private int buff84601Val() {
		if (buff84601map == null) {
			return 0;
		}
		Map.Entry<Integer, Integer> ent = buff84601map.floorEntry(lossRatePct()*100);
		if (ent == null) {
			return 0;
		}
		return ent.getValue();
	}
	
}
