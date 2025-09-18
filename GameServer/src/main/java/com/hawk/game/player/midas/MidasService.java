package com.hawk.game.player.midas;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.uuid.HawkUUIDGenerator;

import com.alibaba.fastjson.JSONObject;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.DiamondRechargeEvent;
import com.hawk.activity.event.impl.FirstRechargeEvent;
import com.hawk.activity.event.impl.RechargeAllRmbEvent;
import com.hawk.activity.event.impl.RechargeMoneyEvent;
import com.hawk.game.GsConfig;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.config.GameConstCfg;
import com.hawk.game.config.PayCfg;
import com.hawk.game.config.PayGiftCfg;
import com.hawk.game.data.RechargeInfo;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.idipscript.util.IdipUtil.MoneyType;
import com.hawk.game.invoker.PayCancelMsgInvoker;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.player.Player;
import com.hawk.game.player.platchange.PlatChangeService;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.recharge.RechargeManager;
import com.hawk.game.recharge.RechargeType;
import com.hawk.game.service.QuestionnaireService;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.LogUtil;
import com.hawk.game.util.GsConst.QuestionnaireConst;
import com.hawk.game.util.GsConst.ScoreType;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;
import com.hawk.log.LogConst.LogInfoType;
import com.hawk.sdk.SDKConst;
import com.hawk.sdk.SDKManager;
import com.hawk.sdk.msdk.entity.BuyItemsResult;
import com.hawk.sdk.msdk.entity.CancelPayResult;
import com.hawk.sdk.msdk.entity.CheckBalanceResult;
import com.hawk.sdk.msdk.entity.PayItemInfo;
import com.hawk.sdk.msdk.entity.PayResult;
import com.hawk.sdk.msdk.entity.PresentResult;
import com.hawk.sdk.msdk.entity.ShoppingGood;
import com.hawk.zoninesdk.ZonineSDK;
import com.hawk.zoninesdk.datamanager.OpDataType;

public class MidasService {

	private static MidasService instance = new MidasService();
	
	public static MidasService getInstance() {
		return instance;
	}
	
	/**
	 * 拉取账户余额
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public int checkBalance(Player player, JSONObject pfTokenJson) {
		// 避免token盗用
		try {
			String openid = pfTokenJson == null ? null : pfTokenJson.getString("open_id");
			if (!player.getOpenId().equals(openid)) {
				HawkLog.errPrintln("MSDK check balance failed, playerId: {}, openId: {}, pftoken openid: {}", player.getId(), player.getOpenId(), openid);
				return -1;
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		CheckBalanceResult result = SDKManager.getInstance().checkBalance(player.getPlatform(), player.getChannel(), player.getPfTokenJson(), player.getServerId());
		
		if (result != null && result.getRet() == SDKConst.ResultCode.SUCCESS) {
			int playerSaveAmt = player.getPlayerBaseEntity().getSaveAmt();
			int diamonds = player.getDiamonds();
			// 此处对于转平台的角色，转完后首次登录游戏，对比金条存量，多扣少加；同时，对redis中的rechargeInfo数据 和 db中的rechargeEntity数据 做些特殊处理（这个要在转的时候处理）
			if (PlatChangeService.getInstance().platTransferAfter(player, result)) {
				HawkLog.logPrintln("MSDK check balance after plat transfer, playerId: {}, openid: {}, platform: {}, old diamonds: {}, old saveAmt: {}, new diamonds: {}, new saveAmt: {}", 
						player.getId(), player.getOpenId(), player.getPlatform(), diamonds, playerSaveAmt, result.getBalance(), result.getSave_amt());
				return result.getSave_amt();
			}
			
			if (result.getSave_amt() > 0) {
				checkBalanceAfter(player, playerSaveAmt, diamonds, result);
			}
			// 日志记录
			HawkLog.logPrintln("MSDK check balance success, playerId: {}, diamonds: {}, saveAmt: {}, player chargeAmt: {}",
					player.getId(), result.getBalance(), result.getSave_amt(), player.getPlayerBaseEntity()._getChargeAmt());

			return result.getSave_amt();
		}

		HawkLog.errPrintln("MSDK check balance failed, playerId: {}, openId: {}, retCode: {}, platform: {}, channel: {}, serverId: {}, pfToken: {}",
				player.getId(), player.getOpenId(), result != null ? result.getRet() : result, player.getPlatform(),
						player.getChannel(), GsConfig.getInstance().getServerId(), player.getPfTokenJson());

		return -1;
	}
	
	/**
	 * 拉取到账户余额数据后的处理
	 * @param playerSaveAmt
	 * @param diamonds
	 * @param result
	 */
	private void checkBalanceAfter(Player player, int playerSaveAmt, int diamonds, CheckBalanceResult result) {
		player.getPlayerBaseEntity().setDiamonds(result.getBalance());
		player.getPlayerBaseEntity().setSaveAmt(result.getSave_amt());
		int rechargeDiamonds = result.getSave_amt() - playerSaveAmt;
		if (rechargeDiamonds > 0) {
			rechargeSuccess(player, playerSaveAmt, rechargeDiamonds, diamonds);
		}
		
		if (result.getBalance() > 0) {
			try {
				String key = "todoSubDiamonds:" + player.getId();
				String todoSubDiamonds = RedisProxy.getInstance().getRedisSession().getString(key);
				int subCount = HawkOSOperator.isEmptyString(todoSubDiamonds) ? 0 : Integer.parseInt(todoSubDiamonds);
				if (subCount > 0) {
					subCount = Math.min(result.getBalance(), subCount);
					ConsumeItems consume = ConsumeItems.valueOf(PlayerAttr.DIAMOND, subCount);
					if (consume.checkConsume(player)) {
						consume.consumeAndPush(player, Action.IDIP_CHANGE_PLAYER_ATTR);
						RedisProxy.getInstance().getRedisSession().increaseBy(key,  0 - subCount, 0);
						HawkLog.logPrintln("subcrease diamonds after checkBalance, playerId: {}, diamonds: {}, todoSubDiamonds: {}, subCount: {}", 
								player.getId(), result.getBalance(), todoSubDiamonds, subCount);
					}
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
	}
	

	/**
	 * 充值成功处理
	 * @param playerSaveAmt 历史累计充值钻石数（不含赠送部分）
	 * @param rechargeAmt   充值钻石数量（不含赠送部分）
	 * @param diamonds      充值前拥有的钻石数
	 */
	@SuppressWarnings("deprecation")
	public void rechargeSuccess(Player player, int playerSaveAmt, int rechargeAmt, int diamonds) {
		if (player.getDiamonds() != diamonds) {
			player.getPush().syncPlayerDiamonds();
		}

		//充值获得额外奖励：vip经验
		int vipExp = (int) (rechargeAmt * ConstProperty.getInstance().getDiaExchangeVipExpCof() / 100.0D);
		AwardItems awardItems = AwardItems.valueOf();
		awardItems.addVipExp(vipExp);
		awardItems.rewardTakeAffectAndPush(player, Action.RECHARGE_BUY_DIAMONDS);
		//db数据更新
		player.getRechargeTotal(); //只为刷新数据（兼容历史）
		int saveAmtTotal = player.getPlayerBaseEntity().getSaveAmtTotal();
		player.getPlayerBaseEntity().setSaveAmtTotal(saveAmtTotal + rechargeAmt);

		//统计信息更新
		GlobalData.getInstance().addRechargePlayer(player.getId());
		RechargeInfo rechargeInfo = new RechargeInfo(player.getOpenId(), player.getId(), player.getPlatId(), player.getServerId(), HawkTime.getSeconds(), rechargeAmt, RechargeType.RECHARGE);
		RedisProxy.getInstance().rechargeBatchSave(rechargeInfo);
		player.rechargeTotalAdd(rechargeAmt);

		//推送活动充值事件
		ActivityManager.getInstance().postEvent(new RechargeMoneyEvent(player.getId(), rechargeAmt));
		ActivityManager.getInstance().postEvent(new RechargeAllRmbEvent(player.getId(), rechargeAmt/10));
		ActivityManager.getInstance().postEvent(new DiamondRechargeEvent(player.getId(), "", rechargeAmt));
		if (saveAmtTotal < PayCfg.getSmallestDiamonds() && player.getPlayerBaseEntity().getSaveAmtTotal() >= PayCfg.getSmallestDiamonds()) {
			ActivityManager.getInstance().postEvent(new FirstRechargeEvent(player.getId()));
			player.getPush().syncHasFirstRecharge();
		}
		
		//触发问卷
		QuestionnaireService.getInstance().questionaireCheck(player, QuestionnaireConst.CONDITION_BUILDING_RECHARGE, 0);
		//数据上报
		int diamondDiff = player.getDiamonds() - diamonds;
		LogUtil.logMoneyFlow(player, Action.RECHARGE_BUY_DIAMONDS, LogInfoType.money_add, diamondDiff, MoneyType.DIAMOND);
		ZonineSDK.getInstance().opDataReport(OpDataType.RECHARGE_AMOUNT, player.getOpenId(), rechargeAmt);
		// 上报积分 - 单次充值金额、累计充值金额、充值时间
		GameUtil.scoreBatch(player, ScoreType.RECHARGE_AMOUNT, rechargeAmt);
		GameUtil.scoreBatch(player, ScoreType.TOTAL_RECHARGE_AMOUNT, player.getPlayerBaseEntity().getSaveAmtTotal());
		GameUtil.scoreBatch(player, ScoreType.RECHARGE_TIME, HawkTime.getSeconds());
	}

	/**
	 * 消费支付
	 * @param diamond
	 */
	@SuppressWarnings("deprecation")
	public String pay(Player player, int diamond, String actionName, List<PayItemInfo> payItems) {
		// 记录订单
		String currency = GameUtil.getPriceType(player.getChannel()).name();
		String billno = RechargeManager.getInstance().generateOrder(player.getPuid(), player.getId(), player.getPlatform(),
				player.getChannel(), player.getCountry(), player.getDeviceId(), "pay", 1, diamond, currency, null);

		PayItemInfo[] array = payItems == null || payItems.isEmpty() ? null : payItems.toArray(new PayItemInfo[payItems.size()]);
		int resultCode = SDKConst.ResultCode.FAILED, reqCount = SDKConst.PAY_REQ_REPEAT_TIMES;
		do {
			// 调用支付
			PayResult result = SDKManager.getInstance().pay(player.getPlatform(), player.getChannel(), player.getPfTokenJson(), player.getServerId(), diamond, billno, array);
			reqCount--;
			int code = result != null ? result.getRet() : SDKConst.ResultCode.FAILED;
			// 正常的支付调用
			if (code == SDKConst.ResultCode.SUCCESS) {
				player.getPlayerBaseEntity().setDiamonds(result.getBalance()); // 刷新余额
				HawkLog.logPrintln("MSDK Pay sucess, playerId: {}, diamonds: {}, saveAmt: {}, cost: {}, billno: {}, action: {}",
						player.getId(), result.getBalance(), result.getSave_amt(), diamond, billno, actionName);
				return billno;
			}

			// 重复发单的支付调用：这种情况下，虽然最终的处理结果是正确的，但米大师返回的信息中没有钻石数信息，所以只能暂时依赖本地的值做运算
			if (resultCode == SDKConst.ResultCode.PAY_RESULT_EXP_CODE1 && code == SDKConst.ResultCode.PAY_RESULT_EXP_CODE2) {
				int oldCount = player.getPlayerBaseEntity().getDiamonds();
				player.getPlayerBaseEntity().setDiamonds(oldCount - diamond); // 刷新余额
				HawkLog.logPrintln("MSDK Pay sucess, playerId: {}, diamonds: {}, saveAmt: {}, cost: {}, billno: {}, action: {}",
						player.getId(), result.getBalance(), result.getSave_amt(), diamond, billno, actionName);
				return billno;
			}

			resultCode = code;

			HawkLog.errPrintln("MSDK Pay Failure, playerId: {}, retCode: {}, platform: {}, channel: {}, "
					+ "serverId: {}, diamond: {}, billino: {}, pfToken: {}, action: {}", player.getId(), resultCode, player.getPlatform(),
					player.getChannel(), player.getServerId(), diamond, billno, player.getPfTokenJson(), actionName);
		} while (resultCode == SDKConst.ResultCode.PAY_RESULT_EXP_CODE1 && reqCount > 0);

		tokenExpireNotify(player, resultCode);

		return null;
	}

	/**
	 * 取消支付
	 * @param diamond
	 * @param billno
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public boolean cancelPay(Player player, int diamond, String billno) {
		int resultCode = SDKConst.ResultCode.FAILED, reqCount = SDKConst.PAY_REQ_REPEAT_TIMES;
		do {
			CancelPayResult result = SDKManager.getInstance().cancelPay(player.getPlatform(), player.getChannel(),
					player.getPfTokenJson(), player.getServerId(), diamond, billno);
			reqCount--;
			int code = result != null ? result.getRet() : SDKConst.ResultCode.FAILED;
			if (code == SDKConst.ResultCode.SUCCESS ||
					(resultCode == SDKConst.ResultCode.PAY_RESULT_EXP_CODE1 && code == SDKConst.ResultCode.PAY_RESULT_EXP_CODE2)) {
				player.dealMsg(MsgId.PAY_CANCLE, new PayCancelMsgInvoker(player));
				HawkLog.logPrintln("MSDK pay cancel sucess, playerId: {}, diamonds: {}, billno: {}", player.getId(), diamond, billno);
				return true;
			}

			resultCode = code;
			HawkLog.errPrintln("MSDK pay cancel failed, playerId: {}, retCode: {}, platform: {}, channel: {}, pftoken: {}, serverId: {}, diamonds: {}, billno: {}",
					player.getId(), resultCode, player.getPlatform(), player.getChannel(), player.getPfTokenJson(), player.getServerId(), diamond, billno);

		} while (resultCode == SDKConst.ResultCode.PAY_RESULT_EXP_CODE1 && reqCount > 0);

		tokenExpireNotify(player, resultCode);

		return false;
	}

	/**
	 * 货币赠送
	 * @param diamond
	 * @param extendParam
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public int present(Player player, int diamond, String extendParam, String actionName, String presentReason) {
		int resultCode = SDKConst.ResultCode.FAILED, reqCount = SDKConst.PAY_REQ_REPEAT_TIMES;
		try {
			String billno = HawkOSOperator.randomUUID();
			do {
				PresentResult presentResult = SDKManager.getInstance().present(
						player.getPlatform(), player.getChannel(), player.getPfTokenJson(), player.getServerId(), diamond, billno, extendParam, presentReason);
				reqCount--;
				int code = presentResult != null ? presentResult.getRet() : SDKConst.ResultCode.FAILED;
				if (code == SDKConst.ResultCode.SUCCESS ||
						(resultCode == SDKConst.ResultCode.PAY_RESULT_EXP_CODE1 && code == SDKConst.ResultCode.PAY_RESULT_EXP_CODE2)) {
					player.getPlayerBaseEntity().setDiamonds(presentResult.getBalance());

					player.getPush().syncPlayerDiamonds();

					HawkLog.logPrintln("MSDK present sucess, playerId: {}, diamonds: {}, add: {}, billno: {}, extendParam: {}, action: {}",
							player.getId(), presentResult.getBalance(), diamond, presentResult.getBillno(), extendParam, actionName);

					return 0;
				}

				resultCode = code;
				HawkLog.errPrintln("MSDK present help recharge failed, playerId: {}, retCode: {}, platform: {}, channel: {}, "
						+ "serverId: {}, diamond: {}, pfToken: {}, billno: {}, extendParam: {}, action: {}", player.getId(), resultCode, player.getPlatform(),
						player.getChannel(), player.getServerId(), diamond, player.getPfTokenJson(), billno, extendParam, actionName);
			} while (resultCode == SDKConst.ResultCode.PAY_RESULT_EXP_CODE1 && reqCount > 0);

			tokenExpireNotify(player, resultCode);

		} catch (Exception e) {
			HawkException.catchException(e);
		}

		return resultCode;
	}

	/**
	 * 道具直购
	 * @param giftCfg
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public String payBuyItems(Player player, PayGiftCfg giftCfg) {
		List<ShoppingGood> shoppingGoods = new ArrayList<ShoppingGood>();
		ShoppingGood good = new ShoppingGood(giftCfg.getId(), giftCfg.getNameData(), (int) (giftCfg.getPayRMB() * GsConst.RECHARGE_BASE),
				1, giftCfg.getDesData(), giftCfg.getShowPicUrl());
		shoppingGoods.add(good);

		String areaId = GsConfig.getInstance().getAreaId();
		//大区redis合并后，如果手Q玩家用1作为areaId去传参请求失败，则可通过开关控制，改成跟区服对应的areaId
		if (GameConstCfg.getInstance().getPayItemsAreaSwith() > 0) {
			areaId = String.valueOf(Integer.parseInt(player.getServerId()) / 10000);
		}
		
		// 透传参数
		StringBuilder buff = new StringBuilder();
		buff.append(areaId).append("*");
		buff.append(player.getServerId()).append("*");
		buff.append(player.getPlatform()).append("*");
		buff.append(player.getId());

		String outTradeNo = HawkUUIDGenerator.genUUID();
		Map<String, Object> map = new HashMap<>();
		map.put("platform", player.getPlatform());
		map.put("channel", player.getChannel());
		map.put("serverId", player.getServerId());
		map.put("appMeta", buff.toString());
		map.put("outTradeNo", outTradeNo);
		BuyItemsResult result = SDKManager.getInstance().payBuyItems(map, player.getPfTokenJson(), shoppingGoods);
		// 充值成功
		if (result != null && result.getRet() == SDKConst.ResultCode.SUCCESS) {
			// GMServer收到米大师的回调请求时，会校验这个token
			JSONObject json = new JSONObject();
			json.put("playerId", player.getId());
			json.put("platform", player.getPlatform());
			json.put("openid", player.getOpenId());
			json.put("token", result.getToken());
			json.put("complete", 0);
			RedisProxy.getInstance().updatePayItemInfo(outTradeNo, json);
			HawkLog.logPrintln("MSDK buy item success, playerId: {}, token: {}, urlParam: {}, goods: {}, pfToken: {}", player.getId(), result.getToken(), result.getUrl_params(), shoppingGoods, player.getPfTokenJson());
			return result.getUrl_params();
		}

		tokenExpireNotify(player, result != null ? result.getRet() : 0);

		HawkLog.errPrintln("MSDK buy item failed, playerId: {}, platform: {}, channel: {}, serverId: {}, goods: {}, appMeta: {}, pfToken: {}",
				player.getId(), player.getPlatform(), player.getChannel(),
				GsConfig.getInstance().getServerId(), shoppingGoods, buff.toString(), player.getPfTokenJson());
		return null;
	}

	/**
	 * 调用midas支付相关接口，token过期提示
	 * 
	 * @param payErrorCode
	 */
	private void tokenExpireNotify(Player player, int payErrorCode) {
		if (payErrorCode != GameConstCfg.getInstance().getTokenExpiredCode()) {
			return;
		}

		player.sendProtocol(HawkProtocol.valueOf(HP.code.LOGIN_TOKEN_EXPIRED_S));
	}
	
}
