package com.hawk.game.invoker;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkRpcMsg;
import org.hawk.msg.invoker.HawkRpcInvoker;

import com.hawk.game.global.GlobalData;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Status;
import com.hawk.game.service.college.CollegeService;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.log.Action;

/**
 * 修改联盟名称
 * 
 * @author Jesse
 *
 */
public class CollegeChangeNameRpcInvoker extends HawkRpcInvoker {
	/** 玩家 */
	private Player player;
	/** 联盟名称 */
	private String collegeName;
	/** 协议Id */
	private int hpCode;
	
	private int rlt;
	
	private ConsumeItems reNameConsume;

	public CollegeChangeNameRpcInvoker(Player player, String collegeName,int hpCode) {
		this.player = player;
		this.collegeName = collegeName;
		this.hpCode = hpCode;
	}

	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkRpcMsg msg, Map<String, Object> result) {
		int checkRlt = CollegeService.getInstance().checkCollegeName(collegeName);
		if(checkRlt != 0){
			player.sendError(hpCode, checkRlt,0);
			return true;
		}
		ItemInfo cost = CollegeService.getInstance().getReNameCost(player.getCollegeId());
		if(Objects.nonNull(cost)){
			ConsumeItems consume = ConsumeItems.valueOf();
			consume.addConsumeInfo(cost, false);
			if(!consume.checkConsume(player)){
				player.sendError(hpCode, Status.Error.ITEM_NOT_ENOUGH_VALUE,0);
				return true;
			}
			this.reNameConsume = consume;
		}
		int changeRlt = CollegeService.getInstance().onchangeName(player, this.collegeName);
		if(changeRlt == 0){
			List<String> list = CollegeService.getInstance().getCollegeAllMember(player.getCollegeId());
			for(String mId : list){
				// 更新世界显示
				WorldPointService.getInstance().updateCollegeNameShow(mId, this.collegeName);
				Player cmember = GlobalData.getInstance().getActivePlayer(mId);
				if(Objects.nonNull(cmember)){
					CollegeService.getInstance().syncCollegeBaseInfo(cmember);
					cmember.getPush().updatePlayerDressEditData();
				}
			}
		}
		return true;
	}

	@Override
	public boolean onComplete(HawkAppObj callerObj, Map<String, Object> result) {
		if (this.rlt == 0 && Objects.nonNull(this.reNameConsume)) {
			this.reNameConsume.consumeAndPush(player, Action.COLLEGE_RENAME_COST);
			player.responseSuccess(hpCode);
		}
		return true;
	}

	public Player getPlayer() {
		return player;
	}

	public String getCollegeName() {
		return collegeName;
	}
	

	public int getHpCode() {
		return hpCode;
	}
	
}
