package com.hawk.game.module;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.hawk.activity.event.impl.OnlineMinuteEvent;
import com.hawk.game.msg.xhjz.XHJZExitCrossInstanceMsg;
import com.hawk.game.msg.xqhx.XQHXExitCrossInstanceMsg;
import com.hawk.game.protocol.*;
import com.hawk.game.service.tblyTeam.TBLYSeasonService;
import com.hawk.game.service.tblyTeam.TBLYWarService;
import com.hawk.game.service.xhjzWar.XHJZWarService;
import com.hawk.game.service.xqhxWar.XQHXWarService;
import org.apache.commons.collections4.CollectionUtils;
import org.hawk.annotation.MessageHandler;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.app.HawkApp;
import org.hawk.callback.HawkCallback;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.redis.HawkRedisSession;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.MsgCallBack;
import com.hawk.activity.msg.ActivityCallBackMsg;
import com.hawk.activity.type.impl.exchangeDecorate.ExchangeDecorateActivity;
import com.hawk.activity.type.impl.exchangeDecorate.entity.ActivityExchangeDecorateEntity;
import com.hawk.activity.type.impl.order.activityOrderTwo.OrderTwoActivity;
import com.hawk.activity.type.impl.order.activityOrderTwo.entity.OrderTwoEntity;
import com.hawk.common.AccountRoleInfo;
import com.hawk.common.BillboardInfo.BillboardScene;
import com.hawk.common.service.BillboardService;
import com.hawk.game.GsApp;
import com.hawk.game.GsConfig;
import com.hawk.game.cfgElement.ArmourEffObject;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.config.CustomKeyCfg;
import com.hawk.game.config.GameConstCfg;
import com.hawk.game.crossproxy.model.CsPlayer;
import com.hawk.game.data.ActivityScoreParamsInfo;
import com.hawk.game.entity.ArmourEntity;
import com.hawk.game.entity.CustomDataEntity;
import com.hawk.game.entity.GuildMemberObject;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.global.LoginStatis;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.invoker.RemoveCityShieldMsgInvoker;
import com.hawk.game.log.DungeonRedisLog;
import com.hawk.game.manager.IconManager;
import com.hawk.game.module.dayazhizhan.playerteam.msg.DYZZExitCrossInstanceMsg;
import com.hawk.game.module.dayazhizhan.playerteam.service.DYZZService;
import com.hawk.game.module.lianmengyqzz.march.msg.YQZZExitCrossInstanceMsg;
import com.hawk.game.module.lianmengyqzz.march.service.YQZZMatchService;
import com.hawk.game.msg.UpdateQQVipLevelMsg;
import com.hawk.game.msg.cyborg.CyborgExitCrossInstanceMsg;
import com.hawk.game.msg.starwars.StarWarsExitCrossInstanceMsg;
import com.hawk.game.msg.tiberium.TiberiumPrepareExitCrossInstanceMsg;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.protocol.Cross.CrossType;
import com.hawk.game.protocol.Login.HPASAInfoNotify;
import com.hawk.game.protocol.Login.HPAssembleFinish;
import com.hawk.game.protocol.Login.HPBackgroundNotice;
import com.hawk.game.protocol.Login.MergeServerInfo;
import com.hawk.game.protocol.Login.MergeServerInfoSync;
import com.hawk.game.protocol.Player.ChangeVipFlagReq;
import com.hawk.game.protocol.Player.GetRealPficonUrlReq;
import com.hawk.game.protocol.Player.GetRealPficonUrlResp;
import com.hawk.game.protocol.Player.HealthGameReportReq;
import com.hawk.game.protocol.Player.ReportType;
import com.hawk.game.protocol.SysProtocol.HPCustomDataDefine;
import com.hawk.game.protocol.SysProtocol.KVData;
import com.hawk.game.protocol.SysProtocol.ModuleGuideData;
import com.hawk.game.protocol.SysProtocol.PfTokenUpdateReq;
import com.hawk.game.queryentity.AccountInfo;
import com.hawk.game.service.ActivityService;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.PlayerImageService;
import com.hawk.game.service.SearchService;
import com.hawk.game.service.cyborgWar.CyborgWarService;
import com.hawk.game.service.starwars.StarWarsActivityService;
import com.hawk.game.util.BuilderUtil;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.LogUtil;
import com.hawk.game.util.LoginUtil;
import com.hawk.game.util.GsConst.IDIPDailyStatisType;
import com.hawk.game.world.service.WorldPlayerService;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.gamelog.GameLog;
import com.hawk.gamelog.LogParam;
import com.hawk.health.HealthGameManager;
import com.hawk.health.entity.ReportRemindedInfo;
import com.hawk.log.LogConst.LogInfoType;
import com.hawk.sdk.SDKConst.UserType;
import com.hawk.sdk.SDKManager;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 空闲模块, 所有模块最后操作
 *
 * @author hawk
 */
public class PlayerIdleModule extends PlayerModule {
	/**
	 * 协议日志记录器
	 */
	private static final Logger logger = LoggerFactory.getLogger("Server");
		
	/**
	 * 构造
	 *
	 * @param player
	 */
	public PlayerIdleModule(Player player) {
		super(player);
	}

	/**
	 * 空闲情况下的心跳相应
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.sys.HEART_BEAT_VALUE)
	private boolean onHeartBeat(HawkProtocol protocol) {
		LoginUtil.onPlayerHeartBeat(protocol);
		return true;
	}
	
	/**
	 * 用户存储自定义数据
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.sys.CUSTOM_DATA_DEFINE_VALUE)
	private boolean onCustomDataDefine(HawkProtocol protocol) {
		HPCustomDataDefine request = protocol.parseProtocol(HPCustomDataDefine.getDefaultInstance());
		KVData kvData = request.getData();
		String key = kvData.getKey();
		if (!GameUtil.isCustomKey(key)) {
			logger.info("customKey not exist, playerId: {}, key: {}", player.getId(), key);
			this.sendError(protocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);
			return true;
		}
		
		String args = kvData.getArg();
		// 设置的值进行定义
		if (!HawkOSOperator.isEmptyString(args) && args.length() >= GameConstCfg.getInstance().getCustomDataLimit()) {
			logger.error("cutomeData too long playerId:{},data:{}", player.getId(), args);
			this.sendError(protocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);
			return true;			
		}

		// 记录新手引导打点日志
		if(logNewBieStep(player, key, args) && !CustomKeyCfg.getTutorialKey().equals(key)) {
			args = String.valueOf(GsConst.NEWBIE_COMPLETE_VALUE);
			key = CustomKeyCfg.getTutorialKey();
			// 触发保护罩相关问题判断
			player.dealMsg(MsgId.REMOVE_CITY_SHIELD, new RemoveCityShieldMsgInvoker(player));
		}
		
		// 联盟坐标指引
		if (key.equals("guildPoint")) {
			String guildPointGuideInfo = LocalRedis.getInstance().getGuildPointGuideInfo(player.getId());
			if (guildPointGuideInfo == null) {
				LocalRedis.getInstance().updateGuildPointGuideInfo(player.getId());
				GuildService.getInstance().sendGuildPointGuideAward(player);
			}
		}
		
		CustomDataEntity entity = player.getData().getCustomDataEntity(key);
		if (entity == null) {
			entity = player.getData().createCustomDataEntity(key, kvData.getVal(), args);
		} else {
			entity.setValue(kvData.getVal());
			if (!HawkOSOperator.isEmptyString(args)) {
				entity.setArg(args);
			}
		}
		
		player.responseSuccess(protocol.getType());
		
		return true;
	}
	
	@ProtocolHandler(code = HP.sys.CUSTOM_MODULE_GUIDE_DATA_DEFINE_VALUE)
	private void onModuleGuideCustomDataDefine(HawkProtocol protocol) {
		ModuleGuideData data = protocol.parseProtocol(ModuleGuideData.getDefaultInstance());
		String key = "moduld_guide";
		CustomDataEntity entity = player.getData().getCustomDataEntity(key);
		if (entity == null) {
			entity = player.getData().createCustomDataEntity(key, 0, "");
		}
		Map<Integer, Integer> stringToMap = SerializeHelper.stringToMap(entity.getArg(), Integer.class, Integer.class, SerializeHelper.ATTRIBUTE_SPLIT, SerializeHelper.SEMICOLON_ITEMS);
		stringToMap.put(data.getChapterId(), data.getStepId());
		String args = SerializeHelper.mapToString(stringToMap, SerializeHelper.ATTRIBUTE_SPLIT, SerializeHelper.SEMICOLON_ITEMS);
		entity.setArg(args);
	}
	
	/**
	 * 客户端给服务器同步买量业务数据上报需要的信息
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.ASA_NOTIFY_C_VALUE)
	private boolean onAsaNotify(HawkProtocol protocol) {
		HPASAInfoNotify notify = protocol.parseProtocol(HPASAInfoNotify.getDefaultInstance());
		String asaInfo = notify.getIosASA();
		if (HawkOSOperator.isEmptyString(asaInfo)) {
			HawkLog.logPrintln("asa nofity failed, playerId: {}, openid: {}", player.getId(), player.getOpenId());
			return false;
		}
		
		String deviceId = player.idfa;
		deviceId = HawkOSOperator.isEmptyString(deviceId) ? "" : deviceId;
		
		JSONObject json = JSONObject.parseObject(asaInfo);
		LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.asa_info);
		logParam.put("deviceId", deviceId)
				.put("clientIp", player.getClientIp())
				.put("clientIpv6", "")
				.put("caid", HawkOSOperator.isEmptyString(player.oaidOrCaid) ? "" : player.oaidOrCaid)
				.put("useragent", HawkOSOperator.isEmptyString(player.useragent) ? "" : player.useragent)
				.put("attribution", json.containsKey("iad-attribution") ? (json.getBooleanValue("iad-attribution") ? 1 : 0) : 0)
				.put("orgName", json.containsKey("iad-org-name") ? json.getString("iad-org-name") : "")
				.put("orgId", json.containsKey("iad-org-id") ? json.getString("iad-org-id") : "")
				.put("campaignID", json.containsKey("iad-campaign-id") ? json.getString("iad-campaign-id") : "")
				.put("campaignName", json.containsKey("iad-campaign-name") ? json.getString("iad-campaign-name") : "")
				.put("clickDate", json.containsKey("iad-click-date") ? json.getString("iad-click-date") : "")
				.put("purchaseDate", json.containsKey("iad-purchase-date") ? json.getString("iad-purchase-date") : "")
				.put("conversionDate", json.containsKey("iad-conversion-date") ? json.getString("iad-conversion-date") : "")
				.put("conversionType", json.containsKey("iad-conversion-type") ? json.getString("iad-conversion-type") : "")
				.put("adgroupID", json.containsKey("iad-adgroup-id") ? json.getString("iad-adgroup-id") : "")
				.put("adgroupName", json.containsKey("iad-adgroup-name") ? json.getString("iad-adgroup-name") : "")
				.put("countryOrRegion", json.containsKey("iad-country-or-region") ? json.getString("iad-country-or-region") : "")
				.put("keyword", json.containsKey("iad-keyword") ? json.getString("iad-keyword") : "")
				.put("keywordID", json.containsKey("iad-keyword-id") ? json.getString("iad-keyword-id") : "")
				.put("keywordMatchtype", json.containsKey("iad-keyword-matchtype") ? json.getString("iad-keyword-matchtype") : "")
				.put("creativesetID", json.containsKey("iad-creativeset-id") ? json.getString("iad-creativeset-id") : "")
				.put("creativesetName", json.containsKey("iad-creativeset-name") ? json.getString("iad-creativeset-name") : "")
				.put("attributionError", 0)
				.put("claimType", json.containsKey("claimType") ? json.getString("claimType") : "")
				.put("impressionDate", json.containsKey("impressionDate") ? json.getString("impressionDate") : "");
		GameLog.getInstance().info(logParam);
		return true;
	}
	
	/**
	 * 前端切入后台通知
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.BACKGROUND_NOTICE_C_VALUE)
	private boolean onBackgroundNotice(HawkProtocol protocol) {
		HPBackgroundNotice notice = protocol.parseProtocol(HPBackgroundNotice.getDefaultInstance());
		player.setBackground(notice.getSwitchToBackground() ? HawkTime.getMillisecond() : 0);
		return true;
	}
	
	/**
	 * 平台token信息更新
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.PFTOKEN_UPDATE_C_VALUE)
	private boolean onPfTokenUpdate(HawkProtocol protocol) {
		PfTokenUpdateReq req = protocol.parseProtocol(PfTokenUpdateReq.getDefaultInstance());
		String pfToken = req.getPfToken();
		try {
			String oldAccessToken = player.getAccessToken();
			JSONObject pfInfoJson = JSONObject.parseObject(pfToken);
			
			// 避免token盗用
			String openid = (String) pfInfoJson.get("open_id");
			if (!player.getOpenId().equals(openid)) {
				HawkLog.errPrintln("player update pftoken-openid error, self openid: {}, pfToken openid: {}", player.getOpenId(), openid);
				return true;
			}
			
			player.setPfTokenJson(pfInfoJson);
			
			String newAccessToken = player.getAccessToken();
			if (HawkOSOperator.isEmptyString(oldAccessToken) || !oldAccessToken.equals(newAccessToken)) {
				if (SDKManager.getInstance().isPayOpen()) {
					int diamonds = player.getPlayerBaseEntity().getDiamonds();
					player.checkBalance();
					if (diamonds != player.getPlayerBaseEntity().getDiamonds()) {
						player.getPush().syncPlayerDiamonds();
					}
				}
				
				HawkLog.logPrintln("update accessToken success, playerId: {}, oldToken: {}, newToken: {}", player.getId(), oldAccessToken, newAccessToken);
			}
			
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		return true;
	}
	
	
	/**
	 * 组装完成的同步
	 */
	@SuppressWarnings("deprecation")
	@Override
	protected boolean onPlayerAssemble() {
		AccountRoleInfo accountRoleInfo = GlobalData.getInstance().getAccountRoleInfo(player.getId());
		if (accountRoleInfo == null) {
			accountRoleInfo = AccountRoleInfo.newInstance().openId(player.getOpenId()).playerId(player.getId())
					.serverId(player.getServerId()).platform(player.getPlatform()).registerTime(player.getCreateTime());
		}
		
		try {
			accountRoleInfo.playerName(player.getName()).playerLevel(player.getLevel()).cityLevel(player.getCityLevel())
			.vipLevel(player.getVipLevel()).battlePoint(player.getPower()).activeServer(GsConfig.getInstance().getServerId())
			.icon(player.getIcon()).loginWay(player.getEntity().getLoginWay()).loginTime(HawkTime.getMillisecond())
			.logoutTime(player.getLogoutTime());
			accountRoleInfo.pfIcon(PlayerImageService.getInstance().getPfIcon(player));
		} catch (Exception e) {
			HawkException.catchException(e, player.getId());
		}
		
		GlobalData.getInstance().addOrUpdateAccountRoleInfo(accountRoleInfo);
		
		if (UserType.getByChannel(player.getChannel()) == UserType.QQ) {
			HawkTaskManager.getInstance().postExtraTask(new HawkTask() {
				@Override
				public Object run() {				
					int level = GameUtil.getQQVipLevel(player);
					// 考虑到QQ vip等级中间发生变化，不管是否为0都需要更新
					HawkApp.getInstance().postMsg(player.getXid(), UpdateQQVipLevelMsg.valueOf(level));
					return null;
				}
			});
		}
		
		LogUtil.logPlayerPower(player);
		
		try {
			if (player.isCsPlayer()) {
				Class<? extends GuildService> clz = GuildService.getInstance().getClass();
				Field field = clz.getDeclaredField("playerGuild");
			    field.setAccessible(true);
			    @SuppressWarnings("unchecked")
				Map<String, GuildMemberObject> playerGuilds = (Map<String, GuildMemberObject>)field.get(GuildService.getInstance());
			    GuildMemberObject memberObj = playerGuilds.get(player.getId());
			    if (memberObj != null) {
			    	playerGuilds.remove(player.getId());
			    	DungeonRedisLog.log(player.getId(), "playerGuild fix");
			    	
			    	String guildId = memberObj.getGuildId();
			    	if (!HawkOSOperator.isEmptyString(guildId)) {
			    		field = clz.getDeclaredField("guildMemberData");
				    	field.setAccessible(true);;
				    	@SuppressWarnings("unchecked")
						Map<String, Set<String>> guildMemberData = (Map<String, Set<String>>)field.get(GuildService.getInstance());
				    	Set<String> guildMembers = guildMemberData.get(guildId);
				    	guildMembers.remove(player.getId());
				    	DungeonRedisLog.log(player.getId(), "guildMemberData fix, guildId:{}", guildId);
			    	}
			    }
			}
		    
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		return true;
	}
	
	/**
	 * 同步QQ vip等级信息
	 */
	@MessageHandler
	private boolean updateQQVipLevel(UpdateQQVipLevelMsg msg) {
		HawkLog.logPrintln("updateQQVipLevel into, playerId: {}, openid: {}, playerName: {}, qqVipLevel: {}", player.getId(), player.getOpenId(), player.getName(), msg.getLevel());
		if (!player.isActiveOnline()) {
			HawkLog.errPrintln("updateQQVipLevel failed, player is offline, playerId: {}", player.getId());
			return false;
		}
		
		AccountRoleInfo roleInfo = GlobalData.getInstance().getAccountRoleInfo(player.getId());
		if (roleInfo != null) {
			roleInfo.setQqSVIPLevel(msg.getLevel());					
			GlobalData.getInstance().addOrUpdateAccountRoleInfo(roleInfo);
		} else {
			HawkLog.errPrintln("updateQQVipLevel failed, accountRoleInfo is null, playerId: {}", player.getId());
		}
		
		return true;
	}
	
	/**
	 * 玩家上线处理
	 *
	 * @return
	 */
	@Override
	protected boolean onPlayerLogin() {

		// 重置新号标记
		AccountInfo accountInfo = GlobalData.getInstance().getAccountInfoByPlayerId(player.getId());
		long currentTime = HawkTime.getMillisecond();
		if (accountInfo != null) {			
			accountInfo.setNewly(false);
			
			// 统计组装时间
			long curTime = currentTime;
			LoginStatis.getInstance().addAssembleTime(curTime - accountInfo.getLoginTime());
			
			// 日志记录
			HawkLog.logPrintln("player assemble time consume, playerId: {}, period: {}", 
					player.getId(), curTime - accountInfo.getLoginTime());
		}
		
		// 老玩家直接走完新手
		resetNewbie();
		resetNewbieChatperSave();
		
		// 同步客户配置信息
		player.getPush().syncClientCfg();
		//玩家身上的标志位
		player.getPush().synPlayerFlag();
		
		try {
			String result = RedisProxy.getInstance().getRedisSession().getString(GsConst.CHARGE_SHOW + ":" + player.getId());
			int val = HawkOSOperator.isEmptyString(result) ? 0 : Integer.parseInt(result);
			CustomDataEntity entity = player.getData().getCustomDataEntity(GsConst.CHARGE_SHOW);
			if (entity == null) {
				entity = player.getData().createCustomDataEntity(GsConst.CHARGE_SHOW, val, "");
			} else {
				entity.setValue(val);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		// 同步用户自定义数据
		player.getPush().syncCustomData();

		// 最后通知客户端所有数据同步组装完成
		HPAssembleFinish.Builder builder = HPAssembleFinish.newBuilder();
		builder.setPlayerId(player.getId());
		builder.setIsDailyFirstLogin(!HawkTime.isSameDay(currentTime, player.getLogoutTime()));
		
		// 系统公告
		JSONArray billboard = BillboardService.getInstance().getActiveBillboard(BillboardScene.AFTER_LOGIN, 
				player.getPlatform(), player.getChannel(), player.getChannelId(), GsConfig.getInstance().getServerId());
		// 个人公告
		JSONArray playerBillboard = BillboardService.getInstance().getPlayerActiveBillboard(player.getId());
		billboard.addAll(playerBillboard);
		
		// 活动公告
		billboard.addAll(ActivityService.getInstance().getActivityBillboard(player.getId()));
		
		if (billboard != null) {
			builder.setBillboard(billboard.toJSONString());
		}
		
		sendProtocol(HawkProtocol.valueOf(HP.code.ASSEMBLE_FINISH_S, builder));
		
		// 更新世界坐标点的活跃状态
		WorldPlayerService.getInstance().setPlayerPointActive(player.getId());

		// 记录活跃玩家
		SearchService.getInstance().addPlayerInfo(player.getName(), player.getId(), true);

		// 检测玩家的tick线程
		GsApp.getInstance().checkObjectTickThread(player);
		
		//同步合服的信息.
		this.synMergeServerInfo();
		if (player.isCsPlayer()) {			
			doCrossLogin();
		}
		
		// 登录时间记录
		if (player.getSession() != null) {
			long costTime = currentTime - player.getSession().getLatestProtoTime(HP.code.LOGIN_C_VALUE);
			// 记录登录同步数据统计
			HawkLog.logMonitor("login statistics, playerId: {}, login costtime: {}, syncSize: {}", player.getId(), costTime, player.getSession().getSendSize());
		}
		
		// 相关活动积分补发的固定逻辑（不是临时代码，请勿删除！！！）
		activityScoreAdd();

		player.setLastContinueOnlineTime(currentTime);
		// 登陆完成日志记录
		logger.info("player login, playerId: {}, puid: {}, playerName: {}, diamonds：{}, gold: {}, coin: {}, level: {}, cityLevel: {}, vip: {}, vit: {}, clientIp: {}, playerHash: {}, dataHash: {}", 
				player.getId(), player.getPuid(), player.getName(), player.getDiamonds(), player.getGold(), player.getCoin(), 
				player.getLevel(), player.getCityLevel(), player.getVipLevel(), player.getVit(), player.getClientIp(), player.hashCode(), player.getData().hashCode());
		
		checkArmourStarAttr();
		return true;
	}

	@Override
	public boolean onTick() {
		long currentTime = HawkApp.getInstance().getCurrentTime();
		long gap = currentTime - player.getLastContinueOnlineTime();
		if(gap < TimeUnit.MINUTES.toMillis(10)){
			return true;
		}
		player.setLastContinueOnlineTime(currentTime);
		ActivityManager.getInstance().postEvent(new OnlineMinuteEvent(player.getId(), 10));
		int continueOnlineSecond = (int) (gap/1000);
		if (continueOnlineSecond > 0 && continueOnlineSecond < 360000) { //这里判断大于0且小于100小时，是为了防止continueOnlineSecond值不正常情况下污染正常数据
			RedisProxy.getInstance().idipDailyStatisAdd(player.getId(), IDIPDailyStatisType.ONLINE_TIME, continueOnlineSecond);
		}
		return true;
	}

	/**
	 * 老玩家跳过新手打点
	 */
	private void resetNewbie() {
		try {
			// 新玩家不管
			if (player.getCreateTime() >= ConstProperty.getInstance().getNewbieVersionTimeValue()) {
				return;
			}
			
			String key = CustomKeyCfg.getTutorialKey();
			CustomDataEntity entity = player.getData().getCustomDataEntity(key);
			String newbieEnd = String.valueOf(GsConst.NEWBIE_COMPLETE_VALUE);
			// 老玩家已完成新手了，也改变
			if (entity != null && newbieEnd.equals(entity.getArg())) {
				return;
			}
			
			if (entity == null) {
				entity = player.getData().createCustomDataEntity(key, 0, "");
			}
			
			entity.setArg(newbieEnd);
			HawkLog.logPrintln("player auto skip newbie, playerId: {}", player.getId());
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	/**
	 * 老玩家跳过新手引导(客户端说把newbie_chapter_save重置为200_12000040就行了)
	 */
	private void resetNewbieChatperSave() {
		try {
			// 新玩家不管
			if (player.getCreateTime() >= ConstProperty.getInstance().getNewbieVersionTimeValue()) {
				return;
			}
			
			String key = "newbie_chapter_save";
			String endFlag = "200_12000040";
			
			CustomDataEntity entity = player.getData().getCustomDataEntity(key);
			// 老玩家已完成新手了，也改变
			if (entity != null && endFlag.equals(entity.getArg())) {
				return;
			}
			
			if (entity == null) {
				entity = player.getData().createCustomDataEntity(key, 0, "");
			}
			
			entity.setArg(endFlag);
			HawkLog.logPrintln("player auto resetNewbieChatperSave, playerId: {}", player.getId());
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	private void doCrossLogin() {
		CsPlayer csPlayer = (CsPlayer)player;
		if (csPlayer.isCrossType(CrossType.TIBERIUM_VALUE)) {
			sendProtocol(HawkProtocol.valueOf(HP.code.TIBERIUM_WAR_CROSS_FINISH_VALUE));
			//如果是泰伯利亚跨服,在这里需要处理.
			if (csPlayer.isFirstCrossServerLogin()) {
				if(TBLYSeasonService.getInstance().isInSeason(player)){
					boolean rlt = TBLYSeasonService.getInstance().joinRoom(player);
					if (!rlt) {
						Player.logger.error("join game fail back to source server id:{}", player.getId());
						TiberiumPrepareExitCrossInstanceMsg crossInstanceMsg = new TiberiumPrepareExitCrossInstanceMsg();
						GsApp.getInstance().postMsg(player.getXid(), crossInstanceMsg);
					}
				}else {
					boolean rlt = TBLYWarService.getInstance().joinRoom(player);
					if (!rlt) {
						Player.logger.error("join game fail back to source server id:{}", player.getId());
						TiberiumPrepareExitCrossInstanceMsg crossInstanceMsg = new TiberiumPrepareExitCrossInstanceMsg();
						GsApp.getInstance().postMsg(player.getXid(), crossInstanceMsg);
					}
				}
			} else {
				if (!player.isInDungeonMap()) {
					Player.logger.error("player login but not in instance id:{}", player.getId());
					TiberiumPrepareExitCrossInstanceMsg crossInstanceMsg = new TiberiumPrepareExitCrossInstanceMsg();
					GsApp.getInstance().postMsg(player.getXid(), crossInstanceMsg);
				}
			}				
		} else if(csPlayer.isCrossType(CrossType.STAR_WARS_VALUE)){
			sendProtocol(HawkProtocol.valueOf(HP.code.STAR_WARS_CROSS_FINISH));
			//如果是星球大战,在这里需要处理.
			if (csPlayer.isFirstCrossServerLogin()) {
				boolean rlt = StarWarsActivityService.getInstance().enterRoom(player);
				if (!rlt) {
					Player.logger.error("join game fail back to source server id:{}", player.getId());
					StarWarsExitCrossInstanceMsg crossInstanceMsg = new StarWarsExitCrossInstanceMsg();
					GsApp.getInstance().postMsg(player.getXid(), crossInstanceMsg);
				}
			} else {
				if (!player.isInDungeonMap()) {
					Player.logger.error("player login but not in instance id:{}", player.getId());
					StarWarsExitCrossInstanceMsg crossInstanceMsg = new StarWarsExitCrossInstanceMsg();
					GsApp.getInstance().postMsg(player.getXid(), crossInstanceMsg);
				}
			}				
		} else if (csPlayer.isCrossType(CrossType.CYBORG_VALUE)) {
			if (csPlayer.isFirstCrossServerLogin()) {
				boolean rlt = CyborgWarService.getInstance().joinRoom(player);
				if (!rlt) {
					Player.logger.error("join game fail back to source server id:{}", player.getId());
					CyborgExitCrossInstanceMsg  msg = new CyborgExitCrossInstanceMsg();
					GsApp.getInstance().postMsg(player.getXid(), msg);
					DungeonRedisLog.log(player.getId(), "join game fail back to source server");
				}
			} else {
				if (!player.isInDungeonMap()) {
					Player.logger.error("player login but not in instance id:{}", player.getId());
					CyborgExitCrossInstanceMsg  msg = new CyborgExitCrossInstanceMsg();
					GsApp.getInstance().postMsg(player.getXid(), msg);
				}
			}
		} else if (csPlayer.isCrossType(CrossType.DYZZ_VALUE)) {
			if (csPlayer.isFirstCrossServerLogin()) {
				boolean rlt = DYZZService.getInstance().joinRoom(player);
				if (!rlt) {
					Player.logger.error("join game fail back to source server id:{}", player.getId());
					DYZZExitCrossInstanceMsg  msg = new DYZZExitCrossInstanceMsg();
					GsApp.getInstance().postMsg(player.getXid(), msg);
					DungeonRedisLog.log(player.getId(), "join game fail back to source server");
				}
			} else {
				if (!player.isInDungeonMap()) {
					Player.logger.error("player login but not in instance id:{}", player.getId());
					DYZZExitCrossInstanceMsg  msg = new DYZZExitCrossInstanceMsg();
					GsApp.getInstance().postMsg(player.getXid(), msg);
				}
			}
		} else if (csPlayer.isCrossType(CrossType.YQZZ_VALUE)) {
			if (csPlayer.isFirstCrossServerLogin()) {
				boolean rlt = YQZZMatchService.getInstance().joinRoom(player);
				if (!rlt) {
					Player.logger.error("join game fail back to source server id:{}", player.getId());
					YQZZExitCrossInstanceMsg  msg = new YQZZExitCrossInstanceMsg();
					GsApp.getInstance().postMsg(player.getXid(), msg);
					DungeonRedisLog.log(player.getId(), "join game fail back to source server");
				}
			} else {
				if (!player.isInDungeonMap()) {
					Player.logger.error("player login but not in instance id:{}", player.getId());
					YQZZExitCrossInstanceMsg  msg = new YQZZExitCrossInstanceMsg();
					GsApp.getInstance().postMsg(player.getXid(), msg);
				}
			}
		} else if (csPlayer.isCrossType(CrossType.XHJZ_VALUE)) {
			if (csPlayer.isFirstCrossServerLogin()) {
				boolean rlt = XHJZWarService.getInstance().joinRoom(player);
				if (!rlt) {
					Player.logger.error("join game fail back to source server id:{}", player.getId());
					XHJZExitCrossInstanceMsg  msg = new XHJZExitCrossInstanceMsg();
					GsApp.getInstance().postMsg(player.getXid(), msg);
					DungeonRedisLog.log(player.getId(), "join game fail back to source server");
				}
			} else {
				if (!player.isInDungeonMap()) {
					Player.logger.error("player login but not in instance id:{}", player.getId());
					XHJZExitCrossInstanceMsg  msg = new XHJZExitCrossInstanceMsg();
					GsApp.getInstance().postMsg(player.getXid(), msg);
				}
			}
		} else if(csPlayer.isCrossType(CrossType.XQHX_VALUE)){
			if (csPlayer.isFirstCrossServerLogin()) {
				boolean rlt = XQHXWarService.getInstance().joinRoom(player);
				if (!rlt) {
					Player.logger.error("join game fail back to source server id:{}", player.getId());
					XQHXExitCrossInstanceMsg msg = new XQHXExitCrossInstanceMsg();
					GsApp.getInstance().postMsg(player.getXid(), msg);
					DungeonRedisLog.log(player.getId(), "join game fail back to source server");
				}
			} else {
				if (!player.isInDungeonMap()) {
					Player.logger.error("player login but not in instance id:{}", player.getId());
					XQHXExitCrossInstanceMsg  msg = new XQHXExitCrossInstanceMsg();
					GsApp.getInstance().postMsg(player.getXid(), msg);
				}
			}
		} else {
			sendProtocol(HawkProtocol.valueOf(CHP.code.CROSS_FINISH_VALUE));
		}
	}

	/**
	 * 玩家下线处理
	 *
	 * @return
	 */
	@Override
	protected boolean onPlayerLogout() {
		// 重置数据
		player.resetParam();

		// 玩家数据对象
		final PlayerData playerData = player.getData();
		
		// 日志记录
		logger.info("player logout, playerId: {}, puid: {}, playerName: {}, diamonds：{}, gold: {}, coin: {}, level: {}, cityLevel: {}, vip: {}, vit: {}, clientIp: {}, playerHash: {}, dataHash: {}", 
				player.getId(), player.getPuid(), player.getName(), player.getDiamonds(), player.getGold(), player.getCoin(), 
				player.getLevel(), player.getCityLevel(), player.getVipLevel(), player.getVit(), player.getClientIp(), player.hashCode(), playerData.hashCode());

		// 最后统计登录所发送的数据量
		int sendSize = 0, recvSize = 0;
		if (player.getSession() != null) {
			sendSize = player.getSession().getSendSize();
			recvSize = player.getSession().getRecvSize();
		}
		HawkLog.logMonitor("logout statistics, playerId: {}, sendSize: {}, recvSize: {}", player.getId(), sendSize, recvSize);

		// 从活跃玩家列表中移除
		GlobalData.getInstance().removeActivePlayer(player.getId());

		// 更新世界坐标点的活跃状态
		WorldPlayerService.getInstance().setPlayerPointActive(player.getId());

		// 清空玩家会话
		player.onSessionClosed();

		// 更新玩家数据访问
		GlobalData.getInstance().notifyPlayerDataAccess(playerData);	
		
		// 如果是一个正在被删除的玩家，走完登出流程后需要清理数据。之所以清理数据的流程要放在这里，是因为玩家的一些退出流程可能需要依赖玩家数据。如果放在前面清理，会报异常
		if (RedisProxy.getInstance().isRemovePlayer(player.getId())) {
			GameUtil.clearAccountRoleData(player);
		}
		
		return true;
	}

	/**
	 * 玩家线程回调消息
	 * @param msg
	 * @return
	 */
	@MessageHandler
	private boolean onCallBack(ActivityCallBackMsg msg) {
		MsgCallBack callBack = msg.getCallBack();
		callBack.execute();
		return true;
	}

	/**
	 * 记录新手引导节点日志
	 * @param player
	 * @param key
	 * @param args
	 * @return
	 */
	private boolean logNewBieStep(Player player, String key, String args) {
		if(!CustomKeyCfg.getTutorialKey().equals(key) && !CustomKeyCfg.getTutorialCompleteKey().equals(key)) {
			return false;
		}
		
		if (GameUtil.isTlogPuidControlled(player.getOpenId())) {
			return true;
		}
		
		try {
			int preStep = GsConst.NEWBIE_FIRST_PRE_STEP;
			CustomDataEntity entity = player.getData().getCustomDataEntity(CustomKeyCfg.getTutorialKey());
			if (entity != null) {
				String[] argArr = entity.getArg().split("_");
				preStep = Integer.parseInt(argArr.length > 1 ? argArr[1] : argArr[0]);
				// 新手引导所有节点已走完
				if (preStep == GsConst.NEWBIE_COMPLETE_VALUE) {
					return true;
				}
			}
			
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.guide_flow);
			if (logParam != null) {
				int step = GsConst.NEWBIE_COMPLETE_VALUE; // 新手引导节点已走完
				if(CustomKeyCfg.getTutorialKey().equals(key) && !HawkOSOperator.isEmptyString(args)){
					String[] argArr = args.split("_");
					step = Integer.parseInt(argArr.length > 1 ? argArr[1] : argArr[0]);
				}
				
				logParam.put("step",step);
				logParam.put("preStep", preStep);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		return true;
	}

	@ProtocolHandler(code = {HP.code.GET_REAL_PFICON_URL_REQ_VALUE})
	private void onGetRealPficonReq(HawkProtocol protocol) {
		GetRealPficonUrlReq cparam = protocol.parseProtocol(GetRealPficonUrlReq.getDefaultInstance());
		
		if (cparam.getIdentifyListList().isEmpty()) {
			return;
		}
		
		GetRealPficonUrlResp.Builder sbuilder = GetRealPficonUrlResp.newBuilder();
		String pficon = null;
		for (String str : cparam.getIdentifyListList()) {
			pficon = IconManager.getInstance().getPficonByCrc(str);
			if (!HawkOSOperator.isEmptyString(pficon)) {
				sbuilder.addIdentifyUrlList(BuilderUtil.buildKeyValuePairStrStr(str, pficon));
			}
		}
		
		HawkProtocol respProtocol = HawkProtocol.valueOf(HP.code.GET_REAL_PFICON_URL_RESP_VALUE, sbuilder);
		player.sendProtocol(respProtocol);
	}
	
	/**
	 * 健康游戏信息提醒时或强制下线信息上报
	 * 
	 */
	@ProtocolHandler(code = {HP.code.HEALTH_GAME_REPORT_C_VALUE})
	private boolean healthGameReportReminded(HawkProtocol protocol) {
		HealthGameReportReq req = protocol.parseProtocol(HealthGameReportReq.getDefaultInstance());
		ReportType reportType = req.getReportType();
		
		GameUtil.postHealthGameTask(new HawkCallback() {
			@Override
			public int invoke(Object args) {
				boolean isAdult = player.isAdult();
				long timeNow = HawkApp.getInstance().getCurrentTime()/1000;
				HawkLog.logPrintln("client report health game info, playerId: {}, type: {}", player.getId(), reportType);
				Map<String, Object> paramMap = GameUtil.getHealthReqParam(player);
				ReportRemindedInfo report = ReportRemindedInfo.valueOf(player.getOpenId(), player.getId(), reportType.getNumber(), timeNow, isAdult);
				HealthGameManager.getInstance().reportReminded(report, paramMap);
				return 0;
			}
		});
		
		return true;
	}
	
	/**
	 * 改变vip flag请求
	 * 
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.CHANGE_VIP_FLAG_REQ_VALUE)
	private void onChangeVipFlagReq(HawkProtocol protocol) {
		ChangeVipFlagReq req = protocol.parseProtocol(ChangeVipFlagReq.getDefaultInstance());
		player.getData().getPlayerEntity().setVipFlag(req.getVipFlag().getNumber());
		player.responseSuccess(protocol.getType());
	}
	
	/**
	 * 同步合服相关信息
	 */
	public void synMergeServerInfo() {
		@SuppressWarnings("deprecation")
		List<String> mergedServerList = GlobalData.getInstance().getMergeServerList(player.getServerId());
		MergeServerInfoSync.Builder sbuilder = MergeServerInfoSync.newBuilder(); 
		if (!CollectionUtils.isEmpty(mergedServerList)) {
			MergeServerInfo.Builder infoBuilder = BuilderUtil.buildMergeServerInfo(mergedServerList.get(0),
						mergedServerList.subList(1, mergedServerList.size()));
			sbuilder.setMergeServerInfo(infoBuilder);
		}
		
		HawkProtocol hawkProtocol = HawkProtocol.valueOf(HP.code.MERGE_SERVER_INFO_SYNC_VALUE, sbuilder);
		this.sendProtocol(hawkProtocol);
	}
	
	/**
	 * 添加活动积分（这个是正式功能，不是临时方法，请勿删除！！！）
	 */
	public void activityScoreAdd() {
		try {
			String fieldKey = "";
			AccountRoleInfo firstRole = null;
			Map<String, String> roleInfoMap = RedisProxy.getInstance().getAccountRole(player.getOpenId());
			for (Entry<String, String> entry : roleInfoMap.entrySet()) {
				AccountRoleInfo roleInfoObj = JSONObject.parseObject(entry.getValue(), AccountRoleInfo.class);
				if (roleInfoObj == null || !roleInfoObj.getPlayerId().equals(player.getId())) {
					continue;
				}
				
				if (HawkOSOperator.isEmptyString(fieldKey)) {
					fieldKey = entry.getKey();
					firstRole = roleInfoObj;
					continue;
				} 
				
				String mapKey = "account_role:" + player.getOpenId();
				if (roleInfoObj.getRegisterTime() <= 0) {
					RedisProxy.getInstance().getRedisSession().hDel(mapKey, entry.getKey());
					HawkLog.logPrintln("player login remove invalid accountRoleInfo, playerId: {}, info: {}", player.getId(), entry.getValue());
				} else if (firstRole.getRegisterTime() <= 0) {
					RedisProxy.getInstance().getRedisSession().hDel(mapKey, fieldKey);
					HawkLog.logPrintln("player login remove invalid accountRoleInfo, playerId: {}, info: {}", player.getId(), firstRole.toString());
				} else if (roleInfoObj.getLoginTime() < firstRole.getLoginTime()) {
					RedisProxy.getInstance().getRedisSession().hDel(mapKey, entry.getKey());
					HawkLog.logPrintln("player login remove invalid accountRoleInfo, playerId: {}, info: {}", player.getId(), entry.getValue());
				} else {
					RedisProxy.getInstance().getRedisSession().hDel(mapKey, fieldKey);
					HawkLog.logPrintln("player login remove invalid accountRoleInfo, playerId: {}, info: {}", player.getId(), firstRole.toString());
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		if (player.isCsPlayer()) {
			return;
		}
		
		// 免费装扮-189, 红警战令-236 
		int[] activityIdArr = {189, 236};
		for (int activityId : activityIdArr) {
			try {
				Map<String, String> paramsMap = LocalRedis.getInstance().getActivityScoreParams(activityId);
				if (paramsMap.isEmpty()) {
					continue;
				}
				
				String paramsInfo = paramsMap.containsKey(player.getId()) ? paramsMap.get(player.getId()) : paramsMap.get(GsConfig.getInstance().getServerId());
				ActivityScoreParamsInfo obj = JSONObject.parseObject(paramsInfo, ActivityScoreParamsInfo.class);
				if (obj == null || obj.getEndTime() < HawkTime.getMillisecond()) {
					continue;
				}
				
				// 这里有个隐患：如果外部平台发送idip请求出现故障，导致间隔性多发请求过来，玩家可以利用这个漏洞多次登录获利，不过这个概率比较低
				String key = "activityScoreAdd:" + activityId + ":" + player.getId() + ":" + obj.getUuid();
				String history = RedisProxy.getInstance().getRedisSession().getString(key);
				if (!HawkOSOperator.isEmptyString(history)) {
					continue;
				}
				
				Optional<ActivityBase> activityOp = ActivityManager.getInstance().getActivity(activityId);
				// 免费装扮-189
				if (activityId == Activity.ActivityType.EXCHANGE_DECORATE_VALUE) {
					ExchangeDecorateActivity activity = (ExchangeDecorateActivity) activityOp.get();
					Optional<ActivityExchangeDecorateEntity> opEntity = activity.getPlayerDataEntity(player.getId());
					if (opEntity.isPresent()) {
						HawkLog.logPrintln("player activity score add, playerId: {}, activityId: {}, score: {}", player.getId(), activityId, obj.getAddScore());
						RedisProxy.getInstance().getRedisSession().setString(key, "1", GsConst.MONTH_SECONDS);
						ActivityExchangeDecorateEntity entity = opEntity.get();
						activity.addExp(entity, obj.getAddScore());
						activity.syncActivityDataInfo(player.getId());
					}
					continue;
				}
				
				// 红警战令-236 
				if (activityId == Activity.ActivityType.ORDER_TWO_VALUE) {
					OrderTwoActivity activity = (OrderTwoActivity) activityOp.get();
					Optional<OrderTwoEntity> opEntity = activity.getPlayerDataEntity(player.getId());
					if (opEntity.isPresent()) {
						HawkLog.logPrintln("player activity score add, playerId: {}, activityId: {}, score: {}", player.getId(), activityId, obj.getAddScore());
						RedisProxy.getInstance().getRedisSession().setString(key, "1", GsConst.MONTH_SECONDS);
						OrderTwoEntity entity = opEntity.get();
						activity.addExp(entity, obj.getAddScore(), 0, 0);
						activity.syncActivityDataInfo(player.getId());
					}
					continue;
				}
				
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
	}
	
	/**
	 * 检测装备泰能属性重复
	 */
	private void checkArmourStarAttr() {
		List<ArmourEntity> armours = player.getData().getArmourEntityList();
		for (ArmourEntity armour : armours) {
			boolean error = false;
			List<Integer> attrs = new ArrayList<>();
			List<ArmourEffObject> starEffs = armour.getStarEff();
			for (ArmourEffObject starEff : starEffs) {
				if (attrs.contains(starEff.getAttrId())) {
					error = true;
					break;
				}
				attrs.add(starEff.getAttrId());
			}
			if (error) {
				HawkRedisSession redis = RedisProxy.getInstance().getRedisSession();
				String info = GsConfig.getInstance().getServerId() + ":" + player.getId() + ":" + armour.getId();
				redis.lPush("ArmourStarAttrError", 0, info);
			}
		}
	}
}
