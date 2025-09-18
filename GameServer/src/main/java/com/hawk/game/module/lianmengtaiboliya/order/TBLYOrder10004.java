package com.hawk.game.module.lianmengtaiboliya.order;

import java.util.ArrayList;
import java.util.List;

import com.hawk.game.march.ArmyInfo;
import com.hawk.game.module.lianmengtaiboliya.ITBLYWorldPoint;
import com.hawk.game.module.lianmengtaiboliya.worldmarch.ITBLYWorldMarch;
import com.hawk.game.module.lianmengtaiboliya.worldpoint.ITBLYBuilding;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.TBLY.PBTBLYOrderUseReq;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.util.WorldUtil;
import com.hawk.game.world.WorldMarch;

/**
 * 新增5个号令效果
1. 围困：对某个敌方占领中的建筑使用，使得该建筑无法被敌方部队增援，持续X秒。
无法被增援，指的是：当前行军中的增援部队到达目的地后会立即返回。以及无法发起新的个人增援和组队增援。
2. 快速撤退：我方部队返回时间固定为3秒，持续X秒。
  仅技能生效期间发起的返回行军生效，正在行军中的不受影响。
3. 驰援：对某个我方占领中的建筑使用，使得对该建筑的援助行军速度固定为3秒，持续X秒。
  仅技能生效期间发起的援助行军生效，正在行军中的不影响。
4. 急救：对某个我方占领中的建筑使用，立即恢复该建筑内的一定比例伤兵。
  恢复后士兵数量不能超过集结容量
5. 立即控制：对当前我方占领中的某个建筑使用，立刻完成控制。（用CD和消耗控制，一局可以使用2次左右）
 * @author lwt
 * @date 2023年8月14日
 */
public class TBLYOrder10004 extends TBLYOrder {

	@Override
	public void onTick() {
		super.onTick();

	}

	@Override
	public int canStartOrder(PBTBLYOrderUseReq req) {
		int result = super.canStartOrder(req);
		if (result > 0) {
			return result;
		}
		ITBLYWorldPoint point = getParent().getParent().getWorldPoint(req.getX(), req.getY()).orElse(null);
		if (point == null || !(point instanceof ITBLYBuilding)) {
			return Status.SysError.DATA_ERROR_VALUE;
		}
		ITBLYBuilding tpoint = (ITBLYBuilding) point;
		if (tpoint.getGuildCamp() != getParent().getCamp()) {
			return Status.Error.TBLY_BUILD_STATUS_VALUE;
		}

		return 0;
	}

	@Override
	public void startOrder(PBTBLYOrderUseReq req) {
		super.startOrder(req);
		ITBLYWorldPoint point = getParent().getParent().getWorldPoint(req.getX(), req.getY()).orElse(null);
		ITBLYBuilding tpoint = (ITBLYBuilding) point;

		List<ITBLYWorldMarch> stayMarchList = getParent().getParent().getPointMarches(tpoint.getPointId(), WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED);
		ITBLYWorldMarch leaderMarch = tpoint.getLeaderMarch();
		int maxMassSoldierNum = leaderMarch.getMaxMassJoinSoldierNum(leaderMarch.getParent());
		List<WorldMarch> ppList = new ArrayList<>();
		for (ITBLYWorldMarch worldMarch : stayMarchList) {
			ppList.add(worldMarch.getMarchEntity());
		}
		int curPopulationCnt = WorldUtil.calcMarchsSoldierCnt(ppList); // 已驻扎士兵人口
		int remainArmyPopu = maxMassSoldierNum - curPopulationCnt;

		double killPct = getConfig().getNumber() * 0.01;

		for (ITBLYWorldMarch march : stayMarchList) {
			if (remainArmyPopu <= 0) {
				break;
			}
			List<ArmyInfo> armys = march.getArmys();
			for (ArmyInfo army : armys) {
				if (remainArmyPopu > 0) {
					int wounded = army.getWoundedCount();
					int add = (int) Math.min(remainArmyPopu, wounded * killPct);
					army.setWoundedCount(army.getWoundedCount() - add);
					remainArmyPopu -= add;
				}
			}
			march.getMarchEntity().setArmys(armys);
			march.updateMarch();
		}
	}

}
