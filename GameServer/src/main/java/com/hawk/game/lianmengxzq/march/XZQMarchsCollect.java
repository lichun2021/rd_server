package com.hawk.game.lianmengxzq.march;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

import com.hawk.game.global.GlobalData;
import com.hawk.game.player.Player;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.march.IWorldMarch;

public class XZQMarchsCollect {
	/**
	 * 小战区行军
	 */
	private final Map<Integer, BlockingDeque<String>> xzqMarchs = new ConcurrentHashMap<Integer, BlockingDeque<String>>();;

	/**
	 * 获取小战区行军
	 * @param pointId
	 * @return
	 */
	public BlockingDeque<String> getXZQMarchs(int pointId) {
		if (!xzqMarchs.containsKey(pointId)) {
			BlockingDeque<String> marchs = new LinkedBlockingDeque<String>();
			xzqMarchs.put(pointId, marchs);
		}
		return xzqMarchs.get(pointId);
	}

	/**
	 * 小战区队长
	 * @return
	 */
	public Player getXZQLeader(int pointId) {
		String leaderMarchId = getXZQLeaderMarchId(pointId);
		if (HawkOSOperator.isEmptyString(leaderMarchId)) {
			return null;
		}

		IWorldMarch leaderMarch = WorldMarchService.getInstance().getMarch(leaderMarchId);
		Player leader = GlobalData.getInstance().makesurePlayer(leaderMarch.getPlayerId());
		return leader;
	}

	/**
	 * 添加小战区行军
	 * @param pointId
	 * @param march
	 * @param isInit
	 */
	public void addXZQMarch(int pointId, IWorldMarch march, boolean isInit) {
		BlockingDeque<String> marchs = getXZQMarchs(pointId);
		// 初始化需要判断队长行军
		if (isInit) {
			if (march.getPlayerId().equals(march.getMarchEntity().getLeaderPlayerId())) {
				marchs.addFirst(march.getMarchId());
			} else {
				marchs.add(march.getMarchId());
			}
		} else {
			// 如果没有行军，则此行军为队长行军
			if (marchs.isEmpty()) {
				march.getMarchEntity().setLeaderPlayerId(march.getPlayerId());
				marchs.add(march.getMarchId());
			} else {
				IWorldMarch leaderMarch = WorldMarchService.getInstance().getMarch(marchs.getFirst());
				march.getMarchEntity().setLeaderPlayerId(leaderMarch.getPlayerId());
				marchs.add(march.getMarchId());
			}
		}
	}

	/**
	 * 更换小战区驻军队长
	 * @param pointId
	 * @param targetPlayerId
	 */
	public void changeXZQMarchLeader(int pointId, String targetPlayerId) {
		String changeMarchId = null;

		BlockingDeque<String> marchs = getXZQMarchs(pointId);
		for (String marchId : marchs) {
			IWorldMarch march = WorldMarchService.getInstance().getMarch(marchId);
			if (!targetPlayerId.equals(march.getPlayerId())) {
				continue;
			}
			changeMarchId = march.getMarchId();
			break;
		}

		if (HawkOSOperator.isEmptyString(changeMarchId)) {
			return;
		}

		marchs.remove(changeMarchId);
		marchs.addFirst(changeMarchId);
	}

	/**
	 * 解散小战区行军
	 */
	public void dissolveAllXZQQuarteredMarchs(int pointId) {
		BlockingDeque<String> marchIds = getXZQMarchs(pointId);
		for (String marchId : marchIds) {
			IWorldMarch worldMarch = WorldMarchService.getInstance().getMarch(marchId);
			if (worldMarch == null || worldMarch.getMarchEntity().isInvalid()) {
				continue;
			}
			WorldMarchService.getInstance().onMarchReturn(worldMarch, HawkTime.getMillisecond(), 0);
		}
	}

	/**
	 * 获取小战区队长行军id
	 * @param pointId
	 * @return
	 */
	public String getXZQLeaderMarchId(int pointId) {
		BlockingDeque<String> marchIds = getXZQMarchs(pointId);
		if (marchIds.isEmpty()) {
			return null;
		}
		return marchIds.getFirst();
	}

	/**
	 * 获取小战区行军
	 * @param pointId
	 * @return
	 */
	public List<IWorldMarch> getXZQStayMarchs(int pointId) {
		BlockingDeque<String> marchIds = getXZQMarchs(pointId);
		List<IWorldMarch> stayMarchs = new ArrayList<IWorldMarch>();

		for (String marchId : marchIds) {
			IWorldMarch march = WorldMarchService.getInstance().getMarch(marchId);
			if (march == null || march.getMarchEntity().isInvalid()) {
				continue;
			}
			stayMarchs.add(march);
		}
		return stayMarchs;
	}

	/**
	 * 是否有小战区行军
	 * @return
	 */
	public boolean hasXZQMarch(int index) {
		return !getXZQMarchs(index).isEmpty();
	}

	/**
	 * 移除小战区行军
	 * @param pointId
	 * @param rmMarchId
	 */
	public void removeXZQMarch(int pointId, String rmMarchId) {
		BlockingDeque<String> marchIds = getXZQMarchs(pointId);
		if (!marchIds.contains(rmMarchId)) {
			return;
		}

		// 是否是队长行军
		boolean isLeaderMarch = rmMarchId.equals(marchIds.getFirst());
		marchIds.remove(rmMarchId);
		if (marchIds.isEmpty()) {
			return;
		}
		if (isLeaderMarch) {
			// 重设队长
			String newLeaderMarchId = marchIds.getFirst();
			IWorldMarch leaderMarch = WorldMarchService.getInstance().getMarch(newLeaderMarchId);
			Player newLeader = GlobalData.getInstance().makesurePlayer(leaderMarch.getPlayerId());
			String leaderId = newLeader.getId();
			for (String marchId : marchIds) {
				IWorldMarch march = WorldMarchService.getInstance().getMarch(marchId);
				march.getMarchEntity().setLeaderPlayerId(leaderId);
			}
		}
	}
}
