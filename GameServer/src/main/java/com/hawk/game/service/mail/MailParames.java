package com.hawk.game.service.mail;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;
import org.hawk.uuid.HawkUUIDGenerator;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.protobuf.ByteString;
import com.hawk.game.config.AwardCfg;
import com.hawk.game.config.SysMailCfg;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.RedisMail.MailEntityContent;
import com.hawk.game.service.MailService;

/** @author luwentao */
public class MailParames {
	private String uuid;
	private int reSendCount;
	private String playerId;
	private MailId mailId;
	private MailRewardStatus awardStatus;
	// 主标题参数
	private String title;
	private String midTitle;
	// 副标题参数
	private String subTitle;
	// 邮件体参数
	private MailEntityContent content;
	private String reward;
	private int icon;
	private String globalMailId;
	// 侦查等邮件对方的玩家Id
	private String oppPfIcon;
	private String additionalParam;
	private boolean keepLog;
	// 邮件提示
	private List<String> tips;

	private DungeonMailType duntype;

	private MailParames() {
	}

	@Override
	public String toString() {
		return com.google.common.base.MoreObjects.toStringHelper(this).add("playerId", playerId).add("mailId", mailId).add("title", title).add("subTitle", subTitle)
				.add("content", content).add("reward", reward).add("icon", icon).add("globalMailId", globalMailId).add("oppPfIcon", oppPfIcon).toString();
	}

	public static Builder newBuilder() {
		return new Builder();
	}

	public static class Builder {
		private String playerId;
		private String uuid;
		private MailId mailId;
		private String title;
		private String midTitle;
		private String subTitle;
		private MailEntityContent.Builder content;
		private String reward;
		private int icon;
		private String globalMailId;
		private String oppPfIcon;
		private String additionalParam;
		private boolean keepLog = true;
		private MailRewardStatus awardStatus = MailRewardStatus.GET;
		private List<String> tips;
		private DungeonMailType duntype = DungeonMailType.NONE;

		private Builder() {
			this.content = MailEntityContent.newBuilder();
			this.tips = new ArrayList<>();
		}

		public MailParames build() {
			Preconditions.checkNotNull(this.mailId, "MailId required");
			if (this.reward == null) {
				getMailAward(mailId);
			}
			MailParames result = new MailParames();
			if(HawkOSOperator.isEmptyString(this.uuid)){
				String genUUID = HawkUUIDGenerator.genUUID();
				if (StringUtils.isNotEmpty(this.reward)) {//带附件
					genUUID = genUUID + MailService.REWARD_MAIL_DOT;
				}
				result.setUuid(genUUID);
			}else{
				result.setUuid(this.uuid);
			}
			result.setPlayerId(this.playerId);
			result.setMailId(this.mailId);
			result.setTitle(omitNullString(this.title));
			result.setSubTitle(omitNullString(this.subTitle));
			result.setMidTitle(omitNullString(this.midTitle));
			result.setContent(this.content.build());
			result.setReward(omitNullString(this.reward));
			result.setIcon(this.icon);
			result.setGlobalMailId(omitNullString(this.globalMailId));
			result.setOppPfIcon(omitNullString(this.oppPfIcon));
			result.setAwardStatus(this.awardStatus);
			result.setAdditionalParam(omitNullString(this.additionalParam));
			result.setTips(ImmutableList.copyOf(this.tips));
			result.setDuntype(duntype);
			result.setKeepLog(keepLog);
			
			return result;
		}

		private String omitNullString(String str) {
			return Objects.isNull(str) ? "" : str;
		}

		/** 邮件默认奖励
		 * 
		 * @param items
		 * @param mailId
		 * @return */
		protected void getMailAward(MailId mailId) {
			SysMailCfg cfg = HawkConfigManager.getInstance().getConfigByKey(SysMailCfg.class, mailId.getNumber());
			if (cfg == null) {
				return;
			}
			AwardCfg awardCfg = HawkConfigManager.getInstance().getConfigByKey(AwardCfg.class, cfg.getAward());
			if (awardCfg == null) {
				return;
			}

			List<ItemInfo> items = awardCfg.getRandomAward().getAwardItems();

			if (items != null && !items.isEmpty()) {
				items.forEach(this::addReward);
			}
			return;
		}

		public Builder setPlayerId(String playerId) {
			this.playerId = playerId;
			return this;
		}
		
		public Builder setUuid(String uuid) {
			this.uuid = uuid;
			return this;
		}
		
		public Builder setMailId(MailId mailId) {
			this.mailId = mailId;
			return this;
		}
		
		public Builder setKeepLog(boolean keepLog) {
			this.keepLog = keepLog;
			return this;
		}

		public Builder addTitles(Object... title) {
			this.title = Joiner.on("_").skipNulls().join(this.title, null, title);
			return this;
		}
		
		public Builder addMidTitles(Object... title) {
			this.midTitle = Joiner.on("_").skipNulls().join(this.midTitle, null, title);
			return this;
		}

		public Builder addSubTitles(Object... subTitle) {
			this.subTitle = Joiner.on("_").skipNulls().join(this.subTitle, null, subTitle);
			return this;
		}

		@SuppressWarnings("rawtypes")
		private Builder addContent(Object content) {
			if (content instanceof MailEntityContent) {
				throw new RuntimeException("unsupported param type!!");
			}

			if (content instanceof com.google.protobuf.GeneratedMessage.Builder) {
				this.content.addContent(((com.google.protobuf.GeneratedMessage.Builder) content).build().toByteString());
				return this;
			}
			if (content instanceof com.google.protobuf.GeneratedMessage) {
				this.content.addContent(((com.google.protobuf.GeneratedMessage) content).toByteString());
				return this;
			}
			this.content.addContent(ByteString.copyFromUtf8(content.toString()));
			return this;
		}

		public Builder addContents(Object... contents) {
			for (Object obj : contents) {
				addContent(obj);
			}
			return this;
		}

		public Builder addTips(Object... tips) {
			for (Object obj : tips) {
				this.tips.add(obj.toString());
			}
			return this;
		}

		public Builder setRewards(String reward) {
			this.reward = reward;
			return this;
		}

		/** 注意 使用set将重新附值,这不同于add方法的累加操作
		 * 
		 * @param reward
		 * @return */
		public Builder setRewards(List<ItemInfo> reward) {
			this.reward = null;
			reward.forEach(this::addReward);
			return this;
		}

		public Builder addRewards(List<ItemInfo> reward) {
			reward.forEach(this::addReward);
			return this;
		}

		public Builder addReward(ItemInfo reward) {
			Preconditions.checkNotNull(reward);
			if (reward.getCount() == 0) {
				return this;
			}
			this.reward = Joiner.on(",").skipNulls().join(this.reward, reward);
			return this;
		}

		public Builder setIcon(int icon) {
			this.icon = icon;
			return this;
		}

		public Builder setGlobalMailId(String globalMailId) {
			this.globalMailId = globalMailId;
			return this;
		}

		public Builder setOppPfIcon(String oppPfIcon) {
			this.oppPfIcon = oppPfIcon;
			return this;
		}

		public Builder setAwardStatus(MailRewardStatus awardStatus) {
			this.awardStatus = awardStatus;
			return this;
		}

		public Builder setAdditionalParam(String additionalParam) {
			this.additionalParam = additionalParam;
			return this;
		}

		public Builder setDuntype(DungeonMailType duntype) {
			this.duntype = duntype;
			return this;
		}
		
	}

	public String getPlayerId() {
		return playerId;
	}

	public MailId getMailId() {
		return mailId;
	}

	public String getTitle() {
		return title;
	}

	public String getMidTitle() {
		return midTitle;
	}

	public String getSubTitle() {
		return subTitle;
	}

	public MailEntityContent getContent() {
		return content;
	}

	public String getReward() {
		return reward;
	}

	public int getIcon() {
		return icon;
	}

	public String getGlobalMailId() {
		return globalMailId;
	}

	public String getOppPfIcon() {
		return oppPfIcon;
	}

	public List<String> getTips() {
		return tips;
	}

	private void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	private void setMailId(MailId mailId) {
		this.mailId = mailId;
	}

	private void setTitle(String title) {
		this.title = title;
	}

	private void setMidTitle(String midTitle) {
		this.midTitle = midTitle;
	}

	private void setSubTitle(String subTitle) {
		this.subTitle = subTitle;
	}

	private void setContent(MailEntityContent mailEntityContent) {
		this.content = mailEntityContent;
	}

	private void setReward(String reward) {
		this.reward = reward;
	}

	private void setIcon(int icon) {
		this.icon = icon;
	}

	private void setGlobalMailId(String globalMailId) {
		this.globalMailId = globalMailId;
	}

	private void setOppPfIcon(String oppPfIcon) {
		this.oppPfIcon = oppPfIcon;
	}

	public MailRewardStatus getAwardStatus() {
		return awardStatus;
	}

	private void setAwardStatus(MailRewardStatus awardStatus) {
		this.awardStatus = awardStatus;
	}

	public String getAdditionalParam() {
		return additionalParam;
	}

	private void setAdditionalParam(String additionalParam) {
		this.additionalParam = additionalParam;
	}

	private void setTips(List<String> tips) {
		this.tips = tips;
	}

	public int getReSendCount() {
		return reSendCount;
	}

	public int incAndGetReSendCount() {
		this.reSendCount++;
		return this.reSendCount;
	}

	private void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getUuid() {
		return uuid;
	}

	public DungeonMailType getDuntype() {
		return duntype;
	}

	private void setDuntype(DungeonMailType duntype) {
		this.duntype = duntype;
	}

	public boolean isKeepLog() {
		return keepLog;
	}

	private void setKeepLog(boolean keepLog) {
		this.keepLog = keepLog;
	}

	private void setReSendCount(int reSendCount) {
		this.reSendCount = reSendCount;
	}

}
