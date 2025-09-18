package com.hawk.game.msg;

import java.util.EnumSet;

import org.hawk.msg.HawkMsg;

import com.hawk.game.protocol.Const.EffType;

/**
 * 皮肤buff结束
 * 
 */
public class PlayerEffectChangeMsg extends HawkMsg {
	private EnumSet<EffType> set;

	private PlayerEffectChangeMsg() {
	}

	public static PlayerEffectChangeMsg valueOf(EffType... types) {
		PlayerEffectChangeMsg msg = new PlayerEffectChangeMsg();
		msg.set = EnumSet.noneOf(EffType.class);
		for (EffType type : types) {
			if (type != null) {
				msg.set.add(type);
			}
		}
		return msg;
	}

	public boolean hasEffectChange(EffType type) {
		return set.contains(type);
	}

	public EnumSet<EffType> getSet() {
		return set;
	}

	public void setSet(EnumSet<EffType> set) {
		this.set = set;
	}

}
