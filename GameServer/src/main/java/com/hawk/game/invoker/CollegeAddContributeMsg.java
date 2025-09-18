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
public class CollegeAddContributeMsg extends HawkMsgInvoker {

	private String collegeId;
	
	private int exp;
	
	private int score;
	
	private int vit;
	
	public CollegeAddContributeMsg(String collegeId,int exp ,int score,int vit) {
		this.collegeId = collegeId;
		this.exp = exp;
		this.score = score;
		this.vit = vit;
	}

	
	@Override
	public boolean onMessage(HawkAppObj arg0, HawkMsg arg1) {
		if(this.exp > 0){
			CollegeService.getInstance().addCollegeExp(this.collegeId, exp);
		}
		if(this.score > 0){
			CollegeService.getInstance().addWeekScore(collegeId, score);
		}
		if(this.vit > 0){
			CollegeService.getInstance().addVit(collegeId, vit);
		}
		return true;
	}
	
	


	
}
