package com.hawk.game.invoker;

import java.util.Map;

import org.hawk.app.HawkAppObj;
import org.hawk.log.HawkLog;
import org.hawk.msg.HawkRpcMsg;
import org.hawk.msg.invoker.HawkRpcInvoker;

import com.hawk.game.player.Player;
import com.hawk.game.protocol.MilitaryCollege.CollegeAuth;
import com.hawk.game.service.college.CollegeService;

/**
 * 修改联盟名称
 * 
 * @author Jesse
 *
 */
public class CollegeCoahAuthChangeMsg extends HawkRpcInvoker {
	private Player from;
	private Player to;
	private int hp;
	
	public CollegeCoahAuthChangeMsg(Player from,Player to,int hp) {
		this.from = from;
		this.to = to;
		this.hp = hp;
	}

	@Override
	public boolean onMessage(HawkAppObj arg0, HawkRpcMsg arg1, Map<String, Object> arg2) {
		this.to.getData().getCollegeMemberEntity().setAuth(CollegeAuth.COACH_VALUE);
		HawkLog.logPrintln("CollegeCoahAuthChangeMsg set targetMember to coach, playerId: {},frontId: {}", 
				to.getId(),from.getId());
		return false;
	}

	@Override
	public boolean onComplete(HawkAppObj arg0, Map<String, Object> arg1) {
		this.from.getData().getCollegeMemberEntity().setAuth(CollegeAuth.TRAINEE_VALUE);
		HawkLog.logPrintln("CollegeCoahAuthChangeMsg set front to trainee, playerId: {},newId: {}", 
				from.getId(),to.getId());
		if(this.from.isActiveOnline()){
			CollegeService.getInstance().syncCollegeInfo(this.from);
		}
		if(this.to.isActiveOnline()){
			CollegeService.getInstance().syncCollegeInfo(this.to);
		}
		if(this.hp != 0){
			from.responseSuccess(this.hp);
		}
		return false;
	}

	
	

	
	
}
