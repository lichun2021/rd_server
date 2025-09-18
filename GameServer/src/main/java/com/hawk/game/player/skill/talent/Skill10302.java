package com.hawk.game.player.skill.talent;

import java.util.List;

import org.hawk.app.HawkApp;
import org.hawk.result.Result;

import com.hawk.game.config.SkillCfg;
import com.hawk.game.msg.TimeLimitStoreTriggerMsg;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.service.ArmyService;
import com.hawk.game.util.GsConst.TimeLimitStoreTriggerType;

/**
 * 能工巧匠：使用后立即获得xx个陷阱，在玩家可以建造的最高等级的陷阱中随机
 * 
 * @author golden
 *
 */
@TalentSkill(skillID = 10302)
public class Skill10302 implements ITalentSkill {

	@Override
	public Result<?> onCastSkill(Player player, SkillCfg skillCfg, List<Integer> params) {
		// 没有战争堡垒，不能使用技能
		if (player.getData().getBuildCount(BuildingType.WAR_FORTS) <= 0) {
			return Result.fail(Status.Error.SKILL_ERROR_HAS_NO_TRAP_BUILD_VALUE);
		}
		
		int trapCapacity = player.getData().getTrapCapacity();
		int countTrapCount = player.getData().getTrapCount();
		if (countTrapCount >= trapCapacity) {
			return Result.fail(Status.Error.SKILL_ERROR_TRAP_IS_FULL_VALUE);
		}
		
		int canBuildId = ArmyService.getInstance().getUnlockedMaxLevelTrap(player);
		int trapCount = Integer.parseInt(skillCfg.getParam1());
		ArmyService.getInstance().awardTrap(player, canBuildId, trapCount);
		HawkApp.getInstance().postMsg(player, new TimeLimitStoreTriggerMsg(TimeLimitStoreTriggerType.SOLDIER_TRAIN, trapCount));
		return Result.success();
	}
}
