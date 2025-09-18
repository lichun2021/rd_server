package com.hawk.activity;

import java.util.HashMap;
import java.util.Map;

import org.hawk.os.HawkException;
import org.hawk.task.HawkTaskManager;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.SubscriberExceptionContext;
import com.google.common.eventbus.SubscriberExceptionHandler;
import com.hawk.activity.event.ActivityEvent;

public class ActivityEventBusPool {
	private Map<Integer, EventBus> eventBusMap;

	public static ActivityEventBusPool create() {
		ActivityEventBusPool pool = new ActivityEventBusPool();
		pool.doCreatEventBus();
		return pool;
	}

	private void doCreatEventBus() {
		eventBusMap = new HashMap<>();
		for (int i = 0; i < HawkTaskManager.getInstance().getThreadNum(); i++) {
			EventBus eventBus = newEventBus();
			eventBusMap.put(i, eventBus);
		}
		eventBusMap.put(Integer.MAX_VALUE, newEventBus()); // 保底
	}

	public EventBus newEventBus() {
		/**
		 * <pre>
		 * 事件中心，对活动事件进行分发
		 * 如需使用事件，请在活动实现类型添加事件处理方法并标注注解：@EventHandler，方法参数为事件类名称
		 * </pre>
		 */
		EventBus eventBus = new EventBus(new SubscriberExceptionHandler() {
			@Override
			public void handleException(Throwable arg0, SubscriberExceptionContext arg1) {
				HawkException.catchException(arg0, arg1);
			}
		});
		return eventBus;
	}

	public boolean post(ActivityEvent event) {
		String targetId = event.getPlayerId();
		int threadIndex = Math.abs(targetId.hashCode()) % HawkTaskManager.getInstance().getThreadNum();
		EventBus eventBus = eventBusMap.get(threadIndex);
		if (eventBus == null) {
			eventBus = eventBusMap.get(Integer.MAX_VALUE);
		}
		eventBus.post(event);
		return true;
	}

	public void register(Object obj) {
		for (EventBus eventBus : eventBusMap.values()) {
			eventBus.register(obj);
		}
	}

	public void unregister(Object obj) {
		for (EventBus eventBus : eventBusMap.values()) {
			eventBus.unregister(obj);
		}
	}
}
