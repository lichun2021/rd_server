package com.hawk.game.config;

import java.util.HashSet;
import java.util.Set;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

import com.hawk.game.util.GsConst;

/**
 * 推送消息配置
 *
 * @author david
 *
 */
@HawkConfigManager.XmlResource(file = "xml/push.xml")
public class PushCfg extends HawkConfigBase {
	@Id
	protected final int id;
	// 消息体外键
	protected final String text;
	// 分组信息
	protected final String key;
	// 消息有效时长
	protected final int usefulTime;
	// 免打扰控制
	protected final int freeofcontrol;
	// 免打扰时间段
	protected final String blockingpushTime;
	// 推送时间限制（同类type推送pushTime内只推送一条）
	protected final int pushServerTime;
	// 不受离线7天后不发推送的限制
	protected final int notControlledByTime;
	
	private static Set<String> groupSet = new HashSet<>();
	// 免打扰开始时间：一天内的整点数
	private static long blockingStart;
	// 免打扰结束时间： 一天内的整点数
	private static long blockingEnd;
	// 是否开启免打扰控制
	private static boolean blockingEnable;
	// 免打扰控制开关
	private static String blockSwitchKey;
	
	private static int pushTimePeriod;
	
	public PushCfg() {
		id = 0;
		text = "";
		key = "";
		usefulTime = 600; // 默认10分钟
		freeofcontrol = 0;
		blockingpushTime = "";
		pushServerTime = 0;
		notControlledByTime = 0;
	}

	public int getId() {
		return id;
	}

	public String getText() {
		return text;
	}

	public String getGroup() {
		return key;
	}
	
	public int getUsefulTime() {
		return usefulTime;
	}
	
	public int getFreeofcontrol() {
		return freeofcontrol;
	}
	
	public int getNotControlledByTime() {
		return notControlledByTime;
	}
	
	public boolean assemble() {
		groupSet.add(key.trim());
		
		if ("blockingpush".equals(key)) {
			blockSwitchKey = key;
			pushTimePeriod = pushServerTime;
			if (!HawkOSOperator.isEmptyString(blockingpushTime)) {
				String[] segment = blockingpushTime.split("_");
				if (segment.length >= 2) {
					blockingStart = Integer.valueOf(segment[0]) % 24 * GsConst.HOUR_MILLI_SECONDS;
					blockingEnd = Integer.valueOf(segment[1]) % 24 * GsConst.HOUR_MILLI_SECONDS;
					blockingEnable = blockingStart < blockingEnd;
				}
			}
		}
		
		return true;
	}
	
	public static boolean isPushGroupKey(String key) {
		return groupSet.contains(key.trim());
	}
	
	public static String getBlockingSwitchKey() {
		return blockSwitchKey;
	}
	
	public static boolean isBlockingEnable() {
		return blockingEnable;
	}
	
	/**
	 * 获取当天的免打扰开始时刻
	 * @return
	 */
	public static long getBlockingStartTime() {
		return HawkTime.getAM0Date().getTime() + blockingStart;
	}
	
	/**
	 * 获取当天的免打扰结束时刻
	 * @return
	 */
	public static long getBlockingEndTime() {
		return HawkTime.getAM0Date().getTime() + blockingEnd;
	}

	public static int getPushTimePeriod() {
		return pushTimePeriod;
	}

}
