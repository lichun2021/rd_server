package com.hawk.game.module.hospice;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkTime;

import com.hawk.game.config.AllianceCareCfg;
import com.hawk.game.config.BuildingCfg;
import com.hawk.game.entity.GuildHospiceEntity;
import com.hawk.game.entity.QueueEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.protocol.Const.QueueStatus;
import com.hawk.game.protocol.Const.QueueType;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.QueueService;
import com.hawk.game.util.GsConst;

/** 累积伤兵阶段 */
public class HospiceStateCONGEST implements IHospiceState {

	@Override
	public String name() {
		return IHospiceState.State.CONGEST.name();
	}

	@Override
	public void tick(Player player, HospiceObj hospiceObj) {
		AllianceCareCfg cfg = HawkConfigManager.getInstance().getConfigByKey(AllianceCareCfg.class, player.getCityLevel());
		GuildHospiceEntity dbEntity = hospiceObj.getGuildHospiceEntity();
		// if(dbEntity.getLostPower()<10){
		// return;
		// }

		Player attacker = GlobalData.getInstance().makesurePlayer(dbEntity.getAttackerId());
		if (!player.isActiveOnline() || attacker == null) {
			return;
		}
		
		if(StringUtils.isEmpty(dbEntity.getAwards())){
			return;
		}
		
		if (dbEntity.getLostPower() < cfg.getMinGrandTotal()) {
			return;
		}
		if (dbEntity.getLostPower() * 1.0 / dbEntity.getMaxPower() < cfg.getPowerCoefficientPercent()) {
			return;
		}
		int todayCount = RedisProxy.getInstance().todayHelped(player.getId());
		if (todayCount >= cfg.getAllianceCareHelpDailyLimit()) {
			return;
		}
		
		BuildingCfg conf = player.getData().getBuildingCfgByType(BuildingType.EMBASSY);
		// 设置到等待帮助队列阶段
		long costtime = TimeUnit.MINUTES.toMillis(3);// ConstProperty.getInstance().getAllianceCareHelpTime();
		if (player.hasGuild() && Objects.nonNull(conf)) {// && GuildService.getInstance().getGuildMemberNum(player.getGuildId()) > 1
			// 添加队列
			QueueEntity queue = QueueService.getInstance().addReusableQueue(player,
					QueueType.GUILD_HOSPICE_QUEUE_VALUE, QueueStatus.QUEUE_STATUS_COMMON_VALUE, "",
					0, costtime, null, GsConst.QueueReusage.GUILD_HOSPICE_QUEUE);
			// 添加帮助
			GuildService.getInstance().applyGuildHelp(player.getGuildId(), player, queue, attacker.getName());
			dbEntity.setHelpQueue(queue.getId());
		}
		long now = HawkTime.getMillisecond();
		dbEntity.setMatchStartTime(now);
		dbEntity.setMatchEndTime(now + costtime + 3000);
		hospiceObj.setState(player, IHospiceState.valueOf(State.MARCH));
		RedisProxy.getInstance().incPlayerHelped(player.getId());
		return;

	}

}
