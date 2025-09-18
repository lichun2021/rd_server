package com.hawk.game.player.itemadd;

import com.hawk.game.player.Player;
import com.hawk.log.Action;
/**
 * 道具添加额外处理逻辑
 * 
 * @author lating
 *
 */
public interface ItemAddLogic {

	public abstract void addLogic(Player player, int itemId, int addCount, Action action);
	
}
