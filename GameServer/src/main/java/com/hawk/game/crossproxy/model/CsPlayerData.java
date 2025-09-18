package com.hawk.game.crossproxy.model;



import java.util.EnumSet;

import org.hawk.log.HawkLog;
import org.hawk.os.HawkTime;

import com.hawk.game.global.GlobalData;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.player.cache.PlayerDataKey;
import com.hawk.game.player.cache.PlayerDataSerializer;
import com.hawk.game.protocol.Cross.CrossType;

public class CsPlayerData extends PlayerData {
	/**
	 * 覆盖PlayerData方法,植入跨服自己的dataCache
	 * 
	 */
	@Override
	public boolean loadPlayerData(String playerId) {
		this.playerId = playerId;
		this.dataCache = CsPlayerDataCache.newCache(playerId);
		return true;
	}

	/**
	 * 必须知道是不是主动跨服,如果是主动跨服必须全量覆盖,否则会有问题.
	 */
	@Override
	public void loadAll(boolean isNewly) {
		if (dataCache == null) {
			throw new RuntimeException("load csplayerdata datacache is null");
		}

		long startTime = HawkTime.getMillisecond();
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (!player.isCsPlayer()) {
			
			throw new RuntimeException("player not a cs player type:"+player.getClass().getName());
		}
		
		CsPlayer csPlayer = player.getCsPlayer();
		//如果该次加载是由主动跨服发起,需要把所有的数据从redis里面读一次,  
		if (csPlayer.isFirstCrossServerLogin()) {
			//2019-09-25 已经忘记了最初这么写的顾虑, 暂时只做补丁修改, 这里应该不用buildFromRedis.
			if (csPlayer.getCrossType() == CrossType.STAR_WARS_VALUE) {
				
			} else {
				boolean result = PlayerDataSerializer.buildFromRedis(this.dataCache, false);
				if (!result) {
					throw new RuntimeException("csplayer build from redis failed");
				}
			}			
			
			this.loadData4Cross();
		} else {
			// 加载所有数据
			for (PlayerDataKey key : EnumSet.allOf(PlayerDataKey.class)) {
				if (!key.crossLoad()) {
					continue;
				}
				dataCache.makesureDate(key);
			}
		}

		// 日志记录
		HawkLog.logPrintln("csplayer login load data success, playerId: {}, costtime: {}", playerId, HawkTime.getMillisecond() - startTime);
	}
}
