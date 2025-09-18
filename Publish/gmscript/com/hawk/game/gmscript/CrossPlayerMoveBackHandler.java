package com.hawk.game.gmscript;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import org.hawk.tuple.HawkTuple3;

import com.hawk.game.GsApp;
import com.hawk.game.crossproxy.CrossService;
import com.hawk.game.global.GlobalData;
import com.hawk.game.msg.cross.PrepareMoveBackCrossPlayerMsg;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Script.ScriptError;

/**
 * 为了可以强制操作成功, 兼容在线的情况， 但是最好是玩家不在线,临界状态,谁也无法预测.
 * force 就是强制.
 * 1 正常签回
 * 2 强制签回
 * 3 清理迁入状态
 * 4 清理玩家迁出状态.
 * @author jm crossPlayerMoveBack?playerId=xxx&opt=
 */
public class CrossPlayerMoveBackHandler extends HawkScript {

	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		String playerId = params.get("playerId");
		int type = Integer.parseInt(params.getOrDefault("opt", "1"));
		String result = "ok";
		if (type == 1) {
			result = doNoramal(playerId);
		} else if (type == 2) {
			result = doForce(playerId);			
		}  else if(type == 3) {
			result = doClearImmigrationStats(playerId);
		}else if (type == 4) {
			result = doClearEmigrationStats(playerId);
		}
		
		return HawkScript.successResponse(result);
	}
	
	private String doClearEmigrationStats(String playerId) {
		List<String> strList = new ArrayList<>();
		if (HawkOSOperator.isEmptyString(playerId)) {
			for (Entry<String, String> entry : CrossService.getInstance().getEmigrationPlayers().entrySet()) {
				CrossService.getInstance().clearEmigrationPlayer(entry.getKey());
				strList.add(entry.getKey());
			}
		} else {
			CrossService.getInstance().clearEmigrationPlayer(playerId);
			strList.add(playerId);
		}
		
		return strList.toString();
	}

	private String doClearImmigrationStats(String playerId) {
		List<String> strList = new ArrayList<>();
		if (HawkOSOperator.isEmptyString(playerId)) {
			for (Entry<String, String> entry : CrossService.getInstance().getImmigrationPlayers().entrySet()) {
				CrossService.getInstance().clearImmigrationPlayer(entry.getKey());
				strList.add(entry.getKey());
			}
		} else {
			CrossService.getInstance().clearImmigrationPlayer(playerId);
			strList.add(playerId);
		}  
		
		return strList.toString();
	}

	private String doForce(String playerId) {
		List<String> strList = new ArrayList<>();
		if (HawkOSOperator.isEmptyString(playerId)) {
			for (Entry<String, String> entry : CrossService.getInstance().getEmigrationPlayers().entrySet()) {
				HawkTuple3<String, Integer, String> tuple = moveBackPlayer(entry.getKey(), true);
				strList.add(tuple.toString());
			}
		} else {
			HawkTuple3<String, Integer, String> tuple = moveBackPlayer(playerId, true);
			strList.add(tuple.toString());
		}
		
		return strList.toString();
	}

	private String doNoramal(String playerId) {
		List<String> strList = new ArrayList<>();
		if (HawkOSOperator.isEmptyString(playerId)) {
			for (Entry<String, String> entry : CrossService.getInstance().getEmigrationPlayers().entrySet()) {
				HawkTuple3<String, Integer, String> tuple = moveBackPlayer(entry.getKey(), false);
				strList.add(tuple.toString());
			}
		} else {
			HawkTuple3<String, Integer, String> tuple = moveBackPlayer(playerId, false);
			strList.add(tuple.toString());
		}
		
		return strList.toString();
	}

	public HawkTuple3<String, Integer, String> moveBackPlayer(String playerId, boolean force) {
		int errorCode = ScriptError.SUCCESS_OK_VALUE;
		String msg = "ok";
		label: 
		{
			try {
				Player player = GlobalData.getInstance().makesurePlayer(playerId);
				if (player == null) {
					errorCode = ScriptError.ACCOUNT_NOT_EXIST_VALUE;
					msg = "player not exist";
	
					break label;
				} 
	
				// 不是本服,
				if (!GlobalData.getInstance().isLocalServer(player.getServerId())) {
					errorCode = ScriptError.ACCOUNT_NOT_EXIST_VALUE;
					msg = "server not the player register server";
	
					break label;
				}
	
				// 只有跨出去的玩家
				String toServer = CrossService.getInstance().getEmigrationPlayerServerId(player.getId());
				if (HawkOSOperator.isEmptyString(toServer)) {
					errorCode = ScriptError.NOT_IN_CROSS_VALUE;
					msg = "player not in cross";
	
					break label;
				}
				
				GsApp.getInstance().postMsg(player.getXid(), new PrepareMoveBackCrossPlayerMsg(force));
	
			} catch (Exception e) {
				HawkException.catchException(e);
	
				errorCode = ScriptError.EXCEPTION_VALUE;
				msg = "exception";
	
				break label;
			}
		}
		
		HawkLog.logPrintln("playerId:{} cross player move back errorCode:{}", playerId, errorCode);
		
		return new HawkTuple3<String, Integer, String>(playerId, errorCode, msg);
	}
}
