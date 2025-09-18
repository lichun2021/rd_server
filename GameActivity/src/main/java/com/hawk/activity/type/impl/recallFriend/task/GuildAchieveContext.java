package com.hawk.activity.type.impl.recallFriend.task;

import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.recallFriend.parser.IGuildAchieveParser;
import com.hawk.activity.type.impl.recallFriend.parser.GuildConsumeMoneyParser;
import com.hawk.activity.type.impl.recallFriend.parser.GuildDonateParser;
import com.hawk.activity.type.impl.recallFriend.parser.GuildRecallFriendParser;
import com.hawk.activity.type.impl.recallFriend.parser.GuildTrainSoldierCompleteParser;
import com.hawk.activity.type.impl.recallFriend.parser.GuildVitreceiveConsumeParser;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author hf
 */
public class GuildAchieveContext {

	/** 任务解析器映射<成就类型，parser>*/
	private static Map<AchieveType, IGuildAchieveParser<?>> PARSER_MAP = new HashMap<>();

	/** 任务解析器事件映射<事件类型，parser>*/
	private static Map<Class<?>, List<IGuildAchieveParser<?>>> PARSER_LISTEN_MAP = new HashMap<>();

	private GuildAchieveContext() {
	}

	public static void registe(IGuildAchieveParser<?> parser) {
		PARSER_MAP.put(parser.geAchieveType(), parser);
		@SuppressWarnings("rawtypes")
		Class<? extends IGuildAchieveParser> parserClass = parser.getClass();
		Type genericSuperclass = parserClass.getGenericSuperclass();
		Type[] paramTypes = ((ParameterizedType) genericSuperclass).getActualTypeArguments();
		Class<?> key = (Class<?>) paramTypes[0];
		List<IGuildAchieveParser<?>> list = PARSER_LISTEN_MAP.get(key);
		if (list == null) {
			list = new ArrayList<>();
			PARSER_LISTEN_MAP.put(key, list);
		}
		list.add(parser);
	}

	public static IGuildAchieveParser<?> getParser(AchieveType type) {
		return PARSER_MAP.get(type);
	}

	public static List<IGuildAchieveParser<?>> getParser(Class<?> eventClass) {
		return PARSER_LISTEN_MAP.get(eventClass);
	}

	/**
	 * 注册成就类型解析器
	 */
	public static void initParser() {
		registe(new GuildConsumeMoneyParser());
		registe(new GuildDonateParser());
		registe(new GuildRecallFriendParser());
		registe(new GuildTrainSoldierCompleteParser());
		registe(new GuildVitreceiveConsumeParser());
	}

}
