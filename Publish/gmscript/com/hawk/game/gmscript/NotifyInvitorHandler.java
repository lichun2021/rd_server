package com.hawk.game.gmscript;

import java.util.Map;

import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;

import com.hawk.game.data.FriendInviteInfo;
import com.hawk.game.global.GlobalData;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Script.ScriptError;
import com.hawk.game.service.RelationService;
import com.hawk.game.util.GameUtil;

/**
 * 被邀请密友通知邀请者
 *
 * localhost:8080/script/notifyInvitor?playerId=7py-4uwfp-1&friendOpenid=
 *
 * @param playerId
 * 
 * @author lating
 */
public class NotifyInvitorHandler extends HawkScript {

	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		try {
			Player player = GlobalData.getInstance().scriptMakesurePlayer(params);
			if (player == null) {
				HawkLog.errPrintln("notifyInvitor script failed, targetPlayer not exist, playerId: {}, friendOpenid: {}", params.get("playerId"), params.get("friendOpenid"));
				return HawkScript.failedResponse(ScriptError.ACCOUNT_NOT_EXIST_VALUE, params.toString());
			}
			
			// 非手Q、微信渠道此功能不开放
			if (!GameUtil.isFriendInviteEnable(player.getChannel())) {
				HawkLog.errPrintln("notifyInvitor script failed, player channel error, playerId: {}, friendOpenid: {}", player.getId(), params.get("friendOpenid"));
				return HawkScript.failedResponse(HawkScript.SCRIPT_ERROR, null);
			}
			
			String friendOpenid = params.get("friendOpenid");
			// 参数错误
			if (HawkOSOperator.isEmptyString(friendOpenid)) {
				HawkLog.errPrintln("notifyInvitor script failed, param error, playerId: {}, friendOpenid: {}", player.getId(), friendOpenid);
				return HawkScript.failedResponse(HawkScript.SCRIPT_ERROR, null);
			}
			
			// 对于未上线的玩家，内存中可能并没有ta的数据，所以这里要先加载数据
			FriendInviteInfo friendInviteInfo = RelationService.getInstance().getFriendInviteInfo(player.getId());
			if (friendInviteInfo.getInviteSuccFriends().isEmpty()) {
				RelationService.getInstance().loadFriendInviteInfo(player.getId());
			}
			
			// 未成功邀请
			if (!RelationService.getInstance().isFriendsInvited(player.getId(), friendOpenid)) {
				HawkLog.errPrintln("notifyInvitor script failed, friend has not succ invited, playerId: {}, friendOpenid: {}", player.getId(), friendOpenid);
				return HawkScript.failedResponse(HawkScript.SCRIPT_ERROR, null);
			}
			
			// 更新任务状态
			int threadIdx = player.getXid().getHashThread(HawkTaskManager.getInstance().getThreadNum());
			HawkTaskManager.getInstance().postTask(new HawkTask() {
				@Override
				public Object run() {
					RelationService.getInstance().refreshInviteFriendInfo(player, friendOpenid);
					return null;
				}
			}, threadIdx);
			
			return HawkScript.successResponse("");
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		return HawkScript.failedResponse(HawkScript.SCRIPT_ERROR, null);
	}
}
