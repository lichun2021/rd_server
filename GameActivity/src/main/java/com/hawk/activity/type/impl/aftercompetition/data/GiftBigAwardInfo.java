package com.hawk.activity.type.impl.aftercompetition.data;

import java.util.Set;

import org.hawk.collection.ConcurrentHashSet;

import com.hawk.activity.type.impl.aftercompetition.AfterCompetitionConst;

/**
 * 大奖信息
 * 
 * @author lating
 *
 */
public class GiftBigAwardInfo {
	
	private int giftId;
	private long unlockTime;
	private int globalBuyCount;
	private Set<String> qqSendAwardIds = new ConcurrentHashSet<>();
	private Set<String> wxSendAwardIds = new ConcurrentHashSet<>();
	
	public GiftBigAwardInfo() {
	}
	
	public GiftBigAwardInfo(int giftId, long unlockTime, int globalBuyCount) {
		this.giftId = giftId;
		this.unlockTime = unlockTime;
		this.globalBuyCount = globalBuyCount;
	}

	public int getGiftId() {
		return giftId;
	}

	public void setGiftId(int giftId) {
		this.giftId = giftId;
	}

	public long getUnlockTime() {
		return unlockTime;
	}

	public void setUnlockTime(long unlockTime) {
		this.unlockTime = unlockTime;
	}

	public int getGlobalBuyCount() {
		return globalBuyCount;
	}

	public void setGlobalBuyCount(int globalBuyCount) {
		this.globalBuyCount = globalBuyCount;
	}

	public Set<String> getQQSendAwardIds() {
		return qqSendAwardIds;
	}

	public Set<String> getWXSendAwardIds() {
		return wxSendAwardIds;
	}
	
	public Set<String> getSendAwardIds(int channel) {
		return channel == AfterCompetitionConst.CHANNEL_QQ ? this.getQQSendAwardIds() : this.getWXSendAwardIds();
	}
}
