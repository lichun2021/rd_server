
package com.hawk.game.guild.manor.building;

import java.util.Collection;
import java.util.List;

import org.hawk.app.HawkApp;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

import com.hawk.game.GsConfig;
import com.hawk.game.config.WorldMapConstProperty;
import com.hawk.game.entity.GuildBuildingEntity;
import com.hawk.game.guild.manor.AbstractBuildable;
import com.hawk.game.guild.manor.GuildBuildingStat;
import com.hawk.game.guild.manor.ManorMarchEnum;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.TerritoryType;
import com.hawk.game.protocol.GuildManor.GuildManorList;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.service.GuildManorService;
import com.hawk.game.service.GuildService;
import com.hawk.game.util.EffectParams;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.service.WorldPlayerService;
import com.hawk.game.world.service.WorldPointService;

/**
 * 联盟建筑抽象类
 * @author zhenyu.shang
 * @since 2017年7月7日
 */
public abstract class AbstractGuildBuilding extends AbstractBuildable implements IGuildBuilding{
	
	public final static int PERIOD = 1000;
	
	private GuildBuildingEntity entity;
	
	private TerritoryType buildType;
	
	private GuildBuildingStat buildStat;
	
	private long lastTickTime;
	
	public AbstractGuildBuilding(GuildBuildingEntity entity, TerritoryType buildType) {
		this.entity = entity;
		this.buildType = buildType;
		this.buildStat = GuildBuildingStat.valueOf(entity.getBuildingStat());
		this.lastTickTime = entity.getLastTickTime();
	}

	@Override
	public TerritoryType getBuildType() {
		return buildType;
	}
	
	@Override
	public GuildBuildingEntity getEntity() {
		return entity;
	}
	
	@Override
	public GuildBuildingStat getBuildStat() {
		return buildStat;
	}
	
	@Override
	public boolean tryChangeBuildStat(int stat) {
		GuildBuildingStat build = GuildBuildingStat.valueOf(stat);
		if(this.buildStat.canEnter(build)){
			this.buildStat = build;
			this.entity.setBuildingStat(stat);
			return true;
		}
		return false;
	}
	
	@Override
	public boolean canEnterState(int val) {
		GuildBuildingStat stat = GuildBuildingStat.valueOf(val);
		if(this.buildStat == stat) {
			return true;
		}
		return this.buildStat.canEnter(stat);
	}
	
	@Override
	public boolean tryEnterState(int stat) {
		return tryChangeBuildStat(stat);
	}
	
	@Override
	public void tick() {
		//不是锁定或者未放置的状态都tick
		if (getBuildStat() == GuildBuildingStat.LOCKED || getBuildStat() == GuildBuildingStat.OPENED) {
			return;
		}
		long interval = 1000;
		long now = HawkApp.getInstance().getCurrentTime();
		if(lastTickTime > 0){
			interval = now - lastTickTime;
		}
		
		if(interval < PERIOD){
			return;
		}
		
		lastTickTime = now;
		switch (buildStat) {
			case BUILDING:
				checkComplete(interval);
				break;
				
			case COLLECT: //收集中，箭塔防御中，仓库存储中
				heartBeats(interval);
				break;
			default:
				break;
		}
	}
	
	public boolean checkMarchEmpty(Collection<String> marchs){
		//如果当前驻军数量为空, 则改变成未驻防或者损毁状态, 或者未完成状态
		if(marchs == null || marchs.isEmpty()){
			if(this.buildStat == GuildBuildingStat.BUILDING){//当前是建造状态, 则改为未完成
				tryEnterState(GuildBuildingStat.UNCOMPELETE.getIndex());
			} else {
				tryEnterState(GuildBuildingStat.COMPELETE.getIndex());
			}
			//设置建造速度为零
			setLastBuildSpeed(0);
			// 广播状态变化
			WorldPointService.getInstance().notifyPointUpdate(getEntity().getPosX(), getEntity().getPosY());
			GuildManorService.getInstance().broadcastGuildBuilding(getGuildId());
			return true;
		}
		return false;
	}
	
	public void checkComplete(long interval){
		//先判断当前驻军数量
		List<String> marchs = GuildManorService.getInstance().getManorBuildMarchId(getPositionId());
		if(checkMarchEmpty(marchs)){
			return;
		}
		
		//此处算兵力数量待定, 战斗力算法不一定
		double lastSpeed = getCurrentSpeed(false, getBuildingUpLimit());
		if(GsConfig.getInstance().isRobotMode() && GsConfig.getInstance().isDebug()){
			//机器人模式，建造速度增加100倍
			lastSpeed *= 1000;
		}
		double addBuildLife = lastSpeed * (interval/1000);
		
		//取得当前的建筑值
		double buildLife = entity.getBuildLife();
		//存入建筑值
		entity.setBuildLife(buildLife + addBuildLife);
		
		//记录最近一次的建筑速度
		if((long)(this.getLastBuildSpeed() * 100000L) != (long)(lastSpeed * 100000L)){//如果本次建造速度有变化,则通知客户端行军
			this.setLastBuildSpeed(lastSpeed);
			this.broadcastPointMarchUpate();
		}
		
		//判断建筑值是否已经满了
		if(entity.getBuildLife() >= getBuildingUpLimit()){
			long now = HawkTime.getMillisecond();
			
			if (getBuildType() != TerritoryType.GUILD_MINE) {
				for (String marchId : marchs) {
					IWorldMarch worldMarch = WorldMarchService.getInstance().getMarch(marchId);
					WorldMarchService.getInstance().onPlayerNoneAction(worldMarch, now);
				}
			}
			
			//存入最大值和完成时间
			entity.setBuildTime(HawkTime.getMillisecond());
			entity.setBuildLife(getBuildingUpLimit());
			//修改状态
			if(this.getBuildType() == TerritoryType.GUILD_BARTIZAN){//箭塔直接进入防御中状态
				tryChangeBuildStat(GuildBuildingStat.COLLECT.getIndex());
			} else {
				tryChangeBuildStat(GuildBuildingStat.COMPELETE.getIndex());
			}
			this.setLastBuildSpeed(0);
			this.onBuildComplete();
			
			// 联盟超级矿，建造完开始采集
			if (getBuildType() == TerritoryType.GUILD_MINE) {
				
				for (String marchId : marchs) {
					try {
						// 老行军
						IWorldMarch oldMarch = WorldMarchService.getInstance().getMarch(marchId);
						
						GuildManorSuperMine superMine = (GuildManorSuperMine) this;
						
						// 不可采集的话直接返回
						if(!checkLevelCanCollect(oldMarch.getPlayer(), superMine.getResType())) {
							WorldMarchService.getInstance().onPlayerNoneAction(oldMarch, now);
							
						} else {
							// 生成新行军
							IWorldMarch newMarch = genManorBuildToCollectMarch(oldMarch.getPlayer(),
									WorldMarchType.MANOR_COLLECT_VALUE,
									oldMarch.getMarchEntity().getTerminalId(),
									oldMarch.getMarchEntity().getTargetId(),
									oldMarch.getMarchEntity().getEffectParams());
							newMarch.getMarchEntity().setArmys(oldMarch.getMarchEntity().getArmyCopy());
							
							// 老行军结束
							WorldMarchService.getInstance().onWorldMarchOver(oldMarch);
						}
					} catch (Exception e) {
						HawkException.catchException(e);
					}
				}
			}
			
			//通知状态
			WorldPointService.getInstance().notifyPointUpdate(getEntity().getPosX(), getEntity().getPosY());
			//组织消息
			GuildManorList.Builder builder = GuildManorService.getInstance().makeManorListBuilder(getGuildId());
			//广播消息
			GuildService.getInstance().broadcastProtocol(getGuildId(), HawkProtocol.valueOf(HP.code.GUILD_MANOR_LIST_S_VALUE, builder));
		}
	}
	
	/**
	 * 联盟矿建造行军转采集
	 * @param player
	 * @param marchType
	 * @param terminalId
	 * @param targetId
	 * @param armys
	 * @param heroId
	 * @param superSoldierId
	 * @return
	 */
	public IWorldMarch genManorBuildToCollectMarch(Player player, int marchType, int terminalId, String targetId, EffectParams effParams) {
		// 起始点id
		int origionId = WorldPlayerService.getInstance().getPlayerPos(player.getId());
		// 目标点
		WorldPoint terminalPoint = WorldPointService.getInstance().getWorldPoint(terminalId);
		
		// 生成行军
		IWorldMarch march = WorldMarchService.getInstance().genMarch(player, marchType, origionId, terminalId, targetId, terminalPoint, 0, "", true, effParams);
		
		// 行军注册
		march.register();
		// 行军结束时间设置为当前(直接进去采集)
		march.getMarchEntity().setEndTime(HawkTime.getMillisecond());
		
		ManorMarchEnum.valueOf(WorldMarchType.MANOR_COLLECT_VALUE).addMarch(march, march.getMarchEntity().getTargetId());
		return march;
	}
	
	/**
	 * 检测当前等级是否可采集对应资源类型
	 * @param player
	 * @param resType
	 * @return
	 */
	public boolean checkLevelCanCollect(Player player, int resType) {
		int[] resLv = WorldMapConstProperty.getInstance().getResLv();
		for (int i = 0; i < GsConst.RES_TYPE.length; i++) {
			if (resType == GsConst.RES_TYPE[i] && player.getCityLv() < resLv[i]) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 心跳, 如果子类中没有心跳需求, 请勿覆盖实现此类
	 */
	public void heartBeats(long interval){
		
	}
	
	@Override
	public boolean isFullLife() {
		return this.entity.getBuildLife() >= getBuildingUpLimit();
	}
	
	@Override
	public String getGuildId() {
		return entity.getGuildId();
	}
	
	@Override
	public int getOverTime() {
		if(getLastBuildSpeed() == 0){
			return 0;
		}
		double leftLife = getBuildingUpLimit() - getEntity().getBuildLife();
		if(leftLife <= 0){
			return 0;
		}
		return (int) (leftLife/getLastBuildSpeed());
	}
	
	@Override
	public boolean canEnterBuildState() {
		return canEnterState(GuildBuildingStat.BUILDING.getIndex());
	}
	
	@Override
	public boolean tryEnterBuildState() {
		return tryChangeBuildStat(GuildBuildingStat.BUILDING.getIndex());
	}
	
	@Override
	public int getPositionId() {
		if(HawkOSOperator.isEmptyString(entity.getPos())){
			return 0;
		}
		return GameUtil.combineXAndY(entity.getPosX(), entity.getPosY());
	}
	
	@Override
	public boolean canRemove() {
		//此处默认没有限制,如果各个建筑有限制的话,可以覆盖次方法进行实现
		return true;
	}
	
	@Override
	public boolean onBuildRemove() {
		//清空坐标
		entity.setPos(null);
		//清空最大值和完成时间
		entity.setBuildTime(0);
		entity.setBuildLife(1);
		entity.setLastTickTime(0);
		entity.setLastTakeBackTime(HawkTime.getMillisecond());
		this.setLastBuildSpeed(0);
		//修改状态
		tryChangeBuildStat(GuildBuildingStat.OPENED.getIndex());
		return true;
	}
	
	@Override
	public boolean onBuildDelete() {
		entity.delete();
		return true;
	}

	@Override
	public boolean onBuildComplete() {
		return true;
	}
	
	@Override
	public double getbuildLife() {
		return entity.getBuildLife();
	}
	
	@Override
	public int getbuildStat() {
		return buildStat.getIndex();
	}
	
	@Override
	public int getLevel() {
		return entity.getLevel();
	}
	
	public boolean isPlaceGround(){
		return this.buildStat != GuildBuildingStat.LOCKED && this.buildStat != GuildBuildingStat.OPENED;
	}
	
	@Override
	public void onCloseServer() {
		this.entity.setLastTickTime(lastTickTime);
	}
}
