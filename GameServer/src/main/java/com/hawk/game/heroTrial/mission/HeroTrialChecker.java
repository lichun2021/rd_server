package com.hawk.game.heroTrial.mission;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.hawk.game.heroTrial.HeroTrialType;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface HeroTrialChecker {
	HeroTrialType type();
}
