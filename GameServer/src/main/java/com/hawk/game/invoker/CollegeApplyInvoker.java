package com.hawk.game.invoker;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;

import com.hawk.game.player.Player;
import com.hawk.game.protocol.Status;
import com.hawk.game.service.college.CollegeService;

/**
 * 申请加入军事学院
 * 
 * @author Jesse
 *
 */
public class CollegeApplyInvoker extends HawkMsgInvoker {
	/** 玩家 */
	private Player player;
	
	/** 学院id*/
	private String collegeId;

	/** 协议Id */
	private int hpCode;

	public CollegeApplyInvoker(Player player, String collegeId, int hpCode) {
		this.player = player;
		this.collegeId = collegeId;
		this.hpCode = hpCode;
	}

	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
		int result = CollegeService.getInstance().applyCollege(player, collegeId);
		if (result == Status.SysError.SUCCESS_OK_VALUE) {
			player.responseSuccess(hpCode);
		} else {
			player.sendError(hpCode, result, 0);
		}
		return true;
	}

	public Player getPlayer() {
		return player;
	}

	public String getCollegeId() {
		return collegeId;
	}

	public int getHpCode() {
		return hpCode;
	}
	
}
