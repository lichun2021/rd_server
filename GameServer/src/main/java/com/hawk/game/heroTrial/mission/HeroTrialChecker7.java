package com.hawk.game.heroTrial.mission;

import java.util.List;

import com.hawk.game.global.GlobalData;
import com.hawk.game.heroTrial.HeroTrialType;
import com.hawk.game.player.Player;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.player.hero.TalentSlot;

@HeroTrialChecker(type = HeroTrialType.TYPE_7)
public class HeroTrialChecker7 implements IHeroTrialChecker {

	@Override
	public boolean touchMission(String playerId, List<Integer> heroIds, List<Integer> condition) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player == null) {
			return false;
		}
		
		
		int count = 0;
		for (PlayerHero hero : player.getHeroByCfgId(heroIds)) {
			
			for (TalentSlot talent : hero.getTalentSlots()) {
				if (talent == null) {
					continue;
				}
				count++;
			}
		}
		return count >= condition.get(1);
	}

}
