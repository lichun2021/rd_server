package com.hawk.game.player.tick;

import com.hawk.game.player.Player;
/**
 * player tick逻辑接口
 * 
 * @author lating
 *
 */
public interface PlayerTickLogic {

	public abstract void onTick(Player player);
	
}
