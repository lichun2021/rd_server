package com.hawk.game.player.skill.talent;

import java.util.List;

import org.hawk.result.Result;

import com.hawk.game.config.SkillCfg;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Status;

/**
 * 丰收：所有资源田立即获得x小时产出
 * @author golden
 *
 */
@TalentSkill(skillID = 10201)
public class Skill10201 implements ITalentSkill {

	@Override
	public Result<?> onCastSkill(Player player, SkillCfg skillCfg, List<Integer> params) {
		// 没有资源矿，不能使用技能
		if (player.getData().getResBuildCount() <= 0) {
			return Result.fail(Status.Error.SKILL_ERROR_HAS_NO_RES_VALUE);
		}
		long timeLong = (long)(Integer.parseInt(skillCfg.getParam1()) * 1000);
		player.productResourceQuickly(timeLong, true);
		return Result.success();
	}
}
