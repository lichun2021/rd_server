package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.alibaba.fastjson.JSONObject;

/**
 * 客户端拉取配置
 * 
 * @author golden
 *
 */
@HawkConfigManager.KVResource(file = "cfg/control.cfg")
public class ControlProperty extends HawkConfigBase {
	/**
	 * 实例
	 */
	private static ControlProperty instance = null;
	/**
	 * 是否开启新手
	 */
	protected final boolean openNewBie;
	/**
	 * 控制release 包是否显示全量日志
	 */
	protected final boolean showFullLog;
	/**
	 * 前端推送控制
	 */
	protected final boolean clientPush;
	/**
	 * 发包后n秒开始转菊花
	 */
	protected final int showWaitingAniStart;
	/**
	 * 转菊花动画时长
	 */
	protected final int showWaitingAniDuration;
	/**
	 * 影子变量加密重置频率 (几次心跳)
	 */
	protected final int shadowChangeFrequency;
	/**
	 * 是否开启功能引导
	 */
	protected final boolean openFuncNewBie;
	/**
	 * 中端机是否显示特效
	 */
	protected final boolean showTerriEffect;
	/**
	 * ios设备包输出性能日志
	 */
	protected final boolean printPerform;
	/**
	 * 客户端行军控制
	 */
	protected final String marchHigh;
	/**
	 * 客户端行军控制
	 */
	protected final String marchMid;
	/**
	 * 客户端行军控制
	 */
	protected final String marchLow;
	/**
	 * cdkUrl
	 */
	protected final String cdkUrl;
	/**
	 * 世界帧率检测
	 */
	protected final boolean mapProfiler;
	/**
	 * 功能引导章节开关
	 */
	protected final String funcNewBieChapterClose;

	/**
	 * 信用分
	 */
	protected final int creditScore;// = 320
	/**
	 * 私聊大本等级
	 */
	protected final int privateChatNormalLv;// = 7
	/**
	 * 信用不足320时 大本等级
	 */
	protected final int privateChatStrictLv;// = 12
	/**
	 * 世界聊天大本等级
	 */
	protected final int chatNormalCityLv;// = 7
	/**
	 * 信用不足320 世界聊天大本等级
	 */
	protected final int chatStrictCityLv;// = 12
	/**
	 * 发言间隔
	 */
	protected final int chatInterval;// = 5
	/**
	 * 大喇叭使用等级
	 */
	protected final int broadcastCityLv;// = 7

	/**
	 * 信用分不足时加好友限制大本等级
	 */
	protected final int addFriendLv;// = 12;
	/**
	 * 每日最多加好友
	 */
	protected final int dayFriendsNum;// = 20
	
	/*
	 * 联盟排行榜开关
	 * */
	protected final int guildRankSwitch; // 1 trun on, else trun off
	
	/**
	 * ios屏蔽活动控制
	 */
	protected final String iosBanActivity;
	
	// 我要变强开关
	protected final boolean openStronger;
	/**
	 * 守护控制开关.
	 */
	protected final boolean guardSwitch;
	/**
	 * 二级密码开关
	 */
	protected final int secPasswordSwitch;
	/**
	 * 微券开关
	 */
	protected final int couponSwitch;
	/**
	 * 客户端包活推送开关
	 */
	protected final boolean pushSwitchFlag;
	/**
	 * 水印
	 */
	protected final boolean showWatermark;
	
	/**
	 * json格式配置
	 */
	private JSONObject controlCfg;

	/**
	 * 构造
	 */
	public ControlProperty() {
		openNewBie = true;
		showFullLog = false;
		clientPush = true;
		showWaitingAniStart = 0;
		showWaitingAniDuration = 0;
		shadowChangeFrequency = 0;
		openFuncNewBie = true;
		showTerriEffect = true;
		printPerform = true;
		marchHigh = "";
		marchMid = "";
		marchLow = "";
		cdkUrl = "";
		mapProfiler = true;
		funcNewBieChapterClose = "";
		creditScore = 320;
		privateChatNormalLv = 7;
		privateChatStrictLv = 12;
		chatNormalCityLv = 7;
		chatStrictCityLv = 12;
		chatInterval = 5;
		broadcastCityLv = 7;
		addFriendLv = 12;
		dayFriendsNum = 20;
		guildRankSwitch = 1;
		iosBanActivity = "";
		openStronger = false;
		guardSwitch = false;	
		secPasswordSwitch = 0;
		couponSwitch = 0;
		pushSwitchFlag = true;
		showWatermark = false;
	}
	
	/**
	 * 获取实例
	 * 
	 * @return
	 */
	public static ControlProperty getInstance() {
		return instance;
	}

	/**
	 * 获取配置数据 json格式
	 * 
	 * @return
	 */
	public JSONObject getControlCfg() {
		return (JSONObject) controlCfg.clone();
	}

	@Override
	protected boolean assemble() {
		JSONObject json = new JSONObject();
		json.put("openNewBie", this.openNewBie);
		json.put("showFullLog", this.showFullLog);
		json.put("clientPush", this.clientPush);
		json.put("showWaitingAniStart", this.showWaitingAniStart);
		json.put("showWaitingAniDuration", this.showWaitingAniDuration);
		json.put("shadowChangeFrequency", this.shadowChangeFrequency);
		json.put("openFuncNewBie", this.openFuncNewBie);
		json.put("showTerriEffect", this.showTerriEffect);
		json.put("printPerform", this.printPerform);
		json.put("marchHigh", this.marchHigh);
		json.put("marchMid", this.marchMid);
		json.put("marchLow", this.marchLow);
		json.put("cdkUrl", this.cdkUrl);
		json.put("mapProfiler", this.mapProfiler);
		json.put("funcNewBieChapterClose", this.funcNewBieChapterClose);
		json.put("creditScore", this.creditScore);
		json.put("privateChatNormalLv", this.privateChatNormalLv);
		json.put("privateChatStrictLv", this.privateChatStrictLv);
		json.put("chatNormalCityLv", this.chatNormalCityLv);
		json.put("chatStrictCityLv", this.chatStrictCityLv);
		json.put("chatInterval", this.chatInterval);
		json.put("broadcastCityLv", this.broadcastCityLv);
		json.put("addFriendLv", this.addFriendLv);
		json.put("dayFriendsNum", this.dayFriendsNum);
		json.put("guildRankSwitch", this.guildRankSwitch);
		json.put("iosBanActivity", this.iosBanActivity);
		json.put("openStronger", this.openStronger);
		json.put("guardSwitch", this.guardSwitch);
		json.put("secPasswordSwitch", this.secPasswordSwitch);
		json.put("couponSwitch", this.couponSwitch);
		json.put("pushSwitchFlag", this.pushSwitchFlag);
		json.put("showWatermark", this.showWatermark);
		controlCfg = json;
		
		instance = this;
		return true;
	}

	public int getDayFriendsNum() {
		return dayFriendsNum;
	}

	public boolean isOpenNewBie() {
		return openNewBie;
	}

	public boolean isShowFullLog() {
		return showFullLog;
	}

	public boolean isClientPush() {
		return clientPush;
	}

	public int getShowWaitingAniStart() {
		return showWaitingAniStart;
	}

	public int getShowWaitingAniDuration() {
		return showWaitingAniDuration;
	}

	public int getShadowChangeFrequency() {
		return shadowChangeFrequency;
	}

	public boolean isOpenFuncNewBie() {
		return openFuncNewBie;
	}

	public boolean isShowTerriEffect() {
		return showTerriEffect;
	}

	public boolean isPrintPerform() {
		return printPerform;
	}

	public String getMarchHigh() {
		return marchHigh;
	}

	public String getMarchMid() {
		return marchMid;
	}

	public String getMarchLow() {
		return marchLow;
	}

	public String getCdkUrl() {
		return cdkUrl;
	}

	public boolean isMapProfiler() {
		return mapProfiler;
	}

	public String getFuncNewBieChapterClose() {
		return funcNewBieChapterClose;
	}

	public int getCreditScore() {
		return creditScore;
	}

	public int getPrivateChatNormalLv() {
		return privateChatNormalLv;
	}

	public int getPrivateChatStrictLv() {
		return privateChatStrictLv;
	}

	public int getChatNormalCityLv() {
		return chatNormalCityLv;
	}

	public int getChatStrictCityLv() {
		return chatStrictCityLv;
	}

	public int getChatInterval() {
		return chatInterval;
	}

	public int getBroadcastCityLv() {
		return broadcastCityLv;
	}

	public int getAddFriendLv() {
		return addFriendLv;
	}

	public int getGuildrankSwitch(){
		return guildRankSwitch;
	}
	
	public static void setInstance(ControlProperty instance) {
		ControlProperty.instance = instance;
	}

	public void setControlCfg(JSONObject controlCfg) {
		this.controlCfg = controlCfg;
	}

	public boolean isGuardSwitch() {
		return guardSwitch;
	}
	
	public int getCouponSwitch() {
		return couponSwitch;
	}
}