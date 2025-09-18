package com.hawk.game.invoker;

import java.util.Map;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.HawkRpcMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;
import org.hawk.msg.invoker.HawkRpcInvoker;

import com.hawk.game.player.Player;
import com.hawk.game.protocol.MilitaryCollege.CollegeAuth;
import com.hawk.game.service.college.CollegeService;
import com.hawk.gamelib.GameConst.MsgId;

/**
 * 修改联盟名称
 * 
 * @author Jesse
 *
 */
public class CollegeSetCoahRpcInvoker  extends HawkMsgInvoker  {
	/** 主动转让教官玩家 */
	private Player player;

	/** 被转让教官玩家 */
	private Player targetMember;
	
	/** 协议Id */
	private int hpCode;
	
	
	
	public CollegeSetCoahRpcInvoker(Player player, Player member, int hpCode) {
		this.player = player;
		this.targetMember = member;
		this.hpCode = hpCode;
	}
	
	

	@Override
	public boolean onMessage(HawkAppObj arg0, HawkMsg arg1) {
		int setRlt = CollegeService.getInstance().setCollegecoach(player, this.targetMember);
		if(setRlt == 0){
			player.rpcCall(MsgId.COLLEGE_AUTH_CHANGE,this.targetMember,
					new CollegeCoahAuthChangeMsg(player,this.targetMember,this.hpCode));
		}else{
			player.sendError(this.hpCode, setRlt, 0);
		}
		return false;
	}

	


	public Player getPlayer() {
		return player;
	}
	public Player getTargetMember() {
		return targetMember;
	}
	public int getHpCode() {
		return hpCode;
	}

	
	


	
}
