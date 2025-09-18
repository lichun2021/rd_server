package com.hawk.game.module.staffofficer;

import org.hawk.annotation.MessageHandler;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.game.msg.HeroChangedMsg;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Hero.PBStaffOfficeSync;

public class PlayerStaffOfficerModule extends PlayerModule {

	public PlayerStaffOfficerModule(Player player) {
		super(player);
	}

	@Override
	protected boolean onPlayerLogin() {
		sync();
		return super.onPlayerLogin();
	}

	@MessageHandler
	private void onEffectChangeEvent(HeroChangedMsg event) {
		if (event.getHero().getConfig().getStaffOfficer() == 1) {
			player.getStaffOffic().refresh(player.getData());
			sync();
		}
	}

	private void sync() {
		PBStaffOfficeSync sync = player.getStaffOffic().buildSyncPB();
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.STAFFOFFICE_SYNC_VALUE, sync.toBuilder()));
	}
}
