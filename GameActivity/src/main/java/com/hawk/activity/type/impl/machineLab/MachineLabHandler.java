package com.hawk.activity.type.impl.machineLab;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.PBMachineLabContributeReq;
import com.hawk.game.protocol.Activity.PBMachineLabExchangeReq;
import com.hawk.game.protocol.Activity.PBMachineLabTipsReq;
import com.hawk.game.protocol.HP;

/**
 * 机甲研究所
 * 
 * @author che
 *
 */
public class MachineLabHandler extends ActivityProtocolHandler {
	
	/**
	 * 捐献道具
	 * @param hawkProtocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code2.MACHINE_LAB_CONTRIBUTE_REQ_VALUE)
	public void machineLabContribute(HawkProtocol hawkProtocol, String playerId){
		MachineLabActivity activity = this.getActivity(ActivityType.MACHINE_LAB_ACTIVITY);
		if(activity == null){
			return;
		}
		PBMachineLabContributeReq req = hawkProtocol.parseProtocol(PBMachineLabContributeReq.getDefaultInstance());
		int count = req.getCount();
		activity.contributeItems(playerId, count, hawkProtocol.getType());
	}
	
	
	/**
	 * 领取战令奖励
	 * @param hawkProtocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code2.MACHINE_LAB_ACHIEVE_REWARD_REQ_VALUE)
	public void machineLabAchieveReward(HawkProtocol hawkProtocol, String playerId){
		MachineLabActivity activity = this.getActivity(ActivityType.MACHINE_LAB_ACTIVITY);
		if(activity == null){
			return;
		}
		activity.achieveReward(playerId);
	}
	
	
	/**
	 * 换道具
	 * @param hawkProtocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code2.MACHINE_LAB_ITEM_EXCHANGE_REQ_VALUE)
	public void machineLabExchange(HawkProtocol hawkProtocol, String playerId){
		MachineLabActivity activity = this.getActivity(ActivityType.MACHINE_LAB_ACTIVITY);
		if(activity == null){
			return;
		}
		PBMachineLabExchangeReq req = hawkProtocol.parseProtocol(PBMachineLabExchangeReq.getDefaultInstance());
		int exchangeId = req.getExchangeId();
		int exchangeCount = req.getNum();
		activity.itemExchange(playerId, exchangeId, exchangeCount, hawkProtocol.getType());
	}
	
	/**
	 * 关注
	 * @param hawkProtocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code2.MACHINE_LAB_TIP_REQ_VALUE)
	public void machineLabupdateTips(HawkProtocol hawkProtocol, String playerId){
		MachineLabActivity activity = this.getActivity(ActivityType.MACHINE_LAB_ACTIVITY);
		if(activity == null){
			return;
		}
		PBMachineLabTipsReq req = hawkProtocol.parseProtocol(PBMachineLabTipsReq.getDefaultInstance());
		activity.updateActivityTips(playerId, req.getTipsList());
	}
	
	
	/**
	 * 排行榜信息
	 * @param hawkProtocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code2.MACHINE_LAB_RANK_REQ_VALUE)
	public void machineLabRank(HawkProtocol hawkProtocol, String playerId){
		MachineLabActivity activity = this.getActivity(ActivityType.MACHINE_LAB_ACTIVITY);
		if(activity == null){
			return;
		}
		activity.rankInfo(playerId);
	}
	
	
}