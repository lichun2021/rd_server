package com.hawk.game.lianmengjunyan.player.npc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.hawk.app.HawkObjModule;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.entifytype.EntityType;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.net.session.HawkSession;
import org.hawk.os.HawkException;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.uuid.HawkUUIDGenerator;
import org.hawk.xid.HawkXID;

import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.ProtocolMessageEnum;
import com.hawk.game.battle.BattleService;
import com.hawk.game.cfgElement.ArmourEffObject;
import com.hawk.game.config.ArmourAdditionalCfg;
import com.hawk.game.config.ArmourBreakthroughCfg;
import com.hawk.game.config.ArmourCfg;
import com.hawk.game.config.ArmourPoolCfg;
import com.hawk.game.config.BattleSoldierCfg;
import com.hawk.game.config.FoggyHeroCfg;
import com.hawk.game.config.ItemCfg;
import com.hawk.game.config.LMJYNpcCfg;
import com.hawk.game.config.PayGiftCfg;
import com.hawk.game.config.PlayerLevelExpCfg;
import com.hawk.game.config.SuperSoldierCfg;
import com.hawk.game.entity.ArmourEntity;
import com.hawk.game.entity.ArmyEntity;
import com.hawk.game.entity.HeroEntity;
import com.hawk.game.entity.ItemEntity;
import com.hawk.game.entity.PlayerBaseEntity;
import com.hawk.game.entity.PlayerEntity;
import com.hawk.game.entity.StatusDataEntity;
import com.hawk.game.entity.SuperSoldierEntity;
import com.hawk.game.item.ArmourAttrTemplate;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.lianmengjunyan.LMJYBattleRoom;
import com.hawk.game.lianmengjunyan.LMJYConst.PState;
import com.hawk.game.lianmengjunyan.LMJYProtocol;
import com.hawk.game.lianmengjunyan.player.ILMJYPlayer;
import com.hawk.game.lianmengjunyan.player.ILMJYPlayerPush;
import com.hawk.game.lianmengjunyan.player.npc.actionScript.ILMJYNPCAction;
import com.hawk.game.lianmengjunyan.player.npc.actionScript.LMJYNPCActionAttack;
import com.hawk.game.lianmengjunyan.player.npc.actionScript.LMJYNPCActionSpy;
import com.hawk.game.lianmengjunyan.worldmarch.ILMJYWorldMarch;
import com.hawk.game.lianmengjunyan.worldmarch.LMJYAttackPlayerMarch;
import com.hawk.game.lianmengjunyan.worldmarch.submarch.ILMJYMassMarch;
import com.hawk.game.manager.AssembleDataManager;
import com.hawk.game.player.PlayerData;
import com.hawk.game.player.PlayerSerializeData;
import com.hawk.game.player.hero.NPCHero;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.player.supersoldier.SuperSoldier;
import com.hawk.game.protocol.Armour.ArmourBriefInfo;
import com.hawk.game.protocol.Armour.ArmourSuitType;
import com.hawk.game.protocol.Army.ArmySoldierPB;
import com.hawk.game.protocol.Chat.HPChatState;
import com.hawk.game.protocol.Common.SystemEvent;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Hero.PBHeroState;
import com.hawk.game.protocol.IDIP.NoticeMode;
import com.hawk.game.protocol.IDIP.NoticeType;
import com.hawk.game.protocol.Login.HPLogin;
import com.hawk.game.protocol.Manhattan.PBDeployedSwInfo;
import com.hawk.game.protocol.Player.LoginWay;
import com.hawk.game.protocol.Player.PlayerStatus;
import com.hawk.game.protocol.Rank.RankType;
import com.hawk.game.protocol.SuperSoldier.PBSuperSoldierState;
import com.hawk.game.protocol.World.SignatureState;
import com.hawk.game.protocol.World.WorldMarchReq;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.protocol.World.WorldPointPB.Builder;
import com.hawk.game.service.ArmyService;
import com.hawk.game.util.EffectParams;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.KeyValuePair;
import com.hawk.game.util.RandomUtil;
import com.hawk.health.entity.UpdateUserInfoResult;
import com.hawk.log.Action;
import com.hawk.log.LogConst.PowerChangeReason;
import com.hawk.sdk.msdk.entity.PayItemInfo;

public class LMJYNPCPlayer extends ILMJYPlayer {
	private boolean dead;
	private LMJYNpcCfg npcCfg;
	private String id;
	/** 虎牢关战场id */
	private String lmjyRoomId;
	/** 虎牢关 1 准备进入 2 在战场中 */
	private PState lmjyState;
	private boolean inited;
	private List<ILMJYNPCAction> actionList;
	private long nextTick;
	/**这之前不执行出兵*/
	private long nextMarch;
	public LMJYNPCPlayer(int lmjyNpcId) {
		super(HawkXID.nullXid());
		this.npcCfg = HawkConfigManager.getInstance().getConfigByKey(LMJYNpcCfg.class, lmjyNpcId);
		this.id = BattleService.NPC_ID + HawkUUIDGenerator.genUUID();
	}

	/** 取得指定页套装 */
	@Override
	public ArmourBriefInfo genArmourBriefInfo(ArmourSuitType suit) {
		return super.genArmourBriefInfo(suit);
	}

	/** 军队以及守军战力 */
	public double armyBattlePoint() {
		double armyBattlePoint = 0;
		for (ArmyEntity armyEntity : getData().getArmyEntities()) {
			BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, armyEntity.getArmyId());
			if (cfg == null) {
				continue;
			}
			armyBattlePoint += cfg.getPower() * armyEntity.getFree();
		}

		List<ILMJYWorldMarch> helpMarchList = assisReachMarches();
		for (ILMJYWorldMarch march : helpMarchList) {
			armyBattlePoint += march.armyBattlePoint();

		}
		return armyBattlePoint;
	}

	@Override
	public int getSoldierStar(int armyId) {
		return Math.max(0, npcCfg.getCityLevel() - 30);
	}
	
	@Override
	public int getSoldierStep(int armyId) {
		return 0;
	}

	@Override
	public boolean hasCollege() {
		return false;
	}

	@Override
	public boolean onTick() {
		long now = HawkTime.getMillisecond();
		if (now < nextTick) {
			return true;
		}
		if (now < getParent().getStartTime()) {
			return true;
		}
		if (isDead()) {
			return true;
		}

		for (ILMJYNPCAction action : actionList) {
			try {
				if (now > action.getCoolDown() && action.doAction()) {
					action.setLastAction(now);
					action.inCD();
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}

		List<ILMJYWorldMarch> assList = getParent().getPlayerMarches(getId(), WorldMarchStatus.MARCH_STATUS_MARCH_ASSIST, WorldMarchType.ASSISTANCE);
		for (ILMJYWorldMarch march : assList) {
			List<ILMJYWorldMarch> atkMes = getParent().getPointMarches(march.getTerminalId(), WorldMarchStatus.MARCH_STATUS_MARCH,
					WorldMarchType.ATTACK_PLAYER);
			if (atkMes.isEmpty()) {
				march.onMarchCallback();
			}
		}

		nextTick = now + 1000;
		getData().getPlayerEntity().setBattlePoint(getPower());
		return super.onTick();
	}

	@Override
	public boolean onMarchArmyBack(ILMJYWorldMarch worldMarch) {
		nextMarch =  nextTick + getNpcCfg().getMarchNotMarch() * 1000;
		
		return super.onMarchArmyBack(worldMarch);
	}

	@Override
	public void onMarchCome(ILMJYWorldMarch march) {
		if (isDead()) { // 死了就不求了
			return;
		}
		if (march instanceof LMJYAttackPlayerMarch) {
			// asstanR="0.8" 当自己守城兵力战力小于对方进攻兵力战力0.8倍时，要求盟友向自己援助
			if (armyBattlePoint() > march.armyBattlePoint() * npcCfg.getAsstanR()) {
				return;
			}
			List<ILMJYPlayer> plist = getParent().getPlayerList(PState.GAMEING);
			for (ILMJYPlayer player : plist) {
				if (player instanceof LMJYNPCPlayer) {
					if (((LMJYNPCPlayer) player).isDead()) {
						continue;
					}
					if (player == this) {
						continue;
					}
					((LMJYNPCPlayer) player).assisTance(march, this);
				}
			}
		}
	}

	@Override
	public void onMarchStart(ILMJYWorldMarch march) {
		if (march instanceof ILMJYMassMarch) {
			if(nextMarch > nextTick){
				return;
			}
			List<ILMJYPlayer> plist = getParent().getPlayerList(PState.GAMEING);
			for (ILMJYPlayer player : plist) {
				if (player instanceof LMJYNPCPlayer) {
					if (((LMJYNPCPlayer) player).isDead()) {
						continue;
					}
					if (player == this) {
						continue;
					}
					((LMJYNPCPlayer) player).joinMassMarch((ILMJYMassMarch) march);
				}
			}
		}
	}

	public void joinMassMarch(ILMJYMassMarch leaderMarch) {
		WorldMarchReq.Builder req = WorldMarchReq.newBuilder();
		req.setMarchId(leaderMarch.getMarchId());
		req.setPosX(leaderMarch.getOrigionX());
		req.setPosY(leaderMarch.getOrigionY());
		req.addAllHeroId(goMarchHeros());
		req.setSuperSoldierId(goMarchSuperSoldier());
		req.setArmourSuit(ArmourSuitType.ONE);
		req.addAllArmyInfo(goMarchArmys(npcCfg.getAsstanArmyNubmer()));
		req.setType(WorldMarchType.MASS_JOIN);
		HawkProtocol protocol = HawkProtocol.valueOf(HP.code.WORLD_MASS_JOIN_C_VALUE, req);
		sendProtocol(protocol);
	}

	public void assisTance(ILMJYWorldMarch march, LMJYNPCPlayer tar) {
		if(nextMarch > nextTick){
			return;
		}
		boolean assi = npcCfg.randA_B_C_D_E(npcCfg.getAsstanPrefer()).second;
		boolean bfalse = getParent().getPlayerMarches(getId(), WorldMarchType.ASSISTANCE).size() == 0;
		if (bfalse && assi && !tar.isDead()) {
			WorldMarchReq.Builder req = WorldMarchReq.newBuilder();
			req.setPosX(tar.getX());
			req.setPosY(tar.getY());
			req.addAllHeroId(goMarchHeros());
			req.setSuperSoldierId(goMarchSuperSoldier());
			req.setArmourSuit(ArmourSuitType.ONE);
			req.addAllArmyInfo(goMarchArmys(npcCfg.getAsstanArmyNubmer()));

			HawkProtocol protocol = HawkProtocol.valueOf(HP.code.WORLD_ASSISTANCE_C, req);
			sendProtocol(protocol);
		}
	}

	public List<Integer> goMarchHeros() {
		return getAllHero().stream()
				.filter(h -> h.getState() == PBHeroState.HERO_STATE_FREE)
				.sorted(Comparator.comparingInt(PlayerHero::power).reversed())
				.limit(2)
				.map(PlayerHero::getCfgId)
				.collect(Collectors.toList());
	}
	
	public int goMarchSuperSoldier(){
		for(SuperSoldier soldier : getAllSuperSoldier()){
			if(soldier.getState() == PBSuperSoldierState.SUPER_SOLDIER_STATE_FREE){
				return soldier.getCfgId();
			}
		}
		return 0;
	}

	public List<ArmySoldierPB> goMarchArmys(int marchNum) {
		List<ArmySoldierPB> result = new ArrayList<>();
		List<ArmyEntity> armyEntities = getData().getMarchArmy();
		int armycount = armyEntities.stream().mapToInt(ArmyEntity::getFree).sum();
		double per = 1;
		int maxMarchSoldierNum = marchNum;
		if (armycount > maxMarchSoldierNum) {
			per = maxMarchSoldierNum * 1D / armycount;
		}

		for (ArmyEntity army : armyEntities) {
			int value = (int) (army.getFree() * per);
			if (value <= 0) {
				continue;
			}
			result.add(ArmySoldierPB.newBuilder().setArmyId(army.getArmyId()).setCount(value).build());
		}
		return result;
	}

	public void removeAction(ILMJYNPCAction action) {
		actionList.remove(action);
	}

	@Override
	public boolean onProtocol(HawkProtocol protocol) {
		return true;
	}

	public void init() {
		if (inited) {
			return;
		}
		actionList = new ArrayList<>();
		actionList.add(new LMJYNPCActionSpy(this));
		actionList.add(new LMJYNPCActionAttack(this));
		setDeployedSwInfo(PBDeployedSwInfo.newBuilder());
		setMechacoreShowInfo("");
		// actionList.add(new LMJYNPCActionCallbackAttackPlayer(this));

		LMJYNPCPlayerData playerdata = LMJYNPCPlayerData.valueOf(this);
		this.setPlayerData(playerdata);
		this.setPlayerPush(new LMJYNpcPlayerPush(this));
		
		// 英雄
		for (int heroId : npcCfg.getHeroIds()) {
			try {
				FoggyHeroCfg cfg = HawkConfigManager.getInstance().getConfigByKey(FoggyHeroCfg.class, heroId);
				NPCHero hero = NPCHero.create(cfg);
				HeroEntity entity = new HeroEntity();
				entity.setId(HawkUUIDGenerator.genUUID());
				entity.setPlayerId(getId());
				entity.recordHeroObj(hero);
				entity.setHeroId(cfg.getHeroId());
				getData().getHeroEntityList().add(entity);
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}

		for (Entry<Integer, Integer> ent : npcCfg.getArmourmap().entrySet()) {
			try {
				int id = ent.getKey();
				int level = ent.getValue();
				addArmour(id, level);
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}

		for (Entry<Integer, Integer> ent : npcCfg.getSuperSoldiermap().entrySet()) {
			try {
				int soldierId = ent.getKey();
				int star = ent.getValue();
				SuperSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(SuperSoldierCfg.class, soldierId);
				if (cfg == null) {
					continue;
				}
				SuperSoldierEntity newSso = new SuperSoldierEntity();
				newSso.setSoldierId(soldierId);
				newSso.setPlayerId(getId());
				newSso.setStar(star);
				newSso.setState(PBSuperSoldierState.SUPER_SOLDIER_STATE_FREE_VALUE);
				LMJYNPCSuperSoldier hero = LMJYNPCSuperSoldier.create(newSso);
				hero.setParent(this);
				hero.getPassiveSkillSlots().forEach(slot -> slot.getSkill().addExp(3000));
				hero.getSkillSlots().forEach(slot -> slot.getSkill().addExp(3000));

				getData().getSuperSoldierEntityList().add(newSso);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		playerdata.getPlayerEffect().init();
		inited = true;
	}
	
	@Override
	public void addArmour(Integer armourPoolId)	{
		
	}
			

	public void addArmour(Integer armourPoolId ,int level) {
		ArmourPoolCfg poolCfg = HawkConfigManager.getInstance().getConfigByKey(ArmourPoolCfg.class, armourPoolId);
		if (poolCfg == null) {
			logger.error("add armour error, armourPoolId not exit, playerId:{}, armourPoolId:{}", getId(), armourPoolId);
			return;
		}

		ArmourCfg amourCfg = HawkConfigManager.getInstance().getConfigByKey(ArmourCfg.class, poolCfg.getArmourId());
		if (amourCfg == null) {
			logger.error("add armour error, armourId not exit, playerId:{}, armourId:{}", getId(), poolCfg.getArmourId());
			return;
		}

		ArmourEntity entity = new ArmourEntity();
		entity.setId(HawkUUIDGenerator.genUUID());
		entity.setPlayerId(getId());
		entity.setArmourId(poolCfg.getArmourId());
		entity.setQuality(poolCfg.getQuality());
		entity.setPersistable(false);
		entity.setEntityType(EntityType.TEMPORARY);

		// 随机额外属性
		List<ArmourEffObject> extrAttrs = new ArrayList<>();

		// 常规铠甲额外属性随机
		List<Integer> alreadyRandType = new ArrayList<>();
		ArmourBreakthroughCfg armourQualityCfg = HawkConfigManager.getInstance().getConfigByKey(ArmourBreakthroughCfg.class, poolCfg.getQuality());
		List<Integer> randomList = armourQualityCfg.randomAttrQualityList();
		for (int i = 0; i < randomList.size();) {
			int quality = randomList.get(i);
			List<ArmourAdditionalCfg> attrCfgs = AssembleDataManager.getInstance().getArmourAdditionCfgs(1, quality);
			ArmourAdditionalCfg randAttrCfg = RandomUtil.random(attrCfgs);
			ArmourAttrTemplate randAttr = RandomUtil.random(randAttrCfg.getAttrList());
			if (alreadyRandType.contains(randAttr.getEffect())) {
				continue;
			}
			extrAttrs.add(new ArmourEffObject(randAttrCfg.getId(), randAttr.getEffect(), HawkRand.randInt(randAttr.getRandMin(), randAttr.getRandMax())));
			alreadyRandType.add(randAttr.getEffect());
			i++;
		}
		Collections.shuffle(extrAttrs);

		for (ArmourEffObject attr : extrAttrs) {
			entity.addExtraAttrEff(attr);
		}

		// 初始等级为0
		entity.setLevel(level);
		entity.addSuit(ArmourSuitType.ONE_VALUE);
		getData().getArmourEntityList().add(entity);

	}

	@Override
	public int getVipExp() {
		return 0;
	}

	@Override
	public int getVipLevel() {
		return 0;
	}

	@Override
	public int getLevel() {
		return npcCfg.getCityLevel();
	}

	@Override
	public KeyValuePair<SignatureState, String> getSignatureInfo() {
		return new KeyValuePair<SignatureState, String>(SignatureState.CAN_NOT_SIGNATURE_NOT, "");
	}

	/** 获取玩家战力值 */
	public long getPower() {
		int result = 0;
		try {
			double armyBattlePoint = 0;
			double trapBattlePoint = 0;
			List<ArmyEntity> armyEntities = getPlayerData().getArmyEntities();
			for (ArmyEntity armyEntity : armyEntities) {
				int liveSoldiers = armyEntity.getFree() + armyEntity.getMarch();
				BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, armyEntity.getArmyId());
				if (cfg == null) {
					continue;
				}
				if (ArmyService.getInstance().isTrap(cfg.getType())) {
					trapBattlePoint += cfg.getPower() * liveSoldiers;
				} else {
					armyBattlePoint += (cfg.getPower() * liveSoldiers + armyEntity.getAdvancePower());
				}
			}
			result += (int) Math.ceil(armyBattlePoint);
			result += (int) Math.ceil(trapBattlePoint);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return result + npcCfg.getPower();
	}

	@Override
	public String getName() {
		return npcCfg.getPlayerName();
	}

	@Override
	public String getGuildId() {
		return "NPC_GUILD";
	}

	@Override
	public int getCityLevel() {

		return npcCfg.getCityLevel();
	}
	
	@Override
	public int getCityPlantLv(){
		return 0;
	}

	@Override
	public boolean hasGuild() {

		return true;
	}

	@Override
	public String getId() {
		return id;
	}

	public String getLmjyRoomId() {
		return lmjyRoomId;
	}

	public void setLmjyRoomId(String lmjyRoomId) {
		this.lmjyRoomId = lmjyRoomId;
	}

	public PState getLmjyState() {
		return lmjyState;
	}

	public void setLmjyState(PState lmjyState) {
		this.lmjyState = lmjyState;
	}

	@Override
	public String getGuildName() {
		return npcCfg.getGuildName();
	}

	@Override
	public String getGuildTag() {
		return npcCfg.getGuildTag();
	}

	@Override
	public void sendError(int hpCode, int errCode, int errFlag, String... params) {
	}

	@Override
	public void sendError(int hpCode, ProtocolMessageEnum errCode, int errFlag) {
	}

	public void sendError(int hpCode, ProtocolMessageEnum errCode) {
	}

	@Override
	public boolean sendProtocol(HawkProtocol protocol) {
		for (Entry<Integer, HawkObjModule> entry : getParent().getObjModules().entrySet()) {
			try {
				if (entry.getValue().isListenProto(protocol.getType())) {
					entry.getValue().onProtocol(LMJYProtocol.valueOf(protocol, this));
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		return true;
	}

	@Override
	public boolean sendProtocol(HawkProtocol protocol, long delayTime) {
		return true;
	}

	@Override
	public void responseSuccess(int hpCode) {
	}

	///////////////////////////////////////////////////////////////////////////////////

	@Override
	public int getMarchCount() {
		// TODO Auto-generated method stub
		return super.getMarchCount();
	}

	@Override
	public LMJYBattleRoom getParent() {
		// TODO Auto-generated method stub
		return super.getParent();
	}

	@Override
	public void setParent(LMJYBattleRoom parentRoom) {
		// TODO Auto-generated method stub
		super.setParent(parentRoom);
	}

	@Override
	public boolean isInSameGuild(ILMJYPlayer tar) {
		return Objects.equals(getGuildId(), tar.getGuildId());
	}

	@Override
	public void initModules() {
	}

	@Override
	public int getPlayerPos() {
		// TODO Auto-generated method stub
		return super.getPlayerPos();
	}

	@Override
	public int[] getPosXY() {
		// TODO Auto-generated method stub
		return super.getPosXY();
	}

	@Override
	public int getX() {
		// TODO Auto-generated method stub
		return super.getX();
	}

	@Override
	public int getPointId() {
		// TODO Auto-generated method stub
		return super.getPointId();
	}

	@Override
	public int getY() {
		// TODO Auto-generated method stub
		return super.getY();
	}

	@Override
	public Builder toBuilder(String viewerId) {
		Builder result = super.toBuilder(viewerId);
		result.setLmjyNpc(1);
		if (isDead()) {
			result.setLmjyState(1);
		}
		return result;
	}

	@Override
	public com.hawk.game.protocol.World.WorldPointDetailPB.Builder toDetailBuilder(String viewerId) {
		com.hawk.game.protocol.World.WorldPointDetailPB.Builder result = super.toDetailBuilder(viewerId);
		result.setLmjyNpc(1);
		if (isDead()) {
			result.setLmjyState(1);
		}
		return result;
	}

	@Override
	public void updateData(PlayerData playerData) {
		// TODO Auto-generated method stub
		super.updateData(playerData);
	}

	@Override
	public ILMJYPlayerPush getPush() {
		// TODO Auto-generated method stub
		return super.getPush();
	}

	@Override
	public int getAoiObjId() {
		// TODO Auto-generated method stub
		return super.getAoiObjId();
	}

	@Override
	public void setAoiObjId(int aoiObjId) {
		// TODO Auto-generated method stub
		super.setAoiObjId(aoiObjId);
	}

	@Override
	public boolean isBackground() {
		// TODO Auto-generated method stub
		return super.isBackground();
	}

	@Override
	public void resetParam() {
		// TODO Auto-generated method stub
		super.resetParam();
	}

	@Override
	public void setBackground(long backgroundTime) {
		// TODO Auto-generated method stub
		super.setBackground(backgroundTime);
	}

	@Override
	public long getBackground() {
		// TODO Auto-generated method stub
		return super.getBackground();
	}

	@Override
	public JSONObject getPfTokenJson() {
		// TODO Auto-generated method stub
		return super.getPfTokenJson();
	}

	@Override
	public void setPfTokenJson(JSONObject pfTokenJson) {
		// TODO Auto-generated method stub
		super.setPfTokenJson(pfTokenJson);
	}

	@Override
	public long getHealthGameUpdateTime() {
		// TODO Auto-generated method stub
		return super.getHealthGameUpdateTime();
	}

	@Override
	public void setHealthGameUpdateTime(long healthGameUpdateTime) {
		// TODO Auto-generated method stub
		super.setHealthGameUpdateTime(healthGameUpdateTime);
	}

	@Override
	public long getNextRemindTime() {
		// TODO Auto-generated method stub
		return super.getNextRemindTime();
	}

	@Override
	public void setNextRemindTime(long nextRemindTime) {
		// TODO Auto-generated method stub
		super.setNextRemindTime(nextRemindTime);
	}

	@Override
	public long getHealthGameTickTime() {
		// TODO Auto-generated method stub
		return super.getHealthGameTickTime();
	}

	@Override
	public void setHealthGameTickTime(long healthGameTickTime) {
		// TODO Auto-generated method stub
		super.setHealthGameTickTime(healthGameTickTime);
	}

	@Override
	public boolean isAdult() {
		// TODO Auto-generated method stub
		return super.isAdult();
	}

	@Override
	public void setAdult(boolean adult) {
		// TODO Auto-generated method stub
		super.setAdult(adult);
	}

	@Override
	public void clearData(boolean isRemove) {
		// TODO Auto-generated method stub
		super.clearData(isRemove);
	}

	@Override
	public PlayerEntity getEntity() {
		// TODO Auto-generated method stub
		return super.getEntity();
	}

	@Override
	public PlayerBaseEntity getPlayerBaseEntity() {
		// TODO Auto-generated method stub
		return super.getPlayerBaseEntity();
	}

	@Override
	public long getCreateTime() {
		// TODO Auto-generated method stub
		return super.getCreateTime();
	}

	@Override
	public int getActiveState() {
		// TODO Auto-generated method stub
		return super.getActiveState();
	}

	@Override
	public void setActiveState(int activeState) {
		// TODO Auto-generated method stub
		super.setActiveState(activeState);
	}

	@Override
	public boolean isActiveOnline() {
		// TODO Auto-generated method stub
		return super.isActiveOnline();
	}

	@Override
	public void idipChangeDiamonds(boolean diamondsChange) {
		// TODO Auto-generated method stub
		super.idipChangeDiamonds(diamondsChange);
	}

	@Override
	public List<Integer> getUnlockedSoldierIds() {
		return super.getUnlockedSoldierIds();
	}

	@Override
	public String getPuid() {
		return "don not know";
	}

	@Override
	public String getOpenId() {
		return "";
	}

	@Override
	public String getServerId() {
		return "10005";
	}

	@Override
	public String getNameEncoded() {
		return "utf-8";
	}

	@Override
	public String getNameWithGuildTag() {
		return GameUtil.getPlayerNameWithGuildTag(getGuildId(), getName());
	}

	@Override
	public int getIcon() {
		return npcCfg.getIcon();
	}

	@Override
	public String getPfIcon() {
		return "2_200002_100001";
	}

	@Override
	public int getShowVIPLevel() {
		return 0;
	}

	@Override
	public String getChannel() {
		return "lwt";
	}

	@Override
	public String getCountry() {
		return "cn";
	}

	@Override
	public String getDeviceId() {
		return "";
	}

	@Override
	public int getExp() {
		return 0;
	}

	@Override
	public int getExpDec() {
		return 0;
	}

	@Override
	public String getLanguage() {
		return "java";
	}

	@Override
	public long getLoginTime() {
		return 0;
	}

	@Override
	public long getLogoutTime() {
		return 0;
	}

	@Override
	public int getOnlineTimeHistory() {
		return 0;
	}

	@Override
	public String getClientIp() {
		return "127.0.0.1";
	}

	@Override
	public void setCityLevel(int level) {
	}

	@Override
	public String getGameAppId() {
		return "";
	}

	@Override
	public int getPlatId() {
		return 0;
	}

	@Override
	public String getChannelId() {
		return "lwt";
	}

	@Override
	public void setFirstLogin(int firstLogin) {
	}

	@Override
	public int getFirstLogin() {
		return 0;
	}

	@Override
	public String getPlatform() {
		return "lwt";
	}

	@Override
	public String getPhoneInfo() {
		// TODO Auto-generated method stub
		return super.getPhoneInfo();
	}

	@Override
	public String getTelecomOper() {
		// TODO Auto-generated method stub
		return super.getTelecomOper();
	}

	@Override
	public String getNetwork() {
		return "";
	}

	@Override
	public String getClientHardware() {
		return "";
	}

	@Override
	public int getGold() {
		return 0;
	}

	@Override
	public int getDiamonds() {
		return 0;
	}

	@Override
	public int getVit() {
		return 100;
	}

	@Override
	public int getCoin() {
		return 0;
	}

	@Override
	public int getCityLv() {
		return npcCfg.getCityLevel();
	}

	@Override
	public int getMilitaryRankLevel() {
		// TODO Auto-generated method stub
		return super.getMilitaryRankLevel();
	}

	@Override
	public long[] getPlunderResAry(int[] RES_TYPE) {
		// TODO Auto-generated method stub
		return super.getPlunderResAry(RES_TYPE);
	}

	@Override
	public int getEffPerByResType(int resType, EffType... effTypes) {
		// TODO Auto-generated method stub
		return super.getEffPerByResType(resType, effTypes);
	}

	@Override
	public Map<Integer, Long> getResBuildOutput() {
		return new HashMap<Integer, Long>();
	}

	@Override
	public void decResStoreByPercent(BuildingType buildType, double decPercent) {
	}

	@Override
	public long getGoldore() {
		return 0;
	}

	@Override
	public long getGoldoreUnsafe() {
		return 0;
	}

	@Override
	public long getOil() {
		return 0;
	}

	@Override
	public long getOilUnsafe() {
		return 0;
	}

	@Override
	public long getSteel() {
		return 0;
	}

	@Override
	public long getSteelUnsafe() {
		return 0;
	}

	@Override
	public long getTombarthite() {
		return 0;
	}

	@Override
	public long getTombarthiteUnsafe() {
		return 0;
	}

	@Override
	public long getSteelSpy() {
		return 0;
	}

	@Override
	public long getTombarthiteSpy() {
		return 0;
	}

	@Override
	public void notifyPlayerKickout(int reason, String msg) {
	}

	@Override
	public void kickout(int reason, boolean notify, String msg) {
	}

	@Override
	public void lockPlayer() {
	}

	@Override
	public void unLockPlayer() {
	}

	@Override
	public void unLockPlayer(int errorCode) {
	}

	@Override
	public void setCareBanStartTime(long startTime) {
	}

	@Override
	public void setScoreBatchTime(long scoreBatchTime) {
	}

	@Override
	public long onCityShieldChange(StatusDataEntity entity, long currentTime) {
		return 0;
	}

	@Override
	public void cityShieldRemovePrepareNotice(StatusDataEntity entity, long leftTime) {
	}

	@Override
	public void onBufChange(int statusId, long endTime) {
	}

	@Override
	protected void onProtocolException(int protocolId, int errorCode, String errorMsg) {
		// TODO Auto-generated method stub
		super.onProtocolException(protocolId, errorCode, errorMsg);
	}

	@Override
	public void onSessionClosed() {
		// TODO Auto-generated method stub
		super.onSessionClosed();
	}

	@Override
	public boolean doPlayerAssembleAndLogin(HawkSession session, HPLogin loginCmd) {
		// TODO Auto-generated method stub
		return super.doPlayerAssembleAndLogin(session, loginCmd);
	}

	@Override
	public void noticeSystemEvent(SystemEvent event) {
		// TODO Auto-generated method stub
		super.noticeSystemEvent(event);
	}

	@Override
	public void increaseVit(int count, Action action, boolean isRecover) {
	}

	@Override
	public int getMaxVit() {
		return 100;
	}

	@Override
	public PlayerLevelExpCfg getCurPlayerLevelCfg() {
		// TODO Auto-generated method stub
		return super.getCurPlayerLevelCfg();
	}

	@Override
	public void increaseGold(long gold, Action action) {
	}

	@Override
	public void increaseGold(long gold, Action action, boolean needLog) {
		// TODO Auto-generated method stub
		super.increaseGold(gold, action, needLog);
	}

	@Override
	public boolean consumeGold(int needGold, Action action) {
		return true;
	}

	@Override
	public boolean increaseDiamond(int diamond, Action action) {
		return true;
	}

	@Override
	public boolean increaseDiamond(int diamond, Action action, String extendParam, String midasReason) {
		return true;
	}

	@Override
	public String consumeDiamonds(int diamond, Action action, List<PayItemInfo> payItems) {
		return "";
	}

	@Override
	public void increaseCoin(int coin, Action action) {
	}

	@Override
	public void increaseGuildContribution(int value, Action action, boolean needLog) {
	}

	@Override
	public void consumeCoin(int coin, Action action) {
	}

	@Override
	public List<ItemEntity> increaseTools(ItemInfo itemAdd, Action action, ItemCfg itemCfg) {
		return Collections.emptyList();
	}

	@Override
	public void consumeTool(String id, int itemType, int disCount, Action action) {
	}

	@Override
	public void increaseResource(long addCnt, int resType, Action action) {
	}

	@Override
	public void increaseResource(long addCnt, int resType, Action action, boolean needLog) {
	}

	@Override
	public long getResByType(int type) {
		// TODO Auto-generated method stub
		return super.getResByType(type);
	}

	@Override
	public long getResbyType(PlayerAttr type) {
		// TODO Auto-generated method stub
		return super.getResbyType(type);
	}

	@Override
	public long getAllResByType(int type) {
		// TODO Auto-generated method stub
		return super.getAllResByType(type);
	}

	@Override
	public void consumeResource(long subCnt, int resType, Action action) {
		// TODO Auto-generated method stub
		super.consumeResource(subCnt, resType, action);
	}

	@Override
	public void consumeVit(int vit, Action action) {
		// TODO Auto-generated method stub
		super.consumeVit(vit, action);
	}

	@Override
	public void increaseLevel(int level, Action action) {
		// TODO Auto-generated method stub
		super.increaseLevel(level, action);
	}

	@Override
	public void decreaceVipExp(int subVipExp, Action action) {
		// TODO Auto-generated method stub
		super.decreaceVipExp(subVipExp, action);
	}

	@Override
	public void increaseVipExp(int addExp, Action action) {
		// TODO Auto-generated method stub
		super.increaseVipExp(addExp, action);
	}

	@Override
	public void decreaseExp(int subExp, Action action) {
		// TODO Auto-generated method stub
		super.decreaseExp(subExp, action);
	}

	@Override
	public void increaseExp(int exp, Action action, boolean push) {
		// TODO Auto-generated method stub
		super.increaseExp(exp, action, push);
	}

	@Override
	public int getMaxCaptiveNum() {
		// TODO Auto-generated method stub
		return super.getMaxCaptiveNum();
	}

	@Override
	public int getMaxMassJoinMarchNum() {
		// TODO Auto-generated method stub
		return super.getMaxMassJoinMarchNum();
	}

	@Override
	public int getMaxMarchSoldierNum(EffectParams effParams) {
		return 10000000;
	}

	@Override
	public int getMaxMarchNum() {
		// TODO Auto-generated method stub
		return npcCfg.getMaxMarchNum();
	}

	@Override
	public int getMaxAllMarchSoldierNum(EffectParams effParams) {
		// TODO Auto-generated method stub
		return super.getMaxAllMarchSoldierNum(effParams);
	}

	@Override
	public int getMaxAssistSoldier() {
		// TODO Auto-generated method stub
		return super.getMaxAssistSoldier();
	}

	@Override
	public int getRemainDisabledCap() {
		// TODO Auto-generated method stub
		return super.getRemainDisabledCap();
	}

	@Override
	public int getCannonCap() {
		// TODO Auto-generated method stub
		return super.getCannonCap();
	}

	@Override
	public int getMaxCapNum() {
		return 0;
	}

	@Override
	public int getMarketBurden() {
		// TODO Auto-generated method stub
		return super.getMarketBurden();
	}

	@Override
	public void refreshPowerElectric(PowerChangeReason reason) {
		// TODO Auto-generated method stub
		super.refreshPowerElectric(reason);
	}

	@Override
	public void refreshPowerElectric(boolean isArmyCure, PowerChangeReason reason) {
		// TODO Auto-generated method stub
		super.refreshPowerElectric(isArmyCure, reason);
	}

	@Override
	public void joinGuild(String guildId, boolean isCreate) {
		// TODO Auto-generated method stub
		super.joinGuild(guildId, isCreate);
	}

	@Override
	public void quitGuild(String guildId) {
		// TODO Auto-generated method stub
		super.quitGuild(guildId);
	}

	@Override
	public long getGuildContribution() {
		// TODO Auto-generated method stub
		return super.getGuildContribution();
	}

	@Override
	public void consumeGuildContribution(int value, Action action) {
		// TODO Auto-generated method stub
		super.consumeGuildContribution(value, action);
	}

	@Override
	public int getGuildAuthority() {
		// TODO Auto-generated method stub
		return super.getGuildAuthority();
	}

	@Override
	public int getGuildFlag() {
		// TODO Auto-generated method stub
		return super.getGuildFlag();
	}

	@Override
	public int getFreeBuildingTime() {
		// TODO Auto-generated method stub
		return super.getFreeBuildingTime();
	}

	@Override
	public int getFreeTechTime() {
		// TODO Auto-generated method stub
		return super.getFreeTechTime();
	}

	@Override
	public void unlockArea(int buildAreaId) {
		// TODO Auto-generated method stub
		super.unlockArea(buildAreaId);
	}

	@Override
	public boolean isZeroEarningState() {
		// TODO Auto-generated method stub
		return super.isZeroEarningState();
	}

	@Override
	public long getZeroEarningTime() {
		// TODO Auto-generated method stub
		return super.getZeroEarningTime();
	}

	@Override
	public long getOilConsumeTime() {
		// TODO Auto-generated method stub
		return super.getOilConsumeTime();
	}

	@Override
	public void sendIDIPZeroEarningMsg() {
		// TODO Auto-generated method stub
		super.sendIDIPZeroEarningMsg();
	}

	@Override
	public void sendIDIPZeroEarningMsg(int msgCode) {
		// TODO Auto-generated method stub
		super.sendIDIPZeroEarningMsg(msgCode);
	}

	@Override
	public Optional<PlayerHero> getHeroByCfgId(int heroId) {
		return getAllHero().stream().filter(e -> e.getCfgId() == heroId).findAny();
	}

	@Override
	public Optional<SuperSoldier> getSuperSoldierByCfgId(int soldierId) {
		return getAllSuperSoldier().stream().filter(e -> e.getCfgId() == soldierId).findAny();
	}

	@Override
	public List<PlayerHero> getHeroByCfgId(List<Integer> heroIdList) {
		return getAllHero().stream().filter(e -> heroIdList.contains(e.getCfgId())).collect(Collectors.toList());
	}

	@Override
	public List<PlayerHero> getAllHero() {
		List<PlayerHero> result = getData().getHeroEntityList().stream()
				.map(HeroEntity::getHeroObj)
				.collect(Collectors.toList());
		return result;
	}

	@Override
	public List<SuperSoldier> getAllSuperSoldier() {
		List<SuperSoldier> result = getData().getSuperSoldierEntityList().stream()
				.map(SuperSoldierEntity::getSoldierObj)
				.collect(Collectors.toList());
		return result;
	}

	@Override
	public void productResourceQuickly(long timeLong, boolean safe) {
		// TODO Auto-generated method stub
		super.productResourceQuickly(timeLong, safe);
	}

	@Override
	public boolean collectResource(String buildingId, int buildCfgId, long timeLong, AwardItems award, boolean safe) {
		// TODO Auto-generated method stub
		return super.collectResource(buildingId, buildCfgId, timeLong, award, safe);
	}

	@Override
	public void addRes(double addTotalOre, double addTotalOil, double addTotalSteel, double addTotalRare, AwardItems award, boolean safe) {
		// TODO Auto-generated method stub
		super.addRes(addTotalOre, addTotalOil, addTotalSteel, addTotalRare, award, safe);
	}

	@Override
	public void addSafeRes(double addTotalOre, double addTotalOil, double addTotalSteel, double addTotalRare, AwardItems award) {
		// TODO Auto-generated method stub
		super.addSafeRes(addTotalOre, addTotalOil, addTotalSteel, addTotalRare, award);
	}

	@Override
	public int getHeroEffectValue(int heroId, EffType eType) {
		// TODO Auto-generated method stub
		return super.getHeroEffectValue(heroId, eType);
	}

	@Override
	public List<Integer> getCastedSkill() {
		// TODO Auto-generated method stub
		return super.getCastedSkill();
	}

	@Override
	public void updateRankScore(int msgId, RankType rankType, long score) {
		// TODO Auto-generated method stub
		super.updateRankScore(msgId, rankType, score);
	}

	@Override
	public void removeSkillBuff(int skillId) {
		// TODO Auto-generated method stub
		super.removeSkillBuff(skillId);
	}

	@Override
	public StatusDataEntity addStatusBuff(int buffId, long endTime) {
		// TODO Auto-generated method stub
		return super.addStatusBuff(buffId, endTime);
	}

	@Override
	public StatusDataEntity addStatusBuff(int buffId) {
		// TODO Auto-generated method stub
		return super.addStatusBuff(buffId);
	}

	@Override
	public StatusDataEntity addStatusBuff(int buffId, String targetId) {
		// TODO Auto-generated method stub
		return super.addStatusBuff(buffId, targetId);
	}

	@Override
	public void removeCityShield() {
		// TODO Auto-generated method stub
		super.removeCityShield();
	}

	@Override
	public List<ArmyEntity> defArmy() {
		ConfigIterator<BattleSoldierCfg> it = HawkConfigManager.getInstance().getConfigIterator(BattleSoldierCfg.class);
		Set<Integer> weaponIds = it.stream().filter(BattleSoldierCfg::isDefWeapon).map(BattleSoldierCfg::getId).collect(Collectors.toSet());
		List<ArmyEntity> defSoldiers = this.getData().getArmyEntities().stream().filter(s -> weaponIds.contains(s.getArmyId())).collect(Collectors.toList());
		return defSoldiers;
	}

	@Override
	public List<ArmyEntity> marchArmy() {
		List<ArmyEntity> result = new ArrayList<>(this.getData().getArmyEntities());
		result.removeAll(defArmy());
		return result;
	}

	@Override
	public boolean isRobot() {
		// TODO Auto-generated method stub
		return super.isRobot();
	}

	@Override
	public Set<String> getShieldPlayers() {
		// TODO Auto-generated method stub
		return super.getShieldPlayers();
	}

	@Override
	public void addShieldPlayer(String shieldPlayerId) {
		// TODO Auto-generated method stub
		super.addShieldPlayer(shieldPlayerId);
	}

	@Override
	public void removeShieldPlayer(String shieldPlayerId) {
		// TODO Auto-generated method stub
		super.removeShieldPlayer(shieldPlayerId);
	}

	@Override
	public List<Integer> getUnlockedResourceType() {
		// TODO Auto-generated method stub
		return super.getUnlockedResourceType();
	}

	@Override
	public boolean isSecondaryBuildQueueUsable() {
		// TODO Auto-generated method stub
		return super.isSecondaryBuildQueueUsable();
	}

	@Override
	public void refreshVipBenefitBox() {
		// TODO Auto-generated method stub
		super.refreshVipBenefitBox();
	}

	@Override
	public int checkBalance() {
		// TODO Auto-generated method stub
		return super.checkBalance();
	}

	@Override
	public void rechargeSuccess(int playerSaveAmt, int rechargeAmt, int diamonds) {
		// TODO Auto-generated method stub
		super.rechargeSuccess(playerSaveAmt, rechargeAmt, diamonds);
	}

	@Override
	public String pay(int diamond, String actionName, List<PayItemInfo> payItems) {
		// TODO Auto-generated method stub
		return super.pay(diamond, actionName, payItems);
	}

	@Override
	public boolean cancelPay(int diamond, String billno) {
		// TODO Auto-generated method stub
		return super.cancelPay(diamond, billno);
	}

	@Override
	public int present(int diamond, String extendParam, String actionName, String presentReason) {
		// TODO Auto-generated method stub
		return super.present(diamond, extendParam, actionName, presentReason);
	}

	@Override
	public String payBuyItems(PayGiftCfg giftCfg) {
		// TODO Auto-generated method stub
		return super.payBuyItems(giftCfg);
	}

	@Override
	public boolean isLocker() {
		// TODO Auto-generated method stub
		return super.isLocker();
	}

	@Override
	public void setLocker(boolean isLocker) {
		// TODO Auto-generated method stub
		super.setLocker(isLocker);
	}

	@Override
	public void synPlayerStatus(PlayerStatus status) {
		// TODO Auto-generated method stub
		super.synPlayerStatus(status);
	}

	@Override
	public void synPlayerStatus(PlayerStatus status, int errorCode) {
		// TODO Auto-generated method stub
		super.synPlayerStatus(status, errorCode);
	}

	@Override
	public PlayerSerializeData getPlayerSerializeData() {
		// TODO Auto-generated method stub
		return super.getPlayerSerializeData();
	}

	@Override
	public String toAnchorJsonStr() {
		// TODO Auto-generated method stub
		return super.toAnchorJsonStr();
	}

	@Override
	public JSONObject toAnchorJsonObj() {
		// TODO Auto-generated method stub
		return super.toAnchorJsonObj();
	}

	@Override
	public void updateRemindTime() {
		// TODO Auto-generated method stub
		super.updateRemindTime();
	}

	@Override
	public void healthGameRemind(UpdateUserInfoResult userInfo) {
		// TODO Auto-generated method stub
		super.healthGameRemind(userInfo);
	}

	@Override
	public void sendHealthGameRemind(int type, int peroidTime, int restTime, long endTime) {
		// TODO Auto-generated method stub
		super.sendHealthGameRemind(type, peroidTime, restTime, endTime);
	}

	@Override
	public void increaseMilitaryRankExp(int add, Action action) {
		// TODO Auto-generated method stub
		super.increaseMilitaryRankExp(add, action);
	}

	@Override
	public void addMoneyReissueItem(int moneyCount, Action action, String additionalParam) {
		// TODO Auto-generated method stub
		super.addMoneyReissueItem(moneyCount, action, additionalParam);
	}

	@Override
	public String getAccessToken() {
		// TODO Auto-generated method stub
		return super.getAccessToken();
	}

	@Override
	public int getToolBackGoldToday(int golds) {
		// TODO Auto-generated method stub
		return super.getToolBackGoldToday(golds);
	}

	@Override
	public int getPlayerRegisterDays() {
		// TODO Auto-generated method stub
		return super.getPlayerRegisterDays();
	}

	@Override
	public HPChatState getChatState() {
		// TODO Auto-generated method stub
		return super.getChatState();
	}

	@Override
	public void setChatState(HPChatState chatState) {
		// TODO Auto-generated method stub
		super.setChatState(chatState);
	}

	@Override
	public void sendIdipMsg(String idipMsg) {
		// TODO Auto-generated method stub
		super.sendIdipMsg(idipMsg);
	}

	@Override
	public void sendIdipNotice(NoticeType type, NoticeMode mode, long relieveTime, int msgCode) {
		// TODO Auto-generated method stub
		super.sendIdipNotice(type, mode, relieveTime, msgCode);
	}

	@Override
	public void sendIdipNotice(NoticeType type, NoticeMode mode, long relieveTime, String msg) {
		// TODO Auto-generated method stub
		super.sendIdipNotice(type, mode, relieveTime, msg);
	}

	@Override
	public boolean isBuildingLockByYuri(int buildType, String buildIndex) {
		// TODO Auto-generated method stub
		return super.isBuildingLockByYuri(buildType, buildIndex);
	}

	@Override
	public boolean isBuildingLockByYuri(String buildIndex) {
		// TODO Auto-generated method stub
		return super.isBuildingLockByYuri(buildIndex);
	}

	@Override
	public int getMaxTrainNum() {
		// TODO Auto-generated method stub
		return super.getMaxTrainNum();
	}

	@Override
	public LoginWay getLoginWay() {
		// TODO Auto-generated method stub
		return super.getLoginWay();
	}

	@Override
	public void setLoginWay(LoginWay loginWay) {
		// TODO Auto-generated method stub
		super.setLoginWay(loginWay);
	}

	@Override
	public long getLastPowerScore() {
		// TODO Auto-generated method stub
		return super.getLastPowerScore();
	}

	@Override
	public void setLastPowerScore(long lastPowerScore) {
		// TODO Auto-generated method stub
		super.setLastPowerScore(lastPowerScore);
	}

	@Override
	public String getAppVersion() {
		return super.getAppVersion();
	}

	@Override
	public boolean needJoinGuild() {
		return false;
	}

	public LMJYNpcCfg getNpcCfg() {
		return npcCfg;
	}

	public boolean isDead() {
		return dead;
	}

	public void setDead(boolean dead) {
		this.dead = dead;
	}

	@Override
	public double getMarchSpeedUp() {
		return getParent().getCfg().getNpcMarchSpeedUp();
	}

	public long getNextMarch() {
		return nextMarch;
	}

	public void setNextMarch(long nextMarch) {
		this.nextMarch = nextMarch;
	}
	
	
}
