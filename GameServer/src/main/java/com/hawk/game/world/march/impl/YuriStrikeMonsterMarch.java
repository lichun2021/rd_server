package com.hawk.game.world.march.impl;

import com.hawk.game.player.Player;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.march.MonsterMarch;
import com.hawk.game.world.march.submarch.BasedMarch;
import com.hawk.game.world.service.WorldPointService;

public class YuriStrikeMonsterMarch extends MonsterMarch implements BasedMarch {

	public YuriStrikeMonsterMarch(WorldMarch marchEntity) {
		super(marchEntity);
	}

	@Override
	public WorldMarchType getMarchType() {
		return WorldMarchType.YURI_STRIKE_MARCH;
	}

	@Override
	public long getMarchNeedTime() {
		return getPlayer().getData().getYuriStrikeEntity().getYuriStrikeObj().getCfg().getArrivalTime() * 1000;
	}

	@Override
	public boolean marchReach() {
		WorldMarch march = this.getMarchEntity();
		WorldPointService.getInstance().removeWorldPoint(march.getOrigionId());
		WorldMarchService.getInstance().removeMarch(this);
		
		Player player = getPlayer();
		onMarchReach(player);
		return true;
	}
	
	@Override
	public void onMarchReach(Player player) {
		player.getData().getYuriStrikeEntity().getYuriStrikeObj().yuriMarchReach(player);
	}
	

	@Override
	public void targetMoveCityProcess(Player targetPlayer, long currentTime) {
		targetPlayer.getData().getYuriStrikeEntity().getYuriStrikeObj().moveCity(targetPlayer);
		
		WorldMarch march = this.getMarchEntity();
		WorldPointService.getInstance().removeWorldPoint(march.getOrigionId());
		WorldMarchService.getInstance().removeMarch(this);
	}
	
	
}
