package com.hawk.game.idipscript.online;

import org.hawk.app.HawkApp;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.config.GameConstCfg;
import com.hawk.game.data.PlayerImageData;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Status;
import com.hawk.game.service.PlayerImageService;
import com.hawk.game.service.RelationService;
import com.hawk.game.util.LoginUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.GsConst.ChangeContentType;
import com.hawk.game.util.LogUtil;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;


/**
 * 平台信息授权变更统一回调 -- 10282134
 *
 * localhost:8081/idip/4411
 * 
 * 		事件 event 取值 说明
		解除全部授权                    -1    取消渠道授权，一般会触发登录态失效，玩家需要重新登陆授权。所有信息（个人信息+平台好友关系链）全部清理。
		解除个人信息                    -2    需要清理游戏内昵称、头像等信息。
		解除平台好友关系链        -3    需要清理当前账号个人的游戏内平台好友关系链、及好友的平台关系链。
		全部授权                               1    重新登陆授权即可自动恢复授权，正常来讲用不上这个值。
		重新授权个人信息               2    恢复游戏内昵称、头像等信息。
		重新授权平台好友关系链   3    恢复当前账号在个人的平台好友关系链及其好友展示。
		
		TLOG 日志
	           （1）玩家取消或增加 隐私授权的流水日志，记录玩家授权信息变化：playerusertagchg
	           （2）登录或登出日志 新增隐私字段，记录当时玩家的授权状态： playerlogin 或 playerlogout
 *
 * @author lating
 */
@HawkScript.Declare(id = "idip/4411")
public class UpdateInfoAuthorizationHandler extends IdipScriptHandler {
	
	public static final String PFAUTH_CANCEL_STATUS_KEY = "pfAuthCancelEvent:";
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.checkAccountAndPlayer(request, result);
		if (player == null) {
			return result;
		}
		
		JSONArray array = request.getJSONObject("body").getJSONArray("Events");
		GlobalData.getInstance().updatePlayerPfAuthInfo(player.getOpenId(), array);
		
		for (int i = 0; i < array.size(); i++) {
			int event = array.getIntValue(i);
			eventHandle(player, event);
			LogUtil.logPlayerUsertagChange(player, event);
		}
		
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		return result;
	}
	
	/**
	 * 事件处理
	 * @param player
	 * @param event
	 */
	protected void eventHandle(Player player, int event) {
		try {
			switch (event) {
				// 解除全部授权. 取消渠道授权，一般会触发登录态失效，玩家需要重新登陆授权。所有信息（个人信息+平台好友关系链）全部清理。
				case GsConst.EVENT_CANCEL_PFAUTH: {
					RelationService.getInstance().removeCachePlatfromFriend(player.getId());
					updatePlayerInfo(player, event);
					// 处理完将玩家强制离线
					if (player.isActiveOnline()) {
						player.kickout(Status.IdipMsgCode.IDIP_AUTH_RELEASE_OFFLINE_VALUE, true, null);
					} else if (player.isBackground()) {
						RedisProxy.getInstance().getRedisSession().setString(PFAUTH_CANCEL_STATUS_KEY + player.getOpenId(), "1", 3600);
					}
					break;
				}
				// 解除个人信息. 需要清理游戏内昵称、头像等信息。
				case GsConst.EVENT_CANCEL_PERSONINFO: {
					updatePlayerInfo(player, event);
					break;
				}
				// 解除平台好友关系链. 需要清理当前账号个人的游戏内平台好友关系链、及好友的平台关系链。
				case GsConst.EVENT_CANCEL_RELATION: {
					RelationService.getInstance().removeCachePlatfromFriend(player.getId());
					break;
				}
				// 恢复游戏内昵称、头像等信息。
				case GsConst.EVENT_RECOVER_PERSONINFO: {
					PlayerImageData data = PlayerImageService.getInstance().getPlayerImageData(player);
					if(PlayerImageService.getInstance().isDefineImage(data.getUseImageId())){
						PlayerImageService.getInstance().changeImageOrCircleSuccess(player);
					}
					break;
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 更新个人头像和昵称
	 * @param player
	 * @param event
	 */
	protected void updatePlayerInfo(Player player, int event) {
		PlayerImageData data = PlayerImageService.getInstance().getPlayerImageData(player);
		if(PlayerImageService.getInstance().isDefineImage(data.getUseImageId())){
			PlayerImageService.getInstance().changeImageOrCircleSuccess(player);
		}
		
		if (player.getName().startsWith(GameConstCfg.getInstance().getNamePrefix())) {
			return;
		}
		
		long lastChangeTime = RedisProxy.getInstance().getChangeContentTime(player.getId(), ChangeContentType.CHANGE_ROLE_NAME);
		if (lastChangeTime <= 0) {
			String playerName = LoginUtil.randomPlayerName(player.getId(), player.getPuid());
			String beforeName = player.getName();
			player.getEntity().setName(playerName);
			RedisProxy.getInstance().updateChangeContentTime(player.getId(), ChangeContentType.CHANGE_ROLE_NAME, HawkApp.getInstance().getCurrentTime());
			HawkLog.logPrintln("UpdateInfoAuthorizationHandler change playerName, playerId: {}, beforeName: {}, afterName: {}, event: {}", player.getId(), beforeName, playerName, event);
		}
	}
}

