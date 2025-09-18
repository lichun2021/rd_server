package com.hawk.game.world.thread.tasks;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hawk.os.HawkOSOperator;

import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.mail.GuildMailService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.util.AlgorithmPoint;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.WorldUtil;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.thread.WorldTask;

/**
 * 世界行军召回任务
 * @author zhenyu.shang
 * @since 2018年1月19日
 */
public class MarchCallbackTask extends WorldTask{
	
	private List<IWorldMarch> marchs;
	
	private long callbackTime;
	
	private WorldPoint worldPoint;
	
	public MarchCallbackTask(IWorldMarch march, long callbackTime, WorldPoint worldPoint) {
		super(GsConst.WorldTaskType.WORLD_MARCH_CALLBACK);
		this.marchs = new ArrayList<IWorldMarch>();
		this.marchs.add(march);
		
		this.callbackTime = callbackTime;
		this.worldPoint = worldPoint;
	}

	public MarchCallbackTask(List<IWorldMarch> marchs, long callbackTime, WorldPoint worldPoint) {
		super(GsConst.WorldTaskType.WORLD_MARCH_CALLBACK);
		this.marchs = marchs;
		this.callbackTime = callbackTime;
		this.worldPoint = worldPoint;
	}

	@Override
	public boolean onInvoke() {
		if(marchs == null || marchs.isEmpty()){
			WorldMarchService.logger.error("[MarchCallbackTask] add world march list is null or empty , marchs : {}", marchs == null ? marchs : 0);
			return false;
		}
		for (IWorldMarch march : marchs) {
			
			if (march != null && !HawkOSOperator.isEmptyString(march.getMarchId())) {
				WorldMarchService.logger.info("marchCallBack, marchId:{}", march.getMarchId());
			}
			
			// 行军途中召回
			if (march.getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_VALUE) {
				// 召回
				AlgorithmPoint point = WorldUtil.getMarchCurrentPosition(march.getMarchEntity());
				WorldMarchService.getInstance().onMarchReturn(march, callbackTime, march.getMarchEntity().getAwardItems(), march.getMarchEntity().getArmys(), point.getX(), point.getY());
				if (march.isMassJoinMarch()) {
					//返回体力
					WorldMarchService.getInstance().onMonsterRelatedMarchAction(march);
					IWorldMarch leaderMarch = WorldMarchService.getInstance().getMarch(march.getMarchEntity().getTargetId());
					if (leaderMarch != null
							&& leaderMarch.getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_WAITING_VALUE) {
						// 发邮件---发车前有人主动离开
						int icon = GuildService.getInstance().getGuildFlagByPlayerId(march.getPlayerId());
						Set<IWorldMarch> marchers = WorldMarchService.getInstance().getMassJoinMarchs(leaderMarch, false);
						if (marchers == null) {
							marchers = new HashSet<IWorldMarch>();
						}
						marchers.add(march);
						marchers.add(leaderMarch);
						for (IWorldMarch worldMarch : marchers) {
							GuildMailService.getInstance().sendMail(MailParames.newBuilder()
									.setPlayerId(worldMarch.getPlayerId())
									.setMailId(MailId.MASS_PLAYER_LEAVE)
									.addSubTitles(march.getMarchEntity().getPlayerName())
									.addContents(march.getMarchEntity().getPlayerName())
									.setIcon(icon)
									.build());
						}
					}
				}
				WorldMarchService.logger.info("world march callback success marchData:{}", march);
				return true;
			}
			WorldMarchService.logger.info("world march callback in MarchCallbackTask, march:{}", march);
			march.onMarchCallback(callbackTime, worldPoint);
		}
		return true;
	}

}
