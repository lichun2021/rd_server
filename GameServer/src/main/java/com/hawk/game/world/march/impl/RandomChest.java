package com.hawk.game.world.march.impl;

import java.util.Objects;

import org.hawk.config.HawkConfigManager;
import org.hawk.delay.HawkDelayAction;
import org.hawk.os.HawkTime;

import com.hawk.game.GsApp;
import com.hawk.game.config.WorldEnemyCfg;
import com.hawk.game.config.WorldMapConstProperty;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.item.AwardItems;
import com.hawk.game.module.PlayerManorWarehouseModule;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.Const.PushMsgType;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.service.MissionService;
import com.hawk.game.service.PushService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.GsConst.MissionFunType;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.PlayerMarch;
import com.hawk.game.world.march.submarch.BasedMarch;
import com.hawk.game.world.service.WorldPointService;

/**
 * 随机宝箱
 * 
 * @author lwt
 * @date 2017年9月18日
 */
public class RandomChest extends PlayerMarch implements BasedMarch {

	public RandomChest(WorldMarch marchEntity) {
		super(marchEntity);
	}

	@Override
	public WorldMarchType getMarchType() {
		return WorldMarchType.RANDOM_BOX;
	}

	@Override
	public void onMarchReach(Player player) {
		String playerId = player.getId();
		long lastOpen = LocalRedis.getInstance().lastOpenRandomChest(playerId);
		int cdTime = WorldMapConstProperty.getInstance().getRandomBoxGetCd() * 1000;
		long passed = HawkTime.getMillisecond() - lastOpen;
		if (passed < cdTime) {
			WorldMarchService.getInstance().onPlayerNoneAction(this, this.getMarchEntity().getReachTime());
			SystemMailService.getInstance().sendMail(MailParames.newBuilder()
					.setPlayerId(playerId)
					.setMailId(MailId.RANDOM_BOX_CD)
					.addContents(cdTime - passed)
					.build());
			return;
		}

		WorldMarch march = getMarchEntity();
		// 点和怪信息
		WorldPoint point = WorldPointService.getInstance().getWorldPoint(march.getTerminalId());
		if (Objects.isNull(point)) {
			WorldMarchService.getInstance().onPlayerNoneAction(this, this.getMarchEntity().getReachTime());
			SystemMailService.getInstance().sendMail(MailParames.newBuilder()
					.setPlayerId(playerId)
					.setMailId(MailId.RANDOM_BOX_DISMISS)
					.build());
			return;
		}
		WorldEnemyCfg monsterCfg = HawkConfigManager.getInstance().getConfigByKey(WorldEnemyCfg.class, Integer.parseInt(march.getTargetId()));
		if (Objects.isNull(monsterCfg)) {
			WorldMarchService.getInstance().onPlayerNoneAction(this, this.getMarchEntity().getReachTime());
			return;
		}

		// 玩家胜利，发放奖励和邮件
		// 击杀奖励
		AwardItems tmpAward = AwardItems.valueOf();
		tmpAward.addAwards(monsterCfg.getKillAwards());

		SystemMailService.getInstance().sendMail(MailParames.newBuilder()
				.setPlayerId(playerId)
				.setMailId(MailId.RANDOM_BOX_SUCC)
				.setAwardStatus(MailRewardStatus.NOT_GET)
				.addContents(monsterCfg.getId())
				.addRewards(tmpAward.getAwardItems())
				.build());
		LocalRedis.getInstance().openRandomChest(getPlayerId());

		// 触发领取世界随机宝箱的任务
		MissionService.getInstance().missionRefresh(player, MissionFunType.FUN_GET_WORLD_BOX, 0, 1);

		// 战斗结束, 行军返回
		WorldMarchService.getInstance().onPlayerNoneAction(this, this.getMarchEntity().getReachTime());

		PlayerManorWarehouseModule module = player.getModule(GsConst.ModuleType.WARE_HOUSE);
		module.pushBoxCooling();
		// 删除原有的怪点数据
		WorldPointService.getInstance().removeWorldPoint(point.getX(), point.getY());

		// 冷却时如玩家不在线,通知上线领取
		GsApp.getInstance().addDelayAction(cdTime, new HawkDelayAction() {
			@Override
			protected void doAction() {
				// 推送消息这块，PushService中会统一去判断玩家是否在线，所有调用的地方都不用自己判断
				PushService.getInstance().pushMsg(playerId, PushMsgType.WORLD_BOX_CD_END_VALUE);
			}
		});
	}

}
