package com.hawk.activity.type.impl.redkoi.cfg;

import java.security.InvalidParameterException;
import java.util.concurrent.TimeUnit;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkTime;
import org.hawk.tuple.HawkTuple3;
import org.hawk.tuple.HawkTuples;

import com.hawk.serialize.string.SerializeHelper;


/**
 * 活动单键属性配置
 * @author che
 *
 */
@HawkConfigManager.KVResource(file = "activity/redkoi/koi_fish_cfg.xml")
public class RedkoiActivityKVCfg extends HawkConfigBase{

	
	/** 服务器开服延时开启活动时间 */
	private final int serverDelay;

	/** 是否每日重置(零点跨天重置) */
	private final int isDailyReset;
	
	/** 每期的免费次数*/
	private final int freeTimes;
	
	/** 许愿时间段*/
	private final String betTime;
	
	/** 开奖时间点*/
	private final String getRewardTime;
	
	
	private final String singlePrice;
	private final String tenPrice;
	private final String itemPrice;
	private final String singleGiftItem;
	private final String tenGiftItem;

	
	
	private int[][] betTimeArr;
	
	private int[][] getRewardTimeArr;
	
	
	
	public RedkoiActivityKVCfg(){
		this.serverDelay = 0;
		this.isDailyReset = 0;
		this.singlePrice = "";
		this.tenPrice = "";
		this.freeTimes = 0;
		this.betTime = "";
		this.getRewardTime = "";
		this.itemPrice = "";
		this.tenGiftItem = "";
		this.singleGiftItem = "";
		
	}
	
	
	
	/**
	 * 获取 上一期   当前期  下一期   结算时间
	 * @return
	 */
	public HawkTuple3<Long,Long,Long> getTurnIds(){
		RedkoiActivityKVCfg config = HawkConfigManager.getInstance().getKVInstance(RedkoiActivityKVCfg.class);
		int[][] awardTimeArr = config.getGetRewardTimeArr();
		final long DAYMILL = TimeUnit.DAYS.toMillis(1);
		final int Size = awardTimeArr.length;
		long[] arr = new long[Size];
		for (int i = 0; i < awardTimeArr.length; i++) {
			arr[i] = todayHHmmssTime(awardTimeArr[i]);
		}

		long[] timeArr = new long[Size * 5];
		for (int i = 0; i < arr.length; i++) {
			timeArr[i] = arr[i] - DAYMILL * 2;
		}
		for (int i = 0; i < arr.length; i++) {
			timeArr[i+Size] = arr[i] - DAYMILL ;
		}
		for (int i = 0; i < arr.length; i++) {
			timeArr[i + Size*2] = arr[i];
		}
		for (int i = 0; i < arr.length; i++) {
			timeArr[i + Size * 3] = arr[i] + DAYMILL;
		}
		for (int i = 0; i < arr.length; i++) {
			timeArr[i + Size * 4] = arr[i] + DAYMILL * 2;
		}
		
		long nowTime = HawkTime.getMillisecond();
		for(int i = 0;i< timeArr.length ;i++){
			if(nowTime > timeArr[i] && nowTime< timeArr[i+1]){
				return HawkTuples.tuple(timeArr[i], timeArr[i+1], timeArr[i+2]);
			}
		}
		throw new RuntimeException("完了 出错了");
	}
	
	long todayHHmmssTime(int[] time){
		int hour = time[0];
		int minute = time[1];
		int second = time[2];
		long zeroTime = HawkTime.getAM0Date().getTime();
		zeroTime +=  hour * HawkTime.HOUR_MILLI_SECONDS;
		zeroTime += minute * HawkTime.MINUTE_MILLI_SECONDS;
		zeroTime += second * 1000;
		return zeroTime;
	}

	
	@Override
	protected boolean assemble() {
		String[] itemArray = this.betTime.split(",");
		int[][] rltArray = null;
		for (int i = 0; i < itemArray.length; i++) {
			String item = itemArray[i];
			String[] propArray = item.split("-");
			if(propArray.length != 2){
				return false;
			}
			String oarrstr = propArray[0];
			String tarrstr =  propArray[1];
			String[] oarr = oarrstr.split(":");
			String[] tarr = tarrstr.split(":");
			if(oarr.length != 3){
				return false;
			}
			if(tarr.length != 3){
				return false;
			}
			if (rltArray == null) {
				rltArray = new int[itemArray.length][];
			}
			rltArray[i] = new int[6];
			for(int j = 0; j < oarr.length; j++) {
				rltArray[i][j] = Integer.parseInt(oarr[j]);
			}
			for(int j = 0; j < tarr.length; j++) {
				rltArray[i][j+3] = Integer.parseInt(tarr[j]);
			}
		}
		this.betTimeArr = rltArray;
		getRewardTimeArr = SerializeHelper.string2IntIntArray(
				this.getRewardTime, SerializeHelper.BETWEEN_ITEMS, SerializeHelper.COLON_ITEMS);
		return true;
	}
	
	
	@Override
	protected boolean checkValid() {
		if(this.betTimeArr == null){
			throw new InvalidParameterException(
					String.format("RedkoiActivityKVCfg betTimeArr error,betTimeArr is null"));
		}
		if(this.getRewardTimeArr == null){
			throw new InvalidParameterException(
					String.format("RedkoiActivityKVCfg getRewardTimeArr error,getRewardTimeArr is null"));
		}
		long zeroTime = HawkTime.getAM0Date().getTime();
		for(int i=0;i<this.betTimeArr.length;i++){
			int[] time = this.betTimeArr[i];
			int shour = time[0];
			int sminute = time[1];
			int sseconde = time[2];
			int ehour = time[3];
			int eminute = time[4];
			int eseconde = time[5];
			long startTime = zeroTime;
			startTime +=  shour * HawkTime.HOUR_MILLI_SECONDS;
			startTime += sminute * HawkTime.MINUTE_MILLI_SECONDS;
			startTime += sseconde * 1000;
			long endTime = zeroTime;
			endTime +=  ehour * HawkTime.HOUR_MILLI_SECONDS;
			endTime += eminute * HawkTime.MINUTE_MILLI_SECONDS;
			endTime += eseconde * 1000;
			if(startTime >=endTime){
				throw new InvalidParameterException(
						String.format("RedkoiActivityKVCfg betTimeArr error,startTime >= endTime!!!"));
			}
		}
		long tempTime = 0;
		for(int i=0;i<this.getRewardTimeArr.length;i++){
			int[] time = this.getRewardTimeArr[i];
			int hour = time[0];
			int minute = time[1];
			int seconde = time[2];
			long awardTime = zeroTime;
			awardTime +=  hour * HawkTime.HOUR_MILLI_SECONDS;
			awardTime += minute * HawkTime.MINUTE_MILLI_SECONDS;
			awardTime += seconde * 1000;
			if(i==0){
				tempTime = awardTime;
				continue;
			}
			if(tempTime >= awardTime){
				throw new InvalidParameterException(
						String.format("RedkoiActivityKVCfg getRewardTimeArr error,时间配置需按照从小到大"));
			}
			tempTime = awardTime;
		}
		return true;
	}
	public long getServerDelay() {
		return serverDelay * 1000l;
	}

	public int getIsDailyReset() {
		return isDailyReset;
	}



	public int getFreeTimes() {
		return freeTimes;
	}


	public int[][] getBetTimeArr() {
		return betTimeArr;
	}


	public int[][] getGetRewardTimeArr() {
		return getRewardTimeArr;
	}

	public String getBetTime() {
		return betTime;
	}


	public String getGetRewardTime() {
		return getRewardTime;
	}


	public String getSinglePrice() {
		return singlePrice;
	}


	public String getTenPrice() {
		return tenPrice;
	}


	public String getItemPrice() {
		return itemPrice;
	}



	public String getSingleGiftItem() {
		return singleGiftItem;
	}



	public String getTenGiftItem() {
		return tenGiftItem;
	}



	


	
	
	
	
	
	
	
	
	
}
