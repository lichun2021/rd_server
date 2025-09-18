package com.hawk.activity.type.impl.drogenBoatFestival.gift.cfg;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.serialize.string.SerializeHelper;

/**
 * 端午-联盟庆典配置
 * 
 * @author che
 *
 */
@HawkConfigManager.KVResource(file = "activity/dw_dragonboat/dw_dragonboat_cfg.xml")
public class DragonBoatGiftKVCfg extends HawkConfigBase {

	//服务器开服延时开启活动时间
	private final int serverDelay;
	
	private final String loginAward;
	
	private final int boatAward;
	
	private final int giftCount;
	
	private final String refreshTimes;
	
	private final String dragonPoint;
	
	private final int areaRadius;
	
	
	private int dragonPointX;
	
	private int dragonPointY;
	
	private List<Integer> refreshTimeList = new ArrayList<>();
	
	
	
	public DragonBoatGiftKVCfg() {
		serverDelay = 0;
		loginAward = "";
		boatAward = 0;
		refreshTimes = "";
		giftCount = 0;
		dragonPoint = "";
		areaRadius = 0;
		
	}

	
	@Override
	protected boolean assemble() {
		SerializeHelper.stringToList(Integer.class, this.refreshTimes, 
				SerializeHelper.ATTRIBUTE_SPLIT,refreshTimeList);
		
		String[] arr = dragonPoint.split("_");
		if(arr.length != 2){
			return false;
		}
		this.dragonPointX = Integer.valueOf(arr[0]);
		this.dragonPointY =  Integer.valueOf(arr[1]);
		return super.assemble();
	}
	public long getServerDelay() {
		return serverDelay * 1000l;
	}

	public List<Integer> getRefreshTimeList() {
		return refreshTimeList;
	}


	public int getGiftCount() {
		return giftCount;
	}


	public String getLoginAward() {
		return loginAward;
	}


	public int getBoatAward() {
		return boatAward;
	}


	public int getDragonPointX() {
		return dragonPointX;
	}

	public int getDragonPointY() {
		return dragonPointY;
	}


	public int getAreaRadius() {
		return areaRadius;
	}


	
	



	
	
	
	
	
}
