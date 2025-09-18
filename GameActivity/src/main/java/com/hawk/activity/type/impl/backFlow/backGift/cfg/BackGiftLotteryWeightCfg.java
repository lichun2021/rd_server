package com.hawk.activity.type.impl.backFlow.backGift.cfg;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.hawk.serialize.string.SerializeHelper;

@HawkConfigManager.XmlResource(file = "activity/back_gift_activity/back_gift_award_weight.xml")
public class BackGiftLotteryWeightCfg extends HawkConfigBase{
	@Id
	private final int id;
	
	private final int type;
	
	private final int weight;
	
	private final String lotteryNum;
	
	
	private List<Integer> lotteryNumList = new ArrayList<Integer>();

	public BackGiftLotteryWeightCfg(){
		id = 0;
		type = 0;
		weight = 0;
		lotteryNum = "";
	}

	
	
	@Override
	protected boolean assemble() {
		if(!HawkOSOperator.isEmptyString(lotteryNum)){
			String[] arr = SerializeHelper.split(lotteryNum, SerializeHelper.BETWEEN_ITEMS);
			if(arr.length >0){
				for(String str : arr){
					lotteryNumList.add(Integer.parseInt(str));
				}
			}
		}
		return super.assemble();
	}



	public int getId() {
		return id;
	}



	public int getType() {
		return type;
	}



	public int getWeight() {
		return weight;
	}



	public List<Integer> getLotteryNumList() {
		return lotteryNumList;
	}


	public String getLotteryNum() {
		return lotteryNum;
	}
	

	
	


	

	

	


	
	
	
	
	
}
