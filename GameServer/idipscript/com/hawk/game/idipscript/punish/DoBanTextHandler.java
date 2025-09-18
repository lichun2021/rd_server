package com.hawk.game.idipscript.punish;

import org.hawk.os.HawkTime;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONObject;
import com.hawk.common.IDIPBanInfo;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.player.Player;
import com.hawk.game.util.GsConst.IDIPBanType;
import com.hawk.idip.IdipConst;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;


/**
 * 禁止修改文本信息（AQ) -- 10282832
 *
 * localhost:8081/idip/4435
 *
 * @author lating
 */
@HawkScript.Declare(id = "idip/4435")
public class DoBanTextHandler extends IdipScriptHandler {
	
	static final IDIPBanType[] ALL_TYPE = {IDIPBanType.BAN_PLAYER_NAME, IDIPBanType.BAN_SIGNATURE, IDIPBanType.BAN_GUILD_NAME, IDIPBanType.BAN_GUILD_NOTICE, IDIPBanType.BAN_GUILD_MANOR, IDIPBanType.BAN_GUILD_ANNOUNCE,
			IDIPBanType.BAN_GUILD_SIGN, IDIPBanType.BAN_GUILD_LEVELNAME, IDIPBanType.BAN_GUILD_TAG, IDIPBanType.BAN_KING_NOTICE, IDIPBanType.BAN_CYBOR_TEAM_NAME, IDIPBanType.BAN_TIBERIUM_TEAM_NAME, 
			IDIPBanType.BAN_CHAT_ROOM_NAME, IDIPBanType.BAN_FRIEND_TXT, IDIPBanType.BAN_ENERGY_MATRIX, IDIPBanType.BAN_MARCH_PRSET_NAME, IDIPBanType.BAN_EQUIP_MARSHALLING, IDIPBanType.BAN_WORLD_FAVORITE};
	static final int all = 99;
	static final int cancel = 0; 
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.checkAccountAndPlayer(request, result);
		if (player == null) {
			return result;
		}
		
		String reason = request.getJSONObject("body").getString("BanReason"); 
		int seconds = request.getJSONObject("body").getIntValue("Time");  // 禁止时长（秒）
		// 修改类型（1、个人昵称,2、个性签名,3、联盟名字,4、联盟公告,5、联盟堡垒名字,6、联盟宣言,7、联盟标记,8、联盟阶级,9、联盟简称,10、国王公告,11、赛博队伍名,12、泰伯利亚队伍名,13、群聊名,14、好友备注,15、能量矩阵,16、部队编队,17、装备编组,18、世界收藏,99全部,,0取消处罚）
		int type = request.getJSONObject("body").getIntValue("Type");  
		reason = IdipUtil.decode(reason);
		
		if (type == all) {
			long now = HawkTime.getMillisecond();
			long endTime = now + 1000L * seconds;
			IDIPBanInfo banInfo = new IDIPBanInfo(player.getId(), reason + "（解封时间：" + HawkTime.formatTime(endTime) + "）", now, endTime, seconds);
			for (IDIPBanType banType : ALL_TYPE) {
				RedisProxy.getInstance().addIDIPBanInfo(player.getId(), banInfo, banType);
			}
		} else if (type == cancel) {
			for (IDIPBanType banType : ALL_TYPE) {
				RedisProxy.getInstance().removeIDIPBanInfo(player.getId(), banType);
			}
		} else if (type > 0 && type <= ALL_TYPE.length) {
			long now = HawkTime.getMillisecond();
			long endTime = now + 1000L * seconds;
			IDIPBanInfo banInfo = new IDIPBanInfo(player.getId(), reason + "（解封时间：" + HawkTime.formatTime(endTime) + "）", now, endTime, seconds);
			RedisProxy.getInstance().addIDIPBanInfo(player.getId(), banInfo, ALL_TYPE[type - 1]);
		} else {
			return sendResult(result, seconds, IdipConst.SysError.PARAM_ERROR, "param error");
		}
		
		return sendResult(result, seconds, 0, "");
	}
	
	private IdipResult sendResult(IdipResult result, int seconds, int resultCode, String msg) {
		int timeNow = HawkTime.getSeconds();
		result.getBody().put("Result", resultCode);
		result.getBody().put("RetMsg", msg);
		result.getBody().put("OperTime", timeNow);     // 封停时间
		result.getBody().put("ExpireTime", timeNow + seconds);   // 解封时间
		result.getBody().put("BanTerm", seconds); // 封停时长
		return result;
	} 
}


