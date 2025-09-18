package com.hawk.game.msg.cross;

import org.hawk.msg.HawkMsg;

import com.hawk.game.crossproxy.ProxyHeader;
import com.hawk.gamelib.GameConst;

/**
 * 目标服该消息
 * @author jm
 *
 */
public class MoveBackCrossPlayerMsg extends HawkMsg {
	/**
	 * 是否是
	 */
	private ProxyHeader proxyHeader;
	/**
	 * 是否是强制签回
	 */
	private boolean force;
	
	public MoveBackCrossPlayerMsg(ProxyHeader proxyHeader, boolean force) {
		super(GameConst.MsgId.CROSS_FORCE_MOVE_BACK);
		this.proxyHeader = proxyHeader;
		this.force = force;
	}
	public ProxyHeader getProxyHeader() {
		return proxyHeader;
	}
	public void setProxyHeader(ProxyHeader proxyHeader) {
		this.proxyHeader = proxyHeader;
	}
	public boolean isForce() {
		return force;
	}
}
