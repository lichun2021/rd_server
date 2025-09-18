package com.hawk.game.crossproxy.model;

import org.hawk.annotation.MessageHandler;
import org.hawk.log.HawkLog;
import org.hawk.xid.HawkXID;

import com.hawk.game.GsApp;
import com.hawk.game.crossproxy.CsSessionManager;
import com.hawk.game.msg.DailyDataClearMsg;
import com.hawk.game.msg.PlayerAssembleMsg;
import com.hawk.game.msg.PlayerLoginMsg;
import com.hawk.game.msg.RefreshEffectMsg;
import com.hawk.game.msg.SessionClosedMsg;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Login.HPLogin.Builder;
import com.hawk.game.protocol.Status;
import com.hawk.game.service.GuildService;
import com.hawk.game.util.GsConst;

/**
 * 底层协议注册的时候,只扫描了本类自己定义的函数,继承的不算. 所以Player这个类中监听的协议,的消息, csPlayer是收不到的
 * 切记切结!!!!!!!!!! 子module不影响.
 *
 */
public class CsPlayer extends Player {
	public CsPlayer(HawkXID xid) {
		super(xid);
	}

	@Override
	public boolean isCsPlayer() {
		return true;
	}
	
	/**
	 * 跨服类型.
	 */
	private int crossType; 
	
	/**
	 *检测是否可以退出跨服.
	 * 
	 * @return
	 */
	public int checkExit() {
		
		if (getCrossStatus() != GsConst.PlayerCrossStatus.EXIT_CROSS_MARCH_FINAL) {
			return Status.CrossServerError.CROSS_HAVE_MARCH_VALUE;
		}
		
		return Status.SysError.SUCCESS_OK_VALUE;
	}

	@Override
	public void kickout(int reason, boolean notify, String msg) {
		if (getData().getPlayerEntity() != null) {
			HawkLog.logPrintln("player been kickout, playerId: {}, reason: {}", getData().getPlayerEntity().getId(), reason);
		}

		if (notify) {
			notifyPlayerKickout(reason, msg);
		}

		if (session != null) {
			// 把回话绑定的玩家对象解除, 不再进行本回话的协议处理响应
			session.setAppObject(null);
			
			GsApp.getInstance().postMsg(this, SessionClosedMsg.valueOf());
			
			//处理session相关
			CsSession csSession = (CsSession)session;
			CsSessionManager.getInstance().removeSession(csSession.getPlayerId());					
		}
	}
	/**
	 * 该次登录请求是否由跨服发起.
	 * 
	 * @return
	 */
	public boolean isFirstCrossServerLogin() {
		Builder hpLogin = this.getHpLogin();
		if (hpLogin == null) {
			HawkLog.errPrintln("occur error hp login is null xid:{}", this.getXid());
			return false;
		}
		
		return CsPlayerUtil.isFirstCrossLogin(hpLogin.build());
	}
	
	
	public boolean isFirstCrossServerLogin(int crossType) {
		Builder hpLogin = this.getHpLogin();
		if (hpLogin == null) {
			HawkLog.errPrintln("occur error hp login is null xid:{}", this.getXid());
			return false;
		}
		return CsPlayerUtil.isFirstCrossLogin(hpLogin.build(), crossType); 
	}
	
	
	@Override
	public String getGuildId() {
		String guildId = GuildService.getInstance().getCPlayerGuildId(getId());
		return guildId;
	}

	@MessageHandler
	@Override
	public void refreshEffectEvent(RefreshEffectMsg msg) {
		super.refreshEffectEvent(msg);
	}
	
	@MessageHandler
	@Override
	public boolean onDailyDataClearMsg(DailyDataClearMsg msg) {
		return super.onDailyDataClearMsg(msg);
	}
	
	@MessageHandler
	@Override
	public boolean onSessionClosedMsg(SessionClosedMsg msg) {
		return super.onSessionClosedMsg(msg);
	}
	
	@MessageHandler
	@Override
	public boolean onPlayerLoginMsg(PlayerLoginMsg msg) {
		return super.onPlayerLoginMsg(msg);
	}
	
	@MessageHandler
	@Override
	public boolean onPlayerAssembleMsg(PlayerAssembleMsg msg) {
		return super.onPlayerAssembleMsg(msg);
	}
	
	public boolean isCrossType(int... crossTypes) {
		for (int crossType : crossTypes) {
			if (crossType == this.crossType) {
				return true;
			}
		}
		return false;
	}

	public int getCrossType() {
		return crossType;
	}

	public void setCrossType(int crossType) {
		this.crossType = crossType;
	}
	
	@Override
	public CsPlayer getCsPlayer() {
		return this;
	}
}
