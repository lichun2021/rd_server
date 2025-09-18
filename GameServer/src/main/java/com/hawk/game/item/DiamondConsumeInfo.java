package com.hawk.game.item;

/**
 * 钻石消耗信息
 *
 * @author lating
 *
 */
public class DiamondConsumeInfo {
	/**
	 * 消耗的钻石数量
	 */
	protected int count;
	/**
	 * 消耗钻石产生的订单号
	 */
	protected String billno;

	public DiamondConsumeInfo() {
		
	}
	
	public DiamondConsumeInfo(int count, String billno) {
		this.billno = billno;
		this.count = count;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public String getBillno() {
		return billno;
	}

	public void setBillno(String billno) {
		this.billno = billno;
	}

}
