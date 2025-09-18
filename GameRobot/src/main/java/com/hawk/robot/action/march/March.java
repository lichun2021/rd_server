package com.hawk.robot.action.march;

import com.hawk.robot.GameRobotEntity;

public interface March {
	
	/**
	 * 开始行军
	 */
	void startMarch(GameRobotEntity robot);
	
	/**
	 * 行军类型
	 * @return
	 */
	int getMarchType();
}
