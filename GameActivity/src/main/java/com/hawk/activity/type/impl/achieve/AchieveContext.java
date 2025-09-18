package com.hawk.activity.type.impl.achieve;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.hawk.os.HawkException;
import org.hawk.util.HawkClassScaner;

import com.hawk.activity.constant.ActivityConst;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;

public class AchieveContext {

	/** 成就解析器映射<成就类型，parser>*/
	private static Map<AchieveType, AchieveParser<?>> PARSER_MAP = new HashMap<>();

	/** 成就解析器事件映射<事件类型，parser>*/
	private static Map<Class<?>, List<AchieveParser<?>>> PARSER_LISTEN_MAP = new HashMap<>();

	/** 成就数据提供者列表*/
	private static List<AchieveProvider> PROVIDER_LIST = new CopyOnWriteArrayList<>();

	private AchieveContext() {
	}

	public static void registe(AchieveParser<?> parser) {
		PARSER_MAP.put(parser.geAchieveType(), parser);
		@SuppressWarnings("rawtypes")
		Class<? extends AchieveParser> parserClass = parser.getClass();
		Type genericSuperclass = parserClass.getGenericSuperclass();
		Type[] paramTypes = ((ParameterizedType) genericSuperclass).getActualTypeArguments();
		Class<?> key = (Class<?>) paramTypes[0];
		List<AchieveParser<?>> list = PARSER_LISTEN_MAP.get(key);
		if (list == null) {
			list = new ArrayList<>();
			PARSER_LISTEN_MAP.put(key, list);
		}
		list.add(parser);
	}

	public static AchieveParser<?> getParser(AchieveType type) {
		return PARSER_MAP.get(type);
	}

	public static List<AchieveParser<?>> getParser(Class<?> eventClass) {
		return PARSER_LISTEN_MAP.get(eventClass);
	}

	public static void registeProvider(AchieveProvider provider) {
		PROVIDER_LIST.add(provider);
	}

	public static List<AchieveProvider> getProviders() {
		return PROVIDER_LIST;
	}

	/**
	 * 注册成就类型解析器
	 */
	public static void initParser() {
		List<Class<?>> allClasses = HawkClassScaner.getAllClasses(ActivityConst.ACHIEVE_PARSER_PACKAGE);
		for (Class<?> clazz : allClasses) {
			if (clazz.getSuperclass() != AchieveParser.class) {
				continue;
			}
			try {
				@SuppressWarnings("rawtypes")
				AchieveParser parser = (AchieveParser) clazz.newInstance();
				registe(parser);
			} catch (InstantiationException | IllegalAccessException e) {
				HawkException.catchException(e);
			}
		}
	}

}
