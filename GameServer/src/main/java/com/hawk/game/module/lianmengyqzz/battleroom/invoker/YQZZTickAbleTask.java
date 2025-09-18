package com.hawk.game.module.lianmengyqzz.battleroom.invoker;

import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;
import org.hawk.thread.HawkTask;

import com.hawk.game.log.DungeonRedisLog;
import com.hawk.game.module.lianmengyqzz.battleroom.IYQZZWorldPoint;
import com.hawk.game.module.lianmengyqzz.battleroom.worldmarch.IYQZZWorldMarch;
import com.hawk.game.module.lianmengyqzz.battleroom.worldpoint.IYQZZBuilding;
import com.hawk.game.module.lianmengyqzz.battleroom.worldpoint.YQZZBuildState;
import com.hawk.game.protocol.World.WorldMarchType;

public class YQZZTickAbleTask extends HawkTask {
	private String battleId;
	private int threadNum;
	private Deque<IYQZZWorldPoint> viewPoints = new LinkedList<>();

	private Deque<IYQZZWorldMarch> marchs = new LinkedList<>();

	/** 准备运行 */
	private AtomicBoolean readyToRace = new AtomicBoolean(false);

	@Override
	public Object run() {
		long beginTimeMs = HawkTime.getMillisecond();
		try {
			while (!viewPoints.isEmpty()) {
				IYQZZWorldPoint point = viewPoints.pop();
				if (point instanceof IYQZZBuilding) {
					buildTick(point);
				} else {
					pointTick(point);
				}
			}

			while (!marchs.isEmpty()) {
				IYQZZWorldMarch march = marchs.pop();
				marchTick(march);
			}

		} catch (Exception e) {
			HawkException.catchException(e);
		} finally {
			readyToRace.set(false);
			// 时间消耗的统计信息
			long costTimeMs = HawkTime.getMillisecond() - beginTimeMs;
			if (costTimeMs > 200) {
				DungeonRedisLog.log(battleId, "{} process tick too much time, costtime: {}", Thread.currentThread().getName(), costTimeMs);
			}
		}
		return true;
	}

	private void marchTick(IYQZZWorldMarch march) {
		try {
			if(march.getParent().getParent().isGameOver()){
				return;
			}
			
			long beginTimeMs = HawkTime.getMillisecond();
			int statusOld = march.getMarchStatus();
			String armyOld = march.getMarchEntity().getArmyStr();

			synchronized (march) { // 玩家操作也可以添加OP的方式到列表等待下次tick首行执行.
				if (march.getMarchType() != WorldMarchType.SPY && march.getMarchEntity().getArmyCount() == 0) {
					march.onMarchBack();
					march.remove();
					return;
				}
				if (march.getMarchStatus() > 0) {
					march.heartBeats();
				}
			}

			long costTimeMs = HawkTime.getMillisecond() - beginTimeMs;
			if (costTimeMs > 50) {
				DungeonRedisLog.log(battleId, "{} march tick too much time, costtime: {} Origion:{},{} Terminal:{},{} marchtype: {} marchstatus: {} -> {} army:{} -> {}",
						Thread.currentThread().getName(), costTimeMs,
						march.getOrigionX(), march.getOrigionY(),
						march.getTerminalX(), march.getTerminalY(),
						march.getMarchType(),
						statusOld, march.getMarchStatus(),
						armyOld, march.getMarchEntity().getArmyStr());
			}

		} catch (Exception e) {
			march.onMarchBack();
			march.remove();
			HawkException.catchException(e);
		}
	}

	private void buildTick(IYQZZWorldPoint point) {
		try {
			if(point.getParent().isGameOver()){
				return;
			}
			
			long beginTimeMs = HawkTime.getMillisecond();
			IYQZZBuilding build = (IYQZZBuilding) point;
			YQZZBuildState stateOld = build.getState();

			point.onTick();
			long costTimeMs = HawkTime.getMillisecond() - beginTimeMs;
			if (costTimeMs > 50) {
				DungeonRedisLog.log(battleId, "{} build tick too much time, costtime: {} pos:{},{}  cfgId: {} state: {} -> {}", Thread.currentThread().getName(), costTimeMs,
						build.getX(), build.getY(),
						build.getCfgId(),
						stateOld.name(), build.getState().name());
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	private void pointTick(IYQZZWorldPoint point) {
		try {
			if (point.getParent().isGameOver()) {
				return;
			}
			
			long beginTimeMs = HawkTime.getMillisecond();
			point.onTick();
			long costTimeMs = HawkTime.getMillisecond() - beginTimeMs;
			if (costTimeMs > 20) {
				DungeonRedisLog.log(battleId, "{} point tick too much time, costtime: {} point {}", Thread.currentThread().getName(), costTimeMs, point.getPointType());
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	public boolean isReadyToRace() {
		return readyToRace.get();
	}

	public void readyToRace() {
		readyToRace.set(true);
	}

	public String getBattleId() {
		return battleId;
	}

	public void setBattleId(String battleId) {
		this.battleId = battleId;
	}

	public boolean addPoint(IYQZZWorldPoint task) {
		if (readyToRace.get()) {
			return false;
		}

		viewPoints.add(task);
		return true;
	}

	public boolean addMarch(IYQZZWorldMarch task) {
		if (readyToRace.get()) {
			return false;
		}

		marchs.add(task);
		return true;
	}

	public int getThreadNum() {
		return threadNum;
	}

	public void setThreadNum(int threadNum) {
		this.threadNum = threadNum;
	}

}
