package com.hawk.activity.type;

import com.hawk.activity.type.impl.deepTreasure.DeepTreasureActivity;
import com.hawk.activity.type.impl.deepTreasure.DeepTreasureHandler;
import com.hawk.activity.type.impl.deepTreasure.DeepTreasureTimeController;
import com.hawk.activity.type.impl.deepTreasure.entity.DeepTreasureEntity;
import com.hawk.activity.type.impl.homeLandWheel.HomeLandRoundActivity;
import com.hawk.activity.type.impl.homeLandWheel.HomeLandRoundActivityHandler;
import com.hawk.activity.type.impl.homeLandWheel.HomeLandRoundTimeController;
import com.hawk.activity.type.impl.homeLandWheel.entity.HomeLandRoundEntity;
import com.hawk.activity.type.impl.growUpBoost.GrowUpBoostActivity;
import com.hawk.activity.type.impl.growUpBoost.GrowUpBoostHandler;
import com.hawk.activity.type.impl.growUpBoost.GrowUpBoostTimeController;
import com.hawk.activity.type.impl.growUpBoost.entity.GrowUpBoostEntity;
import com.hawk.activity.type.impl.guildBack.GuildBackActivity;
import com.hawk.activity.type.impl.guildBack.GuildBackHandler;
import com.hawk.activity.type.impl.guildBack.GuildBackTimeController;
import com.hawk.activity.type.impl.guildBack.entity.GuildBackEntity;
import com.hawk.activity.type.impl.guildDragonAttack.GuildDragonAttackActivity;
import com.hawk.activity.type.impl.guildDragonAttack.GuildDragonAttactHandler;
import com.hawk.activity.type.impl.guildDragonAttack.GuildDragonAttactTimeController;
import com.hawk.activity.type.impl.guildDragonAttack.entity.GuildDragonAttackEntry;
import com.hawk.activity.type.impl.homeland.HomeLandPuzzleActivity;
import com.hawk.activity.type.impl.homeland.HomeLandPuzzleActivityHandler;
import com.hawk.activity.type.impl.homeland.HomeLandPuzzleTimeController;
import com.hawk.activity.type.impl.homeland.entity.HomeLandPuzzleEntity;
import com.hawk.activity.type.impl.jijiaSkin.JijiaSkinActivity;
import com.hawk.activity.type.impl.jijiaSkin.JijiaSkinHandler;
import com.hawk.activity.type.impl.jijiaSkin.JijiaSkinTimeController;
import com.hawk.activity.type.impl.jijiaSkin.entity.JijiaSkinEntity;
import com.hawk.activity.type.impl.newStart.NewStartActivity;
import com.hawk.activity.type.impl.newStart.NewStartHandler;
import com.hawk.activity.type.impl.newStart.NewStartTimeController;
import com.hawk.activity.type.impl.newStart.entity.NewStartEntity;
import com.hawk.activity.type.impl.plantSoldierFactory.PlantSoldierFactoryActivity;
import com.hawk.activity.type.impl.plantSoldierFactory.PlantSoldierFactoryHandler;
import com.hawk.activity.type.impl.plantSoldierFactory.PlantSoldierFactoryTimeController;
import com.hawk.activity.type.impl.plantSoldierFactory.entity.PlantSoldierFactoryActivityEntity;
import com.hawk.activity.type.impl.returnUpgrade.ReturnUpgradeActivity;
import com.hawk.activity.type.impl.returnUpgrade.ReturnUpgradeHandler;
import com.hawk.activity.type.impl.returnUpgrade.ReturnUpgradeTimeController;
import com.hawk.activity.type.impl.returnUpgrade.entity.ReturnUpgradeEntity;
import com.hawk.activity.type.impl.supplyCrate.SupplyCrateActivity;
import com.hawk.activity.type.impl.supplyCrate.SupplyCrateHandler;
import com.hawk.activity.type.impl.supplyCrate.SupplyCrateTimeController;
import com.hawk.activity.type.impl.supplyCrate.entity.SupplyCrateEntity;
import com.hawk.activity.type.impl.urlModel379.UrlModel379Activity;
import com.hawk.activity.type.impl.urlModel379.UrlModel379TimeController;
import com.hawk.activity.type.impl.urlModel380.UrlModel380Activity;
import com.hawk.activity.type.impl.urlModel380.UrlModel380TimeController;
import com.hawk.activity.type.impl.urlModel381.UrlModel381Activity;
import com.hawk.activity.type.impl.urlModel381.UrlModel381TimeController;
import org.hawk.db.HawkDBEntity;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.timeController.ITimeController;
import com.hawk.activity.timeController.impl.PerpetualOpenTimeController;
import com.hawk.activity.type.impl.AnniversaryGfit.AnniversaryGiftActivity;
import com.hawk.activity.type.impl.AnniversaryGfit.AnniversaryGiftTimeController;
import com.hawk.activity.type.impl.AnniversaryGfit.entity.AnniversaryGiftEntity;
import com.hawk.activity.type.impl.accumulateConsume.AccumulateConsumeActivity;
import com.hawk.activity.type.impl.accumulateConsume.AccumulateConsumeTimeController;
import com.hawk.activity.type.impl.accumulateConsume.entity.AccumulateConsumeEntity;
import com.hawk.activity.type.impl.accumulateRecharge.AccumulateRechargeActivity;
import com.hawk.activity.type.impl.accumulateRecharge.AccumulateRechargeTimeController;
import com.hawk.activity.type.impl.accumulateRecharge.entity.AccumulateRechargeEntity;
import com.hawk.activity.type.impl.accumulateRechargeTwo.AccumulateRechargeTwoActivity;
import com.hawk.activity.type.impl.accumulateRechargeTwo.AccumulateRechargeTwoTimeController;
import com.hawk.activity.type.impl.accumulateRechargeTwo.entity.AccumulateRechargeTwoEntity;
import com.hawk.activity.type.impl.aftercompetition.AfterCompetitionActivity;
import com.hawk.activity.type.impl.aftercompetition.AfterCompetitionHandler;
import com.hawk.activity.type.impl.aftercompetition.AfterCompetitionTimeController;
import com.hawk.activity.type.impl.aftercompetition.entity.AfterCompetitionEntity;
import com.hawk.activity.type.impl.airdrop.AirdropSupplyActivity;
import com.hawk.activity.type.impl.airdrop.AirdropSupplyTimeController;
import com.hawk.activity.type.impl.airdrop.entity.AirdropSupplyEntity;
import com.hawk.activity.type.impl.allianceCarnival.AllianceCarnivalActivity;
import com.hawk.activity.type.impl.allianceCarnival.AllianceCarnivalHandler;
import com.hawk.activity.type.impl.allianceCarnival.AllianceCarnivalTimeController;
import com.hawk.activity.type.impl.allianceCarnival.entity.AllianceCarnivalEntity;
import com.hawk.activity.type.impl.alliancecelebrate.AllianceCelebrateActivity;
import com.hawk.activity.type.impl.alliancecelebrate.AllianceCelebrateHandler;
import com.hawk.activity.type.impl.alliancecelebrate.AllianceCelebrateTimeController;
import com.hawk.activity.type.impl.alliancecelebrate.entity.AllianceCelebrateEntity;
import com.hawk.activity.type.impl.alliesWishing.AllianceWishActivity;
import com.hawk.activity.type.impl.alliesWishing.AllianceWishController;
import com.hawk.activity.type.impl.alliesWishing.AllianceWishHandler;
import com.hawk.activity.type.impl.alliesWishing.entity.AllianceWishEntity;
import com.hawk.activity.type.impl.allyBeatBack.AllyBeatBackActivity;
import com.hawk.activity.type.impl.allyBeatBack.AllyBeatBackHanlder;
import com.hawk.activity.type.impl.allyBeatBack.AllyBeatBackTimeController;
import com.hawk.activity.type.impl.allyBeatBack.entity.AllyBeatBackEntity;
import com.hawk.activity.type.impl.anniversaryCelebrate.AnniversaryCelebrateActivity;
import com.hawk.activity.type.impl.anniversaryCelebrate.AnniversaryCelebrateController;
import com.hawk.activity.type.impl.appointget.AppointGetActivity;
import com.hawk.activity.type.impl.appointget.AppointGetActivityHandler;
import com.hawk.activity.type.impl.appointget.AppointGetActivityTimeController;
import com.hawk.activity.type.impl.appointget.entity.AppointGetEntity;
import com.hawk.activity.type.impl.armamentexchange.ArmamentExchangeActivity;
import com.hawk.activity.type.impl.armamentexchange.ArmamentExchangeHandler;
import com.hawk.activity.type.impl.armamentexchange.ArmamentExchangeTimeController;
import com.hawk.activity.type.impl.armamentexchange.entity.ArmamentExchangeEntity;
import com.hawk.activity.type.impl.armiesMass.ArmiesMassActivity;
import com.hawk.activity.type.impl.armiesMass.ArmiesMassActivityHandler;
import com.hawk.activity.type.impl.armiesMass.ArmiesMassTimeController;
import com.hawk.activity.type.impl.armiesMass.entity.ArmiesMassEntity;
import com.hawk.activity.type.impl.backFlow.backGift.BackGiftActivity;
import com.hawk.activity.type.impl.backFlow.backGift.BackGiftActivityHandler;
import com.hawk.activity.type.impl.backFlow.backGift.BackGiftTimeController;
import com.hawk.activity.type.impl.backFlow.backGift.entity.BackGiftEntity;
import com.hawk.activity.type.impl.backFlow.chemistry.ChemistryActivity;
import com.hawk.activity.type.impl.backFlow.chemistry.ChemistryTimeController;
import com.hawk.activity.type.impl.backFlow.chemistry.entity.ChemistryEntity;
import com.hawk.activity.type.impl.backFlow.developSput.DevelopSpurtActivity;
import com.hawk.activity.type.impl.backFlow.developSput.DevelopSpurtHandler;
import com.hawk.activity.type.impl.backFlow.developSput.DevelopSpurtTimeController;
import com.hawk.activity.type.impl.backFlow.developSput.entity.DevelopSpurtEntity;
import com.hawk.activity.type.impl.backFlow.newlyExperience.NewlyExperienceActivity;
import com.hawk.activity.type.impl.backFlow.newlyExperience.NewlyExperienceTimeController;
import com.hawk.activity.type.impl.backFlow.newlyExperience.entity.NewlyExperienceEntity;
import com.hawk.activity.type.impl.backFlow.powerSend.PowerSendActivity;
import com.hawk.activity.type.impl.backFlow.powerSend.PowerSendActivityHandler;
import com.hawk.activity.type.impl.backFlow.powerSend.PowerSendTimeController;
import com.hawk.activity.type.impl.backFlow.powerSend.entity.PowerSendEntity;
import com.hawk.activity.type.impl.backFlow.privilege.PrivilegeActivity;
import com.hawk.activity.type.impl.backFlow.privilege.PrivilegeActivityHandler;
import com.hawk.activity.type.impl.backFlow.privilege.PrivilegeTimeController;
import com.hawk.activity.type.impl.backFlow.privilege.entity.PrivilegeEntity;
import com.hawk.activity.type.impl.backFlow.returnArmyExchange.RetrunArmyExchangeHandler;
import com.hawk.activity.type.impl.backFlow.returnArmyExchange.ReturnArmyExchangeActivity;
import com.hawk.activity.type.impl.backFlow.returnArmyExchange.ReturnArmyExchangeTimeController;
import com.hawk.activity.type.impl.backFlow.returnArmyExchange.entity.ReturnArmyExchangeEntity;
import com.hawk.activity.type.impl.backFlow.returnGift.ReturnGiftActivity;
import com.hawk.activity.type.impl.backFlow.returnGift.ReturnGiftHandler;
import com.hawk.activity.type.impl.backFlow.returnGift.ReturnGiftTimeController;
import com.hawk.activity.type.impl.backFlow.returnGift.entity.ReturnGiftEntity;
import com.hawk.activity.type.impl.backImmigration.BackImmgrationActivity;
import com.hawk.activity.type.impl.backImmigration.BackImmgrationActivityHandler;
import com.hawk.activity.type.impl.backImmigration.BackImmgrationActivityTimeController;
import com.hawk.activity.type.impl.backSoldierExchange.BackSoldierExchangeActivity;
import com.hawk.activity.type.impl.backSoldierExchange.BackSoldierExchangeActivityHandler;
import com.hawk.activity.type.impl.backSoldierExchange.BackSoldierExchangeTimeController;
import com.hawk.activity.type.impl.backSoldierExchange.entity.BackSoldierExchangeEntity;
import com.hawk.activity.type.impl.backToNewFly.BackToNewFlyActivity;
import com.hawk.activity.type.impl.backToNewFly.BackToNewFlyHandler;
import com.hawk.activity.type.impl.backToNewFly.BackToNewFlyOldActivity;
import com.hawk.activity.type.impl.backToNewFly.BackToNewFlyOldTimeController;
import com.hawk.activity.type.impl.backToNewFly.BackToNewFlyTimeController;
import com.hawk.activity.type.impl.backToNewFly.entity.BackToNewFlyEntity;
import com.hawk.activity.type.impl.backToNewFly.entity.BackToNewFlyOldEntity;
import com.hawk.activity.type.impl.bannerkill.BannerKillActivity;
import com.hawk.activity.type.impl.bannerkill.BannerKillActivityHandler;
import com.hawk.activity.type.impl.bannerkill.BannerKillTimeController;
import com.hawk.activity.type.impl.bannerkill.entity.ActivityBannerKillEntity;
import com.hawk.activity.type.impl.baseBuild.BaseBuildActivity;
import com.hawk.activity.type.impl.baseBuild.BaseBuildTimeController;
import com.hawk.activity.type.impl.battlefield.BattleFieldActivity;
import com.hawk.activity.type.impl.battlefield.BattleFieldActivityHandler;
import com.hawk.activity.type.impl.battlefield.BattleFieldTimeController;
import com.hawk.activity.type.impl.battlefield.entity.BattleFieldEntity;
import com.hawk.activity.type.impl.beauty.contest.BeautyContestActivity;
import com.hawk.activity.type.impl.beauty.contest.BeautyContestController;
import com.hawk.activity.type.impl.beauty.contest.BeautyContestHandler;
import com.hawk.activity.type.impl.beauty.contest.entity.BeautyContestEntity;
import com.hawk.activity.type.impl.beauty.finals.BeautyContestFinalActivity;
import com.hawk.activity.type.impl.beauty.finals.BeautyContestFinalController;
import com.hawk.activity.type.impl.beauty.finals.BeautyContestFinalHandler;
import com.hawk.activity.type.impl.beauty.finals.entity.BeautyContestFinalEntity;
import com.hawk.activity.type.impl.bestprize.BestPrizeActivity;
import com.hawk.activity.type.impl.bestprize.BestPrizeHandler;
import com.hawk.activity.type.impl.bestprize.BestPrizeTimeController;
import com.hawk.activity.type.impl.bestprize.entity.BestPrizeEntity;
import com.hawk.activity.type.impl.blackTech.BlackTechActivity;
import com.hawk.activity.type.impl.blackTech.BlackTechActivityTimeController;
import com.hawk.activity.type.impl.blackTech.BlackTechHandler;
import com.hawk.activity.type.impl.blackTech.entity.BlackTechEntity;
import com.hawk.activity.type.impl.blood_corps.BloodCorpsActivity;
import com.hawk.activity.type.impl.blood_corps.BloodCorpsActivityHandler;
import com.hawk.activity.type.impl.blood_corps.BloodCorpsTimeController;
import com.hawk.activity.type.impl.blood_corps.entity.BloodCorpsEntity;
import com.hawk.activity.type.impl.boss_invade.BossInvadeActivity;
import com.hawk.activity.type.impl.boss_invade.BossInvadeTimeController;
import com.hawk.activity.type.impl.bountyHunter.BountyHunterActivity;
import com.hawk.activity.type.impl.bountyHunter.BountyHunterHandler;
import com.hawk.activity.type.impl.bountyHunter.BountyHunterTimeController;
import com.hawk.activity.type.impl.bountyHunter.entity.BountyHunterEntity;
import com.hawk.activity.type.impl.breakShackles.BreakShacklesActivity;
import com.hawk.activity.type.impl.breakShackles.BreakShacklesTimeController;
import com.hawk.activity.type.impl.brokenexchange.BrokenExchangeActivity;
import com.hawk.activity.type.impl.brokenexchange.BrokenExchangeHandler;
import com.hawk.activity.type.impl.brokenexchange.BrokenExchangeTimeController;
import com.hawk.activity.type.impl.brokenexchange.entity.BrokenActivityEntity;
import com.hawk.activity.type.impl.brokenexchangeThree.BrokenExchangeThreeActivity;
import com.hawk.activity.type.impl.brokenexchangeThree.BrokenExchangeThreeHandler;
import com.hawk.activity.type.impl.brokenexchangeThree.BrokenExchangeThreeTimeController;
import com.hawk.activity.type.impl.brokenexchangeThree.entity.BrokenActivityThreeEntity;
import com.hawk.activity.type.impl.brokenexchangeTwo.BrokenExchangeTwoActivity;
import com.hawk.activity.type.impl.brokenexchangeTwo.BrokenExchangeTwoHandler;
import com.hawk.activity.type.impl.brokenexchangeTwo.BrokenExchangeTwoTimeController;
import com.hawk.activity.type.impl.brokenexchangeTwo.entity.BrokenTwoActivityEntity;
import com.hawk.activity.type.impl.buff.BuffActivity;
import com.hawk.activity.type.impl.buff.BuffTimeController;
import com.hawk.activity.type.impl.buildlevel.BuildLevelActivity;
import com.hawk.activity.type.impl.buildlevel.BuildLevelActivityHandler;
import com.hawk.activity.type.impl.buildlevel.BuildLevelTimeController;
import com.hawk.activity.type.impl.buildlevel.entity.ActivityBuildLevelEntity;
import com.hawk.activity.type.impl.cakeShare.CakeShareActivity;
import com.hawk.activity.type.impl.cakeShare.CakeShareHandler;
import com.hawk.activity.type.impl.cakeShare.CakeShareTimeController;
import com.hawk.activity.type.impl.cakeShare.entity.CakeShareEntity;
import com.hawk.activity.type.impl.celebrationCourse.CelebrationCourseActivity;
import com.hawk.activity.type.impl.celebrationCourse.CelebrationCourseActivityHandler;
import com.hawk.activity.type.impl.celebrationCourse.CelebrationCourseTimeController;
import com.hawk.activity.type.impl.celebrationCourse.entity.CelebrationCourseEntity;
import com.hawk.activity.type.impl.celebrationFood.CelebrationFoodActivity;
import com.hawk.activity.type.impl.celebrationFood.CelebrationFoodHandler;
import com.hawk.activity.type.impl.celebrationFood.CelebrationFoodTimeController;
import com.hawk.activity.type.impl.celebrationFood.entity.CelebrationFoodEntity;
import com.hawk.activity.type.impl.celebrationFund.CelebrationFundActivity;
import com.hawk.activity.type.impl.celebrationFund.CelebrationFundHandler;
import com.hawk.activity.type.impl.celebrationFund.CelebrationFundTimeController;
import com.hawk.activity.type.impl.celebrationFund.entity.CelebrationFundEntity;
import com.hawk.activity.type.impl.celebrationShop.CelebrationShopActivity;
import com.hawk.activity.type.impl.celebrationShop.CelebrationShopActivityHandler;
import com.hawk.activity.type.impl.celebrationShop.CelebrationShopTimeController;
import com.hawk.activity.type.impl.celebrationShop.entity.CelebrationShopEntity;
import com.hawk.activity.type.impl.changeServer.ChangeServerActivity;
import com.hawk.activity.type.impl.changeServer.ChangeServerHandler;
import com.hawk.activity.type.impl.changeServer.ChangeServerTimeController;
import com.hawk.activity.type.impl.changeServer.entity.ChangeServerEntity;
import com.hawk.activity.type.impl.christmaswar.ChristmasWarActivity;
import com.hawk.activity.type.impl.christmaswar.ChristmasWarHandler;
import com.hawk.activity.type.impl.christmaswar.ChristmasWarTimeController;
import com.hawk.activity.type.impl.christmaswar.entity.ActivityChristmasWarEntity;
import com.hawk.activity.type.impl.chronoGift.ChronoGiftActivity;
import com.hawk.activity.type.impl.chronoGift.ChronoGiftActivityHandler;
import com.hawk.activity.type.impl.chronoGift.ChronoGiftTimeController;
import com.hawk.activity.type.impl.chronoGift.entity.ChronoGiftEntity;
import com.hawk.activity.type.impl.cnyExam.CnyExamActivity;
import com.hawk.activity.type.impl.cnyExam.CnyExamHandler;
import com.hawk.activity.type.impl.cnyExam.CnyExamTimeController;
import com.hawk.activity.type.impl.cnyExam.entity.CnyExamEntity;
import com.hawk.activity.type.impl.commandAcademy.CommandAcademyActivity;
import com.hawk.activity.type.impl.commandAcademy.CommandAcademyHandler;
import com.hawk.activity.type.impl.commandAcademy.CommandAcademyTimeController;
import com.hawk.activity.type.impl.commandAcademy.entity.CommandAcademyEntity;
import com.hawk.activity.type.impl.commandAcademySimplify.CommandAcademySimplifyActivity;
import com.hawk.activity.type.impl.commandAcademySimplify.CommandAcademySimplifyHandler;
import com.hawk.activity.type.impl.commandAcademySimplify.CommandAcademySimplifyTimeController;
import com.hawk.activity.type.impl.commandAcademySimplify.entity.CommandAcademySimplifyEntity;
import com.hawk.activity.type.impl.commonExchange.CommonExchangeActivity;
import com.hawk.activity.type.impl.commonExchange.CommonExchangeHandler;
import com.hawk.activity.type.impl.commonExchange.CommonExchangeTimeController;
import com.hawk.activity.type.impl.commonExchange.entity.CommonExchangeEntity;
import com.hawk.activity.type.impl.commonExchangeTwo.CommonExchangeTwoActivity;
import com.hawk.activity.type.impl.commonExchangeTwo.CommonExchangeTwoHandler;
import com.hawk.activity.type.impl.commonExchangeTwo.CommonExchangeTwoTimeController;
import com.hawk.activity.type.impl.commonExchangeTwo.entity.CommonExchangeTwoEntity;
import com.hawk.activity.type.impl.continuousRecharge.ContinuousRechargeActivity;
import com.hawk.activity.type.impl.continuousRecharge.ContinuousRechargeHandler;
import com.hawk.activity.type.impl.continuousRecharge.ContinuousRechargeTimeController;
import com.hawk.activity.type.impl.continuousRecharge.entity.ContinuousRechargeEntity;
import com.hawk.activity.type.impl.copyCenter.CopyCenterActivity;
import com.hawk.activity.type.impl.copyCenter.CopyCenterActivityHandler;
import com.hawk.activity.type.impl.copyCenter.CopyCenterTimeController;
import com.hawk.activity.type.impl.coreplate.CoreplateActivity;
import com.hawk.activity.type.impl.coreplate.CoreplateActivityHandler;
import com.hawk.activity.type.impl.coreplate.CoreplateActivityTimeController;
import com.hawk.activity.type.impl.coreplate.entity.CoreplateActivityEntity;
import com.hawk.activity.type.impl.customgift.CustomGiftActivity;
import com.hawk.activity.type.impl.customgift.CustomGiftActivityHandler;
import com.hawk.activity.type.impl.customgift.CustomGiftController;
import com.hawk.activity.type.impl.customgift.entity.CustomGiftEntity;
import com.hawk.activity.type.impl.dailyBuyGift.DailyBuyGiftActivity;
import com.hawk.activity.type.impl.dailyBuyGift.DailyBuyGiftTimeController;
import com.hawk.activity.type.impl.dailyBuyGift.entity.DailyBuyGiftEntity;
import com.hawk.activity.type.impl.dailyPreference.DailyPreferenceActivity;
import com.hawk.activity.type.impl.dailyPreference.DailyPreferenceTimeController;
import com.hawk.activity.type.impl.dailyrecharge.DailyRechargeActivity;
import com.hawk.activity.type.impl.dailyrecharge.DailyRechargeActivityHandler;
import com.hawk.activity.type.impl.dailyrecharge.DailyRechargeTimeController;
import com.hawk.activity.type.impl.dailyrecharge.entity.DailyRechargeEntity;
import com.hawk.activity.type.impl.dailyrechargenew.DailyRechargeNewActivity;
import com.hawk.activity.type.impl.dailyrechargenew.DailyRechargeNewActivityHandler;
import com.hawk.activity.type.impl.dailyrechargenew.DailyRechargeNewTimeController;
import com.hawk.activity.type.impl.dailyrechargenew.entity.DailyRechargeNewEntity;
import com.hawk.activity.type.impl.dailysign.DailySignActivity;
import com.hawk.activity.type.impl.dailysign.DailySignHandler;
import com.hawk.activity.type.impl.dailysign.DailySignTimeController;
import com.hawk.activity.type.impl.dailysign.entity.DailySignEntity;
import com.hawk.activity.type.impl.destinyRevolver.DestinyRevolverActivity;
import com.hawk.activity.type.impl.destinyRevolver.DestinyRevolverHandler;
import com.hawk.activity.type.impl.destinyRevolver.DestinyRevolverTimeController;
import com.hawk.activity.type.impl.destinyRevolver.entity.DestinyRevolverEntity;
import com.hawk.activity.type.impl.developFast.DevelopFastActivity;
import com.hawk.activity.type.impl.developFast.DevelopFastHandler;
import com.hawk.activity.type.impl.developFast.DevelopFastTimeController;
import com.hawk.activity.type.impl.developFast.entity.DevelopFastEntity;
import com.hawk.activity.type.impl.developFastOld.DevelopFastOldActivity;
import com.hawk.activity.type.impl.developFastOld.DevelopFastOldHandler;
import com.hawk.activity.type.impl.developFastOld.DevelopFastOldTimeController;
import com.hawk.activity.type.impl.developFastOld.entity.DevelopFastOldEntity;
import com.hawk.activity.type.impl.diffInfoSave.DiffInfoSaveActivity;
import com.hawk.activity.type.impl.diffInfoSave.DiffInfoSaveHandler;
import com.hawk.activity.type.impl.diffInfoSave.DiffInfoSaveTimeController;
import com.hawk.activity.type.impl.diffInfoSave.entity.DiffInfoSaveEntity;
import com.hawk.activity.type.impl.diffNewServerTech.DiffNewServerTechActivity;
import com.hawk.activity.type.impl.diffNewServerTech.DiffNewServerTechHandler;
import com.hawk.activity.type.impl.diffNewServerTech.DiffNewServerTechTimeController;
import com.hawk.activity.type.impl.diffNewServerTech.entity.DiffNewServerTechEntity;
import com.hawk.activity.type.impl.directGift.DirectGiftActivity;
import com.hawk.activity.type.impl.directGift.DirectGiftHandler;
import com.hawk.activity.type.impl.directGift.DirectGiftTimeController;
import com.hawk.activity.type.impl.directGift.entity.DirectGiftEntity;
import com.hawk.activity.type.impl.dividegold.DivideGoldActivity;
import com.hawk.activity.type.impl.dividegold.DivideGoldHandler;
import com.hawk.activity.type.impl.dividegold.DivideGoldTimeController;
import com.hawk.activity.type.impl.dividegold.entity.DivideGoldEntity;
import com.hawk.activity.type.impl.domeExchange.DomeExchangeActivity;
import com.hawk.activity.type.impl.domeExchange.DomeExchangeHandler;
import com.hawk.activity.type.impl.domeExchange.DomeExchangeTimeController;
import com.hawk.activity.type.impl.domeExchange.entity.DomeExchangeEntity;
import com.hawk.activity.type.impl.domeExchangeTwo.DomeExchangeTwoActivity;
import com.hawk.activity.type.impl.domeExchangeTwo.DomeExchangeTwoHandler;
import com.hawk.activity.type.impl.domeExchangeTwo.DomeExchangeTwoTimeController;
import com.hawk.activity.type.impl.domeExchangeTwo.entity.DomeExchangeTwoEntity;
import com.hawk.activity.type.impl.doubleGift.DoubleGiftActivity;
import com.hawk.activity.type.impl.doubleGift.DoubleGiftActivityHandler;
import com.hawk.activity.type.impl.doubleGift.DoubleGiftTimeController;
import com.hawk.activity.type.impl.doubleGift.entity.DoubleGiftEntity;
import com.hawk.activity.type.impl.doubleRecharge.DoubleRechargeActivity;
import com.hawk.activity.type.impl.doubleRecharge.DoubleRechargeTimeController;
import com.hawk.activity.type.impl.doubleRecharge.entity.DoubleRechargeEntity;
import com.hawk.activity.type.impl.dressCollectionTwo.DressCollectionTwoActivity;
import com.hawk.activity.type.impl.dressCollectionTwo.DressCollectionTwoHandler;
import com.hawk.activity.type.impl.dressCollectionTwo.DressCollectionTwoTimeController;
import com.hawk.activity.type.impl.dressCollectionTwo.entity.DressCollectionTwoEntity;
import com.hawk.activity.type.impl.dressTreasure.DressTreasureActivity;
import com.hawk.activity.type.impl.dressTreasure.DressTreasureController;
import com.hawk.activity.type.impl.dressTreasure.DressTreasureHandler;
import com.hawk.activity.type.impl.dressTreasure.entity.DressTreasureEntity;
import com.hawk.activity.type.impl.dresscollection.DressCollectionActivity;
import com.hawk.activity.type.impl.dresscollection.DressCollectionHandler;
import com.hawk.activity.type.impl.dresscollection.DressCollectionTimeController;
import com.hawk.activity.type.impl.dresscollection.entity.DressCollectionEntity;
import com.hawk.activity.type.impl.dressup.drawingsearch.DrawingSearchActivity;
import com.hawk.activity.type.impl.dressup.drawingsearch.DrawingSearchTimeController;
import com.hawk.activity.type.impl.dressup.drawingsearch.entity.DrawingSearchActivityEntity;
import com.hawk.activity.type.impl.dressup.energygather.EnergyGatherActivity;
import com.hawk.activity.type.impl.dressup.energygather.EnergyGatherTimeController;
import com.hawk.activity.type.impl.dressup.energygather.entity.EnergyGatherEntity;
import com.hawk.activity.type.impl.dressup.firereignite.FireReigniteActivity;
import com.hawk.activity.type.impl.dressup.firereignite.FireReigniteHandler;
import com.hawk.activity.type.impl.dressup.firereignite.FireReigniteTimeController;
import com.hawk.activity.type.impl.dressup.firereignite.entity.FireReigniteEntity;
import com.hawk.activity.type.impl.dressup.gunpowderrise.GunpowderRiseActivity;
import com.hawk.activity.type.impl.dressup.gunpowderrise.GunpowderRiseHandler;
import com.hawk.activity.type.impl.dressup.gunpowderrise.GunpowderRiseTimeController;
import com.hawk.activity.type.impl.dressup.gunpowderrise.entity.GunpowderRiseEntity;
import com.hawk.activity.type.impl.dressuptwo.christmasrecharge.ChristmasRechargeActivity;
import com.hawk.activity.type.impl.dressuptwo.christmasrecharge.ChristmasRechargeTimeController;
import com.hawk.activity.type.impl.dressuptwo.christmasrecharge.entity.ChristmasRechargeEntity;
import com.hawk.activity.type.impl.dressuptwo.energygathertwo.EnergyGatherTwoActivity;
import com.hawk.activity.type.impl.dressuptwo.energygathertwo.EnergyGatherTwoTimeController;
import com.hawk.activity.type.impl.dressuptwo.energygathertwo.entity.EnergyGatherTwoEntity;
import com.hawk.activity.type.impl.dressuptwo.firereignitetwo.FireReigniteTwoActivity;
import com.hawk.activity.type.impl.dressuptwo.firereignitetwo.FireReigniteTwoHandler;
import com.hawk.activity.type.impl.dressuptwo.firereignitetwo.FireReigniteTwoTimeController;
import com.hawk.activity.type.impl.dressuptwo.firereignitetwo.entity.FireReigniteTwoEntity;
import com.hawk.activity.type.impl.dressuptwo.gunpowderrisetwo.GunpowderRiseTwoActivity;
import com.hawk.activity.type.impl.dressuptwo.gunpowderrisetwo.GunpowderRiseTwoHandler;
import com.hawk.activity.type.impl.dressuptwo.gunpowderrisetwo.GunpowderRiseTwoTimeController;
import com.hawk.activity.type.impl.dressuptwo.gunpowderrisetwo.entity.GunpowderRiseTwoEntity;
import com.hawk.activity.type.impl.drogenBoatFestival.benefit.DragonBoatBenefitActivity;
import com.hawk.activity.type.impl.drogenBoatFestival.benefit.DragonBoatBenefitTimeController;
import com.hawk.activity.type.impl.drogenBoatFestival.benefit.entity.DragonBoatBenefitEntity;
import com.hawk.activity.type.impl.drogenBoatFestival.exchange.DragonBoatExchangeActivity;
import com.hawk.activity.type.impl.drogenBoatFestival.exchange.DragonBoatExchangeController;
import com.hawk.activity.type.impl.drogenBoatFestival.exchange.DragonBoatExchangeHandler;
import com.hawk.activity.type.impl.drogenBoatFestival.exchange.entity.DragonBoatExchangeEntity;
import com.hawk.activity.type.impl.drogenBoatFestival.gift.DragonBoatGiftActivity;
import com.hawk.activity.type.impl.drogenBoatFestival.gift.DragonBoatGiftHandler;
import com.hawk.activity.type.impl.drogenBoatFestival.gift.DragonBoatGiftTimeController;
import com.hawk.activity.type.impl.drogenBoatFestival.gift.entity.DragonBoatGiftEntity;
import com.hawk.activity.type.impl.drogenBoatFestival.guildCelebration.DragonBoatCelebrationActivity;
import com.hawk.activity.type.impl.drogenBoatFestival.guildCelebration.DragonBoatCelebrationHandler;
import com.hawk.activity.type.impl.drogenBoatFestival.guildCelebration.DragonBoatCelebrationTimeController;
import com.hawk.activity.type.impl.drogenBoatFestival.guildCelebration.entity.DragonBoatCelebrationEntity;
import com.hawk.activity.type.impl.drogenBoatFestival.luckyBag.DragonBoatLuckyBagActivity;
import com.hawk.activity.type.impl.drogenBoatFestival.luckyBag.DragonBoatLuckyBagController;
import com.hawk.activity.type.impl.drogenBoatFestival.luckyBag.DragonBoatLuckyBagHandler;
import com.hawk.activity.type.impl.drogenBoatFestival.luckyBag.entity.DragonBoatLuckyBagEntity;
import com.hawk.activity.type.impl.drogenBoatFestival.recharge.DragonBoatRechargeActivity;
import com.hawk.activity.type.impl.drogenBoatFestival.recharge.DragonBoatRechargeTimeController;
import com.hawk.activity.type.impl.drogenBoatFestival.recharge.entity.DragonBoatRechargeEntity;
import com.hawk.activity.type.impl.dyzzAchieve.DYZZAchieveActivity;
import com.hawk.activity.type.impl.dyzzAchieve.DYZZAchieveHandler;
import com.hawk.activity.type.impl.dyzzAchieve.DYZZAchieveTimeController;
import com.hawk.activity.type.impl.dyzzAchieve.entity.DYZZAchieveEntity;
import com.hawk.activity.type.impl.emptyModel.EmptyModelActivity;
import com.hawk.activity.type.impl.emptyModel.EmptyModelTimeController;
import com.hawk.activity.type.impl.emptyModelEight.EmptyModelEightActivity;
import com.hawk.activity.type.impl.emptyModelEight.EmptyModelEightTimeController;
import com.hawk.activity.type.impl.emptyModelFive.EmptyModelFiveActivity;
import com.hawk.activity.type.impl.emptyModelFive.EmptyModelFiveTimeController;
import com.hawk.activity.type.impl.emptyModelFour.EmptyModelFourActivity;
import com.hawk.activity.type.impl.emptyModelFour.EmptyModelFourTimeController;
import com.hawk.activity.type.impl.emptyModelSeven.EmptyModelSevenActivity;
import com.hawk.activity.type.impl.emptyModelSeven.EmptyModelSevenTimeController;
import com.hawk.activity.type.impl.emptyModelSix.EmptyModelSixActivity;
import com.hawk.activity.type.impl.emptyModelSix.EmptyModelSixTimeController;
import com.hawk.activity.type.impl.emptyModelThree.EmptyModelThreeActivity;
import com.hawk.activity.type.impl.emptyModelThree.EmptyModelThreeTimeController;
import com.hawk.activity.type.impl.emptyModelTwo.EmptyModelTwoActivity;
import com.hawk.activity.type.impl.emptyModelTwo.EmptyModelTwoTimeController;
import com.hawk.activity.type.impl.energies.EnergiesActivity;
import com.hawk.activity.type.impl.energies.EnergiesHandler;
import com.hawk.activity.type.impl.energies.EnergiesTimeController;
import com.hawk.activity.type.impl.energies.entity.EnergiesEntity;
import com.hawk.activity.type.impl.energyInvest.EnergyInvestActivity;
import com.hawk.activity.type.impl.energyInvest.EnergyInvestHandler;
import com.hawk.activity.type.impl.energyInvest.EnergyInvestTimeController;
import com.hawk.activity.type.impl.energyInvest.entity.EnergyInvestEntity;
import com.hawk.activity.type.impl.equipAchieve.EquipAchieveActivity;
import com.hawk.activity.type.impl.equipAchieve.EquipAchieveTimeController;
import com.hawk.activity.type.impl.equipAchieve.entity.EquipAchieveEntity;
import com.hawk.activity.type.impl.equipBlackMarket.EquipBlackMarketActivity;
import com.hawk.activity.type.impl.equipBlackMarket.EquipBlackMarketActivityHandler;
import com.hawk.activity.type.impl.equipBlackMarket.EquipBlackMarketTimeController;
import com.hawk.activity.type.impl.equipBlackMarket.entity.EquipBlackMarketEntity;
import com.hawk.activity.type.impl.equipCraftsman.EquipCarftsmanActivity;
import com.hawk.activity.type.impl.equipCraftsman.EquipCarftsmanHandler;
import com.hawk.activity.type.impl.equipCraftsman.EquipCarftsmanTimeController;
import com.hawk.activity.type.impl.equipCraftsman.entity.EquipCarftsmanEntity;
import com.hawk.activity.type.impl.equipTech.EquipTechActivity;
import com.hawk.activity.type.impl.equipTech.EquipTechActivityTimeController;
import com.hawk.activity.type.impl.equipTech.entity.EquipTechEntity;
import com.hawk.activity.type.impl.evolution.EvolutionActivity;
import com.hawk.activity.type.impl.evolution.EvolutionActivityHandler;
import com.hawk.activity.type.impl.evolution.EvolutionTimeController;
import com.hawk.activity.type.impl.evolution.entity.ActivityEvolutionEntity;
import com.hawk.activity.type.impl.exchangeDecorate.ExchangeDecorateActivity;
import com.hawk.activity.type.impl.exchangeDecorate.ExchangeDecorateActivityHandler;
import com.hawk.activity.type.impl.exchangeDecorate.ExchangeDecorateTimeController;
import com.hawk.activity.type.impl.exchangeDecorate.entity.ActivityExchangeDecorateEntity;
import com.hawk.activity.type.impl.exclusiveMomory.ExclusiveMemoryActivity;
import com.hawk.activity.type.impl.exclusiveMomory.ExclusiveMemoryController;
import com.hawk.activity.type.impl.exclusiveMomory.ExclusiveMemoryHandler;
import com.hawk.activity.type.impl.exclusiveMomory.entity.ExclusiveMemoryEntity;
import com.hawk.activity.type.impl.festival.FestivalActivity;
import com.hawk.activity.type.impl.festival.FestivalTimeController;
import com.hawk.activity.type.impl.festival.entity.FestivalEntity;
import com.hawk.activity.type.impl.festivalTwo.FestivalTwoActivity;
import com.hawk.activity.type.impl.festivalTwo.FestivalTwoTimeController;
import com.hawk.activity.type.impl.festivalTwo.entity.FestivalTwoEntity;
import com.hawk.activity.type.impl.fighter_puzzle.FighterPuzzleActivity;
import com.hawk.activity.type.impl.fighter_puzzle.FighterPuzzleActivityHandler;
import com.hawk.activity.type.impl.fighter_puzzle.FighterPuzzleTimeController;
import com.hawk.activity.type.impl.fighter_puzzle.entity.FighterPuzzleEntity;
import com.hawk.activity.type.impl.fighter_puzzle_serveropen.FighterPuzzleServeropenActivity;
import com.hawk.activity.type.impl.fighter_puzzle_serveropen.FighterPuzzleServeropenActivityHandler;
import com.hawk.activity.type.impl.fighter_puzzle_serveropen.FighterPuzzleServeropenTimeController;
import com.hawk.activity.type.impl.fighter_puzzle_serveropen.entity.FighterPuzzleServeropenEntity;
import com.hawk.activity.type.impl.fireworks.FireWorksActivity;
import com.hawk.activity.type.impl.fireworks.FireWorksActivityTimeController;
import com.hawk.activity.type.impl.fireworks.FireWorksHandler;
import com.hawk.activity.type.impl.fireworks.entity.FireWorksEntity;
import com.hawk.activity.type.impl.firstRecharge.FirstRechargeActivity;
import com.hawk.activity.type.impl.firstRecharge.FirstRechargeActivityHandler;
import com.hawk.activity.type.impl.firstRecharge.FirstRechargeTimeController;
import com.hawk.activity.type.impl.firstRecharge.enitiy.FirstRechargeEntity;
import com.hawk.activity.type.impl.flightplan.FlightPlanActivity;
import com.hawk.activity.type.impl.flightplan.FlightPlanActivityHandler;
import com.hawk.activity.type.impl.flightplan.FlightPlanTimeController;
import com.hawk.activity.type.impl.flightplan.entity.FlightPlanEntity;
import com.hawk.activity.type.impl.fristRechagerThree.FirstRechargeThreeActivity;
import com.hawk.activity.type.impl.fristRechagerThree.FirstRechargeThreeHandler;
import com.hawk.activity.type.impl.fristRechagerThree.FirstRechargeThreeTimeController;
import com.hawk.activity.type.impl.fristRechagerThree.entity.FirstRechargeThreeEntity;
import com.hawk.activity.type.impl.fullyArmed.FullyArmedActivity;
import com.hawk.activity.type.impl.fullyArmed.FullyArmedActivityTimeController;
import com.hawk.activity.type.impl.fullyArmed.FullyArmedHandler;
import com.hawk.activity.type.impl.fullyArmed.entity.FullyArmedEntity;
import com.hawk.activity.type.impl.ghostSecret.GhostSecretActivity;
import com.hawk.activity.type.impl.ghostSecret.GhostSecretHandler;
import com.hawk.activity.type.impl.ghostSecret.GhostSecretTimeController;
import com.hawk.activity.type.impl.ghostSecret.entity.GhostSecretEntity;
import com.hawk.activity.type.impl.giftzero.GiftZeroActivity;
import com.hawk.activity.type.impl.giftzero.GiftZeroActivityHandler;
import com.hawk.activity.type.impl.giftzero.GiftZeroController;
import com.hawk.activity.type.impl.giftzero.entity.GiftZeroEntity;
import com.hawk.activity.type.impl.giftzeronew.GiftZeroNewActivity;
import com.hawk.activity.type.impl.giftzeronew.GiftZeroNewActivityHandler;
import com.hawk.activity.type.impl.giftzeronew.GiftZeroNewController;
import com.hawk.activity.type.impl.giftzeronew.entity.GiftZeroNewEntity;
import com.hawk.activity.type.impl.globalSign.GlobalSignActivity;
import com.hawk.activity.type.impl.globalSign.GlobalSignHandler;
import com.hawk.activity.type.impl.globalSign.GlobalSignTimeController;
import com.hawk.activity.type.impl.globalSign.entity.GlobalSignEntity;
import com.hawk.activity.type.impl.goldBaby.GoldBabyActivity;
import com.hawk.activity.type.impl.goldBaby.GoldBabyHandler;
import com.hawk.activity.type.impl.goldBaby.GoldBabyTimeController;
import com.hawk.activity.type.impl.goldBaby.entity.GoldBabyEntity;
import com.hawk.activity.type.impl.goldBabyNew.GoldBabyNewActivity;
import com.hawk.activity.type.impl.goldBabyNew.GoldBabyNewHandler;
import com.hawk.activity.type.impl.goldBabyNew.GoldBabyNewTimeController;
import com.hawk.activity.type.impl.goldBabyNew.entity.GoldBabyNewEntity;
import com.hawk.activity.type.impl.gratefulBenefits.GratefulBenefitsActivity;
import com.hawk.activity.type.impl.gratefulBenefits.GratefulBenefitsHandler;
import com.hawk.activity.type.impl.gratefulBenefits.GratefulBenefitsTimeController;
import com.hawk.activity.type.impl.gratefulBenefits.entity.GratefulBenefitsEntity;
import com.hawk.activity.type.impl.gratitudeGift.GratitudeGiftActivity;
import com.hawk.activity.type.impl.gratitudeGift.GratitudeGiftActivityHandler;
import com.hawk.activity.type.impl.gratitudeGift.GratitudeGiftTimeController;
import com.hawk.activity.type.impl.gratitudeGift.entity.GratitudeGiftEntity;
import com.hawk.activity.type.impl.greatGift.GreatGiftActivity;
import com.hawk.activity.type.impl.greatGift.GreatGiftHandler;
import com.hawk.activity.type.impl.greatGift.GreatGiftTimeController;
import com.hawk.activity.type.impl.greatGift.entity.GreatGiftEntity;
import com.hawk.activity.type.impl.greetings.GreetingsActivity;
import com.hawk.activity.type.impl.greetings.GreetingsTimeController;
import com.hawk.activity.type.impl.greetings.entity.GreetingsEntity;
import com.hawk.activity.type.impl.groupBuy.GroupBuyActivity;
import com.hawk.activity.type.impl.groupBuy.GroupBuyActivityHandler;
import com.hawk.activity.type.impl.groupBuy.GroupBuyTimeController;
import com.hawk.activity.type.impl.groupBuy.entity.GroupBuyEntity;
import com.hawk.activity.type.impl.groupPurchase.GroupPurchaseActivity;
import com.hawk.activity.type.impl.groupPurchase.GroupPurchaseActivityHandler;
import com.hawk.activity.type.impl.groupPurchase.GroupPurchaseTimeController;
import com.hawk.activity.type.impl.groupPurchase.entity.GroupPurchaseEntity;
import com.hawk.activity.type.impl.growUpBoost.GrowUpBoostActivity;
import com.hawk.activity.type.impl.growUpBoost.GrowUpBoostHandler;
import com.hawk.activity.type.impl.growUpBoost.GrowUpBoostTimeController;
import com.hawk.activity.type.impl.growUpBoost.entity.GrowUpBoostEntity;
import com.hawk.activity.type.impl.growfund.GrowFundActivity;
import com.hawk.activity.type.impl.growfund.GrowFundActivityHandler;
import com.hawk.activity.type.impl.growfund.GrowFundTimeController;
import com.hawk.activity.type.impl.growfund.entity.GrowFundEntity;
import com.hawk.activity.type.impl.growfundnew.GrowFundNewActivity;
import com.hawk.activity.type.impl.growfundnew.GrowFundNewActivityHandler;
import com.hawk.activity.type.impl.growfundnew.GrowFundNewTimeController;
import com.hawk.activity.type.impl.growfundnew.entity.GrowFundNewEntity;
import com.hawk.activity.type.impl.guildBack.GuildBackActivity;
import com.hawk.activity.type.impl.guildBack.GuildBackHandler;
import com.hawk.activity.type.impl.guildBack.GuildBackTimeController;
import com.hawk.activity.type.impl.guildBack.entity.GuildBackEntity;
import com.hawk.activity.type.impl.guildDragonAttack.GuildDragonAttackActivity;
import com.hawk.activity.type.impl.guildDragonAttack.GuildDragonAttactHandler;
import com.hawk.activity.type.impl.guildDragonAttack.GuildDragonAttactTimeController;
import com.hawk.activity.type.impl.guildDragonAttack.entity.GuildDragonAttackEntry;
import com.hawk.activity.type.impl.guildbanner.GuildBannerActivity;
import com.hawk.activity.type.impl.guildbanner.GuildBannerActivityHandler;
import com.hawk.activity.type.impl.guildbanner.GuildBannerTimeController;
import com.hawk.activity.type.impl.healexchange.HealExchangeActivity;
import com.hawk.activity.type.impl.healexchange.HealExchangeHandler;
import com.hawk.activity.type.impl.healexchange.HealExchangeTimeController;
import com.hawk.activity.type.impl.healexchange.entity.HealExchangeEntity;
import com.hawk.activity.type.impl.heavenBlessing.HeavenBlessingActivity;
import com.hawk.activity.type.impl.heavenBlessing.HeavenBlessingHandler;
import com.hawk.activity.type.impl.heavenBlessing.HeavenBlessingTimeController;
import com.hawk.activity.type.impl.heavenBlessing.entity.HeavenBlessingEntity;
import com.hawk.activity.type.impl.hellfire.HellFireActivity;
import com.hawk.activity.type.impl.hellfire.HellFireHandler;
import com.hawk.activity.type.impl.hellfire.HellFireTimeController;
import com.hawk.activity.type.impl.hellfire.entity.ActivityHellFireEntity;
import com.hawk.activity.type.impl.hellfirethree.HellFireThreeActivity;
import com.hawk.activity.type.impl.hellfirethree.HellFireThreeHandler;
import com.hawk.activity.type.impl.hellfirethree.HellFireThreeTimeController;
import com.hawk.activity.type.impl.hellfirethree.entity.ActivityHellFireThreeEntity;
import com.hawk.activity.type.impl.hellfiretwo.HellFireTwoActivity;
import com.hawk.activity.type.impl.hellfiretwo.HellFireTwoHandler;
import com.hawk.activity.type.impl.hellfiretwo.HellFireTwoTimeController;
import com.hawk.activity.type.impl.hellfiretwo.entity.ActivityHellFireTwoEntity;
import com.hawk.activity.type.impl.heroAchieve.HeroAchieveActivity;
import com.hawk.activity.type.impl.heroAchieve.HeroAchieveTimeController;
import com.hawk.activity.type.impl.heroAchieve.entity.HeroAchieveEntity;
import com.hawk.activity.type.impl.heroBack.HeroBackActivity;
import com.hawk.activity.type.impl.heroBack.HeroBackHandler;
import com.hawk.activity.type.impl.heroBack.HeroBackTimeController;
import com.hawk.activity.type.impl.heroBack.entity.HeroBackEntity;
import com.hawk.activity.type.impl.heroBackExchange.HeroBackExchangeActivity;
import com.hawk.activity.type.impl.heroBackExchange.HeroBackExchangeHandler;
import com.hawk.activity.type.impl.heroBackExchange.HeroBackExchangeTimeController;
import com.hawk.activity.type.impl.heroBackExchange.entity.HeroBackExchangeEntity;
import com.hawk.activity.type.impl.heroLove.HeroLoveActivity;
import com.hawk.activity.type.impl.heroLove.HeroLoveActivityHandler;
import com.hawk.activity.type.impl.heroLove.HeroLoveTimeController;
import com.hawk.activity.type.impl.heroLove.entity.HeroLoveEntity;
import com.hawk.activity.type.impl.heroSkin.HeroSkinActivity;
import com.hawk.activity.type.impl.heroSkin.HeroSkinHandler;
import com.hawk.activity.type.impl.heroSkin.HeroSkinTimeController;
import com.hawk.activity.type.impl.heroSkin.entity.HeroSkinEntity;
import com.hawk.activity.type.impl.heroTheme.HeroThemeActivity;
import com.hawk.activity.type.impl.heroTheme.HeroThemeTimeController;
import com.hawk.activity.type.impl.heroTheme.entity.HeroThemeEntity;
import com.hawk.activity.type.impl.heroTrial.HeroTrialActivity;
import com.hawk.activity.type.impl.heroTrial.HeroTrialHandler;
import com.hawk.activity.type.impl.heroTrial.HeroTrialTimeController;
import com.hawk.activity.type.impl.heroTrial.entity.HeroTrialActivityEntity;
import com.hawk.activity.type.impl.heroWish.HeroWishActivity;
import com.hawk.activity.type.impl.heroWish.HeroWishController;
import com.hawk.activity.type.impl.heroWish.HeroWishHandler;
import com.hawk.activity.type.impl.heroWish.entity.HeroWishEntity;
import com.hawk.activity.type.impl.hiddenTreasure.HiddenTreasureActivity;
import com.hawk.activity.type.impl.hiddenTreasure.HiddenTreasureHandler;
import com.hawk.activity.type.impl.hiddenTreasure.HiddenTreasureTimeController;
import com.hawk.activity.type.impl.hiddenTreasure.entity.HiddenTreasureEntity;
import com.hawk.activity.type.impl.hongfugift.HongFuGiftActivity;
import com.hawk.activity.type.impl.hongfugift.HongFuGiftActivityHandler;
import com.hawk.activity.type.impl.hongfugift.HongFuGiftTimeController;
import com.hawk.activity.type.impl.hongfugift.entity.HongFuGiftEntity;
import com.hawk.activity.type.impl.honorRepay.HonorRepayActivity;
import com.hawk.activity.type.impl.honorRepay.HonorRepayHandler;
import com.hawk.activity.type.impl.honorRepay.HonorRepayTimeController;
import com.hawk.activity.type.impl.honorRepay.entity.HonorRepayEntity;
import com.hawk.activity.type.impl.honourHeroBefell.HonourHeroBefellActivity;
import com.hawk.activity.type.impl.honourHeroBefell.HonourHeroBefellController;
import com.hawk.activity.type.impl.honourHeroBefell.HonourHeroBefellHandler;
import com.hawk.activity.type.impl.honourHeroBefell.entity.HonourHeroBefellEntity;
import com.hawk.activity.type.impl.honourHeroReturn.HonourHeroReturnActivity;
import com.hawk.activity.type.impl.honourHeroReturn.HonourHeroReturnController;
import com.hawk.activity.type.impl.honourHeroReturn.HonourHeroReturnHandler;
import com.hawk.activity.type.impl.honourHeroReturn.entity.HonourHeroReturnEntity;
import com.hawk.activity.type.impl.honourMobilize.HonourMobilizeActivity;
import com.hawk.activity.type.impl.honourMobilize.HonourMobilizeHandler;
import com.hawk.activity.type.impl.honourMobilize.HonourMobilizeTimeController;
import com.hawk.activity.type.impl.honourMobilize.entity.HonourMobilizeEntity;
import com.hawk.activity.type.impl.hotBloodWar.HotBloodWarActivity;
import com.hawk.activity.type.impl.hotBloodWar.HotBloodWarHandler;
import com.hawk.activity.type.impl.hotBloodWar.HotBloodWarTimeController;
import com.hawk.activity.type.impl.hotBloodWar.entity.HotBloodWarEntity;
import com.hawk.activity.type.impl.immgration.ImmgrationActivity;
import com.hawk.activity.type.impl.immgration.ImmgrationActivityHandler;
import com.hawk.activity.type.impl.immgration.ImmgrationActivityTimeController;
import com.hawk.activity.type.impl.inherit.InheritActivity;
import com.hawk.activity.type.impl.inherit.InheritHandler;
import com.hawk.activity.type.impl.inherit.InheritTimeController;
import com.hawk.activity.type.impl.inherit.entity.InheritEntity;
import com.hawk.activity.type.impl.inheritNew.InheritNewActivity;
import com.hawk.activity.type.impl.inheritNew.InheritNewHandler;
import com.hawk.activity.type.impl.inheritNew.InheritNewTimeController;
import com.hawk.activity.type.impl.inheritNew.entity.InheritNewEntity;
import com.hawk.activity.type.impl.invest.InvestActivity;
import com.hawk.activity.type.impl.invest.InvestActivityHandler;
import com.hawk.activity.type.impl.invest.InvestController;
import com.hawk.activity.type.impl.invest.entity.InvestEntity;
import com.hawk.activity.type.impl.inviteMerge.InviteMergeActivity;
import com.hawk.activity.type.impl.inviteMerge.InviteMergeHandler;
import com.hawk.activity.type.impl.inviteMerge.InviteMergeTimeController;
import com.hawk.activity.type.impl.jigsawconnect.JigsawConnectActivity;
import com.hawk.activity.type.impl.jigsawconnect.JigsawConnectTimeController;
import com.hawk.activity.type.impl.jigsawconnect.entity.JigsawConnectEntity;
import com.hawk.activity.type.impl.jigsawconnect326.JXJigsawConnectActivity;
import com.hawk.activity.type.impl.jigsawconnect326.JXJigsawConnectTimeController;
import com.hawk.activity.type.impl.jigsawconnect326.entity.JXJigsawConnectEntity;
import com.hawk.activity.type.impl.jijiaSkin.JijiaSkinActivity;
import com.hawk.activity.type.impl.jijiaSkin.JijiaSkinHandler;
import com.hawk.activity.type.impl.jijiaSkin.JijiaSkinTimeController;
import com.hawk.activity.type.impl.jijiaSkin.entity.JijiaSkinEntity;
import com.hawk.activity.type.impl.joybuy.JoyBuyActivity;
import com.hawk.activity.type.impl.joybuy.JoyBuyActivityHandler;
import com.hawk.activity.type.impl.joybuy.JoyBuyTimeController;
import com.hawk.activity.type.impl.joybuy.entity.ActivityJoyBuyEntity;
import com.hawk.activity.type.impl.loginday.LoginDayActivity;
import com.hawk.activity.type.impl.loginday.LoginDayTimeController;
import com.hawk.activity.type.impl.loginday.entity.ActivityLoginDayEntity;
import com.hawk.activity.type.impl.logindayTwo.LoginDayTwoActivity;
import com.hawk.activity.type.impl.logindayTwo.LoginDayTwoTimeController;
import com.hawk.activity.type.impl.logindayTwo.entity.ActivityLoginDayTwoEntity;
import com.hawk.activity.type.impl.loginfund.LoginFundActivity;
import com.hawk.activity.type.impl.loginfund.LoginFundActivityHandler;
import com.hawk.activity.type.impl.loginfund.LoginFundTimeController;
import com.hawk.activity.type.impl.loginfund.entity.LoginFundEntity;
import com.hawk.activity.type.impl.loginfundtwo.LoginFundTwoActivity;
import com.hawk.activity.type.impl.loginfundtwo.LoginFundTwoTimeController;
import com.hawk.activity.type.impl.loginfundtwo.entity.LoginFundTwoEntity;
import com.hawk.activity.type.impl.logingift.LoginGiftActivity;
import com.hawk.activity.type.impl.logingift.LoginGiftActivityHandler;
import com.hawk.activity.type.impl.logingift.LoginGiftTimeController;
import com.hawk.activity.type.impl.logingift.entity.LoginGiftEntity;
import com.hawk.activity.type.impl.loginsign.LoginSignActivity;
import com.hawk.activity.type.impl.loginsign.LoginSignActivityHandler;
import com.hawk.activity.type.impl.loginsign.LoginSignTimeController;
import com.hawk.activity.type.impl.loginsign.entity.ActivityLoginSignEntity;
import com.hawk.activity.type.impl.lotteryDraw.LotteryDrawActivity;
import com.hawk.activity.type.impl.lotteryDraw.LotteryDrawActivityHandler;
import com.hawk.activity.type.impl.lotteryDraw.LotteryDrawTimeController;
import com.hawk.activity.type.impl.lotteryDraw.entity.LotteryDrawEntity;
import com.hawk.activity.type.impl.lotteryTicket.LotteryTicketActivity;
import com.hawk.activity.type.impl.lotteryTicket.LotteryTicketHandler;
import com.hawk.activity.type.impl.lotteryTicket.LotteryTicketTimeController;
import com.hawk.activity.type.impl.lotteryTicket.entitiy.LotteryTicketEntity;
import com.hawk.activity.type.impl.loverMeet.LoverMeetActivity;
import com.hawk.activity.type.impl.loverMeet.LoverMeetController;
import com.hawk.activity.type.impl.loverMeet.LoverMeetHandler;
import com.hawk.activity.type.impl.loverMeet.entity.LoverMeetEntity;
import com.hawk.activity.type.impl.luckGetGold.LuckGetGoldActivity;
import com.hawk.activity.type.impl.luckGetGold.LuckGetGoldHandler;
import com.hawk.activity.type.impl.luckGetGold.LuckGetGoldTimeController;
import com.hawk.activity.type.impl.luckGetGold.entity.LuckGetGoldEntity;
import com.hawk.activity.type.impl.luckyBox.LuckBoxController;
import com.hawk.activity.type.impl.luckyBox.LuckyBoxActivity;
import com.hawk.activity.type.impl.luckyBox.LuckyBoxHandler;
import com.hawk.activity.type.impl.luckyBox.entity.LuckyBoxEntity;
import com.hawk.activity.type.impl.luckyDiscount.LuckyDiscountActivity;
import com.hawk.activity.type.impl.luckyDiscount.LuckyDiscountActivityTimeController;
import com.hawk.activity.type.impl.luckyDiscount.LuckyDiscountHandler;
import com.hawk.activity.type.impl.luckyDiscount.entity.LuckyDiscountEntity;
import com.hawk.activity.type.impl.luckyStar.LuckyStarActivity;
import com.hawk.activity.type.impl.luckyStar.LuckyStarHandler;
import com.hawk.activity.type.impl.luckyStar.LuckyStartTimeController;
import com.hawk.activity.type.impl.luckyStar.entity.LuckyStarEntity;
import com.hawk.activity.type.impl.luckyWelfare.LuckyWelfareActivity;
import com.hawk.activity.type.impl.luckyWelfare.LuckyWelfareTimeController;
import com.hawk.activity.type.impl.luckyWelfare.entity.LuckyWelfareEntity;
import com.hawk.activity.type.impl.machineAwake.MachineAwakeActivity;
import com.hawk.activity.type.impl.machineAwake.MachineAwakeHandler;
import com.hawk.activity.type.impl.machineAwake.MachineAwakeTimeController;
import com.hawk.activity.type.impl.machineAwake.entity.MachineAwakeEntity;
import com.hawk.activity.type.impl.machineAwakeTwo.MachineAwakeTwoActivity;
import com.hawk.activity.type.impl.machineAwakeTwo.MachineAwakeTwoHandler;
import com.hawk.activity.type.impl.machineAwakeTwo.MachineAwakeTwoTimeController;
import com.hawk.activity.type.impl.machineAwakeTwo.entity.MachineAwakeTwoEntity;
import com.hawk.activity.type.impl.machineLab.MachineLabActivity;
import com.hawk.activity.type.impl.machineLab.MachineLabController;
import com.hawk.activity.type.impl.machineLab.MachineLabHandler;
import com.hawk.activity.type.impl.machineLab.entity.MachineLabEntity;
import com.hawk.activity.type.impl.machineSell.MachineSellActivity;
import com.hawk.activity.type.impl.machineSell.MachineSellHandler;
import com.hawk.activity.type.impl.machineSell.MachineSellTimeController;
import com.hawk.activity.type.impl.machineSell.entity.MachineSellEntity;
import com.hawk.activity.type.impl.materialTransport.MaterialTransportActivity;
import com.hawk.activity.type.impl.materialTransport.MaterialTransportHandler;
import com.hawk.activity.type.impl.materialTransport.MaterialTransportTimeController;
import com.hawk.activity.type.impl.materialTransport.entity.MaterialTransportEntity;
import com.hawk.activity.type.impl.mechacoreexplore.CoreExploreActivity;
import com.hawk.activity.type.impl.mechacoreexplore.CoreExploreHandler;
import com.hawk.activity.type.impl.mechacoreexplore.CoreExploreTimeController;
import com.hawk.activity.type.impl.mechacoreexplore.entity.CoreExploreEntity;
import com.hawk.activity.type.impl.medalAction.MedalActionActivity;
import com.hawk.activity.type.impl.medalAction.MedalActionHandler;
import com.hawk.activity.type.impl.medalAction.MedalActionTimeController;
import com.hawk.activity.type.impl.medalAction.entity.MedalActionEntity;
import com.hawk.activity.type.impl.medalFund.MedalFundActivity;
import com.hawk.activity.type.impl.medalFund.MedalFundHandler;
import com.hawk.activity.type.impl.medalFund.MedalFundTimeController;
import com.hawk.activity.type.impl.medalFund.entity.MedalFundEntity;
import com.hawk.activity.type.impl.medalfundtwo.MedalFundTwoActivity;
import com.hawk.activity.type.impl.medalfundtwo.MedalFundTwoHandler;
import com.hawk.activity.type.impl.medalfundtwo.MedalFundTwoTimeController;
import com.hawk.activity.type.impl.medalfundtwo.entity.MedalFundTwoEntity;
import com.hawk.activity.type.impl.mergeAnnounce.MergeAnnounceActivity;
import com.hawk.activity.type.impl.mergeAnnounce.MergeAnnounceController;
import com.hawk.activity.type.impl.mergecompetition.MergeCompetitionActivity;
import com.hawk.activity.type.impl.mergecompetition.MergeCompetitionActivityHandler;
import com.hawk.activity.type.impl.mergecompetition.MergeCompetitionTimeController;
import com.hawk.activity.type.impl.mergecompetition.entity.MergeCompetitionEntity;
import com.hawk.activity.type.impl.midAutumn.MidAutumnActivity;
import com.hawk.activity.type.impl.midAutumn.MidAutumnHandler;
import com.hawk.activity.type.impl.midAutumn.MidAutumnTimeController;
import com.hawk.activity.type.impl.midAutumn.entity.MidAutumnEntity;
import com.hawk.activity.type.impl.militaryprepare.MilitaryPrepareActivity;
import com.hawk.activity.type.impl.militaryprepare.MilitaryPrepareHandler;
import com.hawk.activity.type.impl.militaryprepare.MilitaryPrepareTimeController;
import com.hawk.activity.type.impl.militaryprepare.entity.MilitaryPrepareEntity;
import com.hawk.activity.type.impl.monster2.YuriAchieveActivity;
import com.hawk.activity.type.impl.monster2.YuriAchieveTimeController;
import com.hawk.activity.type.impl.monster2.entity.Monster2Entity;
import com.hawk.activity.type.impl.monster2Show.YuriAchieveShowActivity;
import com.hawk.activity.type.impl.monster2Show.YuriAchieveShowTimeController;
import com.hawk.activity.type.impl.monthcard.MonthCardActivity;
import com.hawk.activity.type.impl.monthcard.MonthCardActivityHandler;
import com.hawk.activity.type.impl.monthcard.MonthCardTimeController;
import com.hawk.activity.type.impl.monthcard.entity.ActivityMonthCardEntity;
import com.hawk.activity.type.impl.newFirstRecharge.NewFirstRechargeActivity;
import com.hawk.activity.type.impl.newFirstRecharge.NewFirstRechargeHandler;
import com.hawk.activity.type.impl.newFirstRecharge.NewFirstRechargeTimeController;
import com.hawk.activity.type.impl.newFirstRecharge.entity.NewFirstRechargeEntity;
import com.hawk.activity.type.impl.newStart.NewStartActivity;
import com.hawk.activity.type.impl.newStart.NewStartHandler;
import com.hawk.activity.type.impl.newStart.NewStartTimeController;
import com.hawk.activity.type.impl.newStart.entity.NewStartEntity;
import com.hawk.activity.type.impl.newbietrain.NewbieTrainActivity;
import com.hawk.activity.type.impl.newbietrain.NewbieTrainHandler;
import com.hawk.activity.type.impl.newbietrain.NewbieTrainTimeController;
import com.hawk.activity.type.impl.newbietrain.entity.NewbieTrainEntity;
import com.hawk.activity.type.impl.newyearTreasure.NewyearTreasureActivity;
import com.hawk.activity.type.impl.newyearTreasure.NewyearTreasureTimeController;
import com.hawk.activity.type.impl.newyearTreasure.entity.NewyearTreasureEntity;
import com.hawk.activity.type.impl.newyearlottery.NewyearLotteryActivity;
import com.hawk.activity.type.impl.newyearlottery.NewyearLotteryActivityHandler;
import com.hawk.activity.type.impl.newyearlottery.NewyearLotteryTimeController;
import com.hawk.activity.type.impl.newyearlottery.entity.NewyearLotteryEntity;
import com.hawk.activity.type.impl.onermbpurchase.OneRMBPurchaseActivity;
import com.hawk.activity.type.impl.onermbpurchase.OneRMBPurchaseController;
import com.hawk.activity.type.impl.onermbpurchase.entity.OneRMBPurchaseEntity;
import com.hawk.activity.type.impl.order.OrderActivity;
import com.hawk.activity.type.impl.order.OrderActivityHandler;
import com.hawk.activity.type.impl.order.OrderTimeController;
import com.hawk.activity.type.impl.order.activityEquipOrder.OrderEquipActivity;
import com.hawk.activity.type.impl.order.activityEquipOrder.OrderEquipActivityHandler;
import com.hawk.activity.type.impl.order.activityEquipOrder.OrderEquipTimeController;
import com.hawk.activity.type.impl.order.activityEquipOrder.entity.OrderEquipEntity;
import com.hawk.activity.type.impl.order.activityNewOrder.NewOrderActivity;
import com.hawk.activity.type.impl.order.activityNewOrder.NewOrderActivityHandler;
import com.hawk.activity.type.impl.order.activityNewOrder.NewOrderTimeController;
import com.hawk.activity.type.impl.order.activityNewOrder.entity.NewActivityOrderEntity;
import com.hawk.activity.type.impl.order.activityOrderTwo.OrderTwoActivity;
import com.hawk.activity.type.impl.order.activityOrderTwo.OrderTwoActivityHandler;
import com.hawk.activity.type.impl.order.activityOrderTwo.OrderTwoTimeController;
import com.hawk.activity.type.impl.order.activityOrderTwo.entity.OrderTwoEntity;
import com.hawk.activity.type.impl.order.entity.ActivityOrderEntity;
import com.hawk.activity.type.impl.ordnanceFortress.OrdnanceFortressActivity;
import com.hawk.activity.type.impl.ordnanceFortress.OrdnanceFortressController;
import com.hawk.activity.type.impl.ordnanceFortress.OrdnanceFortressHandler;
import com.hawk.activity.type.impl.ordnanceFortress.entity.OrdnanceFortressEntity;
import com.hawk.activity.type.impl.overlordBlessing.OverlordBlessingActivity;
import com.hawk.activity.type.impl.overlordBlessing.OverlordBlessingHandler;
import com.hawk.activity.type.impl.overlordBlessing.OverlordBlessingTimeController;
import com.hawk.activity.type.impl.overlordBlessing.entity.OverlordBlessingEntity;
import com.hawk.activity.type.impl.pandoraBox.PandoraBoxActivity;
import com.hawk.activity.type.impl.pandoraBox.PandoraBoxHandler;
import com.hawk.activity.type.impl.pandoraBox.PandoraBoxTimeController;
import com.hawk.activity.type.impl.pddActivity.PDDActivity;
import com.hawk.activity.type.impl.pddActivity.PDDActivityHandler;
import com.hawk.activity.type.impl.pddActivity.PDDActivityTimeController;
import com.hawk.activity.type.impl.pddActivity.entity.PDDActivityEntity;
import com.hawk.activity.type.impl.peakHonour.PeakHonourActivity;
import com.hawk.activity.type.impl.peakHonour.PeakHonourHandler;
import com.hawk.activity.type.impl.peakHonour.PeakHonourTimeController;
import com.hawk.activity.type.impl.pioneergift.PioneerGiftActivity;
import com.hawk.activity.type.impl.pioneergift.PioneerGiftActivityHandler;
import com.hawk.activity.type.impl.pioneergift.PioneerGiftController;
import com.hawk.activity.type.impl.pioneergift.entity.PioneerGiftEntity;
import com.hawk.activity.type.impl.plan.PlanActivity;
import com.hawk.activity.type.impl.plan.PlanActivityHandler;
import com.hawk.activity.type.impl.plan.PlanActivityTimeController;
import com.hawk.activity.type.impl.plan.entity.PlanEntity;
import com.hawk.activity.type.impl.planetexploration.PlanetExploreActivity;
import com.hawk.activity.type.impl.planetexploration.PlanetExploreHandler;
import com.hawk.activity.type.impl.planetexploration.PlanetExploreTimeController;
import com.hawk.activity.type.impl.planetexploration.entity.PlanetExploreEntity;
import com.hawk.activity.type.impl.plantFortress.PlantFortressActivity;
import com.hawk.activity.type.impl.plantFortress.PlantFortressController;
import com.hawk.activity.type.impl.plantFortress.PlantFortressHandler;
import com.hawk.activity.type.impl.plantFortress.entity.PlantFortressEntity;
import com.hawk.activity.type.impl.plantSoldierFactory.PlantSoldierFactoryActivity;
import com.hawk.activity.type.impl.plantSoldierFactory.PlantSoldierFactoryHandler;
import com.hawk.activity.type.impl.plantSoldierFactory.PlantSoldierFactoryTimeController;
import com.hawk.activity.type.impl.plantSoldierFactory.entity.PlantSoldierFactoryActivityEntity;
import com.hawk.activity.type.impl.plantsecret.PlantSecretActivity;
import com.hawk.activity.type.impl.plantsecret.PlantSecretController;
import com.hawk.activity.type.impl.plantsecret.PlantSecretHandler;
import com.hawk.activity.type.impl.plantsecret.entity.PlantSecretEntity;
import com.hawk.activity.type.impl.plantweapon.PlantWeaponActivity;
import com.hawk.activity.type.impl.plantweapon.PlantWeaponHandler;
import com.hawk.activity.type.impl.plantweapon.PlantWeaponTimeController;
import com.hawk.activity.type.impl.plantweapon.entity.PlantWeaponEntity;
import com.hawk.activity.type.impl.plantweaponback.PlantWeaponBackActivity;
import com.hawk.activity.type.impl.plantweaponback.PlantWeaponBackController;
import com.hawk.activity.type.impl.plantweaponback.PlantWeaponBackHandler;
import com.hawk.activity.type.impl.plantweaponback.entity.PlantWeaponBackEntity;
import com.hawk.activity.type.impl.playerComeBack.ComeBackAchieveTaskActivity;
import com.hawk.activity.type.impl.playerComeBack.ComeBackBuyActivity;
import com.hawk.activity.type.impl.playerComeBack.ComeBackExchangeActivity;
import com.hawk.activity.type.impl.playerComeBack.ComeBackHandler;
import com.hawk.activity.type.impl.playerComeBack.ComeBackRewardActivity;
import com.hawk.activity.type.impl.playerComeBack.ComeBackVersionActivity;
import com.hawk.activity.type.impl.playerComeBack.entity.PlayerComeBackEntity;
import com.hawk.activity.type.impl.playerComeBack.timeController.ComeBackPlayerAchieveTimeController;
import com.hawk.activity.type.impl.playerComeBack.timeController.ComeBackPlayerBuyTimeController;
import com.hawk.activity.type.impl.playerComeBack.timeController.ComeBackPlayerExchangeTimeController;
import com.hawk.activity.type.impl.playerComeBack.timeController.ComeBackPlayerRewardTimeController;
import com.hawk.activity.type.impl.playerComeBack.timeController.ComeBackPlayerVersionTimeController;
import com.hawk.activity.type.impl.playerteamback.PlayerTeamBackActivity;
import com.hawk.activity.type.impl.playerteamback.PlayerTeamBackActivityController;
import com.hawk.activity.type.impl.playerteamback.entity.PlayerTeamBackEntity;
import com.hawk.activity.type.impl.pointSprint.PointSprintActivity;
import com.hawk.activity.type.impl.pointSprint.PointSprintHandler;
import com.hawk.activity.type.impl.pointSprint.PointSprintTimeController;
import com.hawk.activity.type.impl.pointSprint.entity.PointSprintEntity;
import com.hawk.activity.type.impl.powercollect.PowerCollectActivity;
import com.hawk.activity.type.impl.powercollect.PowerCollectHandler;
import com.hawk.activity.type.impl.powercollect.PowerCollectTimeController;
import com.hawk.activity.type.impl.powercollect.entity.PowerCollectEntity;
import com.hawk.activity.type.impl.powerfund.PowerFundActivity;
import com.hawk.activity.type.impl.powerfund.PowerFundActivityHandler;
import com.hawk.activity.type.impl.powerfund.PowerFundTimeController;
import com.hawk.activity.type.impl.powerfund.entity.PowerFundEntity;
import com.hawk.activity.type.impl.powerup.PowerUpActivity;
import com.hawk.activity.type.impl.powerup.PowerUpTimeController;
import com.hawk.activity.type.impl.powerup.entity.PowerUpEntity;
import com.hawk.activity.type.impl.preferential_surprise.PreferentialSupriseActivity;
import com.hawk.activity.type.impl.preferential_surprise.PreferentialSupriseTimeController;
import com.hawk.activity.type.impl.preferential_surprise.entity.PreferentialSupriseEntity;
import com.hawk.activity.type.impl.presentrebate.PresentRebateActivity;
import com.hawk.activity.type.impl.presentrebate.PresentRebateTimeController;
import com.hawk.activity.type.impl.presentrebate.entity.PresentRebateEntity;
import com.hawk.activity.type.impl.prestressingloss.PrestressingLossActivity;
import com.hawk.activity.type.impl.prestressingloss.PrestressingLossActivityHandler;
import com.hawk.activity.type.impl.prestressingloss.PrestressingLossTimeController;
import com.hawk.activity.type.impl.prestressingloss.entity.PrestressingLossEntity;
import com.hawk.activity.type.impl.questTreasure.QuestTreasureActivity;
import com.hawk.activity.type.impl.questTreasure.QuestTreasureHandler;
import com.hawk.activity.type.impl.questTreasure.QuestTreasureTimeController;
import com.hawk.activity.type.impl.questTreasure.entity.QuestTreasureEntity;
import com.hawk.activity.type.impl.questionShare.QuestionShareActivity;
import com.hawk.activity.type.impl.questionShare.QuestionShareHandler;
import com.hawk.activity.type.impl.questionShare.QuestionShareTimeController;
import com.hawk.activity.type.impl.questionShare.entity.QuestionShareEntity;
import com.hawk.activity.type.impl.radiationWar.RadiationWarActivity;
import com.hawk.activity.type.impl.radiationWar.RadiationWarTimeController;
import com.hawk.activity.type.impl.radiationWar.entity.RadiationWarEntity;
import com.hawk.activity.type.impl.radiationWarTwo.RadiationWarTwoActivity;
import com.hawk.activity.type.impl.radiationWarTwo.RadiationWarTwoTimeController;
import com.hawk.activity.type.impl.radiationWarTwo.entity.RadiationWarTwoEntity;
import com.hawk.activity.type.impl.recallFriend.RecallFriendActivity;
import com.hawk.activity.type.impl.recallFriend.RecallFriendHandler;
import com.hawk.activity.type.impl.recallFriend.RecallFriendTimeController;
import com.hawk.activity.type.impl.recallFriend.entity.RecallFriendEntity;
import com.hawk.activity.type.impl.rechargeFund.RechargeFundActivity;
import com.hawk.activity.type.impl.rechargeFund.RechargeFundHandler;
import com.hawk.activity.type.impl.rechargeFund.RechargeFundTimeController;
import com.hawk.activity.type.impl.rechargeFund.entity.RechargeFundEntity;
import com.hawk.activity.type.impl.rechargeGift.RechargeGiftActivity;
import com.hawk.activity.type.impl.rechargeGift.RechargeGiftTimeController;
import com.hawk.activity.type.impl.rechargeGift.entity.RechargeGiftEntity;
import com.hawk.activity.type.impl.rechargeQixi.RechargeQixiActivity;
import com.hawk.activity.type.impl.rechargeQixi.RechargeQixiTimeController;
import com.hawk.activity.type.impl.rechargeQixi.entity.RechargeQixiEntity;
import com.hawk.activity.type.impl.rechargeWelfare.RechargeWelfareActivity;
import com.hawk.activity.type.impl.rechargeWelfare.RechargeWelfareHandler;
import com.hawk.activity.type.impl.rechargeWelfare.RechargeWelfareTimeController;
import com.hawk.activity.type.impl.rechargeWelfare.entity.RechargeWelfareEntity;
import com.hawk.activity.type.impl.recoveryExchange.RecoveryExchangeActivity;
import com.hawk.activity.type.impl.recoveryExchange.RecoveryExchangeController;
import com.hawk.activity.type.impl.recoveryExchange.RecoveryExchangeHandler;
import com.hawk.activity.type.impl.recoveryExchange.entity.RecoveryExchangeEntity;
import com.hawk.activity.type.impl.redEnvelope.RedEnvelopeActivity;
import com.hawk.activity.type.impl.redEnvelope.RedEnvelopeHandler;
import com.hawk.activity.type.impl.redEnvelope.RedEnvelopeTimeController;
import com.hawk.activity.type.impl.redEnvelope.entity.RedEnvelopeEntity;
import com.hawk.activity.type.impl.redEnvelopePlayer.RedEnvelopePlayerActivity;
import com.hawk.activity.type.impl.redEnvelopePlayer.RedEnvelopePlayerTimeController;
import com.hawk.activity.type.impl.redPackage.RedPackageActivity;
import com.hawk.activity.type.impl.redPackage.RedPackageHandler;
import com.hawk.activity.type.impl.redPackage.RedPackageTimeController;
import com.hawk.activity.type.impl.redPackage.entity.RedPackageEntity;
import com.hawk.activity.type.impl.redblueticket.RedBlueTicketActivity;
import com.hawk.activity.type.impl.redblueticket.RedBlueTicketActivityController;
import com.hawk.activity.type.impl.redblueticket.RedBlueTicketActivityHandler;
import com.hawk.activity.type.impl.redblueticket.entity.RedBlueTicketActivityEntity;
import com.hawk.activity.type.impl.redkoi.RedkoiActivity;
import com.hawk.activity.type.impl.redkoi.RedkoiHandler;
import com.hawk.activity.type.impl.redkoi.RedkoiTimeController;
import com.hawk.activity.type.impl.redkoi.entity.RedkoiEntity;
import com.hawk.activity.type.impl.redrecharge.HappyRedRechargeActivity;
import com.hawk.activity.type.impl.redrecharge.HappyRedRechargeHandler;
import com.hawk.activity.type.impl.redrecharge.HappyRedRechargeTimeController;
import com.hawk.activity.type.impl.redrecharge.entity.HappyRedRechargeEntity;
import com.hawk.activity.type.impl.resourceDefense.ResourceDefenseActivity;
import com.hawk.activity.type.impl.resourceDefense.ResourceDefenseHandler;
import com.hawk.activity.type.impl.resourceDefense.ResourceDefenseTimeController;
import com.hawk.activity.type.impl.resourceDefense.entity.ResourceDefenseEntity;
import com.hawk.activity.type.impl.returnUpgrade.ReturnUpgradeActivity;
import com.hawk.activity.type.impl.returnUpgrade.ReturnUpgradeHandler;
import com.hawk.activity.type.impl.returnUpgrade.ReturnUpgradeTimeController;
import com.hawk.activity.type.impl.returnUpgrade.entity.ReturnUpgradeEntity;
import com.hawk.activity.type.impl.return_puzzle.RetrunPuzzleActivityHandler;
import com.hawk.activity.type.impl.return_puzzle.ReturnPuzzleActivity;
import com.hawk.activity.type.impl.return_puzzle.ReturnPuzzleTimeController;
import com.hawk.activity.type.impl.return_puzzle.entity.ReturnPuzzleEntity;
import com.hawk.activity.type.impl.rewardOrder.RewardOrderActivity;
import com.hawk.activity.type.impl.rewardOrder.RewardOrderHandler;
import com.hawk.activity.type.impl.rewardOrder.RewardOrderTimeController;
import com.hawk.activity.type.impl.rewardOrder.entity.RewardOrderEntity;
import com.hawk.activity.type.impl.roseGift.RoseGiftActivity;
import com.hawk.activity.type.impl.roseGift.RoseGiftHandler;
import com.hawk.activity.type.impl.roseGift.RoseGiftTimeController;
import com.hawk.activity.type.impl.roseGift.entity.RoseGiftEntity;
import com.hawk.activity.type.impl.roulette.RouletteActivity;
import com.hawk.activity.type.impl.roulette.RouletteHandler;
import com.hawk.activity.type.impl.roulette.RouletteTimeController;
import com.hawk.activity.type.impl.roulette.entity.RouletteEntity;
import com.hawk.activity.type.impl.samuraiBlackened.SamuraiBlackenedActivity;
import com.hawk.activity.type.impl.samuraiBlackened.SamuraiBlackenedActivityHandler;
import com.hawk.activity.type.impl.samuraiBlackened.SamuraiBlackenedTimeController;
import com.hawk.activity.type.impl.samuraiBlackened.entity.SamuraiBlackenedEntity;
import com.hawk.activity.type.impl.sceneImport.SceneImportActivity;
import com.hawk.activity.type.impl.sceneImport.SceneImportTimeController;
import com.hawk.activity.type.impl.seaTreasure.SeaTreasureActivity;
import com.hawk.activity.type.impl.seaTreasure.SeaTreasureHandler;
import com.hawk.activity.type.impl.seaTreasure.SeaTreasureTimeController;
import com.hawk.activity.type.impl.seaTreasure.entity.SeaTreasureEntity;
import com.hawk.activity.type.impl.seasonActivity.SeasonActivity;
import com.hawk.activity.type.impl.seasonActivity.SeasonActivityHandler;
import com.hawk.activity.type.impl.seasonActivity.SeasonActivityTimeController;
import com.hawk.activity.type.impl.seasonActivity.entity.SeasonActivityEntity;
import com.hawk.activity.type.impl.seasonpuzzle.SeasonPuzzleActivity;
import com.hawk.activity.type.impl.seasonpuzzle.SeasonPuzzleHandler;
import com.hawk.activity.type.impl.seasonpuzzle.SeasonPuzzleTimeController;
import com.hawk.activity.type.impl.seasonpuzzle.entity.SeasonPuzzleEntity;
import com.hawk.activity.type.impl.senceShare.SceneShareActivity;
import com.hawk.activity.type.impl.senceShare.SceneShareTimeController;
import com.hawk.activity.type.impl.senceShare.entity.SceneShareEntity;
import com.hawk.activity.type.impl.sendFlower.SendFlowerActivity;
import com.hawk.activity.type.impl.sendFlower.SendFlowerHandler;
import com.hawk.activity.type.impl.sendFlower.SendFlowerTimeController;
import com.hawk.activity.type.impl.sendFlower.entity.SendFlowerEntity;
import com.hawk.activity.type.impl.shareGlory.ShareGloryActivity;
import com.hawk.activity.type.impl.shareGlory.ShareGloryHandler;
import com.hawk.activity.type.impl.shareGlory.ShareGloryTimeController;
import com.hawk.activity.type.impl.shareprosperity.ShareProsperityActivity;
import com.hawk.activity.type.impl.shareprosperity.ShareProsperityController;
import com.hawk.activity.type.impl.shareprosperity.ShareProsperityHandler;
import com.hawk.activity.type.impl.shareprosperity.entity.ShareProsperityEntity;
import com.hawk.activity.type.impl.shootingPractice.ShootingPracticeActivity;
import com.hawk.activity.type.impl.shootingPractice.ShootingPracticeHandler;
import com.hawk.activity.type.impl.shootingPractice.ShootingPracticeTimeController;
import com.hawk.activity.type.impl.shootingPractice.entity.ShootingPracticeEntity;
import com.hawk.activity.type.impl.shopSkip.ShopSkipActivity;
import com.hawk.activity.type.impl.shopSkip.ShopSkipTimeController;
import com.hawk.activity.type.impl.skinPlan.SkinPlanActivity;
import com.hawk.activity.type.impl.skinPlan.SkinPlanHandler;
import com.hawk.activity.type.impl.skinPlan.SkinPlanTimeController;
import com.hawk.activity.type.impl.skinPlan.entity.SkinPlanEntity;
import com.hawk.activity.type.impl.snowball.SnowballActivity;
import com.hawk.activity.type.impl.snowball.SnowballHandler;
import com.hawk.activity.type.impl.snowball.SnowballTimeController;
import com.hawk.activity.type.impl.snowball.entity.SnowballEntity;
import com.hawk.activity.type.impl.soldierExchange.SoldierExchangeActivity;
import com.hawk.activity.type.impl.soldierExchange.SoldierExchangeActivityHandler;
import com.hawk.activity.type.impl.soldierExchange.SoldierExchangeTimeController;
import com.hawk.activity.type.impl.soldierExchange.entity.SoldierExchangeActivityEntity;
import com.hawk.activity.type.impl.spaceguard.SpaceGuardActivity;
import com.hawk.activity.type.impl.spaceguard.SpaceGuardHandler;
import com.hawk.activity.type.impl.spaceguard.SpaceGuardTimeController;
import com.hawk.activity.type.impl.spaceguard.entity.SpaceGuardEntity;
import com.hawk.activity.type.impl.spread.SpreadActivity;
import com.hawk.activity.type.impl.spread.SpreadActivityHandler;
import com.hawk.activity.type.impl.spread.SpreadActivityTimeController;
import com.hawk.activity.type.impl.spread.entity.SpreadEntity;
import com.hawk.activity.type.impl.starInvest.StarInvestActivity;
import com.hawk.activity.type.impl.starInvest.StarInvestHandler;
import com.hawk.activity.type.impl.starInvest.StarInvestTimeController;
import com.hawk.activity.type.impl.starInvest.entity.StarInvestEntity;
import com.hawk.activity.type.impl.starLightSign.StarLightSignActivity;
import com.hawk.activity.type.impl.starLightSign.StarLightSignActivityHandler;
import com.hawk.activity.type.impl.starLightSign.StarLightSignActivityTimeController;
import com.hawk.activity.type.impl.starLightSign.entity.StarLightSignActivityEntity;
import com.hawk.activity.type.impl.stronestleader.StrongestLeaderActivity;
import com.hawk.activity.type.impl.stronestleader.StrongestLeaderActivityHandler;
import com.hawk.activity.type.impl.stronestleader.StrongestLeaderTimeController;
import com.hawk.activity.type.impl.stronestleader.entity.ActivityStrongestLeaderEntity;
import com.hawk.activity.type.impl.strongestGuild.StrongestGuildActivity;
import com.hawk.activity.type.impl.strongestGuild.StrongestGuildHandler;
import com.hawk.activity.type.impl.strongestGuild.StrongestGuildTimeController;
import com.hawk.activity.type.impl.strongestGuild.entity.StrongestGuildEntity;
import com.hawk.activity.type.impl.strongpoint.StrongpointActivity;
import com.hawk.activity.type.impl.strongpoint.StrongpointTimeController;
import com.hawk.activity.type.impl.strongpoint.entity.ActivityStrongpointEntity;
import com.hawk.activity.type.impl.submarineWar.SubmarineWarActivity;
import com.hawk.activity.type.impl.submarineWar.SubmarineWarHandler;
import com.hawk.activity.type.impl.submarineWar.SubmarineWarTimeController;
import com.hawk.activity.type.impl.submarineWar.entity.SubmarineWarEntity;
import com.hawk.activity.type.impl.superDiscount.SuperDiscountActivity;
import com.hawk.activity.type.impl.superDiscount.SuperDiscountActivityController;
import com.hawk.activity.type.impl.superDiscount.SuperDiscountHandler;
import com.hawk.activity.type.impl.superDiscount.entity.SuperDiscountEntity;
import com.hawk.activity.type.impl.superWeapon.SuperWeaponActivity;
import com.hawk.activity.type.impl.supergold.SuperGoldATimeController;
import com.hawk.activity.type.impl.supergold.SuperGoldActivity;
import com.hawk.activity.type.impl.supergold.SuperGoldHandler;
import com.hawk.activity.type.impl.supergold.entity.SuperGoldEntity;
import com.hawk.activity.type.impl.supergoldtwo.SuperGoldTwoActivity;
import com.hawk.activity.type.impl.supergoldtwo.SuperGoldTwoHandler;
import com.hawk.activity.type.impl.supergoldtwo.SuperGoldTwoTimeController;
import com.hawk.activity.type.impl.supergoldtwo.entity.SuperGoldTwoEntity;
import com.hawk.activity.type.impl.supersoldierInvest.SupersoldierInvestActivity;
import com.hawk.activity.type.impl.supersoldierInvest.SupersoldierInvestHandler;
import com.hawk.activity.type.impl.supersoldierInvest.SupersoldierInvestTimeController;
import com.hawk.activity.type.impl.supersoldierInvest.entity.SupersoldierInvestEntity;
import com.hawk.activity.type.impl.supplyCrate.SupplyCrateActivity;
import com.hawk.activity.type.impl.supplyCrate.SupplyCrateHandler;
import com.hawk.activity.type.impl.supplyCrate.SupplyCrateTimeController;
import com.hawk.activity.type.impl.supplyCrate.entity.SupplyCrateEntity;
import com.hawk.activity.type.impl.supplyStation.SupplyStationActivity;
import com.hawk.activity.type.impl.supplyStation.SupplyStationHandler;
import com.hawk.activity.type.impl.supplyStation.SupplyStationTimeController;
import com.hawk.activity.type.impl.supplyStation.entity.SupplyStationEntity;
import com.hawk.activity.type.impl.supplyStationCopy.SupplyStationCopyActivity;
import com.hawk.activity.type.impl.supplyStationCopy.SupplyStationCopyHandler;
import com.hawk.activity.type.impl.supplyStationCopy.SupplyStationCopyTimeController;
import com.hawk.activity.type.impl.supplyStationCopy.entity.SupplyStationCopyEntity;
import com.hawk.activity.type.impl.supplyStationTwo.SupplyStationTwoActivity;
import com.hawk.activity.type.impl.supplyStationTwo.SupplyStationTwoHandler;
import com.hawk.activity.type.impl.supplyStationTwo.SupplyStationTwoTimeController;
import com.hawk.activity.type.impl.supplyStationTwo.entity.SupplyStationTwoEntity;
import com.hawk.activity.type.impl.tiberiumGuess.TiberiumGuessActivity;
import com.hawk.activity.type.impl.tiberiumGuess.TiberiumGuessHandler;
import com.hawk.activity.type.impl.tiberiumGuess.TiberiumGuessTimeController;
import com.hawk.activity.type.impl.tiberiumGuess.entity.TiberiumGuessEntity;
import com.hawk.activity.type.impl.timeLimitBuy.TimeLimitBuyActivity;
import com.hawk.activity.type.impl.timeLimitBuy.TimeLimitBuyHandler;
import com.hawk.activity.type.impl.timeLimitBuy.TimeLimitBuyTimeController;
import com.hawk.activity.type.impl.timeLimitBuy.entity.TimeLimitBuyEntity;
import com.hawk.activity.type.impl.timeLimitDrop.TimeLimitDropActivity;
import com.hawk.activity.type.impl.timeLimitDrop.TimeLimitDropHandler;
import com.hawk.activity.type.impl.timeLimitDrop.TimeLimitDropTimeController;
import com.hawk.activity.type.impl.timeLimitDrop.entity.TimeLimitDropEntity;
import com.hawk.activity.type.impl.timeLimitLogin.TimeLimitLoginActivity;
import com.hawk.activity.type.impl.timeLimitLogin.TimeLimitLoginHandler;
import com.hawk.activity.type.impl.timeLimitLogin.TimeLimitLoginTimeController;
import com.hawk.activity.type.impl.timeLimitLogin.entity.TimeLimitLoginEntity;
import com.hawk.activity.type.impl.travelshopAssist.TravelShopAssistActivity;
import com.hawk.activity.type.impl.travelshopAssist.TravelShopAssistTimeController;
import com.hawk.activity.type.impl.travelshopAssist.entity.TravelShopAssistEntity;
import com.hawk.activity.type.impl.treasureCavalry.TreasureCavalryActivity;
import com.hawk.activity.type.impl.treasureCavalry.TreasureCavalryHandler;
import com.hawk.activity.type.impl.treasureCavalry.TreasureCavalryTimeController;
import com.hawk.activity.type.impl.treasureCavalry.entity.TreasureCavalryEntity;
import com.hawk.activity.type.impl.treasury.TreasuryActivity;
import com.hawk.activity.type.impl.treasury.TreasuryController;
import com.hawk.activity.type.impl.treasury.TreasuryHandler;
import com.hawk.activity.type.impl.urlModel342.UrlModel342Activity;
import com.hawk.activity.type.impl.urlModel342.UrlModel342TimeController;
import com.hawk.activity.type.impl.urlModel343.UrlModel343Activity;
import com.hawk.activity.type.impl.urlModel343.UrlModel343TimeController;
import com.hawk.activity.type.impl.urlModel344.UrlModel344Activity;
import com.hawk.activity.type.impl.urlModel344.UrlModel344TimeController;
import com.hawk.activity.type.impl.urlModelEight.UrlModelEightActivity;
import com.hawk.activity.type.impl.urlModelEight.UrlModelEightTimeController;
import com.hawk.activity.type.impl.urlModelFive.UrlModelFiveActivity;
import com.hawk.activity.type.impl.urlModelFive.UrlModelFiveTimeController;
import com.hawk.activity.type.impl.urlModelFour.UrlModelFourActivity;
import com.hawk.activity.type.impl.urlModelFour.UrlModelFourTimeController;
import com.hawk.activity.type.impl.urlModelNine.UrlModelNineActivity;
import com.hawk.activity.type.impl.urlModelNine.UrlModelNineTimeController;
import com.hawk.activity.type.impl.urlModelOne.UrlModelOneActivity;
import com.hawk.activity.type.impl.urlModelOne.UrlModelOneTimeController;
import com.hawk.activity.type.impl.urlModelSeven.UrlModelSevenActivity;
import com.hawk.activity.type.impl.urlModelSeven.UrlModelSevenTimeController;
import com.hawk.activity.type.impl.urlModelSix.UrlModelSixActivity;
import com.hawk.activity.type.impl.urlModelSix.UrlModelSixTimeController;
import com.hawk.activity.type.impl.urlModelTen.UrlModelTenActivity;
import com.hawk.activity.type.impl.urlModelTen.UrlModelTenTimeController;
import com.hawk.activity.type.impl.urlModelThree.UrlModelThreeActivity;
import com.hawk.activity.type.impl.urlModelThree.UrlModelThreeTimeController;
import com.hawk.activity.type.impl.urlModelTwo.UrlModelTwoActivity;
import com.hawk.activity.type.impl.urlModelTwo.UrlModelTwoTimeController;
import com.hawk.activity.type.impl.virtualLaboratory.VirtualLaboratoryActivity;
import com.hawk.activity.type.impl.virtualLaboratory.VirtualLaboratoryHandler;
import com.hawk.activity.type.impl.virtualLaboratory.VirtualLaboratoryTimeController;
import com.hawk.activity.type.impl.virtualLaboratory.entity.VirtualLaboratoryEntity;
import com.hawk.activity.type.impl.warFlagTwo.WarFlagTwoActivity;
import com.hawk.activity.type.impl.warFlagTwo.WarFlagTwoHandler;
import com.hawk.activity.type.impl.warFlagTwo.WarFlagTwoTimeController;
import com.hawk.activity.type.impl.warFlagTwo.entity.WarFlagTwoEntity;
import com.hawk.activity.type.impl.warzoneWeal.WarzoneWealActivity;
import com.hawk.activity.type.impl.warzoneWeal.WarzoneWealTimeController;
import com.hawk.activity.type.impl.warzoneWeal.entity.WarzoneWealEntity;
import com.hawk.activity.type.impl.warzonewealcopy.WarzoneWealCopyActivity;
import com.hawk.activity.type.impl.warzonewealcopy.WarzoneWealCopyTimeController;
import com.hawk.activity.type.impl.warzonewealcopy.entity.WarzoneWealCopyEntity;
import com.hawk.activity.type.impl.xzq.XZQActivity;
import com.hawk.activity.type.impl.yuriAchieveShowTwo.YuriAchieveShowTwoActivity;
import com.hawk.activity.type.impl.yuriAchieveShowTwo.YuriAchieveShowTwoTimeController;
import com.hawk.activity.type.impl.yuriAchieveTwo.YuriAchieveTwoActivity;
import com.hawk.activity.type.impl.yuriAchieveTwo.YuriAchieveTwoTimeController;
import com.hawk.activity.type.impl.yuriAchieveTwo.entity.YuriAchieveTwoEntity;
import com.hawk.activity.type.impl.yurirevenge.YuriRevengeActivity;
import com.hawk.activity.type.impl.yurirevenge.YuriRevengeTimeController;
import com.hawk.game.protocol.Activity;

/**
 * 活动类型
 *
 * @author PhilChen
 *
 */
public enum ActivityType {
    /** 空白模版(供展示图片) */
    EMPTY_MODEL_ACTIVITY(Activity.ActivityType.EMPTY_MODEL_VALUE, null, new EmptyModelActivity(0, null), new EmptyModelTimeController()),
    /** 空白模版2(供展示图片) */
    EMPTY_MODEL_TWO_ACTIVITY(Activity.ActivityType.EMPTY_MODEL_TWO_VALUE, null, new EmptyModelTwoActivity(0, null), new EmptyModelTwoTimeController()),
    /** 空白模版3(供展示图片) */
    EMPTY_MODEL_THREE_ACTIVITY(Activity.ActivityType.EMPTY_MODEL_THREE_VALUE, null, new EmptyModelThreeActivity(0, null), new EmptyModelThreeTimeController()),
    /** 空白模版4(供展示图片) */
    EMPTY_MODEL_FOUR_ACTIVITY(Activity.ActivityType.EMPTY_MODEL_FOUR_VALUE, null, new EmptyModelFourActivity(0, null), new EmptyModelFourTimeController()),
    /** 空白模版5(供展示图片) */
    EMPTY_MODEL_FIVE_ACTIVITY(Activity.ActivityType.EMPTY_MODEL_FIVE_VALUE, null, new EmptyModelFiveActivity(0, null), new EmptyModelFiveTimeController()),
    /** 空白模版6(供展示图片) */
    EMPTY_MODEL_SIX_ACTIVITY(Activity.ActivityType.EMPTY_MODEL_SIX_VALUE, null, new EmptyModelSixActivity(0, null), new EmptyModelSixTimeController()),
    /** 空白模版7(供展示图片) */
    EMPTY_MODEL_SEVEN_ACTIVITY(Activity.ActivityType.EMPTY_MODEL_SEVEN_VALUE, null, new EmptyModelSevenActivity(0, null), new EmptyModelSevenTimeController()),
    /** 空白模版8(供展示图片) */
    EMPTY_MODEL_EIGHT_ACTIVITY(Activity.ActivityType.EMPTY_MODEL_EIGHT_VALUE, null, new EmptyModelEightActivity(0, null), new EmptyModelEightTimeController()),

    /** url模版1(供url跳转) */
    URL_MODEL_ONE_ACTIVITY(Activity.ActivityType.URL_MODEL_ONE_VALUE, null, new UrlModelOneActivity(0, null), new UrlModelOneTimeController()),
    /** url模版2(供url跳转) */
    URL_MODEL_TWO_ACTIVITY(Activity.ActivityType.URL_MODEL_TWO_VALUE, null, new UrlModelTwoActivity(0, null), new UrlModelTwoTimeController()),
    /** url模版3(供url跳转) */
    URL_MODEL_THREE_ACTIVITY(Activity.ActivityType.URL_MODEL_THREE_VALUE, null, new UrlModelThreeActivity(0, null), new UrlModelThreeTimeController()),
    /** url模版4(供url跳转) */
    URL_MODEL_FOUR_ACTIVITY(Activity.ActivityType.URL_MODEL_FOUR_VALUE, null, new UrlModelFourActivity(0, null), new UrlModelFourTimeController()),
    /** url模版5(供url跳转) */
    URL_MODEL_FIVE_ACTIVITY(Activity.ActivityType.URL_MODEL_FIVE_VALUE, null, new UrlModelFiveActivity(0, null), new UrlModelFiveTimeController()),
    /** url模版6(供url跳转) */
    URL_MODEL_SIX_ACTIVITY(Activity.ActivityType.URL_MODEL_SIX_VALUE, null, new UrlModelSixActivity(0, null), new UrlModelSixTimeController()),
    /** url模版7(供url跳转) */
    URL_MODEL_SEVEN_ACTIVITY(Activity.ActivityType.URL_MODEL_SEVEN_VALUE, null, new UrlModelSevenActivity(0, null), new UrlModelSevenTimeController()),
    /** url模版8(供url跳转) */
    URL_MODEL_EIGHT_ACTIVITY(Activity.ActivityType.URL_MODEL_EIGHT_VALUE, null, new UrlModelEightActivity(0, null), new UrlModelEightTimeController()),

    /** url模版9(供url跳转) */
    URL_MODEL_NINE_ACTIVITY(Activity.ActivityType.URL_MODEL_NINE_VALUE, null, new UrlModelNineActivity(0, null), new UrlModelNineTimeController()),
    /** url模版10(供url跳转) */
    URL_MODEL_TEN_ACTIVITY(Activity.ActivityType.URL_MODEL_TEN_VALUE, null, new UrlModelTenActivity(0, null), new UrlModelTenTimeController()),

    /** 建筑等级（基地崛起） */
    BUILD_LEVEL_ACTIVITY(Activity.ActivityType.BUILD_LEVEL_VALUE, ActivityBuildLevelEntity.class, new BuildLevelActivity(0, null), new BuildLevelActivityHandler(), new BuildLevelTimeController()),
    /** 累计登录 */
    LOGIN_DAY_ACTIVITY(Activity.ActivityType.LOGIN_DAY_VALUE, ActivityLoginDayEntity.class, new LoginDayActivity(0, null), new LoginDayTimeController()),
    /** 累计登录2(按日期配置开启) */
    LOGIN_DAY_TWO_ACTIVITY(Activity.ActivityType.LOGIN_DAY_TWO_VALUE, ActivityLoginDayTwoEntity.class, new LoginDayTwoActivity(0, null), new LoginDayTwoTimeController()),
    /** 登录签到（每日签到） */
    LOGIN_SIGN_ACTIVITY(Activity.ActivityType.LOGIN_SIGN_VALUE, ActivityLoginSignEntity.class, new LoginSignActivity(0, null), new LoginSignActivityHandler(), new LoginSignTimeController()),
    /** 八日庆典 */
    FESTIVAL_ACTIVITY(Activity.ActivityType.FESTIVAL_VALUE, FestivalEntity.class, new FestivalActivity(0, null), null, new FestivalTimeController()),
    /** 八日庆典2(按日期配置开启) */
    FESTIVAL_TWO_ACTIVITY(Activity.ActivityType.FESTIVAL_TWO_VALUE, FestivalTwoEntity.class, new FestivalTwoActivity(0, null), null, new FestivalTwoTimeController()),
    /** 成长基金 */
    GROW_FUND_ACTIVITY(Activity.ActivityType.GROW_FUND_VALUE, GrowFundEntity.class, new GrowFundActivity(0, null), new GrowFundActivityHandler(), new GrowFundTimeController()),
    /** 新成长基金 */
    GROW_FUND_NEW_ACTIVITY(Activity.ActivityType.GROW_FUND_NEW_VALUE, GrowFundNewEntity.class, new GrowFundNewActivity(0, null), new GrowFundNewActivityHandler(), new GrowFundNewTimeController()),

    /** 战力基金 */
    POWER_FUND_ACTIVITY(Activity.ActivityType.POWER_FUND_VALUE, PowerFundEntity.class, new PowerFundActivity(0, null), new PowerFundActivityHandler(), new PowerFundTimeController()),
    /** 使命战争(按开服时间开启) */
    YURI_ACHIEVE_ACTIVITY(Activity.ActivityType.MONSTER_2_VALUE, Monster2Entity.class, new YuriAchieveActivity(0, null), new YuriAchieveTimeController()),
    /** 辐射危机(按开服时间开启) */
    YURI_ACHIEVE_SHOW_ACTIVITY(Activity.ActivityType.MONSTER_2_SHOW_VALUE, null, new YuriAchieveShowActivity(0, null), new YuriAchieveShowTimeController()),
    /** 使命战争2(按日期配置开启) */
    YURI_ACHIEVE_TWO_ACTIVITY(Activity.ActivityType.YURI_ACHIEVE_TWO_VALUE, YuriAchieveTwoEntity.class, new YuriAchieveTwoActivity(0, null), new YuriAchieveTwoTimeController()),
    /** 辐射危机2(按日期配置开启) */
    YURI_ACHIEVE_SHOW_TWO_ACTIVITY(Activity.ActivityType.YURI_ACHIEVE_SHOW_TWO_VALUE, null, new YuriAchieveShowTwoActivity(0, null), new YuriAchieveShowTwoTimeController()),
    /** 最强指挥官 */
    STRONGEST_LEADER(Activity.ActivityType.STRONEST_LEADER_VALUE, ActivityStrongestLeaderEntity.class, new StrongestLeaderActivity(0, null), new StrongestLeaderActivityHandler(), new StrongestLeaderTimeController()),
    /*** 残卷兑换（战略储备） */
    BROKEN_EXCHANGE_ACTIVITY(Activity.ActivityType.BROKEN_EXCHANGE_VALUE, BrokenActivityEntity.class, new BrokenExchangeActivity(0, null), new BrokenExchangeHandler(), new BrokenExchangeTimeController()),
    /** 战略储备2(按开服时间开启) */
    BROKEN_EXCHANGE_TWO_ACTIVITY(Activity.ActivityType.BROKEN_EXCHANGE_TWO_VALUE, BrokenTwoActivityEntity.class, new BrokenExchangeTwoActivity(0, null), new BrokenExchangeTwoHandler(), new BrokenExchangeTwoTimeController()),
    /** 尤里复仇 */
    YURI_REVENGE_ACTIVITY(Activity.ActivityType.YURI_REVENGE_VALUE, null, new YuriRevengeActivity(0, null), new YuriRevengeTimeController()),
    /** 战力狂飙(按开服时间开启) */
    POWER_UP_ACTIVITY(Activity.ActivityType.POWER_UP_VALUE, PowerUpEntity.class, new PowerUpActivity(0, null), new PowerUpTimeController()),
    /** 登录基金 */
    LOGIN_FUND_ACTIVITY(Activity.ActivityType.LOGIN_FUND_VALUE, LoginFundEntity.class, new LoginFundActivity(0, null), new LoginFundActivityHandler(), new LoginFundTimeController()),
    /** 周卡月卡活动 */
    MONTHCARD_ACTIVITY(Activity.ActivityType.MONTHCARD_VALUE, ActivityMonthCardEntity.class, new MonthCardActivity(0, null), new MonthCardActivityHandler(), new MonthCardTimeController()),
    /** 首充奖励 */
    FIRST_RECHARGE_ACTIVITY(Activity.ActivityType.FIRST_RECHARGE_VALUE, FirstRechargeEntity.class, new FirstRechargeActivity(0, null), new FirstRechargeActivityHandler(), new FirstRechargeTimeController()),
    /** 全服buff */
    BUFF_ACTIVITY(Activity.ActivityType.BUFF_VALUE, null, new BuffActivity(0, null), new BuffTimeController()),
    /** 据点活动 */
    STRONG_POINT_ACTIVITY(Activity.ActivityType.STRONG_POINT_VALUE, ActivityStrongpointEntity.class, new StrongpointActivity(0, null), new StrongpointTimeController()),
    /** 地狱火（火线征召） */
    HELL_FIRE_ACTIVITY(Activity.ActivityType.HELL_FIRE_VALUE, ActivityHellFireEntity.class, new HellFireActivity(0, null), new HellFireHandler(), new HellFireTimeController()),
    /** 地狱火2（全军动员） */
    HELL_FIRE_TWO_ACTIVITY(Activity.ActivityType.HELL_FIRE_TWO_VALUE, ActivityHellFireTwoEntity.class, new HellFireTwoActivity(0, null), new HellFireTwoHandler(), new HellFireTwoTimeController()),
    /** 地狱火3 */
    HELL_FIRE_THREE_ACTIVITY(Activity.ActivityType.HELL_FIRE_THREE_VALUE, ActivityHellFireThreeEntity.class, new HellFireThreeActivity(0, null), new HellFireThreeHandler(), new HellFireThreeTimeController()),
    /** 精工锻造 */
    EQUIP_ACHIEVE_ACTIVITY(Activity.ActivityType.EQUIP_ACHIEVE_VALUE, EquipAchieveEntity.class, new EquipAchieveActivity(0, null), new EquipAchieveTimeController()),
    /** 英雄军团 */
    HERO_ACHIEVE_ACTIVITY(Activity.ActivityType.HERO_ACHIEVE_VALUE, HeroAchieveEntity.class, new HeroAchieveActivity(0, null), new HeroAchieveTimeController()),
    /** 跨服团购（基金团购） */
    GROUP_PURCHASE_ACTIVITY(Activity.ActivityType.GROUP_PURCHASE_VALUE, GroupPurchaseEntity.class, new GroupPurchaseActivity(0, null), new GroupPurchaseActivityHandler(), new GroupPurchaseTimeController()),
    /** 累积充值 */
    ACCUMULATE_RECHARGE_ACTIVITY(Activity.ActivityType.ACCUMULATE_RECHARGE_VALUE, AccumulateRechargeEntity.class, new AccumulateRechargeActivity(0, null), new AccumulateRechargeTimeController()),
    /** 累积消费 */
    ACCUMULATE_CONSUME_ACTIVITY(Activity.ActivityType.ACCUMULATE_CONSUME_VALUE, AccumulateConsumeEntity.class, new AccumulateConsumeActivity(0, null), new AccumulateConsumeTimeController()),
    /** 首领入侵 */
    BOSS_INVADE_ACTIVITY(Activity.ActivityType.BOSS_INVADE_VALUE, null, new BossInvadeActivity(0, null), new BossInvadeTimeController()),
    /** 充值翻倍 */
    DOUBLE_RECHARGE_ACTIVITY(Activity.ActivityType.DOUBLE_RECHARGE_VALUE, DoubleRechargeEntity.class, new DoubleRechargeActivity(0, null), new DoubleRechargeTimeController()),
    /** 每日特惠 (按开服时间开启)*/
    DAILY_PREFERENCE_ACTIVITY(Activity.ActivityType.DAILY_PREFERENCE_VALUE, null, new DailyPreferenceActivity(0, null), new DailyPreferenceTimeController()),
    /** 充值豪礼 **/
    RECHARGE_GIFT_ACTIVITY(Activity.ActivityType.RECHARGE_GIFT_VALUE, RechargeGiftEntity.class, new RechargeGiftActivity(0, null), new RechargeGiftTimeController()),
    /** 超级武器 **/
    SUPER_WEAPON_ACTIVITY(Activity.ActivityType.SUPER_WEAPON_VALUE, null, new SuperWeaponActivity(0, null), new PerpetualOpenTimeController()),
    /** 十连抽活动 */
    LOTTERY_DRAW_ACTIVITY(Activity.ActivityType.LOTTERY_DRAW_VALUE, LotteryDrawEntity.class, new LotteryDrawActivity(0, null), new LotteryDrawActivityHandler(), new LotteryDrawTimeController()),
    /** 超级金矿活动 **/
    SUPER_GOLD_ACTIVITY(Activity.ActivityType.SUPER_GOLD_VALUE, SuperGoldEntity.class, new SuperGoldActivity(0, null), new SuperGoldHandler(), new SuperGoldATimeController()),
    /** 战地福利 */
    WARZONE_WEAL_ACTIVITY(Activity.ActivityType.WARZONE_WEAL_VALUE, WarzoneWealEntity.class, new WarzoneWealActivity(0, null), new WarzoneWealTimeController()),
    /** 铁血军团 */
    BLOOD_CORPS_ACTIVITY(Activity.ActivityType.BLOOD_CORPS_VALUE, BloodCorpsEntity.class, new BloodCorpsActivity(0, null), new BloodCorpsActivityHandler(), new BloodCorpsTimeController()),
    /** 连续充值 */
    CONTINUOUS_RECHARGE_ACTIVITY(Activity.ActivityType.CONTINUOUS_RECHARGE_VALUE, ContinuousRechargeEntity.class, new ContinuousRechargeActivity(0, null), new ContinuousRechargeHandler(), new ContinuousRechargeTimeController()),
    /** 复制中心 */
    COPY_CENTER(Activity.ActivityType.COPY_CENTER_VALUE, null, new CopyCenterActivity(0, null), new CopyCenterActivityHandler(), new CopyCenterTimeController()),
    /**感恩活动*/
    GRATITUDE_GIFT(Activity.ActivityType.GRATITUDE_GIFT_VALUE, GratitudeGiftEntity.class, new GratitudeGiftActivity(0, null), new GratitudeGiftActivityHandler(), new GratitudeGiftTimeController()),
    /**金币宝库*/
    TREASURY(Activity.ActivityType.TREASURY_VALUE, null, new TreasuryActivity(0, null), new TreasuryHandler(), new TreasuryController()),
    /** 潘朵拉宝盒 **/
    PANDORA_BOX(Activity.ActivityType.PANDORA_BOX_VALUE, null, new PandoraBoxActivity(0 , null), new PandoraBoxHandler(), new PandoraBoxTimeController()),
    /** 盟军悬赏令 **/
    REWARD_ORDER(Activity.ActivityType.REWARD_ORDER_VALUE, RewardOrderEntity.class, new RewardOrderActivity(0, null), new RewardOrderHandler(), new RewardOrderTimeController()),
    /** 幸运星 **/
    LUCKY_STAR(Activity.ActivityType.LUCKY_START_VALUE, LuckyStarEntity.class, new LuckyStarActivity(0,null), new LuckyStarHandler(), new LuckyStartTimeController()),
    /** 特惠惊喜 */
    PREFERENTIAL_SURPRISE_ACTIVITY(Activity.ActivityType.PREFERENTIAL_SURPRISE_VALUE, PreferentialSupriseEntity.class, new PreferentialSupriseActivity(0, null), new PreferentialSupriseTimeController()),
    /** 礼包返利  */
    PRESENT_REBATE(Activity.ActivityType.PRESENT_REBATE_VALUE, PresentRebateEntity.class, new PresentRebateActivity(0, null), new PresentRebateTimeController()),

    /** 超值好礼 **/
    GREAT_GIFT(Activity.ActivityType.GREAT_GIFT_VALUE, GreatGiftEntity.class, new GreatGiftActivity(0,null), new GreatGiftHandler(), new GreatGiftTimeController()),
    /**限时掉落*/
    TIME_LIMIT_DROP(Activity.ActivityType.TIME_LIMIT_DROP_VALUE, TimeLimitDropEntity.class, new TimeLimitDropActivity(0, null), new TimeLimitDropHandler(), new TimeLimitDropTimeController()),
    /**盟军反击*/
    ALLY_BEAT_BACK(Activity.ActivityType.ALLY_BEAT_BACK_VALUE, AllyBeatBackEntity.class, new AllyBeatBackActivity(0, null), new AllyBeatBackHanlder(), new AllyBeatBackTimeController()),
    /**
     * 战略储备三
     */
    BROKEN_EXCHANGE_THREE(Activity.ActivityType.BROKEN_EXCHANGE_THREE_VALUE, BrokenActivityThreeEntity.class, new BrokenExchangeThreeActivity(0, null), new BrokenExchangeThreeHandler(), new BrokenExchangeThreeTimeController()),
    /** 穹顶兑换 **/
    DOME_EXCHANGE(Activity.ActivityType.DOME_EXCHANGE_VALUE, DomeExchangeEntity.class, new DomeExchangeActivity(0, null), new DomeExchangeHandler(), new DomeExchangeTimeController()),
    /** 英雄主题最强步兵() */
    HERO_THEME_ACTIVITY(Activity.ActivityType.HERO_THEME_VALUE, HeroThemeEntity.class, new HeroThemeActivity(0, null), new HeroThemeTimeController()),
    /** 机甲觉醒 */
    MACHINE_AWAKE_ACTIVITY(Activity.ActivityType.MACHINE_AWAKE_VALUE, MachineAwakeEntity.class, new MachineAwakeActivity(0, null), new MachineAwakeHandler(), new MachineAwakeTimeController()),
    /**送花*/
    SEND_FLOWER_HUA(Activity.ActivityType.SEND_FLOWER_HUA_VALUE, SendFlowerEntity.class, new SendFlowerActivity(0, null), new SendFlowerHandler(), new SendFlowerTimeController()),
    /**夺宝奇兵*/
    TREASURE_CAVALRY(Activity.ActivityType.TREASURE_CAVALRY_VALUE, TreasureCavalryEntity.class, new TreasureCavalryActivity(0, null), new TreasureCavalryHandler(), new TreasureCavalryTimeController()),
    /**推金币*/
    BOUNTY_HUNTER(Activity.ActivityType.BOUNTER_HUNTER_ACT_VALUE, BountyHunterEntity.class, new BountyHunterActivity(0, null), new BountyHunterHandler(), new BountyHunterTimeController()),
    /** 盟军补给站 **/
    SUPPLY_STATION_ACTIVITY(Activity.ActivityType.SUPPLY_STATION_VALUE, SupplyStationEntity.class, new SupplyStationActivity(0, null), new SupplyStationHandler(), new SupplyStationTimeController()),

    /** 一元购活动(按日期配置开启) */
    ONE_RMB_PURCHARSE_ACTIVITY(Activity.ActivityType.ONE_RMB_PURCHASE_VALUE, OneRMBPurchaseEntity.class, new OneRMBPurchaseActivity(0, null), null, new OneRMBPurchaseController()),
    /** 王者联盟 **/
    STRONGEST_GUILD_ACTIVITY(Activity.ActivityType.STRONGEST_GUILD_VALUE, StrongestGuildEntity.class, new StrongestGuildActivity(0, null), new StrongestGuildHandler(), new StrongestGuildTimeController()),

    /**英雄返场 最强步兵*/
    HERO_BACK(Activity.ActivityType.HERO_BACK_VALUE, HeroBackEntity.class, new HeroBackActivity(0, null), new HeroBackHandler(), new HeroBackTimeController()),

    /**英雄返场 英雄回归*/
    HERO_BACK_EXCHANGE(Activity.ActivityType.HERO_BACK_EXCHANGE_VALUE, HeroBackExchangeEntity.class, new HeroBackExchangeActivity(0, null), new HeroBackExchangeHandler(), new HeroBackExchangeTimeController()),

    /** 豪礼派送【战地福利拷贝】 **/
    GIFT_SEND(Activity.ActivityType.GIFT_SEND_VALUE, WarzoneWealCopyEntity.class, new WarzoneWealCopyActivity(0, null), null, new WarzoneWealCopyTimeController()),
    /** URL跳转 **/
    SHOP_SKIP(Activity.ActivityType.SHOP_SKIP_VALUE, null, new ShopSkipActivity(0, null), null, new ShopSkipTimeController()),
    /** 超能实验室 **/
    SUPER_POWER_LAB(Activity.ActivityType.SUPER_POWER_LAB_VALUE, PowerCollectEntity.class, new PowerCollectActivity(0,null), new PowerCollectHandler(), new PowerCollectTimeController()),
    /** 系统红包  **/
    RED_ENVELOPE_ACTIVITY(Activity.ActivityType.RED_ENVELOPE_VALUE, RedEnvelopeEntity.class, new RedEnvelopeActivity(0,null), new RedEnvelopeHandler(), new RedEnvelopeTimeController()),
    /** 个人红包 **/
    RED_ENVELOPE_PLAYER(Activity.ActivityType.RED_ENVELOPE_PLAYER_VALUE, null, new RedEnvelopePlayerActivity(0,null), null, new RedEnvelopePlayerTimeController()),

    /** 欢购豪礼 **/
    HAPPY_GIFT(Activity.ActivityType.SUPPLY_STATION_COPY_VALUE, SupplyStationCopyEntity.class, new SupplyStationCopyActivity(0,null), new SupplyStationCopyHandler(), new SupplyStationCopyTimeController()),

    DOME_EXCHANGE_TWO(Activity.ActivityType.DOME_EXCHANGE_TWO_VALUE, DomeExchangeTwoEntity.class, new DomeExchangeTwoActivity(0, null), new DomeExchangeTwoHandler(), new DomeExchangeTwoTimeController()),
    /**盟军补给，时空恋人*/
    SUPPLY_STATION_TWO_ACTIVITY(Activity.ActivityType.SUPPLY_STATION_TWO_VALUE, SupplyStationTwoEntity.class, new SupplyStationTwoActivity(0, null), new SupplyStationTwoHandler(), new SupplyStationTwoTimeController()),

    /** 机甲觉醒2(年兽) */
    MACHINE_AWAKE_TWO_ACTIVITY(Activity.ActivityType.MACHINE_AWAKE_TWO_VALUE, MachineAwakeTwoEntity.class, new MachineAwakeTwoActivity(0, null), new MachineAwakeTwoHandler(), new MachineAwakeTwoTimeController()),

    /** 新年寻宝 */
    NEWYEAR_TREASURE_ACTIVITY(Activity.ActivityType.NEWYEAR_TREASURE_VALUE, NewyearTreasureEntity.class, new NewyearTreasureActivity(0, null), new NewyearTreasureTimeController()),

    /** --------老玩家回归系列活动----------- **/
    PLAYER_COME_BACK_REWARD(Activity.ActivityType.COME_BACK_PLAYER_GREAT_GIFT_VALUE, PlayerComeBackEntity.class, new ComeBackRewardActivity(0, null), new ComeBackHandler(), new ComeBackPlayerRewardTimeController()),
    PLAYER_COME_BACK_ACHIEVE(Activity.ActivityType.COME_BACK_PLAYER_ACHIEVE_VALUE, PlayerComeBackEntity.class, new ComeBackAchieveTaskActivity(0, null), new ComeBackPlayerAchieveTimeController()),
    PLAYER_COME_BACK_EXCHANGE(Activity.ActivityType.COME_BACK_PLAYER_EXCHANGE_VALUE, PlayerComeBackEntity.class, new ComeBackExchangeActivity(0, null), new ComeBackPlayerExchangeTimeController()),
    PLAYER_COME_BACK_BUY(Activity.ActivityType.COME_BACK_PLAYER_DISCOUNT_BUY_VALUE, PlayerComeBackEntity.class, new ComeBackBuyActivity(0, null), new ComeBackPlayerBuyTimeController()),
    PLAYER_COME_BACK_VERSION(Activity.ActivityType.COME_BACK_PLAYER_VERSION_VALUE, null, new ComeBackVersionActivity(0, null), new ComeBackPlayerVersionTimeController()),
    /** --------老玩家回归系列活动----------- **/
    /***好友召回*/
    RECALL_FRIEND_ACTIVITY(Activity.ActivityType.RECALL_FRIEND_VALUE, RecallFriendEntity.class, new RecallFriendActivity(0, null), new RecallFriendHandler(), new RecallFriendTimeController()),
    /** 通用兑换 **/
    COMMON_EXCHANGE_ACTIVITY(Activity.ActivityType.COMMON_EXCHANGE_VALUE, CommonExchangeEntity.class, new CommonExchangeActivity(0, null), new CommonExchangeHandler(), new CommonExchangeTimeController()),
    /** 通用兑换2 **/
    COMMON_EXCHANGE_TWO_ACTIVITY(Activity.ActivityType.COMMON_EXCHANGE_TWO_VALUE, CommonExchangeTwoEntity.class, new CommonExchangeTwoActivity(0, null), new CommonExchangeTwoHandler(), new CommonExchangeTwoTimeController()),
    /** 军魂承接*/ // 老的军魂传承已经
    INHERITE(Activity.ActivityType.INHERIT_VALUE, InheritEntity.class, new InheritActivity(0, null), new InheritHandler(), new InheritTimeController()),
    /**在线答题  情报分享**/
    QUESTION_SHARE(Activity.ActivityType.QUESTION_SHARE_VALUE, QuestionShareEntity.class, new QuestionShareActivity(0, null), new QuestionShareHandler(), new QuestionShareTimeController()),

    // 插旗活动
    GUILD_BANNER_ACTIVITY(Activity.ActivityType.GUILD_BANNER_VALUE, null, new GuildBannerActivity(0, null), new GuildBannerActivityHandler(), new GuildBannerTimeController()),
    // 战神降临活动
    BANNER_KILL_ACTIVITY(Activity.ActivityType.BANNER_KILL_ENEMY_VALUE, ActivityBannerKillEntity.class, new BannerKillActivity(0, null), new BannerKillActivityHandler(), new BannerKillTimeController()),

    //机甲破世
    MACHINE_SELL_ACTIVITY(Activity.ActivityType.MACHINE_SELL_VALUE, MachineSellEntity.class, new MachineSellActivity(0,null), new MachineSellHandler(), new MachineSellTimeController()),
    // 定制礼包
    CUSTOM_GIFT_ACTIVITY(Activity.ActivityType.CUSTOM_MADE_GIFT_VALUE, CustomGiftEntity.class, new CustomGiftActivity(0,null), new CustomGiftActivityHandler(), new CustomGiftController()),
    // 0元礼包
    GIFT_ZERO_ACTIVITY(Activity.ActivityType.GIFT_ZERO_VALUE, GiftZeroEntity.class, new GiftZeroActivity(0,null), new GiftZeroActivityHandler(), new GiftZeroController()),
    GIFT_ZERO_NEW_ACTIVITY(Activity.ActivityType.GIFT_ZERO_NEW_VALUE, GiftZeroNewEntity.class, new GiftZeroNewActivity(0,null), new GiftZeroNewActivityHandler(), new GiftZeroNewController()),

    /** 战令活动 */
    ORDER_ACTIVITY(Activity.ActivityType.ORDER_ACTIVITY_VALUE, ActivityOrderEntity.class, new OrderActivity(0, null), new OrderActivityHandler(), new OrderTimeController()),
    /** 战令(新)活动 */
    ORDER_TWO_ACTIVITY(Activity.ActivityType.ORDER_TWO_VALUE, OrderTwoEntity.class, new OrderTwoActivity(0, null), new OrderTwoActivityHandler(), new OrderTwoTimeController()),
    /** 英雄进化之路活动 */
    EVOLUTION_ACTIVITY(Activity.ActivityType.HERO_EVOLUTION_VALUE, ActivityEvolutionEntity.class, new EvolutionActivity(0, null), new EvolutionActivityHandler(), new EvolutionTimeController()),

    /**月签**/
    DAILY_SIGN_ACTIVITY(Activity.ActivityType.DAILY_SIGN_VALUE, DailySignEntity.class, new DailySignActivity(0, null), new DailySignHandler(), new DailySignTimeController()),
    /** 源计划活动 */
    PLAN_ACTIVITY(Activity.ActivityType.PLAN_VALUE, PlanEntity.class, new PlanActivity(0, null), new PlanActivityHandler(), new PlanActivityTimeController()),

    /** 英雄试炼*/
    HERO_TRIAL(Activity.ActivityType.HERO_TRIAL_VALUE, HeroTrialActivityEntity.class, new HeroTrialActivity(0, null), new HeroTrialHandler(), new HeroTrialTimeController()),

    /**推广员活动*/
    SPREAD_ACTIVITY(Activity.ActivityType.SPREAD_VALUE, SpreadEntity.class, new SpreadActivity(0, null), new SpreadActivityHandler(), new SpreadActivityTimeController()),
    SOLDIER_EXCHANGE(Activity.ActivityType.SOLDIER_EXCHANGE_VALUE, SoldierExchangeActivityEntity.class, new SoldierExchangeActivity(0, null), new SoldierExchangeActivityHandler(), new SoldierExchangeTimeController()),
    /** 投资理财活动 */
    INVEST_ACTIVITY(Activity.ActivityType.INVEST_ACTIVITY_VALUE, InvestEntity.class, new InvestActivity(0, null), new InvestActivityHandler(), new InvestController()),
    /**幸运折扣*/
    LUCKY_DISCOUNT_ACTIVITY(Activity.ActivityType.LUCKY_DISCOUNT_VALUE, LuckyDiscountEntity.class, new LuckyDiscountActivity(0, null), new LuckyDiscountHandler(), new LuckyDiscountActivityTimeController()),
    /** 幸运福利 */
    LUCKY_WELFARE_ACTIVITY(Activity.ActivityType.LUCKY_WELFARE_VALUE, LuckyWelfareEntity.class, new LuckyWelfareActivity(0, null), new LuckyWelfareTimeController()),
    /** 黑科技 */
    BLACK_TECH_ACTIVITY(Activity.ActivityType.BLACK_TECH_VALUE, BlackTechEntity.class, new BlackTechActivity(0, null), new BlackTechHandler(),new BlackTechActivityTimeController()),
    /** 全副武装 */
    FULLY_ARMED_ACTIVITY(Activity.ActivityType.FULLY_ARMED_VALUE, FullyArmedEntity.class, new FullyArmedActivity(0, null), new FullyArmedHandler(),new FullyArmedActivityTimeController()),

    /** 先锋豪礼  */
    PIONEER_GIFT_ACTIVITY(Activity.ActivityType.PIONEER_GIFTS_VALUE, PioneerGiftEntity.class, new PioneerGiftActivity(0, null), new PioneerGiftActivityHandler(),new PioneerGiftController()),
    /** 时空轮盘 */
    ROULETTE_ACTIVITY(Activity.ActivityType.ROULETTE_VALUE, RouletteEntity.class, new RouletteActivity(0, null), new RouletteHandler(), new RouletteTimeController()),
    /** 武者拼图 **/
    FIGHTER_PUZZLE_ACTIVITY(Activity.ActivityType.FIGHTER_PUZZLE_VALUE, FighterPuzzleEntity.class, new FighterPuzzleActivity(0, null), new FighterPuzzleActivityHandler(), new FighterPuzzleTimeController()),
    /** 武者拼图 **/
    FIGHTER_PUZZLE_SERVEROPEN_ACTIVITY(Activity.ActivityType.FIGHTER_PUZZLE_SERVEROPEN_VALUE, FighterPuzzleServeropenEntity.class, new FighterPuzzleServeropenActivity(0, null), new FighterPuzzleServeropenActivityHandler(), new FighterPuzzleServeropenTimeController()),
    /** 回流拼图 **/
    RETURN_PUZZLE_ACTIVITY(Activity.ActivityType.RETURN_PUZZLE_VALUE, ReturnPuzzleEntity.class, new ReturnPuzzleActivity(0, null), new RetrunPuzzleActivityHandler(), new ReturnPuzzleTimeController()),
    /** 金字塔 **/
    SKIN_PLAN_ACTIVITY(Activity.ActivityType.SKIN_PLAN_VALUE, SkinPlanEntity.class, new SkinPlanActivity(0, null), new SkinPlanHandler(), new SkinPlanTimeController()),

    /** 累积充值2 */
    ACCUMULATE_RECHARGE_TWO_ACTIVITY(Activity.ActivityType.ACCUMULATE_RECHARGE_TWO_VALUE, AccumulateRechargeTwoEntity.class, new AccumulateRechargeTwoActivity(0, null), new AccumulateRechargeTwoTimeController()),

    /** 今日累充活动  */
    DAILY_RECHARGE_ACC_ACTIVITY(Activity.ActivityType.DAILY_RECHARGE_ACC_VALUE, DailyRechargeEntity.class, new DailyRechargeActivity(0, null), new DailyRechargeActivityHandler(), new DailyRechargeTimeController()),
    /** 今日累充新版 */
    DAILY_RECHARGE_NEW_ACTIVITY(Activity.ActivityType.DAILY_RECHARGE_NEW_VALUE, DailyRechargeNewEntity.class, new DailyRechargeNewActivity(0, null), new DailyRechargeNewActivityHandler(), new DailyRechargeNewTimeController()),

    /**军事备战 */
    MILITARY_PREPARE_ACTIVITY(Activity.ActivityType.MILITARY_PREPARE_VALUE, MilitaryPrepareEntity.class, new MilitaryPrepareActivity(0, null), new MilitaryPrepareHandler(), new MilitaryPrepareTimeController()),

    /**联盟总动员 */
    ALLIANCE_CARNIVAL(Activity.ActivityType.ALLIANCE_CARNIVAL_VALUE, AllianceCarnivalEntity.class, new AllianceCarnivalActivity(0, null), new AllianceCarnivalHandler(), new AllianceCarnivalTimeController()),

    /**中秋庆典 */
    MID_AUTUMN_ACTIVITY(Activity.ActivityType.MID_AUTUMN_VALUE, MidAutumnEntity.class, new MidAutumnActivity(0, null), new MidAutumnHandler(), new MidAutumnTimeController()),

    /** 突破枷锁*/
    BREAK_SHACKLES_ACTIVITY(Activity.ActivityType.BREAK_SHACKLES_ACTIVITY_VALUE,null,new BreakShacklesActivity(0,null),null,new BreakShacklesTimeController()),
    /** 特惠商人助力庆典*/
    TRAVEL_SHOP_ASSIST_ACTIVITY(Activity.ActivityType.TRAVEL_SHOP_ASSIST_VALUE,TravelShopAssistEntity.class,new TravelShopAssistActivity(0,null),null,new TravelShopAssistTimeController() ),

    /**限时登录*/
    TIME_LIMIT_LOGIN_ACTIVITY(Activity.ActivityType.TIME_LIMIT_LOGIN_VALUE, TimeLimitLoginEntity.class, new TimeLimitLoginActivity(0, null), new TimeLimitLoginHandler(), new TimeLimitLoginTimeController()),
    /**勋章宝藏*/
    MEDAL_ACTION_ACTIVITY(Activity.ActivityType.MEDAL_ACTION_VALUE, MedalActionEntity.class, new MedalActionActivity(0, null), new MedalActionHandler(), new MedalActionTimeController()),

    /** 红警锦鲤*/
    REDKOI_ACTIVITY(Activity.ActivityType.REDKOI_ACTIVITY_VALUE,RedkoiEntity.class, new RedkoiActivity(0,null),new RedkoiHandler(),new RedkoiTimeController()),

    /**金币瓜分 */
    DIVIDE_GOLD_ACTIVITY(Activity.ActivityType.DIVIDE_GOLD_VALUE, DivideGoldEntity.class, new DivideGoldActivity(0, null), new DivideGoldHandler(), new DivideGoldTimeController()),

    /**装备科技 */
    EQUIP_TECH_ACTIVITY(Activity.ActivityType.EQUIP_TECH_VALUE, EquipTechEntity.class, new EquipTechActivity(0, null), null, new EquipTechActivityTimeController()),

    /** 场景分享*/
    SCENE_SHARE_ACTIVITY(Activity.ActivityType.SCENE_SHARE_VALUE, SceneShareEntity.class, new SceneShareActivity(0, null), null, new SceneShareTimeController()),


    /** 威龙庆典-飞行计划 */
    FLIGHT_PLAN_ACTIVITY(Activity.ActivityType.FLIGHT_PLAN_VALUE, FlightPlanEntity.class, new FlightPlanActivity(0, null), new FlightPlanActivityHandler(), new FlightPlanTimeController()),


    /**英雄皮肤*/
    HERO_SKIN(Activity.ActivityType.HERO_SKIN_VALUE, HeroSkinEntity.class, new HeroSkinActivity(0, null), new HeroSkinHandler(), new HeroSkinTimeController()),

    /**泰伯利亚竞猜*/
    TBLY_GUESS(Activity.ActivityType.TBLY_GUESS_VALUE, TiberiumGuessEntity.class, new TiberiumGuessActivity(0, null), new TiberiumGuessHandler(), new TiberiumGuessTimeController())
    ,
    /** 黑武士 **/
    SAMURAI_BLACKENED_ACTIVITY(Activity.ActivityType.SAMURAI_BLACKENED_VALUE, SamuraiBlackenedEntity.class, new SamuraiBlackenedActivity(0, null), new SamuraiBlackenedActivityHandler(), new SamuraiBlackenedTimeController()),

    /** 基地飞升 **/
    BASE_BUILD_ACTIVITY(Activity.ActivityType.BASE_BUILD_VALUE, null, new BaseBuildActivity(0, null), new BaseBuildTimeController()),

    /** 指挥官学院*/
    COMMAND_ACADEMY_ACTIVITY(Activity.ActivityType.COMMAND_COLLEGE_VALUE, CommandAcademyEntity.class, new CommandAcademyActivity(0, null), new CommandAcademyHandler(), new CommandAcademyTimeController()),
    COMMAND_ACADEMY_SIMPLIFY_ACTIVITY(Activity.ActivityType.COMMAND_COLLEGE_SIMPLIFY_VALUE, CommandAcademySimplifyEntity.class, new CommandAcademySimplifyActivity(0, null), new CommandAcademySimplifyHandler(), new CommandAcademySimplifyTimeController()),

    /** 装备黑市*/
    EQUIP_BLACK_MARKET_ACTIVITY(Activity.ActivityType.EQUIP_BLACK_MARKET_VALUE, EquipBlackMarketEntity.class, new EquipBlackMarketActivity(0, null), new EquipBlackMarketActivityHandler(), new EquipBlackMarketTimeController()),

    /** 雪球大战*/
    SNOWBALL(Activity.ActivityType.SNOWBALL_VALUE, SnowballEntity.class, new SnowballActivity(0, null), new SnowballHandler(), new SnowballTimeController()),

    /**圣诞BOSS*/
    CHRISTMAS_WAR_ACTIVITY(Activity.ActivityType.CHRISTMAS_WAR_VALUE, ActivityChristmasWarEntity.class, new ChristmasWarActivity(0, null), new ChristmasWarHandler(), new ChristmasWarTimeController()),

    /**命运左轮*/
    DESTINY_REVOLVER(Activity.ActivityType.DESTINY_REVOLVER_VALUE, DestinyRevolverEntity.class, new DestinyRevolverActivity(0, null), new DestinyRevolverHandler(), new DestinyRevolverTimeController()),


    /**时空豪礼*/
    CHRONO_GIFT(Activity.ActivityType.CHRONO_GIFT_VALUE, ChronoGiftEntity.class, new ChronoGiftActivity(0, null), new ChronoGiftActivityHandler(), new ChronoGiftTimeController()),



    /**资源保卫战*/
    RESOURCE_DEFENSE(Activity.ActivityType.RESOURCE_DEFENSE_VALUE, ResourceDefenseEntity.class, new ResourceDefenseActivity(0, null), new ResourceDefenseHandler(), new ResourceDefenseTimeController()),

    /**委任英雄*/
    HERO_LOVE(Activity.ActivityType.HERO_LOVE_VALUE, HeroLoveEntity.class, new HeroLoveActivity(0, null), new HeroLoveActivityHandler(), new HeroLoveTimeController()),
    /**充值基金*/
    RECHARGE_FUND(Activity.ActivityType.RECHARGE_FUND_VALUE, RechargeFundEntity.class, new RechargeFundActivity(0, null), new RechargeFundHandler(), new RechargeFundTimeController()),

    /** 回流活动-回归大礼*/
    BACK_GIFT(Activity.ActivityType.BACK_GIFT_VALUE, BackGiftEntity.class, new BackGiftActivity(0, null), new BackGiftActivityHandler(), new BackGiftTimeController()),

    /** 回流活动-专属军资(老107) */
    RETURN_ARMY_EXCHANGE(Activity.ActivityType.RETURN_ARMY_EXCHANGE_VALUE, ReturnArmyExchangeEntity.class, new ReturnArmyExchangeActivity(0, null), new RetrunArmyExchangeHandler(), new ReturnArmyExchangeTimeController()),
    /** 回流活动-低折回馈(老108) */
    RETURN_GIFT(Activity.ActivityType.RETURN_GIFT_VALUE, ReturnGiftEntity.class, new ReturnGiftActivity(0, null), new ReturnGiftHandler(), new ReturnGiftTimeController()),

    /** 回流活动-版本尝鲜*/
    NEWLY_EXPERIENCE(Activity.ActivityType.NEWLY_EXPERIENCE_VALUE, NewlyExperienceEntity.class, new NewlyExperienceActivity(0, null), null, new NewlyExperienceTimeController()),

    /** 回流活动-体力赠送*/
    POWER_SEND(Activity.ActivityType.POWER_SEND_VALUE, PowerSendEntity.class, new PowerSendActivity(0, null), new PowerSendActivityHandler(), new PowerSendTimeController()),

    /** 回流活动-发展冲刺*/
    DEVELOP_SPURT(Activity.ActivityType.DEVELOP_SPURT_VALUE, DevelopSpurtEntity.class, new DevelopSpurtActivity(0, null), new DevelopSpurtHandler(), new DevelopSpurtTimeController()),

    /** 回流活动-再续前缘*/
    CHEMISTRY(Activity.ActivityType.CHEMISTRY_VALUE, ChemistryEntity.class, new ChemistryActivity(0, null), null, new ChemistryTimeController()),

    /** 回流活动-回归特权*/
    BACK_PRIVILEGE(Activity.ActivityType.BACK_PRIVILEGE_VALUE, PrivilegeEntity.class, new PrivilegeActivity(0, null), new PrivilegeActivityHandler(), new PrivilegeTimeController()),

    /** 军魂承接-新(老111,老的军魂传承已经)*/
    INHERITE_NEW(Activity.ActivityType.INHERIT_NEW_VALUE, InheritNewEntity.class, new InheritNewActivity(0, null), new InheritNewHandler(), new InheritNewTimeController()),

    /** 欢乐购 */
    JOY_BUY_ACTIVITY(Activity.ActivityType.JOY_BUY_VALUE, ActivityJoyBuyEntity.class, new JoyBuyActivity(0, null),new JoyBuyActivityHandler(), new JoyBuyTimeController()),

    /** 兑换装扮 */
    EXCHANGE_DECORATE_ACTIVITY(Activity.ActivityType.EXCHANGE_DECORATE_VALUE, ActivityExchangeDecorateEntity.class, new ExchangeDecorateActivity(0, null),new ExchangeDecorateActivityHandler(), new ExchangeDecorateTimeController()),

    /** 新版辐射战争(按开服时间) */
    RADIATION_WAR_ACTIVITY(Activity.ActivityType.RADIATION_WAR_VALUE, RadiationWarEntity.class, new RadiationWarActivity(0, null), new RadiationWarTimeController()),
    /** 新版辐射战争2(按日期配置开启) */
    RADIATION_WAR_TWO_ACTIVITY(Activity.ActivityType.RADIATION_WAR_TWO_VALUE, RadiationWarTwoEntity.class, new RadiationWarTwoActivity(0, null), new RadiationWarTwoTimeController()),
    /** 能源滚滚活动 */
    ENERGIES_ACTIVITY(Activity.ActivityType.ENERGIES_VALUE, EnergiesEntity.class, new EnergiesActivity(0, null), new EnergiesHandler(), new EnergiesTimeController()),
    /** 幽灵秘宝 */
    GHOST_SECRET_ACTIVITY(Activity.ActivityType.GHOST_SECRET_VALUE, GhostSecretEntity.class, new GhostSecretActivity(0, null),new GhostSecretHandler(), new GhostSecretTimeController()),

    /** 武装技术（翻牌） */
    VIRTUAL_LABORATORY_ACTIVITY(Activity.ActivityType.VIRTUAL_LABORATORY_VALUE, VirtualLaboratoryEntity.class, new VirtualLaboratoryActivity(0, null),new VirtualLaboratoryHandler(), new VirtualLaboratoryTimeController()),

    /** 端午节活动,联盟庆典*/
    DRAGON_BOAT_CELERATION_ACTIVITY(Activity.ActivityType.DRAGON_BOAT_CELEBRATION_VALUE, DragonBoatCelebrationEntity.class, new DragonBoatCelebrationActivity(0, null), new DragonBoatCelebrationHandler(),new DragonBoatCelebrationTimeController()),
    DRAGON_BOAT_EXCHANGE_ACTIVITY(Activity.ActivityType.DRAGON_BOAT_EXCHANGE_VALUE, DragonBoatExchangeEntity.class, new DragonBoatExchangeActivity(0, null), new DragonBoatExchangeHandler(),new DragonBoatExchangeController()),
    DRAGON_BOAT_BENEFIT_ACTIVITY(Activity.ActivityType.DRAGON_BOAT_BENEFIT_VALUE, DragonBoatBenefitEntity.class, new DragonBoatBenefitActivity(0, null), null,new DragonBoatBenefitTimeController()),
    DRAGON_BOAT_LUCKY_BAG_ACTIVITY(Activity.ActivityType.DRAGON_BOAT_LUCKY_BAG_VALUE, DragonBoatLuckyBagEntity.class, new DragonBoatLuckyBagActivity(0, null), new DragonBoatLuckyBagHandler(),new DragonBoatLuckyBagController()),
    DRAGON_BOAT_RECHARGE_ACTIVITY(Activity.ActivityType.DRAGON_BOAT_RECHARGE_VALUE, DragonBoatRechargeEntity.class, new DragonBoatRechargeActivity(0, null), null,new DragonBoatRechargeTimeController()),
    DRAGON_BOAT_GIFT_ACTIVITY(Activity.ActivityType.DRAGON_BOAT_GIFT_VALUE, DragonBoatGiftEntity.class, new DragonBoatGiftActivity(0, null), new DragonBoatGiftHandler(),new DragonBoatGiftTimeController()),

    /** 勋章基金 */
    MEDAL_FUND_ACTIVITY(Activity.ActivityType.MEDAL_FUND_VALUE, MedalFundEntity.class, new MedalFundActivity(0, null),new MedalFundHandler(), new MedalFundTimeController()),
    MEDAL_FUND_TWO_ACTIVITY(Activity.ActivityType.MEDAL_FUND_TWO_VALUE, MedalFundTwoEntity.class, new MedalFundTwoActivity(0, null), new MedalFundTwoHandler(), new MedalFundTwoTimeController()),

    /**充值福利 */
    RECHARGE_WELFARE_ACTIVITY(Activity.ActivityType.RECHARGE_WELFARE_VALUE, RechargeWelfareEntity.class, new RechargeWelfareActivity(0, null),new RechargeWelfareHandler(), new RechargeWelfareTimeController()),
    /** 沙场点兵*/
    ARMIES_MASS_ACTIVITY(Activity.ActivityType.ARMIES_MASS_VALUE, ArmiesMassEntity.class, new ArmiesMassActivity(0,null), new ArmiesMassActivityHandler(),new ArmiesMassTimeController()),

    /**霸主赐福 */
    OVERLORD_BLESS_ACTIVITY(Activity.ActivityType.OVERLORD_BLESS_VALUE, OverlordBlessingEntity.class, new OverlordBlessingActivity(0, null),new OverlordBlessingHandler(), new OverlordBlessingTimeController()),

    /** 能量源投资*/
    ENERGY_INVEST_ACTIVITY(Activity.ActivityType.ENERGY_INVEST_VALUE, EnergyInvestEntity.class, new EnergyInvestActivity(0, null),new EnergyInvestHandler(), new EnergyInvestTimeController()),
    /** 能量源投资*/
    SUPERSOLDIER_INVEST_ACTIVITY(Activity.ActivityType.SUPERSOLDIER_INVEST_VALUE, SupersoldierInvestEntity.class, new SupersoldierInvestActivity(0, null),new SupersoldierInvestHandler(), new SupersoldierInvestTimeController()),

    /**空投补给 */
    AIRDROP_SUPPLY_ACTIVITY(Activity.ActivityType.AIRDROP_SUPPLY_VALUE, AirdropSupplyEntity.class, new AirdropSupplyActivity(0, null), null, new AirdropSupplyTimeController()),

    /** 超级金矿2活动 **/
    SUPER_GOLD_TWO_ACTIVITY(Activity.ActivityType.SUPER_GOLD_TWO_VALUE, SuperGoldTwoEntity.class, new SuperGoldTwoActivity(0, null), new SuperGoldTwoHandler(), new SuperGoldTwoTimeController()),

    /** 新服战令*/
    NEW_ORDER_ACTIVITY(Activity.ActivityType.NEW_ORDER_VALUE, NewActivityOrderEntity.class, new NewOrderActivity(0, null),new NewOrderActivityHandler(), new NewOrderTimeController()),

    /** 军械要塞*/
    ORDNANCE_FORTRESS_ACTIVITY(Activity.ActivityType.ORDNANCE_FORTRESS_VALUE, OrdnanceFortressEntity.class, new OrdnanceFortressActivity(0, null),new OrdnanceFortressHandler(), new OrdnanceFortressController()),

    /** 鹊桥会 */
    WAR_FLAG_TWO_ACTIVITY(Activity.ActivityType.WAR_FLAG_TWO_VALUE, WarFlagTwoEntity.class, new WarFlagTwoActivity(0, null), new WarFlagTwoHandler(), new WarFlagTwoTimeController()),

    /**七夕充值 **/
    RECHARGE_QIXI_ACTIVITY(Activity.ActivityType.RECHARGE_QIXI_VALUE, RechargeQixiEntity.class, new RechargeQixiActivity(0, null), null, new RechargeQixiTimeController()),

    /**团购活动**/
    GROUP_BUY_ACTIVITY(Activity.ActivityType.GROUP_BUY_VALUE, GroupBuyEntity.class, new GroupBuyActivity(0, null), new GroupBuyActivityHandler(), new GroupBuyTimeController()),

    /**双享豪礼活动**/
    DOUBLE_GIFT_ACTIVITY(Activity.ActivityType.DOUBLE_GIFT_VALUE, DoubleGiftEntity.class, new DoubleGiftActivity(0, null), new DoubleGiftActivityHandler(), new DoubleGiftTimeController()),

    /**装备工匠(装备词条投放活动), 活动id:242**/
    EQUIP_CARFTSMAN_ACTIVITY(Activity.ActivityType.EQUIP_CRAFTSMAN_VALUE, EquipCarftsmanEntity.class, new EquipCarftsmanActivity(0, null), new EquipCarftsmanHandler(), new EquipCarftsmanTimeController()),

    /**荣耀返利活动  244*/
    HONOR_REPAY_ACTIVITY(Activity.ActivityType.HONOR_REPAY_VALUE, HonorRepayEntity.class, new HonorRepayActivity(0, null), new HonorRepayHandler(), new HonorRepayTimeController()),

    /** 周年抢红包**/
    RED_PACKAGE_ACTIVITY(Activity.ActivityType.RED_PACKAGE_VALUE, RedPackageEntity.class, new RedPackageActivity(0, null), new RedPackageHandler(), new RedPackageTimeController()),

    // 战场寻宝
    BATTLE_FIELD_ACTIVITY(Activity.ActivityType.BATTLE_FIELD_TREASURE_VALUE, BattleFieldEntity.class, new BattleFieldActivity(0, null), new BattleFieldActivityHandler(), new BattleFieldTimeController()),

    /** 装备战令*/
    ORDER_EQUIP_ACTIVITY(Activity.ActivityType.ORDER_EQUIP_VALUE, OrderEquipEntity.class, new OrderEquipActivity(0, null), new OrderEquipActivityHandler(), new OrderEquipTimeController()),

    /**周年庆祝福语 **/
    GREESTINGS_ACTIVITY(Activity.ActivityType.GREETINGS_VALUE, GreetingsEntity.class, new GreetingsActivity(0, null), null, new GreetingsTimeController()),

    /**军备兑换**/
    ARMAMENT_EXCHANGE_ACTIVITY(Activity.ActivityType.ARMAMENT_EXCHANGE_VALUE, ArmamentExchangeEntity.class, new ArmamentExchangeActivity(0, null), new ArmamentExchangeHandler(), new ArmamentExchangeTimeController()),

    /**周年庆蛋糕同享活动**/
    CAKE_SHARE_ACTIVITY(Activity.ActivityType.CAKE_SHARE_VALUE, CakeShareEntity.class, new CakeShareActivity(0, null), new CakeShareHandler(), new CakeShareTimeController()),

    /**周年庆 烟花盛典活动**/
    FIRE_WORKS_ACTIVITY(Activity.ActivityType.FIRE_WORKS_VALUE, FireWorksEntity.class, new FireWorksActivity(0, null), new FireWorksHandler(), new FireWorksActivityTimeController()),

    /**周年庆 庆典美食*/
    CELEBRATION_FOOD_ACTIVITY(Activity.ActivityType.CELEBRATION_FOOD_VALUE, CelebrationFoodEntity.class, new CelebrationFoodActivity(0, null), new CelebrationFoodHandler(), new CelebrationFoodTimeController()),

    /**周年历程**/
    CELEBRATION_COURSE_ACTIVITY(Activity.ActivityType.CELEBRATION_COURSE_VALUE, CelebrationCourseEntity.class, new CelebrationCourseActivity(0, null), new CelebrationCourseActivityHandler(), new CelebrationCourseTimeController()),

    /**周年商城**/
    CELEBRATION_SHOP_ACTIVITY(Activity.ActivityType.CELEBRATION_SHOP_VALUE, CelebrationShopEntity.class, new CelebrationShopActivity(0, null), new CelebrationShopActivityHandler(), new CelebrationShopTimeController()),


    /** 全服签到**/
    GLOBAL_SIGN_ACTIVITY(Activity.ActivityType.GLOBAL_SIGN_VALUE, GlobalSignEntity.class, new GlobalSignActivity(0, null), new GlobalSignHandler(), new GlobalSignTimeController()),
    /** 小站区*/
    XZQ_ACTIVITY(Activity.ActivityType.XZQ_ACTIVITY_VALUE, null, new XZQActivity(0, null), null, new PerpetualOpenTimeController()),


    /**双十一拼图 */
    JIGSAW_CONNECT_ACTIVITY(Activity.ActivityType.JIGSAW_CONNECT_VALUE, JigsawConnectEntity.class, new JigsawConnectActivity(0, null), null, new JigsawConnectTimeController()),
    JXJIGSAW_CONNECT_ACTIVITY(Activity.ActivityType.JIGSAW_CONNECT_NEW_VALUE, JXJigsawConnectEntity.class, new JXJigsawConnectActivity(0, null), null, new JXJigsawConnectTimeController()),


    /**双十一联盟欢庆活动*/
    ALLIANCE_CELEBRATE_ACTIVITY(Activity.ActivityType.ALLIANCE_CELEBRATE_VALUE, AllianceCelebrateEntity.class, new AllianceCelebrateActivity(0, null), new AllianceCelebrateHandler(), new AllianceCelebrateTimeController()),

    /** 超级折扣**/
    SUPER_DISCOUNT_ACTIVITY(Activity.ActivityType.SUPER_DISCOUNT_VALUE, SuperDiscountEntity.class, new SuperDiscountActivity(0, null), new SuperDiscountHandler(), new SuperDiscountActivityController()),

    /** 泰能宝库*/
    PLANT_FORTRESS_ACTIVITY(Activity.ActivityType.PLANT_FORTRESS_VALUE, PlantFortressEntity.class, new PlantFortressActivity(0, null),new PlantFortressHandler(), new PlantFortressController()),


    /**装扮投放系列活动一:搜寻图纸*/
    DRAWING_SEARCH_ACTIVITY(Activity.ActivityType.DRAWING_SEARCH_VALUE, DrawingSearchActivityEntity.class, new DrawingSearchActivity(0, null), null, new DrawingSearchTimeController()),

    /**装扮投放系列活动二:能量聚集 */
    ENERGY_GATHER_ACTIVITY(Activity.ActivityType.ENERGE_GATHER_VALUE, EnergyGatherEntity.class, new EnergyGatherActivity(0, null), null, new EnergyGatherTimeController()),

    /**装扮投放系列活动三:重燃战火*/
    FIRE_REIGNITE_ACTIVITY(Activity.ActivityType.FIRE_REIGNITE_VALUE, FireReigniteEntity.class, new FireReigniteActivity(0, null), new FireReigniteHandler(), new FireReigniteTimeController()),

    /**装扮投放系列活动四:硝烟再起 */
    GUNPOWDER_RISE_ACTIVITY(Activity.ActivityType.GUNPOWDER_RISE_VALUE, GunpowderRiseEntity.class, new GunpowderRiseActivity(0, null), new GunpowderRiseHandler(), new GunpowderRiseTimeController()),

    /** 限时抢购*/
    TIME_LIMIT_BUY(
            Activity.ActivityType.TIME_LIMIT_BUY_VALUE,
            TimeLimitBuyEntity.class,
            new TimeLimitBuyActivity(0, null),
            new TimeLimitBuyHandler(),
            new TimeLimitBuyTimeController()
    ),

    /** 巅峰荣耀*/
    PEAK_HONOUR(
            Activity.ActivityType.PEAK_HONOUR_VALUE,
            null,
            new PeakHonourActivity(0, null),
            new PeakHonourHandler(),
            new PeakHonourTimeController()
    ),

    /**圣诞节系列活动一:冰雪计划活动*/
    ENERGY_GATHER_TWO_ACTIVITY(Activity.ActivityType.ENERGE_GATHER_TWO_VALUE, EnergyGatherTwoEntity.class, new EnergyGatherTwoActivity(0, null), null, new EnergyGatherTwoTimeController()),

    /**圣诞节系列活动二:冬日装扮活动*/
    FIRE_REIGNITE_TWO_ACTIVITY(Activity.ActivityType.FIRE_REIGNITE_TWO_VALUE, FireReigniteTwoEntity.class, new FireReigniteTwoActivity(0, null), new FireReigniteTwoHandler(), new FireReigniteTwoTimeController()),

    /**圣诞节系列活动三:冰雪商城活动*/
    GUNPOWDER_RISE_TWO_ACTIVITY(Activity.ActivityType.GUNPOWDER_RISE_TWO_VALUE, GunpowderRiseTwoEntity.class, new GunpowderRiseTwoActivity(0, null), new GunpowderRiseTwoHandler(), new GunpowderRiseTwoTimeController()),
    /** 圣诞节系列活动 累积充值 */
    CHRISTMAS_RECHARGE_ACTIVITY(Activity.ActivityType.CHRISTMAS_RECHARGE_VALUE, ChristmasRechargeEntity.class, new ChristmasRechargeActivity(0, null), new ChristmasRechargeTimeController()),
    /** 玩家回流H5活动 */
    PLAYER_TEAM_BACK_ACTIVITY(Activity.ActivityType.PLAYER_TEAM_BACK_VALUE, PlayerTeamBackEntity.class, new PlayerTeamBackActivity(0, null), new PlayerTeamBackActivityController()),


    /** 雄心壮志*/
    COREPLATE_ACTIVITY(Activity.ActivityType.COREPLATE_ACTIVITY_VALUE, CoreplateActivityEntity.class, new CoreplateActivity(0, null),new CoreplateActivityHandler(), new CoreplateActivityTimeController()),

    /** 登录基金2 */
    LOGIN_FUND_TWO_ACTIVITY(Activity.ActivityType.LOGIN_FUND_TWO_VALUE, LoginFundTwoEntity.class, new LoginFundTwoActivity(0, null), null, new LoginFundTwoTimeController()),

    /** 洪福礼包 */
    HONG_FU_GIFT_ACTIVITY(Activity.ActivityType.HONG_FU_GIFT_VALUE, HongFuGiftEntity.class, new HongFuGiftActivity(0, null), new HongFuGiftActivityHandler(), new HongFuGiftTimeController()),

    /** 翻牌活动  */
    REDBLUE_TICKET_ACTIVITY(Activity.ActivityType.RED_BLUE_TICKET_VALUE, RedBlueTicketActivityEntity.class, new RedBlueTicketActivity(0, null), new RedBlueTicketActivityHandler(), new RedBlueTicketActivityController()),

    /** 精装夺宝*/
    DRESS_TREASURE_ACTIVITY(Activity.ActivityType.DRESS_TREASURE_VALUE, DressTreasureEntity.class, new DressTreasureActivity(0, null), new DressTreasureHandler(), new DressTreasureController()),

    /** 预流失（干预）活动  */
    PRESTRESSING_LOSS_ACTIVITY(Activity.ActivityType.PRESTRESSING_LOSS_VALUE, PrestressingLossEntity.class, new PrestressingLossActivity(0, null), new PrestressingLossActivityHandler(), new PrestressingLossTimeController()),

    /** 幸运转盘*/
    LUCKY_BOX_ACTIVITY(Activity.ActivityType.LUCK_BOX_VALUE, LuckyBoxEntity.class, new LuckyBoxActivity(0, null), new LuckyBoxHandler(), new LuckBoxController()),


    /** 选美活动 */
    BEAUTY_CONTEST_ACTIVITY(Activity.ActivityType.BEAUTY_CONTEST_VALUE, BeautyContestEntity.class, new BeautyContestActivity(0, null), new BeautyContestHandler(), new BeautyContestController()),
    BEAUTY_CONTEST_FINAL_ACTIVITY(Activity.ActivityType.BEAUTY_CONTEST_FINAL_VALUE, BeautyContestFinalEntity.class, new BeautyContestFinalActivity(0, null), new BeautyContestFinalHandler(), new BeautyContestFinalController()),

    /** 迁服*/
    IMMGRATION(
            Activity.ActivityType.IMMGRATION_VALUE,
            null,
            new ImmgrationActivity(0, null),
            new ImmgrationActivityHandler(),
            new ImmgrationActivityTimeController()
    ),

    /** 泰能机密 */
    PLANT_SECRET_ACTIVITY(Activity.ActivityType.PLANT_SECRET_VALUE, PlantSecretEntity.class, new PlantSecretActivity(0, null), new PlantSecretHandler(), new PlantSecretController()),
    /** 欢乐限购 */
    RED_RECHARGE_ACTIVITY(Activity.ActivityType.RED_RECHARGE_VALUE, HappyRedRechargeEntity.class, new HappyRedRechargeActivity(0, null), new HappyRedRechargeHandler(), new HappyRedRechargeTimeController()),

    /** 盟军祝福*/
    ALLIANCE_WISH_ACTIVITY(Activity.ActivityType.ALLIANCE_WISH_VALUE, AllianceWishEntity.class, new AllianceWishActivity(0, null), new AllianceWishHandler(), new AllianceWishController()),

    /** 迁服*/
    SEA_TREASURE(
            Activity.ActivityType.SEA_TREASURE_VALUE,
            SeaTreasureEntity.class,
            new SeaTreasureActivity(0, null),
            new SeaTreasureHandler(),
            new SeaTreasureTimeController()
    ),
    /**土拨鼠*/
    HIDDEN_TREASURE_ACTIVITY(Activity.ActivityType.HIDDEN_TREASURE_VALUE, HiddenTreasureEntity.class, new HiddenTreasureActivity(0, null), new HiddenTreasureHandler(), new HiddenTreasureTimeController()),

    /** 七夕相遇*/
    LOVER_MEET_ACTIVITY(
            Activity.ActivityType.LOVER_MEET_VALUE,
            LoverMeetEntity.class,
            new LoverMeetActivity(0, null),
            new LoverMeetHandler(),
            new LoverMeetController()),

    /** 机甲皮肤*/
    JIJIA_SKIN(
            Activity.ActivityType.JIJIA_SKIN_VALUE,
            JijiaSkinEntity.class,
            new JijiaSkinActivity(0, null),
            new JijiaSkinHandler(),
            new JijiaSkinTimeController()),


    /** 幸运补给箱*/
    SUPPLY_CRATE(
            Activity.ActivityType.SUPPLY_CRATE_VALUE,
            SupplyCrateEntity.class,
            new SupplyCrateActivity(0, null),
            new SupplyCrateHandler(),
            new SupplyCrateTimeController()),

    /** 幸运补给箱*/
    PLANT_SOLDIER_FACTORY(
            Activity.ActivityType.PLANT_SOLDIER_FACTORY_VALUE,
            PlantSoldierFactoryActivityEntity.class,
            new PlantSoldierFactoryActivity(0, null),
            new PlantSoldierFactoryHandler(),
            new PlantSoldierFactoryTimeController()),

    /** 天降洪福*/
    HEAVEN_BESSING_ACTIVTY(
            Activity.ActivityType.HEAVEN_BLESSING_VALUE,
            HeavenBlessingEntity.class,
            new HeavenBlessingActivity(0, null),
            new HeavenBlessingHandler(),
            new HeavenBlessingTimeController()),

    /** 感恩福利*/
    GRATEFUL_BENEFITS_ACTIVTY(
            Activity.ActivityType.GRATEFUL_BENEFITS_VALUE,
            GratefulBenefitsEntity.class,
            new GratefulBenefitsActivity(0, null),
            new GratefulBenefitsHandler(),
            new GratefulBenefitsTimeController()),

    /** 新首充*/
    NEW_FIRST_RECHARGE(
            Activity.ActivityType.NEW_FIRST_RECHARGE_VALUE,
            NewFirstRechargeEntity.class,
            new NewFirstRechargeActivity(0, null),
            new NewFirstRechargeHandler(),
            new NewFirstRechargeTimeController()),

    /** 陨晶战场成就活动*/
    DYZZ_ACHIEVE(
            Activity.ActivityType.DYZZ_ACHIEVE_VALUE,
            DYZZAchieveEntity.class,
            new DYZZAchieveActivity(0, null),
            new DYZZAchieveHandler(),
            new DYZZAchieveTimeController()),

    /** 玫瑰赠礼 */
    ROSE_GIFT(
            Activity.ActivityType.ROSE_GIFT_VALUE,
            RoseGiftEntity.class,
            new RoseGiftActivity(0, null),
            new RoseGiftHandler(),
            new RoseGiftTimeController()),

    /** 赛季活动 */
    SEASON_ACTIVITY(
            Activity.ActivityType.SEASON_ACTIVITY_VALUE,
            SeasonActivityEntity.class,
            new SeasonActivity(0, null),
            new SeasonActivityHandler(),
            new SeasonActivityTimeController()),

    /** 世界勋章签到活动 */
    WORLD_HONOR_ACTIVITY(
            Activity.ActivityType.WORLD_HONOR_ACTIVITY_VALUE,
            StarLightSignActivityEntity.class,
            new StarLightSignActivity(0, null),
            new StarLightSignActivityHandler(),
            new StarLightSignActivityTimeController()),

    DIFF_INFO_SAVE(
            Activity.ActivityType.DIFF_INFO_SAVE_VALUE,
            DiffInfoSaveEntity.class,
            new DiffInfoSaveActivity(0, null),
            new DiffInfoSaveHandler(),
            new DiffInfoSaveTimeController()),

    /** 新服科技（全军出击）**/
    DIFF_NEW_SERVER_TECH(
            Activity.ActivityType.DIFF_NEW_SERVER_TECH_VALUE,
            DiffNewServerTechEntity.class,
            new DiffNewServerTechActivity(0, null),
            new DiffNewServerTechHandler(),
            new DiffNewServerTechTimeController()),

    /** 拼多多活动 **/
    PDD_ACTIVITY(
            Activity.ActivityType.PDD_ACTIVITY_VALUE,
            PDDActivityEntity.class,
            new PDDActivity(0, null),
            new PDDActivityHandler(),
            new PDDActivityTimeController()),

    /** 联盟回流 **/
    GUILD_BACK(
            Activity.ActivityType.GUILD_BACK_VALUE,
            GuildBackEntity.class,
            new GuildBackActivity(0, null),
            new GuildBackHandler(),
            new GuildBackTimeController()),

    /** 拆服合服活动 **/
    CHANGE_SVR_ACTIVITY(
            Activity.ActivityType.CHANGE_SVR_ACTIVITY_VALUE,
            ChangeServerEntity.class,
            new ChangeServerActivity(0, null),
            new ChangeServerHandler(),
            new ChangeServerTimeController()),

    /** 鸿运夺金 **/
    LUCK_GET_GOLD(
            Activity.ActivityType.LUCK_GET_GOLD_VALUE,
            LuckGetGoldEntity.class,
            new LuckGetGoldActivity(0, null),
            new LuckGetGoldHandler(),
            new LuckGetGoldTimeController()),

    /** 实力飞升 **/
    DEVELOP_FAST(
            Activity.ActivityType.DEVELOP_FAST_VALUE,
            DevelopFastEntity.class,
            new DevelopFastActivity(0, null),
            new DevelopFastHandler(),
            new DevelopFastTimeController()),

    DEVELOP_FAST_OLD(
            Activity.ActivityType.DEVELOP_FAST_OLD_VALUE,
            DevelopFastOldEntity.class,
            new DevelopFastOldActivity(0, null),
            new DevelopFastOldHandler(),
            new DevelopFastOldTimeController()),

    CNY_EXAM_ACTIVITY(
            Activity.ActivityType.CNY_EXAM_VALUE,
            CnyExamEntity.class,
            new CnyExamActivity(0, null),
            new CnyExamHandler(),
            new CnyExamTimeController()),

    BACK_TO_NEW_FLY(
            Activity.ActivityType.BACK_TO_NEW_FLY_VALUE,
            BackToNewFlyEntity.class,
            new BackToNewFlyActivity(0, null),
            new BackToNewFlyHandler(),
            new BackToNewFlyTimeController()),

    BACK_TO_NEW_FLY_OLD(
            Activity.ActivityType.BACK_TO_NEW_FLY_OLD_VALUE,
            BackToNewFlyOldEntity.class,
            new BackToNewFlyOldActivity(0, null),
            null,
            new BackToNewFlyOldTimeController()),

    RETURN_UPGRADE(
            Activity.ActivityType.RETURN_UPGRADE_346_VALUE,
            ReturnUpgradeEntity.class,
            new ReturnUpgradeActivity(0, null),
            new ReturnUpgradeHandler(),
            new ReturnUpgradeTimeController()),

    GROW_UP_BOOST(
            Activity.ActivityType.GROW_UP_BOOST_VALUE,
            GrowUpBoostEntity.class,
            new GrowUpBoostActivity(0, null),
            new GrowUpBoostHandler(),
            new GrowUpBoostTimeController()),

    NEW_START(
            Activity.ActivityType.NEW_START_VALUE,
            NewStartEntity.class,
            new NewStartActivity(0, null),
            new NewStartHandler(),
            new NewStartTimeController()),

    /**大帝战兑换*/
    HEAL_EXCHANGE_ACTIVITY(Activity.ActivityType.HEAL_EXCHANGE_VALUE, HealExchangeEntity.class, new HealExchangeActivity(0, null), new HealExchangeHandler(), new HealExchangeTimeController()),

    /** 英雄祈福*/
    HERO_WISH_ACTIVITY(Activity.ActivityType.HERO_WISH_VALUE, HeroWishEntity.class,new HeroWishActivity(0, null), new HeroWishHandler(), new HeroWishController()),
    /** 独家记忆*/
    EXCLUSIVE_MEMORY_ACTIVITY(Activity.ActivityType.EXCLUSIVE_MEMORY_VALUE, ExclusiveMemoryEntity.class,new ExclusiveMemoryActivity(0, null), new ExclusiveMemoryHandler(), new ExclusiveMemoryController()),

    /** 周年庆活动入口*/
    ANNIVERSARY_CELEBRATE_ACTIVITY(Activity.ActivityType.ANNIVERSARY_CELEBRATE_VALUE, null,new AnniversaryCelebrateActivity(0, null),new AnniversaryCelebrateController()),

    /** 荣耀英雄降临*/
    HONOUR_HERO_BEFELL_ACTIVITY(Activity.ActivityType.HONOUR_HERO_BEFELL_VALUE, HonourHeroBefellEntity.class,new HonourHeroBefellActivity(0, null), new HonourHeroBefellHandler(), new HonourHeroBefellController()),

    /** 荣耀英雄回归*/
    HONOUR_HERO_RETURN_ACTIVITY(Activity.ActivityType.HONOUR_HERO_RETURN_VALUE, HonourHeroReturnEntity.class,new HonourHeroReturnActivity(0, null), new HonourHeroReturnHandler(), new HonourHeroReturnController()),

    SHARE_GLORY_ACTIVITY(
            Activity.ActivityType.SHARE_GLORY_VALUE,
            AllianceCelebrateEntity.class,
            new ShareGloryActivity(0, null),
            new ShareGloryHandler(),
            new ShareGloryTimeController()),

    /** 道具回收*/
    RECOVERY_EXCHANGE_ACTIVITY(Activity.ActivityType.RECOVERY_EXCHANGE_VALUE,
            RecoveryExchangeEntity.class,
            new RecoveryExchangeActivity(0, null),
            new RecoveryExchangeHandler(),
            new RecoveryExchangeController()),

    /** 新版新手登录活动 */
    LOGIN_GIFT_ACTIVITY(Activity.ActivityType.LOGIN_GIFT_VALUE, LoginGiftEntity.class,new LoginGiftActivity(0, null), new LoginGiftActivityHandler(), new LoginGiftTimeController()),

    /** 双旦活动 */
    NEWYEAR_LOTTERY_ACTIVITY(Activity.ActivityType.NEW_YEAR_LOTTERY_VALUE, NewyearLotteryEntity.class, new NewyearLotteryActivity(0, null), new NewyearLotteryActivityHandler(), new NewyearLotteryTimeController()),

    /** 机甲舱体守护（机甲玩法）活动 */
    SPACE_GUARD_ACTIVITY(Activity.ActivityType.SPACE_MACHINE_GUARD_VALUE, SpaceGuardEntity.class, new SpaceGuardActivity(0, null), new SpaceGuardHandler(), new SpaceGuardTimeController()),


    /** 可拆分和服通告*/
    MERGE_ANNOUNCE_ACTIVITY(Activity.ActivityType.MERGE_ANNOUNCE_VALUE, null, new MergeAnnounceActivity(0, null), new MergeAnnounceController()),

    /** 机甲研究所*/
    MACHINE_LAB_ACTIVITY(Activity.ActivityType.MACHINE_LAB_VALUE, MachineLabEntity.class, new MachineLabActivity(0, null), new MachineLabHandler(), new MachineLabController()),

    /** 周年庆称号  */
    DRESS_COLLECTION_ACTIVITY(Activity.ActivityType.DRESS_COLLECTION_VALUE, DressCollectionEntity.class, new DressCollectionActivity(0, null), new DressCollectionHandler(), new DressCollectionTimeController()),
   
    /** 周年庆称号2  */
    DRESS_COLLECTION_ACTIVITY_TWO(Activity.ActivityType.DRESS_COLLECTION_TWO_VALUE, DressCollectionTwoEntity.class, new DressCollectionTwoActivity(0, null), new DressCollectionTwoHandler(), new DressCollectionTwoTimeController()),
   
    
    /**1026版本】3S委任英雄投放新活动-331*/
    APPOINT_GET(Activity.ActivityType.APPOINT_GET_VALUE, AppointGetEntity.class, new AppointGetActivity(0, null), new AppointGetActivityHandler(), new AppointGetActivityTimeController()),
    /** 周年庆庆典基金  */
    CELEBRATION_FUND_ACTIVITY(Activity.ActivityType.CELEBRATION_FUND_VALUE, CelebrationFundEntity.class, new CelebrationFundActivity(0, null), new CelebrationFundHandler(), new CelebrationFundTimeController()),

    /**金币觅宝 */
    GOLD_BABY_ACTIVITY(Activity.ActivityType.GOLD_BABY_VALUE, GoldBabyEntity.class, new GoldBabyActivity(0, null), new GoldBabyHandler(), new GoldBabyTimeController()),

    /**金币觅宝 新服 */
    GOLD_BABY_NEW_ACTIVITY(Activity.ActivityType.GOLD_BABY_NEW_VALUE, GoldBabyNewEntity.class, new GoldBabyNewActivity(0, null), new GoldBabyNewHandler(), new GoldBabyNewTimeController()),

    /** 新兵作训  */
    NEWBIE_TRAIN_ACTIVITY(Activity.ActivityType.NOVICE_TRAINING_VALUE, NewbieTrainEntity.class, new NewbieTrainActivity(0, null), new NewbieTrainHandler(), new NewbieTrainTimeController()),

    /**新直购礼包*/
    DIRECT_GIFT_ACTIVITY(Activity.ActivityType.DIRECT_GIFT_VALUE, DirectGiftEntity.class, new DirectGiftActivity(0, null), new DirectGiftHandler(), new DirectGiftTimeController()),
    
    /** 合服邀请*/
    INVITE_MERGE(
    		Activity.ActivityType.INVITE_MERGE_VALUE,
    		null,
    		new InviteMergeActivity(0, null),
    		new InviteMergeHandler(),
    		new InviteMergeTimeController()
    		),
    

    URL_342(
    		Activity.ActivityType.URL_MODEL_342_VALUE,
    		null,
    		new UrlModel342Activity(0, null),
    		null,
    		new UrlModel342TimeController()
    		),
    URL_343(
    		Activity.ActivityType.URL_MODEL_343_VALUE,
    		null,
    		new UrlModel343Activity(0, null),
    		null,
    		new UrlModel343TimeController()
    		),
    URL_344(
    		Activity.ActivityType.URL_MODEL_344_VALUE,
    		null,
    		new UrlModel344Activity(0, null),
    		null,
    		new UrlModel344TimeController()
    		),
    /** 积分冲刺*/
    POINT_SPRINT_345(
    		Activity.ActivityType.POINT_SPRINT_345_VALUE,
    		PointSprintEntity.class,
    		new PointSprintActivity(0, null),
    		new PointSprintHandler(),
    		new PointSprintTimeController()
    		),
    /** 星能探索 */
    PLANET_EXPLORE_347(
    		Activity.ActivityType.PLANET_EXPLORE_347_VALUE,
    		PlanetExploreEntity.class,
    		new PlanetExploreActivity(0, null),
    		new PlanetExploreHandler(),
    		new PlanetExploreTimeController()
    		),

    /** 6重好礼活动*/
    ANNIVERSARY_GIFT(
            Activity.ActivityType.ANNIVERSARY_GIFT_VALUE,
            AnniversaryGiftEntity.class,
            new AnniversaryGiftActivity(0, null),
            new AnniversaryGiftTimeController()
            ),
    /** 泰能超武投放活动 */
    PLANT_WEAPON_355(
            Activity.ActivityType.PLANT_WEAPON_355_VALUE,
            PlantWeaponEntity.class,
            new PlantWeaponActivity(0, null),
            new PlantWeaponHandler(),
            new PlantWeaponTimeController()
            ),
    
    /** 每日必买活动*/
    DAILY_BUY_GIFT(
            Activity.ActivityType.DAILY_BUY_GIFT_VALUE,
            DailyBuyGiftEntity.class,
            new DailyBuyGiftActivity(0, null),
            new DailyBuyGiftTimeController()
            ),
    
    

    /** 回流兵种兑换*/
    BACK_SOLDIER_EXCHANGE(
            Activity.ActivityType.BACK_SOLDIER_EXCHANGE_VALUE,
            BackSoldierExchangeEntity.class,
            new BackSoldierExchangeActivity(0, null),
            new BackSoldierExchangeActivityHandler(),
            new BackSoldierExchangeTimeController()
            ),
    
    /** 回流移民*/
    BACK_IMMGRATION(
            Activity.ActivityType.BACK_IMMGRATION_VALUE,
            null,
            new BackImmgrationActivity(0, null),
            new BackImmgrationActivityHandler(),
            new BackImmgrationActivityTimeController()
            ),
    

    /** 嘎嘎乐活动*/
    LOTTERY_TICKET(
            Activity.ActivityType.LOTTERY_TICKET_VALUE,
            LotteryTicketEntity.class,
            new LotteryTicketActivity(0, null),
            new LotteryTicketHandler(),
            new LotteryTicketTimeController()
            ),
    
    


    /** 打靶活动*/
    SHOOTING_PRACTICE(
            Activity.ActivityType.SHOOTING_PRACTICE_VALUE,
            ShootingPracticeEntity.class,
            new ShootingPracticeActivity(0, null),
            new ShootingPracticeHandler(),
            new ShootingPracticeTimeController()
            ),

	 /** 星海投资*/
    STAR_INVEST(
    		Activity.ActivityType.STAR_INVEST_VALUE, 
    		StarInvestEntity.class, 
    		new StarInvestActivity(0, null),
    		new StarInvestHandler(), 
    		new StarInvestTimeController()),
    
    /** 泰能超武返场活动 */
    PLANT_WEAPON_BACK_360(
            Activity.ActivityType.PLANT_WEAPON_BACK_360_VALUE,
            PlantWeaponBackEntity.class,
            new PlantWeaponBackActivity(0, null),
            new PlantWeaponBackHandler(),
            new PlantWeaponBackController()
            ),
	BEST_PRIZE_361(
            Activity.ActivityType.BEST_PRIZE_361_VALUE,
            BestPrizeEntity.class,
            new BestPrizeActivity(0, null),
            new BestPrizeHandler(),
            new BestPrizeTimeController()
            ),
    
    /** 秘境寻宝*/
    QUEST_TREASURE(
    		Activity.ActivityType.QUEST_TREASURE_VALUE, 
    		QuestTreasureEntity.class, 
    		new QuestTreasureActivity(0, null),
    		new QuestTreasureHandler(), 
    		new QuestTreasureTimeController()),
    
    
    /** 潜艇大战*/
    SUBMARINE_WAR(
    		Activity.ActivityType.SUBMARINE_FIGHT_VALUE, 
    		SubmarineWarEntity.class, 
    		new SubmarineWarActivity(0, null),
    		new SubmarineWarHandler(), 
    		new SubmarineWarTimeController()),

    
    
    /** 首冲活动复刻3*/
    FIRST_RECHARGE_THREE(
    		Activity.ActivityType.FIRST_RECHARGE_THREE_VALUE, 
    		FirstRechargeThreeEntity.class, 
    		new FirstRechargeThreeActivity(0, null),
    		new FirstRechargeThreeHandler(), 
    		new FirstRechargeThreeTimeController()),
    
    
    /** 荣耀动员*/
    HONOUR_MOBILIZE(
    		Activity.ActivityType.HONOR_MOBILIZE_VALUE, 
    		HonourMobilizeEntity.class, 
    		new HonourMobilizeActivity(0, null),
    		new HonourMobilizeHandler(), 
    		new HonourMobilizeTimeController()),
	
	/** 合服比拼活动 */	
	MERGE_COMPETITION(
    		Activity.ActivityType.SERVER_MERGE_COMPETITION_VALUE, 
    		MergeCompetitionEntity.class, 
    		new MergeCompetitionActivity(0, null),
    		new MergeCompetitionActivityHandler(), 
    		new MergeCompetitionTimeController()),
    
    /** （机甲）核心勘探  */
    MECHA_CORE_EXPLORE(
    		Activity.ActivityType.MECHA_CORE_EXPLORE_VALUE, 
    		CoreExploreEntity.class, 
    		new CoreExploreActivity(0, null),
    		new CoreExploreHandler(), 
    		new CoreExploreTimeController()),
    
    /** 赛后庆典  */
    AFTER_COMPETITION(
    		Activity.ActivityType.AFTER_COMPETITION_PARTY_VALUE, 
    		AfterCompetitionEntity.class, 
    		new AfterCompetitionActivity(0, null),
    		new AfterCompetitionHandler(), 
    		new AfterCompetitionTimeController()),
	/** 赛后庆典  */
    SCENE_IMPORT(
    		Activity.ActivityType.SCENE_IMPORT_VALUE, 
    		null, 
    		new SceneImportActivity(0, null),
    		new SceneImportTimeController()),

	/** 赛季拼图  */
    SEASON_PUZZLE_373(
    		Activity.ActivityType.SEASON_PUZZLE_VALUE, 
    		SeasonPuzzleEntity.class, 
    		new SeasonPuzzleActivity(0, null),
    		new SeasonPuzzleHandler(), 
    		new SeasonPuzzleTimeController()),
	 /** 巨龙来袭 */
    GUILD_DRAGON_ATTACK(
    		Activity.ActivityType.GUILD_DRAGON_ATTACK_VALUE, 
    		GuildDragonAttackEntry.class, 
    		new GuildDragonAttackActivity(0, null),
    		new GuildDragonAttactHandler(),
    		new GuildDragonAttactTimeController()),
    /** 有福同享 376活动 */
    SHARE_PROSPERITY_376(
    		Activity.ActivityType.SHARE_PROSPERITY_VALUE, 
    		ShareProsperityEntity.class, 
    		new ShareProsperityActivity(0, null),
    		new ShareProsperityHandler(), 
    		new ShareProsperityController()),
    /**深海秘藏 土拨鼠复刻*/
    DEEP_TREASURE_ACTIVITY(Activity.ActivityType.DEEP_TREASURE_VALUE,
            DeepTreasureEntity.class,
            new DeepTreasureActivity(0, null),
            new DeepTreasureHandler(),
            new DeepTreasureTimeController()),
    /**
     * url模版2(供url跳转)
     */
    URL_MODEL_379_ACTIVITY(Activity.ActivityType.URL_MODEL_379_VALUE, null, new UrlModel379Activity(0, null), new UrlModel379TimeController()),
    URL_MODEL_380_ACTIVITY(Activity.ActivityType.URL_MODEL_380_VALUE, null, new UrlModel380Activity(0, null), new UrlModel380TimeController()),
    URL_MODEL_381_ACTIVITY(Activity.ActivityType.URL_MODEL_381_VALUE, null, new UrlModel381Activity(0, null), new UrlModel381TimeController()),
    

    
    /** 热血畅战 378活动 */
    HOT_BLOOD_WAR_378(
    		Activity.ActivityType.HOT_BLOOD_WAR_VALUE, 
    		HotBloodWarEntity.class, 
    		new HotBloodWarActivity(0, null),
    		new HotBloodWarHandler(), 
    		new HotBloodWarTimeController()),
    /** 押镖玩法  */
    MATERIAL_TRANSPORT(
    		Activity.ActivityType.MATERIAL_TRANSPORT_VALUE, 
    		MaterialTransportEntity.class, 
    		new MaterialTransportActivity(0, null),
    		new MaterialTransportHandler(), 
    		new MaterialTransportTimeController()),
    /**
     * 心愿庄园 382活动
     */
    HOME_LAND_PUZZLE(
            Activity.ActivityType.HOME_LAND_PUZZLE_VALUE,
            HomeLandPuzzleEntity.class,
            new HomeLandPuzzleActivity(0, null),
            new HomeLandPuzzleActivityHandler(),
            new HomeLandPuzzleTimeController()),
			
    /**
     * 心愿庄园 383活动
     */
    HOME_LAND_ROUND(
            Activity.ActivityType.HOME_LAND_ROUND_VALUE,
            HomeLandRoundEntity.class,
            new HomeLandRoundActivity(0, null),
            new HomeLandRoundActivityHandler(),
            new HomeLandRoundTimeController()),
	;
	

    /**
     * @param value
     *            活动类型id编号
     * @param activity
     *            活动逻辑处理对象
     * @param handler
     *            活动消息接收对象
     */
    ActivityType(int value, Class<? extends HawkDBEntity> arg, ActivityBase activity, ActivityProtocolHandler handler, ITimeController timeControl) {
        this.value = value;
        this.activity = activity;
        this.handler = handler;
        this.dbEntity = arg;
        this.timeControl = timeControl;
    }

    /**
     * @param value
     *            活动类型id编号
     * @param activity
     *            活动逻辑处理对象
     */
    ActivityType(int value, Class<? extends HawkDBEntity> arg, ActivityBase activity, ITimeController timeControl) {
        this(value, arg, activity, new ActivityProtocolHandler(), timeControl);
    }

    private int value;

    private ActivityBase activity;

    private ActivityProtocolHandler handler;

    /**
     * 活动时间控制器
     */
    private ITimeController timeControl;

    /**
     * 存储db的类型，用于反序列化
     */
    private Class<? extends HawkDBEntity> dbEntity;

    public Class<? extends HawkDBEntity> getDbEntity() {
        return dbEntity;
    }

    public void setDbEntity(Class<HawkDBEntity> dbEntity) {
        this.dbEntity = dbEntity;
    }

    public int intValue() {
        return value;
    }

    public ActivityBase getActivity() {
        return activity;
    }

    public ActivityProtocolHandler getHandler() {
        return handler;
    }

    @SuppressWarnings("unchecked")
    public <T extends ITimeController> T getTimeControl() {
        return (T) timeControl;
    }

    public static ActivityType getType(int activityType) {
        for (ActivityType type : values()) {
            if (type.intValue() == activityType) {
                return type;
            }
        }
        return null;
    }
}
