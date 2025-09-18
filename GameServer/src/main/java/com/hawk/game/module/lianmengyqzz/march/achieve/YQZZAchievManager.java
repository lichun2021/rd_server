package com.hawk.game.module.lianmengyqzz.march.achieve;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;
import com.hawk.game.module.lianmengyqzz.march.entitiy.YQZZAchieve;
import com.hawk.game.module.lianmengyqzz.march.entitiy.YQZZAchievecomponent;
import com.hawk.game.module.lianmengyqzz.march.service.YQZZConst.YQZZAchieveState;
import com.hawk.game.module.lianmengyqzz.march.service.YQZZConst.YQZZAchieveType;

public class YQZZAchievManager {
	
	private Map<YQZZAchieveType,IYQZZAchievParser> parserMap;
	private static YQZZAchievManager ins;
	
	public static YQZZAchievManager getInstance(){
		if(ins == null){
			ins = new YQZZAchievManager();
		}
		return ins;
	}
	
	private YQZZAchievManager() {
		init();
	}
	
	
	public void init(){
		Map<YQZZAchieveType,IYQZZAchievParser> map = new HashMap<>();
		String packageName = IYQZZAchievParser.class.getPackage().getName();
		try {
			ClassPath classPath = ClassPath.from(IYQZZAchievParser.class.getClassLoader());
			ImmutableSet<ClassInfo> set = classPath.getTopLevelClasses(packageName);
			for (ClassInfo info : set) {
				Class<?> cls = info.load();
				if (cls == IYQZZAchievParser.class) {
					continue;
				}
				if (!IYQZZAchievParser.class.isAssignableFrom(cls)) {
					continue;
				}
				IYQZZAchievParser parse = (IYQZZAchievParser) cls.newInstance();
				map.put(parse.getYQZZAchieveType(), parse);
			}
			this.parserMap = ImmutableMap.copyOf(map);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	
	
	private IYQZZAchievParser getYQZZAchievParser(YQZZAchieveType type){
		return this.parserMap.get(type);
	}
	
	
	
	
	public List<YQZZAchieve> parserAchieve(Map<Integer,YQZZAchieve> achives){
		if(achives == null || achives.isEmpty()){
			return null;
		}
		List<YQZZAchieve> upates = new ArrayList<>();
		for(YQZZAchieve achieve : achives.values()){
			if(achieve.getState() != YQZZAchieveState.PROGRESS){
				continue;
			}
			try {
				int conType1 = achieve.getAchieveCfg().getConditionType1();
				List<Long> conValues1 = achieve.getAchieveCfg().getConditionValueList1();
				long targetValue1 = achieve.getAchieveCfg().getTargetValue1();
				YQZZAchieveType type1 =  YQZZAchieveType.valueOf(conType1);
				IYQZZAchievParser parser1 = this.getYQZZAchievParser(type1);
				YQZZAchievecomponent component1 = achieve.getComponent1();
				boolean update1 = false;
				if(parser1 != null && component1!= null){
					update1 = parser1.processAchieveOnUpdate(component1,conValues1,targetValue1);
				}
				
				int conType2 = achieve.getAchieveCfg().getConditionType2();
				List<Long> conValues2 = achieve.getAchieveCfg().getConditionValueList2();
				long targetValue2 = achieve.getAchieveCfg().getTargetValue2();
				YQZZAchieveType type2 =  YQZZAchieveType.valueOf(conType2);
				IYQZZAchievParser parser2 = this.getYQZZAchievParser(type2);
				YQZZAchievecomponent component2 = achieve.getComponent2();
				boolean update2 = false;
				if(parser2 != null && component2!= null){
					update2 = parser2.processAchieveOnUpdate(component2,conValues2,targetValue2);
				}
				boolean finish1 = component1 == null?true:component1.getState() == YQZZAchieveState.FINISH.getValue();
				boolean finish2 = component2 == null?true:component2.getState() == YQZZAchieveState.FINISH.getValue();
				if(finish1 && finish2){
					achieve.setState(YQZZAchieveState.FINISH);
					HawkLog.logPrintln("YQZZAchievManager achieve finish,playerId:{},achieveId:{},component1Vaule:{},component2Vaule:{}",
							achieve.getParent().getPlayerId(),
							achieve.getAchieveId(),
							component1 == null?0:component1.getValue(),
							component2 == null?0:component2.getValue());
				}
				if(update1 || update2){
					upates.add(achieve);
				}
			} catch (Exception e) {
				HawkException.catchException(e);
				continue;
			}
		}
		if(!upates.isEmpty()){
			return upates;
		}
		return null;
	}
	
}
