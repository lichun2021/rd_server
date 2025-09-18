package com.hawk.game.module.lianmengyqzz.battleroom.secondmap;

import java.util.Objects;

import org.hawk.os.HawkTime;

import com.google.common.cache.CacheLoader;
import com.hawk.game.log.DungeonRedisLog;
import com.hawk.game.module.lianmengyqzz.battleroom.IYQZZWorldPoint;
import com.hawk.game.module.lianmengyqzz.battleroom.YQZZBattleRoom;
import com.hawk.game.module.lianmengyqzz.battleroom.player.IYQZZPlayer;
import com.hawk.game.module.lianmengyqzz.battleroom.worldpoint.IYQZZBuilding;
import com.hawk.game.protocol.YQZZ.PBYQZZSecondMapResp;

public class YQZZSecondMapDataLoader extends CacheLoader<String, PBYQZZSecondMapResp.Builder> {
	private final YQZZBattleRoom battleRoom;

	public YQZZSecondMapDataLoader(YQZZBattleRoom battleRoom) {
		this.battleRoom = battleRoom;
	}

	@Override
	public PBYQZZSecondMapResp.Builder load(String guildId) throws Exception {
		long beginTimeMs = HawkTime.getMillisecond();

		PBYQZZSecondMapResp.Builder bul = PBYQZZSecondMapResp.newBuilder();
		for (IYQZZWorldPoint point : battleRoom.getViewPoints()) {
			if (point instanceof IYQZZBuilding) {
				bul.addPoints(point.toSecondMapBuilder());
			}

			if (point instanceof IYQZZPlayer && Objects.equals(guildId, point.getGuildId())) {
				bul.addPoints(point.toSecondMapBuilder());
			}
		}
		bul.setGameStartTime(battleRoom.getGameStartTime());
		bul.setGameOverTime(battleRoom.getOverTime());

		bul.addAllGuildInfo(battleRoom.getLastSyncpbPlayer().getGuildInfoList());
		bul.addAllNationInfo(battleRoom.getLastSyncpbPlayer().getNationInfoList());

		long costTimeMs = HawkTime.getMillisecond() - beginTimeMs;
		if (costTimeMs > 50) {
			DungeonRedisLog.log(battleRoom.getId(), "process tick too much time, costtime: {}", costTimeMs);
		}
		return bul;
	}
}
