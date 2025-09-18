package com.hawk.game.module.nation;

import java.util.List;

import org.hawk.annotation.MessageHandler;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.app.HawkApp;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.activity.helper.PlayerAcrossDayLoginMsg;
import com.hawk.game.config.NationConstCfg;
import com.hawk.game.entity.NationBuildQuestEntity;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.nation.NationService;
import com.hawk.game.nation.NationalBuilding;
import com.hawk.game.nation.construction.NationConstruction;
import com.hawk.game.nation.construction.model.NationalBuildQuestModel;
import com.hawk.game.nation.construction.model.NationalDonatModel;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.president.PresidentFightService;
import com.hawk.game.president.PresidentOfficier;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.National.NationBaseInfoPB;
import com.hawk.game.protocol.National.NationBuildQuestType;
import com.hawk.game.protocol.National.NationBuildingInfoPB;
import com.hawk.game.protocol.National.NationBuildingState;
import com.hawk.game.protocol.National.NationDonateInfoPB;
import com.hawk.game.protocol.National.NationDonatePB;
import com.hawk.game.protocol.National.NationOpenBuilding;
import com.hawk.game.protocol.National.NationRedDot;
import com.hawk.game.protocol.National.NationSelectQuestType;
import com.hawk.game.protocol.National.NationStatus;
import com.hawk.game.protocol.National.NationbuildingType;
import com.hawk.game.protocol.Status;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.LogUtil;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.thread.WorldTask;
import com.hawk.game.world.thread.WorldThreadScheduler;
import com.hawk.log.Action;

/**
 * 国建建设处
 * @author zhenyu.shang
 * @since 2022年3月24日
 */
public class PlayerNationContructionModule extends PlayerModule {
	
	public static final Logger logger = LoggerFactory.getLogger("Server");
	
	/**
	 * 上次刷新时间
	 */
	private long lastRefreshTime = 0;

	
	public PlayerNationContructionModule(Player player) {
		super(player);
	}

	
	@Override
	protected boolean onPlayerLogin() {
		// 跨服玩家不推送
		NationStatus nationStatus = NationService.getInstance().getNationStatus();
		// 国家系统没开启，则不推送
		if(nationStatus == NationStatus.UNOPEN) {
			return true;
		}
		// 推送国家当前整体状态
		NationBaseInfoPB.Builder builder = NationService.getInstance().makeNationalStatusPB();
		if(player.isCsPlayer()) {
			builder.setCrossNation(true); // 标记当前是跨服服务器的国家状态
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.NATIONAL_INFO_SYNC_VALUE, builder));
		
		// 这里需要处理一下容错，如果任务中得行军找不到了。就给任务行军置空
		NationBuildQuestEntity nbq = player.getData().getNationalBuildQuestEntity();
		if(!player.isCsPlayer() && nationStatus == NationStatus.COMPLETE) {
			boolean change = false;
			for (NationalBuildQuestModel model : nbq.getQuestsMap().values()) {
				String marchId = model.getMarchId();
				if(marchId != null && WorldMarchService.getInstance().getMarch(marchId) == null){
					model.setMarchId(null);
					change = true;
					logger.warn("nation build quest error, can not find marchId : {}", marchId);
				}
			}
			if(change) {
				nbq.notifyUpdate();
			}
			
			// 当前剩余次数减去已经出征的队列数
			if(nbq.checkRd()) {
				player.updateNationRDAndNotify(NationRedDot.CONSTRUCTION_IDLE);
			}
		}
		return true;
	}
	
	
	@Override
	protected boolean onPlayerAssemble() {
		try {
			resetQuestTimes(false);
		} catch (Exception e) {
			HawkException.catchException(e, "assemble super gift");
		}
		return true;
	}
	
	@MessageHandler
	public void onCrossDay(PlayerAcrossDayLoginMsg msg) {
		resetQuestTimes(true);
	}


	/**
	 * 重置每日次数
	 */
	private void resetQuestTimes(boolean notify) {
		// 跨服玩家不处理
		if(player.isCsPlayer()) {
			return;
		}
		NationStatus nationStatus = NationService.getInstance().getNationStatus();
		// 国家系统没开启，则不执行
		if(nationStatus == NationStatus.UNOPEN) {
			return;
		}
		long now = HawkTime.getMillisecond();
		NationConstruction construction = (NationConstruction) NationService.getInstance().getNationBuildingByType(NationbuildingType.NATION_BUILDING_CENTER);
		NationBuildQuestEntity nbq = player.getData().getNationalBuildQuestEntity();
		// 判断跨天
		if(nbq != null && nbq.getResetTime() > 0 && !HawkTime.isSameDay(now, nbq.getResetTime())) {
			// 重置任务次数
			nbq.setQuestTimes(NationConstCfg.getInstance().getTimesLimit());
			// 重置刷新次数
			nbq.setRefreshCount(0);
			nbq.setResetTime(now);
			if(notify){
				// 主动推送给玩家数据
				construction.sendQuestInfoToClient(player);
			}
			// 当前剩余次数减去已经出征的队列数
			if(nbq.checkRd()) {
				player.updateNationRDAndNotify(NationRedDot.CONSTRUCTION_IDLE);
			}
		}
	}
	
	
	/**
	 * 更新
	 */
	@Override
	public boolean onTick() {
		if (!player.isCsPlayer()) {
			NationStatus nationStatus = NationService.getInstance().getNationStatus();
			// 国家系统没开启，则不执行
			if(nationStatus == NationStatus.UNOPEN) {
				return true;
			}
			long currentTime = HawkApp.getInstance().getCurrentTime();
			if (currentTime >= lastRefreshTime + 1000) {
				lastRefreshTime = currentTime;
				NationalDonatModel model = NationService.getInstance().getPlayerDonatInfo(player.getId());
				model.checkResumeCount(currentTime);
			}
		}
		return super.onTick();
	}
	
	
	/**
	 * 请求打开国家面板
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code2.NATIONAL_INFO_REQ_VALUE)
	private boolean onGetNationBaseInfo(HawkProtocol protocol) {
		// 推送国家当前整体状态（跨服不处理，请求的都回复本服的）
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.NATIONAL_INFO_SYNC_VALUE, NationService.getInstance().makeNationalStatusPB()));
		// 根据状态推送其他消息
		NationStatus nationStatus = NationService.getInstance().getNationStatus();
		if(nationStatus == NationStatus.INITING){
			// 返回捐献信息
			NationalDonatModel model = NationService.getInstance().getPlayerDonatInfo(player.getId());
			// 发送捐献信息
			sendCurrentDonateStatus(model);
		} else if(nationStatus == NationStatus.REBUILDING || nationStatus == NationStatus.COMPLETE){ // 重建中或者建好了之后
			// 获取当前所有建筑的状态。
			NationBuildingInfoPB.Builder builder = NationBuildingInfoPB.newBuilder();
			builder.addAllBuildings(NationService.getInstance().makeAllBuildingPBBuilder());
			// 发送建筑列表信息
			player.sendProtocol(HawkProtocol.valueOf(HP.code2.NATIONAL_BUILDING_INFO_SYNC_VALUE, builder));
		}
		return true;
	}


	private void sendCurrentDonateStatus(NationalDonatModel model) {
		NationDonateInfoPB.Builder builder = NationDonateInfoPB.newBuilder();
		builder.setCurrentDonatVal(NationService.getInstance().getRebuildingLife());
		builder.setDonatCount(model.getLeftcount());
		builder.setNextResumeTime(model.getResumeTime());
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.NATIONAL_DONATE_INFO_SYNC_VALUE, builder));
	}
	
	/**
	 * 国家捐献
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code2.NATIONAL_DONATE_REQ_VALUE)
	private boolean onNatioDonateRequest(HawkProtocol protocol) {
		NationDonatePB req = protocol.parseProtocol(NationDonatePB.getDefaultInstance());
		// 检查当前剩余次数
		NationalDonatModel model = NationService.getInstance().getPlayerDonatInfo(player.getId());
		if(model.getLeftcount() <= 0){
			player.sendError(HP.code2.NATIONAL_DONATE_REQ_VALUE, Status.Error.DONATE_TIMES_NOT_ENOUGH, 0);
			return true;
		}
		ItemInfo item = NationConstCfg.getInstance().getConsumeInfo(req.getResType());
		if(item == null){
			return false;
		}
		// 检查消耗
		ConsumeItems consume = ConsumeItems.valueOf();
		consume.addConsumeInfo(item, false);
		
		if(!consume.checkConsume(player)){
			return true;
		}
		// 消耗资源
		consume.consumeAndPush(player, Action.NATIONAL_DONATE_COST);
		// 执行捐献
		model.doDonate();
		// 更新redis
		LocalRedis.getInstance().updateNationalDonateInfo(player.getId(), model);
		// 给捐献奖励
		AwardItems items = AwardItems.valueOf(NationConstCfg.getInstance().getRebuildingAward());
		if(items != null){
			items.rewardTakeAffectAndPush(player, Action.NATIONAL_DONATE_AWARD, true);
		}
		// 增加捐献值
		WorldThreadScheduler.getInstance().postWorldTask(new WorldTask(GsConst.WorldTaskType.NATIONAL_REBUILD_DONATE) {
			@Override
			public boolean onInvoke() {
				int beforeVal = NationService.getInstance().getRebuildingLife();
				NationService.getInstance().addAndCheckRebuilding();
				int afterVal = NationService.getInstance().getRebuildingLife();
				
				// 记log
				LogUtil.logNationRebuild(player, beforeVal, afterVal);
				// 推送当前捐献状态
				sendCurrentDonateStatus(model);
				return true;
			}
		});
		
		return true;
	}
	
	
	/**
	 * 打开国家建设处详细界面
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code2.NATIONAL_CONSTRUCTION_INFO_C_VALUE)
	private boolean onNatioConstructionInfoRequest(HawkProtocol protocol) {
		// 直接选取建设处
		NationConstruction construction = (NationConstruction) NationService.getInstance().getNationBuildingByType(NationbuildingType.NATION_BUILDING_CENTER);
		if(construction.getEntity().getLevel() < 1){
			return false;
		}
		// 进入对应建筑 (分为首次)
		construction.doEnterBuilding(player);
		return true;
	}
	
	/**
	 * 首次选择任务类型
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code2.NATIONAL_BUILD_QUEST_SEL_REQ_VALUE)
	private boolean onNatioBuildingQuestRequest(HawkProtocol protocol) {
		NationSelectQuestType req = protocol.parseProtocol(NationSelectQuestType.getDefaultInstance());
		NationBuildQuestType type = req.getQuestType();
		// 直接选取建设处
		NationConstruction construction = (NationConstruction) NationService.getInstance().getNationBuildingByType(NationbuildingType.NATION_BUILDING_CENTER);
		if(construction.getEntity().getLevel() < 1){
			return false;
		}
		NationBuildQuestEntity entity = player.getData().getNationalBuildQuestEntity();
		if(entity.getNationQuestType() > 0){
			return false;
		}
		entity.setNationQuestType(type.getNumber());
		// 回执
		player.responseSuccess(HP.code2.NATIONAL_BUILD_QUEST_SEL_REQ_VALUE);
		// 首次选择后推送红点
		player.updateNationRDAndNotify(NationRedDot.CONSTRUCTION_IDLE);
		
		return true;
	}
	
	/**
	 * 刷新任务
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code2.NATIONAL_CONSTRUCTION_REFRESH_REQ_VALUE)
	private boolean onNatioBuildingRefreshQuest(HawkProtocol protocol) {
		NationConstruction construction = (NationConstruction) NationService.getInstance().getNationBuildingByType(NationbuildingType.NATION_BUILDING_CENTER);
		if(construction == null || construction.getEntity().getLevel() < 1){
			return false;
		}
		// 获取当前次数
		int currentCount = player.getData().getNationalBuildQuestEntity().getRefreshCount();
		if(currentCount < 0 || currentCount > NationConstCfg.getInstance().getRefreshTimesLimit()){
			player.sendError(HP.code2.NATIONAL_CONSTRUCTION_REFRESH_REQ_VALUE, Status.Error.REFRESH_TIMES_NOT_ENOUGH, 0);
			return true;
		}
		List<Integer> canRefreshBuilding = player.getData().getNationalBuildQuestEntity().getCanRefreshBuilding();
		if(canRefreshBuilding.isEmpty()){
			player.sendError(HP.code2.NATIONAL_CONSTRUCTION_REFRESH_REQ_VALUE, Status.Error.NO_BUILDING_TO_REFRESH, 0);
			return true;
		}
		
		if(currentCount > 0){
			ItemInfo refreshItem = NationConstCfg.getInstance().getRefreshCostItem();
			// 检查消耗
			ConsumeItems consume = ConsumeItems.valueOf();
			consume.addConsumeInfo(refreshItem, false);
			
			int errorCode = consume.checkConsumeAndGetResult(player);
			if(errorCode > 0){
				player.sendError(HP.code2.NATIONAL_CONSTRUCTION_REFRESH_REQ_VALUE, errorCode, 0);
				return true;
			}
			// 消耗资源
			consume.consumeAndPush(player, Action.NATIONAL_CONSTRUCTION_REFRESH_COST);
			
			LogUtil.logNationbuildQuestRefresh(player, refreshItem.getCount());
		}
		// 执行刷新并通知前端
		construction.refreshQuest(player, canRefreshBuilding);
		return true;
	}
	
	
	/**
	 * 国家建筑升级
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code2.NATIONAL_BUILD_UPGRADE_REQ_VALUE)
	private boolean onNatioBuildingUpgrade(HawkProtocol protocol) {
		// 这里要判断一下权限，八大功臣和国王
		if(!PresidentFightService.getInstance().isPresidentPlayer(player.getId()) 
				&& !PresidentOfficier.getInstance().isMeritoriousOfficials(player.getId())){
			player.sendError(HP.code2.NATIONAL_BUILD_UPGRADE_REQ_VALUE, Status.Error.ONLY_OFFICER_TO_OPER, 0);
			return true;
		}
		// 判断升级建筑
		NationOpenBuilding req = protocol.parseProtocol(NationOpenBuilding.getDefaultInstance());
		NationbuildingType type = req.getBtype();
		
		NationalBuilding building = NationService.getInstance().getNationBuildingByType(type);
		if(building == null){
			NationService.logger.error("cant find nationbuilding type, type : {}", type);
			return false;
		}
		// 判断建筑是否已经开放
		if(!building.isOpen()){
			player.sendError(HP.code2.NATIONAL_BUILD_UPGRADE_REQ_VALUE, Status.Error.NATION_BUILDING_UNOPEN, 0);
			return true;
		}
		// 判断是否已经在建设中
		if(building.getBuildState() == NationBuildingState.BUILDING){
			player.sendError(HP.code2.NATIONAL_BUILD_UPGRADE_REQ_VALUE, Status.Error.NATION_BUILDING_ALEADY_UPGRADE, 0);
			return true;
		}
		// 检查建筑当前的升级条件
		if(!building.checkBuildCond()){
			player.sendError(HP.code2.NATIONAL_BUILD_UPGRADE_REQ_VALUE, Status.Error.CODITION_NOT_MATCH, 0);
			return true;
		}
		// 检查状态
		if(!building.checkStateCanBuild()) {
			player.sendError(HP.code2.NATIONAL_BUILD_UPGRADE_REQ_VALUE, Status.Error.NATION_BUILDING_RUNNING, 0);
			return true;
		}
		// 投递到世界线程升级
		WorldThreadScheduler.getInstance().postWorldTask(new WorldTask(GsConst.WorldTaskType.NATIONAL_BUILDING_UPGRADE) {
			@Override
			public boolean onInvoke() {
				// 开始升级
				building.startlevelup();
				return true;
			}
		});
		return true;
	}
}
