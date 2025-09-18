package com.hawk.game.msg;

import org.hawk.msg.HawkMsg;

import com.hawk.gamelib.GameConst.MsgId;

/**
 * 装备科技队列完成
 * @author golden
 *
 */
public class EquipResearchQueueFinishMsg extends HawkMsg {

	/**
	 * 信息
	 */
	String info;
	
	public EquipResearchQueueFinishMsg() {
		super(MsgId.EQUIP_RESEARCH_QUEUE_FINISH);
	}

	/**
	 * 构造消息对象
	 */
	public static EquipResearchQueueFinishMsg valueOf(String info) {
		EquipResearchQueueFinishMsg msg = new EquipResearchQueueFinishMsg();
		msg.info = info;
		return msg;
	}
	
	/**
	 * 研究id
	 */
	public int getResearchId() {
		return Integer.parseInt(info.split("_")[0]);
	}

	/**
	 * 研究等级
	 */
	public int getResearchLevel() {
		return Integer.parseInt(info.split("_")[1]);
	}
}
