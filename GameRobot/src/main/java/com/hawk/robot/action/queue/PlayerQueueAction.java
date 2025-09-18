package com.hawk.robot.action.queue;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.hawk.annotation.RobotAction;
import org.hawk.config.HawkConfigManager;
import org.hawk.enums.EnumUtil;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.robot.HawkRobotAction;
import org.hawk.robot.HawkRobotEntity;

import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Const.QueueType;
import com.hawk.game.protocol.Const.ToolSpeedUpType;
import com.hawk.game.protocol.Item.ItemInfo;
import com.hawk.game.protocol.Queue.PaidQueuePayReq;
import com.hawk.game.protocol.Queue.QueueCancelReq;
import com.hawk.game.protocol.Queue.QueueFinishFreeReq;
import com.hawk.game.protocol.Queue.QueuePB;
import com.hawk.game.protocol.Queue.QueueSpeedUpReq;
import com.hawk.game.protocol.Queue.SpeedItem;
import com.hawk.robot.GameRobotApp;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.RobotLog;
import com.hawk.robot.config.ConstProperty;
import com.hawk.robot.config.ItemCfg;

/**
 * 
 * 城内计时器操作action类
 * 
 * @author lating
 *
 */
@RobotAction(valid = false)
public class PlayerQueueAction extends HawkRobotAction {
	
	/**
	 * 队列操作类型
	 */
	private static enum QueueOperType {
		QUEUE_SPEEDUP_1,  // 队列加速，加速队列有多个的原因，是让该操作随机到的概率更大
		QUEUE_SPEEDUP_2,  // 队列加速
		QUEUE_SPEEDUP_3,  // 队列加速
		QUEUE_SPEEDUP_4,  // 队列加速
		QUEUE_SPEEDUP_5,  // 队列加速
		QUEUE_CANCLE,    // 取消队列
		QUEUE_FINISH,    // 免费完成队列
		OPEN_QUEUE       // 开启第二建造队列
	}

	@Override
	public void doAction(HawkRobotEntity robotEntity) {
		GameRobotEntity robot = (GameRobotEntity) robotEntity;
		QueueOperType type = EnumUtil.random(QueueOperType.class);
		switch (type) {
		case QUEUE_CANCLE:
			cancelQueue(robot);
			break;
		case QUEUE_FINISH:
			finishOneQueue(robot);
			break;
		case OPEN_QUEUE:
			openQueue(robot);
			break;
		default:
			doQueueSpeedAction(robot);
			break;
		}
	}

	/**
	 * 开启第二建造队列
	 * 
	 * @param robot
	 */
	private void openQueue(GameRobotEntity robot) {
		long endTime = robot.getBasicData().getPaidQueueEnableEndTime();
		if (endTime == Long.MAX_VALUE) {
			return;
		}
		
		if(endTime > HawkTime.getMillisecond() && HawkRand.randPercentRate(50)) {
			return;
		}
		
		PaidQueuePayReq.Builder builder = PaidQueuePayReq.newBuilder();
		boolean isGold = HawkRand.randPercentRate(50);
		if(!isGold) {
			Optional<ItemInfo> op = robot.getItemObjects().stream().filter(e -> {
				ItemCfg itemCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, e.getItemId());
				return itemCfg.getItemType() == 112;
			}).findAny();
			
			if(!op.isPresent()) {
				return;
			}
			
			builder.setItemUuid(op.get().getUuid());
			builder.setCount(1);
		}
		
		builder.setConsumeGold(isGold);
		robot.sendProtocol(HawkProtocol.valueOf(HP.code.PAID_QUEUE_PAY_C_VALUE, builder));
		RobotLog.cityDebugPrintln("open paid queue action, playerId: {}, isGold: {}", robot.getPlayerId(), isGold);
	}
	
	/**
	 * 取消队列
	 * 
	 * @param robot
	 */
	private void cancelQueue(GameRobotEntity robot) {
		List<QueuePB> queueList = robot.getQueueObjects();
		if(queueList.size() <= 0) {
			return;
		}
		
		Collections.shuffle(queueList);
		for(QueuePB queue : queueList) {
			if (queue.getQueueType() == QueueType.FOGGY_BOX_QUEUE) {
				continue;
			}
			
			if(queue.getEndTime() - HawkTime.getMillisecond() <= 360000) {
				continue;
			}
			QueueCancelReq.Builder builder = QueueCancelReq.newBuilder();
			builder.setId(queue.getId());
			robot.sendProtocol(HawkProtocol.valueOf(HP.code.QUEUE_CANCEL_C_VALUE, builder));
			RobotLog.cityPrintln("finish time queue action, playerId: {}, queueId: {}, time: {}", 
					robot.getPlayerId(), queue.getId(), (queue.getEndTime() - HawkTime.getMillisecond()) / 1000);
			break;
		}
	}
	
	/**
	 * 免费完成队列
	 * 
	 * @param robot
	 */
	private void finishOneQueue(GameRobotEntity robot) {
		List<QueuePB> queueList = robot.getQueueObjects();
		if(queueList.size() <= 0) {
			return;
		}
		
		Collections.shuffle(queueList);
		
		for(QueuePB queue : queueList) {
			QueueType queueType = queue.getQueueType();
			if (queueType == QueueType.FOGGY_BOX_QUEUE) {
				continue;
			}
			
			if (queueType != QueueType.BUILDING_QUEUE && queueType != QueueType.SCIENCE_QUEUE) {
				continue;
			}
			
			long freeTime = queueType == QueueType.BUILDING_QUEUE ? ConstProperty.getInstance().getFreeTime() : ConstProperty.getInstance().getScienceFreeTime();
			long remainTime = queue.getEndTime() - HawkTime.getMillisecond();
			if(remainTime > freeTime * 1000L) {
				return;
			}
			
			finishQueue(robot, queue);
		}
	}
	
	public static synchronized void finishQueue(GameRobotEntity robot, QueuePB queue) {
		if (queue.getEndTime() - HawkTime.getMillisecond() < 15000) {
			return;
		}
		
		QueueFinishFreeReq.Builder builder = QueueFinishFreeReq.newBuilder();
		builder.setId(queue.getId());
		robot.sendProtocol(HawkProtocol.valueOf(HP.code.QUEUE_FININSH_FREE_C_VALUE, builder));
		RobotLog.cityPrintln("finish time queue action, playerId: {}, queueId: {}, time: {}", 
				robot.getPlayerId(), queue.getId(), (queue.getEndTime() - HawkTime.getMillisecond()) / 1000);
	}
	
	/**
	 * 加速队列
	 * 
	 * @param robot
	 */
	public static synchronized void doQueueSpeedAction(GameRobotEntity robot) {
		if(!robot.isOnline()) {
			return;
		}
		
		List<QueuePB> queueList = robot.getQueueObjects();
		if(queueList.size() <= 0) {
			return;
		}
		
		Collections.shuffle(queueList);
		boolean isGold = false;
		List<ItemInfo> itemList = robot.getItemObjects();
		if(itemList.size() <= 0) {
			isGold = true;
		}
		
		if (!isGold) {
			isGold = HawkRand.randPercentRate(30);
		}
		
		ItemInfo item = null;
		QueuePB queue = null;
		for(QueuePB q : queueList) {
			if (q.getQueueType() == QueueType.FOGGY_BOX_QUEUE || q.getQueueType() == QueueType.GUILD_HOSPICE_QUEUE) {
				continue;
			}
			
			if (checkQueueRemainTimeFree(robot, q)) {
				continue;
			}
			
			if (q.getQueueType() != QueueType.BUILDING_QUEUE && q.getQueueType() != QueueType.SCIENCE_QUEUE) {
				isGold = true;
			} 
			
			queue = q;
			if(isGold) {
				break;
			}
			
			item = selectItem(itemList, q);
			if (item != null) {
				break;
			}
		}
		
		if (queue == null) {
			return;
		}
		
		if (!isGold && item == null) {
			if (HawkRand.randPercentRate(60)) {
				return;
			}
			
			isGold = true;
		} 
		
		QueueSpeedUpReq.Builder builder = QueueSpeedUpReq.newBuilder();
		builder.setId(queue.getId());
		builder.setIsGold(isGold);
		if (item != null) {
			SpeedItem.Builder speedItemBuilder = SpeedItem.newBuilder();
			speedItemBuilder.setItemUUid(item.getUuid());
			speedItemBuilder.setCount(HawkRand.randInt(item.getCount() - 1) + 1);
			builder.addSpeedItem(speedItemBuilder);
		}
		
		robot.sendProtocol(HawkProtocol.valueOf(HP.code.QUEUE_SPEED_UP_C_VALUE, builder));
		RobotLog.cityPrintln("speed time queue action, playerId: {}, queueId: {}, time: {}, isGold: {}", robot.getPlayerId(), queue.getId(), (queue.getEndTime() - HawkTime.getMillisecond()) / 1000, isGold);
	}
	
	/**
	 * 选择加速道具
	 * 
	 * @param itemList
	 * @param queue
	 * @return
	 */
	private static ItemInfo selectItem(List<ItemInfo> itemList, QueuePB queue) {
		for(ItemInfo itemInfo : itemList) {
			if (itemInfo.getCount() <= 0) {
				continue;
			}
			
			ItemCfg itemCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, itemInfo.getItemId());
			if(itemCfg == null || itemCfg.getItemType() != Const.ToolType.SPEED_UP_VALUE) {
				continue;
			}
			
			if (!itemCfg.speedUpAble(ToolSpeedUpType.TOOL_SPEED_COMMON_VALUE) 
					&& !itemCfg.speedUpAble(queue.getQueueType().getNumber())) {
				continue;
			}
			
			return itemInfo;
		}
		
		return null;
	}
	
	/**
	 * 判断一个队列的剩余时间是否满足免费加速条件
	 * @param robot
	 * @param queue
	 * @return
	 */
	public static boolean checkQueueRemainTimeFree(GameRobotEntity robot, QueuePB queue) {
		QueueType queueType = queue.getQueueType();
		int freeTime = 300;
		if (queueType == QueueType.BUILDING_QUEUE) {
			freeTime = ConstProperty.getInstance().getFreeTime();
		} else if (queueType == QueueType.SCIENCE_QUEUE) {
			freeTime = ConstProperty.getInstance().getScienceFreeTime();
		}
		
		long remainTime = queue.getEndTime() - HawkTime.getMillisecond();
		if(remainTime <= freeTime * 1000L) {
			if (remainTime < 15000) {
				return true;
			}
			
			if (queueType != QueueType.BUILDING_QUEUE && queueType != QueueType.SCIENCE_QUEUE) {
				return true;
			} 
			
			GameRobotApp.getInstance().executeTask(new Runnable() {
				@Override
				public void run() {
					finishQueue(robot, queue);
				}
			});
			
			return true;
		}
		
		return false;
	}
}
