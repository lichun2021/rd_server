package com.hawk.game.module;

import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.hawk.activity.event.impl.*;
import com.hawk.game.entity.CustomDataEntity;
import com.hawk.game.protocol.*;
import com.hawk.game.util.GsConst;
import com.hawk.serialize.string.SerializeHelper;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.hawk.annotation.MessageHandler;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.app.HawkApp;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.cryption.HawkMd5;
import org.hawk.log.HawkLog;
import org.hawk.net.http.HawkHttpUrlService;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;
import org.hawk.thread.HawkThreadPool;
import org.hawk.tuple.HawkTuple2;
import org.hawk.tuple.HawkTuple3;
import org.hawk.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.protobuf.format.JsonFormat;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.helper.PlayerAcrossDayLoginMsg;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.config.LuckyBagShareCfg;
import com.hawk.game.data.LuckyBagWxResponse;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.item.AwardItems;
import com.hawk.game.module.lianmengtaiboliya.cfg.TBLYFameHallCfg;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.player.supersoldier.SuperSoldier;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.GuildManager.AuthId;
import com.hawk.game.protocol.Hero.DailyShareType;
import com.hawk.game.protocol.Hero.HPHeroShareReq;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Share.BattleReportShareAwardResp;
import com.hawk.game.protocol.Share.BattleReportShareAwardResp.Builder;
import com.hawk.game.protocol.Share.BattleReportSharedAwardStatus;
import com.hawk.game.protocol.Share.LuckyBagShareResp;
import com.hawk.game.protocol.Share.PlayerDailyShareSaveDataPB;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.chat.ChatParames;
import com.hawk.game.service.chat.ChatService;
import com.hawk.game.service.guildtask.event.GuildShareTaskEvent;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.LogUtil;
import com.hawk.l5.L5Helper;
import com.hawk.l5.L5Task;
import com.hawk.log.Action;
import com.hawk.log.LogConst.SnsType;
import com.hawk.sdk.SDKConst.UserType;
import com.hawk.sdk.config.PlatformConstCfg;

/***
 * 玩家分享模块
 * @author yang.rao
 *
 */

public class PlayerShareModule extends PlayerModule {

	static Logger logger = LoggerFactory.getLogger("Server");
	
	public PlayerShareModule(Player player) {
		super(player);
	}
	
	@Override
	protected boolean onPlayerLogin() {
		nofityPlayerDailyBattleReportSharedAwardStatus();
		return super.onPlayerLogin();
	}
	
	/**
	 * 玩家跨天消息事件
	 * 
	 * @param msg
	 * @return
	 */
	@MessageHandler
	public boolean onPlayerAcreossDayLogin(PlayerAcrossDayLoginMsg msg) {
		nofityPlayerDailyBattleReportSharedAwardStatus();
		return true;
	}
	
	/***
	 * 客户端请求获取福袋分享链接
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.LUCKY_BAG_SHARE_REQ_C_VALUE)
	public void onPlayerReqLuckyBagShareURL(HawkProtocol protocol){
		if (GameUtil.isWin32Platform(player)) {
			addTask(new HawkTask() {
				@Override
				public Object run() {
					win32BlessBagShare();
					return null;
				}
			});
		} else if (UserType.getByChannel(player.getChannel()) == UserType.WX) {
			addTask(new HawkTask() {
				@Override
				public Object run() {
					wxBlessBagShare();
					return null;
				}
			});
		} else if (UserType.getByChannel(player.getChannel()) == UserType.QQ) {
			qqBlessBagShare();
		}
	}
	
	/**
	 * 发放福袋分享奖励邮件
	 */
	private void sendShareAwardMail() {
		long time = RedisProxy.getInstance().getBlessBagShareTime(player.getId());
		if (HawkTime.isSameDay(HawkApp.getInstance().getCurrentTime(), time)) {
			return;
		}
		
		SystemMailService.getInstance().sendMail(MailParames.newBuilder()
				.setPlayerId(player.getId())
				.setMailId(MailId.BLESS_BAG_SHARE_AWARD)
				.setAwardStatus(MailRewardStatus.NOT_GET)
				.setRewards(ConstProperty.getInstance().getActiveShareAwardItems())
				.build());
		LogUtil.logChatInfo(player, "", SnsType.SHARE, "", 0);
		
		RedisProxy.getInstance().updateBlessBagShareTime(player.getId(), HawkApp.getInstance().getCurrentTime());
	}
	
	/**
	 * win32环境福袋分享返回
	 */
	private void win32BlessBagShare(){
		LuckyBagShareResp.Builder resp = LuckyBagShareResp.newBuilder();
		resp.setWxUrl("http://www.baidu.com");
		sendProtocol(HawkProtocol.valueOf(HP.code.LUCKY_BAG_SHARE_RES_S_VALUE, resp));		
		HawkLog.logPrintln("playerSharedModule win32BlessBagShare send protocol :{}" , JsonFormat.printToString(resp.build()));
		sendShareAwardMail();
	}
	
	/***
	 * 微信福袋分享
	 */
	@SuppressWarnings("rawtypes")
	private void wxBlessBagShare(){
		
		Integer[] blessBagL5 = PlatformConstCfg.getInstance().getWxBlessBagL5();
		
		HawkTuple3 info = L5Helper.l5Task(blessBagL5[0], blessBagL5[1], 500, new L5Task() {			
			@SuppressWarnings("unchecked")
			@Override
			public HawkTuple2<Integer, Object> run(String host) {
				try {
					if (!host.endsWith("/")) {
						host = host + "/";
					}
					
					String bagUrl = LuckyBagShareCfg.getInstance().getWxLuckyBagUrl();
					String url = String.format("http://%s%s%s", host, bagUrl, getWXParamWithSign(player.getOpenId()));
					HawkLog.logPrintln("playerShareModule wx request, playerId: {}, url: {}", player.getId(), url);
					
					int timeout = LuckyBagShareCfg.getInstance().getTimeout();
					ContentResponse response = HawkHttpUrlService.getInstance().doGet(url, timeout);
					return new HawkTuple2(Integer.valueOf(0), response);
				} catch (TimeoutException arg3) {
					HawkLog.errPrintln("wx lucky bag request timeout, playerId: {}, l5: {}, host: {}", player.getId(), blessBagL5, host );
				} catch (Exception arg4) {
					HawkException.catchException(arg4, new Object[0]);
				}
				return new HawkTuple2(Integer.valueOf(-1), (Object) null);
			}
		});
		
		try {
			ContentResponse response = (ContentResponse)info.third;
			String msg = new String(response.getContent(), Charset.forName("utf-8"));
			HawkLog.logPrintln("playerShareModule wx jsonparser before response, playerId: {}, msg: {}", player.getId(), msg);
			LuckyBagWxResponse res = JsonUtils.String2Object(msg, LuckyBagWxResponse.class);
			
			if (res.getRet() == 0) {
				String url = res.getData().getUrl();
				HawkLog.logPrintln("playerSharedModule lucky bag response, playerId: {}, ret: {}, msg: {}, url: {}", player.getId(), res.getRet(), res.getMsg(), res.getData().getUrl());
				LuckyBagShareResp.Builder resp = LuckyBagShareResp.newBuilder();
				resp.setWxUrl(url);
				sendProtocol(HawkProtocol.valueOf(HP.code.LUCKY_BAG_SHARE_RES_S_VALUE, resp));
				HawkLog.logPrintln("playerSharedModule wxBlessBagShare, playerId: {}, send protocol: {}", player.getId(), JsonFormat.printToString(resp.build()) );
				sendShareAwardMail();
			} else {
				LuckyBagShareResp.Builder resp = LuckyBagShareResp.newBuilder();
				resp.setErrorCode(res.getRet());
				resp.setErrorMsg(res.getMsg());
				boolean result = sendProtocol(HawkProtocol.valueOf(HP.code.LUCKY_BAG_SHARE_RES_S_VALUE, resp));
				HawkLog.logPrintln("playerSharedModule wxBlessBagShare send error protocol, playerId: {}, result: {}, errorCode: {}, errorMsg: {}", player.getId(), result, resp.getErrorCode(),resp.getErrorMsg());
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/***
	 * 手Q福袋分享
	 * 
	 * @param protocol
	 */
	public void qqBlessBagShare(){
		LuckyBagShareResp.Builder resp = LuckyBagShareResp.newBuilder();
		resp.setQqUrl(LuckyBagShareCfg.getInstance().getQqLuckyBagUrl());
		sendProtocol(HawkProtocol.valueOf(HP.code.LUCKY_BAG_SHARE_RES_S_VALUE, resp));
	}
	
	/**
	 * 添加异步任务
	 * 
	 * @param task
	 */
	public void addTask(HawkTask task) {
		HawkThreadPool taskPool = HawkTaskManager.getInstance().getThreadPool("task");
		if (null != taskPool) {
			task.setTypeName("PlayerShareTask");
			taskPool.addTask(task, 0, false);
		}
	}
	
	/***
	 * 获取完整参数
	 * @param openId
	 * @param playerId
	 * @return
	 */
	private String getWXParamWithSign(String openId){
		LuckyBagShareCfg cfg = LuckyBagShareCfg.getInstance();
		int time = HawkTime.getSeconds();
		StringBuilder builder = new StringBuilder();
		builder.append("?actid=").append(cfg.getActid()).append("&")
		       .append("noticeid=").append(cfg.getWxNoticeid()).append("&")
		       .append("num=").append(cfg.getWxNum()).append("&")
		       .append("openid=").append(openId).append("&")
		       .append("serial=").append(player.getId() + "-" + HawkTime.getYearDay()).append("&")
		       .append("stamp=").append(time).append("&")
			   .append("sign=").append(HawkMd5.makeMD5(getWXParam(openId)));
		return builder.toString();
	}

	/***
	 * 获取md5加密之前的参数列表
	 * @param openId
	 * @return
	 */
	private String getWXParam(String openId){
		LuckyBagShareCfg cfg = LuckyBagShareCfg.getInstance();
		int time = HawkTime.getSeconds();
		StringBuilder builder = new StringBuilder();
		builder.append("actid=").append(cfg.getActid()).append("&")
		       .append("noticeid=").append(cfg.getWxNoticeid()).append("&")
		       .append("num=").append(cfg.getWxNum()).append("&")
		       .append("openid=").append(openId).append("&")
		       .append("serial=").append(player.getId() + "-" + HawkTime.getYearDay()).append("&")
		       .append("stamp=").append(time).append("&")
			   .append("key=").append(cfg.getWxKey());
		return builder.toString();
	}
	
	/**
	 * qq平台福袋分享get请求
	 * qq平台比较特殊，需要设置http包头
	 * @param url
	 * @param timeout
	 * @param ip: 玩家真实ip地址
	 * @return
	 * @throws Exception
	 */
	public ContentResponse doGet(String url, int timeout, String... ip) throws Exception {
		HttpClient httpClient = HawkHttpUrlService.getInstance().getHttpClient();
		if(ip == null){
			return null;
		}
		if (httpClient != null && httpClient.isRunning()) {
			if (timeout > 0) {
				httpClient.setConnectTimeout(timeout);
			}
			try {
				// 开启请求
				Request request = httpClient.newRequest(url);
				request.header("Connection", "close");
				if(ip.length == 1){
					request.header("X-Forwarded-For", ip[0]);
				}else{
					StringBuilder builder = new StringBuilder();
					for(int i = 0 ; i < ip.length ; i ++){
						builder.append(ip[i]);
						if(i != ip.length-1){
							builder.append(",");
						}
					}
					request.header("X-Forwarded-For", builder.toString());
				}
				if (timeout > 0) {
					request.timeout(timeout, TimeUnit.MILLISECONDS); 
				}
				ContentResponse response = request.send();
				return response;
			} catch (Exception e) {
				HawkLog.errPrintln("PlayerShareModule get exception, url: {}", url);
				HawkException.catchException(e);
				throw e;
			}			
		}
		return null;
	}
	
	/**
	 * 扭蛋（英雄招募）分享 此协议改造为通用分享接口
	 */
	@ProtocolHandler(code = HP.code.HERO_SHARE_C_VALUE)
	private void onHeroShare(HawkProtocol protocol) {
		HPHeroShareReq request = protocol.parseProtocol(HPHeroShareReq.getDefaultInstance());
		DailyShareType type = request.getType();
		if (type == null) {
			return;
		}	
		LogUtil.playerShare(player,type.getNumber());
		// 联盟任务-联盟成员分享到微信/QQ
		if (player.hasGuild() && request.hasShareTo() && request.getShareTo() == 1) {
			LocalRedis.getInstance().addGuildShareMember(player.getGuildId(), player.getId());
			GuildService.getInstance().postGuildTaskMsg(new GuildShareTaskEvent(player.getGuildId()));
		}
		ActivityManager.getInstance().postEvent(new ShareEvent(player.getId(), type));
		
		switch (type) {
		case SHARE_HERO: {
			final int heroId = request.getHeroId();
			PlayerHero hero = player.getHeroByCfgId(heroId).orElse(null);
			if (hero == null) {
				return;
			}
			if (request.getShareTo() == 1) {
				hero.incShare();
				player.responseSuccess(protocol.getType());
				ActivityManager.getInstance().postEvent(new HeroShareEvent(player.getId(), heroId));
				ActivityManager.getInstance().postEvent(new DailyMissionShareEvent(player.getId(), type));
				return;
			}
			//TODO jason fix 联盟聊天英雄分享权限检测，先屏蔽
//			if(GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.GUILD_HERO_PERMISSION)){
//				sendError(protocol.getType(), Status.Error.GUILD_LOW_AUTHORITY);
//				return;
//			}else {
				ChatParames parames = ChatParames.newBuilder().setChatType(Const.ChatType.CHAT_ALLIANCE)
						.setKey(Const.NoticeCfgId.GetNewHero).setPlayer(player).addParms(heroId).build();
				ChatService.getInstance().addWorldBroadcastMsg(parames);
				player.responseSuccess(protocol.getType());
//			}
			break;
		}
		case SHARE_ACHIEVE:
			if (request.getShareTo() == 1) {
				ActivityManager.getInstance().postEvent(new DailyMissionShareEvent(player.getId(), type));
			}
			break;
		case SHARE_BATTLE_REPORT:
			if (request.getShareTo() == 1) { 
				ActivityManager.getInstance().postEvent(new DailyMissionShareEvent(player.getId(), type));
				//分享战报 每日战报分享领取奖励状态变更
				refreshDailyBattleReportShareAwardStatus();
			}
			break;
		case SHARE_SUPERSOLDIER: {
			final int ssId = request.getSuperSoldierId();
			SuperSoldier soldier = player.getSuperSoldierByCfgId(ssId).orElse(null);
			if (soldier == null) {
				return;
			}
			if (request.getShareTo() == 1) {
				soldier.incShare();
				player.responseSuccess(protocol.getType());
				ActivityManager.getInstance().postEvent(new DailyMissionShareEvent(player.getId(), type));
				return;
			}

			//TODO jason  fix 战报分享需要检测权限
			if(type ==DailyShareType.SHARE_BATTLE_REPORT ){
				if(!GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.GUILD_REPORT_PERMISSION)){
					sendError(protocol.getType(), Status.Error.GUILD_LOW_AUTHORITY);
					return;
				}
				ChatParames parames = ChatParames.newBuilder()
						.setChatType(Const.ChatType.CHAT_ALLIANCE)
						.setKey(Const.NoticeCfgId.GetNewSuperSoldier)
						.setPlayer(player)
						.addParms(ssId)
						.build();
				ChatService.getInstance().addWorldBroadcastMsg(parames);
				player.responseSuccess(protocol.getType());
			}else {
				ChatParames parames = ChatParames.newBuilder()
						.setChatType(Const.ChatType.CHAT_ALLIANCE)
						.setKey(Const.NoticeCfgId.GetNewSuperSoldier)
						.setPlayer(player)
						.addParms(ssId)
						.build();
				ChatService.getInstance().addWorldBroadcastMsg(parames);
				player.responseSuccess(protocol.getType());
			}
			break;
		}
		//记录打点日志
		case DAILY_MISSION_BAG_SHARE:
			LogUtil.logDailyMission(player);
			break;
		// 在线答题 经典红警情报分享
		case SHARE_QUESTION:
			ActivityManager.getInstance().postEvent(QuestionShareEvent.valueOf(player.getId()));
			break;
		case SHARE_TX_URL_EIGHT:
			ActivityManager.getInstance().postEvent(TxUrlEightShareEvent.valueOf(player.getId()));
			break;
		case SHARE_TX_URL_NINE:
			ActivityManager.getInstance().postEvent(TxUrlNineShareEvent.valueOf(player.getId()));
			break;
		case SHARE_TX_URL_TEN:
			ActivityManager.getInstance().postEvent(TxUrlTenShareEvent.valueOf(player.getId()));
			break;
		case SHARE_TANK_FACTORY:
		case SHARE_CHARIOT_FACTORY:
		case SHARE_PLANE_FACTORY:
		case SHARE_SOLDIER_FACTORY:
		case SHARE_PHYSICAL_POWER:
		case SHARE_DIVIDE_GOLD_ACT:
		case SHARE_HATE_RANK:
		case SHARE_ARMIES_MASS:
			break;
		case SHARE_EVOLUTION_HERO:
			ActivityManager.getInstance().postEvent(EvolutionShareEvent.valueOf(player.getId()));
			break;
		case SHARE_J20_CELEBRATE:
			ActivityManager.getInstance().postEvent(WLQDShareEvent.valueOf(player.getId()));
			break;
		case SHARE_RETURN_PUZZLE:
			ActivityManager.getInstance().postEvent(ReturnPuzzleCheckShareEvent.valueOf(player.getId(),request.getReturnOrderId()));
			break;
		case SHARE_GRATEFUL_BENEFITS:
			//感恩福利打点
			ActivityManager.getInstance().postEvent(new GratefulBenefitsShareEvent(player.getId()));
			break;
		case EXCLUSIVE_MEMORY:
			ActivityManager.getInstance().postEvent(ExclusiveMemoryShareEvent.valueOf(player.getId()));
			break;
		case SHARE_TBLY_FAME_HALL:
			//泰伯利亚名人堂分享
			shareTblyFameHall(request.getTblyFameHallId());
			tblyShareIndoReq(null);
			break;
		default:
			break;
		}
		
	}
	
	/**
	 * 玩家分享战报之后奖励状态变更
	 */
	void refreshDailyBattleReportShareAwardStatus(){
		 PlayerDailyShareSaveDataPB.Builder builder = LocalRedis.getInstance().loadPlayerDailyShareSaveData(player.getId());
		 if( BattleReportSharedAwardStatus.SHARE_ENABLE == builder.getBrStatus()){
			builder.setBrStatus(BattleReportSharedAwardStatus.AWARD_ENABLE);
			LocalRedis.getInstance().savePlayerDailyShareSaveData(player.getId(), builder.build());
			nofityPlayerDailyBattleReportSharedAwardStatus();
			
			logger.info("player {} battle_report_share award_enable",player.getId());
		 }
	}
	
	/**
	 * 发送给玩家每日战报领奖状态
	 */
	public void nofityPlayerDailyBattleReportSharedAwardStatus(){
		PlayerDailyShareSaveDataPB savedPb = LocalRedis.getInstance().loadPlayerDailyShareSaveData(player.getId()).build();
		Builder builder = BattleReportShareAwardResp.newBuilder();
		builder.setStatus(savedPb.getBrStatus());
		player.sendProtocol(HawkProtocol.valueOf(HP.code.BATTLE_REPORT_SHARE_AWARD_RES_S, builder));
		logger.debug("send player {} battle_report_share: {}",player.getId(),JsonFormat.printToString(builder.build()));
	}
	
	/**
	 * 扭蛋（英雄招募）分享 此协议改造为通用分享接口
	 */
	@ProtocolHandler(code = HP.code.BATTLE_REPORT_SHARE_AWARD_REQ_C_VALUE)
	private void onBattleShareAewared(HawkProtocol protocol){
		PlayerDailyShareSaveDataPB.Builder pbBuilder = LocalRedis.getInstance().loadPlayerDailyShareSaveData(player.getId());
		if( BattleReportSharedAwardStatus.AWARD_ENABLE == pbBuilder.getBrStatus()){
			// 改状态
			pbBuilder.setBrStatus(BattleReportSharedAwardStatus.AWARDED);
			LocalRedis.getInstance().savePlayerDailyShareSaveData(player.getId(), pbBuilder.build());		
			
			// 添加物品
			try{
				AwardItems awardItem = AwardItems.valueOf(ConstProperty.getInstance().getShareReward());
				awardItem.rewardTakeAffectAndPush(player, Action.SHARE_BTREPORT, true);
				nofityPlayerDailyBattleReportSharedAwardStatus();
				logger.info("player {} battle_report_share award",player.getId());			
			}catch(Exception e){
				HawkException.catchException(e);
			}
		}
	}

	/**
	 * 请求分享状态
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.TBLY_FAME_HALL_REQ_VALUE)
	private void tblyShareIndoReq(HawkProtocol protocol){
		//最高赛季
		int maxSeason = 0;
		//有奖励的赛季
		Set<Integer> rewardSet = new HashSet<>();
		ConfigIterator<TBLYFameHallCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(TBLYFameHallCfg.class);
		while (iterator.hasNext()) {
			TBLYFameHallCfg cfg = iterator.next();
			if(cfg.getRewardList() != null && cfg.getRewardList().size() > 0){
				rewardSet.add(cfg.getSeason());
			}
			if(cfg.getSeason() > maxSeason){
				maxSeason = cfg.getSeason();
			}
		}
		CustomDataEntity customData = player.getData().getCustomDataEntity(GsConst.SHARE_TBLY_FAME_HALL_KEY);
		//如果为空就创建数据库事件
		if (customData == null) {
			customData = player.getData().createCustomDataEntity(GsConst.SHARE_TBLY_FAME_HALL_KEY, 0, "");
		}
		//已领取集合
		Set<Integer> seasonGet = SerializeHelper.stringToSet(Integer.class, customData.getArg(), SerializeHelper.BETWEEN_ITEMS,null);
		Share.TBLYFameHallInfo.Builder builder = Share.TBLYFameHallInfo.newBuilder();
		for(int i = 1; i <= maxSeason; i++){
			Share.TBLYFameHallShareInfo.Builder shareBuilder = Share.TBLYFameHallShareInfo.newBuilder();
			shareBuilder.setSeason(i);
			if(rewardSet.contains(i) && !seasonGet.contains(i)){
				shareBuilder.setIsShared(false);
			}else {
				shareBuilder.setIsShared(true);
			}
			builder.addShareInfos(shareBuilder);
		}

		player.sendProtocol(HawkProtocol.valueOf(HP.code.TBLY_FAME_HALL_RESP, builder));
	}

	/**
	 * 泰伯利亚分享奖励
	 * @param season 分享赛季
	 */
	private void shareTblyFameHall(int season){
		//分享打点
		LogUtil.playerShareFameHall(player, 0, season);
		//策划要求根据冠军配置id发奖，冠军id同赛季最小
		int minId = Integer.MAX_VALUE;
		TBLYFameHallCfg rewardCfg = null;
		ConfigIterator<TBLYFameHallCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(TBLYFameHallCfg.class);
		while (iterator.hasNext()){
			TBLYFameHallCfg cfg = iterator.next();
			if(cfg.getSeason() == season){
				if(cfg.getId() < minId){
					minId = cfg.getId();
					rewardCfg = cfg;
				}
			}
		}
		//如果没有配置直接返回
		if(rewardCfg == null){
			return;
		}
		//每个赛季分享只能领取一次
		//获得数据库领取数据
		CustomDataEntity customData = player.getData().getCustomDataEntity(GsConst.SHARE_TBLY_FAME_HALL_KEY);
		//如果为空就创建数据库事件
		if (customData == null) {
			customData = player.getData().createCustomDataEntity(GsConst.SHARE_TBLY_FAME_HALL_KEY, 0, "");
		}
		//已领取集合
		Set<Integer> seasonGet = SerializeHelper.stringToSet(Integer.class, customData.getArg(), SerializeHelper.BETWEEN_ITEMS,null);
		//当前分享赛季已经领取过直接返回
		if(seasonGet.contains(season)){
			return;
		}
		//设置已领取状态
		seasonGet.add(season);
		//落库
		customData.setArg(SerializeHelper.collectionToString(seasonGet,SerializeHelper.BETWEEN_ITEMS));
		//发奖
		AwardItems awardItem = AwardItems.valueOf();
		for (Reward.RewardItem.Builder itemInfo : rewardCfg.getRewardList()) {
			awardItem.addItem(itemInfo.getItemType(), itemInfo.getItemId(), itemInfo.getItemCount());
		}
		awardItem.rewardTakeAffectAndPush(player, Action.SHARE_TBLY_FAME_HALL_AWARD, true);
		//领奖打点
		LogUtil.playerShareFameHall(player, 1, season);
	}
}
