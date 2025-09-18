package com.hawk.game.invoker;

import org.hawk.app.HawkAppObj;
import org.hawk.log.HawkLog;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;
import org.hawk.os.HawkTime;

import com.hawk.game.entity.NationBuildQuestEntity;
import com.hawk.game.nation.NationService;
import com.hawk.game.nation.NationalBuilding;
import com.hawk.game.nation.construction.NationConstruction;
import com.hawk.game.nation.construction.model.NationalBuildQuestModel;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.National.NationRedDot;
import com.hawk.game.protocol.National.NationbuildingType;
import com.hawk.game.util.LogUtil;
import com.hawk.game.world.march.impl.NationalConstructMarch;

/**
 * 国家任务结束时玩家线程处理
 * @author zhenyu.shang
 * @since 2022年4月14日
 */
public class NationalBuildMarchReturnMsgInvoker extends HawkMsgInvoker {

	private Player player;
	
	private NationalConstructMarch march;
	
	public NationalBuildMarchReturnMsgInvoker(Player player, NationalConstructMarch march) {
		this.player = player;
		this.march = march;
	}
	
	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
		// 获取建设处
		NationConstruction construction = (NationConstruction) NationService.getInstance().getNationBuildingByType(NationbuildingType.NATION_BUILDING_CENTER);
		// 判断当前任务次数是否足够
		NationBuildQuestEntity nbq = player.getData().getNationalBuildQuestEntity();
		// 任务信息
		NationalBuildQuestModel model = nbq.getQuestsMap().get(march.getMarchEntity().getTargetId());
		// 判断是否已经做完任务
		if(march.getMarchEntity().getMarchIntention() == 1){
			// 行军到达后，玩家队列扣除次数，并且删除当前任务信息
			// 扣除玩家次数
			nbq.setQuestTimes(nbq.getQuestTimes() - 1);
			// 删除当前任务
			nbq.removeQuest(march.getMarchEntity().getTargetId());
			// 新增一个任务
			nbq.replaceOverQuest(construction.getEntity().getLevel());
			
			NationalBuilding building = NationService.getInstance().getNationBuildingByTypeId(model.getBuildId());
			// 完成日志
			LogUtil.logNationbuildQuestOver(player, model.getQuestCfgId(), nbq.getQuestTimes(), model.getBuildId(), nbq.getQuestType().getNumber(), 
					march.getBeforeValue(), march.getAfterValue(), building.getEntity().getBuildVal() + "_" + building.getBuildDayLimit(), construction.getEntity().getLevel());
		} else {
			long now = HawkTime.getMillisecond();
			long alreadyTime = march.getMarchEntity().getResStartTime() == 0L ? 0 : now - march.getMarchEntity().getResStartTime();
			long totalTime = march.getMarchEntity().getResEndTime() - march.getMarchEntity().getResStartTime();
			// 计算当前进度百分比,
			int currentPercent = 0;
			// 这里加个异常判断防止流程中断
			if(totalTime <= 0){
				HawkLog.errPrintln("[national][ConstructionMarch] build total time error, totalTime:{}, playerId:{}", totalTime, march.getPlayerId());
			} else {
				currentPercent = (int) ((alreadyTime * 1.0 / totalTime) * 100);
			}
			// 清空行军消息, 并保存进度
			nbq.cancelQuest(march.getMarchEntity().getTargetId(), currentPercent);
			// 中途召回日志
			LogUtil.logNationbuildQuestCancel(player, model.getQuestCfgId(), alreadyTime);
		}
		// 通知玩家任务更新
		construction.sendQuestInfoToClient(player);
		
		// 判断当前是否有可接任务，并且次数足够
		if(nbq.checkRd()) {
			player.updateNationRDAndNotify(NationRedDot.CONSTRUCTION_IDLE);
		}
		
		return true;
	}

	public Player getPlayer() {
		return player;
	}

	public NationalConstructMarch getMarch() {
		return march;
	}
}
