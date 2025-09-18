package com.hawk.game.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.hawk.activity.event.impl.SWScoreEvent;
import com.hawk.activity.helper.PlayerActivityData;
import com.hawk.activity.helper.PlayerDataHelper;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.redis.ActivityGlobalRedis;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.AnniversaryGfit.AnniversaryGiftActivity;
import com.hawk.activity.type.impl.AnniversaryGfit.cfg.AnniversaryGiftAchievecfg;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.helper.AchievePushHelper;
import com.hawk.activity.type.impl.achieve.parser.AccumulateDiamondRechargeParser;
import com.hawk.activity.type.impl.achieve.parser.ConsumeMoneyParser;
import com.hawk.activity.type.impl.achieve.parser.RechargeAllRmbParse;
import com.hawk.activity.type.impl.achieve.parser.RechargeGfitPayCountParse;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.allianceCarnival.AllianceCarnivalActivity;
import com.hawk.activity.type.impl.allianceCarnival.entity.ACBInfo;
import com.hawk.activity.type.impl.allianceCarnival.rank.ACRankInfo;
import com.hawk.activity.type.impl.changeServer.ChangeServerActivity;
import com.hawk.activity.type.impl.changeServer.cfg.ChangeServerTimeCfg;
import com.hawk.activity.type.impl.dailysign.DailySignActivity;
import com.hawk.activity.type.impl.dailysign.entity.DailySignEntity;
import com.hawk.activity.type.impl.groupBuy.GroupBuyActivity;
import com.hawk.activity.type.impl.groupBuy.cfg.GroupBuyPriceCfg;
import com.hawk.activity.type.impl.groupBuy.entity.GroupBuyEntity;
import com.hawk.activity.type.impl.groupBuy.entity.GroupBuyRecord;
import com.hawk.activity.type.impl.inviteMerge.InviteMergeActivity;
import com.hawk.activity.type.impl.inviteMerge.tmp.InviteMergeLeaderTmp;
import com.hawk.activity.type.impl.lotteryTicket.LotteryTicketActivity;
import com.hawk.activity.type.impl.seasonActivity.SeasonActivity;
import com.hawk.activity.type.impl.strongestGuild.StrongestGuildActivity;
import com.hawk.activity.type.impl.strongestGuild.cfg.StrongestGuildCfg;
import com.hawk.activity.type.impl.strongestGuild.rank.StrongestGuildRank;
import com.hawk.activity.type.impl.strongestGuild.rank.impl.StrongestGuildPersonalTotalRank;
import com.hawk.activity.type.impl.strongestGuild.rank.impl.StrongestGuildTotalGuildRank;
import com.hawk.common.AccountRoleInfo;
import com.hawk.game.config.*;
import com.hawk.game.service.commonMatch.data.CMWData;
import com.hawk.game.service.commonMatch.manager.ipml.XHJZSeasonManager;
import com.hawk.game.service.guildTeam.ipml.TBLYGuildTeamManager;
import com.hawk.game.service.guildTeam.model.GuildTeamData;
import com.hawk.game.service.guildTeam.model.GuildTeamRoomData;
import com.hawk.game.service.tblyTeam.TBLYSeasonService;
import com.hawk.game.service.tblyTeam.TBLYWarResidKey;
import com.hawk.game.service.tblyTeam.model.TBLYSeasonData;
import com.hawk.game.service.tiberium.TiberiumConst;
import com.hawk.game.service.xhjzWar.XHJZRedisKey;
import com.hawk.game.service.xhjzWar.XHJZWarTeamData;

import org.hawk.app.HawkApp;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.delay.HawkDelayAction;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.redis.HawkRedisSession;
import org.hawk.script.HawkScript;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;
import org.hawk.thread.HawkThread;
import org.hawk.thread.HawkThreadPool;
import org.hawk.tuple.HawkTuple2;
import org.hawk.xid.HawkXID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.googlecode.protobuf.format.JsonFormat;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.constant.ObjType;
import com.hawk.activity.entity.ActivityPlayerEntity;
import com.hawk.activity.event.ActivityEvent;
import com.hawk.activity.event.impl.CWScoreEvent;
import com.hawk.activity.event.impl.ConsumeMoneyEvent;
import com.hawk.activity.event.impl.DiamondRechargeEvent;
import com.hawk.activity.event.impl.PayGiftBuyEvent;
import com.hawk.activity.event.impl.RechargeAllRmbEvent;
import com.hawk.game.GsApp;
import com.hawk.game.GsConfig;
import com.hawk.game.cfgElement.ArmourEffObject;
import com.hawk.game.cfgElement.ArmourStarExplores;
import com.hawk.game.city.CityManager;
import com.hawk.game.crossactivity.CrossActivityService;
import com.hawk.game.crossproxy.CrossProxy;
import com.hawk.game.entity.ArmourEntity;
import com.hawk.game.entity.ArmyEntity;
import com.hawk.game.entity.CommanderEntity;
import com.hawk.game.entity.DressEntity;
import com.hawk.game.entity.EquipResearchEntity;
import com.hawk.game.entity.GuildBuildingEntity;
import com.hawk.game.entity.GuildInfoObject;
import com.hawk.game.entity.GuildManorEntity;
import com.hawk.game.entity.HeroArchivesEntity;
import com.hawk.game.entity.NationConstructionEntity;
import com.hawk.game.entity.NationShipComponentEntity;
import com.hawk.game.entity.OfficerEntity;
import com.hawk.game.entity.PlayerBaseEntity;
import com.hawk.game.entity.PlayerEntity;
import com.hawk.game.entity.QueueEntity;
import com.hawk.game.entity.StatusDataEntity;
import com.hawk.game.entity.StoryMissionEntity;
import com.hawk.game.entity.TechnologyEntity;
import com.hawk.game.entity.item.DressItem;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.global.RedisKey;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.guild.championship.ChampionshipService;
import com.hawk.game.guild.manor.GuildBuildingStat;
import com.hawk.game.guild.manor.GuildManorObj;
import com.hawk.game.guild.manor.ManorBastionStat;
import com.hawk.game.guild.manor.building.IGuildBuilding;
import com.hawk.game.invoker.GuildDemiseLeaderInvoker;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.item.mission.MissionEntityItem;
import com.hawk.game.log.DungeonRedisLog;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.module.PlayerImmgrationModule;
import com.hawk.game.module.mechacore.entity.MechaCoreEntity;
import com.hawk.game.module.nationMilitary.entity.NationMilitaryEntity;
import com.hawk.game.module.obelisk.service.ObeliskService;
import com.hawk.game.module.plantsoldier.strengthen.PlantSoldierSchoolEntity;
import com.hawk.game.msg.CalcDeadArmy;
import com.hawk.game.msg.CalcSWDeadArmy;
import com.hawk.game.nation.NationService;
import com.hawk.game.nation.NationalBuilding;
import com.hawk.game.nation.ship.NationShipFactory;
import com.hawk.game.nation.tech.NationTechCenter;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.player.cache.PlayerDataKey;
import com.hawk.game.player.cache.PlayerDataSerializer;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.player.hero.SkillSlot;
import com.hawk.game.player.hero.skill.IHeroSkill;
import com.hawk.game.player.platchange.PlatChangeService;
import com.hawk.game.playercopy.PlayerCopyService;
import com.hawk.game.president.PresidentFightService;
import com.hawk.game.president.PresidentOfficier;
import com.hawk.game.president.PresidentRecord;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Immgration;
import com.hawk.game.protocol.Rank;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.TiberiumWar;
import com.hawk.game.protocol.Army.ArmyChangeCause;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.protocol.Const.ItemType;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.protocol.Const.QueueType;
import com.hawk.game.protocol.Const.TerritoryType;
import com.hawk.game.protocol.GuildChampionship.PBChampionPlayer;
import com.hawk.game.protocol.GuildChampionship.PBChampionSoldier;
import com.hawk.game.protocol.GuildManager.AuthId;
import com.hawk.game.protocol.Mail.MailLiteInfo;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.National.NationbuildingType;
import com.hawk.game.protocol.President.OfficerInfo;
import com.hawk.game.protocol.President.OfficerInfoSync;
import com.hawk.game.protocol.President.OfficerRecord;
import com.hawk.game.protocol.President.OfficerType;
import com.hawk.game.protocol.Rank.RankInfo;
import com.hawk.game.protocol.Rank.RankType;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.queryentity.AccountInfo;
import com.hawk.game.rank.RankService;
import com.hawk.game.service.chat.ChatService;
import com.hawk.game.service.college.CollegeService;
import com.hawk.game.service.cyborgWar.CWTeamData;
import com.hawk.game.service.cyborgWar.CyborgWarRedis;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.service.starwars.StarWarsOfficerService;
import com.hawk.game.util.BuilderUtil;
import com.hawk.game.util.EffectParams;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.LogUtil;
import com.hawk.game.util.GsConst.MissionState;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.object.AreaObject;
import com.hawk.game.world.object.Point;
import com.hawk.game.world.object.YuriFactoryPoint;
import com.hawk.game.world.proxy.WorldPointProxy;
import com.hawk.game.world.service.WorldFoggyFortressService;
import com.hawk.game.world.service.WorldPlayerService;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.game.world.thread.WorldDelayTask;
import com.hawk.game.world.thread.WorldTask;
import com.hawk.game.world.thread.WorldThreadScheduler;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.gamelib.rank.RankScoreHelper;
import com.hawk.log.Action;
import com.hawk.log.LogConst.ArmyChangeReason;
import com.hawk.log.LogConst.ArmySection;
import com.hawk.serialize.string.SerializeHelper;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Tuple;

/**
 * sysop脚本服务类 常驻的脚本放这里面
 * @author golden
 *
 */
public class SysOpService {

	public static Logger logger = LoggerFactory.getLogger("Server");
	
	/**
	 * 单例
	 */
	private static SysOpService instance = new SysOpService();
	
	/**
	 * 获取单例
	 */
	public static SysOpService getInstance() {
		return instance;
	}
	
	/**
	 * 常驻脚本
	 * @param opType
	 */
	public boolean sysop(Map<String, String> params, String opType) {
		if (opType.equals("1")) {
			GameUtil.dailyLog();
			return true;
		}

		if (opType.equals("2")) {
			queryGuildPowerScore();
			return true;
		}

		//添加本地拉取puidProfile信息的白名单账号
		if (opType.equals("3") && !HawkOSOperator.isEmptyString(params.get("openid"))) {
			GlobalData.getInstance().addPuidProfileCtrl(params.get("openid"));
			return true;
		}

		//每日上报排行榜数据
		if (opType.equals("4")) {
			GameUtil.dailyRankLog(true);
			return true;
		}

		// 清除军团模拟战报名信息
		if (opType.equals("5")) {
			clearChSignInfo();
			return true;
		}

		if (opType.equals("6")) {
			flushCrossServerList();
			return true;
		}

		// 通用发奖 -- 不要清除
		if (opType.equals("7")) {
			tongyongReward();
			return true;
		}

		// 通用发奖 -- 不要清除
		if (opType.equals("8")) {
			tongyongRewardHuidu();
			return true;
		}
		
		// 扣除物品
		if (opType.equals("9")) {
			batchConsumeGM();
			return true;
		}

		// 修兵
		if (opType.equals("10")) {
			fixArmy();
			return true;
		}

		// 给国家仓库添加材料
		if (opType.equals("11")) {
			addNationWarehouseResource(params);
			return true;
		}

		// 添加国家科技值
		if (opType.equals("12")) {
			addNationTechValue(params);
			return true;
		}

		// 修复联盟堡垒
		if(opType.equals("13")){
			fixGuildBuilding(params);
			return true;
		}

		// 修复科技藏数据
		if(opType.equals("14")){
			technologyEntityCheck(params);
			return true;
		}

		// 清除脏点
		if(opType.equals("15")){
			clearWorldPoint(params);
			return true;
		}

		// 完成赛博任务
		if (opType.equals("16")) {
			finishCybor(params);
			return true;
		}

		// 清除协议封禁计数
		if (opType.equals("17")) {
			clearProtoCounter(params);
			return true;
		}

		// 重新刷幽灵基地点
		if (opType.equals("18")) {
			refreshYuriPoint();
			return true;
		}

		// 转平台
		if (opType.equals("19")) {
			String openId = params.get("openId");
			String serverId = params.get("serverId");
			PlatChangeService.getInstance().changePlatform(openId, serverId);
			return true;
		}

		// 大帝战赛季结算事件补发
		if (opType.equals("20")) {
			String playerId = params.get("playerId");
			long kill = Long.parseLong(params.get("kill"));
			long dead = Long.parseLong(params.get("dead"));
			starWarSeasonEvent(playerId, kill, dead);
			return true;
		}

		//修复剧情任务数据
		if (opType.equals("21")) {
			fixStoryMission(params);
			return true;
		}

		/**
		 * 清玩家缓存 -- golden
		 */
		if (opType.equals("202404171200")) {
			clearPlayerCache(params.get("playerId"));
			return true;
		}

		/**
		 * 清除玩家缓存，重新迁服 -- golden
		 */
		if (opType.equals("202404171201")) {
			String playerId = params.get("playerId");
			String serverId = params.get("serverId");
			immgration(playerId, serverId);
			return true;
		}

		/**
		 * 拆转服数据 -- 汇川
		 */
		if (opType.equals("20240417165501")) {
			getChangeServerPower();
			return true;
		}

		/**
		 * 拆转服数据 -- 汇川
		 */
		if (opType.equals("20240417165502")) {
			String termId = params.get("termId");
			String serverId = params.get("serverId");
			int count = Integer.parseInt(params.get("count"));
			getChangeServerPowerFromRedis(termId, serverId, count);
			return true;
		}

		/**
		 * 拆转服数据 -- 汇川
		 */
		if (opType.equals("20240417165503")) {
			String termId = params.get("termId");
			String beforeServerId = params.get("beforeServerId");
			String beforeServerIdentify = params.get("beforeServerIdentify");
			int beforecount = Integer.parseInt(params.get("coubeforecountnt"));
			String afterServer = params.get("afterServer");
			int afterCount = Integer.parseInt(params.get("afterCount"));
			int isReal = Integer.parseInt(params.get("isReal"));
			fixChangeServerPower(termId, beforeServerId, beforeServerIdentify, beforecount, afterServer, afterCount, isReal);
			return true;
		}

		/**
		 * 拆转服数据 -- 汇川
		 */
		if (opType.equals("202412021515")) {
			int termId = Integer.parseInt(params.get("termId"));
			int isReal = Integer.parseInt(params.get("isReal"));
			fixChangeServerPower(termId, isReal);
			return true;
		}

		/**
		 * 拆转服数据 -- 汇川
		 */
		if (opType.equals("202412041504")) {
			fixChangeServerApply(params);
			return true;
		}

		/**
		 * 拆转服数据 -- 汇川
		 */
		if (opType.equals("20241206152401")) {
			fixChangeServerData(params);
			return true;
		}

		/**
		 * 拆转服数据 -- 汇川
		 */
		if (opType.equals("20241206152402")) {
			fixChangeServerData("7t5-ibp86-2d", "7px-3oxruj-2");
			return true;
		}

		/**
		 * 国家建筑数据修复 -- golden
		 */
		if (opType.equals("2024042501")) {
			fixNationConstruction();
			return true;
		}

		/**
		 * 国家建筑数据修复 -- golden
		 */
		if (opType.equals("2024042502")) {
			fixNationShip();
			return true;
		}

		/**
		 * 处理赛博拆服后的脏数据 -- huichuan
		 */
		if (opType.equals("202405061138")) {
			fixCyborgWarRank();
			return true;
		}

		/**
		 * 修复联盟总动员联盟经验数据 -- che
		 */
		if(opType.equals("202409061120")){
			fixAllianceCarnivalGuildExp();
			return true;
		}

		/**
		 * 转让司令给长人 ?playerId=xxx&tarplayerId=xxxx -- 文涛
		 */
		if (opType.equals("20240090611141")) {
			setPresident(params);
			return true;
		}

		if(opType.equals("202407231555")){
			fixToucai(params);
			return true;
		}

		/**
		 * 修复玩家6重礼数据
		 * http://127.0.0.1:8080/script/sysop?op=202410241800&playerId=uuiueier&type=1&value=100    //累计金条消耗
		 * http://127.0.0.1:8080/script/sysop?op=202410241800&playerId=uuiueier&type=2&value=100    //累计金条充值
		 * http://127.0.0.1:8080/script/sysop?op=202410241800&playerId=uuiueier&type=3&value=100    //累计直购消费
		 * http://127.0.0.1:8080/script/sysop?op=202410241800playerId=uuiueier&&type=4&value=100    //累计人民币消费
		 *
		 */
		if(opType.equals("202410241800")){
			fixAnniversaryGiftActivityAchieve(params);
			return true;
		}
		if (opType.equals("202411051052")) {
			String playerId = params.get("playerId");
			fixPlayer1104(playerId);
			return true;
		}
		if (opType.equals("202411051053")) {
			xiufu1104();
			return true;
		}

		//更新玩家缓存数据 http://127.0.0.1:8080/script/sysop?op=202412031805&playerId=1aat-3oty56-1
		if (opType.equals("202412031805")) {
			updateAccountInfo(params);
			return true;
		}


		if (opType.equals("202412091606")) {
			updatePlayerCollegeInfo(params);
			return true;
		}

		//加载联盟堡垒数据
		if (opType.equals("202412101940")) {
			loadGuildManorData();
			loadGuildBuildData();
			return true;
		}

		if (opType.equals("202412101942")) {
			updateGuildSuperMineStat();
			return true;
		}

		if (opType.equals("202412121059")) {
			fixPifu(params);
			return true;
		}

		if (opType.equals("202412131051")) {
			fixXingneng(params);
			return true;
		}

		if (opType.equals("202412121618")) {
			groupBuyActivityEndSendAward(params);
			return true;
		}


		if (opType.equals("2024121614222")) {
			fix2221215(params);
			fixPifu1216(params);
			return true;
		}

		//修复排行榜数据
		if (opType.equals("202412232208")) {
			fixPlayerCastleRank();
			return true;
		}

		//修复每日签到活动数据
		if (opType.equals("202412301628")) {
			fixDailySignActivityData(params);
			return true;
		}

		if (opType.equals("20250205131601")){
			fixTBLYTeamMatchData(params);
			return true;
		}

		if (opType.equals("20250205131602")) {
			TBLYSeasonService.getInstance().cleanBattleData();
			TBLYSeasonService.getInstance().loadBattleRoom();
			return true;
		}

		if (opType.equals("202504181501")) {
			String teamId = params.get("teamId");
			int win = Integer.parseInt(params.get("win"));
			int lose = Integer.parseInt(params.get("lose"));
			CMWData data = XHJZSeasonManager.getInstance().loadData(teamId);
			data.rankingWinCnt = data.rankingWinCnt + win;
			data.rankingLoseCnt = data.rankingLoseCnt + lose;
			RedisProxy.getInstance().getRedisSession().hSet(XHJZSeasonManager.getInstance().getDataKey(), data.teamId, data.serialize());
			return true;
		}
		
		//批量发放联盟全体成员奖励（tmp/batchGuildAward.txt）
		if (opType.equals("202507041427")) {
			batchSendGuildAward();
			return true;
		}
		
		//批量迁服（tmp/batchImmigration.txt）
		if (opType.equals("202507041428")) {
			batchImmigration();
			return true;
		}
		
		//给单个联盟发奖
		if (opType.equals("202507041429")) {
			sendGuildAwardMail(params.get("guildId"), params.get("awards"));
			return true;
		}
		
		//给单个玩家迁服
		if (opType.equals("202507041430")) {
			try {
				playerImmigrate(params.get("playerId"), params.get("serverId"));
			} catch (Exception e) {
				HawkLog.errPrintln("sysop playerImmigrate exception, playerId: {}, tarServerId: {}", params.get("playerId"), params.get("serverId"));
				HawkException.catchException(e);
			}
			return true;
		}

		// ------- 20250807整理 TODO ----------- 
		if (opType.equals("202412201036")) {
			fixInviteMergeLeader();
			return true;
		}
		
		if (opType.equals("202412201531")) {
			fixInviteMergeLeader1(params);
			return true;
		}
		
		if (opType.equals("202412231554")) {
			fixInviteMergeVotePermission(params);
			return true;
		}

		if (opType.equals("20250205131601")) {
			String password = params.getOrDefault("password","");
			fixTBLYTeamMatchData(password);
			return true;
		}

		if (opType.equals("20250205131602")) {
			TBLYSeasonService.getInstance().cleanBattleData();
			TBLYSeasonService.getInstance().loadBattleRoom();
			return true;
		}

		if (opType.equals("20250205131603")) {
			fixTBLYTeamMatchData("fjskhas");
			return true;
		}

		if (opType.equals("20250215101010")) {
			fixStrongestGuildRankData();
			return true;
		}

		if (opType.equals("202502188160301")) {
			fixTBLYMergeTeamData(false);
			return true;
		}
		if (opType.equals("202502188160302")) {
			fixTBLYMergeTeamData(true);
			return true;
		}
		if (opType.equals("202502188160303")) {
			fixTBLYMatchResult(false, 0);
			return true;
		}
		if (opType.equals("202502188160304")) {
			fixTBLYMatchResult(true, Integer.parseInt(params.getOrDefault("startTermId", "7")));
			return true;
		}
		
		if (opType.equals("202503041543")) {
			String playerId = params.get("playerId");
			fixPlayer1108(playerId);
			return true;
		}
		if (opType.equals("202503041543100")) {
			xiufu1108();
			return true;
		}

		if (opType.equals("202503171055")) {
			String teamId = params.get("teamId");
			int score = Integer.parseInt(params.get("score"));
			CMWData data = XHJZSeasonManager.getInstance().loadData(teamId);
			data.score = data.score + score;
			RedisProxy.getInstance().getRedisSession().hSet(XHJZSeasonManager.getInstance().getDataKey(), data.teamId, data.serialize());
			return true;
		}

		if (opType.equals("202504101656")) {
			String playerIds = params.get("playerIds");
			this.fixPlayerItemBuff(playerIds);
			return true;
		}
		
		if (opType.equals("202504101658")) {
			String fixData = params.get("fixData");
			this.fixPlayerMonthCardBuff(fixData);
			return true;
		}
		
		if (opType.equals("202504140266")) {
			String collegeId = params.get("collegeId");
			String memberId = params.get("memberId");
			this.fixCollegeMembers(collegeId,memberId);
			return true;
		}
		
		if (opType.equals("202504191200")) {
			clear0711Mail();
			return true;
		}
		
		//修复星海赛季数据
		if(opType.equals("202506031618")){
			//key=1245&fixServer=10797&teamData=team1_1,team2_1,team3_4
			this.fixXhjzSeasonTeamData(params);
			return true;
		}
		//重载星海赛季排行榜
		if(opType.equals("202506031619")){
			this.reloadXhjzSeasonTeamData();
			return true;
		}
		
		//重新开放使命战争
		if(opType.equals("202506101616")){
			this.fixObliskData(params);
			return true;
		}

		//发放联盟旗帜
		if(opType.equals("202506163636")){
			this.sendGuildFlag();
			return true;
		}
		
		//重载星海赛季排行榜
		if(opType.equals("202506181619")){
			this.fixXhjzSeasonTeamDataTow();
			return true;
		}
		
		//发放联盟段位积分
		if(opType.equals("202506223939")){
			this.sendSeasonExp();
			return true;
		}
		
		//指定星海联赛匹配
		if(opType.equals("202506261618")){
			this.addXhjzMatch(params);
			return true;
		}
		
		//删除装扮道具，扣除时间（可批量）
		if (opType.equals("202507181105")) {
			delDress(params);
			return true;
		}
		
		if (opType.equals("2024041712012")) {
			String playerId = params.get("playerId");
			String serverId = params.get("serverId");
			immgration(playerId, serverId, null);
			return true;
		}
		
		if(opType.equals("202508051555")){
			fixXXXXPlayer();
			return true;
		}
		
		if (opType.equals("20250730174701")) {
			changeGuildLeader();
			return true;
		}
		
		if (opType.equals("202508041349")) {
			fixImmgrationByDataKey(params);
			return true;
		}
		
		return false;
	}

	private void fixTBLYTeamMatchData(Map<String, String> params){
		String password = params.getOrDefault("password","");
		boolean isReal = "fjskhas".equals(password);
		int startTermId = Integer.parseInt(params.getOrDefault("startTermId", "1"));
		int season = Integer.parseInt(params.getOrDefault("season", "10"));
		for(int i = startTermId; i < TiberiumConstCfg.getInstance().getEliminationStartTermId(); i++){
			try {
				List<String> fileContents = new ArrayList<>();
				try {
					HawkOSOperator.readTextFileLines("tmp/fixTblyMatch.txt", fileContents);
				} catch (Exception e) {
					HawkException.catchException(e);
				}
				for(String serverId : fileContents){
					String roomserverKey = String.format(TBLYWarResidKey.TBLY_WAR_SEASON_ROOM_SERVER, season, i, serverId);
					if (isReal){
						RedisProxy.getInstance().getRedisSession().del(roomserverKey);
					}else {
						HawkLog.logPrintln("TBLYSeasonService fixTBLYTeamMatchData del roomserverKey:{}", roomserverKey);
					}
				}
				String roomKey = String.format(TBLYWarResidKey.TBLY_WAR_SEASON_ROOM, season, i);
				Map<String, String> roomStrMap = RedisProxy.getInstance().getRedisSession().hGetAll(roomKey);
				Map<String, GuildTeamRoomData> roomDataMap = new HashMap<>();
				Set<String> teamIds = new HashSet<>();
				Map<String, Set<String>> serverIdToRoomIdMap = new HashMap<>();
				Map<String, String> updateRoomStrMap = new HashMap<>();
				for(String roomId : roomStrMap.keySet()){
					try {
						String roomStr = roomStrMap.get(roomId);
						//解析房间数据
						GuildTeamRoomData roomData = GuildTeamRoomData.unSerialize(roomStr);
						//如果为空直接跳过
						if(roomData == null){
							HawkLog.logPrintln("TBLYSeasonService fixTBLYTeamMatchData data is null roomData:{}", roomStr);
							continue;
						}
						roomDataMap.put(roomData.id, roomData);
						teamIds.add(roomData.campA);
						teamIds.add(roomData.campB);
					} catch (Exception e) {
						HawkException.catchException(e);
						HawkLog.logPrintln("TBLYSeasonService fixTBLYTeamMatchData error");
					}
				}
				Map<String, GuildTeamData> teamMap = TBLYGuildTeamManager.getInstance().loadTeamMap(teamIds);
				HawkLog.logPrintln("TBLYSeasonService fixTBLYTeamMatchData roomSize:{}, teamSize", roomDataMap.size(), teamMap.size());
				for(String roomId : roomDataMap.keySet()){
					try {
						GuildTeamRoomData roomData = roomDataMap.get(roomId);
						if(roomData == null){
							HawkLog.logPrintln("TBLYSeasonService fixTBLYTeamMatchData data is null roomId:{}", roomId);
							continue;
						}
						GuildTeamData teamData1 = teamMap.get(roomData.campA);
						GuildTeamData teamData2 = teamMap.get(roomData.campB);
						String roomServerId = teamData1.serverId.compareTo(teamData2.serverId) < 0 ? teamData1.serverId : teamData2.serverId;
						if(!roomData.roomServerId.equals(roomServerId)){
							HawkLog.logPrintln("TBLYSeasonService fixTBLYTeamMatchData roomServerId is diff roomId:{}, old:{}, new:{}",
									roomId, roomData.roomServerId, roomServerId);
							roomData.roomServerId = roomServerId;
							updateRoomStrMap.put(roomData.id, roomData.serialize());
						}
						updateRoomIdToServer(serverIdToRoomIdMap, teamData1.serverId, roomData.id);
						updateRoomIdToServer(serverIdToRoomIdMap, teamData2.serverId, roomData.id);
						updateRoomIdToServer(serverIdToRoomIdMap, roomData.roomServerId, roomData.id);
					} catch (Exception e) {
						HawkException.catchException(e);
						HawkLog.logPrintln("TBLYSeasonService fixTBLYTeamMatchData error");
					}
				}
				if(isReal){
					RedisProxy.getInstance().getRedisSession().hmSet(roomKey, updateRoomStrMap, 0);
					for(String toServerId : serverIdToRoomIdMap.keySet()){
						Set<String> roomIds = serverIdToRoomIdMap.get(toServerId);
						RedisProxy.getInstance().getRedisSession().sAdd(String.format(TBLYWarResidKey.TBLY_WAR_SEASON_ROOM_SERVER, season, i, toServerId), 0, roomIds.toArray(new String[roomIds.size()]));
					}
				}else {
					HawkLog.logPrintln("TBLYSeasonService fixTBLYTeamMatchData roomKey:{}", roomKey);
					for(String roomStr : updateRoomStrMap.values()){
						HawkLog.logPrintln("TBLYSeasonService fixTBLYTeamMatchData roomStr:{}", roomStr);
					}
					for(String toServerId : serverIdToRoomIdMap.keySet()){
						String roomserverKey = String.format(TBLYWarResidKey.TBLY_WAR_SEASON_ROOM_SERVER, season, i, toServerId);
						HawkLog.logPrintln("TBLYSeasonService fixTBLYTeamMatchData roomserverKey:{}", roomserverKey);
						Set<String> roomIds = serverIdToRoomIdMap.get(toServerId);
						for(String roomid : roomIds){
							HawkLog.logPrintln("TBLYSeasonService fixTBLYTeamMatchData roomid:{}", roomid);
						}
					}
				}
			} catch (Exception e) {
				HawkException.catchException(e);
				HawkLog.logPrintln("TBLYSeasonService fixTBLYTeamMatchData error");
			}
		}
	}

	public void updateRoomIdToServer(Map<String, Set<String>> serverIdToRoomIdMap, String serverId, String roomId){
		if(!serverIdToRoomIdMap.containsKey(serverId)){
			serverIdToRoomIdMap.put(serverId, new HashSet<>());
		}
		serverIdToRoomIdMap.get(serverId).add(roomId);
	}
	
	/**
	 * 修复剧情任务数据
	 * @param params
	 * @return
	 */
	private String fixStoryMission(Map<String, String> params) {
		Player player = GlobalData.getInstance().makesurePlayer(params.get("playerId"));
		if (player == null) {
			return HawkScript.failedResponse(HawkScript.SCRIPT_ERROR, "player not exist");
		}
		String cfgIdParam = params.get("cfgId");
		if (HawkOSOperator.isEmptyString(cfgIdParam)) {
			return HawkScript.failedResponse(HawkScript.SCRIPT_ERROR, "cfgId param need");
		}
		StoryMissionCfg cfg = HawkConfigManager.getInstance().getConfigByKey(StoryMissionCfg.class, Integer.parseInt(cfgIdParam));
		if (cfg == null) {
			return HawkScript.failedResponse(HawkScript.SCRIPT_ERROR, "config not exist");
		}

		StoryMissionEntity entity = player.getData().getStoryMissionEntity();
		MissionEntityItem entityItem = entity.getStoryMissionItem(cfg.getId());
		if (entityItem == null) {
			return HawkScript.failedResponse(HawkScript.SCRIPT_ERROR, "entityItem not exist");
		}
		if (entityItem.getState() != MissionState.STATE_NOT_FINISH) {
			return HawkScript.failedResponse(HawkScript.SCRIPT_ERROR, "entityItem state finish");
		}
		
		HawkThreadPool threadPool = HawkTaskManager.getInstance().getTaskExecutor();
		int threadIndex = Math.abs(player.getXid().hashCode() % threadPool.getThreadNum());
		HawkTaskManager.getInstance().postTask(new HawkTask() {
			@Override
			public Object run() {
				entityItem.setValue(cfg.getMissionCfgItem().getValue());
				entityItem.setState(MissionState.STATE_FINISH);
				// 设置任务状态(这里要设置一下，调用下entity的set方法，不然可能不会落地)
				entity.setStoryMissionItem(entityItem);
				StoryMissionService.getInstance().logTaskFlow(player, cfg, entityItem.getState());
				StoryMissionService.getInstance().checkChapterComplete(player, entity);
				player.getPush().syncStoryMissionInfo();
				HawkLog.logPrintln("sysop fixStoryMission success, playerId: {}, cfgId: {}", player.getId(), cfgIdParam);
				return null;
			}
		}, threadIndex);
		return HawkScript.successResponse("sysop fixStoryMission success");
	}
	
	/**
	 * 刷新幽灵基地
	 * @return
	 */
	private String refreshYuriPoint() {
		Collection<AreaObject> areas = WorldPointService.getInstance().getAreaVales();
		for (AreaObject area : areas) {
			// 每个区域依次delay 10s
			long delay = area.getId() * 2 * 1000L;
			
			logger.info("create script yuri task, areaId:{}, delay:{}", area.getId(), delay);
			
			WorldThreadScheduler.getInstance().postDelayWorldTask(new WorldDelayTask(999, delay, delay, 1) {
				@Override
				public boolean onInvoke() {
					logger.info("script yuri task begin, areaId:{}", area.getId());
					
					// 移除区域幽灵基地
					removeAreaYuri(area);
					// 生成区域幽灵基地
					createAreaYuri(area);
					
					logger.info("script yuri task end, areaId:{}", area.getId());
					return false;
				}
			});
		}
		return HawkScript.successResponse("success");
	}

	/**
	 * 删除区域的幽灵基地
	 * @param area
	 */
	private void removeAreaYuri(AreaObject area) {
		List<Point> usedPoints = area.getUsedPoints();
		List<Integer> removePoints = new ArrayList<Integer>();
		for (Point point : usedPoints) {
			WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(point.getX(), point.getY());
			if (worldPoint == null) {
				continue;
			}
			if(worldPoint.getPointType() != WorldPointType.FOGGY_FORTRESS_VALUE){
				continue;
			}
			YuriFactoryPoint yuriFactoryPoint = WorldFoggyFortressService.getInstance().getYuriFactoryPoint(point.getId());
			if(yuriFactoryPoint != null && yuriFactoryPoint.isInActive()){
				continue;
			}
			FoggyFortressCfg enemyCfg = HawkConfigManager.getInstance().getConfigByKey(FoggyFortressCfg.class, worldPoint.getMonsterId());
			if(enemyCfg == null){
				continue;
			}
			//移除
			WorldFoggyFortressService.getInstance().removeYuriPoint(point.getId());
			area.delFoggyIdNum(enemyCfg.getLevel());
			worldPoint.setInvalid(true);
			removePoints.add(point.getId());
		}
		// 持久化删除
		WorldPointService.getInstance().removeWorldPoints(removePoints, true);
		logger.info("script remove area yuri point, areaId:{}, size:{}", area.getId(), removePoints.size());
	}
	
	/**
	 * 区域生成幽灵基地点
	 * @param area
	 */
	private void createAreaYuri(AreaObject area) {
		int count = area.getTotalPointCount() * WorldMapConstProperty.getInstance().getWorldfoggyFortressRefreshMax() / 1000 / GsConst.POINT_TO_GRUD;
		WorldFoggyFortressService.getInstance().bornFoggyFortressOnArea(area, null, count, true, false);
		logger.info("script create area yuri point, areaId:{}, count:{}", area.getId(), count);
	}
	
	/**
	 * 查询联盟总战力数据
	 */
	private void queryGuildPowerScore() {
		HawkThreadPool threadPool = HawkTaskManager.getInstance().getThreadPool("task");
		HawkLog.logPrintln("queryGuildPowerScore, theadPool: {}", threadPool == null ? "null" : "useful");
		if (threadPool != null) {
			HawkTask task = new HawkTask() {
				@Override
				public Object run() {
					readGuildPowerScore();
					return null;
				}
			};

			task.setPriority(1);
			task.setTypeName("queryGuildPowerScore");
			threadPool.addTask(task);
		}
	}

	private void readGuildPowerScore() {
		List<RankInfo> rankInfos = RankService.getInstance().getRankCache(RankType.ALLIANCE_FIGHT_KEY);
		if (rankInfos == null || rankInfos.isEmpty()) {
			HawkLog.logPrintln("readGuildPowerScore failed, result is empty");
			return;
		}

		int count = 1;
		String serverId = GsConfig.getInstance().getServerId();
		String output = System.getProperty("user.dir") + "/logs/Guild.log";

		File file = new File(output);
		if (file.exists()) {
			file.delete();
		}

		FileWriter fileWriter = null;
		try {
			fileWriter = new FileWriter(output, true);
			for (RankInfo rankInfo : rankInfos) {
				if (count > 10) {
					break;
				}

				String content = String.format("serverId: %s, guildId: %s, guildName: %s, rank: %s, guildScore: %s",
						serverId, rankInfo.getId(), rankInfo.getAllianceName(), String.valueOf(rankInfo.getRank()), String.valueOf(rankInfo.getRankInfoValue()));
				fileWriter.write(content);
				fileWriter.write("\r\n");
				count++;
			}
		} catch (IOException e) {
			HawkException.catchException(e);
		} finally {
			if (fileWriter != null) {
				try {
					fileWriter.flush();
					fileWriter.close();
				} catch (IOException e1) {
					HawkException.catchException(e1);
				}
			}
		}

		HawkLog.logPrintln("readGuildPowerScore success, count: {}", count);
	}

	/**
	 * 检测清除玩家军团模拟战报名数据
	 */
	private void clearChSignInfo() {
		
		int termId = ChampionshipService.activityInfo.termId;
		
		logger.info("start clearChSignInfo, termId:{}", termId);

		Map<String, Set<String>> memberMap = new HashMap<>();

		List<String> guildIds = GuildService.getInstance().getGuildIds();

		for (String guildId : guildIds) {
			try {
				Set<String> memberIds = RedisProxy.getInstance().getGCPlayerIds(termId, guildId);
				if (memberIds == null || memberIds.isEmpty()) {
					continue;
				}
				memberMap.put(guildId, new HashSet<>(memberIds));
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}

		for (Entry<String, Set<String>> guildInfo : memberMap.entrySet()) {
			
			String guildId = guildInfo.getKey();
			
			logger.info("check clearChSignInfo, termId:{}, guildId:{}", termId, guildId);
			
			for (String playerId : guildInfo.getValue()) {
				
				try {
					
					Player player = GlobalData.getInstance().makesurePlayer(playerId);
					
					PBChampionPlayer.Builder  battleData = RedisProxy.getInstance().getGCPbattleData(termId, playerId);
					
					// 出征部队数量
					int armyCnt = calcArmyCnt(battleData);
					
					// 玩家最大出征上限
					double maxMarchSoldierNum = player.getMaxMarchSoldierNum(new EffectParams());
					
					// 符合条件的就清掉它
					if (armyCnt > (int)Math.ceil(maxMarchSoldierNum * 1.90d)) {
						RedisProxy.getInstance().removeGCPlayerId(termId, guildId, playerId);
						RedisProxy.getInstance().removeGCPbattleData(termId, playerId);
						ChampionshipService.getInstance().syncPageInfo(player);
						
						HawkRedisSession redisSession = RedisProxy.getInstance().getRedisSession();
						redisSession.hSet("clearChSignInfo0926", playerId, GsConfig.getInstance().getServerId(), 86400);
						
						logger.info("check faild, clearChSignInfo, termId:{}, guildId:{}, playerId:{}, armyCnt:{}, max:{}, calcMax:{}", termId, guildId, playerId, armyCnt, (int)maxMarchSoldierNum, (int)Math.ceil(maxMarchSoldierNum * 1.91d));
					} else {
						logger.info("check ok, clearChSignInfo, termId:{}, guildId:{}, playerId:{}, armyCnt:{}, max:{}, calcMax:{}", termId, guildId, playerId, armyCnt, (int)maxMarchSoldierNum, (int)Math.ceil(maxMarchSoldierNum * 1.91d));
					}
					
				} catch (Exception e) {
					HawkException.catchException(e);
				}
			}
		}
		
		logger.info("end clearChSignInfo, termId:{}", termId);
	}
	
	/**
	 * 玩家军团模拟战出征部队数量
	 */
	private int calcArmyCnt(PBChampionPlayer.Builder championPlayerPb) {
		int sum = 0;
		List<PBChampionSoldier> list = championPlayerPb.getSoldiersList();
		if (list == null || list.isEmpty()) {
			return sum;
		}
		for (PBChampionSoldier soldier : list) {
			sum += soldier.getCount();
		}
		return sum;
	}

	private String flushCrossServerList() {
		logger.info("execute load cross server list begin");
		String file = "tmp/cross_server_list.txt";
		List<String> stringList = new ArrayList<>();
		logger.info("load cross server list:{}", stringList);
		try {
			HawkOSOperator.readTextFileLines(file, stringList);
			Map<Integer, String> idServersMap = new HashMap<>(stringList.size());
			for (String lineString : stringList) {
				String[] idServers = lineString.split("\\t");
				idServersMap.put(Integer.parseInt(idServers[0]), idServers[1]);
			}
			
			CrossTimeCfg timeCfg = CrossActivityService.getInstance().getNextCrossTimeCfg();
			if (timeCfg == null) {
				 logger.error("can not find next cross time cfg");
				 return "fail";
			} else {
				int termId = timeCfg.getTermId();
				RedisProxy.getInstance().addCrossMatchList(termId, idServersMap, GsConfig.getInstance().getPlayerRedisExpire());
			}
			
			return "ok";
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		return "fail";
	}
	
	/**
	 * 修兵
	 */
	private void fixArmy() {
		List<AccountInfo> accounts = new ArrayList<>();
		GlobalData.getInstance().getAccountList(accounts);
		
		for (AccountInfo account : accounts) {
			if (account == null) {
				continue;
			}
			account.setArmyFixed(false);
		}
	}
	
	/**
	 * 通用发奖接口，不要清除
	 */
	private final void tongyongReward() {
		if (!GsConfig.getInstance().getServerId().equals("19999")) {
			return;
		}
		
		HawkTaskManager.getInstance().postExtraTask(new HawkTask() {

			@Override
			public Object run() {
				// 最多支持同时发5封邮件
				for (int i = 0; i <= 5; i++) {
					String prefix = i == 0 ? "" : String.valueOf(i);
					String rewardFilename = "tmp/tongyongReward" + prefix + ".txt";
					File file = new File(rewardFilename);
					if (!file.exists()) {
						HawkLog.logPrintln("reward file not exist, filename: {}", rewardFilename);
						continue;
					}
					
					String mailFilename = "tmp/tongyongRewardMail" + prefix + ".txt";
					File mailContentFile = new File(mailFilename);
					if (!mailContentFile.exists()) {
						HawkLog.logPrintln("mail content file not exist, filename: {}", mailFilename);
						continue;
					}
					
					String keyName = "tongyongRewardOnline" + prefix;
					tongyongRewardDetail(keyName, rewardFilename, mailFilename);
				}
				return null;
			}
		});
	}
	
	/**
	 * 通用发奖接口，不要清除
	 */
	private final void tongyongRewardHuidu() {
		if (!GsConfig.getInstance().getServerId().equals("19999")) {
			return;
		}
		
		HawkTaskManager.getInstance().postExtraTask(new HawkTask() {

			@Override
			public Object run() {
				// 最多支持同时发5封邮件
				for (int i = 0; i <= 5; i++) {
					String prefix = i == 0 ? "" : String.valueOf(i);
					String rewardFilename = "tmp/tongyongRewardHuidu" + prefix + ".txt";
					File file = new File(rewardFilename);
					if (!file.exists()) {
						continue;
					}
					
					String keyName = "tongyongRewardHuidu" + prefix;
					String mailFilename = "tmp/tongyongRewardMail" + prefix + ".txt";
					tongyongRewardDetail(keyName, rewardFilename, mailFilename);
				}
				return null;
			}
			
		});
		
	}
	
	/**
	 * 通用发奖接口，不要清除
	 */
	private void tongyongRewardDetail(String redisKey, String rewardFilename, String mailFilename) {
		// redis记一个标记位,避免重复执行
		HawkRedisSession redisSession = RedisProxy.getInstance().getRedisSession();
		boolean redisMarkSetSucc = redisSession.setNx(redisKey, HawkTime.formatNowTime());
		if (!redisMarkSetSucc) {
			HawkLog.errPrintln("tongyongReward error, already");
			return;
		}

		redisSession.expire(redisKey, 3600);
				
		HawkLog.logPrintln("tongyongReward start");
		
		List<String> rewardInfos = new ArrayList<>();
		try {
			// roleid;server;reward (reward奖励内容, 用标准的三段式，多个三段式之间用英文逗号分割)
			HawkOSOperator.readTextFileLines(rewardFilename, rewardInfos);
		} catch (Exception e) {
			HawkException.catchException(e);
			return;
		}
		
		List<String> mailInfos = new ArrayList<>();
		try {
			// 第一行邮件标题，第二行邮件内容
			HawkOSOperator.readTextFileLines(mailFilename, mailInfos);
		} catch (Exception e) {
			HawkException.catchException(e);
			return;
		}
		
		String title = mailInfos.get(0);
		String content = mailInfos.get(1);
		
		for (String info : rewardInfos) {
			try {
				String[] splitInfo = info.split(";");
				if (splitInfo.length != 3) {
					HawkLog.errPrintln("tongyongReward error, splitInfo length, info: {}", info);
					continue;
				}
				String playerId = splitInfo[0];
				String rewards = splitInfo[2];
				
				SystemMailService.getInstance().sendMail(MailParames.newBuilder()
						.setPlayerId(playerId)
						.setMailId(MailId.REWARD_MAIL)
						.setAwardStatus(MailRewardStatus.NOT_GET)
						.addSubTitles(title)
						.addContents(content)
						.setRewards(rewards)
						.build());
				
				HawkLog.logPrintln("tongyongReward success, info: {}", info);
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		
		HawkLog.logPrintln("tongyongReward finish");
	}
	
	
	/**
	 * 批量扣除物品
	 * @return
	 */
	protected void batchConsumeGM() {
		HawkRedisSession redisSession = RedisProxy.getInstance().getRedisSession();
		String redisKey = "batchConsumeGM:" + GsConfig.getInstance().getServerId();
		// redis记一个标记位,避免重复执行
		boolean redisMarkSetSucc = redisSession.setNx(redisKey, HawkTime.formatNowTime());
		if (!redisMarkSetSucc) {
			HawkLog.errPrintln("batchConsumeGM error, already");
			return;
		}

		redisSession.expire(redisKey, 3600);
				
		HawkLog.logPrintln("batchConsumeGM start");
		
		List<String> rewardInfos = new ArrayList<>();
		try {
			HawkOSOperator.readTextFileLines("tmp/gmconsume.txt", rewardInfos); //格式：roleid;server;reward (reward奖励内容, 用标准的三段式，多个三段式之间用英文逗号分割)
		} catch (Exception e) {
			HawkException.catchException(e);
			return;
		}
		
		HawkTaskManager.getInstance().postExtraTask(new HawkTask() {
			@Override
			public Object run() {
				for (String info : rewardInfos) {
					gmConsume(info);
				}
				return null;
			}
		});
		
		HawkLog.logPrintln("batchConsumeGM finish");
	}
	
	/**
	 * 扣除物品
	 * @param info
	 */
	private void gmConsume(String info) {
		try {
			String[] splitInfo = info.split(";");
			if (splitInfo.length != 3) {
				HawkLog.errPrintln("batchConsumeGM error, splitInfo length, info: {}", info);
				return;
			}
			String playerId = splitInfo[0];
			String serverId = splitInfo[1];
			String rewards = splitInfo[2];
			if (!GlobalData.getInstance().isLocalServer(serverId)) {
				return;
			}
			Player player = GlobalData.getInstance().makesurePlayer(playerId);
			if (player == null) {
				HawkLog.errPrintln("batchConsumeGM error, player not found, info: {}", info);
				return;
			}
			
			HawkXID xid = HawkXID.valueOf(ObjType.PLAYER, playerId);
			int threadId = xid.getHashThread(HawkTaskManager.getInstance().getThreadNum());
			HawkTaskManager.getInstance().postTask(new HawkTask() {
				@Override
				public Object run() {
					ConsumeItems consume = ConsumeItems.valueOf();
					List<ItemInfo> itemInfos = ItemInfo.valueListOf(rewards);
					for (ItemInfo item : itemInfos) {
						int count = player.getData().getItemNumByItemId(item.getItemId());
						if (item.getCount() > count) {
							item.setCount(count);
						}
					}
					consume.addConsumeInfo(itemInfos);
					if (!consume.checkConsume(player)) {
						HawkLog.errPrintln("batchConsumeGM error, checkConsume failed, info: {}", info);
					} else {
						consume.consumeAndPush(player, Action.GM_AWARD);
						HawkLog.logPrintln("batchConsumeGM success, info: {}", info);
					}
					return null;
				}
			}, threadId);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 添加国家科技值
	 * @param params
	 */
	private void addNationTechValue(Map<String, String> params) {
		String serverId = params.get("serverId");
		if (!GsConfig.getInstance().getServerId().equals(serverId)) {
			return;
		}
		
		NationTechCenter techCenter = (NationTechCenter)NationService.getInstance().getNationBuildingByType(NationbuildingType.NATION_TECH_CENTER);
		if (techCenter != null) {
			int addTech = Integer.parseInt(params.get("addTech"));
			techCenter.changeNationTechValue(addTech);
		}
	}
	
	/**
	 * 国家仓库添加资源
	 * 
	 * @param params
	 */
	private void addNationWarehouseResource(Map<String, String> params) {
		String serverId = params.get("serverId");
		if (!GsConfig.getInstance().getServerId().equals(serverId)) {
			return;
		}
		
		int resourceId = Integer.parseInt(params.getOrDefault("itemId", "0"));
		long count = Long.parseLong(params.getOrDefault("count", "0"));
		
		String redisKey = "addNationWarehouseResource:" + serverId + ":" + resourceId;
		HawkRedisSession redisSession = RedisProxy.getInstance().getRedisSession();
		String redisMark = redisSession.getString(redisKey);
		if (!HawkOSOperator.isEmptyString(redisMark)) {
			HawkLog.errPrintln("addNationWarehouseResource error, already");
			return;
		}
		
		redisSession.setString(redisKey, HawkTime.formatNowTime(), 600);
		WorldThreadScheduler.getInstance().postWorldTask(new WorldTask(GsConst.WorldTaskType.NATIONAL_STOREHOUSE_DONATE) {
			  @Override
			  public boolean onInvoke() {
				   NationService.getInstance().nationalWarehouseResourceIncrease(resourceId, count, serverId);
				   return true;
			  }
		});
		
		HawkLog.errPrintln("addNationWarehouseResource sysop");
	}
	
	/**(此脚本需要常驻，后面会放到SysOpService中)
	 * 修正联盟建筑数据
	 * @param params
	 * http://localhost:8080/script/sysop?op=53&guildId=1ab5-2b399l-1&serverId=60017
	 */
	private void fixGuildBuilding(Map<String, String> params){
		String fixServerId = params.getOrDefault("serverId", "");
		String guildId = params.getOrDefault("guildId", "");
		HawkLog.logPrintln("fixGuildBuilding begin, serverId:{}，guildId:{}",fixServerId,guildId);
		if(HawkOSOperator.isEmptyString(fixServerId)){
			HawkLog.logPrintln("fixGuildBuilding fixServerId null");
			return;
		}
		if(HawkOSOperator.isEmptyString(guildId)){
			HawkLog.logPrintln("fixGuildBuilding guildId null");
			return;
		}
		if(!GlobalData.getInstance().isLocalServer(fixServerId)){
			HawkLog.logPrintln("fixGuildBuilding not local server");
			return;
		}
		//先修复大本
		List<GuildManorObj>  manors = GuildManorService.getInstance().getGuildManors(guildId);
		if(manors != null && !manors.isEmpty()){
			HawkLog.logPrintln("fixGuildBuilding GuildManorObj begin guildId:{},size:{}",guildId,manors.size());
			for(GuildManorObj manor : manors){
				this.fixGuildManor(manor);
			}
		}
		//修复联盟建筑
		List<IGuildBuilding> builds = GuildManorService.getInstance().getGuildBuildings(guildId);
		if(builds != null && !builds.isEmpty()){
			HawkLog.logPrintln("fixGuildBuilding GuildBuilding begin guildId:{},size:{}",guildId,builds.size());
			for(IGuildBuilding build : builds){
				this.fixGuildBuild(build);
			}
			//检查建筑是否在领地上
			for(IGuildBuilding build : builds){
				this.checkBuildingRemove(guildId, build);
			}
		}
	}
	
	private void checkBuildingRemove(String guildId, IGuildBuilding build) {
		if (!build.isPlaceGround()) {
			return;
		}
		WorldPoint buildPoint = build.getPoint();
		if (buildPoint == null) {
			return;
		}
		// 建筑半径
		int radius = GuildManorService.getInstance().getRadiusByType(build.getBuildType());
		if (radius <= 1) {
			return;
		}
		long startTime = HawkTime.getMillisecond();
		// 建筑占用所有的点 周围占用点+中心点
		List<Point> arroundPoints = WorldPointService.getInstance().getRhoAroundPointsAll(buildPoint.getX(), buildPoint.getY(), radius);
		arroundPoints.add(WorldPointService.getInstance().getAreaPoint(buildPoint.getX(), buildPoint.getY(), false));
		// 所有领地
		List<GuildManorObj> manors = GuildManorService.getInstance().getGuildManors(guildId);
		// 建筑上有任何点在领地内,就不移除
		for (Point point : arroundPoints) {
			for (GuildManorObj manor : manors) {
				if (point == null || manor == null || manor.getEntity() == null) {
					HawkLog.errPrintln("checkBuildingRemove null error, point: {}, manor entity: {}", point, manor == null ? "manor null" : "manor entity null");
					continue;
				} 
				if (GuildManorService.getInstance().isInManor(guildId, manor.getEntity().getManorId(), point.getId())) {
					return;
				}
			}
		}
		// 移除领地建筑
		GuildManorService.getInstance().removeManorBuilding(build);
		HawkLog.logPrintln("check guild building remove, guildId:{}, pointId:{}, type:{}, radius:{}, costTime:{}",
				guildId, buildPoint.getId(), build.getBuildType(), radius, HawkTime.getMillisecond() - startTime);
	}
	
	private void fixGuildManor(GuildManorObj guildManorObj){
		ManorBastionStat stat = guildManorObj.getBastionStat();
		if(stat == ManorBastionStat.LOCKED ||
				stat ==	ManorBastionStat.OPENED){
			return;
		}
		WorldPoint worldPoint = guildManorObj.getPoint();
		//如果没有此世界点，直接修复数据
		if(worldPoint == null){
			guildManorObj.onMonorRemove();
			HawkLog.logPrintln("fixGuildBuilding fixGuildManor worldPoint null,ManorId:{},pointId:{}",
					guildManorObj.getEntity().getManorId(),guildManorObj.getPositionId());
			return;
		}
		//如果此世界点存在，但是类型不是联盟建筑，说明这个点已经被别的点占据，修复数据
		if(worldPoint.getPointType() != WorldPointType.GUILD_TERRITORY_VALUE ){
			guildManorObj.onMonorRemove();
			HawkLog.logPrintln("fixGuildBuilding fixGuildManor worldPointType !=GUILD_TERRITORY_VALUE,ManorId:{},pointId:{}",
					guildManorObj.getEntity().getManorId(),guildManorObj.getPositionId());
			return;
		}
		//如果此世界点存在，但是并不是本联盟的，说明这个点已经被别的点占据，修复数据
		String pointGuildId = worldPoint.getGuildId();
		if(!guildManorObj.getEntity().getGuildId().equals(pointGuildId)){
			HawkLog.logPrintln("fixGuildBuilding fixGuildManor worldPointGuildId != curGuild,ManorId:{},pointId:{},pointGuildId:{}",
					guildManorObj.getEntity().getManorId(),guildManorObj.getPositionId(),pointGuildId);
			guildManorObj.onMonorRemove();
			return;
		}
		//如果世界点联盟建筑类型 和  buiding不同,说明这个点已经被别的点占据，修复数据
		int buildingId = worldPoint.getBuildingId();
		TerritoryType type = TerritoryType.valueOf(buildingId);
		if(type == null || type!= TerritoryType.GUILD_BASTION){
			guildManorObj.onMonorRemove();
			HawkLog.logPrintln("fixGuildBuilding fixGuildManor worldPointBuildId != GUILD_BASTION,ManorId:{},pointId:{},buildingId:{}",
					guildManorObj.getEntity().getManorId(),guildManorObj.getPositionId(),buildingId);
		}
	}
	
	private void fixGuildBuild(IGuildBuilding building){
		GuildBuildingStat stat = building.getBuildStat();
		if(stat == GuildBuildingStat.LOCKED ||
				stat ==	GuildBuildingStat.OPENED){
			return;
		}
		WorldPoint worldPoint = building.getPoint();
		//如果没有此世界点，直接修复
		if(worldPoint == null){
			building.onBuildRemove();
			HawkLog.logPrintln("fixGuildBuilding fixGuildBing worldPoint null ,buidingdId:{},pointId:{}",
					building.getEntity().getBuildingId(),building.getPositionId());
			return;
		}
		//如果此世界点存在，但是类型不是联盟建筑，说明这个点已经被别的点占据，修复数据
		if(worldPoint.getPointType() != WorldPointType.GUILD_TERRITORY_VALUE ){
			building.onBuildRemove();
			HawkLog.logPrintln("fixGuildBuilding fixGuildBing WorldPointType !=  GUILD_TERRITORY_VALUE ,buidingdId:{},pointId:{}",
					building.getEntity().getBuildingId(),building.getPositionId());
			return;
		}
		//如果此世界点存在，但是并不是本联盟的，说明这个点已经被别的点占据，修复数据
		String pointGuildId = worldPoint.getGuildId();
		if(!building.getEntity().getGuildId().equals(pointGuildId)){
			building.onBuildRemove();
			HawkLog.logPrintln("fixGuildBuilding fixGuildBing pointGuildId !=  curGuild ,buidingdId:{},pointId:{}",
					building.getEntity().getBuildingId(),building.getPositionId());
			return;
		}
		//如果世界点联盟建筑类型 和  buiding不同,说明这个点已经被别的点占据，修复数据
		int buildingId = worldPoint.getBuildingId();
		TerritoryType type = TerritoryType.valueOf(buildingId);
		if(type == null || type!= building.getBuildType()){
			building.onBuildRemove();
			HawkLog.logPrintln("fixGuildBuilding fixGuildBing TerritoryType !=  buildingId ,buidingdId:{},pointId:{},wpBuildType:{},guildBuildType:{}",
					building.getEntity().getBuildingId(),building.getPositionId(),buildingId,building.getBuildType());
		}
	}
	
	private void technologyEntityCheck(Map<String, String> params){
		String fixServerId = params.getOrDefault("serverId", "");
		String playerId = params.getOrDefault("playerId", "");
		if(HawkOSOperator.isEmptyString(playerId)){
			logger.info("technologyEntityCheck chek playerId null");
			return;
		}
		if(HawkOSOperator.isEmptyString(fixServerId)){
			logger.info("technologyEntityCheck chek fixServerId null");
			return;
		}
		if(!GlobalData.getInstance().isLocalServer(fixServerId)){
			HawkLog.logPrintln("fixGuildBuilding not local server");
			return;
		}
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if(player == null){
			return;
		}
		if(player.isInDungeonMap()){
			logger.info("technologyEntityCheck chek inDungeonMap,playerId: {}",player.getId());
			return;
		}
		int threadIndex = Math.abs(player.getId().hashCode()) % HawkTaskManager.getInstance().getThreadNum();
		HawkTaskManager.getInstance().postTask(new HawkTask(){
			@Override
			public Object run() {
				Map<Integer,TechnologyEntity> entityMap = new HashMap<>();
				Set<TechnologyEntity> dels = new HashSet<>();
				for (TechnologyEntity entity : player.getData().getTechnologyEntities()) {
					int techId = entity.getTechId();
					int level = entity.getLevel();
					//重复了，留下等级高的
					if(entityMap.containsKey(techId)){
						TechnologyEntity dupEntity = entityMap.get(techId);
						logger.info("technologyEntityCheck chek dupEntity,playerId: {}, techId: {},techLevel:{},dupTechId: {},dupTechLevel:{}",
								player.getId(), entity.getTechId(),entity.getLevel(),dupEntity.getTechId(),dupEntity.getLevel());
						if(level > dupEntity.getLevel()){
							entityMap.put(techId, entity);
							dels.add(dupEntity);
							logger.info("technologyEntityCheck chek add remove dupEntity,playerId: {}, techId: {},techLevel:{}",
									player.getId(), dupEntity.getTechId(),dupEntity.getLevel());
						}else{
							dels.add(entity);
							logger.info("technologyEntityCheck chek add remove dupEntity,playerId: {}, techId: {},techLevel:{}",
									player.getId(), entity.getTechId(),entity.getLevel());
						}
					}else{
						entityMap.put(techId, entity);
					}
				}
				//删除数据
				for(TechnologyEntity entity : dels){
					entity.delete();
					player.getData().getTechnologyEntities().remove(entity);
					logger.info("technologyEntityCheck remove Entity action,playerId: {}, techId: {},techLevel:{}",
							player.getId(), entity.getTechId(),entity.getLevel());
					
				}
				//查看研究中的科技是否有对应的队列
				for (TechnologyEntity entity : player.getData().getTechnologyEntities()) {
					if(entity.isResearching()){
						int techCfgId = entity.getTechId() * 100 + entity.getLevel() + 1;
						Optional<QueueEntity> op = player.getData().getQueueEntities().stream()
								.filter(e ->e.getQueueType() == QueueType.SCIENCE_QUEUE_VALUE)
								.filter(e ->e.getItemId().equals(String.valueOf(techCfgId)) )
								.findAny();
						if(!op.isPresent()){
							logger.info("technologyEntityCheck queue null,playerId: {}, techId: {},techLevel:{}",
									player.getId(), entity.getTechId(),entity.getLevel());
							TechnologyCfg cfg = HawkConfigManager.getInstance().getConfigByKey(TechnologyCfg.class, techCfgId);
							if(cfg != null){
								entity.setLevel(entity.getLevel() + 1);
								logger.info("technologyEntityCheck queue null level up,playerId: {}, techId: {},techLevel:{}",
										player.getId(), entity.getTechId(),entity.getLevel());
							}
							entity.setResearching(false);
						}
					}
				}
				player.getPush().syncTechnologyInfo();
				player.getPush().syncTechSkillInfo();
				return null;
			}
		},threadIndex);
	}
	

	/**
	 * 清除脏点
	 * 
	 * 127.0.0.1:8080/script/sysop?op=22&serverId=60008&x=100&y=100
	 */
	private boolean clearWorldPoint(Map<String, String> params) {
		String serverId = params.get("serverId");
		if (!serverId.equals(GsConfig.getInstance().getServerId())) {
			return false;
		}
		int posX = Integer.parseInt(params.get("x"));
		int posY = Integer.parseInt(params.get("y"));
		
		WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(posX, posY);
		WorldPointService.getInstance().rmFromPoints(worldPoint.getId());
		WorldPointService.getInstance().getWorldScene().leave(worldPoint.getAoiObjId());
		WorldPointService.getInstance().addToAreaFreePoint(worldPoint);
		WorldPointProxy.getInstance().delete(worldPoint);
		return true;
	}
	
	/**
	 * 完成赛博任务
	 * 
	 * 127.0.0.1:8080/script/sysop?op=23&serverId=&playerId=
	 */
	private boolean finishCybor(Map<String, String> params) {
		String serverId = params.get("serverId");
		if (!GsConfig.getInstance().getServerId().equals(serverId)) {
			return true;
		}
		Player player = GlobalData.getInstance().scriptMakesurePlayer(params);
		if (player == null) {
			return true;
		}
		int score = Integer.parseInt(params.getOrDefault("score", "0"));
		int isSeasonOpen = Integer.parseInt(params.getOrDefault("isSeasonOpen", "0"));
		ActivityManager.getInstance().postEvent(new CWScoreEvent(player.getId(), score, true, isSeasonOpen == 1));
		logger.info("finishCybor, serverId:{}, playerId:{}, score:{}", serverId, player.getId(), score);
		return true;
	}

	/**
	 * grep err日志
	 * @param startTime
	 * @param endTime
	 */
	public void grepErrorLog(long startTime, long endTime) {
		HawkThreadPool threadPool = HawkTaskManager.getInstance().getThreadPool("task");
		HawkLog.logPrintln("grepErrorLog, theadPool: {}", threadPool == null ? "null" : "useful");
		if (threadPool != null) {
			HawkTask task = new HawkTask() {
				@Override
				public Object run() {
					logGrep(startTime, endTime);
					return null;
				}
			};

			task.setTypeName("grepErrorLog");
			threadPool.addTask(task);
		}
	}

	/**
	 * grep err日志
	 * @param startTime
	 * @param endTime
	 */
	private void logGrep(long startTime, long endTime) {
		HawkLog.logPrintln("grep error log start");
		if (startTime <= 0 || endTime <= 0 || startTime > endTime) {
			return;
		}
	
		int yearMonthDay = HawkTime.getYyyyMMddIntVal();
		int day = yearMonthDay % 100;
		// 10天清理一次，避免积压太多的日志文件
		if (day == 1 || day == 11 || day == 21) {
			String shell1 = "rm -f " + System.getProperty("user.dir") + "/logs/grepError*";
			String shell2 = "rm -f " + System.getProperty("user.dir") + "/logs/grepException*";
			try {
				Process process1 = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", shell1});
				process1.waitFor();
				Process process2 = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", shell2});
				process2.waitFor();
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
	
		int count = 25;
		String errLog = System.getProperty("user.dir") + "/logs/grepError_" + yearMonthDay + ".log";
		String expLog = System.getProperty("user.dir") + "/logs/grepException_" + yearMonthDay + ".log";
		do {
			String time = HawkTime.formatTime(startTime, "yyyy-MM-dd_HH");
			count--;
			startTime += HawkTime.HOUR_MILLI_SECONDS;
			String[] timeArr = time.split("_");
			String fileNamePrefix1 = "/data/home/user00/logs/game/" + timeArr[0] + "/Server.log." + time + ".";
			String fileNamePrefix2 = "/data/home/user00/logs/game/" + timeArr[0] + "/Exception.log." + time + ".";
			for (int i = 1; i <= 4; i++) {
				String fileName1 = fileNamePrefix1 + i; 
				File file1 = new File(fileName1);
				if (file1.exists()) {
					try {
						String shell = "grep ERROR " + fileName1 + " >> " + errLog;
						Process process = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", shell});
						process.waitFor();
					} catch (Exception e) {
						HawkException.catchException(e);
					}
				}
			
				String fileName2 = fileNamePrefix2 + i; 
				File file2 = new File(fileName2);
				if (file2.exists()) {
					try {
						String shell = "cat " + fileName2 + " >> " + expLog;
						Process process = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", shell});
						process.waitFor();
					} catch (Exception e) {
						HawkException.catchException(e);
					}
				}
			}
		} while (startTime <= endTime && count > 0);
	
		HawkLog.logPrintln("grep error log end");
	}

	/**
	 * 清除协议计数
	 * @param params
	 */
	private void clearProtoCounter(Map<String, String> params) {
		Player player = GlobalData.getInstance().scriptMakesurePlayer(params);
		if (player == null) {
			return;
		}
		player.clearProtoCounter(Integer.parseInt(params.get("protoType")));
	}
	
	/**
	 * 复制账号player的redis数据（基础数据只复制了mysql的数据）
	 * @param params
	 */
	public void copyPlayerRedis(String... playerIds) {
		if (playerIds == null || playerIds.length < 2) {
			return;
		}
		
		String montherPlayerId = playerIds[0];
		
		// 编队信息
		String PLAYER_PRESET_MARCH = "world_preset_march:";
		// 装扮/挂件信息
		String DRESS_SHOW = "dress_show:";
		// vip礼包信息
		String VIP_BOX_STATUS = "vip_box_status";
				
		String marchPrestInfo = RedisProxy.getInstance().getRedisSession().getString(PLAYER_PRESET_MARCH + montherPlayerId);
		String dressShowInfo = RedisProxy.getInstance().getRedisSession().getString(DRESS_SHOW + montherPlayerId);
		Map<String, String> vipBoxInfoMap = RedisProxy.getInstance().getRedisSession().hGetAll(VIP_BOX_STATUS + ":" + montherPlayerId);
		
		for (String sonPlayerId : playerIds) {
			if (sonPlayerId.equals(montherPlayerId)) {
				continue;
			}
			
			if (!HawkOSOperator.isEmptyString(marchPrestInfo)) {
				RedisProxy.getInstance().getRedisSession().setString(PLAYER_PRESET_MARCH + sonPlayerId, marchPrestInfo);
			}
			
			if (!HawkOSOperator.isEmptyString(dressShowInfo)) {
				RedisProxy.getInstance().getRedisSession().setString(DRESS_SHOW + sonPlayerId, dressShowInfo);
			}
			
			if (!vipBoxInfoMap.isEmpty()) {
				RedisProxy.getInstance().getRedisSession().del(VIP_BOX_STATUS + ":" + sonPlayerId);
				RedisProxy.getInstance().getRedisSession().hmSet(VIP_BOX_STATUS + ":" + sonPlayerId, vipBoxInfoMap, 0);
			}
		}
		
		HawkLog.logPrintln("copy month redis data to son finish");
	}

	/**
	 * 补装备
	 * @param info
	 */
	public void addArmour(String info) {
		try {
			String[] infos = info.split("&");
			
			// 先判定下区服
			String tarServerId = GlobalData.getInstance().getMainServerId(infos[0]);
			String serverId = GsConfig.getInstance().getServerId();
			if (!tarServerId.equals(serverId)) {
				logger.info("addArmour, not this server, info:{}", info);
				return;
			}
			
			// 获取玩家
			String playerId = infos[1];
			Player player = GlobalData.getInstance().makesurePlayer(playerId);
			if (player == null) {
				logger.error("addArmour, player not exist, info:{}", info);
				return;
			}
			
			int cfgId = Integer.parseInt(infos[2]);
			int level = Integer.parseInt(infos[3]);
			int quality = Integer.parseInt(infos[4]);
			int star = Integer.parseInt(infos[5]);
			String extraAttr = infos[6];
			String skillAttr = infos[7];
			String starAttr = infos[8];
			int quantum = Integer.parseInt(infos[9]);
			
			ArmourEntity entity = new ArmourEntity();
			entity.setPlayerId(playerId);
			// 装备配置id
			entity.setArmourId(cfgId);
			// 等级
			entity.setLevel(level);
			// 品质
			entity.setQuality(quality);
			// 星级
			for (int i = 0; i < star; i++) {
				entity.addStar();
			}
			// 额外属性
			List<ArmourEffObject> extraAttrEffs = SerializeHelper.stringToList(ArmourEffObject.class, extraAttr, SerializeHelper.BETWEEN_ITEMS, SerializeHelper.ATTRIBUTE_SPLIT, new CopyOnWriteArrayList<>());
			for (ArmourEffObject extraAttrEff : extraAttrEffs) {
				entity.addExtraAttrEff(extraAttrEff);
			}
			// 星级属性
			List<ArmourEffObject> starEffs = SerializeHelper.stringToList(ArmourEffObject.class, starAttr, SerializeHelper.BETWEEN_ITEMS, SerializeHelper.ATTRIBUTE_SPLIT, new CopyOnWriteArrayList<>());
			for (ArmourEffObject starEff : starEffs) {
				entity.addStarEff(starEff);
			}
			// 技能属性
			List<ArmourEffObject> skillEffs = SerializeHelper.stringToList(ArmourEffObject.class, skillAttr, SerializeHelper.BETWEEN_ITEMS, SerializeHelper.ATTRIBUTE_SPLIT, new CopyOnWriteArrayList<>());
			for (ArmourEffObject skillEff : skillEffs) {
				entity.addSkillAttrEff(skillEff);
			}
			// 量子槽位
			for (int i = 0; i < quantum; i++) {
				entity.addQuantum();
			}
			// 创建db实体
			entity.create();
			// 加到缓存里
			player.getData().getArmourEntityList().add(entity);
			// 玩家如果在线,推送给客户端
			if (player.isActiveOnline()) {
				player.getPush().syncArmourInfo(entity);
			}
			logger.info("addArmour, info:{}", info);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	
	/**
	 * 往国家医院添加统帅之战死兵
	 * 
	 * @param params
	 */
	public void addNationalHopitalTszzSoldier(Map<String, String> params) {
		Player player = GlobalData.getInstance().scriptMakesurePlayer(params);
		if (player == null) {
			HawkLog.logPrintln("addNationalHopitalTszzSoldier failed, player not exist, playerId: {}", params.get("playerId"));
			return;
		}
		
		String armysParam = params.get("armys");
		if (HawkOSOperator.isEmptyString(armysParam)) {
			return;
		}
		List<ArmyInfo> armyDeadList = new ArrayList<>();
		for (String armyInfoParam : armysParam.split(",")) {
			String[] armyInfoArr = armyInfoParam.split("_");
			int armyId = Integer.parseInt(armyInfoArr[0]);
			int count = Integer.parseInt(armyInfoArr[1]);
			if (count <= 0) {
				continue;
			}
			BattleSoldierCfg armyCfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, armyId);
			if (armyCfg == null) {
				continue;
			}
			
			ArmyEntity armyEntity = player.getData().getArmyEntity(armyId);
			if (armyEntity == null) {
				armyEntity = new ArmyEntity();
				armyEntity.setPlayerId(player.getId());
				armyEntity.setArmyId(armyId);
				HawkDBManager.getInstance().create(armyEntity);
				player.getData().addArmyEntity(armyEntity);
			} 
			ArmyInfo info = new ArmyInfo();
			info.setArmyId(armyId);
			info.setTszzNationalHospital(count);
			armyDeadList.add(info);
		}
		
		CalcSWDeadArmy msg = CalcSWDeadArmy.valueOf(armyDeadList);
		msg.setGmTrigger(true);
		HawkApp.getInstance().postMsg(player, msg);
	}
	
	/**
	 * 往国家医院添加死兵
	 * @param playerId
	 */
	public void addNationalHopitalSoldier(Map<String, String> params) {
		Player player = GlobalData.getInstance().scriptMakesurePlayer(params);
		if (player == null) {
			HawkLog.logPrintln("addNationalHopitalSoldier failed, player not exist, playerId: {}", params.get("playerId"));
			return;
		}
		
		String armysParam = params.get("armys");
		if (HawkOSOperator.isEmptyString(armysParam)) {
			return;
		}
		List<ArmyInfo> armyDeadList = new ArrayList<>();
		for (String armyInfoParam : armysParam.split(",")) {
			String[] armyInfoArr = armyInfoParam.split("_");
			int armyId = Integer.parseInt(armyInfoArr[0]);
			int count = Integer.parseInt(armyInfoArr[1]);
			if (count <= 0) {
				continue;
			}
			BattleSoldierCfg armyCfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, armyId);
			if (armyCfg == null) {
				continue;
			}
			
			ArmyEntity armyEntity = player.getData().getArmyEntity(armyId);
			if (armyEntity == null) {
				armyEntity = new ArmyEntity();
				armyEntity.setPlayerId(player.getId());
				armyEntity.setArmyId(armyId);
				HawkDBManager.getInstance().create(armyEntity);
				player.getData().addArmyEntity(armyEntity);
			} 
			ArmyInfo armyInfo = new ArmyInfo();
			armyInfo.setArmyId(armyId);
			armyInfo.setDeadCount(count);
			armyInfo.setDirectDeadCount(count);
			armyDeadList.add(armyInfo);
		}
		
		CalcDeadArmy msg  = CalcDeadArmy.valueOf(armyDeadList);
		msg.setGmTrigger(true);
		HawkApp.getInstance().postMsg(player, msg);
	}
	
	/**
	 * 复制玩家数据（拉取母号的数据）
	 */
	public void copyPlayerData() {
		if (PlayerCopyService.getInstance().init()) {
			PlayerCopyService.getInstance().selectMotherPlayer();
		}
	}
	
	/**
	 * 复制账号player的redis数据（基础数据只复制了mysql的数据）
	 * @param params 
	 */
	public void copyPlayerRedis(Map<String, String> params) {
		String motherPlayerId = params.get("motherPlayerId");
		if (HawkOSOperator.isEmptyString(motherPlayerId)) {
			return;
		}
		
		String sonPlayerIds = params.get("sonPlayerIds");
		if (HawkOSOperator.isEmptyString(sonPlayerIds)) {
			return;
		}
		sonPlayerIds = motherPlayerId + "," + sonPlayerIds;
		String[] sonPlayerIdArray = sonPlayerIds.split(",");
		copyPlayerRedis(sonPlayerIdArray);
	}
	
	/**
	 * 清除账号数据复制的标识（使得一个已经复制过目标数据的角色还能再次复制）
	 * @param params
	 */
	public void clearCopyPlayerPlayer(Map<String, String> params) {
		String playerId = params.getOrDefault("playerId", "123");
		String redisKey = "copyPlayer:" + GsConfig.getInstance().getServerId();
		RedisProxy.getInstance().getRedisSession().hDel(redisKey, playerId);
	}
	
	/**
	 * 账号复制数据处理（前置处理）
	 * 
	 * montherRoleData.txt数据：
	 * insert into accumulate_online(id,playerId,dayCount,receivedId,receivedTime,onlineTime,createTime,updateTime,invalid) values('12kx-2vgz51-7','12kx-2vq6nh-1',6,0,0,0,1683792485808,1684166578744,0);
	 * 
	 */
	public String copyDataProc(int direct) {
		String motherRoleInfo = GameConstCfg.getInstance().getMotherRoleInfo();
		String[] montherKeyData = motherRoleInfo.split(",");
		if (montherKeyData.length < 5) {
			HawkLog.logPrintln("copyDataProc failed, monther key data: {}", motherRoleInfo);
			return "motherRoleInfo配置不正确";
		}
		
		List<String> sonPlayerIds = this.getSonPlayerIdList();
		if (sonPlayerIds.isEmpty()) {
			HawkLog.logPrintln("copyDataProc failed, sonPlayerIds not config");
			return "子号角色Id信息empty";
		}
		
		List<String> motherDataList = new ArrayList<>();
		try {
			if (direct > 0) {
				HawkOSOperator.readTextFileLines("logs/Copy.log", motherDataList);
			} else {
				HawkOSOperator.readTextFileLines("tmp/motherRoleData.txt", motherDataList);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		if (motherDataList.isEmpty()) {
			HawkLog.logPrintln("copyDataProc failed, data count error");
			return "母号数据empty";
		}
		
		// 本服serverId
		String serverId = GsConfig.getInstance().getServerId();
		String redisKey = "copyPlayer:" + serverId;
		
		// 母号的信息
		String motherOpenid = montherKeyData[0];
		String motherName = montherKeyData[1];
		String motherRoleId = montherKeyData[2];
		String motherChannel = montherKeyData[3];
		String motherPlatform = montherKeyData[4];
		String motherServerId = montherKeyData.length >= 6 ? montherKeyData[5] : serverId;
		
		List<String[]> sonPlayerInfos = new ArrayList<>();
		for (String sonPlayerId : sonPlayerIds) {
			Player player = GlobalData.getInstance().makesurePlayer(sonPlayerId);
			if (player == null) {
				continue;
			}
			
			String[] sonData = new String[5];
			sonData[0] = player.getOpenId();
			sonData[1] = player.getName();
			sonData[2] = player.getId();
			sonData[3] = player.getChannel();
			sonData[4] = player.getPlatform();
			sonPlayerInfos.add(sonData);
		}
		
		String firstTable = null;
		String motherRoleIdSeg = "'" + motherRoleId + "'";
		List<String> motherDataLatest = new ArrayList<>();
		for (int i = 0; i < motherDataList.size(); i++) {
			String data = motherDataList.get(i);
			// 不是指定母号的数据
			if (data.indexOf(motherRoleIdSeg) < 0) {
				continue;
			}
			
			// insert into accumulate_online(id,playerId,....
			String tableInsertString = data.substring(0, data.indexOf("(") + 1); // 注意这里最后一个字符"("不能少，防止出现一张表的表名是另一张表名的前缀的情况
			// 第一张表
			if (firstTable == null) {
				firstTable = tableInsertString;
			} else if (firstTable.equals(tableInsertString)) {
				// 读到的不是第一份母号数据（注意第一份不代表是第一条）, 先将前面读到的数据（即第n-1份数据）清空，之后再将此次读到的数据（即第n份数据）添加到 motherDataLatest中
				motherDataLatest.clear();
			}
			
			motherDataLatest.add(data);
		}
		
		String output = System.getProperty("user.dir") + "/tmp/Copy.log";
		File file = new File(output);
		if (file.exists()) {
			file.delete();
		}
		
		FileWriter fileWriter = null;
		try {
			StringBuilder sb = new StringBuilder();
			fileWriter = new FileWriter(output, true);
			for (int i = 0; i < sonPlayerInfos.size(); i++) {
				String[] sonData = sonPlayerInfos.get(i);
				if (sonData.length < 5) {
					HawkLog.errPrintln("copyDataProc failed, sonData: {}, size: {}", sonData[0], sonData.length);
					continue;
				}
				
				// 子号原始信息
				String sonOpenid = sonData[0];
				String sonName = sonData[1];
				String sonRoleId = sonData[2];
				String sonChannel = sonData[3];
				String sonPlatform = sonData[4];
				
				sb.append(sonRoleId).append(",");
				RedisProxy.getInstance().getRedisSession().hDel(redisKey, sonRoleId);
				// insert into player(id,puid,openid,serverId,name,...,platform,channel,channelId,country,deviceId,...) values('12kx-2vgz51-1','mother001#android','mother001','50001','mother001',...,'android','guest','0','cn','mother001',...)
				for (String data : motherDataLatest) {
					// 说明这条数据不是指定母账号的数据
					if (data.indexOf(motherRoleIdSeg) < 0) {
						continue;
					}
					
					// 角色id替换（下面替换时都带单引号，是为了尽量精确匹配）
					String sonDataNew = data.replace(motherRoleIdSeg, "'" + sonRoleId + "'");
					// 平台信息替换
					if (!sonPlatform.equals(motherPlatform)) {
						sonDataNew = sonDataNew.replace("'" + motherPlatform + "'", "'" + sonPlatform + "'");
					}
					// puid信息替换
					sonDataNew = sonDataNew.replace("'" + motherOpenid + "#" + motherPlatform + "'", "'" + sonOpenid + "#" + sonPlatform + "'");
					
					// serverId,name联合替换
					sonDataNew = sonDataNew.replace("'" + motherServerId + "','" + motherName + "'", "'" + serverId + "','" + sonName + "'");
					
					// openid和角色名称替换（openid要在platform之后替换，因为puid信息中包含platform）
					sonDataNew = sonDataNew.replace("'" + motherOpenid + "'", "'" + sonOpenid + "'").replace("'" + motherName + "'", "'" + sonName + "'");
					
					// 渠道信息替换
					if (!sonChannel.equals(motherChannel)) {
						sonDataNew = sonDataNew.replace("'" + motherChannel + "'", "'" + sonChannel + "'");
					}
					// 区服信息替换
					if (!serverId.equals(motherServerId)) {
						sonDataNew = sonDataNew.replace("'" + motherServerId + "'", "'" + serverId + "'");
					}
					
					// 最终生成的新的子号信息
					fileWriter.write(sonDataNew);
					fileWriter.write("\r\n");
				}
				
				HawkLog.logPrintln("copyDataProc playerdata succcess, playerId: {}, openid: {}, platform: {}", sonRoleId, sonOpenid, sonPlatform);
			}
			
			HawkLog.logPrintln("copyDataProc sonData roldIds: {}", sb.toString());
		} catch (IOException e) {
			HawkException.catchException(e);
		} finally {
			if (fileWriter != null) {
				try {
					fileWriter.flush();
					fileWriter.close();
				} catch (IOException e1) {
					HawkException.catchException(e1);
				}
			}
		}
		
		HawkLog.logPrintln("copyDataProc success");
		return "";
	}
	
	/**
	 * 移除玩家城点（玩家登录时重新落地）
	 * @param params
	 * @return
	 */
	public String removeCity(Map<String, String> params) {
		Player player = null;
		if (params.containsKey("playerId") || params.containsKey("playerName")) {
			player = GlobalData.getInstance().scriptMakesurePlayer(params);
			if (player == null) {
				return HawkScript.failedResponse(-1, "参数错误，不存在对应玩家");
			}
			
			if (!GlobalData.getInstance().isLocalPlayer(player.getId())) {
				return HawkScript.failedResponse(-1, "不是本服玩家，不可操作");
			}
		}
		
		if (player != null) {
			Player finalPlayer = player;
			try {
				CityManager.getInstance().moveCity(player.getId(), true);
				GsApp.getInstance().addDelayAction(2000, new HawkDelayAction() {
					@Override
					protected void doAction() {
						armysCheckAndFix(finalPlayer);
					}
				});
				HawkLog.logPrintln("testOp remove city, playerId: {}", player.getId());
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		} else {
			List<String> sonPlayerIds = SysOpService.getInstance().getSonPlayerIdList();
			for (String playerId : sonPlayerIds) {
				try {
					Player sonPlayer = GlobalData.getInstance().makesurePlayer(playerId);
					if (sonPlayer == null) {
						HawkLog.logPrintln("testOp remove city failed, playerId: {}", playerId);
						continue;
					}
					CityManager.getInstance().moveCity(playerId, true);
					GsApp.getInstance().addDelayAction(2000, new HawkDelayAction() {
						@Override
						protected void doAction() {
							armysCheckAndFix(sonPlayer);
						}
					});
					HawkLog.logPrintln("testOp remove city, playerId: {}", playerId);
				} catch (Exception e) {
					HawkException.catchException(e);
				}
			}
		}
		
		return HawkScript.successResponse("succ");
	}
	
	public void armysCheckAndFix(Player player) {
		int marchCount = WorldMarchService.getInstance().getPlayerMarchCount(player.getId());
		if (marchCount > 0) {
			HawkLog.logPrintln("testOp remove city, armysCheckAndFix, playerId: {}, marchCount: {}", player.getId(), marchCount);
			return;
		}

		List<Integer> armyIds = new ArrayList<Integer>();
		for (ArmyEntity armyEntity : player.getData().getArmyEntities()) {
			// 出征中的army数量
			int marchArmyCount = armyEntity.getMarch();
			if (marchArmyCount <= 0) {
				HawkLog.logPrintln("testOp remove city, armysCheckAndFix, playerId: {}, marchArmyCount: {}", player.getId(), marchArmyCount);
				continue;
			}
			
			int armyId = armyEntity.getArmyId();
			armyIds.add(armyId);
			
			armyEntity.clearMarch();
			armyEntity.addFree(marchArmyCount);
			LogUtil.logArmyChange(player, armyEntity, marchArmyCount, ArmySection.FREE, ArmyChangeReason.MARCH_FIX);
			HawkLog.logPrintln("testOp remove city, armysCheckAndFix, playerId:{}, armyId:{}, marchArmyCount:{}, armyFree:{}", armyEntity.getPlayerId(), armyEntity.getId(), marchArmyCount, armyEntity.getFree());
		}

		if (!armyIds.isEmpty()) {
			player.getPush().syncArmyInfo(ArmyChangeCause.MARCH_BACK, armyIds.toArray(new Integer[armyIds.size()]));
		}
	}
	
	/**
	 * 获取子号的角色id
	 * @return
	 */
	public List<String> getSonPlayerIdList() {
		List<String> sonPlayerIds = GameConstCfg.getInstance().getSonPlayerIdList();
		if (!sonPlayerIds.isEmpty()) {
			return sonPlayerIds;
		}
		
		Set<String> roleSet = LocalRedis.getInstance().getAllRobotRole();
		if (!roleSet.isEmpty()) {
			sonPlayerIds = new ArrayList<>();
			sonPlayerIds.addAll(roleSet);
		}
		
		return sonPlayerIds;
	}
	
	public void starWarSeasonEvent(String playerId, long kill, long dead){
		ActivityManager.getInstance().postEvent(new SWScoreEvent(playerId,kill,dead,HawkTime.getMillisecond(),true));
	}
	
	/**
	 * 清缓存
	 * @param playerId
	 */
	public void clearPlayerCache(String playerId) {
		// 获取玩家
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		// 玩家在线的话,清掉玩家
		if (GlobalData.getInstance().isOnline(playerId)) {
			player.kickout(Status.Error.MIGRATE_FINISH_VALUE, true, "");
		}
		GlobalData.getInstance().removeActivePlayer(playerId);
		
		try {
			HawkThread.sleep(1000L);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		// 移除player
		HawkXID xid = HawkXID.valueOf(GsConst.ObjType.PLAYER, player.getId());
		GsApp.getInstance().removeObj(xid);
		// 清playerData cache
		PlayerData data = player.getData();
		data.loadPlayerData(playerId);
		
		// 清除playerData
		player.updateData(null);
		// GlobalData移除playerData
		GlobalData.getInstance().invalidatePlayerData(playerId);
		// 清掉活动数据缓存
		PlayerActivityData activityData = PlayerDataHelper.getInstance().getPlayerData(playerId, false);
		if (activityData != null) {
			activityData.getDataMap().clear();
			activityData.getPlayerActivityEntityMap().clear();
		}
	}
	
	@SuppressWarnings("unchecked")
	public void immgration(String playerId, String serverId) {
		// 先清掉玩家缓存
		clearPlayerCache(playerId);
		// 清掉活动数据缓存
		PlayerActivityData activityData = PlayerDataHelper.getInstance().getPlayerData(playerId, false);
		if (activityData != null) {
			activityData.getDataMap().clear();
			Map<Integer, ActivityPlayerEntity> playerActivityEntityMap = activityData.getPlayerActivityEntityMap();
			for (ActivityPlayerEntity entity : playerActivityEntityMap.values()) {
				entity.delete(false);
				HawkLog.logPrintln("delete activity entity:{}", entity);
			}
			playerActivityEntityMap.clear();
		}
		
		// 删除本地所有数据
		PlayerDataSerializer.deleteAllEntity(playerId);
		
		for (PlayerDataKey dataKey : EnumSet.allOf(PlayerDataKey.class)) {
			try {
				// 方尖碑不处理
				if (dataKey == PlayerDataKey.ObeliskEntities) {
					continue;
				}
				// 从redis读取数据
				byte[] bytes = RedisProxy.getInstance().getRedisSession().hGetBytes("player_data:" + playerId, dataKey.name());
				// 反序列化
				Object data = PlayerDataSerializer.unserializeData(dataKey, bytes, false);
				if (data == null) {
					continue;
				}
				if (!dataKey.listMode()) {
					HawkDBEntity entity = (HawkDBEntity) data;
					if (dataKey == PlayerDataKey.PlayerEntity) {
						PlayerEntity playerEntity = (PlayerEntity) data;
						playerEntity.setServerId(serverId);
						GlobalData.getInstance().updateAccountInfo(playerEntity.getPuid(), serverId, playerId, 0, playerEntity.getName());
					}
					
					if (dataKey == PlayerDataKey.PlayerBaseEntity) {
						PlayerBaseEntity playerBaseEntity = (PlayerBaseEntity) data;
						playerBaseEntity.setSaveAmt(0);
						playerBaseEntity._setChargeAmt(0);
					}
					entity.setPersistable(true);
					entity.create();
				} else {
					List<HawkDBEntity> entityList = (List<HawkDBEntity>) data;
					for (HawkDBEntity entity : entityList) {
						try {
							entity.setPersistable(true);
							entity.create();
						} catch (Exception e) {
							HawkException.catchException(e);
						}
					}
				}
			} catch (Exception e) {
				HawkException.catchException(e);				
			}
		}
		// 反序列化活动数据
		immgrationActivityData(playerId);
		// 添加进迁入玩家列表
		GlobalData.getInstance().addImmgrationInPlayerIds(playerId);
		
		// 再清一遍玩家缓存
		clearPlayerCache(playerId);
	}
	
	/**
	 * 反序列化活动数据
	 * @param playerId
	 */
	private void immgrationActivityData(String playerId) {
		String key = "player_data_activity:" + playerId;
		HawkRedisSession session = RedisProxy.getInstance().getRedisSession();
		Map<byte[], byte[]> infos = session.hGetAllBytes(key.getBytes());
		if (infos == null || infos.isEmpty()) {
			return;
		}
		for (Entry<byte[], byte[]> info : infos.entrySet()) {
			try {
				// 活动ID
				String activityId = new String(info.getKey());
				// 数据
				byte[] data = info.getValue();
				
				// 反序列化存储
				com.hawk.activity.type.ActivityType type = com.hawk.activity.type.ActivityType.getType(Integer.parseInt(activityId));
				Class<? extends HawkDBEntity> clz = type.getDbEntity();
				if (clz == null) {
					logger.info("immgrationActivityData error, clz not exist, playerId:{}, activityId:{}, data:{}, type:{}", playerId, activityId, data, type.name());
					continue;
				}
				HawkDBEntity entity = clz.newInstance();
				entity.setPersistable(true);
				if (!entity.parseFrom(data)) {
					logger.info("immgrationActivityData error, parse error, playerId:{}, activityId:{}, data:{}, type:{}", playerId, activityId, data, type.name());
					continue;
				}			
				entity.afterRead();
				entity.create();
				logger.info("immgration activity data success, playerId:{}, activityId:{}, data:{}, type:{}", playerId, activityId, data, type.name());
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void getChangeServerPower(){
		logger.info("================ getChangeServerPower start ======================");
		try {
			Optional<ChangeServerActivity> opActivity = ActivityManager.getInstance().getGameActivityByType(ActivityType.CHANGE_SVR_ACTIVITY.intValue());
			if(!opActivity.isPresent()){
				return;
			}
			ChangeServerActivity activity = opActivity.get();
			Class activityClass = ChangeServerActivity.class;
			Field field = activityClass.getDeclaredField("powerRankList");
			field.setAccessible(true);
			List<Activity.ChangeServerActivityPlayerInfo.Builder> powerRankList = (List<Activity.ChangeServerActivityPlayerInfo.Builder>)field.get(activity);
			for(Activity.ChangeServerActivityPlayerInfo.Builder builder : powerRankList){
				try {
					logger.info("getChangeServerPower :{}", JsonFormat.printToString(builder.build()));
				}catch (Exception e){
					HawkException.catchException(e);
				}
			}
		}catch (Exception e){
			HawkException.catchException(e);
		}
		logger.info("================ getChangeServerPower end ======================");
	}
	
	public void getChangeServerPowerFromRedis(String termId, String serverId, int count){
		logger.info("================ getChangeServerPowerFromRedis start ======================");
		String key = "CHANGE_SVR:" + termId + ":" + serverId + ":POWER";
		List<byte[]> powers = ActivityGlobalRedis.getInstance().getRedisSession().lRange(key.getBytes(), 0, count, 0);
		for (byte[] bytes : powers) {
			try {
				Activity.ChangeServerActivityPlayerInfo.Builder playerInfo = Activity.ChangeServerActivityPlayerInfo.newBuilder();
				playerInfo.mergeFrom(bytes);
				logger.info("getChangeServerPowerFromRedis :{}",JsonFormat.printToString(playerInfo.build()));
			}catch (Exception e){
				HawkException.catchException(e);
			}
		}
		logger.info("================ getChangeServerPowerFromRedis end ======================");
	}
	
	@SuppressWarnings("rawtypes")
	public void fixChangeServerPower(String termId, String beforeServerId, String beforeServerIdentify, int beforecount, String afterServer, int afterCount, int isReal){
		logger.info("================ fixChangeServerPower start ======================");
		try {
			Optional<ChangeServerActivity> opActivity = ActivityManager.getInstance().getGameActivityByType(ActivityType.CHANGE_SVR_ACTIVITY.intValue());
			if(!opActivity.isPresent()){
				return;
			}
			Set<Tuple> rankSet = ActivityGlobalRedis.getInstance().getRedisSession().zRevrangeWithScores(beforeServerId+":"+beforeServerIdentify+":pnoarmy_rank",0, beforecount, 0);
			String key = "CHANGE_SVR:" + termId + ":" + afterServer + ":POWER";
			List<byte[]> powers = ActivityGlobalRedis.getInstance().getRedisSession().lRange(key.getBytes(), 0, afterCount, 0);
			Map<String, Activity.ChangeServerActivityPlayerInfo.Builder> map = new HashMap<>();
			for (byte[] bytes : powers) {
				try {
					Activity.ChangeServerActivityPlayerInfo.Builder playerInfo = Activity.ChangeServerActivityPlayerInfo.newBuilder();
					playerInfo.mergeFrom(bytes);
					map.put(playerInfo.getPlayerId(), playerInfo);
				}catch (Exception e){
					HawkException.catchException(e);
				}
			}
			List<Activity.ChangeServerActivityPlayerInfo.Builder> powerRankList = new ArrayList<>();
			for (Tuple tuple : rankSet) {
				String playerId = tuple.getElement();
				long score = (long) tuple.getScore();
				Activity.ChangeServerActivityPlayerInfo.Builder playerInfo = map.get(playerId);
				if(playerInfo == null){
					logger.info("fixChangeServerPower playerInfo is null, playerId :{},score:{}",playerId,score);
					continue;
				}
				playerInfo.setPower(score);
				powerRankList.add(playerInfo);
			}
			if(isReal == 222555888){
				ChangeServerActivity activity = opActivity.get();
				Class activityClass = ChangeServerActivity.class;
				Field field = activityClass.getDeclaredField("powerRankList");
				field.setAccessible(true);
				field.set(activity, powerRankList);
			}else {
				for(Activity.ChangeServerActivityPlayerInfo.Builder builder : powerRankList){
					try {
						logger.info("fixChangeServerPower :{}",JsonFormat.printToString(builder.build()));
					}catch (Exception e){
						HawkException.catchException(e);
					}
				}
			}
		}catch (Exception e){
			HawkException.catchException(e);
		}
		logger.info("================ fixChangeServerPower end ======================");
	}
	
	public void fixChangeServerPower(int termId, int isReal){
		logger.info("================ fixChangeServerPower start ======================");
		try {
			Optional<ChangeServerActivity> opActivity = ActivityManager.getInstance().getGameActivityByType(ActivityType.CHANGE_SVR_ACTIVITY.intValue());
			if(!opActivity.isPresent()){
				return;
			}
			ChangeServerTimeCfg cfg = HawkConfigManager.getInstance().getConfigByKey(ChangeServerTimeCfg.class, termId);
			if(cfg == null){
				return;
			}
			List<Activity.ChangeServerActivityPlayerInfo.Builder> powerRankList = new ArrayList<>();
			Set<Tuple> rankSet = LocalRedis.getInstance().getRankList(Rank.RankType.PLAYER_NOARMY_POWER_RANK, 1000);
			for (Tuple tuple : rankSet) {
				String playerId = tuple.getElement();
				Player player = GlobalData.getInstance().makesurePlayer(playerId);
				if(player == null){
					continue;
				}
				long score = (long) tuple.getScore();
				Activity.ChangeServerActivityPlayerInfo.Builder playerInfo = creatNewInfo(player, cfg);
				if(playerInfo == null){
					continue;
				}
				playerInfo.setPower(score);
				powerRankList.add(playerInfo);
			}
			if(isReal == 222555888){
				ChangeServerActivity activity = opActivity.get();
				Field field = ChangeServerActivity.class.getDeclaredField("powerRankList");
				field.setAccessible(true);
				field.set(activity, powerRankList);
				String mainServer = GsConfig.getInstance().getServerId();
				String serverId = cfg.getToServerIds(mainServer).get(0);
				String key = "CHANGE_SVR:" + termId + ":" + serverId + ":POWER";
				for(Activity.ChangeServerActivityPlayerInfo.Builder info : powerRankList){
					ActivityGlobalRedis.getInstance().getRedisSession().lPush(key.getBytes(), 0, info.build().toByteArray());
				}
			}else {
				String mainServer = GsConfig.getInstance().getServerId();
				String serverId = cfg.getToServerIds(mainServer).get(0);
				String key = "CHANGE_SVR:" + termId + ":" + serverId + ":POWER";
				logger.info("fixChangeServerPower key:{}",key);
				for(Activity.ChangeServerActivityPlayerInfo.Builder builder : powerRankList){
					try {
						logger.info("fixChangeServerPower :{}",JsonFormat.printToString(builder.build()));
					}catch (Exception e){
						HawkException.catchException(e);
					}
				}
			}
		}catch (Exception e){
			HawkException.catchException(e);
		}
		logger.info("================ fixChangeServerPower end ======================");
	}
	
	@SuppressWarnings("unchecked")
	public void fixChangeServerApply(Map<String, String> params) {
		logger.info("================ fixChangeServerApply start ======================");
		try {
			Player player = GlobalData.getInstance().scriptMakesurePlayer(params);
			if (player == null) {
				logger.info("fixChangeServerApply player is null");
				return;
			}
			Optional<ChangeServerActivity> opActivity = ActivityManager.getInstance().getGameActivityByType(ActivityType.CHANGE_SVR_ACTIVITY.intValue());
			if (!opActivity.isPresent()) {
				logger.info("fixChangeServerApply activity is null");
				return;
			}
			ChangeServerActivity activity = opActivity.get();
			int termId = activity.getActivityTermId();
			if(termId == 0){
				logger.info("fixChangeServerApply termId is zero");
				return;
			}
			ChangeServerTimeCfg cfg = HawkConfigManager.getInstance().getConfigByKey(ChangeServerTimeCfg.class, termId);
			if(cfg == null){
				logger.info("fixChangeServerApply cfg is null");
				return;
			}
			Activity.ChangeServerActivityPlayerInfo.Builder playerInfo = creatNewInfo(player, cfg);
			if(playerInfo == null){
				logger.info("fixChangeServerApply playerInfo is null");
				return;
			}
			Field field1 = ChangeServerActivity.class.getDeclaredField("applyMap");
			field1.setAccessible(true);
			Map<String, Activity.ChangeServerActivityPlayerInfo.Builder> applyMap = (Map<String, Activity.ChangeServerActivityPlayerInfo.Builder>)field1.get(activity);
			Field field2 = ChangeServerActivity.class.getDeclaredField("showList");
			field2.setAccessible(true);
			List<Activity.ChangeServerActivityPlayerInfo.Builder> showList = (List<Activity.ChangeServerActivityPlayerInfo.Builder>)field2.get(activity);
			if(applyMap.containsKey(playerInfo.getPlayerId())){
				logger.info("fixChangeServerApply applyMap have data");
				return;
			}
			applyMap.put(playerInfo.getPlayerId(), playerInfo);
			showList.add(playerInfo);
			String mainServer = GsConfig.getInstance().getServerId();
			String serverId = cfg.getToServerIds(mainServer).get(0);
			String key = "CHANGE_SVR:" + termId + ":" + serverId + ":SHOW";
			ActivityGlobalRedis.getInstance().getRedisSession().hSetBytes(key, playerInfo.getPlayerId(), playerInfo.build().toByteArray());
		}catch (Exception e){
			HawkException.catchException(e);
		}
		logger.info("================ fixChangeServerApply end ======================");
	}
	
	@SuppressWarnings("deprecation")
	private Activity.ChangeServerActivityPlayerInfo.Builder creatNewInfo(Player player, ChangeServerTimeCfg cfg){
		try {
			Activity.ChangeServerActivityPlayerInfo.Builder playerInfo = Activity.ChangeServerActivityPlayerInfo.newBuilder();
			playerInfo.setPlayerId(player.getId());
			playerInfo.setPlayerName(player.getName());
			playerInfo.setPfIcon(player.getPfIcon());
			playerInfo.setIcon(player.getIcon());
			playerInfo.setGuildName(player.getGuildName());
			playerInfo.setScore(0);
			playerInfo.setRank(-1);
			String scourServerId = cfg.getMainServer(player.getServerId());
			playerInfo.setServerId(scourServerId);
			playerInfo.setChangeServerId(scourServerId);
			playerInfo.setTotalCount(0);
			return playerInfo;
		}catch (Exception e){
			HawkException.catchException(e);
			return null;
		}
	}
	
	public void fixChangeServerData(Map<String, String> params) {
		String playerId = params.get("playerId");
		String entityId = params.get("entityId");
		fixChangeServerData(playerId, entityId);
	}

	public void fixChangeServerData(String playerId, String entityId) {
		logger.info("================ fixChangeServerData start ======================");
		try {
			Player player = GlobalData.getInstance().makesurePlayer(playerId);
			if(player == null){
				logger.info("fixChangeServerData player is null");
				return;
			}
			EquipResearchEntity entity = null;
			List<EquipResearchEntity> equipResearchEntityList = player.getData().getEquipResearchEntityList();
			for (EquipResearchEntity researchEntity : equipResearchEntityList) {
				if(researchEntity.getId().equals(entityId)){
					entity = researchEntity;
				}
			}
			if(entity == null){
				logger.info("fixChangeServerData entity is null");
				return;
			}
			equipResearchEntityList.remove(entity);
			entity.delete();
		}catch (Exception e){
			HawkException.catchException(e);
		}
		logger.info("================ fixChangeServerData end ======================");
	}
	
	private void fixNationConstruction() {
		List<String> lines = new ArrayList<>();
		try {
			HawkOSOperator.readTextFileLines("tmp/nationFix.txt", lines);
		} catch (Exception e) {
			HawkException.catchException(e);
			return;
		}
		for (String line : lines) {
			String[] split = line.split(",");
			String serverId = split[0];
			if (!serverId.equals(GsConfig.getInstance().getServerId())) {
				continue;
			}
			int buildingId = Integer.parseInt(split[1]);
			int level = Integer.parseInt(split[2]);
			int buildingStatus = Integer.parseInt(split[3]);
			int buildVal = Integer.parseInt(split[4]);
			int totalVal = Integer.parseInt(split[5]);
			int buildTime = Integer.parseInt(split[6]);
			fixNationConstruction(buildingId, level, buildingStatus, buildVal, totalVal, buildTime);
		}
	}
	
	/**
	 * 修复国家建筑数据
	 */
	private void fixNationConstruction(int buildId,int level,int buildingStatus,int buildVal,int totalVal,int buildTime) {
		try {
			NationalBuilding building = NationService.getInstance().getNationBuildingByTypeId(buildId);
			NationConstructionEntity entity = building.getEntity();
			entity.setLevel(level);
			entity.setBuildingStatus(buildingStatus);
			entity.setBuildVal(buildVal);
			entity.setTotalVal(totalVal);
			entity.setBuildTime(buildTime);
			building.levelupOver();
			building.boardcastBuildState();
			NationService.getInstance().boardcastNationalStatus();
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 修复国家飞船数据
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void fixNationShip() {
		String servers = "10549_10022_10607_10453_10705_10097_10162_10159_10041_10595_10469_10491_10300_10241_10293_10347";
		if (!servers.contains(GsConfig.getInstance().getServerId())) {
			return;
		}
		for (int i = 1; i <= 6; i++) {
			try {
				NationShipFactory shipFactory = (NationShipFactory) NationService.getInstance().getNationBuildingByType(NationbuildingType.NATION_SHIP_FACTORY);
				Class clazz = NationShipFactory.class;
				Field field = clazz.getDeclaredField("allComponent");
				field.setAccessible(true);
				Map<Integer, NationShipComponentEntity> entities = (Map<Integer, NationShipComponentEntity>)field.get(shipFactory);
				NationShipComponentEntity entity = entities.get(i);
				entity.setLevel(10);
				entity.setUpdateTime(0);
				shipFactory.boardcastBuildState();
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
	}
	
	/**
	 * 修复联盟总动员联盟经验缺失
	 */
	private void fixAllianceCarnivalGuildExp(){
		logger.info("================ fixAllianceCarnivalGuildExp start ======================");
		try {
			Optional<AllianceCarnivalActivity> opActivity = ActivityManager.getInstance().getGameActivityByType(ActivityType.ALLIANCE_CARNIVAL.intValue());
			if(!opActivity.isPresent()){
				return;
			}
			Map<String, ACBInfo> baseInfoMap = AllianceCarnivalActivity.baseInfos;
			String serverId = GsConfig.getInstance().getServerId();
			Map<String,String> errGuilds = new HashMap<>();
			for(Map.Entry<String, ACBInfo> guildEntry : baseInfoMap.entrySet()){
				String guildId = guildEntry.getKey();
				ACBInfo guildInfo = guildEntry.getValue();
				int guildExp = guildInfo.getExp();
				Map<String, ACRankInfo> rankMap = AllianceCarnivalActivity.rankObj.getGuildRank(guildId);
				if(Objects.isNull(rankMap)){
					//如果拿不到相应的排行榜数据，不管
					logger.info("====fixAllianceCarnivalGuildExp ACRankInfo is null guildId:{}",guildId);
					continue;
				}
				//计算总分
				int rankExp = 0;
				for(Map.Entry<String, ACRankInfo> rankEntry : rankMap.entrySet()){
					ACRankInfo rankInfo = rankEntry.getValue();
					rankExp += rankInfo.getExp();
				}
				//如果排行榜的总积分 比  联盟记录的积分要多 就修复一下
				if(rankExp > guildExp){
					String str = guildExp+"_"+rankExp;
					String key = serverId+"_"+guildId;
					errGuilds.put(key, str);
					logger.info("====fixAllianceCarnivalGuildExp errGuild guildId:{},guildExp:{},rankExp:{}",guildId,guildExp,rankExp);
				}
			}
			String dataKey = "202409061120FixAllianceCarnivalGuildExp"+HawkTime.getYyyyMMddIntVal();
			RedisProxy.getInstance().getRedisSession().hmSet(dataKey,errGuilds, (int)TimeUnit.DAYS.toSeconds(30));
			
		}catch (Exception e){
			HawkException.catchException(e);
		}
		logger.info("================ fixAllianceCarnivalGuildExp end ======================");
	}
	
	/**
	 * 处理赛博拆服后的脏数据
	 */
	private void fixCyborgWarRank(){
		String serverId = GsConfig.getInstance().getServerId();
		Set<Tuple> tuples = CyborgWarRedis.getInstance().getCWTeamPowerRanks(0, 1000, serverId);
		List<String> teamIds = tuples.stream().map(t -> t.getElement()).collect(Collectors.toList());
		Map<String, CWTeamData> teamDatas = CyborgWarRedis.getInstance().getCWTeamData(teamIds);
		logger.info("fixCyborgWarRank begin,serverId:{}", serverId);
		int rank = 0;
		for (String teamId : teamIds) {
			try {
				rank++;
				logger.info("fixCyborgWarRank team begin,teamId:{}", teamId);
				CWTeamData teamData = teamDatas.get(teamId);
				if (teamData == null) {
					logger.info("fixCyborgWarRank team is null,teamId:{}, rank:{}", teamId, rank);
					CyborgWarRedis.getInstance().removeCWTeamPowerRank(teamId);
					continue;
				}
				String guildId = teamData.getGuildId();
				GuildInfoObject guildObj = GuildService.getInstance().getGuildInfoObject(guildId);
				if (guildObj == null) {
					logger.info("fixCyborgWarRank team guild is null,teamId:{},rank:{}", teamId, rank);
					CyborgWarRedis.getInstance().removeCWTeamPowerRank(teamId);
					continue;
				}
				Set<String> idList = CyborgWarRedis.getInstance().getCWPlayerIds(teamId);
				for (String playerId : idList) {
					logger.info("fixCyborgWarRank team is right,teamId:{},playerId:{},rank:{}", teamId, playerId, rank);
				}
				logger.info("fixCyborgWarRank team end,teamId:{}", teamId);
			}catch (Exception e){
				logger.info("fixCyborgWarRank error,teamId:{}", teamId);
			}
		}
		logger.info("fixCyborgWarRank end,serverId:{}", serverId);
	}
	
	/**
	 * 临时总统可以设置总统
	 */
	@SuppressWarnings("deprecation")
	private String setPresident(Map<String, String> params) {
		String playerId = params.get("playerId");
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if(player==null){
			return HawkScript.failedResponse(HawkScript.SCRIPT_ERROR, "找不到玩家");
		}
		
		String tarplayerId = params.get("tarplayerId");
		Player targetPlayer = GlobalData.getInstance().makesurePlayer(tarplayerId);
		if(targetPlayer==null){
			return HawkScript.failedResponse(HawkScript.SCRIPT_ERROR, "找不到目标玩家");
		}
		
		// 被转让对象一定要有工会
		if (HawkOSOperator.isEmptyString(targetPlayer.getGuildId())) {
			return HawkScript.failedResponse(HawkScript.SCRIPT_ERROR, "转让总统对象没有工会");
		}
		// 已经设置了
		OfficerEntity officerEntity = PresidentOfficier.getInstance().getEntityById(OfficerType.OFFICER_01_VALUE);
		OfficerInfoSync.Builder builder = OfficerInfoSync.newBuilder();
		OfficerEntity oldOfficerEntity = PresidentOfficier.getInstance().getEntityByPlayerId(targetPlayer.getId());
		if (oldOfficerEntity != null) {
			SystemMailService.getInstance().sendMail(MailParames.newBuilder()
					.setPlayerId(oldOfficerEntity.getPlayerId())
					.setMailId(MailId.PRESIDENT_REPEAL_APPOINT)
					.addSubTitles(oldOfficerEntity.getOfficerId())
					.addContents(oldOfficerEntity.getOfficerId())
					.build());
			OfficerInfo.Builder oldInfo = OfficerInfo.newBuilder();
			oldInfo.setOfficerId(oldOfficerEntity.getOfficerId());
			oldInfo.setEndTime(oldOfficerEntity.getEndTime());
			builder.addOfficers(oldInfo);
			PresidentOfficier.getInstance().unsetOfficer(targetPlayer.getId(), oldOfficerEntity.getOfficerId());
		}
		long endTime = PresidentFightService.getInstance().getPresidentCity().getEndTime();
		builder.setAppointEndTime(endTime + PresidentConstCfg.getInstance().getAppointTime());
		PresidentOfficier.getInstance().setOfficer(targetPlayer.getId(), OfficerType.OFFICER_01_VALUE);
		PresidentRecord.getInstance().onPresidentChanged(PresidentFightService.getInstance().getPresidentCity().getPresident().getLastPresidentPlayerId(),
				targetPlayer.getId(), targetPlayer.getGuildId());
		OfficerInfo.Builder curInfo = OfficerInfo.newBuilder();
		curInfo.setOfficerId(officerEntity.getOfficerId());
		curInfo.setEndTime(officerEntity.getEndTime());
		curInfo.setPlayerMsg(BuilderUtil.genMiniPlayer(targetPlayer));
		builder.addOfficers(curInfo);
//		sendProtocol(HawkProtocol.valueOf(HP.code.OFFICER_INFO_SYNC_S_VALUE, builder));
		PresidentFightService.getInstance().getPresidentCity().chanagePresident(targetPlayer);
		PresidentFightService.getInstance().getPresidentCity().broadcastPresidentInfo(null);
//		logger.info("SetPresident operatorId:{},operatedId:{}", player.getId(), targetPlayer.getId());
		SystemMailService.getInstance().sendMail(MailParames.newBuilder()
				.setPlayerId(targetPlayer.getId())
				.setMailId(MailId.PRESIDENT_OFFICAL_APPOINT_APPOINT)
				.build());
		ChatService.getInstance().addWorldBroadcastMsg(Const.ChatType.SPECIAL_BROADCAST, Const.NoticeCfgId.PRESIDENT_APPOINT_NOTICE, null, targetPlayer.getGuildTag(),
				targetPlayer.getName());

		int period = PresidentFightService.getInstance().getPresidentCity().getTurnCount();
		OfficerRecord.Builder info = OfficerRecord.newBuilder();
		info.setTime(HawkTime.getMillisecond());
		info.setPlayerNameSet(targetPlayer.getName());
		if (oldOfficerEntity != null) {
			info.setOriOfficerId(oldOfficerEntity.getOfficerId());
		} else {
			info.setOriOfficerId(OfficerType.OFFICER_00_VALUE);
		}
		info.setCurOfficerId(OfficerType.OFFICER_01_VALUE);
		info.setPlayerNameUnset(player.getName());
		LocalRedis.getInstance().addOfficerRecord(period, info);
		player.responseSuccess(HP.code.OFFICER_SET_C_VALUE);
		
		return HawkScript.successResponse("op service success");
	}
	
	private void fixToucai(Map<String, String> params) {
		String playerId = params.get("playerId");
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if(player==null){
			return;
		}
		player.getData().getMedalEntity().setExp(7200);
		player.getData().getMedalEntity().getFactoryObj().notifyChange();
		player.getData().getMedalEntity().getFactoryObj().sync();
	}
	
	private void xiufu1104(){
		List<String> playerIdList = new ArrayList<>();
		try {
			// openid server platId
			HawkOSOperator.readTextFileLines("tmp/fix1104.txt", playerIdList);
		} catch (Exception e) {
			HawkException.catchException(e);
			return;
		}
		
		for(String playerId: playerIdList){
			fixPlayer1104(playerId);
		}
	}

	private void fixPlayer1104(String playerId) {
		try {
			Player player = GlobalData.getInstance().makesurePlayer(playerId);
			if(Objects.isNull(player)){
				DungeonRedisLog.log("xiufu1104", "playerId is null  {}", playerId);
				return;
			}
			int heroId = 1104;
			PlayerHero hero = player.getHeroByCfgId(heroId).orElse(null);
			if(Objects.isNull(hero)){
				DungeonRedisLog.log("xiufu1104", "playerId : {} can not find hero 1104", playerId);
				return;
			}
			AwardItems awardItem =  AwardItems.valueOf();
			final int exp = hero.getHeroEntity().getExp();
			final int EXP30000 = 1510005; // 30000 经验道具
			int backcount = (int) Math.ceil(exp / 30000D);
			awardItem.addItem(ItemType.TOOL_VALUE, EXP30000, backcount);
			hero.getHeroEntity().setExp(0);
			
			HeroStarLevelCfg starLevelCfg = HawkConfigManager.getInstance().getCombineConfig(HeroStarLevelCfg.class, heroId, hero.getStar(), hero.getStep());
			ConfigIterator<HeroStarLevelCfg> slit = HawkConfigManager.getInstance().getConfigIterator(HeroStarLevelCfg.class);
			for (HeroStarLevelCfg st : slit) {
				if (st.getHeroId() != heroId) {
					continue;
				}
				if (st.getId() >= starLevelCfg.getId()) {
					continue;
				}
				ItemInfo item = new ItemInfo(st.getPiecesForNextLevel());
				item.setItemId(1000005);
				awardItem.addItem(item);
			}
			
			hero.getHeroEntity().setStar(1);
			hero.getHeroEntity().setStep(0);
			
			for(SkillSlot slot : hero.getSkillSlots()){
				onRemoveSkill(awardItem,hero, slot);
			}
			
			onRemoveSoul(awardItem, hero);
			
			hero.notifyChange();
			
			int pcnt = player.getData().getItemNumByItemId(1001104);
			if (pcnt > 0) {
				ConsumeItems consumeItems = ConsumeItems.valueOf();
				consumeItems.addItemConsume(1001104, pcnt);
				if (consumeItems.checkConsume(player, 0)) {
					consumeItems.consumeAndPush(player, Action.NULL);
					awardItem.addItem(ItemType.TOOL_VALUE, 1000005, pcnt);
				}
			}
			
			
			if (!awardItem.getAwardItems().isEmpty()) {
				SystemMailService.getInstance().sendMail(MailParames.newBuilder()
						.setPlayerId(player.getId())
						.setMailId(MailId.REWARD_MAIL)
						.setRewards(awardItem.getAwardItems())
						.setAwardStatus(MailRewardStatus.NOT_GET)
						.addContents("尊敬的指挥官，因临时更新修复荣耀英雄尤利娅技能问题时出现了异常，后续虽修复了此异常问题，但是给您造成了不好的游戏体验，对此我们深表歉意。现根据您的需求针对您已获得的埃托莉亚英雄进行回退，您在此英雄所消耗的所有养成道具均会进行对应的返还。以下是返还的道具详情，请您查收。给您造成的不便非常抱歉，感谢您对游戏的支持！")
						.addSubTitles("埃托莉亚荣耀英雄回退邮件")
						.build());
				
				DungeonRedisLog.log("xiufu1104", "playerId : items  {}", ItemInfo.toString(awardItem.getAwardItems()));
			}
			
		} catch (Exception e) {
			DungeonRedisLog.log("xiufu1104", "playerId : {} exception: {}", playerId,e);
		}
	}
	
	private void onRemoveSkill(AwardItems awardItem, PlayerHero hero, SkillSlot skillslot) {
		IHeroSkill toRemoveSkill = skillslot.getSkill();
		if (toRemoveSkill == null) { // 并没有技能
			return;
		}
		Optional<ItemCfg> skillCfgOP = HawkConfigManager.getInstance().getConfigIterator(ItemCfg.class).stream()
				.filter(cfg -> cfg.getItemType() == Const.ToolType.HERO_SKILL_VALUE)
				.filter(cfg -> cfg.getSkillGet() == toRemoveSkill.skillID())
				.findAny();
		if (skillCfgOP.isPresent()) {
			awardItem.addItem(ItemType.TOOL_VALUE, skillCfgOP.get().getId(), 1);
		}
		final int EXP3000 = 13310005; // 3000 经验道具
		// 返还经验
		if (toRemoveSkill.getExp() > 0) {
			int backcount = (int) Math.ceil(toRemoveSkill.getExp() / 3000D);
			awardItem.addItem(ItemType.TOOL_VALUE, EXP3000, backcount);
		}

		skillslot.setSkill(null);
	}
	
	private void onRemoveSoul(AwardItems awardItem, PlayerHero hero) {
		int cfgId = hero.getSoul().soulLevelMaxCfgId();
		ConfigIterator<HeroSoulLevelCfg>  lvit = HawkConfigManager.getInstance().getConfigIterator(HeroSoulLevelCfg.class);
		for(HeroSoulLevelCfg lcfg : lvit){
			if(lcfg.getHero() == hero.getCfgId() && lcfg.getId() <= cfgId){
				ItemInfo item = ItemInfo.valueOf(lcfg.getConsumption());
				item.setItemId(1000005);
				awardItem.addItem(item);
			}
		}
		
		for(int stageId : hero.getSoul().getSoulStage()){
			HeroSoulStageCfg cfg = HawkConfigManager.getInstance().getConfigByKey(HeroSoulStageCfg.class, stageId);
			ItemInfo item = ItemInfo.valueOf(cfg.getConsumption());
			item.setItemId(1000005);
			awardItem.addItem(item);
		}
		
		hero.getSoul().reset();
	}

	private void fixPifu(Map<String, String> params) {
		String playerId = "7td-ik1gv-o";
		if(params.containsKey("playerId"));{
			playerId = params.get("playerId");
		}
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if(player==null){
			DungeonRedisLog.log("fixPifu", "playerId is null  {}", playerId);
			return;
		}
		player.getData().getMedalEntity().setExp(7200);
		player.getData().getMedalEntity().getFactoryObj().notifyChange();
//		player.getData().getMedalEntity().getFactoryObj().sync();
		
		// 皮肤
		DressEntity dressEntity = player.getData().getDressEntity();
		dressEntity.getDressInfo().clear();
		String dressInfo = "1_1_1541168111276_3153600000000_0_0|2_1_1541168111277_3153600000000_1_4694768111277|3_1_1548289535252_3153600000000_0_0|4_1_1548720247577_3153600000000_0_0|5_1_1548720247578_3153600000000_0_0|5_3_1555585116456_3153600000000_0_0|1_29_1558456248205_3153600000000_0_0|5_4_1558847381870_3153600000000_0_0|5_5_1563668048943_3153600000000_0_0|3_2_1563727546705_3153600000000_0_0|2_10_1569148977506_3153600000000_0_0|6_1_1571097525970_3153600000000_0_0|6_2_1572888770931_3153600000000_0_0|5_6_1574700563198_3153600000000_0_0|5_7_1582598942710_3153600000000_0_0|1_43_1584843568007_3153600000000_0_0|2_13_1587779127649_3153600000000_13_4752827212710|2_23_1594626955632_3153600000000_23_4758452644265|5_9_1594656617670_3153600000000_0_0|2_25_1595420519689_152064000000_25_1733749332344|1_36_1602175217174_3153600000000_0_0|1_40_1602175231697_3153600000000_0_0|5_10_1603685658779_3153600000000_0_0|3_3_1610936238284_3153600000000_3_4764536238284|2_26_1613147005925_3153600000000_26_4766747005925|1_44_1618959440701_3153600000000_0_0|6_5_1619021922807_3153600000000_0_0|2_27_1619369058049_3153600000000_2_4786049534123|1_57_1624702082377_3153600000000_0_0|1_56_1626880980286_3153600000000_0_0|2_24_1629563963659_3153600000000_24_4783163963659|1_23_1631464422309_3153600000000_0_0|2_2_1632449534123_3153600000000_2_4786049534123|3_5_1634195512085_3153600000000_5_4787795512085|1_34_1634252918263_3153600000000_0_0|6_3_1634489199426_3153600000000_0_0|4_10_1635729039680_3153600000000_0_0|1_63_1635997897905_3153600000000_0_0|1_46_1639923173755_3153600000000_0_0|1_67_1640536587673_3153600000000_0_0|4_13_1640692210535_3153600000000_0_0|7_1_1641429139643_3153600000000_0_0|1_48_1641996640629_3153600000000_0_0|5_12_1643214443355_3153600000000_12_4796814443355|1_72_1643700651074_3153600000000_0_0|2_30_1644382414340_3153600000000_13_4741379127649|1_39_1647278009557_3153600000000_0_0|6_7_1651150291391_3153600000000_0_0|4_18_1651452152097_3153600000000_0_0|1_47_1652681548057_3153600000000_0_0|4_19_1654081345663_3153600000000_0_0|5_8_1655052616862_3153600000000_0_0|1_32_1656025416378_3153600000000_0_0|4_20_1656624844718_3153600000000_0_0|1_30_1658455112832_3153600000000_0_0|2_39_1658678607120_3153600000000_10_4722748977506|1_80_1659135597659_3153600000000_0_0|4_21_1659410976089_3153600000000_0_0|4_22_1661304633251_3153600000000_0_0|4_23_1662913507845_3153600000000_0_0|1_73_1663604316887_3153600000000_0_0|2_35_1664036702247_3153600000000_35_4817636702247|1_74_1665570467887_3153600000000_0_0|4_24_1667919237575_3153600000000_0_0|7_17_1667919278692_3153600000000_0_0|1_38_1669332800169_3153600000000_0_0|6_8_1671295797065_3153600000000_0_0|4_26_1672249439642_3153600000000_0_0|4_28_1672249442201_3153600000000_0_0|1_58_1674523602275_3153600000000_0_0|4_29_1674523606911_3153600000000_0_0|2_44_1674578250531_3153600000000_52_4828697870028|2_52_1675097870028_3153600000000_52_4828697870028|3_20_1675918859665_3153600000000_20_4836009882936|4_17_1676566380512_3153600000000_0_0|4_30_1677206764091_3153600000000_0_0|2_70_1679365526412_3153600000000_96_4842213438049|4_31_1679579787095_3153600000000_0_0|1_41_1680074801957_3153600000000_0_0|4_33_1680799022476_3153600000000_0_0|3_25_1681229539576_3153600000000_25_4834829539576|1_33_1682899265201_3153600000000_0_0|4_35_1682899271882_3153600000000_0_0|5_15_1683771997702_3153600000000_1_4702320247578|2_75_1684228822769_3153600000000_75_4839843327699|1_75_1684656958733_3153600000000_0_0|2_72_1684686490170_3153600000000_96_4842213438049|2_48_1685327352202_3153600000000_48_4839538381467|4_36_1685749484530_3153600000000_0_0|1_31_1686503848726_3153600000000_0_0|4_38_1687452981165_3153600000000_0_0|5_17_1688476262164_3153600000000_0_0|2_96_1688613438049_3153600000000_96_4842213438049|2_74_1688921487454_3153600000000_78_4854754784755|4_39_1689970102241_3153600000000_0_0|1_25_1691198723474_3153600000000_0_0|8_1_1692230326732_3153600000000_0_0|3_30_1692593563689_3153600000000_30_4846796790033|4_40_1692694420192_3153600000000_0_0|8_2_1692694441495_3153600000000_0_0|1_100_1692923886951_3153600000000_0_0|5_16_1693088918411_3153600000000_1_4702320247578|2_73_1693196874812_3153600000000_73_4846796874812|1_68_1693196953734_3153600000000_0_0|1_60_1693255873836_3153600000000_0_0|1_24_1695789732260_3153600000000_0_0|4_42_1696113540354_3153600000000_0_0|2_45_1697189387843_3153600000000_45_4850789387843|1_103_1697475507208_3153600000000_0_0|4_43_1697604896316_3153600000000_0_0|7_75_1697604931805_3153600000000_0_0|6_13_1699832794573_3153600000000_0_0|4_46_1700870565246_3153600000000_0_0|2_78_1701154784755_3153600000000_78_4854754784755|6_16_1702174270420_3153600000000_0_0|4_48_1703486723473_3153600000000_0_0|1_108_1703721162111_3153600000000_0_0|2_79_1704039279412_3153600000000_10_4722748977506|5_19_1704547656468_3153600000000_0_0|2_103_1704723234574_3153600000000_103_4858323234574|1_104_1705329100530_3153600000000_0_0|4_44_1705329103769_3153600000000_0_0|4_50_1705366571081_3153600000000_0_0|6_17_1706668032074_3153600000000_0_0|1_111_1707124797781_3153600000000_0_0|4_52_1707124800551_3153600000000_0_0|1_110_1707411830015_3153600000000_0_0|4_51_1707542213082_3153600000000_0_0|7_127_1707660351254_3153600000000_0_0|2_80_1708998142077_3153600000000_80_4862598142077|2_82_1709916230424_3153600000000_82_4863516230424|4_53_1710175603143_3153600000000_0_0|5_23_1711342900274_3153600000000_0_0|2_109_1711582946210_3153600000000_109_4865182946210|2_55_1713112664891_3153600000000_55_4866712664891|4_54_1713283896721_3153600000000_0_0|4_55_1714326596662_3153600000000_0_0|2_76_1716136510337_3153600000000_76_4869736510337|6_28_1716569388959_3153600000000_0_0|5_24_1716741632674_3153600000000_0_0|4_56_1718165490133_3153600000000_0_0|1_21_1718608734067_3153600000000_0_0|1_65_1718608829083_3153600000000_0_0|4_15_1718609052178_3153600000000_0_0|4_14_1718609057874_3153600000000_0_0|4_16_1718609067453_3153600000000_0_0|4_2_1718609078572_3153600000000_0_0|5_25_1720151428983_3153600000000_0_0|2_87_1720657258557_3153600000000_1_4694768111277|3_32_1720657415210_3153600000000_32_4874257415210|4_59_1721189530856_3153600000000_0_0|2_71_1721921060492_23328000000_71_1741525405009|4_58_1723076563868_3153600000000_0_0|4_61_1723608532546_3153600000000_0_0|3_40_1723850978156_3153600000000_40_4880051291722|5_29_1723902164830_3153600000000_0_0|2_121_1724072451472_3153600000000_121_4877672451472|1_117_1724399722569_3153600000000_0_0|5_26_1724895973132_3153600000000_1_4702320247578|2_98_1725500092661_3153600000000_98_4879100092661|2_65_1725902761995_3153600000000_65_4879502761995|4_62_1726590330060_3153600000000_0_0|6_31_1727800949459_3153600000000_0_0|5_27_1728318246288_3153600000000_1_4702320247578|1_122_1729137010276_3153600000000_0_0|4_63_1729285470629_3153600000000_0_0|7_189_1729286045289_3153600000000_0_0|2_101_1729589001784_3153600000000_101_4883189001784|3_35_1729589082892_3153600000000_35_4883189082892|5_30_1729819157551_3153600000000_0_0|1_78_1730331023405_3153600000000_0_0|1_79_1730331321630_3153600000000_0_0|1_118_1730331325663_3153600000000_0_0|1_115_1730331328101_3153600000000_0_0|2_66_1730855574399_3153600000000_66_4884455574399|4_64_1733149148955_3153600000000_0_0";
		List<DressItem> list = SerializeHelper.stringToList(DressItem.class, dressInfo);
		for (DressItem item : list) {
			dressEntity.addDressInfo(item);
		}
		dressEntity.notifyUpdate();
//		player.getPush().syncDressInfo();
//		player.getPush().syncDressSendProtectInfo();
//		// 推送装扮信使礼包每周赠送次数
//		player.getPush().syncSendDressGiftInfo();
//		//可变外显参数同步
//		player.getPush().syncPlayerDressEditData();
		
		// 军工
		NationMilitaryEntity nationMilitaryEntity = player.getData().getNationMilitaryEntity();
		nationMilitaryEntity.setNationMilitarLlevel(502);
		nationMilitaryEntity.setNationMilitaryExp(400000);
		
		// 泰能科技
		PlantSoldierSchoolEntity school = player.getData().getPlantSoldierSchoolEntity();
		school.setInstrumentSerialized("{\"unlock\":true,\"chips\":[\"110010\",\"120010\",\"130010\"],\"cfgId\":10201000}");
		school.setCracksSerialized("[\"{\\\"unlock\\\":true,\\\"chips\\\":[\\\"241000\\\",\\\"242000\\\",\\\"243000\\\",\\\"244000\\\"],\\\"cfgId\\\":240000}\",\"{\\\"unlock\\\":true,\\\"chips\\\":[\\\"211000\\\",\\\"212000\\\",\\\"213000\\\",\\\"214000\\\"],\\\"cfgId\\\":210000}\",\"{\\\"unlock\\\":true,\\\"chips\\\":[\\\"251000\\\",\\\"252000\\\",\\\"253000\\\",\\\"254000\\\"],\\\"cfgId\\\":250000}\",\"{\\\"unlock\\\":true,\\\"chips\\\":[\\\"221010\\\",\\\"222010\\\",\\\"223010\\\",\\\"224010\\\"],\\\"cfgId\\\":220001}\",\"{\\\"unlock\\\":true,\\\"chips\\\":[\\\"261000\\\",\\\"262000\\\",\\\"263000\\\",\\\"264000\\\"],\\\"cfgId\\\":260000}\",\"{\\\"unlock\\\":true,\\\"chips\\\":[\\\"271000\\\",\\\"272000\\\",\\\"273000\\\",\\\"274000\\\"],\\\"cfgId\\\":270000}\",\"{\\\"unlock\\\":true,\\\"chips\\\":[\\\"281000\\\",\\\"282000\\\",\\\"283000\\\",\\\"284000\\\"],\\\"cfgId\\\":280000}\",\"{\\\"unlock\\\":true,\\\"chips\\\":[\\\"231010\\\",\\\"232010\\\",\\\"233010\\\",\\\"234010\\\"],\\\"cfgId\\\":230001}\"]");
		school.setCrystalSerialized("{\"unlock\":true,\"chips\":[\"310010\",\"320010\",\"330010\"],\"cfgId\":30201000}");
		school.setStrengthenSerialized("[\"{\\\"unlock\\\":false,\\\"chips\\\":[],\\\"level\\\":0,\\\"cfgId\\\":100207}\",\"{\\\"unlock\\\":true,\\\"chips\\\":[\\\"421101\\\",\\\"421301\\\",\\\"421201\\\",\\\"421401\\\",\\\"421501\\\",\\\"422101\\\",\\\"422201\\\",\\\"422301\\\",\\\"422401\\\",\\\"422501\\\",\\\"423101\\\",\\\"423201\\\",\\\"423301\\\",\\\"423401\\\",\\\"423501\\\",\\\"424101\\\",\\\"424201\\\",\\\"424301\\\",\\\"424401\\\",\\\"424501\\\",\\\"425501\\\"],\\\"level\\\":5,\\\"cfgId\\\":100107}\",\"{\\\"unlock\\\":true,\\\"chips\\\":[\\\"431101\\\",\\\"431301\\\",\\\"431201\\\",\\\"431401\\\",\\\"431501\\\",\\\"432101\\\",\\\"432201\\\",\\\"432301\\\",\\\"432401\\\",\\\"432501\\\",\\\"433101\\\",\\\"433301\\\",\\\"433201\\\",\\\"433401\\\",\\\"433501\\\",\\\"434101\\\",\\\"434201\\\",\\\"434301\\\",\\\"434401\\\",\\\"434501\\\",\\\"435501\\\"],\\\"level\\\":5,\\\"cfgId\\\":100407}\",\"{\\\"unlock\\\":false,\\\"chips\\\":[],\\\"level\\\":0,\\\"cfgId\\\":100307}\",\"{\\\"unlock\\\":false,\\\"chips\\\":[],\\\"level\\\":0,\\\"cfgId\\\":100607}\",\"{\\\"unlock\\\":false,\\\"chips\\\":[],\\\"level\\\":0,\\\"cfgId\\\":100507}\",\"{\\\"unlock\\\":false,\\\"chips\\\":[],\\\"level\\\":0,\\\"cfgId\\\":100807}\",\"{\\\"unlock\\\":false,\\\"chips\\\":[],\\\"level\\\":0,\\\"cfgId\\\":100707}\"]");
		school.setMilitarySerialized("[\"{\\\"soldierType\\\":1,\\\"unlock\\\":false,\\\"chips\\\":[\\\"511000\\\",\\\"512000\\\",\\\"513000\\\",\\\"514000\\\",\\\"515000\\\"]}\",\"{\\\"soldierType\\\":2,\\\"unlock\\\":true,\\\"chips\\\":[\\\"521040\\\",\\\"522040\\\",\\\"523040\\\",\\\"524040\\\",\\\"525000\\\"]}\",\"{\\\"soldierType\\\":3,\\\"unlock\\\":true,\\\"chips\\\":[\\\"531040\\\",\\\"532040\\\",\\\"533040\\\",\\\"534040\\\",\\\"535040\\\"]}\",\"{\\\"soldierType\\\":4,\\\"unlock\\\":false,\\\"chips\\\":[\\\"541000\\\",\\\"542000\\\",\\\"543000\\\",\\\"544000\\\",\\\"545000\\\"]}\",\"{\\\"soldierType\\\":5,\\\"unlock\\\":false,\\\"chips\\\":[\\\"551000\\\",\\\"552000\\\",\\\"553000\\\",\\\"554000\\\",\\\"555000\\\"]}\",\"{\\\"soldierType\\\":6,\\\"unlock\\\":false,\\\"chips\\\":[\\\"561000\\\",\\\"562000\\\",\\\"563000\\\",\\\"564000\\\",\\\"565000\\\"]}\",\"{\\\"soldierType\\\":7,\\\"unlock\\\":false,\\\"chips\\\":[\\\"571000\\\",\\\"572000\\\",\\\"573000\\\",\\\"574000\\\",\\\"575000\\\"]}\",\"{\\\"soldierType\\\":8,\\\"unlock\\\":false,\\\"chips\\\":[\\\"581000\\\",\\\"582000\\\",\\\"583000\\\",\\\"584000\\\",\\\"585000\\\"]}\"]");
		school.afterRead();
		player.getPlantSoldierSchool().notifyChange(null);
		// 英雄档案
		HeroArchivesEntity archi = player.getData().getHeroArchivesEntity();
		archi.setArchives("1058:5|1062:4|1002:5|1066:3|1003:5|1037:5|1041:5|1042:5|1043:5|1044:5|1077:5|1018:4|1051:5|1020:5|1021:5|1053:5|1054:5|1055:5");
		archi.afterRead();
		
		//星能探索
		CommanderEntity commander = player.getData().getCommanderEntity();
		String starExplore = "[{\"starId\":1,\"progress\":[{\"progressVal\":50,\"protressId\":1},{\"progressVal\":25,\"protressId\":2},{\"progressVal\":25,\"protressId\":3}]},{\"starId\":2,\"progress\":[{\"progressVal\":19,\"protressId\":1},{\"progressVal\":18,\"protressId\":2},{\"progressVal\":13,\"protressId\":3}]},{\"starId\":3,\"progress\":[{\"progressVal\":17,\"protressId\":1},{\"progressVal\":12,\"protressId\":2},{\"progressVal\":22,\"protressId\":3}]},{\"starId\":4,\"progress\":[{\"progressVal\":50,\"protressId\":1},{\"progressVal\":25,\"protressId\":2},{\"progressVal\":25,\"protressId\":3}]},{\"starId\":5,\"progress\":[{\"progressVal\":25,\"protressId\":1},{\"progressVal\":50,\"protressId\":2},{\"progressVal\":25,\"protressId\":3}]},{\"starId\":6,\"progress\":[{\"progressVal\":13,\"protressId\":1},{\"progressVal\":16,\"protressId\":2},{\"progressVal\":21,\"protressId\":3}]},{\"starId\":7,\"progress\":[{\"progressVal\":50,\"protressId\":1},{\"progressVal\":25,\"protressId\":2},{\"progressVal\":25,\"protressId\":3}]}]";
		String starExploreCollect = "[{\"fixAttr\":[{\"attrId\":11028,\"attrVal\":1002}],\"collectId\":1,\"upCount\":2,\"randomAttr\":[{\"attrId\":11031,\"attrVal\":209}]},{\"fixAttr\":[{\"attrId\":11029,\"attrVal\":1000}],\"collectId\":2,\"upCount\":0,\"randomAttr\":[{\"attrId\":11032,\"attrVal\":100}]}]";
		ArmourStarExplores starExplores = ArmourStarExplores.unSerialize(commander, starExplore, starExploreCollect);
		commander.setStarExplores(starExplores);
		commander.beforeWrite();
		commander.notifyUpdate();
				
		player.kickout(Status.Error.MIGRATE_FINISH_VALUE, true, "");
		DungeonRedisLog.log("fixPifu", "fix success  {}", playerId);
	}
	
	// 20293服
	private void fixXingneng(Map<String, String> params) {
		String playerId = "fjj-pq852-7";
		if(params.containsKey("playerId"));{
			playerId = params.get("playerId");
		}
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if(player==null){
			DungeonRedisLog.log("fixXingneng", "playerId is null  {}", playerId);
			return;
		}
		
		//星能探索
		CommanderEntity commander = player.getData().getCommanderEntity();
		String starExplore = "[{\"starId\":1,\"progress\":[{\"progressVal\":30,\"protressId\":1},{\"progressVal\":25,\"protressId\":2},{\"progressVal\":25,\"protressId\":3}]},{\"starId\":2,\"progress\":[{\"progressVal\":16,\"protressId\":1},{\"progressVal\":18,\"protressId\":2},{\"progressVal\":16,\"protressId\":3}]},{\"starId\":3,\"progress\":[{\"progressVal\":25,\"protressId\":1},{\"progressVal\":25,\"protressId\":2},{\"progressVal\":30,\"protressId\":3}]},{\"starId\":4,\"progress\":[{\"progressVal\":50,\"protressId\":1},{\"progressVal\":25,\"protressId\":2},{\"progressVal\":25,\"protressId\":3}]},{\"starId\":5,\"progress\":[{\"progressVal\":25,\"protressId\":1},{\"progressVal\":50,\"protressId\":2},{\"progressVal\":25,\"protressId\":3}]},{\"starId\":6,\"progress\":[{\"progressVal\":16,\"protressId\":1},{\"progressVal\":22,\"protressId\":2},{\"progressVal\":12,\"protressId\":3}]},{\"starId\":7,\"progress\":[{\"progressVal\":16,\"protressId\":1},{\"progressVal\":15,\"protressId\":2},{\"progressVal\":19,\"protressId\":3}]}]";
		String starExploreCollect = "[{\"fixAttr\":[{\"attrId\":11028,\"attrVal\":1033}],\"collectId\":1,\"upCount\":33,\"randomAttr\":[{\"attrId\":11031,\"attrVal\":500}]}]";
		ArmourStarExplores starExplores = ArmourStarExplores.unSerialize(commander, starExplore, starExploreCollect);
		commander.setStarExplores(starExplores);
		commander.beforeWrite();
		commander.notifyUpdate();
		player.kickout(Status.Error.MIGRATE_FINISH_VALUE, true, "");
		DungeonRedisLog.log("fixXingneng", "fix success  {}", playerId);
	}
	
	// 20293服
	private void fixPifu1216(Map<String, String> params) {
		String playerId = "fjj-pq852-7";
		if(params.containsKey("playerId")){
			playerId = params.get("playerId");
		}
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if(player==null){
			DungeonRedisLog.log("fixPifu", "playerId is null  {}", playerId);
			return;
		}
		
		// 皮肤
		DressEntity dressEntity = player.getData().getDressEntity();
		dressEntity.getDressInfo().clear();
		String dressInfo = "1_1_1553214006302_3153600000000_0_0|2_1_1553214006303_3153600000000_1_4706814006303|3_1_1553214006306_3153600000000_0_0|4_1_1553214006307_3153600000000_0_0|5_1_1553214006309_3153600000000_0_0|1_24_1557997711476_3153600000000_0_0|1_29_1558628771383_3153600000000_0_0|5_3_1561738833894_3153600000000_0_0|2_10_1563234075166_3161980800000_10_4725214875166|3_2_1563540879563_3153600000000_2_4717140879563|1_32_1563899034240_3153600000000_0_0|5_4_1565034417950_3153600000000_0_0|1_33_1565128213280_3153600000000_0_0|6_1_1571097363752_3153600000000_0_0|1_36_1571368698947_3153600000000_0_0|5_5_1571588674278_3153600000000_0_0|4_2_1572971698565_3153600000000_0_0|1_23_1573011546310_3153600000000_0_0|1_21_1573011548991_3153600000000_0_0|1_25_1573011593460_3153600000000_0_0|1_31_1573011595102_3153600000000_0_0|6_2_1573085213472_3153686400000_0_0|1_38_1574310645124_3153600000000_0_0|2_13_1578503268735_3153600000000_13_4732103268735|5_6_1579567835068_3153600000000_0_0|1_41_1581672885866_3153600000000_0_0|1_42_1584634210826_3153600000000_0_0|5_7_1587830910684_3153600000000_0_0|1_46_1589732746841_3153600000000_0_0|2_23_1593272470683_3153600000000_23_4746872470683|2_25_1593486864337_190857600000_25_1673729728392|5_9_1598026516318_3153600000000_0_0|1_56_1598404145136_3153600000000_0_0|1_44_1599927557949_3153600000000_0_0|1_34_1599927568890_3153600000000_0_0|1_40_1602173173083_3153600000000_0_0|1_43_1602173176529_3153600000000_0_0|1_57_1602940249886_3153600000000_0_0|5_10_1607560547455_3153600000000_0_0|3_3_1610938276505_3153600000000_3_4764538276505|2_26_1615862708467_3153600000000_26_4769462708467|1_47_1623423251712_3153600000000_0_0|3_5_1631102385868_3153600000000_5_4784702385868|2_2_1632449836697_3153600000000_2_4786049836697|2_48_1634522006701_3153600000000_48_4788122006701|4_10_1635684768194_3153600000000_0_0|1_63_1635960701153_3153600000000_0_0|4_11_1638443164094_3153600000000_0_0|6_7_1638450210241_3153600000000_0_0|1_39_1640061092751_3153600000000_0_0|1_48_1640061094586_3153600000000_0_0|1_67_1640619159630_3153600000000_0_0|4_13_1640619750257_3153600000000_0_0|7_1_1641427982732_3153600000000_0_0|5_12_1642142217905_3153600000000_1_4706814006309|1_72_1643864252308_3153600000000_0_0|1_73_1644925413002_3153600000000_0_0|4_15_1644925414600_3153600000000_0_0|1_65_1647143077817_3153600000000_0_0|1_71_1648004868124_3153600000000_0_0|4_17_1650212655040_3153600000000_0_0|1_75_1650212661323_3153600000000_0_0|2_30_1650808085259_3153600000000_35_4834722248950|4_18_1651880352004_3153600000000_0_0|4_19_1654291452557_3153600000000_0_0|2_9_1657984071226_3153600000000_98_4879095882850|2_64_1658192816833_3153600000000_26_4769462708467|1_80_1659676197832_3153600000000_0_0|4_21_1660058715291_3153600000000_0_0|1_60_1662000783058_3153600000000_0_0|4_23_1663171865995_3153600000000_0_0|4_24_1666521185877_3153600000000_0_0|6_9_1670643540356_3153600000000_0_0|2_44_1670775076978_3153600000000_44_4824375076978|4_26_1671442666273_3153600000000_0_0|2_75_1677381285617_3153600000000_75_4830981285617|4_14_1677408648827_3153600000000_0_0|1_74_1677408732602_3153600000000_0_0|4_31_1679158202859_3153600000000_0_0|4_16_1679193231751_3153600000000_0_0|4_32_1680129130326_3153600000000_0_0|1_91_1680129131968_3153600000000_0_0|4_33_1680527499966_3153600000000_0_0|2_35_1681122248950_3153600000000_35_4834722248950|2_70_1681413001040_3153600000000_73_4851720826331|4_35_1683086388617_3153600000000_0_0|5_15_1684628326265_3153600000000_1_4706814006309|4_36_1685424208682_3153600000000_0_0|2_68_1685712215278_3153600000000_68_4839312215278|1_58_1685900678288_3153600000000_0_0|6_15_1685939869338_3153600000000_0_0|4_38_1687476736931_3153600000000_0_0|5_17_1688481769042_3153600000000_0_0|2_96_1689005105614_3153600000000_96_4842605105614|3_20_1689505908784_3153600000000_20_4843105908784|8_1_1692233685358_3153600000000_0_0|8_2_1692233894242_3153600000000_0_0|4_40_1692701394115_3153600000000_0_0|3_25_1692701669789_3153600000000_25_4846301669789|1_30_1692727278806_3153600000000_0_0|1_100_1692894100682_3153600000000_0_0|3_30_1694738862731_3153600000000_30_4851125565486|4_42_1696011098211_3153600000000_0_0|1_103_1697563130278_3153600000000_0_0|4_43_1697564113399_3153600000000_0_0|7_75_1697564121101_3153600000000_0_0|2_54_1697991952404_3153600000000_54_4851591952404|2_73_1698120826331_3153600000000_73_4851720826331|6_10_1700066221232_3153600000000_0_0|4_46_1700872470426_3153600000000_0_0|6_16_1702569898987_3153600000000_0_0|4_48_1703521119286_3153600000000_0_0|5_19_1704725463969_3153600000000_0_0|1_109_1705248219258_3153600000000_0_0|4_50_1705495464358_3153600000000_0_0|5_16_1705853833108_3153600000000_16_4859453833108|2_103_1706538565653_3153600000000_103_4860138565653|1_110_1707410180599_3153600000000_0_0|1_104_1707547347893_3153600000000_0_0|4_44_1707547350610_3153600000000_0_0|4_51_1707586966319_3153600000000_0_0|7_127_1707661003037_3153600000000_0_0|2_80_1708595506068_3153600000000_80_4862195506068|2_33_1708964011739_3153600000000_33_4862564011739|4_53_1710259460971_3153600000000_0_0|2_82_1710420833135_3153600000000_82_4864020833135|4_54_1713372391502_3153600000000_0_0|4_55_1714387867586_3153600000000_0_0|6_28_1716437345631_3153600000000_0_0|6_13_1716547672115_3153600000000_0_0|5_23_1718294600015_3153600000000_1_4706814006309|4_56_1718296514695_3153600000000_0_0|2_109_1719377841934_3153600000000_109_4872977841934|3_40_1719419445823_15897600000_40_1726369009127|2_57_1721188558399_3153600000000_57_4874788558399|4_59_1721214088430_3153600000000_0_0|5_29_1723076643856_3153600000000_0_0|4_58_1723076937971_3153600000000_0_0|1_117_1723076942410_3153600000000_0_0|2_121_1723468006136_3153600000000_121_4877068006136|4_61_1723630612130_3153600000000_0_0|5_24_1724038271872_3153600000000_1_4706814006309|2_98_1725495882850_3153600000000_98_4879095882850|5_25_1726416614619_3153600000000_1_4706814006309|4_62_1726592880893_3153600000000_0_0|6_33_1727873683391_3153600000000_0_0|2_101_1729047693390_3153600000000_33_4862564011739|1_122_1729096097458_3153600000000_0_0|4_63_1729270177888_3153600000000_0_0|7_189_1729270356453_3153600000000_0_0|5_26_1729828855842_3153600000000_1_4706814006309|1_89_1729853439809_3153600000000_0_0|5_30_1729919087064_3153600000000_0_0|1_102_1730119029593_3153600000000_0_0|2_66_1730791577781_3153600000000_66_4884391577781|2_21_1731581506498_3153600000000_21_4885181506498|4_201_1732486687246_3153600000000_0_0|5_27_1733030745790_3153600000000_27_4886630745790|6_32_1733040808120_5184000000_0_0|2_74_1733093012779_2592000000_74_1735685012779|4_64_1733164211644_3153600000000_0_0";
		List<DressItem> list = SerializeHelper.stringToList(DressItem.class, dressInfo);
		for (DressItem item : list) {
			dressEntity.addDressInfo(item);
		}
		dressEntity.notifyUpdate();
		player.kickout(Status.Error.MIGRATE_FINISH_VALUE, true, "");
		DungeonRedisLog.log("fixPifu", "fix success  {}", playerId);
	}
	
	/**
	 * 转区皮肤全部没了，兵也给降级了，还有星能探索直接变0，尉兵也没了燕姐 辛苦看看是否异常 
	 * @param params
	 */
	private void fix2221215(Map<String, String> params) {
		String playerId = "81f-178yj6-7";
		if(params.containsKey("playerId")){
			playerId = params.get("playerId");
		}
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if(player==null){
			DungeonRedisLog.log("fixPifu", "playerId is null  {}", playerId);
			return;
		}
		player.getData().getMedalEntity().setExp(7200);
		player.getData().getMedalEntity().getFactoryObj().notifyChange();
//		player.getData().getMedalEntity().getFactoryObj().sync();
		
		// 皮肤
		DressEntity dressEntity = player.getData().getDressEntity();
		dressEntity.getDressInfo().clear();
		String dressInfo = "1_1_1582641490246_3153600000000_0_0|2_1_1582641490249_3153600000000_1_4736241490249|3_1_1582641490251_3153600000000_0_0|4_1_1582641490253_3153600000000_0_0|5_1_1582641490255_3153600000000_0_0|6_1_1582641490257_3153600000000_0_0|5_4_1592299946242_3153600000000_0_0|5_5_1594345245776_3153600000000_0_0|5_6_1601786486482_3153600000000_0_0|1_57_1603252685307_3153600000000_0_0|5_3_1604877263306_3153600000000_0_0|5_7_1609119309421_3153600000000_0_0|3_3_1610899988125_3153600000000_3_4764499988125|2_26_1615864294964_3153600000000_26_4769464294964|2_13_1616843029662_3153600000000_13_4770443029662|5_9_1620874643719_3153600000000_0_0|6_5_1629599790297_3153600000000_0_0|5_10_1631854364591_3153600000000_0_0|2_2_1632449930979_3153600000000_2_4786049930979|2_30_1635382464836_3153600000000_30_4788982464836|7_1_1641426963832_3153600000000_0_0|1_72_1644237535543_3153600000000_0_0|4_14_1644237537882_3153600000000_0_0|2_29_1647475472041_3153600000000_29_4801075472041|6_3_1650680366371_3153600000000_0_0|5_12_1653968379595_3153600000000_0_0|5_8_1659329211490_3153600000000_0_0|2_39_1660753008074_3153600000000_39_4814353008074|6_8_1670635467347_3153600000000_0_0|2_44_1672806115390_3153600000000_2_4786049930979|2_52_1673397226178_3153600000000_52_4826997226178|3_20_1677293248683_3153600000000_20_4830893248683|2_75_1679201002130_3153600000000_75_4839029165799|2_45_1684894428422_3153600000000_2_4786049930979|5_15_1685589911714_3153600000000_0_0|6_15_1685679185488_3153600000000_0_0|2_73_1687710399524_3153600000000_73_4841310399524|5_17_1688940330439_3153600000000_0_0|2_96_1688940342889_3153600000000_96_4842540342889|8_1_1692230108000_3153600000000_0_0|8_2_1692277778967_3153600000000_0_0|3_30_1694306458101_3153600000000_30_4847906458101|2_71_1696689166038_38880000000_71_1735231484348|4_43_1697645773486_3153600000000_0_0|1_103_1697848263652_3153600000000_0_0|7_75_1697848564922_3153600000000_0_0|2_50_1698193483540_3153600000000_50_4851793483540|6_11_1700083012699_3153600000000_0_0|4_46_1700872531312_3153600000000_0_0|5_16_1700934758109_3153600000000_1_4736241490255|2_79_1703117862259_3153600000000_79_4856717862259|4_48_1703519480324_3153600000000_0_0|5_19_1703951339384_3153600000000_0_0|2_103_1704119327422_3153600000000_73_4841310399524|4_50_1705396639967_3153600000000_0_0|2_55_1713139469839_3153600000000_55_4866739469839|4_54_1713289271381_3153600000000_0_0|4_55_1714341754699_3153600000000_0_0|6_28_1716518269716_3153600000000_0_0|2_109_1716519765979_3153600000000_109_4870119765979|6_16_1716519772052_3153600000000_0_0|2_83_1716519829568_3153600000000_66_1734258987023|5_23_1716743059199_3153600000000_1_4736241490255|4_56_1718191154456_3153600000000_0_0|2_56_1720757908553_3153600000000_56_4874357908553|4_59_1721350638222_3153600000000_0_0|2_121_1723472369869_3153600000000_121_4877072369869|5_29_1723472475441_3153600000000_0_0|2_25_1723472582703_15552000000_25_1731248584756|4_61_1723728082558_3153600000000_0_0|5_24_1723883018196_3153600000000_1_4736241490255|4_62_1726616089159_3153600000000_0_0|5_25_1727542211783_3153600000000_0_0|6_31_1727974046856_3153600000000_0_0|4_63_1729271003801_3153600000000_0_0|5_30_1730026973611_3153600000000_0_0|2_10_1730036718085_4233600000_10_1730641527686|5_26_1733155573128_3153600000000_0_0|4_64_1733195960288_3153600000000_0_0|2_66_1733654187023_604800000_66_1734258987023";
		List<DressItem> list = SerializeHelper.stringToList(DressItem.class, dressInfo);
		for (DressItem item : list) {
			dressEntity.addDressInfo(item);
		}
		dressEntity.notifyUpdate();
//		player.getPush().syncDressInfo();
//		player.getPush().syncDressSendProtectInfo();
//		// 推送装扮信使礼包每周赠送次数
//		player.getPush().syncSendDressGiftInfo();
//		//可变外显参数同步
//		player.getPush().syncPlayerDressEditData();
		
		// 军工
		NationMilitaryEntity nationMilitaryEntity = player.getData().getNationMilitaryEntity();
		nationMilitaryEntity.setNationMilitarLlevel(502);
		nationMilitaryEntity.setNationMilitaryExp(400000);
		
		// 泰能科技
		PlantSoldierSchoolEntity school = player.getData().getPlantSoldierSchoolEntity();
		school.setInstrumentSerialized("{\"unlock\":true,\"chips\":[\"110010\",\"120010\",\"130010\"],\"cfgId\":10201000}");
		school.setCracksSerialized("[\"{\\\"unlock\\\":true,\\\"chips\\\":[\\\"241000\\\",\\\"242000\\\",\\\"243000\\\",\\\"244000\\\"],\\\"cfgId\\\":240000}\",\"{\\\"unlock\\\":true,\\\"chips\\\":[\\\"211010\\\",\\\"212010\\\",\\\"213010\\\",\\\"214010\\\"],\\\"cfgId\\\":210001}\",\"{\\\"unlock\\\":true,\\\"chips\\\":[\\\"251000\\\",\\\"252000\\\",\\\"253000\\\",\\\"254000\\\"],\\\"cfgId\\\":250000}\",\"{\\\"unlock\\\":true,\\\"chips\\\":[\\\"221010\\\",\\\"222010\\\",\\\"223010\\\",\\\"224010\\\"],\\\"cfgId\\\":220001}\",\"{\\\"unlock\\\":true,\\\"chips\\\":[\\\"261000\\\",\\\"262000\\\",\\\"263000\\\",\\\"264000\\\"],\\\"cfgId\\\":260000}\",\"{\\\"unlock\\\":true,\\\"chips\\\":[\\\"271000\\\",\\\"272000\\\",\\\"273000\\\",\\\"274000\\\"],\\\"cfgId\\\":270000}\",\"{\\\"unlock\\\":true,\\\"chips\\\":[\\\"281000\\\",\\\"282000\\\",\\\"283000\\\",\\\"284000\\\"],\\\"cfgId\\\":280000}\",\"{\\\"unlock\\\":true,\\\"chips\\\":[\\\"231000\\\",\\\"232000\\\",\\\"233000\\\",\\\"234000\\\"],\\\"cfgId\\\":230000}\"]");
		school.setCrystalSerialized("{\"unlock\":true,\"chips\":[\"310010\",\"320010\",\"330010\"],\"cfgId\":30201000}");
		school.setStrengthenSerialized("[\"{\\\"unlock\\\":true,\\\"chips\\\":[\\\"411101\\\",\\\"411201\\\",\\\"411301\\\",\\\"411401\\\",\\\"411501\\\",\\\"412101\\\",\\\"412201\\\",\\\"412301\\\",\\\"412401\\\",\\\"412501\\\",\\\"413101\\\",\\\"413201\\\",\\\"413301\\\",\\\"413401\\\",\\\"413501\\\",\\\"414101\\\",\\\"414201\\\",\\\"414301\\\",\\\"414401\\\",\\\"414501\\\",\\\"415501\\\"],\\\"level\\\":5,\\\"cfgId\\\":100207}\",\"{\\\"unlock\\\":true,\\\"chips\\\":[\\\"421101\\\",\\\"421301\\\",\\\"421201\\\"],\\\"level\\\":0,\\\"cfgId\\\":100107}\",\"{\\\"unlock\\\":false,\\\"chips\\\":[],\\\"level\\\":0,\\\"cfgId\\\":100407}\",\"{\\\"unlock\\\":false,\\\"chips\\\":[],\\\"level\\\":0,\\\"cfgId\\\":100307}\",\"{\\\"unlock\\\":false,\\\"chips\\\":[],\\\"level\\\":0,\\\"cfgId\\\":100607}\",\"{\\\"unlock\\\":false,\\\"chips\\\":[],\\\"level\\\":0,\\\"cfgId\\\":100507}\",\"{\\\"unlock\\\":false,\\\"chips\\\":[],\\\"level\\\":0,\\\"cfgId\\\":100807}\",\"{\\\"unlock\\\":false,\\\"chips\\\":[],\\\"level\\\":0,\\\"cfgId\\\":100707}\"]");
		school.setMilitarySerialized("[\"{\\\"soldierType\\\":1,\\\"unlock\\\":true,\\\"chips\\\":[\\\"511040\\\",\\\"512040\\\",\\\"513040\\\",\\\"514040\\\",\\\"515040\\\"]}\",\"{\\\"soldierType\\\":2,\\\"unlock\\\":false,\\\"chips\\\":[\\\"521000\\\",\\\"522000\\\",\\\"523000\\\",\\\"524000\\\",\\\"525000\\\"]}\",\"{\\\"soldierType\\\":3,\\\"unlock\\\":false,\\\"chips\\\":[\\\"531000\\\",\\\"532000\\\",\\\"533000\\\",\\\"534000\\\",\\\"535000\\\"]}\",\"{\\\"soldierType\\\":4,\\\"unlock\\\":false,\\\"chips\\\":[\\\"541000\\\",\\\"542000\\\",\\\"543000\\\",\\\"544000\\\",\\\"545000\\\"]}\",\"{\\\"soldierType\\\":5,\\\"unlock\\\":false,\\\"chips\\\":[\\\"551000\\\",\\\"552000\\\",\\\"553000\\\",\\\"554000\\\",\\\"555000\\\"]}\",\"{\\\"soldierType\\\":6,\\\"unlock\\\":false,\\\"chips\\\":[\\\"561000\\\",\\\"562000\\\",\\\"563000\\\",\\\"564000\\\",\\\"565000\\\"]}\",\"{\\\"soldierType\\\":7,\\\"unlock\\\":false,\\\"chips\\\":[\\\"571000\\\",\\\"572000\\\",\\\"573000\\\",\\\"574000\\\",\\\"575000\\\"]}\",\"{\\\"soldierType\\\":8,\\\"unlock\\\":false,\\\"chips\\\":[\\\"581000\\\",\\\"582000\\\",\\\"583000\\\",\\\"584000\\\",\\\"585000\\\"]}\"]");
		school.afterRead();
		player.getPlantSoldierSchool().notifyChange(null);
//		// 英雄档案
//		HeroArchivesEntity archi = player.getData().getHeroArchivesEntity();
//		archi.setArchives("1058:5|1062:4|1002:5|1066:3|1003:5|1037:5|1041:5|1042:5|1043:5|1044:5|1077:5|1018:4|1051:5|1020:5|1021:5|1053:5|1054:5|1055:5");
//		archi.afterRead();
		
		//星能探索
		CommanderEntity commander = player.getData().getCommanderEntity();
		String starExplore = "[{\"starId\":1,\"progress\":[{\"progressVal\":2,\"protressId\":1},{\"progressVal\":2,\"protressId\":2},{\"progressVal\":7,\"protressId\":3}]},{\"starId\":2,\"progress\":[{\"progressVal\":50,\"protressId\":1},{\"progressVal\":25,\"protressId\":2},{\"progressVal\":25,\"protressId\":3}]},{\"starId\":3,\"progress\":[{\"progressVal\":15,\"protressId\":1},{\"progressVal\":13,\"protressId\":2},{\"progressVal\":22,\"protressId\":3}]},{\"starId\":4,\"progress\":[{\"progressVal\":4,\"protressId\":1},{\"progressVal\":4,\"protressId\":2},{\"progressVal\":3,\"protressId\":3}]},{\"starId\":5,\"progress\":[{\"progressVal\":22,\"protressId\":1},{\"progressVal\":30,\"protressId\":2},{\"progressVal\":24,\"protressId\":3}]},{\"starId\":6,\"progress\":[{\"progressVal\":50,\"protressId\":1},{\"progressVal\":25,\"protressId\":2},{\"progressVal\":25,\"protressId\":3}]},{\"starId\":7,\"progress\":[{\"progressVal\":10,\"protressId\":1},{\"progressVal\":9,\"protressId\":2},{\"progressVal\":12,\"protressId\":3}]}]";
		String starExploreCollect = "[{\"fixAttr\":[{\"attrId\":11028,\"attrVal\":1000}],\"collectId\":1,\"upCount\":0,\"randomAttr\":[{\"attrId\":11031,\"attrVal\":100}]}]";
		ArmourStarExplores starExplores = ArmourStarExplores.unSerialize(commander, starExplore, starExploreCollect);
		commander.setStarExplores(starExplores);
		commander.beforeWrite();
		commander.notifyUpdate();
				
		player.kickout(Status.Error.MIGRATE_FINISH_VALUE, true, "");
		DungeonRedisLog.log("fixPifu", "fix success  {}", playerId);
	}
	
	private void fixAnniversaryGiftActivityAchieve(Map<String, String> params){
		String playerId = params.get("playerId");
		int type = Integer.parseInt(params.get("type"));
		int value = Integer.parseInt(params.get("value"));
		boolean isLocalPlayer = GlobalData.getInstance().isLocalPlayer(playerId);
		if(!isLocalPlayer){
			HawkLog.logPrintln("fixAnniversaryGiftActivityAchieve isLocalPlayer ERR,player null, id: {}", playerId);
		}
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if(player==null){
			HawkLog.logPrintln("fixAnniversaryGiftActivityAchieve player ERR,player null, id: {}", playerId);
			return;
		}
		HawkXID xid = HawkXID.valueOf(ObjType.PLAYER, playerId);
		int threadId = xid.getHashThread(HawkTaskManager.getInstance().getThreadNum());
		if(type == 1){
			//累计金条消耗
			HawkTaskManager.getInstance().postTask(new HawkTask() {
				@Override
				public Object run() {
					ConsumeMoneyParser parse = (ConsumeMoneyParser) AchieveContext.getParser(AchieveType.CONSUME_MONEY);
			  		ConsumeMoneyEvent costEvent = new ConsumeMoneyEvent(playerId,PlayerAttr.DIAMOND_VALUE,value);
			  		fixAnniversaryGiftActivityAchieveProgress(playerId,AchieveType.CONSUME_MONEY,parse,costEvent);
					return null;
				}
    		},threadId);
		}else if(type == 2){
			//累计金条充值
    		HawkTaskManager.getInstance().postTask(new HawkTask() {
				@Override
				public Object run() {
					AccumulateDiamondRechargeParser parse = (AccumulateDiamondRechargeParser) AchieveContext.getParser(AchieveType.ACCUMULATE_DIAMOND_RECHARGE);
					DiamondRechargeEvent costEvent = new DiamondRechargeEvent(playerId,"0",value);
					fixAnniversaryGiftActivityAchieveProgress(playerId,AchieveType.ACCUMULATE_DIAMOND_RECHARGE,parse,costEvent);
					return null;
				}
    		},threadId);
			
		}else if(type == 3){
			//累计直购消费
			HawkTaskManager.getInstance().postTask(new HawkTask() {
				@Override
				public Object run() {
					RechargeGfitPayCountParse parse = (RechargeGfitPayCountParse) AchieveContext.getParser(AchieveType.RECHARGE_GIFT_PAY_COUNT);
					PayGiftBuyEvent buyEvent = new PayGiftBuyEvent(playerId,"0",value,0);
					fixAnniversaryGiftActivityAchieveProgress(playerId,AchieveType.RECHARGE_GIFT_PAY_COUNT,parse,buyEvent);
					return null;
				}
    		},threadId);
			
		}else if(type == 4){
			//累计人民币消费
			HawkTaskManager.getInstance().postTask(new HawkTask() {
				@Override
				public Object run() {
					RechargeAllRmbParse parse = (RechargeAllRmbParse) AchieveContext.getParser(AchieveType.RECHARGE_ALL_RMB);
					RechargeAllRmbEvent costEvent = new RechargeAllRmbEvent(playerId,value);
					fixAnniversaryGiftActivityAchieveProgress(playerId,AchieveType.RECHARGE_ALL_RMB,parse,costEvent);
					return null;
				}
    		},threadId);
		}
	}
	
	private void fixAnniversaryGiftActivityAchieveProgress(String playerId,AchieveType atype,AchieveParser<?> parse,ActivityEvent event){
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if(Objects.isNull(player)){
			HawkLog.logPrintln("fixAnniversaryGiftActivityAchieveProgress,player null, playerId: {}", playerId);
			return;
		}
		Optional<ActivityBase> opActivity = ActivityManager.getInstance().getActivity(Activity.ActivityType.ANNIVERSARY_GIFT_VALUE);
		AnniversaryGiftActivity activity = (AnniversaryGiftActivity) opActivity.get();
		//修复消耗累计任务
		Optional<AchieveItems> opAchieveItems = activity.getAchieveItems(playerId);
		if(!opAchieveItems.isPresent()){
			return;
		}
		List<AchieveItem> update = new ArrayList<>();
		AchieveItems achieveItems = opAchieveItems.get();
		for(AchieveItem item : achieveItems.getItems()){
			AnniversaryGiftAchievecfg acfg = HawkConfigManager.getInstance()
					.getConfigByKey(AnniversaryGiftAchievecfg.class, item.getAchieveId());
			//处理直购充值
			if(acfg.getReset() == 1){
				continue;
			}
			if(acfg.getAchieveType() != atype){
				continue;
			}
	  		parse.updateAchieveData(item, acfg, event, update);
		}
		achieveItems.getEntity().notifyUpdate();
		if(update.size() > 0){
			AchievePushHelper.pushAchieveUpdate(playerId, update);
		}
	}
	
	/**
	 * 更新玩家缓存数据
	 * @param params
	 */
	private void updateAccountInfo(Map<String, String> params) {
		String pid = params.get("playerId");
		Set<String> playerSet = new HashSet<>();
		if (!HawkOSOperator.isEmptyString(pid)) {
			playerSet.add(pid);
		} else {
			playerSet.add("82v-1cmwp5-2f");
			playerSet.add("82t-1cigut-z");
		}
		HawkTaskManager.getInstance().postExtraTask(new HawkTask() {
			@Override
			public Object run() {
				loadPlayerFromDB(playerSet);
				return null;
			}
		});
	}
	
	private void loadPlayerFromDB(Set<String> playerSet) {
		for (String playerId : playerSet) {
			try {
				StringBuilder sb = new StringBuilder("select id as playerId, puid, serverId, forbidenTime, logoutTime, updateTime, isActive, name as playerName from player where ");
				sb.append("id='").append(playerId).append("'");			
				List<AccountInfo> accountInfoList = HawkDBManager.getInstance().executeQuery(sb.toString(), AccountInfo.class);
				if (accountInfoList == null || accountInfoList.isEmpty()) {
					continue;
				}
				AccountInfo accountInfo = accountInfoList.get(0);
				if (!accountInfo.isActive()) {
					continue;
				}	
				PlayerData playerData = GlobalData.getInstance().getPlayerData(accountInfo.getPlayerId(), true);
				if (playerData != null) {
					playerData.loadStart();
				}
				GlobalData.getInstance().updateAccountInfo(accountInfo.getPuid(), accountInfo.getServerId(), accountInfo.getPlayerId(), 0, accountInfo.getPlayerName());
				HawkLog.logPrintln("sysop updateAccountInfo success, playerId: {}", playerId);
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
	}
	
	private void updatePlayerCollegeInfo(Map<String, String> params){
		String playerId = params.get("playerId");
		int auth = Integer.parseInt(params.get("auth"));
		HawkXID xid = HawkXID.valueOf(ObjType.PLAYER, playerId);
		int threadId = xid.getHashThread(HawkTaskManager.getInstance().getThreadNum());
		HawkTaskManager.getInstance().postTask(new HawkTask() {
			@Override
			public Object run() {
				Player player = GlobalData.getInstance().makesurePlayer(playerId);
				if(Objects.isNull(player)){
					HawkLog.logPrintln("updatePlayerCollegeInfo,player null, playerId: {}", playerId);
					return null;
				}
				player.getData().getCollegeMemberEntity().setAuth(auth);
				player.getData().getCollegeMemberEntity().notifyUpdate(false,0);
				return null;
			}
		},threadId);
	}
	
	/**
	 * 加载联盟堡垒数据
	 */
	private void loadGuildManorData() {
		try {
			Map<String, GuildManorObj> allManors = HawkOSOperator.getFieldValue(GuildManorService.getInstance(), "allManors");
			Map<String, List<GuildManorObj>> guildManors = HawkOSOperator.getFieldValue(GuildManorService.getInstance(), "guildManors");
			long startTime = HawkTime.getMillisecond();
			Map<String, List<GuildManorObj>> guildManorsNew = new HashMap<>();
			//从数据库中加载已有的联盟领地
			List<GuildManorEntity> dbManorList = HawkDBManager.getInstance().query("from GuildManorEntity where invalid = 0");
			if (dbManorList != null && !dbManorList.isEmpty()) {
				for (GuildManorEntity entity : dbManorList) {
					GuildManorObj obj = new GuildManorObj(entity);
					allManors.put(entity.getManorId(), obj);//存入全局对应关系
					String guildId = entity.getGuildId();
					if (HawkOSOperator.isEmptyString(guildId)) {
						HawkLog.logPrintln("sysop manor has not guildId, manorId={}", entity.getManorId());
						continue;
					}
					//公会对应的列表
					List<GuildManorObj> list = guildManorsNew.get(guildId);
					if(list == null){
						list = new ArrayList<GuildManorObj>();
						guildManorsNew.put(guildId, list);
					}
					list.add(obj);
				}
				guildManorsNew.entrySet().forEach(e -> guildManors.put(e.getKey(), e.getValue()));
				HawkLog.logPrintln("sysop load guild manor info success, num: {},  costtime: {} ", dbManorList.size(), HawkTime.getMillisecond() - startTime);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 加载联盟建筑数据
	 */
	private void loadGuildBuildData() {
		try {
			Map<String, IGuildBuilding> allBuildings = HawkOSOperator.getFieldValue(GuildManorService.getInstance(), "allBuildings");
			Map<String, List<IGuildBuilding>> guildBuildings = HawkOSOperator.getFieldValue(GuildManorService.getInstance(), "guildBuildings");
			
			long startTime = HawkTime.getMillisecond();
			Map<String, List<IGuildBuilding>> guildBuildingsNew = new HashMap<>();
			List<GuildBuildingEntity> dbManorBuildList = HawkDBManager.getInstance().query("from GuildBuildingEntity where invalid = 0");
			if (dbManorBuildList != null && !dbManorBuildList.isEmpty()) {
				for (GuildBuildingEntity entity : dbManorBuildList) {
					IGuildBuilding building = GuildManorService.getInstance().loadGuildBuild(entity);
					if(building != null){
						allBuildings.put(entity.getId(), building);//存入全局对应关系
						String guildId = entity.getGuildId();
						if (HawkOSOperator.isEmptyString(guildId)) {
							HawkLog.logPrintln("sysop building has not guildId, building={}", entity.getId());
							continue;
						}
						List<IGuildBuilding> list = guildBuildingsNew.get(guildId);
						if(list == null){
							list = new ArrayList<IGuildBuilding>();
							guildBuildingsNew.put(guildId, list);
						}
						list.add(building);
					}
				}
				
				guildBuildingsNew.entrySet().forEach(e -> guildBuildings.put(e.getKey(), e.getValue()));
				long buildCostTime = HawkTime.getMillisecond() - startTime;
				HawkLog.logPrintln("sysop load guild building info success, num: {},  costtime: {} ", dbManorBuildList.size(), buildCostTime);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	private void updateGuildSuperMineStat() {
		List<String> guildIds = GuildService.getInstance().getGuildIds();
		for (String guildId : guildIds) {
			try {
				int count = 0;
				List<IGuildBuilding> buildings = GuildManorService.getInstance().getGuildBuildings(guildId);
				for (IGuildBuilding iGuildBuilding : buildings) {
					if(iGuildBuilding.getBuildType() == TerritoryType.GUILD_MINE && iGuildBuilding.getBuildStat() == GuildBuildingStat.LOCKED){
						count++;
					}
				}
				if (count >= 4) {
					for (IGuildBuilding iGuildBuilding : buildings) {
						if(iGuildBuilding.getBuildType() == TerritoryType.GUILD_MINE && iGuildBuilding.getBuildStat() == GuildBuildingStat.LOCKED){
							iGuildBuilding.tryChangeBuildStat(GuildBuildingStat.OPENED.getIndex());
						}
					}
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
	}
	
	//万人团购差价补发逻辑开始
	private void groupBuyActivityEndSendAward(Map<String, String> params) {
		List<String> fileContents = new ArrayList<>();
		try {
			HawkOSOperator.readTextFileLines("tmp/seperateServers.txt", fileContents);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		Map<String, String> serverMap = new HashMap<>();
		for (String content : fileContents) {
			String[] arr = content.split(",");
			serverMap.put(arr[0], arr[1]);
		}
		int termId = Integer.parseInt(params.getOrDefault("termId", "48"));
		String serverId = GsConfig.getInstance().getServerId();
		if (!serverMap.containsKey(serverId)) {
			return;
		}
		
		String mainServerId = serverMap.get(serverId);
		String recordKey = "activiy_group_buy_players:" + mainServerId + ":" + termId;
		Set<String> playerIds = ActivityGlobalRedis.getInstance().sMembers(recordKey);
		recordKey = "activiy_group_buy_players:" + serverId + ":" + termId;
		Set<String> playerIdsSlave = ActivityGlobalRedis.getInstance().sMembers(recordKey);
		playerIds.removeAll(playerIdsSlave);
		if (playerIds.isEmpty()) {
			HawkLog.logPrintln("sysop GroupBuyActivity onEnd noPlayerGroupBuy");
			return;
		}
		
		groupBuyActivityEndSendAward(playerIds);
	}

	private void groupBuyActivityEndSendAward(Set<String> playerIds) {
		Optional<ActivityBase> opActivity = ActivityManager.getInstance().getActivity(Activity.ActivityType.GROUP_BUY_VALUE);
		GroupBuyActivity activity = (GroupBuyActivity) opActivity.get();
		Map<String, List<RewardItem.Builder>> dataMap = new HashMap<>();
		Map<String, Integer> buyTimesMap = new HashMap<>();
		for (String playerId : playerIds) {
			if (!GlobalData.getInstance().isLocalPlayer(playerId)) {
				continue;
			}
			
			HawkLog.logPrintln("sysop GroupBuyActivity sendAward, playerId: {}", playerId);
			Optional<GroupBuyEntity> opEntity = activity.getPlayerDataEntity(playerId);
			if (!opEntity.isPresent()) {
				HawkLog.logPrintln("sysop GroupBuyActivity onEnd sendAwardError, entity is null, playerId: {}", playerId);
				continue;
			}
			GroupBuyEntity dataEntity = opEntity.get();
			//计算前后差价的奖励
			List<RewardItem.Builder> rewardList = calcReissueReward(activity, dataEntity);
			if (rewardList == null || rewardList.isEmpty() || rewardList.get(0).getItemCount() <= 0) {
				HawkLog.logPrintln("sysop GroupBuyActivity onEnd DiffRewardList is empty, playerId: {}", playerId);
				continue;
			}
			//购买次数
			int buyTimes = dataEntity.getBuyRecordList().size();
			buyTimesMap.put(playerId, buyTimes);
			dataMap.put(playerId, rewardList);
		}
		
		for (String playerId : playerIds) {
			final List<RewardItem.Builder> rewardData = dataMap.get(playerId);
			final int buyTimes = buyTimesMap.getOrDefault(playerId, 0);
			if (buyTimes == 0 || rewardData == null || rewardData.isEmpty() || rewardData.get(0).getItemCount() <= 0) {
				HawkLog.logPrintln("sysop GroupBuyActivity sendAward condition check break, playerId: {}", playerId);
				continue;
			}
			activity.callBack(playerId, MsgId.GROUP_BUY_END_REWARD, () -> {
				sendReward(activity, playerId, rewardData, buyTimes);
			});
		}
	}

	/**
	 * 补发未领取的奖励
	 * 
	 * @param rewardData
	 */
	private void sendReward(GroupBuyActivity activity, String playerId,  List<RewardItem.Builder> rewardData, int buyTimes) {
		MailId mailId = MailId.GROUP_BUY_REWARD;
		//邮件发送奖励
		Object[] title = new Object[0];
		Object[] subTitle = new Object[0];
		Object[] content = new Object[2];
		content[0] = buyTimes;  //购买商品数量
		content[1] = rewardData.get(0).getItemCount();	//返回金条数量
		// 邮件发送奖励
		activity.sendMailToPlayer(playerId, mailId, title, subTitle, content, rewardData);
		HawkLog.logPrintln("sysop GroupBuyActivity sendDiscountReward success, playerId: {}", playerId);
	}

	/**
	 * 计算待补发奖励信息
	 * @param entity
	 * @return
	 */
	private List<RewardItem.Builder> calcReissueReward(GroupBuyActivity activity, GroupBuyEntity entity) {
		if (entity == null) {
			return null;
		}
		try {
			List<RewardItem.Builder> rewardList = new ArrayList<>();
			//当前礼包折扣数据
			Map<Integer, GroupBuyPriceCfg> discountMap = activity.getDiscountMap();
			
			List<GroupBuyRecord> recordList = entity.getBuyRecordList();
			//实例奖励,取第一个奖励,即金条
			GroupBuyPriceCfg diamondPriceCfg = HawkConfigManager.getInstance().getConfigByIndex(GroupBuyPriceCfg.class, 0);
			RewardItem.Builder diamondCounsumeItem = RewardHelper.toRewardItem(diamondPriceCfg.getPrice());
			diamondCounsumeItem.setItemCount(0);
			for (GroupBuyRecord record : recordList) {
				int cfgId = record.getCfgId();
				int giftId = record.getId();
				GroupBuyPriceCfg priceCfg = HawkConfigManager.getInstance().getConfigByKey(GroupBuyPriceCfg.class, cfgId);
				//购买花费
				String priceStr = priceCfg.getPrice();
				long beforeCost = RewardHelper.toRewardItem(priceStr).getItemCount();
				//当前价格
				String currentCostStr = discountMap.get(giftId).getPrice();
				long currentCost =  RewardHelper.toRewardItem(currentCostStr).getItemCount();
				long addNum = beforeCost - currentCost;
				addNum = addNum * record.getNum();
				//每个礼包的差额奖励
				diamondCounsumeItem.setItemCount(diamondCounsumeItem.getItemCount() + addNum);
				HawkLog.logPrintln("sysop GroupBuyActivity calcReissueReward playerId:{}, giftId:{}, addNum:{} ",entity.getPlayerId(), giftId, addNum);
			}
			rewardList.add(diamondCounsumeItem);
			return rewardList;
			
		} catch (Exception e) {
			HawkException.catchException(e);
			HawkLog.logPrintln("sysop GroupBuyActivity calcRewardData error, playerId:{}, termId:{}, buyRecord:{},", entity.getPlayerId(), entity.getTermId(), entity.getBuyRecord());
		}
		return null;
	}
	
	/**
	 * 修复每日签到活动数据
	 * @param params
	 */
	private void fixDailySignActivityData(Map<String, String> params) {
		String playerId = params.getOrDefault("playerId", "foc-2ag17m-r");
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player == null) {
			return;
		}
		
		Optional<ActivityBase> opActivity = ActivityManager.getInstance().getActivity(Activity.ActivityType.DAILY_SIGN_VALUE);
		DailySignActivity activity = (DailySignActivity) opActivity.get();
		long time = HawkTime.getMillisecond();
		try {
			if(!activity.isOpening(playerId)) {
				RedisProxy.getInstance().getRedisSession().hSet("fixDailySignActivity", GsConfig.getInstance().getServerId() + ":" + playerId, String.valueOf(time), 7200);
				return;
			}
			
			Optional<DailySignEntity> opEntity = activity.getPlayerDataEntity(playerId);
			DailySignEntity entity = opEntity.get();
			List<AchieveItem> itemList = HawkOSOperator.getFieldValue(entity, "itemList");
			Map<Integer, AchieveItem> map = new HashMap<>();
			for (AchieveItem item : itemList) {
				AchieveItem oldItem = map.get(item.getAchieveId());
				if (oldItem == null) {
					map.put(item.getAchieveId(), item);
				} else if (item.getState() > oldItem.getState() || item.getValue(0) > oldItem.getValue(0)) {
					map.put(item.getAchieveId(), item);
				}
			}
			if (map.size() != itemList.size()) {
				itemList.clear();
				itemList.addAll(map.values());
				entity.notifyUpdate();
				AchievePushHelper.pushAchieveUpdate(playerId, itemList);
			}
			
			RedisProxy.getInstance().getRedisSession().hSet("fixDailySignActivitySucc", GsConfig.getInstance().getServerId() + ":" + playerId, String.valueOf(time), 7200);
		} catch (Exception e) {
			HawkException.catchException(e);
			RedisProxy.getInstance().getRedisSession().hSet("fixDailySignActivityExp", GsConfig.getInstance().getServerId() + ":" + playerId, String.valueOf(time), 7200);
		}
	}
	
	private void fixPlayerCastleRank() {
		HawkThreadPool threadPool = HawkTaskManager.getInstance().getThreadPool("task");
		HawkLog.logPrintln("fixPlayerCastleRank, theadPool: {}", threadPool == null ? "null" : "useful");
		if (threadPool != null) {
			HawkTask task = new HawkTask() {
				@Override
				public Object run() {
					fixPlayerCastleRankData();
					return null;
				}
			};

			task.setPriority(1);
			task.setTypeName("fixPlayerCastleRank");
			threadPool.addTask(task);
		} else {
			HawkTaskManager.getInstance().postExtraTask(new HawkTask() {
				@Override
				public Object run() {
					fixPlayerCastleRankData();
					return null;
				}
			});
		}
	}
	
	private void fixPlayerCastleRankData() {
		StringBuilder sb = new StringBuilder("select id as playerId, puid, serverId, forbidenTime, logoutTime, updateTime, isActive, name as playerName from player where loginTime > 1734559500000");			
		List<AccountInfo> accountInfoList = HawkDBManager.getInstance().executeQuery(sb.toString(), AccountInfo.class);
		int accountSize = accountInfoList.size(), succCount = 0;
		for (AccountInfo accountInfo : accountInfoList) {
			try {
				Player player = GlobalData.getInstance().makesurePlayer(accountInfo.getPlayerId());
				BuildingCfg buildingCfg = player.getData().getBuildingCfgByType(BuildingType.CONSTRUCTION_FACTORY);
				int rankScore = buildingCfg.getLevel();
				int honor = buildingCfg.getHonor();
				int progress = buildingCfg.getProgress();
				if (honor > 0 || progress > 0) {
					rankScore = buildingCfg.getLevel() * RankService.HONOR_CITY_OFFSET + progress;
				}
				long upgradeTime = player.getData().getBuildingEntityByType(BuildingType.CONSTRUCTION_FACTORY).getLastUpgradeTime();
				if (buildingCfg.getLevel() >= 40) {
					upgradeTime = GlobalData.getInstance().getCityRankTime(player.getId());
				}

				long value = upgradeTime/1000 - RankScoreHelper.rankSpecialSeconds;
				long score = Long.valueOf(rankScore + "" + (RankScoreHelper.rankSpecialOffset - value));
				LocalRedis.getInstance().updateRankScore(RankType.PLAYER_CASTLE_KEY, score, player.getId());
				succCount++;
			} catch (Exception e) {
				HawkLog.errPrintln("fixPlayerCastleRankData exception, playerId: {}", accountInfo.getPlayerId());
				HawkException.catchException(e);
			}
		}
		RedisProxy.getInstance().getRedisSession().hSet("sysopCastleRank", GsConfig.getInstance().getServerId(), accountSize+"-"+succCount, 7200);
	}
	
	/**
	 * 修复指挥官等级排行榜数据
	 */
	public void fixCommanderLevelRank() {
		HawkThreadPool threadPool = HawkTaskManager.getInstance().getThreadPool("task");
		HawkLog.logPrintln("fixCommanderLevelRank, theadPool: {}", threadPool == null ? "null" : "useful");
		if (threadPool != null) {
			HawkTask task = new HawkTask() {
				@Override
				public Object run() {
					fixCommanderLevelRankData();
					return null;
				}
			};
			task.setPriority(1);
			task.setTypeName("fixCommanderLevelRank");
			threadPool.addTask(task);
		} else {
			HawkTaskManager.getInstance().postExtraTask(new HawkTask() {
				@Override
				public Object run() {
					fixCommanderLevelRankData();
					return null;
				}
			});
		}
	}
	
	/**
	 * 修复指挥官等级排行榜数据
	 */
	private void fixCommanderLevelRankData() {
		List<String> fileContents = new ArrayList<>();
		try {
			//2024/10/25 13:57,10054,60,7ra-hq6nb-f6
			HawkOSOperator.readTextFileLines("tmp/commanderLevel.txt", fileContents);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		Map<String, String> playerLevelMap = new HashMap<>();
		for (String content : fileContents) {
			String[] arr = content.split(",");
			try {
				String serverId = GlobalData.getInstance().getMainServerId(arr[1]);
				//long calcTime = HawkTime.parseTime(arr[0], "yyyy/MM/dd HH:mm");
				//HawkLog.logPrintln("sysop fixCommanderLevelRankData, playerId: {}, serverId: {}, mainServer: {}, timeStr: {}, time: {}", arr[3], arr[1], serverId, arr[0], calcTime);
				if (GsConfig.getInstance().getServerId().equals(serverId)) {
					playerLevelMap.put(arr[3], arr[0]); //playerId,time
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		
		Map<String, Double> scoreMap = new HashMap<>();
		Map<String, String> timeMap = new HashMap<>();
		for (Entry<String, String> entry : playerLevelMap.entrySet()) {
			String playerId = entry.getKey(), timeStr = entry.getValue();
			try {
				Player player = GlobalData.getInstance().makesurePlayer(playerId);
				if (player == null || player.isCsPlayer()) {
					HawkLog.errPrintln("fixCommanderLevelRankData player break, playerId: {}, empty: {}", playerId, player == null);
					continue;
				}
				
				int level = player.getLevel();
				long calcTime = HawkTime.parseTime(timeStr, "yyyy/MM/dd HH:mm");
				long value = calcTime/1000 - RankScoreHelper.rankSpecialSeconds;
				double calcScore = Double.valueOf(level + "" + (RankScoreHelper.rankSpecialOffset - value));
				scoreMap.put(playerId, calcScore);
				timeMap.put(playerId, String.valueOf(calcTime));
			} catch (Exception e) {
				HawkLog.errPrintln("fixCommanderLevelRankData exception, playerId: {}", playerId);
				HawkException.catchException(e);
			}
		}
		
		if (!scoreMap.isEmpty()) {
			//更新排行榜数据
			LocalRedis.getInstance().updateRankScore(RankType.PLAYER_GRADE_KEY, scoreMap);
			//存储玩家等级升级时间，留作后用
			RedisProxy.getInstance().getRedisSession().hmSet(RedisKey.PLAYER_LEVELUP_TIME, timeMap, 0);
			//记录sysop执行凭证信息
			RedisProxy.getInstance().getRedisSession().hSet("sysopCommanderLevelRank", GsConfig.getInstance().getServerId(), playerLevelMap.size()+"-"+scoreMap.size(), 7200);
		} else {
			RedisProxy.getInstance().getRedisSession().hSet("sysopCommanderLevelRank", GsConfig.getInstance().getServerId(), "empty0-0", 7200);
		}
	}
	
	/**
	 * 批量给联盟发送奖励
	 */
	public void batchSendGuildAward() {
		List<String> fileContents = new ArrayList<>();
		try {
			//原服id;guildId;奖励三段式
			HawkOSOperator.readTextFileLines("tmp/batchGuildAward.txt", fileContents);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		if (fileContents.size() < 3) {
			return;
		}
		
		//文件的第一行、第二行是邮件标题和邮件内容
		String title = fileContents.get(0), content = fileContents.get(1);
		for (int i = 2; i < fileContents.size(); i++) {
			String str = fileContents.get(i);
			try {
				String[] arr = str.split(";");
				if (arr.length >= 3 && GlobalData.getInstance().isLocalServer(arr[0])) {
					sendGuildAwardMail(arr[1], arr[2], title, content);
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
	}
	
	/**
	 * 给单个联盟发奖
	 * @param guildId
	 * @param awardItems
	 */
	private void sendGuildAwardMail(String guildId, String awardItems) {
		List<String> fileContents = new ArrayList<>();
		try {
			//原服id;guildId;奖励三段式
			HawkOSOperator.readTextFileLines("tmp/batchGuildAward.txt", fileContents);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		if (fileContents.size() < 2) {
			return;
		}
		
		try {
			if (GuildService.getInstance().getGuildInfoObject(guildId) != null) {
				sendGuildAwardMail(guildId, awardItems, fileContents.get(0), fileContents.get(1));	
			} else {
				HawkLog.errPrintln("sysop sendGuildAwardMail failed, not local guild, guildId: {}", guildId);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 给联盟发奖励
	 * @param guildId
	 * @param awardItems
	 */
	public void sendGuildAwardMail(String guildId, String awardItems, String title, String content) {
		if (GuildService.getInstance().getGuildInfoObject(guildId) == null) {
			return;
		}
		
		String redisKey  = "sysopGuildMemberAward:" + guildId;
		HawkRedisSession redisSession = RedisProxy.getInstance().getRedisSession();
		boolean redisMarkSetSucc = redisSession.setNx(redisKey, HawkTime.formatNowTime());
		if (!redisMarkSetSucc) {
			HawkLog.errPrintln("sysop sendGuildAwardMail error, already");
			return;
		}

		redisSession.expire(redisKey, 3600);
		
		for (String playerId : GuildService.getInstance().getGuildMembers(guildId)) {
			SystemMailService.getInstance().sendMail(MailParames.newBuilder()
					.setPlayerId(playerId)
					.setMailId(MailId.REWARD_MAIL)
					.setAwardStatus(MailRewardStatus.NOT_GET)
					.addSubTitles(title)
					.addContents(content)
					.setRewards(awardItems)
					.build());
		}
		HawkLog.logPrintln("sendGuildAwardMail success, guildId: {}, awardItems: {}", guildId, awardItems);
	}
	
	/**
	 * 批量迁服
	 * @param playerId
	 */
	public void batchImmigration() {
		List<String> fileContents = new ArrayList<>();
		try {
			//原服id,玩家id,迁服目标服
			HawkOSOperator.readTextFileLines("tmp/batchImmigration.txt", fileContents);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		for (String str : fileContents) {
			try {
				String[] arr = str.split(",");
				if (arr.length >= 3 && GlobalData.getInstance().isLocalServer(arr[0])) {
					playerImmigrate(arr[1], arr[2]);
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
	}
	
	/**
	 * 迁服
	 * @param playerId
	 * @param targetServerId
	 */
	public boolean playerImmigrate(String playerId, String targetServerId) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player == null) {
			HawkLog.errPrintln("sysop playerImmigrate failed, player null: {}", playerId);
			return false;
		}

		// 检测一下能不能迁
		if (!this.checkBeforeImmgration(player, targetServerId)) {
			return false;
		}
		
		//有联盟，自动退出联盟
		if (player.hasGuild()) {
			HawkLog.logPrintln("sysop playerImmigrate quit guild, playerId: {}, guildId: {}", playerId, player.getGuildId());
			GuildService.getInstance().onQuitGuild(player.getGuildId(), player.getId());
		}
		// 有军事学院，自动从军事学院退出
		if (player.hasCollege()) {
			HawkLog.logPrintln("sysop playerImmigrate quit college, playerId: {}", playerId);
			CollegeService.getInstance().quitCollege(player);
		}
		// 有守护关系，自动解除守护关系
		if (RelationService.getInstance().hasGuarder(player.getId())) {
			HawkLog.logPrintln("sysop playerImmigrate remove guard, playerId: {}", playerId);
			RelationService.getInstance().onGuardDelete(player.getId());
		}
		
		int delay = 0;
		// 有行军，自动遣返所有行军
		if (WorldMarchService.getInstance().getPlayerMarchCount(player.getId()) > 0) {
			delay = 5000;
			HawkLog.logPrintln("sysop playerImmigrate move city in oldPlace, playerId: {}", playerId);
			WorldThreadScheduler.getInstance().postWorldTask(new WorldTask(GsConst.WorldTaskType.MOVE_CITY_IN_PLACE) {
				@Override
				public boolean onInvoke() {
					WorldPlayerService.getInstance().moveCity(player.getId(), false, true);
					GsApp.getInstance().addDelayAction(2000, new HawkDelayAction() {
						@Override
						protected void doAction() {
							WorldMarchService.getInstance().armysCheckAndFix(player);
						}
					});
					return true;
				}
			});
		}
		
		if (delay > 0) {
			GsApp.getInstance().addDelayAction(delay, new HawkDelayAction() {
				@Override
				protected void doAction() {
					playerImmigrate(player, targetServerId);
				}
			});
		} else {
			playerImmigrate(player, targetServerId);
		}
		
		return true;
	}
	
	@SuppressWarnings("deprecation")
	private void playerImmigrate(Player player, String targetServerId) {
		HawkLog.logPrintln("sysop playerImmigrate start, playerId: {}, targetServerId: {}", player.getId(), targetServerId);
		try {
			// 序列化玩家数据
			boolean flushToRedis = PlayerDataSerializer.flushToRedis(player.getData().getDataCache(), null, false);
			if (!flushToRedis) {
				HawkLog.errPrintln("sysop playerImmigrate flushToRedis failed, playerId: {}, targetServerId: {}", player.getId(), targetServerId);
				return;
			}
			PlayerImmgrationModule module = player.getModule(GsConst.ModuleType.IMMGRATION);
			// 序列化活动数据
			ConfigIterator<ImmgrationActivityCfg> cfgIter = HawkConfigManager.getInstance().getConfigIterator(ImmgrationActivityCfg.class);
			while (cfgIter.hasNext()) {
				ImmgrationActivityCfg cfg = cfgIter.next();
				if (module.flushActivityToRedis(player.getId(), cfg.getActivityId())) {
					HawkLog.logPrintln("sysop playerImmigrate, flush activity to redis, playerId: {}, activityId: {}", player.getId(), cfg.getActivityId());
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
			return;
		}

		// 通知目标服
		Immgration.ImmgrationServerReq.Builder builder = Immgration.ImmgrationServerReq.newBuilder();
		builder.setPlayerId(player.getId());
		builder.setTarServerId(targetServerId);
		HawkProtocol protocol = HawkProtocol.valueOf(HP.code2.IMMGRATION_SERVER_REQ_VALUE, builder);
		CrossProxy.getInstance().sendNotify(protocol, GlobalData.getInstance().getMainServerId(targetServerId), player.getId(), null);
		
		// 日志记录
		try {
			int termId = ImmgrationService.getInstance().getImmgrationActivityTermId();
			JSONObject immgrationLog = new JSONObject();
			immgrationLog.put("playerId", player.getId());
			immgrationLog.put("fromServer", player.getServerId());
			immgrationLog.put("tarServer", targetServerId);
			immgrationLog.put("time", HawkTime.formatNowTime());
			immgrationLog.put("puid", player.getPuid());
			RedisProxy.getInstance().updateImmgrationRecord(termId, player.getId(), immgrationLog.toJSONString());
			RedisProxy.getInstance().addPlayerImmgrationLog(immgrationLog);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		HawkLog.logPrintln("sysop playerImmigrate end, playerId: {}, targetServerId: {}", player.getId(), targetServerId);
	}
	
	/**
	 * 迁服条件检测
	 * @param player
	 * @param tarServerId
	 * @return
	 */
	private boolean checkBeforeImmgration(Player player, String tarServerId) {
		//玩家处于跨服状态中，不能迁服
		if (player.isCsPlayer()) {
			HawkLog.errPrintln("sysop playerImmigrate failed, player is csPlayer: {}", player.getId());
			return false;
		}
		//玩家账号在目标服有角色，不能迁服
		Map<String, AccountRoleInfo> accountRoleInfos = GlobalData.getInstance().getPlayerAccountInfos(player.getOpenId());
		for (AccountRoleInfo accountRole : accountRoleInfos.values()) {
			if (accountRole.getServerId().equals(tarServerId) && accountRole.getPlatform().equals(player.getPlatform())) {
				HawkLog.errPrintln("sysop playerImmigrate failed, player account has role in target server, playerId: {}, tarServerId: {}, tarPlayerId: {}", player.getId(), tarServerId, accountRole.getPlayerId());
				return false;
			}
		}
		
		//有未处理的活动数据 (刮刮乐353活动)，不能迁服
		Optional<ActivityBase> activityOp = ActivityManager.getInstance().getGameActivityByType(Activity.ActivityType.LOTTERY_TICKET_VALUE);
		if(activityOp.isPresent()){
			LotteryTicketActivity activity = (LotteryTicketActivity) activityOp.get();
			if (activity.isOpening(player.getId()) && activity.inApplay(player.getId())) {
				HawkLog.errPrintln("sysop playerImmigrate failed, activity 353 break, playerId: {}", player.getId());
				return false;
			}
		}
				
//		// 有建造中的建筑队列，不能移民
//		if (player.getData().getQueueEntitiesByType(QueueType.BUILDING_QUEUE_VALUE).size() > 0) {
//			return false; //不影响，需验证
//		}
//		// 有研究中的科技，不能移民
//		if (player.getData().getQueueEntitiesByType(QueueType.SCIENCE_QUEUE_VALUE).size() > 0) {
//			return false; //不影响，需验证
//		}
//		// 正在造兵，不能移民
//		if (player.getData().getQueueEntitiesByType(QueueType.SOILDER_QUEUE_VALUE).size() > 0) {
//			return false; //不影响，需验证
//		}
//		// 有治疗中的兵，不能移民
//		if (player.getData().getQueueEntitiesByType(QueueType.CURE_QUEUE_VALUE).size() > 0) {
//			return false; //不影响，需验证
//		}
//		// 装备科技研究中，不能移民
//		if (player.getData().getQueueEntitiesByType(QueueType.EQUIP_RESEARCH_QUEUE_VALUE).size() > 0) {
//			return false; //不影响，需验证
//		}
		
		return true;
	}
	
    // ----------------- 20250807整理  TODO ----------------
	
	/**
	 * 修复迁服数据 指定类型.  不可用于
	 */
	public void fixImmgrationByDataKey(Map<String, String> params) {
		String playerId = params.get("playerId");
		String keyStr = params.get("dataKeys");
		String[] keyArr = keyStr.split(",");
		immgration(playerId, keyArr);
	}
	
	@SuppressWarnings("unchecked")
	public void immgration(String playerId, String[] keyArr) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		{ // 不清理活动数据
			// 获取玩家
			// 玩家在线的话,清掉玩家
			if (GlobalData.getInstance().isOnline(playerId)) {
				player.kickout(Status.Error.MIGRATE_FINISH_VALUE, true, "");
			}
			GlobalData.getInstance().removeActivePlayer(playerId);

			try {
				HawkThread.sleep(1000L);
			} catch (Exception e) {
				HawkException.catchException(e);
			}

			
		}

		for (String keyStr : keyArr) {
			try {
				PlayerDataKey dataKey = PlayerDataKey.valueOf(keyStr);
				// 方尖碑不处理
				if (dataKey == PlayerDataKey.ObeliskEntities || dataKey == PlayerDataKey.PlayerEntity || dataKey == PlayerDataKey.PlayerBaseEntity) {
					continue;
				}

				// 从redis读取数据
				byte[] bytes = RedisProxy.getInstance().getRedisSession().hGetBytes("player_data:" + playerId, dataKey.name());
				// 反序列化
				Object data = PlayerDataSerializer.unserializeData(dataKey, bytes, false);
				if (data == null) {
					continue;
				}

				// 删除本地所有数据
				Object olddata = player.getData().getDataCache().makesureDate(dataKey);
				if (dataKey.listMode()) {
					List<HawkDBEntity> entityList = (List<HawkDBEntity>) olddata;
					for (HawkDBEntity entity : entityList) {
						try {
							entity.setPersistable(true);
							entity.delete();
						} catch (Exception e) {
							HawkException.catchException(e);
						}
					}
				} else {
					HawkDBEntity entity = (HawkDBEntity) olddata;
					entity.setPersistable(true);
					entity.delete();
				}
				// 写入迁服数据
				if (!dataKey.listMode()) {
					HawkDBEntity entity = (HawkDBEntity) data;
					entity.setPersistable(true);
					entity.create();
				} else {
					List<HawkDBEntity> entityList = (List<HawkDBEntity>) data;
					for (HawkDBEntity entity : entityList) {
						try {
							entity.setPersistable(true);
							entity.create();
						} catch (Exception e) {
							HawkException.catchException(e);
						}
					}
				}
				// 更新dataCache数据
				player.getData().getDataCache().update(dataKey, data);
				HawkLog.logPrintln("sysop fix playerData success playerId:{} key : {}", player.getId(), dataKey);
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
	}
	
	/**
	 * 迁服
	 * @param playerId
	 * @param serverId
	 */
	public String immgration(String playerId, String serverId, HawkRedisSession redisSession) {
		// 先清掉玩家缓存
		//clearPlayerCache(playerId);
		
		// 清掉活动数据缓存
		PlayerActivityData activityData = PlayerDataHelper.getInstance().getPlayerData(playerId, false);
		if (activityData != null) {
			activityData.getDataMap().clear();
			Map<Integer, ActivityPlayerEntity> playerActivityEntityMap = activityData.getPlayerActivityEntityMap();
			for (ActivityPlayerEntity entity : playerActivityEntityMap.values()) {
				entity.delete(false);
				HawkLog.logPrintln("delete activity entity:{}", entity);
			}
			playerActivityEntityMap.clear();
		}
		
		if (redisSession == null) {
			redisSession = RedisProxy.getInstance().getRedisSession();
		}
		
		// 删除本地所有数据
		PlayerDataSerializer.deleteAllEntity(playerId);
		
		String puid = "";
		for (PlayerDataKey dataKey : EnumSet.allOf(PlayerDataKey.class)) {
			try {
				// 方尖碑不处理
				if (dataKey == PlayerDataKey.ObeliskEntities) {
					continue;
				}
				// 从redis读取数据
				byte[] bytes = redisSession.hGetBytes("player_data:" + playerId, dataKey.name());
				// 反序列化
				Object data = PlayerDataSerializer.unserializeData(dataKey, bytes, false);
				if (data == null) {
					continue;
				}
				if (!dataKey.listMode()) {
					HawkDBEntity entity = (HawkDBEntity) data;
					if (dataKey == PlayerDataKey.PlayerEntity) {
						PlayerEntity playerEntity = (PlayerEntity) data;
						playerEntity.setServerId(serverId);
						puid = playerEntity.getPuid();
						GlobalData.getInstance().updateAccountInfo(playerEntity.getPuid(), serverId, playerId, 0, playerEntity.getName());
					}
					
					if (dataKey == PlayerDataKey.PlayerBaseEntity) {
						PlayerBaseEntity playerBaseEntity = (PlayerBaseEntity) data;
						playerBaseEntity.setSaveAmt(0);
						playerBaseEntity._setChargeAmt(0);
					}
					entity.setPersistable(true);
					entity.create();
				} else {
					@SuppressWarnings("unchecked")
					List<HawkDBEntity> entityList = (List<HawkDBEntity>) data;
					for (HawkDBEntity entity : entityList) {
						try {
							entity.setPersistable(true);
							entity.create();
						} catch (Exception e) {
							HawkException.catchException(e);
						}
					}
				}
			} catch (Exception e) {
				HawkException.catchException(e);				
			}
		}
		// 反序列化活动数据
		immgrationActivityData(playerId, redisSession);
		// 添加进迁入玩家列表
		GlobalData.getInstance().addImmgrationInPlayerIds(playerId);
		
		// 再清一遍玩家缓存
		clearPlayerCache(playerId);
		
		return puid;
	}
	
	/**
	 * 反序列化活动数据
	 * @param playerId
	 */
	private void immgrationActivityData(String playerId, HawkRedisSession redisSession) {
		String key = "player_data_activity:" + playerId;
		Map<byte[], byte[]> infos = redisSession.hGetAllBytes(key.getBytes());
		if (infos == null || infos.isEmpty()) {
			return;
		}
		for (Entry<byte[], byte[]> info : infos.entrySet()) {
			try {
				// 活动ID
				String activityId = new String(info.getKey());
				// 数据
				byte[] data = info.getValue();
				
				// 反序列化存储
				com.hawk.activity.type.ActivityType type = com.hawk.activity.type.ActivityType.getType(Integer.parseInt(activityId));
				Class<? extends HawkDBEntity> clz = type.getDbEntity();
				if (clz == null) {
					logger.info("immgrationActivityData error, clz not exist, playerId:{}, activityId:{}, data:{}, type:{}", playerId, activityId, data, type.name());
					continue;
				}
				HawkDBEntity entity = clz.newInstance();
				entity.setPersistable(true);
				if (!entity.parseFrom(data)) {
					logger.info("immgrationActivityData error, parse error, playerId:{}, activityId:{}, data:{}, type:{}", playerId, activityId, data, type.name());
					continue;
				}			
				entity.afterRead();
				entity.create();
				logger.info("immgration activity data success, playerId:{}, activityId:{}, data:{}, type:{}", playerId, activityId, data, type.name());
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
	}
	
	/**
	 * 更换盟主
	 */
	public void changeGuildLeader() {
		
		logger.info("start changeGuildLeader...");
		
		String serverId = GsConfig.getInstance().getServerId();
		if (!serverId.equals("20072")) {
			logger.info("changeGuildLeader serverId error, serverId:{}", serverId);
			return;
		}
		
		String beforePlayerId = "fhk-in596-q";
		Player beforePlayer = GlobalData.getInstance().makesurePlayer(beforePlayerId);
		if (beforePlayer == null) {
			logger.info("changeGuildLeader beforePlayer error");
			return;
		}
		
		String afterPlayerId = "fhk-inpg2-3z";
		Player afterPlayer = GlobalData.getInstance().makesurePlayer(afterPlayerId);
		if (afterPlayer == null) {
			logger.info("changeGuildLeader afterPlayer error");
			return;
		}
		
		// 检测两个玩家的联盟
		String beforePlayerGuildId = GuildService.getInstance().getPlayerGuildId(beforePlayerId);
		String afterPlayerGuildId = GuildService.getInstance().getPlayerGuildId(afterPlayerId);
		if (HawkOSOperator.isEmptyString(beforePlayerGuildId) || HawkOSOperator.isEmptyString(afterPlayerGuildId) || !beforePlayerGuildId.equals(afterPlayerGuildId)) {
			logger.info("changeGuildLeader guildId error, beforePlayerGuildId:{}, afterPlayerGuildId:{}", beforePlayerGuildId, afterPlayerGuildId);
			return;
		}
		
		// 权限检测
		if (!GuildService.getInstance().checkGuildAuthority(beforePlayerId, AuthId.ALLIANCE_LEADERSHIP_CHANGE)) {
			logger.info("changeGuildLeader GuildService error");
			return;
		}
		
		// 跨服活动开启期间不能进行部分联盟操作
		if (CrossActivityService.getInstance().isOpen()) {
			logger.info("changeGuildLeader CrossActivityService error");
			return;
		}

		// 国王盟主不可让位
		if (PresidentFightService.getInstance().isProvisionalPresident(beforePlayerId)) {
			logger.info("changeGuildLeader PresidentFightService error");
			return;
		}

		// 霸主和统帅不能转让盟主
		if (StarWarsOfficerService.getInstance().isKing(beforePlayerId)) {
			logger.info("changeGuildLeader StarWarsOfficerService error");
			return;
		}
		
		// 转让盟主
		GuildService.getInstance().dealMsg(MsgId.GUILD_DEMISE_LEADER, new GuildDemiseLeaderInvoker(beforePlayer, afterPlayerId, HP.code.GUILDMANAGER_DEMISELEADER_C_VALUE));
	}
	
	private void fixXXXXPlayer() {
		try {
			String playerId = "831-1d8rx3-11";
			Player player = GlobalData.getInstance().makesurePlayer(playerId);
			if (player == null) {
				HawkLog.logPrintln("fixXXXXPlayer", "playerId : {} not found!!!!", playerId);
				return;
			}
			CommanderEntity commanderEntity = player.getData().getCommanderEntity();
			commanderEntity.setEquipInfo(
					"[\"{\\\"equipId\\\":\\\"\\\",\\\"pos\\\":1}\",\"{\\\"equipId\\\":\\\"\\\",\\\"pos\\\":2}\",\"{\\\"equipId\\\":\\\"\\\",\\\"pos\\\":3}\",\"{\\\"equipId\\\":\\\"\\\",\\\"pos\\\":4}\",\"{\\\"equipId\\\":\\\"\\\",\\\"pos\\\":5}\",\"{\\\"equipId\\\":\\\"\\\",\\\"pos\\\":6}\",\"{\\\"equipId\\\":\\\"\\\",\\\"pos\\\":7}\",\"{\\\"equipId\\\":\\\"\\\",\\\"pos\\\":8}\"]");
			Field starExplore = HawkOSOperator.getClassField(commanderEntity, "starExplore");
			starExplore.set(commanderEntity,
					"[{\"starId\":1,\"progress\":[{\"progressVal\":50,\"protressId\":1},{\"progressVal\":25,\"protressId\":2},{\"progressVal\":25,\"protressId\":3}]},{\"starId\":2,\"progress\":[{\"progressVal\":50,\"protressId\":1},{\"progressVal\":25,\"protressId\":2},{\"progressVal\":25,\"protressId\":3}]},{\"starId\":3,\"progress\":[{\"progressVal\":25,\"protressId\":1},{\"progressVal\":25,\"protressId\":2},{\"progressVal\":50,\"protressId\":3}]},{\"starId\":4,\"progress\":[{\"progressVal\":50,\"protressId\":1},{\"progressVal\":25,\"protressId\":2},{\"progressVal\":25,\"protressId\":3}]},{\"starId\":5,\"progress\":[{\"progressVal\":25,\"protressId\":1},{\"progressVal\":50,\"protressId\":2},{\"progressVal\":25,\"protressId\":3}]},{\"starId\":6,\"progress\":[{\"progressVal\":50,\"protressId\":1},{\"progressVal\":25,\"protressId\":2},{\"progressVal\":25,\"protressId\":3}]},{\"starId\":7,\"progress\":[{\"progressVal\":50,\"protressId\":1},{\"progressVal\":25,\"protressId\":2},{\"progressVal\":25,\"protressId\":3}]}]");

			Field superSoldierSkin = HawkOSOperator.getClassField(commanderEntity, "superSoldierSkin");
			superSoldierSkin.set(commanderEntity,"20001_20002_20004_20006_10010");
	
			Field starExploreCollect = HawkOSOperator.getClassField(commanderEntity, "starExploreCollect");
			starExploreCollect.set(commanderEntity,"[{\"fixAttr\":[{\"attrId\":11028,\"attrVal\":7000}],\"tmpAttr\":[],\"collectId\":1,\"upCount\":6000,\"randomAttr\":[{\"attrId\":11031,\"attrVal\":500}]},{\"fixAttr\":[{\"attrId\":11029,\"attrVal\":7000}],\"tmpAttr\":[],\"collectId\":2,\"upCount\":6000,\"randomAttr\":[{\"attrId\":11032,\"attrVal\":500}]},{\"fixAttr\":[{\"attrId\":11035,\"attrVal\":13000}],\"tmpAttr\":[{\"attrId\":11034,\"attrVal\":1000}],\"collectId\":3,\"upCount\":12000,\"randomAttr\":[{\"attrId\":11034,\"attrVal\":1000}]}]");
			
			Field fgylData = HawkOSOperator.getClassField(commanderEntity, "fgylData");
			fgylData.set(commanderEntity,"{\"rewardTerm\":13}");
			
			commanderEntity.afterRead();
			
			NationMilitaryEntity nationMilitaryEntity = player.getData().getNationMilitaryEntity();
			nationMilitaryEntity.setNationMilitaryExp(9483559);
			nationMilitaryEntity.setNationMilitaryRankTerm(80);
			nationMilitaryEntity.setNationMilitaryRewardDay(205);
			nationMilitaryEntity.setNationMilitaryReward(503);
			nationMilitaryEntity.setNationMilitarLlevel(503);
			
			
			List<MechaCoreEntity> entities = HawkDBManager.getInstance().query("from MechaCoreEntity where playerId = ? and invalid = 0", playerId);
			if(entities.size() == 2){
				for(MechaCoreEntity objData: entities){
					if(objData.getId().equals("7pu-3w6g68-1")){
						player.getData().getDataCache().update(PlayerDataKey.MechaCoreEntity, objData);
					}
					if(objData.getId().equals("7pu-418qek-7")){
						objData.delete();
					}
				}
			}
			
			player.kickout(Status.Error.MIGRATE_FINISH_VALUE, true, "");
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 删除装扮
	 */
	private void delDress(Map<String, String> params) {
		String playerIdParam = params.get("playerId");
		if (!HawkOSOperator.isEmptyString(playerIdParam)) {
			try {
				String serverId = params.get("serverId");
				int itemId = Integer.parseInt(params.get("itemId"));
				int dressType = Integer.parseInt(params.get("dressType"));
				int modelType = Integer.parseInt(params.get("modelType"));
				int delTime = Integer.parseInt(params.get("delTime"));
				delDress(playerIdParam, serverId, itemId, dressType, modelType, delTime);
			} catch (Exception e) {
				HawkLog.logPrintln("sysop del dress exception, playerId: {}", playerIdParam);
				HawkException.catchException(e);
			}
			return;
		}
		
		try {
			List<String> lines = new ArrayList<>();
			HawkOSOperator.readTextFileLines("tmp/20250718_delDress.txt", lines);
			for (String line : lines) {
				try {
					String[] strs = line.split(";");
					String playerId = strs[0];
					String serverId = strs[1];
					int itemId = Integer.parseInt(strs[2]);
					int dressType = Integer.parseInt(strs[3]);
					int modelType = Integer.parseInt(strs[4]);
					int delTime = Integer.parseInt(strs[5]);
					delDress(playerId, serverId, itemId, dressType, modelType, delTime);
				} catch (Exception e) {
					HawkLog.logPrintln("sysop del dress exception, info: {}", line);
					HawkException.catchException(e);
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 删装扮
	 * @param playerId
	 * @param serverId
	 * @param itemId
	 */
	private void delDress(String playerId, String serverId, int itemId, int dressType, int modelType, int delTime) {
		try {
			String mainServerId = GlobalData.getInstance().getMainServerId(serverId);
			if (!mainServerId.equals(GsConfig.getInstance().getServerId())) {
				return;
			}
			Player player = GlobalData.getInstance().scriptMakesurePlayer(playerId);
			if (player == null) {
				return;
			}
			
			HawkThreadPool threadPool = HawkTaskManager.getInstance().getTaskExecutor();
			int threadIdx = Math.abs(player.getXid().hashCode() % threadPool.getThreadNum());
	 		HawkTaskManager.getInstance().postExtraTask(new HawkTask() {
				@Override
				public Object run() {
					//先扣道具,道具不足扣装扮
					ConsumeItems consum = ConsumeItems.valueOf();
					consum.addItemConsume(itemId, 1);
					if (consum.checkConsume(player)) {
						consum.consumeAndPush(player, Action.NULL);
						HawkLog.logPrintln("sysop update dress del model item, playerId: {}, serverId: {}, itemId: {}",  playerId, serverId, itemId);
						return null;
					}
					
					if (dressType > 0 && modelType > 0 && delTime > 0) {
						DressEntity entity = player.getData().getDressEntity();
						DressItem dressInfo = entity.getDressInfo(dressType, modelType);
						if (dressInfo != null) {
							long delTimeLong = delTime * 1000L;
							long beforeShowEnd = dressInfo.getShowEndTime();
							long beforeContinue = dressInfo.getContinueTime();
							dressInfo.setShowEndTime(Math.max(0, beforeShowEnd - delTimeLong));
							dressInfo.setContinueTime(Math.max(0, beforeContinue - delTimeLong));
							entity.notifyUpdate();
							if (player.isActiveOnline()) {
								player.getPush().syncDressInfo();
							}
							HawkLog.logPrintln("sysop update dress model, playerId: {}, serverId: {}, dressType: {}, modelType: {}, delTime: {}",  playerId, serverId, dressType, modelType, delTime);
						} else {
							HawkLog.logPrintln("sysop update dress model error, playerId: {}, serverId: {}, dressType: {}, modelType: {}, delTime: {}",  playerId, serverId, dressType, modelType, delTime);
						}
					} else {
						HawkLog.logPrintln("sysop update dress model param error, playerId: {}, serverId: {}, dressType: {}, modelType: {}, delTime: {}",  playerId, serverId, dressType, modelType, delTime);
					}
					return null;
				}
	 		}, threadIdx);
	 		
		} catch (Exception e) {
			HawkLog.logPrintln("sysop update dress model exception, playerId: {}, serverId: {}, dressType: {}, modelType: {}, delTime: {}",  playerId, serverId, dressType, modelType, delTime);
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 指定星海匹配
	 * @param params
	 */
	private void addXhjzMatch(Map<String, String> params){
		String matchTuple = params.get("matchTuple").trim();
		RedisProxy.getInstance().getRedisSession().setString("xhjz_gm_match_20250626", matchTuple);
		HawkLog.logPrintln("addXhjzMatch-sucess:{}:{}", GsConfig.getInstance().getServerId(),matchTuple);
	}
	
	//发放泰伯联赛星海赛季段位积分
	private void sendSeasonExp(){
		int day = HawkTime.getYyyyMMddIntVal();
		String sid = GsConfig.getInstance().getServerId();
		String redisKey = "sendSeasonExp-" + sid + "-"+ day;
		HawkRedisSession redisSession = RedisProxy.getInstance().getRedisSession();
		boolean rlt = redisSession.setNx(redisKey, redisKey);
		if(!rlt){
			HawkLog.logPrintln("sendSeasonExp fix already -> {}", GsConfig.getInstance().getServerId());
			return;
		}
		
		List<String> lines = new ArrayList<>();
		try {
			HawkOSOperator.readTextFileLines("tmp/20250622_season_exp.txt", lines);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		String local = GsConfig.getInstance().getServerId();
		for (String line : lines) {
			try {
				String[] arr = line.split(",");
				if(arr.length != 3){
					continue;
				}
				String serverId = arr[0];
				String guildId  = arr[1];
				int rank  =Integer.parseInt(arr[2]);
				if(!local.equals(serverId)){
					continue;
				}
				GuildInfoObject guildObj = GuildService.getInstance().getGuildInfoObject(guildId);
				if(Objects.isNull(guildObj)){
					continue;
				}
	            Optional<SeasonActivity> opActivity = ActivityManager.getInstance().getGameActivityByType(com.hawk.game.protocol.Activity.ActivityType.SEASON_ACTIVITY_VALUE);
	            if (opActivity.isPresent()) {
	            	SeasonActivity activity = opActivity.get();
	            	activity.addGuildGradeExpFromMatchRank(Activity.SeasonMatchType.S_TBLY, guildId, rank);
	            }
				HawkLog.logPrintln("sendSeasonExp-sucess:{}:{}:{}", GsConfig.getInstance().getServerId(),guildId,rank);
			}catch (Exception e) {
				HawkException.catchException(e);
			}
		}
	}
	
	private void fixXhjzSeasonTeamDataTow(){
		List<String> lines = new ArrayList<>();
		try {
			HawkOSOperator.readTextFileLines("tmp/20250620_xhjz_season.txt", lines);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		Map<String, CMWData> oldMap = XHJZSeasonManager.getInstance().loadJoinDatas(false);
		Map<String, CMWData> newMap = XHJZSeasonManager.getInstance().loadJoinDatas(true);
		for (String line : lines) {
			try {
				String[] args = line.split(",");
				if (args.length != 3) {
					continue;
				}
				String teamId = args[0];
				int winCnt = Integer.parseInt(args[1]);
				int loseCnt = Integer.parseInt(args[2]);
				if(true){
					CMWData oldData = oldMap.get(teamId);
					if(Objects.nonNull(oldData)){
						if(oldData.rankingWinCnt != winCnt || oldData.rankingLoseCnt != loseCnt){
							oldData.rankingWinCnt = winCnt;
							oldData.rankingLoseCnt = loseCnt;
							HawkLog.logPrintln("fixXhjzSeasonTeamData-old-{},{},{}",teamId,oldData.rankingWinCnt,oldData.rankingLoseCnt);
							RedisProxy.getInstance().getRedisSession().hSet(XHJZSeasonManager.getInstance().getDataKey(), oldData.teamId, oldData.serialize());
						}
					}
				}
				if(true){
					CMWData newData = newMap.get(teamId);
					if(Objects.nonNull(newData)){
						if(newData.rankingWinCnt != winCnt || newData.rankingLoseCnt != loseCnt){
							newData.rankingWinCnt = winCnt;
							newData.rankingLoseCnt = loseCnt;
							HawkLog.logPrintln("fixXhjzSeasonTeamData-new-win-{},{},{}",teamId,newData.rankingWinCnt,newData.rankingLoseCnt);
							RedisProxy.getInstance().getRedisSession().hSet(XHJZSeasonManager.getInstance().getDataKey(), newData.teamId, newData.serialize());
						}
					}
				}
			} catch (Exception e) {
				HawkLog.logPrintln("fixXhjzSeasonTeamData-err-{}",line);
				HawkException.catchException(e);
			}
		}
	}
	
	//发放联盟旗帜
	private void sendGuildFlag(){
		List<String> lines = new ArrayList<>();
		try {
			HawkOSOperator.readTextFileLines("tmp/20250617_guild_flag.txt", lines);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		String local = GsConfig.getInstance().getServerId();
		for (String line : lines) {
			try {
				String[] arr = line.split(",");
				if(arr.length != 3){
					continue;
				}
				String serverId = arr[0];
				String guildId  = arr[1];
				String flagId  = arr[2];
				if(!local.equals(serverId)){
					continue;
				}
				GuildService.getInstance().addRewardFlag(guildId, Integer.parseInt(flagId));
				HawkLog.logPrintln("sendGuildFlag-sucess:{}:{}:{}", GsConfig.getInstance().getServerId(),guildId,flagId);
			}catch (Exception e) {
				HawkException.catchException(e);
			}
		}
	}
	
	/**
	 * 修复使命战争
	 * @param params
	 */
	private void fixObliskData(Map<String, String> params){
		String serverId = GsConfig.getInstance().getServerId();
		String fixServer = params.get("fixServer");
		if(!serverId.equals(fixServer)){
			return;
		}
		String OBELISK_INIT_RECORD = "obelisk:init_record";
		RedisProxy.getInstance().getRedisSession().hDel(OBELISK_INIT_RECORD, serverId);
		ObeliskService.getInstance().init();
	}
	
	private void reloadXhjzSeasonTeamData(){
		XHJZSeasonManager.getInstance().loadAllJoinDatas();
		XHJZSeasonManager.getInstance().loadRankingRank(false);
		XHJZSeasonManager.getInstance().loadRankingRank(true);
	}
	
	private void fixXhjzSeasonTeamData(Map<String, String> params){
		HawkLog.logPrintln("fixXhjzSeanTeamData start -> {}", GsConfig.getInstance().getServerId());
		String fixKey = params.get("key");
		if(HawkOSOperator.isEmptyString(fixKey) ){
			HawkLog.logPrintln("fixXhjzSeanTeamData key null -> {}", GsConfig.getInstance().getServerId());
			return;
		}
		String battleCountStr = params.get("battleCount");
		if(HawkOSOperator.isEmptyString(battleCountStr) ){
			HawkLog.logPrintln("fixXhjzSeanTeamData battleCountStr null -> {}", GsConfig.getInstance().getServerId());
			return;
		}
		String fixServer = params.get("fixServer");
		if(HawkOSOperator.isEmptyString(fixServer) ){
			HawkLog.logPrintln("fixXhjzSeanTeamData fixServer null -> {}", GsConfig.getInstance().getServerId());
			return;
		}
		if(!fixServer.equals(GsConfig.getInstance().getServerId())){
			HawkLog.logPrintln("fixXhjzSeanTeamData fixServer not this -> {}", GsConfig.getInstance().getServerId());
			return;
		}
		int battleCount = Integer.parseInt(battleCountStr);
		String redisKey = "fixXhjzSeasonTeamData-" + fixKey;
		HawkRedisSession redisSession = RedisProxy.getInstance().getRedisSession();
		boolean rlt = redisSession.setNx(redisKey, GsConfig.getInstance().getServerId());
		if(!rlt){
			HawkLog.logPrintln("fixXhjzSeanTeamData fix already -> {}", GsConfig.getInstance().getServerId());
			return;
		}
	
		Map<String, CMWData> oldMap = XHJZSeasonManager.getInstance().loadJoinDatas(false);
		Map<String, CMWData> newMap = XHJZSeasonManager.getInstance().loadJoinDatas(true);
		
		//修复老服数据 
		for(CMWData oldData : oldMap.values()){
			try {
				String json = RedisProxy.getInstance().getRedisSession().hGet(XHJZSeasonManager.getInstance().getDataKey(), oldData.teamId);
				CMWData redisData = CMWData.unSerialize(json);
				int count = redisData.rankingLoseCnt + redisData.rankingWinCnt;
				if(count >= battleCount){
					continue;
				}
				String teamStr = RedisProxy.getInstance().getRedisSession().hGet(XHJZRedisKey.XHJZ_WAR_TEAM, redisData.teamId); 
				XHJZWarTeamData teamData = XHJZWarTeamData.unSerialize(teamStr);
				if(Objects.isNull(teamData)){
					//已经解散的就不管了
					HawkLog.logPrintln("fixXhjzSeanTeamData-teamData-non,old, {}",redisData.serialize());
					continue;
				}
				HawkLog.logPrintln("fixXhjzSeanTeamData-teamData-begin,old, {}",redisData.serialize());
				int add = battleCount - count;
				redisData.rankingWinCnt += add;
				RedisProxy.getInstance().getRedisSession().hSet(XHJZSeasonManager.getInstance().getDataKey(), redisData.teamId, redisData.serialize());
				HawkLog.logPrintln("fixXhjzSeanTeamData-teamData-over,old, {}",redisData.serialize());
			} catch (Exception e) {
				HawkException.catchException(e);
				HawkLog.logPrintln("fixXhjzSeanTeamData-teamData-err,old, {}",oldData.serialize());
			}
		}
		//修复新服数据
		for(CMWData data : newMap.values()){
			try {
				int count = data.rankingLoseCnt + data.rankingWinCnt;
				if(count >= battleCount){
					continue;
				}
				String teamStr = RedisProxy.getInstance().getRedisSession().hGet(XHJZRedisKey.XHJZ_WAR_TEAM, data.teamId);
				XHJZWarTeamData teamData = XHJZWarTeamData.unSerialize(teamStr);
				if(Objects.isNull(teamData)){
					//已经解散的就不管了
					HawkLog.logPrintln("fixXhjzSeanTeamData-teamData-non,new, {}",data.serialize());
					continue;
				}
				HawkLog.logPrintln("fixXhjzSeanTeamData-teamData-begin,new, {}",data.serialize());
				int add = battleCount - count;
				data.rankingWinCnt += add;
				RedisProxy.getInstance().getRedisSession().hSet(XHJZSeasonManager.getInstance().getDataKey(), data.teamId, data.serialize());
				HawkLog.logPrintln("fixXhjzSeanTeamData-teamData-over,new, {}",data.serialize());
			} catch (Exception e) {
				HawkException.catchException(e);
				HawkLog.logPrintln("fixXhjzSeanTeamData-teamData-err,new, {}",data.serialize());
			}
		}
	}
	
	public void clear0711Mail() {
		HawkTaskManager.getInstance().postExtraTask(new HawkTask() {
			@Override
			public Object run() {
				List<String> lines = new ArrayList<>();
				try {
					HawkOSOperator.readTextFileLines("tmp/mailid.txt", lines);
					for (String mailUUid : lines) {
						try {
							clearMailBaiId(mailUUid);
						} catch (Exception e) {
							HawkException.catchException(e);
						}
					}
				} catch (Exception e) {
					HawkException.catchException(e);
				}
				return null;
			}
		});
	}
	
	public void clearMailBaiId(String mailUUid) {
		try (Jedis jedis = RedisProxy.getInstance().getRedisSession().getJedis()) { // 删除活动邮件
			MailLiteInfo.Builder mail = MailService.getInstance().getMailEntity(mailUUid);
			if (mail == null) {
				return;
			}
			String playerId = mail.getPlayerId();
			int type = MailService.getInstance().getMailType(MailId.BEST_PRIZE_END_CLEAR);
			Set<String> toDelMailids = new HashSet<>();
			toDelMailids.add(mail.getId());
			
			if (!toDelMailids.isEmpty()) {
				// 所有的邮件Id
				String keyUnread = MailService.getInstance().keyUnread(playerId, type);
				String keySort = MailService.getInstance().keySort(playerId, type);
				// 删未读
				jedis.zrem(keyUnread, toDelMailids.toArray(new String[0]));
				// 删排序
				jedis.zrem(keySort, toDelMailids.toArray(new String[0]));
				// 所有的邮件Id
				// 删entity
				jedis.del(toDelMailids.stream().map(MailService.getInstance()::keyEntity).toArray(byte[][]::new));
				// 删content
				jedis.del(toDelMailids.stream().map(MailService.getInstance()::keyContent).toArray(byte[][]::new));

				String reward = mail.getReward();
				boolean hasGet = !mail.getHasReward();
				HawkLog.logPrintln("sysop Clear0711Mail, playerId: {}, hasGet: {}, reward: {}", playerId, hasGet, reward);
				DungeonRedisLog.log("Clear0711Mail", "playerId:{}, hasGet:{}  reward:{}", playerId, hasGet, reward);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void fixCollegeMembers(String collegeId,String memberId){
		try {
			Field field = CollegeService.class.getDeclaredField("collegeMembers");
			field.setAccessible(true);
			Map<String, Set<String>> members = (Map<String, Set<String>>) field.get(CollegeService.getInstance());
			Set<String> set = members.get(collegeId);
			if(Objects.isNull(set)){
				HawkLog.logPrintln("fixCollegeMembers memberSet null {}", collegeId);
				return;
			}
			if(set.contains(memberId)){
				set.remove(memberId);
				HawkLog.logPrintln("fixCollegeMembers remove member {}", memberId);
			}
			HawkLog.logPrintln("fixCollegeMembers action over {},{}", collegeId,memberId);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
	}
	private void fixPlayerMonthCardBuff(String data){
		Map<String,Long> dataMap = new HashMap<>();
		if(HawkOSOperator.isEmptyString(data)){
			String curServer = GsConfig.getInstance().getServerId();
			String fixKey = "fixPlayerMonthCardBuff202504101656:"+curServer;
			String fixRlt = RedisProxy.getInstance().getRedisSession().getString(fixKey);
			long curTime = HawkTime.getMillisecond();
			if(!HawkOSOperator.isEmptyString(fixRlt)){
				long fixTime = Long.parseLong(fixRlt);
				if(curTime - fixTime < HawkTime.HOUR_MILLI_SECONDS){
					HawkLog.logPrintln("fixPlayerMonthCardBuff err time is err {}", fixRlt);
					return;
				}
			}
			RedisProxy.getInstance().getRedisSession().setString(fixKey, String.valueOf(curTime));
			try {
				List<String> lines = new ArrayList<>();
				HawkOSOperator.readTextFileLines("tmp/fixPlayerMonthCardBuff.txt", lines);
				for(String line : lines){
					String[] arr = line.split(",");
					String serverId = arr[0];
					String pid = arr[1];
					long endTime = Long.parseLong(arr[2]) * 1000;
					boolean localServer = GlobalData.getInstance().isLocalServer(serverId);
					if(localServer){
						dataMap.put(pid, endTime);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else{
			String[] arr = data.split(",");
			for(String str : arr){
				String[] pArr = str.split("_");
				String pid = pArr[0];
				long endTime = Long.parseLong(pArr[1]) * 1000;
				dataMap.put(pid, endTime);
			}
		}
		for(Map.Entry<String, Long> entry : dataMap.entrySet()){
			try {
				String playeId =  entry.getKey();
				long endTime = entry.getValue();
				Player player = GlobalData.getInstance().makesurePlayer(playeId);
				if(Objects.isNull(player)){
					HawkLog.logPrintln("fixPlayerMonthCardBuff playerId is null {}", playeId);
					continue;
				}
				player.addStatusBuff(23142, endTime);
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
	}
	
	private void fixPlayerItemBuff(String players){
		List<String> playerIds = new ArrayList<>();
		if(HawkOSOperator.isEmptyString(players)){
			String curServer = GsConfig.getInstance().getServerId();
			String fixKey = "fixPlayerItemBuff202504101656:"+curServer;
			String fixRlt = RedisProxy.getInstance().getRedisSession().getString(fixKey);
			long curTime = HawkTime.getMillisecond();
			if(!HawkOSOperator.isEmptyString(fixRlt)){
				long fixTime = Long.parseLong(fixRlt);
				if(curTime - fixTime < HawkTime.HOUR_MILLI_SECONDS){
					HawkLog.logPrintln("stopItemBuff err time is err {}", fixRlt);
					return;
				}
			}
			RedisProxy.getInstance().getRedisSession().setString(fixKey, String.valueOf(curTime));
			try {
				List<String> lines = new ArrayList<>();
				HawkOSOperator.readTextFileLines("tmp/fixPlayerItemBuff.txt", lines);
				for(String line : lines){
					String[] arr = line.split(",");
					String serverId = arr[0];
					String pid = arr[1];
					boolean localServer = GlobalData.getInstance().isLocalServer(serverId);
					if(localServer){
						playerIds.add(pid.trim());
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else{
			String[] arr = players.split(",");
			for(String str : arr){
				playerIds.add(str.trim());
			}
		}
		
		long buffEndTime = HawkTime.getMillisecond();
		for(String pid : playerIds){
			try {
				Player player = GlobalData.getInstance().makesurePlayer(pid);
				if(Objects.isNull(player)){
					HawkLog.logPrintln("stopItemBuff playerId is null {}", pid);
					continue;
				}
				StatusDataEntity addStatusBuff = player.getData().addStatusBuff(102, buffEndTime);
				if (addStatusBuff != null) {
					player.getPush().syncPlayerStatusInfo(false, addStatusBuff);
					HawkLog.logPrintln("setItemBuffEndTime buff, playerId: {}, buffId: {}, endTime: {}", player.getId(), 102, addStatusBuff.getEndTime());

				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
	}
	
	private void xiufu1108(){
		List<String> playerIdList = new ArrayList<>();
		try {
			// openid server platId
			HawkOSOperator.readTextFileLines("tmp/fix1108.txt", playerIdList);
		} catch (Exception e) {
			HawkException.catchException(e);
			return;
		}
		
		for(String playerId: playerIdList){
			fixPlayer1108(playerId);
		}
	}

	private void fixPlayer1108(String playerId) {
		try {
			Player player = GlobalData.getInstance().makesurePlayer(playerId);
			if(Objects.isNull(player)){
				DungeonRedisLog.log("xiufu1108", "playerId is null  {}", playerId);
				return;
			}
			int heroId = 1108;
			int heroitemId = 1000000 + heroId;
			PlayerHero hero = player.getHeroByCfgId(heroId).orElse(null);
			if(Objects.isNull(hero)){
				DungeonRedisLog.log("xiufu1108", "playerId : {} can not find hero 1108", playerId);
				return;
			}
			AwardItems awardItem =  AwardItems.valueOf();
			final int exp = hero.getHeroEntity().getExp();
			final int EXP30000 = 1510005; // 30000 经验道具
			int backcount = (int) Math.ceil(exp / 30000D);
			awardItem.addItem(ItemType.TOOL_VALUE, EXP30000, backcount);
			hero.getHeroEntity().setExp(0);
			
			HeroStarLevelCfg starLevelCfg = HawkConfigManager.getInstance().getCombineConfig(HeroStarLevelCfg.class, heroId, hero.getStar(), hero.getStep());
			ConfigIterator<HeroStarLevelCfg> slit = HawkConfigManager.getInstance().getConfigIterator(HeroStarLevelCfg.class);
			for (HeroStarLevelCfg st : slit) {
				if (st.getHeroId() != heroId) {
					continue;
				}
				if (st.getId() >= starLevelCfg.getId()) {
					continue;
				}
				ItemInfo item = new ItemInfo(st.getPiecesForNextLevel());
				item.setItemId(1000005);
				awardItem.addItem(item);
			}
			
			hero.getHeroEntity().setStar(1);
			hero.getHeroEntity().setStep(0);
			
			for(SkillSlot slot : hero.getSkillSlots()){
				onRemoveSkill(awardItem,hero, slot);
			}
			
			onRemoveSoul(awardItem, hero);
			
			hero.notifyChange();
			
			int pcnt = player.getData().getItemNumByItemId(heroitemId);
			if (pcnt > 0) {
				ConsumeItems consumeItems = ConsumeItems.valueOf();
				consumeItems.addItemConsume(heroitemId, pcnt);
				if (consumeItems.checkConsume(player, 0)) {
					consumeItems.consumeAndPush(player, Action.NULL);
					awardItem.addItem(ItemType.TOOL_VALUE, 1000005, pcnt);
				}
			}
			
			
			if (!awardItem.getAwardItems().isEmpty()) {
				SystemMailService.getInstance().sendMail(MailParames.newBuilder()
						.setPlayerId(player.getId())
						.setMailId(MailId.REWARD_MAIL)
						.setRewards(awardItem.getAwardItems())
						.setAwardStatus(MailRewardStatus.NOT_GET)
						.addContents("尊敬的指挥官，因艾莉克丝技能问题时出现了异常，后续虽修复了此异常问题，但是给您造成了不好的游戏体验，对此我们深表歉意。现根据您的需求针对您已获得的英雄进行回退，您在此英雄所消耗的所有养成道具均会进行对应的返还。以下是返还的道具详情，请您查收。给您造成的不便非常抱歉，感谢您对游戏的支持！")
						.addSubTitles("艾莉克丝荣耀英雄回退邮件")
						.build());
				
				DungeonRedisLog.log("xiufu1108", "playerId : items  {}", ItemInfo.toString(awardItem.getAwardItems()));
			}
			
		} catch (Exception e) {
			DungeonRedisLog.log("xiufu1108", "playerId : {} exception: {}", playerId,e);
		}
	}
	
	private void fixTBLYMatchResult(boolean isReal, int startTermId){
		if(isReal){
			for(int i = startTermId; i < TiberiumConstCfg.getInstance().getEliminationStartTermId(); i++){
				String roomKey = String.format(TBLYWarResidKey.TBLY_WAR_SEASON_ROOM, getSeason(), i);
				RedisProxy.getInstance().getRedisSession().del(roomKey);
				List<String> fileContents = new ArrayList<>();
				try {
					HawkOSOperator.readTextFileLines("tmp/fixTblyMatch.txt", fileContents);
				} catch (Exception e) {
					HawkException.catchException(e);
				}
				for(String serverId : fileContents){
					String roomserverKey = String.format(TBLYWarResidKey.TBLY_WAR_SEASON_ROOM_SERVER, getSeason(), i, serverId);
					RedisProxy.getInstance().getRedisSession().del(roomserverKey);
				}
			}
		}else {
			for(int i = 1; i < TiberiumConstCfg.getInstance().getEliminationStartTermId(); i++){
				String roomKey = String.format(TBLYWarResidKey.TBLY_WAR_SEASON_ROOM, getSeason(), i);
				HawkLog.logPrintln("TBLYSeasonService fix del roomKey:{}", roomKey);
				List<String> fileContents = new ArrayList<>();
				try {
					HawkOSOperator.readTextFileLines("tmp/fixTblyMatch.txt", fileContents);
				} catch (Exception e) {
					HawkException.catchException(e);
				}
				for(String serverId : fileContents){
					String roomserverKey = String.format(TBLYWarResidKey.TBLY_WAR_SEASON_ROOM_SERVER, getSeason(), i, serverId);
					HawkLog.logPrintln("TBLYSeasonService fix del roomserverKey:{}", roomserverKey);
				}
			}
		}
		for(int i = 1; i <= TiberiumConstCfg.getInstance().getTeamCnt(); i++){
			String groupKey = String.format(TBLYWarResidKey.TBLY_WAR_SEASON_GROUP, getSeason(), i);
			Set<String> groupTeamIds = RedisProxy.getInstance().getRedisSession().sMembers(groupKey);
			List<String> teamIds = new ArrayList<>(groupTeamIds);
			if(teamIds.isEmpty()){
				continue;
			}
			Map<Integer, List<HawkTuple2<String, String>>> schedule = generateScheduleNew(teamIds, TiberiumConstCfg.getInstance().getEliminationStartTermId() - 1);
			for(int termId : schedule.keySet()){
				if(isReal && termId < startTermId){
					continue;
				}
				List<HawkTuple2<String, String>> results = schedule.get(termId);
				createRooms(isReal, termId, results, TiberiumWar.TLWServer.TLW_OLD_SERVER, TiberiumWar.TLWGroup.TEAM_GROUP, TiberiumConst.TLWBattleType.TEAM_GROUP_BATTLE, i);
			}
		}
		for(int i = 1; i <= TiberiumConstCfg.getInstance().getTeamCnt(); i++){
			String groupKey = String.format(TBLYWarResidKey.TBLY_WAR_SEASON_GROUP, getSeason(), i);
			Set<String> groupTeamIds = RedisProxy.getInstance().getRedisSession().sMembers(groupKey);
			List<String> teamIds = new ArrayList<>(groupTeamIds);
			if(teamIds.isEmpty()){
				continue;
			}
			Map<Integer, List<HawkTuple2<String, String>>> schedule = generateScheduleNew(teamIds, TiberiumConstCfg.getInstance().getEliminationStartTermId() - 1);
			for(int termId : schedule.keySet()){
				if(isReal && termId < startTermId){
					continue;
				}
				List<HawkTuple2<String, String>> results = schedule.get(termId);
				createRooms(isReal, termId, results, TiberiumWar.TLWServer.TLW_NEW_SERVER, TiberiumWar.TLWGroup.TEAM_GROUP, TiberiumConst.TLWBattleType.TEAM_GROUP_BATTLE, i);
			}
		}
	}
	
	public void createRooms(boolean isReal, int termId, List<HawkTuple2<String, String>> results, TiberiumWar.TLWServer matchType, TiberiumWar.TLWGroup groupType, TiberiumConst.TLWBattleType battleType, int groupId){
		Map<String, Set<String>> serverIdToRoomIdMap = new HashMap<>();
		String roomKey = String.format(TBLYWarResidKey.TBLY_WAR_SEASON_ROOM, getSeason(), termId);
		Map<String, String> roomMap = new HashMap<>();
		TiberiumSeasonTimeCfg timeCfg = TBLYSeasonService.getInstance().getTimeCfgBySeasonAndTermId(getSeason(), termId);
		long warStartTime = timeCfg == null ? 0L : timeCfg.getWarStartTimeValue();
		for(HawkTuple2<String, String> result : results){
			try {
				GuildTeamData teamData1 = TBLYGuildTeamManager.getInstance().loadTeam(result.first);
				GuildTeamData teamData2 = TBLYGuildTeamManager.getInstance().loadTeam(result.second);
				if(teamData1 == null){
					HawkLog.logPrintln("TBLYSeasonService createRoom teamData1 is null, teamId:{}", result.first);
					continue;
				}
				if(teamData2 == null){
					HawkLog.logPrintln("TBLYSeasonService createRoom teamData2 is null, teamId:{}", result.second);
					continue;
				}
				GuildTeamRoomData roomData = createRoom(termId, teamData1, teamData2,serverIdToRoomIdMap);
				if(roomData == null){
					HawkLog.logPrintln("TBLYSeasonService createRoom roomData is null, teamId1:{},teamId2:{}", result.first, result.second);
					continue;
				}
				roomData.matchType = matchType.getNumber();
				roomData.groupType = groupType.getNumber();
				roomData.battleType = battleType.getValue();
				roomData.groupId = groupId;
				roomMap.put(roomData.id, roomData.serialize());
				if(isReal){
					LogUtil.logTimberiumLeaguaMatchInfo(roomData.id, roomData.roomServerId, getSeason(), termId, teamData1.id, teamData1.serverId, teamData2.id,
							teamData2.serverId, warStartTime, groupType.getNumber(), matchType.getNumber());
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		if(isReal){
			RedisProxy.getInstance().getRedisSession().hmSet(roomKey, roomMap, 0);
			for(String toServerId : serverIdToRoomIdMap.keySet()){
				Set<String> roomIds = serverIdToRoomIdMap.get(toServerId);
				RedisProxy.getInstance().getRedisSession().sAdd(String.format(TBLYWarResidKey.TBLY_WAR_SEASON_ROOM_SERVER, getSeason(), termId, toServerId), 0, roomIds.toArray(new String[roomIds.size()]));
			}
		}else {
			HawkLog.logPrintln("TBLYSeasonService fix roomKey:{}", roomKey);
			for(String roomStr : roomMap.values()){
				HawkLog.logPrintln("TBLYSeasonService fix roomStr:{}", roomStr);
			}
			for(String toServerId : serverIdToRoomIdMap.keySet()){
				String roomserverKey = String.format(TBLYWarResidKey.TBLY_WAR_SEASON_ROOM_SERVER, getSeason(), termId, toServerId);
				HawkLog.logPrintln("TBLYSeasonService fix roomserverKey:{}", roomserverKey);
				Set<String> roomIds = serverIdToRoomIdMap.get(toServerId);
				for(String roomid : roomIds){
					HawkLog.logPrintln("TBLYSeasonService fix roomid:{}", roomid);
				}
			}
		}
	}
	
	public int getSeason(){
		return 10;
	}
	
	public GuildTeamRoomData createRoom(int termId, GuildTeamData teamData1, GuildTeamData teamData2 , Map<String, Set<String>> serverIdToRoomIdMap){
		try {
			GuildTeamRoomData roomData = new GuildTeamRoomData(termId, 0, teamData1, teamData2);
			roomData.roomServerId = teamData1.serverId.compareTo(teamData2.serverId) < 0 ? teamData1.serverId : teamData2.serverId;
			updateRoomIdToServer(serverIdToRoomIdMap, teamData1.serverId, roomData.id);
			updateRoomIdToServer(serverIdToRoomIdMap, teamData2.serverId, roomData.id);
			updateRoomIdToServer(serverIdToRoomIdMap, roomData.roomServerId, roomData.id);
			return roomData;
		}catch (Exception e) {
			HawkException.catchException(e);
			return null;
		}
	}

	public Map<Integer, List<HawkTuple2<String, String>>> generateScheduleNew(List<String> teamIds, int days) {
		List<String> results = new ArrayList<>();
		try {
			HawkOSOperator.readTextFileLines("tmp/fixTblyMatchResult.txt", results);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		Map<Integer, List<HawkTuple2<String, String>>> schedule = new HashMap<>();
		for(String result : results){
			String [] resultData = result.split("\\|");
			List<HawkTuple2<String, String>> list = new ArrayList<>();
			for(String t : resultData[1].split(",")){
				String [] tt = t.split("_");
				HawkTuple2<String, String> ht = new HawkTuple2<>(teamIds.get(Integer.parseInt(tt[0]) - 1), teamIds.get(Integer.parseInt(tt[1]) - 1));
				list.add(ht);
			}
			schedule.put(Integer.parseInt(resultData[0]), list);
		}
		return schedule;
	}
	
	private void fixTBLYMergeTeamData(boolean isReal){
		List<String> mergeServers = new ArrayList<>();
		try {
			HawkOSOperator.readTextFileLines("tmp/fixTblyMergeTeam.txt", mergeServers);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		Map<String, String> sToMServer = new HashMap<>();
		for(String line : mergeServers){
			String [] arr = line.split(",");
			sToMServer.put(arr[1], arr[0]);
		}
//		Map<String, TBLYSeasonData> seasonDataMap = TBLYSeasonService.getInstance().l(TBLYWarResidKey.TBLY_WAR_SEASON_NEW_DATA);
//		HawkLog.logPrintln("fixTBLYMergeTeamData seasonDataMap size:{}", seasonDataMap.size());
//		List<GuildTeamData> teamDataList = TBLYGuildTeamManager.getInstance().loadTeams(seasonDataMap.keySet());
//		HawkLog.logPrintln("fixTBLYMergeTeamData teamDataList size:{}", teamDataList.size());
//		for(GuildTeamData teamData : teamDataList){
//			if(!sToMServer.containsKey(teamData.serverId)){
//				continue;
//			}
//			String mainServerId = sToMServer.get(teamData.serverId);
//			HawkLog.logPrintln("fixTBLYMergeTeamData fix teamData serverId teamId:{}, from:{}, to:{}", teamData.id, teamData.serverId, mainServerId);
//			if(isReal){
//				teamData.serverId = mainServerId;
//				TBLYGuildTeamManager.getInstance().updateTeam(teamData);
//			}
//		}

		Map<String, TBLYSeasonData> seasonDataMapOld = TBLYSeasonService.getInstance().loadSeasonTeamData();
		HawkLog.logPrintln("fixTBLYMergeTeamData seasonDataMapOld size:{}", seasonDataMapOld.size());
		List<GuildTeamData> teamDataListOld = TBLYGuildTeamManager.getInstance().loadTeams(seasonDataMapOld.keySet());
		HawkLog.logPrintln("fixTBLYMergeTeamData teamDataListOld size:{}", teamDataListOld.size());
		for(GuildTeamData teamData : teamDataListOld){
			if(!sToMServer.containsKey(teamData.serverId)){
				continue;
			}
			String mainServerId = sToMServer.get(teamData.serverId);
			HawkLog.logPrintln("fixTBLYMergeTeamData fix teamData serverId teamId:{}, from:{}, to:{}", teamData.id, teamData.serverId, mainServerId);
			if(isReal){
				teamData.serverId = mainServerId;
				TBLYGuildTeamManager.getInstance().updateTeam(teamData);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private void fixStrongestGuildRankData(){
		String curServer = GsConfig.getInstance().getServerId();
		String fixKey = "fixStrongestGuildRankData20250215101010:"+curServer+":"+10000;
		String fixRlt = RedisProxy.getInstance().getRedisSession().getString(fixKey);
		if(!HawkOSOperator.isEmptyString(fixRlt)){
			HawkLog.logPrintln("fixStrongestGuildRankData fix dup");
			return;
		}
		RedisProxy.getInstance().getRedisSession().setString(fixKey, fixKey);
		List<String> fileContents = new ArrayList<>();
		StrongestGuildTotalGuildRank guildRank = null;
		StrongestGuildPersonalTotalRank personalRank = null; 
		try {
			HawkOSOperator.readTextFileLines("tmp/strongestGuildRankScore.txt", fileContents);
			if(true){
				Field guildRankField = StrongestGuildActivity.class.getDeclaredField("guildRank");
				guildRankField.setAccessible(true);
				List<StrongestGuildRank> guildRanks = (List<StrongestGuildRank>) guildRankField.get(null);
				for(StrongestGuildRank rank : guildRanks){
					if(rank instanceof StrongestGuildTotalGuildRank){
						guildRank = (StrongestGuildTotalGuildRank) rank;
					}
				}
			}
			if(true){
				Field personRankField = StrongestGuildActivity.class.getDeclaredField("personRank");
				personRankField.setAccessible(true);
				List<StrongestGuildRank> personRanks = (List<StrongestGuildRank>) personRankField.get(null);
				for(StrongestGuildRank rank : personRanks){
					if(rank instanceof StrongestGuildPersonalTotalRank){
						personalRank = (StrongestGuildPersonalTotalRank) rank;
					}
				}
			}
			
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		if(Objects.isNull(guildRank)){
			HawkLog.logPrintln("fixStrongestGuildRankData guildRank object null");
			return;
		}
		if(Objects.isNull(personalRank)){
			HawkLog.logPrintln("fixStrongestGuildRankData personalRank object null");
			return;
		}
		//阶段5配置
		StrongestGuildCfg circularCfg400 = HawkConfigManager.getInstance().getConfigByKey(StrongestGuildCfg.class, 400);
		//阶段6配置
		StrongestGuildCfg circularCfg500 = HawkConfigManager.getInstance().getConfigByKey(StrongestGuildCfg.class, 500);
		for(String content : fileContents){
			try {
				String[] arr = content.split(",");
				String serverId = arr[0];
				String guildId = arr[1];
				String playerId = arr[2];
				long scoreAdd = Long.parseLong(arr[3]);
				if (!GlobalData.getInstance().isLocalServer(serverId)) {
					continue;
				}
				Integer coefficient = circularCfg400.getIndexScoreList().get(0);
				scoreAdd = scoreAdd * coefficient;
				//5阶段积分  转换成6 阶段的积分
				long addScore500 = (circularCfg400.getScoreWeightCof() * scoreAdd /circularCfg500.getScoreWeightCof());
				//个人总榜
				personalRank.addScore(addScore500, playerId);
				//联盟总榜
				GuildInfoObject guild = GuildService.getInstance().getGuildInfoObject(guildId);
				if(Objects.nonNull(guild)){
					guildRank.addScore(addScore500, guildId);
				}
				HawkLog.logPrintln("fixStrongestGuildRankData rankScoreScoure :{},addScore:{}",content,addScore500);
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
	}
	
	private void fixTBLYTeamMatchData(String password){
		boolean isReal = "fjskhas".equals(password);
		if(isReal){
			for(int i = 1; i < TiberiumConstCfg.getInstance().getEliminationStartTermId(); i++){
				String roomKey = String.format(TBLYWarResidKey.TBLY_WAR_SEASON_ROOM, getSeason(), i);
				RedisProxy.getInstance().getRedisSession().del(roomKey);
				List<String> fileContents = new ArrayList<>();
				try {
					HawkOSOperator.readTextFileLines("tmp/fixTblyMatch.txt", fileContents);
				} catch (Exception e) {
					HawkException.catchException(e);
				}
				for(String serverId : fileContents){
					String roomserverKey = String.format(TBLYWarResidKey.TBLY_WAR_SEASON_ROOM_SERVER, getSeason(), i, serverId);
					RedisProxy.getInstance().getRedisSession().del(roomserverKey);
				}
			}
		}else {
			for(int i = 1; i < TiberiumConstCfg.getInstance().getEliminationStartTermId(); i++){
				String roomKey = String.format(TBLYWarResidKey.TBLY_WAR_SEASON_ROOM, getSeason(), i);
				HawkLog.logPrintln("TBLYSeasonService fix del roomKey:{}", roomKey);
				List<String> fileContents = new ArrayList<>();
				try {
					HawkOSOperator.readTextFileLines("tmp/fixTblyMatch.txt", fileContents);
				} catch (Exception e) {
					HawkException.catchException(e);
				}
				for(String serverId : fileContents){
					String roomserverKey = String.format(TBLYWarResidKey.TBLY_WAR_SEASON_ROOM_SERVER, getSeason(), i, serverId);
					HawkLog.logPrintln("TBLYSeasonService fix del roomserverKey:{}", roomserverKey);
				}
			}
		}
		for(int i = 1; i <= TiberiumConstCfg.getInstance().getTeamCnt(); i++){
			String groupKey = String.format(TBLYWarResidKey.TBLY_WAR_SEASON_GROUP, getSeason(), i);
			Set<String> groupTeamIds = RedisProxy.getInstance().getRedisSession().sMembers(groupKey);
			List<String> teamIds = new ArrayList<>(groupTeamIds);
			Map<Integer, List<HawkTuple2<String, String>>> schedule = generateSchedule(teamIds, TiberiumConstCfg.getInstance().getEliminationStartTermId() - 1);
			for(int termId : schedule.keySet()){
				List<HawkTuple2<String, String>> results = schedule.get(termId);
				createRooms(isReal, termId, results, TiberiumWar.TLWServer.TLW_OLD_SERVER, TiberiumWar.TLWGroup.TEAM_GROUP, TiberiumConst.TLWBattleType.TEAM_GROUP_BATTLE, i);
			}
		}
		for(int i = 1; i <= TiberiumConstCfg.getInstance().getTeamCnt(); i++){
			String groupKey = String.format(TBLYWarResidKey.TBLY_WAR_SEASON_GROUP, getSeason(), i);
			Set<String> groupTeamIds = RedisProxy.getInstance().getRedisSession().sMembers(groupKey);
			List<String> teamIds = new ArrayList<>(groupTeamIds);
			Map<Integer, List<HawkTuple2<String, String>>> schedule = generateSchedule(teamIds, TiberiumConstCfg.getInstance().getEliminationStartTermId() - 1);
			for(int termId : schedule.keySet()){
				List<HawkTuple2<String, String>> results = schedule.get(termId);
				createRooms(isReal, termId, results, TiberiumWar.TLWServer.TLW_NEW_SERVER, TiberiumWar.TLWGroup.TEAM_GROUP, TiberiumConst.TLWBattleType.TEAM_GROUP_BATTLE, i);
			}
		}

	}

	public Map<Integer, List<HawkTuple2<String, String>>> generateSchedule(List<String> teamIds, int days) {
		Map<Integer, List<HawkTuple2<String, String>>> schedule = new HashMap<>();
		int totalTeams = teamIds.size();
		// 每支队伍每天有一场比赛，确保公平轮换
		for (int day = 0; day < days; day++) {
			List<HawkTuple2<String, String>> dayMatches = new ArrayList<>();
			// 为了确保公平性，每天根据轮换规则安排对战
			for (int i = 0; i < totalTeams / 2; i++) {
				int team1Index = i;
				int team2Index = (day + i) % (totalTeams / 2) +totalTeams / 2;
				String team1 = teamIds.get(team1Index);
				String team2 = teamIds.get(team2Index);
				dayMatches.add(new HawkTuple2<>(team1, team2));
			}
			schedule.put(day + 1, dayMatches);
		}

		return schedule;
	}
	
	@SuppressWarnings("unchecked")
	private void fixInviteMergeVotePermission(Map<String, String> params) {
		Optional<ActivityBase> opActivity = ActivityManager.getInstance().getActivity(Activity.ActivityType.INVITE_MERGE_VALUE);
		InviteMergeActivity activity = (InviteMergeActivity) opActivity.get();
		long time = HawkTime.getMillisecond();
		try {
			if(!activity.isOpening(null)) {
				RedisProxy.getInstance().getRedisSession().hSet("sysopInviteMergeVotePerClose", GsConfig.getInstance().getServerId(), String.valueOf(time), 7200);
				return;
			}
			
			String playerIds = params.get("playerIds");
			Set<String> idSet = new HashSet<>();
			//没有传参，从文件中读取
			if (HawkOSOperator.isEmptyString(playerIds)) {
				List<String> fileContents = new ArrayList<>();
				try {
					HawkOSOperator.readTextFileLines("tmp/inviteMergeVotePlayers.txt", fileContents);
				} catch (Exception e) {
					HawkException.catchException(e);
				}
				for (String content : fileContents) {
					String[] arr = content.split(",");
					if (GsConfig.getInstance().getServerId().equals(arr[0])) {
						idSet.add(arr[1]);
					}
				}
			} else {
				idSet.addAll(Arrays.asList(playerIds.split(",")));
			}
			
			if (idSet.isEmpty()) {
				RedisProxy.getInstance().getRedisSession().hSet("sysopInviteMergeVotePerEmpty", GsConfig.getInstance().getServerId(), String.valueOf(time), 7200);
				return;
			}
			
			Field votePremissionField = InviteMergeActivity.class.getDeclaredField("votePremission");
			votePremissionField.setAccessible(true);
			Set<String> playerIdSet = (Set<String>) votePremissionField.get(null);
			int size1 = playerIdSet.size();
			playerIdSet.addAll(idSet);
			
			Field votePremissionField1 = InviteMergeActivity.class.getDeclaredField("votePremission");
			votePremissionField1.setAccessible(true);
			Set<String> playerIdSet1 = (Set<String>) votePremissionField1.get(null);
			int size2 = playerIdSet1.size();
			
			RedisProxy.getInstance().getRedisSession().hSet("sysopInviteMergeVotePerSucc", GsConfig.getInstance().getServerId(), String.valueOf(time) +"-"+ (size2-size1), 7200);
		} catch (Exception e) {
			HawkException.catchException(e);
			RedisProxy.getInstance().getRedisSession().hSet("sysopInviteMergeVotePerExp", GsConfig.getInstance().getServerId(), String.valueOf(time), 7200);
		}
	}
	
	/**
	 * 修复邀请合服的执行官
	 */
	private void fixInviteMergeLeader() {
		Optional<ActivityBase> opActivity = ActivityManager.getInstance().getActivity(Activity.ActivityType.INVITE_MERGE_VALUE);
		InviteMergeActivity activity = (InviteMergeActivity) opActivity.get();
		long time = HawkTime.getMillisecond();
		try {
			if(!activity.isOpening(null)) {
				RedisProxy.getInstance().getRedisSession().hSet("sysopInviteLeaderClose", GsConfig.getInstance().getServerId(), String.valueOf(time), 7200);
				return;
			}
			Method method = HawkOSOperator.getClassMethod(activity, "getLeaderInfo", String.class);
			Object object = method.invoke(activity, GsConfig.getInstance().getServerId());
			if (object == null) {
				RedisProxy.getInstance().getRedisSession().hSet("sysopInviteLeaderErr", GsConfig.getInstance().getServerId(), String.valueOf(time), 7200);
				return;
			}
			InviteMergeLeaderTmp leader = (InviteMergeLeaderTmp) object;
			Player player = GlobalData.getInstance().makesurePlayer(leader.getPlayerId());
			//leader是本服玩家，不用重新选
			if (player != null) {
				return;
			}
			
			String playerId = activity.getDataGeter().chooseInviteMergeLeader();
			Player newPlayer = GlobalData.getInstance().makesurePlayer(playerId);
			//重新选，取一盟盟主
			if (newPlayer == null) {
				List<RankInfo> rankCache = RankService.getInstance().getRankCache(RankType.ALLIANCE_FIGHT_KEY, 1);
				String guildId = rankCache.get(0).getId();
				playerId = GuildService.getInstance().getGuildLeaderId(guildId);
				newPlayer = GlobalData.getInstance().makesurePlayer(playerId);
				if (newPlayer == null) {
					RedisProxy.getInstance().getRedisSession().hSet("sysopInviteLeaderFailed", GsConfig.getInstance().getServerId(), playerId+":"+time, 7200);
					return;
				}
			}
			
			String playerName = activity.getDataGeter().getPlayerName(playerId);
			String guildTag = activity.getDataGeter().getGuildTagByPlayerId(playerId);
			InviteMergeLeaderTmp info = new InviteMergeLeaderTmp();
			info.setPlayerId(playerId);
			info.setPlayerName(playerName);
			if (!HawkOSOperator.isEmptyString(guildTag)) {
				info.setGuildTag(guildTag);
			}
			
			Method method1 = HawkOSOperator.getClassMethod(activity, "updateLeaderInfo", InviteMergeLeaderTmp.class);
			method1.invoke(activity, info);
			RedisProxy.getInstance().getRedisSession().hSet("sysopInviteLeaderSucc", GsConfig.getInstance().getServerId(), playerId+":"+time, 7200);
		} catch (Exception e) {
			HawkException.catchException(e);
			RedisProxy.getInstance().getRedisSession().hSet("sysopInviteLeaderExp", GsConfig.getInstance().getServerId(), String.valueOf(time), 7200);
		}
	}
	
	/**
	 * 修复邀请合服的执行官数据
	 * @param params
	 */
	private void fixInviteMergeLeader1(Map<String, String> params) {
		Map<String, String> serverPresidentMap = new HashMap<>();
		String serverPlayers = params.getOrDefault("servers", "10732,8a5-301eza-a;20369,fq0-2vz35u-3;20119,fj3-nr0ie-l");
		if (!HawkOSOperator.isEmptyString(serverPlayers)) {
			String[] arr = serverPlayers.split(";");
			for (String serverPlayer : arr) {
				String[] info = serverPlayer.split(",");
				serverPresidentMap.put(info[0], info[1]);
			}
		} else {
			List<String> fileContents = new ArrayList<>();
			try {
				HawkOSOperator.readTextFileLines("tmp/presidentInfos.txt", fileContents);
			} catch (Exception e) {
				HawkException.catchException(e);
			}
			for (String content : fileContents) {
				String[] arr = content.split(",");
				serverPresidentMap.put(arr[0], arr[1]);
			}
		}
		
		long time = HawkTime.getMillisecond();
		String presidentPlayerId = serverPresidentMap.get(GsConfig.getInstance().getServerId());
		if (HawkOSOperator.isEmptyString(presidentPlayerId)) {
			return;
		}
		RedisProxy.getInstance().getRedisSession().hSet("sysopInviteLeaderNew", GsConfig.getInstance().getServerId(), String.valueOf(time), 7200);
		
		Optional<ActivityBase> opActivity = ActivityManager.getInstance().getActivity(Activity.ActivityType.INVITE_MERGE_VALUE);
		InviteMergeActivity activity = (InviteMergeActivity) opActivity.get();
		try {
			if(!activity.isOpening(null)) {
				RedisProxy.getInstance().getRedisSession().hSet("sysopInviteLeaderClose", GsConfig.getInstance().getServerId(), String.valueOf(time), 7200);
				return;
			}
			Method method = HawkOSOperator.getClassMethod(activity, "getLeaderInfo", String.class);
			Object object = method.invoke(activity, GsConfig.getInstance().getServerId());
			if (object == null) {
				RedisProxy.getInstance().getRedisSession().hSet("sysopInviteLeaderErr", GsConfig.getInstance().getServerId(), String.valueOf(time), 7200);
				return;
			}
			
			//clearInviteMergeActData(2);
			
			InviteMergeLeaderTmp leader = (InviteMergeLeaderTmp) object;
			Player player = GlobalData.getInstance().makesurePlayer(leader.getPlayerId());
			//leader是本服玩家，不用重新选
			if (player != null && player.getId().equals(presidentPlayerId)) {
				return;
			}
			
			String playerId = presidentPlayerId;
			if (HawkOSOperator.isEmptyString(presidentPlayerId)) {
				playerId = activity.getDataGeter().chooseInviteMergeLeader();
			}
			Player newPlayer = GlobalData.getInstance().makesurePlayer(playerId);
			//重新选，取一盟盟主
			if (newPlayer == null) {
				List<RankInfo> rankCache = RankService.getInstance().getRankCache(RankType.ALLIANCE_FIGHT_KEY, 1);
				String guildId = rankCache.get(0).getId();
				playerId = GuildService.getInstance().getGuildLeaderId(guildId);
				newPlayer = GlobalData.getInstance().makesurePlayer(playerId);
				if (newPlayer == null) {
					RedisProxy.getInstance().getRedisSession().hSet("sysopInviteLeaderFailed", GsConfig.getInstance().getServerId(), playerId+":"+time, 7200);
					return;
				}
			}
			
			String playerName = activity.getDataGeter().getPlayerName(playerId);
			String guildTag = activity.getDataGeter().getGuildTagByPlayerId(playerId);
			InviteMergeLeaderTmp info = new InviteMergeLeaderTmp();
			info.setPlayerId(playerId);
			info.setPlayerName(playerName);
			if (!HawkOSOperator.isEmptyString(guildTag)) {
				info.setGuildTag(guildTag);
			}
			Method method1 = HawkOSOperator.getClassMethod(activity, "updateLeaderInfo", InviteMergeLeaderTmp.class);
			method1.invoke(activity, info);
			RedisProxy.getInstance().getRedisSession().hSet("sysopInviteLeaderSuccNew", GsConfig.getInstance().getServerId(), playerId+":"+time, 7200);
		} catch (Exception e) {
			HawkException.catchException(e);
			RedisProxy.getInstance().getRedisSession().hSet("sysopInviteLeaderExp", GsConfig.getInstance().getServerId(), String.valueOf(time), 7200);
		}
	}
	
	/**
	 * 清理邀请合服活动的邀请数据
	 */
	protected void clearInviteMergeActData(int termId) {
		try {
			//邀请数据
			String key = "ACTIVITY_INVITE_MERGE_INVITE:" + termId + ":" + GsConfig.getInstance().getServerId();
			Map<String, String> recordsMap = RedisProxy.getInstance().getRedisSession().hGetAll(key);
			RedisProxy.getInstance().getRedisSession().del(key);
			for (Entry<String, String> entry : recordsMap.entrySet()) {
				String key1 = "ACTIVITY_INVITE_MERGE_BE_INVITE:" + termId + ":" + entry.getKey();
				RedisProxy.getInstance().getRedisSession().del(key1);
			}
			
			//被邀请数据
			String key2 = "ACTIVITY_INVITE_MERGE_BE_INVITE:" + termId + ":" + GsConfig.getInstance().getServerId();
			Map<String, String> recordsMap1 = RedisProxy.getInstance().getRedisSession().hGetAll(key2);
			RedisProxy.getInstance().getRedisSession().del(key2);
			for (Entry<String, String> entry : recordsMap1.entrySet()) {
				String key3 = "ACTIVITY_INVITE_MERGE_INVITE:" + termId + ":" + entry.getKey();
				RedisProxy.getInstance().getRedisSession().del(key3);
			}
			
			//已建立联系的数据
			String key4 = "ACTIVITY_INVITE_MERGE_CONNECT:" + termId;
			RedisProxy.getInstance().getRedisSession().del(key4);
		} catch (Exception e) {
			HawkException.catchException(e);
			RedisProxy.getInstance().getRedisSession().hSet("sysopInviteClearExp", GsConfig.getInstance().getServerId(), "1", 7200);
		}
	}
	
}
