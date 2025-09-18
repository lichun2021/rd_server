package com.hawk.game.idipscript.recharge;

import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import org.hawk.task.HawkDelayTask;
import org.hawk.task.HawkTaskManager;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.player.Player;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;
import com.hawk.sdk.SDKManager;

/**
 * 刷新玩家金条余额状态（玩家在游戏外充值之后，通知游戏内向米大师拉取最新余额） -- 10282165
 *
 * localhost:8081/idip/4479
 *
 * @author lating
 */
@HawkScript.Declare(id = "idip/4479")
public class DiamondRechargeRefreshHandler extends IdipScriptHandler {
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.checkAccountAndPlayer(request, result);
		if (player == null) {
			return result;
		}
		
		// 在线情况下才去拉取余额
		if (player.isActiveOnline()) {
			getBalance(player);
		}
		
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		return result;
	}
	
	/**
	 * 拉取余额
	 * @param player
	 */
	private void getBalance(Player player) {
		// 拉取时间间隔
		int[] times = SDKManager.getInstance().getFetchBalanceTime();
		// 执行延时任务
		int threadIdx = player.getXid().getHashThread(HawkTaskManager.getInstance().getExtraThreadNum());
		HawkTaskManager.getInstance().postExtraTask(new HawkDelayTask(times[0] * 1000, 0, times.length) {
			@Override
			public Object run() {
				int diamonds = player.getDiamonds();
				// 拉取余额
				int saveAmt = player.checkBalance();
				if (saveAmt < 0) {
					if (getTriggerCount() < times.length - 1) {
						setTriggerPeriod(times[getTriggerCount() + 1] * 1000);
					}
					return null;
				}
				
				// 同步最新金条数
				if (diamonds != player.getDiamonds()) {
					player.getPush().syncPlayerDiamonds();
				}

				setFinish();  // 拉取余额成功，结束任务
				return null;
			}
		}, threadIdx);
	}
	
}


