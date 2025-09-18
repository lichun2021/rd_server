package com.hawk.game.service.warFlag;

import java.util.Map;

import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;
import org.slf4j.Logger;

import com.hawk.game.entity.WarFlagEntity;
import com.hawk.game.item.WarFlagSignUpItem;
import com.hawk.game.protocol.WarFlag.FlageState;
import com.hawk.game.service.WarFlagService;
import com.hawk.game.world.WorldMarchService;

/**
 * 战旗
 * @author golden
 *
 */
public class IFlag {

	/** 锁定中*/
	public final FlagState lockedState;
	/** 已解锁*/
	public final FlagState unlockState;
	/** 建造中*/
	public final FlagState buildState;
	/** 已放置*/
	public final FlagState placedState;
	/** 已完成*/
	public final FlagState defendState;
	/** 占领中*/
	public final FlagState beinvadedState;
	/** 损毁*/
	public final FlagState damagedState;
	/** 修复中*/
	public final FlagState fixState;
	
	/**
	 * 实体
	 */
	private WarFlagEntity flagEntity;

	/**
	 * 构造
	 */
	protected IFlag(WarFlagEntity flagEntity) {
		this.flagEntity = flagEntity;
		
		buildState = new FlagBuild(this);
		beinvadedState = new FlagBeinvaded(this);
		damagedState = new FlagDamaged(this);
		defendState = new FlagDefend(this);
		fixState = new FlagFix(this);
		lockedState = new FlagLocked(this);
		placedState = new FlagPlaced(this);
		unlockState = new FlagUnlock(this);
	}
	
	/**
	 * 创建/初始化旗帜
	 */
	public static IFlag create(WarFlagEntity flag) {
		IFlag iFlag = new IFlag(flag);
		return iFlag;
	}
	
	/**
	 * 日志
	 */
	public Logger getLogger() {
		return WarFlagService.logger;
	}
	
	/**
	 * 获取旗帜状态
	 */
	public FlagState getFlagState() {
		
		FlagState state = null;
		
		switch (flagEntity.getState()) {
		
		case FlageState.FLAG_LOCKED_VALUE:
			state = lockedState;
			break;
			
		case FlageState.FLAG_UNLOCKED_VALUE:
			state = unlockState;
			break;
			
		case FlageState.FLAG_BUILDING_VALUE:
			state = buildState;
			break;
			
		case FlageState.FLAG_PLACED_VALUE:
			state = placedState;
			break;
			
		case FlageState.FLAG_DEFEND_VALUE:
			state = defendState;
			break;
			
		case FlageState.FLAG_BEINVADED_VALUE:
			state = beinvadedState;
			break;
			
		case FlageState.FLAG_DAMAGED_VALUE:
			state = damagedState;
			break;
			
		case FlageState.FLAG_FIX_VALUE:
			state = fixState;
			break;
			
		default:
			break;
		}
		
		return state;
	}
	
	/**
	 * tick
	 */
	public void tick() {
		// 状态tick
		getFlagState().stateTick();
		
		// 资源产出tick
		getFlagState().resTick();
		
		// 母旗tick
		centerFlagTick();
		
		// 收回旗帜tick
		takeBackTick();
	}
	
	/**
	 * 旗子收回tick
	 */
	public void takeBackTick() {
		if (getRemoveTime() == 0 || getRemoveTime() > HawkTime.getMillisecond()) {
			return;
		}
		setRemoveTime(0L);
		
		if (!canTakeBack()) {
			return;
		}
		
		WarFlagService.getInstance().takeBackWarFlag(null, getFlagId());
	}
	
	/**
	 * 是否有领地
	 */
	public boolean hasManor() {
		return getFlagState().hasManor();
	}
	
	/**
	 * 是否是建造完成以后的状态
	 */
	public boolean isBuildComplete() {
		return getFlagState().isBuildComplete();
	}
	
	/**
	 * 获取当前建筑值
	 */
	public int getCurrBuildLife() {
		return getFlagState().getCurrBuildLife();
	}
	
	/**
	 * 是否已放置 
	 */
	public boolean isPlaced() {
		return getFlagState().isPlaced();
	}
	
	/**
	 * 是否可以放置
	 */
	public boolean canPlace() {
		return getFlagState().canPlace();
	}

	/**
	 * 是否可以收回
	 */
	public boolean canTakeBack() {
		return getFlagState().canTakeBack();
	}
	
	/**
	 * 行军返回
	 */
	public void marchReturn() {
		getFlagState().marchReturn();
	}
	
	/**
	 * 行军到达
	 */
	public void marchReach(String guildId) {
		getFlagState().marchReach(guildId);
	}
	
	/**
	 * 母旗是否可以产出
	 */
	public boolean canCenterFlagTick() {
		if (!isCenter()) {
			return false;
		}
		
		return getFlagState().canCenterTick(); 
	}
	
	/**
	 * 母旗tick
	 */
	public void centerFlagTick() {
		if (!canCenterFlagTick()) {
			return;
		}
		
		// 还没到发奖时间
		if (HawkTime.getMillisecond() < getCenterNextTickTime()) {
			return;
		} 
		
		// 设置下次的tick时间
		setCenterNextTickTime(WarFlagService.getInstance().calcCentNextTickTime(this));
		
		WarFlagService.getInstance().checkCenterFlag(this);
		
		// 发行军(发奖)
		for (WarFlagSignUpItem signUpInfo : getSignUpInfos().values()) {
			try {
				if (signUpInfo.getBox().isEmpty()) {
					continue;
				}
				WorldMarchService.getInstance().startCenterFlagMarch(signUpInfo.getPlayerId(), signUpInfo, this);
				signUpInfo.getBox().clear();
				signUpInfo.setSpecialBoxCount(0);
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
	}
	
	/**
	 * 获取当前占领值
	 */
	public int getCurrOccupyLife() {
		return getFlagState().getCurrOccupyLife();
	}
	
	public String getFlagId() {
		return flagEntity.getFlagId();
	}

	public void setFlagId(String flagId) {
		flagEntity.setFlagId(flagId);
	}

	public String getOwnerId() {
		return flagEntity.getOwnerId();
	}

	public void setOwnerId(String ownerId) {
		flagEntity.setOwnerId(ownerId);
	}

	public String getCurrentId() {
		return flagEntity.getCurrentId();
	}

	public void setCurrentId(String currentId) {
		flagEntity.setCurrentId(currentId);
	}

	public long getPlaceTime() {
		return flagEntity.getPlaceTime();
	}

	public void setPlaceTime(long placeTime) {
		flagEntity.setPlaceTime(placeTime);
	}

	public int getLife() {
		return flagEntity.getLife();
	}

	public void setLife(int life) {
		flagEntity.setLife(life);
	}

	public long getCompleteTime() {
		return flagEntity.getCompleteTime();
	}

	public void setCompleteTime(long completeTime) {
		flagEntity.setCompleteTime(completeTime);
	}

	public int getState() {
		return flagEntity.getState();
	}

	public void setState(int state) {
		flagEntity.setState(state);
	}

	public long getLastBuildTick() {
		return flagEntity.getLastBuildTick();
	}

	public void setLastBuildTick(long lastBuildTick) {
		flagEntity.setLastBuildTick(lastBuildTick);
	}

	public long getLastResourceTick() {
		return flagEntity.getLastResourceTick();
	}

	public void setLastResourceTick(long lastResourceTick) {
		flagEntity.setLastResourceTick(lastResourceTick);
	}

	public double getSpeed() {
		return flagEntity.getSpeed();
	}

	public void setSpeed(double speed) {
		flagEntity.setSpeed(speed);
	}

	public int getPointId() {
		return flagEntity.getPointId();
	}

	public void setPointId(int pointId) {
		flagEntity.setPointId(pointId);
	}

	public int getOwnIndex() {
		return flagEntity.getOwnIndex();
	}

	public void setOwnIndex(int ownIndex) {
		flagEntity.setOwnIndex(ownIndex);
	}

	public int getOccupyLife() {
		return flagEntity.getOccupyLife();
	}

	public void setOccupyLife(int occupyLife) {
		flagEntity.setOccupyLife(occupyLife);
	}
	
	public void delete() {
		flagEntity.delete();
	}
	
	/**
	 * 是否是母旗
	 */
	public boolean isCenter() {
		return flagEntity.getCenterFlag() == 1;
	}
	
	public Map<String, WarFlagSignUpItem> getSignUpInfos() {
		return flagEntity.getSignUpSet();
	}
	
	public void signUp(WarFlagSignUpItem signUp) {
		Map<String, WarFlagSignUpItem> signUpInfos = getSignUpInfos();
		signUpInfos.put(signUp.getPlayerId(), signUp);
		flagEntity.notifyUpdate();
	}
	
	public void rmSignUpInfo(String playerId) {
		Map<String, WarFlagSignUpItem> signUpInfos = getSignUpInfos();
		signUpInfos.remove(playerId);
		flagEntity.notifyUpdate();
	}
	
	public void clearSignUpInfo() {
		flagEntity.clearSignUpInfo();
	}
	
	public void notifyUpdate() {
		flagEntity.notifyUpdate();
	}
	
	public long getCenterNextTickTime() {
		return flagEntity.getCenterNextTickTime();
	}

	public void setCenterNextTickTime(long centerNextTickTime) {
		flagEntity.setCenterNextTickTime(centerNextTickTime);
	}
	
	public void setCenterAvtive(boolean active) {
		flagEntity.setCenterActive(active ? 1 : 0);
	}
	
	public boolean isCenterActive() {
		return flagEntity.getCenterActive() > 0;
	}
	
	public long getRemoveTime() {
		return flagEntity.getRemoveTime();
	}

	public void setRemoveTime(long removeTime) {
		flagEntity.setRemoveTime(removeTime);
	}
}
