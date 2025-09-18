package com.hawk.game.world.march.impl;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.game.config.NationConstCfg;
import com.hawk.game.config.NationConstructionQuestCfg;
import com.hawk.game.entity.NationBuildQuestEntity;
import com.hawk.game.invoker.NationalBuildMarchReturnMsgInvoker;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.nation.NationService;
import com.hawk.game.nation.NationalBuilding;
import com.hawk.game.nation.construction.NationConstruction;
import com.hawk.game.nation.construction.model.NationalBuildQuestModel;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.ItemType;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.National.NationBuildQuestType;
import com.hawk.game.protocol.National.NationbuildingType;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.PlayerMarch;
import com.hawk.game.world.march.submarch.BasedMarch;
import com.hawk.gamelib.GameConst.MsgId;

/**
 * 国家任务建设行军
 * @author zhenyu.shang
 * @since 2022年4月11日
 */
public class NationalConstructMarch extends PlayerMarch implements BasedMarch {
	
	private static Logger logger = LoggerFactory.getLogger("Server");
	
	private int beforeValue;
	
	private int afterValue;

	/**
	 * 到达，开始做，到家
	 * @param marchEntity
	 */
	public NationalConstructMarch(WorldMarch marchEntity) {
		super(marchEntity);
	}

	@Override
	public WorldMarchType getMarchType() {
		return WorldMarchType.NATIONAL_BUILDING_MARCH;
	}
	
	@Override
	public boolean marchHeartBeats(long time) {
		if (this.getMarchEntity().getMarchStatus() != WorldMarchStatus.MARCH_STATUS_NATION_QUEST_VALUE) {
			return false;
		}
		
		if (this.getMarchEntity().getResEndTime() > HawkTime.getMillisecond()) {
			return false;
		}
		// 结算奖励
		try {
			doCalcAward();
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		// 标识完成返回
		this.getMarchEntity().setMarchIntention(1);
		// 行军返回
		WorldMarchService.getInstance().onMarchReturn(this, this.getMarchEntity().getArmys(), 0);
		return true;
	}
	
	

	private void doCalcAward() {
		String questId = getMarchEntity().getTargetId();
		NationBuildQuestEntity questEntity = getQuestEntity();
		NationalBuildQuestModel model = questEntity.getQuestsMap().get(questId);
		NationConstructionQuestCfg cfg = HawkConfigManager.getInstance().getConfigByKey(NationConstructionQuestCfg.class, model.getQuestCfgId());
		int buildAward = cfg.getNationalBuildAward(); // 获取建设值
		
		NationBuildQuestType questType = getQuestEntity().getQuestType();
		
		MailId mailId = null;
		List<ItemInfo> finalAward = new ArrayList<ItemInfo>();
		// 国家奖励
		if(questType == NationBuildQuestType.NATIONAL){
			NationalBuilding building = NationService.getInstance().getNationBuildingByTypeId(model.getBuildId());
			
			this.beforeValue = building.getEntity().getTotalVal();
			 // 当前是国家类型的，需要给对应的加上建设值
			NationService.getInstance().addBuildQuestVal(model.getBuildId(), buildAward, true);
			
			this.afterValue = building.getEntity().getTotalVal();
			
			mailId = MailId.NATIONAL_NATION_REWARD;
			if(model.isAdvAward()){
				finalAward.addAll(cfg.getNationalBaseAndAdvAward());
			} else {
				finalAward.addAll(cfg.getNationalBaseAwardList());
			}
		} else {
			mailId = MailId.NATIONAL_PERSON_REWARD;
			if(model.isAdvAward()) {
				finalAward.addAll(cfg.getPersonalBaseAndAdvAward());
			} else {
				finalAward.addAll(cfg.getPersonalBaseAwardList());
			}
			
			// 添加个人国家建设值奖励
			Integer buildItemId = NationConstCfg.getInstance().getBuildItemContact(model.getBuildId());
			int buildItemNum = cfg.getPersonalAwardNum();
			if(buildItemId != null && buildItemNum > 0){
				ItemInfo buildItem = new ItemInfo();
				buildItem.setType(ItemType.TOOL_VALUE);
				buildItem.setItemId(buildItemId);
				buildItem.setCount(buildItemNum);
				finalAward.add(buildItem);
			} else {
				logger.error("BuildItemContact add error, buildItemId : {}, buildItemNum: {}", buildItemId, buildItemNum);
			}
		}
		// 发送奖励邮件
		SystemMailService.getInstance().sendMail(MailParames.newBuilder()
				.setMailId(mailId)
				.setPlayerId(this.getPlayerId())
				.addRewards(finalAward)
				.setAwardStatus(MailRewardStatus.NOT_GET)
				.build());
	}
	
	@Override
	public void onMarchStart() {
		NationConstruction construction = (NationConstruction) NationService.getInstance().getNationBuildingByType(NationbuildingType.NATION_BUILDING_CENTER);
		construction.sendQuestInfoToClient(getPlayer());
	}

	@Override
	public void onMarchReach(Player player) {
		this.getMarchEntity().setEndTime(Long.MAX_VALUE);
		//行军到达
		this.onMarchStop(WorldMarchStatus.MARCH_STATUS_NATION_QUEST_VALUE, null, null);
	}
	
	@Override
	public void detailMarchStop(WorldPoint targetPoint) {
		long currentTime = HawkTime.getMillisecond();
		this.getMarchEntity().setResStartTime(currentTime);
		
		String questId = getMarchEntity().getTargetId();
		NationBuildQuestEntity questEntity = getQuestEntity();
		NationalBuildQuestModel model = questEntity.getQuestsMap().get(questId);
		NationConstructionQuestCfg cfg =  HawkConfigManager.getInstance().getConfigByKey(NationConstructionQuestCfg.class, model.getQuestCfgId());
		
		// 计算结束时间
		int taskSoldiers = NationConstCfg.getInstance().getTaskSoldiers();
		// MAX(continueTime/4，continueTime*taskSoldiers*2/（taskSoldiers+出征兵力）) 新版计算公式
		int finalTime = (int) Math.max(cfg.getContinueTime() / 4, cfg.getContinueTime() * taskSoldiers * 2 / (taskSoldiers + getMarchEntity().getArmyFreeCount()));
		
		// 20220707新增进度保存，按进度算出比例
		finalTime = (int) (((100 - model.getCurrentProcess()) / 100.0) * finalTime);
		this.getMarchEntity().setResEndTime(currentTime + finalTime * 1000L);
		NationConstruction construction = (NationConstruction) NationService.getInstance().getNationBuildingByType(NationbuildingType.NATION_BUILDING_CENTER);
		construction.sendQuestInfoToClient(getPlayer());
		logger.info("nationbuildQuestMarch, detailMarchStop, resStartTime:{}, resEndTime:{}", this.getMarchEntity().getResStartTime(), this.getMarchEntity().getResEndTime());
	}
	
	private NationBuildQuestEntity getQuestEntity(){
		return getPlayer().getData().getNationalBuildQuestEntity();
	}

	@Override
	public long getMarchNeedTime() {
		return NationConstCfg.getInstance().getMarchTime() * 1000L;
	}
	
	@Override
	public boolean isNeedCalcTickMarch() {
		return true;
	}
	
	@Override
	public boolean isReachAndStopMarch() {
		return true;
	}
	
	@Override
	public void onMarchReturn() {
		this.doCalcQuest();
	}
	
	public int getBeforeValue() {
		return beforeValue;
	}

	public int getAfterValue() {
		return afterValue;
	}

	private void doCalcQuest() {
		// 投递回玩家线程处理
		getPlayer().dealMsg(MsgId.NATION_BUILD_QUEST_RETURN, new NationalBuildMarchReturnMsgInvoker(getPlayer(), this));
	}
	
	
	@Override
	public void moveCityProcess(long currentTime) {
		WorldMarchService.getInstance().accountMarchBeforeRemove(this);
		if(this.getMarchStatus() != WorldMarchStatus.MARCH_STATUS_RETURN_BACK_VALUE){
			this.doCalcQuest();
		}
	}
}
