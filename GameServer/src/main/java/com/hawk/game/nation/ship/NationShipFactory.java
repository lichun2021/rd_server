package com.hawk.game.nation.ship;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hawk.db.HawkDBManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.game.GsConfig;
import com.hawk.game.config.NationConstCfg;
import com.hawk.game.config.NationShipFactoryCfg;
import com.hawk.game.entity.NationConstructionEntity;
import com.hawk.game.entity.NationShipComponentEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.nation.NationService;
import com.hawk.game.nation.NationalBuilding;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.National.NationBuildingState;
import com.hawk.game.protocol.National.NationRedDot;
import com.hawk.game.protocol.National.NationShipFactoryInfo;
import com.hawk.game.protocol.National.NationalWarehouseResItemPB;
import com.hawk.game.protocol.National.NationbuildingType;
import com.hawk.game.protocol.National.ShipComponentInfo;
import com.hawk.game.protocol.National.ShipComponents;
import com.hawk.game.protocol.Status;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.util.LogUtil;
import com.hawk.gamelog.GameLog;
import com.hawk.gamelog.LogParam;
import com.hawk.log.LogConst.LogInfoType;

/**
 * 国家飞船制造厂
 * @author zhenyu.shang
 * @since 2022年4月12日
 */
public class NationShipFactory extends NationalBuilding {
	
	public static final Logger logger = LoggerFactory.getLogger("Server");
	
	/**
	 * 所有组件数据
	 */
	private Map<Integer, NationShipComponentEntity> allComponent = new HashMap<Integer, NationShipComponentEntity>();
	
	/**
	 * 今日助力总时间
	 */
	private int dayAssistTotalTime;
	
	/**
	 * 当前正在升级的组件（因为同一时间只能有一个升级，所有此处单抽出来）
	 */
	private NationShipComponentEntity currentUpEntity;

	public NationShipFactory(NationConstructionEntity entity, NationbuildingType buildType) {
		super(entity, buildType);
	}

	@Override
	public boolean init() {
		// 加载所有组件数据
		List<NationShipComponentEntity> list = HawkDBManager.getInstance().query("from NationShipComponentEntity where invalid = 0");
		// 首次没有建筑entity
		if(list == null || list.isEmpty()){
			for (ShipComponents comp : ShipComponents.values()) {
				NationShipComponentEntity compEntity = createNewBuildEntity(comp);
				list.add(compEntity);
			}
		}
		// 存入全局
		for (NationShipComponentEntity nationShipComponentEntity : list) {
			if(nationShipComponentEntity.getUpEndTime() > 0){
				this.currentUpEntity = nationShipComponentEntity;
			}
			allComponent.put(nationShipComponentEntity.getComponentId(), nationShipComponentEntity);
		}
		// 读取每日上限
		Integer limit = LocalRedis.getInstance().getNationalDataByKey("dayAssistTotalTime", Integer.class);
		if(limit != null){
			this.dayAssistTotalTime = limit;
		}
		
		return true;
	}
	
	
	@Override
	public void buildingTick(long now) {
		if(currentUpEntity != null && now > currentUpEntity.getUpEndTime()) {
			int currLvl = this.currentUpEntity.getLevel();
			this.currentUpEntity.setLevel(currLvl + 1);
			this.currentUpEntity.setUpEndTime(0);
			// 设置建筑状态为常态
			this.setBuildState(NationBuildingState.IDLE);
			
			LogUtil.logNationShipUpgradeOver(this.currentUpEntity.getComponentId(), this.currentUpEntity.getLevel(), getEntity().getLevel());
			// 升级完成
			this.currentUpEntity = null;
			// 广播状态变化
			this.boardcastBuildState();
			
			// 通知其他玩家取消红点
			for (Player player : GlobalData.getInstance().getOnlinePlayers()) {
				player.rmNationRDAndNotify(NationRedDot.SHIP_IDLE);
			}
		}
	}
	
	private NationShipComponentEntity createNewBuildEntity(ShipComponents comp) {
		NationShipComponentEntity componentEntity = new NationShipComponentEntity();
		componentEntity.setComponentId(comp.getNumber());
		componentEntity.setLevel(0);
		componentEntity.setUpdateTime(0L);
		
		if (!HawkDBManager.getInstance().create(componentEntity)) {
			throw new RuntimeException("create ship factory component error, type=" + comp);
		}
		return componentEntity;
	}

	@Override
	public void levelupOver() {
	}

	@Override
	public void levelupStart() {
	}

	public NationShipComponentEntity getCurrentUpEntity() {
		return currentUpEntity;
	}

	@Override
	public boolean checkStateCanBuild() {
		return this.getBuildState() == NationBuildingState.IDLE || this.getBuildState() == NationBuildingState.INCOMPLETE;
	}
	
	/**
	 * 强化飞船部件 （必须世界线程调用）
	 * @param components
	 */
	public void upShipPart(ShipComponents components, Player player){
		// 判断建筑是否在升级中
		if(getBuildState() == NationBuildingState.BUILDING){
			return;
		}
		// 再次判断一下当前是否有队列
		if(getCurrentUpEntity() != null) {
			player.sendError(HP.code2.NATIONAL_SHIPFACTORY_UP_PART_REQ_VALUE, Status.Error.NATION_SHIP_ALEADY_UPGRADE, 0);
			return;
		}
		// 获取当前部件
		NationShipComponentEntity compEntity = allComponent.get(components.getNumber());
		if(compEntity == null){
			logger.error("ship component type error, type : {}", components);
			return;
		}
		NationShipFactoryCfg cfg = compEntity.getNextCfg();
		// 判断工厂等级
		if(this.getEntity().getLevel() < cfg.getFactoryLevel()){
			player.sendError(HP.code2.NATIONAL_SHIPFACTORY_UP_PART_REQ_VALUE, Status.Error.NATION_SHIP_LEVEL_NOT_ENOUGH, 0);
			return;
		}
		// 判断强化前置条件
		List<Integer> cond = cfg.getPrevCondition();
		for (Integer needId : cond) {
			// 拆解
			int np = needId / 100;
			int lvl = needId % 100;
			// 获取对应部件
			NationShipComponentEntity nce = allComponent.get(np);
			if(nce == null){
				logger.error("can not find ship component type, cond : {}", needId);
				return;
			}
			if(nce.getLevel() < lvl){
				player.sendError(HP.code2.NATIONAL_SHIPFACTORY_UP_PART_REQ_VALUE, Status.Error.CODITION_NOT_MATCH, 0);
				return;
			}
		}
		String serverId = player.getMainServerId();
		// 判断并消耗
		if(!NationService.getInstance().nationalWarehouseResourceConsume(cfg.getConsumeResList(), serverId)){
			player.sendError(HP.code2.NATIONAL_SHIPFACTORY_UP_PART_REQ_VALUE, Status.Error.NATION_RESOURCE_NOT_ENOUGH, 0);
			return;
		}
		// 设置
		this.currentUpEntity = compEntity;
		this.currentUpEntity.setUpEndTime(HawkTime.getMillisecond() + cfg.getContinueTime() * 1000L);
		
		// 设置建筑状态为running
		this.setBuildState(NationBuildingState.RUNNING);
		// 通知玩家部件变化
		this.sendInfoToClient(player);
		// 广播状态变化
		this.boardcastBuildState();
		
		String[] tlogStr = logConsumeString(cfg.getConsumeResList(), serverId);
		LogUtil.logNationShipUpgradeStart(player, compEntity.getComponentId(), compEntity.getLevel(), tlogStr[0], tlogStr[1], getEntity().getLevel());
		// tlog记录：消耗飞船强化材料时，记录服务器id、时间、对应部件id及等级、消耗各材料id及数量、消耗后材料剩余数量
		for (ItemInfo item : cfg.getConsumeResList()) {
			try {
				LogParam logParam = LogUtil.getNonPersonalLogParam(LogInfoType.nation_shippart_consume);
				logParam.put("componentId", compEntity.getComponentId())
				.put("level", compEntity.getLevel())
				.put("itemId", item.getItemId())
				.put("count", item.getCount())
				.put("remainCount", NationService.getInstance().getNationalWarehouseResourse(item.getItemId(), serverId));
				GameLog.getInstance().info(logParam);
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
	}
	
	private String[] logConsumeString(List<ItemInfo> consume, String serverId) {
		String[] res = new String[] {"", ""};
		int i = 0;
		StringBuffer clog = new StringBuffer();
		for (ItemInfo iteminfo : consume) {
			if(i != 0) {
				clog.append(",");
			}
			clog.append(iteminfo.toString());
			i++;
		}
		res[0] = clog.toString();
		
		i = 0;
		Map<Integer, Long> all = NationService.getInstance().getNationalWarehouseResourse(serverId);
		StringBuffer resLog = new StringBuffer();
		for (Entry<Integer, Long> entry : all.entrySet()) {
			if(i != 0) {
				resLog.append(",");
			}
			resLog.append(entry.getKey()).append("_").append(entry.getValue());
		}
		res[1] = resLog.toString();
		return res;
	}
	
	/**
	 * 取消部件升级 (世界线程调用)
	 * @param components
	 * @param player
	 */
	public void cancelUpShipPart(Player player){
		NationShipComponentEntity compEntity = getCurrentUpEntity();
		// 再次判断是否有正在升级中的部件
		if(compEntity == null) {
			if(player != null) {
				player.sendError(HP.code2.NATIONAL_SHIPFACTORY_CANCEL_PART_REQ_VALUE, Status.Error.NATION_SHIP_NOT_UPGRADE, 0);
			}
			return;
		}
		// 返还国家仓库资源
		NationShipFactoryCfg cfg = compEntity.getNextCfg();
		List<ItemInfo> consumeList = cfg.getConsumeResList();
		int returnRate = NationConstCfg.getInstance().getCancelReturn();
		// 返还资源 (如果是合服前停服返回，则返回给对应服id)
		String serverId = GsConfig.getInstance().getServerId();
		if(player != null) {
			serverId = player.getMainServerId();
		}
		
		for (ItemInfo itemInfo : consumeList) {
			long count = (long) (itemInfo.getCount() * (returnRate / 10000.0));
			NationService.getInstance().nationalWarehouseResourceIncrease(itemInfo.getItemId(), count, serverId);
		}
		
		compEntity.setUpEndTime(0);
		// 取消升级中得部件
		this.currentUpEntity = null;
		// 设置建筑状态为常态
		this.setBuildState(NationBuildingState.IDLE);
		
		if(player != null) {
			// 通知玩家部件变化
			this.sendInfoToClient(player);
			// 广播状态变化
			this.boardcastBuildState();
			String[] logRes = logConsumeString(consumeList, serverId);
			LogUtil.logNationShipUpgradeCancel(player, compEntity.getComponentId(), compEntity.getLevel(), logRes[0], logRes[1]);
		}
	}
	
	/**
	 * 强化助力
	 * @param player
	 */
	public void assistTime(Player player){
		NationShipComponentEntity compEntity = getCurrentUpEntity();
		// 再次判断是否有正在升级中的部件
		if(compEntity == null) {
			player.sendError(HP.code2.NATIONAL_SHIPFACTORY_ASSIST_REQ_VALUE, Status.Error.NATION_SHIP_NOT_UPGRADE, 0);
			return;
		}
		int assisTime = NationConstCfg.getInstance().getAssistTime();
		int assisLimit = NationConstCfg.getInstance().getAssistLimit();
		
		boolean notify = true;
		long beforeTime = compEntity.getUpEndTime();
		// 判断当前助力是否已经到达上限
		if(getDayAssistTotalTime() < assisLimit){
			int cha = assisLimit - getDayAssistTotalTime();
			if(cha > assisTime){
				cha = assisTime;
			}
			this.setDayAssistTotalTime(getDayAssistTotalTime() + cha);
			
			long now = HawkTime.getMillisecond();
			long resTime = compEntity.getUpEndTime() - cha * 1000L;
			
			if(resTime < now){
				resTime = now;
			}
			compEntity.setUpEndTime(resTime);
			// 助力成功不用提示了
			notify = false;
			// 更新世界点信息
			this.boardcastBuildState();
		}
		// 通知玩家
		this.sendInfoToClient(player);
		// 没有助力上需要通知
		if(notify){
			player.sendError(HP.code2.NATIONAL_SHIPFACTORY_ASSIST_REQ_VALUE, Status.Error.NATION_SHIP_ASSIST_LIMIT, 0);
		}
		// 给奖励
		AwardItems awards = AwardItems.valueOf(NationConstCfg.getInstance().getAssistAward());
		//awards.rewardTakeAffectAndPush(player, Action.NATIONAL_SHIP_ASSIST_AWARD);
		
		SystemMailService.getInstance().sendMail(MailParames.newBuilder()
				.setPlayerId(player.getId())
				.setMailId(MailId.NATION_SHIP_HELP_REWARD)
				.setRewards(awards.getAwardItems())
				.setAwardStatus(MailRewardStatus.NOT_GET)
				.build());
		
		LogUtil.logNationShipUpgradeAssist(player, compEntity.getComponentId(), compEntity.getLevel(), beforeTime, compEntity.getUpEndTime());
	}
	
	@Override
	public long getRunningEndTime() {
		long endTime = 0L;
		if(this.currentUpEntity != null){
			// 这里存在多线程调用currentUpEntity 为空的情况。所以加个异常捕获，但是不处理
			try {
				endTime = this.currentUpEntity.getUpEndTime();
			} catch (Exception e) {
			}
			
		}
		return endTime;
	}
	
	@Override
	public long getRunningTotalTime() {
		long totalTime = 0L;
		if(this.currentUpEntity != null){
			// 这里存在多线程调用currentUpEntity 为空的情况。所以加个异常捕获，但是不处理
			try {
				totalTime = currentUpEntity.getNextCfg().getContinueTime() * 1000L;
			} catch (Exception e) {
			}
		}
		return totalTime;
	}

	/**
	 * 给前端推送当前飞船制造厂
	 * @param player
	 */
	public void sendInfoToClient(Player player){
		NationShipFactoryInfo.Builder builder = NationShipFactoryInfo.newBuilder();
		
		builder.setBuildLvl(getEntity().getLevel());
		builder.setAssistCount(player.getData().getDailyDataEntity().getNationShipAssist());
		builder.setAssistLimit(getDayAssistTotalTime());
		
		String serverId = player.getMainServerId();
		 Map<Integer, Long> map = NationService.getInstance().getNationalWarehouseResourse(serverId);
		 for (Entry<Integer, Long> entry : map.entrySet()) {
			 if (entry.getKey() == PlayerAttr.DIAMOND_VALUE) {
				 continue;
			 }
			 NationalWarehouseResItemPB.Builder resItem = NationalWarehouseResItemPB.newBuilder();
			 resItem.setResourceId(entry.getKey());
			 resItem.setCount(entry.getValue());
			 builder.addResource(resItem);
		 }
		
		for (NationShipComponentEntity compEntity : allComponent.values()) {
			ShipComponentInfo.Builder builder2 = ShipComponentInfo.newBuilder();
			builder2.setLvl(compEntity.getLevel());
			
			ShipComponents cp = compEntity.getComponent();
			
			builder2.setType(cp);
			// 找出正在进行中的
			if(compEntity.getUpEndTime() > 0){
				builder.setUpEndTime(compEntity.getUpEndTime());
				builder.setUpPart(cp);
			}
			
			builder.addParts(builder2);
		}
		
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.NATIONAL_SHIPFACTORY_INFO_S_VALUE, builder));
	}

	public int getDayAssistTotalTime() {
		return dayAssistTotalTime;
	}

	public void setDayAssistTotalTime(int dayAssistTotalTime) {
		this.dayAssistTotalTime = dayAssistTotalTime;
		LocalRedis.getInstance().updateNationalDataByKey("dayAssistTotalTime", dayAssistTotalTime);
	}
	
	@Override
	public String runningStateParam() {
		try {
			if(currentUpEntity == null){
				return "";
			}
			return String.valueOf(this.currentUpEntity.getComponentId());
		} catch (Exception e) {
		}
		return "";
	}
}
