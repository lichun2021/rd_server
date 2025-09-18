package com.hawk.game.invoker;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;

import com.hawk.game.log.BehaviorLogger;
import com.hawk.game.log.BehaviorLogger.Params;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Status;
import com.hawk.game.service.GuildService;
import com.hawk.game.util.LogUtil;
import com.hawk.log.Action;
import com.hawk.log.LogConst.LogMsgType;
import com.hawk.log.Source;
/**
 * 修改联盟阶级称谓
 * @author Jesse
 *
 */
public class GuildChangeLvlNameInvoker extends HawkMsgInvoker {
	/** 玩家 */
	private Player player;

	/** 阶级称谓 */
	private String[] names;

	/** 日志信息 */
	private String logInfo;

	/** 协议Id */
	private int hpCode;

	public GuildChangeLvlNameInvoker(Player player, String[] names, String logInfo, int hpCode) {
		this.player = player;
		this.names = names;
		this.logInfo = logInfo;
		this.hpCode = hpCode;
	}
	
	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
		int result = GuildService.getInstance().onChangeLevelName(player.getGuildId(), names);
		BehaviorLogger.log4Service(player, Source.GUILD_OPRATION, Action.GUILD_CHANGELEVELNAME, Params.valueOf("guildId", player.getGuildId()));
		if (result != Status.SysError.SUCCESS_OK_VALUE) {
			player.sendError(hpCode, result, 0);
			return false;
		}

		LogUtil.logSecTalkFlow(player, null, LogMsgType.GUILD_LEVEL_NAME_CHANGE, "", logInfo);
		player.responseSuccess(hpCode);
		return true;
	}

	public Player getPlayer() {
		return player;
	}

	public String[] getNames() {
		return names;
	}

	public String getLogInfo() {
		return logInfo;
	}

	public int getHpCode() {
		return hpCode;
	}
	
}
