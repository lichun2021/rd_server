package com.hawk.game.lianmengstarwars.worldpoint;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.Objects;
import java.util.Set;

import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.tuple.HawkTuple2;
import org.hawk.tuple.HawkTuples;

import com.hawk.game.config.SWHeadQuartersCfg;
import com.hawk.game.lianmengstarwars.SWBattleRoom;
import com.hawk.game.lianmengstarwars.SWConst.SWOverType;
import com.hawk.game.lianmengstarwars.roomstate.SWGameOver;
import com.hawk.game.log.DungeonRedisLog;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.protocol.Const.NoticeCfgId;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.chat.ChatParames;

/**
 * 司令部
 *
 */
public class SWHeadQuarters extends ISWBuilding {
	private Set<String> notice177Pushed = new HashSet<>();
	private Set<String> notice178Pushed = new HashSet<>();

	public SWHeadQuarters(SWBattleRoom parent) {
		super(parent);
	}

	@Override
	public boolean onTick() {
		super.onTick();
		sendNotice();
		checkWin();
		return true;
	}

	private void checkWin() {
		try {
			HawkTuple2<String, Long> topContinulControl = topControlGuild(); // 累计控制最多盟
			String winGuild = null;
			SWOverType overType = null;
			if (getParent().getCurTimeMil() > getParent().getOverTime()) {// 时间到结束
				winGuild = topContinulControl.first;
				overType = SWOverType.TIMEOVER;
				DungeonRedisLog.log(getParent().getId(), "SW GAME OVER time over guildId: {} , battleId: {}", winGuild, getParent().getId());
			}
			if (getLastControlTime() > getParent().getCfg().getConControlWin()) {// 连续控制时间
				winGuild = getLastControlGuild();
				overType = SWOverType.CTCROL;
				DungeonRedisLog.log(getParent().getId(),"SW GAME OVER control 1/4 guildId: {} , battleId: {}", winGuild, getParent().getId());
			}

			if (topContinulControl.second > getParent().getCfg().getAccControlWin()) {
				winGuild = topContinulControl.first;
				overType = SWOverType.LJCROL;
				DungeonRedisLog.log(getParent().getId(),"SW GAME OVER total 1/2 guildId: {} , battleId: {}", winGuild, getParent().getId());
			}

			if (Objects.nonNull(winGuild)) {
				getParent().setWinGuild(winGuild);
				getParent().setOverType(overType);
				getParent().setState(new SWGameOver(getParent()));
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	private void sendNotice() {
		try {
			if (getParent().isHasNotBrodcast_SW_183() && getParent().getCurTimeMil() > getProtectedEndTime()) {
				getParent().setHasNotBrodcast_SW_183(false);

				ChatParames parames = ChatParames.newBuilder().setChatType(ChatType.CHAT_FUBEN_SPECIAL_BROADCAST).setKey(NoticeCfgId.SW_183).build();
				getParent().addWorldBroadcastMsg(parames);
			}

			final long Tenmills = TimeUnit.MINUTES.toMillis(10);
			if (!notice177Pushed.contains(getLastControlGuild())) {
				if (getParent().getCfg().getConControlWin() - getLastControlTime() < Tenmills) {
					ChatParames parames = ChatParames.newBuilder().setChatType(ChatType.CHAT_FUBEN_SPECIAL_BROADCAST)
							.setKey(NoticeCfgId.SW_177).addParms(getGuildTag()).addParms(getGuildName()).addParms(getLastControlTime()).build();
					getParent().addWorldBroadcastMsg(parames);
					notice177Pushed.add(getLastControlGuild());
				}
			}
			if (!notice178Pushed.contains(getLastControlGuild())) {
				long leijiControl = getControlGuildTimeMap().get(getLastControlGuild());
				if (getParent().getCfg().getAccControlWin() - leijiControl < Tenmills) {
					ChatParames parames = ChatParames.newBuilder().setChatType(ChatType.CHAT_FUBEN_SPECIAL_BROADCAST)
							.setKey(NoticeCfgId.SW_178).addParms(getGuildTag()).addParms(getGuildName()).addParms(leijiControl).build();
					getParent().addWorldBroadcastMsg(parames);
					notice178Pushed.add(getLastControlGuild());
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	private HawkTuple2<String, Long> topControlGuild() {
		HawkTuple2<String, Long> result = HawkTuples.tuple("", 0L);
		for (Entry<String, Long> ent : getControlGuildTimeMap().asMap().entrySet()) {
			if (Objects.isNull(result) || result.second < ent.getValue()) {
				result = HawkTuples.tuple(ent.getKey(), ent.getValue());
			}
		}
		return result;
	}

	public static SWHeadQuartersCfg getCfg() {
		return HawkConfigManager.getInstance().getKVInstance(SWHeadQuartersCfg.class);
	}

	@Override
	public WorldPointType getPointType() {
		return WorldPointType.SW_HEADQUARTERS;
	}

	@Override
	public int getControlCountDown() {
		return getCfg().getControlCountDown();
	}

	@Override
	public double getGuildHonorPerSecond() {
		return getCfg().getGuildHonor();
	}

	@Override
	public double getPlayerHonorPerSecond() {
		return getCfg().getHonor();
	}

	@Override
	public double getFirstControlGuildHonor() {
		return getCfg().getFirstControlGuildHonor();
	}

	@Override
	public double getFirstControlPlayerHonor() {
		return getCfg().getFirstControlHonor();
	}

	@Override
	public int getProtectTime() {
		return getCfg().getProtectTime();
	}

	@Override
	public int getCollectArmyMin() {
		return getCfg().getCollectArmyMin();
	}
	
	@Override
	public int getWorldPointRadius() {
		return 7;
	}
}
