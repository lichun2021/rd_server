package com.hawk.game.module.hospice;

import com.hawk.game.msg.HospiceQueueFinishMsg;
import com.hawk.game.player.Player;

public interface IHospiceState {
	public enum State {
		/** 累积奖励 */
		CONGEST,
		/** 奖励行军中 */
		MARCH,
	}

	public static IHospiceState valueOf(State state) {
		switch (state) {
		case CONGEST:
			return new HospiceStateCONGEST();
		case MARCH:
			return new HospiceStateMARCH();
		default:
			break;
		}
		return new HospiceStateCONGEST();
	}

	public String name();

	default void syncInfo(Player player, HospiceObj hospiceObj) {
	}

	default void tick(Player player, HospiceObj hospiceObj) {
	}

	default void playerLogin(Player player, HospiceObj hospiceObj) {
	}

	default void onGuildhospiceMsg(HospiceQueueFinishMsg msg, HospiceObj hospiceObj) {
	}

}
