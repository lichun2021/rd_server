package com.hawk.game.battle.effect.impl.plant45;

import org.hawk.config.HawkConfigManager;

import com.hawk.game.battle.BattleSoldier;
import com.hawk.game.battle.BattleUnity;
import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.config.BattleSoldierSkillCfg;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.PBSoldierSkill;

/**
 *  【54501】突击步兵
- 突击步兵（兵种类型 = 5），新增技能id = 54501
- 技能效果：敌方出征空军单位数每达到1个，自身攻击+XX%（该效果可加，至多X层）
  - 敌方空军单位数：敌方战报中的单排空军计数
    - 不同玩家不同等级的空军均视为独立单位
    - 空军：直升机（兵种类型 = 4）和轰炸机（兵种类型 = 3）
    - 单人时取敌方单人所有空军部队，集结时取集结敌方所有玩家的所有空军部队
  - 此为常规外围属性加成效果，与其他作用号累加计算
    - 即 实际属性 = 基础属性*（1 + 各类加成 +【本作用值】）
  - 相关技能参数读取battle_soldier_skill表，对应参数效果如下
    - trigger：触发概率，这里无意义（填上10000）
      - 配置格式：万分比
    - p1：敌方兵种类型
      - 配置格式：兵种类型1_兵种类型2_......
    - p2：敌方出征空军单位数 生效指定数值
      - 配置格式：绝对值
    - p3：单次增加攻击加成数值
      - 配置格式：万分比
    - p4：该效果最多叠加层数
      - 配置格式：绝对值
 */
@BattleTupleType(tuple = { Type.ATK })
@EffectChecker(effType = EffType.EFF_SOLDIER_SKILL_54501)
public class CheckerSkill54501 implements IChecker {

	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (!parames.solider.hasSkill(PBSoldierSkill.SOLDIER_SKILL_54501)) {
			return CheckerKVResult.DefaultVal;
		}

		BattleSoldierSkillCfg scfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierSkillCfg.class, 54501);
		if (scfg == null) {
			return CheckerKVResult.DefaultVal;
		}
		
		int cen = (int) parames.tarStatic.getUnityList().stream().map(BattleUnity::getSolider).filter(BattleSoldier::isPlan).count();
		int effPer = Math.min(cen, scfg.getP4IntVal()) * scfg.getP3IntVal();

		return new CheckerKVResult(effPer, 0);
	}

	@Override
	public boolean tarTypeSensitive() { 
		return false;
	}
}
