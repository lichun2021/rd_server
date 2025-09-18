package com.hawk.game.msg;

import org.hawk.msg.HawkMsg;
import org.hawk.xid.HawkXID;

import com.hawk.gamelib.GameConst.MsgId;

/**
 * 消息 - 对象移除
 * 
 * @author hawk
 *
 */
public class RemoveObjectMsg extends HawkMsg {
	/**
	 * 需要移除的对象id
	 */
	private HawkXID removeXid;
	
	public RemoveObjectMsg(HawkXID xid) {
		super(MsgId.REMOVE_OBJECT);
		removeXid = xid;
	}
	
	public HawkXID getRemoveXid() {
		return removeXid;
	}

	public void setRemoveXid(HawkXID removeXid) {
		this.removeXid = removeXid;
	}

	/**
	 * 构造消息对象
	 * 
	 * @param xid
	 * @return
	 */
	public static RemoveObjectMsg valueOf(HawkXID xid) {
		return new RemoveObjectMsg(xid); 
	}
}
