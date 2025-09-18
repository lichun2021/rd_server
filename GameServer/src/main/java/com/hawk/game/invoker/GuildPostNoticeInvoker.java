package com.hawk.game.invoker;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;

import com.hawk.game.player.Player;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.service.GuildService;
import com.hawk.game.util.LogUtil;
import com.hawk.log.LogConst.LogMsgType;
/**
 * 发表联盟公告
 * @author Jesse
 *
 */
public class GuildPostNoticeInvoker extends HawkMsgInvoker {
	/** 玩家 */
	private Player player;
	
	/** 公告内容 */
	private String notice;
	
	/** 协议Id */
	private int hpCode;

	public GuildPostNoticeInvoker(Player player, String notice, int hpCode) {
		this.player = player;
		this.notice = notice;
		this.hpCode = hpCode;
	}

	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
		int operationResult = GuildService.getInstance().onPostGuildNotice(notice, player.getGuildId());
		if (operationResult == Status.SysError.SUCCESS_OK_VALUE) {
			LogUtil.logSecTalkFlow(player, null, LogMsgType.GUILD_NOTICE_CHANGE, "", notice);
			player.responseSuccess(HP.code.GUILDMANAGER_POSTNOTICE_C_VALUE);
			return true;
		}
		player.sendError(hpCode, operationResult, 0);
		return true;
	}

	public Player getPlayer() {
		return player;
	}

	public String getNotice() {
		return notice;
	}

	public int getHpCode() {
		return hpCode;
	}
	
}
