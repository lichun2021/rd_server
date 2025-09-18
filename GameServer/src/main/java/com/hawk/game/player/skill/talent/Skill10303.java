package com.hawk.game.player.skill.talent;

import java.util.List;

import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkTime;
import org.hawk.result.Result;

import com.hawk.game.config.BuffCfg;
import com.hawk.game.config.SkillCfg;
import com.hawk.game.entity.StatusDataEntity;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const;
import com.hawk.game.util.GsConst;

/**
 * 遍地泥浆：使用后攻击或者侦查你的行军时间提升n倍，持续m分钟
 * @param player
 */
@TalentSkill(skillID = 10303)
public class Skill10303 implements ITalentSkill {

	@Override
	public Result<?> onCastSkill(Player player, SkillCfg skillCfg, List<Integer> params) {
		// 清空之前的守兵死亡记录
		player.skill10303DurningDead = 0;
		
		int buffId = skillCfg.getBuffId();
		BuffCfg buffCfg = HawkConfigManager.getInstance().getConfigByKey(BuffCfg.class, buffId);
		long now = HawkTime.getMillisecond();
		long endTime = now + (int) Math.ceil(buffCfg.getTime() * (1 + player.getEffect().getEffVal(Const.EffType.HERO_1629) * GsConst.EFF_PER)) * 1000;
		
		StatusDataEntity entity = player.addStatusBuff(buffId,endTime);
		if (entity != null) {
			player.getPush().syncPlayerStatusInfo(false, entity);
		}
		return Result.success();
	}
}
