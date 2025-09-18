package com.hawk.game.msg;

import java.util.List;

import org.hawk.msg.HawkMsg;

import com.hawk.game.battle.BattleOutcome;
import com.hawk.game.march.ArmyInfo;
import com.hawk.gamelib.GameConst.MsgId;

/**
 * 消息 - 进攻玩家战后处理
 * 
 * @author golden
 *
 */
public class AtkPlayerAfterWarMsg extends HawkMsg {

	private boolean isAtkWin;
	
	private List<ArmyInfo> leftList;
	
	private int defMaxFactoryLvl;
	/**
	 * 没有我要的信息，所以加个补丁
	 */
	private BattleOutcome battleOutcome;

	public AtkPlayerAfterWarMsg(boolean isAtkWin, List<ArmyInfo> leftList, int defMaxFactoryLvl, BattleOutcome battleOutcome) {
		super(MsgId.ATK_PLAYER_REFRESH);
		this.isAtkWin = isAtkWin;
		this.leftList = leftList;
		this.defMaxFactoryLvl = defMaxFactoryLvl;
		this.battleOutcome = battleOutcome;
	}
	
	public boolean isAtkWin() {
		return isAtkWin;
	}

	public void setAtkWin(boolean isAtkWin) {
		this.isAtkWin = isAtkWin;
	}

	public List<ArmyInfo> getLeftList() {
		return leftList;
	}

	public void setLeftList(List<ArmyInfo> leftList) {
		this.leftList = leftList;
	}

	public int getDefMaxFactoryLvl() {
		return defMaxFactoryLvl;
	}

	public void setDefMaxFactoryLvl(int defMaxFactoryLvl) {
		this.defMaxFactoryLvl = defMaxFactoryLvl;
	}

	public BattleOutcome getBattleOutcome() {
		return battleOutcome;
	}

	public void setBattleOutcome(BattleOutcome battleOutcome) {
		this.battleOutcome = battleOutcome;
	}
}
