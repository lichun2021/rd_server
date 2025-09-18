package com.hawk.game.guild.championship;

public interface GCConst {
	public enum GCBattleState {
		/** 未开启 */
		WAIT,
		/** 预算 */
		ADVANCE_CALC,
		/** 计算完成 */
		CALC_FINISH,
		/** 开始展示*/
		OPEN_SHOW,
	}
	
	public enum GCGuildGrade {
		GRADE_1(1),
		GRADE_2(2),
		GRADE_3(3),
		GRADE_4(4),
		GRADE_5(5);
		private int value;
		
		private GCGuildGrade highGrade;
		private GCGuildGrade lowGrade;
		static {
			GRADE_1.setHighGrade(GRADE_2);
			GRADE_2.setHighGrade(GRADE_3);
			GRADE_3.setHighGrade(GRADE_4);
			GRADE_4.setHighGrade(GRADE_5);
			GRADE_5.setHighGrade(GRADE_5);
			GRADE_1.setLowGrade(GRADE_1);
			GRADE_2.setLowGrade(GRADE_1);
			GRADE_3.setLowGrade(GRADE_2);
			GRADE_4.setLowGrade(GRADE_3);
			GRADE_5.setLowGrade(GRADE_4);
		}
		GCGuildGrade(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}

		public GCGuildGrade getHighGrade() {
			return highGrade;
		}

		private void setHighGrade(GCGuildGrade highGrade) {
			this.highGrade = highGrade;
		}

		public GCGuildGrade getLowGrade() {
			return lowGrade;
		}

		private void setLowGrade(GCGuildGrade lowGrade) {
			this.lowGrade = lowGrade;
		}
		
		public static GCGuildGrade valueOf(int value) {
			for (GCGuildGrade grade : GCGuildGrade.values()) {
				if (grade.getValue() == value) {
					return grade;
				}
			}
			return null;
		}
		
	}
	
	
	/** 联盟小组战斗级别*/
	public enum GCGuildStage{
		TOP_16(0),
		TOP_8(1),
		TOP_4(2),
		TOP_2(3),
		TOP_1(4);
		private int value;

		GCGuildStage(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}
	}
	
	public enum GCBattleStage {
		/** 16进8 */
		TO_8(1, GCGuildStage.TOP_16, GCGuildStage.TOP_8),
		/** 8进4 */
		TO_4(2, GCGuildStage.TOP_8, GCGuildStage.TOP_4),
		/** 4进2 */
		TO_2(3, GCGuildStage.TOP_4, GCGuildStage.TOP_2),
		/** 2进1 */
		TO_1(4, GCGuildStage.TOP_2, GCGuildStage.TOP_1);

		private int value;
		private GCGuildStage loser;
		private GCGuildStage winner;

		private GCBattleStage nestStage;

		private GCBattleStage lastStage;

		private boolean isFinalBattle = false;

		static {
			TO_8.setNestStage(TO_4);
			TO_4.setNestStage(TO_2);
			TO_2.setNestStage(TO_1);
			TO_1.setNestStage(TO_1);
			TO_8.setLastStage(TO_8);
			TO_4.setLastStage(TO_8);
			TO_2.setLastStage(TO_4);
			TO_1.setLastStage(TO_2);
			TO_1.setTop(true);
		}

		public int getValue() {
			return value;
		}

		GCBattleStage(int value, GCGuildStage loser, GCGuildStage winner) {
			this.value = value;
			this.loser = loser;
			this.winner = winner;
		}

		public GCBattleStage getNestStage() {
			return nestStage;
		}

		private void setNestStage(GCBattleStage nestStage) {
			this.nestStage = nestStage;
		}

		public GCBattleStage getLastStage() {
			return lastStage;
		}

		private void setLastStage(GCBattleStage lastStage) {
			this.lastStage = lastStage;
		}

		public boolean isTop() {
			return isFinalBattle;
		}

		private void setTop(boolean isTop) {
			this.isFinalBattle = isTop;
		}

		public GCGuildStage getLoser() {
			return loser;
		}

		public GCGuildStage getWinner() {
			return winner;
		}

		public static GCBattleStage getType(int value) {
			for (GCBattleStage stage : values()) {
				if (stage.value == value) {
					return stage;
				}
			}
			return null;
		}
	}
	
	/** 段位及排名类型*/
	public enum GCGradeRankType {
		/** 联盟击杀 */
		G_KILL(1, "gc_gkill"),
		/** 个人击杀 */
		S_KILL(2, "gc_skill"),
		/** 个人连胜 */
		S_CWIN(3, "gc_sswin"),
		/** 个人击败 */
		S_BEAT(4, "gc_sbeat");
		
		static{
			G_KILL.setGuildRank(true);
		}
		
		private int value;

		private String skey;
		
		private boolean isGuildRank = false;

		GCGradeRankType(int value, String skey) {
			this.value = value;
			this.skey = skey;
		}

		public int getValue() {
			return value;
		}

		public String getSkey() {
			return skey;
		}
		
		public boolean isGuildRank() {
			return isGuildRank;
		}

		private void setGuildRank(boolean isGuildRank) {
			this.isGuildRank = isGuildRank;
		}

		public static GCGradeRankType valueOf(int value) {
			for (GCGradeRankType rankType : GCGradeRankType.values()) {
				if (rankType.getValue() == value) {
					return rankType;
				}
			}
			return null;
		}
	}
}
