package com.hawk.game.module.lianmengyqzz.battleroom;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;

import com.hawk.game.log.DungeonRedisLog;
import com.hawk.game.module.lianmengyqzz.battleroom.worldpoint.YQZZBuildType;
import com.hawk.game.module.lianmengyqzz.march.cfg.YQZZBattleStageTimeCfg;
import com.hawk.game.module.lianmengyqzz.march.service.YQZZMatchService;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.protocol.Const.NoticeCfgId;
import com.hawk.game.service.chat.ChatParames;

public class YQZZBattleStageTime {
	private final YQZZBattleRoom parent;
	private List<YQZZBattleStageTimeCfg> waitNociceList = new LinkedList<>();
	private YQZZBattleStageTimeCfg curStage;

	private YQZZBattleStageTime(YQZZBattleRoom parent) {
		this.parent = parent;
	}

	public static YQZZBattleStageTime create(YQZZBattleRoom parent) {
		YQZZBattleStageTime result = new YQZZBattleStageTime(parent);
		result.init();
		return result;
	}

	public void onTick() {
		checkCurStage();

	}

	private void checkCurStage() {
		if (waitNociceList.isEmpty()) {
			return;
		}
		YQZZBattleStageTimeCfg tcfg = waitNociceList.get(0);
		if (tcfg.getStageStartTime() * 1000 + parent.getCreateTime() < parent.getCurTimeMil()) {
			curStage = waitNociceList.remove(0);
			DungeonRedisLog.log(parent.getId(), "stage changed : {}", curStage.getStageId());
			for (YQZZGuildBaseInfo binfo : parent.getBattleCamps()) {
				binfo.declareWarPoint = Math.min(binfo.declareWarPoint + curStage.getSend(), parent.getCfg().getDeclareWarOrderMax());
			}

			// TODO 看情况发送阶段变化通知notice
			NoticeCfgId stageNoticeId = curStage.getNoticeId();
			if (stageNoticeId != null) {
				// 广播通知(防守成功)
				ChatParames parames = ChatParames.newBuilder()
						.setChatType(ChatType.CHAT_FUBEN_SPECIAL_BROADCAST)
						.setKey(stageNoticeId)
						.build();
				this.parent.addWorldBroadcastMsg(parames);
			}
			YQZZMatchService.getInstance().addGuildStageControlLog(this.parent.getId(),
					curStage.getStageId(), this.parent.getLastSyncpb());
		}
	}

	private void init() {
		ConfigIterator<YQZZBattleStageTimeCfg> it = HawkConfigManager.getInstance().getConfigIterator(YQZZBattleStageTimeCfg.class);
		ArrayList<YQZZBattleStageTimeCfg> list = it.stream().sorted(Comparator.comparingInt(YQZZBattleStageTimeCfg::getStageId)).collect(Collectors.toCollection(ArrayList::new));
		waitNociceList.addAll(list);
		curStage = waitNociceList.remove(0);
	}

	public long getProtectedEndTime(YQZZBuildType type) {
		if (curStage.getStageOpenBuildList().contains(type.intValue())) {
			return 0;
		}

		for (YQZZBattleStageTimeCfg cfg : waitNociceList) {
			if (cfg.getStageOpenBuildList().contains(type.intValue())) {
				return cfg.getStageStartTime() * 1000 + parent.getCreateTime();
			}
		}

		return Long.MAX_VALUE;
	}

	public YQZZBattleStageTimeCfg getCurStage() {
		return curStage;
	}

	public void setCurStage(YQZZBattleStageTimeCfg curStage) {
		this.curStage = curStage;
	}

}
