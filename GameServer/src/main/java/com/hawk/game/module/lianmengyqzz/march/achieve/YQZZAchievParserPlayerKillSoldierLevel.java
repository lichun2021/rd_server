package com.hawk.game.module.lianmengyqzz.march.achieve;

import java.util.List;
import java.util.Map;

import com.hawk.game.module.lianmengyqzz.battleroom.player.YQZZPlayer;
import com.hawk.game.module.lianmengyqzz.battleroom.player.according.YQZZBattleStatics;
import com.hawk.game.module.lianmengyqzz.march.entitiy.YQZZAchievecomponent;
import com.hawk.game.module.lianmengyqzz.march.service.YQZZConst.YQZZAchieveType;
import com.hawk.game.player.Player;

public class YQZZAchievParserPlayerKillSoldierLevel extends IYQZZAchievParser{

	@Override
	public YQZZAchieveType getYQZZAchieveType() {
		return YQZZAchieveType.PLAYER_KILL_SOLDIER_LIMIT_LEVEL;
	}

	@Override
	public boolean updateAchieveOnUpdate(YQZZAchievecomponent achieve,List<Long> conditionValueList,long targetValue) {
		Player player = achieve.getParent().getParent().getParent();
		if(player == null){
			return false;
		}
		if(player instanceof YQZZPlayer){
			YQZZPlayer yqzzplayer = (YQZZPlayer) player;
			Map<Integer, YQZZBattleStatics> map = yqzzplayer.getBattleStatics();
			long total = 0;
			long level = conditionValueList.get(0);
			for (YQZZBattleStatics statics : map.values()) {
				int soldierLevel = statics.getLevel();
				if(soldierLevel < level){
					continue;
				}
				total += statics.getKillCount();
			}
			long achieveVal = achieve.getValue();
			if(total > achieveVal){
				achieve.setValue(total);
				return true;
			}
		}
		return false;
	}

	
}
