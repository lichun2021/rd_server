package com.hawk.game.module.material.data;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.os.HawkRand;

import com.hawk.activity.type.impl.materialTransport.cfg.MaterialTransportGroupCfg;
import com.hawk.activity.type.impl.materialTransport.cfg.MaterialTransportRefreshCfg;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.module.material.MTConst.MTTruckType;
import com.hawk.game.protocol.MaterialTransport.PBMTPlayer;
import com.hawk.game.protocol.MaterialTransport.PBMTTruckGroup;
import com.hawk.game.protocol.MaterialTransport.PBTruckReward;
import com.hawk.game.util.RandomUtil;

/**
 * 货车车厢
 */
public class MTTruckGroup {
	private int refreshCnt = -1;
	private int index;
	private int groupId; // MaterialTransportGroupCfg
	private List<MTReward> rewards = new ArrayList<>();

	private List<MTMember> memberList = new ArrayList<>();

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public List<MTMember> getMemberList() {
		return memberList;
	}

	public void setMemberList(List<MTMember> memberList) {
		this.memberList = memberList;
	}

	public int getRefreshCnt() {
		return refreshCnt;
	}

	public void setRefreshCnt(int refreshCnt) {
		this.refreshCnt = refreshCnt;
	}

	public PBMTTruckGroup toPBObj() {
		PBMTTruckGroup.Builder builder = PBMTTruckGroup.newBuilder();
		builder.setRefreshCnt(refreshCnt);
		builder.setIndex(index);
		builder.setGroupCfgId(groupId);
		for (MTReward ward : rewards) {
			builder.addRewards(ward.toPBObj());
		}
		for (MTMember me : memberList) {
			builder.addMembers(me.toPBObj());
		}
		return builder.build();
	}

	public void mergeFrom(PBMTTruckGroup obj) {
		this.refreshCnt = obj.getRefreshCnt();
		this.index = obj.getIndex();
		this.groupId = obj.getGroupCfgId();
		List<MTReward> rewards = new ArrayList<>();
		for (PBTruckReward pbre : obj.getRewardsList()) {
			MTReward reward = new MTReward();
			reward.mergerFrom(pbre);
			rewards.add(reward);
		}
		this.rewards = rewards;

		List<MTMember> memberList = new ArrayList<>();
		for (PBMTPlayer pbre : obj.getMembersList()) {
			MTMember reward = new MTMember();
			reward.mergeFrom(pbre);
			memberList.add(reward);
		}
		this.memberList = memberList;
	}

	public List<ItemInfo> getRewards() {
		List<ItemInfo> list = new ArrayList<>();
		for (MTReward ward : rewards) {
			list.add(ward.getReward());
		}
		return list;
	}

	/**
	 * @param robCntAdd 是否增加被抢次数.  不计数 则不损失到达奖励. 
	 */
	public List<ItemInfo> rob(boolean robCntAdd) {
		MaterialTransportGroupCfg rewardCfg = HawkConfigManager.getInstance().getConfigByKey(MaterialTransportGroupCfg.class, groupId);
		List<MTReward> robs = RandomUtil.randomWeightObject(rewards, rewardCfg.getRobNumber());
		List<ItemInfo> robList = new ArrayList<>();
		for (MTReward ward : robs) {
			robList.add(ward.rob(robCntAdd));
		}
		return robList;
	}

	public void refreshAward(MTTruckType type) {
		refreshCnt++;
		MaterialTransportGroupCfg rewardCfg = randReward(type, refreshCnt);
		List<MTReward> rewardsList = new ArrayList<>(rewardCfg.getRewardList().size());
		for (int rid : rewardCfg.getRewardList()) {
			MTReward ward = new MTReward();
			ward.setRewardId(rid);
			rewardsList.add(ward);
		}
		this.groupId = rewardCfg.getId();
		this.rewards = rewardsList;
	}

	private MaterialTransportGroupCfg randReward(MTTruckType type, int refreshNumber) {
		ConfigIterator<MaterialTransportRefreshCfg> rit = HawkConfigManager.getInstance().getConfigIterator(MaterialTransportRefreshCfg.class);
		MaterialTransportRefreshCfg rcfgSelect = null;
		for (MaterialTransportRefreshCfg rfg : rit) {
			if (rfg.getType() != type.getNumber()) {
				continue;
			}
			if (rfg.getRefreshNumber() <= refreshNumber) {
				rcfgSelect = rfg;
			}
		}
		int quality = HawkRand.randomWeightObject(rcfgSelect.getRefreshQualityMap());

		ConfigIterator<MaterialTransportGroupCfg> git = HawkConfigManager.getInstance().getConfigIterator(MaterialTransportGroupCfg.class);
		List<MaterialTransportGroupCfg> glist = git.stream().filter(gcfg -> gcfg.getQuality() == quality && gcfg.getType() == type.getNumber()).collect(Collectors.toList());
		return RandomUtil.randomWeightObject(glist);
	}

	public int getGroupId() {
		return groupId;
	}

	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}

	public void setRewards(List<MTReward> rewards) {
		this.rewards = rewards;
	}

	public boolean isMember(String playerId) {
		for (MTMember m : memberList) {
			if (m.getPlayerId().equals(playerId)) {
				return true;
			}
		}
		return false;
	}

}
