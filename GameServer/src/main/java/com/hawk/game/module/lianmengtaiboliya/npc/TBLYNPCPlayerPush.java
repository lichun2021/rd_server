package com.hawk.game.module.lianmengtaiboliya.npc;

import java.util.concurrent.TimeUnit;

import com.hawk.game.entity.BuildingBaseEntity;
import com.hawk.game.module.lianmengtaiboliya.player.ITBLYPlayerData;
import com.hawk.game.module.lianmengtaiboliya.player.ITBLYPlayerPush;
import com.hawk.game.module.lianmengtaiboliya.player.TBLYPlayer;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.BuildingStatus;

public class TBLYNPCPlayerPush extends ITBLYPlayerPush {
	final long PORTECTED_A = TimeUnit.MINUTES.toMillis(11);
	final long PORTECTED_B = TimeUnit.MINUTES.toMillis(3);
	final int CITY_SHIELD_BUFF_ID = 23001;

	public TBLYNPCPlayerPush(Player player) {
		super(player);
	}

	/**
	 * 同步世界收藏夹
	 * 
	 * @return
	 */
	@Override
	public void syncWorldFavorite() {
	}

	@Override
	public void syncGuildWarCount() {
	}

	@Override
	public void syncPlayerInfo() {

	}

	/** 隐藏真是数据 */
	@Override
	public void pushJoinGame() {

	}

	@Override
	public void pushGameOver() {
	}

	public TBLYPlayer getPlayer() {
		return (TBLYPlayer) player;
	}

	@Override
	public ITBLYPlayerData getData() {
		return (ITBLYPlayerData) player.getData();
	}

	/** 同步世界信息 */
	@Override
	public void syncPlayerWorldInfo() {
	}

	/** 同步城防信息 */
	public void syncCityDef(boolean cityOnFireStateChange) {
	}

	/**
	 * 推送建筑状态的变化
	 * 
	 * @param buildingEntity
	 *            建筑实体
	 * @param status
	 *            建筑的状态
	 */
	public void pushBuildingStatus(BuildingBaseEntity buildingEntity, BuildingStatus status) {
	}
}
