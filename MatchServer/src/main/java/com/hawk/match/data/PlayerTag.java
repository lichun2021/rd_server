package com.hawk.match.data;

import org.hawk.os.HawkException;

/**
 * 玩家标签
 * 
 * @author hawk
 *
 */
public class PlayerTag {
	/**
	 * 服务器id
	 */
	private String serverId;
	/**
	 * 玩家id
	 */
	private String playerId;

	/**
	 * 私有构造
	 */
	private PlayerTag() {

	}

	/**
	 * 获取服务器id
	 * 
	 * @return
	 */
	public String getServerId() {
		return serverId;
	}

	/**
	 * 获取玩家id
	 * 
	 * @return
	 */
	public String getPlayerId() {
		return playerId;
	}

	/**
	 * 转换字符串
	 * 
	 */
	@Override
	public String toString() {
		return String.format("%s#%s", serverId, playerId);
	}

	/**
	 * 创建标签
	 * 
	 * @param serverId
	 * @param playerId
	 * @return
	 */
	public static PlayerTag valueOf(String serverId, String playerId) {
		PlayerTag tag = new PlayerTag();
		tag.serverId = serverId;
		tag.playerId = playerId;
		return tag;
	}

	/**
	 * 创建标签
	 * 
	 * @param tagVal
	 * @param playerId
	 * @return
	 */
	public static PlayerTag valueOf(String tagVal) {
		try {
			String[] infoArr = tagVal.split("#");
			PlayerTag tag = new PlayerTag();
			tag.serverId = infoArr[0];
			tag.playerId = infoArr[1];
			return tag;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return null;
	}
}
