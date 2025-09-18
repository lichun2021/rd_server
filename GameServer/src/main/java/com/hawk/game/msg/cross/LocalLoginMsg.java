package com.hawk.game.msg.cross;

import org.hawk.msg.HawkMsg;

import com.hawk.gamelib.GameConst.MsgId;

/**
 * 在走完远程登录之后,本地一些需要处理的信息走这里,
 * 切记本机不可修改玩家身上携带的数据.
 * @author jm
 *
 */
public class LocalLoginMsg extends HawkMsg {
	public LocalLoginMsg() {
		super(MsgId.CROSS_LOCAL_LOGIN);
	}
}
