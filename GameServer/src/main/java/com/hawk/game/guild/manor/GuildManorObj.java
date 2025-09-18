package com.hawk.game.guild.manor;

import java.util.List;

import org.hawk.app.HawkApp;
import org.hawk.config.HawkConfigManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

import com.hawk.game.GsConfig;
import com.hawk.game.config.GuildConstProperty;
import com.hawk.game.config.GuildManorCfg;
import com.hawk.game.entity.GuildManorEntity;
import com.hawk.game.entity.GuildMemberObject;
import com.hawk.game.global.GlobalData;
import com.hawk.game.guild.manor.building.IGuildBuilding;
import com.hawk.game.item.AwardItems;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.Const.TerritoryType;
import com.hawk.game.protocol.GuildManager.AuthId;
import com.hawk.game.protocol.GuildManor.GuildManorList;
import com.hawk.game.protocol.GuildManor.GuildManorStat;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.service.GuildManorService;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.MailService;
import com.hawk.game.service.mail.GuildMailService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.LogUtil;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.game.world.thread.WorldTask;
import com.hawk.game.world.thread.WorldThreadScheduler;
import com.hawk.log.LogConst.GuildAction;

/**
 * 领地实体对象
 * @author zhenyu.shang
 * @since 2017年7月6日
 */
public class GuildManorObj extends AbstractBuildable{
	
	public final static int RADIUS = 2;
	
	public final static int PERIOD = 1000;

	private GuildManorEntity entity;
	
	private ManorBastionStat bastionStat;
	
	private long lastTickTime;
	
	public GuildManorObj(GuildManorEntity entity) {
		this.entity = entity;
		this.bastionStat = ManorBastionStat.valueOf(this.entity.getManorState());
		this.lastTickTime = entity.getLastTickTime();
	}

	public GuildManorEntity getEntity() {
		return entity;
	}

	public GuildManorCfg getManorCfg() {
		return HawkConfigManager.getInstance().getConfigByKey(GuildManorCfg.class, entity.getManorIndex());
	}
	
	/**
	 * 检查是否可以解锁
	 * @return
	 */
	public void checkUnlockAndChangeStat(){
		String guildId = entity.getGuildId();
		GuildManorCfg manorCfg = getManorCfg();
		//达到条件，而且必须是锁定状态，才可改变至未放置状态
		if(GuildService.getInstance().getGuildMembers(guildId).size() >= manorCfg.getPeopleLimit()
				&& GuildService.getInstance().getGuildBattlePoint(guildId) >= manorCfg.getPowerLimit()
				&& GuildService.getInstance().getGuildScienceVal(guildId) >= manorCfg.getScienceLimit()
				&& entity.getManorState() == GuildManorStat.LOCKED_M_VALUE){
			this.tryEnterState(GuildManorStat.OPENED_M_VALUE);
			int icon = GuildService.getInstance().getGuildFlag(guildId);
			String guildTag = GuildService.getInstance().getGuildTag(guildId);
			String guildName = GuildService.getInstance().getGuildName(guildId);
			GuildMailService.getInstance().sendGuildMail(guildId, MailParames.newBuilder()
					.setIcon(icon)
					.setMailId(MailId.GUILD_MANOR_UNLOCK)
					.addContents(guildTag)
					.addContents(guildName));
			
			// 联盟堡垒结算奖励邮件
			AwardItems award = AwardItems.valueOf(GuildConstProperty.getInstance().getAllianceManorUnlockMailAward());
			MailParames.Builder parames = MailParames.newBuilder()
							.setIcon(icon)
							.setMailId(MailId.GUILD_MANOR_UNLOCK_REWARD)
							.addContents(guildTag)
							.addContents(guildName)
							.addRewards(award.getAwardItems())
							.setAwardStatus(MailRewardStatus.NOT_GET);
			
			for (String playerId : GuildService.getInstance().getGuildMembers(guildId)) {
				GuildMemberObject member = GuildService.getInstance().getGuildMemberObject(playerId);
				member.updateManorUnlockTimes(member.getManorUnlockTimes() + 1);
				if(member.getManorUnlockTimes() <= GuildConstProperty.getInstance().getAllianceManorUnlockMailNum()){
					parames.setPlayerId(playerId);
					MailService.getInstance().sendMail(parames.build());
				}
			}
		}
	}
	
	/**
	 * 修改领地名称
	 * @param name
	 */
	public void changeManorName(String name){
		this.entity.setManorName(name);
	}

	public ManorBastionStat getBastionStat() {
		return bastionStat;
	}

	@Override
	public boolean canEnterState(int val) {
		ManorBastionStat stat = ManorBastionStat.valueOf(val);
		if(this.bastionStat == stat) {
			return true;
		}
		return this.bastionStat.canEnter(stat);
	}
	
	@Override
	public boolean tryEnterState(int stat) {
		ManorBastionStat manorStat = ManorBastionStat.valueOf(stat);
		if(this.bastionStat.canEnter(manorStat)){
			this.bastionStat = manorStat;
			this.entity.setManorState(stat);
			return true;
		}
		return false;
	}

	@Override
	public boolean isFullLife() {
		return this.entity.getBuildingLife() >= getManorCfg().getBuildingUpLimit();
	}

	@Override
	public String getGuildId() {
		return entity.getGuildId();
	}
	
	/**
	 * 重置初始值
	 */
	public void resetBuildLife(){
		if(this.entity.getLastTakeBackTime() > 0 && this.entity.getCompleteTime() > 0){ 
			this.entity.setBuildingLife(getManorCfg().getResetBuilding());
		} else {//第一次放置
			this.entity.setBuildingLife(getManorCfg().getBuildingInitial());
		}
	}
	
	public void tick() {
		//不是锁定或者未放置的状态都tick
		if(getBastionStat() == ManorBastionStat.LOCKED || getBastionStat() == ManorBastionStat.OPENED){
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
		switch (bastionStat) {
			case BREAKING:
			case BUILDING:
			case REPAIRING:
			case UN_BREAKING:
			case UNCOMPELETE:
				checkComplete(interval);
				break;
				
			case GARRISON:
				List<IWorldMarch> marchs = GuildManorService.getInstance().getManorBuildMarch(getPositionId());
				checkMarchEmpty(marchs);
				break;
				
			default:
				break;
		}
		
		WorldPoint point = WorldPointService.getInstance().getWorldPoint(entity.getPosX(), entity.getPosY());
		if(point == null){
			//修改状态
			onMonorRemove();
		}
		
		
	}
	
	public boolean checkMarchEmpty(List<IWorldMarch> marchs){
		//如果当前驻军数量为空, 则改变成未驻防或者损毁状态, 或者未完成状态
		if(marchs == null || marchs.isEmpty()){
			if(this.bastionStat == ManorBastionStat.BUILDING || this.bastionStat == ManorBastionStat.UN_BREAKING){//当前是建造状态, 则改为未完成
				tryEnterState(ManorBastionStat.UNCOMPELETE.getIndex());
			} else {
				if(this.getEntity().getBuildingLife() >= getManorCfg().getBuildingUpLimit()){
					tryEnterState(ManorBastionStat.UNGARRISON.getIndex());
				} else {
					tryEnterState(ManorBastionStat.DAMAGED.getIndex());
				}
			}
			//设置建造速度为0
			setLastBuildSpeed(0);
			
			//通知状态
			WorldPointService.getInstance().notifyPointUpdate(getEntity().getPosX(), getEntity().getPosY());
			
			//推送变化消息
			GuildManorList.Builder builder = GuildManorList.newBuilder();
			
			//领地哨塔列表
			GuildManorService.getInstance().makeManorBastion(builder, getGuildId());
			
			//广播消息
			GuildService.getInstance().broadcastProtocol(getGuildId(), HawkProtocol.valueOf(HP.code.GUILD_MANOR_LIST_S_VALUE, builder));
			return true;
		}
		return false;
	}
	
	/**
	 * 检查建造或, 攻击或修复
	 */
	public void checkComplete(long interval){
		//先判断当前驻军数量
		List<IWorldMarch> marchs = GuildManorService.getInstance().getManorBuildMarch(getPositionId());
		if(checkMarchEmpty(marchs)){//如果没兵了就不继续往下
			return;
		}
		Player leader = GlobalData.getInstance().makesurePlayer(marchs.get(0).getPlayerId());
		int buildLimit = getManorCfg().getBuildingUpLimit();
		if(this.bastionStat == ManorBastionStat.BUILDING || this.bastionStat == ManorBastionStat.REPAIRING || this.bastionStat == ManorBastionStat.UNCOMPELETE){
			double lastSpeed = getCurrentSpeed(false, buildLimit);
			if(GsConfig.getInstance().isRobotMode() && GsConfig.getInstance().isDebug()){
				//机器人模式，建造速度增加100倍
				lastSpeed *= 1000;
			}
			double addBuildLife = lastSpeed * (interval/1000.0);
			//取得当前的建筑值
			double buildLife = entity.getBuildingLife();
			//存入建筑值
			entity.setBuildingLife(buildLife + addBuildLife);
			//记录最近一次的建筑速度
			if((long)(this.getLastBuildSpeed() * 100000L) != (long)(lastSpeed * 100000L)){
				//如果本次建造速度有变化,则通知客户端行军
				this.setLastBuildSpeed(lastSpeed);
				this.broadcastPointMarchUpate();
			}
			if(interval > 10000){//如果大于10秒。记录一次日志
				GuildManorService.logger.warn("last interval time over 10 second, interval:{}, speed:{}, addlife:{}", interval, lastSpeed, addBuildLife);
			}
			//判断建筑值是否已经满了
			if(entity.getBuildingLife() >= buildLimit){
				if(this.bastionStat == ManorBastionStat.BUILDING){//建造中
					List<IGuildBuilding> buildings = GuildManorService.getInstance().getGuildBuildings(getGuildId());
					//如果这个是新建成的堡垒，不是之前被打碎的堡垒，则解锁新箭塔, 添加新的下一级未解锁的箭塔
					if(this.entity.getCompleteTime() == 0){
						//解锁未解锁的建筑
						for (IGuildBuilding iGuildBuilding : buildings) {
							if(iGuildBuilding.getBuildType() == TerritoryType.GUILD_MINE){
								if(!GuildManorService.getInstance().hasManorComplete(getGuildId()) && iGuildBuilding.getBuildStat() == GuildBuildingStat.LOCKED){
									iGuildBuilding.tryChangeBuildStat(GuildBuildingStat.OPENED.getIndex());
								}
							} else {
								if(iGuildBuilding.getBuildStat() == GuildBuildingStat.LOCKED){
									iGuildBuilding.tryChangeBuildStat(GuildBuildingStat.OPENED.getIndex());
								}
							}
						}
						if(!leader.isRobot()){ //机器人不解锁, 初始化时候全部解锁
							//添加新的下一级未解锁的箭塔
							GuildManorService.getInstance().initGuildManorTower(getEntity().getManorIndex() + 1, getGuildId());
						}
					}
					//存入最大值和完成时间
					entity.setCompleteTime(HawkTime.getMillisecond());
				}
				entity.setBuildingLife(getManorCfg().getBuildingUpLimit());
				//设置上次速度为0
				setLastBuildSpeed(0);
				//修改状态
				tryEnterState(ManorBastionStat.GARRISON.getIndex());
				//修改所有行军状态为驻守
				for (IWorldMarch worldMarch : marchs) {
					worldMarch.getMarchEntity().setMarchStatus(WorldMarchStatus.MARCH_STATUS_MANOR_GARRASION_VALUE);
					worldMarch.updateMarch();
				}
				//通知状态
				WorldPointService.getInstance().notifyPointUpdate(getEntity().getPosX(), getEntity().getPosY());
				//组织消息
				GuildManorList.Builder builder = GuildManorService.getInstance().makeManorListBuilder(getGuildId());
				//广播消息
				GuildService.getInstance().broadcastProtocol(getGuildId(), HawkProtocol.valueOf(HP.code.GUILD_MANOR_LIST_S_VALUE, builder));
				//调用建成
				int pointId = GameUtil.combineXAndY(entity.getPosX(), entity.getPosY());
				GuildManorService.getInstance().guildBuildComplete(getGuildId(), getEntity().getManorId(), TerritoryType.GUILD_BASTION, pointId);
			}
		} else if(this.bastionStat == ManorBastionStat.BREAKING || this.bastionStat == ManorBastionStat.UN_BREAKING){
			double lastSpeed = -getCurrentSpeed(true, buildLimit);//摧毁速度是负数
			double addBuildLife = lastSpeed * (interval/1000.0);
			//取得当前的建筑值
			double buildLife = entity.getBuildingLife();
			//存入建筑值
			entity.setBuildingLife(buildLife + addBuildLife);
			//记录最近一次的摧毁速度
			if((long)(this.getLastBuildSpeed() * 100000L) != (long)(lastSpeed * 100000L)){//如果本次摧毁速度有变化,则通知客户端行军
				this.setLastBuildSpeed(lastSpeed);
				this.broadcastPointMarchUpate();
			}
			if(interval > 10000){//如果大于10秒。记录一次日志
				GuildManorService.logger.warn("last interval time over 10 second, interval:{}, speed:{}, addlife:{}", interval, lastSpeed, addBuildLife);
			}
			//判断建筑值是否已经满了
			if(entity.getBuildingLife() <= 0){
				//日志记录
				if(isComplete()) {
					LogUtil.logGuildAction(GuildAction.GUILD_ATK_COMPLETE_MANOR_COM, getGuildId());
				} else {
					LogUtil.logGuildAction(GuildAction.GUILD_ATK_BUILDING_MANOR_COM, getGuildId());
				}
				//移除领地
				GuildManorService.getInstance().rmGuildManor(getGuildId(), getEntity().getManorId());
				//移除领地本身属性状态
				this.onMonorRemove();
				//通知状态
				WorldPointService.getInstance().notifyPointUpdate(getEntity().getPosX(), getEntity().getPosY());
				//组织消息
				GuildManorList.Builder builder = GuildManorService.getInstance().makeManorListBuilder(getGuildId());
				//广播消息
				GuildService.getInstance().broadcastProtocol(getGuildId(), HawkProtocol.valueOf(HP.code.GUILD_MANOR_LIST_S_VALUE, builder));
				int icon = GuildService.getInstance().getGuildFlag(getEntity().getGuildId());
				//发送被摧毁邮件
				String guildName = GuildService.getInstance().getGuildName(leader.getGuildId());
				GuildMailService.getInstance().sendGuildMail(getGuildId(), MailParames.newBuilder().setMailId(MailId.GUILD_MANOR_BE_ATTACKED).addContents(guildName).setIcon(icon));
			}
		}
	}
	
	/**
	 * 所有已经完成状态, 完成时间必须大于0
	 * @return
	 */
	public boolean isComplete(){
		return this.bastionStat == ManorBastionStat.BREAKING || this.bastionStat == ManorBastionStat.DAMAGED
				|| this.bastionStat == ManorBastionStat.GARRISON || this.bastionStat == ManorBastionStat.REPAIRING
				|| this.bastionStat == ManorBastionStat.UNGARRISON;
	}
	
	/**
	 * 获取剩余时间
	 * @return
	 */
	public int getOverTime(){
		if(getLastBuildSpeed() == 0){
			return 0;
		}
		if(getLastBuildSpeed() < 0){//损毁中的剩余时间
			return (int) (entity.getBuildingLife()/-getLastBuildSpeed());
		} 
		double leftLife = getManorCfg().getBuildingUpLimit() - entity.getBuildingLife();
		if(leftLife <= 0){
			return 0;
		}
		return (int) (leftLife/getLastBuildSpeed());
	}

	@Override
	public boolean canEnterBuildState() {
		return canEnterState(ManorBastionStat.BUILDING.getIndex());
	}

	@Override
	public boolean tryEnterBuildState() {
		return tryEnterState(ManorBastionStat.BUILDING.getIndex());
	}
	
	@Override
	public int getPositionId() {
		if(HawkOSOperator.isEmptyString(entity.getPos())){
			return 0;
		}
		return GameUtil.combineXAndY(entity.getPosX(), entity.getPosY());
	}
	
	/**
	 * 领地被移除
	 */
	public void onMonorRemove(){
		entity.setBuildingLife(0);
		entity.setLastTickTime(0);
		entity.setLastTakeBackTime(HawkTime.getMillisecond());
		this.setLastBuildSpeed(0);
		//修改状态
		tryEnterState(ManorBastionStat.OPENED.getIndex());
	}
	
	/**
	 * 移除领地entity
	 */
	public void delete(){
		entity.delete();
	}
	
	@Override
	public double getbuildLife() {
		return entity.getBuildingLife();
	}
	
	@Override
	public int getbuildStat() {
		return bastionStat.getIndex();
	}
	
	@Override
	public TerritoryType getBuildType() {
		return TerritoryType.GUILD_BASTION;
	}
	
	@Override
	public int getLevel() {
		return entity.getLevel();
	}
	
	public void onCloseServer(){
		this.entity.setLastTickTime(lastTickTime);
	}
	
	@Override
	public int getbuildLimtUp() {
		return getManorCfg().getBuildingUpLimit();
	}
	
	/**
	 * 遣返行军
	 */
	@Override
	public boolean repatriateMarch(Player player, String targetPlayerId) {
		// 驻军队长行军
		IWorldMarch leanderMarch = getMarchLeader();
		if (leanderMarch == null) {
			return false;
		}
		
		// R4盟主队长可以遣返
		boolean guildAuthority = GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.ALLIANCE_MANOR_SET);
		boolean isLeader = player.getId().equals(leanderMarch.getMarchEntity().getPlayerId());
		if (!guildAuthority && !isLeader) {
			return false;
		}
		
		// 摧毁状态中遣返
		if (isBreaking()) 
		{
			// 驻军队长所属联盟
			String marchGuildId = GuildService.getInstance().getPlayerGuildId(leanderMarch.getMarchEntity().getPlayerId());
			if (!player.getGuildId().equals(marchGuildId)) {
				return false;
			}
			
			List<IWorldMarch> marchs = GuildManorService.getInstance().getManorBuildMarch(getPositionId());
			for (IWorldMarch iWorldMarch : marchs) {
				if (!iWorldMarch.getPlayerId().equals(targetPlayerId)) {
					continue;
				}
				WorldMarchService.getInstance().onPlayerNoneAction(iWorldMarch, HawkApp.getInstance().getCurrentTime());
			}
		}
		
		// 其它状态遣返
		else 
		{
			// 不是本盟的没有权限操作
			if (!player.getGuildId().equals(getGuildId())) {
				return false;
			}
			
			List<IWorldMarch> marchs = GuildManorService.getInstance().getManorBuildMarch(getPositionId());
			for (IWorldMarch iWorldMarch : marchs) {
				if (!iWorldMarch.getPlayerId().equals(targetPlayerId)) {
					continue;
				}
				WorldMarchService.getInstance().onPlayerNoneAction(iWorldMarch, HawkApp.getInstance().getCurrentTime());
			}
		}
		
		return true;
	}
	
	private boolean isBreaking() {
		return getbuildStat() == ManorBastionStat.BREAKING.index || getbuildStat() == ManorBastionStat.UN_BREAKING.index;
	}
	
	
	/**
	 * 任命队长
	 * @param player
	 * @param targetPlayerId
	 * @return
	 */
	public boolean cheangeQuarterLeader(Player player, String targetPlayerId) {
		// 驻军队长行军
		IWorldMarch leanderMarch = getMarchLeader();
		if (leanderMarch == null) {
			return false;
		}

		// R4盟主队长可以遣返
		boolean guildAuthority = GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.ALLIANCE_MANOR_SET);
		boolean isLeader = player.getId().equals(leanderMarch.getMarchEntity().getPlayerId());
		if (!guildAuthority && !isLeader) {
			return false;
		}

		// 摧毁状态中遣返
		if (isBreaking()) {
			// 驻军队长所属联盟
			String marchGuildId = GuildService.getInstance().getPlayerGuildId(leanderMarch.getMarchEntity().getPlayerId());
			if (!player.getGuildId().equals(marchGuildId)) {
				return false;
			}

			WorldThreadScheduler.getInstance().postWorldTask(new WorldTask(GsConst.WorldTaskType.CHANGE_QUARTER_LEADER) {
				@Override
				public boolean onInvoke() {
					WorldMarchService.getInstance().changeManorMarchLeader(getPositionId(), targetPlayerId);
					return true;
				}
			});
		}

		// 其它状态遣返
		else {
			// 不是本盟的没有权限操作
			if (!player.getGuildId().equals(getGuildId())) {
				return false;
			}

			WorldThreadScheduler.getInstance().postWorldTask(new WorldTask(GsConst.WorldTaskType.CHANGE_QUARTER_LEADER) {
				@Override
				public boolean onInvoke() {
					WorldMarchService.getInstance().changeManorMarchLeader(getPositionId(), targetPlayerId);
					return true;
				}
			});
		}
		return true;
	}
}
