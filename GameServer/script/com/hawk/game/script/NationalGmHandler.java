package com.hawk.game.script;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.redis.HawkRedisSession;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;

import com.hawk.game.config.BattleSoldierCfg;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.msg.CalcDeadArmy;
import com.hawk.game.nation.NationService;
import com.hawk.game.nation.NationalBuilding;
import com.hawk.game.nation.hospital.module.NationalHospitalModule;
import com.hawk.game.nation.tech.NationTechCenter;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.National.NationBuildingState;
import com.hawk.game.protocol.National.NationStatus;
import com.hawk.game.protocol.National.NationbuildingType;
import com.hawk.game.protocol.Script.ScriptError;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;

/**
 * 国家系统GM命令
 * @author zhenyu.shang
 * @since 2022年4月13日
 */
public class NationalGmHandler extends HawkScript  {

	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		String opType = params.get("op");
		if (HawkOSOperator.isEmptyString(opType)) {
			return HawkScript.failedResponse(ScriptError.PARAMS_ERROR_VALUE, "param op needed");
		}
		
		// 修改国家状态
		if (opType.equals("1")) {
			return changeBuildingState(params);
		}
		// 直接完成建筑升级
		if (opType.equals("2")) {
			return buildingLvupComplete(params);
		}
		// 增加建筑值
		if (opType.equals("3")) {
			return addNationBuildingVal(params);
		}
		// 增加科技值
		if (opType.equals("4")) {
			return addNationTechVal(params);
		}
		
		// 修改国家医院中士兵的数量
		if (opType.equals("changeHospitalArmy")) {
			return changeHospitalArmy(params);
		}
		
		return HawkScript.successResponse(null);
	}
	
	/**
	 * 修改国家状态
	 * @param params
	 * @return
	 */
	private String changeBuildingState(Map<String, String> params) {
		String status = params.get("status");
		NationStatus ns = NationStatus.valueOf(Integer.parseInt(status));
		if(ns == null){
			return HawkScript.failedResponse(ScriptError.PARAMS_ERROR_VALUE, "nation status error");
		}
		
		if(NationService.getInstance().getNationStatus() == NationStatus.UNOPEN || NationService.getInstance().getNationStatus() == NationStatus.OPEN){
			NationService.getInstance().initNationalBuildingPoint(false);
		}
		NationService.getInstance().setNationStatus(ns, "GM change");
		NationService.getInstance().boardcastNationalStatus();
		return HawkScript.successResponse(null);
	}
	
	/**
	 * 直接完成建筑升级
	 * 
	 * @param params
	 * @return
	 */
	private String buildingLvupComplete(Map<String, String> params) {
		String type = params.get("type");
		NationbuildingType nationbuildingType = NationbuildingType.valueOf(Integer.parseInt(type));
		if(nationbuildingType == null){
			return HawkScript.failedResponse(ScriptError.PARAMS_ERROR_VALUE, "nation build type error");
		}
		
		NationalBuilding building = NationService.getInstance().getNationBuildingByType(nationbuildingType);
		
		if(building.getBuildState() == NationBuildingState.BUILDING){
			// 设置建设时间
			building.getEntity().setBuildTime(0L);
			// 等级加1
			int lvl = building.getEntity().getLevel() + 1;
			// 设置新的等级
			building.getEntity().setLevel(lvl);
			// 进入常态
			building.getEntity().setBuildingStatus(NationBuildingState.IDLE_VALUE);
			// 建造完成
			building.levelupOver();
			// 同步
			building.boardcastBuildState();
		}
		
		return HawkScript.successResponse(null);
	}
	
	/**
	 * 增加建筑值
	 * @param params
	 * @return
	 */
	private String addNationBuildingVal(Map<String, String> params) {
		String type = params.get("type");
		NationbuildingType nationbuildingType = NationbuildingType.valueOf(Integer.parseInt(type));
		if(nationbuildingType == null){
			return HawkScript.failedResponse(ScriptError.PARAMS_ERROR_VALUE, "nation build type error");
		}
		String val = params.get("val");
		String day = params.getOrDefault("day", "false");
		
		NationalBuilding building = NationService.getInstance().getNationBuildingByType(nationbuildingType);
		building.addBuildingVal(Integer.parseInt(val), Boolean.parseBoolean(day));
		return HawkScript.successResponse(null);
	}
	
	/**
	 * 增加科技值
	 * 
	 * @param params
	 * @return
	 */
	private String addNationTechVal(Map<String, String> params) {
		NationTechCenter techCenter = (NationTechCenter)NationService.getInstance().getNationBuildingByType(NationbuildingType.NATION_TECH_CENTER);
		if (techCenter != null) {
			int addTech = Integer.parseInt(params.get("addTech"));
			techCenter.changeNationTechValue(addTech);
		}
		
		return HawkScript.successResponse(null);
	}
	
	/**
	 * 修改国家医院中兵力的数量
	 * 
	 * @param params
	 * @return
	 */
	private String changeHospitalArmy(Map<String, String> params) {
		if (!params.containsKey("armyInfo")) {
			return HawkScript.failedResponse(ScriptError.PARAMS_ERROR_VALUE, "param armyInfo missed");
		}

		Player player = GlobalData.getInstance().scriptMakesurePlayer(params);
		if (player == null) {
			return HawkScript.failedResponse(ScriptError.PARAMS_ERROR_VALUE, "account not found");
		}
		
		List<ArmyInfo> armyDeadList = new ArrayList<ArmyInfo>();
		String armyInfos = params.get("armyInfo");
		for (String info : armyInfos.split(",")) {
			String[] armyInfoArr = info.split("_");
			int armyId = Integer.parseInt(armyInfoArr[0]);
			BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, armyId);
			if (cfg == null) {
				return HawkScript.failedResponse(ScriptError.PARAMS_ERROR_VALUE, "armyInfo param error");
			}
			int count = Integer.parseInt(armyInfoArr[1]);
			if (count <= 0) {
				continue;
			}
			ArmyInfo armyInfo = new ArmyInfo();
			armyInfo.setArmyId(armyId);
			armyInfo.setDeadCount(count);
			armyDeadList.add(armyInfo);
		}
		
		if (armyDeadList.isEmpty()) {
			return HawkScript.failedResponse(ScriptError.PARAMS_ERROR_VALUE, "change army none");
		}
		
		if (!GameUtil.isWin32Platform(player)) {
			String redisKey = "changeHospitalArmy:" + player.getId();
			HawkRedisSession redisSession = RedisProxy.getInstance().getRedisSession();
			boolean redisMarkSetSucc = redisSession.setNx(redisKey, HawkTime.formatNowTime());
			if (!redisMarkSetSucc) {
				HawkLog.errPrintln("change army repeated nearly, playerId: {}", player.getId());
				return HawkScript.failedResponse(ScriptError.PARAMS_ERROR_VALUE, "change army repeated nearly");
			}
			
			// 10分钟过期
			redisSession.expire(redisKey, 600);
		}
		
		int threadIdx = player.getXid().getHashThread(HawkTaskManager.getInstance().getThreadNum());
		HawkTaskManager.getInstance().postTask(new HawkTask() {
			@Override
			public Object run() {
				NationalHospitalModule module = player.getModule(GsConst.ModuleType.NATIONAL_HOSPITAL);
				Method method = module.getMsgMethod(CalcDeadArmy.class, "");
				try {
					CalcDeadArmy msg = CalcDeadArmy.valueOf(armyDeadList);
					msg.setGmTrigger(true);
					method.invoke(module, msg);
				} catch (Exception e) {
					HawkException.catchException(e);
				}
				
				return null;
			}
		}, threadIdx);
		
		return HawkScript.successResponse(null);
	}

}
