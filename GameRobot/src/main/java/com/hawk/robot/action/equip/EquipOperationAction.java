package com.hawk.robot.action.equip;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.hawk.annotation.RobotAction;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;
import org.hawk.robot.HawkRobotAction;
import org.hawk.robot.HawkRobotEntity;

import com.hawk.game.protocol.Const.QueueType;
import com.hawk.game.protocol.Equip;
import com.hawk.game.protocol.Equip.EquipForgeOrQualityUpReq;
import com.hawk.game.protocol.Equip.EquipInfo;
import com.hawk.game.protocol.Equip.EquipState;
import com.hawk.game.protocol.Equip.PBEquipSlot;
import com.hawk.game.protocol.Equip.PutOnEquipReq;
import com.hawk.game.protocol.Equip.TakeOffEquipReq;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Item;
import com.hawk.game.protocol.Queue.QueuePB;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.RobotLog;
import com.hawk.robot.action.item.PlayerItemBuyAction;
import com.hawk.robot.config.ConstProperty;
import com.hawk.robot.config.EquipCfg;
import com.hawk.robot.config.element.ItemInfo;
import com.hawk.robot.data.BasicData;

/**
 * 
 * 装备操作
 * @author Jesse
 *
 */
@RobotAction(valid = false)
public class EquipOperationAction extends HawkRobotAction {
	
	private Map<EquipActionType, Integer> actionMap = new HashMap<EquipActionType, Integer>();
	/**
	 * 玩家装备最大数量
	 */
	private final int MAX_EQUIP_COUNT = 100;
	
	private enum EquipActionType{
		FORGE(20),  //打造
		PUT_ON(40), //穿戴
		TAKE_OFF(30), //卸下
		QUALITY_UP(10); //升品
		
		private final int rand;
		
		private EquipActionType(int rand){
			this.rand = rand;
		}
	
		public int getRand() {
			return rand;
		}
	}

	public EquipOperationAction() {
		actionMap.put(EquipActionType.FORGE, EquipActionType.FORGE.getRand());
		actionMap.put(EquipActionType.PUT_ON, EquipActionType.PUT_ON.getRand());
		actionMap.put(EquipActionType.TAKE_OFF, EquipActionType.TAKE_OFF.getRand());
		actionMap.put(EquipActionType.QUALITY_UP, EquipActionType.QUALITY_UP.getRand());
	}

	@Override
	public void doAction(HawkRobotEntity robotEntity) {
		GameRobotEntity robot = (GameRobotEntity) robotEntity;
		
		EquipActionType type = HawkRand.randomWeightObject(actionMap);
		switch (type) {
		case FORGE:
			doForge(robot);
			break;
		case PUT_ON:
			doPutOn(robot);
			break;
		case TAKE_OFF:
			doTakeOff(robot);
			break;
		case QUALITY_UP:
			onEquipQualityEnhance(robot);
			break;
		}
	}
	
	/**
	 * 装备升品
	 * @param robot
	 */
	public void onEquipQualityEnhance(GameRobotEntity robot) {
		if (isQueueBusy(robot)) {
			return;
		}
		
		Predicate<EquipInfo> predicate = new Predicate<Equip.EquipInfo>() {
			@Override
			public boolean test(EquipInfo arg0) {
				EquipCfg cfg = HawkConfigManager.getInstance().getConfigByKey(EquipCfg.class, arg0.getCfgId());
				return arg0.getState() == EquipState.FREE && cfg.getQuality() < ConstProperty.getInstance().getEquipMaxQuality();
			}
		};
		
		Optional<EquipInfo> opEquip = randomEquip(robot.getBasicData(), predicate);
		if(!opEquip.isPresent()){
			return;
		}
		
		EquipInfo equipObj = opEquip.get();
		EquipCfg cfg = HawkConfigManager.getInstance().getConfigByKey(EquipCfg.class, equipObj.getCfgId());
		if (!isMaterialEnough(robot, cfg)) {
			return;
		}
		
		EquipCfg aftCfg = getEquipCfgWithOutId(cfg.getMouldId(), cfg.getQuality() + 1);
		EquipForgeOrQualityUpReq.Builder builder = EquipForgeOrQualityUpReq.newBuilder();
		builder.setTargetId(aftCfg.getId());
		builder.setMaterialEquip(equipObj.getId());
		builder.setImmediately(HawkRand.randPercentRate(50));
		robot.sendProtocol(HawkProtocol.valueOf(HP.code.EQUIP_QUALITY_UP_C_VALUE, builder));
		RobotLog.cityPrintln("send equip operation protocol, equip quality enhance, playerId: {}, equipId: {}, targetId: {}", 
				robot.getPlayerId(), equipObj.getCfgId(), aftCfg.getId());
	}
	
	/**
	 * 穿戴装备
	 * @param robot
	 */
	public void doPutOn(GameRobotEntity robot){
		BasicData basicData = robot.getBasicData();
		int playerLvl = basicData.getPlayerInfo().getLevel();
		Predicate<EquipInfo> predicate = new Predicate<Equip.EquipInfo>() {
			@Override
			public boolean test(EquipInfo arg0) {
				EquipCfg cfg = HawkConfigManager.getInstance().getConfigByKey(EquipCfg.class, arg0.getCfgId());
				return arg0.getState() == EquipState.FREE && cfg.getLevel()<= playerLvl;
			}
		};
		
		Optional<EquipInfo> opEquip = randomEquip(basicData, predicate);
		if(!opEquip.isPresent()){
			return;
		}
		
		PutOnEquipReq.Builder builder = PutOnEquipReq.newBuilder();
		builder.setEquipId(opEquip.get().getId());
		robot.sendProtocol(HawkProtocol.valueOf(HP.code.PUT_ON_EQUIP_C_VALUE, builder));
		RobotLog.cityPrintln("send equip operation protocol, put on equip, playerId: {}, equipId: {}", robot.getPlayerId(), opEquip.get().getId());
	}
	
	/**
	 * 卸下装备
	 * @param robot
	 */
	public void doTakeOff(GameRobotEntity robot) {
		BasicData basicData = robot.getBasicData();
		List<PBEquipSlot> list = basicData.getCommanderEquipSlotList();
		Collections.shuffle(list);
		Optional<PBEquipSlot> opEquip = list.stream()
				.filter(e -> !HawkOSOperator.isEmptyString(e.getEquipId()))
				.findFirst();
		if (!opEquip.isPresent()) {
			return;
		}
		TakeOffEquipReq.Builder builder = TakeOffEquipReq.newBuilder();
		builder.setEquipId(opEquip.get().getEquipId());
		robot.sendProtocol(HawkProtocol.valueOf(HP.code.TAKE_OFF_EQUIP_C_VALUE, builder));
		RobotLog.cityPrintln("send equip operation protocol, take off equip, playerId: {}, equipId: {}", robot.getPlayerId(), opEquip.get().getEquipId());
	}
	
	/**
	 * 装备打造
	 * @param robot
	 */
	public void doForge(GameRobotEntity robot){
		if (isQueueBusy(robot)) {
			return;
		}
		
		int equipCnt = robot.getBasicData().getEquipList().size();
		// 装备数大于100件,则不再进行合成
		if (equipCnt > MAX_EQUIP_COUNT) {
			return;
		}
		
		ConfigIterator<EquipCfg> it = HawkConfigManager.getInstance().getConfigIterator(EquipCfg.class);
		List<EquipCfg> list = it.stream().filter(e -> !e.getForgeMaterialList().isEmpty()).collect(Collectors.toList());
		if(list.isEmpty()){
			RobotLog.cityPrintln("forge equip failed, equip cfg empty, playerId: {}", robot.getPlayerId());
			return;
		}
		
		EquipCfg cfg = list.get(HawkRand.randInt(list.size() -1));
		if (!isMaterialEnough(robot, cfg)) {
			return;
		}
		
		EquipForgeOrQualityUpReq.Builder builder = EquipForgeOrQualityUpReq.newBuilder();
		builder.setTargetId(cfg.getId());
		builder.setImmediately(HawkRand.randPercentRate(50));
		robot.sendProtocol(HawkProtocol.valueOf(HP.code.EQUIP_FORGE_C_VALUE, builder));
		RobotLog.cityPrintln("send equip operation protocol, forge equip, playerId: {}, targetId: {}", robot.getPlayerId(), cfg.getId());
	}
	
	/**
	 * 随机一件装备
	 * @param basicData
	 * @param predicate
	 * @return
	 */
	public Optional<EquipInfo> randomEquip(BasicData basicData, Predicate<EquipInfo> predicate){
		List<EquipInfo> list = basicData.getEquipList();
		Collections.shuffle(list);
		return list.stream().filter(predicate).findFirst();
	}
	
	private EquipCfg getEquipCfgWithOutId(int mouldId, int quality) {
		Optional<EquipCfg> opCfg = HawkConfigManager.getInstance().getConfigIterator(EquipCfg.class).stream()
				.filter(e -> e.getMouldId() == mouldId && e.getQuality() == quality)
				.findAny();
		if (opCfg.isPresent()) {
			return opCfg.get();
		}
		return null;
	}
	
	/**
	 * 判断装备队列是否空闲
	 * @param robot
	 * @return
	 */
	private boolean isQueueBusy(GameRobotEntity robot) {
		Optional<QueuePB> queueOptional = robot.getQueueObjects().stream()
                .filter(queue -> queue.getQueueType() == QueueType.EQUIP_QUEUE)
                .findAny();
		return queueOptional.isPresent();
	}
	
	/**
	 * 打造装备、装备升品判断材料是否充足
	 * @param robot
	 * @param cfg
	 * @return
	 */
	private boolean isMaterialEnough(GameRobotEntity robot, EquipCfg cfg) {
		List<ItemInfo> consumeList = cfg.getForgeMaterialList();
		boolean enough = true;
		for (ItemInfo cost : consumeList) {
			Optional<Item.ItemInfo> opBagItem = robot.getBasicData().getItemInfo(cost.getItemId());
			if(!opBagItem.isPresent() || opBagItem.get().getCount() < cost.getCount()) {
				RobotLog.cityPrintln("forge equip failed, equip material not enough, playerId: {}, equipId: {}", robot.getPlayerId(), cfg.getId());
				PlayerItemBuyAction.buyItem(robot, cost.getItemId(), cost.getCount());
				enough = false;
			}
		}
		
		return enough;
	}
}
