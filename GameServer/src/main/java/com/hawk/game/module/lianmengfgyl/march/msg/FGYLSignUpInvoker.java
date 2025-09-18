package com.hawk.game.module.lianmengfgyl.march.msg;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;

import com.hawk.game.module.lianmengfgyl.march.service.FGYLMatchService;
import com.hawk.game.player.Player;

/**
 * 解散军事学院
 * 
 * @author Jesse
 *
 */
public class FGYLSignUpInvoker extends HawkMsgInvoker {
	/** 玩家 */
	private Player player;

	/** 协议Id */
	private int hpCode;
	
	private int level;
	
	private int timeIndex;
	

	public FGYLSignUpInvoker(Player player, int hpCode,int level,int timeIndex) {
		this.player = player;
		this.hpCode = hpCode;
		this.level = level;
		this.timeIndex = timeIndex;
	}

	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
		FGYLMatchService.getInstance().signupWar(this.hpCode, player, level, timeIndex);
		return true;
	}

	public Player getPlayer() {
		return player;
	}

	public int getHpCode() {
		return hpCode;
	}
	
}
