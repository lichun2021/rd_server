package com.hawk.game.module;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.hawk.game.invoker.xhjz.XHJZWarChangeNameMsgInvoker;
import com.hawk.game.service.xhjzWar.XHJZWarService;
import org.hawk.annotation.MessageHandler;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.app.HawkApp;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;
import org.hawk.thread.HawkThreadPool;
import org.hawk.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.hawk.common.IDIPBanInfo;
import com.hawk.game.GsApp;
import com.hawk.game.GsConfig;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.config.ItemCfg;
import com.hawk.game.config.PlayerLevelExpCfg;
import com.hawk.game.config.PlayerShowCfg;
import com.hawk.game.config.ShopCfg;
import com.hawk.game.config.WorldMapConstProperty;
import com.hawk.game.crossactivity.CrossActivityService;
import com.hawk.game.crossproxy.CrossProxy;
import com.hawk.game.crossproxy.CrossService;
import com.hawk.game.data.ReportingFilterCallbackData;
import com.hawk.game.data.ReportingFilterInfo;
import com.hawk.game.entity.StatisticsEntity;
import com.hawk.game.entity.StatusDataEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.invoker.PlayerChangeNameMsgInvoker;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.log.BehaviorLogger;
import com.hawk.game.log.BehaviorLogger.Params;
import com.hawk.game.module.college.entity.CollegeMemberEntity;
import com.hawk.game.msg.ReportingInfoFilterFinishMsg;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.player.PowerElectric;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.PlayerState;
import com.hawk.game.protocol.Const.StateType;
import com.hawk.game.protocol.IDIP.NoticeMode;
import com.hawk.game.protocol.IDIP.NoticeType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MilitaryCollege.CollegeAuth;
import com.hawk.game.protocol.Player.EffectPB;
import com.hawk.game.protocol.Player.HPGetPlayerLevelUpReward;
import com.hawk.game.protocol.Player.HPPlayerEffectSync;
import com.hawk.game.protocol.Player.MsgCategory;
import com.hawk.game.protocol.Player.OtherPlayerDetailResp;
import com.hawk.game.protocol.Player.OtherPlayerEffectReq;
import com.hawk.game.protocol.Player.PlayerChangeIconReq;
import com.hawk.game.protocol.Player.PlayerChangeNameReq;
import com.hawk.game.protocol.Player.PlayerCheckNameReq;
import com.hawk.game.protocol.Player.PlayerCheckNameResp;
import com.hawk.game.protocol.Player.PlayerDetailReq;
import com.hawk.game.protocol.Player.PlayerDetailRes;
import com.hawk.game.protocol.Player.RemoveShieldPlayerReq;
import com.hawk.game.protocol.Player.ReportingCDPB;
import com.hawk.game.protocol.Player.ReportingInfoPB;
import com.hawk.game.protocol.Player.SensitiveWordCheckReq;
import com.hawk.game.protocol.Player.SensitiveWordCheckResp;
import com.hawk.game.protocol.Player.ShieldPlayerInfo;
import com.hawk.game.protocol.Player.ShieldPlayerReq;
import com.hawk.game.protocol.Player.ShieldPlayerResp;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Status.IDIPErrorCode;
import com.hawk.game.protocol.Status.SysError;
import com.hawk.game.protocol.SysProtocol.ClosePasswdReq;
import com.hawk.game.protocol.SysProtocol.PasswdCheckReq;
import com.hawk.game.protocol.SysProtocol.PasswdInfoPB;
import com.hawk.game.protocol.SysProtocol.PersonalProtectSetReq;
import com.hawk.game.protocol.SysProtocol.SetPasswdReq;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.SearchService;
import com.hawk.game.tsssdk.GameTssService;
import com.hawk.game.tsssdk.GameMsgCategory;
import com.hawk.game.util.BuilderUtil;
import com.hawk.game.util.EffectParams;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst.BanPlayerOperType;
import com.hawk.game.util.GsConst.ChangeContentType;
import com.hawk.game.util.GsConst.GlobalControlType;
import com.hawk.game.util.GsConst.IDIPBanType;
import com.hawk.game.util.LogUtil;
import com.hawk.game.world.service.WorldPlayerService;
import com.hawk.game.world.service.WorldRobotService;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;
import com.hawk.log.LogConst.LogMsgType;
import com.hawk.log.LogConst.PowerChangeReason;
import com.hawk.log.Source;
import com.hawk.sdk.msdk.entity.PayItemInfo;

/**
 * 玩家功能操作模块
 * @author luke
 */
public class PlayerOperationModule extends PlayerModule {

	static final Logger logger = LoggerFactory.getLogger("Server");

	/**
	 * 构造函数
	 * @param player
	 */
	public PlayerOperationModule(Player player) {
		super(player);
	}

	/**
	 * @param protocol
	 * @return 打开指挥官面板
	 */
	@ProtocolHandler(code = HP.code.OPEN_PLAYER_BOARD_C_VALUE)
	private boolean openPlayerBoard(HawkProtocol protocol) {
		player.responseSuccess(protocol.getType());
		return true;
	}
	
	/**
	 * 点击伊娃红点
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.CLICK_YIWA_REDPOINT_C_VALUE)
	private boolean onClickYiwaRedPoint(HawkProtocol protocol) {
		RedisProxy.getInstance().deleteYiwaTipMsg(player.getId());
		player.responseSuccess(protocol.getType());
		return true;
	}

	/**
	 * @param protocol
	 * @return 新手引导激活VIP
	 */
	@ProtocolHandler(code = HP.code.GUIDE_ACTIVATE_VIP_VALUE)
	private boolean onGuideActivateVip(HawkProtocol protocol) {
		//满足大本等级、vip未激活等条件 玩家等级6等配置
		if (!player.getData().getVipActivated() &&
				player.getCityLv() < WorldMapConstProperty.getInstance().getStepCityLevel1() &&
				player.getLevel() < WorldMapConstProperty.getInstance().getStepCityLevel1()) {
			player.responseSuccess(protocol.getType());
			player.getPush().syncPlayerInfo();

			BehaviorLogger.log4Service(player, Source.SYSTEM_OPERATION, Action.NEWBIE_GUIDE);
			return true;
		}

		logger.error("guide active vip failed, playerId: {}, playerLv: {}, cityLv: {}", player.getId(), player.getLevel(), player.getCityLv());
		sendError(protocol.getType(), Status.Error.VIP_NOT_ACTIVE);
		return false;
	}

	/**
	 * 升级奖励
	 * 
	 * @param protocol
	 * @return 
	 */
	@ProtocolHandler(code = HP.code.LEVEL_UP_REWARD_C_VALUE)
	private boolean onPlayerGetLevelUpReward(HawkProtocol protocol) {
		HPGetPlayerLevelUpReward req = protocol.parseProtocol(HPGetPlayerLevelUpReward.getDefaultInstance());

		int rewardLevel = req.getRewardLevel();

		StatusDataEntity lvUpStateEntity = player.getData().getStateById(PlayerState.REWARD_LEVEL_VALUE, StateType.PLAYER_STATE_VALUE);
		if (lvUpStateEntity == null || lvUpStateEntity.getVal() != rewardLevel - 1) {
			logger.error("player level up reward failed, playerId: {}, playerLv: {}, rewardLv: {}", player.getId(), player.getLevel(), rewardLevel);
			sendError(protocol.getType(), Status.Error.LEVELUP_REWARD_LEVEL_ERROR);
			return false;
		}

		PlayerLevelExpCfg levelExpCfg = HawkConfigManager.getInstance().getConfigByKey(PlayerLevelExpCfg.class, rewardLevel);
		if (levelExpCfg == null || levelExpCfg.getBonusList().size() <= 0) {
			logger.error("player level up reward failed, levelExpCfgerror, playerId: {}, playerLv: {}, rewardLv: {}", player.getId(), player.getLevel(), rewardLevel);
			sendError(protocol.getType(), Status.Error.LEVELUP_REWARD_NOT_EXIST);
			return false;
		}

		BehaviorLogger.log4Service(player, Source.ATTR_CHANGE, Action.PLAYER_LEVEL_UP_AWARD,
				Params.valueOf("curLv", player.getLevel()), Params.valueOf("rewardLv", rewardLevel));

		AwardItems award = AwardItems.valueOf();
		award.addItemInfos(levelExpCfg.getBonusList());
		award.rewardTakeAffectAndPush(player, Action.PLAYER_LEVEL_UP_AWARD, true);

		lvUpStateEntity.setVal(rewardLevel);
		player.getPush().syncPlayerStatusInfo(false, lvUpStateEntity);

		player.responseSuccess(protocol.getType());

		return true;
	}

	/**
	 * 角色更换/购买icon
	 * 
	 * 修改头像不再是走此协议，而是 PlayerImageModule中 的 HP.code.PLAYER_USE_NEW_IMAGE_OR_CIRCLE
	 * 
	 * @param protocol
	 * @return 
	 */
	@ProtocolHandler(code = HP.code.PLAYER_CHANGE_ICON_C_VALUE)
	private boolean onPlayerChangeIconRequest(HawkProtocol protocol) {
		PlayerChangeIconReq req = protocol.parseProtocol(PlayerChangeIconReq.getDefaultInstance());

		boolean useGold = req.getUseGold();
		int iconId = req.getIconId();
		int cfgId = iconId + PlayerShowCfg.ID_BASE;
		PlayerShowCfg cfg = HawkConfigManager.getInstance().getConfigByKey(PlayerShowCfg.class, cfgId);
		if (cfg == null) {
			logger.error("player change icon failed, config error, playerId: {}, iconId: {}, cfgId: {}", player.getId(), iconId, cfgId);
			sendError(protocol.getType(), Status.Error.PLAYER_ICON_NOT_EXIST);
			return false;
		}

		int[] iconIds = getIconIds();

		// 购买切换头像
		if (useGold && cfg.getType() == 1) {
			ConsumeItems consumeItems = ConsumeItems.valueOf();
			consumeItems.addConsumeInfo(Const.PlayerAttr.GOLD, cfg.getPrice());
			if (!consumeItems.checkConsume(player, protocol.getType())) {
				return false;
			}
			consumeItems.consumeAndPush(player, Action.PLAYER_BUY_ICON);
			addIconId2Buy(iconId);
		} else {
			if (cfg.getType() == 1 && !findIconInBuyIds(iconId, iconIds)) {
				logger.error("player change icon failed, playerId: {}, iconId: {}", player.getId(), iconId);
				sendError(protocol.getType(), Status.Error.PLAYER_ICON_NOT_BUY);
				return false;
			}
		}

		BehaviorLogger.log4Service(player, Source.ATTR_CHANGE, Action.PLAYER_BUY_ICON,
				Params.valueOf("curIcon", player.getEntity().getIcon()), Params.valueOf("tarIcon", iconId));

		player.getEntity().setIcon(iconId);
		player.responseSuccess(protocol.getType());
		player.getPush().syncPlayerInfo();

		// 修改玩家城点数据
		WorldPlayerService.getInstance().updatePlayerPointInfo(player.getId(), player.getName(),
				player.getCityLv(), player.getIcon(), player.getData().getPersonalProtectVals());

		return true;
	}

	private void addIconId2Buy(int iconId) {
		String iconBuy = player.getEntity().getIconBuy();
		if (HawkOSOperator.isEmptyString(iconBuy)) {
			iconBuy = String.valueOf(iconId);
		} else {
			iconBuy += "," + iconBuy;
		}
		player.getEntity().setIconBuy(iconBuy);
	}

	private int[] getIconIds() {
		String iconBuy = player.getEntity().getIconBuy();
		if (HawkOSOperator.isEmptyString(iconBuy)) {
			return null;
		}
		String[] iconAry = iconBuy.split(",");
		int[] ids = new int[iconAry.length];
		for (int i = 0; i < iconAry.length; i++) {
			ids[i] = Integer.parseInt(iconAry[i]);
		}
		return ids;
	}

	private boolean findIconInBuyIds(int iconId, int[] iconIds) {
		if (iconIds == null || iconIds.length <= 0) {
			return false;
		}
		for (int i = 0; i < iconIds.length; i++) {
			if (iconIds[i] == iconId) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 玩家操作没有物品（如改名，行军加速，迁城等），调用协议时直接扣金币
	 * 
	 * @param hpCode 协议号
	 * @param shopId 商店里的商品常量
	 * @param itemId 道具ID常量
	 * @param count  道具数量
	 * @param action Action类型 错误码
	 */
	public int costGoldFromItem(int hpCode, Const.ShopId shopId, Const.ItemId itemId, int count, Action action) {
		ShopCfg shopCfg = HawkConfigManager.getInstance().getConfigByKey(ShopCfg.class, shopId.getNumber());
		if (shopCfg == null || shopCfg.getShopItemID() != itemId.getNumber()) {
			return Status.SysError.CONFIG_ERROR_VALUE;
		}

		//检查钱够不够，以及扣钱
        ItemInfo priceItem = shopCfg.getPriceItemInfo();
        priceItem.setCount(priceItem.getCount() * count);
        
        ConsumeItems consume = ConsumeItems.valueOf();
        consume.addConsumeInfo(priceItem, true);
		if (!consume.checkConsume(player, hpCode)) {
			return Status.Error.GOLD_NOT_ENOUGH_VALUE;
		}

		if (consume.getBuilder().hasAttrInfo() && consume.getBuilder().getAttrInfo().getDiamond() > 0) {
			consume.addPayItemInfo(new PayItemInfo(String.valueOf(itemId), (int)priceItem.getCount(), count));
		}
		consume.consumeAndPush(player, action);
		return Status.SysError.SUCCESS_OK_VALUE;
	}

	/**
	 * 玩家操作扣物品（如改名，行军加速，迁城等），判断物品是否存在，足够
	 * 
	 * @param hpCode 协议号
	 * @param itemId 道具常量
	 * @param count  道具数量
	 * @param action 操作类型 错误码
	 */
	public int costItem(int hpCode, Const.ItemId itemId, int count, Action action) {
		// 是否存在本物品
		int itemCnt = player.getData().getItemNumByItemId(itemId.getNumber());
		if (itemCnt == 0) {
			return Status.Error.ITEM_NOT_FOUND_VALUE;
		}

		// 物品配置
		ItemCfg itemCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, itemId.getNumber());
		if (itemCfg == null) {
			return Status.SysError.CONFIG_ERROR_VALUE;
		}

		//消耗物品
		ConsumeItems consume = ConsumeItems.valueOf();
		consume.addItemConsume(itemId.getNumber(), count);
		if (!consume.checkConsume(player, hpCode)) {
			return Status.Error.ITEM_NOT_ENOUGH_VALUE;
		}

		consume.consumeAndPush(player, action);

		return Status.SysError.SUCCESS_OK_VALUE;
	}
	
	/**
	 * 敏感词检测
	 * 
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.SENSITIVE_WORD_CHECK_C_VALUE)
	private void onCheckSensitiveWord(HawkProtocol protocol) {
//		// 禁止输入文本，不能直接调这个接口，否则前端有些场景会出现弹框提示后转菊花的情况
//		if (!GameUtil.checkBanMsg(player)) {
//			return;
//		}
		
		// 禁言玩家推送禁言提示
		if (player.getEntity().getSilentTime() > HawkTime.getMillisecond()) {
			IDIPBanInfo banInfo = RedisProxy.getInstance().getIDIPBanInfo(player.getId(), IDIPBanType.BAN_SEND_MSG);
			if (banInfo != null) {
				SensitiveWordCheckResp.Builder resp = SensitiveWordCheckResp.newBuilder();
				resp.setSensitive(true);
				resp.setErrorCode(Status.NameError.CONTAINS_FILTER_WORD_VALUE);
				resp.setMsg(banInfo.getBanMsg());
				player.sendProtocol(HawkProtocol.valueOf(HP.code.SENSITIVE_WORD_CHECK_S, resp));
				return;
			}
		}
				
		SensitiveWordCheckReq req = protocol.parseProtocol(SensitiveWordCheckReq.getDefaultInstance());
		String word = req.getWord();
		if (HawkOSOperator.isEmptyString(word)) {
			SensitiveWordCheckResp.Builder resp = SensitiveWordCheckResp.newBuilder();
			resp.setSensitive(false);
			player.sendProtocol(HawkProtocol.valueOf(HP.code.SENSITIVE_WORD_CHECK_S, resp));
			return;
		}
		
		// 先走本地过滤
		Collection<String> silenceWords = GsApp.getInstance().getSilenceWordFilter().find(word);
		if (silenceWords != null && !silenceWords.isEmpty()) {
			SensitiveWordCheckResp.Builder resp = SensitiveWordCheckResp.newBuilder();
			resp.setSensitive(true);
			resp.setErrorCode(Status.NameError.CONTAINS_FILTER_WORD_VALUE);
			player.sendProtocol(HawkProtocol.valueOf(HP.code.SENSITIVE_WORD_CHECK_S, resp));
			return;
		}
		
		// 搜索游戏内好友，
		int category = GameMsgCategory.SENSITIVE_WORD_CHECK;
		if (req.hasCategory()) {
			category = req.getCategory().getNumber();
		}
		
		JSONObject json = null;
		if (category == MsgCategory.ENERGY_NAME.getNumber()) {
			IDIPBanInfo banInfo = RedisProxy.getInstance().getIDIPBanInfo(player.getId(), IDIPBanType.BAN_ENERGY_MATRIX);
			if (banInfo != null && banInfo.getEndTime() >  HawkTime.getMillisecond()) {
				SensitiveWordCheckResp.Builder resp = SensitiveWordCheckResp.newBuilder();
				resp.setSensitive(true);
				resp.setErrorCode(Status.NameError.CONTAINS_FILTER_WORD_VALUE);
				resp.setMsg(banInfo.getBanMsg());
				player.sendProtocol(HawkProtocol.valueOf(HP.code.SENSITIVE_WORD_CHECK_S, resp));
				return;
			}
			
			json = new JSONObject();
			json.put("alliance_id", player.hasGuild() ? player.getGuildId() : "");
			json.put("param_id", req.hasParam() ? req.getParam().trim() : "");
		} 
		
		GameTssService.getInstance().wordUicChatFilter(player, word, 
				category, GameMsgCategory.SENSITIVE_WORD_CHECK, 
				"", json, protocol.getType());
	}
	
	/**
	 * 角色更换name
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.PLAYER_CHANGE_NAME_C_VALUE)
	private boolean onPlayerChangeNameRequest(HawkProtocol protocol) {
		IDIPBanInfo banInfo = RedisProxy.getInstance().getIDIPBanInfo(player.getId(), IDIPBanType.BAN_PLAYER_NAME);
		if (banInfo != null && banInfo.getEndTime() >  HawkTime.getMillisecond()) {
			player.sendIdipNotice(NoticeType.SEND_MSG, NoticeMode.NOTICE_MSG, 0, banInfo.getBanMsg());
			return false;
		}
		
		if (!GameUtil.checkBanMsg(player)) {
			return false;
		}
		
		if (GlobalData.getInstance().isGlobalBan(GlobalControlType.CHANGE_NAME)) {
			String reason = GlobalData.getInstance().getGlobalBanReason(GlobalControlType.CHANGE_NAME);
			if (HawkOSOperator.isEmptyString(reason)) {
				sendError(protocol.getType(), SysError.GLOBAL_BAN_CHANGE_NAME);
			} else {
				player.sendIdipNotice(NoticeType.BAN_MSG, NoticeMode.NOTICE_MSG, 0, reason);
			}
			return false;
		}

		int checkResult = GameUtil.changeContentCDCheck(player.getId(), ChangeContentType.CHANGE_ROLE_NAME);
		if (checkResult < 0) {
			sendError(protocol.getType(), IDIPErrorCode.CHANGE_ROLE_NAME_CD_ING);
			return false;
		}
		
		long endTime = RedisProxy.getInstance().getPlayerBanEndTime(player.getId(), BanPlayerOperType.BAN_CHANGE_NAME);
		final long startTime = HawkTime.getMillisecond();
		if (endTime > startTime) {
			sendError(protocol.getType(), IDIPErrorCode.CHANGE_ROLE_NAME_CD_ING);
			return false;
		}
		
		PlayerChangeNameReq req = protocol.parseProtocol(PlayerChangeNameReq.getDefaultInstance());
		String name = req.getName();

		boolean useGold = req.getUseGold();
//		Action action = Action.PLAYER_CHANGE_NAME;
//		Const.ItemId itemId = Const.ItemId.ITEM_CHANGE_NAME;
//		int count = 1;

		int errCode = GameUtil.tryOccupyPlayerName(player.getId(), player.getPuid(), name);
		if (errCode != Status.SysError.SUCCESS_OK_VALUE) {
			sendError(protocol.getType(), errCode);
			logger.error("player change name failed, playerId: {}, tarName: {}, errCode: {}", player.getId(), name, errCode);
			return false;
		}
		
		JSONObject gameDataJson = new JSONObject();
		gameDataJson.put("alliance_id", player.hasGuild() ? player.getGuildId() : "");
		gameDataJson.put("param_id", "");
		
		JSONObject json = new JSONObject();
		json.put("useGold", useGold);
		json.put("checkResult", checkResult);
		GameTssService.getInstance().wordUicChatFilter(player, name, 
				MsgCategory.PLAYER_NAME.getNumber(), GameMsgCategory.PLAYER_CHANGE_NAME, 
				json.toJSONString(), gameDataJson, protocol.getType());
		return true;
	}
	
	/**
	 * 改名
	 * 
	 * @param name
	 * @param action
	 * @param protoType
	 */
	@SuppressWarnings("deprecation")
	public void changeName(String name, Action action, int protoType) {
		// 删除老名字信息
		GameUtil.removePlayerNameInfo(player.getEntity().getName());
		String oriName = player.getEntity().getName();

		// 设置当前名字
		player.getEntity().setName(name);
		RedisProxy.getInstance().updateChangeContentTime(player.getId(), ChangeContentType.CHANGE_ROLE_NAME, HawkApp.getInstance().getCurrentTime());
		// 改名日志
		BehaviorLogger.log4Service(player, Source.ATTR_CHANGE, action, Params.valueOf("formerName", oriName), Params.valueOf("curName", name));

		// 修改全局数据管理器的名字信息
		GlobalData.getInstance().updateAccountInfo(player.getPuid(), player.getServerId(), player.getId(), player.getEntity().getForbidenTime(), name);

		// 改玩家联盟信息
		GuildService.getInstance().dealMsg(MsgId.PLAYER_CHANGE_NAME, new PlayerChangeNameMsgInvoker(player));
		XHJZWarService.getInstance().dealMsg(MsgId.XHJZ_NAME_REFRESH, new XHJZWarChangeNameMsgInvoker(player.getId(), player.getName()));

		CrossActivityService.getInstance().updatePlayerInfo(player, player.getGuildId());
		// 回复前端请求
		if (protoType > 0) {
			player.responseSuccess(protoType);
		}

		// 更新城点数据
		WorldPlayerService.getInstance().updatePlayerPointInfo(player.getId(), player.getName(), player.getCityLevel(), player.getIcon(), player.getData().getPersonalProtectVals());

		// 更新搜索服务信息
		SearchService.getInstance().removePlayerInfo(oriName);
		SearchService.getInstance().addPlayerInfo(name, player.getId(), true);

		SearchService.getInstance().removePlayerNameLow(oriName, player.getId());
		SearchService.getInstance().addPlayerNameLow(name, player.getId());

		CollegeMemberEntity member = getPlayerData().getCollegeMemberEntity();
		if (member != null && member.getAuth() == CollegeAuth.COACH_VALUE) {
			SearchService.getInstance().removeCoachName(oriName);
			SearchService.getInstance().addCoachName(name, player.getId());
		}
	}
	
	/**
	 * 检查昵称是否可用
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.PLAYER_CHECK_NAME_C_VALUE)
	private boolean onPlayerCheckNameRequest(HawkProtocol protocol) {
//		// 禁止输入文本，不能直接调这个接口，否则前端有些场景会出现弹框提示后转菊花的情况
//		if (!GameUtil.checkBanMsg(player)) {
//			return;
//		}
		
		// 禁言玩家推送禁言提示
		if (player.getEntity().getSilentTime() > HawkTime.getMillisecond()) {
			IDIPBanInfo banInfo = RedisProxy.getInstance().getIDIPBanInfo(player.getId(), IDIPBanType.BAN_SEND_MSG);
			if (banInfo != null) {
				PlayerCheckNameResp.Builder resp = PlayerCheckNameResp.newBuilder();
				resp.setResult(false);
				resp.setErrorCode(Status.NameError.CONTAINS_FILTER_WORD_VALUE);
				resp.setMsg(banInfo.getBanMsg());
				player.sendProtocol(HawkProtocol.valueOf(HP.code.PLAYER_CHECK_NAME_S_VALUE, resp));
				return false;
			}
		}
			
		JSONObject gameDataJson = new JSONObject();
		gameDataJson.put("alliance_id", player.hasGuild() ? player.getGuildId() : "");
		gameDataJson.put("param_id", "");
		
		PlayerCheckNameReq req = protocol.parseProtocol(PlayerCheckNameReq.getDefaultInstance());
		String name = req.getName();
		GameTssService.getInstance().wordUicChatFilter(player, name, 
				MsgCategory.PLAYER_NAME.getNumber(), GameMsgCategory.PLAYER_CHECK_NAME, 
				"", gameDataJson, protocol.getType());
		return true;
	}

	/**
	 * 领主详情
	 * 
	 * @param protocol
	 * @return 
	 */
	@ProtocolHandler(code = HP.code.PLAYER_DETAIL_C_VALUE)
	private boolean onPlayerDetailRequest(HawkProtocol protocol) {
		// 组装用户详情数据
		PlayerDetailRes.Builder builder = PlayerDetailRes.newBuilder();
		StatisticsEntity entity = player.getData().getStatisticsEntity();
		PowerElectric powerElectic = player.getData().getPowerElectric();
		// 战力
		player.refreshPowerElectric(PowerChangeReason.OTHER);
		builder.setBattlePoint(powerElectic.getBattlePoint());
		builder.setPlayerBattlePoint(powerElectic.getPlayerBattlePoint());
		builder.setArmyBattlePoint(powerElectic.getArmyBattlePoint());
		builder.setBuildBattlePoint(powerElectic.getBuildBattlePoint());
		builder.setTechBattlePoint(powerElectic.getTechBattlePoint());
		builder.setTrapBattlePoint(powerElectic.getTrapBattlePoint());
		builder.setHeroBattlePoint(powerElectic.getHeroBattlePoint());
		builder.setMechaBattlePoint(powerElectic.getSuperSoldierBattlePoint());
		builder.setArmourBattlePoint(powerElectic.getArmourBattlePoint());
		builder.setEquipResearchPoint(powerElectic.getEquipResearchPoint());
		builder.setPlantTechBattlePoint(powerElectic.getPlantTechBattlePoint());
		builder.setPlantSoldierBattlePoint(powerElectic.getPlantSchoolBattlePoint());
		builder.setPlantScienceBattlePoint(powerElectic.getPlantScienceBattlePoint());
		builder.setStarExporeBattlePoint(powerElectic.getStarExploreBattlePoint());
		builder.setManhattanBaseBattlePoint(powerElectic.getManhattanBaseBattlePoint());
		builder.setManhattanSWBattlePoint(powerElectic.getManhattanSWBattlePoint());
		builder.setMechacoreTechPower(powerElectic.getMechacoreTechPower());
		builder.setMechacoreModulePower(powerElectic.getMechacoreModulePower());
		builder.setHomeLandPower(powerElectic.getHomeLandPower());

		// 战斗状态
		builder.setStatistic(BuilderUtil.genStatisticBuilder(entity));
		
		// 军事
		builder.setMaxMarchNum(player.getMaxMarchNum());
		builder.setMaxMarchSoldierNum(player.getMaxMarchSoldierNum(new EffectParams()));
		builder.setMaxTrainNum(player.getMaxTrainNum());
		
		// 城市发展
		builder.setMaxCapNum(player.getMaxCapNum());
		
		// 城防
		builder.setCityDefVal(player.getData().getRealMaxCityDef());
		builder.setMaxTrapNum(player.getData().getTrapCapacity());
		
		builder.setArmourInfo(BuilderUtil.genArmourBriefInfo(player.getData()));
		
		player.sendProtocol(HawkProtocol.valueOf(HP.code.PLAYER_DETAIL_S_VALUE, builder));
		return true;
	}

	/**
	 * 其它领主详情
	 * 
	 * @param protocol
	 * @return 
	 */
	@ProtocolHandler(code = HP.code.PLAYER_DETAIL_OTHER_C_VALUE)
	private boolean onPlayerDetailOtherRequest(HawkProtocol protocol) {
		PlayerDetailReq req = protocol.parseProtocol(PlayerDetailReq.getDefaultInstance());
		String playerId = req.getPlayerId();
		if (GlobalData.getInstance().isResetAccount(playerId)) {
			HawkLog.errPrintln("player is removed player, myPlayerId: {}, target playerId: {}", player.getId(), playerId);
			sendError(protocol.getType(), Status.SysError.ACCOUNT_NOT_EXIST_VALUE);
			return false;
		}
		
		// 为了防止平台好友信息过期缓存失效需要重新拉取的影响，这里改成异步模式
		HawkThreadPool threadPool = HawkTaskManager.getInstance().getThreadPool("task");
		if (threadPool != null) {
			HawkTask task = new HawkTask() {
				@Override
				public Object run() {
					fetchOtherPlayerDetail(protocol, playerId);
					return null;
				}
			};
			task.setPriority(1);
			task.setTypeName("detailOtherPlayer");
			int threadIndex = Math.abs(player.getXid().hashCode() % threadPool.getThreadNum());
			threadPool.addTask(task, threadIndex, false);
			return true;
		}
		
		return fetchOtherPlayerDetail(protocol, playerId);
	}

	/**
	 * 获取其他玩家的领主详情信息
	 * 
	 * @param protocol
	 * @param playerId
	 * @return
	 */
	private boolean fetchOtherPlayerDetail(HawkProtocol protocol, String playerId) {
		
		boolean isRobot = WorldRobotService.getInstance().isRobotId(playerId);
		if (!isRobot) {
			//如果是在跨服状态,协议会有限被跨服处理.
			if (player.isCsPlayer() && !GlobalData.getInstance().isExistPlayerId(playerId)) {
				String fromServerId = CrossService.getInstance().getImmigrationPlayerServerId(player.getId()); 
				return CrossProxy.getInstance().sendNotify(protocol, fromServerId, player.getId());
			}
		}
		
		// 快照数据
		Player snapshot = GlobalData.getInstance().makesurePlayer(playerId);
		if (snapshot == null) {
			logger.error("fetch player detail info failed, playerId: {}, tarPlayerId: {}", player.getId(), playerId);
			sendError(protocol.getType(), Status.SysError.ACCOUNT_NOT_EXIST_VALUE);
			return false;
		}
		
		OtherPlayerDetailResp.Builder builder = OtherPlayerDetailResp.newBuilder();
		builder.setSnapshot(BuilderUtil.buildSnapshotData(snapshot));
		builder.setState(0);
		builder.setTodayDueled(RedisProxy.getInstance().todayDueled(playerId));
		builder.setArmourInfo(BuilderUtil.genArmourBriefInfo(snapshot.getData()));
		builder.setArmourTechInfo(BuilderUtil.genArmourEquipBriefInfo(snapshot.getData()));
		player.sendProtocol(HawkProtocol.valueOf(HP.code.PLAYER_DETAIL_OTHER_S_VALUE, builder));
		return true;
	}
	
	/**
	 * 屏蔽聊完玩家 
	 * @return
	 */
	@ProtocolHandler(code = HP.code.SHIELD_PLAYER_C_VALUE)
	private boolean onAddShieldPlayer(HawkProtocol protocol) {
		ShieldPlayerReq req = protocol.parseProtocol(ShieldPlayerReq.getDefaultInstance());
		String shieldPlayerId = req.getPlayerId();
		
		if (HawkOSOperator.isEmptyString(shieldPlayerId) || shieldPlayerId.equals(player.getId())) {
			logger.error("player shield failed, params invalid, playerId: {}, shieldPlayerId: {}", player.getId(), shieldPlayerId);
			sendError(protocol.getType(), Status.SysError.PARAMS_INVALID);
			return false;
		}

		Set<String> shieldPlayerIds = player.getShieldPlayers();

		if (shieldPlayerIds.contains(shieldPlayerId)) {
			logger.error("player shield failed, shieldPlayer has been shield, playerId: {}, shieldPlayerId: {}", player.getId(), shieldPlayerId);
			sendError(protocol.getType(), Status.Error.PLAYER_HAS_BEEN_SHIELD_VALUE);
			return false;
		}
		
		// 屏蔽上限是100个
		if (shieldPlayerIds.size() > ConstProperty.getInstance().getBlockListLimit()) {
			logger.error("player shield failed, shield num up limit, playerId: {}, size: {}", player.getId(), shieldPlayerIds.size());
			sendError(protocol.getType(), Status.Error.SHIELD_NUM_UP_LIMIT_VALUE);
			return false;
		}

		ShieldPlayerInfo.Builder builder = getShieldPlayerInfo(shieldPlayerId);
		if (builder == null) {
			logger.error("player shield failed, shield playerInfo fetch failed, playerId: {}, shieldPlayerId: {}", player.getId(), shieldPlayerId);
			return false;
		}

		BehaviorLogger.log4Service(player, Source.USER_OPERATION, Action.ADD_SHIELD_PLAYER, 
				Params.valueOf("shieldPlayerId", shieldPlayerId));

		LocalRedis.getInstance().addShieldPlayer(player.getId(), shieldPlayerId);
		player.addShieldPlayer(shieldPlayerId);
		
		ShieldPlayerResp.Builder resp = ShieldPlayerResp.newBuilder();
		resp.setResult(true);
		resp.setShieldPlayer(builder);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.SHIELD_PLAYER_S_VALUE, resp));

		return true;
	}
	
	private ShieldPlayerInfo.Builder getShieldPlayerInfo(String playerId) {
		ShieldPlayerInfo.Builder builder = ShieldPlayerInfo.newBuilder();
		builder.setPlayerId(playerId);
		
		//优先从内存取玩家信息
		Player shieldPlayer = GlobalData.getInstance().getActivePlayer(playerId);
		String guildName = null;
		if (shieldPlayer != null) {
			guildName = shieldPlayer.getGuildName();
			builder.setName(shieldPlayer.getName());
			builder.setIcon(shieldPlayer.getIcon());
			builder.setBattlePoint(shieldPlayer.getPower());
			builder.setGuildName(guildName != null ? guildName : "");
			return builder;
		} 
		
		Player playerInfo = GlobalData.getInstance().makesurePlayer(playerId);
		if (playerInfo != null) {
			guildName = playerInfo.getGuildName();
			builder.setPlayerId(playerInfo.getId());
			builder.setName(playerInfo.getName());
			builder.setIcon(playerInfo.getIcon());
			builder.setBattlePoint(playerInfo.getPower());
			builder.setGuildName(guildName != null ? guildName : "");
			return builder;
		}
		
		return null;
	}

	/**
	 * 解除屏蔽
	 * @return
	 */
	@ProtocolHandler(code = HP.code.REMOVE_SHIELD_C_VALUE)
	private boolean onRemoveShieldPlayer(HawkProtocol protocol) {
		RemoveShieldPlayerReq req = protocol.parseProtocol(RemoveShieldPlayerReq.getDefaultInstance());
		String shieldPlayerId = req.getPlayerId();

		if (HawkOSOperator.isEmptyString(shieldPlayerId)) {
			logger.error("remove shield player failed, param is empty, playerId: {}, shieldPlayerId: {}", player.getId(), shieldPlayerId);
			sendError(protocol.getType(), Status.SysError.PARAMS_INVALID);
			return false;
		}

		BehaviorLogger.log4Service(player, Source.USER_OPERATION, Action.REMOVE_SHIELD_PLAYER, 
				Params.valueOf("shieldPlayerId", shieldPlayerId));

		long count = LocalRedis.getInstance().removeShieldPlayer(player.getId(), shieldPlayerId);
		if (count <= 0) {
			logger.error("remove shield player failed, playerId: {}, shieldPlayerId: {}", player.getId(), shieldPlayerId);
			sendError(protocol.getType(), Status.Error.PLAYER_HAS_NOT_BEEN_SHIELD_VALUE);
			return false;
		}
		
		player.removeShieldPlayer(shieldPlayerId);
		player.responseSuccess(protocol.getType());
		return true;
	}

	/**
	 * 其他玩家作用号数据请求
	 * @return
	 */
	@ProtocolHandler(code = HP.code.OTHER_PLAYER_EFFECT_C_VALUE)
	private boolean onGetOtherPlayerEffect(HawkProtocol protocol) {
		OtherPlayerEffectReq req  = protocol.parseProtocol(OtherPlayerEffectReq.getDefaultInstance());
		String playerId = req.getPlayerId();
		List<Integer> effectIds = req.getEffectIdList();
		if (HawkOSOperator.isEmptyString(playerId) || effectIds == null || effectIds.isEmpty()) {
			sendError(protocol.getType(), Status.SysError.PARAMS_INVALID);
			return false;
		}
		
		Player otherPlayer = GlobalData.getInstance().makesurePlayer(playerId);
		if (otherPlayer == null) {
//			sendError(protocol.getType(), Status.SysError.ACCOUNT_NOT_EXIST);
			return false;
		}
		
		HPPlayerEffectSync.Builder builder = HPPlayerEffectSync.newBuilder();
		for (int effectId : effectIds) {
			int effectVal = otherPlayer.getEffect().getEffVal(EffType.valueOf(effectId));
			
			EffectPB.Builder effPB = EffectPB.newBuilder();
			effPB.setEffId(effectId);
			effPB.setEffVal(effectVal);
			builder.addEffList(effPB);
		}
		
		player.sendProtocol(HawkProtocol.valueOf(HP.code.OTHER_PLAYER_EFFECT_S, builder));
		return true;
	}
	
	/**
	 * 玩家举报
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.REPORTING_REQ_C_VALUE)
	private boolean onReporting(HawkProtocol protocol) {
		ReportingInfoPB req = protocol.parseProtocol(ReportingInfoPB.getDefaultInstance());
		String targetPlayerId = req.getReportRoleId();
		Player targetPlayer = GlobalData.getInstance().makesurePlayer(targetPlayerId);
		if (targetPlayer == null) {
			logger.error("reporting failed, targetPlayer not exist, playerId: {}, targetId: {}", player.getId(), targetPlayerId);
			return false;
		}
		
		if (!GameUtil.checkBanMsg(player)) {
			return false;
		}
		
		String reportInfo = LocalRedis.getInstance().getReportInfo(player.getId(), targetPlayerId);
		if (!HawkOSOperator.isEmptyString(reportInfo)) {
			 long endTime = Long.valueOf(reportInfo) + ConstProperty.getInstance().getReportingCD() * 1000;
			 if (HawkApp.getInstance().getCurrentTime() < endTime) {
				 logger.error("reporting failed, last reporting cd not end, playerId: {}, targetId: {}", player.getId(), targetPlayerId);
				 sendReportingCD(targetPlayerId, Long.valueOf(reportInfo), false);
				 return false;
			 }
		}
		
		List<Integer> reportTypeList = req.getReportTypeList();
		if (reportTypeList.size() == 0) {
			logger.error("reporting failed, reportType cannot be empty, playerId: {}, targetId: {}", player.getId(), targetPlayerId);
			return false;
		}
		
		LocalRedis.getInstance().addReportInfo(player.getId(), targetPlayerId, String.valueOf(HawkApp.getInstance().getCurrentTime()));
		sendReportingCD(targetPlayerId, HawkApp.getInstance().getCurrentTime(), true);
		logReporting(req, targetPlayer, reportTypeList);
		
		return true;
	}
	
	/**
	 * 发送举报间隔倒计时信息
	 * 
	 * @param targetId
	 * @param reportingTime
	 * @param normal
	 */
	private void sendReportingCD(String targetId, long reportingTime, boolean normal) {
		ReportingCDPB.Builder builder = ReportingCDPB.newBuilder();
		builder.setReportRoleId(targetId);
		builder.setNextReportingTime(reportingTime + ConstProperty.getInstance().getReportingCD() * 1000);
		builder.setNormal(normal);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.REPORTTING_S_VALUE, builder));
	}
	
	/**
	 * 记日志
	 * 
	 * @param req
	 * @param targetPlayer
	 * @param reportTypeList
	 */
	private void logReporting(ReportingInfoPB req, Player targetPlayer, List<Integer> reportTypeList) {
		int reportScene = req.getReportScene();
		String reportDesc = req.hasReportDesc() ? req.getReportDesc() : "";
		String reportContent = req.hasReportChatContent() ? req.getReportChatContent() : "";
		if (HawkOSOperator.isEmptyString(reportDesc) && HawkOSOperator.isEmptyString(reportContent)) {
			for (Integer reportType : reportTypeList) {
				LogUtil.logReporting(player, targetPlayer, reportScene, reportType, reportDesc, reportContent, req.getReportPicUrlListList());
			}
			
			return;
		}
		
		if (!GsConfig.getInstance().isTssSdkEnable() || !GsConfig.getInstance().isTssSdkUicEnable()) {
			reportDesc = GsApp.getInstance().getWordFilter().filterWord(reportDesc);
			reportContent = GsApp.getInstance().getWordFilter().filterWord(reportContent);
			for (Integer reportType : reportTypeList) {
				LogUtil.logReporting(player, targetPlayer, reportScene, reportType, reportDesc, reportContent, req.getReportPicUrlListList());
			}

			return;
		}
		
		ReportingFilterInfo reportingInfo = new ReportingFilterInfo(reportDesc, reportContent);
		ReportingFilterCallbackData dataObject = new ReportingFilterCallbackData(targetPlayer.getId(), reportScene, reportTypeList, req.getReportPicUrlListList());
		GameTssService.getInstance().wordUicChatFilter(player, JSONObject.toJSONString(reportingInfo), 
				GameMsgCategory.REPORTING, GameMsgCategory.REPORTING, 
				JsonUtils.Object2Json(dataObject), null, 0);
	}
	
	@MessageHandler
	private boolean onReportingInfoFilterFinish(ReportingInfoFilterFinishMsg msg) {
		ReportingFilterInfo filterInfo = JSONObject.parseObject(msg.getMsgContent(), ReportingFilterInfo.class);
		ReportingFilterCallbackData callBackData = JSONObject.parseObject(msg.getCallbackData(), ReportingFilterCallbackData.class);
		
		int reportScene = callBackData.getReportScene();
		List<Integer> reportTypeList = callBackData.getReportTypeList();
		List<String> picUrlList = callBackData.getPicUrlList();
		String targetPlayerId = callBackData.getTargetPlayerId();
		
		String reportDesc = filterInfo.getReportDesc();
		String reportContent = filterInfo.getReportContent();
		Player targetPlayer = GlobalData.getInstance().makesurePlayer(targetPlayerId);
		for (Integer reportType : reportTypeList) {
			LogUtil.logReporting(player, targetPlayer, reportScene, reportType, reportDesc, reportContent, picUrlList);
		}
		
		if (!HawkOSOperator.isEmptyString(reportDesc)) {
			LogUtil.logSecTalkFlow(player, null, LogMsgType.REPORT_CONTENT, "", reportDesc);
		}
		
		if (!HawkOSOperator.isEmptyString(reportContent)) {
			LogUtil.logSecTalkFlow(player, null, LogMsgType.REPORT_CONTENT, "", reportContent);
		}
		
		return true;
	}
	
	/**
	 * 设置或修改二级密码
	 * 
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.SET_PASSWD_C_VALUE)
	private void onChangePasswd(HawkProtocol protocol) {
		SetPasswdReq req = protocol.parseProtocol(SetPasswdReq.getDefaultInstance());
		String newPasswd = req.getPasswd();
		String oldPasswd = "";
		if (req.hasOldPasswd()) {
			oldPasswd = req.getOldPasswd();
		}
		
		if (HawkOSOperator.isEmptyString(newPasswd)) {
			// 密码设置不能为空
			player.sendError(protocol.getType(), Status.Error.SEC_PASSWD_CANNOT_EMPTY, 0);
			HawkLog.errPrintln("onChangePasswd failed, errCode: {}, playerId: {}", Status.Error.SEC_PASSWD_CANNOT_EMPTY_VALUE, player.getId());
			return;
		}
		
		String serverPasswd = player.getPlayerSecPasswd();
		if (!HawkOSOperator.isEmptyString(serverPasswd) && HawkOSOperator.isEmptyString(oldPasswd)) {
			serverPasswd = RedisProxy.getInstance().readSecPasswd(player.getId());
			player.setPlayerSecPasswd(serverPasswd);
		}
		
		if (!HawkOSOperator.isEmptyString(serverPasswd) && !serverPasswd.equals(oldPasswd)) {
			// 密码校验不通过
			player.sendError(protocol.getType(), Status.Error.OLD_PASSWD_INPUT_INVALID, 0);
			HawkLog.errPrintln("onChangePasswd failed, errCode: {}, playerId: {}, serverPasswd: {}, clientPasswd: {}", 
					Status.Error.OLD_PASSWD_INPUT_INVALID_VALUE, player.getId(), serverPasswd, oldPasswd);
			return;
		}
		
		RedisProxy.getInstance().setSecPasswd(player.getId(), newPasswd);
		player.setPlayerSecPasswd(newPasswd);
		
		PasswdInfoPB.Builder builder = PasswdInfoPB.newBuilder();
		builder.setPasswd(true);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.PASSWD_INFO_SYNC, builder));
		
		player.responseSuccess(protocol.getType());
		
		HawkLog.logPrintln("setPasswd success, playerId: {}, passwd: {}", player.getId(), newPasswd);
	}
	
	/**
	 * 二级密码验证
	 * 
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.PASSWD_CHECK_C_VALUE)
	private void onCheckPasswd(HawkProtocol protocol) {
		PasswdCheckReq req = protocol.parseProtocol(PasswdCheckReq.getDefaultInstance());
		String passwd = req.getPasswd();
		String serverPasswd = player.getPlayerSecPasswd();
		if (!HawkOSOperator.isEmptyString(serverPasswd) && !serverPasswd.equals(passwd)) {
			// 密码校验不通过
			player.sendError(protocol.getType(), Status.Error.SEC_PASSWD_CHECK_FAILED, 0);
			HawkLog.errPrintln("onCheckPasswd failed, errCode: {}, playerId: {}, serverPasswd: {}, clientPasswd: {}", 
					Status.Error.SEC_PASSWD_CHECK_FAILED_VALUE, player.getId(), serverPasswd, passwd);
			return;
		}
		
		player.responseSuccess(protocol.getType());
	}
	
	/**
	 * 关闭二级密码
	 * 
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.CLOSE_PASSWD_C_VALUE)
	private void onClosePasswd(HawkProtocol protocol) {
		String serverPasswd = player.getPlayerSecPasswd();
		if (HawkOSOperator.isEmptyString(serverPasswd)) {
			// 还没有设置二级密码
			player.sendError(protocol.getType(), Status.Error.SEC_PASSWD_NOT_EXIST, 0);
			HawkLog.errPrintln("onUnsetPasswd failed, errCode: {}, playerId: {}", Status.Error.SEC_PASSWD_NOT_EXIST_VALUE, player.getId());
			return;
		}
		
		ClosePasswdReq req = protocol.parseProtocol(ClosePasswdReq.getDefaultInstance());
		boolean force = req.getForceClose(); 
		int time = force ? ConstProperty.getInstance().getSecPasswordExamineTime() : 0;		
		RedisProxy.getInstance().closeSecPasswd(player.getId(), time);
		PasswdInfoPB.Builder builder = PasswdInfoPB.newBuilder();
		builder.setPasswd(force && time > 0);
		if (time > 0) {
			long expiryTime = HawkTime.getMillisecond() + time * 1000L;
			builder.setClosedTime(expiryTime);
			player.setSecPasswdExpiryTime(expiryTime);
		} else {
			player.setPlayerSecPasswd(null);
		}
		
		player.sendProtocol(HawkProtocol.valueOf(HP.code.PASSWD_INFO_SYNC, builder));
		
		HawkLog.logPrintln("close passwd success, playerId: {}, force: {}, time: {}", player.getId(), force, time);
	}
	
	/**
	 * 撤销关闭二级密码
	 * 
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.UNDO_CLOSE_PASSWD_C_VALUE)
	private void onUndoClosePasswd(HawkProtocol protocol) {
		String serverPasswd = player.getPlayerSecPasswd();
		if (HawkOSOperator.isEmptyString(serverPasswd)) {
			// 二级密码已时效
			player.sendError(protocol.getType(), Status.Error.SEC_PASSWD_INALID, 0);
			HawkLog.errPrintln("onUndoUnsetPasswd failed, errCode: {}, playerId: {}", Status.Error.SEC_PASSWD_INALID_VALUE, player.getId());
			return;
		}
		
		RedisProxy.getInstance().undoClosePasswd(player.getId());
		player.setSecPasswdExpiryTime(0);
		PasswdInfoPB.Builder builder = PasswdInfoPB.newBuilder();
		builder.setPasswd(true);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.PASSWD_INFO_SYNC, builder));
		
		HawkLog.logPrintln("undoClosePasswd success, playerId: {}", player.getId());
	}
	
	/**
	 * 请求二级密码信息
	 * 
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.SEC_PASSWD_REQ_VALUE)
	private void onSecPasswdReq(HawkProtocol protocol) {
		String serverPasswd = player.getPlayerSecPasswd();
		PasswdInfoPB.Builder builder = PasswdInfoPB.newBuilder();
		
		if (HawkOSOperator.isEmptyString(serverPasswd)) {
			builder.setPasswd(false);
		} else {
			builder.setPasswd(true);
			if (player.getSecPasswdExpiryTime() > 0) {
				builder.setClosedTime(player.getSecPasswdExpiryTime());
			}
		}
		
		player.sendProtocol(HawkProtocol.valueOf(HP.code.PASSWD_INFO_SYNC, builder));
	}
	
	/**
	 * 个保法相关开关设置
	 * 
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.PERSONAL_PROTECT_SET_REQ_VALUE)
	private void onPersonalProtectSetReq(HawkProtocol protocol) {
		PersonalProtectSetReq req = protocol.parseProtocol(PersonalProtectSetReq.getDefaultInstance());
		int switchNum = req.getSwitchIndex();
		int clientVal = req.getSwitchVal();
		
		List<Integer> switchVals = player.getData().getPersonalProtectListVals();
		
		int switchIntCount = switchNum / 31; // 只有1到31位表示开关， 第32位不用来表示开关
		// 保证switchIntCount是从0开始
		if (switchNum % 31 == 0) {
			switchIntCount = switchIntCount - 1;
		}
		
		switchNum = switchNum - switchIntCount * 31;  // 31个开关按位凑成一个int型整数
		
		int switchVal = switchVals.get(switchIntCount);
		int locationPositive = 1 << (switchNum - 1);
		if (clientVal > 0) {
			switchVal = switchVal | locationPositive;
		} else {
			switchVal = switchVal & ~locationPositive;
		}
		
		switchVals.set(switchIntCount, switchVal);
		
		player.getData().updatePersonalProtectVals(switchVals);
		player.getPush().syncPersonalProtectVals();
		
		WorldPlayerService.getInstance().updatePlayerPointInfo(player.getId(), player.getName(), player.getCityLevel(), player.getIcon(), player.getData().getPersonalProtectVals());
	}
	
}
