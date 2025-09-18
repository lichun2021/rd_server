package com.hawk.game.world.robot;

import java.util.List;

import org.hawk.config.HawkConfigManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.xid.HawkXID;

import com.hawk.common.AccountRoleInfo;
import com.hawk.game.config.BattleSoldierCfg;
import com.hawk.game.config.BuildingCfg;
import com.hawk.game.config.PlayerImageCfg;
import com.hawk.game.entity.BuildingBaseEntity;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.protocol.Player.ImageType;
import com.hawk.game.service.PlayerImageService;
import com.hawk.game.util.GameUtil;

public class WORPlayer extends Player {

	private final String id;
	private boolean inited;
	private String sourPlayerId;
	// private String pfIcon;
	private int playerPos;
	private AccountRoleInfo accountRoleInfo;

	public WORPlayer(HawkXID xid) {
		super(xid);
		this.id = xid.getUUID();
	}

	@Override
	public boolean onProtocol(HawkProtocol protocol) {
		return true;
	}

	public void init(String sourPlayerId) {
		if (inited) {
			return;
		}
		// this.pfIcon = GlobalData.getInstance().makesurePlayer(sourPlayerId).getPfIcon();
		this.sourPlayerId = sourPlayerId;
		setPlayerPush(new WORPlayerPush(this));
		super.updateData(WORPlayerData.valueOf(id, sourPlayerId));
		
		PlayerImageService.getInstance().getPlayerImageData(this).useImageOrCircle(ImageType.IMAGE, PlayerImageCfg.randmIamge());
		inited = true;
	}

	@Override
	public int[] getPosXY() {
		return GameUtil.splitXAndY(playerPos);
	}

	@Override
	public int getPlayerPos() {
		return playerPos;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getGuildId() {
		return "";
	}

	@Override
	public String getGuildName() {
		return "";
	}

	@Override
	public int getCityPlantLv(){
		return 0;
	}
	@Override
	public int getSoldierStar(int armyId) {
		return 0;
	}
	@Override
	public int getSoldierStep(int armyId) {
		return 0;
	}
	
	@Override
	public String getGuildTag() {
		return "";
	}

	@Override
	public int getGuildFlag() {
		return 0;
	}

	@Override
	public String getPfIcon() {
		return getData().getPfIcon();
	}

	public String getSourPlayerId() {
		return sourPlayerId;
	}

	public void setPlayerPos(int playerPos) {
		this.playerPos = playerPos;
	}

	public AccountRoleInfo getAccountRoleInfo() {
		return accountRoleInfo;
	}

	public void setAccountRoleInfo(AccountRoleInfo accountRoleInfo) {
		this.accountRoleInfo = accountRoleInfo;
	}

}
