package com.hawk.game.idipscript.attrmodify;

import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.config.PlayerShowCfg;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.log.BehaviorLogger;
import com.hawk.game.log.BehaviorLogger.Params;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Player.ImageType;
import com.hawk.game.service.PlayerImageService;
import com.hawk.game.world.service.WorldPlayerService;
import com.hawk.idip.IdipConst;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;
import com.hawk.log.Action;
import com.hawk.log.Source;


/**
 * 修改玩家默认头像 -- 10282827
 *
 * localhost:8080/script/idip/4341
 *
 * @param OpenId  用户openId
 * 
 * @author lating
 */
@HawkScript.Declare(id = "idip/4341")
public class ChangePlayerIconHandler extends IdipScriptHandler {
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.playerCheck(request, result);
		if (player == null) {
			return result;
		}
		
		int imageId = request.getJSONObject("body").getIntValue("ImageId");
		// 修改指挥官头像（不同于形象）
		int retCode = PlayerImageService.getInstance().changeImageOrCircle(player, ImageType.IMAGE, imageId);
		
		if (retCode != 0) {
			HawkLog.logPrintln("idip changePlayerImage, playerId: {}, openid: {}, imageId: {}, resultCode: {}", player.getId(), player.getOpenId(), imageId, retCode);
		}
		
		if (retCode == 0 || retCode == Status.Error.THIS_IMAGE_INUSE_VALUE) {
			result.getBody().put("Result", 0);
			result.getBody().put("RetMsg", "");
		} else {
			result.getBody().put("Result", IdipConst.SysError.API_EXCEPTION);
			result.getBody().put("RetMsg", "change image failed: " + retCode);
		}
		
		return result;
	}
	
	/**
	 * 修改指挥官形象
	 * 
	 * @param player
	 * @param result
	 * @param iconId
	 */
	protected void changePlayerIcon(Player player, IdipResult result, int iconId) {
		int cfgId = iconId + PlayerShowCfg.ID_BASE;
		PlayerShowCfg cfg = HawkConfigManager.getInstance().getConfigByKey(PlayerShowCfg.class, cfgId);
		if (cfg == null) {
			HawkLog.errPrintln("player idip change icon failed, config error, playerId: {}, iconId: {}, cfgId: {}", player.getId(), iconId, cfgId);
			result.getBody().put("Result", IdipConst.SysError.PARAM_ERROR);
			result.getBody().put("RetMsg", "icon config not exist");
			return;
		}
		
		player.getEntity().setIcon(iconId);
		player.getPush().syncPlayerInfo();
		// 修改玩家城点数据
		WorldPlayerService.getInstance().updatePlayerPointInfo(player.getId(), player.getName(),
				player.getCityLv(), player.getIcon(), player.getData().getPersonalProtectVals());
		
		BehaviorLogger.log4Service(player, Source.ATTR_CHANGE, Action.PLAYER_BUY_ICON,
				Params.valueOf("curIcon", player.getEntity().getIcon()), Params.valueOf("tarIcon", iconId));
		
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
	}
	
}


