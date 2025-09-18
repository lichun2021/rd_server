package com.hawk.activity.type.impl.aftercompetition.cfg;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;

import com.hawk.serialize.string.SerializeHelper;

@HawkConfigManager.KVResource(file = "activity/after_competition_party/after_competition_party_cfg.xml")
public class AfterCompetitionConstCfg extends HawkConfigBase {
	
	private final int serverDelay;
	
	/** 致敬奖励 */
	private final String homageReward;
	
	/** 致敬注水 */
	private final String homageAddValue;
	
	/** 每日手动发送大赏时间  */
	private final String dailySendTime;
	
	/** 活动结束前x小时不可赠礼 */
	private final int competitionGiveTimeLimit;
	
	/** 活动结束前x小时自动发大赏 */
	private final int achieveEndAutoSendTime;
	
	private List<int[]> homageAddValList = new ArrayList<>();
	private List<long[]> sendTimeArrList = new ArrayList<>();
	
	private List<String> rewardStrList = new ArrayList<>();
	private List<Integer> rewardWeightList = new ArrayList<>();
	

	private static AfterCompetitionConstCfg instance;
	
	public static AfterCompetitionConstCfg getInstance() {
		return instance;
	}
	
	public AfterCompetitionConstCfg() {
		this.serverDelay = 0;
		this.homageReward = "";
		this.homageAddValue = "";
		this.dailySendTime = "";
		this.competitionGiveTimeLimit = 0;
		this.achieveEndAutoSendTime = 0;
	}
	
	@Override
	protected boolean assemble() {
		String timeBase = HawkTime.formatNowTime("yyyy-MM-dd");
		long am0Time = HawkTime.getAM0Date().getTime();
		String[] timeArr = dailySendTime.split(",");
		for (String str : timeArr) {
			String[] arr = str.split("_");
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
			sendTimeArrList.add(msArr);
		}
		
		homageAddValList = SerializeHelper.str2intList(this.homageAddValue);
		
		String[] arr = homageReward.split(",");
		for (String item : arr) {
			String[] itemStr = item.split("_");
			rewardStrList.add(String.format("%s_%s_%s", itemStr[0], itemStr[1], itemStr[2]));
			rewardWeightList.add(Integer.parseInt(itemStr[3]));
		}
		
		instance = this;
		return true;
	}
	
	public long getServerDelay() {
		return serverDelay * 1000L;
	}

	public long getCompetitionGiveTimeLimit() {
		return competitionGiveTimeLimit * 3600000L;
	}

	public long getAchieveEndAutoSendTime() {
		return achieveEndAutoSendTime * 3600000L;
	}

	public String getHomageReward() {
		return homageReward;
	}

	public String getHomageAddValue() {
		return homageAddValue;
	}

	public String getDailySendTime() {
		return dailySendTime;
	}

	public List<int[]> getHomageAddValList() {
		return homageAddValList;
	}

	public List<long[]> getSendTimeArrList() {
		return sendTimeArrList;
	}
	
	public boolean checkSendTimeRange() {
		long time = HawkTime.getMillisecond();
		long am0Time = HawkTime.getAM0Date().getTime();
		for (long[] msArr : sendTimeArrList) {
			if (time >= am0Time + msArr[0] && time <= am0Time + msArr[1]) {
				return true;
			}
		}
		return false;
	}
	
	public String getRandHomageRewardStr() {
		return HawkRand.randomWeightObject(rewardStrList, rewardWeightList);
	}
	
}
