package com.hawk.activity.type.impl.backFlow.backGift.cfg;

import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.type.impl.backFlow.backGift.entity.BackGiftEntity;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.serialize.string.SerializeHelper;

@HawkConfigManager.XmlResource(file = "activity/back_gift_activity/back_gift_activity_award.xml")
public class BackGiftLotteryCfg extends HawkConfigBase{
	@Id
	private final int id;
	
	private final int type;
	
	private final String lossDays;
	
	private final String vip;
	
	private final String awards;
	
	private final String chooseNum;
	
	
	
	
	
	private int lossDaysStart;
	private int lossDaysEnd;
	private int vipStart;
	private int vipEnd;
	private int lotteryNumStart;
	private int lotteryNumEnd;
	private List<RewardItem.Builder> rewardList;
	
	public BackGiftLotteryCfg(){
		id = 0;
		type = 0;
		lossDays = "";
		vip = "";
		awards = "";
		chooseNum = "";
	}

	
	
	@Override
	protected boolean assemble() {
		String[] lossDay = SerializeHelper.split(lossDays, SerializeHelper.ATTRIBUTE_SPLIT);
		if(lossDay.length != 2){
			return false;
		}
		lossDaysStart = Integer.parseInt(lossDay[0]);
		lossDaysEnd = Integer.parseInt(lossDay[1]);
		
		String[] viplimt = SerializeHelper.split(vip, SerializeHelper.ATTRIBUTE_SPLIT);
		if(viplimt.length != 2){
			return false;
		}
		vipStart = Integer.parseInt(viplimt[0]);
		vipEnd = Integer.parseInt(viplimt[1]);
		
		String[] chooseNumArr = SerializeHelper.split(chooseNum, SerializeHelper.ATTRIBUTE_SPLIT);
		if(chooseNumArr.length ==2){
			lotteryNumStart = Integer.parseInt(chooseNumArr[0]);
			lotteryNumEnd = Integer.parseInt(chooseNumArr[1]);
		}else if(chooseNumArr.length ==1){
			lotteryNumStart = Integer.parseInt(chooseNumArr[0]);
			lotteryNumEnd = Integer.parseInt(chooseNumArr[0]);
		}else{
			return false;
		}
		
		
		rewardList = RewardHelper.toRewardItemImmutableList(awards);
		return super.assemble();
	}
	
	public int getId() {
		return id;
	}

	public int getType() {
		return type;
	}

	public String getLossDays() {
		return lossDays;
	}

	public String getVip() {
		return vip;
	}

	public String getAwards() {
		return awards;
	}



	public int getLossDaysStart() {
		return lossDaysStart;
	}


	public int getLossDaysEnd() {
		return lossDaysEnd;
	}

	public int getVipStart() {
		return vipStart;
	}

	public int getVipEnd() {
		return vipEnd;
	}


	public List<RewardItem.Builder> getAwardList() {
		return rewardList;
	}



	public int getLotteryNumStart() {
		return lotteryNumStart;
	}



	public int getLotteryNumEnd() {
		return lotteryNumEnd;
	}



	


	
	public boolean isAdapt(BackGiftEntity entity){
		int lossDays = entity.getLossDays();
		if(entity.getLossVip() >= this.getVipStart() && 
				entity.getLossVip() <= this.getVipEnd() && 
						lossDays >= this.getLossDaysStart() && 
								lossDays <= this.getLossDaysEnd()){
			return true;
		}
		return false;
	}
	
	
	
	
}
