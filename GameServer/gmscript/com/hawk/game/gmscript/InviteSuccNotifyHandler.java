package com.hawk.game.gmscript;

import java.util.Map;

import org.hawk.app.HawkApp;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;

import com.hawk.game.config.ConstProperty;
import com.hawk.game.data.FriendInviteInfo;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Script.ScriptError;
import com.hawk.game.service.RelationService;
import com.hawk.game.util.GameUtil;

/**
 * qq、微信密友邀请成功处理
 *
 * localhost:8080/script/inviteSuccNotify?playerId=7py-4uwfp-1&friendOpenid=&serverId=
 *
 * @param playerId
 * 
 * @author lating
 */
public class InviteSuccNotifyHandler extends HawkScript {

	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		try {
			Player player = GlobalData.getInstance().scriptMakesurePlayer(params);
			if (player == null) {
				HawkLog.errPrintln("inviteSuccNotify script failed, targetPlayer not exist, playerId: {}, friendOpenid: {}", params.get("playerId"), params.get("friendOpenid"));
				return HawkScript.failedResponse(ScriptError.ACCOUNT_NOT_EXIST_VALUE, params.toString());
			}
			
			// 非手Q、微信渠道此功能不开放
			if (!GameUtil.isFriendInviteEnable(player.getChannel())) {
				HawkLog.errPrintln("inviteSuccNotify script failed, player channel error, playerId: {}, friendOpenid: {}", player.getId(), params.get("friendOpenid"));
				return HawkScript.failedResponse(HawkScript.SCRIPT_ERROR, null);
			}
			
			String friendOpenid = params.get("friendOpenid");
			String sopenid = params.get("sopenid");
			String serverId = params.get("serverId");
			String platform = params.get("platform");
			// 参数错误
			if (HawkOSOperator.isEmptyString(friendOpenid) || HawkOSOperator.isEmptyString(serverId) || HawkOSOperator.isEmptyString(platform)) {
				HawkLog.errPrintln("inviteSuccNotify script failed, param error, playerId: {}, friendOpenid: {}, serverId: {}, platform: {}", player.getId(), friendOpenid, serverId, platform);
				return HawkScript.failedResponse(HawkScript.SCRIPT_ERROR, null);
			}
			
			int inviteTime = RedisProxy.getInstance().getInviteTime(player.getId(), sopenid);
			int now = (int) (HawkApp.getInstance().getCurrentTime() / 1000);
			// 没有邀请过该好友或已过了邀请有效期
			if (now - inviteTime > ConstProperty.getInstance().getFriendInviteExpireTime()) {
				HawkLog.errPrintln("inviteSuccNotify script failed, time error, playerId: {}, friendOpenid: {}, inviteTime: {}", player.getId(), friendOpenid, inviteTime);
				return HawkScript.failedResponse(HawkScript.SCRIPT_ERROR, null);
			}
			
			// 对于未上线的玩家，内存中可能并没有ta的数据，所以这里要先加载数据
			FriendInviteInfo friendInviteInfo = RelationService.getInstance().getFriendInviteInfo(player.getId());
			if (friendInviteInfo.getInviteSuccFriends().isEmpty()) {
				RelationService.getInstance().loadFriendInviteInfo(player.getId());
			}
			
			// 已邀请成功
			if (RelationService.getInstance().isFriendsInvited(player.getId(), friendOpenid)) {
				HawkLog.errPrintln("inviteSuccNotify script failed, friend has invited, playerId: {}, friendOpenid: {}, inviteTime: {}", player.getId(), friendOpenid, inviteTime);
				return HawkScript.failedResponse(HawkScript.SCRIPT_ERROR, null);
			}
			
			// 更新任务状态
			int threadIdx = player.getXid().getHashThread(HawkTaskManager.getInstance().getThreadNum());
			HawkTaskManager.getInstance().postTask(new HawkTask() {
				@Override
				public Object run() {
					RelationService.getInstance().addInviteSuccFriend(player, friendOpenid, sopenid, serverId, platform);
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
