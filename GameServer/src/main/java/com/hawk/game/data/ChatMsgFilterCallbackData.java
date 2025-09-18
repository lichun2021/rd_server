package com.hawk.game.data;

/**
 * 世界、联盟聊天消息过滤回调数据
 * 
 * @author lating
 *
 */
public class ChatMsgFilterCallbackData {
	
	String voiceId;

	int voiceLength;
	
	int chatType;
	
	long startTime;
	
	public ChatMsgFilterCallbackData() {
		
	}
	
	public ChatMsgFilterCallbackData(String voiceId, int voiceLength, int chatType, long startTime) {
		this.voiceId = voiceId;
		this.voiceLength = voiceLength;
		this.chatType = chatType;
		this.startTime = startTime;
	}

	public String getVoiceId() {
		return voiceId;
	}

	public void setVoiceId(String voiceId) {
		this.voiceId = voiceId;
	}

	public int getVoiceLength() {
		return voiceLength;
	}

	public void setVoiceLength(int voiceLength) {
		this.voiceLength = voiceLength;
	}

	public int getChatType() {
		return chatType;
	}

	public void setChatType(int chatType) {
		this.chatType = chatType;
	}
	
	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}
}
