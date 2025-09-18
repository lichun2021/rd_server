package com.hawk.game.config;

import java.util.List;
import java.util.stream.Collectors;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;

@HawkConfigManager.XmlResource(file = "xml/cyborg_build_tree.xml")
public class CYBORGBuildTreeCfg extends HawkConfigBase {
	@Id
	private final String position;// "1_1"
	private final int id;
	private final int camp;// ="1"
	private final int root;
	private final String link; // id1,id2,id3
	private final int buffId; // 建筑所在圈对应buff等级需要额外计算
	private ImmutableList<Integer> linkList;

	public CYBORGBuildTreeCfg() {
		this.position = "";
		this.id = 0;
		this.camp = 0;
		this.root = 0;
		this.link = "";
		this.buffId = 0;
	}

	@Override
	protected boolean assemble() {

		List<Integer> linkIdList = Splitter.on(",").splitToList(link).stream().map(Integer::valueOf).collect(Collectors.toList());
		linkList = ImmutableList.copyOf(linkIdList);

		return true;
	}

	public boolean isLinkedNode(int treeid) {
		return linkList.contains(treeid);
	}

	public int getId() {
		return id;
	}

	public int getRoot() {
		return root;
	}

	public String getLink() {
		return link;
	}

	public int getCamp() {
		return camp;
	}

	public String getPosition() {
		return position;
	}

	public int getBuffId() {
		return buffId;
	}

}
