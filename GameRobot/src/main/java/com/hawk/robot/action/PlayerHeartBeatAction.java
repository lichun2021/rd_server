package com.hawk.robot.action;

import java.util.concurrent.atomic.AtomicLong;

import org.hawk.annotation.RobotAction;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkTime;
import org.hawk.robot.HawkRobotAction;
import org.hawk.robot.HawkRobotEntity;

import com.hawk.robot.GameRobotApp;
import com.hawk.robot.GameRobotEntity;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.SysProtocol.HPHeartBeat;

/**
 * 心跳
 * 
 * @author lating
 *
 */
@RobotAction(valid = true)
public class PlayerHeartBeatAction extends HawkRobotAction {
	/**
	 * 上一次初始化账号的时间
	 */
	public static AtomicLong lastAccountResetTime = new AtomicLong(HawkTime.getMillisecond());
	/**
	 * 初始化账号时间间隔
	 */
	protected static final long RESET_ACCOUNT_DURATION = GameRobotApp.getInstance().getConfig().getInt("robot.resetAccountDuration") * 1000L;
	
	@Override
	public void doAction(HawkRobotEntity robotEntity) {
		if (!robotEntity.isOnline()) {
			return;
		}
		
		GameRobotEntity gameRobotEntity = (GameRobotEntity) robotEntity;
		HPHeartBeat.Builder builder = HPHeartBeat.newBuilder();
		long now = HawkTime.getMillisecond();
		builder.setTimeStamp(now);
		if (now - lastAccountResetTime.get() > RESET_ACCOUNT_DURATION) {
			builder.setResetAccount(true);
			lastAccountResetTime.set(now);
			HawkLog.logPrintln("reset account, puid: {}", gameRobotEntity.getPuid());
		}
		gameRobotEntity.sendProtocol(HawkProtocol.valueOf(HP.sys.HEART_BEAT_VALUE, builder));
	}
	
	/**
	 * 判断是否可以进行初始化账号了
	 * @return
	 */
	public static boolean checkResetAccount() {
		return HawkTime.getMillisecond() - lastAccountResetTime.get() > RESET_ACCOUNT_DURATION;
	}
	
}
