package com.hawk.game.player.skill.talent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkTime;
import org.hawk.result.Result;

import com.hawk.game.config.SkillCfg;
import com.hawk.game.global.GlobalData;
import com.hawk.game.guild.manor.building.GuildManorSuperMine;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.GuildAssistant.AssistanceCallbackNotifyPB;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.service.GuildManorService;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.game.world.service.WorldStrongPointService;

/**
 * 立即返回：所有在外的部队在x秒内返回玩家基地，不包含参与集结中的部队
 * @author golden
 *
 */
@TalentSkill(skillID = 10101)
public class Skill10101 implements ITalentSkill {

	public Result<?> onCastSkill(Player player, SkillCfg skillCfg, List<Integer> params) {
		// 玩家所有行军
		BlockingQueue<IWorldMarch> marchs = WorldMarchService.getInstance().getPlayerMarch(player.getId());

		// 非返程状态并且非集结类型的行军
		List<IWorldMarch> retMarchs = new ArrayList<>();
		// 已经是返程状态的行军
		List<IWorldMarch> returnBackMarch = new ArrayList<>();
		
		for (IWorldMarch march : marchs) {
			// 返程中的部队
			if (march.isReturnBackMarch()) {
				returnBackMarch.add(march);
				continue;
			}
			
			// 不包含参与集结中的部队
			if (!march.isMassJoinMarch() && !march.isMassMarch()) {
				retMarchs.add(march);
			}
		}
		
		// 没有出征的行军， 不能使用技能
		if (retMarchs.isEmpty() && returnBackMarch.isEmpty()) {
			return Result.fail(Status.Error.SKILL_ERROR_HAS_NO_MARCH_VALUE);
		}
		
		long marchTime = (long)(Integer.parseInt(skillCfg.getParam1()) * 1000);
		for (IWorldMarch march : retMarchs) {
			onMarchCallBack(march, marchTime);
		}
		
		for (IWorldMarch march : returnBackMarch) {
			long reamainTime = march.getMarchEntity().getEndTime() - HawkTime.getMillisecond();
			march.getMarchEntity().setEndTime(HawkTime.getMillisecond() + Math.min(reamainTime, marchTime));
			march.updateMarch();
		}
		
		return Result.success();
	}
	
	/**
	 * 行军召回。 不同类型行军不同处理
	 * @param march
	 * @param marchTime
	 */
	private void onMarchCallBack(IWorldMarch march, long marchTime) {
		long currentTime =  HawkTime.getMillisecond();
		
		// 行军途中召回
		if (march.isMarchState()) {
			WorldMarchService.getInstance().onMarchReturn(march, currentTime, marchTime);
			return;
		}

		WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(march.getMarchEntity().getTerminalX(), march.getMarchEntity().getTerminalY());
		
		if (march.getMarchType() == WorldMarchType.ARMY_QUARTERED) {
			WorldPointService.getInstance().notifyQuarteredFinish(worldPoint);
			WorldMarchService.getInstance().onMarchReturn(march, currentTime, marchTime);
			
		} else if (march.getMarchType() == WorldMarchType.COLLECT_RESOURCE) {
			WorldMarchService.getInstance().onResourceMarchCallBack(march, currentTime, march.getMarchEntity().getArmys(), marchTime);
			
		} else if (march.getMarchType() == WorldMarchType.MANOR_COLLECT) {
			GuildManorSuperMine mine = (GuildManorSuperMine) GuildManorService.getInstance().getAllBuildings().get(worldPoint.getGuildBuildId());
			WorldMarchService.getInstance().onMarchReturn(march, currentTime, marchTime);
			mine.removeCollectMarch(march.getMarchId());
		} else if (march.getMarchType() == WorldMarchType.ASSISTANCE) {
			AssistanceCallbackNotifyPB.Builder callbackNotifyPB = AssistanceCallbackNotifyPB.newBuilder();
			callbackNotifyPB.setMarchId(march.getMarchId());
			Player assistPlayer = GlobalData.getInstance().makesurePlayer(march.getMarchEntity().getTargetId());
			assistPlayer.sendProtocol(HawkProtocol.valueOf(HP.code.ASSISTANCE_MARCH_CALLBACK, callbackNotifyPB));
			WorldMarchService.getInstance().onMarchReturn(march, currentTime, marchTime);
		} else if(march.getMarchType() == WorldMarchType.YURI_EXPLORE){
			march.getMarchEntity().setResEndTime(currentTime);
			WorldMarchService.getInstance().onMarchReturn(march, currentTime, marchTime);
		} else if(march.getMarchType() == WorldMarchType.STRONGPOINT) {
			WorldStrongPointService.getInstance().doStrongpointReturn(march, march.getMarchEntity().getArmys(), false);
			long reamainTime = march.getMarchEntity().getEndTime() - HawkTime.getMillisecond();
			march.getMarchEntity().setEndTime(HawkTime.getMillisecond() + Math.min(reamainTime, marchTime));
			march.updateMarch();
		} else if (march.getMarchType() == WorldMarchType.TREASURE_HUNT_RESOURCE) {
			march.onMarchCallback(HawkTime.getMillisecond(), worldPoint);
			long reamainTime = march.getMarchEntity().getEndTime() - HawkTime.getMillisecond();
			march.getMarchEntity().setEndTime(HawkTime.getMillisecond() + Math.min(reamainTime, marchTime));
			march.updateMarch();
		} else if (march.getMarchType() == WorldMarchType.PYLON_MARCH) {
			march.onMarchCallback(HawkTime.getMillisecond(), worldPoint);
			long reamainTime = march.getMarchEntity().getEndTime() - HawkTime.getMillisecond();
			march.getMarchEntity().setEndTime(HawkTime.getMillisecond() + Math.min(reamainTime, marchTime));
			march.updateMarch();
		} else if (march.getMarchType() == WorldMarchType.CHRISTMAS_BOX_MARCH) {
			march.onMarchCallback(HawkTime.getMillisecond(), worldPoint);
			long remainTime = march.getMarchEntity().getEndTime() - HawkTime.getMillisecond();
			march.getMarchEntity().setEndTime(HawkTime.getMillisecond() + Math.min(remainTime, marchTime));
			march.updateMarch();
		} else {
			WorldMarchService.getInstance().onMarchReturn(march, currentTime, marchTime);
		}
	}
	
	@Override
	public boolean isWorldSkill() {
		return true;
	}
}
