package com.hawk.game.crossproxy.yqzz;



import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkTime;

import com.hawk.game.GsConfig;
import com.hawk.game.crossproxy.CrossProxy;
import com.hawk.game.crossproxy.CrossService;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.log.DungeonRedisLog;
import com.hawk.game.module.lianmengyqzz.march.service.YQZZMatchService;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Cross.CrossType;
import com.hawk.game.protocol.Cross.InnerEnterCrossMsg;
import com.hawk.game.protocol.Cross.RpcCommonResp;
import com.hawk.game.protocol.Cross.YQZZCrossMsg;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Login.HPLogin;
import com.hawk.game.protocol.Status;
import com.hawk.game.queryentity.AccountInfo;
import com.hawk.game.util.GsConst;

/**
 * 不用内部类，就把涉及到Callback的调用全部定义在这里.
 * @author jm
 *
 */
public class YQZZCallbackOperationService {
	private static final YQZZCallbackOperationService instance = new YQZZCallbackOperationService();
	public static YQZZCallbackOperationService getInstance() {
		return instance;
	}
	
	private YQZZCallbackOperationService() {
		
	}
		
	/**
	 * 预处理返回.
	 * @param player
	 * @param hawkProtocol
	 * @param enterCrossMsg
	 */
	public void onPrepareCrossBack(Player player, HawkProtocol hawkProtocol, YQZZCrossMsg enterCrossMsg) {
		RpcCommonResp rpcCommonResp = hawkProtocol.parseProtocol(RpcCommonResp.getDefaultInstance());
		Player.logger.info("yqzz playerId:{} received inner enter cross errorCode:{}", player.getId(), rpcCommonResp.getErrorCode());
		
		if (rpcCommonResp.getErrorCode() == Status.SysError.SUCCESS_OK_VALUE) {
			onPrepareCrossOk(player, enterCrossMsg);
		} else {
			onPrepareCrossFail(player);
		}
	}
	
	/**
	 * 预跨服成功之后把玩家的信息
	 * @param enterCrossMsg
	 */
	public void onPrepareCrossOk(Player player, YQZZCrossMsg crossMsg) {
		String targetServerId = crossMsg.getServerId();
		//添加到本服的其它跨服玩家
		CrossService.getInstance().addEmigrationPlayer(player.getId(), targetServerId);
		// 通知客户端清理数据
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.YQZZ_WAR_CROSS_BEGIN));
		
		//记录日志 
		//LogUtil.logTimberiumCross(player, targetServerId, CrossStateType.CROSS_START);
		
		//构建一条带跨服信息的登录协议发送到目标服.
		HPLogin.Builder loginBuilder = player.getHpLogin();
		InnerEnterCrossMsg.Builder innerBuilder = loginBuilder.getInnerEnterCrossMsgBuilder();
		if (innerBuilder == null) {
			innerBuilder = InnerEnterCrossMsg.newBuilder();
			loginBuilder.setInnerEnterCrossMsg(innerBuilder);			
		}
		innerBuilder.setYqzzCrossMsg(crossMsg);
		innerBuilder.setPlayerNmae(player.getName());
		String guildId = player.getGuildId();
		guildId = guildId == null ? "" : guildId; 
		innerBuilder.setGuildId(guildId);
		innerBuilder.setCrossType(CrossType.YQZZ_VALUE);
		innerBuilder.setGuildAuth(player.getGuildAuthority());
		
		
		//玩家发起跨服的时候其它协议是不能处理的，所以这里走rpc
		HPLogin.Builder cloneLoginBuilder = loginBuilder.clone();
		cloneLoginBuilder.setFlag(1);
		HawkProtocol hawkProtocol = HawkProtocol.valueOf(HP.code.LOGIN_C_VALUE, cloneLoginBuilder);
		CrossProxy.getInstance().rpcRequest(hawkProtocol, new YQZZEnterCallback(player), targetServerId, player.getId(), player.getId());
		DungeonRedisLog.log(player.getId(), "{}",targetServerId);
	}

	/** 在玩家线程.
	 *  预跨服失败的时候需要把玩家给找回来.
	 */
	public void onPrepareCrossFail(Player player) {
		Player.logger.info("YQZZ prepare cross fail playerId:{}", player.getId());
		YQZZMatchService.getInstance().getDataManger().removeJoinExtraPlayer(player.getId());
		player.setCrossStatus(GsConst.PlayerCrossStatus.NOTHING);
		//放在最前,防止后面的逻辑判断有问题.
		String toServerId = CrossService.getInstance().removeEmigrationPlayer(player.getId());		
		toServerId = toServerId == null ? "NULL" : toServerId;
		// 通知客户端清理数据
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.YQZZ_WAR_CROSS_FINISH));
		
		//记录泰伯利亚失败.
		//LogUtil.logTimberiumCross(player, toServerId, CrossStateType.CROSS_FAIL);
		
		//模拟login协议需要的数据.
		AccountInfo accoutnInfo = GlobalData.getInstance().getAccountInfoByPlayerId(player.getId()); 
		accoutnInfo.setLoginTime(HawkTime.getMillisecond());
		HawkProtocol loginProtocol = HawkProtocol.valueOf(HP.code.LOGIN_C_VALUE, player.getHpLogin());
		player.getSession().setUserObject("account", accoutnInfo);
		loginProtocol.bindSession(player.getSession());		
		player.onProtocol(loginProtocol);
		
		//在login的时候会加，所以这减掉
		GlobalData.getInstance().changePfOnlineCnt(player, false);
		
		//如果失败了，就需要把玩家的状态给清理掉.
		boolean setCrossStatus = RedisProxy.getInstance().setPlayerCrossStatus(GsConfig.getInstance().getServerId(), player.getId(), GsConst.PlayerCrossStatus.NOTHING);
		if (!setCrossStatus) {
			Player.logger.error("playerId:{} prepare cross fail fix player set cross status fail", player.getId());
		}
		
		DungeonRedisLog.log(player.getId(), "{}",toServerId);
	}
}
