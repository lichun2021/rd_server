package com.hawk.game.world.thread;

import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.thread.HawkTask;

import com.hawk.game.GsConfig;

/**
 * 世界线程任务
 * @author zhenyu.shang
 * @since 2017年9月5日
 */
public abstract class WorldTask extends HawkTask {
	/**
	 * 任务状态
	 * 
	 * @author hawk
	 *
	 */
	static class TaskState {
		final static int INIT = 0;
		final static int START = 1;
		final static int FINISH = 2;
	}
	
	/**
	 * 当前状态
	 */
	private volatile int status;
	
	/**
	 * 世界任务类型
	 */
	private int worldTaskType;
	
	/**
	 * 携带应用逻辑层参数, 常用
	 * 
	 * @param params
	 */
	public WorldTask(int worldTaskType) {
		this.worldTaskType = worldTaskType;
		this.status = TaskState.INIT;
	}
	
	/**
	 * 设置任务状态
	 * 
	 * @param status
	 */
	public void setStatus(int status) {
		this.status = status;
	}

	/**
	 * 获取世界任务类型
	 * 
	 * @return
	 */
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

	/**
	 * 执行消息任务
	 */
	@Override
	public Object run() {
		boolean result = false;
		long beginTimeMs = HawkTime.getMillisecond();
		try {
			result = this.onInvoke();
		} catch (Exception e) {
			HawkException.catchException(e);
		} finally {
			this.status = TaskState.FINISH;
			
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
	
	/**
	 * 阻塞当前线程直到本投递任务完成
	 * @param timeout 超时时间
	 */
	public void blockThread(long timeout){
		if(timeout <= 0){
			throw new RuntimeException("timeout not support");
		}
		
		if(status == TaskState.START){
			long time = HawkTime.getMillisecond();
			while (true) {
				if(this.status == TaskState.FINISH || HawkTime.getMillisecond() - time > timeout) {
					break;
				}
				
				try {
					HawkOSOperator.sleep();
				} catch (Exception e) {
					HawkException.catchException(e);
				}
			}
		}
	}
}

