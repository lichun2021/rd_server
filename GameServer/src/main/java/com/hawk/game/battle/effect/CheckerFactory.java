package com.hawk.game.battle.effect;

import java.util.Collection;
import java.util.List;

import org.hawk.collection.ConcurrentHashTable;
import org.hawk.os.HawkException;
import org.hawk.util.HawkClassScaner;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.impl.Checker100;
import com.hawk.game.protocol.Const.EffType;

/**
 * 
 * @author lwt
 * @date 2017年11月6日
 */
public class CheckerFactory {
	private static transient CheckerFactory INSTANCE = new CheckerFactory();
	
	private Table<BattleTupleType.Type,EffType,IChecker>  table ;

	private CheckerFactory() {
	}

	/**
	 * 已注册做用号,对应取值器
	 * @param type
	 * @return
	 */
	public Collection<IChecker> checkersMap(BattleTupleType.Type type){
		return table.row(type).values();
	}


	public static CheckerFactory getInstance() {
		return INSTANCE;

	}

	public void init() {
		table = ConcurrentHashTable.create();
		String packageName = Checker100.class.getPackage().getName();
		try {
//			ClassPath classPath = ClassPath.from(IChecker.class.getClassLoader());
//			ImmutableSet<ClassInfo> set = classPath.getTopLevelClasses(packageName);
//			for (ClassInfo info : set) {
//			Class<?> cls = info.load();
			List<Class<?>> allClasses = HawkClassScaner.getAllClasses(packageName);
			for(Class<?> cls : allClasses){
				if (!cls.isAnnotationPresent(EffectChecker.class)) {
					continue;
				}
				if (!cls.isAnnotationPresent(BattleTupleType.class)) {
					continue;
				}
				if (!IChecker.class.isAssignableFrom(cls)) {
					continue;
				}

				EffType effType = cls.getAnnotation(EffectChecker.class).effType();
				Type[] tuples = cls.getAnnotation(BattleTupleType.class).tuple();
				for (Type tuple : tuples) {
					table.put(tuple, effType, (IChecker) cls.newInstance());
				}
			}
			table = ImmutableTable.copyOf(table);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
}
