package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 指挥官常量配置
 */
@HawkConfigManager.KVResource(file = "xml/commander_const.xml")
public class CommanderConstProperty extends HawkConfigBase {
	/**
	 * 单例对象
	 */
	private static CommanderConstProperty instance = null;

	public static CommanderConstProperty getInstance() {
		return instance;
	}

	//# 关押俘虏数量
	private final int captiveNum ;

	//# 关押状态倒计时（即多久后释放俘虏）（秒）
	private final int captureTime ;

	//# 用刑CD时间（秒）
	private final int punishTime ;

	//# 俘虏处决倒计时（秒）
	private final int executeTime ;

	//# 俘获俘虏所需钻石
	private final int rebornGold ;

	public CommanderConstProperty() {
		instance = this;
		
		captiveNum = 0 ;
		captureTime = 0 ;
		executeTime = 0 ;
		rebornGold = 0 ;
		punishTime = 0 ;
	}

	public int getRebornGold() {
		return rebornGold;
	}

	public int getExecuteTime() {
		return executeTime * 1000;
	}

	public int getPunishTime() {
		return punishTime * 1000;
	}

	public int getCaptureTime() {
		return captureTime * 1000;
	}

	public int getCaptiveNum() {
		return captiveNum;
	}

	@Override
	protected boolean checkValid() {
		return super.checkValid();
	}

	@Override
	protected boolean assemble() {
		return super.assemble();
	}


}
