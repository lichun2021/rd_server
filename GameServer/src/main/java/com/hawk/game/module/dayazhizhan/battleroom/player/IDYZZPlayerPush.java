package com.hawk.game.module.dayazhizhan.battleroom.player;

import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerPush;
import com.hawk.game.protocol.Const.EffType;

public abstract class IDYZZPlayerPush extends PlayerPush{

	public IDYZZPlayerPush(Player player) {
		super(player);
	}

	/**
	 * 游戏结束 数据刷新
	 */
	public  void pushGameOver(){
		
	}
	
	@Override
	public void syncPlayerEffect(EffType... types) {
		// IDYZZGamer 这个方法不会抛出PlayerEffectChangeMsg事件
	}

	/**
	 * 游戏开始推送 
	 */
	public void pushJoinGame() {
		// TODO Auto-generated method stub
		
	}

	public void pushCampNoticeTime(){

	}
}
