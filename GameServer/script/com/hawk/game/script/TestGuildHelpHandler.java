package com.hawk.game.script;

import java.util.Collection;
import java.util.Map;

import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.hawk.game.global.GlobalData;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Script.ScriptError;
import com.hawk.game.service.GuildService;

/**
 * 联盟成员一键帮助
 * localhost:8080/script/guildHelp?playerName=l0001&period=3000
 * playerName: 玩家名字
 * 
 * @author Jesse
 *
 */
public class TestGuildHelpHandler extends HawkScript {
	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		try {
			Player player = GlobalData.getInstance().scriptMakesurePlayer(params);
			long period = Long.valueOf(params.get("period"));
			if (player == null) {
				return HawkScript.failedResponse(ScriptError.ACCOUNT_NOT_EXIST_VALUE, params.toString());
			}
			String guildId = player.getGuildId();
			if (guildId == null) {
				return successResponse("no guild!");
			}
			Collection<String> playerIds = GuildService.getInstance().getGuildMembers(guildId);
			StringBuilder sb = new StringBuilder();
			long time = HawkTime.getMillisecond();
			for (String playerId : playerIds) {
				Thread.sleep(period);
				Player member = GlobalData.getInstance().scriptMakesurePlayer(playerId);
				GuildService.getInstance().helpAllGuildQueues(guildId, member);
				sb.append(HawkTime.getMillisecond() - time).append(",");
			}
			return successResponse("help success!costTime: "+ sb.toString());

		} catch (Exception e) {
			HawkException.catchException(e);
		}

		return HawkScript.failedResponse(ScriptError.EXCEPTION_VALUE, "");
	}
}
