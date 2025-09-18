package com.hawk.game.world.thread.tasks;

import java.util.Set;

import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;
import org.hawk.thread.HawkTask;

import com.hawk.game.config.GameConstCfg;
import com.hawk.game.global.GlobalData;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.World.MarchData;
import com.hawk.game.protocol.World.MarchEvent;
import com.hawk.game.protocol.World.MarchEventSync;
import com.hawk.game.protocol.World.WorldMarchPB;
import com.hawk.game.protocol.World.WorldMarchRelation;
import com.hawk.game.util.WorldUtil;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.march.IWorldMarch;

/**
 * 广播行军更新
 * @author zhenyu.shang
 * @since 2018年1月22日
 */
public class NotifyMarchUpdateTask extends HawkTask{

	private int eventType;
	
	private WorldMarch march;
	
	public NotifyMarchUpdateTask(int eventType, WorldMarch march) {
		this.eventType = eventType;
		this.march = march;
	}
	
	@Override
	public Object run() {
		long beginTimeMs = HawkTime.getMillisecond();
		// 计算可见起点和终点的玩家集合
		Set<String> effectPlayerIds = null;
		try {
			// 计算不同事件的同步半径
			int viewRadiusX = GameConstCfg.getInstance().getViewXRadius();
			int viewRadiusY = GameConstCfg.getInstance().getViewYRadius();
			if (eventType == MarchEvent.MARCH_UPDATE_VALUE || eventType == MarchEvent.MARCH_DELETE_VALUE) {
				viewRadiusX *= 2;
				viewRadiusY *= 2;
			}
	
			// 分别计算起点和终点的影响玩家列表
			Set<String> origionPlayerIds = WorldUtil.calcPointViewers(march.getOrigionX(), march.getOrigionY(), viewRadiusX, viewRadiusY);
			Set<String> terminalPlayerIds = WorldUtil.calcPointViewers(march.getTerminalX(), march.getTerminalY(), viewRadiusX, viewRadiusY);
			if (origionPlayerIds != null) {
				effectPlayerIds = origionPlayerIds;
				if (terminalPlayerIds != null) {
					effectPlayerIds.addAll(terminalPlayerIds);
				}
			} else {
				effectPlayerIds = terminalPlayerIds;
			}
	
			try {
				// 拿参与集结的人
				IWorldMarch leaderMarch = null;
				IWorldMarch iMarch = WorldMarchService.getInstance().getMarch(march.getMarchId());
				if (iMarch != null) {
					if (iMarch.isMassMarch()) {
						leaderMarch = iMarch;
					}
					if (iMarch.isMassJoinMarch()) {
						leaderMarch = WorldMarchService.getInstance().getMarch(march.getTargetId());
					}
					if (leaderMarch != null) {
						Set<? extends IWorldMarch> massJoinMarchs = leaderMarch.getMassJoinMarchs(false);
						for (IWorldMarch joinMarch : massJoinMarchs) {
							if (joinMarch == null) {
								continue;
							}
							effectPlayerIds.add(joinMarch.getPlayerId());
						}
					}
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
			
			// 判断最终受影响的玩家列表
			if (effectPlayerIds == null || effectPlayerIds.size() <= 0) {
				return null;
			}
			WorldMarchPB.Builder marchBuilder = WorldMarchPB.newBuilder();
			// 通知所有影响到的玩家本条行军事件
			for (String playerId : effectPlayerIds) {
				try {
					Player player = GlobalData.getInstance().getActivePlayer(playerId);
					if (player != null) {
						// 自己的行军不走这种同步模式
						WorldMarchRelation relation = WorldUtil.getRelation(march, player);
						if (relation.equals(WorldMarchRelation.SELF)) {
							continue;
						}
	
						MarchEventSync.Builder builder = MarchEventSync.newBuilder();
						builder.setEventType(eventType);
	
						MarchData.Builder dataBuilder = MarchData.newBuilder();
						dataBuilder.setMarchId(march.getMarchId());
						if (eventType != MarchEvent.MARCH_DELETE_VALUE) {
							dataBuilder.setMarchPB(march.toBuilder(marchBuilder.clear(), relation));
						}
						builder.addMarchData(dataBuilder);
	
						player.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_MARCH_EVENT_SYNC_VALUE, builder));
					}
				} catch (Exception e) {
					HawkException.catchException(e);
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		} finally {
			// 时间消耗的统计信息
			long costTimeMs = HawkTime.getMillisecond() - beginTimeMs;
			if (costTimeMs > 200) {
				HawkLog.warnPrintln("process notifyMarchEvent too much time, costtime: {}, marchType: {}, marchstatus :{}, effectIdsNum: {}", 
						costTimeMs, march.getMarchType(), march.getMarchStatus(), effectPlayerIds == null ? 0 : effectPlayerIds.size());
			}
		}
		return null;
	}

}
