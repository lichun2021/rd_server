package com.hawk.activity.type.impl.spaceguard.task;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hawk.os.HawkException;
import org.hawk.util.HawkClassScaner;

import com.hawk.activity.constant.ActivityConst;

public class SpaceMechaTaskContext {

	/** 任务解析器映射<成就类型，parser>*/
	private static Map<SpaceMechaTaskType, SpaceMechaTaskParser<?>> PARSER_MAP = new HashMap<>();

	/** 任务解析器事件映射<事件类型，parser>*/
	private static Map<Class<?>, List<SpaceMechaTaskParser<?>>> PARSER_LISTEN_MAP = new HashMap<>();

	private SpaceMechaTaskContext() {
	}

	public static void registe(SpaceMechaTaskParser<?> parser) {
		PARSER_MAP.put(parser.getTaskType(), parser);
		@SuppressWarnings("rawtypes")
		Class<? extends SpaceMechaTaskParser> parserClass = parser.getClass();
		Type[] genericInterfaces = parserClass.getGenericInterfaces();
		for (Type type : genericInterfaces) {
			Type[] paramTypes = ((ParameterizedType) type).getActualTypeArguments();
			Class<?> key = (Class<?>) paramTypes[0];
			List<SpaceMechaTaskParser<?>> list = PARSER_LISTEN_MAP.get(key);
			if (list == null) {
				list = new ArrayList<>();
				PARSER_LISTEN_MAP.put(key, list);
			}
			list.add(parser);
			break;
		}
	}

	public static SpaceMechaTaskParser<?> getParser(SpaceMechaTaskType type) {
		return PARSER_MAP.get(type);
	}

	public static List<SpaceMechaTaskParser<?>> getParser(Class<?> eventClass) {
		return PARSER_LISTEN_MAP.get(eventClass);
	}

	/**
	 * 注册成就类型解析器
	 */
	public static void initParser() {
		List<Class<?>> allClasses = HawkClassScaner.getAllClasses(ActivityConst.SPACE_POINT_PACKAGE);
		for (Class<?> clazz : allClasses) {
			if (clazz.isAssignableFrom(SpaceMechaTaskParser.class)) {
				continue;
			}
			try {
				@SuppressWarnings("rawtypes")
				SpaceMechaTaskParser parser = (SpaceMechaTaskParser) clazz.newInstance();
				registe(parser);
			} catch (InstantiationException | IllegalAccessException e) {
				HawkException.catchException(e);
			}
		}
	}

}
