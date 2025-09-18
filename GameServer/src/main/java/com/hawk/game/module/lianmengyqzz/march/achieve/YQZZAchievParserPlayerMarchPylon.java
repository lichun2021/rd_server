package com.hawk.game.module.lianmengyqzz.march.achieve;

import java.util.List;
import java.util.Map;

import com.hawk.game.module.lianmengyqzz.battleroom.player.YQZZPlayer;
import com.hawk.game.module.lianmengyqzz.battleroom.player.according.YQZZPylonHonor;
import com.hawk.game.module.lianmengyqzz.march.entitiy.YQZZAchievecomponent;
import com.hawk.game.module.lianmengyqzz.march.service.YQZZConst.YQZZAchieveType;
import com.hawk.game.player.Player;

public class YQZZAchievParserPlayerMarchPylon extends IYQZZAchievParser{

	@Override
	public YQZZAchieveType getYQZZAchieveType() {
		return YQZZAchieveType.PLAYER_MARCH_PYLON;
	}

	@Override
	public boolean updateAchieveOnUpdate(YQZZAchievecomponent achieve,List<Long> conditionValueList,long targetValue) {
		Player player = achieve.getParent().getParent().getParent();
		if(player == null){
			return false;
		}
		if(player instanceof YQZZPlayer){
			YQZZPlayer yqzzPlayer = (YQZZPlayer) player;
			Map<Integer, YQZZPylonHonor> kills = yqzzPlayer.getPylonHonorMap();
			long curVal = 0;
			if(kills!= null){
				for(YQZZPylonHonor monster : kills.values()){
					curVal += monster.getPylonMarchCount();
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
