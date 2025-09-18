package com.hawk.game.president;

import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkTime;

import com.hawk.game.config.PresidentConstCfg;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.President.PresidentResourceInfoSyn;

public class PresidentResource {
	private static PresidentResource instance = new PresidentResource();
	/**
	 * 资源的类型
	 */
	private int attrType;
	
	public static PresidentResource getInstance() {
		return instance;
	}
	
	private PresidentResource() {
		//测试的时候调用.
		init();
	} 
	
	public void init() {
		PresidentResourceInfoSyn.Builder sbuilder = LocalRedis.getInstance().getPresidentResource();
		if (sbuilder == null) {
			sbuilder = PresidentResourceInfoSyn.newBuilder();
			sbuilder.setAttrType(0);
			sbuilder.setLastSetTime(0);
		}
		
		this.attrType = sbuilder.getAttrType();
		LocalRedis.getInstance().addOrUpdatePresidentResource(sbuilder);
	}
	
	public int getAttrType() {
		return attrType;
	}

	public void setAttrType(int attrType) {
		this.attrType = attrType;
	}
	
	/**
	 * 设置资源
	 * @param player
	 * @param attrType
	 * @return
	 */
	public int setPresidentResource(Player player, int attrType) {
		switch (attrType) {
		case PlayerAttr.GOLDORE_UNSAFE_VALUE:
		case PlayerAttr.OIL_UNSAFE_VALUE:
		case PlayerAttr.STEEL_UNSAFE_VALUE:
		case PlayerAttr.TOMBARTHITE_UNSAFE_VALUE:
			break;
		default:
			HawkLog.warnPrintln("playerId:{} president set resource type incorrect type:{}", player.getId(), attrType);
			return Status.SysError.PARAMS_INVALID_VALUE;				
		}
		int curTime = HawkTime.getSeconds();
		PresidentResourceInfoSyn.Builder sbuilder = LocalRedis.getInstance().getPresidentResource();
		//配置
		int cdTime = PresidentConstCfg.getInstance().getChangeResCd();
		if (sbuilder.getLastSetTime() + cdTime > curTime) {
			return Status.Error.PRESIDENT_RESOURCE_CD_VALUE;
		}
		
		
		this.attrType = attrType;
		sbuilder.setAttrType(attrType);
		sbuilder.setLastSetTime(HawkTime.getSeconds());
		
		LocalRedis.getInstance().addOrUpdatePresidentResource(sbuilder);
		
		return Status.SysError.SUCCESS_OK_VALUE;
	}
	
	
	public void synPresidentResouceInfo(Player player, PresidentResourceInfoSyn.Builder sbuilder) {
		HawkProtocol hawkProtocol = HawkProtocol.valueOf(HP.code.PRESIDENT_RESOURCE_INFO_SYN_VALUE, sbuilder);
		
		player.sendProtocol(hawkProtocol);
	}
	/**
	 * 同步资源设置信息
	 * @param player
	 */
	public void synPresidentResouceInfo(Player player) {
		PresidentResourceInfoSyn.Builder sbuilder = LocalRedis.getInstance().getPresidentResource();
		this.synPresidentResouceInfo(player, sbuilder);
	}
	
	
}
