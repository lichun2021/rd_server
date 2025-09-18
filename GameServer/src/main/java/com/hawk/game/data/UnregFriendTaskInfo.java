package com.hawk.game.data;

import java.util.ArrayList;
import java.util.List;

import com.hawk.game.protocol.Friend.UnRegFriendInfoPB;

/**
 * 未注册好友相关信息
 * 
 * @author lating
 *
 */
public class UnregFriendTaskInfo {
	/**
	 * 未注册好友数据
	 */
	private List<UnRegFriendInfoPB> unregFriendList = new ArrayList<UnRegFriendInfoPB>();
	/**
	 * 微信密友模型ID
	 */
	private int modelId;
	/**
	 * 已拉取过的未注册好友列表末尾下标
	 */
	private int index;
	
	public List<UnRegFriendInfoPB> getUnregFriendList() {
		return unregFriendList;
	}
	
	public void setUnregFriendList(List<UnRegFriendInfoPB> unregFriendList) {
		this.unregFriendList = unregFriendList;
	}
	
	public int getModelId() {
		return modelId;
	}
	
	public void setModelId(int modelId) {
		this.modelId = modelId;
	}
	
	public int getIndex() {
		return index;
	}
	
	public void setIndex(int index) {
		this.index = index;
	}
	
}
