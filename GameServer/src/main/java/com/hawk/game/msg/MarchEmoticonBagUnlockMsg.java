package com.hawk.game.msg;

import org.hawk.msg.HawkMsg;

/**
 * 行军表情包解锁消息
 * 
 * @author lating
 *
 */
public class MarchEmoticonBagUnlockMsg extends HawkMsg {
	/**
	 * 购买的gift
	 */
	private String payGiftId;
	
	public MarchEmoticonBagUnlockMsg(String giftId) {
		this.payGiftId = giftId;
	}
	
	public String getPayGiftId() {
		return payGiftId;
	}
	
	public void setPayGiftId(String payGiftId) {
		this.payGiftId = payGiftId;
	}
}
