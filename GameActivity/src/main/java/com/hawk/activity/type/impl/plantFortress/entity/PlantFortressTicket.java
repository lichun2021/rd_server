package com.hawk.activity.type.impl.plantFortress.entity;

import java.util.List;

import org.hawk.config.HawkConfigManager;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.type.impl.plantFortress.cfg.PlantFortressBigReward;
import com.hawk.activity.type.impl.plantFortress.cfg.PlantFortressCommReward;
import com.hawk.game.protocol.Activity.PBPlantFortressTicket;
import com.hawk.game.protocol.Activity.PlantFortressRewardType;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.serialize.string.DataArray;
import com.hawk.serialize.string.SplitEntity;

/**
 * 时空之门
 * 
 * @author che
 *
 */
public class PlantFortressTicket implements SplitEntity {
	
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
	
	
	
	public PlantFortressTicket() {
		
	}
	
	
	
	public static PlantFortressTicket valueOf(int ticketId,int rewardType,int rewardId,int openCount,long openTime) {
		PlantFortressTicket data = new PlantFortressTicket();
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
		return new PlantFortressTicket();
	}
	
	public List<RewardItem.Builder> rewards(){
		String rewards= "";
		if(this.rewardType == PlantFortressRewardType.PLANT_FORTRESS_COMM_REWEARD_VALUE){
			PlantFortressCommReward cfg = HawkConfigManager.getInstance().
					getConfigByKey(PlantFortressCommReward.class, this.rewardId);
			rewards = cfg.getRewards();
		}else if(this.rewardType == PlantFortressRewardType.PLANT_FORTRESS_BIG_REWEARD_VALUE){
			PlantFortressBigReward cfg = HawkConfigManager.getInstance().
					getConfigByKey(PlantFortressBigReward.class, this.rewardId);
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

	
	public PBPlantFortressTicket.Builder toBuilder(){
		PBPlantFortressTicket.Builder builder  = PBPlantFortressTicket.newBuilder();
		builder.setTicketId(this.ticketId);
		builder.setRewardType(PlantFortressRewardType.valueOf(this.rewardType));
		builder.setRewardId(this.rewardId);
		return builder;
	}
	
	
	@Override
	public String toString() {
		return "[ticketId=" + ticketId + ", rewardType=" + rewardType + 
				", rewardId=" + rewardId + ", openCount=" + openCount +", openTime=" + openTime +"]";
	}
	

	
}
