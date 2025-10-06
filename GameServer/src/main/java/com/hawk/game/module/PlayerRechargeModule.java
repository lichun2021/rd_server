package com.hawk.game.module;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;

import org.hawk.annotation.MessageHandler;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.app.HawkApp;
import org.hawk.collection.ConcurrentHashSet;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.helper.HawkAssert;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.task.HawkDelayTask;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;
import org.hawk.thread.HawkThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.game.GsConfig;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.config.ControlProperty;
import com.hawk.game.config.GameConstCfg;
import com.hawk.game.config.PayCfg;
import com.hawk.game.config.PayGiftCfg;
import com.hawk.game.config.ServerAwardCfg;
import com.hawk.game.controler.SystemControler;
import com.hawk.game.entity.RechargeDailyEntity;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.manager.AssembleDataManager;
import com.hawk.game.msg.PlayerRechargeGrantItemMsg;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.protocol.Activity.ActivityType;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Recharge.ExchangeDiamondsToGold;
import com.hawk.game.protocol.Recharge.HPTakeCouponParam;
import com.hawk.game.protocol.Recharge.HPTakeCouponReq;
import com.hawk.game.protocol.Recharge.RechargeBuyItemRequest;
import com.hawk.game.protocol.Recharge.RechargeBuyItemResp;
import com.hawk.game.protocol.Recharge.RechargeItemInfoClient;
import com.hawk.game.protocol.Recharge.RechargeReq;
import com.hawk.game.protocol.Recharge.RechargeResp;
import com.hawk.game.protocol.Recharge.RechargeSuccRequest;
import com.hawk.game.protocol.Recharge.RechargeSuccRespCode;
import com.hawk.game.protocol.Recharge.RechargeSuccResponse;
import com.hawk.game.protocol.Status;
import com.hawk.game.recharge.CouponManager;
import com.hawk.game.recharge.RechargeManager;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.GsConst.ControlerModule;
import com.hawk.game.util.GsConst.DiamondPresentReason;
import com.hawk.game.util.GsConst.IDIPDailyStatisType;
import com.hawk.game.recharge.RechargeType;
import com.hawk.game.util.LogUtil;
import com.hawk.log.Action;
import com.hawk.log.LogConst.GiftType;
import com.hawk.sdk.SDKConst;
import com.hawk.sdk.SDKConst.UserType;
import com.hawk.sdk.SDKManager;
import com.hawk.sdk.msdk.entity.PayItemInfo;
import com.hawk.zoninesdk.ZonineSDK;
import com.hawk.zoninesdk.datamanager.OpDataType;

/**
 * 充值模块
 *
 * @author hawk
 */
public class PlayerRechargeModule extends PlayerModule {
	
	static final Logger logger = LoggerFactory.getLogger("Recharge");
	
	/**
	 * 正在处理的订单
	 */
	private static Set<String> processOrderSet = new ConcurrentHashSet<>();

	/**
	 * 构造
	 *
	 * @param player
	 */
	public PlayerRechargeModule(Player player) {
		super(player);
	}

	/**
	 * 玩家上线处理
	 *
	 * @return
	 */
	@Override
	protected boolean onPlayerLogin() {
		deleteExpiredRechargeDailyEntity();
		player.getPush().syncHasFirstRecharge();
		player.getPush().syncMonthCardRechargeInfo();
		player.getPush().syncGiftList();
		RechargeManager.getInstance().syncRechargeInfo(player);
		// 查询玩家的微信券信息，并同步
		CouponManager.queryAllCoupon(player);
		return true;
	}
	
	/**
	 * 删除"过期"的充值记录
	 */
	private void deleteExpiredRechargeDailyEntity() {
		try {
			int hour = HawkTime.getHour();
			int[] freeTime = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 13, 14, 15, 16, 17, 18, 22 };
			boolean free = IntStream.of(freeTime).filter(t -> t == hour).findAny().isPresent();
			if (!free) {
				return;
			}
			
			long now = HawkApp.getInstance().getCurrentTime();
			List<RechargeDailyEntity> removeEntities = new ArrayList<>();
			for (RechargeDailyEntity entity : player.getData().getPlayerRechargeDailyEntities()) {
				if (now - entity.getTime() > HawkTime.DAY_MILLI_SECONDS * 2) {
					removeEntities.add(entity);
					entity.delete(true);
				}
			}
			
			if (!removeEntities.isEmpty()) {
				player.getData().getPlayerRechargeDailyEntities().removeAll(removeEntities);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 商品列表信息请求
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.FETCH_RECHARGE_INFO_VALUE)
	private boolean onGoodsListRequest(HawkProtocol protocol) {
		RechargeManager.getInstance().syncRechargeInfo(player);
		return true;
	}

	/**
	 * 获取礼包直购信息
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.FETCH_GIFT_INFO_VALUE)
	private void onFetchGiftList(HawkProtocol protocol) {
		player.getPush().syncGiftList();
	}
	
	/**
	 * 客户端上报充值营销信息
	 * 
	 * @return
	 */
	@ProtocolHandler(code = HP.code.RECHARGE_ITEM_INFO_C_VALUE)
	private boolean cilentPushRechargeItemInfo(HawkProtocol protocol) {
		RechargeItemInfoClient itemInfo = protocol.parseProtocol(RechargeItemInfoClient.getDefaultInstance());
		logger.debug("recharge item info from client: {}", itemInfo);
		return true;
	}
	
	/**
	 * 货币充值通知请求（客户端向米大师请求前发送）
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.RECHARGE_C_VALUE)
	private boolean onRechargeRequest(HawkProtocol protocol) {
		// 支付模块关闭
		if (SystemControler.getInstance().isModuleClosed(ControlerModule.RECHARGE_PAY)) {
			sendError(protocol.getType(), Status.SysError.MODULE_CLOSED);
			return false;
		}
		
		RechargeReq req = protocol.parseProtocol(RechargeReq.getDefaultInstance());
		final String goodsId = req.getGoodsId();
		
		// 钻石礼包充值入口被关闭
		if (SystemControler.getInstance().isSystemItemsClosed(ControlerModule.RECHARGE_PAY, Integer.valueOf(goodsId))) {
			sendError(protocol.getType(), Status.SysError.RECHARGE_GIFT_OFF);
			logger.error("recharge goods closed, playerId: {}, goodsId: {}", player.getId(), goodsId);
			return false;
		}
		
		PayCfg payCfg = HawkConfigManager.getInstance().getConfigByKey(PayCfg.class, goodsId);
		if (payCfg == null) {
			sendError(protocol.getType(), Status.Error.PAY_GOODS_NOT_EXIST_VALUE);
			logger.error("recharge miss pay config, playerId: {}, goodsId: {}", player.getId(), goodsId);
			return false;
		}

		// 判断商品是否可出售
		if (payCfg.getPayOrNot() <= 0) {
			sendError(protocol.getType(), Status.Error.PAY_GOODS_CANNOT_SALE_VALUE);
			logger.error("recharge cannot pay for goods, playerId: {}, goodsId: {}", player.getId(), goodsId);
			return false;
		}
		
		// 充值未开启
		if (!GameUtil.isWin32Platform(player) && !SDKManager.getInstance().isPayOpen()) {
			sendError(protocol.getType(), Status.SysError.MODULE_CLOSED);
			logger.error("recharge closed by MSDK, playerId: {}", player.getId());
			return false;
		}
		
		String timestamp = HawkTime.formatTime(HawkApp.getInstance().getCurrentTime(), "yyyyMMddHHmmss");
		String orderId = String.format("%s_%s", timestamp, HawkOSOperator.randomUUID());
		
		// 生成票据
		boolean succ = RedisProxy.getInstance().addRechargeInfo(player.getId(), goodsId, orderId, GsConfig.getInstance().getOrderExpire());
		if (!succ) {
			logger.error("recharge order generate failed, playerId: {}, goodsId: {}", player.getId(), goodsId);
			sendError(protocol.getType(), Status.Error.RECHARGE_ORDER_GEN_FAILED);
			return false;
		}
		
		// 记录日志
		logger.info("recharge order generate success, playerId: {}, goodsId: {}, orderId: {}", player.getId(), goodsId, orderId);
		
		// 通知客户端对应订单存根
		RechargeResp.Builder builder = RechargeResp.newBuilder();
		builder.setRechargeId(orderId);
		builder.setGoodsId(goodsId);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.RECHARGE_S_VALUE, builder));
		
		return true;
	}
	
	/**
	 * 货币充值成功通知请求（客户端向米大师请求成功后发送）
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.RECHARGE_SUCC_C_VALUE)
	private boolean onRechargeSuccRequest(HawkProtocol protocol) {
		RechargeSuccRequest request = protocol.parseProtocol(RechargeSuccRequest.getDefaultInstance());
		String rechargeId = request.getRechargeId();
		
		// 订单正在处理中。。。。。
		if (processOrderSet.contains(rechargeId)) {
			rechargeFailedResponse(rechargeId, RechargeSuccRespCode.RECHARGE_PROCESSING_VALUE, false);
			return false;
		}
		
		String goodsId = request.getGoodsId();
		// 订单已过期，或者前端发来的是假订单
		String serverGoodsId = RedisProxy.getInstance().getGoodsIdByOrderId(player.getId(), rechargeId);
		if (HawkOSOperator.isEmptyString(serverGoodsId)) {
			rechargeFailedResponse(rechargeId, RechargeSuccRespCode.RECHARGE_INVALID_ORDER_VALUE, false);
			
			// 错误信息记录
			logger.error("recharge order expired, playerId: {}, rechargeId: {}, goodsId: {}", player.getId(), rechargeId, goodsId);
			
			return false;
		}
		
		// 前后端商品id不匹配
		if (!goodsId.equals(serverGoodsId)) {
			sendError(protocol.getType(), Status.Error.RECHARGE_GOODS_UNMATCHED);
			
			rechargeFailedResponse(rechargeId, RechargeSuccRespCode.RECHARGE_INVALID_ORDER_VALUE, true);
			
			// 错误信息记录
			logger.error("recharge goodsId match failed, playerId: {}, rechargeId: {}, goodsId: {}, serverGoodsId: {}", 
					player.getId(), rechargeId, goodsId, serverGoodsId);
			
			return false;
		}
		
		// 没有充值信息
		if (!request.hasRechargeInfo()) {
			rechargeFailedResponse(rechargeId, RechargeSuccRespCode.RECHARGE_INVALID_ORDER_VALUE, true);
			
			// 错误信息记录
			logger.error("recharge info miss, playerId: {}, rechargeId: {}", player.getId(), rechargeId);
			
			return false;
		}
		
		String rechargeInfo = request.getRechargeInfo();
		PayCfg payCfg = HawkConfigManager.getInstance().getConfigByKey(PayCfg.class, goodsId);

		// win32直接充值成功
		if (GameUtil.isWin32Platform(player)) {
			win32DirectRecharge(payCfg, rechargeId, rechargeInfo);
			player.responseSuccess(protocol.getType());
			return true;
		} 
		
		try {
			// 明显失败的订单信息 
			JSONObject json = JSONObject.parseObject(rechargeInfo);
			if (json != null && json.containsKey("resultCode")) {
				// 2-用户取消; 3-参数错误
				int resultCode = json.getInteger("resultCode");
				if (resultCode == 2 || resultCode == 3) {					
					rechargeFailedResponse(rechargeId, RechargeSuccRespCode.RECHARGE_INVALID_ORDER_VALUE, true);
					
					// 记录错误新
					logger.error("recharge notify discard result code, playerId: {},  rechargeInfo: {}", player.getId(), rechargeInfo);
					return false;
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		// 日志记录
		logger.info("recharge notify fetch balance, playerId: {},  rechargeInfo: {}", player.getId(), rechargeInfo);
		
		processOrderSet.add(rechargeId);
		
		// 拉取余额
		onPlayerFetchBalanceAction(payCfg, rechargeInfo, rechargeId);
		
		player.responseSuccess(protocol.getType());
		return true;
	}
	
	/**
	 * win32充值
	 * 
	 * @param payCfg
	 * @param rechargeId
	 * @param rechargeInfo
	 */
	private void win32DirectRecharge(PayCfg payCfg, String rechargeId, String rechargeInfo) {
		int playerSaveAmt = player.getPlayerBaseEntity().getSaveAmt();
		int diamonds = player.getDiamonds();
		player.getPlayerBaseEntity().setDiamonds(diamonds + payCfg.getGainDia());
		player.getPlayerBaseEntity().setSaveAmt(playerSaveAmt + payCfg.getGainDia());
		player.getPlayerBaseEntity()._setChargeAmt(player.getPlayerBaseEntity()._getChargeAmt() + payCfg.getGainDia());
		player.getPush().syncPlayerDiamonds();
		
		// 删除票据存根
		RedisProxy.getInstance().removeRechargeInfo(player.getId(), rechargeId);
		
		// 通知充值成功
		player.rechargeSuccess(playerSaveAmt, payCfg.getGainDia(), diamonds);
		rechargeSuccess(payCfg.getGainDia(), payCfg, rechargeInfo, rechargeId);
		// 通知客户端
		notifyClient(rechargeId, payCfg);
	}
	
	/**
	 * 充值失败返回
	 * 
	 * @param rechargeId
	 * @param builder
	 * @param removeOrder
	 */
	private void rechargeFailedResponse(String rechargeId, int resultCode, boolean removeOrder) {
		// 移除票据
		if (removeOrder) {
			RedisProxy.getInstance().removeRechargeInfo(player.getId(), rechargeId);
		}
		
		RechargeSuccResponse.Builder builder = RechargeSuccResponse.newBuilder();
		builder.setRechargeId(rechargeId);
		builder.setResult(resultCode);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.RECHARGE_SUCC_S_VALUE, builder));		
	}
	
	/**
	 * 礼包直购请求
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.BUY_GIFT_REQUEST_VALUE)
	private boolean onBuyItemRequest(HawkProtocol protocol) {
		// 支付模块关闭
		if (SystemControler.getInstance().isModuleClosed(ControlerModule.RECHARGE_SHOP)) {
			sendError(protocol.getType(), Status.SysError.MODULE_CLOSED);
			return false;
		}
		
		// 特定礼包被禁止购买
		RechargeBuyItemRequest req = protocol.parseProtocol(RechargeBuyItemRequest.getDefaultInstance());
		String giftId = req.getGiftId();  // "290001"
		if (SystemControler.getInstance().isSystemItemsClosed(ControlerModule.RECHARGE_SHOP, Integer.valueOf(giftId))) {
			logger.error("daily special gift closed, playerId: {}, giftId: {}", player.getId(), giftId);
			sendError(protocol.getType(), Status.SysError.DAILY_SPECIAL_GIFT_OFF);
			return false;
		}

		// 礼包不存在
		PayGiftCfg giftCfg = HawkConfigManager.getInstance().getConfigByKey(PayGiftCfg.class, giftId);
		if (giftCfg == null) {
			sendError(protocol.getType(), Status.Error.PAY_GIFT_NOT_EXIST);
			logger.error("MSDK buy item failed, gift config error, playerId: {}, giftId: {}",  player.getId(), giftId);
			return false;
		}
		
		// 领取月卡免费宝箱
		if (giftCfg.isFree()) {
			return takeMonthCardFreeBox(giftCfg, protocol.getType());
		}
		
		// 购买条件判断
		if (!RechargeManager.getInstance().giftBuyCheck(player, giftCfg, req, protocol.getType())) {
			return false;
		}
		
		return buyItem(giftCfg, protocol.getType());
	}
	
	/**
	 * 道具直购特殊处理（win32环境走模拟路径，方便测试）
	 * 
	 * @param payGiftCfg
	 * @param protocol
	 * @return
	 */
	private boolean buyItem(PayGiftCfg payGiftCfg, int protocol) {
		if (!GameUtil.isWin32Platform(player)) {
			return buyItemRequest(payGiftCfg, protocol);
		}

		String currency = GameUtil.getPriceType(player.getChannel()).name();
		String billno = HawkOSOperator.randomUUID();
		boolean succ = RechargeManager.getInstance().createRechargeRecord(player, billno, "token" + billno, payGiftCfg.getId(), 
				payGiftCfg.getPayRMB() / 10, payGiftCfg.getPayRMB(), currency, RechargeType.GIFT, HawkTime.getSeconds(), payGiftCfg.getGainDia());
		
		if (!succ) {
			sendError(protocol, Status.Error.PAY_GIFT_BUY_FAILED);
			return false;
		}
		
		// 发货通知
		HawkApp.getInstance().postMsg(player.getXid(), PlayerRechargeGrantItemMsg.valueOf(payGiftCfg, billno, payGiftCfg.getPayRMB()));
		
		RechargeBuyItemResp.Builder resp = RechargeBuyItemResp.newBuilder();
		resp.setResultInfo("");
		resp.setGoodsId(payGiftCfg.getId());
		sendProtocol(HawkProtocol.valueOf(HP.code.GIFT_SUCCESS_RESP, resp));
		
		return true;
	}
	
	
	/**
	 * 领取月卡免费宝箱
	 * 
	 * @param entity
	 * @param protoType
	 * @return
	 */
	private boolean takeMonthCardFreeBox(PayGiftCfg payGiftCfg, int protoType) {
		long now = HawkApp.getInstance().getCurrentTime();
		long lastTakenTime = LocalRedis.getInstance().getFreeBoxTakenTime(player.getId());
		boolean otherDay = !HawkTime.isSameDay(lastTakenTime, now) && now - lastTakenTime >= HawkTime.DAY_MILLI_SECONDS - 300000;
		// 判断当日是否已领取过宝箱
		if (!otherDay) {
			sendError(protoType, Status.Error.TAKE_MONTHCARD_BOX_FAILED);
			logger.error("take monthcard box failed, today has taken, playerId: {}, giftId: {}", player.getId(), payGiftCfg.getId());
			return false;
		}
		
		ServerAwardCfg cfg = AssembleDataManager.getInstance().getServerAwardByAwardId(payGiftCfg.getServerAwardId());
		if (cfg != null) {
			AwardItems awardItems = cfg.getAwardItems();
			awardItems.rewardTakeAffectAndPush(player, Action.TAKE_MONTHCARD_BOX_AWARD, true);
		}
		
		// 钻石奖励不再走AwardItems体系，单独添加
		if (payGiftCfg.getGainDia() > 0) {
			// 赠送原因 recharge_activity
			player.increaseDiamond(payGiftCfg.getGainDia(), Action.TAKE_MONTHCARD_BOX_AWARD, null, DiamondPresentReason.RECHATGE);
		}
		
		// 向前端推送操作成功的响应
		RechargeBuyItemResp.Builder resp = RechargeBuyItemResp.newBuilder();
		resp.setResultInfo("");
		resp.setGoodsId(payGiftCfg.getId());
		resp.setFreeBox(true);
		sendProtocol(HawkProtocol.valueOf(HP.code.GIFT_SUCCESS_RESP, resp));
		
		LocalRedis.getInstance().updateFreeBoxTakenTime(player.getId(), HawkTime.getAM0Date().getTime());
		
		// 同步数据
		player.getPush().syncGiftList();
		
		return true;
	}
	
	/**
	 * 道具直购
	 * 
	 * @param giftCfg
	 * @param protoType
	 * @return
	 */
	private boolean buyItemRequest(PayGiftCfg giftCfg, int protoType) {
		// ios直购不走后端请求
		if (player.getChannel().indexOf("ios") >= 0) {
			logger.error("MSDK buy item failed, ios platform, playerId: {}", player.getId());
			return false;
		}
		
		// 返回下订单的结果
		if (!SDKManager.getInstance().isPayOpen()) {
			logger.info("buyItemRequest msdk pay not open, playerId: {}", player.getId());
			sendError(protoType, Status.Error.PAY_GIFT_BUY_FAILED);
			return false;
		}

		int sdkType = SDKManager.getInstance().getSdkType();
		if (sdkType != SDKConst.SDKType.MSDK) {
			logger.info("buyItemRequest sdkType error, playerId: {},  sdkType: {}, required Type: {}", player.getId(), sdkType, SDKConst.SDKType.MSDK);
			sendError(protoType, Status.Error.PAY_GIFT_BUY_FAILED);
			return false;
		}

		if (giftCfg.getGiftType() == RechargeType.MONTH_CARD && giftCfg.getMonthCardType() == ConstProperty.getInstance().getGoldPrivilegeType()) {
			int itemCount = player.getData().getItemNumByItemId(ConstProperty.getInstance().getGoldPrivilegeDiscountItem());
			String androidPriceCutId = ConstProperty.getInstance().getGoldPrivilegePayGiftIdAndroid();
			String iosPriceCutId = ConstProperty.getInstance().getGoldPrivilegePayGiftIdIos();
			if (itemCount > 0 && !giftCfg.getId().equals(androidPriceCutId) && !giftCfg.getId().equals(iosPriceCutId)) {
				String cutPriceGiftId = !player.getPlatform().equalsIgnoreCase("ios") ? androidPriceCutId : iosPriceCutId;
				PayGiftCfg tmpCfg = HawkConfigManager.getInstance().getConfigByKey(PayGiftCfg.class, cutPriceGiftId);
				giftCfg = tmpCfg == null ? giftCfg : tmpCfg;
			}
		}

		String urlParam = "";
		// ios道具直购不走后端请求urlParam，直接回调
		if (!player.getPlatform().equalsIgnoreCase("ios")) {
			urlParam = player.payBuyItems(giftCfg);
			if (HawkOSOperator.isEmptyString(urlParam)) {
				sendError(protoType, Status.Error.PAY_GIFT_BUY_FAILED);
				return false;
			}
		}
		
		// 记下直购道具的id，在接收到回调或客户端操作失败通知之前，不让再买
		RedisProxy.getInstance().addUnfinishedRechargeGoods(player.getId(), giftCfg.getId(), GsConfig.getInstance().getBuyItemOrderExpire());
		
		logger.info("buyItemRequest success, playerId: {}, giftId: {}", player.getId(), giftCfg.getId());

		RechargeBuyItemResp.Builder resp = RechargeBuyItemResp.newBuilder();
		resp.setResultInfo(urlParam);
		resp.setGoodsId(giftCfg.getId());
		sendProtocol(HawkProtocol.valueOf(HP.code.GIFT_SUCCESS_RESP, resp));
		return true;
	}

	/**
	 * 礼包直购回调处理
	 * 
	 * @param msg
	 * @return
	 */
	@MessageHandler
	private void onPlayerRechargeAction(PlayerRechargeGrantItemMsg msg) {
		String billno = msg.getBillno();
		PayGiftCfg payGiftCfg = msg.getPayGiftCfg();
		// 回调回来了，直接删除点击购买时记下的直购道具ID
		RedisProxy.getInstance().removeUnfinishedRechargeGoods(player.getId(), payGiftCfg.getId());
		
		// 查订单记录
		RechargeDailyEntity rechargeEntity = player.getData().getPlayerRechargeDailyEntity(billno);
		if (rechargeEntity == null) {
			logger.error("recharge entity is null, billno: {}, playerId: {}, openId: {}",billno, player.getId(), player.getOpenId());
			return;
		}
		
		// 校验是否已发货
		if (!HawkOSOperator.isEmptyString(rechargeEntity.getAwardItems())) {
			logger.error("recharge entity has been delivered, billno: {}, playerId: {}, openId: {}, goodsId: {}, deliverAwards: {}",
					billno, player.getId(), player.getOpenId(), rechargeEntity.getGoodsId(), rechargeEntity.getAwardItems());
			return;
		}
		
		// 实际发货
		try {
			RechargeManager.getInstance().deliverGoods(player, payGiftCfg, rechargeEntity);
			if (!payGiftCfg.isMonthCard()) {
				// 同步
				player.getPush().syncGiftList();
				// 每日特惠礼包购买打点日志
				LogUtil.logGiftBagFlow(player, GiftType.PREFERENTIAL_GIFT, payGiftCfg.getId(), payGiftCfg.getPayRMB(), 0, 0);
			}
			
			RedisProxy.getInstance().idipDailyStatisAdd(player.getId(), IDIPDailyStatisType.GIFT_RECHARGE, payGiftCfg.getPayRMB() / 10);
			RedisProxy.getInstance().idipDailyStatisAdd(player.getId(), IDIPDailyStatisType.TOTAL_RECHARGE, payGiftCfg.getPayRMB() / 10);
			ZonineSDK.getInstance().opDataReport(OpDataType.RECHARGE_GIFT, player.getOpenId(), payGiftCfg.getPayRMB() / 10);
					
			LogUtil.logRechargeFlow(player, payGiftCfg.isMonthCard() ? RechargeType.MONTH_CARD : RechargeType.GIFT, payGiftCfg.getId(), payGiftCfg.getPayRMB());
			
			// 日志记录
			logger.info("MSDK recharge notify success, playerId: {}, openId: {}, goodsId: {}, award: {} ", 
					player.getId(), player.getOpenId(), payGiftCfg.getId(), payGiftCfg.isMonthCard() ? payGiftCfg.getMonthCardType() : payGiftCfg.getServerAwardId());
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 钻石兑换水晶
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.EXCHANGE_DIAMONDS_TO_GOLD_VALUE)
	private boolean onExchangeDiamondsToGold(HawkProtocol protocol) {
		// 参数读取
		ExchangeDiamondsToGold request = protocol.parseProtocol(ExchangeDiamondsToGold.getDefaultInstance());
		int times = request.getExchangeTimes();
		int exchangeValue = ConstProperty.getInstance().getExchangeValue();
		// 消耗钻石
		int cost = exchangeValue * times;
		if(cost <= 0){
			cost = Math.max(0, request.getExchangeCount());
		}
		HawkAssert.checkPositive(cost);
		ConsumeItems consumeItem = ConsumeItems.valueOf();
		consumeItem.addConsumeInfo(PlayerAttr.DIAMOND, cost);
		if (!consumeItem.checkConsume(player, protocol.getType())) {
			return false;
		}
		
		consumeItem.addPayItemInfo(new PayItemInfo(String.valueOf(PlayerAttr.GOLD_VALUE), 1, cost));
		consumeItem.consumeAndPush(player, Action.EXCHANGE_DIAMONDS_TO_GOLD);

		// 增加水晶
		AwardItems awardItems = AwardItems.valueOf();
		awardItems.addGold(cost);
		awardItems.rewardTakeAffectAndPush(player, Action.EXCHANGE_DIAMONDS_TO_GOLD);
		return true;
	}
	
	
	/**
	 * 托管充值定时器拉取余额
	 * 
	 * @return
	 */
	private void onPlayerFetchBalanceAction(PayCfg payCfg, String rechargeInfo, String rechargeId) {
		// 拉取时间间隔
		int[] times = SDKManager.getInstance().getFetchBalanceTime();
		// 执行延时任务
		int threadIdx = player.getXid().getHashThread(HawkTaskManager.getInstance().getExtraThreadNum());
		HawkTaskManager.getInstance().postExtraTask(new HawkDelayTask(times[0] * 1000, 0, times.length) {
			@Override
			public Object run() {
				try {
					int diamonds = player.getDiamonds();
					
					// 拉取不成功
					logger.info("player fetch balance action, playerId: {}, diamonds: {}, saveAmt: {}, player chargeAmt: {}", 
							player.getId(), diamonds, player.getPlayerBaseEntity().getSaveAmt(), player.getPlayerBaseEntity()._getChargeAmt());
					
					// 拉取余额
					int saveAmt = player.checkBalance();
					
					// 同步钻石
					if (diamonds != player.getDiamonds()) {
						player.getPush().syncPlayerDiamonds();
						// 拉取微券信息
						CouponManager.queryAllCoupon(player);
					}
					
					// 判断充值和记账是否有可用差额
					if (saveAmt <= player.getPlayerBaseEntity()._getChargeAmt()) {
						if (getTriggerCount() < times.length - 1) {
							setTriggerPeriod(times[getTriggerCount() + 1] * 1000);
							return null;
						}
						
						if (saveAmt < 0) {
							rechargeFailedResponse(rechargeId, RechargeSuccRespCode.RECHARGE_DEAL_FAILED_VALUE, false);
						} else {
							// 移除票根
							RedisProxy.getInstance().removeRechargeInfo(player.getId(), rechargeId);
							notifyClient(rechargeId, payCfg);
						}
						
						processOrderSet.remove(rechargeId);
						return null;
					}
					
					// 拉取钻石成功，延迟任务结束执行
					setFinish();  
					
					// 充值配置
					PayCfg rechargePayCfg = payCfg;
					// 不符合本配置, 寻找最合适配置
					if (player.getPlayerBaseEntity()._getChargeAmt() + payCfg.getGainDia() > saveAmt) {
						rechargePayCfg = selectPayCfg(rechargeId, payCfg);
					} else {
						RedisProxy.getInstance().removeRechargeInfo(player.getId(), rechargeId);
					}
					
					// 拉取余额完成
					if (rechargePayCfg != null) {
						fetchBalanceSuccess(rechargePayCfg, rechargeInfo, rechargeId);
					}
					
				} catch (Exception e) {
					HawkException.catchException(e);
				}
				
				processOrderSet.remove(rechargeId);
				return null;
			}
		}, threadIdx);
	}
	
	/**
	 * 选取合适的payCfg
	 * 
	 * @param rechargeId 前后端校验用的订单id
	 * @param payCfg 客户端传的payCfg
	 * @return
	 */
	private PayCfg selectPayCfg(String rechargeId, PayCfg payCfg) {
		// 不符合本配置, 寻找最合适配置
		// 选中配置
		PayCfg selectPayCfg = null;
		// 选中的充值id
		String selectRechargeId = null;
		
		// 通过差额计算
		int amtDiff = player.getPlayerBaseEntity().getSaveAmt() - player.getPlayerBaseEntity()._getChargeAmt();
		
		// 获取本地缓存的待充值订单记录
		Map<String, String> rechargeOrders = RedisProxy.getInstance().getAllRechargeInfo(player.getId());
		rechargeOrders.put(rechargeId, payCfg.getId());
		
		// 查订单票据
		for (Entry<String, String> entry : rechargeOrders.entrySet()) {
			PayCfg orderPayCfg = HawkConfigManager.getInstance().getConfigByKey(PayCfg.class, entry.getValue());
			if (orderPayCfg != null && orderPayCfg.getGainDia() == amtDiff) {
				selectPayCfg = orderPayCfg;
				selectRechargeId = entry.getKey();
				break;
			}
		}
		
		// 遍历配置
		if (selectPayCfg == null) {
			// 获取所有的充值配置项
			ConfigIterator<PayCfg> cfgIterator = HawkConfigManager.getInstance().getConfigIterator(PayCfg.class);
			
			while (cfgIterator.hasNext()) {
				PayCfg currentCfg = cfgIterator.next();
				
				// 充值实际获得的钻石数，比配置项配置的所得钻石数大
				if (currentCfg.getGainDia() > amtDiff) {
					continue;
				}
				
				// 本地没有待充值订单记录缓存的时候，取配置中钻石数最接近充值所得钻石数的那条配置
				if (selectPayCfg == null || selectPayCfg.getGainDia() < currentCfg.getGainDia()) {
					selectPayCfg = currentCfg;
				}
			}
		}
		
		// 移除查到的票据
		if (!HawkOSOperator.isEmptyString(selectRechargeId)) {
			RedisProxy.getInstance().removeRechargeInfo(player.getId(), selectRechargeId);
		}
		
		// 没有匹配到合适的配置信息，且未结算的钻石数小于最小配置，删除所有的订单，因为剩下的订单再也匹配不上了
		if (selectPayCfg == null && amtDiff < PayCfg.getSmallestDiamonds()) {
			RedisProxy.getInstance().removeAllRechargeInfo(player.getId());
		}
		
		// 记录日志
		logger.info("recharge fetch amt not match complete, playerId: {}, amtDiff: {}, clientCfgId: {}, selectCfgId: {}, selectRechargeId: {}", 
				player.getId(), amtDiff, payCfg.getId(), selectPayCfg == null ? "null" : selectPayCfg.getId(), selectRechargeId);
		
		return selectPayCfg;
	}
	
	/**
	 * 充值成功处理
	 * 
	 * @param payCfg
	 * @param rechargeInfo
	 * @param rechargeId
	 */
	private void fetchBalanceSuccess(PayCfg payCfg, String rechargeInfo, String rechargeId) {
		// 更新记账信息
		player.getPlayerBaseEntity()._setChargeAmt(player.getPlayerBaseEntity()._getChargeAmt() + payCfg.getGainDia());
		
		// 不欠账的时候, 清理所有存根
		if (player.getPlayerBaseEntity().getSaveAmt() == player.getPlayerBaseEntity()._getChargeAmt()) {
			RedisProxy.getInstance().removeAllRechargeInfo(player.getId());
		}
		
		// 通知成功
		rechargeSuccess(payCfg.getGainDia(), payCfg, rechargeInfo, rechargeId);
		
		// 通知客户端
		notifyClient(rechargeId, payCfg);
		
		// 成功后日志记录信息
		logger.info("recharge fetch balance success, playerId: {}, diamonds: {}, saveAmt: {}, chargeAmt: {}, payCfg: {}, rechargeId: {}", 
				player.getId(), player.getDiamonds(), 
				player.getPlayerBaseEntity().getSaveAmt(), 
				player.getPlayerBaseEntity()._getChargeAmt(), 
				payCfg.getId(), rechargeId);
	}
	
	/**
	 * 充值成功后向客户端同步信息
	 * 
	 * @param rechargeId
	 * @param orderId
	 * @param payCfg
	 */
	private void notifyClient(String rechargeId, PayCfg payCfg) {
		Optional<ActivityBase> opActivity = ActivityManager.getInstance().getActivity(ActivityType.DOUBLE_RECHARGE_VALUE);
		if (!opActivity.isPresent() || !opActivity.get().isOpening(player.getId())) {
			RechargeManager.getInstance().syncRechargeInfo(player);
		}

		RechargeSuccResponse.Builder builder = RechargeSuccResponse.newBuilder();
		builder.setResult(RechargeSuccRespCode.RECHARGE_DEAL_SUCCESS_VALUE);
		builder.setRechargeId(rechargeId);
		builder.setGoodsId(payCfg.getId());
		player.sendProtocol(HawkProtocol.valueOf(HP.code.RECHARGE_SUCC_S_VALUE, builder));
	}
	
	/**
	 * 直充充值成功
	 * 
	 * @param rechargeDiamonds 充值所得钻石数（不包括赠送部分）
	 * @param payCfg
	 * @param rechargeInfo
	 */
	public String rechargeSuccess(int rechargeDiamonds, PayCfg payCfg, String rechargeInfo, String rechargeId) {
		String currency = GameUtil.getPriceType(player.getChannel()).name();
		// 生成充值记录相关的订单ID
		String orderId = RechargeManager.getInstance().generateOrder(player.getPuid(), 
				player.getId(), player.getPlatform(), player.getChannel(), player.getCountry(),
				player.getDeviceId(), payCfg.getId(), 1, payCfg.getPayRMB(), currency, rechargeInfo);
		
		logger.info("recharge accounting success, playerId: {}, goodsId: {}, gaiDiamonds: {}, orderId: {}, rechargeId: {}", 
				player.getId(), payCfg.getId(), rechargeDiamonds, orderId, rechargeId);
		
		// 生成充值记录
		RechargeManager.getInstance().createRechargeRecord(player, orderId, "token", 
				payCfg.getId(), (int)(payCfg.getPayRMB() * GsConst.RECHARGE_BASE), 
				payCfg.getPayRMB(), currency, RechargeType.RECHARGE, HawkTime.getSeconds(), payCfg.getGainDia());
		
		// 充值买钻打点日志
		LogUtil.logGiftBagFlow(player, GiftType.RECHARGE_GIFT, payCfg.getId(), payCfg.getPayRMB(), 0, 0);
		LogUtil.logRechargeFlow(player, RechargeType.RECHARGE, payCfg.getId(), payCfg.getPayRMB());
		
		return orderId;
	}
	
	/**
	 * 发放微信券
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.TAKE_COUPON_REQ_VALUE)
	private boolean onSendCoupon(HawkProtocol protocol) {
		if (ControlProperty.getInstance().getCouponSwitch() == 0) {
			return false;
		}
		
		if (!GameUtil.isWin32Platform(player) && UserType.getByChannel(player.getChannel()) != UserType.WX) {
			return false;
		}
		
		HPTakeCouponReq req = protocol.parseProtocol(HPTakeCouponReq.getDefaultInstance());
		List<HPTakeCouponParam> reqParams = req.getCouponList();
		HawkThreadPool threadPool = HawkTaskManager.getInstance().getThreadPool("task");
		if (threadPool != null) {
			HawkTask task = new HawkTask() {
				@Override
				public Object run() {
					sendCoupon(protocol.getType(), reqParams);
					return null;
				}
			};
			
			task.setPriority(1);
			task.setTypeName("sendCoupon");
			int threadIndex = Math.abs(player.getXid().hashCode() % threadPool.getThreadNum());
			threadPool.addTask(task, threadIndex, false);
		} else {
			HawkTaskManager.getInstance().postExtraTask(new HawkTask() {
				@Override
				public Object run() {
					sendCoupon(protocol.getType(), reqParams);
					return null;
				}
			});
		}
		
		player.responseSuccess(protocol.getType());
		
		return true;
	}
	
	/**
	 * 发微券
	 * 
	 * @param protoType
	 * @param reqParams
	 */
	private void sendCoupon(int protoType, List<HPTakeCouponParam> reqParams) {
		boolean success = false;
		for (HPTakeCouponParam param : reqParams) {
			String couponId = param.getCouponId();
			String actId = param.getActId();
			String result = CouponManager.sendCoupon(player, couponId, actId);
			if (HawkOSOperator.isEmptyString(result)) {
				success = true;
			} else {
				HawkLog.logPrintln("sendCoupon failed, openid: {}, playerId: {}, result: {}, couponId: {}, actId: {}", 
						player.getOpenId(), player.getId(), result, couponId, actId);
			}
		}
		
		if (!success) {
			player.sendError(protoType, Status.Error.SEND_COUPON_FAILED, 0);
		}
		
		long waitTime = GameConstCfg.getInstance().getSendCouponWaitMs();
		HawkTaskManager.getInstance().postExtraTask(new HawkDelayTask(waitTime, waitTime, 1) {
			@Override
			public Object run() {
				CouponManager.queryAllCoupon(player);
				return null;
			}
		});
	}
	
}
