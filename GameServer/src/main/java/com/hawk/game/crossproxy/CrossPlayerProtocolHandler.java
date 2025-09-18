package com.hawk.game.crossproxy;

import org.hawk.delay.HawkDelayAction;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.net.session.HawkSession;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.GsApp;
import com.hawk.game.GsConfig;
import com.hawk.game.crossproxy.model.CsPlayer;
import com.hawk.game.crossproxy.model.CsPlayerUtil;
import com.hawk.game.crossproxy.model.CsSession;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.log.DungeonRedisLog;
import com.hawk.game.msg.cross.LocalLoginMsg;
import com.hawk.game.player.Player;
import com.hawk.game.player.cache.PlayerDataKey;
import com.hawk.game.protocol.CHP;
import com.hawk.game.protocol.Cross.RpcCommonResp;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Login.HPLogin;
import com.hawk.game.protocol.Login.HPLoginRet;
import com.hawk.game.protocol.Status;
import com.hawk.game.queryentity.AccountInfo;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.LoginUtil;

public class CrossPlayerProtocolHandler {
	public static CrossPlayerProtocolHandler getInstance() {
		return CrossMessageHandlerHolder.crossMessageEHandler;
	}

	static class CrossMessageHandlerHolder {
		private static CrossPlayerProtocolHandler crossMessageEHandler = new CrossPlayerProtocolHandler();
	}

	public void onTransportProtocol(ProxyHeader header, HawkProtocol protocol) {
		Player player = GlobalData.getInstance().makesurePlayer(header.getTarget());
		if (player == null) {
			return;
		}
		
		if (player.getSession() == null) {
			HawkSession session = CrossService.getInstance().getPlayerIdSession(header.getTarget());
			player.setSession(session);
		}
		
		//在互相顶号的时候会发Login_s的错误码, playerIdSession里面的session保存时最后的玩家, 收到login_s的玩家应该是最后的玩家.
		if (protocol.getType() == HP.code.LOGIN_S_VALUE) {
			HawkSession session = CrossService.getInstance().getPlayerIdSession(header.getTarget());
			if (session != null && session.isActive()) {
				session.sendProtocol(protocol);
				return;
			} 
		}
		
		boolean succ = player.sendProtocol(protocol);
		if (!succ) {
			// 到此登录已失败, 怀疑在消息返回中玩家再次建立了连接. 尝试再发一次
			HawkSession session = CrossService.getInstance().getPlayerIdSession(header.getTarget());
			player.setSession(session);
			succ = player.sendProtocol(protocol);
		}
		// 如果收到assemble_finish,通知本服做好友的初始化
		if (protocol.getType() == HP.code.ASSEMBLE_FINISH_S_VALUE) {
			DungeonRedisLog.log(player.getId(), "cross player assemble finish, playerId: {}, send result: {}", header.getTarget(), succ);
			// 接受到这个返回之后就改成在跨服中了.
			player.setCrossStatus(GsConst.PlayerCrossStatus.CORSS);
			GsApp.getInstance().postMsg(player.getXid(), new LocalLoginMsg());			
		} else if (protocol.getType() == HP.code.PLAYER_KICKOUT_S_VALUE) {				
			// 把玩家身上原有的session清理掉.
			HawkSession oldSession = player.getSession();
			player.setSession(null);

			// 如果发现map里面的session和player身上的对不上, 则说明是被顶号了把新的session设置到玩家上面
			HawkSession mapSession = CrossService.getInstance().getPlayerIdSession(header.getTarget());
			if (mapSession != null && mapSession != oldSession) {
				player.setSession(mapSession);
			}
			
			int hashCode = oldSession == null ? 0 : oldSession.hashCode();
			int newHashCode = mapSession == null ? 0 : mapSession.hashCode();
			
			DungeonRedisLog.log(player.getId(), "receive kickout playerId:{}, oldSession:{}, newSession:{}",  
					header.getTarget(), hashCode, newHashCode);
			
			if (oldSession != null) {
				GsApp.getInstance().addDelayAction(1000, new HawkDelayAction() {
					@Override
					protected void doAction() {
						oldSession.close();
					}
				});
			}
		}
	}

	/**
	 * 跨服之后的玩家消息入口 准备整个玩家登陆请求处理链上需要的数据
	 * 
	 * @param proxyHeader
	 * @param hawkProtocol
	 */
	public boolean onProtocol(ProxyHeader proxyHeader, HawkProtocol hawkProtocol) {
		switch (hawkProtocol.getType()) {
		case HP.code.LOGIN_C_VALUE:
			return doLogin(proxyHeader, hawkProtocol);
		case CHP.code.INNER_LOGOUT_VALUE:
			return doLoginOut(proxyHeader, hawkProtocol);
		case CHP.code.EXIT_CORSS_VALUE:
			return doExitCross(proxyHeader, hawkProtocol);
		case HP.code.TIBERIUM_WAR_EXIT_INSTANCE_REQ_VALUE:
			return doTiberiumExitCross(proxyHeader, hawkProtocol);
		case HP.code.STAR_WARS_EXIT_REQ_VALUE:
			return doStarWarsExitCross(proxyHeader, hawkProtocol);
		case HP.code2.YQZZ_WAR_EXIT_INSTANCE_REQ_VALUE:
			return doYQZZExitCross(proxyHeader, hawkProtocol);
		default:
			// 非跨入,则是跨出.
			if (!CrossService.getInstance().isImmigrationPlayer(proxyHeader.getTarget())) {
				Player player = GlobalData.getInstance().makesurePlayer(proxyHeader.getTarget());
				if (player == null || player.getSession() == null) {
					DungeonRedisLog.log(proxyHeader.getTarget(), "emigration player not find playerId:{}, player is null:{}, protocol:{}", 
													proxyHeader.getTarget(), player == null, hawkProtocol.getType());

					return false;
				}
				hawkProtocol.bindSession(player.getSession());

				return true;
			}

			CsSession csSession = CsSessionManager.getInstance().getSession(proxyHeader.getTarget());
			if (csSession == null) {
				DungeonRedisLog.log(proxyHeader.getTarget(), "other protocol cssession is null playerId:{}, fromServerId:{}",
						proxyHeader.getTarget(), proxyHeader.getFrom());

				return false;
			}
			
			hawkProtocol.bindSession(csSession);
			csSession.updateAccessTime();
			
			if (!hawkProtocol.getSession().isActive()) {
				DungeonRedisLog.log(proxyHeader.getTarget(), "cross session not active playerId:{}", proxyHeader.getTarget());

				return false;
			}

			return true;
		}
	}

	/**
	 * 月球之战玩家发起退出
	 * @param proxyHeader
	 * @param hawkProtocol
	 * @return
	 */
	private boolean doYQZZExitCross(ProxyHeader proxyHeader, HawkProtocol hawkProtocol) {
		// 先把连接删了防止再发消息.
		DungeonRedisLog.log(proxyHeader.getTarget(), "YQZZ cross player send exit cross protocol playerId:{}", proxyHeader.getTarget());
		CsSession csSession = CsSessionManager.getInstance().removeSession(proxyHeader.getTarget());
		if (csSession == null) {
			DungeonRedisLog.log(proxyHeader.getTarget(), "YQZZ remove cross session fail playerId:{}", proxyHeader.getTarget());

			return false;
		} else {
			hawkProtocol.bindSession(csSession);

			return true;
		}
	}

	/**
	 * 大帝战玩家发起退出
	 * @param proxyHeader
	 * @param hawkProtocol
	 * @return
	 */
	private boolean doStarWarsExitCross(ProxyHeader proxyHeader, HawkProtocol hawkProtocol) {
		// 先把连接删了防止再发消息.
		DungeonRedisLog.log(proxyHeader.getTarget(), "starWars cross player send exit cross protocol playerId:{}", proxyHeader.getTarget());
		CsSession csSession = CsSessionManager.getInstance().removeSession(proxyHeader.getTarget());
		if (csSession == null) {
			DungeonRedisLog.log(proxyHeader.getTarget(), "starWars remove cross session fail playerId:{}", proxyHeader.getTarget());

			return false;
		} else {
			hawkProtocol.bindSession(csSession);

			return true;
		}
	}

	private boolean doTiberiumExitCross(ProxyHeader proxyHeader, HawkProtocol hawkProtocol) {
		// 先把连接删了防止再发消息.
		DungeonRedisLog.log(proxyHeader.getTarget(), "tiberium cross player send exit cross protocol playerId:{}", proxyHeader.getTarget());
		CsSession csSession = CsSessionManager.getInstance().removeSession(proxyHeader.getTarget());
		if (csSession == null) {
			DungeonRedisLog.log(proxyHeader.getTarget(), "tiberium remove cross session fail playerId:{}", proxyHeader.getTarget());

			return false;
		} else {
			hawkProtocol.bindSession(csSession);

			return true;
		}
	}

	private boolean doLogin(ProxyHeader proxyHeader, HawkProtocol hawkProtocol) {
		DungeonRedisLog.log(proxyHeader.getTarget(), "cs player do cross login playerId:{}", proxyHeader.getTarget());
		
		CsSession oldSession = CsSessionManager.getInstance().getSession(proxyHeader.getTarget());
		if (oldSession != null && oldSession.getCreateTime() + (GsConfig.getInstance().getLoginElapse() / 1000) > HawkTime.getSeconds()) {
			DungeonRedisLog.log(proxyHeader.getTarget(), "cs player login interval is short playerId:{} ", proxyHeader.getTarget());
			
			HPLoginRet.Builder builder = HPLoginRet.newBuilder();
			builder.setErrCode(Status.SysError.PLAYER_FREQUENT_LOGIN_VALUE);
			HawkProtocol respProtocol = HawkProtocol.valueOf(HP.code.LOGIN_S, builder);			
			CrossProxy.getInstance().sendProtocol(respProtocol, proxyHeader.getFrom(), proxyHeader.getTarget());
			
			return false;
		}
		
		HPLogin loginCmd = hawkProtocol.parseProtocol(HPLogin.getDefaultInstance());
		if (CsPlayerUtil.isFirstCrossLogin(loginCmd)) {
			boolean checkResult = CrossService.getInstance().removeAndCheckPrepareCrossTime(proxyHeader.getTarget());
			if (!checkResult) {
				DungeonRedisLog.log(proxyHeader.getTarget(), "cs player first cross login time check fail playerId:{}", proxyHeader.getTarget());
				
				return false;
			}
		} else {
			//检测是否已经在跨服列表里面
			if (!CrossService.getInstance().isImmigrationPlayer(proxyHeader.getTarget())) {
				DungeonRedisLog.log(proxyHeader.getTarget(), "cs player send  login protocol but not in immigration playerId:{}", proxyHeader.getTarget());
				return false;
			}
			
			// 先看下redis中数据是否还存在，不存在直接打回
			if (!checkCrossDataExist(proxyHeader)) {
				return false;
			}
		}
		
		
		CsSession csSession = onSessionOpen(proxyHeader);
		hawkProtocol.bindSession(csSession);

		boolean initPlayer = initPlayer(csSession, loginCmd);
		if (!initPlayer) {
			crossResponse(proxyHeader, Status.SysError.EXCEPTION_VALUE);
			return false;
		} else {
			// 在踢号的时候有可能把session和playerId的关系割裂,
			// 因为都是playerId和Session的映射无法区分,在这里尝试加回.
			if (!CsSessionManager.getInstance().existSession(proxyHeader.getTarget())) {
				CsSessionManager.getInstance().addSession(csSession);
			}
			crossResponse(proxyHeader, Status.SysError.SUCCESS_OK_VALUE);
		}

		return true;
	}
	
	/**
	 * 检测数据是否存在
	 * 
	 * @param playerId
	 * @return
	 */
	private boolean checkCrossDataExist(ProxyHeader proxyHeader) {
		String playerId = proxyHeader.getTarget();
		try {
			String redisKey = "player_data:" + playerId;
			boolean exist = RedisProxy.getInstance().getRedisSession().hExists(redisKey, PlayerDataKey.PlayerEntity.name(), 0);
			if (!exist) {
				DungeonRedisLog.log(proxyHeader.getTarget(), "cs player send login protocol but data not exist, playerId: {}, key: {}", playerId, PlayerDataKey.PlayerEntity.name());
				crossResponse(proxyHeader, 99099099);
				return false;
			}
			
			exist = RedisProxy.getInstance().getRedisSession().hExists(redisKey, PlayerDataKey.PlayerBaseEntity.name(), 0);
			if (!exist) {
				DungeonRedisLog.log(proxyHeader.getTarget(), "cs player send login protocol but data not exist, playerId: {}, key: {}", playerId, PlayerDataKey.PlayerBaseEntity.name());
				crossResponse(proxyHeader, 99099099);
				return false;
			}
			
		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}
		
		return true;
	}

	/**
	 * 在跨服登录的过程中出现了问题, 发回一个消息,让原服能回退.
	 * 
	 * @param proxyHeader
	 */
	private void crossResponse(ProxyHeader proxyHeader, int errorCode) {
		RpcCommonResp.Builder builder = RpcCommonResp.newBuilder();
		builder.setErrorCode(errorCode);
		HawkProtocol respProtocol = HawkProtocol.valueOf(CHP.code.INNER_ENTER_CROSS_RESP, builder);

		// 失败也有可能是其它情况, 正常登陆的时候失败, 就不处理了.
		if (!HawkOSOperator.isEmptyString(proxyHeader.getRpcid())) {
			CrossProxy.getInstance().rpcResponse(proxyHeader, respProtocol);
		}
	}

	private boolean doExitCross(ProxyHeader proxyHeader, HawkProtocol hawkProtocol) {
		// 先把连接删了防止再发消息.
		DungeonRedisLog.log(proxyHeader.getTarget(), "cross player send exit cross protocol playerId:{}", proxyHeader.getTarget());
		CsSession csSession = CsSessionManager.getInstance().removeSession(proxyHeader.getTarget());
		if (csSession == null) {
			DungeonRedisLog.log(proxyHeader.getTarget(), "remove cross session fail playerId:{}", proxyHeader.getTarget());

			return false;
		} else {
			hawkProtocol.bindSession(csSession);

			return true;
		}

	}

	private boolean doLoginOut(ProxyHeader proxyHeader, HawkProtocol hawkProtocol) {
		
		CsSession csSession = CsSessionManager.getInstance().removeSession(proxyHeader.getTarget());
		
		DungeonRedisLog.log(proxyHeader.getTarget(), "cross player logout playerId:{}", proxyHeader.getTarget());
		
		if (csSession == null) {
			DungeonRedisLog.log(proxyHeader.getTarget(), "cross player logout session is null playerId:{}", proxyHeader.getTarget());
			
			return false;
		}
		
		hawkProtocol.bindSession(csSession);

		return true;
	}

	public boolean initPlayer(CsSession csSession, HPLogin loginCmd) {
		CrossService crossService = CrossService.getInstance();
		GlobalData globalData = GlobalData.getInstance();
		// 清理同区在线
		LoginUtil.kickoutActiveRoleForCross(loginCmd);
		// 先把玩家加进去.
		crossService.addImmigrationPlayer(csSession.getPlayerId(), csSession.getFromServerId());
		// 添加玩家到AccountInfo里面
		String puid = GameUtil.getPuidByPlatform(loginCmd.getPuid(), loginCmd.getPlatform());
		// playerName必须反序列化之后才能拿出来,直接通过协议带过来, 要保证这里的serverId的正确性, 只有第一次innerEnterCrossMsg才有值.
		if (CsPlayerUtil.isFirstCrossLogin(loginCmd)) {
			String serverId = loginCmd.getServerId();
			if (!HawkOSOperator.isEmptyString(serverId) && Integer.parseInt(serverId) > 2000000) {
				serverId = serverId.substring(2);
				loginCmd = loginCmd.toBuilder().setServerId(serverId).build();
			}
			globalData.updateAccountInfo(puid, serverId, csSession.getPlayerId(), 0,
					loginCmd.getInnerEnterCrossMsg().getPlayerNmae(), false);
		}
		
		Player player = globalData.makesurePlayer(csSession.getPlayerId());
		if (player == null || player.getData() == null) {
			DungeonRedisLog.log(csSession.getPlayerId(), "playerId:{} init csplayer fail ", csSession.getPlayerId());
			return false;
		}
		
		CsPlayer csPlayer = player.getCsPlayer();
		if (csPlayer == null) {
			DungeonRedisLog.log(player.getId(), "get csplayer fail playerId:{}  type:{}", player.getId(), player.getClass().getName());
			return false;
		}
		
		if(!loginCmd.getServerId().equals(player.getServerId())) {
			DungeonRedisLog.log(csPlayer.getId(),"cross player serverId:{},loginCmdServerId:{} not equal", player.getServerId(), loginCmd.getServerId());
		}
		
		// 平台的token信息
		handlePfToken(player, loginCmd);

		// 设置AccountInfo信息 登录的时候把状态
		AccountInfo accountInfo = GlobalData.getInstance().getAccountInfoByPlayerId(csSession.getPlayerId());
		accountInfo.setLoginTime(HawkTime.getMillisecond());
		
		csSession.setUserObject("account", accountInfo);
		
		//到这里之后就认为是在跨服中了.
		boolean setStatus = RedisProxy.getInstance().setPlayerCrossStatus(csSession.getFromServerId(), csSession.getPlayerId(), GsConst.PlayerCrossStatus.CORSS);
		if (!setStatus) {
			//状态设置失败就失败吧，不做处理.下次重新登陆的时候还会尝试写入.
			DungeonRedisLog.log(csPlayer.getId(),"csplayer set cross status error playerId:{}", csSession.getPlayerId());			
		}
		
		//只有第一次登陆的时候才能取出这里面的信息.
		if (CsPlayerUtil.isFirstCrossLogin(loginCmd)) {
			int crossType = loginCmd.getInnerEnterCrossMsg().getCrossType();
			csPlayer.setCrossType(crossType);
			DungeonRedisLog.log(csPlayer.getId(),"player cross type:{}", csPlayer.getCrossType());
			RedisProxy.getInstance().setPlayerCrossType(csSession.getFromServerId(), csSession.getPlayerId(), crossType);
		}		

		return true;
	}

	private void handlePfToken(Player player, HPLogin cmd) {
		// 设置平台的token信息
		if (!GameUtil.isWin32Platform(cmd.getPlatform(), cmd.getChannel())) {
			JSONObject pfInfoJson = JSONObject.parseObject(cmd.getPfToken());
			player.setPfTokenJson(pfInfoJson);
		}
	}

	public CsSession onSessionOpen(ProxyHeader proxyHeader) {
		CsSession csSession = new CsSession(proxyHeader.getTarget(), proxyHeader.getFrom());
		csSession.setActive(true);
		CsSessionManager.getInstance().addSession(csSession);
		DungeonRedisLog.log(proxyHeader.getTarget(),"onSessionOpen session:{}", csSession.hashCode());
		return csSession;
	}
}
