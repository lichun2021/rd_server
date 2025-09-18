package com.hawk.game.world.march.submarch;

/**
 * 援助类行军
 * @author zhenyu.shang
 * @since 2017年8月29日
 */
public interface AssistanceMarch extends BasedMarch{

	@Override
	default boolean isAssistanceMarch() {
		return true;
	}
}

