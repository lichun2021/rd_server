package com.hawk.game.invoker;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;
import com.hawk.game.player.Player;
import com.hawk.game.service.MissionService;
import com.hawk.game.util.GsConst.MissionFunType;

public class FunMissionRefreshInvoker extends HawkMsgInvoker {
	
	private Player player;
	private MissionFunType funType;
	private int funId;
	private int funVal;
	
	public FunMissionRefreshInvoker(Player player, MissionFunType funType, int funId, int funVal) {
		this.player = player;
		this.funType = funType;
		this.funId = funId;
		this.funVal = funVal;
	}
	
	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
		MissionService.getInstance().missionRefreshAsync(player, funType, funId, funVal);
		return true;
	}

	public Player getPlayer() {
		return player;
	}

	public MissionFunType getFunType() {
		return funType;
	}

	public int getFunId() {
		return funId;
	}

	public int getFunVal() {
		return funVal;
	}
	
}
