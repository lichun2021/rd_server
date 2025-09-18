package com.hawk.game.invoker;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;

import com.hawk.game.entity.GuildMemberObject;
import com.hawk.game.service.GuildService;
/**
 * 刷新联盟成员战力
 * @author Jesse
 *
 */
public class GuildMemberRefreshPowerInvoker extends HawkMsgInvoker {
	/** 玩家id*/
	private String playerId;
	
	/** 玩家战力*/
	private long power;
	private long noArmyPower;
	
	public GuildMemberRefreshPowerInvoker(String playerId, long power, long noArmyPower) {
		this.playerId = playerId;
		this.power = power;
		this.noArmyPower = noArmyPower;
	}


	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
		GuildMemberObject member = GuildService.getInstance().getGuildMemberObject(playerId);
		if (member != null) {
			member.updateMemberPower(power);
			member.updateMemberNoArmyPower(noArmyPower);
		}
		return true;
	}
}
