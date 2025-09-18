package com.hawk.game.battle.effect.impl.eff12131;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.battle.BattleSoldier_2;
import com.hawk.game.battle.effect.BattleConst.WarEff;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;

/**
 【12143】
	- 【万分比】【12143】战技持续期间，触发磁暴干扰时，并额外降低目标 +XX.XX% 的轰炸概率（该效果不可叠加，持续 2 回合）
	  - 战报相关
	- 于战报中隐藏
	- 不合并至精简战报中
	  - 此作用号绑定磁暴干扰作用号【12133】，仅在【12133】的当次追加攻击命中目标后生效
	  - 此伤害加成降低效果为debuff效果，记在被攻击方身上
	  - 轰炸概率
	- 对直升机兵种，是指其兵种技能【黑鹰轰炸】【id = 403】的发动概率
	  - 即【黑鹰轰炸】实际概率 = 基础概率 + 其他加成 - 【本作用值】
	    - 基础概率读取const表，字段trigger
	- 对轰炸机兵种，是指其兵种技能【俯冲轰炸】【id = 303】的发动概率
	  - 即【俯冲轰炸】实际概率 = 基础概率 + 其他加成 - 【本作用值】
	    - 基础概率读取const表，字段trigger
	  - 该效果不可叠加，先到先得，持续回合结束后消失
	- 数值不叠加
	- 回合数不可刷新重置
	  - 另外：此作用号效果与英雄米迦勒的专属芯片效果【1553】不可同时生效
	- 具体实现为：当作用号【12143】存在时，强制让作用号【1553】失效
	[图片]
	  - 此为英雄战技专属作用号，配置格式如下：
	- 作用号id_参数1_参数2
	  - 参数1：作用号系数
	    - 配置格式：浮点数
	    - 即本作用值 = 英雄军事值 * 参数1/10000
	  - 参数2：持续回合数
	    - 配置格式：绝对值
	    - 注：由被附加开始到当前回合结束，算作 1 回合
 * @author lwt
 * @date 2023年12月4日
 */

@BattleTupleType(tuple = Type.SOLDIER_SKILL)
@EffectChecker(effType = EffType.HERO_12143)
public class Checker12143 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		if (parames.type == SoldierType.TANK_SOLDIER_2 && parames.troopEffType != WarEff.NO_EFF) {
			effPer = parames.unity.getEffVal(effType());
			parames.solider.setEffVal(EffType.SSS_SKILL_12143_P2, parames.unity.getEffVal(EffType.SSS_SKILL_12143_P2));
		}
		return new CheckerKVResult(effPer, effNum);
	}
}
