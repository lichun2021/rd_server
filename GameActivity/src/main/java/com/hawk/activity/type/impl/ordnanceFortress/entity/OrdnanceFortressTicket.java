package com.hawk.activity.type.impl.ordnanceFortress.entity;

import java.util.List;

import org.hawk.config.HawkConfigManager;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.type.impl.ordnanceFortress.cfg.OrdnanceFortressBigReward;
import com.hawk.activity.type.impl.ordnanceFortress.cfg.OrdnanceFortressCommReward;
import com.hawk.game.protocol.Activity.OrdnanceFortressRewardType;
import com.hawk.game.protocol.Activity.PBOrdnanceFortressTicket;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.serialize.string.DataArray;
import com.hawk.serialize.string.SplitEntity;

/**
 * 时空之门
 * 
 * @author che
 *
 */
public class OrdnanceFortressTicket implements SplitEntity {
	
	/** 奖券ID*/
	private int ticketId;
	
	/** 是否是大奖*/
	private int rewardType;

	/** 奖励ID*/
	private int rewardId;

	/** 打开的次数*/
	private int openCount;
	
	/** 打开的时间*/
	private long openTime;
	
	
	
	public OrdnanceFortressTicket() {
		
	}
	
	
	
	public static OrdnanceFortressTicket valueOf(int ticketId,int rewardType,int rewardId,int openCount,long openTime) {
		OrdnanceFortressTicket data = new OrdnanceFortressTicket();
		data.ticketId = ticketId;
		data.rewardType = rewardType;
		data.rewardId = rewardId;
		data.openCount = openCount;
		data.openTime = openTime;
		return data;
	}
	
	
	

	
	public int getTicketId() {
		return ticketId;
	}

	public void setTicketId(int ticketId) {
		this.ticketId = ticketId;
	}


	public int getRewardType() {
		return rewardType;
	}



	public void setRewardType(int rewardType) {
		this.rewardType = rewardType;
	}

	public int getRewardId() {
		return rewardId;
	}

	public void setRewardId(int rewardId) {
		this.rewardId = rewardId;
	}

	public int getOpenCount() {
		return openCount;
	}

	public void setOpenCount(int openCount) {
		this.openCount = openCount;
	}

	public long getOpenTime() {
		return openTime;
	}

	public void setOpenTime(long openTime) {
		this.openTime = openTime;
	}
	

	@Override
	public SplitEntity newInstance() {
		return new OrdnanceFortressTicket();
	}
	
	public List<RewardItem.Builder> rewards(){
		String rewards= "";
		if(this.rewardType == OrdnanceFortressRewardType.COMM_REWEARD_VALUE){
			OrdnanceFortressCommReward cfg = HawkConfigManager.getInstance().
					getConfigByKey(OrdnanceFortressCommReward.class, this.rewardId);
			rewards = cfg.getRewards();
		}else if(this.rewardType == OrdnanceFortressRewardType.BIG_REWEARD_VALUE){
			OrdnanceFortressBigReward cfg = HawkConfigManager.getInstance().
					getConfigByKey(OrdnanceFortressBigReward.class, this.rewardId);
			rewards = cfg.getReward();
		}
		return RewardHelper.toRewardItemList(rewards);
	}
	
	@Override
	public void serializeData(List<Object> dataList) {
		dataList.add(this.ticketId);
		dataList.add(this.rewardType);
		dataList.add(this.rewardId);
		dataList.add(this.openCount);
		dataList.add(this.openTime);
	}

	
	@Override
	public void fullData(DataArray dataArray) {
		dataArray.setSize(5);
		this.ticketId = dataArray.getInt();
		this.rewardType = dataArray.getInt();
		this.rewardId =  dataArray.getInt();
		this.openCount = dataArray.getInt();
		this.openTime = dataArray.getLong();
	}

	
	public PBOrdnanceFortressTicket.Builder toBuilder(){
		PBOrdnanceFortressTicket.Builder builder  = PBOrdnanceFortressTicket.newBuilder();
		builder.setTicketId(this.ticketId);
		builder.setRewardType(OrdnanceFortressRewardType.valueOf(this.rewardType));
		builder.setRewardId(this.rewardId);
		return builder;
	}
	
	
	
	@Override
	public String toString() {
		return "[ticketId=" + ticketId + ", rewardType=" + rewardType + 
				", rewardId=" + rewardId + ", openCount=" + openCount +", openTime=" + openTime +"]";
	}
	

	
}
