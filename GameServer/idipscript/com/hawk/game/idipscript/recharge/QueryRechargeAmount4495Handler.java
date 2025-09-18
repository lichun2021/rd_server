package com.hawk.game.idipscript.recharge;

import java.util.List;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.config.PayGiftCfg;
import com.hawk.game.data.PlatTransferInfo;
import com.hawk.game.data.RechargeInfo;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.player.Player;
import com.hawk.game.recharge.RechargeType;
import com.hawk.idip.IdipConst;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;
import com.hawk.log.LogConst.Platform;

/**
 * 查询玩家充值及直购总金额请求  -- 10282173
 *
 * localhost:8080/script/idip/4495
 * 
 * @param BeginTime 开始时间
 * @param EndTime 结束时间
 * @param Type 类型：0（充值+直购）、1（充值）、2（直购）
 * 
 * @author lating
 */
@HawkScript.Declare(id = "idip/4495")
public class QueryRechargeAmount4495Handler extends IdipScriptHandler {
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.playerCheck(request, result);
		if (player == null) {
			return result;
		}
		
		int beginTime = request.getJSONObject("body").getIntValue("BeginTime");
		int endTime = request.getJSONObject("body").getIntValue("EndTime");
		int rechargeType = request.getJSONObject("body").getIntValue("Type");
		int diamonds = 0;
		try {
			diamonds = getPlayerRechargeAmount(player, rechargeType, beginTime, endTime);
		}catch (Exception e) {
			HawkException.catchException(e);
			result.getBody().put("Result", IdipConst.SysError.API_EXCEPTION);
			result.getBody().put("RetMsg", "server exception");
			result.getBody().put("Amount", 0);
			return result;
		}
		
		result.getBody().put("Amount", diamonds);
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		return result;
	}
	
	/**
	 * 查询充值总金条数
	 * @param player
	 * @param rechargeType 类型：0（充值+直购）、1（充值）、2（直购）
	 * @return
	 */
	@SuppressWarnings("deprecation")
	private int getPlayerRechargeAmount(Player player, int rechargeType, int beginTime, int endTime) {
		String playerId = player.getId();
		String openid = player.getOpenId();
		List<RechargeInfo> rechargeInfos = RedisProxy.getInstance().getAllRechargeInfoByOpenid(openid);
		
		int sumGold = 0;
		String tarServer = player.getServerId();
		String sourceServer = GlobalData.getInstance().getImmgrationSource(playerId, tarServer);
		String platform = PlatTransferInfo.getSourcePlatform(playerId, player.getPlatform());
		for(RechargeInfo info : rechargeInfos){
			if (rechargeType != 0 && rechargeType != info.getType()) {
				continue;
			}
			int rechargeTime = info.getTime();
			if (rechargeTime < beginTime || rechargeTime > endTime) {
				continue;
			}
			
			int count = info.getCount();
			if (info.getType() == RechargeType.GIFT) {
				PayGiftCfg cfg = HawkConfigManager.getInstance().getConfigByKey(PayGiftCfg.class, info.getGoodsId());
				count = cfg != null ? cfg.getPayRMB()/10 : count;
			}
			
			// rechargeInfo中的playerId字段是在2024年3月份才添加的，之前没有playerId信息只能通过serverId+platform来匹配，后面就可以直接通过playerId匹配了
			if (!HawkOSOperator.isEmptyString(info.getPlayerId()) && info.getPlayerId().equals(playerId)) {
				sumGold += count;
				continue;
			}
			
			String rechargeServer = info.getServer();
			String rechargePlatform = Platform.valueOf(info.getPlatId()).strLowerCase();
			if (!rechargePlatform.equals(platform)) {
				continue;
			}
			
			if (rechargeServer.equals(tarServer) || rechargeServer.equals(sourceServer)) {
				sumGold += count;
			}
		}
		
		return sumGold;
	}
	
}
