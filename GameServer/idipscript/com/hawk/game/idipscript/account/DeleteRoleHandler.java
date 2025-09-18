package com.hawk.game.idipscript.account;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.os.HawkException;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.invoker.AccountCancellationRpcInvoker;
import com.hawk.game.invoker.guard.GuardDeleteInvoker;
import com.hawk.game.player.Player;
import com.hawk.game.service.RelationService;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.LogUtil;
import com.hawk.gamelib.GameConst;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 删除指定角色 -- 10282157
 *
 * localhost:8080/script/idip/4463
 * 
 * 删除角色功能关联处理验收标准：
	1.若账号有联盟且非盟主，则退出联盟   done
	2.若账号有联盟且为盟主，则转让盟主给其他成员，并退出联盟    done
	3.解除本账号的守护关系
	4.删除本账号的所有好友
	5.清除本账号的世界坐标点    done
	6.清除本账号的排行榜上榜数据    done
	7.其他玩家尝试查看此账号的个人数据时（打开指挥官信息界面），不返回相关数据，弹出错误码提示：找不到相关数据
 * 
 * @author lating
 */
@HawkScript.Declare(id = "idip/4463")
public class DeleteRoleHandler extends IdipScriptHandler {
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.playerCheck(request, result);
		if (player == null) {
			return result;
		}

		// 删除守护关系
		RelationService.getInstance().dealMsg(GameConst.MsgId.GUARD_DELETE, new GuardDeleteSubInvoker(player));
		LogUtil.logIdipSensitivity(player, request, 0, 0);
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		return result;
	}
	
	public static class GuardDeleteSubInvoker extends GuardDeleteInvoker {

		private Player player;
		
		public GuardDeleteSubInvoker(Player player) {
			super(player.getId());
			this.player = player;
		}
		
		public boolean onMessage(HawkAppObj arg0, HawkMsg arg1) {
			try {
				super.onMessage(arg0, arg1);
			} catch (Exception e) {
				HawkException.catchException(e);
			}
			// 删除好友关系
			player.rpcCall(MsgId.ACCOUNT_CANCELLATION, RelationService.getInstance(), new AccountCancellationRpcInvoker(player));
			GameUtil.resetAccount(player);
			return true;
		}
	}
	
}
