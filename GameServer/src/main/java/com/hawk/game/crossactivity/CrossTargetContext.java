package com.hawk.game.crossactivity;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hawk.os.HawkException;
import org.hawk.util.HawkClassScaner;

public class CrossTargetContext {
	
	public static  final String TARGET_PACKAGE = "com.hawk.game.crossactivity.impl";

	/** 成就解析器映射<成就类型，parser>*/
	private static Map<CrossTargetType, CrossTargetParser<?>> PARSER_MAP = new HashMap<>();

	/** 成就解析器事件映射<事件类型，parser>*/
	private static Map<Class<?>, List<CrossTargetParser<?>>> PARSER_LISTEN_MAP = new HashMap<>();

	private CrossTargetContext() {
	}

	public static void registe(CrossTargetParser<?> parser) {
		PARSER_MAP.put(parser.getTargetType(), parser);
		@SuppressWarnings("rawtypes")
		Class<? extends CrossTargetParser> parserClass = parser.getClass();
		Type[] genericInterfaces = parserClass.getGenericInterfaces();
		for (Type type : genericInterfaces) {
			Type[] paramTypes = ((ParameterizedType) type).getActualTypeArguments();
			Class<?> key = (Class<?>) paramTypes[0];
			List<CrossTargetParser<?>> list = PARSER_LISTEN_MAP.get(key);
			if (list == null) {
				list = new ArrayList<>();
				PARSER_LISTEN_MAP.put(key, list);
			}
			list.add(parser);
			break;
		}
	}

	public static CrossTargetParser<?> getParser(CrossTargetType type) {
		return PARSER_MAP.get(type);
	}

	public static List<CrossTargetParser<?>> getParser(Class<?> eventClass) {
		return PARSER_LISTEN_MAP.get(eventClass);
	}

	/**
	 * 注册成就类型解析器
	 */
	public static void initParser() {
		List<Class<?>> allClasses = HawkClassScaner.getAllClasses(TARGET_PACKAGE);
		for (Class<?> clazz : allClasses) {
			if (clazz.isAssignableFrom(CrossTargetParser.class)) {
				continue;
			}
			try {
				@SuppressWarnings("rawtypes")
				CrossTargetParser parser = (CrossTargetParser) clazz.newInstance();
				registe(parser);
			} catch (InstantiationException | IllegalAccessException e) {
				HawkException.catchException(e);
			}
		}
	}

}
