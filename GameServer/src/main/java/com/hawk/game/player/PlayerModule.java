package com.hawk.game.player;

import org.hawk.app.HawkObjModule;
import org.hawk.net.protocol.HawkProtocol;

import com.google.protobuf.ProtocolMessageEnum;

/**
 * 玩家模块基类
 *
 * @author hawk
 */
public class PlayerModule extends HawkObjModule {
	/**
	 * 玩家对象
	 */
	protected Player player = null;

	/**
	 * 构造函数
	 *
	 * @param player
	 */
	public PlayerModule(Player player) {
		super(player);
		this.player = player;
	}

	/**
	 * 获取玩家数据
	 *
	 * @return
	 */
	protected PlayerData getPlayerData() {
		if (player != null) {
			return player.getData();
		}
		return null;
	}

	/**
	 * 玩家组装完成, 主要用来后期数据同步
	 */
	protected boolean onPlayerAssemble() {
		return true;
	}
	
	/**
	 * 玩家上线处理, 数据同步
	 *
	 * @return
	 */
	protected boolean onPlayerLogin() {
		return true;
	}

	/**
	 * 玩家下线处理
	 *
	 * @return
	 */
	protected boolean onPlayerLogout() {
		return true;
	}

	/**
	 * 发送协议
	 *
	 * @param protocol
	 * @return
	 */
	public boolean sendProtocol(HawkProtocol protocol) {
		if (player != null) {
			return player.sendProtocol(protocol);
		}
		return false;
	}

	/**
	 * 通知错误码
	 *
	 * @param errCode
	 */
	public void sendError(int hpCode, int errCode) {
		player.sendError(hpCode, errCode, 0);
	}

	/**
	 * 通知错误码
	 *
	 * @param errCode
	 */
	public void sendError(int hpCode, ProtocolMessageEnum errCode) {
		player.sendError(hpCode, errCode.getNumber(), 0);
	}
}
