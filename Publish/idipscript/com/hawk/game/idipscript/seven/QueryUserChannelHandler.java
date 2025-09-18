package com.hawk.game.idipscript.seven;

import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.GsConfig;
import com.hawk.game.global.GlobalData;
import com.hawk.game.player.Player;
import com.hawk.game.queryentity.AccountInfo;
import com.hawk.game.util.GameUtil;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;
import com.hawk.log.LogConst.Platform;

/**
 * 查询指定时间范围内对应openid所创建角色的渠道号
 *
 * localhost:8080/script/idip/4301
 *
 * @param BeginTime
 * @param EndTime  
 * @param OpenId
 * 
 * @author jesse
 */
@HawkScript.Declare(id = "idip/4301")
public class QueryUserChannelHandler extends IdipScriptHandler {
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		
		int channelId = 0;
		try {
			long startTime = request.getJSONObject("body").getLongValue("BeginTime") * 1000;
			long endTime = request.getJSONObject("body").getLongValue("EndTime") * 1000;
			
			int platId = request.getJSONObject("body").getIntValue("PlatId");
			String platform = platId == Platform.ANDROID.intVal() ? Platform.ANDROID.strLowerCase() : Platform.IOS.strLowerCase();
			String puid = request.getJSONObject("body").getString("OpenId");
			puid = GameUtil.getPuidByPlatform(puid, platform);
			String serverId = request.getJSONObject("body").getString("Partition");
			if (HawkOSOperator.isEmptyString(serverId) || serverId.equals("0")) {
				serverId = GsConfig.getInstance().getServerId();
			}
			
			AccountInfo accountInfo = GlobalData.getInstance().getAccountInfo(puid, serverId);
			Player player = GlobalData.getInstance().makesurePlayer(accountInfo.getPlayerId());
			if (player != null && player.getCreateTime() >= startTime && player.getCreateTime() <= endTime) {
				channelId = Integer.valueOf(player.getEntity().getChannelId());
			}
			
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		result.getBody().put("Channel", channelId);
		return result;
	}
}
