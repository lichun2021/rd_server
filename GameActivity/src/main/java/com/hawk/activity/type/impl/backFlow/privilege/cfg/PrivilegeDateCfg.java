package com.hawk.activity.type.impl.backFlow.privilege.cfg;

import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.type.impl.backFlow.comm.BackFlowPlayer;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.serialize.string.SerializeHelper;

@HawkConfigManager.XmlResource(file = "activity/back_privilege_activity/back_privilege_activity_date.xml")
public class PrivilegeDateCfg extends HawkConfigBase{
	@Id
	private final int id;
	
	private final String lossDays;
	
	private final String vip;
	
	private final int duration;
	
	private final String buffs;
	
	private final String rewards;
	
	
	private int lossDaysStart;
	private int lossDaysEnd;
	private int vipStart;
	private int vipEnd;
	
	private List<Integer> buffList;
	private List<RewardItem.Builder> rewardList; 
	
	public PrivilegeDateCfg(){
		id = 0;
		lossDays = "";
		vip = "";
		duration = 0;
		buffs = "";
		rewards = "";
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
		
		buffList = SerializeHelper.cfgStr2List(buffs, SerializeHelper.ATTRIBUTE_SPLIT);
		rewardList = RewardHelper.toRewardItemImmutableList(rewards);
		
		return super.assemble();
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


	public int getDuration() {
		return duration;
	}



	public boolean isAdapt(BackFlowPlayer bplayer){
		int lossDays = bplayer.getLossDays();
		if(bplayer.getVipLevel() >= this.getVipStart() && 
				bplayer.getVipLevel() <= this.getVipEnd() && 
						lossDays >= this.getLossDaysStart() && 
								lossDays <= this.getLossDaysEnd()){
			return true;
		}
		return false;
	}



	public int getId() {
		return id;
	}



	public List<Integer> getBuffList() {
		return buffList;
	}



	public List<RewardItem.Builder> getRewardList() {
		return rewardList;
	}



	
	
}
