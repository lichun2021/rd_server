package com.hawk.game.player.skill.talent;

import java.util.List;

import org.hawk.result.Result;

import com.hawk.game.config.SkillCfg;
import com.hawk.game.player.Player;

/**
 * 总动员： 行军上限增加x%, 持续m小时
 * @author golden
 *
 */
@TalentSkill(skillID = 10102)
public class Skill10102 implements ITalentSkill {

	@Override
	public Result<?> onCastSkill(Player player, SkillCfg skillCfg, List<Integer> params) {
		int buffId = skillCfg.getBuffId();
		player.addStatusBuff(buffId);
		return Result.success();
	}
}
