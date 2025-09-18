package com.hawk.game.heroTrial.mission;

import java.util.List;

import com.hawk.game.global.GlobalData;
import com.hawk.game.heroTrial.HeroTrialType;
import com.hawk.game.player.Player;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.player.hero.SkillSlot;
import com.hawk.game.player.hero.skill.IHeroSkill;

@HeroTrialChecker(type = HeroTrialType.TYPE_6)
public class HeroTrialChecker6 implements IHeroTrialChecker {

	@Override
	public boolean touchMission(String playerId, List<Integer> heroIds, List<Integer> condition) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player == null) {
			return false;
		}
		
		
		int count = 0;
		for (PlayerHero hero : player.getHeroByCfgId(heroIds)) {
			
			for (SkillSlot slot : hero.getSkillSlots()) {
				IHeroSkill skill = slot.getSkill();
				if (skill == null) {
					continue;
				}
				if (skill.skillID() != condition.get(2)) {
					continue;
				}
				if (skill.getLevel() < condition.get(1)) {
					continue;
				}
				count++;
			}
		}
		return count >= condition.get(3);
	}

}
