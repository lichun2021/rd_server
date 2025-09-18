package com.hawk.game.module.dayazhizhan.battleroom.worldpoint;

import java.util.LinkedList;
import java.util.List;

import org.hawk.config.HawkConfigManager;

import com.hawk.game.module.dayazhizhan.battleroom.DYZZBattleRoom;
import com.hawk.game.module.dayazhizhan.battleroom.DYZZRoomManager.DYZZCAMP;
import com.hawk.game.module.dayazhizhan.battleroom.cfg.DYZZBaseCfg;
import com.hawk.game.module.dayazhizhan.battleroom.player.rogue.DYZZRogueType;
import com.hawk.game.module.dayazhizhan.battleroom.roomstate.DYZZGameOver;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.protocol.DYZZ.DYZZBuildState;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.chat.ChatParames;

/**
 * 军事基地
 *
 */
public class DYZZBase extends IDYZZNuclearShootAble {
	private int hp;
	boolean hpLess60 = true;
	boolean hpLess30 = true;

	private int order1005Val;// 1005 护盾
	private long order1005End;

	private int order1006Val;// 1006 受伤加重
	private long order1006End;

	private int order1007Val;// 1007 受伤-
	private long order1007End;

	/**出生归属*/
	private DYZZCAMP bornCamp;
	private List<Integer> rogueSelectBaseHpList = new LinkedList<>();

	public DYZZBase(DYZZBattleRoom parent) {
		super(parent);
		rogueSelectBaseHpList.addAll(parent.getCfg().getRogueSelectBaseHpList());
	}

	public void dcresHP(int atkVal) {
		atkVal += order1006Val;
		atkVal -= order1007Val;
		atkVal = Math.max(0, atkVal);
		if (!getParent().isGameOver()) {
			if (order1005Val >= atkVal) {
				order1005Val -= atkVal;
				atkVal = 0;
			} else if (order1005Val < atkVal) {
				atkVal -= order1005Val;
				order1005Val = 0;
			}

			hp -= atkVal;
			hp = Math.max(hp, 0);
		}
		if (hp <= 0) {
			if (bornCamp == DYZZCAMP.A) {
				getParent().setWinCamp(DYZZCAMP.B);
			} else {
				getParent().setWinCamp(DYZZCAMP.A);
			}

			getParent().setState(new DYZZGameOver(getParent()));
		}
	}

	@Override
	public boolean onTick() {
		onShootTick();
		if (order1005End < getParent().getCurTimeMil()) {
			order1005Val = 0;
		}

		if (order1006End < getParent().getCurTimeMil()) {
			order1006Val = 0;
		}
		if (order1007End < getParent().getCurTimeMil()) {
			order1007Val = 0;
		}

		if (!rogueSelectBaseHpList.isEmpty() && 1D * hp / getCfg().getInitBlood() < rogueSelectBaseHpList.get(0) * 0.01) {
			int param = rogueSelectBaseHpList.remove(0);
			getParent().getCampPlayers(bornCamp).forEach(p -> p.getRogueCollec().rogueOnce(DYZZRogueType.BASEHP, param));
		}

		if (hpLess60 && 1D * hp / getCfg().getInitBlood() < 0.6) {
			hpLess60 = false;
			// 广播战场
			ChatParames paramesBroad = ChatParames.newBuilder().setChatType(ChatType.CHAT_FUBEN_SPECIAL_BROADCAST).setKey(Const.NoticeCfgId.DYZZ_342)
					.addParms(getParent().getCampGuildTag(getBornCamp()))
					.build();
			getParent().addWorldBroadcastMsg(paramesBroad);
		}

		if (hpLess30 && 1D * hp / getCfg().getInitBlood() < 0.3) {
			hpLess30 = false;
			// 广播战场
			ChatParames paramesBroad = ChatParames.newBuilder().setChatType(ChatType.CHAT_FUBEN_SPECIAL_BROADCAST).setKey(Const.NoticeCfgId.DYZZ_343)
					.addParms(getParent().getCampGuildTag(getBornCamp()))
					.build();
			getParent().addWorldBroadcastMsg(paramesBroad);
		}

		return true;
	}

	@Override
	public long getProtectedEndTime() {
		return 0;
	}

	public static DYZZBaseCfg getCfg() {
		return HawkConfigManager.getInstance().getKVInstance(DYZZBaseCfg.class);
	}

	@Override
	public int getAtkCd() {
		return getCfg().getAtkCd();
	}

	@Override
	public int getAtkVal() {
		return getCfg().getAtkVal();
	}

	@Override
	public WorldPointType getPointType() {
		return WorldPointType.DYZZ_BASE;
	}

	@Override
	public int getControlCountDown() {
		return 0;
	}

	public int getHp() {
		return hp;
	}

	public void setHp(int hp) {
		this.hp = hp;
	}

	public DYZZCAMP getBornCamp() {
		return bornCamp;
	}

	public void setBornCamp(DYZZCAMP bornCamp) {
		this.bornCamp = bornCamp;
	}

	@Override
	public DYZZBuildState getState() {
		return DYZZBuildState.ZHAN_LING;
	}

	@Override
	public String getGuildId() {
		return getParent().getCampGuild(bornCamp);
	}

	@Override
	public String getGuildTag() {
		return getParent().getCampGuildTag(bornCamp);
	}

	@Override
	public int getGuildFlag() {
		return getParent().getCampGuildFlag(bornCamp);
	}

	@Override
	public int getWellAtkVal() {
		return getCfg().getWellAtkVal();
	}

	@Override
	public int getOrderAtkVal() {
		return getCfg().getOrderAtkVal();
	}

	public boolean isHpLess60() {
		return hpLess60;
	}

	public void setHpLess60(boolean hpLess60) {
		this.hpLess60 = hpLess60;
	}

	public boolean isHpLess30() {
		return hpLess30;
	}

	public void setHpLess30(boolean hpLess30) {
		this.hpLess30 = hpLess30;
	}

	public int getOrder1005Val() {
		return order1005Val;
	}

	public void setOrder1005Val(int order1005Val) {
		this.order1005Val = order1005Val;
	}

	public long getOrder1005End() {
		return order1005End;
	}

	public void setOrder1005End(long order1005End) {
		this.order1005End = order1005End;
	}

	public List<Integer> getRogueSelectBaseHpList() {
		return rogueSelectBaseHpList;
	}

	public void setRogueSelectBaseHpList(List<Integer> rogueSelectBaseHpList) {
		this.rogueSelectBaseHpList = rogueSelectBaseHpList;
	}

	public int getOrder1006Val() {
		return order1006Val;
	}

	public void setOrder1006Val(int order1006Val) {
		this.order1006Val = order1006Val;
	}

	public long getOrder1006End() {
		return order1006End;
	}

	public void setOrder1006End(long order1006End) {
		this.order1006End = order1006End;
	}

	public int getOrder1007Val() {
		return order1007Val;
	}

	public void setOrder1007Val(int order1007Val) {
		this.order1007Val = order1007Val;
	}

	public long getOrder1007End() {
		return order1007End;
	}

	public void setOrder1007End(long order1007End) {
		this.order1007End = order1007End;
	}

}
