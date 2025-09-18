package com.hawk.activity.type.impl.backFlow.chemistry.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.type.impl.backFlow.comm.BackFlowPlayer;
import com.hawk.serialize.string.SerializeHelper;

@HawkConfigManager.XmlResource(file = "activity/grassback/grassback_gift_activity_date.xml")
public class ChemistryDateCfg extends HawkConfigBase{
	@Id
	private final int id;
	
	private final String lossDays;
	
	private final String vip;
	
	private final int duration;
	
	
	
	private int lossDaysStart;
	private int lossDaysEnd;
	private int vipStart;
	private int vipEnd;
	
	public ChemistryDateCfg(){
		id = 0;
		lossDays = "";
		vip = "";
		duration = 0;
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



	
	
	
}
