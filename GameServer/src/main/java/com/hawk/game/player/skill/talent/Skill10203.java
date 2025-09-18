package com.hawk.game.player.skill.talent;

import java.util.List;

import org.hawk.result.Result;

import com.hawk.game.config.SkillCfg;
import com.hawk.game.player.Player;

/**
 * 资源保护：基地中所有资源在x小时内不会被掠夺
 * @author golden
 *
 */
@TalentSkill(skillID = 10203)
public class Skill10203 implements ITalentSkill {

	@Override
	public Result<?> onCastSkill(Player player, SkillCfg skillCfg, List<Integer> params) {
		int buffId = skillCfg.getBuffId();
		player.addStatusBuff(buffId);
		return Result.success();
	}
}
