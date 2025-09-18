package com.hawk.game.yuriStrikes;

import org.apache.commons.lang.StringUtils;
import org.hawk.config.HawkConfigManager;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.game.config.YuristrikeCfg;
import com.hawk.game.entity.YuriStrikeEntity;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.YuriStrike.YuriState;

public class YuriStrike {
	private YuriStrikeEntity dbEntity;
	private IYuriStrikeState state;

	/**
	 * 同步客户端
	 */
	public void syncInfo(Player player) {
		player.sendProtocol(HawkProtocol.valueOf(HP.code.YURI_STRIKE_INFO_SYNC, state.toPBBuilder(player,this)));
	}

	public void onLogin(Player player) {
		state.login(player, this);
		syncInfo(player);
	}

	public void tick(Player player) {
		state.tick(player, this);
	}

	/** 尤里行军到达 */
	public void yuriMarchReach(Player player) {
		state.yuriMarchReach(player, this);
	}

	/** 反击尤里胜利 */
	public void attackWin(Player player) {
		state.attackWin(player, this);
	}

	public void startClean(Player player) {
		state.startClean(player, this);
	}

	public void cleanOver(Player player) {
		state.cleanOver(player, this);
	}
	
	public void moveCity(Player player){
		state.moveCity(player, this);
	}

	/** 领取净化奖励 */
	public void obtainReward(Player player) {
		state.obtainReward(player, this);
	}

	public YuriStrikeEntity getDbEntity() {
		return dbEntity;
	}

	public void setDbEntity(YuriStrikeEntity dbEntity) {
		this.dbEntity = dbEntity;
		this.dbEntity.setYuriStrikeObj(this);
		if (StringUtils.isEmpty(dbEntity.getState())) {
			this.state = IYuriStrikeState.valueOf(YuriState.LOCK);
			dbEntity.setState(state.name());
		} else {
			this.state = IYuriStrikeState.valueOf(YuriState.valueOf(dbEntity.getState()));
		}
	}

	public void setState(Player player, IYuriStrikeState state) {
		this.state = state;
		this.dbEntity.setState(state.name());
		this.syncInfo(player);
	}

	public YuristrikeCfg getCfg() {
		return HawkConfigManager.getInstance().getConfigByKey(YuristrikeCfg.class, dbEntity.getCfgId());
	}

}
