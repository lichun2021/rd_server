package com.hawk.game.module.dayazhizhan.battleroom.player.module;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

import org.hawk.annotation.MessageHandler;
import org.hawk.config.HawkConfigManager;

import com.google.common.base.Joiner;
import com.hawk.game.entity.ItemEntity;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.log.DungeonRedisLog;
import com.hawk.game.module.dayazhizhan.battleroom.DYZZRoomManager;
import com.hawk.game.module.dayazhizhan.battleroom.cfg.DYZZBattleCfg;
import com.hawk.game.module.dayazhizhan.battleroom.msg.DYZZJoinRoomMsg;
import com.hawk.game.module.dayazhizhan.battleroom.msg.DYZZQuitRoomMsg;
import com.hawk.game.module.dayazhizhan.battleroom.player.IDYZZPlayer;
import com.hawk.game.msg.PlayerEffectChangeMsg;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;

public class DYZZPlayerModule extends PlayerModule {

	public DYZZPlayerModule(Player player) {
		super(player);
	}

	@Override
	protected boolean onPlayerLogin() {
		IDYZZPlayer gamer = DYZZRoomManager.getInstance().makesurePlayer(player.getId());
		if (gamer != null && gamer.getParent() != null) {
			boolean needClean = gamer.getParent().getPlayer(player.getId()) == null || gamer.getParent().isGameOver();
			if (needClean) {
				gamer.getData().unLockOriginalData();
				gamer.setDYZZRoomId("");
				gamer.setDYZZState(null);
				DYZZRoomManager.getInstance().invalidate(gamer);
				return true;
			}
		}

		if (Objects.nonNull(gamer)) {
			gamer.onLogin(player);
		} else {
			DYZZBattleCfg bcfg = HawkConfigManager.getInstance().getKVInstance(DYZZBattleCfg.class);
			List<ItemEntity> list = player.getData().getItemsByItemId(bcfg.getSpeedupItem()).stream().filter(it -> it.getItemCount() > 0).collect(Collectors.toList());
			if (!list.isEmpty()) {
				list.forEach(si -> si.setItemCount(0));
				player.getPush().syncItemInfo();
			}
		}
		return super.onPlayerLogin();
	}
	
	@MessageHandler
	private void onEffectChangeEvent(PlayerEffectChangeMsg event) {
		IDYZZPlayer gamer = DYZZRoomManager.getInstance().makesurePlayer(player.getId());
		if (Objects.nonNull(gamer)) {
			gamer.getPush().syncPlayerEffect(event.getSet().toArray(new EffType[0]));
		}
	}

	@MessageHandler
	private void onJoinRoomMsg(DYZZJoinRoomMsg msg) {
		String armyStr = Joiner.on("|").join(player.getData().getArmyEntities());
		// 记录玩家兵力
//		BehaviorLogger.log4Service(player, Source.SYSTEM_OPERATION, Action.DYZZ_JOIN_ROOM,
//				Params.valueOf("Army", armyStr));
		DungeonRedisLog.log(player.getId(), "roomId {} , army {}", player.getDYZZRoomId(), armyStr);
		msg.getBattleRoom().joinRoom(msg.getPlayer());
	}

	/** 结束, 记录胜负什么的 */
	@MessageHandler
	private void onQuitRoomMsg(DYZZQuitRoomMsg msg) {
		String armyStr = Joiner.on("|").join(player.getData().getArmyEntities());
		// 记录玩家兵力
//		BehaviorLogger.log4Service(player, Source.SYSTEM_OPERATION, Action.DYZZ_QUIT_ROOM,
//				Params.valueOf("Army", armyStr));
		DungeonRedisLog.log(player.getId(), "roomId {} , army {}", msg.getRoomId(), armyStr);
		
		Map<String, String> csmap = RedisProxy.getInstance().jbsTakeOutAllCreateSoldier(player.getId());
		if (csmap.isEmpty()) {
			return;
		}
		String items = "";
		for (Entry<String, String> ent : csmap.entrySet()) {
			items += String.format("70000_%s_%s,", ent.getKey(), ent.getValue());
		}

		SystemMailService.getInstance().sendMail(MailParames.newBuilder()
				.setPlayerId(player.getId())
				.setMailId(MailId.REWARD_MAIL)
				.setAwardStatus(MailRewardStatus.NOT_GET)
				// .addSubTitles("叫爸爸!!!!")
				.addContents("DYZZ退回造兵")
				.setRewards(items)
				.build());
	}


}
