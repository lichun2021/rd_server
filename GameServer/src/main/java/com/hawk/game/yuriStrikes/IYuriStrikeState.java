package com.hawk.game.yuriStrikes;

import com.hawk.game.player.Player;
import com.hawk.game.protocol.YuriStrike.YuriState;
import com.hawk.game.protocol.YuriStrike.YuriStrikeInfo;

public interface IYuriStrikeState {

	static IYuriStrikeState valueOf(YuriState state) {
		switch (state) {
		case LOCK:
			return new YuriStrikeStateLock();
		case SCANING:
			return new YuriStrikeStateScaning();
		case MARCH:
			return new YuriStrikeStateMarch();
		case YURI_HOLD:
			return new YuriStrikeStateYuriHold();
		case PRE_CLEAN:
			return new YuriStrikeStatePreClean();
		case CLEAN:
			return new YuriStrikeStateClean();
		case CLEAN_OVER:
			return new YuriStrikeStateCleanOver();
		default:
			return new YuriStrikeStateCleanOver();
		}
	}

	default YuriStrikeInfo.Builder toPBBuilder(Player player,YuriStrike obj) {
		YuriStrikeInfo.Builder result = YuriStrikeInfo.newBuilder()
				.setState(pbState())
				.setYuriCfgId(obj.getDbEntity().getCfgId());
		return result;
	}

	default YuriState pbState() {
		return getClass().getAnnotation(YuriStrikeState.class).pbState();
	}

	default String name() {
		return pbState().name();
	}

	default void login(Player player, YuriStrike obj) {
	}

	default void tick(Player player, YuriStrike obj) {
	}

	/** 迁城跑了 */
	default void moveCity(Player player, YuriStrike obj) {
	}

	/** 尤里行军到达. 开始占据 */
	default void yuriMarchReach(Player player, YuriStrike obj) {
	}

	/** 城内攻占胜利 */
	default void attackWin(Player player, YuriStrike obj) {
	}

	/** 收取净化奖励 */
	default void obtainReward(Player player, YuriStrike obj) {
	}

	/** 玩家请求clear */
	default void startClean(Player player, YuriStrike obj) {
	}

	default void cleanOver(Player player, YuriStrike obj) {
	}

}
