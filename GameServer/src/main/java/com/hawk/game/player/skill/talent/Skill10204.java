package com.hawk.game.player.skill.talent;

import java.util.List;

import org.hawk.result.Result;

import com.hawk.game.config.SkillCfg;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Status;
import com.hawk.game.world.service.WorldResourceService;

/**
 * 无中生有：在世界随机放置一个种类x的资源点
 * 
 * @author golden
 *
 */
@TalentSkill(skillID = 10204)
public class Skill10204 implements ITalentSkill {

	@Override
	public Result<?> onCastSkill(Player player, SkillCfg skillCfg, List<Integer> params) {
		if (params == null || params.size() < 3) {
			return Result.fail(Status.SysError.PARAMS_INVALID_VALUE);
		}
		
		return WorldResourceService.getInstance().genResourcePoint(player, params.get(0), params.get(1), params.get(2));
	}
	
}
