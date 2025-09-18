package com.hawk.game.player.skill.tech;

import com.hawk.game.player.Player;

/**
 * 铁哥们-立即补满当前巨龙的友好度
 * @author admin
 *
 */
@TechSkill(skillID = 20301)
public class Skill20301 implements ITechSkill {

	@Override
	public boolean onCastSkill(Player player) {
		if(!canCastSkill(player)){
			return false;
		}
		// TODO 具体实现,待有巨龙系统后添加
		enterCD(player);
		return true;
	}

	

}
