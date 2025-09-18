package com.hawk.game.module;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hawk.annotation.MessageHandler;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.config.HawkConfigManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.redis.HawkRedisSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.googlecode.protobuf.format.JsonFormat;
import com.googlecode.protobuf.format.JsonFormat.ParseException;
import com.hawk.common.IDIPBanInfo;
import com.hawk.game.GsApp;
import com.hawk.game.GsConfig;
import com.hawk.game.config.OfficerCfg;
import com.hawk.game.config.PresidentConstCfg;
import com.hawk.game.config.PresidentGiftCfg;
import com.hawk.game.config.WorldMapConstProperty;
import com.hawk.game.crossproxy.CrossSkillService;
import com.hawk.game.entity.OfficerEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.msg.TaxPlayerUpdateMsg;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.president.PresidentBuff;
import com.hawk.game.president.PresidentCity;
import com.hawk.game.president.PresidentFightService;
import com.hawk.game.president.PresidentGift;
import com.hawk.game.president.PresidentOfficier;
import com.hawk.game.president.PresidentRecord;
import com.hawk.game.president.PresidentResource;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.protocol.Const.NoticeCfgId;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.protocol.Cross.PBCrossSkillCastReq;
import com.hawk.game.protocol.CrossActivity.CorssRecordType;
import com.hawk.game.protocol.CrossActivity.CrossSkillSendInfo;
import com.hawk.game.protocol.CrossActivity.CrossTaxSendRecord;
import com.hawk.game.protocol.IDIP.NoticeMode;
import com.hawk.game.protocol.IDIP.NoticeType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Player.CrossPlayerStruct;
import com.hawk.game.protocol.Player.MsgCategory;
import com.hawk.game.protocol.President.CountryInfoSettingReq;
import com.hawk.game.protocol.President.FetchPresidentInfoReq;
import com.hawk.game.protocol.President.OfficerInfo;
import com.hawk.game.protocol.President.OfficerInfoSync;
import com.hawk.game.protocol.President.OfficerRecord;
import com.hawk.game.protocol.President.OfficerRecordSync;
import com.hawk.game.protocol.President.OfficerSetReq;
import com.hawk.game.protocol.President.OfficerType;
import com.hawk.game.protocol.President.PresidentBuffOpenReq;
import com.hawk.game.protocol.President.PresidentEvent;
import com.hawk.game.protocol.President.PresidentEventSync;
import com.hawk.game.protocol.President.PresidentHistory;
import com.hawk.game.protocol.President.PresidentHistorySync;
import com.hawk.game.protocol.President.PresidentInfo;
import com.hawk.game.protocol.President.PresidentInfoSync;
import com.hawk.game.protocol.President.PresidentManifestoUpdate;
import com.hawk.game.protocol.President.PresidentSearchReq;
import com.hawk.game.protocol.President.PresidentSearchRes;
import com.hawk.game.protocol.President.PresidentSendGiftReq;
import com.hawk.game.protocol.President.PresidentSilentPlayerReq;
import com.hawk.game.protocol.President.PresidentTowerEventReq;
import com.hawk.game.protocol.President.PresidentTowerInfoReq;
import com.hawk.game.protocol.President.TaxGuildInfoSync;
import com.hawk.game.protocol.President.TaxGuildRecord;
import com.hawk.game.protocol.President.TaxGuildReq;
import com.hawk.game.protocol.Status;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.chat.ChatParames;
import com.hawk.game.service.chat.ChatService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.tsssdk.GameTssService;
import com.hawk.game.tsssdk.GameMsgCategory;
import com.hawk.game.util.BuilderUtil;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.LogUtil;
import com.hawk.game.util.GsConst.IDIPBanType;
import com.hawk.log.Action;

/**
 * 国王战模块
 *
 * @author hawk
 */
public class PlayerPresidentModule extends PlayerModule {

	/**
	 * 协议日志记录器
	 */
	static final Logger logger = LoggerFactory.getLogger("Server");

	/**
	 * 构造
	 *
	 * @param player
	 */
	public PlayerPresidentModule(Player player) {
		super(player);
	}

	/**
	 * 组装完成的同步
	 */
	@Override
	protected boolean onPlayerAssemble() {
		return true;
	}

	/**
	 * 玩家上线处理
	 *
	 * @return
	 */
	@Override
	protected boolean onPlayerLogin() {
		// 同步国王战信息
		PresidentFightService.getInstance().getPresidentCity().broadcastPresidentInfo(player);
		PresidentFightService.getInstance().getPresidentCity().broadcastAllPresidentTowerInfo(player);
		CrossSkillService.getInstance().syncCrossSkillInfo(player);
		presidentLogin();
		return true;
	}
	
	private void presidentLogin() {
		if (player.isCsPlayer() || !PresidentFightService.getInstance().isPresidentPlayer(player.getId())) {
			return;
		}
		
		PresidentConstCfg constCfg = PresidentConstCfg.getInstance();
		int now = HawkTime.getSeconds();
		PresidentCity presidentCity = PresidentFightService.getInstance().getPresidentCity();
		if (presidentCity.getLastNoticeTime() + constCfg.getNoticeCdTime() > now) {
			return;
		}		
		presidentCity.setLastNoticeTime(now);
		
		ChatParames.Builder chatParames = ChatParames.newBuilder();
		chatParames.setKey(NoticeCfgId.PRESIDENT_LOGIN);
		chatParames.setChatType(ChatType.SPECIAL_BROADCAST);
		chatParames.addParms(player.getName());
		ChatService.getInstance().addWorldBroadcastMsg(chatParames.build());
	}

	/**
	 * 玩家下线处理
	 *
	 * @return
	 */
	@Override
	protected boolean onPlayerLogout() {
		return true;
	}

	/**
	 * 国王修改王国信息
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.COUNTRY_INFO_SETTING_C_VALUE)
	private boolean onCountryInfoSetting(HawkProtocol protocol) {
		// 禁止输入文本
		if (!GameUtil.checkBanMsg(player)) {
			return false;
		}
				
		CountryInfoSettingReq req = protocol.parseProtocol(CountryInfoSettingReq.getDefaultInstance());

		// 非国王
		if (!player.getId().equals(PresidentFightService.getInstance().getPresidentPlayerId())) {
			player.sendError(protocol.getType(), Status.Error.PRESIDENT_NOT_KING, 0);
			return false;
		}

		// 只能修改一次
		if (PresidentFightService.getInstance().getPresidentCity().getCountryModifyTimes() >= PresidentConstCfg.getInstance().getCountryModifyTimes()) {
			player.sendError(protocol.getType(), Status.Error.COUNTRY_CANNOT_CHANGE_AGAIN, 0);
			return false;
		}

		// 国家名字校验
		String countryName = PresidentFightService.getInstance().getPresidentCity().getCountryName();
		if (HawkOSOperator.isEmptyString(req.getCountryName())) {
			// 通知王国信息修改
			PresidentFightService.getInstance().notifyCountryInfoChanged(countryName);
			return true;
		}
		
		GameTssService.getInstance().wordUicChatFilter(player, req.getCountryName(), 
				GameMsgCategory.SET_COUNTRY_INFO, GameMsgCategory.SET_COUNTRY_INFO, 
				"", null, protocol.getType());
		return true;
	}

	/**
	 * 国王礼包数据初始化请求
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.PRESIDENT_GIFT_INFO_C_VALUE)
	private boolean onGiftInfo(HawkProtocol protocol) {
		PresidentGift.getInstance().syncGiftInfo(player);
		return true;
	}

	/**
	 * 国王颁发礼包
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.PRESIDENT_SEND_GIFT_C_VALUE)
	private boolean onSendGift(HawkProtocol protocol) {
		// 数据校验
		PresidentSendGiftReq request = protocol.parseProtocol(PresidentSendGiftReq.getDefaultInstance());
		if (!request.hasGiftId()) {
			player.sendError(protocol.getType(), Status.SysError.PARAMS_INVALID, 0);
			return false;
		}
		// 查询配置数据
		PresidentGiftCfg giftCfg = HawkConfigManager.getInstance().getConfigByKey(PresidentGiftCfg.class, request.getGiftId());
		if (null == giftCfg) {
			player.sendError(protocol.getType(), Status.Error.PRESIDENT_GIFT_CHECK, 0);
			return false;
		}
		List<String> playerIds = request.getPlayerIdsList();
		if (null == playerIds || playerIds.size() <= 0) {
			player.sendError(protocol.getType(), Status.SysError.PARAMS_INVALID, 0);
			return false;
		}
		// 职位判断
		String presidentId = PresidentFightService.getInstance().getPresidentPlayerId();
		if (!player.getId().equals(presidentId)) {
			player.sendError(protocol.getType(), Status.Error.PRESIDENT_NOT_KING, 0);
			return false;
		}
		PresidentGift.getInstance().sendGiftLogic(player, giftCfg, playerIds);
		return true;
	}

	/**
	 * 国王搜索玩家信息
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.PRESIDENT_SEARCH_C_VALUE)
	private boolean onSearchMember(HawkProtocol protocol) {
//		// 禁止输入文本，不能直接调这个接口，否则前端有些场景会出现弹框提示后转菊花的情况
//		if (!GameUtil.checkBanMsg(player)) {
//			return false;
//		}
		
		// 禁言玩家推送禁言提示
		if (player.getEntity().getSilentTime() > HawkTime.getMillisecond()) {
			IDIPBanInfo banInfo = RedisProxy.getInstance().getIDIPBanInfo(player.getId(), IDIPBanType.BAN_SEND_MSG);
			if (banInfo != null) {
				PresidentSearchRes.Builder response = PresidentSearchRes.newBuilder();
				response.setMsg(banInfo.getBanMsg());
				player.sendProtocol(HawkProtocol.valueOf(HP.code.PRESIDENT_SEARCH_S, response));
				return false;
			}
		}
				
		PresidentSearchReq request = protocol.parseProtocol(PresidentSearchReq.getDefaultInstance());
		if (!request.hasName()) {
			player.sendError(protocol.getType(), Status.SysError.PARAMS_INVALID, 0);
			return false;
		}

		if (!player.hasGuild()) {
			player.sendError(protocol.getType(), Status.SysError.PARAMS_INVALID_VALUE, 0);

			return false;
		}

		GameTssService.getInstance().wordUicChatFilter(player, request.getName(), 
				MsgCategory.PRESIDENT_SEARCH_MEMBER.getNumber(), GameMsgCategory.PRESIDENT_SEARCH_MEMBER, 
				String.valueOf(request.getType()), null, protocol.getType());
		return true;
	}

	/**
	 * 国王颁发礼包记录查看
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.PRESIDENT_GIFT_RECORD_C_VALUE)
	private boolean onGiftRecord(HawkProtocol protocol) {
		PresidentGift.getInstance().giftSendRecord(player);
		return true;
	}

	/**
	 * 获取历届国王
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.FETCH_PRESIDENT_HISTORY_C_VALUE)
	private boolean onFetchPresidentHistory(HawkProtocol protocol) {
		PresidentHistorySync.Builder builder = PresidentHistorySync.newBuilder();

		PresidentConstCfg constCfg = PresidentConstCfg.getInstance();
		List<PresidentHistory.Builder> historyList = LocalRedis.getInstance().getElectedPresident(constCfg.getMaxHistoryCount());
		Set<Integer> recordTurn = new HashSet<>();
		for (PresidentHistory.Builder historyBuilder : historyList) {
			if (!recordTurn.contains(historyBuilder.getTurnCount())) {
				builder.addHistory(historyBuilder);
				recordTurn.add(historyBuilder.getTurnCount());
			}
		}

		protocol.response(HawkProtocol.valueOf(HP.code.PRESIDENT_HISTORY_SYNC, builder));
		return true;
	}

	/**
	 * 获取国王战战争记录
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.FETCH_PRESIDENT_EVENT_C_VALUE)
	private boolean onFetchPresidentEvent(HawkProtocol protocol) {
		PresidentEventSync.Builder builder = PresidentEventSync.newBuilder();

		PresidentConstCfg constCfg = PresidentConstCfg.getInstance();
		List<PresidentEvent.Builder> eventList = LocalRedis.getInstance().getPresidentEvent(constCfg.getMaxEventCount());
		for (PresidentEvent.Builder eventBuilder : eventList) {
			builder.addEvent(eventBuilder);
		}

		protocol.response(HawkProtocol.valueOf(HP.code.PRESIDENT_EVENT_SYNC, builder));
		return true;
	}

	/**
	 * 获取国王战箭塔战争记录
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.FETCH_PRESIDENT_TOWER_EVENT_C_VALUE)
	private boolean onFetchPresidentTowerEvent(HawkProtocol protocol) {
		PresidentTowerEventReq req = protocol.parseProtocol(PresidentTowerEventReq.getDefaultInstance());
		int towerPointId = GameUtil.combineXAndY(req.getX(), req.getY());
		if (GsConst.PresidentTowerPointId.valueOf(towerPointId) == null) {
			return false;
		}

		PresidentEventSync.Builder builder = PresidentEventSync.newBuilder();
		PresidentConstCfg constCfg = PresidentConstCfg.getInstance();
		List<PresidentEvent.Builder> eventList = LocalRedis.getInstance().getPresidentTowerEvent(constCfg.getMaxEventCount(), towerPointId);
		for (PresidentEvent.Builder eventBuilder : eventList) {
			builder.addEvent(eventBuilder);
		}
		protocol.response(HawkProtocol.valueOf(HP.code.FETCH_PRESIDENT_TOWER_EVENT_S, builder));
		return true;
	}

	/**
	 * 获取制定服务器的国王信息
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.FETCH_PRESIDENT_INFO_VALUE)
	private boolean onFetchPresidentInfo(HawkProtocol protocol) {
		FetchPresidentInfoReq req = protocol.parseProtocol(FetchPresidentInfoReq.getDefaultInstance());

		// 本服同步
		if (!req.hasServerName() || HawkOSOperator.isEmptyString(req.getServerName())
				|| req.getServerName().equals(GsConfig.getInstance().getServerId())) {
			PresidentFightService.getInstance().getPresidentCity().broadcastPresidentInfo(player);
			return true;
		}

		// 直接从redis中拉取制定国家的国王信息
		PresidentInfoSync.Builder builder = PresidentInfoSync.newBuilder();
		builder.setServerName(req.getServerName());
		PresidentInfo.Builder infoBuilder = RedisProxy.getInstance().getPresidentInfo(req.getServerName());
		if (infoBuilder != null) {
			builder.setInfo(infoBuilder);
		}

		// 同步给玩家
		player.sendProtocol(HawkProtocol.valueOf(HP.code.PRESIDENT_INFO_SYNC, builder));

		return true;
	}

	/**
	 * 官职历史信息同步（打开官职历史面板）
	 */
	@ProtocolHandler(code = HP.code.OFFICER_RECORD_SYNC_C_VALUE)
	private boolean onSyncOfficerRecord(HawkProtocol protocol) {
		OfficerRecordSync.Builder builder = OfficerRecordSync.newBuilder();

		int period = PresidentFightService.getInstance().getPresidentCity().getTurnCount();
		List<String> recordList = LocalRedis.getInstance().getOfficerRecord(period);

		if (recordList != null && recordList.size() > 0) {
			for (String string : recordList) {
				OfficerRecord.Builder info = OfficerRecord.newBuilder();
				try {
					JsonFormat.merge(string, info);
					builder.addRecords(info);
				} catch (ParseException e) {
					HawkException.catchException(e);
				}
			}
		}

		sendProtocol(HawkProtocol.valueOf(HP.code.OFFICER_RECORD_SYNC_S_VALUE, builder));
		return true;
	}

	/**
	 * 官职信息同步（打开官职面板）
	 */
	@ProtocolHandler(code = HP.code.OFFICER_INFO_SYNC_C_VALUE)
	private boolean onSyncOfficerInfo(HawkProtocol protocol) {
		OfficerInfoSync.Builder builder = OfficerInfoSync.newBuilder();

		List<OfficerEntity> list = PresidentOfficier.getInstance().getOfficerEntityList();
		long endTime = PresidentFightService.getInstance().getPresidentCity().getEndTime();
		builder.setAppointEndTime(endTime + PresidentConstCfg.getInstance().getAppointTime());

		GlobalData globalData = GlobalData.getInstance();
		for (OfficerEntity entity : list) {
			OfficerInfo.Builder info = OfficerInfo.newBuilder();
			info.setOfficerId(entity.getOfficerId());
			info.setEndTime(entity.getEndTime());

			// 说明没有任职.
			if (!HawkOSOperator.isEmptyString(entity.getPlayerId())) {
				if (globalData.isLocalPlayer(entity.getPlayerId())) {
					Player playerBuilder = globalData.makesurePlayer(entity.getPlayerId());
					if (playerBuilder != null) {
						info.setPlayerMsg(BuilderUtil.genMiniPlayer(playerBuilder));
						info.setGuildTag(playerBuilder.getGuildTag());
					}
				} else if (entity.getOfficerId() == OfficerType.OFFICER_01_VALUE) {
					// 而且必须要是国王才可以.
					CrossPlayerStruct crossKingInfo = PresidentFightService.getInstance().getPresidentCity().getCrossKingInfo();
					if (crossKingInfo != null) {
						info.setPlayerMsg(BuilderUtil.genMiniPlayer(crossKingInfo));
						info.setGuildTag(crossKingInfo.getGuildTag());
					}
				}
			}

			builder.addOfficers(info);
		}

		sendProtocol(HawkProtocol.valueOf(HP.code.OFFICER_INFO_SYNC_S_VALUE, builder));

		return true;
	}

	/**
	 * 临时总统可以设置总统
	 */
	private void setPresident(Player targetPlayer) {
		// 超过任命时间 外面调用的地方已经判断你是不是总统了
		PresidentConstCfg presidentConstCfg = PresidentConstCfg.getInstance();
		// 被转让对象一定要有工会
		if (HawkOSOperator.isEmptyString(targetPlayer.getGuildId())) {
			this.sendError(HP.code.OFFICER_SET_C_VALUE, Status.Error.PRESIDENT_TARGET_HAS_NO_GUILD_VALUE);
			return;
		}
		if (HawkTime.getMillisecond() - PresidentFightService.getInstance().getPresidentCity().getEndTime() > presidentConstCfg.getAppointTime()) {
			this.sendError(HP.code.OFFICER_SET_C_VALUE, Status.Error.PRESIDENT_SET_OUT_TIME_VALUE);
			return;
		}
		// 已经设置了
		OfficerEntity officerEntity = PresidentOfficier.getInstance().getEntityById(OfficerType.OFFICER_01_VALUE);
		if (officerEntity.getEndTime() != 0l) {
			this.sendError(HP.code.OFFICER_SET_C_VALUE, Status.Error.PRESIDENT_HAVE_SET_VALUE);
			return;
		}
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
		sendProtocol(HawkProtocol.valueOf(HP.code.OFFICER_INFO_SYNC_S_VALUE, builder));
		PresidentFightService.getInstance().getPresidentCity().chanagePresident(targetPlayer);
		PresidentFightService.getInstance().getPresidentCity().broadcastPresidentInfo(null);
		logger.info("SetPresident operatorId:{},operatedId:{}", player.getId(), targetPlayer.getId());
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
	}

	/**
	 * 任命官职
	 */
	@ProtocolHandler(code = HP.code.OFFICER_SET_C_VALUE)
	private boolean onSetOfficer(HawkProtocol protocol) {
		OfficerSetReq req = protocol.parseProtocol(OfficerSetReq.getDefaultInstance());
		OfficerInfoSync.Builder builder = OfficerInfoSync.newBuilder();

		int officerId = req.getOfficerId();
		String playerId = req.getPlayerId();

		if (!GlobalData.getInstance().isExistPlayerId(playerId)) {
			sendError(protocol.getType(), Status.SysError.ACCOUNT_NOT_EXIST);
			return false;
		}

		if (!GlobalData.getInstance().isLocalPlayer(playerId)) {
			this.sendError(protocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);

			return false;
		}

		Player snapshot = GlobalData.getInstance().makesurePlayer(playerId);
		if (snapshot == null) {
			sendError(protocol.getType(), Status.SysError.ACCOUNT_NOT_EXIST);
			return false;
		}

		// 官职不存在
		OfficerType officerType = OfficerType.valueOf(officerId);
		if (officerType == null) {
			sendError(protocol.getType(), Status.Error.OFFICER_TYPE_NOT_EXIST);
			return false;
		}

		// 不是元首不可任命官职
		if (!player.getId().equals(PresidentFightService.getInstance().getPresidentPlayerId())) {
			sendError(protocol.getType(), Status.Error.PRESIDENT_NOT_KING);
			return false;
		}

		if (player.getId().equals(playerId)) {
			sendError(protocol.getType(), Status.Error.OFFICER_SET_SELF_ERROR);
			return false;
		}

		// 如果是设置总统走设置总统流程.
		if (officerType == OfficerType.OFFICER_01) {
			setPresident(snapshot);
			return true;
		}

		// 玩家当前官职
		OfficerEntity curEntity = PresidentOfficier.getInstance().getEntityByPlayerId(playerId);
		// 玩家目标官职
		OfficerEntity tarEntity = PresidentOfficier.getInstance().getEntityById(officerId);
		if (tarEntity == null) {
			sendError(protocol.getType(), Status.Error.OFFICER_TYPE_NOT_EXIST);
			return false;
		}

		// 多次给同一人设置同一官职
		if (tarEntity == curEntity) {
			sendError(protocol.getType(), Status.Error.OFFICER_SET_SAME_ERROR);
			return false;
		}

		// 同一官职距上次任命时间小于CD
		long now = HawkTime.getMillisecond();
		if (tarEntity.getEndTime() > now) {
			sendError(protocol.getType(), Status.Error.OFFICER_SET_CDTIME_ERROR);
			return false;
		}

		logger.info(" set officer oldOfficerId:{}, newOfficerId:{}, playerId:{}", curEntity == null ? 0 : curEntity.getOfficerId(), officerId, playerId);

		String name = GameUtil.getPlayerNameWithGuildTag(snapshot.getGuildId(), snapshot.getName());
		// 如果玩家当前有职有身，先卸职
		int officerIdOri = OfficerType.OFFICER_00_VALUE;
		if (curEntity != null) {
			officerIdOri = curEntity.getOfficerId();
			PresidentOfficier.getInstance().unsetOfficer(playerId, officerIdOri);
			// 发送邮件---更改任命邮件
			SystemMailService.getInstance().sendMail(MailParames.newBuilder()
					.setPlayerId(playerId)
					.setMailId(MailId.PRESIDENT_CHANGE_APPOINT)
					.addSubTitles(name, officerIdOri, officerId)
					.addContents(name, snapshot.getIcon(), name, snapshot.getPower(), officerIdOri, officerId)
					.build());
			// 系统广播
			ChatService.getInstance().addWorldBroadcastMsg(Const.ChatType.SPECIAL_BROADCAST,
					Const.NoticeCfgId.PRESIDENT_CHANGE_TITLE, null, snapshot.getName(),
					getOfficerName(officerIdOri), getOfficerName(officerId));
		} else {
			// 发送邮件---首次任命全服邮件
			SystemMailService.getInstance().sendMail(MailParames.newBuilder()
					.setPlayerId(playerId)
					.setMailId(MailId.PRESIDENT_FIRST_APPOINT)
					.addSubTitles(name, officerId)
					.addContents(name, snapshot.getIcon(), name, snapshot.getPower(), officerId)
					.build());
			// 系统广播
			ChatService.getInstance().addWorldBroadcastMsg(Const.ChatType.SPECIAL_BROADCAST,
					Const.NoticeCfgId.PRESIDENT_APPOINT_TITLE, null, snapshot.getName(), getOfficerName(officerId));
		}

		String playerNameUnset = "";
		if (!HawkOSOperator.isEmptyString(tarEntity.getPlayerId())) {
			playerNameUnset = GlobalData.getInstance().getPlayerNameById(tarEntity.getPlayerId());
			// 发送邮件---被撤职邮件
			SystemMailService.getInstance().sendMail(MailParames.newBuilder()
					.setPlayerId(tarEntity.getPlayerId())
					.setMailId(MailId.PRESIDENT_REPEAL_APPOINT_TO_OTHER)
					.addSubTitles(snapshot.getName())
					.addContents(officerId, snapshot.getIcon(), snapshot.getName(), snapshot.getPower())
					.build());
			// 系统广播
			if (curEntity != null) {
				ChatService.getInstance().addWorldBroadcastMsg(Const.ChatType.SPECIAL_BROADCAST,
						Const.NoticeCfgId.PRESIDENT_CHANGE_TITLE_WITH_DISMISS, null, snapshot.getName(),
						getOfficerName(officerIdOri), getOfficerName(officerId), playerNameUnset);
			} else {
				ChatService.getInstance().addWorldBroadcastMsg(Const.ChatType.SPECIAL_BROADCAST,
						Const.NoticeCfgId.PRESIDENT_APPOINT_TITLE_WITH_DISMISS, null, snapshot.getName(),
						getOfficerName(officerId), playerNameUnset);
			}
		}

		// 设置官职，并同步玩家（1或2个）的作用号数据
		PresidentOfficier.getInstance().setOfficer(playerId, officerId);

		int period = PresidentFightService.getInstance().getPresidentCity().getTurnCount();
		OfficerRecord.Builder info = OfficerRecord.newBuilder();
		info.setTime(now);
		info.setPlayerNameSet(snapshot.getName());
		info.setOriOfficerId(officerIdOri);
		info.setCurOfficerId(officerId);
		info.setPlayerNameUnset(playerNameUnset);
		LocalRedis.getInstance().addOfficerRecord(period, info);

		player.responseSuccess(protocol.getType());

		// 同步改变的两条官职数据
		if (curEntity != null) {
			OfficerInfo.Builder curInfo = OfficerInfo.newBuilder();
			curInfo.setOfficerId(curEntity.getOfficerId());
			curInfo.setEndTime(curEntity.getEndTime());
			if (curEntity.getPlayerId() != null && !curEntity.getPlayerId().equals("")) {
				curInfo.setPlayerMsg(BuilderUtil.genMiniPlayer(curEntity.getPlayerId()));
			}

			builder.addOfficers(curInfo);
		}

		long endTime = PresidentFightService.getInstance().getPresidentCity().getEndTime();
		builder.setAppointEndTime(endTime + PresidentConstCfg.getInstance().getAppointTime());
		OfficerInfo.Builder tarInfo = OfficerInfo.newBuilder();
		tarInfo.setOfficerId(tarEntity.getOfficerId());
		tarInfo.setEndTime(tarEntity.getEndTime());
		tarInfo.setPlayerMsg(BuilderUtil.genMiniPlayer(playerId));
		builder.addOfficers(tarInfo);
		sendProtocol(HawkProtocol.valueOf(HP.code.OFFICER_INFO_SYNC_S_VALUE, builder));

		// 把某某官职任命给了谁
		LogUtil.logAppointOfficer(player, snapshot, officerId);

		boolean online = GlobalData.getInstance().isOnline(playerId);
		if (online) {
			snapshot.getPush().syncPlayerInfo();
		}
		
		return true;
	}

	private String getOfficerName(int id) {
		OfficerCfg cfg = HawkConfigManager.getInstance().getConfigByKey(OfficerCfg.class, id);
		if (cfg != null) {
			return cfg.getOfficeName();
		}
		return "";
	}

	/**
	 * 解除官职任命（撤职）
	 */
	@ProtocolHandler(code = HP.code.OFFICER_UNSET_C_VALUE)
	private boolean onUnsetOfficer(HawkProtocol protocol) {
		OfficerSetReq req = protocol.parseProtocol(OfficerSetReq.getDefaultInstance());

		int officerId = req.getOfficerId();
		String playerId = req.getPlayerId();

		// 官职不存在
		OfficerType officerType = OfficerType.valueOf(officerId);
		if (officerType == null) {
			sendError(protocol.getType(), Status.Error.OFFICER_TYPE_NOT_EXIST);
			return false;
		}

		// 元首不可以卸任
		if (officerType == OfficerType.OFFICER_01) {
			sendError(protocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);
			return false;
		}

		// 不是元首不可任命官职
		if (!player.getId().equals(PresidentFightService.getInstance().getPresidentPlayerId())) {
			sendError(protocol.getType(), Status.Error.PRESIDENT_NOT_KING);
			return false;
		}

		OfficerEntity tarEntity = PresidentOfficier.getInstance().getEntityById(officerId);
		if (tarEntity == null) {
			sendError(protocol.getType(), Status.Error.OFFICER_TYPE_NOT_EXIST);
			return false;
		}

		// 校验官职对应的玩家
		if (HawkOSOperator.isEmptyString(playerId) || !playerId.equals(tarEntity.getPlayerId())) {
			sendError(protocol.getType(), Status.SysError.PARAMS_INVALID);
			return false;
		}

		PresidentOfficier.getInstance().unsetOfficer(playerId, officerId);

		player.responseSuccess(protocol.getType());

		Player snapshot = GlobalData.getInstance().makesurePlayer(playerId);
		if (snapshot != null) {
			int period = PresidentFightService.getInstance().getPresidentCity().getTurnCount();
			OfficerRecord.Builder info = OfficerRecord.newBuilder();
			info.setTime(HawkTime.getMillisecond());
			info.setPlayerNameSet("");
			info.setOriOfficerId(officerId);
			info.setCurOfficerId(OfficerType.OFFICER_00_VALUE);
			info.setPlayerNameUnset(snapshot.getName());
			LocalRedis.getInstance().addOfficerRecord(period, info);
		}

		OfficerInfoSync.Builder builder = OfficerInfoSync.newBuilder();
		OfficerInfo.Builder tarInfo = OfficerInfo.newBuilder();
		tarInfo.setOfficerId(tarEntity.getOfficerId());
		tarInfo.setEndTime(tarEntity.getEndTime());
		// 这里已经是没有玩家ID的
		// tarInfo.setPlayerMsg(BuilderUtil.genMiniPlayer(tarEntity.getPlayerId()));
		builder.addOfficers(tarInfo);
		long endTime = PresidentFightService.getInstance().getPresidentCity().getEndTime();
		builder.setAppointEndTime(endTime + PresidentConstCfg.getInstance().getAppointTime());
		sendProtocol(HawkProtocol.valueOf(HP.code.OFFICER_INFO_SYNC_S_VALUE, builder));

		// 被撤职公告
		if (snapshot != null) {
			ChatService.getInstance().addWorldBroadcastMsg(Const.ChatType.SPECIAL_BROADCAST,
					Const.NoticeCfgId.PRESIDENT_PLAYER_BE_DISMISSED, null, snapshot.getName(),
					getOfficerName(officerId));
		}
		// 发送邮件---被撤职邮件
		SystemMailService.getInstance().sendMail(MailParames.newBuilder()
				.setPlayerId(playerId)
				.setMailId(MailId.PRESIDENT_REPEAL_APPOINT)
				.addSubTitles(officerId)
				.addContents(officerId)
				.build());
		
		boolean online = GlobalData.getInstance().isOnline(playerId);
		if (online) {
			snapshot.getPush().syncPlayerInfo();
		}
		return true;
	}

	/**
	 * 征税信息同步（打开征税面板）
	 */
	@ProtocolHandler(code = HP.code.PRESIDENT_TAX_INFO_SYNC_C_VALUE)
	private boolean onSyncTaxInfo(HawkProtocol protocol) {
		TaxGuildInfoSync.Builder builder = TaxGuildInfoSync.newBuilder();

		// 只有元首可以查看征税对象
		if (PresidentFightService.getInstance().isPresidentPlayer(player.getId())) {
			addTaxGuildInfo(builder);
		}

		final int MAX = 20;// 最多返回20条记录
		int period = PresidentFightService.getInstance().getPresidentCity().getTurnCount();
		while (period > 0 && builder.getRecordsCount() < MAX) {
			List<String> recordList = LocalRedis.getInstance().getTaxGuildRecord(period);
			if (recordList != null && recordList.size() > 0) {
				for (String string : recordList) {
					TaxGuildRecord.Builder info = TaxGuildRecord.newBuilder();
					try {
						JsonFormat.merge(string, info);
						builder.addRecords(0, info);
						if (builder.getRecordsCount() >= MAX * 1.5) {// 在一期之内可以超50%的记录以完整记录本期征税
							break;
						}
					} catch (ParseException e) {
						HawkException.catchException(e);
					}
				}
			}
			period--;
		}

		sendProtocol(HawkProtocol.valueOf(HP.code.PRESIDENT_TAX_INFO_SYNC_S_VALUE, builder));

		return true;
	}

	private void addTaxGuildInfo(TaxGuildInfoSync.Builder builder) {
		// Set<String> guildList = ManorService.getInstance().getTaxGuildIdList();
		// Map<String, String> taxInfo = LocalRedis.getInstance().getTaxGuildInfo();
		// if (taxInfo != null && taxInfo.size() > 0) {
		// for (String guildId : taxInfo.keySet()) {
		// long taxTime = Long.valueOf(taxInfo.get(guildId));
		// TaxGuildInfo.Builder info = genTaxGuildInfo(guildId, taxTime);
		// if (info == null) {
		// continue;
		// }
		// builder.addTaxInfos(info);
		// if (guildList.contains(guildId)) {
		// guildList.remove(guildId);
		// }
		// }
		// }
		//
		// //未被征税的数据
		// String guildIdKing = PresidentService.getInstance().getPresidentGuildId();
		// for (String guildId : guildList) {
		// if (guildId.equals(guildIdKing)) {
		// continue;
		// }
		// TaxGuildInfo.Builder info = genTaxGuildInfo(guildId, 0);
		// if (info == null) {
		// continue;
		// }
		// builder.addTaxInfos(info);
		// }
	}

	// /**
	// * 生成征税pb
	// * @return
	// */
	// private TaxGuildInfo.Builder genTaxGuildInfo(String guildId, long taxTime) {
	// String playerId = GuildService.getInstance().getGuildLeaderId(guildId);
	// Player tarPlayer = GlobalData.getInstance().makesurePlayer(playerId);
	// if (tarPlayer == null) {
	// return null;
	// }
	//
	// TaxGuildInfo.Builder info = TaxGuildInfo.newBuilder();
	// info.setGuildId(guildId);
	// info.setPlayerId(playerId);
	// info.setPlayerName(tarPlayer.getName());
	// info.setGuildIcon(tarPlayer.getGuildFlag());
	// info.setGuildLevel(GuildService.getInstance().getGuildLevel(guildId));
	// info.setGuildTag(GuildService.getInstance().getGuildTag(guildId));
	// info.setGuildName(tarPlayer.getGuildName());
	// info.setTaxTime(taxTime);
	//
	// // 征过税则不显示资源数
	// info.setGoldore(taxTime > 0 ? 0 : tarPlayer.getGoldore());
	// info.setOil(taxTime > 0 ? 0 : tarPlayer.getOil());
	// info.setSteel(taxTime > 0 ? 0 : tarPlayer.getSteelSpy());
	// info.setTombarthite(taxTime > 0 ? 0 : tarPlayer.getTombarthiteSpy());
	// return info;
	// }

	/**
	 * 征税
	 */
	@ProtocolHandler(code = HP.code.PRESIDENT_TAX_GUILD_C_VALUE)
	private boolean onTaxGuild(HawkProtocol protocol) {
		int period = PresidentFightService.getInstance().getPresidentCity().getTurnCount();
		TaxGuildReq req = protocol.parseProtocol(TaxGuildReq.getDefaultInstance());

		String playerId = req.getPlayerId();
		Player tarPlayer = GlobalData.getInstance().makesurePlayer(playerId);
		if (tarPlayer == null) {
			sendError(protocol.getType(), Status.SysError.ACCOUNT_NOT_EXIST);
			return false;
		}

		// 不是元首不可征税
		if (!player.getId().equals(PresidentFightService.getInstance().getPresidentPlayerId())) {
			sendError(protocol.getType(), Status.Error.PRESIDENT_NOT_KING);
			return false;
		}

		boolean needRefresh = false;
		String guildId = tarPlayer.getGuildId();
		if (HawkOSOperator.isEmptyString(guildId)) {
			sendError(protocol.getType(), Status.Error.GUILD_TAX_PLAYER_CHANGED);
			needRefresh = true;
		}

		else if (!tarPlayer.getId().equals(GuildService.getInstance().getGuildLeaderId(guildId))) {
			sendError(protocol.getType(), Status.Error.GUILD_TAX_PLAYER_CHANGED);
			needRefresh = true;
		}

		else if (guildId.equals(PresidentFightService.getInstance().getPresidentGuildId())) {
			sendError(protocol.getType(), Status.Error.GUILD_TAX_SELF);
			needRefresh = true;
		}

		else if (!HawkOSOperator.isEmptyString(LocalRedis.getInstance().getTaxGuildInfo(guildId))) {
			sendError(protocol.getType(), Status.Error.GUILD_TAXED);
			needRefresh = true;
		}

		// else if (!ManorService.getInstance().isTarTaxGuild(guildId)) {
		// sendError(protocol.getType(), Status.Error.GUILD_TAX_TARGET_ERROR);
		// needRefresh = true;
		// }

		if (needRefresh) {
			TaxGuildInfoSync.Builder builder = TaxGuildInfoSync.newBuilder();
			addTaxGuildInfo(builder);
			sendProtocol(HawkProtocol.valueOf(HP.code.PRESIDENT_TAX_INFO_SYNC_S_VALUE, builder));
			return false;
		}

		double percent = PresidentConstCfg.getInstance().getTaxPercent() * GsConst.EFF_PER;
		int[] resAry = new int[] {
				(int) Math.ceil(tarPlayer.getGoldore() * percent),
				(int) Math.ceil(tarPlayer.getOil() * percent),
				(int) Math.ceil(tarPlayer.getSteelSpy() * percent),
				(int) Math.ceil(tarPlayer.getTombarthiteSpy() * percent)
		};

		// 资源类型判断
		final int[] RES_LV = WorldMapConstProperty.getInstance().getResLv();
		for (int i = 0; i < GsConst.RES_TYPE.length; i++) {
			if (player.getCityLv() < RES_LV[i]) {
				resAry[i] = 0;
			}
		}

		final AwardItems awardItems = AwardItems.valueOf(null);
		final ConsumeItems consumeItems = ConsumeItems.valueOf();
		StringBuilder strBuilder = new StringBuilder();
		boolean isAdd = false;
		for (int i = 0; i < GsConst.RES_TYPE.length; i++) {
			awardItems.addItem(Const.ItemType.PLAYER_ATTR_VALUE, GsConst.RES_TYPE[i], resAry[i]);
			consumeItems.addConsumeInfo(PlayerAttr.valueOf(GsConst.RES_TYPE[i]), resAry[i]);
			if (isAdd) {
				strBuilder.append("_");
			}
			strBuilder.append(GsConst.RES_TYPE[i]).append("_").append(resAry[i]);
			isAdd = true;
		}

		awardItems.rewardTakeAffectAndPush(player, Action.PRESIDENT_REVENUE);

		// 发消息让目标玩家自己更新
		GsApp.getInstance().postMsg(tarPlayer, TaxPlayerUpdateMsg.valueOf(consumeItems));

		// 添加征税记录
		long now = HawkTime.getMillisecond();
		LocalRedis.getInstance().addTaxGuildInfo(guildId, String.valueOf(now));

		// 记录日志
		TaxGuildRecord.Builder builder = TaxGuildRecord.newBuilder();
		builder.setTaxPlayerId(player.getId());
		builder.setTaxPlayerName(player.getName());
		builder.setTaxGuildName(player.getGuildName());
		builder.setTaxTime(now);
		builder.setPlayerId(tarPlayer.getId());
		builder.setPlayerName(tarPlayer.getName());
		builder.setGuildName(tarPlayer.getGuildName());
		LocalRedis.getInstance().addTaxGuildRecord(period, guildId, builder);

		player.responseSuccess(protocol.getType());

		// 发送邮件---征税邮件
		SystemMailService.getInstance().sendMail(MailParames.newBuilder()
				.setPlayerId(player.getId())
				.setMailId(MailId.PRESIDENT_TAX)
				.addSubTitles(tarPlayer.getName())
				.addContents(tarPlayer.getName(), strBuilder.toString())
				.build());
		// 发送邮件---被征税邮件
		SystemMailService.getInstance().sendMail(MailParames.newBuilder()
				.setPlayerId(playerId)
				.setMailId(MailId.PRESIDENT_BE_TAXED)
				.addSubTitles(player.getName())
				.addContents(strBuilder.toString())
				.build());

		// 征税系统通知
		ChatService.getInstance().addWorldBroadcastMsg(Const.ChatType.SPECIAL_BROADCAST, Const.NoticeCfgId.PRESIDENT_TAX, null, player.getName(), tarPlayer.getName());

		return true;
	}

	/**
	 * 被征税，更新资源数据
	 * 
	 * @param msg
	 */
	@MessageHandler
	private boolean onTaxedUpdatePlayerAfterWar(TaxPlayerUpdateMsg msg) {
		final ConsumeItems consumeItems = msg.getConsumeItems();
		if (consumeItems != null && consumeItems.checkConsume(player)) {
			consumeItems.consumeAndPush(player, Action.PRESIDENT_REVENUED);
		}
		return true;
	}

	/**
	 * 国王禁言玩家
	 * @param playerName : 玩家名称
	 * @param startTime : 开始时间
	 * @param endTime : 结束时间
	 * **/
	@ProtocolHandler(code = HP.code.PRESIDENT_MAKE_PLAYER_CILENT_C_VALUE)
	public boolean onPresidentMakePlayerSilent(HawkProtocol protocol) {
		PresidentSilentPlayerReq req = protocol.parseProtocol(PresidentSilentPlayerReq.getDefaultInstance());
		// 被禁言玩家名称
		String playerId = req.getPlayerId();

		// 不能禁言我自己
		if (player.getId().equals(playerId)) {
			logger.error("make president silent");
			return false;
		}

		// 我不是国王
		if (!PresidentFightService.getInstance().isPresidentPlayer(player.getId())) {
			sendError(protocol.getType(), Status.Error.PRESIDENT_NOT_KING);
			return false;
		}

		// 今天世界频道已经禁言过
		JSONObject json = LocalRedis.getInstance().getSilentPlayer(playerId);
		if (json != null) {
			if (json.getIntValue("silentChannel") == GsConst.SilentChannel.WORLD && HawkTime.isSameDay(HawkTime.getMillisecond(), json.getLong("startTime"))) {
				sendError(protocol.getType(), Status.Error.PLAYER_BE_WORLD_SILENT_TODAY);
				return false;
			}
		}

		// 禁言
		return LocalRedis.getInstance().addSilentPlayer(playerId, HawkTime.getMillisecond(), PresidentConstCfg.getInstance().getBroadCastBanTime());
	}

	/**
	 * 国王解除禁言玩家
	 * @param playerName : 玩家名称
	 * @param startTime : 开始时间
	 * @param endTime : 结束时间
	 * **/
	@ProtocolHandler(code = HP.code.PRESIDENT_CANCEL_PLAYER_CILENT_C_VALUE)
	public boolean onPresidentCancelPlayerSilent(HawkProtocol protocol) {
		PresidentSilentPlayerReq req = protocol.parseProtocol(PresidentSilentPlayerReq.getDefaultInstance());
		// 被禁言玩家名称
		String playerId = req.getPlayerId();

		// 我不是国王
		if (!PresidentFightService.getInstance().isPresidentPlayer(player.getId())) {
			sendError(protocol.getType(), Status.Error.PRESIDENT_NOT_KING);
			return false;
		}

		// 禁言
		return LocalRedis.getInstance().updateSilentTime(playerId, 0);
	}

	/**
	 * 获取王城驻军列表
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.PRESIDENT_QUARTER_INFO_C_VALUE)
	private boolean getPresidentQuarterInfo(HawkProtocol protocol) {
		PresidentFightService.getInstance().sendPresidentQuarterInfo(player);
		return true;
	}

	/**
	 * 获取王城箭塔驻军列表
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.PRESIDENT_TOWER_QUARTER_INFO_C_VALUE)
	private boolean getPresidentTowerQuarterInfo(HawkProtocol protocol) {
		PresidentTowerInfoReq req = protocol.parseProtocol(PresidentTowerInfoReq.getDefaultInstance());
		int pointId = GameUtil.combineXAndY(req.getX(), req.getY());
		PresidentFightService.getInstance().sendPresidentTowerQuarterInfo(player, pointId);
		return true;
	}

	@ProtocolHandler(code = HP.code.PRESIDENT_BUFF_OPEN_REQ_VALUE)
	private void onPreisdentOpenBuff(HawkProtocol protocol) {
		PresidentBuffOpenReq req = protocol.parseProtocol(PresidentBuffOpenReq.getDefaultInstance());
		int buffId = req.getBuffId();
		PresidentConstCfg constCfg = PresidentConstCfg.getInstance();

		// 我不是国王
		if (!PresidentFightService.getInstance().isPresidentPlayer(player.getId())) {
			sendError(protocol.getType(), Status.Error.PRESIDENT_NOT_KING);

			return;
		}

		int code = 0;
		if (constCfg.getGlobalBuffMap().containsKey(buffId)) {
			code = PresidentBuff.getInstance().onOpenBuff(player, req.getBuffId(), protocol.getType());
		} else {
			code = PresidentResource.getInstance().setPresidentResource(player, buffId);
		}
		if (code == Status.SysError.SUCCESS_OK_VALUE) {
			player.responseSuccess(protocol.getType());
			PresidentBuff.getInstance().synBuff(player);
		} else {
			player.sendError(protocol.getType(), code, 0);
		}
	}

	@ProtocolHandler(code = HP.code.PRESIDENT_BUFF_INFO_REQ_VALUE)
	private void onPresidentBuffReq(HawkProtocol protocl) {
		PresidentBuff.getInstance().synBuff(player);
	}

	@ProtocolHandler(code = HP.code.PRESIDENT_MANIFESTO_REQ_VALUE)
	private void onPresidentManifestoReq(HawkProtocol protocol) {
		PresidentOfficier.getInstance().synPresidentManifesto(player);
	}

	@ProtocolHandler(code = HP.code.PRESIDENT_MANIFESTO_UPDATE_VALUE)
	private void PresidentManifestoUpdate(HawkProtocol protocol) {
		IDIPBanInfo idipBanInfo = RedisProxy.getInstance().getIDIPBanInfo(player.getId(), IDIPBanType.BAN_KING_NOTICE);
		if (idipBanInfo != null && idipBanInfo.getEndTime() >  HawkTime.getMillisecond()) {
			player.sendIdipNotice(NoticeType.SEND_MSG, NoticeMode.NOTICE_MSG, 0, idipBanInfo.getBanMsg());
			return;
		}
		
		// 禁止输入文本
		if (!GameUtil.checkBanMsg(player)) {
			return;
		}
		
		// 禁止修改
		HawkRedisSession redisSession = RedisProxy.getInstance().getRedisSession();
		String banInfo = redisSession.hGet("president_manifest_ban", GsConfig.getInstance().getServerId());
		if (!HawkOSOperator.isEmptyString(banInfo) && HawkTime.parseTime(banInfo) + GsConst.WEEK_MILLI_SECONDS > HawkTime.getMillisecond()) {
			player.sendError(protocol.getType(), Status.SysError.MODULE_CLOSED, 0);
			return;
		}
		
		PresidentManifestoUpdate req = protocol.parseProtocol(PresidentManifestoUpdate.getDefaultInstance());
		//int threadIndex = Math.abs(player.getXid().hashCode()) % HawkTaskManager.getInstance().getThreadNum();
		if (HawkOSOperator.isEmptyString(req.getManifesto())) {
			player.sendError(protocol.getType(), Status.SysError.PARAMS_INVALID_VALUE, 0);

			return;
		}

		if (req.getManifesto().length() > PresidentConstCfg.getInstance().getManifestoLength()) {
			sendError(protocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);

			return;
		}

		// 我不是国王
		if (!PresidentFightService.getInstance().isPresidentPlayer(player.getId())) {
			sendError(protocol.getType(), Status.Error.PRESIDENT_NOT_KING);

			return;
		}
		
		JSONObject json = new JSONObject();
		json.put("msg_type", 0);
		json.put("post_id", 0);
		json.put("alliance_id", player.hasGuild() ? player.getGuildId() : "");
		json.put("param_id", "");
		GameTssService.getInstance().wordUicChatFilter(player, req.getManifesto(), 
				MsgCategory.PRESIDENT_NOTICE.getNumber(), GameMsgCategory.PRESIDENT_MANIFEST_UPDATE, 
				"", json, protocol.getType());
	}

	/**
	 * 释放技能
	 */
	@ProtocolHandler(code = HP.code.CROSS_SKILL_CAST_C_VALUE)
	private void onCastCrossSkill(HawkProtocol protocol) {
		if (!PresidentFightService.getInstance().isPresidentPlayer(player.getId()) || player.isCsPlayer()) { // 非国王或者跨服状态不给用
			player.sendError(protocol.getType(), Status.Error.PRESIDENT_NOT_KING, 0);
			return;
		}
		PBCrossSkillCastReq req = protocol.parseProtocol(PBCrossSkillCastReq.getDefaultInstance());

		boolean bfalse = CrossSkillService.getInstance().castSkill(player, req.getSkillId(), req.getParamesList());
		player.responseSuccess(protocol.getType());
		if (bfalse) {// ...
			CrossSkillSendInfo sinfo = CrossSkillSendInfo.newBuilder()
					.setCrossSkillId(req.getSkillId())
					.setFromPlayerName(player.getName())
					.setTime(HawkTime.getMillisecond())
					.build();
			RedisProxy.getInstance().addTaxSendRecord(CrossTaxSendRecord.newBuilder().setType(CorssRecordType.CROSS_SKILL).setCrossSkill(sinfo));
		}
	}
}
