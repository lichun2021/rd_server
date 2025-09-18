package com.hawk.game.queryentity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * 统计注册等本服用户信息
 *
 * @author zhjx
 *
 */
@Entity
public class ServerPlayerStat {
	@Id
	@Column(name = "channel")
	protected String channel;
	
	@Column(name = "register")
	protected int register;
	
	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}
	
	public int getRegister() {
		return register;
	}

	public void setRegister(int register) {
		this.register = register;
	}

}
