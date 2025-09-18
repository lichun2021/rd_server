package com.hawk.game.activity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.pddActivity.PDDActivity;
import com.hawk.activity.type.impl.pddActivity.data.PDDOrderData;
import com.hawk.game.protocol.Activity;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.db.HawkDBEntity;
import org.hawk.msg.HawkMsg;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.serializer.HawkSerializer;

import com.hawk.activity.ActivityHandler;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.ActivityEvent;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.game.log.DungeonRedisLog;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.protocol.Cross.PBCrossPostActivityEvent;
import com.hawk.game.protocol.HP;


/**
 * 玩家活动模块
 * @author PhilChen
 *
 */
public class ActivityModule extends PlayerModule {

	private long lastTickTime = 0;
	
	public ActivityModule(Player player) {
		super(player);
	}

	@Override
	public boolean isListenMsg(HawkMsg msg) {
		//只需要处理跨入的玩家,跨出的玩家数据会在源头拦截
		if (player.isCsPlayer()) {
			return false;
		}
		return ActivityHandler.getMessageMethodMap().containsKey(msg.getMsgCls());
	}

	@Override
	public boolean isListenProto(int proto) {
		//只需要处理跨入的玩家,跨出的玩家数据会在源头拦截
		if (player.isCsPlayer()) {
			return false;
		}
		if(super.isListenProto(proto)){
			return true;
		}
		return ActivityHandler.getProtoMethodMap().containsKey(proto);
	}
	
	@Override
	public boolean onMessage(HawkMsg msg) {
		// 只需要处理跨入的玩家,跨出的玩家数据会在源头拦截
		if (player.isCsPlayer()) {
			return false;
		}
		return ActivityHandler.onMessage(player.getId(), player, msg);
	}

	@Override
	public boolean onProtocol(HawkProtocol protocol) {
		//只需要处理跨入的玩家,跨出的玩家数据会在源头拦截
		if (player.isCsPlayer()) {
			return false;
		}
		try {
			boolean result = ActivityHandler.onProtocol(player.getId(), player, protocol);
			if (result) {
				return true;
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return super.onProtocol(protocol);
	}

	@Override
	protected boolean onPlayerLogin() {
		//只需要处理跨入的玩家,跨出的玩家数据会在源头拦截
		if (player.isCsPlayer()) {
			return false;
		}
		return ActivityHandler.onPlayerLogin(player.getId());
	}
	
	@Override
	protected boolean onPlayerLogout() {
		//只需要处理跨入的玩家,跨出的玩家数据会在源头拦截
		if (player.isCsPlayer()) {
			return false;
		}
		return ActivityHandler.onPlayerLogout(player.getId());
	}

	@Override
	public boolean onTick() {
		//只需要处理跨入的玩家,跨出的玩家数据会在源头拦截
		if (player.isCsPlayer()) {
			return false;
		}
		// 10min检查一次,控制下频率
		long currentTime = HawkTime.getMillisecond();
		if (currentTime - lastTickTime < TimeUnit.MINUTES.toMillis(10)) {
			return true;
		}
		lastTickTime = currentTime;
		return ActivityHandler.onPlayerTick(player.getId());
	}

	/**
	 * target-> local 
	 * 跨服活动事件投递 
	 */
	@ProtocolHandler(code = HP.sys.CROSS_POST_ACTIVITY_EVENT_VALUE)
	private void onCrossPostEvent(HawkProtocol protocol) {
		if (player.isCsPlayer()) {
			return;
		}
		
		PBCrossPostActivityEvent req = protocol.parseProtocol(PBCrossPostActivityEvent.getDefaultInstance());
		try {
			Class<?> cls = Class.forName(req.getClassName());
			ActivityEvent event = (ActivityEvent) HawkSerializer.deserialize(req.getEventSerialize().toByteArray(), cls);
			ActivityManager.getInstance().postEvent(event, true);
			
			if (event instanceof ContinueLoginEvent) {
//				ActivityHandler.onPlayerLogin(player.getId());
				if(((ContinueLoginEvent) event).isCrossDay()){
					DungeonRedisLog.log(player.getId(), "ContinueLoginEvent is crossday corssPostEvent");
				}
				
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	public <T extends HawkDBEntity> Map<String, T> converEntityMap(List<T> entityList) {
		Map<String, T> entityMap = new HashMap<>();
		for (T entity : entityList) {
			entityMap.put(entity.getPrimaryKey(), entity);
		}
		return entityMap;
	}

	/**
	 * 拼多多信息请求
	 */
	@ProtocolHandler(code = HP.code2.CHAT_CARD_INFO_REQ_VALUE)
	public boolean onChatCardInfoReq(HawkProtocol protocol) {
		Activity.ChatCardInfoReq req = protocol.parseProtocol(Activity.ChatCardInfoReq.getDefaultInstance());
		if(req.getType() != Activity.ChatCardType.PDD_CARD){
			return false;
		}
		Activity.ChatCardInfoResp.Builder builder = Activity.ChatCardInfoResp.newBuilder();
		List<String> orderIdList = req.getIdsList();
		for (String orderId : orderIdList) {
			Activity.PDDOrderInfo.Builder pddCardInfo = getChatPddCardInfo(orderId);
			if (pddCardInfo != null) {
				builder.addPddCardInfos(pddCardInfo);
			}
		}
		builder.setType(req.getType());
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.CHAT_CARD_INFO_RESP_VALUE, builder));
		return true;
	}

	private Activity.PDDOrderInfo.Builder getChatPddCardInfo(String orderId){
		try {
			Optional<PDDActivity> opActivity = ActivityManager.getInstance().getGameActivityByType(ActivityType.PDD_ACTIVITY.intValue());
			if (!opActivity.isPresent()) {
				return null;
			}
			if(HawkOSOperator.isEmptyString(orderId)){
				return null;
			}
			String[] infos = orderId.split(":");
			if(infos.length != 2){
				return null;
			}
			PDDActivity activity = opActivity.get();
			PDDOrderData data = activity.loadUserOrder(infos[0], orderId);
			if(data == null){
				return null;
			}
			return activity.fillOrderPb(data);
		}catch (Exception e){
			HawkException.catchException(e);
		}
		return null;
	}
}
