package com.hawk.game.data;

/****
 * 红包回调
 * @author yang.rao
 *
 */
public class RedEnvelopeMsgData {
	
	/** 红包的id **/
	private String id;
	
	private int noticeCfgId;
	
	/** 红包配置id **/
	private int redId;
	
	private int chatType;

	public static RedEnvelopeMsgData valueOf(String id, int noticeCfgId, int redId, int chatType){
		RedEnvelopeMsgData data = new RedEnvelopeMsgData();
		data.setId(id);
		data.setNoticeCfgId(noticeCfgId);
		data.setRedId(redId);
		data.setChatType(chatType);
		return data;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getNoticeCfgId() {
		return noticeCfgId;
	}

	public void setNoticeCfgId(int noticeCfgId) {
		this.noticeCfgId = noticeCfgId;
	}

	public int getRedId() {
		return redId;
	}

	public void setRedId(int redId) {
		this.redId = redId;
	}

	public int getChatType() {
		return chatType;
	}

	public void setChatType(int chatType) {
		this.chatType = chatType;
	}
}
