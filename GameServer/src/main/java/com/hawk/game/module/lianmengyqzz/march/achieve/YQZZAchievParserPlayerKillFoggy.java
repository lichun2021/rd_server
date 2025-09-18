package com.hawk.game.module.lianmengyqzz.march.achieve;

import java.util.List;
import java.util.Map;

import com.hawk.game.module.lianmengyqzz.battleroom.player.YQZZPlayer;
import com.hawk.game.module.lianmengyqzz.battleroom.player.according.YQZZFoggyHonor;
import com.hawk.game.module.lianmengyqzz.march.entitiy.YQZZAchievecomponent;
import com.hawk.game.module.lianmengyqzz.march.service.YQZZConst.YQZZAchieveType;
import com.hawk.game.player.Player;

public class YQZZAchievParserPlayerKillFoggy extends IYQZZAchievParser{

	@Override
	public YQZZAchieveType getYQZZAchieveType() {
		return YQZZAchieveType.PLAYER_KILL_FOGGY;
	}

	@Override
	public boolean updateAchieveOnUpdate(YQZZAchievecomponent achieve,List<Long> conditionValueList,long targetValue) {
		Player player = achieve.getParent().getParent().getParent();
		if(player == null){
			return false;
		}
		if(player instanceof YQZZPlayer){
			YQZZPlayer yqzzPlayer = (YQZZPlayer) player;
			Map<Integer, YQZZFoggyHonor> kills = yqzzPlayer.getFoggyHonorMap();
			long curVal = 0;
			if(kills!= null){
				for(YQZZFoggyHonor monster : kills.values()){
					curVal += monster.getKillCount();
				}
			}
			long achieveVal = achieve.getValue();
			if(curVal > achieveVal){
				achieve.setValue(curVal);
				return true;
			}
		}
		return false;
	}


}
