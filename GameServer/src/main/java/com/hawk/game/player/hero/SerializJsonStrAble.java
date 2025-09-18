package com.hawk.game.player.hero;

public interface SerializJsonStrAble {
	/**
	 * 序列化保存
	 */
	String serializ();

	/**
	 * 反序列化
	 * 
	 * @param serialiedStr
	 */
	void mergeFrom(String serialiedStr);
}
