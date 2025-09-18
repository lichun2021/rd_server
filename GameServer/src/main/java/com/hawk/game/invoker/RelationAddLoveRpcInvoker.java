package com.hawk.game.invoker;

import java.util.Map;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkRpcMsg;
import org.hawk.msg.invoker.HawkRpcInvoker;

import com.hawk.game.service.RelationService;

/**
 * 好友模块添加亲密度
 * @author jm
 *
 */
public class RelationAddLoveRpcInvoker extends HawkRpcInvoker {
	/**
	 * 操作者ID
	 */
	private String operatorId;
	/**
	 * 被操作者ID
	 */
	private String operatoredId;
	/**
	 * 增加的好友度
	 */
	private int addLove;
	public RelationAddLoveRpcInvoker(String operatorId, String operatoredId, int addLove) {
		this.operatorId = operatorId;
		this.operatoredId = operatoredId;
		this.addLove = addLove;
	}
	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkRpcMsg msg, Map<String, Object> result) {
		((RelationService)targetObj).addLove(operatorId, operatoredId, addLove);		
		return true;
	}

	@Override
	public boolean onComplete(HawkAppObj callerObj, Map<String, Object> result) {
		return false;
	}
	
	public String getOperatorId() {
		return operatorId;
	}
	
	public String getOperatoredId() {
		return operatoredId;
	}
	
	public int getAddLove() {
		return addLove;
	}
}
