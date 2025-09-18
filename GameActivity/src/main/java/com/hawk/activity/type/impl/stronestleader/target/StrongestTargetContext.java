package com.hawk.activity.type.impl.stronestleader.target;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hawk.os.HawkException;
import org.hawk.util.HawkClassScaner;

import com.hawk.activity.constant.ActivityConst;

public class StrongestTargetContext {

	/** 成就解析器映射<成就类型，parser>*/
	private static Map<StrongestTargetType, StrongestTargetParser<?>> PARSER_MAP = new HashMap<>();

	/** 成就解析器事件映射<事件类型，parser>*/
	private static Map<Class<?>, List<StrongestTargetParser<?>>> PARSER_LISTEN_MAP = new HashMap<>();

	private StrongestTargetContext() {
	}

	public static void registe(StrongestTargetParser<?> parser) {
		PARSER_MAP.put(parser.getTargetType(), parser);
		@SuppressWarnings("rawtypes")
		Class<? extends StrongestTargetParser> parserClass = parser.getClass();
		Type[] genericInterfaces = parserClass.getGenericInterfaces();
		for (Type type : genericInterfaces) {
			Type[] paramTypes = ((ParameterizedType) type).getActualTypeArguments();
			Class<?> key = (Class<?>) paramTypes[0];
			List<StrongestTargetParser<?>> list = PARSER_LISTEN_MAP.get(key);
			if (list == null) {
				list = new ArrayList<>();
				PARSER_LISTEN_MAP.put(key, list);
			}
			list.add(parser);
			break;
		}
	}

	public static StrongestTargetParser<?> getParser(StrongestTargetType type) {
		return PARSER_MAP.get(type);
	}

	public static List<StrongestTargetParser<?>> getParser(Class<?> eventClass) {
		return PARSER_LISTEN_MAP.get(eventClass);
	}

	/**
	 * 注册成就类型解析器
	 */
	public static void initParser() {
		List<Class<?>> allClasses = HawkClassScaner.getAllClasses(ActivityConst.STRONGEST_TARGET_PACKAGE);
		for (Class<?> clazz : allClasses) {
			if (clazz.isAssignableFrom(StrongestTargetParser.class)) {
				continue;
			}
			try {
				@SuppressWarnings("rawtypes")
				StrongestTargetParser parser = (StrongestTargetParser) clazz.newInstance();
				registe(parser);
			} catch (InstantiationException | IllegalAccessException e) {
				HawkException.catchException(e);
			}
		}
	}

}
