package com.hawk.game.module.hospice;

import org.apache.commons.lang.StringUtils;

import com.hawk.game.entity.GuildHospiceEntity;
import com.hawk.game.module.hospice.IHospiceState.State;
import com.hawk.game.msg.HospiceQueueFinishMsg;
import com.hawk.game.player.Player;

public class HospiceObj {
	private GuildHospiceEntity guildHospiceEntity;
	private IHospiceState state;

	public void syncInfo(Player player) {
		state.syncInfo(player, this);

	}

	public void onTick(Player player) {
		long power = player.getData().getPowerElectric().getPowerData().getArmyBattlePoint();
		if (getGuildHospiceEntity().getMaxPower() < power) {
			getGuildHospiceEntity().setMaxPower(power);
		}
		state.tick(player, this);
	}

	public void onPlayerLogin(Player player) {
		state.playerLogin(player, this);
	}

	public void setDbEntity(GuildHospiceEntity dbEntity) {
		this.guildHospiceEntity = dbEntity;
		this.guildHospiceEntity.setHospiceObj(this);
		if (StringUtils.isEmpty(dbEntity.getState())) {
			this.state = IHospiceState.valueOf(State.CONGEST);
			dbEntity.setState(state.name());
		} else {
			this.state = IHospiceState.valueOf(State.valueOf(dbEntity.getState()));
		}
	}

	public void setState(Player player, IHospiceState state) {
		this.state = state;
		this.guildHospiceEntity.setState(state.name());
		this.syncInfo(player);
	}

	public GuildHospiceEntity getGuildHospiceEntity() {
		return guildHospiceEntity;
	}

	public void setGuildHospiceEntity(GuildHospiceEntity guildHospiceEntity) {
		this.guildHospiceEntity = guildHospiceEntity;
	}

	public IHospiceState getState() {
		return state;
	}

	public void onguildhospiceMsg(HospiceQueueFinishMsg msg) {
		this.state.onGuildhospiceMsg(msg, this);

	}

}
