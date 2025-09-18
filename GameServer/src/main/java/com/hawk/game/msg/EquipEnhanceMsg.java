package com.hawk.game.msg;

import org.hawk.msg.HawkMsg;

import com.hawk.gamelib.GameConst.MsgId;

/**
 * 装备强化消息
 * 
 * @author lating
 *
 */
public class EquipEnhanceMsg extends HawkMsg {

	/**
	 * 强化次数
	 */
	private int enhanceTimes;

	/**
	 * 下次强化需要的物品数量
	 */
	private int nextNeedItemNum;
	
	public int getEnhanceTimes() {
		return enhanceTimes;
	}

	public void setEnhanceTimes(int enhanceTimes) {
		this.enhanceTimes = enhanceTimes;
	}

	public int getNextNeedItemNum() {
		return nextNeedItemNum;
	}

	public void setNextNeedItemNum(int nextNeedItemNum) {
		this.nextNeedItemNum = nextNeedItemNum;
	}

	public EquipEnhanceMsg() {
		super(MsgId.EQUIP_ENHANCE);
	}

	/**
	 * 构造消息对象
	 * 
	 * @return
	 */
	public static EquipEnhanceMsg valueOf(int enhanceTimes, int nextNeedItemNum) {
		EquipEnhanceMsg msg = new EquipEnhanceMsg();
		msg.setEnhanceTimes(enhanceTimes);
		msg.setNextNeedItemNum(nextNeedItemNum);
		return msg;
	}

}
