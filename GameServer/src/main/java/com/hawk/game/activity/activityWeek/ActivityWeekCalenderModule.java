package com.hawk.game.activity.activityWeek;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.hawk.game.module.lianmengyqzz.march.cfg.YQZZTimeCfg;
import com.hawk.game.module.lianmengyqzz.march.service.YQZZConst;
import com.hawk.game.module.lianmengyqzz.march.service.YQZZMatchService;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkTime;

import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.machineAwakeTwo.cfg.MachineAwakeTwoActivityTimeCfg;
import com.hawk.game.GsConfig;
import com.hawk.game.config.ActivityWeekCalenderCfg;
import com.hawk.game.config.ChampionshipConstCfg;
import com.hawk.game.config.ChampionshipTimeCfg;
import com.hawk.game.config.CrossTimeCfg;
import com.hawk.game.config.PresidentConstCfg;
import com.hawk.game.config.StarWarsConstCfg;
import com.hawk.game.config.StarWarsPartCfg;
import com.hawk.game.config.StarWarsTimeCfg;
import com.hawk.game.config.SuperWeaponConstCfg;
import com.hawk.game.config.TiberiumTimeCfg;
import com.hawk.game.config.WarCollegeTimeControlCfg;
import com.hawk.game.config.XHJZWarTimeCfg;
import com.hawk.game.crossactivity.CActivityInfo;
import com.hawk.game.crossactivity.CrossActivityService;
import com.hawk.game.manager.AssembleDataManager;
import com.hawk.game.module.dayazhizhan.playerteam.cfg.DYZZTimeCfg;
import com.hawk.game.module.lianmengfgyl.march.cfg.FGYLTimeCfg;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.president.PresidentCity;
import com.hawk.game.president.PresidentFightService;
import com.hawk.game.protocol.Activity.ActivityWeekCalendar;
import com.hawk.game.protocol.Activity.ActivityWeekCalendarElement;
import com.hawk.game.protocol.Activity.ActivityWeekCalendarInfoResp;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.President.PresidentPeriod;
import com.hawk.game.util.GameUtil;
/**
 * 活动周历
 * @author che
 *
 */
public class ActivityWeekCalenderModule  extends PlayerModule {

	public ActivityWeekCalenderModule(Player player) {
		super(player);
	}
	
	
	/**
	 * 获取活动周历信息
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.TAKE_ACTIVITY_WEEK_CALENDER_INFO_C_VALUE)
	public boolean onActivityWeekCalenderInfo(HawkProtocol protocol) {
		if (player.isCsPlayer()) {
			return false;
		}
		return syncToPlayer();
	}
	
	
	
	
	@Override
	protected boolean onPlayerLogin() {
		if (player.isCsPlayer()) {
			return false;
		}
		return syncToPlayer();
	}
	
	
	
	
	public boolean syncToPlayer(){
		ActivityWeekCalendar.Builder abuilder = this.loadActivityWeekCalendarInfo();
		ActivityWeekCalendarInfoResp.Builder rbuilder = ActivityWeekCalendarInfoResp.newBuilder();
		rbuilder.setWeekCalendar(abuilder);
		sendProtocol(HawkProtocol.valueOf(HP.code.PUSH_ACTIVITY_WEEK_CALENDER_INFO_SYNC_S_VALUE, rbuilder));
		return true;
	}
	
	public ActivityWeekCalendar.Builder loadActivityWeekCalendarInfo(){
		long startTime = HawkTime.getFirstDayOfCurWeek().getTime();
		long endTime = HawkTime.DAY_MILLI_SECONDS * 7  + startTime;
		ActivityWeekCalendar.Builder abuilder = this.getOpenActivityInfos(startTime, endTime);
		return abuilder;
	}
	
	
	
	
	public ActivityWeekCalendar.Builder getOpenActivityInfos(long startTime,long endTime){
		ActivityWeekCalendar.Builder abuilder = ActivityWeekCalendar.newBuilder();
		ConfigIterator<ActivityCfg> config = HawkConfigManager.getInstance().getConfigIterator(ActivityCfg.class);
		Map<Object,ActivityCfg> activityMap = config.toMap();
		ConfigIterator<ActivityWeekCalenderCfg> wcfg = HawkConfigManager.getInstance().
				getConfigIterator(ActivityWeekCalenderCfg.class);
		StringBuilder sb = new StringBuilder();
		for(ActivityWeekCalenderCfg cfg : wcfg){
			List<long[]> timeList = null;
			if(cfg.getId() == 1){
				//总统战
				timeList = this.presientOpenTimes(startTime,endTime);
			}else if(cfg.getId() == 2){
				//泰伯利亚
				timeList = this.tiberianOpenTimes(startTime, endTime);
			}else if(cfg.getId() == 5){
				//联合军演
				timeList = this.WarCollegeOpenTimes();
			}else if(cfg.getId() == 9){
				//统帅之战
				timeList = this.starWarOpenTimes(startTime, endTime);
			}else if(cfg.getId() == 38){
				//战区争夺
				timeList = this.superBarrackOpenTimes(startTime, endTime);
			}else if(cfg.getId() == 90){
				//航海远征
				timeList = this.crossVoyageOpenTimes(startTime, endTime);
			}else if(cfg.getId() == 129){
				//军团模拟战
				timeList = this.ChampionshipOpenTimes(startTime, endTime);
			}else if(cfg.getId() == 997){
				//反攻幽灵
				timeList = this.fgylOpenTimes(startTime, endTime);
			}else if(cfg.getId() == 998){
				timeList = this.yqzzOpenTimes(startTime, endTime);
			}else if(cfg.getId() == 999){
				timeList = this.xhjzOpenTimes(startTime, endTime);
			}else if(cfg.getId() == 1000){
				//军团模拟战
				timeList = this.dyzzOpenTimes(startTime, endTime);
			}else if(activityMap.containsKey(cfg.getId())){
				ActivityCfg acfg = activityMap.get(cfg.getId());
				timeList = acfg.getType().getTimeControl().getOpenTimes(startTime, endTime,acfg);
			}
			if(timeList == null || timeList.size() <= 0){
				continue;
			}
			for(long[] arr : timeList){
				ActivityWeekCalendarElement.Builder builder = ActivityWeekCalendarElement.newBuilder();
				builder.setActivityId(cfg.getId());
				builder.setStartTime(arr[0]);
				builder.setEndTime(arr[1]);
				this.addExtParam(builder, cfg.getId(), arr);
				abuilder.addElements(builder);
				sb.append(cfg.getId()+"_"+HawkTime.formatTime(arr[0])+"_"+HawkTime.formatTime(arr[1]));
			}
		}
		HawkLog.logPrintln("WeekOpenActivityInfos, playerId:{}, weekActivitys:{}", this.player.getId(), sb.toString());
		return abuilder;
	}
	
	
	
	
	public void addExtParam(ActivityWeekCalendarElement.Builder builder,int activityId,long[] cfgArr){
		if(activityId == ActivityType.MACHINE_AWAKE_TWO_ACTIVITY.intValue()){
			//机甲觉醒，幽灵行动 2合一活动。
			int termId = (int) cfgArr[2];
			MachineAwakeTwoActivityTimeCfg timeCfg = HawkConfigManager.getInstance().
					getConfigByKey(MachineAwakeTwoActivityTimeCfg.class, termId);
			String ep = String.valueOf(timeCfg.getNianType());
			builder.setExtParams(ep);
		}
	}
	
	public List<long[]> starWarOpenTimes(long startTime,long endTime){
		List<long[]> list = new ArrayList<long[]>();
		if (StarWarsConstCfg.getInstance().isSystemClose()) {
			return list;
		}
		String serverId = GsConfig.getInstance().getServerId();			
		StarWarsPartCfg starWarsCfg = AssembleDataManager.getInstance().getServerPartCfg(serverId);				
		if(starWarsCfg == null){
			return list;
		}
		
		ConfigIterator<StarWarsTimeCfg> its = HawkConfigManager.getInstance().
				getConfigIterator(StarWarsTimeCfg.class);
		for (StarWarsTimeCfg timeCfg : its) {
			long startCfgOne = timeCfg.getWarStartTimeOneValue();
			long endCfgOne = timeCfg.getWarEndTimeOneValue();
			
			long startCfgTwo = timeCfg.getWarStartTimeTwoValue();
			long endCfgTwo = timeCfg.getWarEndTimeTwoValue();
			boolean cfgOne = true;
			boolean cfgTwo = true;
			if (startCfgOne >=  endTime || startTime >= endCfgOne) {
				cfgOne = false;
			}
			if (startCfgTwo >=  endTime || startTime >= endCfgTwo) {
				cfgTwo = false;
			}
			
			if(cfgOne){
				long[] arr = new long[]{startCfgOne,endCfgOne};
				list.add(arr);
			}
			if(cfgTwo){
				long[] arr = new long[]{startCfgTwo,endCfgTwo};
				list.add(arr);
			}
		}
		return list;
	}
	
	public List<long[]> superBarrackOpenTimes(long startTime,long endTime){
		long serverOpenTime = GameUtil.getServerOpenTime();
		SuperWeaponConstCfg constCfg = SuperWeaponConstCfg.getInstance();
		// 计算基准时间
		long baseTime = serverOpenTime + (constCfg.getInitPeaceTime() * 1000L);
		baseTime = Math.max(HawkTime.getMillisecond(), baseTime);
		long startCfg = HawkTime.getNextTimeDayOfWeek(baseTime, constCfg.getInitday(), constCfg.getInitTime(), constCfg.getInitMinute());
		long endCfg = startCfg + constCfg.getWarfareTime() * 1000L;
		List<long[]> list = new ArrayList<long[]>();
		// 开始  结束 配置开启     配置 结束  
		if (startCfg >=  endTime) {
			return list;
		}
		//配置开启     配置结束   开始  结束 
		if (startTime >= endCfg) {
			return list;
		}
		
		long[] arr = new long[2];
		arr[0] = startCfg;
		arr[1] = endCfg;
		list.add(arr);
		return list;
		
	}
	
	public List<long[]> presientOpenTimes(long startTime,long endTime){
		List<long[]> list = new ArrayList<long[]>();
		List<long[]> times = new ArrayList<long[]>();
		PresidentCity percity = PresidentFightService.getInstance().getPresidentCity();
		PresidentConstCfg constCfg = PresidentConstCfg.getInstance();
		
		long lastFightTick = percity.getLastTickTime();
		//和平和初始时期
		if(percity.getStatus() == PresidentPeriod.INIT_VALUE || 
				percity.getStatus() == PresidentPeriod.PEACE_VALUE){
			long startCfg = percity.getStartTime();
			long endCfg = startCfg + constCfg.getWarfareTime() * 1000L;
			long[] arr = new long[]{startCfg,endCfg};
			times.add(arr);
			//上一次进入WARFARE  和 OVERTIME 阶段最后心跳时间 是否是在本周
			if(lastFightTick < endTime && lastFightTick > startTime){
				long zero = HawkTime.getFirstDayOfWeek(lastFightTick).getTime();
				long fstartCfg = HawkTime.getNextTimeDayOfWeek(zero, constCfg.getInitday(), 
						constCfg.getInitTime(), constCfg.getInitMinute());
				long fendCfg = fstartCfg + constCfg.getWarfareTime() * 1000;
				long[] farr = new long[]{fstartCfg,fendCfg};
				times.add(farr);
			}
		}
		//战争时期
		if(percity.getStatus() == PresidentPeriod.WARFARE_VALUE || 
				percity.getStatus() == PresidentPeriod.OVERTIME_VALUE){
			//当天的时间点
			long zero = HawkTime.getAM0Date().getTime();
			long startCfg = zero + constCfg.getInitTime()*HawkTime.HOUR_MILLI_SECONDS+ 
					constCfg.getInitMinute()*HawkTime.HOUR_MILLI_SECONDS;
			long endCfg = startCfg + constCfg.getWarfareTime() * 1000;
			long[] arr = new long[]{startCfg,endCfg};
			times.add(arr);
			
		}
		
		for(long[] arr : times){
			long startCfg = arr[0];
			long endCfg = arr[1];
			// 开始  结束 配置开启     配置 结束  
			if (startCfg >=  endTime) {
				continue;
			}
			//配置开启     配置结束   开始  结束 
			if (startTime >= endCfg) {
				continue;
			}
			list.add(arr);
		}
	
		return list;
	}
	
	
	
	
	
	public List<long[]> ChampionshipOpenTimes(long startTime,long endTime){
		ConfigIterator<ChampionshipTimeCfg> its = HawkConfigManager.getInstance().getConfigIterator(ChampionshipTimeCfg.class);
		String serverId = GsConfig.getInstance().getServerId();
		Long mergeTime =  AssembleDataManager.getInstance().getServerMergeTime(serverId);

		long serverOpenAm0 = HawkTime.getAM0Date(new Date(GameUtil.getServerOpenTime())).getTime();
		long serverDelay = ChampionshipConstCfg.getInstance().getServerDelay();
		List<long[]> list = new ArrayList<long[]>();
		for (ChampionshipTimeCfg timeCfg : its) {
			List<String> limitServerLimit = timeCfg.getLimitServerList();
			List<String> forbidServerLimit = timeCfg.getForbidServerList();
			
			// 开服时间不满足
			if (serverOpenAm0 + serverDelay > timeCfg.getSignStartTimeValue()) {
				continue;
			}
			
			// 合服时间在活动期间内,则不参与本次锦标赛
			if (mergeTime != null && mergeTime >= timeCfg.getShowStartTimeValue() &&
					mergeTime <= timeCfg.getHiddenTimeValue()) {
				continue;
			}
			
			// 开启判定,如果没有开启区服限制,或者本期允许本服所在区组开放
			if (!limitServerLimit.isEmpty() && !limitServerLimit.contains(serverId) ) {
				continue;
			}
			
			if (!forbidServerLimit.isEmpty() && forbidServerLimit.contains(serverId) ) {
				continue;
			}
			
		
			long startCfg = timeCfg.getWarStartTimeValue();
			long endCfg = timeCfg.getEndStartTimeValue();
			boolean isOpne = true;
			// 开始  结束 配置开启     配置 结束  
			if (startCfg >=  endTime) {
				isOpne =false;
			}
			//配置开启     配置结束   开始  结束 
			if (startTime >= endCfg) {
				isOpne =false;
			}
			if(isOpne){
				long[] arr = new long[2];
				arr[0] = startCfg;
				arr[1] = endCfg;
				list.add(arr);
			}
			
		}
		return list;
	}

	
	public List<long[]> crossVoyageOpenTimes(long startTime,long endTime){
		ConfigIterator<CrossTimeCfg> it = HawkConfigManager.getInstance().getConfigIterator(CrossTimeCfg.class);
		Integer crossId = AssembleDataManager.getInstance().getCrossServerCfgId(GsConfig.getInstance().getServerId());
		List<long[]> list = new ArrayList<long[]>();
		CrossTimeCfg lastCfg = null;
		for (CrossTimeCfg timeCfg : it) {
			long startCfg = timeCfg.getStartTimeValue();
			long endCfg = timeCfg.getEndTimeValue();
			boolean isOpne = true;
			// 开始  结束 配置开启     配置 结束  
			if (startCfg >=  endTime) {
				isOpne =false;
			}
			//配置开启     配置结束   开始  结束 
			if (startTime >= endCfg) {
				isOpne =false;
			}
			if(isOpne){
				List<Integer> groupLimit = timeCfg.getLimitGroupList();
				boolean rlt1 = groupLimit.isEmpty()|| !groupLimit.contains(crossId);
				boolean rlt2 = CrossActivityService.getInstance().checkServerMergeTime(timeCfg, lastCfg);
				boolean rlt = rlt1 && rlt2;
				if(rlt){
					long[] arr = new long[2];
					arr[0] = startCfg;
					arr[1] = endCfg;
					list.add(arr);
				}
			}
			lastCfg = timeCfg;
		}
		return list;
	}

	public List<long[]> yqzzOpenTimes(long startTime,long endTime){
		ConfigIterator<YQZZTimeCfg> it = HawkConfigManager.getInstance().getConfigIterator(YQZZTimeCfg.class);
		List<long[]> list = new ArrayList<>();
		for (YQZZTimeCfg timeCfg : it) {
			long startCfg = timeCfg.getBattleTimeValue();
			long endCfg = timeCfg.getRewardTimeValue();
			boolean isOpne = true;
			YQZZConst.YQZZActivityJoinState joinState = YQZZMatchService.getInstance().getDataManger().getStateData().getJoinGame();
			if(joinState == YQZZConst.YQZZActivityJoinState.OUT){
				isOpne = false;
			}
			// 开始  结束 配置开启     配置 结束
			if (startCfg >=  endTime) {
				isOpne =false;
			}
			//配置开启     配置结束   开始  结束
			if (startTime >= endCfg) {
				isOpne =false;
			}
			if(isOpne){
				long[] arr = new long[2];
				arr[0] = startCfg;
				arr[1] = endCfg;
				list.add(arr);
			}
		}
		return list;
	}
	
	
	public List<long[]> tiberianOpenTimes(long startTime,long endTime){
		ConfigIterator<TiberiumTimeCfg> it = HawkConfigManager.getInstance().getConfigIterator(TiberiumTimeCfg.class);
		String serverId = GsConfig.getInstance().getServerId();
		List<long[]> list = new ArrayList<long[]>();
		for (TiberiumTimeCfg timeCfg : it) {
			List<String> limitServerLimit = timeCfg.getLimitServerList();
			List<String> forbidServerLimit = timeCfg.getForbidServerList();
			if (!limitServerLimit.isEmpty() && !limitServerLimit.contains(serverId)) {
				continue;
			}
			if(!forbidServerLimit.isEmpty() && forbidServerLimit.contains(serverId)){
				continue;
			}
			long startCfg = timeCfg.getWarStartTimeValue();
			long endCfg = timeCfg.getWarEndTimeValue();
			boolean isOpne = true;
			// 开始  结束 配置开启     配置 结束  
			if (startCfg >=  endTime) {
				isOpne =false;
			}
			//配置开启     配置结束   开始  结束 
			if (startTime >= endCfg) {
				isOpne =false;
			}
			if(isOpne){
				long[] arr = new long[2];
				arr[0] = startCfg;
				arr[1] = endCfg;
				list.add(arr);
			}
		}
		return list;
	}
	
	public List<long[]> WarCollegeOpenTimes(){
		WarCollegeTimeControlCfg wcfig = HawkConfigManager.getInstance().
				getKVInstance(WarCollegeTimeControlCfg.class);
		List<long[]> list = new ArrayList<long[]>();
		int[] days = wcfig.getDaysArray();
		int[] startArr = wcfig.getStartTimeArray();
		int[] endArr = wcfig.getEndTimeArray();
		long startTime = HawkTime.getFirstDayOfCurWeek().getTime();
		for(int day : days){
			long astartTime = HawkTime.getNextTimeDayOfWeek(startTime, day, startArr[0], startArr[1]);
			long endTime =  HawkTime.getNextTimeDayOfWeek(startTime, day, endArr[0], endArr[1]);
			long[] arr = new long[2];
			arr[0] = astartTime;
			arr[1] = endTime;
			list.add(arr);
		}
		return list;
	}
	

	
	
	public List<long[]> dyzzOpenTimes(long startTime,long endTime){
		ConfigIterator<DYZZTimeCfg> it = HawkConfigManager.getInstance().getConfigIterator(DYZZTimeCfg.class);
		List<long[]> list = new ArrayList<long[]>();
		for (DYZZTimeCfg timeCfg : it) {
			long startCfg = timeCfg.getStartTimeValue();
			long endCfg = timeCfg.getEndTimeValue();
			boolean isOpne = true;
			// 开始  结束 配置开启     配置 结束  
			if (startCfg >=  endTime) {
				isOpne =false;
			}
			//配置开启     配置结束   开始  结束 
			if (startTime >= endCfg) {
				isOpne =false;
			}
			if(isOpne){
				long[] arr = new long[2];
				arr[0] = startCfg;
				arr[1] = endCfg;
				list.add(arr);
			}
		}
		return list;
	}
	
	
	public List<long[]> xhjzOpenTimes(long startTime,long endTime){
		ConfigIterator<XHJZWarTimeCfg> it = HawkConfigManager.getInstance().getConfigIterator(XHJZWarTimeCfg.class);
		List<long[]> list = new ArrayList<long[]>();
		for (XHJZWarTimeCfg timeCfg : it) {
			long startCfg = timeCfg.getBattleTime();
			long endCfg = timeCfg.getSettleTime();
			boolean isOpne = true;
			// 开始  结束 配置开启     配置 结束  
			if (startCfg >=  endTime) {
				isOpne =false;
			}
			//配置开启     配置结束   开始  结束 
			if (startTime >= endCfg) {
				isOpne =false;
			}
			if(isOpne){
				long[] arr = new long[2];
				arr[0] = startCfg;
				arr[1] = endCfg;
				list.add(arr);
			}
		}
		return list;
	}
	
	
	public List<long[]> fgylOpenTimes(long startTime,long endTime){
		ConfigIterator<FGYLTimeCfg> it = HawkConfigManager.getInstance().getConfigIterator(FGYLTimeCfg.class);
		List<long[]> list = new ArrayList<long[]>();
		for (FGYLTimeCfg timeCfg : it) {
			long startCfg = timeCfg.getStartTimeValue();
			long endCfg = timeCfg.getEndTimeValue();
			boolean isOpne = true;
			// 开始  结束 配置开启     配置 结束  
			if (startCfg >=  endTime) {
				isOpne =false;
			}
			//配置开启     配置结束   开始  结束 
			if (startTime >= endCfg) {
				isOpne =false;
			}
			if(isOpne){
				long[] arr = new long[2];
				arr[0] = startCfg;
				arr[1] = endCfg;
				list.add(arr);
			}
		}
		return list;
	}
}
