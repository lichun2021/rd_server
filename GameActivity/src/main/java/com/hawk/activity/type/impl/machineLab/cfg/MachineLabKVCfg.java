package com.hawk.activity.type.impl.machineLab.cfg;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import org.hawk.os.HawkOSOperator;

import com.google.common.collect.ImmutableMap;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.gamelib.activity.ConfigChecker;

@HawkConfigManager.KVResource(file = "activity/machine_lab/machine_lab_cfg.xml")
public class  MachineLabKVCfg extends HawkConfigBase {
	/**
	 * 服务器开服延时开启活动时间；单位:秒
	 */
	private final int serverDelay;
	
	private final int iosPayId;
	
	private final int androidPayId;

	//可捐献道具ID
	private final String donateItemId;
	// 捐献道具换算比例（全服经验_个人经验_攻坚点数）
	private final String donateProp;// = 10_10_10
	//捐献道具暴击权重 倍数_权重,倍数_权重
	private final String donatMultiple;// 200_30,300_20 
	private final String stormingItemId; //攻坚点道具Id
	private final int giftDropMultiple; //特权掉落倍数
	private final int maxDonateLimit; //单抽捐献上限
	//排行榜显示人数
	private final int rankShowSize;
	//  注水目标经验
	private final int floodAimExp;

	//  注水开始时间
	private final int floodBegin;

	// 注水结束时间
	private final int floodEnd; 

	// 注水检测时间周期
	private final int floodCd;
	
	
	private int donateServerExp;
	private int donatePlayerExp;
	private int donateStormingPoint;
	private Map<Integer,Integer> donatMultipleMap;
	
	public MachineLabKVCfg(){
		serverDelay =0;
		iosPayId=0;
		androidPayId =0;
		donateItemId = "";
		donateProp = "";
		donatMultiple = "";
		stormingItemId= "";
		giftDropMultiple = 1;
		maxDonateLimit = 100;
		floodAimExp = 0;
		floodBegin = 0;
		floodEnd = 0; 
		floodCd = 0;
		rankShowSize = 100;
		
	}
	
	
	@Override
	protected boolean assemble() {
		if(!HawkOSOperator.isEmptyString(this.donateProp)){
			String[] arr = this.donateProp.split("_");
			this.donateServerExp = Integer.parseInt(arr[0]);
			this.donatePlayerExp = Integer.parseInt(arr[1]);
			this.donateStormingPoint = Integer.parseInt(arr[2]);
		}
		Map<Integer,Integer> donatMultipleMapTemp = new HashMap<>();
		if(!HawkOSOperator.isEmptyString(this.donatMultiple)){
			String[] arr = this.donatMultiple.split(",");
			for(String str : arr){
				String[] valArr = str.split("_");
				int key = Integer.parseInt(valArr[0]);
				int val = Integer.parseInt(valArr[1]);
				donatMultipleMapTemp.put(key,val);
			}
		}
		this.donatMultipleMap = ImmutableMap.copyOf(donatMultipleMapTemp);
		return super.assemble();
	}
	
	
	
	@Override
	protected boolean checkValid() {
		boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(this.stormingItemId);
		if (!valid) {
			throw new InvalidParameterException(String.format("MachineLabKVCfg stormingItemId error, stormingItemId: %s", stormingItemId));
		}
		valid = ConfigChecker.getDefaultChecker().checkAwardsValid(this.donateItemId);
		if (!valid) {
			throw new InvalidParameterException(String.format("MachineLabKVCfg donateItemId error, donateItemId: %s", donateItemId));
		}
		return super.checkValid();
	}


	public long getServerDelay() {
		return serverDelay * 1000L;
	}

	
	
	public int getAndroidPay() {
		return androidPayId;
	}
	
	
	public int getIosPay() {
		return iosPayId;
	}
	
	public int getFloodAimExp() {
		return floodAimExp;
	}
	
	public int getFloodBegin() {
		return floodBegin;
	}
	
	public int getFloodCd() {
		return floodCd;
	}
	
	public int getFloodEnd() {
		return floodEnd;
	}

	
	public RewardItem.Builder getDonateItem() {
		return RewardHelper.toRewardItem(this.donateItemId);
	}
	
	public int getDonatePlayerExp() {
		return donatePlayerExp;
	}
	
	public int getDonateServerExp() {
		return donateServerExp;
	}
	
	public int getDonateStormingPoint() {
		return donateStormingPoint;
	}
	
	public Map<Integer, Integer> getDonatMultipleMap() {
		return donatMultipleMap;
	}
	
	public int getRankShowSize() {
		return rankShowSize;
	}
	
	public RewardItem.Builder getStormingItem() {
		return RewardHelper.toRewardItem(this.stormingItemId);
	}
	
	public int getGiftDropMultiple() {
		return giftDropMultiple;
	}
	
	public int getMaxDonateLimit() {
		return maxDonateLimit;
	}
}