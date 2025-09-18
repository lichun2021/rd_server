package com.hawk.game.guild;

import org.hawk.os.HawkOSOperator;

import com.hawk.game.config.GuildConstProperty;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.util.GuildUtil;

public class GuildCreateObj {
	private String name;

	private String announce;
	
	private int protoType;

	private int tryTimes;
	
	private ConsumeItems consume;
	/**
	 * 简称首个字符
	 */
	private char[] tagArr;
	
	private String tag;

	public GuildCreateObj(String name, String announce, int protoType, ConsumeItems consume) {
		this.name = name;
		this.announce = announce;
		this.protoType = protoType;
		this.consume = consume;
		this.tagArr = name.toCharArray();
		this.tag = "";
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTag() {
		return this.tag;
	}

	public String getAnnounce() {
		return announce;
	}

	public void setAnnounce(String announce) {
		this.announce = announce;
	}

	public int getProtoType() {
		return protoType;
	}

	public void setProtoType(int protoType) {
		this.protoType = protoType;
	}

	public ConsumeItems getConsume() {
		return consume;
	}

	public int getTryTimes() {
		return tryTimes;
	}

	public void addTryTimes() {
		this.tryTimes += 1;
	}

	public String randomTag() {
		if (HawkOSOperator.isEmptyString(this.tag)) {
			int lengthLimit = GuildConstProperty.getInstance().getGuildTagLength();
			if (name.length() >= lengthLimit) {
				this.tag = name.substring(0, GuildConstProperty.getInstance().getGuildTagLength());
			} else {
				this.tag = name;
			}
		} else {
			String randomStr = GuildUtil.getTagRandomStr(tryTimes);
			int length = randomStr.length();
			int headLength = GuildConstProperty.getInstance().getGuildTagLength() - length;
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < headLength; i++) {
				sb.append(this.tagArr[i]);
			}
			sb.append(randomStr);
			tryTimes++;
			this.tag = sb.toString();
		}
		return this.tag;
	}

	@Override
	public String toString() {
		return "GuildCreateObj [name=" + name + ", tag=" + tag + ", tryTimes=" + tryTimes + ", announce=" + announce + "]";
	}
	
	

}
