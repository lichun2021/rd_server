package com.hawk.game.idipscript.online;

import java.util.concurrent.atomic.AtomicInteger;

import org.hawk.app.HawkApp;
import org.hawk.callback.HawkCallback;
import org.hawk.log.HawkLog;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import org.hawk.task.HawkTaskManager;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.GsApp;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.invoker.PlayerChangeNameMsgInvoker;
import com.hawk.game.log.BehaviorLogger;
import com.hawk.game.log.BehaviorLogger.Params;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Status;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.SearchService;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.LogUtil;
import com.hawk.game.util.GsConst.ChangeContentType;
import com.hawk.game.world.service.WorldPlayerService;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.idip.IdipConst;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;
import com.hawk.log.Action;
import com.hawk.log.Source;
import com.hawk.log.LogConst.LogMsgType;
import com.hawk.tsssdk.util.UicNameDataInfo;


/**
 * 修改玩家角色名称(AQ)
 *
 * localhost:8081/idip/4345
 *
 * @param OpenId     
 * @param PlatId     
 * @param Partition 
 * @param RoleName
 * 
 * @author lating
 */
@HawkScript.Declare(id = "idip/4345")
public class ChangeRoleNameAQHandler extends IdipScriptHandler {
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.checkAccountAndPlayer(request, result);
		if (player == null) {
			return result;
		}
		
		String encodedName = request.getJSONObject("body").getString("RoleName");
		String name = IdipUtil.decode(encodedName);
		int errCode = GameUtil.tryOccupyPlayerName(player.getId(), player.getPuid(), name);
		if (errCode != Status.SysError.SUCCESS_OK_VALUE || GsApp.getInstance().getWordFilter().hasWord(name)) {
			HawkLog.logPrintln("script change name failed, playerId: {}, tarName: {}, errCode: {}", player.getId(), name, errCode);
			result.getBody().put("Result", IdipConst.SysError.API_EXCEPTION);
			result.getBody().put("RetMsg", "target name check failed");
			return result;
		}
		
		AtomicInteger checkResult = new AtomicInteger(-1);
		int threadIdx = player.getXid().getHashThread(HawkTaskManager.getInstance().getThreadNum());
		GameUtil.wordUicNameCheck(name, new HawkCallback() {
			@Override
			public int invoke(Object args) {
				UicNameDataInfo dataInfo = (UicNameDataInfo) args;
				if (dataInfo.msg_result_flag != 0) {
					checkResult.set(1);
					HawkLog.logPrintln("script change name failed, playerId: {}, tarName: {}, result: {}", player.getId(), name, result);
					return 0;
				}
				
				checkResult.set(0);
				changeName(player, name, Action.PLAYER_CHANGE_NAME);
				LogUtil.logSecTalkFlow(player, null, LogMsgType.CHANGE_NAME, "", name);
				return 0;
			}
		}, threadIdx);
		
		IdipUtil.wait4AsyncProccess(result, checkResult, "change name failed");
		return result;
	}
	
	/**
	 * 改名
	 * 
	 * @param name
	 * @param action
	 * @param protoType
	 */
	private void changeName(Player player, String name, Action action) {
		// 删除老名字信息
		GameUtil.removePlayerNameInfo(player.getEntity().getName());
		String oriName = player.getEntity().getName();
		
		// 设置当前名字
		player.getEntity().setName(name);
		RedisProxy.getInstance().updateChangeContentTime(player.getId(), ChangeContentType.CHANGE_ROLE_NAME, HawkApp.getInstance().getCurrentTime());
		//改名日志
		BehaviorLogger.log4Service(player, Source.ATTR_CHANGE, action, Params.valueOf("formerName", oriName), Params.valueOf("curName", name));
		
		// 修改全局数据管理器的名字信息
		GlobalData.getInstance().updateAccountInfo(player.getPuid(), player.getServerId(), player.getId(), 
				player.getEntity().getForbidenTime(), name);
		
		// 改玩家联盟信息
		GuildService.getInstance().dealMsg(MsgId.PLAYER_CHANGE_NAME, new PlayerChangeNameMsgInvoker(player));
		
		
		// 回复前端请求
		player.getPush().syncPlayerInfo();
		
		// 更新城点数据
		WorldPlayerService.getInstance().updatePlayerPointInfo(player.getId(), player.getName(),
				player.getCityLevel(), player.getIcon());
		
		// 更新搜索服务信息
		SearchService.getInstance().removePlayerInfo(oriName);
		SearchService.getInstance().addPlayerInfo(name, player.getId(), true);
		
		SearchService.getInstance().removePlayerNameLow(oriName, player.getId());
		SearchService.getInstance().addPlayerNameLow(name, player.getId());
	}
}


