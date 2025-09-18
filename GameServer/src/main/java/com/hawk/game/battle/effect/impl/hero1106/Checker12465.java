package com.hawk.game.battle.effect.impl.hero1106;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;

/**
 * - 【万分比】【12465】集结战斗开始前，若自身出征直升机数量超过自身出征部队总数 50%且不低于集结部队总数 5%，自身出征数量最多的直升机于本场战斗中获取如下效果（多个薇拉同时存在时，至多有 2(effect12465Maxinum) 个直升机单位生效）:
  - 空域护航: 每攻击命中 1(effect12465AddFirePoint) 次敌方单位后，增加自身 10(effect12465AddFirePoint) 点磁能；
    - 每第 5(effect12465AtkRound) 回合开始时，若当前磁能值不低于100(effect12465AtkThresholdValue) 点，则为自身及身后 2(effect12465InitChooseNum) 个非直升机单位提供空域护航效果，使其受到伤害减少 【固定值(effect12465BaseVaule) + XX.XX%【12465】*己方远程单位数】，并清空自身磁能值（持续 3(effect12465ContinueRound) 回合；
    - 己方远程单位计数时至多取 8(effect12465CountMaxinum) 个）
  - 战报相关
    - 于战报中隐藏
    - 不合并至精简战报中
  - 仅对集结战斗生效（包含集结进攻和集结防守）
  - 在战斗开始前判定，满足条件后本场战斗全程生效
  - 注：若存在荣耀所罗门的幻影部队，此作用号对自身幻影部队不生效
  - 注：此作用号与卡尔专属作用号【12201】基本完全一致
[图片]
  - 每攻击命中 1 次敌方单位后，增加自身 10 点磁能
    - 增加磁能读取const表，字段effect12465AddFirePoint
      - 配置格式：绝对值
  - 每第 5 回合开始时
    - 战斗中每第 5 的倍数的回合开始后，自身开始普攻前，进行判定（须保证在伊娜莉莎释放轮番轰炸技能之前）
    - 指定回合数读取const表，字段effect12465AtkRound
      - 配置格式：绝对值
  - 若当前磁能值不低于 100 点
    - 数量1 = 自身直升机磁能值
    - 数量2 = 触发磁能值
      - 数量1/数量2 >= 指定数量时生效以下效果
        - 指定数值读取const表，字段effect12465AtkThresholdValue
  - 为自身及身后最近 2 个非直升机单位提供空域护航效果，使其受到伤害减少 【固定值 + XX.XX%*己方远程单位数】
    - 自身及身后最近 2 个非直升机单位（直升机兵种类型 = 4）
      - 直升机后的兵种有：突击步兵（兵种类型 = 5）、狙击兵（兵种类型 = 6）、攻城车（兵种类型 = 7）
      - 注：此处所谓身后最近的意思与尤利娅的专属作用号【12412】的选取逻辑类似
[图片]
      - 具体选取规则如下（在选取过程中，己方单位只能接受 1 层此作用号效果）
        - 筛选出身后（战斗中的排序）的战斗单位（排除掉非直升机单位和已拥有此效果的单位）
        - 取 1 个最近的
          - 然后以此循环直至己方目标单位不足或已达到可选取上限
    - 基础可选取上限数量读取const表，字段effect12465InitChooseNum
      - 配置格式：绝对值
    - 其受到伤害减少
      - 注：此处有随己方兵种类型，有内置系数
      - 实际数值 = （该作用号固定数值 + 【作用值】*己方远程单位数）*【兵种修正系数】
      - 该作用号固定数值读取const表，字段effect12465BaseVaule
        - 配置格式：万分比
      - 各兵种修正系数读取const表，字段effect12465Adjust
        - 配置格式：兵种类型id1_修正系数1，......兵种类型id8_修正系数8
          - 修正系数具体配置为万分比
      - 该作用号为伤害减少效果，与其他作用号累乘计算，即 
        - 实际伤害 = 基础伤害*（1 - 其他作用号）*（1 - 【实际数值】）
  - 持续 3 回合（本回合被附加到下回合开始算 1 回合）
    - 持续回合数读取const表，字段effect12465ContinueRound
      - 配置格式：绝对值
  - （己方远程单位计数时至多取 8 个）
    - 不同玩家不同等级的远程单位均视为独立单位
    - 远程单位包含有：直升机（兵种类型 = 4）、突击步兵（兵种类型 = 5）、狙击兵（兵种类型 = 6）和攻城车（兵种类型 = 7）
    - 单人时取己方单人所有远程单位，集结时取集结己方所有玩家的所有远程单位
    - 另外该计数有最高值限制，读取const表，字段effect12465CountMaxinum
      - 配置格式：绝对值
  - （多个薇拉同时存在时，至多有 2 个直升机单位获得上述效果）
    - 单玩家拥有此作用号时，只能提供 1 层效果；集结中存在多个此作用号时，可以同时生效
    - 效果记录在己方部队身上，限制该作用号生效的玩家个数和战斗单位个数
      - 注：这里限制的是生效层数上限，若集结中层数超出此上限，取作用号数值高的；即这里只能有 2 个玩家携带的作用号生效（而实际被施加此效果的战斗单位，至多只能接受 1 层数值）
      - 层数上限读取const表，字段effect12465Maxinum
        - 配置格式：绝对值
 */
@BattleTupleType(tuple = Type.SOLDIER_SKILL2)
@EffectChecker(effType = EffType.HERO_12465)
public class Checker12465 implements IChecker {

	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (parames.solider.getEffVal(EffType.HERO_12461) <= 0) {
			return CheckerKVResult.DefaultVal;
		}

		int effPer = 0;
		int effNum = 0;
		effPer = parames.unity.getEffVal(effType());

		return new CheckerKVResult(effPer, effNum);
	}

	@Override
	public boolean tarTypeSensitive() {
		return false;
	}
}
