package com.hawk.game.data;

/**
 * 服务器控制变量数据
 * 
 * @author lating
 *
 */
public class ServerSettingData {
	/**
	 * 同时在线人数上线
	 */
	private int maxOnlineCount;
	/**
	 * 注册人数上限
	 */
	private int maxRegisterCount;
	/**
	 * 登录排队上限
	 */
	private int maxWaitCount;
	
	/**
	 * 同时在线人数上线(配置数据)
	 */
	private int cfgMaxOnlineCount;
	/**
	 * 注册人数上限(配置数据)
	 */
	private int cfgMaxRegisterCount;
	/**
	 * 登录排队上限(配置数据)
	 */
	private int cfgMaxWaitCount;
	
	public ServerSettingData() {
		
	}
	
	public ServerSettingData(int maxOnlineCount, int maxRegisterCount, int maxWaitCount) {
		this.maxOnlineCount = maxOnlineCount;
		this.maxRegisterCount = maxRegisterCount;
		this.maxWaitCount = maxWaitCount;
	}

	public int getMaxOnlineCount() {
		return maxOnlineCount;
	}

	public void setMaxOnlineCount(int maxOnlineCount) {
		this.maxOnlineCount = maxOnlineCount;
	}

	public int getMaxRegisterCount() {
		return maxRegisterCount;
	}

	public void setMaxRegisterCount(int maxRegisterCount) {
		this.maxRegisterCount = maxRegisterCount;
	}

	public int getMaxWaitCount() {
		return maxWaitCount;
	}

	public void setMaxWaitCount(int maxWaitCount) {
		this.maxWaitCount = maxWaitCount;
	}

	public int getCfgMaxOnlineCount() {
		return cfgMaxOnlineCount;
	}

	public void setCfgMaxOnlineCount(int cfgMaxOnlineCount) {
		this.cfgMaxOnlineCount = cfgMaxOnlineCount;
	}

	public int getCfgMaxRegisterCount() {
		return cfgMaxRegisterCount;
	}

	public void setCfgMaxRegisterCount(int cfgMaxRegisterCount) {
		this.cfgMaxRegisterCount = cfgMaxRegisterCount;
	}

	public int getCfgMaxWaitCount() {
		return cfgMaxWaitCount;
	}

	public void setCfgMaxWaitCount(int cfgMaxWaitCount) {
		this.cfgMaxWaitCount = cfgMaxWaitCount;
	}
	
}
