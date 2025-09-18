package com.hawk.game.module.lianmengyqzz.march.cfg;

import java.util.List;

import com.hawk.game.item.ItemInfo;
import com.hawk.game.protocol.YQZZWar.PBYQZZWarAchieveType;

public interface YQZZAchieveCfg {

	
	public int getAchieveId();
	
	public int getConditionType1();
	
	public int getConditionType2();
	
	public List<Long> getConditionValueList1();
	
	public long getTargetValue1();
	
	public List<Long> getConditionValueList2();
	
	public long getTargetValue2();
	
	public List<ItemInfo> getRewardList();
	
	
	default int getAchieveType(){
		if(this instanceof YQZZPlayerAchieveCfg){
			return PBYQZZWarAchieveType.YQZZ_ACHIEVE_TYPE_PLAYER_VALUE;
		}else if(this instanceof YQZZAllianceAchieveCfg){
			return PBYQZZWarAchieveType.YQZZ_ACHIEVE_TYPE_GUILD_VALUE;
		}else if(this instanceof YQZZCountryAchieveCfg){
			return PBYQZZWarAchieveType.YQZZ_ACHIEVE_TYPE_COUNTRY_VALUE;
		}
		return 0;
	}
	
}
