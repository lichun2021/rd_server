package com.hawk.activity.type.impl.rank;

import com.hawk.activity.type.impl.bannerkill.rank.BannerKillRankProvider;
import com.hawk.activity.type.impl.guildbanner.rank.GuildBannerRankProvider;
import com.hawk.activity.type.impl.hellfire.rank.HellFireRankProvider;
import com.hawk.activity.type.impl.hellfiretwo.rank.HellFireTwoRankProvider;
import com.hawk.activity.type.impl.mergecompetition.rank.impl.GiftScoreRankProvider;
import com.hawk.activity.type.impl.mergecompetition.rank.impl.GuildPowerRankProvider;
import com.hawk.activity.type.impl.mergecompetition.rank.impl.PersonalPowerRankProvider;
import com.hawk.activity.type.impl.mergecompetition.rank.impl.VitCostRankProvider;
import com.hawk.activity.type.impl.peakHonour.rank.PeakHonourRankProvider;
import com.hawk.activity.type.impl.pointSprint.rank.PointSprintRankProvider;
import com.hawk.activity.type.impl.seasonActivity.rank.GuildSeasonKingGradeRankProvider;
import com.hawk.activity.type.impl.stronestleader.rank.StrongestStageRankProvider;
import com.hawk.activity.type.impl.stronestleader.rank.StrongestTotalRankProvider;
import com.hawk.activity.type.impl.timeLimitDrop.rank.TimeLimitDropRankProvider;

public enum ActivityRankType {

	STRONGEST_STAGE_RANK(StrongestStageRankProvider.class),
	STRONGEST_TOTALL_RANK(StrongestTotalRankProvider.class),
	TIME_LIMIT_DROP_RANK(TimeLimitDropRankProvider.class),
	HELL_FIRE_RANK(HellFireRankProvider.class),            // 全军动员
	HELL_FIRE_TWO_RANK(HellFireTwoRankProvider.class),     // 全军动员
	BANNER_KILL_RANK(BannerKillRankProvider.class),        // 战神降临活动排名
	GUILD_BANNER_RANK(GuildBannerRankProvider.class),      // 插旗活动排名
	
	PEAK_HONOUR(PeakHonourRankProvider.class), // 巅峰荣耀
	GUILD_SEASON_KING_GRADE_RANK(GuildSeasonKingGradeRankProvider.class),//联盟赛季王者排名
	POINT_SPRINT_345(PointSprintRankProvider.class),
	
	MERGE_COMPETITION_PERSON_POWER_RANK(PersonalPowerRankProvider.class),//合服比拼活动排名（个人去兵战力）
	MERGE_COMPETITION_VITCOST_RANK(VitCostRankProvider.class),           //合服比拼活动排名（个人体力消耗）
	MERGE_COMPETITION_GUILD_POWER_RANK(GuildPowerRankProvider.class),    //合服比拼活动排名（联盟去兵战力）
	MERGE_COMPETITION_GIFT_SCORE_RANK(GiftScoreRankProvider.class),      //合服比拼活动排名（嘉奖积分）
	;
	
	ActivityRankType(Class<?> providerClass) {
		this.providerClass = providerClass;
	}
	
	private Class<?> providerClass;
	
	public Class<?> getProviderClass() {
		return providerClass;
	}
}
