package com.hawk.game.battle.effect.impl.manhattan;

import com.hawk.game.battle.BattleUnity;
import com.hawk.game.battle.effect.BattleConst;
import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;

/**
 * 【11042~11043】
- 【万分比】【11042~11043】进攻战斗时，在战斗开始时，随机选中敌方某指挥官的全部部队进行 1 轮风暴干扰；使其随机陷入下述状态之一
  - 效果A：超能攻击、攻击减少 XX%（该效果不可叠加，持续 10 回合）（此效果对空军 翻倍）
  - 效果B：防御、生命减少 XX%（该效果不可叠加，持续 10 回合）（此效果对空军 翻倍）
  - 注：因涉及2个数值，此处拆分为2个作用号进行开发；其中【11042】作为主作用号，控制机制和效果A数值，【11043】作为辅作用号，仅影响效果B数值
  - 战报相关
    - 于战报中隐藏
    - 不合并至精简战报中
  - 在战斗开始时判定，满足条件后本次战斗生效1次
    - 这里可理解为在战斗开始时，给敌方目标直接上debuff
  - 该作用号仅在进攻战斗时生效（包含个人进攻和集结进攻）
    - 集结时，进攻方均可生效此作用号，各甩各的，相互独立
  - 随机选中敌方某指挥官的全部部队
    - 按敌方参战玩家进行纯随机，选中后目标为该玩家本次战斗的全部部队
      - 注：优先选中未被此作用号影响的敌方玩家
        - 即：每次选择目标时，筛选出未被此作用号影响的敌方玩家，从中随机1个作为目标
      - 注：若为NPC部队，则按NPC所属进行随机
  - 使其随机陷入下述状态之一
    - 这里纯随机效果A和效果B，其中之一
    - 效果A：超能攻击、攻击减少 XX%（该效果不可叠加，持续 10 回合）（此效果对空军 翻倍）
      - 此debuff效果为外围属性加成数值减少
        - 即实际超能攻击 = 各类超能攻击加成 - 【本作用值】
        - 即实际攻击 = 基础攻击*（1 + 各类攻击加成  - 【本作用值】）
      - 即某单位挂上该作用号后，后续再生效此作用号时，数值不变且持续回合数不变
        - 持续回合数读取const表，字段effect11042ContinueRound
          - 配置格式：绝对值
          - 注：从被赋予作用号开始到本回合结束后，算作1回合
      - 另外此效果对于各兵种有单独修正系数（玩家不可见），读取const表，字段effect11042Adjust
        - 配置格式：兵种类型id1_修正系数1，......兵种类型id8_修正系数8
        - 即实际效果数值 = 【11042】* 修正系数 
      - 注：若存在荣耀所罗门的幻影部队，此作用号对幻影部队无效！！
    - 效果B：防御、生命减少 XX%（该效果不可叠加，持续 10 回合）（此效果对空军 翻倍）
      - 此debuff效果为外围属性加成数值减少
        - 即实际防御 = 基础防御*（1 + 各类防御加成  - 【本作用值】）
        - 即实际生命 = 基础生命*（1 + 各类生命加成  - 【本作用值】）
      - 即某单位挂上该作用号后，后续再生效此作用号时，数值不变且持续回合数不变
        - 持续回合数读取const表，字段effect11043ContinueRound
          - 配置格式：绝对值
          - 注：从被赋予作用号开始到本回合结束后，算作1回合
      - 另外此效果对于各兵种有单独修正系数（玩家不可见），读取const表，字段effect11043Adjust
        - 配置格式：兵种类型id1_修正系数1，......兵种类型id8_修正系数8
        - 即实际效果数值 = 【11043】* 修正系数 
      - 注：若存在荣耀所罗门的幻影部队，此作用号对幻影部队无效！！
 */
@BattleTupleType(tuple = Type.SOLDIER_SKILL)
@EffectChecker(effType = EffType.EFF_11042)
public class Checker11042 implements IChecker {

	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (!BattleConst.WarEff.ATK.check(parames.troopEffType)) {
			return CheckerKVResult.DefaultVal;
		}
		String pid = parames.unity.getPlayerId();
		BattleUnity max = parames.getPlayeMaxMarchArmy(pid);
		if (parames.unity != max) {
			return CheckerKVResult.DefaultVal;
		}

		int effPer = 0;
		int effNum = 0;
		if (isSoldier(parames.type)) {
			effPer = parames.unity.getEffVal(effType());
		}

		return new CheckerKVResult(effPer, effNum);
	}

	@Override
	public boolean tarTypeSensitive() {
		return false;
	}

}
