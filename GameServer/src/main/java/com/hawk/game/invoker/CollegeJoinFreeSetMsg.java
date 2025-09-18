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
public class CollegeJoinFreeSetMsg extends HawkMsgInvoker {

	private Player player;
	
	private int type;
	
	private int hp;
	
	public CollegeJoinFreeSetMsg(Player player,int type,int hp) {
		this.player = player;
		this.type = type;
		this.hp = hp;
	}

	
	@Override
	public boolean onMessage(HawkAppObj arg0, HawkMsg arg1) {
		CollegeService.getInstance().setCollegeJoinFree(player, type);
		CollegeService.getInstance().syncCollegeBaseInfo(player);
		return true;
	}
	
	

	public int getHp() {
		return hp;
	}
	
}
