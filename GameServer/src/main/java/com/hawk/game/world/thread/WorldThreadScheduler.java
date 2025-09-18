package com.hawk.game.world.thread;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.thread.HawkTask;
import org.hawk.thread.HawkThread;
import org.hawk.tickable.HawkTickable;

import com.alibaba.fastjson.JSONObject;

/**
 * 世界线程调度器
 * 
 * @author zhenyu.shang
 * @since 2017年9月6日
 */
public class WorldThreadScheduler {
	/**
	 * 世界单线程
	 */
	private HawkThread worldThread;
	/**
	 * 是否运行中
	 */
	protected volatile boolean running;
	/**
	 * 线程索引
	 */
	private AtomicLong threadIdx;
	/**
	 * 任务cache
	 */
	private Set<HawkTask> taskCache;

	/**
	 * 单例使用
	 */
	static WorldThreadScheduler instance;

	/**
	 * 获取全局管理器
	 * 
	 * @return
	 */
	public static WorldThreadScheduler getInstance() {
		if (instance == null) {
			instance = new WorldThreadScheduler();
		}
		return instance;
	}

	/**
	 * 构造
	 */
	private WorldThreadScheduler() {
		threadIdx = new AtomicLong();
		taskCache = new HashSet<HawkTask>();
		worldThread = new HawkThread();
	}

	public long getPushTaskCnt() {
		return worldThread.getPushTaskCnt(null);
	}

	public long getPushTaskCnt(String typeName) {
		return worldThread.getPushTaskCnt(typeName);
	}

	public long getPopTaskCnt() {
		return worldThread.getPopTaskCnt(null);
	}

	public long getAmassTaskCnt() {
		return worldThread.getAmassTaskCnt(null);
	}

	/**
	 * 开启线程
	 */
	public void startWorldThread(int tickPeriod) {
		if (worldThread != null) {
			worldThread.setDaemon(true);
			worldThread.setName("World-" + threadIdx.incrementAndGet());
			worldThread.setUncaughtExceptionHandler(new WorldThreadExceptionHandler());
			worldThread.getTickableContainer().setTickPeriod(tickPeriod);

			worldThread.start();
			running = true;
		}
	}

	/**
	 * 重置世界线程
	 * 
	 * @param tickPeriod
	 */
	public void resetWorldThread(int tickPeriod) {
		this.worldThread = new HawkThread();
		this.running = false;
		startWorldThread(tickPeriod);
	}

	/**
	 * 关闭任务管理器
	 * 
	 * @return
	 */
	public boolean close() {
		try {
			// 设置非运行状态
			running = false;
			if (worldThread != null) {
				worldThread.close(true);
			}
			HawkLog.logPrintln("start world thread closed ...");
			return true;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return false;
	}

	/**
	 * 给World线程添加tickable对象
	 * 
	 * @param tickable
	 * @param threadIdx
	 * @return
	 */
	public void addWorldTickable(HawkTickable tickable) {
		worldThread.addTickable(tickable);
	}

	/**
	 * 投递任务到世界线程
	 * 
	 * @param task
	 * @param threadIdx
	 * @return
	 */
	public boolean postWorldTask(WorldTask task) {
		if (task == null) {
			HawkLog.errPrintln("world task is null");
			return false;
		}

		// 不允许世界线程给世界线程投递消息
		long worldThreadId = worldThread.getId();
		long currThreadId = HawkOSOperator.getThreadId();
		if (currThreadId == worldThreadId) {
			throw new RuntimeException("world thread cannot post msg to world Thread");
		}

		task.setStatus(WorldTask.TaskState.START);
		if (running && worldThread != null) {
			return worldThread.addTask(task);
		} else {
			taskCache.add(task);
			HawkLog.errPrintln("world task cannot post to thread , type : {}, taskCacheSize : {}", task.getWorldTaskType(), taskCache.size());
		}

		return false;
	}
	
	/**
	 * 投递延迟任务到线程
	 * @param delayTask
	 * @return
	 */
	public boolean postDelayWorldTask(WorldDelayTask delayTask){
		if (delayTask == null) {
			HawkLog.errPrintln("world task is null");
			return false;
		}

		if (running && worldThread != null) {
			return worldThread.addDelayTask(delayTask);
		} else {
			taskCache.add(delayTask);
			HawkLog.errPrintln("world task cannot post to thread , type : {}, taskCacheSize : {}", delayTask.getWorldTaskType(), taskCache.size());
		}
		
		return false;
	}

	/**
	 * 获取线程状态信息
	 * 
	 * @param threadPool
	 * @return
	 */
	public JSONObject getThreadState() {
		JSONObject threadInfo = new JSONObject();
		threadInfo.put("threadId", worldThread.getId());
		threadInfo.put("pushCount", worldThread.getPushTaskCnt(null));
		threadInfo.put("popCount", worldThread.getPopTaskCnt(null));
		threadInfo.put("amassTask", worldThread.getAmassTaskCnt(null));
		return threadInfo;
	}

	/**
	 * 获取之前的任务缓存
	 * 
	 * @return
	 */
	public Set<HawkTask> getTaskCache() {
		return taskCache;
	}
	
	
	public boolean isRunning() {
		return running;
	}
}
