package com.hawk.game.invoker;

import java.util.HashMap;
import java.util.Map;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.game.player.Player;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MilitaryCollege.CollegeVitalitySend;
import com.hawk.game.protocol.MilitaryCollege.CollegeVitalitySendReq;
import com.hawk.game.protocol.MilitaryCollege.CollegeVitalitySendResp;
import com.hawk.game.service.college.CollegeService;

/**
 * 修改联盟名称
 * 
 * @author Jesse
 *
 */
public class CollegeVitSendMsg extends HawkMsgInvoker {

	private Player player;
	
	private CollegeVitalitySendReq req;
	
	
	
	public CollegeVitSendMsg(Player player,CollegeVitalitySendReq req) {
		this.player = player;
		this.req = req;
	}

	
	@Override
	public boolean onMessage(HawkAppObj arg0, HawkMsg arg1) {
		Map<String,Integer> map = new HashMap<>();
		for(CollegeVitalitySend send : req.getSendsList()){
			map.put(send.getMemberId(), send.getVitality());
		}
		int rlt = CollegeService.getInstance().vitalitySend(player, map);
		if(rlt == 0){
			CollegeVitalitySendResp.Builder resp = CollegeVitalitySendResp.newBuilder();
			resp.setVitality(CollegeService.getInstance().genVitBuilder(player));
			this.player.sendProtocol(HawkProtocol.valueOf(HP.code2.COLLEGE_VITALITY_INFO_RESP_S_VALUE, resp));
			this.player.responseSuccess(HP.code2.COLLEGE_VITALITY_SEND_REQ_C_VALUE);
		}else{
			this.player.sendError(HP.code2.COLLEGE_VITALITY_SEND_REQ_C_VALUE, rlt,0);
		}
		return true;
	}
	
	


	
}
