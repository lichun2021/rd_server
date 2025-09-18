package com.hawk.activity.type.impl.globalSign;

import com.hawk.game.protocol.Activity.GlobalSignInBulletChat;

public class BulletChat {

	/** 玩家ID*/
	private String playerId;
	/** 玩家名称*/
	private String playerName;
	/** 弹幕ID*/
	private int chatId;
	/** 发送时间*/
	private long chatTime;
	
	
	
	public static BulletChat valueOf(String playerId,String playerName,int chatId,long chatTime){
		BulletChat chat = new BulletChat();
		chat.playerId = playerId;
		chat.playerName = playerName;
		chat.chatId = chatId;
		chat.chatTime = chatTime;
		return chat;
	}
	
	
	public GlobalSignInBulletChat.Builder createBuilder(){
		GlobalSignInBulletChat.Builder builder = GlobalSignInBulletChat.newBuilder();
		builder.setPlayerName(this.playerName);
		builder.setChatId(this.chatId);
		return builder;
	}
	
	public String getPlayerId() {
		return playerId;
	}
	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}
	public String getPlayerName() {
		return playerName;
	}
	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}
	public int getChatId() {
		return chatId;
	}
	public void setChatId(int chatId) {
		this.chatId = chatId;
	}
	public long getChatTime() {
		return chatTime;
	}
	public void setChatTime(long chatTime) {
		this.chatTime = chatTime;
	}
	
	
	
	
	
}
