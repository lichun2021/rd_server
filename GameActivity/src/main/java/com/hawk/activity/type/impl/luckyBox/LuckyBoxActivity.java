package com.hawk.activity.type.impl.luckyBox;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.hawk.config.HawkConfigManager;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.uuid.HawkUUIDGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.exchangeTip.AExchangeTipConfig;
import com.hawk.activity.type.impl.exchangeTip.IExchangeTip;
import com.hawk.activity.type.impl.luckyBox.cfg.LuckyBoxExchangeCfg;
import com.hawk.activity.type.impl.luckyBox.cfg.LuckyBoxKVCfg;
import com.hawk.activity.type.impl.luckyBox.cfg.LuckyBoxRewardCfg;
import com.hawk.activity.type.impl.luckyBox.cfg.LuckyBoxTimeCfg;
import com.hawk.activity.type.impl.luckyBox.cfg.LuckyBoxTurntableRewardCfg;
import com.hawk.activity.type.impl.luckyBox.entity.LuckyBoxEntity;
import com.hawk.game.protocol.Activity.LuckBoxInfoResp;
import com.hawk.game.protocol.Activity.LuckBoxItemExchange;
import com.hawk.game.protocol.Activity.LuckBoxRadnomRewardResp;
import com.hawk.game.protocol.Activity.LuckBoxReward;
import com.hawk.game.protocol.Const.ItemType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Status;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;
/**
 * 幸运转盘
 * @author che
 *
 */
public class LuckyBoxActivity extends ActivityBase implements IExchangeTip<AExchangeTipConfig> {
	/**
	 * 日志对象
	 */
	private static final Logger logger = LoggerFactory.getLogger("Server");
	/**
	 * 构造
	 * @param activityId
	 * @param activityEntity
	 */
	public LuckyBoxActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	/**
	 * 活动类型
	 * @return
	 */
	@Override
	public ActivityType getActivityType() {
		return ActivityType.LUCKY_BOX_ACTIVITY;
	}

	/**
	 * 实例化
	 * @param config
	 * @param activityEntity
	 * @return
	 */
	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		LuckyBoxActivity activity = new LuckyBoxActivity(
				config.getActivityId(), activityEntity);
		return activity;
	}

	/**
	 * 从DB加载数据
	 * @param playerId
	 * @param termId
	 * @return
	 */
	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<LuckyBoxEntity> queryList = HawkDBManager.getInstance()
				.query("from LuckyBoxEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			LuckyBoxEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	/**
	 * 创建数据库数据对象
	 * @param playerId
	 * @param termId
	 * @return
	 */
	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		LuckyBoxEntity entity = new LuckyBoxEntity(playerId, termId);
		return entity;
	}

	/**
	 * 活动开启
	 */
	@Override
	public void onOpen() {
		//取所有在线玩家
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		//所有在线玩家的循环
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, MsgId.LUCKY_BOX_INIT, () -> {
				//初始化玩家数据，兑换和奖励数值表格初始化到玩家数据库
				initData(playerId);
				//给客户端同步玩家幸运转盘数据
				syncActivityDataInfo(playerId);
			});
		}
	}

	/**
	 * 当前活动数据同步给客户端
	 * @param playerId
	 */
	@Override
	public void syncActivityDataInfo(String playerId) {
		//取玩家幸运转盘数据库对象
		Optional<LuckyBoxEntity> opDataEntity = getPlayerDataEntity(playerId);
		//如果数据库对象不为空
		if (opDataEntity.isPresent()) {
			//当前活动数据同步给客户端
			syncActivityInfo(playerId, opDataEntity.get());
		}
	}

	/**
	 * 玩家登陆时的回调
	 * 1 如果活动已经结束扣除玩家活动道具
	 * @param playerId
	 */
	@Override
	public void onPlayerLogin(String playerId) {
		//回收道具，活动结束后，需要扣除玩家身上的所有活动道具，并发送通知邮件给玩家
		this.recoverItem(playerId);
		//初始化数据
		this.initData(playerId);
	}
	
	/**
	 * 初始化数据
	 * @param playerId
	 */
	public void initData(String playerId){
		//获取玩家数据库数据对象
		Optional<LuckyBoxEntity> opDataEntity = getPlayerDataEntity(playerId);
		//没找到数据对象不继续处理
		if (!opDataEntity.isPresent()) {
			return;
		}
		//取数据实体
		LuckyBoxEntity entity = opDataEntity.get();
		//数据实体中有数据，说明已经初始化过的玩家，不继续处理
		if(entity.getCellCount() > 0){
			return;
		}
		//取奖励策划表格
		List<LuckyBoxTurntableRewardCfg> rewards = HawkConfigManager.getInstance().
				getConfigIterator(LuckyBoxTurntableRewardCfg.class).toList();

		int cellId = 1;
		//遍历奖励表
		for(LuckyBoxTurntableRewardCfg reward : rewards){
			//把所有奖励的权重、数量、是否可选初始化到奖励数据对象\
			int initRewardId = 0;
			if(reward.getCanSelected() <= 0){
				initRewardId = reward.getRewardIdList().get(0);
			}
			LuckyBoxCell cell = new LuckyBoxCell(cellId, initRewardId,
					reward.getNumber(), reward.getWeight(), reward.getCanSelected());
			//把奖励数据对象保存在数据库对象里
			entity.addLuckyBoxCell(cell);
			cellId ++; 
		}
		//兑换策划表数据
		List<LuckyBoxExchangeCfg> exchanges = HawkConfigManager.getInstance().getConfigIterator(LuckyBoxExchangeCfg.class).toList();
		List<Integer> tips = new ArrayList<>();
		//把兑换表格数据插入到list
		for(LuckyBoxExchangeCfg exchange : exchanges){
			tips.add(exchange.getId());
		}
		//把兑换数据加入到玩家数据库
		entity.addTipList(tips);
	}

	/**
	 * 活动结束后扣除玩家身上剩余的活动道具，并根据扣除数量兑换为指定的物品
	 * @param playerId
	 */
	public void recoverItem(String playerId){
		//取当前活动期数
		int termId = this.getActivityTermId();
		if(termId > 0){
			return;
		}
		//取幸运转盘活动基础表格
		LuckyBoxKVCfg config = HawkConfigManager.getInstance().getKVInstance(LuckyBoxKVCfg.class);
		if (config == null) {
			return;
		}
		//活动结束时回收道具ID
		int recoverItem = config.getRecoverItem();
		if(recoverItem <= 0){
			return;
		}
		//取玩家身上此道具的数量
		int count = this.getDataGeter().getItemNum(playerId, recoverItem);
		if(count <= 0){
			return;
		}
		//扣除道具的数据准备
		List<RewardItem.Builder> costList = new ArrayList<>();
		RewardItem.Builder costBuilder = RewardItem.newBuilder();
		//类型为道具
		costBuilder.setItemType(ItemType.TOOL_VALUE);
		//待扣除物品ID
		costBuilder.setItemId(recoverItem);
		//待扣除的物品数量
		costBuilder.setItemCount(count);
		//把待扣除的物品数据加入参数容器
		costList.add(costBuilder);
		//注意这里先扣除源道具，如果失败，不给兑换后的道具
		boolean cost = this.getDataGeter().cost(playerId,costList, 1, Action.LUCKY_BOX_RECOVER_COST, true);
		//扣除失败不继续处理
		if (!cost) {
			return;
		}
		//道具扣除成功兑换物品给玩家，给玩家发通知邮件
		//先取扣除时兑换的策划表
		List<RewardItem.Builder> rewardList = config.getRecoverSendItemList();
		//兑换道具表格数据的循环
		for(RewardItem.Builder reward : rewardList){
			//兑换表格里的数量 X 扣除的源物品的数量
			long num = reward.getItemCount() * count;
			reward.setItemCount(num);
		}
		Object[] content =  new Object[]{count};
		//通过邮件给玩家发放物品奖励
		this.getDataGeter().sendMail(playerId, MailId.LUCKY_BOX_RECOVER, null, null, content,
				rewardList, false);
		logger.info("LuckyBoxActivity,recoverItem,playerId: "+ "{},count:{}", playerId,count);
	}
	
	/**
	 * 更换奖励
	 * LUCK_BOX_REWARD_SELECT_REQ_VALUE 协议调用
	 * @param playerId
	 * @param cellId
	 * @param rewardId
	 * @param protocolType
	 */
	public void selectReward(String playerId,int cellId,int rewardId,int protocolType){
		LuckyBoxKVCfg config = HawkConfigManager.getInstance().getKVInstance(LuckyBoxKVCfg.class);
		//读表出错不处理
		if (config == null) {
			return;
		}
		//玩家数据库对象没初始化不处理
		Optional<LuckyBoxEntity> opDataEntity = getPlayerDataEntity(playerId);
		if (!opDataEntity.isPresent()) {
			return;
		}
		//取玩家数据对象
		LuckyBoxEntity entity = opDataEntity.get();
		//不可更换物品，返回错误码给客户端
		if(this.luckyBoxRewardOver(entity.getCellMap()) > 0){
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, protocolType,
					Status.Error.LUCKY_BOX_FINISH_LIMIT_VALUE);
			return;
		}
		//取转盘格子对象
		LuckyBoxCell cell = entity.getLuckyBoxCell(cellId);
		if(cell == null){
			return;
		}
		//格子是否允许选择物品
		if(cell.getCanSelect() <= 0){
			return;
		}
		//格子里物品数量必须大于0
		if(cell.getCount() <= 0){
			return;
		}

		LuckyBoxTurntableRewardCfg turnCfg = HawkConfigManager.getInstance().getConfigByKey(
				LuckyBoxTurntableRewardCfg.class,cell.getCellId());

		if(null == turnCfg){
			return;
		}

		if( !turnCfg.getRewardIdList().contains(rewardId) ){
			return;
		}



		//从策划表格数值，检查想要更换的目标物品是否是允许更换的
		LuckyBoxRewardCfg rcfg = HawkConfigManager.getInstance().getConfigByKey(
				LuckyBoxRewardCfg.class,rewardId);

		if(null == rcfg){
			return;
		}
		//把转盘格子的当前物品修改为目标物品
		cell.setRewardId(rcfg.getId());
		//给客户端返回同步消息
		syncActivityDataInfo(playerId);
	}
	
	/**
	 * 摇奖，开始转盘
	 * LUCK_BOX_RANDOM_REQ_VALUE 协议调用
	 * @param playerId 玩家ID
	 * @param times	连续自动开奖次数
	 * @param protocolType
	 */
	public void luckyBoxRandom(String playerId,int times,int protocolType){
		//取表格
		LuckyBoxKVCfg config = HawkConfigManager.getInstance().getKVInstance(LuckyBoxKVCfg.class);
		if (config == null) {
			return;
		}
		//取玩家数据
		Optional<LuckyBoxEntity> opDataEntity = getPlayerDataEntity(playerId);
		if (!opDataEntity.isPresent()) {
			return;
		}
		//取玩家数据对象
		LuckyBoxEntity entity = opDataEntity.get();
		//玩家是否可刷新道具，如果道具可选，并且数量大于0,返回true,否则false
		if(this.luckyBoxRewardOver(entity.getCellMap()) > 0){
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, protocolType,
					Status.Error.LUCKY_BOX_FINISH_LIMIT_VALUE);
			return;
		}
		//刷新次数超过表格配置允许值，不处理
		if(times > config.getRotationtimes()){
			return;
		}
		//随机奖励，先把玩家当前格子数据复制一份
		Map<Integer,LuckyBoxCell> cells = new HashMap<>();
		for(LuckyBoxCell cell : entity.getCellMap().values()){
			//有任何限定奖励没有选择，不允许开奖
			if(cell.getCanSelect() > 0 && cell.getRewardId() <= 0){
				return;
			}
			LuckyBoxCell cellCopy = cell.copy();
			cells.put(cellCopy.getCellId(), cellCopy);
		}
		//玩家必中数据复制一份
		Set<Integer> mustSet = new HashSet<>();
		mustSet.addAll(entity.getMustList());

		//取玩家随机次数
		int randomCount = entity.getRandomCount();
		//初始化一个奖励容器
		List<LuckBoxReward.Builder> rewardList = new ArrayList<>();
		//自动开奖次数的循环
		for(int i=1;i <= times;i++ ){
			randomCount += 1;
			LuckBoxReward.Builder reward = this.randomReward(cells, randomCount, mustSet);
			rewardList.add(reward);
			if(reward.getBoxOver() > 0){
				break;
			}
		}
		//消耗
		List<RewardItem.Builder> makeCost = config.getOnceCostItemList();
		int costMul = rewardList.size();
		boolean cost = this.getDataGeter().cost(playerId,makeCost, costMul, Action.LUCKY_BOX_RANDOM_COST, true);
		if (!cost) {
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, 
					protocolType, Status.Error.ITEM_NOT_ENOUGH_VALUE);
			return;
		}
		//合并结果
		this.mergeCells(entity, cells, mustSet, costMul);
		//发奖励
		List<RewardItem.Builder> awardTotal = this.calTotalRandomReward(rewardList);
		this.getDataGeter().takeReward(playerId, awardTotal, 
				1, Action.LUCKY_BOX_RANDOM_GAIN, false);
		//发送结果
		LuckBoxRadnomRewardResp.Builder builder = LuckBoxRadnomRewardResp.newBuilder();
		for(LuckBoxReward.Builder boxReward : rewardList){
			builder.addRewards(boxReward);
		}
		PlayerPushHelper.getInstance().pushToPlayer(playerId,
				HawkProtocol.valueOf(HP.code2.LUCK_BOX_RANDOM_RESP, builder));
		this.addTlog(playerId, entity.getTermId(), randomCount, rewardList, cells);
	}
	
	/**
	 * Tlog日志
	 * @param playerId
	 * @param termId
	 * @param count
	 * @param rewardList
	 * @param cells
	 */
	public void addTlog(String playerId,int termId,int count,List<LuckBoxReward.Builder> rewardList,Map<Integer,LuckyBoxCell> cells){
		String groupId = HawkUUIDGenerator.genUUID();
		for(LuckBoxReward.Builder builder : rewardList){
			count ++;
			LuckyBoxCell cell = cells.get(builder.getCellId());
			if(cell == null){
				continue;
			}
			this.getDataGeter().logLuckyBoxRandom(playerId, termId, groupId,count, cell.getCellId(), 
					cell.getRewardId(), cell.getCanSelect(), builder.getBoxOver());
		}
	}
	
	/**
	 * 获取所有奖励物品
	 * @param rewardList
	 * @return
	 */
	public List<RewardItem.Builder> calTotalRandomReward(List<LuckBoxReward.Builder> rewardList){
		List<RewardItem.Builder> allReward =new ArrayList<>();
		for(LuckBoxReward.Builder boxReward : rewardList){
			if(boxReward.getCellRewardsCount() > 0){
				for(RewardItem.Builder reward : boxReward.getCellRewardsBuilderList()){
					allReward.add(reward.clone());
				}
			}
			if(boxReward.getOverRewardsCount() > 0){
				for(RewardItem.Builder reward : boxReward.getOverRewardsBuilderList()){
					allReward.add(reward.clone());
				}
			}
		}
		return allReward;
	}
	
	
	/**
	 * 合并数据
	 * @param entity
	 * @param cells
	 * @param mustSet
	 * @param randomCount
	 */
	public void mergeCells(LuckyBoxEntity entity,Map<Integer,LuckyBoxCell> cells,Set<Integer> mustSet,int randomCount){
		Map<Integer,LuckyBoxCell> entityCells = entity.getCellMap();
		for(LuckyBoxCell cell : cells.values()){
			LuckyBoxCell entityCell = entityCells.get(cell.getCellId());
			entityCell.setCount(cell.getCount());
			entityCell.setWeight(cell.getWeight());
		}
		entity.addMust(mustSet);
		int rcount = entity.getRandomCount() + randomCount;
		entity.setRandomCount(rcount);
		entity.notifyUpdate();	
	}
	
	/**
	 * 随机奖励
	 *
	 * @param cells 转盘格子
	 * @param randomCount 已经随机的次数（处理必中逻辑时需要），就是说随机多少次以后会必中的规则
	 * @param mustSet
	 * @return
	 */
	public LuckBoxReward.Builder randomReward(Map<Integer,LuckyBoxCell> cells,int randomCount,Set<Integer> mustSet){
		boolean must = false;
		//取表
		LuckyBoxKVCfg config = HawkConfigManager.getInstance().getKVInstance(LuckyBoxKVCfg.class);
		//策划表必中项列表的循环
		for(int mustTime : config.getMustList()){
			//如果已经随机的次数大于等于策划表的当前值并且玩家必中记录中没有此必中项目，则本次抽奖为必中
			//randomCount数值是玩家数据库中的值
			//mustSet是玩家的数据库项
			if(randomCount >= mustTime && !mustSet.contains(mustTime)){
				//必中
				must = true;
				//把当前必中的随机次数值保存在玩家必中数据库项里
				mustSet.add(mustTime);
				break;
			}
		}
		List<LuckyBoxCell> cellList = new ArrayList<>();
		//如果是必中，找到所有限定奖励，并加入待抽奖奖池
		//如果不是必中，就把所有奖品加入待抽奖奖池
		for(LuckyBoxCell cell : cells.values()){
			if(must){
				if(cell.getCount() > 0 && cell.getCanSelect() > 0){
					cellList.add(cell);
				}
			}else{
				if(cell.getCount() > 0){
					cellList.add(cell);
				}
			}
		}
		
		LuckyBoxCell awardCell = HawkRand.randomWeightObject(cellList);
		awardCell.consumeCount();
		//当前格子奖品都抽走了，空格子，分摊权重给其它有奖品的格子
		if(awardCell.getCount() <= 0){
			this.sendWeight(awardCell, cells);
		}

		//凭手气抽中限定奖励，要算一次必中
		if(!must && awardCell.getCanSelect() > 0){
			consumeNextMustTime(mustSet, config, awardCell);
		}

		//发放剩余奖励
		int rewardOver = this.luckyBoxRewardOver(cells);
		List<RewardItem.Builder> rewards = awardCell.getReward();
		List<RewardItem.Builder> overRewards =new ArrayList<>();
		if(rewardOver > 0){
			List<RewardItem.Builder> allOver = this.getAllOverReward(cells);
			overRewards.addAll(allOver);
		}
		LuckBoxReward.Builder builder = LuckBoxReward.newBuilder();
		builder.setCellId(awardCell.getCellId());
		builder.setBoxOver(rewardOver);
		
		if(rewards.size() > 0){
			for(RewardItem.Builder rbuilder : rewards){
				builder.addCellRewards(rbuilder);
			}
		}
		if(overRewards.size() > 0){
			for(RewardItem.Builder rbuilder : overRewards){
				builder.addOverRewards(rbuilder);
			}
		}
		for(LuckyBoxCell cell : cells.values()){
			if(cell.getCount() <= 0){
				continue;
			}
			builder.addCells(cell.getCellBuilder());
		}
		builder.setRandomCount(randomCount);
		return builder;
	}

	private void consumeNextMustTime(Set<Integer> mustSet, LuckyBoxKVCfg config, LuckyBoxCell cell) {
		for(int mustTime : config.getMustList()){
			if(!mustSet.contains(mustTime)){
				//把当前必中的随机次数值保存在玩家必中数据库项里
				mustSet.add(mustTime);
				break;
			}
		}
	}

	/**
	 * 分发权重
	 * @param awardCell
	 * @param cells
	 */
	public void sendWeight(LuckyBoxCell awardCell,Map<Integer,LuckyBoxCell> cells){
		//抽中的格子的权重
		int weigth = awardCell.getWeight();
		int allWeight = 0;
		List<LuckyBoxCell> cellList = new ArrayList<>();
		//把所有还有奖品的格子的权重累加，得到总权重
		for(LuckyBoxCell cell : cells.values()){
			if(cell.getCount() > 0){
				cellList.add(cell);
				allWeight += cell.getWeight();
			}
		}
		//防止异常
		if(allWeight <= 0){
			return;
		}

		//待分配的权重
		int rewardWeight =  weigth / allWeight;

		//按每个格子里奖励的权重来分配待分配的权重
		int allWeightTemp = allWeight;
		for(LuckyBoxCell cell : cellList){
			int getWeight = (int) Math.ceil(cell.getWeight() * rewardWeight);
			if(allWeightTemp < getWeight){
				getWeight = allWeightTemp;
			}
			allWeightTemp -= getWeight;
			int cellWeigth = cell.getWeight() + getWeight;
			cell.setWeight(cellWeigth);
		}
		
	}
	
	
	/**
	 * 是否完成
	 * @param cells
	 * @return 如果道具可选，并且数量大于0,返回true,否则false
	 */
	public int luckyBoxRewardOver(Map<Integer,LuckyBoxCell> cells){
		for(LuckyBoxCell cell : cells.values()){
			if(cell.getCanSelect() > 0 && cell.getCount() > 0){
				return 0;
			}
		}
		return 1;
	}

	/**
	 * 获取转盘上剩余奖励
	 * @param cells
	 * @return
	 */
	public List<RewardItem.Builder> getAllOverReward(Map<Integer,LuckyBoxCell> cells){
		List<RewardItem.Builder> all = new ArrayList<>();
		for(LuckyBoxCell cell : cells.values()){
			if(cell.getCount() <= 0){
				continue;
			}
			List<RewardItem.Builder> rewards = cell.getReward();
			for(RewardItem.Builder builder : rewards){
				long count = builder.getItemCount() * cell.getCount();
				builder.setItemCount(count);
			}
			cell.setCount(0);
			all.addAll(rewards);
		}
		return all;
	}
	
	/**
	 * 道具购买
	 * @param playerId
	 * @param
	 */
	public void buyNeedItem(String playerId,int count,int protocolType){
		LuckyBoxKVCfg config = HawkConfigManager.getInstance().getKVInstance(LuckyBoxKVCfg.class);
		if (config == null) {
			return;
		}
		Optional<LuckyBoxEntity> opDataEntity = getPlayerDataEntity(playerId);
		if (!opDataEntity.isPresent()) {
			return;
		}

		LuckyBoxEntity entity = opDataEntity.get();

		int buyCount = entity.getBuyNeedCount();
		
		if(buyCount + count > config.getBuytimes()){
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, protocolType,
					Status.Error.LUCKY_BOX_BUY_COUNT_LIMIT_VALUE);
			logger.info("LuckyBoxActivity,buyItem,fail,countless,playerId: "
					+ "{},count:{}", playerId,count);
			return;
		}
		
		List<RewardItem.Builder> makeCost = config.getBuyItemPriceList();
		boolean cost = this.getDataGeter().cost(playerId,makeCost, count, Action.LUCKY_BOX_BUY_NEED_ITEM_COST, true);
		if (!cost) {
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, 
					protocolType, Status.Error.ITEM_NOT_ENOUGH_VALUE);
			return;
		}
		int curCount = buyCount + count;
		//增加兑换次数
		entity.setBuyNeedCount(curCount);
		//发奖励
		this.getDataGeter().takeReward(playerId, config.getGainItemList(), 
				count, Action.LUCKY_BOX_BUY_NEED_ITEM_GAIN, true);
		//同步
		this.syncActivityInfo(playerId,entity);
		logger.info("LuckyBoxActivity,buyItem,sucess,playerId: "
				+ "{},count:{}", playerId,count);
	
	}
	
	/**
	 * 道具兑换
	 * @param playerId
	 * @param
	 */
	public void itemExchange(String playerId,int exchangeId,int exchangeCount,int protocolType){
		LuckyBoxExchangeCfg config = HawkConfigManager.getInstance().getConfigByKey(LuckyBoxExchangeCfg.class, exchangeId);
		if (config == null) {
			return;
		}

		Optional<LuckyBoxEntity> opDataEntity = getPlayerDataEntity(playerId);
		if (!opDataEntity.isPresent()) {
			return;
		}

		LuckyBoxEntity entity = opDataEntity.get();
		if(!this.inShopTime() && this.luckyBoxRewardOver(entity.getCellMap()) <= 0){
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, protocolType,
					Status.Error.LUCKY_BOX_EXCHANGE_TIME_LIMIT_VALUE);
			logger.info("luckyBoxActivity,itemExchange,fail,time err ,playerId: "
					+ "{},exchangeType:{},ecount:{}", playerId,exchangeId,exchangeCount);
			return;
		}

		int eCount = entity.getExchangeCount(exchangeId);
		if(eCount + exchangeCount > config.getLimittimes()){
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, protocolType,
					Status.Error.LUCKY_BOX_EXCHANGE_COUNT_LIMIT_VALUE);
			logger.info("luckyBoxActivity,itemExchange,fail,countless,playerId: "
					+ "{},exchangeType:{},ecount:{}", playerId,exchangeId,exchangeCount);
			return;
		}
		
		List<RewardItem.Builder> makeCost = config.getNeedItemList();
		boolean cost = this.getDataGeter().cost(playerId,makeCost, exchangeCount, Action.DRESS_TREASURE_EXCAHNGE_COST, true);
		if (!cost) {
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, 
					protocolType, Status.Error.ITEM_NOT_ENOUGH_VALUE);
			return;
		}
		//增加兑换次数
		entity.addExchangeCount(exchangeId, exchangeCount);
		//发奖励
		this.getDataGeter().takeReward(playerId, config.getGainItemList(), 
				exchangeCount, Action.DRESS_TREASURE_EXCAHNGE_GAIN, true);
		//同步
		this.syncActivityInfo(playerId,entity);
		logger.info("luckyBoxActivity,itemExchange,sucess,playerId: "
				+ "{},exchangeType:{},ecount:{}", playerId,exchangeId,eCount);
		
	}
	
	/**
	 * 是否可以兑换
	 */
	public boolean inShopTime(){
		int termId = this.getActivityTermId();
		LuckyBoxTimeCfg timecfg = HawkConfigManager.getInstance().getConfigByKey(LuckyBoxTimeCfg.class, termId);
		if(timecfg.getExchangestartTimeValue() > HawkTime.getMillisecond()){
			return false;
		}
		return true;
	}
	
	/**
	 * 添加关注
	 * @param playerId
	 * @param id
	 * @param tips
	 */
	public void changeExchangeTips(String playerId, int id, int tips){
		Optional<LuckyBoxEntity> opt = getPlayerDataEntity(playerId);
		if(!opt.isPresent()){
			return;
		}
		LuckyBoxEntity entity = opt.get();
		if(tips > 0){
			entity.addTip(id);
		}else{
			entity.removeTip(id);
		}
		//同步
		this.syncActivityInfo(playerId,entity);
		logger.info("luckyBoxActivity,changeExchangeTips,sucess,playerId: "
				+ "{},id:{},tips:{}", playerId,id,tips);
	}
	
	
	/**
	 * 信息同步
	 * @param playerId
	 */
	public void syncActivityInfo(String playerId,LuckyBoxEntity entity){
		LuckBoxInfoResp.Builder builder = this.createLuckyBoxInfoBuilder(entity);
		PlayerPushHelper.getInstance().pushToPlayer(playerId,
				HawkProtocol.valueOf(HP.code2.LUCK_BOX_INFO_RESP, builder));
	}
	
	/**
	 * 创建同步信息
	 * @param entity
	 * @return
	 */
	public LuckBoxInfoResp.Builder createLuckyBoxInfoBuilder(LuckyBoxEntity entity){
		int termId = this.getActivityTermId();
		LuckyBoxTimeCfg timecfg = HawkConfigManager.getInstance().getConfigByKey(LuckyBoxTimeCfg.class, termId);
		LuckBoxInfoResp.Builder builder = LuckBoxInfoResp.newBuilder();
		builder.setBoxOver(this.luckyBoxRewardOver(entity.getCellMap()));
		builder.setShopTime(timecfg.getExchangestartTimeValue());
		for(LuckyBoxCell cells : entity.getCellMap().values()){
			if(cells.getCount() <= 0){
				continue;
			}
			builder.addCells(cells.getCellBuilder());
		}
		for(Map.Entry<Integer, Integer> entry : entity.getExchangeNumMap().entrySet()){
			LuckBoxItemExchange.Builder eBuilder = LuckBoxItemExchange.newBuilder();
			eBuilder.setExchangeId(entry.getKey());
			eBuilder.setNum(entry.getValue());
			builder.addExchangeItems(eBuilder);
		}
		builder.setBuyNeedCount(entity.getBuyNeedCount());
		builder.setRandomCount(entity.getRandomCount());
		
		builder.addAllTips(getTips(LuckyBoxExchangeCfg.class, entity.getTipSet()));
		
		return builder;
	}
}
