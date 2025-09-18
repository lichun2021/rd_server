package com.hawk.game.invoker;

import java.util.List;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;

import com.hawk.game.player.Player;
import com.hawk.game.protocol.Status;
import com.hawk.game.service.college.CollegeService;

/**
 * 踢出军事学院成员
 * 
 * @author Jesse
 *
 */
public class CollegeKickInvoker extends HawkMsgInvoker {
	/** 玩家 */
	private Player player;

	private List<String> targetIdList;

	/** 协议Id */
	private int hpCode;

	public CollegeKickInvoker(Player player, List<String> targetIdList, int hpCode) {
		this.player = player;
		this.targetIdList = targetIdList;
		this.hpCode = hpCode;
	}

	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
		int result = CollegeService.getInstance().kickMember(player, targetIdList);
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

	public List<String> getTargetIdList() {
		return targetIdList;
	}

	public int getHpCode() {
		return hpCode;
	}

}
