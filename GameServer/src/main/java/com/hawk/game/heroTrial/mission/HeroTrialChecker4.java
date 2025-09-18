package com.hawk.game.heroTrial.mission;

import java.util.List;

import com.hawk.game.global.GlobalData;
import com.hawk.game.heroTrial.HeroTrialType;
import com.hawk.game.player.Player;
import com.hawk.game.player.hero.PlayerHero;

@HeroTrialChecker(type = HeroTrialType.TYPE_4)
public class HeroTrialChecker4 implements IHeroTrialChecker {

	@Override
	public boolean touchMission(String playerId, List<Integer> heroIds, List<Integer> condition) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player == null) {
			return false;
		}
		
		
		int count = 0;
		for (PlayerHero hero : player.getHeroByCfgId(heroIds)) {
			if (hero.getLevel() < condition.get(1)) {
				continue;
			}
			if (hero.getConfig().getQualityColor() < condition.get(2)) {
				continue;
			}
			count++;
		}
		return count >= condition.get(3);
	}

}