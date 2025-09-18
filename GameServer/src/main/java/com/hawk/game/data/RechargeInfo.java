package com.hawk.game.data;

/**
 * 玩家充值信息
 * 
 * @author lating
 *
 */
public class RechargeInfo {
	/**
	 * openid
	 */
	private String openid;
	/**
	 * 平台
	 */
	private int platId;
	/**
	 * 所在区服
	 */
	private String server;
	/**
	 * 充值时间:秒
	 */
	private int time;
	
	/**
	 * 充值金额（钻石数）
	 */
	private int count;
	/**
	 * 充值类型：购买钻石礼包或道具直购
	 */
	private int type;
	/**
	 * 商品ID
	 */
	private String goodsId = "";
	/**
	 * 角色id
	 */
	private String playerId = "";
	
	public RechargeInfo() {
		
	}
	
	public RechargeInfo(String openid, String playerId, int platId, String server, int time, int count, int type) {
		this.openid = openid;
		this.playerId = playerId;
		this.platId = platId;
		this.server = server;
		this.time = time;
		this.count = count;
		this.type = type;
	}

	public String getOpenid() {
		return openid;
	}

	public void setOpenid(String openid) {
		this.openid = openid;
	}

	public int getPlatId() {
		return platId;
	}

	public void setPlatId(int platId) {
		this.platId = platId;
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public int getTime() {
		return time;
	}

	public void setTime(int time) {
		this.time = time;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getGoodsId() {
		return goodsId;
	}

	public void setGoodsId(String goodsId) {
		this.goodsId = goodsId;
	}

	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}
	
}
