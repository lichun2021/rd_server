package com.hawk.game.guild.manor.building;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.hawk.config.HawkConfigManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.config.GuildManorMineCfg;
import com.hawk.game.entity.GuildBuildingEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.guild.manor.GuildBuildingStat;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.player.Player;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.player.supersoldier.SuperSoldier;
import com.hawk.game.protocol.Common.KeyValuePairInt;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.TerritoryType;
import com.hawk.game.protocol.GuildManor.GuildBuildingNorStat;
import com.hawk.game.protocol.GuildManor.GuildManorList.Builder;
import com.hawk.game.protocol.GuildManor.GuildSuperMineBase;
import com.hawk.game.protocol.GuildManor.GuildSuperMineType;
import com.hawk.game.protocol.GuildManor.ManorPlayerInfo;
import com.hawk.game.protocol.GuildManor.ManorPlayerInfoList;
import com.hawk.game.protocol.GuildManor.PanelState;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.service.GuildManorService;
import com.hawk.game.service.GuildService;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.LogUtil;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.log.LogConst.GuildAction;

/**
 * 联盟超级矿
 * @author zhenyu.shang
 * @since 2017年7月7日
 */
public class GuildManorSuperMine extends AbstractGuildBuilding{
	
	public final static int RADIUS = 2;
	
	public final static String RESOURCE_NUM_KEY = "1";
	
	public final static String MARCHID_KEY = "2";
	
	private GuildSuperMineType mineType;
	
	private BlockingQueue<String> marchs;
	
	private long resourceNum;
	
	private int resType;
	
	/** 上一轮tick的余数 */
	private int lastRemainder = 0;
	
	public GuildManorSuperMine(GuildBuildingEntity entity, TerritoryType buildType, GuildSuperMineType mineType) {
		super(entity, buildType);
		this.mineType = mineType;
		this.marchs = new LinkedBlockingQueue<String>();
		GuildManorMineCfg cfg = getCfg();
		this.resType = cfg.getResType();
	}
	
	public GuildManorMineCfg getCfg(){
		return HawkConfigManager.getInstance().getConfigByKey(GuildManorMineCfg.class, getEntity().getBuildingId());
	}

	@Override
	public void addProtocol2Builder(Builder builder) {
		GuildSuperMineBase.Builder mineBuilder = GuildSuperMineBase.newBuilder();
		mineBuilder.setType(mineType);
		mineBuilder.setResouceNum(getResourceNum());
		mineBuilder.setBuildLife((int) getEntity().getBuildLife());
		mineBuilder.setLevel(getEntity().getLevel());
		mineBuilder.setOverTime(getOverTime());
		mineBuilder.setStat(GuildBuildingNorStat.valueOf(getEntity().getBuildingStat()));
		mineBuilder.setX(getEntity().getPosX());
		mineBuilder.setY(getEntity().getPosY());
		
		builder.addAllSuperMine(mineBuilder.build());
	}

	public GuildSuperMineType getMineType() {
		return mineType;
	}

	public void setMineType(GuildSuperMineType mineType) {
		this.mineType = mineType;
	}
	
	public void addCollectMarch(String marchId){
		this.marchs.add(marchId);
		this.getEntity().setChanged(true);
	}
	
	public void removeCollectMarch(String marchId){
		this.marchs.remove(marchId);
		this.getEntity().setChanged(true);
	}
	
	/**
	 * 情况并遣返所有部队
	 */
	public void clearCollectMarch(){
		this.marchs.clear();
		this.getEntity().setChanged(true);
	}

	@Override
	public void heartBeats(long interval) {
		//如果没有行军了。改变状态
		if(checkMarchEmpty(marchs)){
			return;
		}
		//判断还有没有矿
		if(resourceNum <= 0){
			int posX = getEntity().getPosX();
			int posY = getEntity().getPosY();
			//先从地图中移除建筑
			GuildManorService.getInstance().removeManorBuilding(this);
			//广播状态变化
			WorldPointService.getInstance().notifyPointUpdate(posX, posY);
			GuildManorService.getInstance().broadcastGuildBuilding(getGuildId());
			GuildManorService.logger.info("Guild mine has been exhaustion , will be remove id:{}, type:{}, x:{}, y{}", getEntity().getId(), getMineType(), posX, posY);
			return;
		}
		//此处为了计算精准, 需要对每次间隔时间进行精确计算, 确定为每秒一次,余数存入下次加入使用
		int runTimes = (int) ((interval + lastRemainder) / PERIOD);
		this.lastRemainder = (int) ((interval + lastRemainder) % PERIOD);
		if(runTimes > 1){
			GuildManorService.logger.warn("runTimes is too much , runTime={}, lastRemainder={}, interval={}, resource={}", runTimes, lastRemainder, interval, resourceNum);
		}
		long now = HawkTime.getMillisecond();
		for (String marchId : marchs) {//计算所有部队挖矿情况
			try {
				//矿如果挖完了，直接退出
				if(resourceNum <= 0){
					break;
				}
				//先检查行军存不存在
				if(!WorldMarchService.getInstance().checkMarchExist(marchId)){
					this.removeCollectMarch(marchId);
					GuildManorService.logger.error("world march collect resource, march has already remove !!!");
					continue;
				}
				IWorldMarch march = WorldMarchService.getInstance().getMarch(marchId);
				//先判断目标点是不是，如果目标点不是，则直接遣返
				if(march.getMarchEntity().getTerminalId() != this.getPositionId()){
					this.removeCollectMarch(marchId);
					GuildManorService.logger.error("world march collect resource, point error, targetpoint is not this point, this point : {}, march:{}", this.getEntity().getPos(), march);
					continue;
				}
				//如果是回程状态,则移除此行军
				if(march.getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_RETURN_BACK_VALUE){
					this.removeCollectMarch(marchId);
					GuildManorService.logger.error("world march collect resource, status error : {}", march);
					continue;
				}
				//先判断行军状态，没有到达则不往下执行
				if(march.getMarchEntity().getMarchStatus() != WorldMarchStatus.MARCH_STATUS_MARCH_COLLECT_VALUE){
					continue;
				}
				// 获取玩家对象
				Player player = GlobalData.getInstance().makesurePlayer(march.getPlayerId());
				//获取已经采集
				long res = 0;
				AwardItems items = march.getMarchEntity().getAwardItems();
				for (ItemInfo itemInfo : items.getAwardItems()) {
					res += itemInfo.getCount();
				}
				//获取负重
				long loadNum = WorldMarchService.getInstance().getArmyCarryResNum(player, march.getMarchEntity().getArmys(), resType, march.getMarchEntity().getEffectParams());
				if(res >= loadNum){ //已经采满了
					WorldMarchService.getInstance().resetCollectResource(march.getMarchEntity(), (int) loadNum, resType);
					this.removeCollectMarch(marchId);
					WorldMarchService.getInstance().onPlayerNoneAction(march, now);
					//推送面板消息
					player.sendProtocol(HawkProtocol.valueOf(HP.code.MANOR_MINE_COLLECT_LIST_S_VALUE, makeSuperMineBuilder(player)));
					continue;
				}
				
				//获取当前速度
				double speed = march.getMarchEntity().getCollectSpeed() * (1 + getCfg().getCollectSpeed() * GsConst.EFF_PER);
				
				long sum = (long)(speed * runTimes);
				if (sum == 0) {
					sum = 1;
				}
				
				//此次挖到的矿
				long collect = resourceNum > sum ? sum : resourceNum;
				//矿总数减少
				this.resourceNum -= collect;
				//计算总量
				long sumCollect = Math.min(loadNum, collect + res);
				//重置采集
				WorldMarchService.getInstance().resetCollectResource(march.getMarchEntity(), sumCollect, resType);
				//判断设置开始时间和结束时间
				if(march.getMarchEntity().getResStartTime() == 0){
					march.getMarchEntity().setResStartTime(now);
					march.getMarchEntity().setResEndTime(now + (long)((loadNum/speed) * 1000));
					march.updateMarch();
				} else {
					//判断结束时间变化后需要更新
					long endTime = march.getMarchEntity().getResEndTime();
					long newEndTime = now + (long)(((loadNum - sumCollect)/speed) * 1000);
					//此处算剩余时间, 需要计算剩余的采集量除以速度 + 当前时间
					//由于心跳本身取当前时间有误差,所以此处判断误差超过3秒, 才进行更新, 普通的负重或速度变换, 不会小于3秒
					if(endTime != newEndTime && Math.abs(endTime - newEndTime) > 3000){
						march.getMarchEntity().setResEndTime(newEndTime);
						march.updateMarch();
					}
				}
			} catch (Exception e) {
				HawkException.catchException(e, marchId);
			}
		}
		//心跳之后需存库
		getEntity().setChanged(true);
	}

	public long getResourceNum() {
		return resourceNum;
	}

	public void setResourceNum(long resourceNum) {
		this.resourceNum = resourceNum;
		getEntity().setChanged(true);
	}
	
	/**
	 * 获取超级矿个人采集速度
	 * 此处计算的是每分钟的采集量, 只可用于显示, 不可用于计算, 切记切记
	 * @return
	 */
	public long getCollectSpeed(Player player){
		double baseSpeed = 0L;
		double totalSpeed = 0L;
		
		BigDecimal effectDecimal = new BigDecimal(Double.toString(getCfg().getCollectSpeed() * GsConst.EFF_PER));
		BigDecimal minute = new BigDecimal(Double.toString(60));
		
		for (String marchId : marchs) {//计算所有部队挖矿情况
			IWorldMarch march = WorldMarchService.getInstance().getMarch(marchId);
			if(march == null){
				this.removeCollectMarch(marchId);
				continue;
			}
			//先判断行军状态，没有到达则不往下执行
			if(march.getMarchEntity().getMarchStatus() != WorldMarchStatus.MARCH_STATUS_MARCH_COLLECT_VALUE){
				continue;
			}
			if (!player.getId().equals(march.getPlayerId())) {
				continue;
			}
			BigDecimal baseSpeedDecimal = new BigDecimal(Double.toString(march.getMarchEntity().getCollectBaseSpeed()));
			BigDecimal totalSpeedDecimal = new BigDecimal(march.getMarchEntity().getCollectSpeed());
			
			baseSpeed += baseSpeedDecimal.add(baseSpeedDecimal.multiply(effectDecimal)).multiply(minute).doubleValue();
			totalSpeed += totalSpeedDecimal.add(totalSpeedDecimal.multiply(effectDecimal)).multiply(minute).doubleValue();
		}
		
		long res = ((long)(baseSpeed) << 32) | (long)((totalSpeed - baseSpeed));
		return res;
	}
	
	/**
	 * 获取超级矿总的采集速度
	 * @return
	 */
	public int getCollectTotalSpeed() {
		double totalSpeed = 0;
		for (String marchId : marchs) {// 计算所有部队挖矿情况
			IWorldMarch march = WorldMarchService.getInstance().getMarch(marchId);
			if(march == null){
				this.removeCollectMarch(marchId);
				continue;
			}
			// 先判断行军状态，没有到达则不往下执行
			if (march.getMarchEntity().getMarchStatus() != WorldMarchStatus.MARCH_STATUS_MARCH_COLLECT_VALUE) {
				continue;
			}
			double speed =  march.getMarchEntity().getCollectSpeed() * (1 + getCfg().getCollectSpeed() * GsConst.EFF_PER);
			totalSpeed = totalSpeed + speed;
		}
		return (int) Math.round(totalSpeed);
	}
	
	/**
	 * 获取己方部队的已采集量
	 * @param player
	 * @return
	 */
	public long getSelfCollect(Player player) {
		long resCount = 0;
		for (String marchId : marchs) {// 计算所有部队挖矿情况
			IWorldMarch march = WorldMarchService.getInstance().getMarch(marchId);
			if(march == null){
				this.removeCollectMarch(marchId);
				continue;
			}
			if (!player.getId().equals(march.getPlayerId())) {
				continue;
			}
			// 先判断行军状态，没有到达则不往下执行
			if (march.getMarchEntity().getMarchStatus() != WorldMarchStatus.MARCH_STATUS_MARCH_COLLECT_VALUE) {
				continue;
			}
			AwardItems items = march.getMarchEntity().getAwardItems();
			for (ItemInfo itemInfo : items.getAwardItems()) {
				resCount += itemInfo.getCount();
			}
			return resCount;
		}
		return 0;
	}
	
	/**
	 * 获取己方部队的负重
	 * @param player
	 * @return
	 */
	public long getSelfLoad(Player player) {
		long load = 0;
		for (String marchId : marchs) {// 计算所有部队挖矿情况
			IWorldMarch march = WorldMarchService.getInstance().getMarch(marchId);
			if(march == null){
				this.removeCollectMarch(marchId);
				continue;
			}
			if (!player.getId().equals(march.getPlayerId())) {
				continue;
			}
			// 先判断行军状态，没有到达则不往下执行
			if (march.getMarchEntity().getMarchStatus() != WorldMarchStatus.MARCH_STATUS_MARCH_COLLECT_VALUE) {
				continue;
			}
			load += WorldMarchService.getInstance().getArmyCarryResNum(player, march.getMarchEntity().getArmys(), resType, march.getMarchEntity().getEffectParams());
			return load;
		}
		return 0L;
	}
	

	@Override
	public void parseBuildingParam(String buildParam) {
		if(buildParam != null){
			//解析参数
			JSONObject json = JSONObject.parseObject(getEntity().getBuildParam());
			if(!json.isEmpty()){
				JSONArray marchIds = json.getJSONArray(MARCHID_KEY);
				if(marchIds != null){
					for (Object object : marchIds) {
						this.marchs.add((String) object);
					}
				}
				this.resourceNum = json.getLongValue(RESOURCE_NUM_KEY);
			}
		}
	}

	@Override
	public String genBuildingParamStr() {
		JSONObject json = new JSONObject();
		json.put(RESOURCE_NUM_KEY, this.resourceNum);
		if(!marchs.isEmpty()){
			List<String> marchIds = new ArrayList<String>();
			for (String marchId : marchs) {
				marchIds.add(marchId);
			}
			json.put(MARCHID_KEY, marchIds);
		}
		return json.toJSONString();
	}

	@Override
	public int getBuildingUpLimit() {
		return getCfg().getBuildingUpLimit();
	}

	@Override
	public boolean onBuildRemove() {
		resourceNum = 0;
		lastRemainder = 0;
		clearCollectMarch();
		//超级矿移除后需要将其他矿置为可用状态
		List<IGuildBuilding> buildings = GuildManorService.getInstance().getGuildBuildings(getGuildId());
		for (IGuildBuilding iGuildBuilding : buildings) {
			if(iGuildBuilding.getBuildType() == TerritoryType.GUILD_MINE){
				iGuildBuilding.tryChangeBuildStat(GuildBuildingStat.OPENED.getIndex());
			}
		}
		//通用移除
		super.onBuildRemove();
		LogUtil.logGuildAction(GuildAction.GUILD_SUPERMINE_REMOVE, getGuildId());
		return true;
	}
	
	@Override
	public boolean onBuildComplete() {
		//建筑完成后, 将资源量补充满
		long cfgNum = getCfg().getResourceUpLimit();
		
		int effValue = 0;
		
		int effType = EffType.SUPER_MINE_RESOURCE_NUM_VALUE;
		Map<Integer, Integer> effectsGuildTech = GuildService.getInstance().getEffectsGuildTech(getGuildId());
		if (effectsGuildTech != null && effectsGuildTech.containsKey(effType)) {
			effValue = effectsGuildTech.get(effType);
		}
		
		cfgNum = cfgNum + (long)Math.ceil(cfgNum * GsConst.EFF_PER * effValue);
		
		this.resourceNum = cfgNum;
		LogUtil.logGuildAction(GuildAction.GUILD_BUILD_SUPERMINE_COMPLETE, getGuildId());
		return true;
	}
	
	public int getResType() {
		return resType;
	}
	
	public ManorPlayerInfoList.Builder makeSuperMineBuilder(Player player){
		ManorPlayerInfoList.Builder builder = ManorPlayerInfoList.newBuilder();
		builder.setBuildLife((int) this.getEntity().getBuildLife());
		builder.setLevel(this.getEntity().getLevel());
		builder.setResourceNum(this.getResourceNum());
		long speed = this.getCollectSpeed(player);                                                                      
		builder.setCollectSpeed((int) (0x00000000FFFFFFFFL & (speed >> 32)));
		builder.setCollectBuffSpeed((int) (0x00000000FFFFFFFFL & speed));
		builder.setAlreadyCollect(this.getSelfCollect(player));
		//总速度
		builder.setTotalCollectSpeed(this.getCollectTotalSpeed());
		//负重
		builder.setMarchLoadNum(this.getSelfLoad(player));
		for (String marchId : marchs) {
			IWorldMarch march = WorldMarchService.getInstance().getMarch(marchId);
			if(march == null){
				this.removeCollectMarch(marchId);
				continue;
			}
			ManorPlayerInfo.Builder infoBuilder = ManorPlayerInfo.newBuilder();
			// 获取玩家对象
			Player marchPlayer = GlobalData.getInstance().makesurePlayer(march.getPlayerId());
			//先判断行军状态，没有到达则不往下执行
			if(march.getMarchEntity().getMarchStatus() != WorldMarchStatus.MARCH_STATUS_MARCH_COLLECT_VALUE){
				infoBuilder.setState(PanelState.MOVEING);
			} else {
				infoBuilder.setState(PanelState.ACTION);
			}
			if(march.getPlayerId().equals(player.getId())){
				builder.setSelfMarchId(march.getMarchId());
			}
			infoBuilder.setPlayerId(marchPlayer.getId());
			infoBuilder.setName(marchPlayer.getName());
			infoBuilder.setPfIcon(marchPlayer.getPfIcon());
			infoBuilder.setIcon(marchPlayer.getIcon());
			infoBuilder.setGuildTag(marchPlayer.getGuildTag());
			
			List<PlayerHero> hero = march.getHeros();
			for (PlayerHero playerHero : hero) {
				infoBuilder.addHeros(playerHero.toPBobj());
			}
			Optional<SuperSoldier> sups = marchPlayer.getSuperSoldierByCfgId(march.getSuperSoldierId());
			if(sups.isPresent()){
				infoBuilder.setSsoldier(sups.get().toPBobj());
			}
			
			List<ArmyInfo> armyInfos = march.getMarchEntity().getArmys();
			for (ArmyInfo armyInfo : armyInfos) {
				KeyValuePairInt.Builder kb = KeyValuePairInt.newBuilder();
				kb.setKey(armyInfo.getArmyId());
				kb.setVal(armyInfo.getFreeCnt());
				kb.setSoldierStar(marchPlayer.getSoldierStar(armyInfo.getArmyId()));
				kb.setSoldierPlantStep(marchPlayer.getSoldierStep(armyInfo.getArmyId()));
				kb.setPlantSkillLevel(marchPlayer.getSoldierPlantSkillLevel(armyInfo.getArmyId()));
				kb.setPlantMilitaryLevel(marchPlayer.getSoldierPlantMilitaryLevel(armyInfo.getArmyId()));
				infoBuilder.addArmy(kb.build());
			}
			builder.addInfos(infoBuilder.build());
		}
		return builder;
	}
	
	/**
	 * 判断超级矿中是否有自己的行军
	 * @param playerId
	 * @return
	 */
	public boolean checkHasMarch(String playerId){
		for (String marchId : marchs) {//计算所有部队挖矿情况
			IWorldMarch march = WorldMarchService.getInstance().getMarch(marchId);
			if(march == null){
				this.removeCollectMarch(marchId);
				continue;
			}
			if (playerId.equals(march.getPlayerId())) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("resourceNum:" + resourceNum);
		sb.append(" | ");
		sb.append("resType:" + resType);
		return sb.toString();
	}
	
	@Override
	public int getbuildLimtUp() {
		return getCfg().getBuildingUpLimit();
	}
}
