package com.hawk.game.player.skill.talent;

import java.util.List;

import org.hawk.result.Result;

import com.hawk.game.config.SkillCfg;
import com.hawk.game.player.Player;

/**
 * 采集资源速度提升x% 持续x小时
 * @author golden
 *
 */
@TalentSkill(skillID = 10202)
public class Skill10202 implements ITalentSkill {

	@Override
	public Result<?> onCastSkill(Player player, SkillCfg skillCfg, List<Integer> params) {
		int buffId = skillCfg.getBuffId();
		player.addStatusBuff(buffId);
		return Result.success();
	}
}
