package com.hawk.game.idipscript.recharge;

import org.hawk.app.HawkApp;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.config.PayGiftCfg;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.msg.PlayerRechargeGrantItemMsg;
import com.hawk.game.player.Player;
import com.hawk.game.recharge.RechargeManager;
import com.hawk.game.recharge.RechargeType;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.LogUtil;
import com.hawk.idip.IdipConst;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;
import com.hawk.log.LogConst.Platform;

/**
 * 模拟个人直购道具请求 -- 10282170
 *
 * @author lating
 */
@HawkScript.Declare(id = "idip/4489")
public class RechargeBuyItem4489Handler extends IdipScriptHandler {
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.checkAccountAndPlayer(request, result);
		if (player == null) {
			return result;
		}
		
		int platId = request.getJSONObject("body").getIntValue("PlatId"); //平台：IOS（0），安卓（1）
		Platform platform = Platform.valueOf(platId);
		long goodsIdParam = request.getJSONObject("body").getLongValue("GoodsId");
		String goodsId = String.valueOf(goodsIdParam);
		PayGiftCfg payGiftCfg = HawkConfigManager.getInstance().getConfigByKey(PayGiftCfg.class, goodsId);
		//配置不存在
		if (payGiftCfg == null) {
			HawkLog.errPrintln("RechargeBuyItem4489 config not exist, playerId: {}, openid: {}, goodsId: {}", player.getId(), player.getOpenId(), goodsId);
			result.getBody().put("Result", IdipConst.SysError.PARAM_ERROR);
			result.getBody().put("RetMsg", "goods config not exist");
			return result;
		}
		
		//channelType不匹配
		if (!payGiftCfg.getChannelType().equalsIgnoreCase(platform.strLowerCase())) {
			HawkLog.errPrintln("RechargeBuyItem4489 payGift channelType not match, playerId: {}, openid: {}, goodsId: {}", player.getId(), player.getOpenId(), goodsId);
			result.getBody().put("Result", IdipConst.SysError.PARAM_ERROR);
			result.getBody().put("RetMsg", "goods channelType not match");
			return result;
		}
		
		//当日购买次数达上限
		int rechargeTimesToday = player.getData().getRechargeTimesToday(RechargeType.GIFT, goodsId);
		if (!payGiftCfg.isMonthCard() && payGiftCfg.getPayCount() > 0 && rechargeTimesToday >= payGiftCfg.getPayCount()) {
			HawkLog.errPrintln("RechargeBuyItem4489 pay gift full today, playerId: {}, openId: {}, giftId: {}",  player.getId(), player.getOpenId(), goodsId);
			result.getBody().put("Result", IdipConst.SysError.API_EXCEPTION);
			result.getBody().put("RetMsg", "buy goods this type full today");
			return result;
		}
		
		// 限制单日发放次数
		String key = "RechargeBuyItem4489:" + player.getId() + ":" + HawkTime.getYyyyMMddIntVal();
		String numVal = RedisProxy.getInstance().getRedisSession().hGet(key, goodsId);
		if (!HawkOSOperator.isEmptyString(numVal) && Integer.parseInt(numVal) > 0) {
			HawkLog.errPrintln("RechargeBuyItem4489 pay gift today already, playerId: {}, openId: {}, giftId: {}",  player.getId(), player.getOpenId(), goodsId);
			result.getBody().put("Result", IdipConst.SysError.API_EXCEPTION);
			result.getBody().put("RetMsg", "idip buy goods this type limit today");
			return result;
		}
		
		int ts = HawkTime.getSeconds();
		String currency = GameUtil.getPriceType(player.getChannel()).name();
		//生成充值记录相关的订单ID
		String orderId = RechargeManager.getInstance().generateOrder(player.getPuid(), player.getId(),player.getPlatform(), 
				player.getChannel(), player.getCountry(), player.getDeviceId(), payGiftCfg.getId(), 1, payGiftCfg.getPayRMB(), currency, "");
		//订单id生成失败
		if (HawkOSOperator.isEmptyString(orderId)) {
			HawkLog.errPrintln("RechargeBuyItem4489 generate orderId error, playerId: {}, openId: {}, giftId: {}",  player.getId(), player.getOpenId(), goodsId);
			result.getBody().put("Result", IdipConst.SysError.API_EXCEPTION);
			result.getBody().put("RetMsg", "buy goods failed");
			return result;
		}
		
		int price = payGiftCfg.getPayRMB();
		int payMoney = (int)(price * GsConst.RECHARGE_BASE);
		RechargeManager.getInstance().createRechargeRecord(player, orderId, "token", goodsId, payMoney, price, currency, RechargeType.GIFT, ts, payGiftCfg.getGainDia());
		
		//通知发货
		HawkApp.getInstance().postMsg(player.getXid(), PlayerRechargeGrantItemMsg.valueOf(payGiftCfg, orderId, price));
		RedisProxy.getInstance().getRedisSession().hIncrBy(key, goodsId, 1, GsConst.DAY_SECONDS);
		
		HawkLog.logPrintln("RechargeBuyItem4489 buy goods success, playerId: {}, openid: {}, goodsId: {}", player.getId(), player.getOpenId(), goodsId);
		LogUtil.logIdipSensitivity(player, request, 0, 0);
	    result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		return result;
	}
	
}
