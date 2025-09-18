package com.hawk.game.invoker;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;

import com.hawk.game.player.Player;
import com.hawk.game.service.college.CollegeService;

/**
 * 修改联盟名称
 * 
 * @author Jesse
 *
 */
public class CollegeCoachAutoChangeCoahMsg extends HawkMsgInvoker {
	private Player player;

	
	public CollegeCoachAutoChangeCoahMsg(Player player) {
		this.player = player;
	}

	
	@Override
	public boolean onMessage(HawkAppObj arg0, HawkMsg arg1) {
		CollegeService.getInstance().checkCoahChange(player);
		return true;
	}
	
	public Player getPlayer() {
		return player;
	}


	
}
