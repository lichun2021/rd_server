package com.hawk.game.invoker;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;

import com.hawk.game.log.BehaviorLogger;
import com.hawk.game.log.BehaviorLogger.Params;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.service.GuildService;
import com.hawk.log.Action;
import com.hawk.log.Source;
/**
 * 修改联盟公开招募
 * @author Jesse
 *
 */
public class ChangeGuildApplyPermition extends HawkMsgInvoker {
	
	private Player player;
	private boolean isOpen;
	private int buildingLvl;
	private int power;
	private int commonderLvl;
	private String lang;
	private int hpCode;
	
	public ChangeGuildApplyPermition(Player player, boolean isOpen, int buildingLvl, int power, int commonderLvl, String lang, int hpCode) {
		this.player = player;
		this.isOpen = isOpen;
		this.buildingLvl = buildingLvl;
		this.power = power;
		this.commonderLvl = commonderLvl;
		this.lang = lang;
		this.hpCode = hpCode;
	}

	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
		int operationResult = GuildService.getInstance().onChangeGuildApplyPermition(player.getGuildId(), isOpen, buildingLvl, power, commonderLvl, lang);
		if (operationResult == Status.SysError.SUCCESS_OK_VALUE) {
			BehaviorLogger.log4Service(player, Source.GUILD_OPRATION, Action.GUILD_CHANGEAPPLY, Params.valueOf("guildId", player.getGuildId()),
					Params.valueOf("openApply", isOpen));
			player.responseSuccess(HP.code.GUILDMANAGER_CHANGEAPPLYPERMITON_C_VALUE);
			return true;
		}
		player.sendError(hpCode, operationResult, 0);
		return true;
	}

	public Player getPlayer() {
		return player;
	}

	public boolean isOpen() {
		return isOpen;
	}

	public int getBuildingLvl() {
		return buildingLvl;
	}

	public int getPower() {
		return power;
	}

	public int getCommonderLvl() {
		return commonderLvl;
	}

	public String getLang() {
		return lang;
	}

	public int getHpCode() {
		return hpCode;
	}
	
}
