package com.hawk.game.idipscript.third;

import org.hawk.app.HawkAppObj;
import org.hawk.log.HawkLog;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;
import org.hawk.os.HawkException;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ConsumeItems;
import com.hawk.log.Action;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.util.LogUtil;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.idip.IdipConst;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 非安全资源转换为安全资源
 *
 * localhost:8080/script/idip/4191
 * 
 * @author lating
 */
@HawkScript.Declare(id = "idip/4191")
public class PlayerResourceTransferHandler extends IdipScriptHandler {
	
	public static final int[] SAFE_RESOURCE_TYPE = {PlayerAttr.GOLDORE_VALUE, PlayerAttr.OIL_VALUE, PlayerAttr.STEEL_VALUE, PlayerAttr.TOMBARTHITE_VALUE};
	
	public static final int[] UNSAFE_RESOURCE_TYPE = {PlayerAttr.GOLDORE_UNSAFE_VALUE, PlayerAttr.OIL_UNSAFE_VALUE, PlayerAttr.STEEL_UNSAFE_VALUE, PlayerAttr.TOMBARTHITE_UNSAFE_VALUE};
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.playerCheck(request, result);
		if (player == null) {
			return result;
		}
		
		if (player.isActiveOnline()) {
			player.dealMsg(MsgId.IDIP_CHANGE_PLAYER_INFO, new ResourceTransferMsgInvoker(player, request));
			player.kickout(Status.IdipMsgCode.IDIP_RESOURCE_TRANSFER_VALUE, true, null);
		} else {
			try {
				resourceTransfer(request, player);
			} catch (Exception e) {
				HawkException.catchException(e);
				result.getBody().put("Result", IdipConst.SysError.API_EXCEPTION);
				result.getBody().put("RetMsg", "resource transfer failed");
				return result;
			}
		}
		
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		return result;
	}
	
	public static class ResourceTransferMsgInvoker extends HawkMsgInvoker {
		private Player player;
		private JSONObject request;
		
		public ResourceTransferMsgInvoker(Player player, JSONObject request) {
			this.player = player;
			this.request = request;
		}
		
		@Override
		public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
			resourceTransfer(request, player);
			return true;
		}
	}
	
	/**
	 * 资源转换
	 * @param request
	 * @param player
	 */
	private static void resourceTransfer(JSONObject request, Player player) {
		ConsumeItems consume = ConsumeItems.valueOf();
		AwardItems awardItems = AwardItems.valueOf();
		long total = 0;
		for (int i = 0; i < UNSAFE_RESOURCE_TYPE.length; i++) {
			long resNum = player.getResByType(UNSAFE_RESOURCE_TYPE[i]);
			consume.addConsumeInfo(PlayerAttr.valueOf(UNSAFE_RESOURCE_TYPE[i]), resNum);
			awardItems.addResource(SAFE_RESOURCE_TYPE[i], resNum);
			total += resNum;
		}
		
		if (consume.checkConsume(player)) {
			consume.consumeAndPush(player, Action.IDIP_CHANGE_PLAYER_ATTR);
		}
		awardItems.rewardTakeAffectAndPush(player, Action.IDIP_CHANGE_PLAYER_ATTR, true);
		// 记录敏感日志
		LogUtil.logIdipSensitivity(player, request, 0, (int) total);
		HawkLog.logPrintln("idip transfer unsafe resource to safe, playerId: {}, total: {}", player.getId(), total);
	}
}
