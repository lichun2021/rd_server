package com.hawk.game.module.nation;

import org.hawk.annotation.MessageHandler;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.activity.helper.PlayerAcrossDayLoginMsg;
import com.hawk.game.config.NationConstCfg;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.nation.NationService;
import com.hawk.game.nation.NationalConst;
import com.hawk.game.nation.ship.NationShipFactory;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.president.PresidentFightService;
import com.hawk.game.president.PresidentOfficier;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.National.NationBuildingState;
import com.hawk.game.protocol.National.NationRedDot;
import com.hawk.game.protocol.National.NationbuildingType;
import com.hawk.game.protocol.National.ShipComponents;
import com.hawk.game.protocol.National.UpgradeShipComponent;
import com.hawk.game.protocol.Status;
import com.hawk.game.util.GsConst;
import com.hawk.game.world.thread.WorldTask;
import com.hawk.game.world.thread.WorldThreadScheduler;

/**
 * 飞船制造厂
 * @author zhenyu.shang
 * @since 2022年4月20日
 */
public class PlayerNationShipFactoryModule extends PlayerModule {
	
	public static final Logger logger = LoggerFactory.getLogger("Server");
	
	public PlayerNationShipFactoryModule(Player player) {
		super(player);
	}
	
	
	@Override
	protected boolean onPlayerLogin() {
		if(!player.isCsPlayer()){
			checkRD(this.player);
		}
		return super.onPlayerLogin();
	}


	private void checkRD(Player player) {
		try {
			NationShipFactory shipFactory = (NationShipFactory) NationService.getInstance().getNationBuildingByType(NationbuildingType.NATION_SHIP_FACTORY);
			if (shipFactory == null || shipFactory.getLevel() <= 0) {
				return;
			}
			// 判断是否已经有正在升级中得部件
			if(player.getData().getDailyDataEntity().getNationShipAssist() == 0 && shipFactory.getCurrentUpEntity() != null){
				player.updateNationRDAndNotify(NationRedDot.SHIP_IDLE);
			} else {
				player.rmNationRDAndNotify(NationRedDot.SHIP_IDLE);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	@MessageHandler
	public void onCrossDay(PlayerAcrossDayLoginMsg msg) {
		checkRD(this.player);
	}
	
	/**
	 * 请求打开国家面板
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code2.NATIONAL_SHIPFACTORY_INFO_C_VALUE)
	private boolean onGetNationShipInfo(HawkProtocol protocol) {
		// 直接选飞船制造厂
		NationShipFactory shipFactory = (NationShipFactory) NationService.getInstance().getNationBuildingByType(NationbuildingType.NATION_SHIP_FACTORY);
		shipFactory.sendInfoToClient(player);
		return true;
	}

	/**
	 * 请求强化部件
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code2.NATIONAL_SHIPFACTORY_UP_PART_REQ_VALUE)
	private boolean onUpNationShipPart(HawkProtocol protocol) {
		// 这里要判断一下权限，八大功臣和国王
		if(!PresidentFightService.getInstance().isPresidentPlayer(player.getId()) 
				&& !PresidentOfficier.getInstance().isMeritoriousOfficials(player.getId())){
			player.sendError(HP.code2.NATIONAL_SHIPFACTORY_UP_PART_REQ_VALUE, Status.Error.ONLY_OFFICER_TO_OPER, 0);
			return true;
		}
		NationShipFactory shipFactory = (NationShipFactory) NationService.getInstance().getNationBuildingByType(NationbuildingType.NATION_SHIP_FACTORY);
		// 飞船重建好了才可以强化
		if(shipFactory.getEntity().getLevel() < 1) {
			return true;
		}
		// 判断是否在升级中
		if(shipFactory.getBuildState() == NationBuildingState.BUILDING){
			return false;
		}
		// 判断是否已经有正在升级中得部件
		if(shipFactory.getCurrentUpEntity() != null) {
			player.sendError(HP.code2.NATIONAL_SHIPFACTORY_UP_PART_REQ_VALUE, Status.Error.NATION_SHIP_ALEADY_UPGRADE, 0);
			return true;
		}
		
		UpgradeShipComponent req = protocol.parseProtocol(UpgradeShipComponent.getDefaultInstance());
		ShipComponents comp = req.getType();
		
		// 投递到世界线程升级
		WorldThreadScheduler.getInstance().postWorldTask(new WorldTask(GsConst.WorldTaskType.NATIONAL_SHIP_UPGRADE) {
			@Override
			public boolean onInvoke() {
				// 开始升级
				shipFactory.upShipPart(comp, player);
				
				// 通知其他玩家可助力红点
				for (Player player : GlobalData.getInstance().getOnlinePlayers()) {
					checkRD(player);
				}
				return true;
			}
		});
		
		return true;
	}
	
	/**
	 * 请求取消强化
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code2.NATIONAL_SHIPFACTORY_CANCEL_PART_REQ_VALUE)
	private boolean onCancelShipPart(HawkProtocol protocol) {
		// 这里要判断一下权限，八大功臣和国王
		if(!PresidentFightService.getInstance().isPresidentPlayer(player.getId()) 
				&& !PresidentOfficier.getInstance().isMeritoriousOfficials(player.getId())){
			player.sendError(HP.code2.NATIONAL_SHIPFACTORY_UP_PART_REQ_VALUE, Status.Error.ONLY_OFFICER_TO_OPER, 0);
			return true;
		}
		NationShipFactory shipFactory = (NationShipFactory) NationService.getInstance().getNationBuildingByType(NationbuildingType.NATION_SHIP_FACTORY);
		// 判断是否已经有正在升级中得部件
		if(shipFactory.getCurrentUpEntity() == null) {
			player.sendError(HP.code2.NATIONAL_SHIPFACTORY_CANCEL_PART_REQ_VALUE, Status.Error.NATION_SHIP_NOT_UPGRADE, 0);
			return true;
		}
		// 判断是否有取消CD
		long now = HawkTime.getMillisecond();
		long cd = LocalRedis.getInstance().getNationCancelCd(NationbuildingType.NATION_SHIP_FACTORY.toString());
		if(cd > 0 && cd > now){
			long left = cd - now;
			player.sendError(HP.code2.NATIONAL_SHIPFACTORY_CANCEL_PART_REQ_VALUE, Status.Error.NATION_CANCEL_IN_CD_VALUE, 0, NationalConst.formatTime(left));
			return true;
		}
		// 设置取消CD
		LocalRedis.getInstance().setNationCancelCd(NationbuildingType.NATION_SHIP_FACTORY.toString(), now + NationConstCfg.getInstance().getModelGiveUpCD() * 1000L);
		// 投递到世界线程升级
		WorldThreadScheduler.getInstance().postWorldTask(new WorldTask(GsConst.WorldTaskType.NATIONAL_SHIP_CANCEL_UPGRADE) {
			@Override
			public boolean onInvoke() {
				// 开始升级
				shipFactory.cancelUpShipPart(player);
				return true;
			}
		});
		
		// 通知其他玩家取消红点
		for (Player player : GlobalData.getInstance().getOnlinePlayers()) {
			player.rmNationRDAndNotify(NationRedDot.SHIP_IDLE);
		}
		return true;
	}
	
	/**
	 * 请求助力
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code2.NATIONAL_SHIPFACTORY_ASSIST_REQ_VALUE)
	private boolean onShipAssist(HawkProtocol protocol) {
		NationShipFactory shipFactory = (NationShipFactory) NationService.getInstance().getNationBuildingByType(NationbuildingType.NATION_SHIP_FACTORY);
		// 判断是否已经有正在升级中得部件
		if(shipFactory.getCurrentUpEntity() == null) {
			player.sendError(HP.code2.NATIONAL_SHIPFACTORY_ASSIST_REQ_VALUE, Status.Error.NATION_SHIP_NOT_UPGRADE, 0);
			return true;
		}
		// 判断玩家是否还有助力次数
		if(player.getData().getDailyDataEntity().getNationShipAssist() > 0){
			player.sendError(HP.code2.NATIONAL_SHIPFACTORY_ASSIST_REQ_VALUE, Status.Error.ASSIST_TIMES_NOT_ENOUGH, 0);
			return true;
		}
		// 扣取次数
		player.getData().getDailyDataEntity().setNationShipAssist(1);
		// 投递到世界线程进行助力
		WorldThreadScheduler.getInstance().postWorldTask(new WorldTask(GsConst.WorldTaskType.NATIONAL_SHIP_CANCEL_UPGRADE) {
			@Override
			public boolean onInvoke() {
				// 开始升级
				shipFactory.assistTime(player);
				return true;
			}
		});
		// 去掉红点
		player.rmNationRDAndNotify(NationRedDot.SHIP_IDLE);
		return true;
	}	
}
