package com.hawk.game.world.robot;

import org.hawk.os.HawkRand;

import com.hawk.game.config.PlayerImageCfg;
import com.hawk.game.player.PlayerData;
import com.hawk.game.world.robot.cache.WORPlayerDataCache;

public class WORPlayerData extends PlayerData {
	private String sourPlayerId;
	private int image;
	private int vip;

	public static WORPlayerData valueOf(String playerId, String sourPlayerId) {
		WORPlayerData result = new WORPlayerData();
		result.playerId = playerId;
		result.sourPlayerId = sourPlayerId;
		result.setDataCache(WORPlayerDataCache.newCache(playerId, sourPlayerId));
		result.playerEffect = new WORPlayerEffect(result);
		result.image = PlayerImageCfg.randmIamge();
		result.vip = 102000 + HawkRand.randInt(0, 8);
		return result;

	}

	@Override
	public String getPfIcon() {
		return "2_" + image + "_" + vip;
	}

	@Override
	public boolean loadPlayerData(String playerId) {
		return true;
	}

	@Override
	public void loadAll(boolean isNewly) {
	}

	@Override
	public long getCityShieldTime() {
		return 0;
	}

	@Override
	public int getAchievePoint() {
		return 0;
	}

	@Override
	public String getPlayerId() {
		return playerId;
	}

	public String getSourPlayerId() {
		return sourPlayerId;
	}

	public void setSourPlayerId(String sourPlayerId) {
		this.sourPlayerId = sourPlayerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

}
