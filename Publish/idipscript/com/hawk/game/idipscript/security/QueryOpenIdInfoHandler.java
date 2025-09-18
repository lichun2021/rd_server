package com.hawk.game.idipscript.security;

import org.hawk.os.HawkOSOperator;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.player.Player;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 查询OPENID基本信息(AQ)
 *
 * localhost:8080/script/idip/4129
 *
 * @author Jesse
 */
@HawkScript.Declare(id = "idip/4129")
public class QueryOpenIdInfoHandler extends IdipScriptHandler {
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.checkAccountAndPlayer(request, result);
		if (player == null) {
			return result;
		}

		// 角色名称
		result.getBody().put("RoleName", player.getNameEncoded());
		// VIP等级
		result.getBody().put("VipLevel", player.getVipLevel());
		// 钻石数量
		result.getBody().put("Diamond", player.getDiamonds());
		// 水晶数量
		result.getBody().put("Crystal", player.getGold());
		// 金矿数量
		result.getBody().put("GoldCount", String.valueOf(player.getGoldore()));
		// 石油数量
		result.getBody().put("OilCount", String.valueOf(player.getOil()));
		// 铀矿数量
		result.getBody().put("UraniumCount", String.valueOf(player.getSteel()));
		// 合金数量
		result.getBody().put("TombarthiteCount", String.valueOf(player.getTombarthite()));
		// 非保护金矿数量
		result.getBody().put("UnsafeGoldCount", String.valueOf(player.getGoldoreUnsafe()));
		// 非保护石油数量
		result.getBody().put("UnsafeOilCount", String.valueOf(player.getOilUnsafe()));
		// 非保护铀矿数量
		result.getBody().put("UnsafeUraniumCount", String.valueOf(player.getSteelUnsafe()));
		// 非保护合金数量
		result.getBody().put("UnsafeTombarthiteCount", String.valueOf(player.getTombarthiteUnsafe()));
				
		// 联盟id
		String guildId = player.getGuildId();
		result.getBody().put("UnionID", HawkOSOperator.isEmptyString(guildId) ? "0" : guildId);
		// 联盟贡献
		result.getBody().put("UnionPerIntegral", player.getGuildContribution());
		return result;
	}
}
