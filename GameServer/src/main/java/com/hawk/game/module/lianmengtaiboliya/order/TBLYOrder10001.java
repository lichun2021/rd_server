package com.hawk.game.module.lianmengtaiboliya.order;

import com.hawk.game.module.lianmengtaiboliya.ITBLYWorldPoint;
import com.hawk.game.module.lianmengtaiboliya.TBLYRoomManager.CAMP;
import com.hawk.game.module.lianmengtaiboliya.worldpoint.ITBLYBuilding;
import com.hawk.game.module.lianmengtaiboliya.worldpoint.TBLYBuildState;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.protocol.Const.NoticeCfgId;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.TBLY.PBTBLYOrderUseReq;
import com.hawk.game.protocol.World.PBTBLYBuildSkill;
import com.hawk.game.service.chat.ChatParames;

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
public class TBLYOrder10001 extends TBLYOrder {
	ITBLYBuilding target;

	@Override
	public void onTick() {
		super.onTick();
		if (target != null) {
			if (!inEffect() || target.getGuildCamp() == getParent().getCamp()) {
				target.setOrder10001DebuffCamp(null);
				target.getShowOrder().remove(TBLYOrderCollection.Order10001);
				getParent().getParent().worldPointUpdate(target);
				setEffectEndTime(0);
				target = null;
			}
		}
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
		ITBLYBuilding build = (ITBLYBuilding) point;
		if (build.getGuildCamp() == getParent().getCamp()) {
			return Status.Error.TBLY_BUILD_STATUS_VALUE;
		}

		if (build.getOrder10001DebuffCamp() != null) {
			return Status.Error.TBLY_ORDER10001_DOUBLE_VALUE;
		}

		return 0;
	}

	@Override
	public void startOrder(PBTBLYOrderUseReq req) {
		super.startOrder(req);
		ITBLYWorldPoint point = getParent().getParent().getWorldPoint(req.getX(), req.getY()).orElse(null);
		target = (ITBLYBuilding) point;
		CAMP order10001DebuffCamp = getParent().getCamp() == CAMP.A ? CAMP.B : CAMP.A;
		target.setOrder10001DebuffCamp(order10001DebuffCamp);
		PBTBLYBuildSkill buff = PBTBLYBuildSkill.newBuilder()
				.setSkillId(TBLYOrderCollection.Order10001)
				.setCamp(getParent().getCamp().intValue())
				.setX(target.getX())
				.setY(target.getY())
				.setStartTime(getEffectStartTime())
				.setEndTime(getEffectEndTime())
				.build();
		target.getShowOrder().put(TBLYOrderCollection.Order10001, buff);
		getParent().getParent().worldPointUpdate(target);
		// 1. 使用围困技能时，发送公告：{0}{1}对{2}使用了围困，效果持续X秒。
		// 2. 使用驰援技能时，发送公告：{0}{1}对{2}使用了驰援，效果持续X秒。
		// 3. 使用立即控制时，发送公告：{0}{1}对{2}使用了立即控制，立即获得了该建筑的控制权。
		ChatParames parames = ChatParames.newBuilder().setChatType(ChatType.CHAT_FUBEN_SPECIAL_BROADCAST).setKey(NoticeCfgId.TBLY_WEIKUN)
				.addParms(getParent().getParent().getCampGuildTag(getParent().getCamp()))
				.addParms(getParent().getParent().getCampGuildName(getParent().getCamp()))
				.addParms(target.getX())
				.addParms(target.getY()).build();
		getParent().getParent().addWorldBroadcastMsg(parames);
	}

}
