package com.hawk.game.heroTrial.mission;

import java.util.List;

import com.hawk.game.global.GlobalData;
import com.hawk.game.heroTrial.HeroTrialType;
import com.hawk.game.player.Player;
import com.hawk.game.player.hero.PlayerHero;

@HeroTrialChecker(type = HeroTrialType.TYPE_3)
public class HeroTrialChecker3 implements IHeroTrialChecker {

	@Override
	public boolean touchMission(String playerId, List<Integer> heroIds, List<Integer> condition) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player == null) {
			return false;
		}
		
		
		int conditionValue = condition.get(1);
		
		int attr = 0;
		for (PlayerHero hero : player.getHeroByCfgId(heroIds)) {
			attr += hero.attrs().get(103).getNumber();
		}
		return attr >= conditionValue;
	}

}