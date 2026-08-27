package com.hawk.game.battle.effect.impl.hero1122;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.protocol.Const.EffType;

@BattleTupleType(tuple = { Type.ATK, Type.DEF, Type.HP })
@EffectChecker(effType = EffType.HERO_12968)
public class Checker12968 extends Hero1122SourceChecker {
}
