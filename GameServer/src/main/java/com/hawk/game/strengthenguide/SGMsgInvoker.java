package com.hawk.game.strengthenguide;

import com.hawk.game.config.StrengthenGuideConstProperty;
import com.hawk.game.global.GlobalData;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.StrengthenGuide.StrengthenGuideType;
import com.hawk.game.strengthenguide.op.SGPlayerEntityOP;

public abstract class SGMsgInvoker {
	
	protected StrengthenGuideType sgType;
	
	protected Player player;
	
	protected SGMsgInvoker(Player player, StrengthenGuideType sgType){
		this.player = player;
		this.sgType = sgType;
	}
	public void invoke(){
		if(invokeEnable()){
			SGPlayerEntityOP.updateWithType(sgType,player);
		}
	}
	
	public boolean invokeEnable(){		
		return GlobalData.getInstance().isOnline(player.getId()) &&  player.getCityLevel() >= StrengthenGuideConstProperty.getInstance().getFunctionUnlockLevel();
	}
	
}
