package com.hawk.activity.type.impl.ghostSecret.cfg;

import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.HawkConfigBase.Id;

import com.hawk.serialize.string.SerializeHelper;

@HawkConfigManager.XmlResource(file = "activity/equip_treasure/equip_treasure_weight.xml")
public class GhostSecretWeightCfg extends HawkConfigBase {
	// <data id="1" specAwardGot="0" drewTimes="0_10" specCardCount="0" card1Weight="8000" card2Weight="1000" card3Weight="1000" />
	//id
	@Id
	private final int id;
	//是否获得过特等奖
	private final int specAwardGot;
	//抽次数范围
	private final String drewTimes;
	//特级卡数量
	private final int specCardCount;
	//卡1 2 3权重
	private final String cardWeight;
	
	private Map<Integer, Integer> cardWeightMap;
	private int drewMin;
	private int drewMax;
	
	public GhostSecretWeightCfg(){
		id = 0;
		specAwardGot = 0;
		drewTimes = "";
		specCardCount = 0;
		cardWeight = "";
		
	}
	
	@Override
	protected boolean assemble() {
		cardWeightMap = SerializeHelper.stringToMap(cardWeight, Integer.class, Integer.class, SerializeHelper.ATTRIBUTE_SPLIT, SerializeHelper.BETWEEN_ITEMS);
		
		String[] drewTimesArr = SerializeHelper.split(drewTimes, SerializeHelper.ATTRIBUTE_SPLIT);
		if(drewTimesArr.length != 2){
			return false;
		}
		drewMin = Integer.parseInt(drewTimesArr[0]);
		drewMax = Integer.parseInt(drewTimesArr[1]);
		return super.assemble();
	}

	public int getId() {
		return id;
	}

	public boolean getSpecAwardGot() {
		return specAwardGot == 1;
	}

	public String getDrewTimes() {
		return drewTimes;
	}

	public int getSpecCardCount() {
		return specCardCount;
	}

	public int getDrewMin() {
		return drewMin;
	}

	public int getDrewMax() {
		return drewMax;
	}

	public String getCardWeight() {
		return cardWeight;
	}

	public Map<Integer, Integer> getCardWeightMap() {
		return cardWeightMap;
	}

}
