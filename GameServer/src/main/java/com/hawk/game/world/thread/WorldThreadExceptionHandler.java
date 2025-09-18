package com.hawk.game.world.thread;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Deque;
import java.util.Set;

import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.thread.HawkTask;
import org.hawk.thread.HawkThread;
import org.hawk.tickable.HawkTickable;

import com.hawk.game.GsConfig;

/**
 * World 线程异常捕获处理器
 * 
 * @author zhenyu.shang
 * @since 2017年10月31日
 */
public class WorldThreadExceptionHandler implements UncaughtExceptionHandler {
	@Override
	public void uncaughtException(Thread thread, Throwable e) {
		HawkLog.errPrintln("world task died, exception={}, switch new world thread ...", e.getMessage());
		if (!(thread instanceof HawkThread)) {
			return;
		}
		
		try {
			// 先重新启动新的线程
			WorldThreadScheduler.getInstance().resetWorldThread(GsConfig.getInstance().getTickPeriod());

			// 获取已经中断的线程
			HawkThread worldThread = (HawkThread) thread;

			// 重置所有tick
			Set<HawkTickable> ticks = worldThread.getTickableContainer().getTickables();
			for (HawkTickable tickable : ticks) {
				WorldThreadScheduler.getInstance().addWorldTickable(tickable);
			}
			HawkLog.logPrintln("world thread switch, reset tickable count: {}", ticks.size());
			ticks.clear();
			
			// 重置所有任务
			Deque<HawkTask> tasks = worldThread.getTasks();
			for (HawkTask task : tasks) {
				if (task instanceof WorldTask) {
					WorldThreadScheduler.getInstance().postWorldTask((WorldTask) task);
				}
			}
			HawkLog.logPrintln("world thread switch, reset task count: {}", tasks.size());
			tasks.clear();
			
			// 恢复在重建线程期间的缓存任务
			Set<HawkTask> cache = WorldThreadScheduler.getInstance().getTaskCache();
			for (HawkTask worldTask : cache) {
				if(worldTask instanceof WorldTask){
					WorldThreadScheduler.getInstance().postWorldTask((WorldTask) worldTask);
				} else if(worldTask instanceof WorldDelayTask){
					WorldThreadScheduler.getInstance().postDelayWorldTask((WorldDelayTask) worldTask);
				}
			}
			HawkLog.errPrintln("world thread switch, reset cache task count: {}", cache.size());
			cache.clear();
			
		} catch (Exception e1) {
			HawkException.catchException(e1);
		}
	}
}
