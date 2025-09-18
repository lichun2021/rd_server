package com.hawk.game.msg;

import org.hawk.msg.HawkMsg;
import com.hawk.game.config.PayGiftCfg;
import com.hawk.gamelib.GameConst.MsgId;

/**
 * 消息 - 玩家充值成功发放道具消息
 * 
 * @author lating
 *
 */
public class PlayerRechargeGrantItemMsg extends HawkMsg {
	/**
	 * 礼包配置对象
	 */
	private PayGiftCfg payGiftCfg;
	/**
	 * 
	 */
	private String billno;
	
	private int price;
	
	public PayGiftCfg getPayGiftCfg() {
		return payGiftCfg;
	}

	public void setPayGiftCfg(PayGiftCfg payGiftCfg) {
		this.payGiftCfg = payGiftCfg;
	}

	public String getBillno() {
		return billno;
	}

	public void setBillno(String billno) {
		this.billno = billno;
	}
	
	public int getPrice() {
		return price;
	}

	public void setPrice(int price) {
		this.price = price;
	}
	
	public PlayerRechargeGrantItemMsg() {
		super(MsgId.RECHARGE_ITEM_GRANT);
	}

	/**
	 * 构造消息对象
	 * 
	 * @return
	 */
	public static PlayerRechargeGrantItemMsg valueOf(PayGiftCfg payGiftCfg, String billno, int price) {
		PlayerRechargeGrantItemMsg msg = new PlayerRechargeGrantItemMsg();
		msg.payGiftCfg = payGiftCfg;
		msg.billno = billno;
		msg.price = price;
		return msg;
	}

}
