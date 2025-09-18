package com.hawk.game.crossproxy.skill;

import com.hawk.game.crossactivity.CrossActivityService;
import com.hawk.game.protocol.Const.EffType;

@CrossSkill(skillID = "1004")
public class CrossSkill1004 extends ICrossSkill {

	@Override
	public int getEffectValIfContinue(EffType effType) {
		if (!CrossActivityService.getInstance().isOpen()) {
			return 0;
		}
		return super.getEffectValIfContinue(effType);
	}

}