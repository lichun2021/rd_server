package com.hawk.game.player.skill.tech;

import java.util.List;

import com.hawk.game.player.Player;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.march.IWorldMarch;

/**
 * 集结突击-玩家发起的集结立即出发
 * @author admin
 *
 */
@TechSkill(skillID = 20901)
public class Skill20901 implements ITechSkill {

	@Override
	public boolean onCastSkill(Player player) {
		if(!canCastSkill(player)){
			return false;
		}
		List<IWorldMarch> playerMassMarchs = WorldMarchService.getInstance().getPlayerMassMarch(player.getId());
		playerMassMarchs.forEach(march -> WorldMarchService.getInstance().doMassMarchStart(march.getMarchEntity()));
		enterCD(player);
		return false;
	}

	@Override
	public boolean canCastSkill(Player player) {
		if(!ITechSkill.super.canCastSkill(player)){
			return false;
		}
		if(!WorldMarchService.getInstance().hasMassMarch(player.getId())){
			player.sendError(HP.code.CAST_TECH_SKILL_REQ_C_VALUE, Status.Error.SKILL_CONDITION_NOT_MATCH, 0);
			return false;
		}
		return true;
	}
	
	

}
