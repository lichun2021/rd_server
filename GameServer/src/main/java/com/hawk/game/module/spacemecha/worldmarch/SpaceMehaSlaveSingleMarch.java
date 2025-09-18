package com.hawk.game.module.spacemecha.worldmarch;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.hawk.config.HawkConfigManager;

import com.hawk.game.module.spacemecha.MechaSpaceInfo;
import com.hawk.game.module.spacemecha.SpaceMechaService;
import com.hawk.game.module.spacemecha.MechaSpaceConst.SpacePointIndex;
import com.hawk.game.module.spacemecha.config.SpaceMechaConstCfg;
import com.hawk.game.module.spacemecha.config.SpaceMechaSubcabinCfg;
import com.hawk.game.module.spacemecha.worldpoint.SpaceWorldPoint;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.SpaceMecha.SpaceMechaStage;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.GuildService;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.march.PlayerMarch;
import com.hawk.game.world.march.submarch.IPassiveAlarmTriggerMarch;
import com.hawk.game.world.march.submarch.IReportPushMarch;
import com.hawk.game.world.service.WorldPointService;

/**
 * 联盟机甲子舱体守卫单人行军
 * 
 * @author lating
 *
 */
public class SpaceMehaSlaveSingleMarch extends PlayerMarch implements MechaSpaceMarch, IReportPushMarch, IPassiveAlarmTriggerMarch {

	public SpaceMehaSlaveSingleMarch(WorldMarch marchEntity) {
		super(marchEntity);
	}

	@Override
	public WorldMarchType getMarchType() {
		return WorldMarchType.SPACE_MECHA_SLAVE_MARCH_SINGLE;
	}

	@Override
	public boolean needShowInGuildWar() {
		return true;
	}
	
	@Override
	public void onMarchReach(Player player) {
		MechaSpaceMarch.super.onMarchReach(player);
		// 删除行军报告
		//this.removeAttackReport();
		if (this.getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED_VALUE) {
			for (IWorldMarch targetMarch : alarmPointMarches()) {
				if (targetMarch.getMarchType() != WorldMarchType.SPACE_MECHA_MONSTER_ATTACK_MARCH) {
					continue;
				}
				
				if (targetMarch instanceof IReportPushMarch) {
					((IReportPushMarch) targetMarch).pushAttackReport();
				}
			}
		}
	}

	@Override
	public Set<String> attackReportRecipients() {
		return Collections.emptySet();
	}

	@Override
	public void onMarchStart() {
		MechaSpaceMarch.super.onMarchStart();
		//this.pushAttackReport();
	}

	@Override
	public void onMarchReturn() {
		MechaSpaceMarch.super.onMarchReturn();
		// 删除行军报告
		//this.removeAttackReport();
		this.removeAttackReportFromPoint(getOrigionX(), getOrigionY());
	}

	@Override
	public void remove() {
		super.remove();
		// 删除行军报告
		//this.removeAttackReport();
		this.removeAttackReportFromPoint(getTerminalX(), getTerminalY());
	}

	@Override
	public void pullAttackReport() {
//		for (IWorldMarch targetMarch : alarmPointMarches()) {
//			if (targetMarch instanceof IReportPushMarch) {
//				((IReportPushMarch) targetMarch).pushAttackReport();
//			}
//		}
	}
	
	@Override
	public void pullAttackReport(String playerId) {
//		for (IWorldMarch targetMarch : alarmPointMarches()) {
//			if (targetMarch instanceof IReportPushMarch) {
//				((IReportPushMarch) targetMarch).pushAttackReport(playerId);
//			}
//		}
	}
	
	@Override
	public boolean isSpaceStateMatch() {
		String guildId = this.getMarchEntity().getTargetId();
		MechaSpaceInfo spaceObject = SpaceMechaService.getInstance().getGuildSpace(guildId);
		SpaceMechaStage state = spaceObject.getStageVal();
		return state == SpaceMechaStage.SPACE_GUARD_1 || state == SpaceMechaStage.SPACE_PREPARE;
	}

	@Override
	public boolean marchCountLimitCheck() {
		// 每人最多只可驻守1支部队
		String playerId = this.getMarchEntity().getPlayerId();
		String guildId = GuildService.getInstance().getPlayerGuildId(playerId);
		MechaSpaceInfo spaceObject = SpaceMechaService.getInstance().getGuildSpace(guildId);
		
		SpaceWorldPoint point = (SpaceWorldPoint) WorldPointService.getInstance().getWorldPoint(this.getMarchEntity().getTerminalId());
		int index = point.getSpaceIndex();
		List<IWorldMarch> marchs = spaceObject.getSpaceMarchs(index);
		SpaceMechaSubcabinCfg cfg = SpaceMechaSubcabinCfg.getCfg(spaceObject.getLevel());
		if (marchs.size() >= cfg.getGuardLimit()) {
			return false;   
		}
		Optional<IWorldMarch> optional = marchs.stream().filter(e -> e.getPlayerId().equals(playerId)).findAny();
		return !optional.isPresent();
	}
	
	@Override
	public long getMarchNeedTime() {
		SpaceMechaConstCfg cfg = HawkConfigManager.getInstance().getKVInstance(SpaceMechaConstCfg.class);
		return cfg.getMarchTime();
	}
	
	public boolean isSelfGuildSpace() {
		String playerId = this.getMarchEntity().getPlayerId();
		String guildId = GuildService.getInstance().getPlayerGuildId(playerId);
		MechaSpaceInfo spaceObject = SpaceMechaService.getInstance().getGuildSpace(guildId);
		
		int terminalId = this.getMarchEntity().getTerminalId();
		return terminalId == spaceObject.getSpacePointId(SpacePointIndex.SUB_SPACE_1) || terminalId == spaceObject.getSpacePointId(SpacePointIndex.SUB_SPACE_2);
	}
	
	public boolean pointCheck() {
		int terminalId = this.getMarchEntity().getTerminalId();
        WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(terminalId);
        if (worldPoint == null || worldPoint.getPointType() != WorldPointType.SPACE_MECHA_SLAVE_VALUE) {
        	return false;
        }
        
        String playerId = this.getMarchEntity().getPlayerId();
		String guildId = GuildService.getInstance().getPlayerGuildId(playerId);
		MechaSpaceInfo spaceObject = SpaceMechaService.getInstance().getGuildSpace(guildId);
        return spaceObject.getStageVal() == SpaceMechaStage.SPACE_PREPARE || spaceObject.getStageVal() == SpaceMechaStage.SPACE_GUARD_1;
	}
	
}
