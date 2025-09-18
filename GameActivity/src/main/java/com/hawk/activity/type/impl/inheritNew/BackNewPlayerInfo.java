package com.hawk.activity.type.impl.inheritNew;

public class BackNewPlayerInfo {
	private String openId;
	
	// 当前新注册服务器id
	private String curNewServer;
	
	// 当前注册新服的角色id
	private String curNewPlayer;
	
	// 新服注册时间
	private long registTime;
	
	// 回归的老服
	private String curOldServer;
	
	// 回归的老服的角色id
	private String curOldPlayerId;
	
	// 老服帐号回归时间
	private long backTime;

	// 当前继承的老服信息
	private String currInheritServer;
	
	// 当前继承老服的角色id
	private String currInheritPlayerId;
	
	// 军魂承接激活时间
	private long inheritTime;
	
	// 活动期数
	private int termId;
	
	// 回流次数
	private int returnCnt;
	
	// 本次传承有效激时间(老服回归为回归时间+有效期,新服有效期内注册后变更为新服注册时间+传承活动市场)
	private long currValidTime;

	public String getOpenId() {
		return openId;
	}

	public void setOpenId(String openId) {
		this.openId = openId;
	}

	public String getCurNewPlayer() {
		return curNewPlayer;
	}

	public void setCurNewPlayer(String curNewPlayer) {
		this.curNewPlayer = curNewPlayer;
	}

	public String getCurOldPlayerId() {
		return curOldPlayerId;
	}

	public void setCurOldPlayerId(String curOldPlayerId) {
		this.curOldPlayerId = curOldPlayerId;
	}

	public String getCurrInheritPlayerId() {
		return currInheritPlayerId;
	}

	public void setCurrInheritPlayerId(String currInheritPlayerId) {
		this.currInheritPlayerId = currInheritPlayerId;
	}

	public String getCurNewServer() {
		return curNewServer;
	}

	public void setCurNewServer(String curNewServer) {
		this.curNewServer = curNewServer;
	}

	public long getRegistTime() {
		return registTime;
	}

	public void setRegistTime(long registTime) {
		this.registTime = registTime;
	}

	public String getCurOldServer() {
		return curOldServer;
	}

	public void setCurOldServer(String curOldServer) {
		this.curOldServer = curOldServer;
	}

	public long getBackTime() {
		return backTime;
	}

	public void setBackTime(long backTime) {
		this.backTime = backTime;
	}

	public String getCurrInheritServer() {
		return currInheritServer;
	}

	public void setCurrInheritServer(String currInheritServer) {
		this.currInheritServer = currInheritServer;
	}

	public int getTermId() {
		return termId;
	}

	public long getInheritTime() {
		return inheritTime;
	}

	public void setInheritTime(long inheritTime) {
		this.inheritTime = inheritTime;
	}

	public void setTermId(int termId) {
		this.termId = termId;
	}

	public int getReturnCnt() {
		return returnCnt;
	}

	public void setReturnCnt(int returnCnt) {
		this.returnCnt = returnCnt;
	}

	public long getCurrValidTime() {
		return currValidTime;
	}

	public void setCurrValidTime(long currValidTime) {
		this.currValidTime = currValidTime;
	}
	
}
