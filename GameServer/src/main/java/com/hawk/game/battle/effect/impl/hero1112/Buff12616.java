package com.hawk.game.battle.effect.impl.hero1112;

import com.google.common.util.concurrent.AtomicLongMap;
import com.hawk.game.protocol.Const.SoldierType;

public class Buff12616 {
	public int round;
	public AtomicLongMap<SoldierType> roundCnt = AtomicLongMap.create();
	public AtomicLongMap<SoldierType> defCnt = AtomicLongMap.create();
}
