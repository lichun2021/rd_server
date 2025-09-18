package com.hawk.game.module;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.hawk.annotation.MessageHandler;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.app.HawkApp;
import org.hawk.callback.HawkCallback;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.net.session.HawkSession;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.task.HawkCallbackTask;
import org.hawk.task.HawkDelayTask;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkThreadPool;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.googlecode.protobuf.format.JsonFormat;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.IDIPGmRechargeEvent;
import com.hawk.activity.event.impl.RechargeAllRmbEvent;
import com.hawk.activity.helper.PlayerAcrossDayLoginMsg;
import com.hawk.activity.type.impl.monthcard.cfg.MonthCardActivityCfg;
import com.hawk.activity.type.impl.monthcard.entity.ActivityMonthCardEntity;
import com.hawk.activity.type.impl.monthcard.entity.MonthCardItem;
import com.hawk.common.AccountRoleInfo;
import com.hawk.common.IDIPBanInfo;
import com.hawk.common.ServerInfo;
import com.hawk.common.VersionInfo;
import com.hawk.game.GsApp;
import com.hawk.game.GsConfig;
import com.hawk.game.activity.impl.backflow.BackFlowService;
import com.hawk.game.activity.impl.inherit.InheritNewService;
import com.hawk.game.city.CityManager;
import com.hawk.game.config.BaseInitCfg;
import com.hawk.game.config.BattleSoldierCfg;
import com.hawk.game.config.BuffCfg;
import com.hawk.game.config.BuildAreaCfg;
import com.hawk.game.config.BuildingCfg;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.config.CustomKeyCfg;
import com.hawk.game.config.GameConstCfg;
import com.hawk.game.config.ItemCfg;
import com.hawk.game.config.OpenidAward;
import com.hawk.game.config.PlayerLevelExpCfg;
import com.hawk.game.config.ServerInfoCfg;
import com.hawk.game.config.VersionRewardCfg;
import com.hawk.game.config.WorldMapConstProperty;
import com.hawk.game.crossproxy.CrossService;
import com.hawk.game.crossproxy.model.CsPlayer;
import com.hawk.game.data.ScoreBatchInfo;
import com.hawk.game.entity.ArmyEntity;
import com.hawk.game.entity.BuildingBaseEntity;
import com.hawk.game.entity.CustomDataEntity;
import com.hawk.game.entity.DailyDataEntity;
import com.hawk.game.entity.DressEntity;
import com.hawk.game.entity.ItemEntity;
import com.hawk.game.entity.MoneyReissueEntity;
import com.hawk.game.entity.PlayerBaseEntity;
import com.hawk.game.entity.PlayerEntity;
import com.hawk.game.entity.PlayerGiftEntity;
import com.hawk.game.entity.QueueEntity;
import com.hawk.game.entity.StatisticsEntity;
import com.hawk.game.entity.StatusDataEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.global.LoginStatis;
import com.hawk.game.global.RedisKey;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.guild.guildrank.GuildRankMgr;
import com.hawk.game.guild.voice.VoiceRoomManager;
import com.hawk.game.invoker.GuildLeaderLogoutMsgInvoker;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.log.BehaviorLogger;
import com.hawk.game.log.BehaviorLogger.Params;
import com.hawk.game.manager.IconManager;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.player.roleexchange.RoleExchangeService;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.GuildAuthority;
import com.hawk.game.protocol.Const.ItemType;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.protocol.Const.StateType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.IDIP.NoticeMode;
import com.hawk.game.protocol.IDIP.NoticeType;
import com.hawk.game.protocol.Login.HPLogin;
import com.hawk.game.protocol.Login.HPLoginRet;
import com.hawk.game.protocol.Login.HPServerInfo;
import com.hawk.game.protocol.Login.LoginandRename;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Player.LoginWay;
import com.hawk.game.protocol.Player.MsgCategory;
import com.hawk.game.protocol.Player.PlayerFacadeInfoReq;
import com.hawk.game.protocol.Player.PlayerFacadeInfoResp;
import com.hawk.game.protocol.Rank.RankType;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Status.IdipMsgCode;
import com.hawk.game.protocol.Status.SysError;
import com.hawk.game.protocol.SysProtocol.HPErrorCode;
import com.hawk.game.protocol.SysProtocol.PasswdInfoPB;
import com.hawk.game.queryentity.AccountInfo;
import com.hawk.game.rank.RankService;
import com.hawk.game.service.BuildingService;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.MissionService;
import com.hawk.game.service.PlayerAchieveService;
import com.hawk.game.service.QQScoreBatch;
import com.hawk.game.service.QueueService;
import com.hawk.game.service.StoryMissionService;
import com.hawk.game.service.chat.ChatService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.service.mssion.MissionManager;
import com.hawk.game.service.mssion.event.EventBuildingCreate;
import com.hawk.game.service.mssion.event.EventDailyFirstLogin;
import com.hawk.game.task.PlayerDataLoadTask;
import com.hawk.game.tsssdk.GameTssService;
import com.hawk.game.tsssdk.GameMsgCategory;
import com.hawk.game.util.BuilderUtil;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.GsConst.DailyInfoField;
import com.hawk.game.util.GsConst.DiamondPresentReason;
import com.hawk.game.util.GsConst.IDIPBanType;
import com.hawk.game.util.GsConst.IDIPDailyStatisType;
import com.hawk.game.util.GsConst.MissionFunType;
import com.hawk.game.util.GsConst.ScoreType;
import com.hawk.game.util.GsConst.StatisticDataType;
import com.hawk.game.util.GsConst.TimerEventEnum;
import com.hawk.game.util.LogUtil;
import com.hawk.game.util.LoginUtil;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.gamelib.rank.RankScoreHelper;
import com.hawk.gamelog.GameLog;
import com.hawk.gamelog.LogParam;
import com.hawk.health.HealthGameManager;
import com.hawk.health.entity.PushEndGameResult;
import com.hawk.health.entity.QueryUserInfoResult;
import com.hawk.log.Action;
import com.hawk.log.LogConst.ArmyChangeReason;
import com.hawk.log.LogConst.ArmySection;
import com.hawk.log.LogConst.IMoneyType;
import com.hawk.log.LogConst.LogInfoType;
import com.hawk.log.LogConst.PowerChangeReason;
import com.hawk.log.Source;
import com.hawk.sdk.SDKConst.UserType;
import com.hawk.sdk.SDKConst;
import com.hawk.sdk.SDKManager;
import com.hawk.serialize.string.SerializeHelper;
import com.hawk.util.TimeUtil;
import com.hawk.zoninesdk.ZonineSDK;
import com.hawk.zoninesdk.datamanager.OpDataType;

/**
 * 玩家登陆模块
 *
 * @author hawk
 */
public class PlayerLoginModule extends PlayerModule {
	/**
	 * 构造函数
	 *
	 * @param player
	 */
	public PlayerLoginModule(Player player) {
		super(player);
	}

	/**
	 * 初始化角色必须数据
	 * 
	 * @param playerEntity
	 */
	private void initPlayer() {
		long startTime = HawkTime.getMillisecond();

		// 日志记录
		HawkLog.logPrintln("init player, playerId: {}, puid: {}, playerName: {}, diamonds：{}, gold: {}, coin: {}, level: {}, vip: {}, vit: {}", player.getId(), player.getPuid(),
				player.getName(), player.getDiamonds(), player.getGold(), player.getCoin(), player.getLevel(), player.getVipLevel(), player.getVit());

		// 组装玩家数据
		PlayerData playerData = player.getData();
		playerData.assembleData(player);

		// 玩家基础数据
		PlayerBaseEntity playerBaseEntity = player.getPlayerBaseEntity();
		int playerLevel = playerBaseEntity.getLevel();
		// 首次修改saveAmtTotal数据
		int saveAmtTotal = playerBaseEntity.getSaveAmtTotal();
		if (saveAmtTotal < 0) {
			playerBaseEntity.setSaveAmtTotal(playerBaseEntity.getSaveAmt());
			HawkLog.logPrintln("reset saveAmtTotal first, playerId: {}, openid: {}, platform: {}, saveAmtTotal before: {}, after: {}", player.getId(), 
					player.getOpenId(), player.getPlatform(), saveAmtTotal, playerBaseEntity.getSaveAmtTotal());
		}

		// 禁言信息
		IDIPBanInfo banMsgInfo = RedisProxy.getInstance().getIDIPBanInfo(player.getOpenId(), IDIPBanType.AREA_BAN_SEND_MSG); // 保留这一行是为了兼容历史
		if (banMsgInfo == null) {
			banMsgInfo = RedisProxy.getInstance().getIDIPBanInfo(player.getPuid(), IDIPBanType.AREA_BAN_SEND_MSG);
		}
		if (banMsgInfo != null) {
			long banEndTime = banMsgInfo.getEndTime();
			player.getData().getPlayerEntity().setSilentTime(banEndTime);
			banMsgInfo.setTargetId(player.getId());
			banMsgInfo.setBanMsg(banMsgInfo.getBanMsg() + "（解封时间：" + HawkTime.formatTime(banEndTime) + "）");
			RedisProxy.getInstance().addIDIPBanInfo(player.getId(), banMsgInfo, IDIPBanType.BAN_SEND_MSG);
		}
		
		// 跨服玩家直接返回，不走初始化流程
		if (player.isCsPlayer()) {
			return;
		}
		
		// 判断是否为新号
		if (playerLevel > 0) {
			return;
		}
		
		// 在playerLevel的基础上再加上此判断，是为了容错处理
		if (player.getCityLevel() > 0) {
			return;
		}

		logRegisterInfo();
		playerLoginCheckName(false);

		// 创建角色是需要上报角色名称，角色名称又是每条上报信息固有的字段，故这里不用传值
		GameUtil.scoreBatch(player, ScoreType.REG_CHANNEL, player.getChannelId());
		GameUtil.scoreBatch(player, ScoreType.REGISTER_TIME, HawkTime.getSeconds());
		QQScoreBatch.getInstance().scoreBatch(player);

		// 新号进行各项数据的初始化操作
		PlayerLevelExpCfg levelExpCfg = HawkConfigManager.getInstance().getConfigByKey(PlayerLevelExpCfg.class, 1);
		player.increaseLevel(1, Action.PLAYER_CREATE);
		player.increaseVit(ConstProperty.getInstance().getNewVitPoint(), Action.PLAYER_CREATE, false);

		// 默认数据
		PlayerEntity playerEntity = player.getEntity();
		playerEntity.setIcon(GameConstCfg.getInstance().getDefaultIcon());

		// 初始化玩家需要创建的entity实例
		List<HawkDBEntity> initEntities = new ArrayList<HawkDBEntity>();

		// 初始奖励(物品挑选出来)
		List<ItemInfo> initAwardList = levelExpCfg.getBonusList();
		initItemEntity(initAwardList, initEntities);
		// 其余初始化奖励发放
		AwardItems award = AwardItems.valueOf();
		award.addItemInfos(initAwardList);
		award.rewardTakeAffect(player, Action.PLAYER_CREATE);

		// 初始化建筑数据
		initBuildingEntity(initEntities);

		// 任务刷新
		MissionService.getInstance().initMissionList(player, initEntities);

		// 初始化军队信息
		initArmy(initEntities);

		// 初始化队列
		initQueueEntity(initEntities);

		// 初始化新手保护状态
		initNewPlayerProtectBuf(initEntities);

		// 初始化数据的db创建
		if (GsConfig.getInstance().isAsyncInitData()) {
			for (HawkDBEntity entity : initEntities) {
				entity.create(true);
			}
		} else {
			HawkDBEntity.batchCreate(initEntities);
		}

		// 解锁初始化区域
		unlockInitArea();

		// 刷新电力
		player.refreshPowerElectric(PowerChangeReason.INIT_PLAYER);

		// 剧情任务刷新
		StoryMissionService.getInstance().initStroyMission(player);

		// 初始化玩家成就
		PlayerAchieveService.getInstance().initPlayerAchieve(player);
		
		// 世界装扮
		initWorldDressShow();

		// 发邮件---新手邮件
		SystemMailService.getInstance().sendMail(MailParames.newBuilder().setPlayerId(player.getId()).setMailId(MailId.NEW_ACCOUNT).build());

		// 发邮件---openid奖励
		OpenidAward openidAward = HawkConfigManager.getInstance().getConfigByKey(OpenidAward.class, player.getOpenId());
		if (openidAward != null) {
			SystemMailService.getInstance().sendMail(MailParames.newBuilder().setPlayerId(player.getId()).addRewards(ItemInfo.valueListOf(openidAward.getAward()))
					.setMailId(MailId.OPENID_AWARD).setAwardStatus(MailRewardStatus.NOT_GET).build());
		}

		//破晓启航
		CrossService.getInstance().newStartCheck(player);

		// 日志记录
		HawkLog.logPrintln("player init create success: {}, puid: {}, playerName: {}, diamonds：{}, gold: {}, coin: {}, level: {}, vip: {}, costtime: {}", player.getId(),
				player.getPuid(), player.getName(), player.getDiamonds(), player.getGold(), player.getCoin(), player.getLevel(), player.getVipLevel(),
				HawkTime.getMillisecond() - startTime);
	}
	
	/**
	 * 记录创角信息
	 */
	private void logRegisterInfo() {
		LogParam logParam0 = LogUtil.getPersonalLogParam(player, LogInfoType.createuser);
		if (logParam0 != null) {
			logParam0.put("inviteComing", player.getEntity().isBeInvited() ? 1: 0);
			GameLog.getInstance().info(logParam0);
		}
		
		try {
			String channelId = player.getChannelId();
			if (!HawkOSOperator.isEmptyString(channelId)) {
				RedisProxy.getInstance().getRedisSession().setString("PlayerRegChannel:" + player.getOpenId(), channelId);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		try {
			// 经分新提的累计登录相关需求  20230112  -- args第1个参数表示注册后连续登录天数，第二个参数是累计登录天数
			player.getData().createCustomDataEntity(GsConst.CONTINUE_LOGIN_KEY, HawkTime.getSeconds(), String.format("%d_%d", 1, 1));
			
			Map<String, String> accountRoleMap = RedisProxy.getInstance().getAccountRole(player.getOpenId());
			String key = GsConfig.getInstance().getServerId() + ":" + player.getPlatform();
			AccountRoleInfo roleInfoObj = accountRoleMap.get(key) != null ? JSONObject.parseObject(accountRoleMap.get(key), AccountRoleInfo.class) : null;
			// 上报一个账号首次注册的tlog打点
			if (accountRoleMap.isEmpty() || (accountRoleMap.size() == 1 && roleInfoObj != null && player.getId().equals(roleInfoObj.getPlayerId()))) {
				LogParam logParam1 = LogUtil.getPersonalLogParam(player, LogInfoType.account_register);
				if (logParam1 != null) {
					logParam1.put("inviteComing", player.getEntity().isBeInvited() ? 1: 0);
					GameLog.getInstance().info(logParam1);
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 初始化建筑数据
	 * 
	 * @param initDbEntities
	 */
	private void initBuildingEntity(List<HawkDBEntity> initDbEntities) {
		long now = HawkApp.getInstance().getCurrentTime();
		ConfigIterator<BaseInitCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(BaseInitCfg.class);
		while (iterator.hasNext()) {
			BaseInitCfg cfg = iterator.next();
			int buildCfgId = cfg.getBuildId();
			final BuildingCfg buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, buildCfgId);
			BuildingBaseEntity building = player.getData().createBuildingEntity(buildingCfg, cfg.getIndex(), true);
			building.setLastUpgradeTime(now);
			
			player.getData().addBuildingEntity(building);
			initDbEntities.add(building);
			
			player.getData().refreshNewBuilding(building);
			BuildingCfg buildCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, building.getBuildingCfgId());
			if (buildCfg != null) {
				BuildingService.getInstance().refreshBuildEffect(player, buildCfg, false);
			}

			if (cfg.needTrigTask()) {
				MissionService.getInstance().refreshBuildLevelConditionMission(player, building.getBuildingCfgId());
				MissionManager.getInstance().postMsg(player, new EventBuildingCreate(building.getBuildingCfgId()));
			}

			LogUtil.logBuildFlow(player, building, 0, 1);
		}
	}

	/**
	 * 解锁初始化区域
	 */
	private void unlockInitArea() {
		List<Integer> areaList = BuildAreaCfg.getUnlockedArea(player.getCityLevel());
		if (!areaList.isEmpty()) {
			int count = 0;
			for (Integer area : areaList) {
				BuildAreaCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BuildAreaCfg.class, area);
				// 不允许解锁，或需要点击解锁
				if (!cfg.isAllowedUnlock() || cfg.isNeedClick()) {
					continue;
				}

				player.unlockArea(area);
				if (GameUtil.isCityOutsideAreaBlock(area)) {
					count++;
				}
			}

			MissionService.getInstance().missionRefresh(player, MissionFunType.FUN_UNLOCK_AREA, 0, count);
		}
	}

	/**
	 * 物品道具信息初始化
	 * 
	 * @param initAwardList
	 */
	private void initItemEntity(List<ItemInfo> initAwardList, List<HawkDBEntity> initDbEntities) {
		Iterator<ItemInfo> iter = initAwardList.iterator();
		while (iter.hasNext()) {
			ItemInfo itemInfo = iter.next();
			if (itemInfo.getItemType() != ItemType.TOOL) {
				continue;
			}

			iter.remove();
			ItemEntity itemEntity = new ItemEntity();
			itemEntity.setId(HawkOSOperator.randomUUID());
			itemEntity.setItemId(itemInfo.getItemId());
			itemEntity.setItemCount((int) itemInfo.getCount());
			itemEntity.setPlayerId(player.getId());
			
			getPlayerData().getItemEntities().add(itemEntity);

			initDbEntities.add(itemEntity);
			
			BehaviorLogger.log4Service(player, Source.TOOLS_ADD, Action.PLAYER_CREATE, Params.valueOf("itemId", itemInfo.getItemId()), Params.valueOf("add", itemInfo.getCount()),
					Params.valueOf("after", itemInfo.getCount()));

			ItemCfg itemCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, itemInfo.getItemId());
			if (itemCfg == null) {
				HawkLog.errPrintln("player init item failed, playerId: {}, itemId: {}", player.getId(), itemInfo.getItemId());
				continue;
			}

			LogUtil.logItemFlow(player, Action.PLAYER_CREATE, LogInfoType.goods_add, itemCfg.getItemType(), itemInfo.getItemId(), itemInfo.getCount(), 0, IMoneyType.MT_GOLD);
		}

	}

	/**
	 * 新手部队初始化
	 */
	private void initArmy(List<HawkDBEntity> initDbEntities) {
		int[] soldiers = ConstProperty.getInstance().getInitSoldiers();
		if (soldiers == null || soldiers.length < 2) {
			return;
		}

		List<ArmyEntity> playerArmyData = getPlayerData().getArmyEntities();
		for (int i = 0; i < soldiers.length / 2; i++) {
			int armyId = soldiers[i * 2];
			BattleSoldierCfg soldierCfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, armyId);
			if (soldierCfg == null) {
				continue;
			}

			ArmyEntity armyEntity = new ArmyEntity();
			armyEntity.setId(HawkOSOperator.randomUUID());
			armyEntity.setPlayerId(player.getId());
			armyEntity.setArmyId(armyId);
			armyEntity.addFree(soldiers[i * 2 + 1]);

			playerArmyData.add(armyEntity);
			initDbEntities.add(armyEntity);
			
			LogUtil.logArmyChange(player, armyEntity, soldiers[i * 2 + 1], ArmySection.FREE, ArmyChangeReason.AWARD);
		}
	}

	/**
	 * 初始化可重用队列
	 * 
	 * @return
	 */
	private void initQueueEntity(List<HawkDBEntity> initDbEntities) {
		List<QueueEntity> playerQueueData = player.getData().getQueueEntities();
		for (int i = 0; i < GsConst.REUSABLE_QUEUE_COUNT; i++) {
			QueueEntity queueEntity = QueueService.getInstance().addReusableQueue(player, 0, 0, GsConst.NULL_STRING, 0, 0d, null, GsConst.QueueReusage.FREE);
			playerQueueData.add(queueEntity);
			initDbEntities.add(queueEntity);
		}

		// 初始化付费队列
		QueueEntity queueEntity = QueueService.getInstance().initPaidQueue(player);
		if (queueEntity != null && player.getData().getQueueEntity(queueEntity.getId()) == null) {
			initDbEntities.add(queueEntity);
			playerQueueData.add(queueEntity);
		}
	}

	/**
	 * 创建新手保护Buf
	 */
	private boolean initNewPlayerProtectBuf(List<HawkDBEntity> initDbEntities) {
		long now = HawkApp.getInstance().getCurrentTime();
		StatusDataEntity protectBufEntity = player.getData().getStatusById(EffType.CITY_SHIELD_VALUE);
		if (protectBufEntity == null) {
			protectBufEntity = new StatusDataEntity();			
			protectBufEntity.setUuid(HawkOSOperator.randomUUID());
			protectBufEntity.setPlayerId(player.getId());
			protectBufEntity.setType(StateType.BUFF_STATE_VALUE);
			protectBufEntity.setStatusId(EffType.CITY_SHIELD_VALUE);
		}

		protectBufEntity.setStartTime(now);
		if (!GsConfig.getInstance().isRobotMode()) {
			protectBufEntity.setVal(GsConst.ProtectState.NEW_PLAYER);
			protectBufEntity.setEndTime(now + ConstProperty.getInstance().getNewProtectTime() * 1000);
		} else {
			protectBufEntity.setEndTime(0);
			protectBufEntity.setVal(GsConst.ProtectState.NO_BUFF);
			protectBufEntity.resetPushed(true);
		}

		player.getData().getStatusDataEntities().add(protectBufEntity);
		initDbEntities.add(protectBufEntity);
		return true;
	}

	/**
	 * 登陆协议处理
	 *
	 * @param session
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.LOGIN_C_VALUE)
	private boolean onLoginRequest(HawkProtocol protocol) {
		// 获取携带信息
		final HawkSession session = protocol.getSession();
		final HPLogin loginCmd = protocol.parseProtocol(HPLogin.getDefaultInstance());

		// 账号信息是否存在
		final AccountInfo accountInfo = session.getUserObject("account");
		if (accountInfo == null) {
			HawkLog.errPrintln("login request accountInfo not exist, loginCmd: {}", loginCmd.toString());
			return false;
		}

		// 检测最大在线数
		if (!GsApp.getInstance().checkSessionMaxSize(protocol.getSession())) {
			return false;
		}
		
		// 同一个在线玩家会话发重复登录协议, 丢弃协议
		if (session == player.getSession() && player.getActiveState() == GsConst.PlayerState.ONLINE) {
			HawkLog.errPrintln("login request illegality, accountInfo: {}, loginCmd: {}", accountInfo.toString(), loginCmd.toString());

			return false;
		}

		// 设置正在登录的状态
		player.setActiveState(GsConst.PlayerState.LOGINING);

		// 异步加载玩家数据
		HawkLog.logPrintln("login request async load player data, accountInfo: {}", accountInfo.toString());
		//登录之后把玩家的登录协议收起来
		player.setHpLogin(loginCmd.toBuilder());
		setAsaInfo(loginCmd.getAsaInfo());
		
		// 开始异步加载
		HawkThreadPool threadPool = HawkDBManager.getInstance().getThreadPool();
		int threadIdx = Math.abs(loginCmd.getPuid().hashCode() % threadPool.getThreadNum());
		
		// 异步加载回调
		HawkCallbackTask dataLoadTask = HawkCallbackTask.valueOf(new PlayerDataLoadTask(accountInfo), new HawkCallback() {
			@Override
			public int invoke(Object args) {
				PlayerData playerData = (PlayerData) args;
				if (playerData == null) {
					if (HawkDBManager.getInstance().isDbInException()) {
						HPErrorCode.Builder builder = HPErrorCode.newBuilder();
						builder.setHpCode(HP.code.LOGIN_C_VALUE);
						builder.setErrCode(SysError.SERVER_BUSY_LIMIT_VALUE);
						builder.setErrFlag(0);
						
						session.sendProtocol(HawkProtocol.valueOf(HP.sys.ERROR_CODE, builder));
					}
					
					player.clearData(false);
					HawkLog.errPrintln("async load player data failed, accountInfo: {}", accountInfo.toString());
					return 0;
				}

				// 日志记录
				HawkLog.logPrintln("async load player data success, accountInfo: {}", accountInfo.toString());

				// 通知加载成功
				onPlayerDataLoadSuccess(playerData, session, loginCmd);
				return 0;
			}
		});
		
		// 投递数据加载任务
		dataLoadTask.setPriority(1);
		dataLoadTask.setTypeName("PlayerDataLoadTask");
		dataLoadTask.setTaskOwner(player.getXid().hashCode());
		threadPool.addTask(dataLoadTask, threadIdx, false);
		
		// 回复登录协议
		HPLoginRet.Builder builder = HPLoginRet.newBuilder();
		builder.setErrCode(Status.SysError.SUCCESS_OK_VALUE);
		builder.setPlayerId(accountInfo.getPlayerId());
		builder.setPuid(accountInfo.getPuid());
		builder.setServerOpenTime(GameUtil.getServerOpenTime());
		builder.setFlag(loginCmd.getFlag());
		builder.setZoneId(accountInfo.getServerId());
		builder.setServerId(GsConfig.getInstance().getServerId());
		String crossServerId = CrossService.getInstance().getImmigrationPlayerServerId(accountInfo.getPlayerId());
		if (!HawkOSOperator.isEmptyString(crossServerId)) {
			builder.setCrossServerId(GsConfig.getInstance().getServerId());
			CsPlayer csPlayer = player.getCsPlayer();
			if (csPlayer != null) {
				builder.setCrossType(csPlayer.getCrossType());
			} else {
				HawkLog.errPrintln("immigration player cant not get csplayer id:{}", player.getId());
			}
		}

		// 版本信息
		VersionInfo versionInfo = RedisProxy.getInstance().getVersionInfo(GsConfig.getInstance().getAreaId(), loginCmd.getPlatform());
		if (versionInfo != null) {
			builder.setVersion(versionInfo.getVersion());
		}

		long curTime = HawkTime.getMillisecond();
		builder.setTimeStamp(curTime);
		builder.setAreaId(Integer.parseInt(GsConfig.getInstance().getAreaId()));
		
		boolean wxserver = true;
		builder.setWxServer(wxserver ? 1 : 0); //是否是微信服
		buildAreaServerInfos(wxserver, builder);
		protocol.response(HawkProtocol.valueOf(HP.code.LOGIN_S, builder));

		LoginStatis.getInstance().addLoginTime(curTime - accountInfo.getLoginTime());
		// 日志记录
		HawkLog.logPrintln("player login time consume, playerId: {}, period: {}, zoneId: {}", accountInfo.getPlayerId(), curTime - accountInfo.getLoginTime(), accountInfo.getServerId());

		return true;
	}
	
	/**
	 * 同步本大区的区服信息
	 */
	private void buildAreaServerInfos(boolean wxserver, HPLoginRet.Builder builder) {
		try {
			Map<String, ServerInfo> serverMap = GlobalData.getInstance().getServerMap();
			for (Entry<String, ServerInfo> entry : serverMap.entrySet()) {
				ServerInfo serverInfo = entry.getValue();
				HPServerInfo.Builder serverBuilder = HPServerInfo.newBuilder();
				serverBuilder.setServerId(serverInfo.getId());
				serverBuilder.setServerName(serverInfo.getName());
				ServerInfoCfg cfg = HawkConfigManager.getInstance().getConfigByKey(ServerInfoCfg.class, Integer.parseInt(serverInfo.getId()));
				if (cfg != null) {
					serverBuilder.setAreaId(cfg.getAreaId());
					serverBuilder.setIsQQ(cfg.getIsQQ());
					if (!HawkOSOperator.isEmptyString(cfg.getPlatformName())) {
						serverBuilder.setPlatformName(cfg.getPlatformName());
					}
				}
				builder.addServerInfo(serverBuilder);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 设置asa信息
	 * 
	 * @param asaInfo
	 */
	private void setAsaInfo(String asaInfo) {
		if (HawkOSOperator.isEmptyString(asaInfo)) {
			player.oaidOrCaid = "";
			player.useragent = "";
			player.idfa = "";
		} else {
			try {
				JSONObject json = JSONObject.parseObject(asaInfo);
				player.oaidOrCaid = json.getString("caid");
				player.useragent = json.getString("useragent");
				player.idfa = json.getString("idfa");
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
	}

	private boolean onPlayerDataLoadSuccess(PlayerData playerData, HawkSession session, HPLogin loginCmd) {
		if (playerData == null) {
			return false;
		}

		// 设置数据初始化完毕
		AccountInfo accountInfo = GlobalData.getInstance().getAccountInfoByPlayerId(playerData.getPlayerId());
		if (accountInfo != null) {
			accountInfo.setInBorn(false);
		}

		// 设置玩家数据对象
		player.updateData(playerData);

		// 更新玩家数据访问
		GlobalData.getInstance().notifyPlayerDataAccess(player.getData());

		// 更新设备信息
		updateDeviceInfo(loginCmd.getDeviceId());
		
		// 更新启动游戏方式：正常启动，通过游戏中心启动，或通过其他方式启动
		updateLoginWay(loginCmd.getLoginWay());
		
		// 更新设备信息
		updatePhoneInfo(loginCmd.getPhoneInfo());

		// 更新推送信息
		updatePushInfo(loginCmd.getPushInfo(), loginCmd.getLang());

		// 更新版本信息
		updateVersion(loginCmd.getVersion());

		// 绑定会话
		player.setSession(session);
		
		player.gamematrix = loginCmd.hasGamematrix() ? loginCmd.getGamematrix() : 0;

		// 重新设置缓存失效周期
		int transitoryCacheLevel = GsConfig.getInstance().getTransitoryCacheLevel();
		if(transitoryCacheLevel > 0 && player.getCityLevel() < transitoryCacheLevel) {
			playerData.updateCacheExpire(GsConfig.getInstance().getTransitoryCacheTime());
		} else {
			playerData.updateCacheExpire(GsConfig.getInstance().getCacheExpireTime());
		}
		
		boolean isWin32 = GameUtil.isWin32Platform(loginCmd.getPlatform(), loginCmd.getChannel()); 
		if (!isWin32 && player.getPfTokenJson() == null && !HawkOSOperator.isEmptyString(loginCmd.getPfToken())) {
			JSONObject pfInfoJson = JSONObject.parseObject(loginCmd.getPfToken());
			player.setPfTokenJson(pfInfoJson);
			try {
				String channelId = player.getChannelId();
				if (!HawkOSOperator.isEmptyString(channelId)) {
					RedisProxy.getInstance().getRedisSession().setNx("PlayerRegChannel:" + player.getOpenId(), channelId);
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
			HawkLog.logPrintln("onPlayerDataLoadSuccess set pftoken, playerId: {}", player.getId());
		}
		
		// 记录平台信息
		HawkLog.logPrintln("player pftoken, playerId: {}, playerName: {}, pfToken: {}", 
				player.getId(), player.getName(), loginCmd.getPfToken());

		// 投递消息
		player.doPlayerAssembleAndLogin(session, loginCmd);
		
		return true;
	}

	/**
	 * 玩家组装处理
	 *
	 * @return
	 */
	@Override
	protected boolean onPlayerAssemble() {
		// 角色初始化
		try {
			initPlayer();
		} catch (Exception e) {
			HawkLog.errPrintln("player init failed, playerId: {}", player.getId());

			HawkException.catchException(e);
		}
		
		checkNameAfterMergeArea();
		//将互通区上的QQ玩家的rechargeInfo数据转储到另一个key上
		if (UserType.getByChannel(player.getChannel()) == UserType.QQ) {
			rechargeInfoMoveRestore();
		}
		
		//刷新排行榜，处理拆服后部分迁服玩家上不了榜的问题
		refreshRank();
		
		GlobalData.getInstance().clearPfAuthStatus(player.getOpenId());
		
		long nowTime = HawkTime.getMillisecond();
		PlayerEntity playerEntity = getPlayerData().getPlayerEntity();

		// 登录天数计算，需放在角色初始化之后
		long lastTime = playerEntity.getLogoutTime();
		Boolean isNewDay = !HawkTime.isSameDay(nowTime, lastTime);
		if (isNewDay) {
			// 日活新增
			RedisProxy.getInstance().incServerDailyInfo(DailyInfoField.DAY_LOGIN, 1);
			ZonineSDK.getInstance().opDataReport(OpDataType.ACTIVE_USER, player.getOpenId(), 1);
			// 每日登陆
			doNewPlayerOrNewDay();
		}

		processPlayerAcreossDayLogin(true);

		// 最近7天活跃
		updateLast7DayLively(playerEntity, true);

		playerEntity.setLastLoginTime(playerEntity.getLoginTime());
		playerEntity.setLoginTime(nowTime);
		playerEntity.setResetTime(nowTime);
		player.setHealthGameUpdateTime(nowTime);
		// 更新活跃标记
		updateLoginMask(playerEntity);
		
		// 登录的时候清空身上的头像，触发从redis拉取
		player.getData().setPfIcon(null);

		// 拉取平台头像
		String puidProfile = RedisProxy.getInstance().getPuidProfile(player.getPuid());
		updatePfIcon(playerEntity, puidProfile);

		// 记录玩家登录信息
		logLoginInfo();

		// 每日数据重置
		AccountInfo accountInfo = GlobalData.getInstance().getAccountInfoByPlayerId(player.getId());
		if (accountInfo != null && !accountInfo.isNewly()) {
			DailyDataEntity dailyDataEntity = getPlayerData().getDailyDataEntity();
			if (TimeUtil.isNeedReset(TimerEventEnum.ZERO_CLOCK.getClock(), nowTime, dailyDataEntity.getResetTime())) {
				dailyDataEntity.clear();
				LocalRedis.getInstance().clearFriendPresentGift(player.getId());
				
				PlayerGiftEntity playerGiftEntity = player.getData().getPlayerGiftEntity();
				playerGiftEntity.clearDailyGiftAdvice();
			}
		}
		
		long costtime = HawkTime.getMillisecond() - nowTime;
		if (costtime > GsConfig.getInstance().getTaskTimeout()) {
			HawkLog.logPrintln("assemble module data, playerId: {}, costtime: {}", player.getId(), costtime);
		}

		return true;
	}
	
	/**
	 * 将互通区上的手Q玩家的rechargeInfo数据转储到另一个key中
	 */
	private void rechargeInfoMoveRestore() {
		//上一次登录游戏的时间在 2025-08-21 06:30:00（保险起见，2025-08-14 06:30:00往后推一个星期） 之前的才要处理，在那之后登录的就相当于处理过了
		if (player.getLoginTime() < 1755729000000L) {
			try {
				RedisProxy.getInstance().qqRechargeInfoRestore(player);
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
	}
	
	/**
	 * 刷新排行榜，处理拆服后部分迁服玩家上不了榜的问题
	 */
	private void refreshRank() {
		try {
			int level = player.getPlayerBaseEntity().getLevel();
			long time = player.getPlayerBaseEntity().getLevelUpTime();
			//以 2025-01-16 20:00:00 这个时间点为界限
			if (level >= 58 && time <= 0 && player.getLoginTime() < 1737028800000L) {
				String timeStr = RedisProxy.getInstance().getRedisSession().hGet(RedisKey.PLAYER_LEVELUP_TIME, player.getId());
				if (!HawkOSOperator.isEmptyString(timeStr)) {
					player.getPlayerBaseEntity().setLevelUpTime(Long.parseLong(timeStr));
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		//上一次登录游戏的时间在 2024-12-24 18:00:00 之前的，重新刷一下榜单
		if (player.getLoginTime() >= 1735034400000L) {
			return;
		}
		try {
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
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 记录玩家登录信息
	 */
	private void logLoginInfo() {
		try {
			CustomDataEntity customData = player.getData().getCustomDataEntity(GsConst.CONTINUE_LOGIN_KEY);
			int regContinueLoing = 0, totalLogin = 0;
			if (customData != null) {
				long lastLoginTime = customData.getValue() * 1000L;
				long now = HawkTime.getMillisecond();
				customData.setValue((int) (now / 1000));
				int crossDay = HawkTime.getCrossDay(lastLoginTime, now, 0);
				int[] vals = SerializeHelper.string2IntArray(customData.getArg(), "_");
				regContinueLoing = vals[0];
				totalLogin = vals[1];
				// 注册后连续登录天数
				if (crossDay == 1 && regContinueLoing == totalLogin) {
					regContinueLoing += 1;
				}
				
				// 累计登录天数
				if (crossDay >= 1) {
					totalLogin += 1;
				}
				
				customData.setArg(String.format("%d_%d", regContinueLoing, totalLogin));
			}
			
			LogUtil.logPlayerLogin(player, regContinueLoing, totalLogin);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 更新头像信息
	 * 
	 * @param playerEntity
	 * @param puidProfile
	 * @return
	 */
	private void updatePfIcon(PlayerEntity playerEntity, String puidProfile) {
		if (!HawkOSOperator.isEmptyString(puidProfile) && !GlobalData.getInstance().isPfPersonInfoCancel(playerEntity.getOpenid())) {
			try {
				JSONObject profileJson = JSON.parseObject(puidProfile);
				JSONObject newProfileJson = checkpPuidProfileUpdate(profileJson);
				if (newProfileJson != null) {
					profileJson = newProfileJson;
				}
				String pfIcon = null;
				if (UserType.getByChannel(player.getChannel()) == UserType.QQ) {
					pfIcon = profileJson.getString("picture100");
					if (HawkOSOperator.isEmptyString(pfIcon)) {
						pfIcon = profileJson.getString("picture40");
					}
				} else if (UserType.getByChannel(player.getChannel()) == UserType.WX) {
					pfIcon = profileJson.getString("picture");
					if (!HawkOSOperator.isEmptyString(pfIcon)) {
						pfIcon = pfIcon + "/96";
					}
				}
				
				// 记下玩家原始头像地址
				player.getData().setPrimitivePfIcon(pfIcon);

				if (!HawkOSOperator.isEmptyString(pfIcon)) {
					String generateCrc = IconManager.getInstance().getPficonCrc(pfIcon);
					String playerIconCrc = player.getData().getIMPfIcon();
					if (!generateCrc.equals(playerIconCrc)) {
						RedisProxy.getInstance().updatePfIcon(player.getPuid(), pfIcon);
					}

					if (HawkOSOperator.isEmptyString(playerIconCrc)) {
						player.getData().setPfIcon(null);
					}
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		} else {
			// 模拟的平台图标
			String pfIcon = RedisProxy.getInstance().getPfIcon(player.getPuid());
			if (HawkOSOperator.isEmptyString(pfIcon)) {
				if ("android".equals(playerEntity.getPlatform()) && "guest".equals(playerEntity.getChannel())) {
					pfIcon = GameConstCfg.getInstance().randomPfIcon();
				}

				if (!HawkOSOperator.isEmptyString(pfIcon)) {
					RedisProxy.getInstance().updatePfIcon(player.getPuid(), pfIcon);
				}
			}
		}
	}
	
	/**
	 * 检查puidProfile信息是否更新
	 * @param profileJson
	 * @return
	 */
	private JSONObject checkpPuidProfileUpdate(JSONObject profileJson) {
		if (!GlobalData.getInstance().isPuidProfileCtrlExist(player.getOpenId())) {
			return null;
		}
		
		Integer expireTimeInt = profileJson.getInteger("expireTimeInGame");
		// 上一次更新后，还没有到过期时间，不判断
		if (expireTimeInt != null && HawkTime.getSeconds() < expireTimeInt.intValue()) {
			return null;
		}
		JSONObject pfInfoJson = player.getPfTokenJson();
		Map<String, String> params = new HashMap<String, String>();
		params.put("channel", player.getChannel());
		JSONObject newProfileJson = SDKManager.getInstance().fetchProfile(SDKConst.SDKType.MSDK, params, pfInfoJson, player.getSession().getAddress());
		if (newProfileJson == null) {
			return null;
		}
		
		newProfileJson.put("expireTimeInGame", HawkTime.getSeconds() + 3600 * 12); //信息更新的同时，设置12小时的过期时间
		RedisProxy.getInstance().updatePuidProfile(player.getPuid(), newProfileJson.toJSONString());
		return newProfileJson;
	}

	/**
	 * 更新玩家的活跃标记
	 * 
	 * @param playerEntity
	 */
	private void updateLoginMask(PlayerEntity playerEntity) {
		// 计算这是第几天登录
		Calendar calendar = HawkTime.getCalendar(true);
		calendar.setTimeInMillis(playerEntity.getCreateTime());
		long dayDiff = HawkTime.calendarDiff(HawkTime.getCalendar(false), calendar);
		if (dayDiff <= 60) {
			long loginMask = (1 << dayDiff) | playerEntity.getLoginMask();
			playerEntity.setLoginMask(loginMask);
		}
	}

	/**
	 * 更新最近7天活跃标记
	 * 
	 * @param playerEntity
	 */
	private void updateLast7DayLively(PlayerEntity playerEntity, boolean isLogin) {
		try {
			long lastLogoutTime = playerEntity.getLogoutTime();
			if (lastLogoutTime == 0) {
				lastLogoutTime = HawkTime.getMillisecond();
			}

			// 最近七天活跃
			Calendar lastLogoutCalendar = HawkTime.getCalendar(true);
			lastLogoutCalendar.setTimeInMillis(lastLogoutTime);
			int livelyDayDiff = 0;
			int newMask = 0;
			// 在线跨天的时候按一天算, 不在线跨天按最后一次的退出时间算.
			if (isLogin) {
				livelyDayDiff = HawkTime.calendarDiff(HawkTime.getCalendar(false), lastLogoutCalendar);
				ConstProperty constProperty = ConstProperty.getInstance();
				if (livelyDayDiff > constProperty.getFriendCycle()) {
					livelyDayDiff = constProperty.getFriendCycle();
				}
			} else {
				livelyDayDiff = 1;
			}

			newMask = playerEntity.getLivelyMask() << livelyDayDiff;

			// 超过7天的数据不要
			newMask = newMask & 0x7F;
			newMask |= 1;

			playerEntity.setLivelyMask(newMask);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	private void doNewPlayerOrNewDay() {
		// 每天vip登陆增加积分
		int addVipPoint = ConstProperty.getInstance().getLoginVipPoint();
		if (addVipPoint <= 0) {
			return;
		}
		if (player.getData().getVipActivated()) {
			addVipPoint *= ConstProperty.getInstance().getLoginVipBundle();
		}

		AwardItems awardItem = AwardItems.valueOf();
		awardItem.addVipExp(addVipPoint);
		awardItem.rewardTakeAffect(player, Action.LOGIN_GAME);

		player.getEntity().setVipFreePoint(addVipPoint);
	}

	/**
	 * 玩家上线处理
	 *
	 * @return
	 */
	@Override
	protected boolean onPlayerLogin() {
		// 设置玩家登录状态
		player.setActiveState(GsConst.PlayerState.ONLINE);
		player.setBackground(0);
		//player.setCredit(GameUtil.queryCreditScore(player));
		long now = HawkTime.getMillisecond();
		playerLoginUpdateInfo(now);
		
		// 玩家上线后，城市保护罩buf由player自己去tick
		CityManager.getInstance().removeCityShieldInfo(player.getId());
		// 刷新一下战力
		player.getData().getPowerElectric().calcElectricBeforeChange();
		// 第一次登陆的时候 清空上一次的playerInfoBuilder;
		player.getPush().clearLastPlayerInfoBuilder();
		// 同步基础信息
		player.getPush().syncPlayerInfo();

		// 同步聊天屏蔽的玩家信息
		player.getPush().syncShieldPlayerInfo();

		// 同步buff增益效果显示
		player.getPush().syncPlayerStatusInfo(true);
		player.getPush().syncPlayerEffect();
		
		// 全服buff信息
		player.getPush().synGlobalBuffInfo();

		// 记录活跃玩家
		GlobalData.getInstance().addActivePlayer(player);

		// 同步每日数据
		player.getPush().synPlayerDailyData();
		
		// 同步已解锁的行军表情包
		player.getPush().syncMarchEmoticon();

		// 零收益
		if (player.getZeroEarningTime() > now) {
			player.sendIDIPZeroEarningMsg(IdipMsgCode.ZERO_EARNING_LOGIN_VALUE);
		}

		// 发送禁言提示
		if (now < player.getData().getPlayerEntity().getSilentTime()) {
			ChatService.getInstance().sendBanMsgNotice(player, player.getData().getPlayerEntity().getSilentTime());
		} else {
			player.getData().getPlayerEntity().setSilentTime(0);
		}

		// 健康引导信息更新
		healthGameInfoLoginUpdate();
		
		// 手Q成就上报
		loginScoreBatch(now);
		
		// 同步个保法设定开关
		player.getPush().syncPersonalProtectVals();
		
		// 推送服务器活动配置版本信息
		player.getPush().syncActivityCfgVersionInfo();
		
		AccountInfo accountInfo = GlobalData.getInstance().getAccountInfoByPlayerId(player.getId());
		if (!accountInfo.isNewly()) {
			// 登录时拉去钻石数量信息
			pullDiamonds();
			
			// 被大总统禁言信息
			JSONObject presidentSilentInfo = LocalRedis.getInstance().getSilentPlayer(player.getName());
			if (presidentSilentInfo != null) {
				player.getData().setPresidentSilentInfo(presidentSilentInfo);
			}
			
			// 推送IDIP系统消息
			String idipMsg = LocalRedis.getInstance().getIDIPMsg(player.getId());
			if (!HawkOSOperator.isEmptyString(idipMsg)) {
				player.sendIdipMsg(idipMsg);
				LocalRedis.getInstance().delIDIPMsg(player.getId());
			}
			
			// 伊娃红点信息
			String yiwaTipMsg = RedisProxy.getInstance().getYiwaTipMsg(player.getId());
			if (!HawkOSOperator.isEmptyString(yiwaTipMsg)) {
//				//只针对手Q服玩家（最终将数据写入global redis）
//				if (GsConfig.getInstance().getServerId().startsWith("20")) {
//					RedisProxy.getInstance().updateYiwaTipMsg(player.getId(), yiwaTipMsg);
//				}
				player.sendIdipNotice(NoticeType.YIWA_RED_POINT, NoticeMode.NONE, 0, yiwaTipMsg);
			}
			
			
			String passwd = RedisProxy.getInstance().readSecPasswd(player.getId());
			player.setPlayerSecPasswd(passwd);
			if (!HawkOSOperator.isEmptyString(passwd)) {
				PasswdInfoPB.Builder builder = PasswdInfoPB.newBuilder();
				builder.setPasswd(true);
				long ttl = RedisProxy.getInstance().getPasswdTTL(player.getId());
				player.setSecPasswdExpiryTime(ttl);
				if (ttl > 0) {
					long expiryTime = HawkTime.getMillisecond() + ttl * 1000;
					player.setSecPasswdExpiryTime(expiryTime);
					builder.setClosedTime(expiryTime);
				}
				player.sendProtocol(HawkProtocol.valueOf(HP.code.PASSWD_INFO_SYNC, builder));
			}
			
			// 检查月卡附带的自动打野作用号
			checkMonthCardBuff(2);
			// 跨服玩家登录处理
			crossPlayerLoginProcess();
			// 登录初始化相关状态值，用于缓存，防止每次用到时都需要查redis
			RoleExchangeService.getInstance().refreshStatus(player);
		}
		
		// 登录承接(新)状态检测
		InheritNewService.getInstance().onPlayerLogin(player, accountInfo);
		//老玩家回流检测
		BackFlowService.getInstance().onPlayerLogin(player, accountInfo);
		
		// 判断红点开关
		redDotSwitchCheck();
		
		//修复战力数值：因超出int上限导致战力将为负数
		refreshBattlePoint();
		
		//判断玩家版本号 发版本更新奖励
		sendVersionUpReward();
		
		// 行为日志
		BehaviorLogger.log4Service(player, Source.USER_OPERATION, Action.LOGIN_GAME, Params.valueOf("gold", player.getGold()), Params.valueOf("diamonds", player.getDiamonds()),
				Params.valueOf("coin", player.getCoin()), Params.valueOf("level", player.getLevel()), Params.valueOf("vipLevel", player.getVipLevel()),
				Params.valueOf("deviceId", player.getDeviceId()), Params.valueOf("ipaddr", player.getClientIp()));
		return true;
	}
	
	/**
	 * 发送版本更新奖励
	 */
	private void sendVersionUpReward() {
		try{
			HPLogin.Builder loginCmd = player.getHpLogin();
			HawkLog.debugPrintln(" version_reward:{}", JsonFormat.printToString(loginCmd.build()));
			if(null != loginCmd && !HawkOSOperator.isEmptyString(loginCmd.getVersion())){
				List<VersionRewardCfg> canAwardCfgs = new ArrayList<VersionRewardCfg>();
				ConfigIterator<VersionRewardCfg> iter = HawkConfigManager.getInstance().getConfigIterator(VersionRewardCfg.class);
				List<String> awardedVersions = RedisProxy.getInstance().getPlayerRewardVersions(player.getId());
				long curMs = HawkTime.getMillisecond();
				while(iter.hasNext()){
					VersionRewardCfg cfg = iter.next();
					if(loginCmd.getVersion().startsWith(cfg.getVersion()) 
							&& cfg.getStartTimeValue() < curMs 
							&& cfg.getEndTimeValue() > curMs){
						//还没领取过
						if(awardedVersions.stream().filter( vs -> vs.equals(cfg.getVersion())).toArray().length == 0){
							canAwardCfgs.add(cfg);
						}
					}
				}
				if(!canAwardCfgs.isEmpty()){
					//发奖励
					int mailId = ConstProperty.getInstance().getVersionUpRewardMailId();
					for(VersionRewardCfg cfg : canAwardCfgs){
						SystemMailService.getInstance().sendMail(MailParames.newBuilder()
								.setPlayerId(player.getId())
								.setMailId(MailId.valueOf(mailId))
								.setAwardStatus(MailRewardStatus.NOT_GET)
								.addTitles(cfg.getMainTitle())
								.addSubTitles(cfg.getSubTitile())
								.addContents(cfg.getContent())
								.setRewards(cfg.getReward())
								.build());
						awardedVersions.add(cfg.getVersion());
					}
					//存数据库
					RedisProxy.getInstance().setPlayerRewardVersions(player.getId(), String.join(",", awardedVersions ));
				}
			}
		}catch(Exception e){
			HawkException.catchException(e, player.getId() ," reward_version_update error!");
		}
	}
	
	/**
	 * 刷新战力
	 */
	private void refreshBattlePoint() {
		try {
			if (player.getPower() < 0) {
				long oldValue = player.getPower();
				player.getData().getPowerElectric().refreshPowerElectric(player, false, false, PowerChangeReason.OTHER);
				HawkLog.logPrintln("player login refresh power, playerId: {}, openid: {}, oldPower: {}, newPower: {}", player.getId(), player.getOpenId(), oldValue, player.getPower());
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 月卡数据刷新
	 */
	private void checkMonthCardBuff(int cardType) {
		try {
			ActivityMonthCardEntity entity = GameUtil.getMonthCardEntity(player.getId());
			if (entity == null) {
				return;
			}
			
			MonthCardItem monthCardItem = entity.getEfficientCard(cardType);
			if (monthCardItem == null) {
				return;
			}
			
			MonthCardActivityCfg cardCfg = HawkConfigManager.getInstance().getConfigByKey(MonthCardActivityCfg.class, monthCardItem.getCardId());
			if (cardCfg == null) {
				return;
			}
			
			long endTime = cardCfg.getValidEndTime(monthCardItem.getPucharseTime());
			for (int buffId : cardCfg.getBuffList()) {
				BuffCfg buffCfg = HawkConfigManager.getInstance().getConfigByKey(BuffCfg.class, buffId);
				StatusDataEntity statusEntity = player.getData().getStatusById(buffCfg.getEffect());
				if (statusEntity == null || statusEntity.getEndTime() != endTime) {
					statusEntity = player.addStatusBuff(buffId, endTime);
					HawkLog.logPrintln("monthCard login fix buff, playerId: {}, cardId: {}, buffId: {}, endTime: {}", player.getId(), cardCfg.getCardId(), buffId, statusEntity.getEndTime());
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 登录时更新信息
	 * 
	 * @param nowTime
	 */
	private void playerLoginUpdateInfo(long nowTime) {
		String banInfoStr = RedisProxy.getInstance().batchUpdateLoginInfo(player);
		if (!HawkOSOperator.isEmptyString(banInfoStr)) {
			IDIPBanInfo careBanInfo = JSONObject.parseObject(banInfoStr, IDIPBanInfo.class);
			if (careBanInfo != null && careBanInfo.getStartTime() >= nowTime) {
				player.setCareBanStartTime(careBanInfo.getStartTime());
			} else if (careBanInfo == null || careBanInfo.getEndTime() <= nowTime) {
				player.setCareBanStartTime(0);
			}
		}
	}
	
	/**
	 * 登录时检测名字是否包含敏感词
	 */
	private void playerLoginCheckName(boolean sync) {
		AccountInfo accountInfo = GlobalData.getInstance().getAccountInfoByPlayerId(player.getId());
		if (accountInfo.isNewly()) {
			StringBuilder builder = new StringBuilder();
			builder.append("charac_no=").append(player.getId());
			GameTssService.getInstance().wordUicChatFilter(player, player.getName(), MsgCategory.PLAYER_CREATE.getNumber(), GameMsgCategory.PLAYER_NAME_CHECK_LOGIN, String.valueOf(sync ? 1 : 0), 0, builder.toString(), null);
		} else {
			JSONObject json = new JSONObject();
			json.put("alliance_id", player.hasGuild() ? player.getGuildId() : "");
			json.put("param_id", "");
			GameTssService.getInstance().wordUicChatFilter(player, player.getName(), MsgCategory.PLAYER_NAME.getNumber(), GameMsgCategory.PLAYER_NAME_CHECK_LOGIN, String.valueOf(sync ? 1 : 0), json, 0);
		}
		
	}

	/**
	 * 上线时更新健康游戏相关信息
	 */
	private void healthGameInfoLoginUpdate() {
		if (!GlobalData.getInstance().isHealthGameEnable()) {
			return;
		}

		try {
			GameUtil.postHealthGameTask(new HawkCallback() {
				@Override
				public int invoke(Object args) {
					Map<String, Object> paramMap = GameUtil.getHealthReqParam(player);
					String accessToken = player.getAccessToken();
					QueryUserInfoResult queryUserInfoResult = HealthGameManager.getInstance().getUserInfoSingle(player.getOpenId(), player.getId(), accessToken, paramMap);
					HawkLog.debugPrintln("player login health game report result: {}, accessToken: {}, playerId: {}", queryUserInfoResult, accessToken, player.getId());
					// 设置玩家的身份信息：成年或是未成年
					if (queryUserInfoResult != null) {
						player.setAdult(queryUserInfoResult.isAdult());
					}
					
					player.updateRemindTime();
					
					return 0;
				}
			});
			
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 手Q成就上报
	 * 
	 * @param time
	 */
	private void loginScoreBatch(long time) {
		int onlineTimeCurDay = player.getEntity().getOnlineTimeCurDay();
		int day = (0xfffe0000 & onlineTimeCurDay) >> 17;
		int todayOfYear = HawkTime.getYearDay();
		// 不是同一天或不是同一年的同一天
		if (todayOfYear != day || time - player.getEntity().getLogoutTime() > HawkTime.DAY_MILLI_SECONDS) {
			onlineTimeCurDay = (todayOfYear << 17) | 0;
			player.getEntity().setOnlineTimeCurDay(onlineTimeCurDay);
		}

		player.setScoreBatchTime(time);

		if (!GameUtil.isScoreBatchEnable(player)) {
			return;
		}
		
		player.setLastPowerScore(player.getPower());

		// 上报成就积分-最近登录时间
		QQScoreBatch.getInstance().scoreBatch(player, ScoreBatchInfo.valueOf(ScoreType.LATEST_LOGIN_TIME, HawkTime.getSeconds()), ScoreBatchInfo.valueOf(ScoreType.CHANNEL, player.getChannelId()));

		// 对于已加入联盟的玩家，登录时需要上报联盟ID项，而这一项又是每条上报信息固有的字段，故这里不传参数
		if (!HawkOSOperator.isEmptyString(player.getGuildId())) {
			QQScoreBatch.getInstance().scoreBatch(player);
		}

		Map<String, String> map = LocalRedis.getInstance().getAllScoreBatchFlag(player.getId());
		if (!map.isEmpty()) {
			for (Entry<String, String> entry : map.entrySet()) {
				GameUtil.scoreBatch(player, ScoreType.valueOf(Integer.valueOf(entry.getKey())), entry.getValue());
			}

			LocalRedis.getInstance().removeScoreBatchFlag(player.getId());
		}
	}	

	/**
	 * 玩家的离线处理
	 */
	@Override
	protected boolean onPlayerLogout() {
		// 设置下线时间
		long nowTime = HawkTime.getMillisecond();
		player.getEntity().setLogoutTime(nowTime);
		GuildService.getInstance().onPlayerLogout(player.getId(), nowTime);
		LoggerFactory.getLogger("Server").info("voice_room_logger onPlayerLogout playerId:{}", player.getId());
		VoiceRoomManager.getInstance().onPlayerQuit(player.getId());
		
		// 设置活跃标记
		updateLoginMask(getPlayerData().getPlayerEntity());

		playerLogoutInfoStore(nowTime);
		
		// 玩家下线时将破罩时间记录下来，用于tick
		StatusDataEntity cityProtectBufEntity = player.getData().getStatusById(EffType.CITY_SHIELD_VALUE);
		if (Objects.nonNull(cityProtectBufEntity) && cityProtectBufEntity.getEndTime() > nowTime) {
			CityManager.getInstance().addCityShieldInfo(player.getId(), cityProtectBufEntity.getEndTime());
		}

		// 在线时长变更
		changeOnlineTime();

		// 监控游戏检查
		healthGameInfoLogoutUpdate();

		// 上报tlog
		if (!GameUtil.isTlogPuidControlled(player.getOpenId()) && player.getLogoutTime() - player.getLoginTime() > 0) {
			LogUtil.logPlayerLogout(player);
		}

		// 上报积分-vip等级
		GameUtil.scoreBatch(player, ScoreType.VIP_LEVEL, player.getVipLevel());
		
		// 上报积分-战力
		if (player.getPower() != player.getLastPowerScore() && player.getCityLevel() > GameConstCfg.getInstance().getPowerScoreBatchLv()) {
			GameUtil.scoreBatch(player,ScoreType.POWER, player.getPower());
		}

		if (player.getGuildAuthority() == GuildAuthority.L5_VALUE) {
			final String guildId = player.getGuildId();
			GuildService.getInstance().dealMsg(MsgId.GUILD_LEADER_LOGOUT, new GuildLeaderLogoutMsgInvoker(player.getId(), guildId));
		}

		// 行为日志
		BehaviorLogger.log4Service(player, Source.USER_OPERATION, Action.LOGOUT_GAME, Params.valueOf("level", player.getLevel()), Params.valueOf("gold", player.getGold()),
				Params.valueOf("diamonds", player.getDiamonds()), Params.valueOf("coin", player.getCoin()), Params.valueOf("level", player.getLevel()),
				Params.valueOf("vipLevel", player.getVipLevel()));
		
		
		return true;
	}
	
	/**
	 * 下线信息存储
	 * 
	 * @param nowTime
	 */
	@SuppressWarnings("deprecation")
	private void playerLogoutInfoStore(long nowTime) {
		long lastTime = getPlayerData().getPlayerEntity().getLoginTime();
		Boolean isNewDay = !HawkTime.isSameDay(nowTime, lastTime);

		AccountRoleInfo accountRole = null;
		if (player.getEntity().isActive()) {
			accountRole = GlobalData.getInstance().getAccountRoleInfo(player.getId());
			if (accountRole != null) {
				accountRole.openId(player.getOpenId()).playerId(player.getId()).playerName(player.getName()).playerLevel(player.getLevel())
				.cityLevel(player.getCityLevel()).vipLevel(player.getVipLevel()).loginTime(player.getLoginTime())
				.battlePoint(player.getPower()).icon(player.getIcon()).serverId(player.getServerId()).pfIcon(player.getPfIcon())
				.platform(player.getPlatform()).registerTime(player.getCreateTime()).logoutTime(player.getLogoutTime());
			}
		}
		
		RedisProxy.getInstance().batchUpdateLogoutInfo(player, accountRole, nowTime, isNewDay);
	}

	/**
	 * 在线时长变更
	 */
	private void changeOnlineTime() {
		// 在线时长
		int onlineTime = (int) (player.getLogoutTime() - player.getLoginTime()) / 1000;
		onlineTime = onlineTime >= 0 ? onlineTime : 0;
		player.getEntity().setOnlineTimeHistory(player.getOnlineTimeHistory() + onlineTime);
		int continueOnlineSecond = (int)((player.getLogoutTime() - player.getLastContinueOnlineTime()) / 1000);
		if (continueOnlineSecond > 0 && continueOnlineSecond < 360000) { //这里判断大于0且小于100小时，是为了防止continueOnlineSecond值不正常情况下污染正常数据
			RedisProxy.getInstance().idipDailyStatisAdd(player.getId(), IDIPDailyStatisType.ONLINE_TIME, continueOnlineSecond);
		}
		
		int onlineTimeCurDay = player.getEntity().getOnlineTimeCurDay();
		int day = (0xfffe0000 & onlineTimeCurDay) >> 17;
		int todayOfYear = HawkTime.getYearDay();
		int onlineTimeToday = 0x0001ffff & onlineTimeCurDay;
		
		// 不是同一天
		if (todayOfYear != day) {
			onlineTimeToday = Math.max(0, (int) (player.getEntity().getLogoutTime() - HawkTime.getAM0Date().getTime()) / 1000);
			GuildRankMgr.getInstance().onPlayerOnlineTMChange(player.getId(), player.getId(), onlineTimeToday );
		} else {
			onlineTimeToday += onlineTime ;
			GuildRankMgr.getInstance().onPlayerOnlineTMChange(player.getId(), player.getId(), onlineTime );
		}
		
		onlineTimeCurDay = (todayOfYear << 17) | onlineTimeToday;
		player.getEntity().setOnlineTimeCurDay(onlineTimeCurDay);
		GameUtil.scoreBatch(player, ScoreType.DAIY_GAME_TIME, onlineTimeToday);
	}

	/**
	 * 下线时更新健康游戏信息
	 */
	private void healthGameInfoLogoutUpdate() {
		GameUtil.postHealthGameTask(new HawkCallback() {
			@Override
			public int invoke(Object args) {
				Map<String, Object> paramMap = GameUtil.getHealthReqParam(player);
				int periodTime = (int) ((player.getLogoutTime() - player.getHealthGameUpdateTime()) / 1000);
				if (periodTime > 0) {
					PushEndGameResult result = HealthGameManager.getInstance().pushUserEndGame(player.getOpenId(), periodTime, paramMap);
					HawkLog.logPrintln("player logout health game report result: {}", result);
				}
				player.setNextRemindTime(0);
				return 0;
			}
		});
	}

	/**
	 * 玩家跨天消息事件
	 * 
	 * @param msg
	 * @return
	 */
	@MessageHandler
	public boolean onPlayerAcreossDayLogin(PlayerAcrossDayLoginMsg msg) {
		processPlayerAcreossDayLogin(msg.isLogin());
		// 更新登录标记,活跃
		this.updateLoginMask(player.getData().getPlayerEntity());
		this.updateLast7DayLively(player.getData().getPlayerEntity(), false);
		// 设置重置时间标记已经处理过了
		player.getData().getPlayerEntity().setResetTime(HawkTime.getMillisecond());
		
		// 跨天自动重置启动方式，推送作用号变化
		updateLoginWay(LoginWay.COMMON_LOGIN);
		getPlayerData().getPlayerEffect().clearEffectPlat(player);
		
		ZonineSDK.getInstance().opDataReport(OpDataType.ACTIVE_USER, player.getOpenId(), 1);

		return true;
	}

	/**
	 * 登录时同步钻石数量
	 */
	private void pullDiamonds() {
		
		// 登陆游戏后异步拉取玩家钻石信息
		if (SDKManager.getInstance().isPayOpen()) {
			try {
				int saveAmt = player.checkBalance();
				if (saveAmt >= 0) {
					pullDiamondSuccess();
				} else {
					// 拉取时间间隔
					int[] times = SDKManager.getInstance().getFetchBalanceTime();
					// 执行延时任务
					int threadIdx = player.getXid().getHashThread(HawkTaskManager.getInstance().getExtraThreadNum());
					HawkTaskManager.getInstance().postExtraTask(new HawkDelayTask(times[0] * 1000, 0, times.length) {
						@Override
						public Object run() {
							int saveAmt = player.checkBalance();
							if (saveAmt < 0) {
								if (getTriggerCount() < times.length - 1) {
									setTriggerPeriod(times[getTriggerCount() + 1] * 1000);
								}

								return null;
							}

							// 拉取钻石成功，延迟任务结束执行
							setFinish();
							pullDiamondSuccess();
							return null;
						}
					}, threadIdx);
				}

				player.idipChangeDiamonds(false);

			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
	}

	/**
	 * 拉取钻石成功后处理
	 */
	private void pullDiamondSuccess() {
		player.getPush().syncHasFirstRecharge();

		if (player.getPlayerBaseEntity().getSaveAmt() != player.getPlayerBaseEntity()._getChargeAmt()) {
			HawkLog.logPrintln("midas saveAmt gt player chargeAmt, playerId: {}, midas saveAmt: {}, player chargeAmt: {}", player.getId(),
					player.getPlayerBaseEntity().getSaveAmt(), player.getPlayerBaseEntity()._getChargeAmt());
		}

		List<MoneyReissueEntity> entityList = player.getData().getMoneyReissueEntityList();
		if (entityList.isEmpty()) {
			return;
		}

		List<MoneyReissueEntity> loopEntities = new ArrayList<>(entityList);
		for (MoneyReissueEntity entity : loopEntities) {
			if (entity.getCreateTime() <= 0) {
				continue;
			}
			
			// 先删除记录
			entity.delete();

			// 具体操作
			if (entity.getCount() > 0) {
				// 补偿   赠送原因  customer_compensation
				player.increaseDiamond(entity.getCount(), Action.getAction(entity.getSource()), entity.getReissueParam(), DiamondPresentReason.COMPENSATION);
			} else {
				// 扣除
				int diamondsBefore = player.getDiamonds();
				int positive = Math.abs(entity.getCount());
				ConsumeItems consume = ConsumeItems.valueOf(PlayerAttr.DIAMOND, diamondsBefore > positive ? positive : diamondsBefore);
				if (consume.checkConsume(player)) {
					consume.consumeAndPush(player, Action.getAction(entity.getSource()));
				}
				
				if (positive > diamondsBefore) {
					String key = "todoSubDiamonds:" + player.getId();
					RedisProxy.getInstance().getRedisSession().increaseBy(key, positive - diamondsBefore, 0);
					HawkLog.logPrintln("reissue dimonds subcrease record success, playerId: {}, diamonds: {}", player.getId(), positive - diamondsBefore);
				} 
			}

			HawkLog.logPrintln("reissue dimonds success, playerId: {}, reissue diamonds: {}, player diamonds: {}", player.getId(), entity.getCount(), player.getDiamonds());
		}

		entityList.removeAll(loopEntities);
	}

	/**
	 * 处理玩家跨天登录(包括登录跨天和在线跨天)
	 * 
	 * @param isLogin
	 * @param isNewDay
	 */
	private void processPlayerAcreossDayLogin(boolean isLogin) {
		long now = HawkTime.getMillisecond();
		long loginTime = player.getLoginTime();
		boolean isCrossDay = !(HawkTime.isSameDay(now, player.getEntity().getResetTime()));
		// 是否跨服状态玩家
		boolean isCrossPlayer = false;// CrossService.getInstance().isCrossPlayer(player.getId());
		// 非跨服状态下玩家跨天状态
		boolean isActivityCross = !isCrossPlayer && !(HawkTime.isSameDay(now, player.getEntity().getActResetTime()));
		
		// 如果玩家的活动重置时间未初始化,则此时的活动跨天状态以玩家跨天状态为准,且刷新活动跨天状态时间
		if(player.getEntity().getActResetTime() == 0){
			isActivityCross = isCrossDay;
			player.getEntity().setActResetTime(now);
		}
		
		// 非跨服状态玩家刷新活动时间记录
		if (!isCrossPlayer) {
			player.getEntity().setActResetTime(now);
		}
		StatisticsEntity statisticsEntity = player.getData().getStatisticsEntity();
		if(player.hasGuild()){
			GuildService.getInstance().memberDailyLoginCheck(player.getId(), player.getGuildId());
			// 联盟签到信息
			GuildService.getInstance().syncGuildSignatureInfo(player);
		}
		
		// 跨天处理
		if (isCrossDay) {
			HawkLog.logPrintln("cross day login check, loginTime: {}, now: {}, isLogin: {}", loginTime, now, isLogin);
			statisticsEntity.addLoginDay(1);
			statisticsEntity.setCommonStatisData(StatisticDataType.GROUP_PVP_TOTAL_TODAY, 0);
			statisticsEntity.setCommonStatisData(StatisticDataType.GROUP_TOTAL_TODAY, 0);
			statisticsEntity.setCommonStatisData(StatisticDataType.COLLECT_RES_TODAY, 0);
			statisticsEntity.setCommonStatisData(StatisticDataType.ATK_GHOST_TOTAL_TODAY, 0);
			statisticsEntity.setCommonStatisData(StatisticDataType.PVE_TOTAL_TODAY, 0);
			MissionManager.getInstance().postMsg(player, new EventDailyFirstLogin());
//			AccountInfo accountInfo = GlobalData.getInstance().getAccountInfoByPlayerId(player.getId());
//			// 登录时校验玩家角色名称
//			if (!accountInfo.isNewly()) {
//				playerLoginCheckName(true);
//			}
		}
		
		ActivityManager.getInstance().postEvent(new ContinueLoginEvent(player.getId(), statisticsEntity.getLoginDay(), 0, isLogin, isActivityCross));
	}

	/**
	 * 更新设备信息
	 *
	 * @param deviceId
	 * @return
	 */
	protected boolean updateDeviceInfo(String deviceId) {
		try {
			if (!HawkOSOperator.isEmptyString(deviceId) && !player.getEntity().getDeviceId().equals(deviceId)) {
				player.getEntity().setDeviceId(deviceId);
			}
			return true;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return false;
	}
	
	/**
	 * 更新登录方式
	 * 
	 * @param loginWay
	 */
	protected void updateLoginWay(LoginWay loginWay) {
		int oldLoginWayAndDay = player.getEntity().getLoginWayAndDay();
		int newLoginWayAndDay = oldLoginWayAndDay;
		int day = (0xffff0000 & oldLoginWayAndDay) >> 16;
  		int todayOfYear = HawkTime.getYearDay();
  		int oldWay = 0x0000ffff & oldLoginWayAndDay;
  		// 不是同一天, 或当天之前是普通登录，但此次是其他方式登录
  		if (todayOfYear != day || (oldWay == LoginWay.COMMON_LOGIN_VALUE && loginWay.getNumber() != oldWay)) {
  			newLoginWayAndDay = (todayOfYear << 16) | loginWay.getNumber();
  		}
		player.setLoginWay(loginWay); 
  		player.getEntity().setLoginWayAndDay(newLoginWayAndDay);
	}

	/**
	 * 更新移动终端信息
	 * 
	 * @param phoneInfo
	 * @return
	 */
	protected boolean updatePhoneInfo(String phoneInfo) {
		try {
			if (!HawkOSOperator.isEmptyString(phoneInfo) && !phoneInfo.equals(player.getPhoneInfo())) {
				RedisProxy.getInstance().updatePlayerPhoneInfo(player.getId(), phoneInfo);
			}
			return true;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return false;
	}

	/**
	 * 更新推送信息
	 */
	protected boolean updatePushInfo(String pushInfo, String lang) {
		try {
			if (!HawkOSOperator.isEmptyString(pushInfo) && !HawkOSOperator.isEmptyString(lang)) {
				JSONObject json = JSONObject.parseObject(pushInfo);
				if (!HawkOSOperator.isEmptyString(json.getString("pushDeviceId"))) {
					json.put("lang", lang);
					CustomDataEntity customData = player.getData().getCustomDataEntity(GsConst.PUSH_INFO_KEY);
					if (customData == null) {
						customData = player.getData().createCustomDataEntity(GsConst.PUSH_INFO_KEY, 0, json.toString());
					} else {
						customData.setArg(json.toString());
					}
				}
			}
			return true;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return false;
	}

	/**
	 * 更新客户端当前版本信息
	 */
	protected boolean updateVersion(String version) {
		if (!HawkOSOperator.isEmptyString(version)) {
			player.getEntity().setVersion(version);
		}
		return true;
	}

	/**
	 * 获取玩家信息
	 * 
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.PLAYER_FACADE_INFO_REQ_VALUE)
	private void onPresidentFacadeReq(HawkProtocol protocol) {
		PlayerFacadeInfoReq cparam = protocol.parseProtocol(PlayerFacadeInfoReq.getDefaultInstance());
		String playerId = cparam.getPlayerId();
		Player pbBuilder = GlobalData.getInstance().makesurePlayer(playerId);
		PlayerFacadeInfoResp.Builder sbuilder = PlayerFacadeInfoResp.newBuilder();
		if (pbBuilder != null) {
			sbuilder.setPlayerMsg(BuilderUtil.genMiniPlayer(pbBuilder));
			sbuilder.setGuildName(pbBuilder.getGuildName());
			sbuilder.setGuildTag(pbBuilder.getGuildTag());
			sbuilder.setFrom(cparam.getFrom());
		} else {
			Player.logger.error("playerId:{} not found ", playerId);
			
			return;
		}

		player.sendProtocol(HawkProtocol.valueOf(HP.code.PLAYER_FACADE_INFO_RESP_VALUE, sbuilder));
	}

	/**
	 * 初始化世界装扮
	 */
	private void initWorldDressShow() {
		DressEntity dressEntity = player.getData().getDressEntity();
		Map<Integer, Integer> initWorldDressShowMap = WorldMapConstProperty.getInstance().getInitWorldDressShowMap();
		for (Entry<Integer, Integer> initWorldDressShow : initWorldDressShowMap.entrySet()) {
			dressEntity.addOrUpdateDressInfo(initWorldDressShow.getKey(), initWorldDressShow.getValue(), GsConst.PERPETUAL_MILL_SECOND);
			WorldPointService.getInstance().updateShowDress(player.getId(), initWorldDressShow.getKey(),
					dressEntity.getDressInfo(initWorldDressShow.getKey(), initWorldDressShow.getValue()));
		}
	}
	
	/**
	 * 红点开关检测
	 */
	private void redDotSwitchCheck() {
		// 跨服玩家不处理
		if (!player.getMainServerId().equals(GsConfig.getInstance().getServerId())) {
			return;
		}
		
		List<CustomKeyCfg> cfgList = CustomKeyCfg.getIdipRedDotSwitchKeys();
		for (CustomKeyCfg cfg : cfgList) {
			try {
				CustomDataEntity entity = player.getData().getCustomDataEntity(cfg.getKey());
				long time = GlobalData.getInstance().getRedDotSwitchOpenTime(cfg.getIdipRedDotType());
				if (time == 0) {
					// idip开关已经去掉了, 但个人身上还有记录且没有点开过，置为1表示已点开过
					if (entity != null && entity.getValue() == 0) {
						entity.setValue(1);
					}
					continue;
				}
				
				// 第一次开启，个人身上还没有记录
				if (entity == null) {
					player.getData().createCustomDataEntity(cfg.getKey(), 0, String.valueOf(time));
					continue;
				} 
				
				// 不是此次开启的, 将数据更新一下
				if (!HawkOSOperator.isEmptyString(entity.getArg()) && Long.parseLong(entity.getArg()) < time) {
					entity.setArg(String.valueOf(time));
					entity.setValue(0);
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
	} 
	
	/**
	 * 跨服玩家登录处理
	 */
	private void crossPlayerLoginProcess() {
		if (!player.isCsPlayer()) {
			return;
		}
		
		try {
			String key = RedisKey.IDIP_GM_RECHARGE + ":loginProcess:" + player.getId();
			String todayymd = String.valueOf(HawkTime.getYyyyMMddIntVal());
			String rechargeDiamonds = RedisProxy.getInstance().getRedisSession().hGet(key, todayymd);
			if (HawkOSOperator.isEmptyString(rechargeDiamonds)) {
				return;
			}
			
			RedisProxy.getInstance().getRedisSession().del(key);
			//跨服玩家当天首次登录，只需要在ContinueLoginEvent事件中处理即可，此处不用再抛事件，否则会累计双份
			if (!HawkTime.isSameDay(HawkTime.getMillisecond(), player.getEntity().getLastLoginTime())) {
				return;
			}
			int diamondsTotal = Integer.parseInt(rechargeDiamonds);
			if (diamondsTotal > 0) {
				ActivityManager.getInstance().postEvent(new IDIPGmRechargeEvent(player.getId(), diamondsTotal));
				int rmb = diamondsTotal / 10;
				if (diamondsTotal % 10 > 0) {
					rmb += 1;
				}
				ActivityManager.getInstance().postEvent(new RechargeAllRmbEvent(player.getId(), rmb));
			}
			HawkLog.logPrintln("cross player login process, playerId: {}, diamondsTotal: {}", player.getId(), diamondsTotal);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 大区合并后玩家首次登录，判断是否需要改名字
	 */
	@SuppressWarnings("deprecation")
	protected void checkNameAfterMergeArea() {
		try {
			String serverId = player.getServerId();
			int areaId = Integer.parseInt(serverId) / 10000;
			//非手Q区合过来的玩家不需要动
			if (areaId != 2) {
				return;
			}
			String playerName = player.getName();
			String playerId = GlobalData.getInstance().getPlayerIdByName(playerName);
			//是自己占用了这个名字，不需要动
			if (player.getId().equals(playerId)) {
				return;
			}
			
			//这个名字还没被占用
			if (HawkOSOperator.isEmptyString(playerId)) {
				if (GlobalData.getInstance().tryOccupyOrUpdatePlayerName(player.getId(), playerName)) {
					return;
				}
			}
			
			//到了这一步，需要改名字了
			String beforeName = player.getName();
			playerName = LoginUtil.randomPlayerName(player.getId(), player.getPuid());
			PlayerOperationModule module = player.getModule(GsConst.ModuleType.OPERATION_MODULE);
			module.changeName(playerName, Action.PLAYER_CHANGE_NAME, 0);
			
			HawkLog.logPrintln("login check, player name repeated, playerId: {}, beforeName: {}, afterName: {}", player.getId(), beforeName, playerName);
			player.getPush().syncPlayerInfo();
			LoginandRename.Builder builder = LoginandRename.newBuilder();
			builder.setBeforeName(beforeName);
			builder.setAfterName(playerName);
			player.sendProtocol(HawkProtocol.valueOf(HP.code.PLAYER_NAME_CONTAIN_SENS_VALUE, builder));
			
			String key = "change_name_card_send:" + player.getId() + ":" + HawkTime.getYyyyMMddIntVal();
			String times = RedisProxy.getInstance().getRedisSession().getString(key);
			int limit = ConstProperty.getInstance().getLoginandRenameDailyTime();
			// 没有达到次数则发邮件
			if (HawkOSOperator.isEmptyString(times) || Integer.parseInt(times) < limit) {
				RedisProxy.getInstance().getRedisSession().increaseBy(key, 1, 86400);
				SystemMailService.getInstance().sendMail(MailParames.newBuilder()
						.setPlayerId(player.getId())
						.setMailId(MailId.LOGIN_CHANGE_NAME)
						.setAwardStatus(MailRewardStatus.NOT_GET)
						.setRewards(ConstProperty.getInstance().getLoginandRename())
						.build());
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
}
