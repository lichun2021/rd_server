package com.hawk.game.msg;

import org.hawk.msg.HawkMsg;

import com.hawk.gamelib.GameConst.MsgId;

/**
 * 指挥官经验值增加消息
 * 
 * @author lating
 *
 */
public class CommanderExpAddMsg extends HawkMsg {

	public CommanderExpAddMsg() {
		super(MsgId.COMMANDER_EXP_ADD);
	}

	/**
	 * 构造消息对象
	 * 
	 * @return
	 */
	public static CommanderExpAddMsg valueOf() {
		CommanderExpAddMsg msg = new CommanderExpAddMsg();
		return msg;
	}

}
