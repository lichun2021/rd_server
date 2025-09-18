package com.hawk.game.script;

import java.util.Calendar;
import java.util.Map;

import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.hawk.game.entity.PlayerEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Script.ScriptError;
import com.hawk.game.protocol.Status;

/**
 * 玩家禁言
 * 
 * localhost:8080/script/silent?playerName[playerId]=
 * 
 * playerId: 玩家Id
 * playerName: 玩家名字
 *
 * @author david
 */
public class MakePlayerSilentHandler extends HawkScript {
	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		try {

			Player player = GlobalData.getInstance().scriptMakesurePlayer(params);
			if (player == null) {
				return HawkScript.failedResponse(ScriptError.ACCOUNT_NOT_EXIST_VALUE, params.toString());
			}

			// 封号
			Calendar calendar = HawkTime.getCalendar(true);
			calendar.add(Calendar.YEAR, 1);
			PlayerEntity entity = player.getEntity();
			entity.setForbidenTime(calendar.getTimeInMillis());
			GlobalData.getInstance().updateAccountInfo(entity.getPuid(), entity.getServerId(), entity.getId(), entity.getForbidenTime(), entity.getName());

			// 踢下线
			if (player != null) {
				player.kickout(Status.SysError.PLAYER_KICKOUT_VALUE, true, null);
			}

			return HawkScript.successResponse(null);
		} catch (Exception e) {
			HawkException.catchException(e);
			return HawkException.formatStackMsg(e);
		}
	}
}
