package com.hawk.game.module.hospice;

import org.apache.commons.lang.StringUtils;
import org.hawk.annotation.MessageHandler;

import com.hawk.game.entity.GuildHospiceEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.msg.HospiceLostPowerMsg;
import com.hawk.game.msg.HospiceQueueFinishMsg;
import com.hawk.game.msg.WorldMoveCityMsg;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;

public class PlayerHospiceModule extends PlayerModule {
	private long count;

	public PlayerHospiceModule(Player player) {
		super(player);
	}

	@Override
	public boolean onTick() {
		if(player.isInDungeonMap()){
			return true; // 副本中不发行军
		}
		count++;
		if (count % 10 != 0) {// 一次tick大约100mm
			return true;
		}
		getHospiceObj().onTick(player);
		return super.onTick();
	}

	@Override
	protected boolean onPlayerLogin() {
		getHospiceObj().onPlayerLogin(player);
		return super.onPlayerLogin();
	}

	@MessageHandler
	private void onWorldMoveCityMsg(WorldMoveCityMsg msg) {
		getHospiceObj().syncInfo(player);
	}

	@MessageHandler
	private void onLostPowerMsg(HospiceLostPowerMsg msg) {
		Player attacker = GlobalData.getInstance().makesurePlayer(msg.getAtttackerId());
		if (player.isCsPlayer() || attacker == null || attacker.isCsPlayer()) {
			return;
		}

		GuildHospiceEntity dbEntity = getHospiceObj().getGuildHospiceEntity();
		long lostPower = (long) (dbEntity.getLostPower() + msg.getDeadSoldierPower() + msg.getInjuredSoldierPower());
		AwardItems awards = AwardItems.valueOf();
		if (StringUtils.isNotEmpty(dbEntity.getAwards())) {
			awards.addItemInfos(ItemInfo.valueListOf(dbEntity.getAwards()));
		}
		awards.addItemInfos(msg.getCurelist());
		awards.addItemInfos(msg.getDeadlist());
		dbEntity.setAwards(ItemInfo.toString(awards.getAwardItems()));

		dbEntity.setOverwhelming(msg.getOverwhelming());
		dbEntity.setLostPower(lostPower);
		dbEntity.setAttackerId(msg.getAtttackerId());
	}

	/**
	 * 关怀帮助队列结束
	 * 
	 * @return
	 */
	@MessageHandler
	private void onGuildhospiceMsg(HospiceQueueFinishMsg msg) {
		getHospiceObj().onguildhospiceMsg(msg);

	}

	private HospiceObj getHospiceObj() {
		return player.getData().getGuildHospiceEntity().getHospiceObj();
	}
}
