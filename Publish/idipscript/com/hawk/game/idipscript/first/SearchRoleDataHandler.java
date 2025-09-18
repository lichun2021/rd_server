package com.hawk.game.idipscript.first;

import org.hawk.os.HawkTime;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.global.GlobalData;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.player.Player;
import com.hawk.game.queryentity.AccountInfo;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 玩家角色信息查询
 *
 * localhost:8080/script/idip/4099?OpenId=&RoleId=
 *
 * @param OpenId  用户openId
 * @param RoleId  玩家playerId 
 * 
 * @author lating
 */
@HawkScript.Declare(id = "idip/4099")
public class SearchRoleDataHandler extends IdipScriptHandler {
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.playerCheck(request, result);
		if (player == null) {
			return result;
		}
		
		// 角色ID
		result.getBody().put("RoleId", player.getId());
		// 角色名称
		result.getBody().put("RoleName", player.getNameEncoded());  
		// 当前等级
		result.getBody().put("Level", player.getLevel()); 
		// 角色经验值
		result.getBody().put("Exp", player.getExp());     
		// 钻石数量
		result.getBody().put("Diamond", player.getDiamonds());   
		
		result.getBody().put("GoldCount", player.getGoldore());
		result.getBody().put("OilCount", player.getOil());
		result.getBody().put("UraniumCount", player.getSteel());
		result.getBody().put("TombarthiteCount", player.getTombarthite());
		result.getBody().put("UnsafeGoldCount", player.getGoldoreUnsafe());
		result.getBody().put("UnsafeOilCount", player.getOilUnsafe());
		result.getBody().put("UnsafeUraniumCount", player.getSteelUnsafe());
		result.getBody().put("UnsafeTombarthiteCount", player.getTombarthiteUnsafe());
		// VIP等级
		result.getBody().put("VipLevel", player.getVipLevel());   
		// VIP经验值
		result.getBody().put("VipExp", player.getData().getPlayerEntity().getVipExp());   
		// 注册时间
		result.getBody().put("RegisterTime", String.valueOf(player.getCreateTime()/1000));  
		// 累计登陆时长
		long onlineTime = player.getOnlineTimeHistory();
		// 累计登陆时间
		result.getBody().put("TotalLoginTime", onlineTime > 0 ? onlineTime : (HawkTime.getMillisecond() - player.getLoginTime()) / 1000); 
		// 最近登录时间
		result.getBody().put("LastLoginTime", HawkTime.formatTime(player.getLoginTime()));     
		// 最后登出时间
		result.getBody().put("LastLogoutTime", player.getLogoutTime() > 0 ? HawkTime.formatTime(player.getLogoutTime()) : "");
		// 账号是否在线：1离线0在线
		result.getBody().put("IsOnline", GlobalData.getInstance().isOnline(player.getId()) ? 0 : 1);
		AccountInfo accountInfo = GlobalData.getInstance().getAccountInfo(player.getPuid(), player.getServerId());
		// 账号状态:1封停0正常
		result.getBody().put("Status", HawkTime.getMillisecond() < accountInfo.getForbidenTime() ? 1 : 0);
		
		return result;
	}
	
}
