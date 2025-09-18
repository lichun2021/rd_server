package com.hawk.game.invoker;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;
import com.hawk.game.player.Player;
import com.hawk.game.service.QuestionnaireService;

public class QuestionnaireCheckInvoker extends HawkMsgInvoker {
	
	private Player player;
	private int conditionType;
	private int itemId;
	
	public QuestionnaireCheckInvoker(Player player, int conditionType, int itemId) {
		this.player = player;
		this.conditionType = conditionType;
		this.itemId = itemId;
	}
	
	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
		// 问卷调查检测
		QuestionnaireService.getInstance().questionaireCheck(player, conditionType, itemId);
		return true;
	}

	public Player getPlayer() {
		return player;
	}

	public int getConditionType() {
		return conditionType;
	}

	public int getItemId() {
		return itemId;
	}
	
}
