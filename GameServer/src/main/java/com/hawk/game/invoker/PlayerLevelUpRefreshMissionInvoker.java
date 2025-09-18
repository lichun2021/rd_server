package com.hawk.game.invoker;

import java.util.List;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;

import com.hawk.game.config.MissionCfg;
import com.hawk.game.entity.MissionEntity;
import com.hawk.game.player.Player;
import com.hawk.game.service.MissionService;

public class PlayerLevelUpRefreshMissionInvoker extends HawkMsgInvoker {
	
	private Player player;
	
	public PlayerLevelUpRefreshMissionInvoker(Player player) {
		this.player = player;
	}
	
	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
		List<MissionEntity> newOpenMissions = MissionService.getInstance().openMission(player, MissionCfg.getAfterMissionsByPlayerLv(player.getLevel()));
		// 推送新建出来的已开启任务
		MissionService.getInstance().syncNewOpenedMissions(player, newOpenMissions);
		
		return true;
	}

	public Player getPlayer() {
		return player;
	}
	
}
