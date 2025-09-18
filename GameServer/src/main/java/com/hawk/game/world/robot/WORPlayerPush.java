package com.hawk.game.world.robot;

import java.util.List;
import java.util.Map;

import com.hawk.game.entity.BuildingBaseEntity;
import com.hawk.game.entity.EquipEntity;
import com.hawk.game.entity.StatusDataEntity;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.player.PlayerPush;
import com.hawk.game.player.equip.EquipSlot;
import com.hawk.game.protocol.Army.ArmyChangeCause;
import com.hawk.game.protocol.Common.RedType;
import com.hawk.game.protocol.Const.BuildingStatus;
import com.hawk.game.protocol.Const.EffType;

public class WORPlayerPush extends PlayerPush {

	public WORPlayerPush(Player player) {
		super(player);
	}

	@Override
	public PlayerData getData() {
		// TODO Auto-generated method stub
		return super.getData();
	}

	@Override
	public void syncPlayerInfo() {
	}

	@Override
	public void syncPlayerWorldInfo() {
	}

	@Override
	public void syncPlayerStatusInfo(boolean isLogin, StatusDataEntity... entities) {
	}

	@Override
	public void syncPlayerEffect(EffType... types) {
	}

	@Override
	public void synGlobalBuffInfo() {
	}

	@Override
	public void syncItemInfo() {
	}

	@Override
	public void syncItemInfo(String... ids) {
	}

	@Override
	public void syncTalentInfo() {
	}

	@Override
	public void syncTalentSkillInfo() {
	}

	@Override
	public void syncBuildingEntityInfo() {
	}

	@Override
	public void syncBuildingEntityInfo(List<BuildingBaseEntity> buildingList) {
	}

	@Override
	public void synUnlockedArea() {
	}

	@Override
	public void syncQueueEntityInfo() {
	}

	@Override
	public void syncTechnologyInfo() {
	}

	@Override
	public void syncTechSkillInfo() {
	}

	@Override
	public void syncTechnologyLevelUpFinish(int techId) {
	}

	@Override
	public void syncArmyInfo(ArmyChangeCause cause, Integer... armyIds) {
	}

	@Override
	public void syncArmyInfo(ArmyChangeCause cause, Map<Integer, Integer> soldierAdds) {
	}

	@Override
	public void pushBuildingStatus(BuildingBaseEntity buildingEntity, BuildingStatus status) {
	}

	@Override
	public void syncWorldFavorite() {
	}

	@Override
	public void syncMissionList() {
	}

	@Override
	public void syncCustomData() {
	}

	@Override
	public void syncGuildInfo() {
	}

	@Override
	public void pushLeaveGuild() {
	}

	@Override
	public void syncShieldPlayerInfo() {
	}

	@Override
	public void syncRedPoint(RedType type, String value) {
	}

	@Override
	public void syncStoryMissionInfo() {
	}

	@Override
	public void syncStoryMissionAward(boolean isChapterAward, int missionId, int chapterId) {
	}

	@Override
	public void syncNewlyPointSucc(boolean success, int pointId) {
	}

	@Override
	public void syncGachaInfo() {
	}

	@Override
	public void syncPlayerDiamonds() {
	}

	@Override
	public void notifyMailDeleted(int mailType, List<String> mailIds) {
	}

	@Override
	public void syncGiftList() {
	}

	@Override
	public void syncMonthCardRechargeInfo() {
	}

	@Override
	public void syncMonsterKilled(int monsterCfgId, boolean isWin) {
	}

	@Override
	public void pushMarchRemove(String marchId) {
	}

	@Override
	public void pushMassJoinMarchRemove(String marchId) {
	}

	@Override
	public void syncSecondaryBuildQueue() {
	}

	@Override
	public void syncCityDef(boolean cityOnFireStateChange) {
	}

	@Override
	public void syncBubbleRewardInfo(int bubbleType) {
	}

	@Override
	public void syncGiftReceiveTimes() {
	}

	@Override
	public void syncRecommFriends() {
	}

	@Override
	public void syncMaxMonsterLevel() {
	}

	@Override
	public void syncFriendBuildStatus() {
	}

	@Override
	public void loveAddPush(String sendGiftPlayerId, int loveAdd) {
	}

	@Override
	public void synPlayerDailyData() {
	}

	@Override
	public void syncGuildWarCount() {
	}

	@Override
	public void syncClientCfg() {
	}

	@Override
	public void syncEquipInfo() {
	}

	@Override
	public void syncEquipInfo(List<EquipEntity> entityList) {
	}

	@Override
	public void syncEquipInfo(EquipEntity entity) {
	}

	@Override
	public void syncAddEquipInfo(EquipEntity entity) {
	}

	@Override
	public void syncCommanderEquipSoltInfo() {
	}

	@Override
	public void syncCommanderEquipSoltInfo(EquipSlot equipSolt) {
	}

	@Override
	public void pushHeroList() {
	}

	@Override
	public void syncAllVipBoxStatus(boolean pushAll, boolean isNewly) {
	}

	@Override
	public void syncVipBoxStatus(int vipLevel, boolean status) {
	}

	@Override
	public void syncVipShopItemInfo(int... params) {
	}

	@Override
	public void syncDressInfo() {
	}

	@Override
	public void syncHasFirstRecharge() {
	}

	@Override
	public void clearLastPlayerInfoBuilder() {
	}

	@Override
	public void synPlayerFlag() {
	}

	@Override
	public void syncMilitaryRankAwardState() {
	}

	@Override
	public void notifyGuildFavouriteRedPoint(int type, int manorIndex) {
	}

}
