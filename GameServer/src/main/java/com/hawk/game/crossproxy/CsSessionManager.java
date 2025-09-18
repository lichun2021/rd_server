package com.hawk.game.crossproxy;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;

import com.hawk.game.GsConfig;
import com.hawk.game.crossproxy.model.CsSession;
import com.hawk.game.global.GlobalData;
import com.hawk.game.log.DungeonRedisLog;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Status;

public class CsSessionManager {

	private CsSessionManager() {

	}

	public static CsSessionManager getInstance() {
		return CsSessionManagerHolder.crossMessageEHandler;
	}

	static class CsSessionManagerHolder {
		private static CsSessionManager crossMessageEHandler = new CsSessionManager();
	}

	/**
	 * 存储所有的session
	 */
	Map<String, CsSession> allCsSessions = new ConcurrentHashMap<>(500);

	public Map<String, CsSession> getAllCsSessions() {
		return allCsSessions;
	}

	public boolean addSession(CsSession csSession) {
		return allCsSessions.put(csSession.getPlayerId(), csSession) == null;
	}

	/**
	 * 判断这个玩家是不是连接.
	 * @param playerId
	 * @return
	 */
	public boolean existSession(String playerId) {
		return allCsSessions.containsKey(playerId);
	}
	
	/**
	 * 获取Session
	 * 
	 * @param playerId
	 * @return
	 */
	public CsSession getSession(String playerId) {
		return allCsSessions.get(playerId);
	}

	/**
	 * 从Session列表里面清除
	 * 
	 * @param playerId
	 * @return
	 */
	public CsSession removeSession(String playerId) {
		return allCsSessions.remove(playerId);
	}
	
	
	public void onTick() {
		long curTime = HawkTime.getMillisecond();
		int idleTime = GsConfig.getInstance().getSessionIdleTime();
		idleTime = idleTime == 0 ? 60000 : idleTime;
		
		Iterator<CsSession> iterator = allCsSessions.values().iterator();
		CsSession csSession = null;
		while(iterator.hasNext()) {
			csSession = iterator.next();
			if (csSession.getAccessTime() * 1000l + idleTime < curTime) {
				try {
					doSessionIdle(csSession);
				} catch(Exception e) {
					HawkException.catchException(e);
				}
				
				//多清理一次
				iterator.remove();
			}		
		}
	}
	
	private void doSessionIdle(CsSession csSession) {
		HawkLog.logPrintln("cs session idle timeout playerId:{}", csSession.getPlayerId());
		Player player = GlobalData.getInstance().queryPlayer(csSession.getPlayerId());
		if (player == null) {
			HawkLog.errPrintln("do session idle player is null playerId:{}", csSession.getPlayerId());
			
			return;
		}
		
		player.kickout(Status.SysError.BACKGROUND_KICKOUT_VALUE, true, ""); 
		DungeonRedisLog.log(csSession.getPlayerId(), "kick out session:{}", csSession.hashCode());
	}
}
