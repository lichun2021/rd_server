package com.hawk.game.crossproxy.model;

import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.net.session.HawkSession;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;

import com.hawk.game.crossproxy.CrossProxy;

public class CsSession extends HawkSession {
	/**
	 * 通过这个变量来模拟session的激活.
	 */
	private boolean isActive;
	/**
	 * 玩家ID
	 */
	private String playerId;
	/**
	 * 从哪个服来的
	 */
	private String fromServerId;
	
	/**
	 * 创建时间。
	 */
	private int createTime;
	/**
	 * 
	 */
	private int accessTime;
	/**
	 * 默认构造
	 */
	public CsSession() {
		super(null);
		isActive = true;
	}

	/**
	 * 构造
	 * 
	 * @param playerId
	 * @param fromServerId
	 */
	public CsSession(String playerId, String fromServerId) {
		this();
		this.playerId = playerId;
		this.fromServerId = fromServerId;
		this.createTime = HawkTime.getSeconds();
		this.accessTime = this.createTime;
	}

	/**
	 * 是否为激活状态
	 */
	@Override
	public boolean isActive() {
		return isActive;
	}

	/**
	 * 设置激活状态
	 * 
	 * @param active
	 */
	public void setActive(boolean active) {
		this.isActive = active;
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
	 * 设置玩家id
	 * 
	 * @param playerId
	 */
	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	/**
	 * 获取来源的服务器id
	 * 
	 * @return
	 */
	public String getFromServerId() {
		return fromServerId;
	}

	/**
	 * 设置来源服务器id
	 * 
	 * @param fromServerId
	 */
	public void setFromServerId(String fromServerId) {
		this.fromServerId = fromServerId;
	}

	/**
	 * 从这里走的协议会回到原服再次进行投递
	 * 
	 * @param protocol
	 * @return
	 */
	public boolean sendNotify(HawkProtocol protocol) {
		return CrossProxy.getInstance().sendNotify(protocol, fromServerId, playerId);
	}

	/**
	 * 从这个协议走的默认直接转发客户端
	 */
	@Override
	public boolean sendProtocol(HawkProtocol protocol) {
		try {
			if (protocol.getType() == 29001 || protocol.getType() == 29002 || protocol.getType() == 2110 || protocol.getType() == 2111
					|| protocol.getType() == 29003 || protocol.getType() == 29004) {
				HawkLog.logPrintln("csSessionSendProtocol playerId:{} protocolType:{} size:{}", playerId, protocol.getType(), protocol.getSize());
			}			
		} catch (Exception e) {
			HawkException.catchException(e);
		}		
		return CrossProxy.getInstance().sendProtocol(protocol, fromServerId, playerId);
	}

	public int getCreateTime() {
		return createTime;
	}

	public void setCreateTime(int createTime) {
		this.createTime = createTime;
	}

	public int getAccessTime() {
		return accessTime;
	}

	public void setAccessTime(int accessTime) {
		this.accessTime = accessTime;
	}
	
	public void updateAccessTime() {
		this.accessTime = HawkTime.getSeconds();
	}
}
