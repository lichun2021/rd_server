package com.hawk.game.world.march.impl;

import org.hawk.app.HawkApp;
import org.hawk.config.HawkConfigManager;
import org.hawk.net.protocol.HawkProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.VitCostEvent;
import com.hawk.game.config.AgencyConstCfg;
import com.hawk.game.config.AgencyEventCfg;
import com.hawk.game.config.AgencyLevelCfg;
import com.hawk.game.config.WorldMarchConstProperty;
import com.hawk.game.entity.AgencyEntity;
import com.hawk.game.entity.AgencyEventEntity;
import com.hawk.game.item.AwardItems;
import com.hawk.game.msg.AgencyFinishMsg;
import com.hawk.game.msg.PlayerVitCostMsg;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Agency.AgencyEventState;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.Const.Result;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.World.HPBattleResultInfoSync;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.util.GameUtil;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.PlayerMarch;
import com.hawk.game.world.march.submarch.BasedMarch;
import com.hawk.game.world.object.Point;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.log.Action;

public class AgencyCoasterMarch extends PlayerMarch implements BasedMarch {
	
	/**
	 * 日志
	 */
	public static Logger logger = LoggerFactory.getLogger("Server");
	
	public AgencyCoasterMarch(WorldMarch marchEntity) {
		super(marchEntity);
	}

	/**
	 * 获取行军类型
	 */
	@Override
	public WorldMarchType getMarchType() {
		return WorldMarchType.AGENCY_MARCH_COASTER;
	}
	
	
	@Override
	protected double getPartMarchTime(double distance, double speed, boolean isSlowDownPart) {
		AgencyEntity agency = this.getPlayer().getData().getAgencyEntity();
		int agencyLevel = agency.getCurrLevel();
		AgencyLevelCfg cfg = HawkConfigManager.getInstance().getConfigByKey(AgencyLevelCfg.class, agencyLevel);
		if(cfg != null){
			double factor = 1;
			if (isSlowDownPart) {
				factor = WorldMarchConstProperty.getInstance().getWorldMarchCoreRangeTime();
			}
			// 行军距离修正参数
			double param1 = WorldMarchConstProperty.getInstance().getDistanceAdjustParam();
			// 部队行军类型行军时间调整参数
			double param2 = cfg.getTimeModulus2();
			speed = 1.0d;
			return Math.pow((distance), param1) * param2 * factor / speed;
		}
		return super.getPartMarchTime(distance, speed, isSlowDownPart);
	}

	
	@Override
	public long getMarchNeedTime() {
		if (this.getPlayer().getData().getAgencyEntity().getHasKilled() == 0) {
			return AgencyConstCfg.getInstance().getFirstEventTime() * 1000;
		}
		return super.getMarchNeedTime();
	}
	
	

	@Override
	public void onMarchReach(Player player) {
		WorldMarch march = this.getMarchEntity();
		int terminalId = getTerminalId();
		// 返回
		WorldMarchService.getInstance().onPlayerNoneAction(this, march.getReachTime());
		// 目标点被占用了
		WorldPoint point = WorldPointService.getInstance().getWorldPoint(terminalId);
		if (point != null) {
			WorldMarchService.getInstance().onMonsterRelatedMarchAction(this);
			this.sendAgencyPointChangeEmail(player);
			return;
		}
		int[] pos = GameUtil.splitXAndY(terminalId);
		Point freePoint = WorldPointService.getInstance().getAreaPoint(pos[0], pos[1], true);
		if (freePoint == null) {
			WorldMarchService.getInstance().onMonsterRelatedMarchAction(this);
			this.sendAgencyPointChangeEmail(player);
			return;
		}
		
		AgencyEventEntity agencyEvent = this.getPlayer().getData().getAgencyEntity().getAgencyEvent(march.getTargetId());
		// 事件不存在
		if (agencyEvent == null) {
			WorldMarchService.getInstance().onMonsterRelatedMarchAction(this);
			this.sendAgencyPointChangeEmail(player);
			return;
		}
		// 事件已完成
		if (agencyEvent.getEventState() == AgencyEventState.AGENCY_FINISHED_VALUE) {
			WorldMarchService.getInstance().onMonsterRelatedMarchAction(this);
			return;
		}
		this.getPlayer().getData().getAgencyEntity().setHasKilled(1);
		HawkApp.getInstance().postMsg(getPlayer(), AgencyFinishMsg.valueOf(agencyEvent.getUuid()));
		ActivityManager.getInstance().postEvent(new VitCostEvent(march.getPlayerId(), march.getVitCost()));
		HawkApp.getInstance().postMsg(getPlayer(), PlayerVitCostMsg.valueOf(march.getPlayerId(), march.getVitCost()));
		
		AgencyEventCfg cfg = HawkConfigManager.getInstance().getConfigByKey(AgencyEventCfg.class, agencyEvent.getEventId());
		
		AwardItems award = AwardItems.valueOf();
		award.addItemInfos(cfg.getActionReward());
		award.rewardTakeAffectAndPush(player, Action.AGENCY_COASTER_MARCH_REWARD, false, null);
		
		// 邮件发奖
		SystemMailService.getInstance().sendMail(MailParames.newBuilder()
				.setPlayerId(player.getId())
				.setMailId(MailId.AGENCY_COASTERMARCH)
				.setRewards(award.getAwardItems())
				.setAwardStatus(MailRewardStatus.GET)
				.build());
		
		HPBattleResultInfoSync.Builder builder = HPBattleResultInfoSync.newBuilder();
		builder.setMarchId(march.getMarchId());
		builder.setIsWin(Result.SUCCESS_VALUE);
		getPlayer().sendProtocol(HawkProtocol.valueOf(HP.code.BATTLE_INFO_S_VALUE, builder));
	}
	
	
	@Override
	public void moveCityProcess(long currentTime) {
		WorldMarchService.getInstance().accountMarchBeforeRemove(this);
		
		if (!this.isReturnBackMarch()) {
			// 打怪行军
			WorldMarchService.getInstance().onMonsterRelatedMarchAction(this);
		}
	}
	
	private void sendAgencyPointChangeEmail(Player player){
		SystemMailService.getInstance().sendMail(MailParames.newBuilder()
				.setPlayerId(player.getId())
				.setMailId(MailId.AGENCY_POINT_NULL)
				.build());
	}
}