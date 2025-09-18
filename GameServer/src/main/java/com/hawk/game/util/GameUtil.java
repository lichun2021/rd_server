package com.hawk.game.util;

import java.net.URLEncoder;
import java.security.InvalidParameterException;
import java.security.MessageDigest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.hawk.game.protocol.World.PresetMarchManhattan;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.eclipse.jetty.client.api.ContentResponse;
import org.hawk.app.HawkApp;
import org.hawk.callback.HawkCallback;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.cryption.HawkMd5;
import org.hawk.log.HawkLog;
import org.hawk.net.http.HawkHttpUrlService;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.task.HawkDelayTask;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;
import org.hawk.thread.HawkThreadPool;
import org.hawk.tuple.HawkTuple2;
import org.hawk.tuple.HawkTuple3;
import org.hawk.util.HawkNumberConvert;
import org.hawk.uuid.HawkUUIDGenerator;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.DressActiveEvent;
import com.hawk.activity.event.impl.SoldierNumChangeEvent;
import com.hawk.activity.event.impl.TrainSoldierCompleteEvent;
import com.hawk.activity.event.impl.UpgradeResourceProductorEvent;
import com.hawk.activity.event.impl.UseItemSpeedUpEvent;
import com.hawk.activity.type.impl.monthcard.MonthCardActivity;
import com.hawk.activity.type.impl.monthcard.entity.ActivityMonthCardEntity;
import com.hawk.common.AccountRoleInfo;
import com.hawk.common.ServerInfo;
import com.hawk.game.GsApp;
import com.hawk.game.GsConfig;
import com.hawk.game.battle.BattleService;
import com.hawk.game.cfgElement.ArmourEffObject;
import com.hawk.game.cfgElement.EffectObject;
import com.hawk.game.config.AllianceStorehouseCfg;
import com.hawk.game.config.AllianceStorehouseCharacterCfg;
import com.hawk.game.config.AppUrlCfg;
import com.hawk.game.config.ArmourAdditionalCfg;
import com.hawk.game.config.ArmourBreakthroughCfg;
import com.hawk.game.config.ArmourCfg;
import com.hawk.game.config.ArmourChargeLabCfg;
import com.hawk.game.config.ArmourConstCfg;
import com.hawk.game.config.ArmourLevelCfg;
import com.hawk.game.config.ArmourQuantumCfg;
import com.hawk.game.config.ArmourQuantumConsumeCfg;
import com.hawk.game.config.ArmourStarCfg;
import com.hawk.game.config.ArmourStarConsumeCfg;
import com.hawk.game.config.ArmourSuitCfg;
import com.hawk.game.config.BattleSoldierCfg;
import com.hawk.game.config.BuffCfg;
import com.hawk.game.config.BuildingCfg;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.config.ControlProperty;
import com.hawk.game.config.CustomKeyCfg;
import com.hawk.game.config.EffectCfg;
import com.hawk.game.config.EquipResearchCfg;
import com.hawk.game.config.EquipResearchLevelCfg;
import com.hawk.game.config.EquipResearchRewardCfg;
import com.hawk.game.config.GameConstCfg;
import com.hawk.game.config.GrayPuidCtrl;
import com.hawk.game.config.GuildConstProperty;
import com.hawk.game.config.ItemCfg;
import com.hawk.game.config.MilitaryRankCfg;
import com.hawk.game.config.OBPuidCtrl;
import com.hawk.game.config.RegisterPuidCtrl;
import com.hawk.game.config.SysFunctionCfg;
import com.hawk.game.config.TechnologyCfg;
import com.hawk.game.config.TlogPuidCtrl;
import com.hawk.game.config.VipCfg;
import com.hawk.game.config.WorldMapConstProperty;
import com.hawk.game.crossproxy.CrossService;
import com.hawk.game.data.ServerSettingData;
import com.hawk.game.entity.ArmourEntity;
import com.hawk.game.entity.ArmyEntity;
import com.hawk.game.entity.BuildingBaseEntity;
import com.hawk.game.entity.CustomDataEntity;
import com.hawk.game.entity.EquipResearchEntity;
import com.hawk.game.entity.GuildInfoObject;
import com.hawk.game.entity.GuildMemberObject;
import com.hawk.game.entity.QueueEntity;
import com.hawk.game.entity.StatusDataEntity;
import com.hawk.game.entity.TechnologyEntity;
import com.hawk.game.entity.item.DressItem;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.invoker.BanPersonalRankMsgInvoker;
import com.hawk.game.invoker.GuildDemiseLeaderInvoker;
import com.hawk.game.invoker.GuildDismissRpcInvoker;
import com.hawk.game.invoker.GuildKickMemberInvoker;
import com.hawk.game.invoker.PlayerAccountResetInvoker;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.lianmengjunyan.LMJYConst.PState;
import com.hawk.game.lianmengstarwars.SWConst.SWState;
import com.hawk.game.manager.AssembleDataManager;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.module.lianmengtaiboliya.TBLYConst.TBLYState;
import com.hawk.game.msg.PlayerImageFresh;
import com.hawk.game.msg.TimeLimitStoreTriggerMsg;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.president.PresidentOfficier;
import com.hawk.game.protocol.Activity.ActivityType;
import com.hawk.game.protocol.Armour.ArmourSuitType;
import com.hawk.game.protocol.Army.ArmySoldierPB;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.BuildingStatus;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.ItemType;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.protocol.Const.QueueStatus;
import com.hawk.game.protocol.Const.QueueType;
import com.hawk.game.protocol.Const.RankGroup;
import com.hawk.game.protocol.Const.SoldierType;
import com.hawk.game.protocol.Const.SpeedUpTimeWeightType;
import com.hawk.game.protocol.Login.HPLogin.Builder;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.MechaCore.MechaCoreSuitType;
import com.hawk.game.protocol.Player.LoginWay;
import com.hawk.game.protocol.Player.PlayerFlagPosition;
import com.hawk.game.protocol.President.OfficerType;
import com.hawk.game.protocol.Rank.RankInfo;
import com.hawk.game.protocol.Rank.RankType;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Talent.TalentType;
import com.hawk.game.protocol.World.PlayerPresetMarchInfo;
import com.hawk.game.protocol.World.PresetMarchInfo;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.queryentity.AccountInfo;
import com.hawk.game.rank.RankService;
import com.hawk.game.service.ArmyService;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.QQScoreBatch;
import com.hawk.game.service.SearchService;
import com.hawk.game.service.chat.ChatService;
import com.hawk.game.service.mssion.MissionManager;
import com.hawk.game.service.mssion.event.EventSoldierTrain;
import com.hawk.game.service.mssion.event.EventUpgradeResourceProductor;
import com.hawk.game.service.mssion.event.EventUseItemSpeed;
import com.hawk.game.service.starwars.StarWarsOfficerService;
import com.hawk.game.strengthenguide.StrengthenGuideManager;
import com.hawk.game.strengthenguide.msg.SGPlayerSoldierNumChangeMsg;
import com.hawk.game.util.GsConst.AccountPuidType;
import com.hawk.game.util.GsConst.ChangeContentType;
import com.hawk.game.util.GsConst.DailyResetUseTimesType;
import com.hawk.game.util.GsConst.DiamondPresentReason;
import com.hawk.game.util.GsConst.EffectType;
import com.hawk.game.util.GsConst.GuildOffice;
import com.hawk.game.util.GsConst.MissionState;
import com.hawk.game.util.GsConst.PriceType;
import com.hawk.game.util.GsConst.ScoreType;
import com.hawk.game.util.GsConst.SysFunctionContionType;
import com.hawk.game.util.GsConst.TimeLimitStoreTriggerType;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.service.WorldPlayerService;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.gamelog.GameLog;
import com.hawk.gamelog.LogParam;
import com.hawk.l5.L5Helper;
import com.hawk.l5.L5Task;
import com.hawk.log.Action;
import com.hawk.log.LogConst.Channel;
import com.hawk.log.LogConst.LogInfoType;
import com.hawk.log.LogConst.Platform;
import com.hawk.sdk.SDKConst.QQVipQueryType;
import com.hawk.sdk.SDKConst.UserType;

import redis.clients.jedis.Tuple;

import com.hawk.sdk.SDKManager;

/**
 * 游戏帮助类
 *
 * @author hawk
 */
public class GameUtil {

	/**
	 * 开服持续时间
	 * @return
	 */
	public static long getServerOpenDuration() {
		long hasOpenTime = HawkApp.getInstance().getCurrentTime() - GameUtil.getServerOpenTime();
		return hasOpenTime;
	}

	/**
	 * 可收取资源建筑当前储量
	 * @param buildCfg
	 * @param timeLong 产出资源的时长
	 * @return
	 */
	public static long resStore(Player player, String buidingId, BuildingCfg buildCfg, long timeLong) {
		double realOutputRate = getResOutputRate(player, buidingId, buildCfg);
		// 资源建筑最大储量
		double realOutputLimit = realOutputRate / buildCfg.getResPerHour() * buildCfg.getResLimit();
		// 产出量
		long product = (long) (timeLong * 1.0D / GsConst.HOUR_MILLI_SECONDS * realOutputRate);
		product = (long) Math.min(realOutputLimit, product);
		return product;
	}

	/**
	 * 获取资源产出速率
	 * @param player
	 * @param buidingId
	 * @param buildCfg
	 * @return
	 */
	public static double getResOutputRate(Player player, String buidingId, BuildingCfg buildCfg) {
		double allAddBuff = player.getData().getEffVal(EffType.RES_OUTPUT);
		double allAddBuffBoost = player.getData().getEffVal(EffType.RES_OUTPUT_BOOST);
		double allBackPrivilegeAddBuffBoost = player.getData().getEffVal(EffType.BACK_PRIVILEGE_RES_OUTPUT_BOOST);
		double territoryBoost = player.getData().getEffVal(EffType.RES_OUTPUT_TERRITORY_BOOST);
		double addBuff = 0d;
		double addBuffBoost = 0d;
		switch (buildCfg.getBuildType()) {
		case BuildingType.ORE_REFINING_PLANT_VALUE: {
			addBuff = player.getData().getEffVal(EffType.RES_OUTPUT_GOLDORE, buidingId);
			addBuffBoost = player.getData().getEffVal(EffType.RES_OUTPUT_GOLDORE_BOOST);
			break;
		}
		case BuildingType.OIL_WELL_VALUE: {
			addBuff = player.getData().getEffVal(EffType.RES_OUTPUT_OIL, buidingId);
			addBuffBoost = player.getData().getEffVal(EffType.RES_OUTPUT_OIL_BOOST);
			break;
		}
		case BuildingType.STEEL_PLANT_VALUE: {
			addBuff = player.getData().getEffVal(EffType.RES_OUTPUT_STEEL, buidingId);
			addBuffBoost = player.getData().getEffVal(EffType.RES_OUTPUT_STEEL_BOOST);
			break;
		}
		case BuildingType.RARE_EARTH_SMELTER_VALUE: {
			addBuff = player.getData().getEffVal(EffType.RES_OUTPUT_ALLOY, buidingId);
			addBuffBoost = player.getData().getEffVal(EffType.RES_OUTPUT_ALLOY_BOOST);
			break;
		}
		default:
			break;
		}

		// 产出速率
		int originalRate = buildCfg.getResPerHour();
		double realOutputRate = originalRate * (1 + (allAddBuff + addBuff) * GsConst.EFF_PER) * (1 + (allAddBuffBoost + addBuffBoost + allBackPrivilegeAddBuffBoost) * GsConst.EFF_PER)
				* (1 + territoryBoost * GsConst.EFF_PER);
		return realOutputRate;
	}

	/**
	 * 随机万分位概率
	 *
	 * @param refValue
	 * @return
	 */
	public static boolean randomProbability(int refValue) {
		return HawkRand.randInt(GsConst.RANDOM_BASE_VALUE, GsConst.RANDOM_MYRIABIT_BASE) <= refValue;
	}

	/**
	 * 随机万分位概率
	 * @return
	 */
	public static int randomProbability() {
		return HawkRand.randInt(GsConst.RANDOM_BASE_VALUE, GsConst.RANDOM_MYRIABIT_BASE);
	}

	/**
	 * 获取服务器的开服时间
	 * 
	 * @return
	 */
	public static long getServerOpenTime() {
		return GsApp.getInstance().getServerOpenTime();
	}
	
	/**开服天数*/
	public static int getServerOpenDay(){
		Calendar openServer = HawkTime.getCalendar(true);
		openServer.setTimeInMillis(GsApp.getInstance().getServerOpenTime());
		int day = HawkTime.calendarDiff(HawkTime.getCalendar(false), openServer);
		return day;
	}

	/**
	 * 获取服务器的开服天数
	 * 
	 * @return
	 */
	public static int getServerOpenDays() {
		if (!GsApp.getInstance().isServerOpened()) {
			return 0;
		}
		return (int) ((HawkTime.getMillisecond() - GsApp.getInstance().getServerOpenAM0Time()) / GsConst.DAY_MILLI_SECONDS + 1);
	}

	/**
	 * 判断是否为自定义key
	 * 
	 * @param key
	 * @return
	 */
	public static boolean isCustomKey(String key) {
		return HawkConfigManager.getInstance().getConfigByKey(CustomKeyCfg.class, key) != null;
	}

	/**
	 * 是否能堆叠
	 *
	 * @param itemType
	 * @return
	 */
	public static boolean itemCanOverlap(int itemType) {
		if (itemType >= GsConst.ITEM_TYPE_BASE) {
			itemType /= GsConst.ITEM_TYPE_BASE;
		}

		return itemType == Const.ItemType.PLAYER_ATTR_VALUE || itemType == Const.ItemType.TOOL_VALUE || itemType == Const.ItemType.SOLDIER_VALUE;
	}

	/**
	 * 转换到标准物品类型定义
	 *
	 * @param itemType
	 * @return
	 */
	public static int convertToStandardItemType(int itemType) {
		if (itemType >= GsConst.ITEM_TYPE_BASE) {
			return (itemType / GsConst.ITEM_TYPE_BASE) * GsConst.ITEM_TYPE_BASE;
		} else {
			return itemType * GsConst.ITEM_TYPE_BASE;
		}
	}

	/**
	 * 是否在新手保护期
	 * 
	 * @return
	 */
	public static boolean isInNewlyProtectPeriod() {
		long serverOpenTime = GameUtil.getServerOpenTime();
		long protectPeriod = ConstProperty.getInstance().getNewProtectTime() * 1000;
		if (GsApp.getInstance().getCurrentTime() <= serverOpenTime + protectPeriod) {
			return true;
		}
		return false;
	}

	/**
	 * 是否是刷精英怪的时间
	 * @return
	 */
	public static boolean isSuperMonsterActivityOpened() {
		Date date = HawkTime.getAM0Date();
		String am0Str = new SimpleDateFormat("yyyy-MM-dd").format(date);
		long now = HawkTime.getMillisecond();
		try {
			List<String[]> startEnds = WorldMapConstProperty.getInstance().getSuperEnemyRefreshTimes();
			for (String[] startEnd : startEnds) {
				String start = startEnd[0];
				String end = startEnd[1];
				long startTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(am0Str + " " + start).getTime();
				long endTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(am0Str + " " + end).getTime();
				// 已是第二天了
				if (endTime < startTime) {
					endTime += GsConst.DAY_MILLI_SECONDS;
				}
				if (startTime <= now && now < endTime) {
					return true;
				}
			}
		} catch (ParseException e) {
			HawkException.catchException(e);
		}
		return false;
	}

	/**
	 * 字符串过滤
	 * @param str
	 * @return
	 */
	public static String filterString(String value) {
		value = value.trim().replace("#", "*")
				.replace("&", "*")
				.replace(">", "*")
				.replace("%", "*")
				.replace("^", "*");

		return value;
	}

	/**
	 * 是否为USD支付渠道
	 * 
	 * @param channel
	 * @return
	 */
	public static boolean isUSDChannel(String channel) {
		return GameConstCfg.isUSDChannel(channel);
	}

	/**
	 * 队列加速
	 * @param time
	 * @param qtype
	 * @return
	 */
	public static int caculateTimeGold(long time, QueueType qtype, QueueStatus queueStatus) {
		SpeedUpTimeWeightType type;
		switch (qtype) {
		case BUILDING_DEFENER:
		case GUILD_SCIENCE_QUEUE:
		case NUCLEAR_MACHINE_QUEUE:
		case NUCLEAR_CREATE_QUEUE:
		case BUILDING_QUEUE: {
			type = SpeedUpTimeWeightType.TIME_WEIGHT_BUILDING;
			break;
		}
		case PLANT_ADVANCE_QUEUE:
		case TRAP_QUEUE:
		case SOLDIER_ADVANCE_QUEUE:
		case SOILDER_QUEUE: {
			type = SpeedUpTimeWeightType.TIME_WEIGHT_TRAINSOLDIER;
			break;
		}
		case CURE_QUEUE:
		case CURE_PLANT_QUEUE:{
			type = SpeedUpTimeWeightType.TIME_WEIGHT_CURESOLDIER;
			break;
		}
		case CROSS_TECH_QUEUE:
		case SCIENCE_QUEUE:
		case PLANT_SCIENCE_QUEUE:{
			type = SpeedUpTimeWeightType.TIME_WEIGHT_TECHNOLOGY;
			break;
		}
		case HERO_QUEUE: {
			if (QueueStatus.QUEUE_HERO_TRAIN == queueStatus) {
				type = SpeedUpTimeWeightType.TIME_WEIGHT_TRAINHERO;
				break;
			}
			type = SpeedUpTimeWeightType.TIME_WEIGHT_CUREHERO;
			break;
		}
		case EQUIP_QUEUE: {
			type = SpeedUpTimeWeightType.EQUIP_OPERATION;
			break;
		}
		case EQUIP_RESEARCH_QUEUE: {
			type = SpeedUpTimeWeightType.EQUIP_RESEARCH;
			break;
		}
		default: {
			throw new RuntimeException("Unkonw speed up type for queue ,type :" + qtype);
		}
		}

		return caculateTimeGold(time, type);
	}

	/**
	 * 计算时间金币
	 * @param time 时间(秒)
	 * @param type 权重类型
	 * @return
	 */
	public static int caculateTimeGold(long time, SpeedUpTimeWeightType type) {
		if (time <= 0) {
			return 0;
		}
		double W = 1;
		ConstProperty constPropertyInstance = ConstProperty.getInstance();
		switch (type) {
		case TIME_WEIGHT_BUILDING: {
			W = constPropertyInstance.getBuildingTimeWeight();
			break;
		}
		case TIME_WEIGHT_CUREHERO: {
			W = constPropertyInstance.getCureHeroTimeWeight();
			break;
		}
		case TIME_WEIGHT_TRAINHERO: {
			W = constPropertyInstance.getTrainHeroTimeWeight();
			break;
		}
		case TIME_WEIGHT_CURESOLDIER: {
			W = constPropertyInstance.getCureSoldierTimeWeight();
			break;
		}
		case TIME_WEIGHT_TRAINSOLDIER: {
			W = constPropertyInstance.getTrainSoldierTimeWeight();
			break;
		}
		case TIME_WEIGHT_TECHNOLOGY: {
			W = constPropertyInstance.getTechResearchTimeWeight();
			break;
		}
		case SPEEDUP_COEFFICIENT: {
			W = constPropertyInstance.getSpeedUpCoefficient();
			break;
		}
		case EQUIP_OPERATION: {
			W = constPropertyInstance.getEquipQueueTimeWeight();
			break;
		}

		case HERO_TRIAL: {
			W = constPropertyInstance.getHeroTrialQueueTimeWeight();
			break;
		}

		case EQUIP_RESEARCH: {
			W = constPropertyInstance.getEquipResearchQueueTimeWeight();
			break;
		}
		
		default: {
			throw new RuntimeException("Unknow time weight for speed up type : " + type.name());
		}
		}

		HawkTuple2<Long, Long> kb = constPropertyInstance.speedupKB((long) (time * W));
		double gold = (kb.first * (time * W) + kb.second) / 1000000;

		return (int) Math.floor(gold);
	}

	/**
	 * 计算资源金币
	 * @param resType 资源类型
	 * @param resCnt 资源多少
	 * @param cityLv 大本等级
	 * @return
	 */
	public static int caculateResGold(Const.PlayerAttr resType, long resCnt) {
		if (resCnt <= 0) {
			return 0;
		}
		double W = 1;
		ConstProperty constPropertyInstance = ConstProperty.getInstance();
		switch (resType) {
		case GOLDORE:
		case GOLDORE_UNSAFE: {// # 黄金资源价值钻石的权重
			W = constPropertyInstance.getGoldResWeight();
			break;
		}
		case OIL:
		case OIL_UNSAFE: {// # 石油资源价值钻石的权重
			W = constPropertyInstance.getOilResWeight();
			break;
		}
		case TOMBARTHITE:
		case TOMBARTHITE_UNSAFE: {
			W = constPropertyInstance.getSoilResWeight();
			break;
		}
		case STEEL:
		case STEEL_UNSAFE: {// # 合金资源价值钻石的权重
			W = constPropertyInstance.getUraniumResWeight();
			break;
		}
		default: {
			throw new RuntimeException("Unknow res weight type : " + resType.name());
		}
		}
		HawkTuple2<Long, Long> kb = constPropertyInstance.buyResKB((long) (resCnt * W));

		double gold = (kb.first * (resCnt * W) + kb.second) / 1000000;
		return (int) Math.floor(gold);
	}

	/**
	 * 字符串只包括检测
	 * @param string
	 * @param regexType
	 * @return
	 */
	public static boolean stringOnlyContain(String string, int regexType, String extra) {
		return stringMatch(string, buildRegex(regexType, extra, true, true));
	}

	/**
	 * 字符串只包括检测
	 * @param string
	 * @param regexType
	 * @return
	 */
	public static boolean stringContian(String string, int regexType, String extra) {
		return stringMatch(string, buildRegex(regexType, extra, true, false));
	}

	/**
	 * 字符串正则检测
	 * @param string
	 * @param regex
	 * @return
	 */
	public static boolean stringMatch(String string, String regex) {
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(string);
		return m.matches();
	}

	/**
	 * 构建正则
	 * 
	 * @param regexType
	 * @param extra
	 * @param isContian
	 * @param isOnly
	 * @return
	 */
	private static String buildRegex(int regexType, String extra, boolean isContian, boolean isOnly) {
		String regex = "";
		String contianStart = "[";
		String contianEnd = "]";
		String onlyStart = "^";
		String onlyEnd = "*$";
		if (isOnly) {
			regex += onlyStart;
		}
		if (isContian) {
			regex += contianStart;
		}
		if ((regexType & GsConst.RegexType.NUM) == GsConst.RegexType.NUM)
			regex += "0-9";
		if ((regexType & GsConst.RegexType.UPPERLETTER) == GsConst.RegexType.UPPERLETTER)
			regex += "A-Z";
		if ((regexType & GsConst.RegexType.LOWERLETTER) == GsConst.RegexType.LOWERLETTER)
			regex += "a-z";
		if ((regexType & GsConst.RegexType.SPACE) == GsConst.RegexType.SPACE)
			regex += " ";
		if ((regexType & GsConst.RegexType.CHINESELETTER) == GsConst.RegexType.CHINESELETTER)
			regex += "\u4e00-\u9fa5";

		if (extra != null) {
			regex += extra;
		}
		if (isContian) {
			regex += contianEnd;
		}
		if (isOnly) {
			regex += onlyEnd;
		}
		return regex;
	}

	/**
	 * 把int值拆分高16低16,index = 0 x;index = 1 y
	 * @param value
	 * @return
	 */
	public static int[] splitXAndY(int value) {
		return new int[] { (value & 0x0000ffff), (value >> 16) & 0x0000ffff };
	}

	/**
	 * 两个int分作高地位组合一个int值
	 * 
	 * @param  坐标y
	 * @param  坐标x
	 * @return
	 */
	public static Integer combineXAndYCacheIndex(int x, int y) {
		return WorldPointService.getInstance().getWorldIndex(x, y);
	}

	/**
	 * 两个int分作高地位组合一个int值
	 * 
	 * @param  坐标y
	 * @param  坐标x
	 * @return
	 */
	public static int combineXAndY(int x, int y) {
		return (y << 16) | x;
	}

	/**
	 * 把long值拆分高32低32,index = 0 x;index = 1 y
	 * @param value
	 * @return
	 */
	public static int[] splitFromAndTo(long value) {
		return new int[] { (int) (value & 0x00000000ffffffffL), (int) ((value >> 32) & 0x00000000ffffffffL) };
	}

	/**
	 * 两个int分作高地位组合一个long值
	 * 
	 * @param  坐标y
	 * @param  坐标x
	 * @return
	 */
	public static long combineFromAndTo(int x, int y) {
		return ((long) y << 32) | x;
	}

	/**
	 * 根据给定的浮动范围求一个数的近似值
	 * @param num
	 * @param randomRange
	 * @return
	 */
	public static int getProbablyNum(int num, double randomRange) {
		int randomResult = 0;
		int diff = (int) (num * randomRange);
		randomResult = num - diff + HawkRand.randInt(Math.max(diff * 2, 1));
		return randomResult;
	}

	/**
	 * n天做某事m次
	 * @param value
	 * @param n (连续的自然天，起点时第一次计数起)
	 * @param m (达到多少次)
	 * @return
	 */
	public static int addValue(int value, int n, int m) {
		int low = value & 0x0000ffff; // 低位次数
		int high = (value >> 16) & 0x0000ffff; // 高位时间

		int currentDay = HawkTime.getCalendar(false).get(Calendar.DAY_OF_YEAR);
		int diff = currentDay - high;
		// 跨年
		if (diff < 0) {
			Calendar today = HawkTime.getCalendar(true);
			Calendar cal = HawkTime.getCalendar(true);
			cal.set(Calendar.YEAR, today.get(Calendar.YEAR) - 1);
			cal.set(Calendar.DAY_OF_YEAR, high);
			diff = Math.abs(HawkTime.calendarDiff(cal, today));
		}

		if (diff < n
				|| (diff == n && HawkTime.getMillisecond() - HawkTime.getAM0Date().getTime() < ConstProperty.getInstance().getDailyMissionTime() * GsConst.HOUR_MILLI_SECONDS)) {
			low = m; // 时间内累计1次
		} else {
			low = 1; // 时间外，重新计数
			high = currentDay;
		}

		return (high << 16) | low;
	}

	/**
	 * 获取n天m次的次数
	 * @param value
	 * @param n
	 * @param m
	 * @return
	 */
	public static int getValue(int value, int n) {
		int low = value & 0x0000ffff; // 低位次数
		int high = (value >> 16) & 0x0000ffff; // 高位时间

		int currentDay = HawkTime.getCalendar(false).get(Calendar.DAY_OF_YEAR);
		int diff = currentDay - high;
		// 跨年
		if (diff < 0) {
			Calendar today = HawkTime.getCalendar(true);
			Calendar cal = HawkTime.getCalendar(true);
			cal.set(Calendar.YEAR, today.get(Calendar.YEAR) - 1);
			cal.set(Calendar.DAY_OF_YEAR, high);
			diff = Math.abs(HawkTime.calendarDiff(cal, today));
		}

		if (diff < n
				|| (diff == n && HawkTime.getMillisecond() - HawkTime.getAM0Date().getTime() < ConstProperty.getInstance().getDailyMissionTime() * GsConst.HOUR_MILLI_SECONDS)) {
			return low;
		} else {
			return 0;
		}
	}

	/**
	 * 生成玩家的查询json信息串
	 * 
	 * @param player
	 * @return
	 */
	public static JSONObject genSearchJson(Player player) {
		JSONObject info = new JSONObject();
		info.put("name", player.getName());

		// 保护状态
		if (player.getData().getCityShieldTime() > HawkTime.getMillisecond()) {
			info.put("protect", "true");
		} else {
			info.put("protect", "false");
		}

		info.put("level", player.getLevel());
		info.put("gold", player.getGold());
		info.put("diamonds", player.getDiamonds());
		info.put("fight", player.getPower());
		info.put("goldore", player.getGoldore());
		info.put("oil", player.getOil());
		info.put("steel", player.getSteel());
		info.put("tombarthite", player.getTombarthite());

		// 世界城点位置
		int[] posXY = WorldPlayerService.getInstance().getPlayerPosXY(player.getId());
		info.put("pos", String.format("%d,%d", posXY[0], posXY[1]));
		if (!HawkOSOperator.isEmptyString(player.getGuildName())) {
			info.put("guild", player.getGuildName());
		}

		// 守城部队数量
		int armyCount = 0;
		List<ArmyEntity> armyList = player.getData().getArmyEntities();
		for (ArmyEntity entity : armyList) {
			armyCount += entity.getFree();
		}
		info.put("army", armyCount);

		// 驻扎在外的部队(位置, 部队数)
		List<IWorldMarch> quarteredMarchs = WorldMarchService.getInstance().getPlayerMarch(player.getId(), WorldMarchType.ARMY_QUARTERED_VALUE);
		if (quarteredMarchs != null && quarteredMarchs.size() > 0) {
			JSONArray quarteredInfo = new JSONArray();
			for (IWorldMarch worldMarch : quarteredMarchs) {
				WorldMarch march = worldMarch.getMarchEntity();
				if (march.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED_VALUE) {
					JSONObject marchInfo = new JSONObject();
					posXY = GameUtil.splitXAndY(march.getTerminalId());
					marchInfo.put("pos", String.format("%d,%d", posXY[0], posXY[1]));
					armyCount = WorldUtil.getMarchArmyTotal(march);
					marchInfo.put("army", armyCount);
					quarteredInfo.add(marchInfo);
				}
			}
			info.put("quartered", quarteredInfo);
		}

		// 在外采集的部队(位置, 部队数)
		List<IWorldMarch> collectMarchs = WorldMarchService.getInstance().getPlayerMarch(player.getId(), WorldMarchType.COLLECT_RESOURCE_VALUE);
		if (collectMarchs != null && collectMarchs.size() > 0) {
			JSONArray collectInfo = new JSONArray();
			for (IWorldMarch worldMarch : collectMarchs) {
				WorldMarch march = worldMarch.getMarchEntity();
				if (march.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_COLLECT_VALUE) {
					JSONObject marchInfo = new JSONObject();
					posXY = GameUtil.splitXAndY(march.getTerminalId());
					marchInfo.put("pos", String.format("%d,%d", posXY[0], posXY[1]));
					armyCount = WorldUtil.getMarchArmyTotal(march);
					marchInfo.put("army", armyCount);
					collectInfo.add(marchInfo);
				}
			}
			info.put("collect", collectInfo);
		}

		return info;
	}

	/**
	 * 计算造兵需要的时间: 单位为秒
	 * @param armyCfg
	 * @param count
	 * @param speed
	 * @return
	 */
	public static long calTrainTime(Player player, BattleSoldierCfg armyCfg, int count, int speed) {
		return calTrainTime(player, armyCfg, count, speed, false);
	}

	/**
	 * 计算造兵需要的时间: 单位为秒
	 * @param armyCfg
	 * @param count
	 * @param speed
	 * @return
	 */
	public static long calTrainTime(Player player, BattleSoldierCfg armyCfg, int count, int speed, boolean isAdvance) {
		// 黑科技时间
		int black = player.getEffect().getEffVal(Const.EffType.EFF_1478) + player.getEffect().getEffVal(Const.EffType.EFF_522);
		// 训练加速作用加成
		int effectValue = player.getData().getEffVal(Const.EffType.CITY_SPD_ALL);
		effectValue += player.getData().getEffVal(Const.EffType.BACK_PRIVILEGE_CITY_SPD_SOLDIER);
		// 黑科技
		switch (armyCfg.getType()) {
		case Const.SoldierType.TANK_SOLDIER_1_VALUE:
			effectValue += player.getData().getEffVal(Const.EffType.TRAIN_SPEED_TANK_1);
			effectValue += player.getData().getEffVal(Const.EffType.CITY_SPD_TANK);
			break;
		case Const.SoldierType.TANK_SOLDIER_2_VALUE:
			effectValue += player.getData().getEffVal(Const.EffType.TRAIN_SPEED_TANK_2);
			effectValue += player.getData().getEffVal(Const.EffType.CITY_SPD_TANK);
			break;
		case Const.SoldierType.PLANE_SOLDIER_3_VALUE:
			effectValue += player.getData().getEffVal(Const.EffType.TRAIN_SPEED_PLANE_3);
			effectValue += player.getData().getEffVal(Const.EffType.CITY_SPD_PLANE);
			break;
		case Const.SoldierType.PLANE_SOLDIER_4_VALUE:
			effectValue += player.getData().getEffVal(Const.EffType.TRAIN_SPEED_PLANE_4);
			effectValue += player.getData().getEffVal(Const.EffType.CITY_SPD_PLANE);
			break;
		case Const.SoldierType.FOOT_SOLDIER_5_VALUE:
			effectValue += player.getData().getEffVal(Const.EffType.TRAIN_SPEED_FOOT_5);
			effectValue += player.getData().getEffVal(Const.EffType.CITY_SPD_FOOT);
			break;
		case Const.SoldierType.FOOT_SOLDIER_6_VALUE:
			effectValue += player.getData().getEffVal(Const.EffType.TRAIN_SPEED_FOOT_6);
			effectValue += player.getData().getEffVal(Const.EffType.CITY_SPD_FOOT);
			break;
		case Const.SoldierType.CANNON_SOLDIER_7_VALUE:
			effectValue += player.getData().getEffVal(Const.EffType.TRAIN_SPEED_CANNON_7);
			effectValue += player.getData().getEffVal(Const.EffType.CITY_SPD_CANNON);
			break;
		case Const.SoldierType.CANNON_SOLDIER_8_VALUE:
			effectValue += player.getData().getEffVal(Const.EffType.TRAIN_SPEED_CANNON_8);
			effectValue += player.getData().getEffVal(Const.EffType.CITY_SPD_CANNON);
			break;
		case Const.SoldierType.WEAPON_LANDMINE_101_VALUE:
			black = 0;
			effectValue = player.getData().getEffVal(Const.EffType.WAR_TRAP_SPEED);
			effectValue += player.getData().getEffVal(Const.EffType.PRODUCE_TRAP_SPEED_101);
			break;
		case Const.SoldierType.WEAPON_ACKACK_102_VALUE:
			black = 0;
			effectValue = player.getData().getEffVal(Const.EffType.WAR_TRAP_SPEED);
			effectValue += player.getData().getEffVal(Const.EffType.PRODUCE_TRAP_SPEED_102);
			break;
		case Const.SoldierType.WEAPON_ANTI_TANK_103_VALUE:
			// 战争堡垒不享受402作用号加成
			black = 0;
			effectValue = player.getData().getEffVal(Const.EffType.WAR_TRAP_SPEED);
			effectValue += player.getData().getEffVal(Const.EffType.PRODUCE_TRAP_SPEED_103);
			break;
		default:
			break;
		}

		// 等级士兵专用作用号
		int armyLevelEffectVal = 0;
		// 只对四个兵营生效，对城防工厂无效, 晋升无效
		if (!isAdvance && isSoldierBuildType(armyCfg.getBuilding())) {
			if (armyCfg.getLevel() <= 7) {
				armyLevelEffectVal = player.getData().getEffVal(Const.EffType.TRAIN_SPEED_PRE7_PER);
			} else if (armyCfg.getLevel() == 8) {
				armyLevelEffectVal = player.getData().getEffVal(Const.EffType.TRAIN_SPEED_EQ8_PER);
			} else if (armyCfg.getLevel() == 9) {
				armyLevelEffectVal = player.getData().getEffVal(Const.EffType.TRAIN_SPEED_EQ9_PER);
			} else if (armyCfg.getLevel() == 10) {
				armyLevelEffectVal = player.getData().getEffVal(Const.EffType.TRAIN_SPEED_EQ10_PER);
			}	
			if (armyCfg.getLevel() >= 10) {
				armyLevelEffectVal += player.getData().getEffVal(Const.EffType.TRAIN_ARMY11_SPEED_4025);
			}
			// 注：上述作用号仅对指定兵种生效，与其他加成作用号累加计算；即实际训练时间 = 基础时间/（1 + 作用值A/10000 + 本作用值/10000）
			// 注：【前端同学】需要在训练界面和晋升界面中展示对应训练或晋升时间
			if (armyCfg.getLevel() == 12) {
				if (armyCfg.getType() == Const.SoldierType.TANK_SOLDIER_1_VALUE) {
					armyLevelEffectVal += player.getData().getEffVal(Const.EffType.SOLDIER12_4028);
				}
				if (armyCfg.getType() == Const.SoldierType.PLANE_SOLDIER_3_VALUE) {
					armyLevelEffectVal += player.getData().getEffVal(Const.EffType.SOLDIER12_4029);
				}
				if (armyCfg.getType() == Const.SoldierType.FOOT_SOLDIER_5_VALUE) {
					armyLevelEffectVal += player.getData().getEffVal(Const.EffType.SOLDIER12_4030);
				}
				if (armyCfg.getType() == Const.SoldierType.CANNON_SOLDIER_7_VALUE) {
					armyLevelEffectVal += player.getData().getEffVal(Const.EffType.SOLDIER12_4031);
				}
			}
		}
		
		return (long) Math.ceil(
				armyCfg.getTime() * count * (1 - black * GsConst.EFF_PER) / (1 + speed * GsConst.EFF_PER + effectValue * GsConst.EFF_PER + armyLevelEffectVal * GsConst.EFF_PER));
	}

	/**
	 * 造兵资源消耗
	 * 
	 * @param armyCfg
	 * @param count
	 * @param immediate
	 * @param trainTime
	 * @param useGold
	 */
	public static List<ItemInfo> trainConsume(Player player, BattleSoldierCfg armyCfg, List<ItemInfo> itemInfos, int count, long trainTime, boolean immediate, boolean useGold,
			int hpCode) {
		// 资源消耗作用加成
		ConsumeItems consume = ConsumeItems.valueOf();
		consume.addConsumeInfo(itemInfos, immediate || useGold);

		// 立即训练
		if (immediate) {
			consume.addConsumeInfo(PlayerAttr.GOLD, GameUtil.caculateTimeGold(trainTime, SpeedUpTimeWeightType.TIME_WEIGHT_TRAINSOLDIER));
		}

		if (!consume.checkConsume(player, hpCode)) {
			return null;
		}

		AwardItems realCostItems = consume.consumeAndPush(player, Action.TRAIN_SOLDIER);
		return realCostItems.getAwardItems();
	}

	/**
	 * 训练士兵所需要资源
	 * @param player
	 * @param armyCfg
	 * @param count
	 * @return
	 */
	public static List<ItemInfo> trainCost(Player player, BattleSoldierCfg armyCfg, int count) {
		List<ItemInfo> resList = armyCfg.getResList();
		resList.forEach(item -> item.setCount(item.getCount()*count));
		// 资源消耗作用加成
		int[] goldArr = player.getEffect().getEffValArr(EffType.TRAIN_RES_4024, EffType.CITY_ARMY_TRAIN_REDUCE, EffType.ARMY_TRAIN_CONSUME_REDUCE, EffType.EFF_1473, EffType.TRAIN_GOLDORE_REDUCE_PER,
				EffType.EFF_1474);
		int[] oillArr = player.getEffect().getEffValArr(EffType.TRAIN_RES_4024, EffType.CITY_ARMY_TRAIN_REDUCE, EffType.ARMY_TRAIN_CONSUME_REDUCE, EffType.EFF_1473, EffType.TRAIN_OIL_REDUCE_PER,
				EffType.EFF_1475);
		int[] tombArr = player.getEffect().getEffValArr(EffType.TRAIN_RES_4024, EffType.CITY_ARMY_TRAIN_REDUCE, EffType.ARMY_TRAIN_CONSUME_REDUCE, EffType.EFF_1473, EffType.TRAIN_TOMBARTHITE_REDUCE_PER,
				EffType.EFF_1476);
		int[] stelArr = player.getEffect().getEffValArr(EffType.TRAIN_RES_4024, EffType.CITY_ARMY_TRAIN_REDUCE, EffType.ARMY_TRAIN_CONSUME_REDUCE, EffType.EFF_1473, EffType.TRAIN_STEEL_REDUCE_PER,
				EffType.EFF_1477);
		
		int[] medal = player.getEffect().getEffValArr(EffType.BLACK_TECH_367813);
		
		GameUtil.reduceByEffect(resList, goldArr, oillArr, tombArr, stelArr, medal);

		return resList;
	}

	/**
	 * 恢复伤兵消耗
	 * @return
	 */
	public static List<ItemInfo> cureItems(Player player, List<ArmySoldierPB> cureList) {
		if (player.getLmjyState() == PState.GAMEING || player.getTBLYState() == TBLYState.GAMEING) {
			return new ArrayList<>(0);
		}
		List<ItemInfo> itemInfos = soldierRecoverConsume(player, cureList, false);
		return itemInfos;
	}

	
	/**
	 * 恢复泰能伤兵消耗
	 * @return
	 */
	public static List<ItemInfo> curePlantItems(Player player, List<ArmySoldierPB> cureList) {
		if (player.getLmjyState() == PState.GAMEING || player.getTBLYState() == TBLYState.GAMEING) {
			return new ArrayList<>(0);
		}
		List<ItemInfo> itemInfos = plantSoldierRecoverConsume(player, cureList, false);
		return itemInfos;
	}
	
	
	/**
	 * 士兵恢复消耗
	 * 
	 * @param player
	 * @param soldierList
	 * @param firstAid
	 * @return
	 */
	public static List<ItemInfo> soldierRecoverConsume(Player player, List<ArmySoldierPB> soldierList, boolean firstAid) {
		Map<Integer, ItemInfo> coverResMap = new HashMap<Integer, ItemInfo>();
		for (ArmySoldierPB army : soldierList) {
			int armyId = army.getArmyId();
			int count = army.getCount();
			BattleSoldierCfg armyCfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, armyId);
			for (ItemInfo itemInfo : armyCfg.getResList()) {
				int itemId = itemInfo.getItemId();
				// recoverRes不再代表治疗伤兵需要消耗的具体资源数，而是一个比例系数，治疗伤兵消耗 = 造兵消耗 * 比例系数%
				int resCount = (int) Math.floor(itemInfo.getCount() * count * armyCfg.getRecoverRes(firstAid) * 0.01D);
				if (coverResMap.containsKey(itemId)) {
					ItemInfo item = coverResMap.get(itemId);
					item.setCount(item.getCount() + resCount);
					coverResMap.put(itemId, item);
				} else {
					ItemInfo item = new ItemInfo(itemInfo.getType(), itemId, resCount);
					coverResMap.put(itemId, item);
				}
			}
		}

		List<ItemInfo> itemInfos = new ArrayList<ItemInfo>(coverResMap.values());
		// 治疗伤兵资源消耗作用加成
		if (!firstAid) {
			int eff414 = player.getData().getEffVal(EffType.CITY_HURT_CRUT_REDUCE);
			int eff462 = player.getData().getEffVal(EffType.ARMY_RECOVER_CONSUME_REDUCE);
			int eff4026 = player.getSwState() == SWState.GAMEING ? player.getData().getEffVal(EffType.SW_4026) : 0;
			int eff339 = player.getData().getEffVal(EffType.EFF_339);
			for (ItemInfo itemInfo : itemInfos) {
				int effPer = player.getEffPerByResType(itemInfo.getItemId(), EffType.CURE_GOLDORE_REDUCE_PER,
						EffType.CURE_OIL_REDUCE_PER, EffType.CURE_TOMBARTHITE_REDUCE_PER, EffType.CURE_STEEL_REDUCE_PER);
				double count = itemInfo.getCount() 
						* (1 - effPer * GsConst.EFF_PER) 
						* (1 - eff414 * GsConst.EFF_PER) 
						* (1 - eff462 * GsConst.EFF_PER)
						* (1 - eff4026 * GsConst.EFF_PER)
						* (1 - eff339 * GsConst.EFF_PER);;
				int itemCount = (int) Math.ceil(count);
				itemInfo.setCount(Math.max(0, itemCount));
			}
		}
		
		return itemInfos;
	}
	
	
	/**
	 * 士兵恢复消耗
	 * 
	 * @param player
	 * @param soldierList
	 * @param firstAid
	 * @return
	 */
	public static List<ItemInfo> plantSoldierRecoverConsume(Player player, List<ArmySoldierPB> soldierList, boolean firstAid) {
		Map<Integer, ItemInfo> coverResMap = new HashMap<Integer, ItemInfo>();
		for (ArmySoldierPB army : soldierList) {
			int armyId = army.getArmyId();
			int count = army.getCount();
			BattleSoldierCfg armyCfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, armyId);
			List<ItemInfo> resList = ItemInfo.valueListOf(armyCfg.getPlantSoldierHealRes());
			for (ItemInfo item : resList) {
				int itemId = item.getItemId();
				long itemCount = item.getCount() * count;
				itemCount = (long) Math.ceil(itemCount / 1000d);
				if (coverResMap.containsKey(itemId)) {
					ItemInfo itemCoverRes = coverResMap.get(itemId);
					item.setCount(itemCoverRes.getCount() + itemCount);
					coverResMap.put(itemId, item);
				} else {
					ItemInfo itemCoverRes = new ItemInfo(item.getType(), itemId, itemCount);
					coverResMap.put(itemId, itemCoverRes);
				}
			}

		}

		List<ItemInfo> itemInfos = new ArrayList<ItemInfo>(coverResMap.values());
		// 治疗伤兵资源消耗作用加成
		if (!firstAid) {
			int eff4130 = player.getData().getEffVal(EffType.PLANT_SOLDIER_4130);
			for (ItemInfo itemInfo : itemInfos) {
				double count = itemInfo.getCount()
						* (1 - eff4130 * GsConst.EFF_PER);
				int itemCount = (int) Math.ceil(count);
				itemInfo.setCount(Math.max(0, itemCount));
			}
		}
		
		return itemInfos;
	}

	/**
	 * 恢复伤兵用时 (秒)
	 */
	public static double recoverTime(Player player, final List<ArmySoldierPB> cureList) {
		double recoverTime = 0d;
		int effPer = player.getData().getEffVal(Const.EffType.CITY_HURT_CRUT_SPD);
		for (ArmySoldierPB army : cureList) {
			int realEffPer = effPer + getCureSpeedEffPerByArmyId(player, army.getArmyId());
			BattleSoldierCfg armyCfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, army.getArmyId());
			// 治疗时间 = 基础治疗时间/(1+作用值/10000）
			recoverTime += armyCfg.getRecoverTime() * army.getCount() / (1 + realEffPer * GsConst.EFF_PER);
		}

		return recoverTime;
	}
	
	
	/**
	 * 恢复泰能伤兵用时 (秒)
	 */
	public static double plantRecoverTime(Player player, final List<ArmySoldierPB> cureList) {
		double recoverTime = 0d;
		for (ArmySoldierPB army : cureList) {
			BattleSoldierCfg armyCfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, army.getArmyId());
			int effPer = getCurePlantSpeedEffPerByArmyId(player, army.getArmyId());
			effPer += player.getData().getEffVal(Const.EffType.PLANT_SOLDIER_4127);
			// 治疗时间 = 基础治疗时间/(1+作用值/10000）
			recoverTime += armyCfg.getPlantSoldierHealTime() * army.getCount() /(1 + effPer * GsConst.EFF_PER) ;
		}
		return recoverTime;
	}

	/**
	 * 获取伤兵治疗加速
	 * @param player
	 * @param type
	 * @return
	 */
	public static int getCureSpeedEffPerByArmyId(Player player, int armyId) {
		int effectVal = 0;
		BattleSoldierCfg armyCfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, armyId);
		switch (armyCfg.getBuilding()) {
		case BuildingType.BARRACKS_VALUE:
			effectVal = player.getData().getEffVal(Const.EffType.CURE_FOOT_SPEED_PER);
			break;
		case BuildingType.WAR_FACTORY_VALUE:
			effectVal = player.getData().getEffVal(Const.EffType.CURE_TANK_SPEED_PER);
			break;
		case BuildingType.REMOTE_FIRE_FACTORY_VALUE:
			effectVal = player.getData().getEffVal(Const.EffType.CURE_CANNON_SPEED_PER);
			break;
		case BuildingType.AIR_FORCE_COMMAND_VALUE:
			effectVal = player.getData().getEffVal(Const.EffType.CURE_PLANE_SPEED_PER);
			break;
		default:
			break;
		}
		// 注：上述作用号仅对指定兵种生效，与其他加成作用号累加计算；即实际治疗时间 = 基础时间/（1 + 作用值A/10000 + 本作用值/10000）
		// 注：【前端同学】需要在伤兵治疗界面中展示对应治疗时间
		if (armyCfg.getLevel() >= 12) {
			if (armyCfg.getType() == Const.SoldierType.TANK_SOLDIER_1_VALUE) {
				effectVal += player.getData().getEffVal(Const.EffType.SOLDIER12_4032);
			}
			if (armyCfg.getType() == Const.SoldierType.PLANE_SOLDIER_3_VALUE) {
				effectVal += player.getData().getEffVal(Const.EffType.SOLDIER12_4033);
			}
			if (armyCfg.getType() == Const.SoldierType.FOOT_SOLDIER_5_VALUE) {
				effectVal += player.getData().getEffVal(Const.EffType.SOLDIER12_4034);
			}
			if (armyCfg.getType() == Const.SoldierType.CANNON_SOLDIER_7_VALUE) {
				effectVal += player.getData().getEffVal(Const.EffType.SOLDIER12_4035);
			}
		}

		return effectVal;
	}

	
	
	/**
	 * 获取伤兵治疗加速
	 * @param player
	 * @param type
	 * @return
	 */
	public static int getCurePlantSpeedEffPerByArmyId(Player player, int armyId) {
		int effectVal = 0;
		BattleSoldierCfg armyCfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, armyId);
		// 注：上述作用号仅对指定兵种生效，与其他加成作用号累加计算；即实际治疗时间 = 基础时间/（1 + 作用值A/10000 + 本作用值/10000）
		// 注：【前端同学】需要在伤兵治疗界面中展示对应治疗时间
		if (armyCfg.isPlantSoldier()) {
			if (armyCfg.getType() == Const.SoldierType.TANK_SOLDIER_1_VALUE) {
				effectVal += player.getData().getEffVal(Const.EffType.PLANT_SOLDIER_4103);
			}
			if (armyCfg.getType() == Const.SoldierType.TANK_SOLDIER_2_VALUE) {
				effectVal += player.getData().getEffVal(Const.EffType.PLANT_SOLDIER_4102);
			}
			if (armyCfg.getType() == Const.SoldierType.PLANE_SOLDIER_3_VALUE) {
				effectVal += player.getData().getEffVal(Const.EffType.PLANT_SOLDIER_4105);
			}
			if (armyCfg.getType() == Const.SoldierType.PLANE_SOLDIER_4_VALUE) {
				effectVal += player.getData().getEffVal(Const.EffType.PLANT_SOLDIER_4104);
			}
			if (armyCfg.getType() == Const.SoldierType.FOOT_SOLDIER_5_VALUE) {
				effectVal += player.getData().getEffVal(Const.EffType.PLANT_SOLDIER_4107);
			}
			if (armyCfg.getType() == Const.SoldierType.FOOT_SOLDIER_6_VALUE) {
				effectVal += player.getData().getEffVal(Const.EffType.PLANT_SOLDIER_4106);
			}
			if (armyCfg.getType() == Const.SoldierType.CANNON_SOLDIER_7_VALUE) {
				effectVal += player.getData().getEffVal(Const.EffType.PLANT_SOLDIER_4109);
			}
			if (armyCfg.getType() == Const.SoldierType.CANNON_SOLDIER_8_VALUE) {
				effectVal += player.getData().getEffVal(Const.EffType.PLANT_SOLDIER_4108);
			}
		}

		return effectVal;
	}
	
	
	
	/**
	 * 造兵完成任务刷新
	 * @param armyId
	 * @param count
	 */
	public static void soldierAddRefresh(Player player, int armyId, int count) {
		BattleSoldierCfg armyCfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, armyId);
		// 刷新周期性任务活动积分
		if (armyCfg != null) {
			ActivityManager.getInstance().postEvent(new TrainSoldierCompleteEvent(player.getId(), armyCfg.getType(), armyId, count, armyCfg.getLevel()));
			ActivityManager.getInstance().postEvent(new SoldierNumChangeEvent(player.getId(), armyId, count));
			MissionManager.getInstance().postMsg(player, new EventSoldierTrain(armyId, 0, count));
			// 我要变强
			StrengthenGuideManager.getInstance().postMsg(new SGPlayerSoldierNumChangeMsg(player));
			HawkApp.getInstance().postMsg(player, new TimeLimitStoreTriggerMsg(TimeLimitStoreTriggerType.SOLDIER_TRAIN, count));
		}
	}

	/**
	 * 获取玩家身上某兵种的士兵数量
	 * @param player
	 * @param armyId 为0时表示获取玩家身上所有兵种的士兵数量总和
	 * @return
	 */
	public static int getSoldierHaveNum(PlayerData playerData, int armyId) {
		ArmyEntity armyEntity = playerData.getArmyEntity(armyId);
		int actualNum = 0;
		if (armyEntity == null) {
			for (ArmyEntity army : playerData.getArmyEntities()) {
				actualNum += army.getCureCount() + army.getWoundedCount() + army.getFree() + army.getMarch();
			}
		} else {
			actualNum = armyEntity.getCureCount() + armyEntity.getWoundedCount() + armyEntity.getFree() + armyEntity.getMarch();
		}
		return actualNum;
	}

	/**
	 * 获取玩家完成某种科技研究的累计次数
	 * @param player
	 * @param techType 为0时表示获取玩家研究所有科技的总次数
	 * @return
	 */
	public static int getTechResearchTimes(Player player, int techType) {
		TechnologyEntity techEntity = player.getData().getTechEntityByTechId(techType);
		int actualNum = 0;
		if (techEntity == null) {
			for (TechnologyEntity technology : player.getData().getTechnologyEntities()) {
				actualNum += technology.getLevel();
			}
		} else {
			actualNum = techEntity.getLevel();
		}
		return actualNum;
	}

	/**
	 * 获取玩家身上某种科技的等级
	 * @param player
	 * @param techType
	 * @return
	 */
	public static int getTechLevel(Player player, int techType) {
		TechnologyEntity techEntity = player.getData().getTechEntityByTechId(techType);
		if (techEntity != null) {
			return techEntity.getLevel();
		}
		return 0;
	}

	/**
	 * 获取防御建筑的数量
	 * @param player
	 * @return
	 */
	public static int getDefenceBuildingTotal(Player player) {
		int total = 0;
		List<BuildingBaseEntity> prismList = player.getData().getBuildingListByType(BuildingType.PRISM_TOWER);
		if (prismList != null) {
			total += prismList.size();
		}
		List<BuildingBaseEntity> patriotList = player.getData().getBuildingListByType(BuildingType.PATRIOT_MISSILE);
		if (patriotList != null) {
			total += patriotList.size();
		}
		List<BuildingBaseEntity> pillboxList = player.getData().getBuildingListByType(BuildingType.PILLBOX);
		if (pillboxList != null) {
			total += pillboxList.size();
		}
		return total;
	}

	public static String getPlayerNameWithGuildTag(String guild, String playerName) {
		String guildTag = GuildService.getInstance().getGuildTag(guild);
		if (!HawkOSOperator.isEmptyString(guildTag)) {
			return "[" + guildTag + "]" + playerName;
		} else {
			return playerName;
		}
	}

	/**
	 * 修改建筑状态并通知客户端
	 * @param buildingType
	 * @param status
	 */
	public static void changeBuildingStatus(Player player, int buildingType, Const.BuildingStatus status) {
		changeBuildingStatus(player, buildingType, status, true);
	}

	/**
	 * 修改建筑状态并通知客户端
	 * @param buildingType
	 * @param status
	 * @param push
	 */
	public static void changeBuildingStatus(Player player, int buildingType, Const.BuildingStatus status, boolean push) {
		for (BuildingBaseEntity buildingEntity : player.getData().getBuildingListByType(BuildingType.valueOf(buildingType))) {
			int oldStatus = buildingEntity.getStatus();
			buildingEntity.setStatus(status.getNumber());
			if (push) {
				player.getPush().pushBuildingStatus(buildingEntity, status);
			}

			HawkLog.debugPrintln("change building status, playerId: {}, buildingUuid: {}, buildCfgId: {}, status before: {}, status after: {}",
					player.getId(), buildingEntity.getId(), buildingEntity.getBuildingCfgId(), oldStatus, status.getNumber());
		}
	}

	/**
	 * 获取建筑
	 * @param building
	 */
	public static BuildingStatus getBuildingStatus(Player player, BuildingBaseEntity building) {
		int buildType = building.getType();
		switch (buildType) {
		case BuildingType.HOSPITAL_STATION_VALUE: {
			if (ArmyService.getInstance().getCureFinishCount(player) > 0) {
				building.setStatus(BuildingStatus.CURE_FINISH_HARVEST_VALUE);
				break;
			}

			if (ArmyService.getInstance().getWoundedCount(player) > 0) {
				building.setStatus(BuildingStatus.SOLDIER_WOUNDED_VALUE);
				break;
			}

			building.setStatus(BuildingStatus.COMMON_VALUE);
			break;
		}
		
		case BuildingType.PLANT_HOSPITAL_VALUE: {
			if (ArmyService.getInstance().getPlantCureFinishCount(player) > 0) {
				building.setStatus(BuildingStatus.PLANT_CURE_FINISH_HARVEST_VALUE);
				break;
			}

			if (ArmyService.getInstance().getPlantWoundedCount(player) > 0) {
				building.setStatus(BuildingStatus.PLANT_SOLDIER_WOUNDED_VALUE);
				break;
			}

			building.setStatus(BuildingStatus.COMMON_VALUE);
			break;
		}

		case BuildingType.BARRACKS_VALUE:
		case BuildingType.WAR_FACTORY_VALUE:
		case BuildingType.AIR_FORCE_COMMAND_VALUE:
		case BuildingType.REMOTE_FIRE_FACTORY_VALUE:
		case BuildingType.WAR_FORTS_VALUE: {
			for (ArmyEntity entity : player.getData().getArmyEntities()) {
				BattleSoldierCfg armyCfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, entity.getArmyId());
				if (armyCfg.getBuilding() == buildType && entity.getTrainFinishCount() > 0) {
					building.setStatus(buildType == BuildingType.WAR_FORTS_VALUE ? BuildingStatus.TRAP_HARVEST_VALUE : BuildingStatus.SOILDER_HARVEST_VALUE);
				}
			}
			break;
		}

		default:
			break;
		}

		return BuildingStatus.valueOf(building.getStatus());
	}

	/**
	 * 判断兵种是否已解锁
	 * @param buildCfgId
	 * @param armyId
	 * @return
	 */
	public static boolean isSoldierUnlocked(Player player, int buildCfgId, int armyId) {
		BuildingCfg buildCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, buildCfgId);
		return isSoldierUnlocked(player, buildCfg, armyId);
	}

	public static boolean isSoldierUnlocked(Player player, BuildingCfg buildCfg, int armyId) {
		BattleSoldierCfg armyCfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, armyId);
		if (Objects.isNull(armyCfg)) {
			return false;
		}
		boolean result = buildCfg.getUnlockedSoldierIds().contains(armyId);
		switch (armyCfg.getSoldierType()) {
		case TANK_SOLDIER_1:
			if (armyCfg.getLevel() == 12) {
				result = player.getEffect().getEffVal(EffType.SOLDIER12_1589) > 0 && player.getEffect().getEffVal(EffType.SINCE_1130) > 0;
			}
			break;
		case TANK_SOLDIER_2:
			if (armyCfg.getLevel() == 11) {
				result = player.getEffect().getEffVal(EffType.SINCE_1130) > 0;
			}
			break;
		case PLANE_SOLDIER_3:
			if (armyCfg.getLevel() == 12) {
				result = player.getEffect().getEffVal(EffType.SOLDIER12_1590) > 0 && player.getEffect().getEffVal(EffType.SINCE_1131) > 0;
			}
			break;
		case PLANE_SOLDIER_4:
			if (armyCfg.getLevel() == 11) {
				result = player.getEffect().getEffVal(EffType.SINCE_1131) > 0;
			}
			break;
		case FOOT_SOLDIER_5:
			if (armyCfg.getLevel() == 12) {
				result = player.getEffect().getEffVal(EffType.SOLDIER12_1591) > 0 && player.getEffect().getEffVal(EffType.SINCE_1132) > 0;
			}
			break;
		case FOOT_SOLDIER_6:
			if (armyCfg.getLevel() == 11) {
				result = player.getEffect().getEffVal(EffType.SINCE_1132) > 0;
			}
			break;
		case CANNON_SOLDIER_7:
			if (armyCfg.getLevel() == 12) {
				result = player.getEffect().getEffVal(EffType.SOLDIER12_1592) > 0 && player.getEffect().getEffVal(EffType.SINCE_1133) > 0;
			}
			break;
		case CANNON_SOLDIER_8:
			if (armyCfg.getLevel() == 11) {
				result = player.getEffect().getEffVal(EffType.SINCE_1133) > 0;
			}
			break;

		default:
			break;
		}
		return result;
	}

	/**
	 * 判断是否是城内区块
	 */
	public static boolean isCityOutsideAreaBlock(int areaBlockId) {
		List<Integer> cityOutsideAreas = ConstProperty.getInstance().getCityOutsideAreas();
		if (cityOutsideAreas != null && cityOutsideAreas.contains(Integer.valueOf(areaBlockId))) {
			return true;
		}

		return false;
	}

	/**
	 * 获取玩家信息
	 * @param player
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static JSONObject gmGetAccountInfo(Player player) {
		JSONObject dataJson = new JSONObject();
		// 角色ID
		dataJson.put("RoleId", player.getId());
		// 角色名称
		dataJson.put("RoleName", player.getNameEncoded());
		// 当前等级
		dataJson.put("Level", player.getLevel());
		// 角色经验值
		dataJson.put("Exp", player.getExp());
		// 钻石数量
		dataJson.put("Diamond", player.getDiamonds());

		dataJson.put("GoldCount", player.getGoldore());
		dataJson.put("OilCount", player.getOil());
		dataJson.put("UraniumCount", player.getSteel());
		dataJson.put("TombarthiteCount", player.getTombarthite());
		dataJson.put("UnsafeGoldCount", player.getGoldoreUnsafe());

		dataJson.put("UnsafeOilCount", player.getOilUnsafe());
		dataJson.put("UnsafeUraniumCount", player.getSteelUnsafe());
		dataJson.put("UnsafeTombarthiteCount", player.getTombarthiteUnsafe());
		// VIP等级
		dataJson.put("VipLevel", player.getVipLevel());
		// VIP经验值
		dataJson.put("VipExp", player.getData().getPlayerEntity().getVipExp());

		// 注册时间
		dataJson.put("RegisterTime", String.valueOf(player.getCreateTime() / 1000));
		// 累计登陆时长
		long onlineTime = player.getOnlineTimeHistory();

		dataJson.put("TotalLoginTime", onlineTime > 0 ? onlineTime : (HawkTime.getMillisecond() - player.getLoginTime()) / 1000);
		// 最近登录时间
		dataJson.put("LastLoginTime", HawkTime.formatTime(player.getLoginTime()));
		// 最后登出时间
		dataJson.put("LastLogoutTime", player.getLogoutTime() > 0 ? HawkTime.formatTime(player.getLogoutTime()) : "");
		// 账号是否在线：1离线0在线
		dataJson.put("IsOnline", GlobalData.getInstance().isOnline(player.getId()) ? 0 : 1);
		AccountInfo accountInfo = GlobalData.getInstance().getAccountInfo(player.getPuid(), player.getServerId());

		// 账号状态:1封停0正常
		dataJson.put("Status", HawkTime.getMillisecond() < accountInfo.getForbidenTime() ? 1 : 0);
		return dataJson;
	}

	/**
	 * 判断是否是win32玩家
	 * @param player
	 * @return
	 */
	public static boolean isWin32Platform(Player player) {
		return isWin32Platform(player.getPlatform(), player.getChannel());
	}

	/**
	 * 判断是否是win32玩家
	 * @param player
	 * @return
	 */
	public static boolean isWin32Platform(String platform, String channel) {
		return "android".equals(platform) && "guest".equals(channel);
	}
	
	/** 判断是否是安卓用户 */
	public static boolean isAndroidAccount(Player player) {
		return "android".equals(player.getPlatform());
	}

	/**
	 * 获取字符串占位长度
	 * @param str
	 * @return
	 */
	public static int getStringLength(String str) {
		int length = 0;
		if (str != null) {
			for (char c : str.toCharArray()) {
				if (Character.toString(c).matches("[\\u4e00-\\u9fa5]")) {
					length += 2;
				} else {
					length++;
				}
			}
		}

		return length;
	}

	/**
	 * 检测玩家名字合法性
	 * 
	 */
	public static int checkPlayerNameCode(String playerName) {
		// 名称不能为空
		int len = getStringLength(playerName);
		if (len <= 0) {
			return Status.NameError.NAME_BLANK_ERROR_VALUE;
		}

		// 长度必须在4-12个字符之间
		if (len < ConstProperty.getInstance().getPlayerNameMin() || len > ConstProperty.getInstance().getPlayerNameMax()) {
			return Status.NameError.NAME_LENGTH_ERROR_VALUE;
		}

		// 检查是否包含非法字符，名称只能包含中英文或数字
		int regexType = GsConst.RegexType.ALLLETTER + GsConst.RegexType.NUM + GsConst.RegexType.CHINESELETTER;
		if (!GameUtil.stringOnlyContain(playerName, regexType, null)) {
			HawkLog.logPrintln("check player name invalid, playerName: {}", playerName);
			return Status.NameError.CONTAIN_ILLEGAL_CHART_VALUE;
		}

		return Status.SysError.SUCCESS_OK_VALUE;
	}

	/**
	 * 检测玩家名字
	 * 
	 * @param playerName
	 * @return
	 */
	public static int tryOccupyPlayerName(String playerId, String puid, String playerName) {
		// 检查屏蔽字, 存在敏感字符
		int checkCode = checkPlayerNameCode(playerName);
		if (checkCode != Status.SysError.SUCCESS_OK_VALUE) {
			return checkCode;
		}

		// 检测是否可以占用
		if (!HawkOSOperator.isEmptyString(playerId) || !HawkOSOperator.isEmptyString(puid)) {
			if (!GlobalData.getInstance().tryOccupyOrUpdatePlayerName(playerId, playerName)) {
				return Status.NameError.ALREADY_EXISTS_VALUE;
			}
		} else {
			playerId = GlobalData.getInstance().getPlayerIdByName(playerName);
			if (!HawkOSOperator.isEmptyString(playerId)) {
				return Status.NameError.ALREADY_EXISTS_VALUE;
			}
		}

		if (!HawkOSOperator.isEmptyString(RedisProxy.getInstance().getPreinstallNameUser(playerName))) {
			return Status.NameError.ALREADY_EXISTS_VALUE;
		}
		return Status.SysError.SUCCESS_OK_VALUE;
	}

	/**
	 * 更新玩家的名字绑定信息
	 * 
	 * @param playerId
	 * @param puid
	 * @param playerName
	 * @return
	 */
	public static boolean updateNewPlayerName(String playerId, String playerName) {
		return GlobalData.getInstance().tryOccupyOrUpdatePlayerName(playerId, playerName);
	}

	/**
	 * 根据玩家名称取playerId
	 * 
	 * @param name
	 * @return
	 */
	public static String getPlayerIdByName(String name) {
		return GlobalData.getInstance().getPlayerIdByName(name);
	}

	/**
	 * 移除玩家名字信息
	 * 
	 * @param name
	 */
	public static void removePlayerNameInfo(String name) {
		GlobalData.getInstance().removePlayerNameInfo(name);
	}

	/**
	 * 拼装puid
	 * @param puid
	 * @param platform
	 * @return
	 */
	public static String getPuidByPlatform(String puid, String platform) {
		if (GsConfig.getInstance().getPfDistinct() == AccountPuidType.PLATFORM_DISTINCT) {
			return String.format("%s#%s", puid, platform);
		}
		return puid;
	}

	/**
	 * 将字符串形式的平台信息转换成数字 0-ios 1-android
	 * @param platform
	 * @return
	 */
	public static int changePlatform2Int(String platform) {
		if (HawkOSOperator.isEmptyString(platform)) {
			throw new RuntimeException("platform null error");
		}
		return platform.equalsIgnoreCase(Platform.IOS.strVal()) ? Platform.IOS.intVal() : Platform.ANDROID.intVal();
	}

	/**
	 * 获取联盟资源捐献重置次数
	 * @param player
	 * @return
	 */
	public static int getDonateResetTimes(Player player) {
		int resetTimes = GuildConstProperty.getInstance().getDonateResetLimit();
		if (!player.getData().getVipActivated()) {
			return resetTimes;
		}
		int vipLevel = player.getVipLevel();
		VipCfg vipCfg = HawkConfigManager.getInstance().getConfigByKey(VipCfg.class, vipLevel);
		if (vipCfg == null) {
			HawkLog.errPrintln("vip config error, playerId: {}, vipLevel: {}", player.getId(), vipLevel);
			return resetTimes;
		}

		resetTimes += vipCfg.getDonateResetTimes();
		return resetTimes;
	}

	/**
	 * puid是否受白名单限制, 受限时不记录日志
	 * @param puid
	 * @return true 受限制 false 不受限制
	 */
	public static boolean isTlogPuidControlled(String puid) {
		return HawkConfigManager.getInstance().getConfigByKey(TlogPuidCtrl.class, puid) != null;
	}

	/**
	 * 是否注册白名单玩家
	 * 
	 * @param openid
	 * @return
	 */
	public static boolean isRegisterPuidCtrlPlayer(String openid) {
		// 注册白名单中的账号可以直接注册
		RegisterPuidCtrl registerPuidCtrl = HawkConfigManager.getInstance().getConfigByKey(RegisterPuidCtrl.class, openid);
		if (registerPuidCtrl != null) {
			return true;
		}

		// 灰度账号也可以直接注册
		GrayPuidCtrl grayPuidCtrl = HawkConfigManager.getInstance().getConfigByKey(GrayPuidCtrl.class, openid);
		if (grayPuidCtrl != null) {
			return true;
		}

		// 判断此账号是否被脚本命令加入注册白名单
		if (RedisProxy.getInstance().checkPuidControl(openid)) {
			return true;
		}

		// 检测idip添加的注册白名单账号
		if (RedisProxy.getInstance().checkRegisterPuidControl(openid)) {
			return true;
		}

		return false;
	}
	
	/**
	 * 是否tblyOb
	 */
	public static boolean isOBPuidCtrlPlayer(String openid) {
		OBPuidCtrl registerPuidCtrl = HawkConfigManager.getInstance().getConfigByKey(OBPuidCtrl.class, openid);
		if (registerPuidCtrl != null) {
			return true;
		}
		return false;
	}

	/**
	 * 字符串过滤
	 * @param str
	 * @return
	 */
	public static String stringFilter(String str) {
		if (HawkOSOperator.isEmptyString(str)) {
			return str;
		}
		return str.replaceAll("\\|", "").replaceAll("\\\n", "").replaceAll("\\\\", "");
	}

	/**
	 * 字符串编码
	 * @return
	 */
	public static String getStrEncoded(String str) {
		if (!HawkOSOperator.isEmptyString(str)) {
			String strEncoded = str.replaceAll("\\|", "").replaceAll("\\n", "");
			try {
				return URLEncoder.encode(strEncoded, "utf-8");
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}

		return null;
	}

	/**
	 * 根据建筑类型获取其产出的资源类型
	 * @param buildingType
	 * @return
	 */
	public static int getResTypeByBuildingType(int buildingType) {
		switch (buildingType) {
		case BuildingType.ORE_REFINING_PLANT_VALUE:
			return PlayerAttr.GOLDORE_UNSAFE_VALUE;
		case BuildingType.OIL_WELL_VALUE:
			return PlayerAttr.OIL_UNSAFE_VALUE;
		case BuildingType.STEEL_PLANT_VALUE:
			return PlayerAttr.STEEL_UNSAFE_VALUE;
		case BuildingType.RARE_EARTH_SMELTER_VALUE:
			return PlayerAttr.TOMBARTHITE_UNSAFE_VALUE;
		default:
			return 0;
		}
	}

	/**
	 * 初始化账号
	 * @param player
	 */
	public static void resetAccount(Player player) {
		player.dealMsg(MsgId.ACCOUNT_RESET, new PlayerAccountResetInvoker(player, 0));
	}

	/**
	 * 账号重置
	 * @param player
	 * @param code
	 */
	public static void resetAccount(Player player, int code) {
		String playerId = player.getId();
		try {
			RedisProxy.getInstance().addRemovePlayerFlag(playerId);
			HawkLog.logPrintln("player reset account start, playerId: {}, puid: {}, active: {}", player.getId(), player.getPuid(), player.getEntity().isActive());

			// 联盟数据处理
			if (player.hasGuild()) {
				resetAccountGuild(player);
			}
			// 清除排行榜数据
			RankService.getInstance().dealMsg(MsgId.PERSONAL_RANK_BAN, new BanPersonalRankMsgInvoker(playerId));
			
			// 玩家在线情况踢下线
			if (player.isActiveOnline()) {
				int kickoutCode = code != 0 ? code : Status.IdipMsgCode.IDIP_ACCOUNT_RESET_OFFLINE_VALUE;
				player.kickout(kickoutCode, true, null);
			} else {
				clearAccountRoleData(player);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
			RedisProxy.getInstance().clearRemovePlayerFlag(playerId);
		}
	}
	
	/**
	 * 删除角色时清楚数据
	 * 
	 * @param player
	 */
	public static void clearAccountRoleData(Player player) {
		player.getData().getPlayerEntity().setActive(false);
		// 这里必须是同步落地，而不能是异步落地，因为后面从 GlobalData中删除掉accountInfo之后就落不了地了（落地时会调用GlobalData中的isLocalPlayer接口进行判断）
		player.getData().getPlayerEntity().notifyUpdate(false, 0);
		
		RedisProxy.getInstance().deleRecentServer(player.getEntity().getServerId(), player.getOpenId(), player.getPlatform());

		// 执行这一步是为了防止内存中还没有accountRoleInfo信息
		GlobalData.getInstance().getAccountRoleInfo(player.getId());
		// 清除账号信息
		GlobalData.getInstance().removeAccountInfo(player.getId());
		GlobalData.getInstance().removeAccountRoleInfo(player.getId());
		// 清除城点
		int pointId = WorldPlayerService.getInstance().getPlayerPos(player.getId());
		WorldPointService.getInstance().removeWorldPoint(pointId, true);
		
		GlobalData.getInstance().uncachePlayerData(player.getId());
		GlobalData.getInstance().removeCacheEntity(player.getId());
		HawkLog.logPrintln("player reset account finish, playerId: {}, puid: {}, active: {}", player.getId(), player.getPuid(), player.getEntity().isActive());
	}
	
	/**
	 * 重置联盟相关信息
	 * 
	 * @param player
	 */
	private static void resetAccountGuild(Player player) {
		if (GuildService.getInstance().getGuildMemberNum(player.getGuildId()) == 1) {
			// 只有自己一个人，解散联盟
			player.rpcCall(MsgId.DISMISS_GUILD, GuildService.getInstance(), new GuildDismissRpcInvoker(player, player.getGuildId(), 0));
		} else if (player.getGuildLeaderId().equals(player.getId())) {
			// 自己是盟主，转让盟主
			GuildMemberObject target = null;
			Collection<String> memberIds = GuildService.getInstance().getGuildMembers(player.getGuildId()); 
			for (String id : memberIds) {
				GuildMemberObject member = GuildService.getInstance().getGuildMemberObject(id);
				if (member != null && member.getOfficeId() != GuildOffice.NONE.value && member.getOfficeId() != GuildOffice.LEADER.value) {
					target = member;
					break;
				}
			}
			if (target == null) {
				for (String id : memberIds) {
					GuildMemberObject member = GuildService.getInstance().getGuildMemberObject(id);
					if (member != null && member.getOfficeId() != GuildOffice.LEADER.value) {
						target = member;
						break;
					}
				}
			}
			
			if (target != null) {
				GuildService.getInstance().dealMsg(MsgId.GUILD_DEMISE_LEADER, new GuildDemiseLeaderInvoker(player, target.getPlayerId(), 0));
				// 自己不是盟主，被盟主踢出联盟
				Player leaderPlayer = GlobalData.getInstance().makesurePlayer(target.getPlayerId());
				GuildService.getInstance().dealMsg(MsgId.GUILD_KICK_MEMBER, new GuildKickMemberInvoker(leaderPlayer, player.getId(), 0));
			}
		} else {
			// 自己不是盟主，被盟主踢出联盟
			Player leaderPlayer = GlobalData.getInstance().makesurePlayer(player.getGuildLeaderId());
			GuildService.getInstance().dealMsg(MsgId.GUILD_KICK_MEMBER, new GuildKickMemberInvoker(leaderPlayer, player.getId(), 0));
		}
	}
	
	public static boolean reduceByEffect(List<ItemInfo> itemInfos, int resItemId, int[] effVal) {
		double left = 1;
		for (int val : effVal) {
			left = left * (1 - val * GsConst.EFF_PER);
		}

		for (ItemInfo item : itemInfos) {
			if (item.getItemId() == resItemId) {
				int itemCount = (int) Math.ceil((item.getCount() * left));
				item.setCount(Math.max(0, itemCount));
			}
		}

		return true;
	}

	/**
	 * 受作用号影响减少基础资源的消耗：黄金石油合金铀矿
	 * @param itemInfos
	 * @param goldArr
	 * @param oillArr
	 * @param tombArr
	 * @param stelArr
	 * @param medal 勋章
	 * @return
	 */
	public static boolean reduceByEffect(List<ItemInfo> itemInfos, int[] goldArr, int[] oillArr, int[] tombArr, int[] stelArr, int[] medal) {
		reduceByEffect(itemInfos, PlayerAttr.GOLDORE_VALUE, goldArr);
		reduceByEffect(itemInfos, PlayerAttr.GOLDORE_UNSAFE_VALUE, goldArr);

		reduceByEffect(itemInfos, PlayerAttr.OIL_VALUE, oillArr);
		reduceByEffect(itemInfos, PlayerAttr.OIL_UNSAFE_VALUE, oillArr);

		reduceByEffect(itemInfos, PlayerAttr.TOMBARTHITE_VALUE, tombArr);
		reduceByEffect(itemInfos, PlayerAttr.TOMBARTHITE_UNSAFE_VALUE, tombArr);

		reduceByEffect(itemInfos, PlayerAttr.STEEL_VALUE, stelArr);
		reduceByEffect(itemInfos, PlayerAttr.STEEL_UNSAFE_VALUE, stelArr);
		
		reduceByEffect(itemInfos, GsConst.MEDAL_ITEM_ID, medal);
		return true;
	}

	/**
	 * 取出数组最大值索引
	 * @param arr
	 * @return
	 */
	public static int getMaxIndex(int[] arr) {
		int index = 0;
		if (arr.length == 1) {
			return 0;
		}
		for (int i = 1; i < arr.length; i++) {
			if (arr[i] > arr[index]) {
				index = i;
			}
		}
		return index;
	}

	/**
	 * 随机联盟宝藏
	 * @param cityLevel 大本等级
	 * @return
	 */
	public static int randomStore(final int cityLevel, final boolean free, int minGroup) {

		List<AllianceStorehouseCharacterCfg.WeightObj> characterList = HawkConfigManager.getInstance().getConfigIterator(AllianceStorehouseCharacterCfg.class).stream()
				.filter(cfg -> minGroup == 0 || cfg.getId() == minGroup)
				.map(cfg -> cfg.toWeightObj(free))
				.collect(Collectors.toList());

		AllianceStorehouseCharacterCfg rewardCfg = RandomUtil.random(characterList).getCfg();
		final int groupId = rewardCfg.getId();
		List<AllianceStorehouseCfg> storeList = HawkConfigManager.getInstance().getConfigIterator(AllianceStorehouseCfg.class).stream()
				.filter(c -> c.getGroupId() == groupId)
				.filter(c -> cityLevel <= c.getLevelMax())
				.filter(c -> cityLevel >= c.getLevelMin())
				.collect(Collectors.toList());
		if (storeList.isEmpty()) {// 策划总配错. 保险点
			storeList = HawkConfigManager.getInstance().getConfigIterator(AllianceStorehouseCfg.class).stream()
					.filter(c -> c.getGroupId() == groupId)
					.collect(Collectors.toList());
		}

		AllianceStorehouseCfg storeCfg = RandomUtil.random(storeList);

		return storeCfg.getId();
	}

	/**
	 * 城点防御失败后获取城点着火结束时间
	 * @param lastEndTime 上次防御失败后着火结束时间
	 * @return
	 */
	public static long getOnFireEndTime(long lastEndTime) {
		long now = HawkApp.getInstance().getCurrentTime();
		int onFireTimeAdd = ConstProperty.getInstance().getOnceAttackWallFireTime();
		long newEndTime = lastEndTime;
		if (now >= lastEndTime) {
			newEndTime = now + onFireTimeAdd * 1000L;
		} else {
			newEndTime = lastEndTime + onFireTimeAdd * 1000L;
			long maxOnFireTime = ConstProperty.getInstance().getWallFireMaxTime() * 1000L;
			if (newEndTime - now > maxOnFireTime) {
				newEndTime = now + maxOnFireTime;
			}
		}

		return newEndTime;
	}

	/** 进出联盟4邮件共用参数 */
	public static Object[] guild4MailContents(String guildId) {
		// 联盟盟主头像（和平台头像），盟主名字，联盟id，联盟简称，联盟全称，联盟旗帜，联盟战力，联盟玩家人数，联盟上限。
		GuildInfoObject guild = GuildService.getInstance().getGuildInfoObject(guildId);
		String leaderId = GuildService.getInstance().getGuildLeaderId(guildId);
		Player leader = GlobalData.getInstance().makesurePlayer(leaderId);
		Object[] result = new Object[] {
				leader.getIcon(),
				leader.getPfIcon(),
				leader.getName(),
				guild.getId(),
				guild.getTag(),
				guild.getName(),
				guild.getFlagId(),
				GuildService.getInstance().getGuildBattlePoint(guildId),
				GuildService.getInstance().getGuildMemberNum(guildId),
				GuildService.getInstance().getGuildMemberMaxNum(guildId)
		};
		return result;
	}

	/**
	 * 根据建筑等级获取作用号加成效果
	 * @param player
	 * @param effTypes
	 * @param level
	 * @return
	 */
	public static int getBuildSpeedEffByLevel(Player player, EffType[] effTypes, int level) {
		for (EffType effType : effTypes) {
			EffectCfg cfg = HawkConfigManager.getInstance().getConfigByKey(EffectCfg.class, effType.getNumber());
			if (cfg == null) {
				return player.getEffect().getEffVal(effType);
			}

			int[] array = cfg.getConditionElements();
			if (array == null) {
				return player.getEffect().getEffVal(effType);
			}

			if (array[0] <= level && level <= array[1]) {
				return player.getEffect().getEffVal(effType);
			}
		}

		return 0;
	}

	/**
	 * 获取某一类型科技的研究次数
	 * @param player
	 * @param type 传0表示所有类型
	 * @param needFinish 是否需要研究完才算
	 * @return
	 */
	public static int getTechRechearTimes(PlayerData playerData, int type, boolean needFinish) {
		int times = 0;
		for (TechnologyEntity entity : playerData.getTechnologyEntities()) {
			TechnologyCfg cfg = HawkConfigManager.getInstance().getConfigByKey(TechnologyCfg.class, entity.getCfgId());
			if (cfg != null && (type == 0 || type == cfg.getTechType())) {
				times += entity.getLevel();
			}
			if (!needFinish && entity.isResearching()) {
				times += 1;
			}
		}
		return times;
	}

	/**
	 * 判断一个作用号是否是资源增产作用号
	 * @param effectId
	 * @return
	 */
	public static boolean isResProduceUpEffect(Integer effectId) {
		List<Integer> effects = ConstProperty.getInstance().getResUpEffectList();
		return effects != null && effects.contains(effectId);
	}

	/**
	 * 迁出玩家
	 * 删除global 里面的
	 * @param playerId
	 * @param playerName
	 */
	public static void migrateOutPlayer(String playerId, String playerName) {
		// 删除名字缓存
		GlobalData.getInstance().removePlayerNameInfo(playerName);
		// 删除玩家ID账号信息
		GlobalData.getInstance().removeAccountInfo(playerId);
		// 删除搜索里面的玩家名字
		SearchService.getInstance().removePlayerInfo(playerName);
		// 删除搜索里面的玩家名字缓存（模糊搜索的数据)
		SearchService.getInstance().removePlayerNameLow(playerName, playerId);
	}

	/**
	 * 测试远程服务器的 web service 是否可用
	 * @param serverInfo
	 * @return
	 */
	public static boolean testServerWebService(ServerInfo serverInfo) {
		String httpStr = "http" + "://" + serverInfo.getWebHost() + "/script/testPing";
		try {
			ContentResponse content = HawkHttpUrlService.getInstance().doGet(httpStr, 2000);
			if (content == null || HawkOSOperator.isEmptyString(content.getContentAsString())) {
				return false;
			}
		} catch (Exception e) {
			HawkException.catchException(e, "test target server error, targetWebHost=" + serverInfo.getWebHost());

			return false;
		}

		return true;

	}

	/**
	 * 
	 * @param serverInfo
	 * @param info
	 * @return
	 */
	public static String getImmgratePlayerURL(ServerInfo serverInfo, String playerId, String serverId) {
		StringBuilder sb = new StringBuilder();
		String url = "http://" + serverInfo.getWebHost() + "/script/migrateInPlayer";
		sb.append(url);
		sb.append("?");
		sb.append("playerId=");
		sb.append(playerId);
		sb.append("&");
		sb.append("targetServerId=");
		sb.append(serverId);

		return sb.toString();
	}

	/**
	 * 不在线的时候用该方法判定是否活跃
	 * @param pbBuilder
	 * @return
	 */
	public static boolean isLiveLy(PlayerData playerData) {
		if (playerData.getPlayerEntity().getLogoutTime() <= 0) {
			return false;
		}
		Calendar calendar = HawkTime.getCalendar(true);
		calendar.setTimeInMillis(playerData.getPlayerEntity().getLogoutTime());

		int livelyDayDiff = HawkTime.calendarDiff(HawkTime.getCalendar(false), calendar);
		ConstProperty constProperty = ConstProperty.getInstance();
		int newMask = 0;
		if (livelyDayDiff > constProperty.getFriendCycle()) {
			livelyDayDiff = constProperty.getFriendCycle();
		}

		newMask = playerData.getPlayerEntity().getLivelyMask() << livelyDayDiff;
		// 超过7天的数据不要
		newMask = newMask & 0x7F;
		newMask |= 1;

		Calendar openServer = HawkTime.getCalendar(true);
		openServer.setTimeInMillis(GsApp.getInstance().getServerOpenTime());
		int day = HawkTime.calendarDiff(HawkTime.getCalendar(false), openServer);
		if (day >= (constProperty.getFriendCycle() - 1)) {
			return Long.bitCount(newMask) >= constProperty.getFriendCycleCom();
		} else {
			return Long.bitCount(newMask) * 1.0f / (day + 1) >= constProperty.getFriendActivePer();
		}
	}

	/**
	 * 获取装扮信息剩余时间
	 * @param dressType
	 * @param modelType
	 * @return
	 */
	public static long getDressRemainTime(DressItem dressItem) {
		if (dressItem == null) {
			return 0L;
		} else {
			long remainTime = dressItem.getStartTime() + dressItem.getContinueTime() - HawkTime.getMillisecond();
			return remainTime > 0L ? remainTime : 0L;
		}
	}

	/**
	 * 获取物品列表中指定物品的个数
	 * @param itemList
	 * @param itemType
	 * @param itemId
	 * @return
	 */
	public static int getItemNumByItemId(List<ItemInfo> itemList, ItemType itemType, int itemId) {
		int num = 0;
		for (ItemInfo itemInfo : itemList) {
			if (itemInfo.getType() == itemType.getNumber() * GsConst.ITEM_TYPE_BASE && itemInfo.getItemId() == itemId) {
				num += itemInfo.getCount();
			}
		}
		return num;
	}

	/**
	 * 添加buff
	 * @param player
	 * @param itemCfg
	 * @param targetId
	 */
	public static void addBuff(Player player, ItemCfg itemCfg, String targetId) {
		StatusDataEntity entity = player.addStatusBuff(itemCfg.getBuffId(), targetId);
		checkItemImageBuff(player, itemCfg);
		// 同步buff增益效果显示
		if (entity != null) {
			player.getPush().syncPlayerStatusInfo(false, entity);
		}
		// 使用增加矿石产量类道具，触发活动成就
		if (GameUtil.isResProduceUpEffect(itemCfg.getEffect())) {
			ActivityManager.getInstance().postEvent(new UpgradeResourceProductorEvent(player.getId(), itemCfg.getEffect()));
			MissionManager.getInstance().postMsg(player, new EventUpgradeResourceProductor());
		}
		// 使用加速类道具，触发活动成就
		if (itemCfg.getItemType() == ItemType.PLAYER_ATTR_VALUE) {
			ActivityManager.getInstance().postEvent(new UseItemSpeedUpEvent(player.getId(), itemCfg.getSpeedUpTime() / 60));
			MissionManager.getInstance().postMsg(player, new EventUseItemSpeed(itemCfg.getSpeedUpTime() / 60));
		}
	}

	private static void checkItemImageBuff(Player player, ItemCfg itemCfg) {
		BuffCfg buffCfg = HawkConfigManager.getInstance().getConfigByKey(BuffCfg.class, itemCfg.getBuffId());
		if (buffCfg == null) {
			return;
		}
		EffectCfg effCfg = HawkConfigManager.getInstance().getConfigByKey(EffectCfg.class, buffCfg.getEffect());
		if (effCfg == null) {
			return;
		}
		if (effCfg.getType() == EffectType.IMAGE_ITEM.getValue()) {
			HawkTaskManager.getInstance().postMsg(player.getXid(), new PlayerImageFresh());
			ActivityManager.getInstance().postEvent(new DressActiveEvent(player.getId(), effCfg.getId()));
		}
	}

	/**
	 * 开启资源宝箱
	 * 
	 * @param player
	 * @param itemCfg
	 * @param itemCount
	 * @param targetId
	 */
	public static void addPlayeAttr(Player player, ItemCfg itemCfg, int itemCount, String targetId, Action action) {
		long resCount = itemCfg.getAttrVal();
		resCount *= itemCount;
		do {
			long count = Math.min(resCount, Integer.MAX_VALUE - 1);
			ItemInfo item = new ItemInfo(ItemType.PLAYER_ATTR_VALUE, itemCfg.getAttrType(), (int) count);
			resCount -= count;
			if (item.getItemId() == PlayerAttr.EXP_VALUE && NumberUtils.isNumber(targetId)) {
				// 给玩家加经验的道具也可以给英雄加经验
				int heroId = NumberUtils.toInt(targetId);
				Optional<PlayerHero> heroOp = player.getHeroByCfgId(heroId);
				if (heroOp.isPresent()) {
					heroOp.get().addExp((int) item.getCount());
					return;
				}
			}

			action = itemCfg.getAttrType() == PlayerAttr.GOLD_VALUE ? Action.USE_GOLD_ITEM : action;
			AwardItems award = AwardItems.valueOf();
			award.addItem(item);
			award.rewardTakeAffectAndPush(player, action, true, RewardOrginType.ITEM_RESOURCE, itemCfg.getId());
		} while (resCount > 0);
	}
	
	/**
	 * 使用道具返还对应物品
	 * @param player
	 * @param itemId 使用的道具ID
	 * @param count使用的数量
	 */
	public static boolean itemPropExchangeAward(Player player, int itemId, int count) {
		Object[] params = ConstProperty.getInstance().getPropExchangeParam(itemId);
		if (params == null) {
			return false;
		}
		
		count = count / (int)params[1];
		ItemInfo itemInfo = ItemInfo.valueOf((String) params[2]);
		if (itemInfo.getItemType() == Const.ItemType.PLAYER_ATTR && itemInfo.getItemId() == PlayerAttr.DIAMOND_VALUE) {
			player.increaseDiamond((int)itemInfo.getCount() * count, Action.PROP_EXCHANGE, null, DiamondPresentReason.GAMEPLAY);
		} else {
			itemInfo.setCount(itemInfo.getCount() * count);
			AwardItems awardItems = AwardItems.valueOf();
			awardItems.addItem(itemInfo);
			awardItems.rewardTakeAffectAndPush(player, Action.PROP_EXCHANGE, true);
		}
		return true;
	}

	/**
	 * 获取充值相关的价格类型
	 */
	public static PriceType getPriceType(String channel) {
		if (isUSDChannel(channel)) {
			return PriceType.USD;
		}

		return PriceType.RMB;
	}

	/**
	 * 获取开服天数
	 * @return
	 */
	public static int getNumOfDaysToOpenServer() {
		Calendar openServer = HawkTime.getCalendar(true);
		openServer.setTimeInMillis(getServerOpenTime());
		int days = HawkTime.calendarDiff(HawkTime.getCalendar(false), openServer);
		days = days >= 0 ? days : 0;
		return days;
	}

	/**
	 * 获取玩家的月卡数据
	 * 
	 * @param playerId
	 * @return
	 */
	public static ActivityMonthCardEntity getMonthCardEntity(String playerId) {
        if (playerId == null || playerId.startsWith("NPC")) {
        	return null;
        }
		// 如果是一个迁入玩家, 就不读取月卡的数据.
		if (CrossService.getInstance().isImmigrationPlayer(playerId)) {
			return null;
		}
		Optional<ActivityBase> opActivity = ActivityManager.getInstance().getActivity(ActivityType.MONTHCARD_VALUE);
		// 购买了月卡且生效时才能购买每日特惠礼包或领取免费宝箱
		if (opActivity.isPresent()) {
			MonthCardActivity monthCardActivity = (MonthCardActivity) opActivity.get();
			Optional<ActivityMonthCardEntity> opEntity = monthCardActivity.getPlayerDataEntity(playerId);
			if (opEntity.isPresent()) {
				return opEntity.get();
			}
		}

		return null;
	}

	/**
	 * 获取玩家当日完成某某的数量情况
	 * 
	 * @param playerId
	 * @return
	 */
	public static HawkTuple2<Long, Integer> spliteTimeAndCount(String playerId, String timeCountInfo) {
		int count = 0;
		long lastTime = 0;
		// 跨天重置
		if (!HawkOSOperator.isEmptyString(timeCountInfo)) {
			String[] buyTimeArray = timeCountInfo.split(":");
			lastTime = Long.valueOf(buyTimeArray[0]);
			if (HawkTime.isSameDay(lastTime, HawkTime.getMillisecond())) {
				count = Integer.valueOf(buyTimeArray[1]);
			} else {
				count = -1;
			}
		}

		return new HawkTuple2<Long, Integer>(lastTime, count);
	}

	/**
	 * IP转数字
	 * 
	 * @param ip
	 * @return
	 */
	public static Long ipToNumber(String ip) {
		Long ips = 0L;
		String[] numbers = ip.split("\\.");
		for (int i = 0; i < numbers.length; i++) {
			ips = ips << 8 | Integer.parseInt(numbers[i]);
		}

		return ips;
	}

	/**
	 * 数字转IP
	 * 
	 * @param number
	 * @return
	 */
	public static String numberToIp(Long number) {
		StringBuilder ip = new StringBuilder();
		for (int i = 3; i >= 0; i--) {
			ip.append(String.valueOf((number & 0xff)));
			if (i != 0) {
				ip.append(".");
			}

			number = number >> 8;
		}

		return ip.toString();
	}

	/**
	 * 时间队列类型转换成对应的加速Action
	 * 
	 * @param queueType
	 * @return
	 */
	public static Action queueTypeToAction(int queueType) {
		switch (queueType) {
		case QueueType.BUILDING_QUEUE_VALUE:
			return Action.PLAYER_BUILDING_QUEUE_SPEEDUP;
		case QueueType.SCIENCE_QUEUE_VALUE:
			return Action.PLAYER_TECH_QUEUE_SPEEDUP;
		case QueueType.SOILDER_QUEUE_VALUE:
			return Action.PLAYER_TRAIN_QUEUE_SPEEDUP;
		case QueueType.TRAP_QUEUE_VALUE:
			return Action.PLAYER_TRAP_QUEUE_SPEEDUP;
		case QueueType.CURE_QUEUE_VALUE:
		case QueueType.CURE_PLANT_QUEUE_VALUE:
			return Action.PLAYER_CURE_QUEUE_SPEEDUP;
		case QueueType.EQUIP_QUEUE_VALUE:
			return Action.PLAYER_EQUIP_QUEUE_SPEEDUP;
		case QueueType.CROSS_TECH_QUEUE_VALUE:
			return Action.PLAYER_CROSS_TECH_QUEUE_SPEEDUP;
		case QueueType.EQUIP_RESEARCH_QUEUE_VALUE:
			return Action.EQUIP_RESEARCH_LEVEL_UP;
		default:
			return Action.PLAYER_QUEUE_SPEEDUP;
		}
	}

	/**
	 * 使用道具类型转换成对应的action
	 * 
	 * @param itemCfg
	 * @return
	 */
	public static Action itemTypeToAction(ItemCfg itemCfg) {
		switch (itemCfg.getItemType()) {
		case Const.ToolType.STATUS_VALUE: { // 加Buff
			if (isResProduceUpEffect(itemCfg.getEffect())) {
				return Action.RES_BUILDING_OUTPUT_INC;
			}
			return Action.USE_GAIN_STATUS_ITEM;
		}
		case Const.ToolType.REWARD_VALUE: { // 开箱子
			return Action.USE_REWARD_BOX_ITEM;
		}
		case Const.ToolType.WISH_ITEM_VALUE: { // 增加许愿次数
			return Action.USE_WISH_ADD_ITEM;
		}
		case Const.ToolType.DRESS_VALUE: { // 使用装扮道具
			return Action.USE_DRESS_ITEM;
		}
		case Const.ToolType.ADD_ATTR_VALUE: {
			switch (itemCfg.getAttrType()) {
			case PlayerAttr.GOLD_VALUE: {
				return Action.USE_GOLD_ITEM;
			}
			case PlayerAttr.VIT_VALUE: {
				return Action.USE_VIT_ITEM;
			}
			case PlayerAttr.EXP_VALUE: {
				return Action.USE_EXP_ITEM;
			}
			case PlayerAttr.GOLDORE_VALUE:
			case PlayerAttr.OIL_VALUE:
			case PlayerAttr.STEEL_VALUE:
			case PlayerAttr.TOMBARTHITE_VALUE: {
				return Action.USE_RESOURCE_ITEM;
			}
			default:
				return Action.TOOL_USE;
			}
		}
		default:
			return Action.TOOL_USE;
		}
	}

	public static boolean setFlagAndPush(Player player, PlayerFlagPosition position, int value) {
		boolean setResult = player.getData().setFlag(position, value);
		if (setResult) {
			player.getPush().synPlayerFlag();
		}

		return setResult;
	}

	/**
	 * 组装作用号配置
	 * @param effect
	 * @return
	 */
	public static List<EffectObject> assambleEffectObject(String effect) {
		List<EffectObject> effectList = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(effect)) {
			String[] array = effect.split(",");
			for (String val : array) {
				String[] info = val.split("_");
				effectList.add(new EffectObject(Integer.parseInt(info[0]), Integer.parseInt(info[1])));
			}
		}
		return effectList;
	}

	/**
	 * 组装作用号配置
	 */
	public static Map<Integer, Integer> assambleEffectMap(String effect) {
		Map<Integer, Integer> effectMap = new HashMap<>();
		if (!HawkOSOperator.isEmptyString(effect)) {
			String[] array = effect.split(",");
			for (String val : array) {
				String[] info = val.split("_");
				effectMap.put(Integer.parseInt(info[0]), Integer.parseInt(info[1]));
			}
		}
		return effectMap;
	}

	/**
	 * 组装作用号配置
	 * @param effect
	 * @return
	 */
	public static List<EffectObject> assambleSuperBarrackEffect(String effect) {
		List<EffectObject> effectList = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(effect)) {
			String[] array = effect.split("\\|");
			for (String val : array) {
				String[] info = val.split("_");
				effectList.add(new EffectObject(Integer.parseInt(info[0]), Integer.parseInt(info[1])));
			}
		}
		return effectList;
	}

	/**
	 * 根据军衔经验获取军衔等级
	 * @param militaryExp
	 * @return
	 */
	public static int getMilitaryRankByExp(int militaryExp) {
		int configSize = HawkConfigManager.getInstance().getConfigSize(MilitaryRankCfg.class);
		if (configSize <= 0) {
			throw new RuntimeException("get military rank by exp error !");
		}

		MilitaryRankCfg cfg = null;

		ConfigIterator<MilitaryRankCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(MilitaryRankCfg.class);
		while (configIterator.hasNext()) {
			MilitaryRankCfg thisCfg = configIterator.next();
			if (militaryExp < thisCfg.getRankExp()) {
				cfg = thisCfg;
				break;
			}
		}

		if (cfg == null) {
			cfg = HawkConfigManager.getInstance().getConfigByIndex(MilitaryRankCfg.class, configSize - 1);
		}

		return cfg.getRankLevel();
	}

	/**
	 * 判断是否是钻石类型
	 * 
	 * @param itemType
	 * @param itemId
	 * @return
	 */
	public static boolean isDiamond(int itemType, int itemId) {
		return itemType / GsConst.ITEM_TYPE_BASE == ItemType.PLAYER_ATTR_VALUE && itemId == PlayerAttr.DIAMOND_VALUE;
	}

	/**
	 * 判断是否是资源item
	 * 
	 * @param itemInfo
	 * @return
	 */
	public static boolean isResItem(ItemInfo itemInfo) {
		return isResItem(itemInfo.getItemType(), itemInfo.getItemId());
	}

	public static boolean isResItem(ItemType itemType, int itemId) {
		if (itemType != ItemType.PLAYER_ATTR) {
			return false;
		}

		return itemId == PlayerAttr.GOLDORE_VALUE
				|| itemId == PlayerAttr.GOLDORE_UNSAFE_VALUE
				|| itemId == PlayerAttr.OIL_VALUE
				|| itemId == PlayerAttr.OIL_UNSAFE_VALUE
				|| itemId == PlayerAttr.STEEL_VALUE
				|| itemId == PlayerAttr.STEEL_UNSAFE_VALUE
				|| itemId == PlayerAttr.TOMBARTHITE_VALUE
				|| itemId == PlayerAttr.TOMBARTHITE_UNSAFE_VALUE;
	}

	/**
	 * 判断是否是补偿邮件
	 * 
	 * @param mailId
	 * @return
	 */
	public static boolean isRewardMail(int mailId) {
		switch(mailId) {
		case MailId.REWARD_MAIL_VALUE:
		case MailId.INVEST_PROFIT_VALUE:
		case MailId.RECHARGE_FUND_REWARD_VALUE:
		case MailId.GROUP_BUY_REWARD_VALUE:
		case MailId.GIFT_ZERO_CONSUME_BACK_REWARD_VALUE:
		case MailId.HONOR_REPAY_REWARD_VALUE:
		case MailId.HONOR_REPAY_GOLD_VALUE:
		case MailId.PDD_ORDER_FAIL_VALUE:
		case MailId.LUCK_GET_GOLD_WIN_VALUE:
		case MailId.LOTTERY_TICKET_ASSIST_USE_REWARD_VALUE:
		case MailId.SHARE_PROSP_OLDSVR_REWARD_VALUE:
			return true;
		}
		return false;
	}

	/**
	* 检测功能模块开启
	* @param player
	* @param sysFunctionId
	* @return
	*/
	public static boolean checkSysFunctionOpen(Player player, int sysFunctionId) {
		SysFunctionCfg sysFunctionCfg = HawkConfigManager.getInstance().getConfigByKey(SysFunctionCfg.class, sysFunctionId);
		if (Objects.isNull(sysFunctionCfg)) {
			return true;
		}
		Map<Integer, List<Integer>> conditions = sysFunctionCfg.getConditions();
		for (Entry<Integer, List<Integer>> condition : conditions.entrySet()) {
			if (!checkSysFunctionOpen(player, condition.getKey(), condition.getValue())) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 检测功能模块开启
	 * @param player
	 * @param conType
	 * @param conVal
	 * @return
	 */
	private static boolean checkSysFunctionOpen(Player player, int conType, List<Integer> conVal) {
		boolean isOpen = true;

		switch (conType) {

		case SysFunctionContionType.BUILDLVL:
			int buildLvl = player.getData().getBuildingMaxLevel(conVal.get(0));
			isOpen = (buildLvl >= conVal.get(1));
			break;

		case SysFunctionContionType.PLAYERLVL:
			int playerLvl = player.getLevel();
			isOpen = (playerLvl >= conVal.get(0));
			break;

		case SysFunctionContionType.STORYMISSIONLVL:
			int chapterId = player.getData().getStoryMissionEntity().getChapterId();
			int chapterState = player.getData().getStoryMissionEntity().getChapterState();
			isOpen = ((chapterId > conVal.get(0)) || ((chapterId == conVal.get(0)) && (chapterState != MissionState.STATE_NOT_FINISH)));
			break;

		case SysFunctionContionType.MILITARYRANK:
			int militaryExp = player.getEntity().getMilitaryExp();
			int militaryRank = GameUtil.getMilitaryRankByExp(militaryExp);
			isOpen = (militaryRank >= conVal.get(0));
			break;
		}

		return isOpen;
	}

	/**
	 * 获取大区id
	 * 
	 * @return
	 */
	public static int getWorldId() {
		return Integer.valueOf(GsConfig.getInstance().getAreaId());
	}
	
	/**
	 * 获取小区ID
	 * @return
	 */
	public static int getServerId() {
		return Integer.parseInt(GsConfig.getInstance().getServerId());
	}

	/**
	 * 上报成就积分信息
	 * 
	 * @param scoreType 成就积分类型
	 * @param value  成就值
	 * 
	 */
	public static void scoreBatch(Player player, ScoreType scoreType, Object value) {
		if (!isScoreBatchEnable(player)) {
			return;
		}

		QQScoreBatch.getInstance().scoreBatch(player, scoreType, value, scoreType.bcover, scoreType.expires);
	}

	/**
	 * 是否需要上报积分
	 * @param player
	 * @return
	 */
	public static boolean isScoreBatchEnable(Player player) {
		if (!GsConfig.getInstance().isScoreBatchEnable() || UserType.getByChannel(player.getChannel()) != UserType.QQ) {
			return false;
		}

		return true;
	}

	/**
	 * 获取手Q玩家的QQ会员等级
	 * @param player
	 * 
	 * @return 返回值大于0表示超级会员等级，小于0表示普通会员等级
	 */
	public static int getQQVipLevel(Player player) {
		if (GsConfig.getInstance().isRobotMode() || isWin32Platform(player)) {
			return 0;
		}

		if (UserType.getByChannel(player.getChannel()) != UserType.QQ) {
			return 0;
		}

		String qqVipInfo = RedisProxy.getInstance().getQQVip(player.getOpenId());
		if (qqVipInfo != null) {
			int qqVipLv = Integer.parseInt(qqVipInfo);
			if (qqVipLv < 0 && player.getEntity().getLoginWay() == LoginWay.COMMON_LOGIN_VALUE) {
				qqVipLv = 0;
			}

			return qqVipLv;
		}

		JSONObject json = SDKManager.getInstance().queryQQVip(player.getChannel(), player.getPfTokenJson(), QQVipQueryType.VIP_SUPER | QQVipQueryType.VIP_NORMAL);
		if (json == null) {
			HawkLog.errPrintln("query QQVipInfo failed, playerId: {}", player.getId());
			return 0;
		}

		if (!json.containsKey("ret") || json.getIntValue("ret") != 0) {
			HawkLog.errPrintln("query QQVipInfo failed, playerId: {}, result: {}", player.getId(), json);
			return 0;
		}

		int vipNormalLv = 0;
		JSONArray array = json.getJSONArray("lists");
		for (int i = 0; i < array.size(); i++) {
			JSONObject vipInfo = array.getJSONObject(i);
			// QQ超级会员，只有isvip有效，level字段无效
			if (vipInfo.getIntValue("flag") == QQVipQueryType.VIP_SUPER) {
				int isvip = vipInfo.getIntValue("isvip");
				if (isvip > 0) {
					RedisProxy.getInstance().updateQQVip(player.getOpenId(), isvip);
					return isvip; // 超级会员等级优先，用正数表示，这里的值只表示有效无效，而不是指具体的等级
				}
			}

			if (vipInfo.getIntValue("flag") == QQVipQueryType.VIP_NORMAL) {
				int level = vipInfo.getIntValue("level");
				// 普通会员等级只有在以游戏中心登录才有效
				if (player.getEntity().getLoginWay() != LoginWay.COMMON_LOGIN_VALUE) {
					vipNormalLv = 0 - level; // 普通会员等级用负数表示
				}
			}
		}

		RedisProxy.getInstance().updateQQVip(player.getOpenId(), vipNormalLv);
		return vipNormalLv;
	}

	/**
	 * 获取超时空急救站的冷却时间信息
	 * 
	 * @param rescueCd
	 * @return
	 */
	public static HawkTuple2<Integer, Integer> getSuperTimeRescueCd(int rescueCd) {
		int day = (0xffff0000 & rescueCd) >> 16;
		int todayOfYear = HawkTime.getYearDay();
		int index = 0x0000ffff & rescueCd;
		// 不是同一天
		if (todayOfYear != day) {
			index = 0;
		} else {
			index++;
		}

		List<Integer> cdList = ConstProperty.getInstance().getSuperTimeRescueCdList();
		if (index >= cdList.size()) {
			index = cdList.size() - 1;
		}

		int timeLong = cdList.get(index);
		int todayRemainSeconds = (int) Math.floor((HawkTime.getNextAM0Date() - HawkTime.getMillisecond()) / 1000);
		timeLong = Math.min(timeLong, todayRemainSeconds);
		rescueCd = (todayOfYear << 16) | index;

		return new HawkTuple2<Integer, Integer>(timeLong, rescueCd);
	}

	/**
	 * 玩家登录时检测建筑状态，处理异常情况
	 * 
	 * @param player
	 */
	public static void checkBuildingStatus(Player player) {
		List<BuildingBaseEntity> buildingEntities = player.getData().getBuildingEntitiesIgnoreStatus();
		try {
			// 医院可能有多个，但它们的状态却是一致的，只要处理过其中一个，其它就不用再处理了
			boolean hospitalChecked = false;
			String buildIndex = "";
			for (BuildingBaseEntity buildingEntity : buildingEntities) {
				// 检测光棱塔数据
				if (buildingEntity.getType() == BuildingType.PRISM_TOWER_VALUE) {
					if (HawkOSOperator.isEmptyString(buildIndex)) {
						buildIndex = buildingEntity.getBuildIndex();
					} else if (buildIndex.equals(buildingEntity.getBuildIndex())) {
						buildingEntity.setBuildIndex(buildIndex.equals("1") ? "2" : "1");
					}
				}

				int buildingStatus = buildingEntity.getStatus();
				// 普通建筑
				if (buildingStatus == BuildingStatus.BUILDING_CREATING_VALUE) {
					QueueEntity queueEntity = player.getData().getQueueEntityByItemId(buildingEntity.getId());
					// 1、建筑处于建造状态，但不存在建造队列
					if (queueEntity == null) {
						buildingEntity.setStatus(BuildingStatus.COMMON_VALUE);
						HawkLog.logPrintln("checkBuildingStatus, building creating status error, playerId: {}, buildingId: {}, cfgId: {}", player.getId(), buildingEntity.getId(),
								buildingEntity.getBuildingCfgId());
					}
				}

				int type = buildingEntity.getType();

				// 造兵建筑（包括城防工厂）
				if (isSoldierBuildType(type) || type == BuildingType.WAR_FORTS_VALUE) {
					if (buildingStatus == BuildingStatus.SOILDER_HARVEST_VALUE || buildingStatus == BuildingStatus.TRAP_HARVEST_VALUE) {
						// 2、训练完(或陷阱制造完)待领取，兵种身上没有这部分的兵
						if (ArmyService.getInstance().getTrainFinishCount(player, type) <= 0) {
							buildingEntity.setStatus(BuildingStatus.COMMON_VALUE);
							HawkLog.logPrintln("checkBuildingStatus, building soldier harvest status error, back to common status, playerId: {}, buildingId: {}, cfgId: {}",
									player.getId(), buildingEntity.getId(), buildingEntity.getBuildingCfgId());
						}
					} else {
						// 3、建筑处于普通状态，当其存在待领取的兵
						if (ArmyService.getInstance().getTrainFinishCount(player, type) > 0) {
							if (type == BuildingType.WAR_FORTS_VALUE) {
								buildingEntity.setStatus(BuildingStatus.TRAP_HARVEST_VALUE);
							} else {
								buildingEntity.setStatus(BuildingStatus.SOILDER_HARVEST_VALUE);
							}

							HawkLog.logPrintln("checkBuildingStatus, building common status error, back to soldier harvest status, playerId: {}, buildingId: {}, cfgId: {}",
									player.getId(), buildingEntity.getId(), buildingEntity.getBuildingCfgId());
						}
					}
				}

				// 医院建筑
				if (type == BuildingType.HOSPITAL_STATION_VALUE && !hospitalChecked) {

					hospitalChecked = true;

					if (buildingStatus == BuildingStatus.CURE_FINISH_HARVEST_VALUE) { // 待收取
						if (ArmyService.getInstance().getCureFinishCount(player) > 0) {
							continue;
						}

						// 4、治疗完待收取，兵种身上没有这部分的兵
						// 具体还原回什么状态，得看具体情况，没有伤兵还原回普通状态，否则还原回待治疗状态
						if (ArmyService.getInstance().getWoundedCount(player) <= 0) {
							GameUtil.changeBuildingStatus(player, BuildingType.HOSPITAL_STATION_VALUE, BuildingStatus.COMMON, false);
							HawkLog.logPrintln("checkBuildingStatus, building cure soldier harvest status error, back to common status, playerId: {}, buildingId: {}, cfgId: {}",
									player.getId(), buildingEntity.getId(), buildingEntity.getBuildingCfgId());
						} else {
							GameUtil.changeBuildingStatus(player, BuildingType.HOSPITAL_STATION_VALUE, BuildingStatus.SOLDIER_WOUNDED, false);
							HawkLog.logPrintln("checkBuildingStatus, building cure soldier harvest status error, back to wounded status, playerId: {}, buildingId: {}, cfgId: {}",
									player.getId(), buildingEntity.getId(), buildingEntity.getBuildingCfgId());
						}

					} else if (buildingStatus == BuildingStatus.SOLDIER_WOUNDED_VALUE) { // 待治疗状态
						if (ArmyService.getInstance().getWoundedCount(player) > 0) {
							continue;
						}

						// 5、有兵待治疗，兵种身上没有伤兵
						// 具体还原回什么状态，得看具体情况，没有治疗完的兵还原回普通状态，否则还原回待收取状态
						if (ArmyService.getInstance().getCureFinishCount(player) <= 0) {
							GameUtil.changeBuildingStatus(player, BuildingType.HOSPITAL_STATION_VALUE, BuildingStatus.COMMON, false);
							HawkLog.logPrintln("checkBuildingStatus, building soldier wounded status error, back to common status, playerId: {}, buildingId: {}, cfgId: {}",
									player.getId(), buildingEntity.getId(), buildingEntity.getBuildingCfgId());
						} else {
							GameUtil.changeBuildingStatus(player, BuildingType.HOSPITAL_STATION_VALUE, BuildingStatus.CURE_FINISH_HARVEST, false);
							HawkLog.logPrintln(
									"checkBuildingStatus, building soldier wounded status error, back to cure finish harvest status, playerId: {}, buildingId: {}, cfgId: {}",
									player.getId(), buildingEntity.getId(), buildingEntity.getBuildingCfgId());
						}

					} else { // 普通状态

						// 6、普通状态，兵种身上有待收取的兵
						if (ArmyService.getInstance().getCureFinishCount(player) > 0) {
							GameUtil.changeBuildingStatus(player, BuildingType.HOSPITAL_STATION_VALUE, BuildingStatus.CURE_FINISH_HARVEST, false);
							HawkLog.logPrintln("checkBuildingStatus, building common status error, back to cure finish harvest status, playerId: {}, buildingId: {}, cfgId: {}",
									player.getId(), buildingEntity.getId(), buildingEntity.getBuildingCfgId());
						} else if (ArmyService.getInstance().getWoundedCount(player) > 0) {
							// 7、普通状态，兵种身上有待治疗的兵
							GameUtil.changeBuildingStatus(player, BuildingType.HOSPITAL_STATION_VALUE, BuildingStatus.SOLDIER_WOUNDED, false);
							HawkLog.logPrintln("checkBuildingStatus, building common status error, back to wounded status, playerId: {}, buildingId: {}, cfgId: {}",
									player.getId(), buildingEntity.getId(), buildingEntity.getBuildingCfgId());
						}

					}
				} // end 医院建筑

			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 判断是不是造兵建筑
	 * @param buildingType
	 * @return
	 */
	public static boolean isSoldierBuildType(int buildingType) {
		if (buildingType == BuildingType.BARRACKS_VALUE
				|| buildingType == BuildingType.WAR_FACTORY_VALUE
				|| buildingType == BuildingType.REMOTE_FIRE_FACTORY_VALUE
				|| buildingType == BuildingType.AIR_FORCE_COMMAND_VALUE) {
			return true;
		}

		return false;
	}

	/**
	 * 获取一支部队的战力
	 * 
	 * @param armyInfos
	 * @return
	 */
	public static double getArmyPower(Collection<ArmyInfo> armyInfos) {
		double armyBattlePoint = 0;
		for (ArmyInfo armyInfo : armyInfos) {
			BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, armyInfo.getArmyId());
			if (cfg == null) {
				continue;
			}

			int liveSoldiers = armyInfo.getTotalCount() - armyInfo.getDeadCount();
			armyBattlePoint += cfg.getPower() * liveSoldiers;
		}

		return armyBattlePoint;
	}

	/**
	 * 刷新配置后检测更新全服数据
	 */
	public static void checkUpdateGlobalData() {
		updateServerControlData();
	}

	/**
	 * 更新服务器控制参数
	 */
	public static void updateServerControlData() {
		int maxOnlineCnt = GsConfig.getInstance().getSessionMaxSize();
		int maxRegisterCnt = GsConfig.getInstance().getRegisterMaxNum();
		int maxWaitCnt = GsConfig.getInstance().getLoginWaitMaxNum();
		ServerSettingData serverSetting = GlobalData.getInstance().getServerSettingData();

		boolean update = false;

		if (maxOnlineCnt > 0 && maxOnlineCnt != serverSetting.getCfgMaxOnlineCount()) {
			serverSetting.setMaxOnlineCount(maxOnlineCnt);
			serverSetting.setCfgMaxOnlineCount(maxOnlineCnt);
			update = true;
		}

		if (maxRegisterCnt > 0 && maxRegisterCnt != serverSetting.getCfgMaxRegisterCount()) {
			serverSetting.setMaxRegisterCount(maxRegisterCnt);
			serverSetting.setCfgMaxRegisterCount(maxRegisterCnt);
			update = true;
		}

		if (maxWaitCnt > 0 && maxWaitCnt != serverSetting.getCfgMaxWaitCount()) {
			serverSetting.setMaxWaitCount(maxWaitCnt);
			serverSetting.setCfgMaxWaitCount(maxWaitCnt);
			update = true;
		}

		if (update) {
			RedisProxy.getInstance().updateServerControlData(serverSetting);
		}
	}

	/**
	 * 判断密友邀请功能是否开放
	 * 
	 * @return
	 */
	public static boolean isFriendInviteEnable(String channel) {
		if (GsConfig.getInstance().isRobotMode()) {
			return false;
		}

		int userType = UserType.getByChannel(channel);
		if (userType != UserType.QQ && userType != UserType.WX) {
			return false;
		}

		return true;
	}

	/**
	 * 根据玩家id判定Player是否是NPC
	 * @param playerId
	 * @return
	 */
	public static boolean isNpcPlayer(String playerId) {
		if (HawkOSOperator.isEmptyString(playerId)) {
			return false;
		}
		return playerId.startsWith(BattleService.NPC_ID);
	}

	/**
	 * 检测禁言
	 * 
	 * @return
	 */
	public static boolean checkBanMsg(Player player) {
		long silentTime = player.getEntity().getSilentTime();
		// 禁言玩家推送禁言提示
		if (silentTime > HawkTime.getMillisecond()) {
			ChatService.getInstance().sendBanMsgNotice(player, silentTime);
			return false;
		}

		return true;
	}

	/**
	 * 获取原始头像地址
	 * 
	 * @param puid
	 * @return
	 */
	public static String getPrimitivePfIcon(String channel, String puid) {
		String pfIcon = null;
		try {
			String puidProfile = RedisProxy.getInstance().getPuidProfile(puid);
			JSONObject profileJson = JSON.parseObject(puidProfile);
			if (UserType.getByChannel(channel) == UserType.QQ) {
				pfIcon = profileJson.getString("picture100");
				if (HawkOSOperator.isEmptyString(pfIcon)) {
					pfIcon = profileJson.getString("picture40");
				}
			} else if (UserType.getByChannel(channel) == UserType.WX) {
				pfIcon = profileJson.getString("picture");
				if (!HawkOSOperator.isEmptyString(pfIcon)) {
					pfIcon = pfIcon + "/96";
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}

		return pfIcon;
	}

	/**
	 * 获取信用分
	 * 
	 * @return
	 */
	public static int queryCreditScore(Player player) {
		if (GsConfig.getInstance().isRobotMode() || isWin32Platform(player)) {
			return ControlProperty.getInstance().getCreditScore() + 1;
		}

		try {
			if (UserType.getByChannel(player.getChannel()) == UserType.GUEST) {
				return ControlProperty.getInstance().getCreditScore() + 1;
			}

			int creditScore = RedisProxy.getInstance().getCredit(player.getOpenId());
			if (creditScore != -1) {
				return creditScore;
			}

			JSONObject pfInfoJson = player.getPfTokenJson();
			String result = SDKManager.getInstance().queryCreditScore(player.getOpenId(), player.getChannel(), pfInfoJson);
			if (result == null) {
				return 0;
			}

			JSONObject json = JSONObject.parseObject(result);
			int retCode = json.getIntValue("ret");
			// ret=0且score<320，或 ret=1310不允许聊天，其他情况都允许
			if (retCode == 1310) {
				RedisProxy.getInstance().updateCredit(player.getOpenId(), "-1310"); // 不允许聊天
				return -1310;
			}

			if (retCode != 0) {
				int score = ControlProperty.getInstance().getCreditScore() + 1;
				RedisProxy.getInstance().updateCredit(player.getOpenId(), String.valueOf(score)); // 允许聊天
				return score;
			}

			JSONObject data = json.getJSONObject("data");
			// 这里因为取出来的结果要存redis，就不直接取int类型了，以免后面要再次转换
			String score = data.getString("score");
			if (score != null) {
				RedisProxy.getInstance().updateCredit(player.getOpenId(), score); // 小于320不允许聊天，大于或等于才允许
				return Integer.parseInt(score);
			}

		} catch (Exception e) {
			HawkException.catchException(e);
		}

		return 0;
	}

	/**
	 * 投递健康游戏请求任务
	 * 
	 * @param callback
	 */
	public static void postHealthGameTask(HawkCallback callback) {
		if (!GlobalData.getInstance().isHealthGameEnable()) {
			return;
		}

		HawkThreadPool threadPool = HawkTaskManager.getInstance().getThreadPool("task");
		if (threadPool == null) {
			HawkLog.logPrintln("sendHealthGameReq failed, task threadPool null");
			return;
		}

		HawkTask task = new HawkTask() {
			@Override
			public Object run() {
				try {
					callback.invoke(null);
				} catch (Exception e) {
					HawkException.catchException(e);
				}
				return null;
			}
		};

		threadPool.addTask(task);
	}

	/**
	 * 获取当前服渠道
	 * 
	 * @return
	 */
	public static String getServerChannel() {
		String channelName = null;
		try {
			String areaId = GsConfig.getInstance().getAreaId();
			Channel channel = Channel.valueOf(Integer.parseInt(areaId));
			channelName = channel.name();
		} catch (Exception e) {
			HawkException.catchException(e);
		}

		return channelName;
	}

	/**
	 * 通知装扮显示更新
	 */
	public static void notifyDressShow(String playerId) {
		WorldPoint worldPoint = WorldPlayerService.getInstance().getPlayerWorldPoint(playerId);
		if (worldPoint == null) {
			return;
		}
		WorldPointService.getInstance().getWorldScene().update(worldPoint.getAoiObjId());
	}
	
	/**
	 * 检测签名合法性
	 */
	public static boolean canSignatureUse(String signature) {
		int length = signature.length();
		if (length > ConstProperty.getInstance().getSignatureLengthLimit()) {
			return false;
		}

		if (!GameUtil.stringOnlyContain(signature, GsConst.RegexType.ALLLETTER + GsConst.RegexType.NUM + GsConst.RegexType.CHINESELETTER, null)) {
			return false;
		}

		return true;
	}

	public static boolean canArmourSuitNameUse(String signature) {
		int length = signature.length();
		if (length > ArmourConstCfg.getInstance().getSuitNameLength()) {
			return false;
		}

		if (!GameUtil.stringOnlyContain(signature, GsConst.RegexType.ALLLETTER + GsConst.RegexType.NUM + GsConst.RegexType.CHINESELETTER, null)) {
			return false;
		}

		return true;
	}

	public static boolean canGuildFormationNameUse(String signature) {
		int length = signature.length();
		String rallySetTeamName = ConstProperty.getInstance().getRallySetTeamName();
		int low = Integer.parseInt(rallySetTeamName.split("_")[0]);
		int high = Integer.parseInt(rallySetTeamName.split("_")[1]);
		if (length < low || length > high) {
			return false;
		}

		if (!GameUtil.stringOnlyContain(signature, GsConst.RegexType.ALLLETTER + GsConst.RegexType.NUM + GsConst.RegexType.CHINESELETTER, null)) {
			return false;
		}

		return true;
	}
	
	/**
	 * 组装玩家预设信息
	 * @return
	 */
	public static PlayerPresetMarchInfo.Builder makeMarchPresetBuilder(String playerId) {
		String presetMarchStr = RedisProxy.getInstance().getPlayerPresetWorldMarch(playerId);
		if (presetMarchStr == null) {
			return PlayerPresetMarchInfo.newBuilder();
		}
		PlayerPresetMarchInfo.Builder infos = PlayerPresetMarchInfo.newBuilder();
		JSONArray arr = JSONArray.parseArray(presetMarchStr);
		int i = 0;
		for (Object obj : arr) {
			i++;
			JSONObject jsonObj = (JSONObject) obj;
			if (jsonObj == null) {
				continue;
			}
			
			PresetMarchInfo.Builder marchBuilder = PresetMarchInfo.newBuilder();
			marchBuilder.setIdx(i);
			String name = (String) jsonObj.get("name");
			if (name != null) {
				marchBuilder.setName(name);
			}
			
			if (!jsonObj.containsKey("superSoldierId")) {
				infos.addMarchInfos(marchBuilder);
				continue;
			}
			
			JSONArray armyArr = (JSONArray) jsonObj.get("army");
			if (armyArr != null && !armyArr.isEmpty()) {
				for (Object armyObj : armyArr) {
					ArmySoldierPB.Builder armyBuilder = ArmySoldierPB.newBuilder();
					JSONObject armyjsonObj = (JSONObject) armyObj;
					armyBuilder.setArmyId(armyjsonObj.getIntValue("id"));
					armyBuilder.setCount(armyjsonObj.getIntValue("count"));
					marchBuilder.addArmy(armyBuilder);
				}
			}
			JSONArray heroArr = (JSONArray) jsonObj.get("hero");
			if (heroArr != null && !heroArr.isEmpty()) {
				for (Object heroObj : heroArr) {
					marchBuilder.addHeroIds((int) heroObj);
				}
			}
			Boolean per = (Boolean) jsonObj.get("percent");
			if (per != null) {
				marchBuilder.setPercentArmy(per);
			}
			Boolean chero = (Boolean) jsonObj.get("commandHero");
			if (chero != null) {
				marchBuilder.setCommandHero(chero);
			}
			Boolean same = (Boolean) jsonObj.get("sameArmy");
			if (same != null) {
				marchBuilder.setSameArmy(same);
			}
			
			Integer itemId = (Integer) jsonObj.get("itemId");
			if (itemId != null) {
				marchBuilder.setItemId(itemId);
			}
			marchBuilder.setSuperSoldierId(jsonObj.getIntValue("superSoldierId"));

			marchBuilder.setIsActivateDressGroup(jsonObj.getBooleanValue("isActivateDressGroup"));
			
			ArmourSuitType armourSuitType = ArmourSuitType.valueOf(jsonObj.getIntValue("armourSuit"));
			if (armourSuitType != null) {
				marchBuilder.setArmourSuit(armourSuitType);
			}
			
			MechaCoreSuitType machecoreSuit = MechaCoreSuitType.valueOf(jsonObj.getIntValue("mechacoreSuit"));
			if (machecoreSuit != null && machecoreSuit != MechaCoreSuitType.MECHA_NONE) {
				marchBuilder.setMechacoreSuit(machecoreSuit);
			} else {
				marchBuilder.setMechacoreSuit(MechaCoreSuitType.MECHA_ONE);
			}
			
			TalentType talent = TalentType.valueOf(jsonObj.getIntValue("talent"));
			if (talent != null) {
				marchBuilder.setTalentType(talent);
			}
			
			Integer superLab = jsonObj.getInteger("superLab");
			if (superLab != null) {
				marchBuilder.setSuperLab(superLab);
			}
			
			JSONArray dressArr = (JSONArray) jsonObj.get("dress");
			if (dressArr != null && !dressArr.isEmpty()) {
				for (Object dressObj : dressArr) {
					marchBuilder.addMarchDress((int) dressObj);
				}
			}
			PresetMarchManhattan.Builder presetMarchManhattan = PresetMarchManhattan.newBuilder();
			Integer manhattanAtkSwId = (Integer) jsonObj.get("manhattanAtkSwId");
			if (manhattanAtkSwId != null) {
				presetMarchManhattan.setManhattanAtkSwId(manhattanAtkSwId);
			}
			Integer manhattanDefSwId = (Integer) jsonObj.get("manhattanDefSwId");
			if (manhattanDefSwId != null) {
				presetMarchManhattan.setManhattanDefSwId(manhattanDefSwId);
			}
			marchBuilder.setManhattan(presetMarchManhattan.build());
			infos.addMarchInfos(marchBuilder);
		}
		return infos;
	}

	/**
	 * 获取appUrl
	 */
	public static String getAppUrl(Player player) {
		Builder hpLogin = player.getHpLogin();
		String channelId = hpLogin.getChannelId();
		AppUrlCfg appUrlCfg = HawkConfigManager.getInstance().getConfigByKey(AppUrlCfg.class, channelId);

		if (appUrlCfg == null) {
			ConfigIterator<AppUrlCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(AppUrlCfg.class);
			while (iterator.hasNext()) {
				AppUrlCfg cfg = iterator.next();
				if (cfg.isDefault()) {
					appUrlCfg = cfg;
					break;
				}
			}
		}

		if (appUrlCfg == null) {
			return "";
		}

		return appUrlCfg.getAppUrl();
	}

	/**
	 * 获取健康引导请求（公共）参数
	 * 
	 * @param player
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static Map<String, Object> getHealthReqParam(Player player) {
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("area", GsConfig.getInstance().getAreaId());
		if (player == null) {
			paramMap.put("partition", GsConfig.getInstance().getServerId());
			paramMap.put("channel", GameUtil.getServerChannel());
		} else {
			paramMap.put("partition", player.getServerId());
			paramMap.put("platform", player.getPlatform());
			paramMap.put("channel", player.getChannel());
		}

		return paramMap;
	}

	/**
	 * 分割坐标列表
	 */
	public static List<int[]> splitPosList(String pos) {
		List<int[]> posList = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(pos)) {
			String[] posArrayStr = pos.split(",");
			for (int i = 0; i < posArrayStr.length; i++) {
				String[] singlePosStr = posArrayStr[i].split("_");
				int[] singlePos = new int[2];
				singlePos[0] = Integer.parseInt(singlePosStr[0]);
				singlePos[1] = Integer.parseInt(singlePosStr[1]);
				posList.add(singlePos);
			}
		}
		return posList;
	}

	/**
	 * 合并作用号
	 */
	public static void mergeEffval(Map<EffType, Integer> effect, Map<EffType, Integer> mergeEffect) {
		for (Entry<EffType, Integer> merge : mergeEffect.entrySet()) {
			effect.merge(merge.getKey(), merge.getValue(), (v1, v2) -> v1 + v2);
		}
	}

	/**
	 * 获取铠甲作用号
	 */
	public static Map<EffType, Integer> getArmourEffect(PlayerData playerData, ArmourEntity armour,int redCount) {
		Map<EffType, Integer> effect = new HashMap<>();
		
		// 基础属性
		ArmourCfg armourCfg = HawkConfigManager.getInstance().getConfigByKey(ArmourCfg.class, armour.getArmourId());
		if (armourCfg != null) {
			for (EffectObject eff : armourCfg.getBaseAttrList()) {
				ArmourLevelCfg armourLevelCfg = HawkConfigManager.getInstance().getConfigByKey(ArmourLevelCfg.class, armour.getLevel());
				if (armourLevelCfg == null) {
					continue;
				}
				ArmourBreakthroughCfg armourQualityCfg = HawkConfigManager.getInstance().getConfigByKey(ArmourBreakthroughCfg.class, armour.getQuality());
				if (armourQualityCfg == null) {
					continue;
				}
				
				// 基础
				int value = (int) Math.ceil(1L * eff.getEffectValue() * armourLevelCfg.getLevelGrowUp() * armourQualityCfg.getBreakGrowUp() * GsConst.EFF_PER * GsConst.EFF_PER);
				
				// 量子
				if(armour.getQuantum() >= ArmourConstCfg.getInstance().getQuantumRedLevel()){
					value = (int) Math.ceil(1L * eff.getEffectValue() * armourLevelCfg.getLevelGrowUp() * ArmourConstCfg.getInstance().getBreakGrowUpRed() * GsConst.EFF_PER * GsConst.EFF_PER);
				}
				// 翻倍
				
				// 装备研究
				int researchValue = getEquipResearchFixAttr(playerData);
				int effstZhuVal = getStZhuVal(playerData, armourCfg); // 策划要求和装备研究值相加计算
				value = (int) Math.ceil(value * GsConst.EFF_PER * (10000 + researchValue + effstZhuVal));
				effect.merge(eff.getType(), value, (v1, v2) -> v1 + v2);
			}
		}

		// 额外属性
		for (ArmourEffObject extrEff : armour.getExtraAttrEff()) {
			int extrValue = extrEff.getEffectValue();
			
			// 翻倍
			
			// 1602
			int eff1602 = getEff1602Val(playerData, armour, extrEff);
			extrValue = (int) Math.ceil(extrValue * (10000 + eff1602 ) * GsConst.EFF_PER);
			
			// 装备研究
			int researchValue = getEquipResearchRandAttr(playerData);
			int effstVal = getStVal(playerData, armourCfg); // 策划要求参考1602  即与装备研究分开相乘
			int eff11034 = playerData.getEffVal(EffType.ARMOUR_STAR_EXPLORE_11034) * redCount;
			int eff11035 = playerData.getEffVal(EffType.ARMOUR_STAR_EXPLORE_11035);
			int value = (int) Math.ceil(extrValue* GsConst.EFF_PER * (10000 + researchValue + effstVal + eff11034 + eff11035) );
			effect.merge(EffType.valueOf(extrEff.getEffectType()), value, (v1, v2) -> v1 + v2);
			
		}

		// 特技属性
		for (ArmourEffObject skillEff : armour.getSkillEff()) {
			effect.merge(EffType.valueOf(skillEff.getEffectType()), skillEff.getEffectValue(), (v1, v2) -> v1 + v2);
		}

		// 星级属性
		ArmourStarCfg armourStarCfg = AssembleDataManager.getInstance().getArmourStarCfg(armour.getArmourId(), armour.getStar());
		if (armourStarCfg != null) {
			for (EffectObject eff : armourStarCfg.getStarEff()) {
				// 装备泰晶实际属性 = 泰晶基础配置 * (1+11001作用值/10000)
				int finalVal = (int)Math.ceil(eff.getEffectValue() * (1 + playerData.getEffVal(EffType.ARMOUR_11001) * GsConst.EFF_PER));
				effect.merge(eff.getType(), finalVal, (v1, v2) -> v1 + v2);
			}
		}
		
		// 星级额外属性
		for (ArmourEffObject starExtrEff : armour.getStarEff()) {
			ArmourChargeLabCfg cfg = HawkConfigManager.getInstance().getConfigByKey(ArmourChargeLabCfg.class, starExtrEff.getAttrId());
			EffectObject attributeValue = cfg.getAttributeValue();
			int effectType = attributeValue.getEffectType();
			int rate = starExtrEff.getRate();
			// 装备充能实际属性 = 充能基础属性 * 充能进度/10000 * (1+11002作用值/10000)
			int realEffectValue = (int)Math.ceil(attributeValue.getEffectValue() * rate * GsConst.EFF_PER * (1 + playerData.getEffVal(EffType.ARMOUR_11002) * GsConst.EFF_PER));
			effect.merge(EffType.valueOf(effectType), realEffectValue, (v1, v2) -> v1 + v2);
		}
		
		// 量子槽位属性
		ArmourQuantumCfg armourQuantumCfg = AssembleDataManager.getInstance().getArmourQuantumCfg(armour.getArmourId(), armour.getQuantum());
		if (armourQuantumCfg != null) {
			for (EffectObject eff : armourQuantumCfg.getQuantumEff()) {
				// 装备泰晶实际属性 = 泰晶基础配置 * (1+11001作用值/10000)
				int effVal = playerData.getEffVal(EffType.ARMOUR_11001);
				if (eff.getShowType() == 1) {
					effVal += playerData.getEffVal(EffType.ARMOUR_STAR_EXPLORE_11028);
					effVal += playerData.getEffVal(EffType.ARMOUR_STAR_EXPLORE_11031)* redCount;
				}
				if (eff.getShowType() == 2) {
					effVal += playerData.getEffVal(EffType.ARMOUR_STAR_EXPLORE_11029);
					effVal += playerData.getEffVal(EffType.ARMOUR_STAR_EXPLORE_11032)* redCount;
				}
				int finalVal = (int)Math.ceil(eff.getEffectValue() * (1 + effVal * GsConst.EFF_PER));
				effect.merge(eff.getType(), finalVal, (v1, v2) -> v1 + v2);
			}
		}
		
		return effect;
	}
	
	private static int getStZhuVal(PlayerData playerData, ArmourCfg armourCfg) {
		int val = 0;
		switch (armourCfg.getPos()) {
		case ArmourCfg.POS1:
			val = playerData.getEffVal(EffType.ARMOUR_STAR_EXPLORE_11014);
			break;
		case ArmourCfg.POS2:
			val = playerData.getEffVal(EffType.ARMOUR_STAR_EXPLORE_11015);
			break;
		case ArmourCfg.POS3:
			val = playerData.getEffVal(EffType.ARMOUR_STAR_EXPLORE_11016);
			break;
		case ArmourCfg.POS4:
			val = playerData.getEffVal(EffType.ARMOUR_STAR_EXPLORE_11017);
			break;
		case ArmourCfg.POS5:
			val = playerData.getEffVal(EffType.ARMOUR_STAR_EXPLORE_11018);
			break;
		case ArmourCfg.POS6:
			val = playerData.getEffVal(EffType.ARMOUR_STAR_EXPLORE_11019);
			break;
		case ArmourCfg.POS7:
			val = playerData.getEffVal(EffType.ARMOUR_STAR_EXPLORE_11020);
			break;

		default:
			break;
		}
		return val;
	}

	private static int getStVal(PlayerData playerData, ArmourCfg armourCfg) {
		int val = 0;
		switch (armourCfg.getPos()) {
		case ArmourCfg.POS1:
			val = playerData.getEffVal(EffType.ARMOUR_STAR_EXPLORE_11021);
			break;
		case ArmourCfg.POS2:
			val = playerData.getEffVal(EffType.ARMOUR_STAR_EXPLORE_11022);
			break;
		case ArmourCfg.POS3:
			val = playerData.getEffVal(EffType.ARMOUR_STAR_EXPLORE_11023);
			break;
		case ArmourCfg.POS4:
			val = playerData.getEffVal(EffType.ARMOUR_STAR_EXPLORE_11024);
			break;
		case ArmourCfg.POS5:
			val = playerData.getEffVal(EffType.ARMOUR_STAR_EXPLORE_11025);
			break;
		case ArmourCfg.POS6:
			val = playerData.getEffVal(EffType.ARMOUR_STAR_EXPLORE_11026);
			break;
		case ArmourCfg.POS7:
			val = playerData.getEffVal(EffType.ARMOUR_STAR_EXPLORE_11027);
			break;

		default:
			break;
		}
		return val;
	}

	private static int getEff1602Val(PlayerData playerData, ArmourEntity armour, ArmourEffObject extrEff) {
		int extrId = extrEff.getAttrId();
		int extrValue = extrEff.getEffectValue();
		int eff1602 = 0;
		ArmourAdditionalCfg additCfg = HawkConfigManager.getInstance().getConfigByKey(ArmourAdditionalCfg.class, extrId);
		if (additCfg == null) {
			return 0;
		}

		int perfectValue = AssembleDataManager.getInstance().getArmourPerfectAttr(additCfg.getQuality(), extrEff.getEffectType());
		if (armour.isSuper()) {
			perfectValue = AssembleDataManager.getInstance().getArmourSuperPerfectAttr(additCfg.getQuality(), extrEff.getEffectType());
		}

		if (extrValue == perfectValue) {
			eff1602 = playerData.getEffVal(EffType.ARMOUR_1602);
		}
		return eff1602;
	}

	/**
	 * 获取铠甲套装作用号
	 */
	public static Map<EffType, Integer> getArmourSuitEffect(PlayerData playerData, int armourSuitId, List<ArmourEntity> armours) {
		Map<EffType, Integer> effect = new HashMap<>();

		ArmourSuitCfg armourSuitCfg = HawkConfigManager.getInstance().getConfigByKey(ArmourSuitCfg.class, armourSuitId);
		if (armourSuitCfg == null) {
			return effect;
		}

		int suitTochIndex = ArmourConstCfg.getInstance().getSuitCombination(armours.size());
		if (suitTochIndex <= 0) {
			return effect;
		}
		int redCount = 0;
		int redlevel = ArmourConstCfg.getInstance().getQuantumRedLevel();
		for(ArmourEntity armour : armours){
			if(armour.getQuantum() >= redlevel){
				redCount++;
			}
		}
		
		int count = 0;
		int minQuality = 0;
		for (int i = suitTochIndex; i > 0; i--) {
			
			List<EffectObject> touchSuitEffect = new ArrayList<>();
			switch (i) {
			case 1:
				count = ArmourConstCfg.getInstance().getSuitCombination().get(i - 1);
				minQuality = getCountMinQuality(armours, count);
				if(redCount >= count){
					touchSuitEffect = armourSuitCfg.getSuitQuantumEff1();
				}else {
					touchSuitEffect = armourSuitCfg.getSuitAttribute1Eff(minQuality);
				}
				break;
				
			case 2:
				count = ArmourConstCfg.getInstance().getSuitCombination().get(i - 1);
				minQuality = getCountMinQuality(armours, count);

				if(redCount >= count){
					touchSuitEffect = armourSuitCfg.getSuitQuantumEff2();
				}else {
					touchSuitEffect = armourSuitCfg.getSuitAttribute2Eff(minQuality);
				}
				break;
				
			case 3:
				count = ArmourConstCfg.getInstance().getSuitCombination().get(i - 1);
				minQuality = getCountMinQuality(armours, count);

				if(redCount >= count){
					touchSuitEffect = armourSuitCfg.getSuitQuantumEff3();
				}else {
					touchSuitEffect = armourSuitCfg.getSuitAttribute3Eff(minQuality);
				}
				break;
			
			case 4:
				if (playerData.getEffVal(EffType.ARMOUR_1603) > 0) {
					count = ArmourConstCfg.getInstance().getSuitCombination().get(i - 1);
					minQuality = getCountMinQuality(armours, count);

					if(redCount >= count){
						touchSuitEffect = armourSuitCfg.getSuitQuantumEff4();
					}else {
						touchSuitEffect = armourSuitCfg.getSuitAttribute4Eff(minQuality);
					}
				}
				break;
				
			default:
				break;
			}
			
			for (EffectObject eff : touchSuitEffect) {
				Integer value = effect.get(eff.getType());
				if (value == null) {
					effect.put(eff.getType(), eff.getEffectValue());
				} else {
					effect.put(eff.getType(), eff.getEffectValue() + value);
				}
			}
		}

		// 计算装备科技作用号
		int researchValue = getEquipResearchSuitAttr(playerData);
		Map<EffType, Integer> effectRet = new HashMap<>();
		for (Entry<EffType, Integer> eff : effect.entrySet()) {
			int value = (int) Math.ceil(eff.getValue() * (10000 + researchValue) * GsConst.EFF_PER);
			effectRet.put(eff.getKey(), value);
		}
		return effectRet;
	}

	/**
	 * 获取铠甲中前index件中的最低品质
	 */
	public static int getCountMinQuality(List<ArmourEntity> armours, int index) {
		int qualityCount = HawkConfigManager.getInstance().getConfigSize(ArmourBreakthroughCfg.class);
		int[] qualityArray = new int[qualityCount];
		for (ArmourEntity armour : armours) {
			qualityArray[armour.getQuality() - 1] = qualityArray[armour.getQuality() - 1] + 1; 
		}
		
		
		for (int i = qualityCount - 1; i >= 0; i--) {
			index = index - qualityArray[i];
			if (index <= 0) {
				return i + 1;
			}
		}
		
		return 1;
	}
	
	/**
	 * 获取铠甲战力
	 */
	public static int getArmourPower(ArmourEntity armour) {
		int power = 0;

		ArmourLevelCfg armourLevelCfg = HawkConfigManager.getInstance().getConfigByKey(ArmourLevelCfg.class, armour.getLevel());
		power += armourLevelCfg.getArmourCombat();

		ArmourBreakthroughCfg armourQualityCfg = HawkConfigManager.getInstance().getConfigByKey(ArmourBreakthroughCfg.class, armour.getQuality());
		power += armourQualityCfg.getArmourCombat();

		for (ArmourEffObject extraAttr : armour.getExtraAttrEff()) {
			ArmourAdditionalCfg armourAttrCfg = HawkConfigManager.getInstance().getConfigByKey(ArmourAdditionalCfg.class, extraAttr.getAttrId());
			power += (int) (1L * extraAttr.getEffectValue() * armourAttrCfg.getArmourCombat() * GsConst.EFF_PER);
		}

		for (ArmourEffObject skillAttr : armour.getSkillEff()) {
			ArmourAdditionalCfg armourAttrCfg = HawkConfigManager.getInstance().getConfigByKey(ArmourAdditionalCfg.class, skillAttr.getAttrId());
			power += (int) (1L * skillAttr.getEffectValue() * armourAttrCfg.getArmourCombat() * GsConst.EFF_PER);
		}

		// 星级属性战力
		ArmourStarConsumeCfg armourStarCfg = HawkConfigManager.getInstance().getConfigByKey(ArmourStarConsumeCfg.class, armour.getStar());
		if (armourStarCfg != null) {
			power += armourStarCfg.getStarPower();
		}
		
		// 星级额外属性战力
		for (ArmourEffObject starExtrEff : armour.getStarEff()) {
			ArmourChargeLabCfg charegeLabCfg = HawkConfigManager.getInstance().getConfigByKey(ArmourChargeLabCfg.class	, starExtrEff.getAttrId());
			int rate = starExtrEff.getRate();
			power += Math.ceil(rate * charegeLabCfg.getAttributePower());
		}

		// 量子槽位战力
		ArmourQuantumConsumeCfg armourQuantumCfg = HawkConfigManager.getInstance().getConfigByKey(ArmourQuantumConsumeCfg.class, armour.getQuantum());
		if (armourQuantumCfg != null) {
			power += armourQuantumCfg.getPower();
		}
		return power;
	}

	/**
	 * 获取铠甲套装战力
	 */
	public static int getArmourSuitPower(PlayerData playerData, List<ArmourEntity> armours) {
		int power = 0;
		
		try {
			
			int suitTochIndex = ArmourConstCfg.getInstance().getSuitCombination(armours.size());
			if (suitTochIndex <= 0) {
				return power;
			}
			
			for (int i = suitTochIndex; i > 0; i--) {
				
				int count = 0;
				int minQuality = 0;
				
				switch (i) {
				case 1:
					count = ArmourConstCfg.getInstance().getSuitCombination().get(i - 1);
					minQuality = getCountMinQuality(armours, count);
					break;
					
				case 2:
					count = ArmourConstCfg.getInstance().getSuitCombination().get(i - 1);
					minQuality = getCountMinQuality(armours, count);
					break;
					
				case 3:
					count = ArmourConstCfg.getInstance().getSuitCombination().get(i - 1);
					minQuality = getCountMinQuality(armours, count);
					break;
				
				case 4:
					if (playerData.getEffVal(EffType.ARMOUR_1603) > 0) {
						count = ArmourConstCfg.getInstance().getSuitCombination().get(i - 1);
						minQuality = getCountMinQuality(armours, count);
					}
					break;
					
				default:
					break;
				}
				
				if (count == 0 || minQuality == 0) {
					continue;
				}
				
				ArmourBreakthroughCfg armourQualityCfg = HawkConfigManager.getInstance().getConfigByKey(ArmourBreakthroughCfg.class, minQuality);
				if (armourQualityCfg == null) {
					continue;
				}
				
				int attrSuit = ArmourConstCfg.getInstance().getSuitCombination(armours.size());
				if (attrSuit > 0) {
					power += armourQualityCfg.getSuitCombination(attrSuit - 1);
				}
			}
				
		} catch(Exception e) {
			HawkException.catchException(e);
		}

		return power;
	}
	
	/**
	 * 修改内容CD时间检测
	 * 
	 * @return -1 表示CD时间未结束（不能修改）; 0表是其它情况; 1表示上次已修改过且CD时间已结束（后面将CD清零）
	 */
	public static int changeContentCDCheck(String objectId, ChangeContentType type) {
		int cdTimeLong = RedisProxy.getInstance().getChangeContentCDTime(objectId, type);
		// CD时长大于0
		if (cdTimeLong > 0) {
			long lastChangeTime = RedisProxy.getInstance().getChangeContentTime(objectId, type);
			// CD时间未结束
			if (lastChangeTime > 0 && HawkApp.getInstance().getCurrentTime() - lastChangeTime < cdTimeLong * 1000L) {
				return -1;
			} else if (lastChangeTime > 0) {  // 只要已经记过修改时间，且CD时间结束，就将CD时间清零
				return 1;
			}
		}
		
		return 0;
	}
	
	/**
	 * 修改玩家角色活跃服信息
	 * 
	 * @param playerId
	 * @param serverId
	 */
	public static void updateActiveServer(String playerId, String serverId) {
		AccountRoleInfo accountRoleInfo = GlobalData.getInstance().getAccountRoleInfo(playerId);
		if (accountRoleInfo == null) {
			HawkLog.logPrintln("updateActiveServer failed, accountRoleInfo not exist, playerId: {}, serverId: {}", playerId, serverId);
			return;
		}
		
		accountRoleInfo.setActiveServer(serverId);
		GlobalData.getInstance().addOrUpdateAccountRoleInfo(accountRoleInfo);
	}
	
	/**
	 * 此方法不通用和 {@link HawkUUIDGenerator#genUUID() 牢牢绑定的}
	 * @param playerId
	 * @return
	 */
	public static String getServerIdFromPlayerId(String playerId) {
		if (HawkOSOperator.isEmptyString(playerId)) {
			throw new RuntimeException("playerId can not be null or empty");
		}
		String[] idPartsArray = playerId.split("-");
		if (idPartsArray.length != 3) {
			throw new RuntimeException(String.format("playerId:%s is incorrect", playerId));
		}
		
		//serverId 不能有字符.
		int serverId = HawkNumberConvert.convertInt(idPartsArray[0]);
		
		return serverId + "";
	}
	
	/**
	 * 这里取出来的官职ID包含星球大战, 国王战.
	 * 星球大战覆盖国王战.
	 * @param playerId
	 * @return
	 */
	public static int getOfficerId(String playerId) {
		int officerId = StarWarsOfficerService.getInstance().getPlayerOfficerId(playerId);
		if (officerId > 0) {
			return officerId;
		}

		officerId = getPresidentOfficerId(playerId);

		return officerId;
	}

	public static int getPresidentOfficerId(String playerId) {
		boolean isCrossKing = PresidentOfficier.getInstance().isCrossKing(playerId);
		if (isCrossKing) {
			return OfficerType.OFFICER_CROSS_PRESIDENT_VALUE;
		}

		int officerId = PresidentOfficier.getInstance().getOfficerId(playerId);

		if (CrossService.getInstance().isImmigrationPlayer(playerId)) {
			officerId = RedisProxy.getInstance().getPlayerOfficerId(playerId);
		}
		return officerId;
	}
	
	
	public static Set<Integer> getAllOfficerIdSet(String playerId){
		Set<Integer> set = new HashSet<>();
		//大底站职位
		int starWarofficerId = StarWarsOfficerService.getInstance().getPlayerOfficerId(playerId);
		if(starWarofficerId > 0){
			set.add(starWarofficerId);
		}
		//当前服职务
		int officerId = PresidentOfficier.getInstance().getOfficerId(playerId);
		if(officerId != OfficerType.OFFICER_00_VALUE){
			set.add(officerId);
		}
		//跨服盟主
		boolean isCrossKing = PresidentOfficier.getInstance().isCrossKing(playerId);
		if (isCrossKing) {
			set.add(OfficerType.OFFICER_CROSS_PRESIDENT_VALUE);
		}
		return set;
	}
	
	/**
	 * 判断行军表情包是否已解锁
	 * 
	 * @param emoticon
	 * @return
	 */
	public static boolean isMarchEmoticonBagUnlocked(Player player, int emoticon) {
		CustomDataEntity customData = player.getData().getCustomDataEntity(GsConst.MARCH_EMOTICON_BAG);
		if (customData == null || HawkOSOperator.isEmptyString(customData.getArg())) {
			return false;
		}
		
		String[] emoticonBags = customData.getArg().split(",");
		for (String bag : emoticonBags) {
			if (emoticon == Integer.parseInt(bag)) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * 当日已使用次数 （对于每日限定使用一定次数，跨天重置类场景的问题，都可以使用此接口）
	 *  
	 * @param playerId
	 * @param type
	 * @return
	 */
	public static int getTodayUseTimes(String playerId, DailyResetUseTimesType type) {
		HawkTuple2<Long, Integer> tuple = RedisProxy.getInstance().getDaiyResetUseTimesInfo(type.name(), playerId);
		int useTimes = tuple.second;
		if (useTimes > 0) {
			useTimes = HawkTime.isSameDay(HawkTime.getMillisecond(), tuple.first) ? useTimes : 0;
		}
		
		return useTimes; 
	}

	/**
	 * 从uuid获取serverId
	 * 
	 * @param uuid
	 * @return
	 */
	public static String strUUID2ServerId(String uuid) {
		String[] values = uuid.split("-");
		if (values.length != 3) {
			throw new InvalidParameterException("incorrect uuid: " + uuid);
		}
		
		int serverId = HawkNumberConvert.convertInt(values[0]);
		return String.valueOf(serverId);
	}
	
	/**
	 * 获取装备科技固定条目属性加成
	 */
	public static int getEquipResearchFixAttr(PlayerData playerData) {
		int value = 0;
		List<EquipResearchEntity> entities = playerData.getEquipResearchEntityList();
		for (EquipResearchEntity entity : entities) {
			int researchId = entity.getResearchId();
			int researchLevel = entity.getResearchLevel();
			for (int i = 1; i <= researchLevel; i++) {
				EquipResearchLevelCfg cfg = AssembleDataManager.getInstance().getEquipResearchLevelCfg(researchId, i);
				if (cfg == null) {
					continue;
				}
				value += cfg.getFixAttr();
			}
		}
		return value;
	}
	
	/**
	 * 获取装备科技额外条目属性加成
	 */
	public static int getEquipResearchRandAttr(PlayerData playerData) {
		int value = 0;
		List<EquipResearchEntity> entities = playerData.getEquipResearchEntityList();
		for (EquipResearchEntity entity : entities) {
			int researchId = entity.getResearchId();
			int researchLevel = entity.getResearchLevel();
			for (int i = 1; i <= researchLevel; i++) {
				EquipResearchLevelCfg cfg = AssembleDataManager.getInstance().getEquipResearchLevelCfg(researchId, i);
				if (cfg == null) {
					continue;
				}
				value += cfg.getRandAttr();
			}
		}
		return value;
	}
	
	/** 
	 * 获取装备科技套装条目属性加成
	 */
	public static int getEquipResearchSuitAttr(PlayerData playerData) {
		int value = 0;
		List<EquipResearchEntity> entities = playerData.getEquipResearchEntityList();
		for (EquipResearchEntity entity : entities) {
			int researchId = entity.getResearchId();
			int researchLevel = entity.getResearchLevel();
			for (int i = 1; i <= researchLevel; i++) {
				EquipResearchLevelCfg cfg = AssembleDataManager.getInstance().getEquipResearchLevelCfg(researchId, i);
				if (cfg == null) {
					continue;
				}
				value += cfg.getSuitAttr();
			}
		}
		return value;
	}
	
	/**
	 * 获取装备科技作用号
	 */
	public static Map<Integer, Integer> getEquipResearchEff(PlayerData playerData) {
		Map<Integer, Integer> effect = new HashMap<>();
		List<EquipResearchEntity> entities = playerData.getEquipResearchEntityList();
		for (EquipResearchEntity entity : entities) {
			int researchId = entity.getResearchId();
			int researchLevel = entity.getResearchLevel();
			for (int i = 1; i <= researchLevel; i++) {
				EquipResearchLevelCfg cfg = AssembleDataManager.getInstance().getEquipResearchLevelCfg(researchId, i);
				if (cfg == null) {
					continue;
				}
				List<EffectObject> attrEff = cfg.getAttrEff();
				for (EffectObject attr : attrEff) {
					Integer before = effect.getOrDefault(attr.getEffectType(), 0);
					effect.put(attr.getEffectType(), before + attr.getEffectValue());
				}
			}
			
			// 宝箱作用号
			Set<Integer> receiveBoxSet = entity.getReceiveBoxSet();
			for (int box : receiveBoxSet) {
				EquipResearchRewardCfg rewardCfg = AssembleDataManager.getInstance().getEquipResearchRewardCfg(entity.getResearchId(), box);
				if (rewardCfg == null) {
					continue;
				}
				for (EffectObject eff : rewardCfg.getEffTouchList()) {
					Integer before = effect.getOrDefault(eff.getEffectType(), 0);
					effect.put(eff.getEffectType(), before + eff.getEffectValue());
				}
			}
		}
		
		return effect;
	}
	
	/**
	 * 装备科技外显是否解锁
	 */
	public static int isEquipResearchShowUnlock(Player player) {
		int levelMaxCount = 0;
		List<EquipResearchEntity> entities = player.getData().getEquipResearchEntityList();
		for (EquipResearchEntity entity : entities) {
			EquipResearchCfg researchCfg = HawkConfigManager.getInstance().getConfigByKey(EquipResearchCfg.class, entity.getResearchId());
			// 二期的装备科技不算在内
			if (researchCfg != null && researchCfg.getPhaseTwo() > 0) {
				continue;
			}
			// 判断是否是最大等级
			EquipResearchLevelCfg cfg = AssembleDataManager.getInstance().getEquipResearchLevelCfg(entity.getResearchId(), entity.getResearchLevel() + 1);
			if (cfg != null) {
				continue;
			}
			levelMaxCount++;
		}
		
		int ret = 0;
		List<Integer> equipResearchShowUnlockList = ConstProperty.getInstance().getEquipResearchShowUnlockList();
		for (int i = 0; i < equipResearchShowUnlockList.size(); i++) {
			if (levelMaxCount >= equipResearchShowUnlockList.get(i)) {
				ret = i + 1;
			}
		}
		return ret;
	}

	/**
	 * 通知点燃烟花效果更新
	 */
	public static void notifyFireWorksShow(String playerId) {
		WorldPoint worldPoint = WorldPlayerService.getInstance().getPlayerWorldPoint(playerId);
		if (worldPoint == null) {
			return;
		}
		WorldPointService.getInstance().getWorldScene().update(worldPoint.getAoiObjId());
	}
	
	/**
	 * 每日定时日志 
	 */
	public static void dailyLog() {
		try {
			// 区服id
			String serverId = GsConfig.getInstance().getServerId();
			// 合服列表
			List<String> mergedServerList = AssembleDataManager.getInstance().getMergedServerList(serverId);
			if (mergedServerList == null) {
				mergedServerList = new ArrayList<>();
			}
			// 服务器开服时间
			String serverOpenTime = HawkTime.formatTime(GsApp.getInstance().getServerOpenTime());
			// 个人战力第一
			long playerPowerOne = 0;
			// 个人战力前十总和
			long playerPowerTen = 0;
			List<RankInfo> playerPowerRank = RankService.getInstance().getRankCache(RankType.PLAYER_FIGHT_RANK);
			for (RankInfo rankInfo : playerPowerRank) {
				if (rankInfo.getRank() == 1) {
					playerPowerOne = rankInfo.getRankInfoValue();
				}
				if (rankInfo.getRank() <= 10) {
					playerPowerTen += rankInfo.getRankInfoValue();
				}
			}
			
			// 个人基地等级第一
			long playerCityOne = 0;
			// 个人基地等级前十总和
			long playerCityTen = 0;
			List<RankInfo> playerCityRank = RankService.getInstance().getRankCache(RankType.PLAYER_CASTLE_KEY);
			for (RankInfo rankInfo : playerCityRank) {
				if (rankInfo.getRank() == 1) {
					playerCityOne = rankInfo.getRankInfoValue();
				}
				if (rankInfo.getRank() <= 10) {
					playerCityTen += rankInfo.getRankInfoValue();
				}
			}
			
			// 个人等级第一
			long playerLevelOne = 0;
			// 个人等级前十总和
			long playerLevelTen = 0;
			List<RankInfo> playerLevelRank = RankService.getInstance().getRankCache(RankType.PLAYER_GRADE_KEY);
			for (RankInfo rankInfo : playerLevelRank) {
				if (rankInfo.getRank() == 1) {
					playerLevelOne = rankInfo.getRankInfoValue();
				}
				if (rankInfo.getRank() <= 10) {
					playerLevelTen += rankInfo.getRankInfoValue();
				}
			}
			
			// 联盟战力第一
			long guildPowerOne = 0;
			// 联盟战力前十总和
			long guildPowerTen = 0;
			List<RankInfo> guildPowerRank = RankService.getInstance().getRankCache(RankType.ALLIANCE_FIGHT_KEY);
			for (RankInfo rankInfo : guildPowerRank) {
				if (rankInfo.getRank() == 1) {
					guildPowerOne = rankInfo.getRankInfoValue();
				}
				if (rankInfo.getRank() <= 10) {
					guildPowerTen += rankInfo.getRankInfoValue();
				}
			}
			
			LogUtil.logDailyStatistic(serverId, StringUtils.join(mergedServerList, ","), serverOpenTime, playerPowerOne, playerPowerTen, playerCityOne, playerCityTen, playerLevelOne, playerLevelTen, guildPowerOne, guildPowerTen);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 每日上报排行榜数据【20250228之后开的新服，每天0点上报排行榜中：1-100名的联盟战斗力排行榜、联盟消灭敌军排行榜、指挥官战斗力排行榜、消灭敌军数排行榜、指挥官基地排行榜、指挥官等级排行榜、去兵战斗力排行榜】
	 */
	public static void dailyRankLog(boolean debug) {
		long serverOpenTime = GsApp.getInstance().getServerOpenTime();
		long gap = GsApp.getInstance().getCurrentTime() - serverOpenTime;
		//开服时间在 2025-02-28 00:00:00 之前的，或者距离开服时间已经过了1个月，就不用上报了
		if (!debug && (serverOpenTime < 1740672000000L || gap > HawkTime.DAY_MILLI_SECONDS * 30)) {
			return;
		}
		
		List<RankType> specialTypes = Arrays.asList(RankType.PLAYER_FIGHT_RANK, RankType.PLAYER_NOARMY_POWER_RANK, RankType.PLAYER_KILL_ENEMY_RANK);
		for (RankType type : GsConst.PERSONAL_RANK_TYPE) {
			dailyRankLog(type, true, specialTypes);
		}
		
		for (RankType type : GsConst.GUILD_RANK_TYPE) {
			dailyRankLog(type, false, specialTypes);
		}
	}
	
	/**
	 * 每日上报排行榜数据
	 * @param type
	 * @param personal
	 */
	private static void dailyRankLog(RankType type, boolean personal, List<RankType> specialTypes) {
		int max = 30;
		try {
			List<RankInfo> rankInfoList = RankService.getInstance().getRankCache(type);
			if (specialTypes.contains(type)) {
				rankInfoList = getSortedRank(type, max * 10);
			}
			
			for (int index = 0; index < max; index++) {
				if (index >= 10 && !specialTypes.contains(type)) {
					return;
				}
				
				int start = index * 10 + 1, end = index * 10 + 10, gap = index * 50 + 10;
				HawkTuple2<Integer, String> tuple = getRankLogInfo(type, rankInfoList, start, end, personal);
				String logInfo = tuple.second;
				if (HawkOSOperator.isEmptyString(logInfo)) {
					continue;
				}
				
				int rankEnd = index * 10 + tuple.first;
				HawkTaskManager.getInstance().postTask(new HawkDelayTask(gap, gap, 1) {
		        	@Override
		        	public Object run() {
		        		LogParam logParam = LogUtil.getNonPersonalLogParam(LogInfoType.daily_rank_log);
		    			logParam.put("rankType", type.getNumber())
		    					.put("rankStart", start)
		    					.put("rankEnd", rankEnd)
		    					.put("logInfo", logInfo);
		    			GameLog.getInstance().info(logParam);
		        		return null;
		        	}
		        });
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 获取排行榜上报信息
	 * @param rankType
	 * @param rankStart
	 * @param rankEnd
	 * @return
	 */
	private static HawkTuple2<Integer, String> getRankLogInfo(RankType type, List<RankInfo> rankInfoList, int rankStart, int rankEnd, boolean personal) {
		StringJoiner sj = new StringJoiner(",");
		int count = 0;
		for (RankInfo info : rankInfoList) {
			if (info.getRank() < rankStart || info.getRank() > rankEnd) {
				continue;
			}

			//战力大于等于150万，去兵战力大于等于100万，杀敌大于等于10万
			long value = info.getRankInfoValue();
			if (type == RankType.PLAYER_FIGHT_RANK && value < 1500000) {
				continue;
			}
			if (type == RankType.PLAYER_NOARMY_POWER_RANK && value < 1000000) {
				continue;
			}
			if (type == RankType.PLAYER_KILL_ENEMY_RANK && value < 100000) {
				continue;
			}
			
			StringBuilder sb = new StringBuilder();

			//个人：roleID、出生服和昵称【rank_id_name_serverid】；联盟：id和联盟名称【rank_id_name】
			sb.append(info.getRank()).append("_").append(info.getId()).append("_");
			if (personal) {
				AccountRoleInfo roleInfo = GlobalData.getInstance().getAccountRoleInfo(info.getId());
				sb.append(info.getPlayerName()).append("_").append(roleInfo == null ? "null" : roleInfo.getServerId());
			} else {
				sb.append(info.getAllianceName());
			}
			sb.append("_").append(value);
			sj.add(sb.toString());
			count++;
		}
		
		return new HawkTuple2<Integer, String>(count, sj.toString());
	}
	
	/**
	 * 获取排行榜信息
	 * @param rankType
	 * @param count
	 * @return
	 */
	private static List<RankInfo> getSortedRank(RankType rankType, int count) {
		Set<Tuple> rankSet = LocalRedis.getInstance().getRankList(rankType, count);
		int rank = 1, idx = 0;
		String[] playerIds = new String[rankSet.size()];
		for (Tuple tuple : rankSet) {
			playerIds[idx++] = tuple.getElement();
		}

		List<RankInfo> rankList = new ArrayList<>(rankSet.size());
		Map<String, Player> snapshotMap = GlobalData.getInstance().getPlayerMap(playerIds);
		for (Tuple tuple : rankSet) {
			try {
				String playerId = tuple.getElement();
				Player playerInfo = snapshotMap.get(playerId);
				if (playerInfo == null || GameUtil.isNpcPlayer(playerId)) {
					continue;
				}
				
				GuildInfoObject guildInfo = null;
				if (playerInfo.hasGuild()) {
					guildInfo = GuildService.getInstance().getGuildInfoObject(playerInfo.getGuildId());
				}
				
				long realScore = RankService.getInstance().getRealScore(rankType, (long) tuple.getScore());
				
				RankInfo.Builder rankInfo = RankInfo.newBuilder();
				rankInfo.setRankType(rankType);
				rankInfo.setId(tuple.getElement());
				rankInfo.setPlayerName(playerInfo.getName());
				rankInfo.setIcon(playerInfo.getIcon());
				rankInfo.setVipLevel(playerInfo.getVipLevel());
				rankInfo.setCommon(BuilderUtil.genPlayerCommonBuilder(playerInfo));
				if (!HawkOSOperator.isEmptyString(playerInfo.getPfIcon())) {
					rankInfo.setPfIcon(playerInfo.getPfIcon());
				}
				rankInfo.setAllianceName(guildInfo == null ? "" : guildInfo.getName());
				rankInfo.setGuildTag(guildInfo == null ? "" : guildInfo.getTag());
				rankInfo.setRank(rank);
				rankInfo.setRankKey(tuple.getElement());
				rankInfo.setRankInfoValue(realScore);
				rankInfo.setRankGrop(RankGroup.PLAYER_TYPE_VALUE);
				
				rankList.add(rankInfo.build());
				rank++;
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		
		return rankList;
	}
	
	/**
	 * 检测装备星级外显(泰能装备外显)
	 * @param player
	 * @return
	 */
	public static int checkEquipStarShow(Player player) {
		// 套装id
		int suitId = 0;
		
		int armourSuit = player.getEntity().getArmourSuit();
		List<ArmourEntity> armours = player.getData().getSuitArmours(armourSuit);
		
		// 7件装备全穿在身上
		if (armours.size() != 7) {
			return 0;
		}
		boolean isQuantum = true;
		for (ArmourEntity armour : armours) {
			ArmourCfg armouCfg = HawkConfigManager.getInstance().getConfigByKey(ArmourCfg.class, armour.getArmourId());
			// 判断7件装备是相同套装
			if (suitId != 0 && suitId != armouCfg.getArmourSuitId()) {
				return 0;
			}
			// 小于30星
			if (armour.getStar() < 30) {
				return 0;
			}
			// 三件属性全激活了
			if (armour.getStarEff().size() < 3) {
				return 0;
			}
			// 小于40星
			if (armour.getStar() < 40) {
				isQuantum = false;
			}
			// 三件属性全激活了
			if (armour.getStarEff().size() < 4) {
				isQuantum = false;
			}
			suitId = armouCfg.getArmourSuitId();
		}
		return isQuantum ? (100 + suitId) : suitId;
	}
	
	/**
	 * 获取下一周的开始时间
	 * 
	 * @return
	 */
	public static long getNextWeekStarttime() {
		long nextWeekStarttime = getCurrentWeekStarttime() + HawkTime.DAY_MILLI_SECONDS * 7;
		return nextWeekStarttime;
	}
	
	/**
	 * 获取本周的开始时间
	 * 
	 * @return
	 */
	public static long getCurrentWeekStarttime() {
		int day = HawkTime.getDayOfWeek();
		long weekStartTime = HawkTime.getNextAM0Date() - (day * HawkTime.DAY_MILLI_SECONDS);
		return weekStartTime;
	}
	
	/**
	 * 获取最大该兵种最大等级
	 * @param playerData
	 * @param soldierType
	 * @return
	 */
	public static int getMaxLevelArmyId(Player player, SoldierType soldierType) {
		int retArmyId = 0;
		int levelMark = 0;
		
		ConfigIterator<BattleSoldierCfg> cfgIter = HawkConfigManager.getInstance().getConfigIterator(BattleSoldierCfg.class);
		while (cfgIter.hasNext()) {
			BattleSoldierCfg cfg = cfgIter.next();
			if (cfg.getType() != soldierType.getNumber()) {
				continue;
			}
			if (cfg.getLevel() <= levelMark) {
				continue;
			}
			if (!ConfigUtil.isSoldierUnlocked(player, cfg.getId())) {
				continue;
			}
			levelMark = cfg.getLevel();
			retArmyId = cfg.getId();
		}
		return retArmyId;
	}
	
	/**
	 * 判断一起的装备科技是否都满级了
	 * 
	 * @param playerData
	 * @return
	 */
	public static boolean checkPhaseOneArmourTechMaxLevel(PlayerData playerData) {
		int count = 0;
		List<EquipResearchEntity> equipResearchEntityList = playerData.getEquipResearchEntityList();
		for (EquipResearchEntity researchEntity : equipResearchEntityList) {
			EquipResearchCfg researchCfg = HawkConfigManager.getInstance().getConfigByKey(EquipResearchCfg.class, researchEntity.getResearchId());
			if (researchCfg == null || researchCfg.getPhaseTwo() > 0) {
				continue;
			}
			
			count++;
			int nextLevel = researchEntity.getResearchLevel() + 1;
			EquipResearchLevelCfg cfg = AssembleDataManager.getInstance().getEquipResearchLevelCfg(researchEntity.getResearchId(), nextLevel);
			if (cfg != null) {
				return false;
			}
		}
		
		long phaseOneCount = HawkConfigManager.getInstance().getConfigIterator(EquipResearchCfg.class).stream().filter(e -> e.getPhaseTwo() <= 0).count();
		return count == phaseOneCount;
	}
	
	/**
	 * 判断两个时间点是否跨月份
	 * 
	 * @param time1
	 * @param time2
	 * @return
	 */
	public static boolean isCrossMonth(long time1, long time2) {
		String month1 = HawkTime.formatTime(time1, "yyyy-MM");
		String month2 = HawkTime.formatTime(time2, "yyyy-MM");
		return !month1.equals(month2);
	}
	
	/**
	 * 根据ip获取地名
	 * @param ip
	 * @return
	 */
	public static String convertIP2Address(String ip) {
		if (HawkOSOperator.isEmptyString(ip)) {
			return null;
		}
		
		int appid = GameConstCfg.getInstance().getIp_convert_appid();
		String token = GameConstCfg.getInstance().getIp_convert_token();
		int timestamp = HawkTime.getSeconds();
		String echo = GameConstCfg.getInstance().getIp_convert_echo();
		String randStr = GameConstCfg.getInstance().getIp_convert_randstr();
		String sigBasicStr = String.format("%d|%s|%d|%s", timestamp, echo, appid, token);
		String sig = "";
		try {
			MessageDigest messageDigest = MessageDigest.getInstance("MD5");
			messageDigest.update(sigBasicStr.getBytes("utf-8"));
			byte[] md5Bytes = messageDigest.digest();
			byte[] randStrBytes = randStr.getBytes("utf-8");
			byte[] newBytes = new byte[md5Bytes.length + randStrBytes.length];
			System.arraycopy(md5Bytes, 0, newBytes, 0, md5Bytes.length);
			System.arraycopy(randStrBytes, 0, newBytes, md5Bytes.length, randStrBytes.length);
			messageDigest.update(newBytes);
			byte[] md5Bytes1 = messageDigest.digest();
			sig = HawkMd5.md5BytesToHexString(md5Bytes1);
		} catch (Exception e) {
			HawkException.catchException(e);
			return null;
		}
		
		JSONObject header = new JSONObject();
		header.put("command", GameConstCfg.getInstance().getIp_convert_command());
		header.put("appid", appid);
		header.put("unix_sec", timestamp);
		header.put("echo", echo);
		header.put("rand_str", randStr);
		header.put("signature", sig);

		JSONArray jsonArray = new JSONArray();
		for (long id = 3000000050L; id <= 3000000054L; id++) {
			JSONObject json = new JSONObject();
			json.put("var_id", id);
			JSONArray array = new JSONArray();
			array.add("raw_ipv4");
			array.add(String.valueOf(ip2Int(ip)));
			json.put("input_parameter_list", array);
			jsonArray.add(json);
		}
		
		JSONObject paramJson = new JSONObject();
		paramJson.put("header", header);
		paramJson.put("var_query_list", jsonArray);
		
		int modId = GameConstCfg.getInstance().getIp_convert_modid();
		int cmdId = GameConstCfg.getInstance().getIp_convert_cmdid();
		HawkTuple3<Integer, String, Object> retInfo = L5Helper.l5Task(modId, cmdId, 500, new L5Task() {
			@Override
			public HawkTuple2<Integer, Object> run(String host) {
				try {
					String subAttr = GameConstCfg.getInstance().getIp_convert_suburl();
					if (!host.endsWith("/")) {
						host += "/";
					}
					
					String url = String.format("http://%s%s", host, subAttr);
					ContentResponse response = HawkHttpUrlService.getInstance().doPost(url, paramJson.toJSONString(), 500);
					return new HawkTuple2<Integer, Object>(0, response);
				} catch (Exception e) {
					HawkException.catchException(e);
				}
				
				return new HawkTuple2<Integer, Object>(-1, null);
			}
		});
		
		if (retInfo.third != null) {
			try {
				ContentResponse resp = (ContentResponse)retInfo.third;
				String content = resp.getContentAsString();
				JSONObject json = JSONObject.parseObject(content);
				JSONArray array = json.getJSONArray("var_query_rsp_list");
				for (int i = 0; i < array.size(); i++) {
					JSONObject obj = array.getJSONObject(i);
					if (obj.getLongValue("var_id") != 3000000052L) {
						continue;
					}
					
					String value = obj.getString("var_value");
					if (value != null && value.indexOf("香港") >= 0) {
						return "中国香港";
					} 
					if (value != null && value.indexOf("澳门") >= 0) {
						return "中国澳门";
					} 
					if (value != null && value.indexOf("台湾") >= 0) {
						return "中国台湾";
					} 
					return value;
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		
		return null;
	}
	
	/**
	 * ip转数字
	 * 
	 * @param ip
	 * @return
	 */
	private static long ip2Int(String ip) {
		long ipInt = 0;
		String[] ipStrArray = ip.split("\\.");
		for (String ipStr : ipStrArray) {
			ipInt = ipInt << 8 | Integer.parseInt(ipStr);
		}
		
		return ipInt;
	}
	
}
