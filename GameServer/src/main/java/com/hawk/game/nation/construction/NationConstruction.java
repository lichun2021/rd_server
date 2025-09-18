package com.hawk.game.nation.construction;

import java.util.List;

import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkTime;

import com.hawk.game.config.NationConstCfg;
import com.hawk.game.entity.NationBuildQuestEntity;
import com.hawk.game.entity.NationConstructionEntity;
import com.hawk.game.nation.NationService;
import com.hawk.game.nation.NationalBuilding;
import com.hawk.game.nation.construction.model.NationalBuildQuestModel;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.National.NationBuildQuestInfo;
import com.hawk.game.protocol.National.NationConstructDetailInfoPB;
import com.hawk.game.protocol.National.NationStatus;
import com.hawk.game.protocol.National.NationbuildingType;

/**
 * 国家建设处
 * @author zhenyu.shang
 * @since 2022年3月22日
 */
public class NationConstruction extends NationalBuilding {
	
	
	public NationConstruction(NationConstructionEntity entity, NationbuildingType buildType) {
		super(entity, buildType);
	}

	@Override
	public boolean init() {
		// 判断建设处状态，同步国家整体状态
		if(this.entity.getLevel() > 0){
			NationService.getInstance().setNationStatus(NationStatus.COMPLETE, "NationConstruction init over");
		}
		return false;
	}

	@Override
	public void levelupOver() {
		// 当建设处初次建设完成时，将国家整体状态修改为完成状态
		if(this.entity.getLevel() == 1){
			// 通知国家状态
			NationService.getInstance().changeStatusNotify(NationStatus.COMPLETE, "NationConstruction build over");
		}
	}

	@Override
	public void levelupStart() {
		
	}

	public void doEnterBuilding(Player player) {
		// 判断玩家是否选择过类型
		NationBuildQuestEntity entity = player.getData().getNationalBuildQuestEntity();
		if(entity.getNationQuestType() == 0 && entity.getQuestsMap().size() == 0){ // 首次进入。创建任务信息
			this.createNewPlayerQuest(entity);
		}
		// 获取任务数据并发送
		sendQuestInfoToClient(player);
	}

	/**
	 * 创建玩家任务条目
	 * @param player
	 * @return
	 */
	public void createNewPlayerQuest(NationBuildQuestEntity entity){
		entity.setRefreshCount(0);
		entity.setQuestTimes(NationConstCfg.getInstance().getTimesLimit()); // 初始给上限
		entity.setResetTime(HawkTime.getMillisecond());
		// 初始化任务
		entity.initRandomQuest(getEntity().getLevel());
	}

	
	/**
	 * 刷新任务
	 * @param player
	 * @param buildings
	 */
	public void refreshQuest(Player player, List<Integer> buildings){
		NationBuildQuestEntity entity = player.getData().getNationalBuildQuestEntity();
		// 增加任务次数
		entity.incrRefreshCount();
		// 刷新任务
		entity.refreshQuest(this.getEntity().getLevel(), buildings, true);
		// 推送给前端
		sendQuestInfoToClient(player);
	}
	
	/**
	 * 建设处消息推送给前端
	 * @param player
	 */
	public void sendQuestInfoToClient(Player player){
		NationConstructDetailInfoPB.Builder builder = NationConstructDetailInfoPB.newBuilder();
		NationBuildQuestEntity entity = player.getData().getNationalBuildQuestEntity();
		
		builder.setQuestLeftCount(entity.getQuestTimes());
		builder.setRefreshCount(entity.getRefreshCount());
		if(entity.getNationQuestType() > 0){
			builder.setQuestType(entity.getQuestType());
		}
		builder.addAllBuildings(NationService.getInstance().makeAllBuildingPBBuilder());
		
		for (NationalBuildQuestModel model : entity.getQuestsMap().values()) {
			NationBuildQuestInfo.Builder qb = NationBuildQuestInfo.newBuilder();
			qb.setBtype(NationbuildingType.valueOf(model.getBuildId()));
			qb.setQuestCfgId(model.getQuestCfgId());
			qb.setQuestId(model.getQuestId());
			qb.setProcessor(model.getCurrentProcess());
			String marchId = model.getMarchId();
			if(marchId != null){
				qb.setMarchId(marchId);
			}
			// 添加任务
			builder.addQuests(qb);
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.NATIONAL_CONSTRUCTION_INFO_S_VALUE, builder));
	}

	@Override
	public boolean checkStateCanBuild() {
		return true;
	}
	
	@Override
	public int getNationBuildingVal() {
		if(this.getLevel() == 0) {
			return NationService.getInstance().getRebuildingLife();
		}
		return super.getNationBuildingVal();
	}
}
