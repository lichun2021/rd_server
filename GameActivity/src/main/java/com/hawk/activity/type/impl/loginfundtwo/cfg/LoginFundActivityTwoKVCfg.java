package com.hawk.activity.type.impl.loginfundtwo.cfg;

import com.hawk.gamelib.activity.ConfigChecker;
import com.hawk.serialize.string.SerializeHelper;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;

/**
 * 登录基金活动全局K-V配置
 * @author hf
 */
@HawkConfigManager.KVResource(file = "activity/login_fund_two/%s/loginfund_two_activity_cfg.xml", autoLoad=false, loadParams="285")
public class LoginFundActivityTwoKVCfg extends HawkConfigBase {
	
	/** 服务器开服延时开启活动时间*/
	private final int serverDelay;
	
	/** 可购买登录基金的vip等级限制*/
	private final String limitVipLevel;
	
	/** 购买价格*/
	private final String price;

	/**活动开启的服务器开服时间*/
	private final String openTimeBegin;
	/**活动截止的服务器开服时间*/
	private final String openTimeEnd;
	/**ios礼包ID*/
	private final String iosAdvance;
	/**android礼包ID*/
	private final String androidAdvance;
	/**开启大本等级限制*/
	private final int openCtiyLimt;
	/**各基金可购买大本等级限制*/
	private final String buyCtiyLimt;
	private final long resetTime;
	private long openTimeBeginValue;

	private long openTimeEndValue;

	private Map<Integer, String> iosAdvanceMap = new HashMap<>();

	private Map<Integer, String> androidAdvanceMap = new HashMap<>();

	private Map<Integer, Integer> buyCityLimitMap = new HashMap<>();

	private Map<Integer, Integer> limitVipLevelMap = new HashMap<>();


	public LoginFundActivityTwoKVCfg() {
		serverDelay = 0;
		limitVipLevel = "";
		price = "";
		openTimeBegin = "";
		openTimeEnd = "";
		iosAdvance ="";
		androidAdvance = "";
		openCtiyLimt = 0;
		buyCtiyLimt = "";
		resetTime = 0;
	}
	
	public long getServerDelay() {
		return serverDelay * 1000L;
	}

	public int getLimitVipLevelByType(int type) {
		return limitVipLevelMap.getOrDefault(type, 0);
	}

	public String getPrice() {
		return price;
	}

	public int getOpenCtiyLimt() {
		return openCtiyLimt;
	}

	@Override
	protected boolean assemble() {
		iosAdvanceMap = SerializeHelper.stringToMap(iosAdvance, Integer.class, String.class, SerializeHelper.ATTRIBUTE_SPLIT, SerializeHelper.BETWEEN_ITEMS);
		androidAdvanceMap = SerializeHelper.stringToMap(androidAdvance, Integer.class, String.class, SerializeHelper.ATTRIBUTE_SPLIT, SerializeHelper.BETWEEN_ITEMS);
		buyCityLimitMap = SerializeHelper.stringToMap(buyCtiyLimt, Integer.class, Integer.class, SerializeHelper.ATTRIBUTE_SPLIT, SerializeHelper.BETWEEN_ITEMS);
		limitVipLevelMap = SerializeHelper.stringToMap(limitVipLevel, Integer.class, Integer.class, SerializeHelper.ATTRIBUTE_SPLIT, SerializeHelper.BETWEEN_ITEMS);

		if (!HawkOSOperator.isEmptyString(openTimeBegin)) {
			openTimeBeginValue = HawkTime.parseTime(openTimeBegin);
		}
		if (!HawkOSOperator.isEmptyString(openTimeEnd)) {
			openTimeEndValue = HawkTime.parseTime(openTimeEnd);
		}
		return true;
	}

	@Override
	protected final boolean checkValid() {
		boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(price);
		if (!valid) {
			throw new InvalidParameterException(String.format("LoginFundActivityTwoKVCfg reward error, price: %s", price));
		}
		return super.checkValid();
	}

	public int getBuyCityLimitByType(int type) {
		return buyCityLimitMap.getOrDefault(type, 0);
	}

	public long getOpenTimeBeginValue() {
		return openTimeBeginValue;
	}

	public long getOpenTimeEndValue() {
		return openTimeEndValue;
	}

	/**
	 * 根据礼包获取类型
	 * @param giftId
	 * @return
	 */
	public int getBuyType(String giftId){
		for (Map.Entry<Integer, String> entryIos:iosAdvanceMap.entrySet()) {
			int type = entryIos.getKey();
			String id = entryIos.getValue();
			if (id.equals(giftId)){
				return type;
			}
		}
		for (Map.Entry<Integer, String> entryIos:androidAdvanceMap.entrySet()) {
			int type = entryIos.getKey();
			String id = entryIos.getValue();
			if (id.equals(giftId)){
				return type;
			}
		}
		return 0;
	}

	/**
	 * 礼包是否合法
	 * @param giftId
	 * @return
	 */
	public boolean isCheckValid(String giftId){
		boolean iosGift = iosAdvanceMap.values().contains(giftId);
		boolean androidsGift = androidAdvanceMap.values().contains(giftId);
		return iosGift || androidsGift;
	}

	public long getResetTime() {
		return resetTime * 1000l;
	}

}