package com.hawk.game.invoker;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;
import org.hawk.os.HawkOSOperator;

import com.hawk.game.player.Player;
import com.hawk.game.protocol.Status;
import com.hawk.game.service.college.CollegeService;

/**
 * 申请加入军事学院
 * 
 * @author Jesse
 *
 */
public class CollegeFastJoinInvoker extends HawkMsgInvoker {
	/** 玩家 */
	private Player player;
	/** 协议Id */
	private int hpCode;

	private String collegeId;
	public CollegeFastJoinInvoker(Player player, String collegeId,int hpCode) {
		this.player = player;
		this.hpCode = hpCode;
		this.collegeId = collegeId;
	}

	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
		int result = 0;
		if(HawkOSOperator.isEmptyString(this.collegeId)){
			result = CollegeService.getInstance().joinCollegeFast(player, this.hpCode);
		}else{
			result = CollegeService.getInstance().joinCollegeFast(player, this.collegeId,this.hpCode);
		}
		if (result != Status.SysError.SUCCESS_OK_VALUE) {
			player.sendError(hpCode, result, 0);
		}
		return true;
	}

	public Player getPlayer() {
		return player;
	}


	public int getHpCode() {
		return hpCode;
	}
	
}
