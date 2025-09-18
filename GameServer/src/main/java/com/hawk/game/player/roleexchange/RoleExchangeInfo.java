package com.hawk.game.player.roleexchange;

/**
 * 角色交易转换信息
 * 
 * @author lating
 *
 */
public class RoleExchangeInfo {
	
	private String playerId;
	
	private String sellerOpenid;
	
	private String buyerOpenid;
	/**
	 * 角色出生服ID
	 */
	private String bornSvrId;
	/**
	 * 角色转移时所在服ID
	 */
	private String currSvrId;
	
	private String channel;
	
	private String platform;
	
	private long exchangeTime;
	
	public RoleExchangeInfo() {
	}
	
	public RoleExchangeInfo(String playerId, String sellerOpenid, String buyerOpenid, String bornSvrId, 
			String currSvrId, String channel, String platform, long exchangeTime) {
		this.playerId = playerId;
		this.sellerOpenid = sellerOpenid;
		this.buyerOpenid = buyerOpenid;
		this.bornSvrId = bornSvrId;
		this.currSvrId = currSvrId;
		this.channel = channel;
		this.platform = platform;
		this.exchangeTime = exchangeTime;
	}

	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public String getSellerOpenid() {
		return sellerOpenid;
	}

	public void setSellerOpenid(String sellerOpenid) {
		this.sellerOpenid = sellerOpenid;
	}

	public String getBuyerOpenid() {
		return buyerOpenid;
	}

	public void setBuyerOpenid(String buyerOpenid) {
		this.buyerOpenid = buyerOpenid;
	}

	public String getBornSvrId() {
		return bornSvrId;
	}

	public void setBornSvrId(String bornSvrId) {
		this.bornSvrId = bornSvrId;
	}

	public String getCurrSvrId() {
		return currSvrId;
	}

	public void setCurrSvrId(String currSvrId) {
		this.currSvrId = currSvrId;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public String getPlatform() {
		return platform;
	}

	public void setPlatform(String platform) {
		this.platform = platform;
	}

	public long getExchangeTime() {
		return exchangeTime;
	}

	public void setExchangeTime(long exchangeTime) {
		this.exchangeTime = exchangeTime;
	}
	
}
