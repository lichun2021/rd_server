package com.hawk.game.world.march.impl;

import org.hawk.os.HawkTime;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.HeroResourceCollectEvent;
import com.hawk.game.crossactivity.CrossActivityService;
import com.hawk.game.guild.manor.ManorMarchEnum;
import com.hawk.game.guild.manor.building.GuildManorSuperMine;
import com.hawk.game.item.AwardItems;
import com.hawk.game.log.BehaviorLogger;
import com.hawk.log.Action;
import com.hawk.game.log.BehaviorLogger.Params;
import com.hawk.log.Source;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.service.ArmyService;
import com.hawk.game.service.GuildManorService;
import com.hawk.game.util.WorldUtil;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.PlayerMarch;
import com.hawk.game.world.march.submarch.CollectMarch;
import com.hawk.game.world.march.submarch.ManorMarch;
import com.hawk.game.world.service.WorldPointService;

/**
 * 联盟超级矿采集行军类型
 * @author zhenyu.shang
 * @since 2017年8月28日
 */
public class ManorCollectMarch extends PlayerMarch implements ManorMarch, CollectMarch{

	public ManorCollectMarch(WorldMarch marchEntity) {
		super(marchEntity);
	}

	@Override
	public WorldMarchType getMarchType() {
		return WorldMarchType.MANOR_COLLECT;
	}

	@Override
	public void onMarchReach(Player player) {
		WorldMarch march = getMarchEntity();
		WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(march.getTerminalId());
		if (worldPoint == null || !WorldUtil.isGuildBuildPoint(worldPoint)) { // 点类型错误，直接返回
			WorldMarchService.getInstance().onPlayerNoneAction(this, march.getReachTime());
			return;
		}
		GuildManorSuperMine mine = (GuildManorSuperMine) GuildManorService.getInstance().getAllBuildings().get(worldPoint.getGuildBuildId());
		if(mine == null){
			WorldMarchService.getInstance().onPlayerNoneAction(this, march.getReachTime());
			return;
		}
		// 获取行军枚举
		ManorMarchEnum manorMarch = ManorMarchEnum.valueOf(march.getMarchType());
		if (manorMarch == null) {
			mine.removeCollectMarch(this.getMarchId());
			WorldMarchService.getInstance().onPlayerNoneAction(this, HawkTime.getMillisecond());
			GuildManorService.logger.error("world march collect reach, ManorMarch error, ManorMarch is null, marchType:{}", march.getMarchType());
			return;
		}
		// 不同类型的行军各自检查
		int resCode = manorMarch.checkMarch(worldPoint, player, true);
		if (resCode != Status.SysError.SUCCESS_OK_VALUE) {
			mine.removeCollectMarch(this.getMarchId());
			WorldMarchService.getInstance().onPlayerNoneAction(this, HawkTime.getMillisecond());
			GuildManorService.logger.error("world march collect reach, resCode error, resCode is {}, march is {}", resCode, march);
			return;
		}
		this.onMarchStop(WorldMarchStatus.MARCH_STATUS_MARCH_COLLECT_VALUE, null, worldPoint);
		//通知到达
		manorMarch.onMarchReach(worldPoint, this);
		if (!march.getHeroIdList().isEmpty() ) {
			ActivityManager.getInstance().postEvent(new HeroResourceCollectEvent(player.getId()));
		}
		// 行为日志
		BehaviorLogger.log4Service(player, Source.WORLD_ACTION, Action.WORLD_MANOR_COLLECT_MARCH_REACH,
				Params.valueOf("marchType", march.getMarchType()),
				Params.valueOf("superMine", mine),
				Params.valueOf("marchData", march));
	}
	
	@Override
	public void onMarchCallback(long callbackTime, WorldPoint worldPoint) {
		// 获取当前行军的目标矿
		GuildManorSuperMine mine = (GuildManorSuperMine) GuildManorService.getInstance().getAllBuildings().get(worldPoint.getGuildBuildId());
		// 行军返回
		WorldMarchService.getInstance().onPlayerNoneAction(this, callbackTime);
		// 移除march
		mine.removeCollectMarch(this.getMarchId());
	}
	
	@Override
	public boolean beforeImmediatelyRemoveMarchProcess(Player player) {
		// 无兵行军不进行资源结算， 直接返回
		if (WorldUtil.getFreeArmyCnt(getMarchEntity().getArmys()) <= 0) {
			ArmyService.getInstance().onArmyBack(player, getMarchEntity().getArmys(), getMarchEntity().getHeroIdList(),getMarchEntity().getSuperSoldierId(), this);
			return false;
		}
		AwardItems items = getMarchEntity().getAwardItems();
		if (items != null && items.getAwardItems().size() > 0) {
			refreshCollectMission(player, items, getMarchEntity()); // 刷新任务
			
			// 交税
			String hasTax = CrossActivityService.getInstance().addTax(player, items);
			
			sendCollectMail(getMarchEntity(), hasTax); // 发邮件
			
			// 计算石油转化作用号(注意会改变award，在发奖前调用)
			player.calcOilChangeEff(items);
			
			items.rewardTakeAffectAndPush(player, Action.WORLD_COLLECT_RES, false); // 发奖
		}
		return true;
	}
}
