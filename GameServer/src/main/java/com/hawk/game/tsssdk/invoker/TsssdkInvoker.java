package com.hawk.game.tsssdk.invoker;

import com.hawk.game.player.Player;

public interface TsssdkInvoker {

	int invoke(Player player, int result, String msgContent, int protocol, String callbackData);
	
	default void putObj2Callback(String key, Object... objects) {
		// 默认实现
	}
}
