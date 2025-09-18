package com.hawk.game.gmscript;

import java.util.Map;

import org.hawk.app.HawkApp;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.config.PayGiftCfg;
import com.hawk.game.config.ServerAwardCfg;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.manager.AssembleDataManager;
import com.hawk.game.msg.PlayerRechargeGrantItemMsg;
import com.hawk.game.player.Player;
import com.hawk.game.queryentity.AccountInfo;
import com.hawk.game.recharge.RechargeManager;
import com.hawk.game.util.GameUtil;
import com.hawk.game.recharge.RechargeType;

/**
 * 充值回调通知入口
 * 
 * <pre> -- ios不接订单中心参数
 *  recharge notify, 
 * 	openid: 637276034711AE74525BFB266850F709, 
 * 	appid: 1450012316, 
 * 	ts: 1512639845, 
 * 	payitem: 210001*10*1, 
 * 	token: B79582D73141E1153A65622C16E341C315980, 
 * 	billno: -APPDJ68655-20171207-1743538605, 
 * 	version: v3, 
 * 	zoneid: 992, 
 * 	providetype: 5, 
 * 	amt: 95, 
 * 	appmeta: 991*2001*android*1jl-15i6v-1*qqwallet*qq, 
 * 	cftid: 10000234016011201712071193755855, 
 * 	channel_id: 73213123-android-73213123-qq-1105906633-637276034711AE74525BFB266850F709-qq, 
 * 	clientver: android, 
 * 	payamt_coins: 0, 
 * 	pubacct_payamt_coins: , 
 * 	bazinga: null, 
 * 	sig: ngO3LilEWcTbidUkv0XUfJXXIPo= 
 * <pre>
 * 
 * <pre> -- android接订单中心参数
 * 	openid
 * 	appid： 【应用ID，在Midas侧注册的应用的唯一标识】
 * 	product_id： 【商品id，仅支持数字、字母、下划线（_）、横杠字符（-）、点（.）的组合。】
 * 	num： 【购买的商品的数量。例如：1】
 * 	amt： 【用户实际支付金额，单位：分（人民币）】
 * 	out_trade_no： 【业务订单号，仅支持数字、字母、下划线（_）、横杠字符（-）、点（.）的组合。】
 * 	paychannelsubid： 【支付子渠道】
 * 	transaction_id： 【调用下单接口获取的Midas交易订单】
 * 	version： 【协议版本号，目前固定为“v5”】
 * 	providetype： 【发货类型，目前集团产品应用，固定为10】
 * 	meta_data： 【应用下单时传的meta_data字段，长度不超过255字符】
 * 	channel： 【用户支付渠道，取值说明如下：（wechat：微信支付，qqwallet：QQ钱包，bank：财付通网银，remitpay：线下打款，os_remitpay：海外线下打款）】
 * 	channel_orderid： 【微信支付、财付通网银的订单号】
 * 	动态字段： 【如abazinga】
 * 	ts
 * 	sign
 * 	sign_type： 【签名验证方法，目前固定为RSA】
 * <pre>
 * 
 * @author hawk
 */
public class RechargeHandler extends HawkScript {
	static Logger logger = LoggerFactory.getLogger("Recharge");

	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		String outTradeNo = params.get("out_trade_no");
		//android接了订单中心，另外处理
		if (!HawkOSOperator.isEmptyString(outTradeNo)) {
			return orderCenterNotifyProcess(params);
		}
		
		String appmeta = params.get("appmeta");
		String platform = "ios", playerId = null;
		if (appmeta.indexOf("android") > 0) {
			String[] appmetaInfos = appmeta.split("\\*");
			platform = appmetaInfos[2];
			playerId = appmetaInfos[3];
		}

		String payitem = params.get("payitem");
		String goodsId = null;
		int goodsCount = 0, payMoney = 0;
		if (!HawkOSOperator.isEmptyString(payitem)) {
			String[] strs = payitem.split("\\*");
			goodsId = strs[0];
			String price = strs[1];
			goodsCount = Integer.parseInt(strs[2]);
			payMoney = Integer.parseInt(price) * goodsCount;
			
			if (platform.equals("ios")) {
				goodsId = PayGiftCfg.getId(goodsId, platform);
			}
		}

		// 获取回调参数
		String openid = params.get("openid");
		String appid = params.get("appid");
		String ts = params.get("ts");
		String token = params.get("token");
		String billno = params.get("billno");
		String version = params.get("version");
		String zoneid = params.get("zoneid");
		String providetype = params.get("providetype");
		String amt = params.get("amt");
		String cftid = params.get("cftid");
		String channel_id = params.get("channel_id");
		String clientver = params.get("clientver");
		String payamt_coins = params.get("payamt_coins");
		String pubacct_payamt_coins = params.get("pubacct_payamt_coins");
		String bazinga = params.get("bazinga");
		String sig = params.get("sig");
		
		// 记录回调参数
		logger.info("recharge notify, openid: {}, appid: {}, ts: {}, payitem: {}, token: {}, billno: {}, version: {}, zoneid: {}, providetype: {}, "
				+ "amt: {}, appmeta: {}, cftid: {}, channel_id: {}, clientver: {}, payamt_coins: {}, pubacct_payamt_coins: {}, bazinga: {}, sig: {}", 
				openid, appid, ts, payitem, token, billno, version, zoneid, providetype, amt, appmeta, cftid, channel_id, clientver, payamt_coins, 
				pubacct_payamt_coins, bazinga, sig);
		
		if (HawkOSOperator.isEmptyString(playerId)) {
			String puid = openid + "#" + platform;
			AccountInfo accountInfo = GlobalData.getInstance().getAccountInfo(puid, zoneid);
			playerId = accountInfo.getPlayerId();
		}

		// 初始化返回json对象
		JSONObject result = new JSONObject();
		result.put("result", -1);

		try {
			Player player = GlobalData.getInstance().makesurePlayer(playerId);
			if (!orderCheck(params, player, goodsId, goodsCount, payMoney)) {
				// 错误日志
				logger.error("recharge notify order check failed, openid: {}, appid: {}, ts: {}, payitem: {}, token: {}, billno: {}, version: {}, zoneid: {}, providetype: {}, "
						+ "amt: {}, appmeta: {}, cftid: {}, channel_id: {}, clientver: {}, payamt_coins: {}, pubacct_payamt_coins: {}, bazinga: {}, sig: {}", 
						openid, appid, ts, payitem, token, billno, version, zoneid, providetype, amt, appmeta, cftid, channel_id, clientver, payamt_coins, 
						pubacct_payamt_coins, bazinga, sig);
				
				return result.toJSONString();
			}
			
			PayGiftCfg payGiftCfg = HawkConfigManager.getInstance().getConfigByKey(PayGiftCfg.class, goodsId);
			
			// 每日特惠礼包限购措施
			int rechargeTimesToday = player.getData().getRechargeTimesToday(RechargeType.GIFT, goodsId);
			if (!payGiftCfg.isMonthCard() && payGiftCfg.getPayCount() > 0 && rechargeTimesToday >= payGiftCfg.getPayCount()) {
				logger.error("recharge notify failed, pay gift full today, playerId: {}, openId: {}, giftId: {}",  
						player.getId(), player.getOpenId(), goodsId);
				return result.toJSONString();
			}
			
			// 记录充值订单，创建订单entity是用billo作为主键，保证了每笔订单的唯一性，也就不存在多线程问题
			String currency = GameUtil.getPriceType(player.getChannel()).name();
			boolean succ = RechargeManager.getInstance().createRechargeRecord(player, billno, token, goodsId, 
					payMoney, payMoney * 10, currency, RechargeType.GIFT, Integer.valueOf(ts), payGiftCfg.getGainDia());
			if (!succ) {
				logger.error("recharge notify failed, billno: {}, token: {}, playerId: {}, goodsId: {}, payMaoney: {}",
						billno, token, playerId, goodsId, payMoney);
				
				return result.toJSONString();
			}
			
			// 发货通知
			HawkApp.getInstance().postMsg(player.getXid(), PlayerRechargeGrantItemMsg.valueOf(payGiftCfg, billno, payMoney * 10));
			
			// 充值回调成功
			logger.info("recharge notify success, billno: {}, token: {}, playerId: {}, goodsId: {}, goodsPrice: {}, payMoney: {}",
					billno, token, player.getId(), goodsId, payMoney * 10, payMoney);
			
			result.put("result", 0);
			
		} catch (Exception e) {
			HawkException.catchException(e);
			logger.error("recharge notify exception, billno: {}, token: {}, playerId: {}, goodsId: {}, payMaoney: {}",
					billno, token, playerId, goodsId, payMoney);
		}

		return result.toJSONString();
	}
	
	/**
	 * 订单信息校验
	 * @param params
	 * @param player
	 * @param goodsId
	 * @param goodsCount
	 * @param payMoney
	 * @return
	 */
	private boolean orderCheck(Map<String, String> params, Player player, String goodsId, int goodsCount, int payMoney) {
		String token = params.get("token");
		String billno = params.get("billno");
		String appmeta = params.get("appmeta");
		if (player == null) {
			logger.error("recharge notify failed, player not exist, billno: {}, token: {}, appmeta: {}, goodsId: {}", billno, token, appmeta, goodsId);
			return false;
		}
		
		// 道具ID不对
		if (HawkOSOperator.isEmptyString(goodsId)) {
			logger.error("recharge notify failed, goodsId is null, billno: {}, token: {}, playerId: {}, goodsId: {}", billno, token, player.getId(), goodsId);
			return false;
		}
		
		// 订单判断：GMServer在收到米大师回调请求，向GameServer转发之前，会先把订单相关信息保存下来，留待此处核对校验
		JSONObject orderJson = RedisProxy.getInstance().getCallBackOrderInfo(player.getPuid(), billno);
		if (orderJson == null) {
			logger.error("recharge notify failed, order info not exist, billno: {}, puid: {}, token: {},  goodsId: {}", billno, player.getPuid(), token, goodsId);
			return false;
		}

		// 商品判断
		PayGiftCfg payGiftCfg = HawkConfigManager.getInstance().getConfigByKey(PayGiftCfg.class, goodsId);
		if (payGiftCfg == null) {
			logger.error("recharge notify failed, paygift config not exist, billno: {}, token: {}, playerId: {}, goodsId: {}", billno, token, player.getId(), goodsId);
			return false;
		}
		
		ServerAwardCfg cfg = AssembleDataManager.getInstance().getServerAwardByAwardId(payGiftCfg.getServerAwardId());
		if (payGiftCfg.getServerAwardId() != 0 && cfg == null) {
			logger.error("recharge notify failed, paygift award not exist, billno: {}, token: {}, playerId: {}, goodsId: {}, serverAwardId: {}",
					billno, token, player.getId(), goodsId, payGiftCfg.getServerAwardId());
			return false;
		}

		// 金额判断
		int goodsTotalPrice = payGiftCfg.getPayRMB() * goodsCount;
		
		// 以Q点为单位，1Q币=10Q点，单价的制定需遵循腾讯定价规范
		if (payMoney * 10 != goodsTotalPrice) {
			logger.error("recharge notify failed, pay money error, billno: {}, token: {}, playerId: {}, goodsId: {}, goodsPrice: {}, payMoney: {}",
					billno, token, player.getId(), goodsId, goodsTotalPrice, payMoney);
			return false;
		}
		
		return true;
	}
	
	/**
	 * 接入订单中心的直购回调处理（只有android接入了）
	 * @return
	 */
	private String orderCenterNotifyProcess(Map<String, String> params) {
		//获取回调参数
		String openid = params.get("openid");
		String goodsId = params.get("product_id");
		String billno = params.get("transaction_id");
		int ts = Integer.parseInt(params.getOrDefault("ts", "0"));
		int goodsCount = Integer.parseInt(params.getOrDefault("num", "1"));
		String outTradeNo = params.get("out_trade_no");
		
		JSONObject result = new JSONObject();
		result.put("result", -1);

		String payItemInfo = RedisProxy.getInstance().getPayItemInfo(outTradeNo);
		if (HawkOSOperator.isEmptyString(payItemInfo)) {
			logger.error("recharge notify payItemInfo of out_trade_no empty, openid: {}, billno: {}, out_trade_no: {}", openid, billno, outTradeNo);
			return result.toJSONString();
		}
		
		JSONObject json = JSONObject.parseObject(payItemInfo);
		String playerId = json.getString("playerId");
		try {
			Player player = GlobalData.getInstance().makesurePlayer(playerId);
			if (!orderCheck(params, player, goodsId, json)) {
				logger.error("recharge notify order check failed, openid: {}, playerId: {}", openid, playerId);
				return result.toJSONString();
			}
			
			PayGiftCfg payGiftCfg = HawkConfigManager.getInstance().getConfigByKey(PayGiftCfg.class, goodsId);
			int rechargeTimesToday = player.getData().getRechargeTimesToday(RechargeType.GIFT, goodsId);
			//每日特惠礼包限购措施
			if (!payGiftCfg.isMonthCard() && payGiftCfg.getPayCount() > 0 && rechargeTimesToday >= payGiftCfg.getPayCount()) {
				logger.error("recharge notify failed, pay gift full today, playerId: {}, openId: {}, giftId: {}", player.getId(), player.getOpenId(), goodsId);
				return result.toJSONString();
			}
			
			String token = json.getString("token"); //这里不想去修改内部接口，所以给它赋值一个空字符串
			int payMoney = payGiftCfg.getPayRMB()/10 * goodsCount;
			
			//记录充值订单，创建订单entity是用billo作为主键，保证了每笔订单的唯一性，也就不存在多线程问题
			String currency = GameUtil.getPriceType(player.getChannel()).name();
			boolean succ = RechargeManager.getInstance().createRechargeRecord(player, billno, token, goodsId, payMoney, payMoney*10, currency, RechargeType.GIFT, ts, payGiftCfg.getGainDia());
			if (!succ) {
				logger.error("recharge notify failed, billno: {}, openid: {}, playerId: {}, goodsId: {}", billno, openid, playerId, goodsId);
				return result.toJSONString();
			}
			
			//发货通知
			HawkApp.getInstance().postMsg(player.getXid(), PlayerRechargeGrantItemMsg.valueOf(payGiftCfg, billno, payMoney*10));
			json.put("complete", 1);
			RedisProxy.getInstance().updatePayItemInfo(outTradeNo, json);
			
			result.put("result", 0);
			logger.info("recharge notify success, billno: {}, openid: {}, playerId: {}, goodsId: {}", billno, openid, player.getId(), goodsId);
		} catch (Exception e) {
			HawkException.catchException(e);
			logger.error("recharge notify exception, billno: {}, openid: {}, playerId: {}, goodsId: {}", billno, openid, playerId, goodsId);
		}

		return result.toJSONString();
	}
	
	/**
	 * 订单信息校验
	 * @param params
	 * @param player
	 * @param goodsId
	 * @return
	 */
	private boolean orderCheck(Map<String, String> params, Player player, String goodsId, JSONObject payItemInfo) {
		String billno = params.get("transaction_id");
		String appmeta = params.get("meta_data");
		if (player == null) {
			logger.error("notify player not exist, billno: {}, appmeta: {}, goodsId: {}", billno, appmeta, goodsId);
			return false;
		}
		
		if (payItemInfo.getIntValue("complete") > 0) {
			logger.error("notify of payItemInfo repeated, openid: {}, playerId: {}, billno: {}", player.getOpenId(), player.getId(), billno);
			return false;
		}
		
		// 道具ID不对
		if (HawkOSOperator.isEmptyString(goodsId)) {
			logger.error("notify goodsId is null, billno: {}, playerId: {}, goodsId: {}", billno, player.getId(), goodsId);
			return false;
		}
		
		// 订单判断：GMServer在收到米大师回调请求，向GameServer转发之前，会先把订单相关信息保存下来，留待此处核对校验
		JSONObject orderJson = RedisProxy.getInstance().getCallBackOrderInfo(player.getOpenId(), billno);
		if (orderJson == null) {
			logger.error("notify order info not exist, billno: {}, puid: {},  goodsId: {}", billno, player.getPuid(), goodsId);
			return false;
		}

		// 商品判断
		PayGiftCfg payGiftCfg = HawkConfigManager.getInstance().getConfigByKey(PayGiftCfg.class, goodsId);
		if (payGiftCfg == null) {
			logger.error("notify paygift config not exist, billno: {}, playerId: {}, goodsId: {}", billno, player.getId(), goodsId);
			return false;
		}
		
		ServerAwardCfg cfg = AssembleDataManager.getInstance().getServerAwardByAwardId(payGiftCfg.getServerAwardId());
		if (payGiftCfg.getServerAwardId() != 0 && cfg == null) {
			logger.error("notify paygift award not exist, billno: {}, playerId: {}, goodsId: {}, serverAwardId: {}", billno, player.getId(), goodsId, payGiftCfg.getServerAwardId());
			return false;
		}
		return true;
	}
	
}
