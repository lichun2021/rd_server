package com.hawk.game.script;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.hawk.log.HawkLog;
import org.hawk.os.HawkRand;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import org.hawk.tickable.HawkPeriodTickable;

import com.hawk.game.GsApp;
import com.hawk.game.GsConfig;
import com.hawk.game.global.GlobalData;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Script.ScriptError;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.queryentity.AccountInfo;
import com.hawk.game.util.EffectParams;
import com.hawk.game.util.GameUtil;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.service.WorldPlayerService;

/**
 * 自动机器人PVP战斗, 生成地图的PVE行军
 *
 * localhost:8080/script/robotwar?count=?&distance=?
 *
 * count: 参与战斗行军的机器人数量
 * distance: 发起行军的距离限制(可选)
 *
 * @author hawk
 *
 */
public class RobotWorldWarHandler extends HawkScript {
	/**
	 * 自动行军的距离
	 */
	private static int AUTO_MARCH_DISTANCE = 200;

	static RobotWarLaunch robotWarLaunch = null;

	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		if (!GsConfig.getInstance().isDebug()) {
			return HawkScript.failedResponse(SCRIPT_ERROR, "");
		}
		
		List<AccountInfo> accountList = new LinkedList<AccountInfo>();
		GlobalData.getInstance().getAccountList(accountList);
		Iterator<AccountInfo> iterator = accountList.iterator();
		while (iterator.hasNext()) {
			AccountInfo accountInfo = iterator.next();
			if (accountInfo.getPuid().indexOf("robot") < 0) {
				iterator.remove();
			}
		}

		Collections.shuffle(accountList);

		int robotCount = 0;
		if (params.containsKey("count")) {
			robotCount = Integer.valueOf(params.get("count"));
		}
		if (robotCount <= 0 || robotCount > accountList.size()) {
			return HawkScript.failedResponse(ScriptError.PARAMS_ERROR_VALUE, "join battle robot error count: " + robotCount + "/" + accountList.size());
		}

		accountList.subList(robotCount, accountList.size()).clear();

		if (params.containsKey("distance")) {
			AUTO_MARCH_DISTANCE = Integer.valueOf(params.get("distance"));
		}

		GsApp.getInstance().getTickableContainer().removeTickable(robotWarLaunch);
		robotWarLaunch = new RobotWarLaunch(accountList);
		GsApp.getInstance().addTickable(robotWarLaunch);

		return HawkScript.successResponse("join battle robot count: " + accountList.size() + " distance: " + AUTO_MARCH_DISTANCE);
	}

	class RobotWarLaunch extends HawkPeriodTickable {
		private List<AccountInfo> accountList;

		public RobotWarLaunch(List<AccountInfo> accountList) {
			super(10000, 10000);
			this.accountList = accountList;
		}

		@Override
		public void onPeriodTick() {
			// 每次乱序一下
			for (int i = 0; accountList.size() > 1 && i < accountList.size(); i++) {
				// 每个机器人只有一条行军
				AccountInfo accountInfo = accountList.get(i);
				if (WorldMarchService.getInstance().getPlayerMarchCount(accountInfo.getPlayerId()) > 0) {
					continue;
				}

				Player player = GlobalData.getInstance().scriptMakesurePlayer(accountInfo.getPlayerId());
				if (player == null) {
					continue;
				}

				// 检测机器人城点是否被清理了
				int[] posInfo = WorldPlayerService.getInstance().getPlayerPosXY(player.getId());
				if (posInfo[0] <= 0 || posInfo[1] <= 0) {
					WorldPlayerService.getInstance().randomSettlePoint(player, false);
				}
				// 随机范围内目标
				AccountInfo targetAccount = randNearTarget(accountInfo);
				if (targetAccount == null) {
					continue;
				}

				int[] targetPos = WorldPlayerService.getInstance().getPlayerPosXY(targetAccount.getPlayerId());
				int posId = GameUtil.combineXAndY(targetPos[0], targetPos[1]);
				List<ArmyInfo> armyList = new ArrayList<ArmyInfo>();
				armyList.add(new ArmyInfo(100011, 100));
				armyList.add(new ArmyInfo(100021, 100));
				armyList.add(new ArmyInfo(100031, 100));
				armyList.add(new ArmyInfo(100041, 100));

				// 开启行军
				WorldMarchService.getInstance().startMarch(player, WorldMarchType.ATTACK_PLAYER_VALUE, posId, targetAccount.getPlayerId(), new EffectParams());

				HawkLog.logPrintln("robot start march, robotId: {}, marchType: {}, targetId: {}, targetPos: ({}, {})",
						accountInfo.getPlayerId(), WorldMarchType.ATTACK_PLAYER_VALUE,
						targetAccount.getPlayerId(), targetPos[0], targetPos[1]);
			}
		}

		public AccountInfo randNearTarget(AccountInfo accountInfo) {
			int[] selfPos = WorldPlayerService.getInstance().getPlayerPosXY(accountInfo.getPlayerId());
			if (selfPos[0] <= 0 || selfPos[1] <= 0) {
				return null;
			}

			List<AccountInfo> nearTargetList = new LinkedList<AccountInfo>();
			for (int i = 0; accountList.size() > 1 && i < accountList.size(); i++) {
				AccountInfo targetAccount = accountList.get(i);
				if (targetAccount.getPlayerId().equals(accountInfo.getPlayerId())) {
					continue;
				}

				int[] targetPos = WorldPlayerService.getInstance().getPlayerPosXY(targetAccount.getPlayerId());
				if (targetPos[0] <= 0 || targetPos[1] <= 0) {
					continue;
				}

				if (Math.abs(selfPos[0] - targetPos[0]) <= AUTO_MARCH_DISTANCE && Math.abs(selfPos[1] - targetPos[1]) <= AUTO_MARCH_DISTANCE) {
					nearTargetList.add(targetAccount);
				}
			}

			if (nearTargetList.size() > 0) {
				return nearTargetList.get(HawkRand.randInt(nearTargetList.size() - 1));
			}
			return null;
		}
	}

}