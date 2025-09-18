package com.hawk.game.script;

import java.util.Calendar;
import java.util.Map;

import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.hawk.common.IDIPBanInfo;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Script.ScriptError;
import com.hawk.game.util.GsConst.IDIPBanType;

/**
 * 获取玩家数据
 *
 * localhost:8080/script/forbiden?playerName[playerId]=
 *
 * playerId: 玩家Id
 * playerName: 玩家名字
 *
 * @author david
 */
public class ForbidenPlayerHandler extends HawkScript {
	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		try {
			Player player = GlobalData.getInstance().scriptMakesurePlayer(params);
			if (player == null) {
				return HawkScript.failedResponse(ScriptError.PARAMS_ERROR_VALUE, "player not found");
			}

			boolean forbiden = true;
			if (params.containsKey("forbiden")) {
				forbiden = "true".equals(params.get("forbiden"));
			}

			// 封号
			if (forbiden) {
				Calendar calendar = HawkTime.getCalendar(true);
				calendar.add(Calendar.YEAR, 1);
				player.getEntity().setForbidenTime(calendar.getTimeInMillis());

				long nowTime = HawkTime.getMillisecond();
				long endTime = calendar.getTimeInMillis();
				IDIPBanInfo banInfo = new IDIPBanInfo(player.getId(), "账号已经被封禁！", nowTime, endTime, (int)(endTime - nowTime)/1000);
				
				// 更新缓存状态
				GlobalData.getInstance().updateAccountInfo(player.getPuid(), player.getServerId(), player.getId(), calendar.getTimeInMillis(), player.getName());
				RedisProxy.getInstance().addIDIPBanInfo(player.getId(), banInfo, IDIPBanType.BAN_ACCOUNT);
				
				// 在线玩家即踢下线
				if (player.isActiveOnline()) {
					player.kickout(Status.SysError.PLAYER_KICKOUT_VALUE, true, null);
				}
			} else {
				player.getEntity().setForbidenTime(0);

				// 更新缓存状态
				GlobalData.getInstance().updateAccountInfo(player.getPuid(), player.getServerId(), player.getId(), 0, player.getName());
			}

			return HawkScript.successResponse("ok");
		} catch (Exception e) {
			HawkException.catchException(e);
			return HawkException.formatStackMsg(e);
		}
	}
}
