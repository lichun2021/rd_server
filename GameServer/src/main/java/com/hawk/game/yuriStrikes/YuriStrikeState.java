package com.hawk.game.yuriStrikes;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.hawk.game.protocol.YuriStrike.YuriState;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface YuriStrikeState {
	YuriState pbState();
}
