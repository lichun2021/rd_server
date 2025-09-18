package com.hawk.activity.type.impl.bestprize.cfg;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkTime;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 新春头奖专柜活动
 * @author lating
 *
 */
@HawkConfigManager.KVResource(file = "activity/the_best_prize/the_best_prize_const.xml")
public class BestPrizeKVCfg extends HawkConfigBase {
	/**
	 * 起服延迟开放时间
	 */
	private final int serverDelay;
	
	/**
	 * 每日刷新时间间隔（分钟）
	 */
	private final int dailyRefreshTime;
	
	/**
	 * 活动可抽取时间段
	 */
	private final String drawTimes;
	
	/**
	 * 基地等级大于等于xx级
	 */
	private final int baseLevelLimit;
	
	/**
	 * 贵族等级大于x级
	 */
	private final int vipLevelLimit;
	
	/**
	 * 微信分组数量
	 */
	private final int groupAmount;
	/**
	 * qq微信分组数量
	 */
	private final int groupQQAmount;
	
	/**
	 * 抽奖记录数据存储上限
	 */
	private final int saveDataLimit;
	
	/**
	 * 活动结束转换抽奖道具
	 */
	private final String drawItem;
	
	/**
	 * 活动结束保留抽奖道具
	 */
	private final String drawReserveItem;

	/**
	 * 抽奖道具活动结束兑换成指定物品
	 */
	private final String drawItemChange;
	
	/**
	 * 分组时间（活动开启后**秒内服务器正在分组）
	 */
	private final int groupingTime;
	
	/**
	 * 添加奖池的时间
	 */
	private final String addPoolTimeHour;
	
	
	private List<long[]> drawTimeArrList = new ArrayList<>();
	
	private int drawItemId = 0;
	private int drawReserveItemId = 0;
	private List<Integer> addPoolTimeHourList;
	
	/**
	 * 单例
	 */
	private static BestPrizeKVCfg instance;
	
	/**
	 * 获取单例
	 * @return
	 */
	public static BestPrizeKVCfg getInstance(){
		return instance;
	}
	
	/**
	 * 构造
	 */
	public BestPrizeKVCfg(){
		serverDelay = 0;
		dailyRefreshTime = 5;
		drawTimes = "";
		baseLevelLimit = 0;
		vipLevelLimit = 0;
		groupAmount = 1;
		groupQQAmount = 1;
		saveDataLimit = 100;
		drawItem = "";
		drawReserveItem = "";
		drawItemChange = "";
		groupingTime = 300;
		addPoolTimeHour = "";
	}
	
	public long getGroupingTime() {
		return groupingTime * 1000L;
	}

	public long getServerDelay() {
		return serverDelay * 1000L;
	}

	public long getDailyRefreshTime() {
		return dailyRefreshTime * 60000L;
	}

	public String getDrawTimes() {
		return drawTimes;
	}

	public int getBaseLevelLimit() {
		return baseLevelLimit;
	}

	public int getVipLevelLimit() {
		return vipLevelLimit;
	}

	public int getGroupAmount() {
		return groupAmount;
	}

	@Override
	protected boolean assemble() {
		String timeBase = HawkTime.formatNowTime("yyyy-MM-dd");
		long am0Time = HawkTime.getAM0Date().getTime();
		String[] timeArr = drawTimes.split(",");
		for (String str : timeArr) {
			String[] arr = str.split("-");
			String[] arr1 = arr[0].split(":");
			String[] arr2 = arr[1].split(":");
			String time1 = timeBase + " " + (arr1.length > 2 ? arr[0] : (arr[0] + ":00"));
			String time2 = timeBase + " " + (arr2.length > 2 ? arr[1] : (arr[1] + ":00"));
			long time1Long = HawkTime.parseTime(time1);
			long time2Long = HawkTime.parseTime(time2);
			if (time1Long >= time2Long) {
				return false;
			}
			long[] msArr = new long[2];
			msArr[0] = time1Long - am0Time;
			msArr[1] = time2Long - am0Time;
			drawTimeArrList.add(msArr);
		}
		
		List<RewardItem.Builder> consume = RewardHelper.toRewardItemImmutableList(drawItem);
		drawItemId = consume.get(0).getItemId();
		
		List<RewardItem.Builder> consume1 = RewardHelper.toRewardItemImmutableList(drawReserveItem);
		drawReserveItemId = consume1.get(0).getItemId();
		
		addPoolTimeHourList = SerializeHelper.stringToList(Integer.class, addPoolTimeHour, ",");
		
		instance = this;
		return true;
	}
	
	public List<long[]> getDrawTimeArrList() {
		return drawTimeArrList;
	}
	
	public boolean checkDrawTimeRange(long time) {
		long am0Time = HawkTime.getAM0Date().getTime();
		for (long[] msArr : drawTimeArrList) {
			if (time >= am0Time + msArr[0] && time <= am0Time + msArr[1]) {
				return true;
			}
		}
		return false;
	}

	public int getSaveDataLimit() {
		return saveDataLimit;
	}

	public String getDrawItemChange() {
		return drawItemChange;
	}

	public int getGroupQQAmount() {
		return groupQQAmount;
	}

	public String getDrawItem() {
		return drawItem;
	}

	public String getDrawReserveItem() {
		return drawReserveItem;
	}
	
	public int getDrawItemId() {
		return drawItemId;
	}
	
	public int getDrawReserveItemId() {
		return drawReserveItemId;
	}
	
	public List<Integer> getAddPoolTimeHourList() {
		return addPoolTimeHourList;
	}
}
