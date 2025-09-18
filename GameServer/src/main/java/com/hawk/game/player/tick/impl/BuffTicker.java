package com.hawk.game.player.tick.impl;

import java.util.ArrayList;
import java.util.List;

import org.hawk.app.HawkApp;
import org.hawk.config.HawkConfigManager;
import org.hawk.db.HawkDBEntity;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;
import org.hawk.task.HawkTaskManager;

import com.google.common.collect.ImmutableList;
import com.hawk.game.config.EffectCfg;
import com.hawk.game.entity.StatusDataEntity;
import com.hawk.game.log.BehaviorLogger;
import com.hawk.game.log.BehaviorLogger.Params;
import com.hawk.game.msg.BuffHeroSkinEndMsg;
import com.hawk.game.msg.PlayerImageFresh;
import com.hawk.game.player.Player;
import com.hawk.game.player.tick.PlayerTickLogic;
import com.hawk.game.protocol.Const.StateType;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.GsConst.EffectType;
import com.hawk.log.Action;
import com.hawk.log.Source;

public class BuffTicker implements PlayerTickLogic {
	
	@Override
	public void onTick(Player player) {
		try {
			long currentTime = HawkApp.getInstance().getCurrentTime();
			List<StatusDataEntity> list = ImmutableList.copyOf(player.getData().getStatusDataEntities());
			if (list == null || list.size() == 0) {
				return;
			}

			List<StatusDataEntity> deleteList = new ArrayList<>();
			for (StatusDataEntity entity : list) {
				long endTime = entity.getEndTime();
				if (endTime == 0 || entity.getType() != StateType.BUFF_STATE_VALUE) {
					continue;
				}

				long leftTime = endTime - currentTime;
				if (leftTime > 0) {
					entity.resetPushed(false);
				} else if (!entity.getPushed()) {
					int statusId = entity.getStatusId();
					if ((statusId == 302 || statusId == 304 || statusId == 306 || statusId == 308) && Math.abs(leftTime) > HawkTime.DAY_MILLI_SECONDS * 2) {
						if (deleteList.size() < 10) {
							deleteList.add(entity);
						}
						continue;
					}
					endTime = player.onCityShieldChange(entity, currentTime);
					entity.resetPushed(true);
					player.onBufChange(entity.getStatusId(), endTime);
					this.buffOver(player, entity.getStatusId());
					if (entity.getVal() != GsConst.ProtectState.NO_BUFF) {
						BehaviorLogger.log4Service(player, Source.SYSTEM_OPERATION, Action.ITEM_BUF_EXPIRED, Params.valueOf("effId", entity.getStatusId()));
					}
				}

				player.cityShieldRemovePrepareNotice(entity, endTime - currentTime);
			}
			
			if (!deleteList.isEmpty()) {
				HawkLog.logPrintln("buff tick remove entity, playerId: {}, delete count: {}", player.getId(), deleteList.size());
				player.getData().getStatusDataEntities().removeAll(deleteList);
				HawkDBEntity.batchDelete(deleteList);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	public void buffOver(Player player, int statusId) {
		EffectCfg effCfg = HawkConfigManager.getInstance().getConfigByKey(EffectCfg.class, statusId);
		if (effCfg == null) {
			return;
		}
		if (effCfg.getType() == EffectType.IMAGE_ITEM.getValue()) {
			HawkTaskManager.getInstance().postMsg(player.getXid(), new PlayerImageFresh());
		} else if (effCfg.getType() == EffectType.HERO_SKIN.getValue()) {
			HawkTaskManager.getInstance().postMsg(player.getXid(), BuffHeroSkinEndMsg.valueOf(statusId));
		}
	}

}
