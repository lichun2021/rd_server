package com.hawk.game.script;

import java.util.Map;
import java.util.Optional;

import org.hawk.app.HawkApp;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.DiamondRechargeEvent;
import com.hawk.activity.event.impl.FirstRechargeEvent;
import com.hawk.activity.event.impl.RechargeMoneyEvent;
import com.hawk.game.config.PayCfg;
import com.hawk.game.config.PayGiftCfg;
import com.hawk.game.entity.RechargeEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.msg.PlayerRechargeGrantItemMsg;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Script.ScriptError;
import com.hawk.game.recharge.RechargeManager;
import com.hawk.game.util.GsConst;
import com.hawk.game.recharge.RechargeType;

/**
 * 模拟充值回调通知入口
 * 
 * localhost:8080/script/gmRecharge?playerName=?&rechargeType=?&goodsId=?
 * 
 * recharteType 1：充值 2：礼包
 * goodsId 充值：pay.xml的key 礼包：payGift.xml的key
 * 
 * @author hawk
 */
public class GmRechargeHandler extends HawkScript {
	static Logger logger = LoggerFactory.getLogger("Recharge");

	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		Player player = GlobalData.getInstance().scriptMakesurePlayer(params);
		if (player == null) {
			return HawkScript.failedResponse(ScriptError.PARAMS_ERROR_VALUE, "player not found");
		}

		// 1 充值 2礼包
		int rechargeType = Integer.parseInt(params.get("rechargeType"));
		// 物品id
		String goodsId = params.get("goodsId");
		
		// 初始化返回json对象
		JSONObject result = new JSONObject();
		
		if (rechargeType == RechargeType.RECHARGE) {
			gmRecharge(player, goodsId, result);
			
		} else if (rechargeType == RechargeType.GIFT) {
			gmBuyGift(player, goodsId, result);
			
		} else {
			result.put("result", "rechargeType error!");
		}
		
		return result.toJSONString();
	}
	
	/**
	 * 充值
	 * @param player
	 * @param goodsId
	 */
	public void gmRecharge(Player player, String goodsId, JSONObject result) {
		Optional<RechargeEntity> optional = player.getData().getPlayerRechargeEntities().parallelStream().filter(e -> e.getType() == RechargeType.RECHARGE).findAny();
		// 抛首充事件
		if (!optional.isPresent()) {
			ActivityManager.getInstance().postEvent(new FirstRechargeEvent(player.getId()));
		}
		
		// 创建订单
		PayCfg payCfg = HawkConfigManager.getInstance().getConfigByKey(PayCfg.class, goodsId);
		if (payCfg == null) {
			result.put("result", "payCfg is null!");
			return;
		}
		
		// 订单id
		String orderId = HawkOSOperator.randomUUID();
		
		RechargeManager.getInstance().createRechargeRecord(player, orderId, "",
				payCfg.getId(), (int)(payCfg.getPayRMB() * GsConst.RECHARGE_BASE), payCfg.getPayRMB(),
				"RMB", RechargeType.RECHARGE, HawkTime.getSeconds(), payCfg.getGainDia());
		player.getPlayerBaseEntity().setSaveAmt(1);
		player.getPush().syncHasFirstRecharge();
		//测试代码，仅供测试充值成就
		ActivityManager.getInstance().postEvent(new DiamondRechargeEvent(player.getId(), goodsId, payCfg.getGainDia()));
		//测试代码，仅测试充值豪礼成就
		ActivityManager.getInstance().postEvent(new RechargeMoneyEvent(player.getId(), payCfg.getGainDia()));
		result.put("result", "recharge success!");
	}
	
	/**
	 * 礼包
	 * @param player
	 * @param goodsId
	 */
	public void gmBuyGift(Player player, String goodsId, JSONObject result) {
		PayGiftCfg payGiftCfg = HawkConfigManager.getInstance().getConfigByKey(PayGiftCfg.class, goodsId);
		if (payGiftCfg == null) {
			result.put("result", "payGiftCfg is null!");
			return;
		}
		
		// 构造参数
		String billno = HawkOSOperator.randomUUID();
		int payMoney = payGiftCfg.getPayRMB() / 10;
		
		// 创建订单
		RechargeManager.getInstance().createRechargeRecord(player, billno, "", goodsId, 
				payMoney, payMoney * 10, "RMB", RechargeType.GIFT, HawkTime.getSeconds(), payGiftCfg.getGainDia());
		
		// 发货通知
		HawkApp.getInstance().postMsg(player.getXid(), PlayerRechargeGrantItemMsg.valueOf(payGiftCfg, billno, payMoney * 10));
		result.put("result", "buy gift success!");
	}
}
