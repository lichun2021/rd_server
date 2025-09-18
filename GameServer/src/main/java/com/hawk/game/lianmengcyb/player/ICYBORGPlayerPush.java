package com.hawk.game.lianmengcyb.player;

import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerPush;

public abstract class ICYBORGPlayerPush extends PlayerPush{

	public ICYBORGPlayerPush(Player player) {
		super(player);
	}

	/**
	 * 游戏结束 数据刷新
	 */
	public  void pushGameOver(){
		
	}

	/**
	 * 游戏开始推送 
	 */
	public void pushJoinGame() {
		// TODO Auto-generated method stub
		
	}
}
