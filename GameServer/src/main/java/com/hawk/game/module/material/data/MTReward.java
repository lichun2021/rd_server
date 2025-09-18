package com.hawk.game.module.material.data;

import org.hawk.config.HawkConfigManager;

import com.hawk.activity.type.impl.materialTransport.cfg.MaterialTransportRewardCfg;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.protocol.MaterialTransport.PBTruckReward;

public class MTReward {
	private int rewardId; // MaterialTransportRewardCfg
	private int robCnt; // 被抢次数

	public ItemInfo rob(boolean robCntAdd) {
		if(robCntAdd){
			robCnt++;
		}
		MaterialTransportRewardCfg cfg = HawkConfigManager.getInstance().getConfigByKey(MaterialTransportRewardCfg.class, rewardId);
		ItemInfo item = ItemInfo.valueOf(cfg.getItem());
		int robnum = (int) (item.getCount() * cfg.getRobRate());
		ItemInfo robItem = item.clone();
		robItem.setCount(robnum);
		return robItem;
	}

	/**得到奖励*/
	public ItemInfo getReward() {
		MaterialTransportRewardCfg cfg = HawkConfigManager.getInstance().getConfigByKey(MaterialTransportRewardCfg.class, rewardId);
		ItemInfo item = ItemInfo.valueOf(cfg.getItem());
		int robnum = (int) (item.getCount() * cfg.getRobRate()) * robCnt;
		item.setCount(item.getCount() - robnum);
		return item;
	}

	public int getRewardId() {
		return rewardId;
	}

	public void setRewardId(int rewardId) {
		this.rewardId = rewardId;
	}

	public int getRobCnt() {
		return robCnt;
	}

	public void setRobCnt(int robCnt) {
		this.robCnt = robCnt;
	}

	public PBTruckReward toPBObj() {
		PBTruckReward.Builder builder = PBTruckReward.newBuilder();
		builder.setRewardCfgId(rewardId);
		builder.setRobCnt(robCnt);
		return builder.build();
	}
	
	public void mergerFrom(PBTruckReward obj){
		this.rewardId = obj.getRewardCfgId();
		this.robCnt = obj.getRobCnt();
	}

}
