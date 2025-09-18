package com.hawk.game.world.thread;

import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;
import org.hawk.task.HawkDelayTask;

import com.hawk.game.GsConfig;

/**
 * 世界延迟消息
 * @author zhenyu.shang
 * @since 2018年2月24日
 */
public abstract class WorldDelayTask extends HawkDelayTask{
	
	/**
	 * 世界任务类型
	 */
	private int worldTaskType;

	public WorldDelayTask(int worldTaskType, long period, long delayTime, int maxCount) {
		super(period, delayTime, maxCount);
		this.worldTaskType = worldTaskType;
	}
	
	public int getWorldTaskType() {
		return worldTaskType;
	}

	/**
	 * 消息响应
	 * 
	 * @param targetObj
	 * @param msg
	 * @return
	 */
	public abstract boolean onInvoke();

	@Override
	public Object run() {
		boolean result = false;
		long beginTimeMs = HawkTime.getMillisecond();
		try {
			result = this.onInvoke();
		} catch (Exception e) {
			HawkException.catchException(e);
		} finally {
			// 时间消耗的统计信息
			long costTimeMs = HawkTime.getMillisecond() - beginTimeMs;
			if(costTimeMs > GsConfig.getInstance().getProtoTimeout()){
				HawkLog.logPrintln("process world task, type: {}, result: {}, costtime: {}", 
						worldTaskType, result, costTimeMs);
			}
		}
		return result;
	}

	/**
	 * 获取任务类型名
	 */
	@Override
	public String getTypeName() {
		return "WorldTask-" + worldTaskType;
	}
}
