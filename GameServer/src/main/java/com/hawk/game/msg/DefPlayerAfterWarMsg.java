package com.hawk.game.msg;

import java.util.List;

import org.hawk.msg.HawkMsg;

import com.hawk.game.item.ConsumeItems;
import com.hawk.game.march.ArmyInfo;
import com.hawk.gamelib.GameConst.MsgId;

/**
 * 消息 - 防守玩家战后处理
 * 
 * @author golden
 *
 */
public class DefPlayerAfterWarMsg extends HawkMsg {
	
	private boolean isAtkWin;
	
	private List<ArmyInfo> leftArmyList;
	
	private ConsumeItems consumeItems;

	public DefPlayerAfterWarMsg(boolean isAtkWin, List<ArmyInfo> leftArmyList, ConsumeItems consumeItems) {
		super(MsgId.DEF_PLAYER_REFRESH);
		this.isAtkWin = isAtkWin;
		this.leftArmyList = leftArmyList;
		this.consumeItems = consumeItems;
	}
	
	public boolean isAtkWin() {
		return isAtkWin;
	}

	public void setAtkWin(boolean isAtkWin) {
		this.isAtkWin = isAtkWin;
	}

	public List<ArmyInfo> getLeftArmyList() {
		return leftArmyList;
	}

	public void setLeftArmyList(List<ArmyInfo> leftArmyList) {
		this.leftArmyList = leftArmyList;
	}

	public ConsumeItems getConsumeItems() {
		return consumeItems;
	}

	public void setConsumeItems(ConsumeItems consumeItems) {
		this.consumeItems = consumeItems;
	}
	
	public static DefPlayerAfterWarMsg valueOf(boolean isAtkWin, List<ArmyInfo> leftArmyList, ConsumeItems consumeItems) {
		return new DefPlayerAfterWarMsg(isAtkWin, leftArmyList, consumeItems);
	}
}
