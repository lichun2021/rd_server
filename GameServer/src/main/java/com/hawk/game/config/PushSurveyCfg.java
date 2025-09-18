package com.hawk.game.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;


/**
 * 调查问卷配置
 *
 * @author Jesse
 *
 */
@HawkConfigManager.XmlResource(file = "xml/pushsurvey.xml")
public class PushSurveyCfg extends HawkConfigBase {
	@Id
	protected final int id;

	// 推送方式 1 触发推送 2 后台推送
	protected final int pushType;

	// 筛选条件
	protected final String condition;

	// 展示位置 1 邮件 2 主界面
	protected final String pushShow;
	
	// 奖励
	protected final int award;

	// 有效期
	protected final int validityTime;

	// 是否开启
	protected final int isOpen;
	
	// 问卷进入提示
	protected final String enterTips;
	
	// 问卷链接
	protected final String url;
	
	// 问卷服务系统下对应的问卷ID
	protected final String sid;
	// 对应的渠道，1：微信，2:qq，0或其他值代表不限渠道
	protected final int channel;
	
	// 展示位置列表
	protected List<Integer> pushShowList = null;
	
	// 筛选条件列表
	protected List<int[]> conditionList;
	
	protected static Map<String, Integer> sid2SurveyIdMap = new HashMap<String, Integer>();

	public PushSurveyCfg() {
		id = 0;
		pushType = 0;
		condition = "";
		pushShow = "";
		award = 0;
		validityTime = 0;
		isOpen = 0;
		enterTips = "";
		url = "";
		sid = "";
		channel = 0;
		pushShowList = new ArrayList<>();
		conditionList = new ArrayList<>();
	}

	public int getId() {
		return id;
	}

	public int getPushType() {
		return pushType;
	}

	public String getCondition() {
		return condition;
	}

	public String getPushShow() {
		return pushShow;
	}

	public int getAward() {
		return award;
	}

	public int getValidityTime() {
		return validityTime;
	}
	
	/**
	 * 判定该问卷是否开启
	 * @return
	 */
	public boolean IsOpen() {
		return isOpen == 1;
	}
	
	public String getEnterTips() {
		return enterTips;
	}

	public String getUrl() {
		return url;
	}

	public List<Integer> getPushShowList() {
		return pushShowList;
	}

	public List<int[]> getConditionList() {
		return conditionList;
	}

	@Override
	protected boolean checkValid() {
		AwardCfg awardCfg = HawkConfigManager.getInstance().getConfigByKey(AwardCfg.class, award);
		if (awardCfg == null) {
			logger.error("PushSurveyCfg error, id : {}, awardId : {} error", id, award);
			return false;
		}
		return true;
	}
	
	@Override
	protected boolean assemble() {
		if (!HawkOSOperator.isEmptyString(sid)) {
			sid2SurveyIdMap.put(sid, id);
		}
		
		pushShowList = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(condition)) {
			String[] conditionStrs = condition.split(",");
			for (String conditionStr : conditionStrs) {
				String[] strs = conditionStr.split("_");
				int[] condition = new int[3];
				for (int i = 0; i < strs.length; i++) {
					condition[i] = Integer.parseInt(strs[i]);
				}
				conditionList.add(condition);
			}
		}
		pushShowList = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(pushShow)) {
			String[] pushShowStrs = pushShow.split(",");
			for (String pushShowStr : pushShowStrs) {
				pushShowList.add(Integer.parseInt(pushShowStr));
			}
		}
		return super.assemble();
	}
	
	public static int getSurveyId(String sid) {
		return sid2SurveyIdMap.getOrDefault(sid, 0);
	}
	
	public String getSid() {
		return sid;
	}
	public int getChannel() {
		return channel;
	}
}
