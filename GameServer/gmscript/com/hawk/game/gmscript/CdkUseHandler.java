package com.hawk.game.gmscript;

import java.util.Map;

import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import org.hawk.util.service.HawkCdkService;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.config.GameConstCfg;
import com.hawk.game.config.SpecialCdkCfg;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.item.AwardItems;
import com.hawk.log.Action;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Script.ScriptError;

/**
 * CDK使用测试
 * 
 * localhost:8080/script/cdkuse?playerId=******&cdk=***
 */

public class CdkUseHandler extends HawkScript {
	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		try {
			Player player = GlobalData.getInstance().scriptMakesurePlayer(params);
			String cdk = params.get("cdk");
			
			// 特殊类型cdk检测
			AwardItems awardItems = rewardSpecialCdks(player, cdk);
			if (awardItems != null) {
				return HawkScript.successResponse(awardItems.toString());
			}

			int status = HawkCdkService.CDK_STATUS_OK;
			String type = HawkCdkService.getTypeNameFromCdk(cdk);

			// 判断是否使用过同类cdkey
			if (!GameConstCfg.getInstance().isCycleCdkType(type) && RedisProxy.getInstance().checkCdkTypeUsed(player.getId(), type)) {
				status = HawkCdkService.CDK_STATUS_TYPE_USED;
			}

			// 使用cdk
			StringBuilder cdkString = new StringBuilder();
			if (status == HawkCdkService.CDK_STATUS_OK) {
				JSONObject attrJson = new JSONObject();
				attrJson.put("platform", player.getPlatform());
				attrJson.put("channel", player.getChannel());
				status = HawkCdkService.getInstance().useCdk(player.getId(), attrJson.toJSONString(), cdk, cdkString);
				if (status == HawkCdkService.CDK_STATUS_OK) {
					// 发放奖励
					awardItems = AwardItems.valueOf(cdkString.toString());
					if (awardItems != null) {
						awardItems.rewardTakeAffectAndPush(player, Action.USE_CDK, false);
					}

					//设置用户使用cdk类型
					RedisProxy.getInstance().updateCdkUsed(player.getId(), type, 1);
					
					return HawkScript.successResponse(awardItems.toString());
				}
			}

			return HawkScript.successResponse("status:" + status);
			
		} catch (Exception e) {
			HawkException.catchException(e);
		}

		return HawkScript.failedResponse(ScriptError.EXCEPTION_VALUE, "");

	}
	
	/**
	 * 特殊cdk
	 * 
	 * @param player
	 * @param cdk
	 * @return
	 */
	private AwardItems rewardSpecialCdks(Player player, String cdk) {
		// 取CDK数据
		SpecialCdkCfg cdkCfg = HawkConfigManager.getInstance().getConfigByKey(SpecialCdkCfg.class, cdk);
		if(cdkCfg == null) {
			return null;
		}
		
		long now = HawkTime.getMillisecond();
		if(cdkCfg.getStartTime() > now || cdkCfg.getEndTime() < now) {
			return null;
		}
		
		// 查询是否使用过
		int status = HawkCdkService.CDK_STATUS_OK;

		// 检查是否使用
		if (RedisProxy.getInstance().checkCdkTypeUsed(player.getId(), cdk)) {
			status = HawkCdkService.CDK_STATUS_TYPE_USED;
		}

		// 发放奖励
		if (status == HawkCdkService.CDK_STATUS_OK) {
			AwardItems awardItems = cdkCfg.getAwardItems();
			if (awardItems != null) {
				awardItems.rewardTakeAffectAndPush(player, Action.USE_CDK, false);
			}

			//设置用户使用cdk类型
			RedisProxy.getInstance().updateCdkUsed(player.getId(), cdk, 1);
			return awardItems;
		}

		return null;
	}
}
