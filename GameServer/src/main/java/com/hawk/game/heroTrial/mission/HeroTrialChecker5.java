package com.hawk.game.heroTrial.mission;

import java.util.List;

import com.hawk.game.global.GlobalData;
import com.hawk.game.heroTrial.HeroTrialType;
import com.hawk.game.player.Player;
import com.hawk.game.player.hero.PlayerHero;

@HeroTrialChecker(type = HeroTrialType.TYPE_5)
public class HeroTrialChecker5 implements IHeroTrialChecker {

	@Override
	public boolean touchMission(String playerId, List<Integer> heroIds, List<Integer> condition) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player == null) {
			return false;
		}
		
		
		int count = 0;
		for (PlayerHero hero : player.getHeroByCfgId(heroIds)) {
			if (hero.getCfgId() != condition.get(3)) {
				continue;
			}
			if (hero.getLevel() < condition.get(1)) {
				continue;
			}
			if (hero.getStar() < condition.get(2)) {
				continue;
			}
			count++;
		}
		return count >= 1;
	}

}