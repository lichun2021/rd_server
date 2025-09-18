package com.hawk.game.module;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.BlockingDeque;

import com.hawk.game.config.*;
import com.hawk.game.item.AwardItems;
import com.hawk.game.protocol.*;
import com.hawk.log.Action;
import com.hawk.sdk.msdk.entity.PayItemInfo;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.common.IDIPBanInfo;
import com.hawk.game.entity.CustomDataEntity;
import com.hawk.game.entity.DressEntity;
import com.hawk.game.entity.item.DressItem;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.manager.AssembleDataManager;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.protocol.Dress.DressSuccessResp;
import com.hawk.game.protocol.Dress.DressTitleType;
import com.hawk.game.protocol.Dress.DressType;
import com.hawk.game.protocol.Dress.HPChangeShowTypeReq;
import com.hawk.game.protocol.Dress.HPDoDressReq;
import com.hawk.game.protocol.Dress.HPDoSignatureReq;
import com.hawk.game.protocol.Dress.HPDoUnloadReq;
import com.hawk.game.protocol.Dress.PlayerDressAskInfoRes;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.GsConst.BanPlayerOperType;
import com.hawk.game.util.GsConst.ChangeContentType;
import com.hawk.game.util.GsConst.IDIPBanType;
import com.hawk.game.util.LogUtil;
import com.hawk.game.protocol.Player.MsgCategory;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Status.IDIPErrorCode;
import com.hawk.game.protocol.Dress.PlayerDressAskReq;
import com.hawk.game.protocol.Dress.PlayerDressPlayerInfo;
import com.hawk.game.protocol.Dress.PlayerDressSendReq;
import com.hawk.game.protocol.IDIP.NoticeMode;
import com.hawk.game.protocol.IDIP.NoticeType;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.RelationService;
import com.hawk.game.tsssdk.GameTssService;
import com.hawk.game.tsssdk.GameMsgCategory;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.sdk.config.PlatformConstCfg;

/**
 * 装扮
 * 
 * @author golden
 *
 */
public class PlayerDressModule extends PlayerModule {

	private static Logger logger = LoggerFactory.getLogger("Server");

	/**
	 * tick周期
	 */
	private static final long PERIOD = 3 * 1000L;

	/**
	 * 上次检测时间
	 */
	private long lastCheckTime = 0;

	public PlayerDressModule(Player player) {
		super(player);
	}

	@Override
	public boolean onTick() {
		long currentTime = HawkTime.getMillisecond();
		if (lastCheckTime != 0 && currentTime - lastCheckTime < PERIOD) {
			return true;
		}
		if (checkDressUpdate()) {
			player.getPush().syncDressInfo();
			player.getEffect().resetEffectDress(player);
		}
		lastCheckTime = currentTime;
		return true;
	}

	@Override
	protected boolean onPlayerLogin() {
		WorldPointService.getInstance().removeShowDress(player.getId());
		checkDressInit();

		// 初始化赠送保护道具
		player.initSendTimeLimitTool();
		
		player.getPush().syncDressInfo();
		player.getPush().syncDressSendProtectInfo();
		// 推送装扮信使礼包每周赠送次数
		player.getPush().syncSendDressGiftInfo();
		//可变外显参数同步
		player.getPush().syncPlayerDressEditData();
		return true;
	}

	/**
	 * 装扮
	 * 
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.DO_DRESS_REQ_VALUE)
	public void doDress(HawkProtocol protocol) {
		HPDoDressReq req = protocol.parseProtocol(HPDoDressReq.getDefaultInstance());
		DressType dressType = req.getDressType();
		int modelType = req.getModelType();

		DressEntity dressEntity = player.getData().getDressEntity();
		DressItem dressInfo = dressEntity.getDressInfo(dressType.getNumber(), modelType);
		if (dressInfo == null) {
			return;
		}

		if (dressType == DressType.MARCH_DRESS || dressType == DressType.DERMA) {
			long endTime = dressInfo.getStartTime() + dressInfo.getContinueTime();
			dressInfo.setShowType(modelType);
			dressInfo.setShowEndTime(endTime);
			dressEntity.notifyUpdate();
			LogUtil.logDressShowChange(player, dressType.getNumber(), modelType, modelType, (int)(endTime/1000));
		}
		
		WorldPointService.getInstance().updateShowDress(player.getId(), dressType.getNumber(), dressInfo);
		player.getPush().syncDressInfo();
		player.getEffect().resetEffectDress(player);
		GameUtil.notifyDressShow(player.getId());
		
		player.responseSuccess(HP.code.DO_DRESS_REQ_VALUE);
		DressSuccessResp.Builder builder = DressSuccessResp.newBuilder();
		builder.setDressType(dressType);
		builder.setModelType(modelType);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.DRESS_SUCCESS_RESP, builder));
		
		logger.info("do dress, playerId:{}, dressType:{}, modelType:{}", player.getId(), dressType.getNumber(),
				modelType);
	}
	
	/**
	 * 请求更换装扮外观
	 * 
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.DO_CHANGE_DRESS_SHOW_REQ_VALUE)
	public void doChangeDressShowType(HawkProtocol protocol) {
		HPChangeShowTypeReq req = protocol.parseProtocol(HPChangeShowTypeReq.getDefaultInstance());
		DressType dressType = req.getDressType();
		if (dressType != DressType.MARCH_DRESS && dressType != DressType.DERMA &&
				dressType != DressType.PENDANT) {
			sendError(protocol.getType(), Status.Error.NOT_SUPPORT_DRESS_TYPE);
			logger.error("dressType not support change showType, playerId: {}, dressType: {}", player.getId(), dressType);
			return;
		}
		
		int showType = req.getShowType();
		DressEntity dressEntity = player.getData().getDressEntity();
		DressItem currentDress = WorldPointService.getInstance().getShowDress(player.getId(), dressType.getNumber());
		DressItem showDressInfo = dressEntity.getDressInfo(dressType.getNumber(), showType);
		if (currentDress == null || showDressInfo == null) {
			sendError(protocol.getType(), Status.Error.DRESS_ITEM_NOT_FOUND);
			logger.error("currentDress or showDress not exist, playerId: {}, dressType: {}, currentDress is null: {}", player.getId(), dressType, currentDress == null);
			return;
		}
		
		DressCfg dressCfg = AssembleDataManager.getInstance().getDressCfg(currentDress.getDressType(), currentDress.getModelType());
		if (dressCfg == null || dressCfg.getCanChange() == 0) {
			sendError(protocol.getType(), Status.Error.DRESS_SHOW_CHANGE_NOT_SUPPORT);
			logger.error("currentDress cannot change showType, playerId: {}, dressType: {}, modelType: {}", player.getId(), dressType, currentDress.getModelType());
			return;
		}
		
		long showTypeEndTime = showDressInfo.getStartTime() + showDressInfo.getContinueTime();
		if (showTypeEndTime < HawkTime.getMillisecond()) {
			sendError(protocol.getType(), Status.Error.TARGET_DRESS_NOT_ACTIVATE);
			logger.error("showDress is not activated, playerId: {}, dressType: {}, showType: {}, showTypeEndTime: {}", player.getId(), dressType, showType, HawkTime.formatTime(showTypeEndTime));
			return;
		}

		DressItem dressInfo = dressEntity.getDressInfo(dressType.getNumber(), currentDress.getModelType());
		dressInfo.setShowType(showType);
		dressInfo.setShowEndTime(showTypeEndTime);
		dressEntity.notifyUpdate();
		WorldPointService.getInstance().updateShowDress(player.getId(), dressType.getNumber(), dressInfo);

		player.getPush().syncDressInfo();
		player.getEffect().resetEffectDress(player);
		GameUtil.notifyDressShow(player.getId());
		
		player.responseSuccess(protocol.getType());
		LogUtil.logDressShowChange(player, dressType.getNumber(), dressInfo.getModelType(), showType, (int)(showTypeEndTime/1000));
		
		logger.info("do change dress showType, playerId: {}, dressType: {}, modelType: {}, showType: {}", player.getId(), dressType, currentDress.getModelType(), showType);
	}

	/**
	 * 卸下装扮
	 * 
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.DO_UPLOAD_DRESS_REQ_VALUE)
	private void unloadDress(HawkProtocol protocol) {
		HPDoUnloadReq req = protocol.parseProtocol(HPDoUnloadReq.getDefaultInstance());
		DressType dressType = req.getDressType();

		DressEntity dressEntity = player.getData().getDressEntity();
		DressItem showDress = WorldPointService.getInstance().getShowDress(player.getId(), dressType.getNumber());
		if (showDress == null) {
			return;
		}

		// 切换成默认装扮
		Map<Integer, Integer> initWorldDressShowMap = WorldMapConstProperty.getInstance().getInitWorldDressShowMap();
		WorldPointService.getInstance().updateShowDress(player.getId(), dressType.getNumber(),
				dressEntity.getDressInfo(dressType.getNumber(), initWorldDressShowMap.get(dressType.getNumber())));

		player.getPush().syncDressInfo();
		player.getEffect().resetEffectDress(player);
		GameUtil.notifyDressShow(player.getId());
		logger.info("unload dress, playerId:{}, dressType:{}, modelType:{}", player.getId(), dressType.getNumber(),
				showDress.getModelType());
	}

	/**
	 * 初始装扮检测
	 */
	private void checkDressInit() {
		DressEntity dressEntity = player.getData().getDressEntity();

		Map<Integer, Integer> initWorldDressShowMap = WorldMapConstProperty.getInstance().getInitWorldDressShowMap();
		for (Entry<Integer, Integer> initWorldDressShow : initWorldDressShowMap.entrySet()) {
			// 如果已经有此初始装备，则不处理
			if (dressEntity.hasDress(initWorldDressShow.getKey(), initWorldDressShow.getValue())) {
				continue;
			}

			// 添加初始装备
			dressEntity.addOrUpdateDressInfo(initWorldDressShow.getKey(), initWorldDressShow.getValue(),
					GsConst.PERPETUAL_MILL_SECOND);
			WorldPointService.getInstance().updateShowDress(player.getId(), initWorldDressShow.getKey(), dressEntity
					.getDressInfo(initWorldDressShow.getKey(), initWorldDressShowMap.get(initWorldDressShow.getKey())));
			GameUtil.notifyDressShow(player.getId());
			logger.info("check dress init, dressType:{}, modelType:{}", player.getId(), initWorldDressShow.getKey(),
					initWorldDressShow.getValue());
		}
	}

	/**
	 * 检测更新
	 * 
	 * @return
	 */
	private boolean checkDressUpdate() {
		// 是否需要更新
		boolean needUpdate = false;
		if(checkGodDressUpdate()){
			needUpdate = true;
		}
		long currentTime = HawkTime.getMillisecond();

		// 装扮信息
		DressEntity dressEntity = player.getData().getDressEntity();
		BlockingDeque<DressItem> dressInfos = dressEntity.getDressInfo();

		Iterator<DressItem> iterator = dressInfos.iterator();
		while (iterator.hasNext()) {
			DressItem dressInfo = iterator.next();
			if (dressInfo.getShowType() != 0 && dressInfo.getModelType() != dressInfo.getShowType() && dressInfo.getShowEndTime() <= currentTime) {
				dressInfo.setShowType(dressInfo.getModelType());
				dressInfo.setShowEndTime(dressInfo.getStartTime() + dressInfo.getContinueTime());
				WorldPointService.getInstance().updateShowDress(player.getId(), dressInfo.getDressType(), dressInfo);
				GameUtil.notifyDressShow(player.getId());
				needUpdate = true;
			}
			
			// 当前装扮没有结束
			if (currentTime < dressInfo.getStartTime() + dressInfo.getContinueTime()) {
				continue;
			}
			// 移除显示
			if (WorldPointService.getInstance().rmShowDressIfExist(player.getId(), dressInfo.getDressType(),
					dressInfo.getModelType())) {
				Map<Integer, Integer> initWorldDressShowMap = WorldMapConstProperty.getInstance()
						.getInitWorldDressShowMap();
				WorldPointService.getInstance().updateShowDress(player.getId(), dressInfo.getDressType(), dressEntity
						.getDressInfo(dressInfo.getDressType(), initWorldDressShowMap.get(dressInfo.getDressType())));
				GameUtil.notifyDressShow(player.getId());
				logger.info("remove world dress show, playerId:{}, dressType:{}, modelType:{}", player.getId(),
						dressInfo.getDressType(), dressInfo.getModelType());
			}
			iterator.remove();
			needUpdate = true;
			logger.info("remove dress, playerId:{}, dressType:{}, modelType:{}", player.getId(),
					dressInfo.getDressType(), dressInfo.getModelType());
		}
		
		if (needUpdate) {
			dressEntity.notifyUpdate();
		}
		
		return needUpdate;
	}


	private boolean checkGodDressUpdate(){
		boolean needUpdate = false;
		if(!ConstProperty.getInstance().isDressGodOpen()){
			return needUpdate;
		}
		long now = HawkTime.getMillisecond();
		int godCount = 0;
		long godMinEndTime = -1;
		// 装扮信息
		DressEntity dressEntity = player.getData().getDressEntity();
		BlockingDeque<DressItem> dressInfos = dressEntity.getDressInfo();
		Iterator<DressItem> iterator = dressInfos.iterator();
		while (iterator.hasNext()) {
			DressItem dressInfo = iterator.next();
			DressCfg dressCfg = AssembleDataManager.getInstance().getDressCfg(dressInfo.getDressType(), dressInfo.getModelType());
			if (dressCfg != null && dressCfg.getIsShowMyth() == 1 && dressInfo.getStartTime() + dressInfo.getContinueTime() >= now) {
				godCount += 1;
				if (godMinEndTime == -1
						|| dressInfo.getStartTime() + dressInfo.getContinueTime() < godMinEndTime) {
					godMinEndTime = dressInfo.getStartTime() + dressInfo.getContinueTime();
				}
			}
			if(ConstProperty.getInstance().isDressGodOpen() && dressCfg.getIsShowMyth() > 0 && dressInfo.getStartTime() + dressInfo.getContinueTime() >= now){
				CustomDataEntity customData = player.getData().getCustomDataEntity(GsConst.DRESS_GOD_ACTIVE);
				if(customData == null){
					player.getData().createCustomDataEntity(GsConst.DRESS_GOD_ACTIVE, 0, "");
					needUpdate = true;
				}
			}
		}
		ConfigIterator<DressGodCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(DressGodCfg.class);
		for(DressGodCfg  cfg: configIterator){
			if(godCount >= cfg.getNeedCount()){
				DressCfg dressCfg = HawkConfigManager.getInstance().getConfigByKey(DressCfg.class, cfg.getDressId());
				if (dressCfg == null) {
					continue;
				}
				int dressType = dressCfg.getDressType();
				int modelType = dressCfg.getModelType();
				DressItem dressInfo = dressEntity.getDressInfo(dressType, modelType);
				if(dressInfo == null){
					dressInfo = new DressItem();
					dressInfo.setDressType(dressType);
					dressInfo.setModelType(modelType);
					dressEntity.addDressInfo(dressInfo);
				}
				if(dressInfo.getStartTime() + dressInfo.getContinueTime() != godMinEndTime){
					dressInfo.setStartTime(now);
					dressInfo.setContinueTime(godMinEndTime - now);
					if (dressType == DressType.MARCH_DRESS_VALUE || dressType == DressType.DERMA_VALUE) {
						dressInfo.setShowType(modelType);
						dressInfo.setShowEndTime(godMinEndTime);
					}
					needUpdate = true;
					LogUtil.logDressGodChange(player, cfg.getDressId(), dressType, modelType, now, godMinEndTime);
				}
			}
		}
		return needUpdate;
	}

	/**
	 * 请求更换签名
	 * 
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.DO_SIGNATURE_REQ_VALUE)
	private void doSignature(HawkProtocol protocol) {
		//刘渊飞：535的敏感节点要到了。可以热更一下6月3日 20点-6月5日 20点，禁止全服修改个性签名吗。。
		long timeNow = HawkTime.getMillisecond();
		long ctrlStartTime = 1717416000000L, ctrlEndTime = 1717588800000L;
		if (timeNow > ctrlStartTime && timeNow < ctrlEndTime) {
			player.sendIdipNotice(NoticeType.SEND_MSG, NoticeMode.NOTICE_MSG, 0, "因系统调整，目前禁止修改个性签名");
			return;
		}
		
		IDIPBanInfo banInfo = RedisProxy.getInstance().getIDIPBanInfo(player.getId(), IDIPBanType.BAN_SIGNATURE);
		if (banInfo != null && banInfo.getEndTime() > timeNow) {
			player.sendIdipNotice(NoticeType.SEND_MSG, NoticeMode.NOTICE_MSG, 0, banInfo.getBanMsg());
			return;
		}
		
		// 禁止输入文本
		if (!GameUtil.checkBanMsg(player)) {
			return;
		}
		
		HPDoSignatureReq req = protocol.parseProtocol(HPDoSignatureReq.getDefaultInstance());
		if (player.getVipLevel() < ConstProperty.getInstance().getSignatureVipLimit()) {
			return;
		}

		int checkResult = GameUtil.changeContentCDCheck(player.getId(), ChangeContentType.CHANGE_SIGNATURE);
		if (checkResult < 0) {
			sendError(protocol.getType(), IDIPErrorCode.CHANGE_SIGNATURE_CD_ING);
			return;
		}
		
		long endTime = RedisProxy.getInstance().getPlayerBanEndTime(player.getId(), BanPlayerOperType.BAN_CHANGE_SIG);
		if (endTime > HawkTime.getMillisecond()) {
			sendError(protocol.getType(), IDIPErrorCode.CHANGE_SIGNATURE_CD_ING);
			return;
		}
		
		JSONObject gameDataJson = new JSONObject();
		gameDataJson.put("alliance_id", player.hasGuild() ? player.getGuildId() : "");
		gameDataJson.put("param_id", "");
		
		GameTssService.getInstance().wordUicChatFilter(player, req.getSignature(), 
				MsgCategory.PLAYER_SIGNATURE.getNumber(), GameMsgCategory.PLAYER_SIGNATURE, 
				String.valueOf(checkResult), gameDataJson, protocol.getType());
	}

	/**
	 * 更新装扮称号显示类型
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.DRESS_TITLE_TYPE_REQ_VALUE)
	private void dressTitleType(HawkProtocol protocol) {
		DressTitleType req = protocol.parseProtocol(DressTitleType.getDefaultInstance());
		WorldPointService.getInstance().updateDressTitleType(player.getId(), req.getType());
		GameUtil.notifyDressShow(player.getId());
	}
	
	/**
	 * 请求玩家装扮赠送信息
	 * 
	 * @param protocol
	 */

	@ProtocolHandler(code = HP.code.PLAYER_DRESS_ASK_INFO_C_VALUE)
	public void doPlayerDressAskInfoReq(HawkProtocol protocol) {
		PlayerDressAskInfoRes.Builder builder = PlayerDressAskInfoRes.newBuilder();
		List<PlayerDressPlayerInfo> sendList = player.getData().getPlayerDressSendEntities();
		List<PlayerDressPlayerInfo> askList = player.getData().getPlayerDressAskEntities();
		builder.addAllAskLog(askList);
		builder.addAllSendLog(sendList);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.PLAYER_DRESS_ASK_INFO_S, builder));
	}

	/**
	 * 乞讨装扮
	 * 
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.PLAYER_DRESS_ASK_REQ_C_VALUE)
	public void doPlayerDressAskReq(HawkProtocol protocol) {
		IDIPBanInfo banInfo = RedisProxy.getInstance().getIDIPBanInfo(player.getId(), IDIPBanType.BAN_ASK_DRESS);
		if (banInfo != null && banInfo.getEndTime() >  HawkTime.getMillisecond()) {
			player.sendIdipNotice(NoticeType.SEND_MSG, NoticeMode.NOTICE_MSG, 0, banInfo.getBanMsg());
			return;
		}
		
		// 禁止输入文本
		if (!GameUtil.checkBanMsg(player)) {
			return;
		}
		
		try {
			MailConst.MailId mailId = MailConst.MailId.PLAYER_DRESS_FRIEND_ASK;
			PlayerDressAskReq req = protocol.parseProtocol(PlayerDressAskReq.getDefaultInstance());
			// 判断合法？
			DressCfg dressCfg = HawkConfigManager.getInstance().getConfigByKey(DressCfg.class, req.getId());
			if (null == dressCfg) {
				sendError(protocol.getType(), Status.Error.ASK_DRESS_DRESS_NOT_CFG);
				return;
			}

			Player tPlayer = GlobalData.getInstance().makesurePlayer(req.getPlayerId());
			if (null == tPlayer) {
				sendError(protocol.getType(), Status.Error.ASK_DRESS_NOT_TPLAYER);
				return;
			}

			CustomDataEntity customSetEntity = tPlayer.getData().getCustomDataEntity(GsConst.PLAYER_DRESS_ASKREFUSE);
			if (null != customSetEntity && !HawkOSOperator.isEmptyString(customSetEntity.getArg())
					&& 0 != Integer.valueOf(customSetEntity.getArg())) {
				sendError(protocol.getType(), Status.Error.ASK_DRESS_TARGET_REFUSE);
				return;
			}
			// 2盟友/1好友
			if (2 == req.getType()) {
				mailId = MailConst.MailId.PLAYER_DRESS_ALLY_ASK;
				if (!GuildService.getInstance().isInTheSameGuild(req.getPlayerId(), player.getId())) {
					sendError(protocol.getType(), Status.Error.ASK_DRESS_NOT_SM_GUILD_VALUE);
					return;
				}
			} else {
				mailId = MailConst.MailId.PLAYER_DRESS_FRIEND_ASK;
				if (!RelationService.getInstance().isFriend(player.getId(), req.getPlayerId())) {
					sendError(protocol.getType(), Status.Error.ASK_DRESS_NOT_FRIEND_VALUE);
					return;
				}
			}

			PlayerDressSendCfg cfg = HawkConfigManager.getInstance().getKVInstance(PlayerDressSendCfg.class);
			if (null == cfg) {
				sendError(protocol.getType(), Status.Error.ASK_DRESS_CFG_NOTFOUND_VALUE);
				return;
			}

			if (req.getAskMsg().getBytes().length > cfg.getDressSendMsgLen()) {
				sendError(protocol.getType(), Status.Error.ASK_DRESS_WORD_LEN);
				return;
			}
			
			JSONObject callback = new JSONObject();
			callback.put("mailId", mailId.getNumber());
			callback.put("id", req.getId());
			callback.put("playerId", req.getPlayerId());

			JSONObject gameData = new JSONObject();
			String pfIconPrimitive = tPlayer.getData().getPrimitivePfIcon();
			if (GlobalData.getInstance().isBanPortraitAccount(tPlayer.getOpenId())) {
				pfIconPrimitive = PlatformConstCfg.getInstance().getImage_def();
			}
			gameData.put("recv_account", tPlayer.getOpenId());
			gameData.put("recv_role_name", tPlayer.getName());
			gameData.put("recv_role_pic_url", pfIconPrimitive);
			gameData.put("recv_area_id", "wx".equalsIgnoreCase(tPlayer.getChannel()) ? 1 : 2);
			gameData.put("recv_plat_id", GameUtil.isAndroidAccount(tPlayer) ? 1 : 0);
			gameData.put("recv_world_id", tPlayer.getServerId());
			gameData.put("recv_role_id", tPlayer.getId());
			gameData.put("recv_role_level", tPlayer.getLevel());
			gameData.put("recv_role_battlepoint", tPlayer.getPower());
			
			GameTssService.getInstance().wordUicChatFilter(player, req.getAskMsg(), 
					MsgCategory.ASK_DRESS.getNumber(), GameMsgCategory.PLAYER_ASK_DRESS, 
					callback.toJSONString(), gameData, protocol.getType());
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 赠送装扮
	 * 
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.PLAYER_DRESS_SEND_REQ_C_VALUE)
	public void doPlayerDressSendReq(HawkProtocol protocol) {
		IDIPBanInfo banInfo = RedisProxy.getInstance().getIDIPBanInfo(player.getId(), IDIPBanType.BAN_SEND_DRESS);
		if (banInfo != null && banInfo.getEndTime() >  HawkTime.getMillisecond()) {
			player.sendIdipNotice(NoticeType.SEND_MSG, NoticeMode.NOTICE_MSG, 0, banInfo.getBanMsg());
			return;
		}
		
		// 禁止输入文本
		if (!GameUtil.checkBanMsg(player)) {
			return;
		}
		
		PlayerDressSendReq req = protocol.parseProtocol(PlayerDressSendReq.getDefaultInstance());
		MailConst.MailId mailId = MailConst.MailId.PLAYER_DRESS_FRIEND_SEND;
		try {
			// 道具合不合法
			List<RewardItem.Builder> sendItemList = RewardHelper.toRewardItemList(req.getItemStr());
			// 判断合法？
			if (sendItemList.size() != 1) {
				sendError(protocol.getType(), Status.Error.ASK_DRESS_DRESS_NOT_CFG);
				return;
			}

			ItemCfg itemCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class,
					sendItemList.get(0).getItemId());
			if (null == itemCfg) {
				sendError(protocol.getType(), Status.Error.ASK_DRESS_NOT_ITEM_CFG);
				return;
			}

			if (1 != itemCfg.getCanGive()) {
				sendError(protocol.getType(), Status.Error.ASK_DRESS_ITEM_CANNOT_SEND);
				return;
			}

			DressToolCfg dressToolCfg = HawkConfigManager.getInstance().getConfigByKey(DressToolCfg.class,
					itemCfg.getDressId());
			if (null == dressToolCfg) {
				sendError(protocol.getType(), Status.Error.ASK_DRESS_DRESS_NOT_CFG);
				return;
			}
			// dressId
			DressCfg dressCfg = HawkConfigManager.getInstance().getConfigByKey(DressCfg.class,
					dressToolCfg.getDressId());
			if (null == dressCfg) {
				sendError(protocol.getType(), Status.Error.ASK_DRESS_DRESS_NOT_CFG);
				return;
			}

			PlayerDressSendCfg dressSendCfg = HawkConfigManager.getInstance().getKVInstance(PlayerDressSendCfg.class);
			if (null == dressSendCfg) {
				sendError(protocol.getType(), Status.Error.ASK_DRESS_CFG_NOTFOUND_VALUE);
				return;
			}

			if (req.getSendMsg().getBytes().length > dressSendCfg.getDressSendMsgLen()) {
				sendError(protocol.getType(), Status.Error.ASK_DRESS_WORD_LEN);
				return;
			}
			
			if (GlobalData.getInstance().isResetAccount(req.getDestPlayerId())) {
				sendError(protocol.getType(), Status.Error.GUILD_INVITE_NOT_INGAME_FRIEND_VALUE);
				return;
			}
			
			// 是不是2盟友/1好友
			Player tPlayer = GlobalData.getInstance().makesurePlayer(req.getDestPlayerId());
			if (null == tPlayer) {
				sendError(protocol.getType(), Status.Error.ASK_DRESS_NOT_TPLAYER);
				return;
			}

			// 盟友
			if (2 == req.getType()) {
				mailId = MailConst.MailId.PLAYER_DRESS_ALLY_SEND;
				if (!GuildService.getInstance().isInTheSameGuild(req.getDestPlayerId(), player.getId())) {
					sendError(protocol.getType(), Status.Error.ASK_DRESS_NOT_SM_GUILD_VALUE);
					return;
				}
			} else {
				mailId = MailConst.MailId.PLAYER_DRESS_FRIEND_SEND;
				if (!RelationService.getInstance().isFriend(player.getId(), req.getDestPlayerId())) {
					sendError(protocol.getType(), Status.Error.ASK_DRESS_NOT_FRIEND_VALUE);
					return;
				}
			}
			
			//List<PlayerDressPlayerInfo> list = player.getData().getPlayerDressSendEntities();

			List<RewardItem.Builder> costItems = new ArrayList<RewardItem.Builder>();
			//如果装扮时间为0 就是永久的装扮使用道具2赠送
			//如果限时和永久 的都没有使用通用的
			boolean isForeverDress = false;
			
			// 信使道具
			List<ItemInfo> extraItem = null;
			if( dressToolCfg.getContinueTime() == 0){
				int num = player.getData().getItemNumByItemId(dressSendCfg.getItemList2().get(0).getItemId());
				if(num > 0){
					extraItem = dressSendCfg.getNeedItem2();
				}else{
					extraItem = dressSendCfg.getNeedItem3();
				}
				isForeverDress = true;
			}else{
				int num = player.getData().getItemNumByItemId(dressSendCfg.getItemList().get(0).getItemId());
				if(num > 0){
					extraItem = dressSendCfg.getNeedItem();
				}else{
					extraItem = dressSendCfg.getNeedItem3();
				}
			}
			
			// 赠送保护期道具
			long sendProtect = itemCfg.getProtectionPeriod();
			// 还在赠送保护期内
			if (sendProtect > 0) {
				if (player.getSendTimeLimitTool(itemCfg.getId()) + sendProtect > HawkTime.getMillisecond()) {
					for (ItemInfo item : extraItem) {
						item.setCount(item.getCount() + itemCfg.getProtectionConsume());
					}
				}
			}
			
			costItems.addAll(RewardHelper.toRewardItemImmutableList(ItemInfo.toString(extraItem)));
			costItems.addAll(sendItemList);
			
			
			List<ItemInfo> needItems = new ArrayList<>();
			JSONObject callback = new JSONObject();
			int num = 1;
			for (RewardItem.Builder rewardItem : costItems) {
				ItemInfo item = new ItemInfo(rewardItem.getItemType(), rewardItem.getItemId(), rewardItem.getItemCount()); 
				needItems.add(item);
				callback.put(String.valueOf(num++), item.toString());
			}

			
			ConsumeItems consumteItems = ConsumeItems.valueOf();
			consumteItems.addConsumeInfo(needItems);
			if (!consumteItems.checkConsume(player)) {
				if(isForeverDress){
					sendError(protocol.getType(), Status.Error.ASK_DRESS_WORD_ZERO_ITEM_VALUE);
				}else{
					sendError(protocol.getType(), Status.Error.ASK_DRESS_NOT_ITEM_VALUE);
				}
				return;
			}
			
			callback.put("destPlayerId", req.getDestPlayerId());
			callback.put("itemId", sendItemList.get(0).getItemId());
			callback.put("mailId", mailId.getNumber());
			callback.put("itemStr", req.getItemStr());
			
			JSONObject gameData = new JSONObject();
			String pfIconPrimitive = tPlayer.getData().getPrimitivePfIcon();
			if (GlobalData.getInstance().isBanPortraitAccount(tPlayer.getOpenId())) {
				pfIconPrimitive = PlatformConstCfg.getInstance().getImage_def();
			}
			gameData.put("recv_account", tPlayer.getOpenId());
			gameData.put("recv_role_name", tPlayer.getName());
			gameData.put("recv_role_pic_url", pfIconPrimitive);
			gameData.put("recv_area_id", "wx".equalsIgnoreCase(tPlayer.getChannel()) ? 1 : 2);
			gameData.put("recv_plat_id", GameUtil.isAndroidAccount(tPlayer) ? 1 : 0);
			gameData.put("recv_world_id", tPlayer.getServerId());
			gameData.put("recv_role_id", tPlayer.getId());
			gameData.put("recv_role_level", tPlayer.getLevel());
			gameData.put("recv_role_battlepoint", tPlayer.getPower());
			
			GameTssService.getInstance().wordUicChatFilter(player, req.getSendMsg(), 
					MsgCategory.SEND_DRESS.getNumber(), GameMsgCategory.PLAYER_SEND_DRESS, 
					callback.toJSONString(), gameData, protocol.getType());
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	@ProtocolHandler(code = HP.code.DRESS_BUY_REQ_VALUE)
	public void doDressBuy(HawkProtocol protocol) {
		Dress.DressBuyReq req = protocol.parseProtocol(Dress.DressBuyReq.getDefaultInstance());
		DressToolCfg dressToolCfg = HawkConfigManager.getInstance().getConfigByKey(DressToolCfg.class,
				req.getCfgId());
		if(dressToolCfg == null){
			return;
		}
		if(!dressToolCfg.canBuy()){
			return;
		}
		DressCfg dressCfg = HawkConfigManager.getInstance().getConfigByKey(DressCfg.class,
				dressToolCfg.getDressId());
		if(dressCfg == null){
			return;
		}
		DressEntity dressEntity = player.getData().getDressEntity();
		DressItem dressInfo = dressEntity.getDressInfo(dressCfg.getDressType(), dressCfg.getModelType());
		if (dressInfo != null && dressInfo.getContinueTime() >= GsConst.PERPETUAL_MILL_SECOND) {
			return;
		}
		int count = player.getData().getItemNumByItemId(dressToolCfg.getItem());
		if(count > 0){
			return;
		}
		ItemInfo cost = ItemInfo.valueOf(dressToolCfg.getCostItem());
		ConsumeItems consume = ConsumeItems.valueOf();
		consume.addConsumeInfo(cost, false);
		if (!consume.checkConsume(player, protocol.getType())) {
			return;
		}
		if (consume.getBuilder().hasAttrInfo() && consume.getBuilder().getAttrInfo().getDiamond() > 0) {
			consume.addPayItemInfo(new PayItemInfo(String.valueOf(dressToolCfg.getItem()), (int)cost.getCount(), 1));
		}
		consume.consumeAndPush(player, Action.DRESS_BUY);// 扣除技能道具
		AwardItems awardItem = AwardItems.valueOf();
		awardItem.addItem(Const.ItemType.TOOL_VALUE, dressToolCfg.getItem(), 1);
		awardItem.rewardTakeAffectAndPush(player, Action.DRESS_BUY);
	}
	
	
}