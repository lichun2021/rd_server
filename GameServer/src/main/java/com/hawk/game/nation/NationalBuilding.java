package com.hawk.game.nation;

import java.util.List;

import org.hawk.config.HawkConfigManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.game.GsApp;
import com.hawk.game.config.NationConstructionBaseCfg;
import com.hawk.game.config.NationConstructionLevelCfg;
import com.hawk.game.entity.NationConstructionEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.nation.construction.NationConstruction;
import com.hawk.game.nation.construction.comm.BuildCondtion;
import com.hawk.game.nation.hospital.NationalHospital;
import com.hawk.game.nation.mission.NationMissionCenter;
import com.hawk.game.nation.releation.NationReleationCenter;
import com.hawk.game.nation.ship.NationShipFactory;
import com.hawk.game.nation.space.NationSpaceFlight;
import com.hawk.game.nation.tech.NationTechCenter;
import com.hawk.game.nation.wearhouse.NationWearhouse;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.National.NationBuildingDetail;
import com.hawk.game.protocol.National.NationBuildingState;
import com.hawk.game.protocol.National.NationbuildingType;
import com.hawk.game.util.LogUtil;
import com.hawk.game.world.service.WorldPointService;

/**
 * 国家建筑基类
 * @author zhenyu.shang
 * @since 2022年3月22日
 */
public abstract class NationalBuilding {
	
	
	public static final Logger logger = LoggerFactory.getLogger("Server");
	/**
	 * 建筑类型
	 */
	protected NationbuildingType buildType;
	
	/**
	 * 建筑建设的entity
	 */
	protected NationConstructionEntity entity;
	
	
	public NationalBuilding(NationConstructionEntity entity, NationbuildingType buildType) {
		this.entity = entity;
		this.buildType = buildType;
	}
	
	/**
	 * 初始化
	 * @return
	 */
	public abstract boolean init();
	
	/**
	 * 升级完成
	 */
	public abstract void levelupOver();
	
	/**
	 * 开始升级
	 */
	public abstract void levelupStart();
	
	/**
	 * 当前状态是否可以升级
	 * @return
	 */
	public abstract boolean checkStateCanBuild();
	
	/**
	 * 运行状态下需要同步给世界点的状态的参数，（比如：飞船制造厂，当前是升级的那个部件）
	 * @return
	 */
	public String runningStateParam() {
		return "";
	}
	
	public void enterRunnigState() {
		this.setBuildState(NationBuildingState.RUNNING);
	}
	
	public void exitRunningState() {
		this.setBuildState(NationBuildingState.IDLE);
	}
	
	/**
	 * 开始升级，需要进入建设状态，并且开始读秒
	 */
	public boolean startlevelup() {
		// 这里再判断一下状态，是因为跨线程处理，如果同时出现研究或者运行，则不可升级
		if(!this.checkBuildCond()) {
			return false;
		}
		// 检查状态
		if(!this.checkStateCanBuild()){
			return false;
		}
		// 判断是否已经在建设中
		if(this.getBuildState() == NationBuildingState.BUILDING){
			return false;
		}
		
		int lvl = entity.getLevel(); // 初始为0级
		int baseId = buildType.getNumber() * 100 + (lvl + 1);
		
		NationConstructionLevelCfg cfg = HawkConfigManager.getInstance().getConfigByKey(NationConstructionLevelCfg.class, baseId);
		// 取不到证明已经到了最大等级，不可再升级
		if(cfg == null) {
			logger.error("[nation][building] building level up error, building is max level, lvl:{}", lvl);
			return false;
		}
		if(entity.getTotalVal() < cfg.getTotalBuildVal()){
			logger.error("[nation][building] building level up error, buildinglife not enough, curr:{}, need:{}", entity.getTotalVal(), cfg.getTotalBuildVal());
			return false;
		}
		// 减去需要升级的建设值
		this.entity.setTotalVal(entity.getTotalVal() - cfg.getTotalBuildVal());
		// 进入建设状态
		this.entity.setBuildingStatus(NationBuildingState.BUILDING_VALUE);
		// 设置建设时间
		this.entity.setBuildTime(HawkTime.getMillisecond() + cfg.getLevelUpTime() * 1000L);
		// 触发升级开始
		this.levelupStart();
		// 同步
		this.boardcastBuildState();
		
		LogUtil.logNationBuildingUpgradeStart(buildType.getNumber(), getEntity().getLevel(), getEntity().getTotalVal());
		return true;
	}
	
	/**
	 * tick
	 */
	public void tick() {
		try {
			long now = GsApp.getInstance().getCurrentTime();
			// 检查升级是否完成
			this.checkBuildingComplete(now);
			// 各个建筑的tick
			this.buildingTick(now);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 子类如果有tick需求可覆盖此类
	 */
	public void buildingTick(long now){
	}
	
	/**
	 * 停服处理
	 */
	public void onShutdown() {
	}
	
	/**
	 * 检查建筑是否升级完成
	 */
	private void checkBuildingComplete(long now){
		if(this.getBuildState() == NationBuildingState.BUILDING && this.entity.getBuildTime() > 0 && now >= this.entity.getBuildTime()) {
			// 设置建设时间
			this.entity.setBuildTime(0L);
			// 等级加1
			int lvl = this.entity.getLevel() + 1;
			// 设置新的等级
			this.entity.setLevel(lvl);
			// 进入常态
			this.entity.setBuildingStatus(NationBuildingState.IDLE_VALUE);
			// 建造完成
			this.levelupOver();
			// 同步
			this.boardcastBuildState();
			
			// 存入redis，用于跨服
			RedisProxy.getInstance().updateNationBuildLvl(buildType.getNumber(), lvl);
			
			LogUtil.logNationBuildingUpgradeEnd(buildType.getNumber(), getEntity().getLevel());
		}
	}
	
	
	public void boardcastBuildState(){
		// 同步状态到前端
		for (Player player : GlobalData.getInstance().getOnlinePlayers()) {
			player.sendProtocol(HawkProtocol.valueOf(HP.code2.NATIONAL_SINGLE_BUILD_STATUS_SYNC_VALUE, toBuilder()));
		}
		// 更新点信息
		NationConstructionBaseCfg cfg = getBaseCfg();
		WorldPointService.getInstance().notifyPointUpdate(cfg.getX(), cfg.getY());
	}
	
	/**
	 * 增加建设值
	 * @param val
	 * @param addDayVal 是否增加每日 （注：金条捐献为False，只能世界线程调用）
	 */
	public void addBuildingVal(int val, boolean addDayVal) {
		if(addDayVal) {
			int current = this.getEntity().getBuildVal();
			int limit = getBuildDayLimit();
			// 溢出了只加到上限
			if(current + val > limit){
				val = limit - current;
			}
			this.getEntity().setBuildVal(current + val);
		}
		// 增加总值
		this.entity.setTotalVal(this.entity.getTotalVal() + val);
	}
	
	/**
	 * 重置每日建设值
	 */
	public void resetBuildVal(){
		this.entity.setBuildVal(0);
	}
	
	
	public NationBuildingDetail.Builder toBuilder(){
		NationBuildingDetail.Builder builder = NationBuildingDetail.newBuilder();
		builder.setBtype(getBuildType());
		builder.setBstate(getBuildState());
		builder.setLevel(getEntity().getLevel());
		builder.setDayBuildVal(getEntity().getBuildVal());
		builder.setTotalBuildVal(getEntity().getTotalVal());
		builder.setBuildEndTime(getEntity().getBuildTime());
		builder.setRunningEndTime(getRunningEndTime());
		builder.setRunningTotalTime(getRunningTotalTime());
		return builder;
	}
	
	/**
	 * 构建国家建筑对象
	 * @param entity
	 * @param type
	 * @return
	 */
	public static NationalBuilding createNationalBuilding(NationConstructionEntity entity, NationbuildingType type){
		NationalBuilding building = null;
		switch (type) {
		case NATION_BUILDING_CENTER:
			building = new NationConstruction(entity, type);
			break;
		case NATION_HOSPITAL:
			building = new NationalHospital(entity, type);
			break;
		case NATION_QUEST_CENTER:
			building = new NationMissionCenter(entity, type);
			break;
		case NATION_RELATIONS:
			building = new NationReleationCenter(entity, type);
			break;
		case NATION_SHIP_FACTORY:
			building = new NationShipFactory(entity, type);
			break;
		case NATION_SPACE_FLIGHT:
			building = new NationSpaceFlight(entity, type);
			break;
		case NATION_TECH_CENTER:
			building = new NationTechCenter(entity, type);
			break;
		case NATION_WEARHOUSE:
			building = new NationWearhouse(entity, type);
			break;
		default:
			break;
		}
		
		return building;
	}

	public NationbuildingType getBuildType() {
		return buildType;
	}

	public NationConstructionEntity getEntity() {
		return entity;
	}
	
	public NationBuildingState getBuildState() {
		return NationBuildingState.valueOf(this.entity.getBuildingStatus());
	}
	
	public void setBuildState(NationBuildingState state){
		this.entity.setBuildingStatus(state.getNumber());
	}
	
	public long getRunningEndTime(){
		return 0L;
	}
	
	public long getRunningTotalTime(){
		return 0L;
	}
	
	public int getCurrentPercent(){
		NationConstructionLevelCfg nextCfg = getNextLevelCfg();
		if(nextCfg == null) { // 已经到最高了
			return -1;
		}
		int totalBuildVal = nextCfg.getTotalBuildVal();
		if(totalBuildVal == 0){
			return 0;
		}
		
		double per = ((this.entity.getTotalVal() * 1.0) / totalBuildVal);
		
		int res = (int) (per * 100);
		return res > 100 ? 100 : res;
	}
	
	public NationConstructionLevelCfg getNextLevelCfg(){
		int baseId = buildType.getNumber() * 100 + entity.getLevel() + 1;
		return HawkConfigManager.getInstance().getConfigByKey(NationConstructionLevelCfg.class, baseId);
	}
	
	/**
	 * 获取当前等级建设处配置
	 * @return
	 */
	public NationConstructionLevelCfg getCurrentLevelCfg() {
		int baseId = buildType.getNumber() * 100 + entity.getLevel();
		return HawkConfigManager.getInstance().getConfigByKey(NationConstructionLevelCfg.class, baseId);
	}
	
	/**
	 * 获取x等级配置
	 * @param level
	 * @return
	 */
	public NationConstructionLevelCfg getLevelCfg(int level) {
		int baseId = buildType.getNumber() * 100 + level;
		return HawkConfigManager.getInstance().getConfigByKey(NationConstructionLevelCfg.class, baseId);
	}
	
	public long getCurrentBuildEndTime(){
		return this.entity.getBuildTime();
	}
	
	public boolean isOpen() {
		NationConstructionBaseCfg cfg = HawkConfigManager.getInstance().getConfigByKey(NationConstructionBaseCfg.class, getEntity().getBuildingId() * 100);
		if(cfg == null){
			return false;
		}
		return cfg.getIsOpen() == 1;
	}
	
	public boolean checkBuildCond() {
		// 0-1 可以直接升
		if(this.entity.getLevel() == 0){
			return true;
		}
		// 判断条件
		NationConstructionLevelCfg cfg = getNextLevelCfg();
		if(cfg == null) {
			return false;
		}
		List<BuildCondtion> list = cfg.getBuildConds();
		if(list.isEmpty()) {
			return true;
		}
		for (BuildCondtion buildCond : list) {
			// 只要有一个条件不通过就算不过
			if(!buildCond.isMeetConditions(this)){
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 获取建筑
	 * @return
	 */
	public int getLevel() {
		return entity.getLevel();
	}
	
	/**
	 * 获取基础配置
	 * @return
	 */
	public NationConstructionBaseCfg getBaseCfg(){
		return HawkConfigManager.getInstance().getConfigByKey(NationConstructionBaseCfg.class, getBuildType().getNumber() * 100);
	}
	
	/**
	 * 获取当前建筑值上限
	 * @return
	 */
	public int getBuildDayLimit() {
		int limit = 0;
		// 获取当日最高上限
		int baseId = buildType.getNumber() * 100 + entity.getLevel();
		
		if(entity.getLevel() == 0){
			NationConstructionBaseCfg cfg = HawkConfigManager.getInstance().getConfigByKey(NationConstructionBaseCfg.class, baseId);
			limit = cfg.getDaylimit();
		} else {
			NationConstructionLevelCfg cfg = HawkConfigManager.getInstance().getConfigByKey(NationConstructionLevelCfg.class, baseId);
			limit = cfg.getDaylimit();
		}
		return limit;
	}
	
	
	public int getNationBuildingVal() {
		return getEntity().getTotalVal();
	}
}
