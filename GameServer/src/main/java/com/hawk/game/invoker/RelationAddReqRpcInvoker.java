package com.hawk.game.invoker;

import java.util.List;
import java.util.Map;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkRpcMsg;
import org.hawk.msg.invoker.HawkRpcInvoker;

import com.hawk.game.global.RedisProxy;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.service.RelationService;
import com.hawk.game.util.LogUtil;
import com.hawk.log.LogConst.LogMsgType;

/**
 * 请求添加好友
 * @author jm
 *
 */
public class RelationAddReqRpcInvoker extends HawkRpcInvoker {
	/**
	 * 玩家ID
	 */
	private List<String> idList;
	/**
	 * 发起申请的文字
	 */
	private String content;
	/**
	 * 玩家
	 */
	private Player player;
	
	public RelationAddReqRpcInvoker(Player player, List<String> idList, String content) {
		this.player = player;
		this.idList = idList;
		this.content = content;
	}
	
	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkRpcMsg msg, Map<String, Object> result) {		
		int rlt = RelationService.getInstance().friendAddReq(player.getId(), idList, content);
		result.put("rlt", rlt);
		if (rlt == Status.SysError.SUCCESS_OK_VALUE) {
			LogUtil.logSecTalkFlow(player, null, LogMsgType.FRIEND, "", content);
		}
		
		return true;
	}

	@Override
	public boolean onComplete(HawkAppObj callerObj, Map<String, Object> result) {
		int rlt = (Integer) result.get("rlt");
		if (rlt == Status.SysError.SUCCESS_OK_VALUE) {
			player.responseSuccess(HP.code.FRIEND_ADD_REQ_VALUE);
			RedisProxy.getInstance().dayAddFriendInc(player.getId(), idList.size());
		} else {
			player.sendError(HP.code.FRIEND_ADD_REQ_VALUE, rlt, 0);
		}

		return true;
	}

	public List<String> getIdList() {
		return idList;
	}

	public String getContent() {
		return content;
	}

	public Player getPlayer() {
		return player;
	}
	
}
