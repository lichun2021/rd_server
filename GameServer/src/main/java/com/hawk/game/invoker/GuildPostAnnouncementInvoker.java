package com.hawk.game.invoker;

import org.hawk.app.HawkApp;
import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;

import com.hawk.game.global.RedisProxy;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.service.GuildService;
import com.hawk.game.util.LogUtil;
import com.hawk.game.util.GsConst.ChangeContentType;
import com.hawk.log.LogConst.LogMsgType;
/**
 * 发表联盟宣言
 * @author Jesse
 *
 */
public class GuildPostAnnouncementInvoker extends HawkMsgInvoker {
	/** 玩家 */
	private Player player;

	/** 宣言内容 */
	private String ann;

	/** 协议Id */
	private int hpCode;
	/**
	 * 清除CD时间
	 */
	private boolean clearCDTime;

	public GuildPostAnnouncementInvoker(Player player, String ann, int hpCode, boolean clearCDTime) {
		this.player = player;
		this.ann = ann;
		this.hpCode = hpCode;
		this.clearCDTime = clearCDTime;
	}

	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
		int operationResult = GuildService.getInstance().onPostGuildAnnouncement(ann, player.getGuildId());
		if (operationResult == Status.SysError.SUCCESS_OK_VALUE) {
			RedisProxy.getInstance().updateChangeContentTime(player.getGuildId(), ChangeContentType.CHANGE_GUILD_ANNOUNCE, HawkApp.getInstance().getCurrentTime());
			if (clearCDTime) {
				RedisProxy.getInstance().removeChangeContentCDTime(player.getGuildId(), ChangeContentType.CHANGE_GUILD_ANNOUNCE);
			}
			
			LogUtil.logSecTalkFlow(player, null, LogMsgType.GUILD_ANNOUNCEMENT_CHANGE, "", ann);
			player.responseSuccess(HP.code.GUILDMANAGER_POSTANNOUNCEMENT_C_VALUE);
			return true;
		}
		player.sendError(hpCode, operationResult, 0);
		return true;
	}

	public Player getPlayer() {
		return player;
	}

	public String getAnn() {
		return ann;
	}

	public int getHpCode() {
		return hpCode;
	}
	
}
