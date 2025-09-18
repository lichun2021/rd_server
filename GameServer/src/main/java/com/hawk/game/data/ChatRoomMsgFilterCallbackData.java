package com.hawk.game.data;

/**
 * 聊天室消息过滤回调数据
 * 
 * @author lating
 *
 */
public class ChatRoomMsgFilterCallbackData {
	/**
	 * 聊天室ID
	 */
	String roomId;
	/**
	 * 协议号
	 */
	int protocol;
	
	long startTime;
	
	public ChatRoomMsgFilterCallbackData() {
		
	}
	
	public ChatRoomMsgFilterCallbackData(String roomId, int protocol, long startTime) {
		this.roomId = roomId;
		this.protocol = protocol;
		this.startTime = startTime;
	}

	public String getRoomId() {
		return roomId;
	}

	public void setRoomId(String roomId) {
		this.roomId = roomId;
	}

	public int getProtocol() {
		return protocol;
	}

	public void setProtocol(int protocol) {
		this.protocol = protocol;
	}
	
	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}
	
}
