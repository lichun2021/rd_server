package com.hawk.game.data;

import java.util.ArrayList;
import java.util.List;

/**
 * 创建聊天室过滤回调数据
 * 
 * @author lating
 *
 */
public class CreateChatRoomFilterCallbackData {

	private int chatType;
	
	private List<String> toPlayerIds = new ArrayList<String>();
	
	private int protocol;
	
	public CreateChatRoomFilterCallbackData() {
		
	}
	
	public CreateChatRoomFilterCallbackData(int chatType, List<String> toPlayerIds, int protocol) {
		this.chatType = chatType;
		this.toPlayerIds.addAll(toPlayerIds);
		this.protocol = protocol;
	}

	public int getChatType() {
		return chatType;
	}

	public void setChatType(int chatType) {
		this.chatType = chatType;
	}

	public List<String> getToPlayerIds() {
		return toPlayerIds;
	}

	public void setToPlayerIds(List<String> toPlayerIds) {
		this.toPlayerIds = toPlayerIds;
	}

	public int getProtocol() {
		return protocol;
	}

	public void setProtocol(int protocol) {
		this.protocol = protocol;
	}
	
}
