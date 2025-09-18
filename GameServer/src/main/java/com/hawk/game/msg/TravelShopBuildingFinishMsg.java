package com.hawk.game.msg;

import org.hawk.msg.HawkMsg;

import com.hawk.gamelib.GameConst.MsgId;

public class TravelShopBuildingFinishMsg extends HawkMsg {
	/**
	 * 城建配置ID
	 */
	private int cfgType;
	
	public TravelShopBuildingFinishMsg() {
		super(MsgId.TRAVEL_SHOP_BUILDING_FINISH);
	}
	

	public int getCfgType() {
		return cfgType;
	}


	public void setCfgType(int cfgType) {
		this.cfgType = cfgType;
	}


	public static TravelShopBuildingFinishMsg valueOf(int cfgType){
		TravelShopBuildingFinishMsg msg = new TravelShopBuildingFinishMsg();
		msg.cfgType = cfgType;
		
		return msg;
	}
}
 