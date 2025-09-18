package com.hawk.game.module.spacemecha.worldmarch;

import java.util.Objects;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

import com.hawk.game.entity.CustomDataEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.module.spacemecha.MechaSpaceConst;
import com.hawk.game.module.spacemecha.MechaSpaceInfo;
import com.hawk.game.module.spacemecha.SpaceMechaService;
import com.hawk.game.module.spacemecha.config.SpaceMechaBoxCfg;
import com.hawk.game.module.spacemecha.config.SpaceMechaConstCfg;
import com.hawk.game.module.spacemecha.worldpoint.MechaBoxWorldPoint;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.World.StrongpointStatus;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.PlayerMarch;
import com.hawk.game.world.march.submarch.BasedMarch;
import com.hawk.game.world.service.WorldPointService;

/**
 * 联盟机甲舱体防守胜利爆出的宝箱
 * 
 * @author lating
 */
public class SpaceMechaBoxMarch extends PlayerMarch implements BasedMarch {

	public SpaceMechaBoxMarch(WorldMarch marchEntity) {
		super(marchEntity);
	}

	@Override
	public WorldMarchType getMarchType() {
		return WorldMarchType.SPACE_MECHA_BOX_COLLECT;
	}
	
	@Override
	public boolean marchHeartBeats(long currTime) {
		if (getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_COLLECT_VALUE) {
			// 采集结束的处理
			if (getMarchEntity().getMarchType() == WorldMarchType.SPACE_MECHA_BOX_COLLECT_VALUE) {
				if (getMarchEntity().getResEndTime() != 0 && getMarchEntity().getResEndTime() <= currTime) {
					collectEnd();
					return true;
				}
			}
			return true;
		}
		
		return false;
	}

	@Override
	public void onMarchReach(Player player) {
		String playerId = player.getId();
		String guildId = player.getGuildId();
		// 不在联盟了
		if (HawkOSOperator.isEmptyString(guildId)) {
			WorldMarchService.getInstance().onPlayerNoneAction(this, this.getMarchEntity().getReachTime());
			return;
		}
		
		// 舱体不在了
		MechaSpaceInfo spaceObj = SpaceMechaService.getInstance().getGuildSpace(guildId);
		if (spaceObj == null) {
			WorldMarchService.getInstance().onPlayerNoneAction(this, this.getMarchEntity().getReachTime());
			return;
		} 

		WorldMarch march = getMarchEntity();
		WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(march.getTerminalId());
		// 宝箱点不存在了
		if (Objects.isNull(worldPoint)) {
			collectFailed(playerId, march.getTargetId(), spaceObj.getLevel());
			return;
		}
		
		// 不是宝箱点
		if (worldPoint.getPointType() != WorldPointType.SPACE_MECHA_BOX_VALUE) {
			collectFailed(playerId, march.getTargetId(), spaceObj.getLevel());
			return;
		}
		
		// 不是自己联盟的宝箱
		if (!guildId.equals(worldPoint.getGuildId())) {
			collectFailed(playerId, march.getTargetId(), spaceObj.getLevel());
			return;
		}
		
		// 有人在里面了
		if (!HawkOSOperator.isEmptyString(worldPoint.getMarchId())) {
			collectFailed(playerId, march.getTargetId(), spaceObj.getLevel());
			return;
		}
		
		// 配置错误
		SpaceMechaBoxCfg boxCfg = HawkConfigManager.getInstance().getConfigByKey(SpaceMechaBoxCfg.class, worldPoint.getResourceId());
		if (Objects.isNull(boxCfg)) {
			collectFailed(playerId, march.getTargetId(), spaceObj.getLevel());
			return;
		}
		
		HawkLog.logPrintln("spaceMecha collect box start, guildId: {}, playerId: {}, posX: {}, poxY: {}, boxId: {}", worldPoint.getGuildId(), march.getPlayerId(), worldPoint.getX(), worldPoint.getY(), boxCfg.getId());
		
		// 开始采集
		onMarchStop(WorldMarchStatus.MARCH_STATUS_MARCH_COLLECT_VALUE, march.getArmys(), worldPoint);
		// 初始化玩家和行军信息
		worldPoint.initPlayerInfo(player.getData());
		worldPoint.setMarchId(march.getMarchId());
		// 通知场景点的变化
		WorldPointService.getInstance().getWorldScene().update(worldPoint.getAoiObjId());
		// 记录采集打点日志
		SpaceMechaService.getInstance().logSpaceMechaCollect(player, boxCfg.getId());
	}
	
	/**
	 * 采集失败
	 * 
	 * @param playerId
	 * @param level
	 */
	private void collectFailed(String playerId, String targetId, int level) {
		WorldMarchService.getInstance().onPlayerNoneAction(this, this.getMarchEntity().getReachTime());
		int boxId = HawkOSOperator.isEmptyString(targetId) ? 0 : Integer.parseInt(targetId);
		SystemMailService.getInstance().sendMail(MailParames.newBuilder()
				.setPlayerId(playerId)
				.setMailId(MailId.SPACE_MECHA_BOX_COLLECT_FAILED) 
				.addContents(new Object[] { level, boxId })
				.build());
	}
	
	@Override
	public long getMarchNeedTime() {
		SpaceMechaConstCfg cfg = HawkConfigManager.getInstance().getKVInstance(SpaceMechaConstCfg.class);
		return cfg.getMarchTime();
	}
	
	@Override
	public void detailMarchStop(WorldPoint targetPoint) {
		WorldMarch march = getMarchEntity();
		MechaBoxWorldPoint worldPoint = (MechaBoxWorldPoint) WorldPointService.getInstance().getWorldPoint(march.getTerminalId());
		SpaceMechaBoxCfg boxCfg = HawkConfigManager.getInstance().getConfigByKey(SpaceMechaBoxCfg.class, worldPoint.getBoxId());
		long currentTime = HawkTime.getMillisecond();
		march.setResStartTime(currentTime);
		march.setLastExploreTime(currentTime);
		march.setResEndTime(march.getResStartTime() + boxCfg.getGatherTime() * 1000L);
		worldPoint.setMarchId(this.getMarchId());
		worldPoint.setCollectPlayerId(march.getPlayerId());
		worldPoint.setCollectPlayerName(march.getPlayerName());
		worldPoint.setCollectEndTime(march.getResEndTime());
	}
	
	/**
	 * 采集结束
	 */
	private void collectEnd() {
		WorldMarch march = getMarchEntity();
		WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(march.getTerminalId());
		if (worldPoint == null) {
			WorldMarchService.getInstance().onMarchReturn(this, HawkTime.getMillisecond(), 0);
			return;
		}
		
		MechaSpaceInfo spaceObj = SpaceMechaService.getInstance().getGuildSpace(worldPoint.getGuildId());
		try {
			HawkLog.logPrintln("spaceMecha collect box end, guildId: {}, playerId: {}, posX: {}, poxY: {}, boxId: {}", worldPoint.getGuildId(), march.getPlayerId(), worldPoint.getX(), worldPoint.getY(), march.getTargetId());
			SpaceMechaBoxCfg boxCfg = HawkConfigManager.getInstance().getConfigByKey(SpaceMechaBoxCfg.class, Integer.parseInt(march.getTargetId()));
			AwardItems tmpAward = AwardItems.valueOf();
			tmpAward.addItemInfos(ItemInfo.valueListOf(boxCfg.getReward()));
			
			int awardTimes = 0;
			Player player = GlobalData.getInstance().makesurePlayer(march.getPlayerId());
			if (player != null) {
				CustomDataEntity customData = SpaceMechaService.getInstance().getCustomDataEntity(player, MechaSpaceConst.PERSONAL_BOX_AWARD_TOTAL);
				awardTimes = customData.getValue();
				customData.setValue(customData.getValue() + 1);
			}
			
			SpaceMechaConstCfg constCfg = HawkConfigManager.getInstance().getKVInstance(SpaceMechaConstCfg.class);
			if (awardTimes < constCfg.getBoxAwardLimit()) {
				SystemMailService.getInstance().sendMail(MailParames.newBuilder()
						.setPlayerId(march.getPlayerId())
						.setMailId(MailId.SPACE_MECHA_BOX_COLLECT_AWARD) 
						.setAwardStatus(MailRewardStatus.NOT_GET)
						.addContents(new Object[] { spaceObj.getLevel() })
						.addRewards(tmpAward.getAwardItems())
						.build());
			} 
		} catch (Exception e) {
			HawkException.catchException(e, march.getPlayerId());
		}

		// 行军返回
		WorldMarchService.getInstance().onMarchReturn(this, HawkTime.getMillisecond(), 0);
		spaceObj.removeBoxPoint(worldPoint.getId());
		WorldPointService.getInstance().removeWorldPoint(worldPoint.getId(), true);
		// 所有的点都已经采集完了，提前结束
		if (spaceObj.getBoxPointIdSet().isEmpty() && spaceObj.getStage() != null) {
			spaceObj.getStage().stageEnd();
		} 
	}
	
	/**
	 * 行军召回
	 */
	public void onMarchCallback(long callbackTime, WorldPoint worldPoint) {
		MechaBoxWorldPoint mechaBoxPoint = (MechaBoxWorldPoint) worldPoint;
		if (this.getMarchEntity().getPlayerId().equals(mechaBoxPoint.getCollectPlayerId())) {
			mechaBoxPoint.setCollectPlayerId("");
			mechaBoxPoint.setCollectPlayerName("");
			mechaBoxPoint.setCollectEndTime(0);
			long thisTickTime = HawkTime.getMillisecond() - this.getMarchEntity().getResStartTime();
			thisTickTime = thisTickTime > worldPoint.getLastActiveTime() ? worldPoint.getLastActiveTime() : thisTickTime;
			worldPoint.setLastActiveTime((long) (worldPoint.getLastActiveTime() - thisTickTime));
			worldPoint.setMarchId("");
			worldPoint.setPlayerId("");
			worldPoint.setPlayerName("");
			worldPoint.setPlayerIcon(0);
			worldPoint.setCityLevel(0);
			worldPoint.setPointStatus(StrongpointStatus.SP_EMPTY_VALUE);
		}

		WorldPointService.getInstance().getWorldScene().update(worldPoint.getAoiObjId());
		WorldMarchService.getInstance().onMarchReturn(this, callbackTime, getMarchEntity().getAwardItems(), getMarchEntity().getArmys(), 0, 0);
	}
	
	public void moveCityProcess(long currentTime) {
		WorldMarchService.getInstance().accountMarchBeforeRemove(this);
		WorldMarch march = getMarchEntity();
		WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(march.getTerminalId());
		if (worldPoint == null) {
			return;
		}
		
		MechaBoxWorldPoint mechaBoxPoint = (MechaBoxWorldPoint) worldPoint;
		if (march.getPlayerId().equals(mechaBoxPoint.getCollectPlayerId())) {
			mechaBoxPoint.setCollectPlayerId("");
			mechaBoxPoint.setCollectPlayerName("");
			mechaBoxPoint.setCollectEndTime(0);
			long thisTickTime = HawkTime.getMillisecond() - this.getMarchEntity().getResStartTime();
			thisTickTime = thisTickTime > worldPoint.getLastActiveTime() ? worldPoint.getLastActiveTime() : thisTickTime;
			worldPoint.setLastActiveTime((long) (worldPoint.getLastActiveTime() - thisTickTime));
			worldPoint.setMarchId("");
			worldPoint.setPlayerId("");
			worldPoint.setPlayerName("");
			worldPoint.setPlayerIcon(0);
			worldPoint.setCityLevel(0);
			worldPoint.setPointStatus(StrongpointStatus.SP_EMPTY_VALUE);
		}
		WorldPointService.getInstance().getWorldScene().update(worldPoint.getAoiObjId());
	}

}
