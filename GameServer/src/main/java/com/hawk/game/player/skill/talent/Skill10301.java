package com.hawk.game.player.skill.talent;

import java.util.List;

import org.hawk.result.Result;

import com.hawk.game.config.ConstProperty;
import com.hawk.game.config.SkillCfg;
import com.hawk.log.Action;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Status;

/**
 * 体力充沛：使用后立即获得x点体力
 * @author golden
 *
 */
@TalentSkill(skillID = 10301)
public class Skill10301 implements ITalentSkill {

	@Override
	public Result<?> onCastSkill(Player player, SkillCfg skillCfg, List<Integer> params) {
		if (player.getVit() >= ConstProperty.getInstance().getActualVitLimit()) {
			return Result.fail(Status.Error.SKILL_ERROR_VIT_IS_FULL_VALUE); //体力已满，不能使用技能
		}
		
		player.increaseVit(Integer.parseInt(skillCfg.getParam1()), Action.USE_SKILL_10301, false);
		player.getPush().syncPlayerInfo();
		return Result.success();
	}
}
