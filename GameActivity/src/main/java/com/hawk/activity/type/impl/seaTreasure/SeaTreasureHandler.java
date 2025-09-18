package com.hawk.activity.type.impl.seaTreasure;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.ActivitySeaTreasure.SeaTreasureBoxCommonReq;
import com.hawk.game.protocol.ActivitySeaTreasure.SeaTreasureBuyItemReq;
import com.hawk.game.protocol.HP;
import com.hawk.gamelib.GameConst.MsgId;

/**
 * 秘海珍寻
 * @author Golden
 *
 */
public class SeaTreasureHandler extends ActivityProtocolHandler {
	/**
	 * 请求界面信息
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code2.ACTIVITY_SEA_TREASURE_PAGEINFO_REQ_VALUE)
	public void pageInfo(HawkProtocol protocol, String playerId){
		SeaTreasureActivity activity = getActivity(ActivityType.SEA_TREASURE);
		if (!activity.doCheck(playerId)) {
			return;
		}
		activity.syncPageInfo(playerId);
	}
	
	/**
	 * 寻宝
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code2.ACTIVITY_SEA_TREASURE_DO_VALUE)
	public void doSearch(HawkProtocol protocol, String playerId){
		SeaTreasureActivity activity = getActivity(ActivityType.SEA_TREASURE);
		if (!activity.doCheck(playerId)) {
			return;
		}
		activity.doSearch(playerId);
	}
	
	/**
	 * 开启宝箱
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code2.ACTIVITY_SEA_TREASURE_OPEN_VALUE)
	public void doOpen(HawkProtocol protocol, String playerId){
		SeaTreasureActivity activity = getActivity(ActivityType.SEA_TREASURE);
		if (!activity.doCheck(playerId)) {
			return;
		}
		SeaTreasureBoxCommonReq req = protocol.parseProtocol(SeaTreasureBoxCommonReq.getDefaultInstance());
		activity.doOpen(playerId, req.getGrid());
	}
	
	/**
	 * 收取
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code2.ACTIVITY_SEA_TREASURE_RECEIVE_VALUE)
	public void doReceive(HawkProtocol protocol, String playerId){
		SeaTreasureActivity activity = getActivity(ActivityType.SEA_TREASURE);
		if (!activity.doCheck(playerId)) {
			return;
		}
		SeaTreasureBoxCommonReq req = protocol.parseProtocol(SeaTreasureBoxCommonReq.getDefaultInstance());
		activity.doReceive(playerId, req.getGrid());
	}
	
	/**
	 * 收取所有
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code2.ACTIVITY_SEA_TREASURE_RECEIVE_ALL_VALUE)
	public void doReceiveAll(HawkProtocol protocol, String playerId){
		SeaTreasureActivity activity = getActivity(ActivityType.SEA_TREASURE);
		if (!activity.doCheck(playerId)) {
			return;
		}
		activity.doReceiveAll(playerId);
	}
	
	/**
	 * 购买道具
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code2.ACTIVITY_SEA_TREASURE_BUY_ITEM_VALUE)
	public void doBuy(HawkProtocol protocol, String playerId){
		SeaTreasureActivity activity = getActivity(ActivityType.SEA_TREASURE);
		if (!activity.doCheck(playerId)) {
			return;
		}
		SeaTreasureBuyItemReq req = protocol.parseProtocol(SeaTreasureBuyItemReq.getDefaultInstance());
		activity.doBuyItem(playerId, req.getCount(), true);
	}
	
	/**
	 * 购买并开启
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code2.SEA_TREASURE_BUY_ADN_RECEIVEALL_REQ_VALUE)
	public void doBuyAndReceiveAll(HawkProtocol protocol, String playerId){
		SeaTreasureActivity activity = getActivity(ActivityType.SEA_TREASURE);
		if (!activity.doCheck(playerId)) {
			return;
		}
		SeaTreasureBuyItemReq req = protocol.parseProtocol(SeaTreasureBuyItemReq.getDefaultInstance());
		activity.doBuyItem(playerId, req.getCount(), false);
		
		activity.callBack(playerId, MsgId.SEA_TREASURE_RECEIVE, () -> {
			if (req.getGrid() == 0) {
				activity.doReceiveAll(playerId);
			} else {
				activity.doReceive(playerId, req.getGrid());
			}
		});
	}
}
