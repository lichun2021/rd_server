package com.hawk.game.module.autologic;

import com.hawk.activity.type.impl.planetexploration.cfg.PlanetExploreKVCfg;
import com.hawk.game.config.ResTreasureCfg;
import com.hawk.game.entity.ArmyEntity;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.player.Player;
import com.hawk.game.player.manhattan.PlayerManhattanModule;
import com.hawk.game.protocol.Armour.ArmourSuitType;
import com.hawk.game.protocol.Army.ArmySoldierPB;
import com.hawk.game.protocol.MechaCore.MechaCoreSuitType;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Status.AutoGatherErr;
import com.hawk.game.protocol.Talent.TalentType;
import com.hawk.game.protocol.World.AutoGatherParaSetupReq;
import com.hawk.game.protocol.World.AutoMarchPB;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.object.MapBlock;
import com.hawk.game.world.object.Point;
import com.hawk.game.world.service.WorldPointService;

import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;

import java.util.ArrayList;
import java.util.List;

public class PlayerAutoResourceParam {
    /**
     * 玩家ID
     */
    private String playerId;

    /**
     * 普通锅
     */
    private boolean commonResoure;


    /**
     * 大锅
     */
    private boolean superResource;

    
    /**
     * 超大1140003
     */
    private boolean hugeResource;
    
    /**
     * 是否开启自动放锅
     */
    private boolean autoStationPut;

    /**
     * 自动放锅点
     */
    private int stationPutPos;


    /**
     * 是否开启自动拉锅
     */
    private boolean autoStationMarch;

    /**
     * 自动拉锅点
     */
    private int stationMarchPos;

    /**
     * 自动队列列表
     */
    private List<AutoMarchInfo> marchInfos = new ArrayList<>();

    private boolean autoPutResourceEffect = false;
    private int marchIndex;

    public PlayerAutoResourceParam() {
        autoPutResourceEffect = false;
        autoStationMarch = false;
        commonResoure = false;
        superResource = false;
        hugeResource = false;
        autoStationPut = false;
        marchIndex = 1;
    }

    public int checkAutoPut(Player player, AutoGatherParaSetupReq req) {

        Point point = WorldPointService.getInstance().getAreaPoint(req.getPutX(), req.getPutY(), true);

        //如果是阻挡，需要看阻挡是不是锅
        if (point == null) {

            WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(
                    GameUtil.combineXAndY(req.getPutX(), req.getPutY()));
            //如果阻挡是锅，开启自动放锅
            if (worldPoint != null && worldPoint.getPointType() == WorldPointType.RESOURC_TRESURE_VALUE) {
                this.stationPutPos = GameUtil.combineXAndY(req.getPutX(), req.getPutY());
                this.commonResoure = req.getNormalResSel();
                this.superResource = req.getSuperResSel();
                this.hugeResource = req.getHugeResSel();
                tmpLog("autoresource player: {} checkAutoPut RESOURC_TRESURE_VALUE AUTO_PUT_ALREADY_EXIST_VALUE", player.getId());
                return AutoGatherErr.AUTO_PUT_ALREADY_EXIST_VALUE;
            }

            tmpLog("autoresource player: {} checkAutoPut point == null AUTO_PUT_POS_ERR_VALUE", player.getId());
            //否则不开,并返回错误码
            return AutoGatherErr.AUTO_PUT_POS_ERR_VALUE;
        }

        //坐标不符合客户端规范
        if(!point.canPlayerSeat()){
            tmpLog("autoresource player: {} checkAutoPut !point.canPlayerSeat AUTO_PUT_POS_ERR_VALUE", player.getId());
            return AutoGatherErr.AUTO_PUT_POS_ERR_VALUE;
        }

        //黑土地不能放
        if (WorldPointService.getInstance().isInCapitalArea(point.getId())) {
            tmpLog("autoresource player: {} checkAutoPut isInCapitalArea AUTO_PUT_ON_BLACK_NOT_ALLOW_VALUE", player.getId());
            return AutoGatherErr.AUTO_PUT_ON_BLACK_NOT_ALLOW_VALUE;
        }
        // 请求点为阻挡点
        if (MapBlock.getInstance().isStopPoint(point.getId())) {
            tmpLog("autoresource player: {} checkAutoPut isStopPoint AUTO_PUT_POS_ERR_VALUE", player.getId());
            return AutoGatherErr.AUTO_PUT_POS_ERR_VALUE;
        }
        tmpLog("autoresource player: {} checkAutoPut successed", player.getId());
        this.stationPutPos = GameUtil.combineXAndY(req.getPutX(), req.getPutY());
        this.commonResoure = req.getNormalResSel();
        this.superResource = req.getSuperResSel();
        this.hugeResource = req.getHugeResSel();
        return 0;
    }

    public int checkAutoMarch(Player player, AutoGatherParaSetupReq req) {

        this.stationMarchPos = GameUtil.combineXAndY(req.getGatherX(), req.getGatherY());

        Point point = WorldPointService.getInstance().getAreaPoint(req.getGatherX(), req.getGatherY(), false);
        if (point == null) {
            tmpLog("autoresource player: {} checkAutoMarch point == null AUTO_MARCH_POS_ERR_VALUE", player.getId());
            return AutoGatherErr.AUTO_MARCH_POS_ERR_VALUE;
        }
        if ((req.getGatherX() + req.getGatherY()) % 2 == 0) {
            tmpLog("autoresource player: {} checkAutoMarch req.getGatherX() + req.getGatherY()) % 2 == 0 AUTO_MARCH_POS_ERR_VALUE", player.getId());
            return AutoGatherErr.AUTO_MARCH_POS_ERR_VALUE;
        }
        if (WorldPointService.getInstance().isInCapitalArea(point.getId())) {
            tmpLog("autoresource player: {} checkAutoMarch isInCapitalArea AUTO_MARCH_ON_BLACK_NOT_ALLOW_VALUE", player.getId());
            return AutoGatherErr.AUTO_MARCH_ON_BLACK_NOT_ALLOW_VALUE;
        }
        // 请求点为阻挡点
        if (MapBlock.getInstance().isStopPoint(point.getId())) {
            tmpLog("autoresource player: {} checkAutoMarch isStopPoint AUTO_MARCH_POS_ERR_VALUE", player.getId());
            return AutoGatherErr.AUTO_MARCH_POS_ERR_VALUE;
        }
        
        WorldPoint wp = WorldPointService.getInstance().getWorldPoint(req.getGatherX(), req.getGatherY());
        if (wp != null) {
        	if (wp.getPointType() != WorldPointType.RESOURC_TRESURE_VALUE) {
        		tmpLog("autoresource player: {} checkAutoMarch RESOURC_TRESURE_VALUE AUTO_MARCH_TARGET_ERR_VALUE", player.getId());
        		return AutoGatherErr.AUTO_MARCH_TARGET_ERR_VALUE;
        	}
        	
        	ResTreasureCfg resTreCfg = HawkConfigManager.getInstance().getConfigByKey(ResTreasureCfg.class, wp.getResourceId());
        	PlanetExploreKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(PlanetExploreKVCfg.class);
        	if (resTreCfg == null || resTreCfg.getId() == cfg.getRefreshTargetId()) {
        		tmpLog("autoresource player: {} checkAutoMarch RESOURC_TRESURE_VALUE AUTO_MARCH_TARGET_ERR_VALUE -- planetExplore point", player.getId());
        		return AutoGatherErr.AUTO_MARCH_TARGET_ERR_VALUE;
        	}
        } else {
        	 if (!point.canPlayerSeat()) {
        		 tmpLog("autoresource player: {} checkAutoMarch wp == null && !point.canPlayerSeat() AUTO_MARCH_POS_ERR_VALUE", player.getId());
                 return AutoGatherErr.AUTO_MARCH_POS_ERR_VALUE;
        	 }
        }

        List<AutoMarchPB> autoMarchPBList = req.getMarchInfoList();
        int marchCont = player.getMaxMarchNum();
        if (autoMarchPBList.isEmpty()){
            tmpLog("autoresource player: {} checkAutoMarch autoMarchPBList.isEmpty AUTO_MARCH_NO_MARCH_SELECTED_VALUE", player.getId());
            return AutoGatherErr.AUTO_MARCH_NO_MARCH_SELECTED_VALUE;
        }
        if(autoMarchPBList.size() > marchCont) {
            tmpLog("autoresource player: {} checkAutoMarch autoMarchPBList.size() > marchCont AUTO_MARCH_NO_MARCH_SELECTED_VALUE", player.getId());
            return AutoGatherErr.AUTO_MARCH_NO_MARCH_SELECTED_VALUE;
        }

        tmpLog("autoresource player: {} checkAutoMarch success marchCont: {}",
                player.getId(), autoMarchPBList.size());

        this.marchInfos.clear();

        for (AutoMarchPB marchPB : autoMarchPBList) {
            AutoMarchInfo march = AutoMarchInfo.valueOf(player, marchPB, this.marchIndex++);
            if (march == null) {
                return Status.SysError.CONFIG_ERROR_VALUE;
            }
            this.marchInfos.add(march);
        }
        return 0;
    }


    public int checkStart() {
        if (autoStationPut) {
            int[] posArr = GameUtil.splitXAndY(this.stationPutPos);
            Point point = WorldPointService.getInstance().getAreaPoint(posArr[0], posArr[1], false);
            if (point == null) {
                return Status.SysError.CONFIG_ERROR_VALUE;
            }
        }

        if (autoStationMarch) {
            //检查点
            int[] posArr = GameUtil.splitXAndY(this.stationMarchPos);
            Point point = WorldPointService.getInstance().getAreaPoint(posArr[0], posArr[1], false);
            if (point == null) {
                return Status.SysError.CONFIG_ERROR_VALUE;
            }
            if (this.marchInfos.size() <= 0) {
                return Status.SysError.CONFIG_ERROR_VALUE;
            }
        }
        return 0;
    }

	public int getMarchCount() {
		return this.marchInfos.size();
	}

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }


    public boolean isCommonResoure() {
        return commonResoure;
    }


    public void setCommonResoure(boolean commonResoure) {
        this.commonResoure = commonResoure;
    }


    public boolean isSuperResource() {
        return superResource;
    }


    public void setSuperResource(boolean superResource) {
        this.superResource = superResource;
    }
    
    public boolean isHugeResource() {
		return hugeResource;
	}
    
    public void setHugeResource(boolean hugeResource) {
		this.hugeResource = hugeResource;
	}


    public boolean isAutoStationPut() {
        return autoStationPut;
    }

    public void setAutoStationPut(boolean autoStationPut) {
        this.autoStationPut = autoStationPut;
    }

    public int getStationPutPos() {
        return stationPutPos;
    }

    public void setStationPutPos(int stationPutPos) {
        this.stationPutPos = stationPutPos;
    }

    public boolean isAutoStationMarch() {
        return autoStationMarch;
    }

    public void setAutoStationMarch(boolean autoStationMarch) {
        this.autoStationMarch = autoStationMarch;
    }

    public int getStationMarchPos() {
        return stationMarchPos;
    }

    public void setStationMarchPos(int stationMarchPos) {
        this.stationMarchPos = stationMarchPos;
    }

    public List<AutoMarchInfo> getMarchInfos() {
        return marchInfos;
    }

    public void setMarchInfos(List<AutoMarchInfo> marchInfos) {
        this.marchInfos = marchInfos;
    }

    public boolean isAutoPutResourceEffect() {
        return autoPutResourceEffect;
    }

    public void setAutoPutResourceEffect(boolean autoPutResourceEffect) {
        this.autoPutResourceEffect = autoPutResourceEffect;
    }

    /**
     * 打野出征部队信息
     */
    public static class AutoMarchInfo {
        // 编队信息 0-默认，1-编队1,2-编队2，3-编队3， 4-编队4
        private int index;
        // 打野出征部队信息
        private List<ArmyInfo> army;
        // 打野出征英雄ID
        private List<Integer> heroIds;
        // 打野出征机甲ID
        private int superSoldierId;
        // 铠甲
        private int armourSuitType;
        // 机甲核心
        private int mechacoreSuit;
        // 天赋
        private int talent;
        // 超能实验室
        private int superLab;

        private AutoMarchPB autoMarchPB;
        
        public static AutoMarchInfo valueOf(Player player, AutoMarchPB autoMarchPB, int index) {
            List<Integer> heroIds = autoMarchPB.getHeroIdsList();
            int superSoldierId = autoMarchPB.getSuperSoldierId();
            ArmourSuitType armourSuit = autoMarchPB.getArmourSuit();
            TalentType talentType = autoMarchPB.getTalentType();
            List<ArmySoldierPB> armyList = autoMarchPB.getArmyList();
            List<ArmyInfo> armyInfoList = new ArrayList<ArmyInfo>();
            for (ArmySoldierPB armySoldier : armyList) {
                // 不存在的兵种（未训练过的兵种）
                ArmyEntity entity = player.getData().getArmyEntity(armySoldier.getArmyId());
                if (entity == null || armySoldier.getCount() <= 0) {
                    return null;
                }
                ArmyInfo armyInfo = new ArmyInfo(armySoldier.getArmyId(), armySoldier.getCount());
                armyInfoList.add(armyInfo);
            }

            if (autoMarchPB.hasSuperLab() && autoMarchPB.getSuperLab() != 0 && !player.isSuperLabActive(autoMarchPB.getSuperLab())) {
                return null;
            }
            if (autoMarchPB.getManhattan().getManhattanAtkSwId() > 0 || autoMarchPB.getManhattan().getManhattanDefSwId() > 0) {
                // 检测超武信息,判断是否解锁，
                PlayerManhattanModule manhattanModule = player.getModule(GsConst.ModuleType.MANHATTAN);
                if (!manhattanModule.checkMarchReq(autoMarchPB.getManhattan())) {
                    return null;
                }
            }
            MechaCoreSuitType mechaSuit = autoMarchPB.getMechacoreSuit();
            int talent = talentType != null ? talentType.getNumber() : TalentType.TALENT_TYPE_DEFAULT_VALUE;
            AutoMarchInfo autoMarchInfo = new AutoMarchInfo();
            autoMarchInfo.setArmy(armyInfoList);
            autoMarchInfo.setHeroIds(heroIds.subList(0, Math.min(heroIds.size(), 2)));
            autoMarchInfo.setSuperSoldierId(superSoldierId);
            //autoMarchInfo.setPriority(autoMarchPB.getPriority());
            autoMarchInfo.setIndex(index);
            autoMarchInfo.setArmourSuitType(armourSuit != null ? armourSuit.getNumber() : 0);
            autoMarchInfo.setMechacoreSuit(mechaSuit != null ? mechaSuit.getNumber() : MechaCoreSuitType.MECHA_ONE_VALUE);
            autoMarchInfo.setTalent(talent);
            autoMarchInfo.setSuperLab(autoMarchPB.getSuperLab());
            autoMarchInfo.setAutoMarchPB(autoMarchPB);
            return autoMarchInfo;
        }

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public List<ArmyInfo> getArmy() {
            return army;
        }

        public void setArmy(List<ArmyInfo> army) {
            this.army = army;
        }

        public List<Integer> getHeroIds() {
            return heroIds;
        }

        public void setHeroIds(List<Integer> heroIds) {
            this.heroIds = heroIds;
        }

        public int getSuperSoldierId() {
            return superSoldierId;
        }

        public void setSuperSoldierId(int superSoldierId) {
            this.superSoldierId = superSoldierId;
        }


        public int getArmourSuitType() {
            return armourSuitType;
        }

        public void setArmourSuitType(int armourSuitType) {
            this.armourSuitType = armourSuitType;
        }

        public int getTalent() {
            return talent;
        }

        public void setTalent(int talent) {
            this.talent = talent;
        }

        public int getSuperLab() {
            return superLab;
        }

        public void setSuperLab(int superLab) {
            this.superLab = superLab;
        }

		public AutoMarchPB getAutoMarchPB() {
			return autoMarchPB;
		}

		public void setAutoMarchPB(AutoMarchPB autoMarchPB) {
			this.autoMarchPB = autoMarchPB;
		}

		public int getMechacoreSuit() {
			return mechacoreSuit;
		}

		public void setMechacoreSuit(int mechacoreSuit) {
			this.mechacoreSuit = mechacoreSuit;
		}
    }

    public void tmpLog(String msg, Object... params){
        if (params != null && params.length > 0) {
            HawkLog.debugPrintln(msg, params);
        } else {
            HawkLog.debugPrintln(msg);
        }

    }
    public void tmpDebugLog(String msg, Object... params){
        if (params != null && params.length > 0) {
            HawkLog.debugPrintln(msg, params);
        } else {
            HawkLog.debugPrintln(msg);
        }
    }
}
