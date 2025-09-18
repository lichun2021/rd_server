package com.hawk.game.gmscript;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;
import org.hawk.tuple.HawkTuple3;

import com.hawk.common.AccountRoleInfo;
import com.hawk.game.GsApp;
import com.hawk.game.GsConfig;
import com.hawk.game.crossproxy.CrossService;
import com.hawk.game.global.GlobalData;
import com.hawk.game.lianmengcyb.CYBORGRoomManager;
import com.hawk.game.lianmengstarwars.SWBattleRoom;
import com.hawk.game.lianmengstarwars.SWExtraParam;
import com.hawk.game.lianmengstarwars.SWRoomManager;
import com.hawk.game.lianmengstarwars.roomstate.SWGameOver;
import com.hawk.game.module.lianmengtaiboliya.TBLYRoomManager;
import com.hawk.game.msg.starwars.StarWarsPrepareMoveBackMsg;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Script.ScriptError;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.PlayerImageService;
import com.hawk.game.service.starwars.StarWarsConst.SWWarType;


/**
 * @author jm
 * localhost:port/script/starWarsMoveBack
 */
public class StarWarsMoveBackHandler extends HawkScript {
	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		String playerId = params.get("playerId");
		int type = Integer.parseInt(params.getOrDefault("opt", "1"));
		String result = "ok";
		if (type == 1) {
			result = doForce(playerId);			
		}  else if(type == 2) {
			result = doClearImmigrationStats(playerId);
		}else if (type == 3) {
			result = doClearEmigrationStats(playerId);
		} else if (type == 4) {
			result = doClearImmigrationPlayer(playerId);
		} else if(type == 5){
			createSW( );
		} else if(type == 6){
			swOver();
		}
		
		return HawkScript.successResponse(result);
	}

	private void swOver() {
		for(SWBattleRoom swroom : SWRoomManager.getInstance().findAllRoom()){
			SWGameOver swstate = new SWGameOver(swroom);
			swstate.setKillGame(true);
			swroom.setState(swstate);
		}
	}
	
	private void createSW( ) {
		String gid = "tguyhnjko6";
		if (!SWRoomManager.getInstance().hasGame(gid)) {
			SWExtraParam extParm = new SWExtraParam();
			extParm.setDebug(true); 
			extParm.setWarType(SWWarType.FIRST_WAR);
			SWRoomManager.getInstance().creatNewBattle(HawkTime.getMillisecond(), HawkTime.getMillisecond() + 3600 * 1000, gid, extParm);
//			SWRoomManager.getInstance().joinGame(gid, player);

			HawkTaskManager.getInstance().postExtraTask(new HawkTask() {
				@Override
				public Object run() {
					int joinCnt = 0;
					for (String guildiId : GuildService.getInstance().getGuildIds()) {
						addAllguildMembers(3, guildiId);
						joinCnt += GuildService.getInstance().getGuildMembers(guildiId).size();
						if(joinCnt> 500){
							break;
						}
					}
					return null;
				}
			});
			
		}else{
//			SWRoomManager.getInstance().joinGame(gid, player);
		}
	}
	
	public static void addAllguildMembers(int fuben, String guildId) {
		int joinCnt = 0;
		for (String playerId : GuildService.getInstance().getGuildMembers(guildId)) {
			HawkOSOperator.osSleep(10);
			Player robot = GlobalData.getInstance().makesurePlayer(playerId);
			robot.getData().loadAll(false);
			if (!robot.isInDungeonMap()) {
//				copyArmy(from, robot);
//				if (robot.getLevel() < 20) {
//					CreateFubenUtil.doCopy(from, robot);
//				}
				
				
				AccountRoleInfo accountRoleInfo = GlobalData.getInstance().getAccountRoleInfo(robot.getId());
				if (Objects.isNull(accountRoleInfo)) {

					if (accountRoleInfo == null) {
						accountRoleInfo = AccountRoleInfo.newInstance().openId(robot.getOpenId()).playerId(robot.getId())
								.serverId(robot.getServerId()).platform(robot.getPlatform()).registerTime(robot.getCreateTime());
					}

					try {
						accountRoleInfo.playerName(robot.getName()).playerLevel(robot.getLevel()).cityLevel(robot.getCityLevel())
								.vipLevel(robot.getVipLevel()).battlePoint(robot.getPower()).activeServer(GsConfig.getInstance().getServerId())
								.icon(robot.getIcon()).loginWay(robot.getEntity().getLoginWay()).loginTime(HawkTime.getMillisecond())
								.logoutTime(robot.getLogoutTime());
						accountRoleInfo.pfIcon(PlayerImageService.getInstance().getPfIcon(robot));
					} catch (Exception e) {
						HawkException.catchException(e, robot.getId());
					}

					GlobalData.getInstance().addOrUpdateAccountRoleInfo(accountRoleInfo);

				}
				
				
				
				joinCnt++;
				if (fuben == 1) {
					TBLYRoomManager.getInstance().joinGame("abcd", robot);
					System.out.println("Join Tbly " + robot.getName()+ " joinCnt:"+joinCnt);
				}
				if (fuben == 2) {
					CYBORGRoomManager.getInstance().joinGame("abcd", robot);
					System.out.println("Join Cyborg " + robot.getName()+ " joinCnt:"+joinCnt);
					if (joinCnt >= 30) {
						break;
					}
				}
				if(fuben == 3){
					SWRoomManager.getInstance().joinGame("tguyhnjko6", robot);
					System.out.println("Join Cyborg " + robot.getName()+ " joinCnt:"+joinCnt);
				}
			}
		}
	}
	
	private String doClearImmigrationPlayer(String playerId) {
		if (HawkOSOperator.isEmptyString(playerId)) {
			return "player is null"; 
		}
		
		CrossService.getInstance().clearStarWarsImmigrationPlayer(playerId, false);
		
		return "ok";
	}

	private String doClearEmigrationStats(String playerId) {
		List<String> strList = new ArrayList<>();
		if (HawkOSOperator.isEmptyString(playerId)) {
			for (Entry<String, String> entry : CrossService.getInstance().getEmigrationPlayers().entrySet()) {
				CrossService.getInstance().clearStarWarsEmigrationPlayer(entry.getKey());
				strList.add(entry.getKey());
			}
		} else {
			CrossService.getInstance().clearStarWarsEmigrationPlayer(playerId);
			strList.add(playerId);
		}
		
		return strList.toString();
	}

	private String doClearImmigrationStats(String playerId) {
		List<String> strList = new ArrayList<>();
		if (HawkOSOperator.isEmptyString(playerId)) {
			for (Entry<String, String> entry : CrossService.getInstance().getImmigrationPlayers().entrySet()) {
				CrossService.getInstance().clearStarWarsImmigrationPlayer(entry.getKey());
				strList.add(entry.getKey());
			}
		} else {			
			CrossService.getInstance().clearStarWarsImmigrationPlayer(playerId);
			strList.add(playerId);
		}  
		
		return strList.toString();
	}

	private String doForce(String playerId) {
		List<String> strList = new ArrayList<>();
		if (HawkOSOperator.isEmptyString(playerId)) {
			for (Entry<String, String> entry : CrossService.getInstance().getEmigrationPlayers().entrySet()) {
				HawkTuple3<String, Integer, String> tuple = moveBackPlayer(entry.getKey());
				strList.add(tuple.toString());
			}
		} else {
			HawkTuple3<String, Integer, String> tuple = moveBackPlayer(playerId);
			strList.add(tuple.toString());
		}
		
		return strList.toString();
	}

	public HawkTuple3<String, Integer, String> moveBackPlayer(String playerId) {
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
				
				GsApp.getInstance().postMsg(player.getXid(), new StarWarsPrepareMoveBackMsg());
	
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
