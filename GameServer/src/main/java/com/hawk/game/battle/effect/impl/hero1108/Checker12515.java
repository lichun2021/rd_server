package com.hawk.game.battle.effect.impl.hero1108;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;

/**
12515】
- 【万分比】【12515】自身出征数量最多的主战坦克在触发坦克对决时，本次攻击额外附加如下效果
  - 重力缠绕：本次攻击中，被攻击单位和所有被追加攻击的单位会进入同一重力缠绕状态；其中某一单位受到自身主战坦克伤害时，处于重力缠绕中的其他单位同时受到此次伤害的 XX.XX% 【12515】，触发后解除本次生效的重力缠绕状态（该效果可以叠加，集结时每个主战坦克单位造成的重力缠绕，都仅对自身生效，每个单位最多同时受到 X（effect12515Maxinum） 次重力缠绕）；->针对敌方兵种留个内置修正系数（effect12515SoldierAdjust）
  - 战报相关
    - 于战报中隐藏
    - 不合并至精简战报中
  - 自身出征数量最多的主战坦克在触发坦克对决时
    - 触发条件同【12513】
  - 本次攻击中
    - 自身出征数量最多的主战坦克在触发坦克对决时所进行的攻击
  - 被攻击单位和所有被追加攻击的单位
    - 本次攻击目标单位
    - 本次攻击触发的追加攻击目标单位
      - 坦克对决【12131】、歼灭突袭【12513】和猎袭追击【12514】所有目标单位
  - 会进入同一重力缠绕状态
    - 进入同一重力缠绕状态
      - 此处可理解为多个单位被相互链接，每次触发会产生新的链接，但是和原有链接完全独立存在
        - 如：A、B和C被链接标记为1号链接，A和D被链接时会标记为2号链接，A同属处于两个重力缠绕状态
      - 同一重力缠绕状态，则为被同号标记的链接，此处A的同缠绕状态有B、C、D，但是和D同缠绕状态的只有A
    - 注：一个单位也可以单独进入一个缠绕状态
  - 其中某一单位受到自身出征数量最多的主战坦克伤害时，处于重力缠绕中的其他单位同时受到此次伤害的 XX.XX% 
    - 传递伤害和解除状态效果，优先于施加重力缠绕状态
      - 本次攻击目标如已有重力缠绕状态，则优先触发传递伤害和解除状态，后根据本次攻击结果再次进行重力缠绕状态施加
    - 直接传递伤害数值：在主目标计算完防御、增减伤和其他作用号影响后，实际受到的伤害数值再进行传递，即
      - 其他单位受到实际伤害 = 主目标受到实际伤害数值 *【本作用值】*敌方兵种修正系数/10000）
        - 其他单位受到的实际伤害：不再计算其防御、增减伤和其他作用号影响，直接对单位造成该值的伤害
        - 实际针对敌方各兵种类型，单独配置系数；敌方兵种修正系数 读取const表，字段effect12515SoldierAdjust
          - 配置格式：兵种类型id1_修正系数1，......兵种类型id8_修正系数8
            - 修正系数具体配置为万分比
        - 配置格式：万分比
    - 注：此处触发重力缠绕状态需要自身出征数量最多的主战坦克
    - 注：只有其他单位会受到此次伤害，如果重力缠绕单位只有一个单位，则不会造成传导伤害，但是还是视为触发重力缠绕状态，会接触并触发后续效果
  - 触发后解除本次生效的重力缠绕状态（该效果可以叠加，集结时每个主战坦克单位造成的重力缠绕，都仅对自身生效，
    - 所有处于该重力缠绕状态的单位，均会解除该状态
    - 如果某单位处于多个重力缠绕状态中，只有本次生效的重力缠绕状态被解除
      - 多个玩家的重力缠绕状态独立，不会交互触发，也不会解除其他玩家的
      - 单个玩家的重力缠绕状态，理论上不会出现一个单位处于多个重力缠绕状态（因为每次攻击都会先触发再链接）
  - 每个单位最多同时受到 X（effect12515Maxinum） 次重力缠绕）；
    - 层数上限读取const表，字段effect12515Maxinum
      - 配置格式：绝对值
 */

@BattleTupleType(tuple = Type.SOLDIER_SKILL2)
@EffectChecker(effType = EffType.HERO_12515)
public class Checker12515 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (parames.solider.getEffVal(EffType.HERO_12131) <= 0|| parames.unity != parames.getPlayerMaxFreeArmy(parames.unity.getPlayerId(), SoldierType.TANK_SOLDIER_2)) {
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