package com.hawk.activity.type.impl.redEnvelopePlayer.cfg;

import java.util.ArrayList;
import java.util.List;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;
import org.hawk.os.HawkRand;

import com.google.common.collect.ImmutableList;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.gamelib.activity.ConfigChecker;

@HawkConfigManager.XmlResource(file = "activity/playerRedPacket/playerRedPacket.xml")
public class RedEnvelopePlayerDetailsCfg extends HawkConfigBase {

	@Id
	private final int id;
	
	private final String itemId;
	
	private final String grouping;
	
	private final int noticeID;
	
	private final int weight;
	
	private final int times;
	
	private final int receivelimit;
	
	private final int isshow;
	
	private List<RewardItem.Builder> list;
	
	private List<String> rewardItemList = new ArrayList<String>();
	private List<Integer> weightList;
	
	public RedEnvelopePlayerDetailsCfg(){
		this.id = 0;
		this.itemId = "";
		this.grouping = "";
		this.noticeID = 0;
		this.weight = 0;
		this.times = 0;
		this.receivelimit = 0;
		this.isshow = 0;
	}

	@Override
	protected boolean assemble() {
		try {
			this.list = RewardHelper.toRewardItemImmutableList(this.itemId);
			//
			String src[] = grouping.split(",");
			if (weight == 0) {
				for (String rewardItem : src) {
					rewardItemList.add(rewardItem);
				}
			} else {
				weightList = new ArrayList<Integer>();
				for (String rewardItem : src) {
					String[] splits = rewardItem.split("_");
					if (splits.length < 4) {
						return false;
					}
					
					rewardItemList.add(String.format("%s_%s_%s", splits[0], splits[1], splits[2]));
					weightList.add(Integer.valueOf(splits[3]));
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}
		return super.assemble();
	}

	public int getId() {
		return id;
	}

	public String getItemId() {
		return itemId;
	}

	public String getGrouping() {
		return grouping;
	}

	public int getNoticeID() {
		return noticeID;
	}
	
	public boolean isWeight() {
		return weight > 0;
	}
	
	public int getTimes() {
		return times;
	}

	public List<RewardItem.Builder> getItemList() {
		List<RewardItem.Builder> list = new ArrayList<RewardItem.Builder>();
		list.addAll(RewardHelper.toRewardItemList(this.itemId));
		return list;
	}

	public List<RewardItem.Builder> getList() {
		return list;
	}
	
	public int getNum(){
		if (!isWeight()) {
			return rewardItemList.size();
		}
		
		return times > 0 ? times : rewardItemList.size();
	}
	
	public List<String> getRewards(){
		if (!isWeight()) {
			return ImmutableList.copyOf(rewardItemList);
		} 
		
		int count = times;
		if (count <= 0) {
			count = rewardItemList.size();
		}
		List<String> rewardItems = new ArrayList<String>();
		for (int i=0; i < count; i++) {
			String rewardItem = HawkRand.randomWeightObject(rewardItemList, weightList);
			rewardItems.add(rewardItem);
		}
	
		return rewardItems;
	}

	@Override
	protected boolean checkValid() {
		if (weight > 0 && times <= 0) {
			return false;
		}
		
		String src[] = grouping.split(",");
		if(src.length < 1){
			logger.info("grouping 字段配置不正确..");
			return false;
		}
		if(!ConfigChecker.getDefaultChecker().checkAwardsValid(itemId)){
			logger.info("在award.xml表找不到itemId:{}", itemId);
			return false;
		}
		return super.checkValid();
	}

	public int getReceivelimit() {
		return receivelimit;
	}
	
	public boolean isShow() {
		return isshow == 0;
	}
}
