package com.hawk.game.player.item;

import com.hawk.game.config.ItemCfg;
import com.hawk.game.player.Player;

public abstract class AbstractItemUseEffect {
	/**
	 * 道具使用校验
	 * @return
	 */
	public abstract boolean useItemCheck(Player player, ItemCfg itemCfg, int itemId, int itemCount, int protoType, String targetId);
	/**
	 * 道具使用效果处理
	 * @return
	 */
	public abstract boolean useEffect(Player player, ItemCfg itemCfg, int itemCount, String targetId);
	/**
	 * 道具类型
	 * @return
	 */
	public abstract int itemType();
}
