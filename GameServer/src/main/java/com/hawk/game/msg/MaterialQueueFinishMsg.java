package com.hawk.game.msg;

import java.util.List;

import org.hawk.msg.HawkMsg;

import com.hawk.gamelib.GameConst.MsgId;

/**
 * 消息 - 装备材料挖掘队列完成消息
 * 
 * @author Jesse
 *
 */
public class MaterialQueueFinishMsg extends HawkMsg {
	/**
	 * 挖掘位角标
	 */
	List<Integer> indexList;
	/**
	 * 是否加速完成
	 */
	boolean immediate;

	public List<Integer> getIndexList() {
		return indexList;
	}

	public void setIndexList(List<Integer> indexList) {
		this.indexList = indexList;
	}

	public boolean getImmediate() {
		return immediate;
	}

	public void setImmediate(boolean immediate) {
		this.immediate = immediate;
	}

	public MaterialQueueFinishMsg() {
		super(MsgId.MATERIAL_PRODUCT_FINISH);
	}

	/**
	 * 构造消息对象
	 * 
	 * @return
	 */
	public static MaterialQueueFinishMsg valueOf(List<Integer> indexList, boolean immediate) {
		MaterialQueueFinishMsg msg = new MaterialQueueFinishMsg();
		msg.indexList = indexList;
		msg.immediate = immediate;
		return msg;
	}
}
