package com.hawk.game.battle.effect.impl.plant45;

import java.util.Objects;

import org.hawk.config.HawkConfigManager;

import com.hawk.game.battle.BattleUnity;
import com.hawk.game.battle.effect.BattleConst;
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
 *  【44501】直升机
- 直升机（兵种类型 = 4），新增技能id = 44501
- 技能效果：【光环】集结战斗时，己方全体空军和步兵单位，生命+XX%（该效果可加，至多X层）
  - 空军单位类型包含有：轰炸机（兵种类型 = 3）、直升机（兵种类型 = 4）
  - 步兵单位类型包含有：突击步兵（兵种类型 = 5）、狙击兵（兵种类型 = 6）
  - 此为常规外围属性加成效果，与其他作用号累加计算
    - 即 实际属性 = 基础属性*（1 + 各类加成 +【本作用值】）
  - 相关技能参数读取battle_soldier_skill表，对应参数效果如下
    - trigger：触发概率，这里无意义（填上10000）
      - 配置格式：万分比
    - p1：友方兵种类型
      - 配置格式：兵种类型1_兵种类型2_......
    - p2：单次增加生命加成数值
      - 配置格式：万分比
    - p3：该效果最多叠加层数
      - 配置格式：绝对值
 */
@BattleTupleType(tuple = Type.HP)
@EffectChecker(effType = EffType.EFF_SOLDIER_SKILL_44501)
public class CheckerSkill44501 implements IChecker {

	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (!BattleConst.WarEff.MASS.check(parames.troopEffType)) {
			return CheckerKVResult.DefaultVal;
		}
		if (!parames.solider.isPlan() && !parames.solider.isFoot()) {
			return CheckerKVResult.DefaultVal;
		}

		int effPer = 0;
		String key = getSimpleName();
		Object object = parames.getLeaderExtryParam(key);
		if (Objects.nonNull(object)) {
			effPer = (Integer) object;
			return new CheckerKVResult(effPer, 0);
		} 

		BattleSoldierSkillCfg scfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierSkillCfg.class, 44501);
		if (scfg == null) {
			return CheckerKVResult.DefaultVal;
		}

		int cen = (int) parames.unityList.stream()
				.map(BattleUnity::getSolider)
				.filter(s -> s.hasSkill(PBSoldierSkill.SOLDIER_SKILL_44501))
				.count();

		effPer = Math.min(cen, scfg.getP3IntVal()) * scfg.getP2IntVal();
		parames.putLeaderExtryParam(key, effPer);

		return new CheckerKVResult(effPer, 0);
	}

	

	@Override
	public boolean tarTypeSensitive() {
		return false;
	}
}
