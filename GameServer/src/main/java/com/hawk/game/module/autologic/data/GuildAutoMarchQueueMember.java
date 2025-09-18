package com.hawk.game.module.autologic.data;

public class GuildAutoMarchQueueMember {

	//玩家ID
	private String playerId;
	//排序参数
	private long orderParam;
	//队列位置
	private int queueOrder;
	

	public GuildAutoMarchQueueMember(String playerId,long orderParam,int initOrder) {
		this.playerId = playerId;
		this.orderParam = orderParam;
		this.queueOrder = initOrder;
	}
	
	public String getPlayerId() {
		return playerId;
	}
	
	
	public void setOrderParam(long orderParam) {
		this.orderParam = orderParam;
	}
	
	
	public long getOrderParam() {
		return orderParam;
	}
	
	
	public void setQueueOrder(int queueOrder) {
		this.queueOrder = queueOrder;
	}
	
	public int getQueueOrder() {
		return queueOrder;
	}
	
	
}
