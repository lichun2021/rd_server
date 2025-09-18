package com.hawk.activity.type.impl.redEnvelope.base;

import java.util.List;

import org.hawk.os.HawkException;

/***
 * 红包
 * ##发红包的时候，有人发出一个包，有n个人可领取此包，该类为一个人领取到的那一份红包
 * @author yang.rao
 *
 */
public class OnceRedEnvelope {
	
	/** 领取的玩家id，如果为null则未领取 **/
	private String recieveId;
	
	/** 奖励 **/
	private List<String> rewards;
	
	public static OnceRedEnvelope valueOf(String recieveId, List<String> rewards){
		OnceRedEnvelope red = new OnceRedEnvelope();
		red.recieveId = recieveId;
		red.rewards = rewards;
		return red;
	}

	public String getRecieveId() {
		return recieveId;
	}

	public void setRecieveId(String recieveId) {
		this.recieveId = recieveId;
	}

	public List<String> getRewards() {
		return rewards;
	}

	public void setRewards(List<String> rewards) {
		this.rewards = rewards;
	}
	
	/***
	 * 获取红包金币数量
	 * @return
	 */
	public int getItemCnt(){
		try {
			return Integer.parseInt(rewards.get(0).split("_")[2]);
		} catch (Exception e) {
			HawkException.catchException(e);
			return 0;
		}
	}
	
	public String getRewardItem() {
		return rewards.get(0);
	}
}
