package com.hawk.game.config;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;
import org.hawk.tuple.HawkTuple2;

import com.hawk.game.item.ItemInfo;

/**
 * 联盟常量配置
 *
 * @author julia
 *
 */
@HawkConfigManager.KVResource(file = "xml/guild_const.xml")
public class GuildConstProperty extends HawkConfigBase {
	/**
	 * 设置一个单例对象, 以便访问
	 */
	private static GuildConstProperty instance = null;

	public static GuildConstProperty getInstance() {
		return instance;
	}

	// 礼物上限
	private final int allianceGiftUpLimit;
	// 礼物过期时间
	private final int allianceGiftDisappearTime;
	// 联盟名称最短最长（字符数）
	protected final String allianceNameMinMax;
	// 堡垒名称最短最长（字符数）
	protected final String manorNameMinMax;
	// 初始的联盟人数
	private final int initAlliancePeople;
	// 联盟公开招募花费
	private final String publicRecruitCost;
	// 花费金币创建联盟等级
	private final int createGuildCostGoldLevel;
	// 创建联盟花费金币
	private final int createGuildCostGold;
	// 创建默认人数上限
	private final int guildMemberNormalMaxNum;
	// 联盟缩写长度
	private final int guildTagLength;
	// 改变联盟名称
	private final int changeGuildNameGold;
	// 改变联盟旗帜
	private final int changeGuildFlagGold;
	// 改变联盟简称
	private final int changeGuildTagGold;
	// 联盟宣言长度
	private final int guildDeclarationLength;
	// 联盟留言字符限制
	private final int allianceLeaveMsgLenMax;
	// 联盟阶级名字限制
	private final String allianceMemberLevelNameLenMax;
	// 加入联盟时间间隔
	private final long allianceJoinCooldownTime;
	// 推荐联盟成员人数百分比
	private final int recommendAllianceMemberPercent;
	// 联盟留言保存条数
	private final int allianceLeaveMsgSave;
	// 联盟日志保存条数
	private final int allianceDiarySave;
	// 切换生效时间
	private final int guildManorActivationTime;
	//每个悬赏令赏金上限
	private final int beatbackMaxBouns;
	// 每人每日可领取的赏金上限
	private final int beatbackMaxDailyBouns;
	
	// 联盟名字最长最短值
	private int guildNameMin = 0;
	private int guildNameMax = 0;

	// 联盟堡垒名字最长最短值
	private int manorNameMin = 0;
	private int manorNameMax = 0;

	// 联盟成员阶级称谓最长最短值
	private int guildLvlNameMin = 0;

	private int guildLvlNameMax = 0;
	// 联盟商店购买记录上限
	private final int allianceStorePurchaseConfigLimit;

	// 宝藏自动刷新时间
	private final int refreshTime;

	// 资源捐献暴击权重
	private final String resourceCrit;
	// 宝藏挖掘次数上限
	private final int excavateNumber;

	// 宝藏挖掘恢复时间
	private final int excavateTime;

	// 宝藏帮助次数上限
	private final int helpNumber;

	// 宝藏帮助恢复时间
	private final int helpTime;

	// 宝藏付费挖掘消耗
	private final String excavateCost;

	// 宝藏限制时间
	private final int storehouseLimitTime;// = 60

	// 免费次数恢复时间
	private final int freeOfchargeTime;// = 720

	// 水晶捐献暴击权重
	private final String crystalCrit;

	// 资源捐献次数上限
	private final int resourceDonateNumber;

	// 资源捐献恢复时间
	private final int resourceDonateTime;

	// 水晶捐献消耗参数C0
	private final float donateParameter0;

	// 水晶捐献消耗参数C1
	private final float donateParameter1;

	// 水晶捐献消耗参数C2
	private final float donateParameter2;

	// 入盟捐献限制时间
	private final int donateLimitTime;

	// 捐献重置时间
	private final int donateRefreshTime;

	// 重置资源捐献次数消耗
	private final String donateResetCost;

	// 重置次数上限
	private final int donateResetLimit;
	
	// 首次加入联盟捐献次数
	private final int firstJoinAllianceDonateNumber;

	// 领地范围
	private final int manorRadius;

	// 建造速度参数1
	private final float buildSpeedparameter1;

	// 建造速度参数2
	private final float buildSpeedparameter2;

	// 摧毁速度参数1
	private final float destroySpeedparameter;

	// 联盟仓库存取行军速度
	private final int warehouseMarchSpeed;

	// 联盟矿采集速度
	private final int collectSpeedAddition;

	private final int resourceUpLimitAddition;

	private final int warehouseUpLimitAddition;

	private final int recommendUpLimit;

	private final int defaultBuildSpeed;
	
	// 礼物记录上限
	private final int allianceGiftRecordUpLimit;
	
	/** 前n天加入联盟的玩家会收到教学邮件*/
	private final int initDaySendMail;
	
	/** 超过xx天不登陆的玩家,不刷新联盟任务*/
	private final int taskNoRefreshDay;

	/** 资源捐献暴击等级列表 */
	private List<Integer> resourceCritLvls = new ArrayList<>();

	/** 资源捐献暴击权重 */
	private List<Integer> resourceCritWeights = new ArrayList<>();

	/** 钻石捐献暴击等级列表 */
	private List<Integer> crystalCritLvls = new ArrayList<>();

	/** 钻石捐献暴击权重 */
	private List<Integer> crystalCritWeights = new ArrayList<>();

	/** 公开招募消耗 */
	private ItemInfo publicRecruitConsume;

	/** 捐献次数重置消耗 */
	private ItemInfo donateResetConsume;

	/** 清理资源消耗 */
	protected final String clearResCost;

	/** 清理资源消耗 */
	private ItemInfo clearResConsume;

	/** 清理资源最大次数 */
	protected final int clearMaxNum;

	/** 查找联盟显示的数量 */
	protected final int searchAllianceNumber;

	/** 查找查找玩家名显示的数量 */
	protected final int searchPlayerNumber;

	/** 联盟邀请函每天收到的上限 */
	private final int allianceInvitationNum;

	/** 收到邀请函后，多少秒内不会再收取到 */
	private final int allianceInvitationCD;

	/** 收到邀请函的实际，登录后的x秒后 */
	private final String allianceInvitationTime;

	/** 大本等级高于配置，才会收到邀请函 */
	private final int allianceInvitationCityLevel;
	// # 首次固定刷新宝藏
	private final String firstStorehouse;// = 112_121_111
	
	/**手动刷新x 次后品质+1*/
	private final int storehouseLevelUpTimes;
	
	/** 邀请入盟、邀请迁城CD 单位 :s*/
	private final int inviteMailCd;
	
	/** 加入联盟成功邮件发送奖励次数*/
	private final int joinAllianceMailNum;

	/** 联盟堡垒解锁邮件发送奖励次数*/
	private final int allianceManorUnlockMailNum;

	/** 加入联盟成功邮件发送奖励*/
	private final String joinAllianceMailAward;

	/** 联盟堡垒解锁邮件发送奖励*/
	private final String allianceManorUnlockMailAward;

	/** 联盟坐标指引奖励*/
	private final String guildPointGuideAward;
	
	/** 联盟标记说明字符长度(字符数)*/
	private final int allianceSignExplainLen;
	
	/** 盟主取代功能时间(4阶以上可取代)*/
	private final int leaderReplaceTime1;
	
	/** 盟主取代功能时间(所有成员可取代)*/
	private final int leaderReplaceTime2;
	
	/** 取代盟主所需费用*/
	private final String leaderReplaceCost;
	
	/** 联盟坐标指引奖励*/
	private List<ItemInfo> guildPointGuideAwardList;
	
	/** 邀请函延迟显示时间区间 */
	private HawkTuple2<Integer, Integer> invitationRange;
	
	/** 取代盟主消耗 */
	private ItemInfo leaderReplaceConsume;
	
	/** 盟主离线多久算死盟 **/
	private final int guildLeaderLogoutTime;
	/** 玩家离线多久算死号 **/
	private final int guildMemberLogoutTime;
	/** 死号比例达到多少算死盟 **/
	private final float guildDeadMemberPersent;
	/** 死盟检测时间 **/
	private final int deadGuildCheckInterval;
	/** 死盟延迟检测时间 **/
	private final int deadGuildCheckDelay;
	/** 推荐联盟的满员率 **/
	private final float guildRecommendPersent;
	/** 联盟推荐玩家扩大到离线多久的玩家 **/
	private final int guildRecommendLogoutTime;
	/** 死盟玩家最多拒绝多少次，再也不推荐联盟 **/
	private final int deadGuildMemberRecommendMaxCnt;
	/** 推荐活盟至少有多少人才优先推荐 **/
	private final int recommendMinGuildMember;
	
	/** 联盟随机任务单次随机任务数*/
	private final int allianceRandomTaskNo;

	/** 联盟任务防刷时间(秒)*/
	private final int allianceTaskCD;

	/**
	 * 被邀请入盟vip限制
	 */
	private final int beInviteLevelLimit;
	
	/**
	 * 被邀请入盟CD
	 */
	private final int beInviteVipCD;
	
	public GuildConstProperty() {
		instance = this;
		allianceGiftUpLimit = 0;
		allianceGiftDisappearTime = 0;
		storehouseLimitTime = 0;
		firstStorehouse = "";
		// 免费次数恢复时间
		freeOfchargeTime = 0;

		allianceNameMinMax = "";
		initAlliancePeople = 0;
		manorNameMinMax = "";
		publicRecruitCost = "";
		createGuildCostGoldLevel = 0;
		createGuildCostGold = 0;
		guildMemberNormalMaxNum = 0;
		guildTagLength = 0;
		changeGuildNameGold = 0;
		changeGuildFlagGold = 0;
		changeGuildTagGold = 0;
		guildDeclarationLength = 0;
		allianceLeaveMsgLenMax = 0;
		allianceMemberLevelNameLenMax = "";
		allianceJoinCooldownTime = 0;
		recommendAllianceMemberPercent = 0;
		allianceLeaveMsgSave = 0;
		allianceDiarySave = 0;
		allianceStorePurchaseConfigLimit = 0;
		guildManorActivationTime = 0;
		refreshTime = 0;
		excavateNumber = 0;
		excavateTime = 0;
		helpNumber = 0;
		helpTime = 0;
		excavateCost = "";

		resourceCrit = "";
		crystalCrit = "";
		resourceDonateNumber = 0;
		resourceDonateTime = 60;
		donateParameter0 = 0;
		donateParameter1 = 0;
		donateParameter2 = 0;
		donateLimitTime = 0;
		donateRefreshTime = 0;
		donateResetCost = "";
		donateResetLimit = 0;
		firstJoinAllianceDonateNumber = 0;
		manorRadius = 0;
		buildSpeedparameter1 = 0.0f;
		buildSpeedparameter2 = 0.0f;
		destroySpeedparameter = 0.0f;
		warehouseMarchSpeed = 0;
		collectSpeedAddition = 0;
		resourceUpLimitAddition = 0;
		warehouseUpLimitAddition = 0;
		recommendUpLimit = 0;
		defaultBuildSpeed = 0;
		clearMaxNum = 0;
		clearResCost = "";
		searchAllianceNumber = 0;
		searchPlayerNumber = 0;
		allianceInvitationNum = 0;
		allianceInvitationCD = 0;
		allianceInvitationTime = "";
		allianceInvitationCityLevel = 0;
		allianceGiftRecordUpLimit= 0;
		storehouseLevelUpTimes = 0;
		initDaySendMail = 0;
		inviteMailCd = 0;
		joinAllianceMailNum = 0;
		allianceManorUnlockMailNum = 0;
		joinAllianceMailAward = "";
		allianceManorUnlockMailAward = "";
		guildPointGuideAward = "";
		allianceSignExplainLen = 0;
		
		leaderReplaceTime1 = Integer.MAX_VALUE;
		leaderReplaceTime2 = Integer.MAX_VALUE;
		leaderReplaceCost = "";
		
		guildLeaderLogoutTime = 0;
		guildMemberLogoutTime = 0;
		guildDeadMemberPersent = 0.0f;
		deadGuildCheckInterval = 0;
		deadGuildCheckDelay = 0;
		guildRecommendPersent = 0.0f;
		guildRecommendLogoutTime = 0;
		deadGuildMemberRecommendMaxCnt = 0;
		recommendMinGuildMember = 0;
		allianceRandomTaskNo = 0;
		allianceTaskCD = 0;
		beatbackMaxBouns = 200;
		beatbackMaxDailyBouns = 200;
		taskNoRefreshDay = 30;
		beInviteLevelLimit = 0;
		beInviteVipCD = 0;
	}

	public String getExcavateCost() {
		return excavateCost;
	}

	public String getAllianceNameMinMax() {
		return allianceNameMinMax;
	}

	public int getGuildDeclarationLength() {
		return guildDeclarationLength;
	}

	public int getCreateGuildCostGold() {
		return createGuildCostGold;
	}

	public int getAllianceStorePurchaseConfigLimit() {
		return allianceStorePurchaseConfigLimit;
	}

	public int getCreateGuildCostGoldLevel() {
		return createGuildCostGoldLevel;
	}

	public int getGuildMemberNormalMaxNum() {
		return guildMemberNormalMaxNum;
	}

	public int getGuildTagLength() {
		return guildTagLength;
	}

	public int getChangeGuildNameGold() {
		return changeGuildNameGold;
	}

	public int getChangeGuildFlagGold() {
		return changeGuildFlagGold;
	}

	public int getChangeGuildTagGold() {
		return changeGuildTagGold;
	}

	public ItemInfo getPublicRecruitCost() {
		return publicRecruitConsume;
	}

	public int getAllianceDiarySave() {
		return allianceDiarySave;
	}

	public long getAllianceJoinCooldownTime() {
		return allianceJoinCooldownTime * 1000;
	}

	public int getAllianceLeaveMsgLenMax() {
		return allianceLeaveMsgLenMax;
	}

	public int getAllianceLeaveMsgSave() {
		return allianceLeaveMsgSave;
	}

	public String getAllianceMemberLevelNameLenMax() {
		return allianceMemberLevelNameLenMax;
	}

	public float getRecommendAllianceMemberPercent() {
		return recommendAllianceMemberPercent / 100f;
	}

	public int getGuildNameMin() {
		return guildNameMin;
	}

	public int getGuildNameMax() {
		return guildNameMax;
	}

	public int getManorNameMin() {
		return manorNameMin;
	}

	public int getManorNameMax() {
		return manorNameMax;
	}

	public int getGuildLvlNameMin() {
		return guildLvlNameMin;
	}

	public int getGuildLvlNameMax() {
		return guildLvlNameMax;
	}

	public String getResourceCrit() {
		return resourceCrit;
	}

	public int getRefreshTime() {
		return refreshTime;
	}

	public int getExcavateNumber() {
		return excavateNumber;
	}

	public int getExcavateTime() {
		return excavateTime;
	}

	public int getHelpNumber() {
		return helpNumber;
	}

	public int getHelpTime() {
		return helpTime;
	}

	public String getCrystalCrit() {
		return crystalCrit;
	}

	public int getResourceDonateNumber() {
		return resourceDonateNumber;
	}

	public int getResourceDonateTime() {
		return resourceDonateTime;
	}

	public float getDonateParameter0() {
		return donateParameter0;
	}

	public float getDonateParameter1() {
		return donateParameter1;
	}

	public float getDonateParameter2() {
		return donateParameter2;
	}

	public int getDonateLimitTime() {
		return donateLimitTime;
	}

	public int getDonateRefreshTime() {
		return donateRefreshTime;
	}

	public int getManorRadius() {
		return manorRadius;
	}

	public float getBuildSpeedparameter1() {
		return buildSpeedparameter1;
	}

	public float getBuildSpeedparameter2() {
		return buildSpeedparameter2;
	}

	public float getDestroySpeedparameter() {
		return destroySpeedparameter;
	}

	public List<Integer> getResourceCritLvls() {
		return resourceCritLvls;
	}

	public List<Integer> getResourceCritWeights() {
		return resourceCritWeights;
	}

	public int getDonateResetLimit() {
		return donateResetLimit;
	}
	
	public int getFirstJoinAllianceDonateNumber() {
		return firstJoinAllianceDonateNumber;
	}

	public ItemInfo getDonateResetConsume() {
		return donateResetConsume;
	}

	public List<Integer> getCrystalCritLvls() {
		return crystalCritLvls;
	}

	public List<Integer> getCrystalCritWeights() {
		return crystalCritWeights;
	}

	public int getStorehouseLimitTime() {
		return storehouseLimitTime;
	}

	public int getFreeOfchargeTime() {
		return freeOfchargeTime;
	}

	public int getWarehouseMarchSpeed() {
		return warehouseMarchSpeed;
	}

	public int getCollectSpeedAddition() {
		return collectSpeedAddition;
	}

	public int getResourceUpLimitAddition() {
		return resourceUpLimitAddition;
	}

	public int getWarehouseUpLimitAddition() {
		return warehouseUpLimitAddition;
	}

	public int getRecommendUpLimit() {
		return recommendUpLimit;
	}

	public int getDefaultBuildSpeed() {
		return defaultBuildSpeed;
	}

	public String getClearResCost() {
		return clearResCost;
	}

	public int getClearMaxNum() {
		return clearMaxNum;
	}

	public ItemInfo getClearResConsume() {
		return clearResConsume;
	}

	public int getSearchAllianceNumber() {
		return searchAllianceNumber;
	}

	public int getSearchPlayerNumber() {
		return searchPlayerNumber;
	}

	public int getAllianceInvitationNum() {
		return allianceInvitationNum;
	}

	public int getAllianceInvitationCD() {
		return allianceInvitationCD;
	}

	public int getAllianceInvitationCityLevel() {
		return allianceInvitationCityLevel;
	}

	public HawkTuple2<Integer, Integer> getInvitationRange() {
		return invitationRange;
	}
	
	public int getAllianceGiftRecordUpLimit() {
		return allianceGiftRecordUpLimit;
	}

	@Override
	protected boolean checkValid() {
		if(leaderReplaceTime2 < leaderReplaceTime1){
			return false;
		}
		return super.checkValid();
	}

	@Override
	protected boolean assemble() {
		// 联盟名字最大最小值
		if (!HawkOSOperator.isEmptyString(allianceNameMinMax)) {
			String[] strs = allianceNameMinMax.split("_");
			guildNameMin = Integer.parseInt(strs[0]);
			guildNameMax = Integer.parseInt(strs[1]);
		}

		// 堡垒名称长度限制
		if (!HawkOSOperator.isEmptyString(manorNameMinMax)) {
			String[] strs = manorNameMinMax.split("_");
			manorNameMin = Integer.parseInt(strs[0]);
			manorNameMax = Integer.parseInt(strs[1]);
		}

		// 联盟阶级称谓长度限制
		if (!HawkOSOperator.isEmptyString(allianceMemberLevelNameLenMax)) {
			String[] strs = allianceMemberLevelNameLenMax.split("_");
			guildLvlNameMin = Integer.parseInt(strs[0]);
			guildLvlNameMax = Integer.parseInt(strs[1]);
		}

		// 资源捐献暴击权重
		if (!HawkOSOperator.isEmptyString(resourceCrit)) {
			resourceCritLvls = new ArrayList<>();
			resourceCritWeights = new ArrayList<>();
			String[] strs = resourceCrit.split(",");
			for (String str : strs) {
				String[] vals = str.split("_");
				resourceCritLvls.add(Integer.valueOf(vals[0]));
				resourceCritWeights.add(Integer.valueOf(vals[1]));
			}
		}

		// 钻石捐献暴击权重
		if (!HawkOSOperator.isEmptyString(crystalCrit)) {
			crystalCritLvls = new ArrayList<>();
			crystalCritWeights = new ArrayList<>();
			String[] strs = crystalCrit.split(",");
			for (String str : strs) {
				String[] vals = str.split("_");
				crystalCritLvls.add(Integer.valueOf(vals[0]));
				crystalCritWeights.add(Integer.valueOf(vals[1]));
			}
		}

		// 公开招募消耗
		if (!HawkOSOperator.isEmptyString(publicRecruitCost)) {
			publicRecruitConsume = new ItemInfo();
			if (!publicRecruitConsume.init(publicRecruitCost)) {
				logger.error("guild_const.xml assemble error ,publicRecruitCost : {}", publicRecruitCost);
				return false;
			}
		}

		// 捐献次数重置消耗
		if (!HawkOSOperator.isEmptyString(donateResetCost)) {
			donateResetConsume = new ItemInfo();
			if (!donateResetConsume.init(donateResetCost)) {
				logger.error("guild_const.xml assemble error ,donateResetCost : {}", donateResetCost);
				return false;
			}
		}

		// 清除资源点消耗
		if (!HawkOSOperator.isEmptyString(clearResCost)) {
			clearResConsume = new ItemInfo();
			if (!clearResConsume.init(clearResCost)) {
				logger.error("guild_const.xml assemble error ,clearResCost : {}", clearResCost);
				return false;
			}
		}

		// 邀请函延迟显示时间区间
		invitationRange = new HawkTuple2<Integer, Integer>(0, 0);
		if (!HawkOSOperator.isEmptyString(allianceInvitationTime)) {
			String[] strs = allianceInvitationTime.split("_");
			int time1 = Integer.parseInt(strs[0]);
			int time2 = Integer.parseInt(strs[1]);
			if (time1 > time2) {
				logger.error("guild_const.xml assemble error ,allianceInvitationTime : {}", allianceInvitationTime);
				return false;
			}
			invitationRange = new HawkTuple2<Integer, Integer>(time1, time2);
		}
		
		List<ItemInfo> guildPointGuideAwardList = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(guildPointGuideAward)) {
			guildPointGuideAwardList = ItemInfo.valueListOf(guildPointGuideAward);
		}
		this.guildPointGuideAwardList = guildPointGuideAwardList;
		
		// 取代盟主消耗
		if (!HawkOSOperator.isEmptyString(leaderReplaceCost)) {
			leaderReplaceConsume = new ItemInfo();
			if (!leaderReplaceConsume.init(leaderReplaceCost)) {
				logger.error("guild_const.xml assemble error ,leaderReplaceCost : {}", leaderReplaceCost);
				return false;
			}
		}
		
		if(guildLeaderLogoutTime <= 0){
			logger.error("guild_const.xml assemble error, guildLeaderLogoutTime : {}", guildLeaderLogoutTime);
		}
		if(guildMemberLogoutTime <= 0){
			logger.error("guild_const.xml assemble error, guildMemberLogoutTime : {}", guildMemberLogoutTime);
		}
		if(guildDeadMemberPersent <= 0){
			logger.error("guild_const.xml assemble error, guildDeadMemberPersent : {}", guildDeadMemberPersent);
			return false;
		}
		if(guildRecommendPersent <= 0){
			logger.error("guild_const.xml assemble error, guildRecommendPersent : {}", guildRecommendPersent);
			return false;
		}
		if(deadGuildMemberRecommendMaxCnt <= 0){
			logger.error("guild_const.xml assemble error, deadGuildMemberRecommendMaxCnt : {}", deadGuildMemberRecommendMaxCnt);
			return false;
		}
		
		return super.assemble();
	}

	public int getGuildManorActivationTime() {
		return guildManorActivationTime;
	}

	public String getFirstStorehouse() {
		return firstStorehouse;
	}

	public int getInitAlliancePeople() {
		return initAlliancePeople;
	}

	public int getAllianceGiftUpLimit() {
		return allianceGiftUpLimit;
	}

	public int getAllianceGiftDisappearTime() {
		return allianceGiftDisappearTime;
	}

	public int getStorehouseLevelUpTimes() {
		return storehouseLevelUpTimes;
	}

	public int getInitDaySendMail() {
		return initDaySendMail;
	}

	public int getInviteMailCd() {
		return inviteMailCd;
	}

	public int getJoinAllianceMailNum() {
		return joinAllianceMailNum;
	}

	public int getAllianceManorUnlockMailNum() {
		return allianceManorUnlockMailNum;
	}

	public String getJoinAllianceMailAward() {
		return joinAllianceMailAward;
	}

	public String getAllianceManorUnlockMailAward() {
		return allianceManorUnlockMailAward;
	}

	public List<ItemInfo> getGuildPointGuideAwardList() {
		return guildPointGuideAwardList;
	}

	public int getAllianceSignExplainLen() {
		return allianceSignExplainLen;
	}

	public long getLeaderReplaceTime1() {
		return leaderReplaceTime1 * 1000l;
	}

	public long getLeaderReplaceTime2() {
		return leaderReplaceTime2 * 1000l;
	}

	public ItemInfo getLeaderReplaceConsume() {
		return leaderReplaceConsume;
	}
	
	public int getGuildLeaderLogoutTime() {
		return guildLeaderLogoutTime;
	}

	public int getGuildMemberLogoutTime() {
		return guildMemberLogoutTime;
	}

	public float getGuildDeadMemberPersent() {
		return guildDeadMemberPersent;
	}

	public int getDeadGuildCheckInterval() {
		return deadGuildCheckInterval;
	}

	public int getDeadGuildCheckDelay() {
		return deadGuildCheckDelay;
	}

	public float getGuildRecommendPersent() {
		return guildRecommendPersent;
	}

	public int getGuildRecommendLogoutTime() {
		return guildRecommendLogoutTime;
	}

	public int getDeadGuildMemberRecommendMaxCnt() {
		return deadGuildMemberRecommendMaxCnt;
	}

	public int getRecommendMinGuildMember() {
		return recommendMinGuildMember;
	}

	public long getAllianceTaskCD() {
		return allianceTaskCD * 1000l;
	}

	public int getAllianceRandomTaskNo() {
		return allianceRandomTaskNo;
	}

	public int getBeatbackMaxBouns() {
		return beatbackMaxBouns;
	}

	public int getBeatbackMaxDailyBouns() {
		return beatbackMaxDailyBouns;
	}

	public int getTaskNoRefreshDay() {
		return taskNoRefreshDay;
	}

	public int getBeInviteLevelLimit() {
		return beInviteLevelLimit;
	}

	public long getBeInviteVipCD() {
		return beInviteVipCD * 1000L;
	}
}
