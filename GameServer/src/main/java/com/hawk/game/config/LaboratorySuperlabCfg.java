package com.hawk.game.config;

import java.util.List;
import java.util.stream.Collectors;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.google.common.base.Splitter;

@HawkConfigManager.XmlResource(file = "xml/laborary_superlab.xml")
public class LaboratorySuperlabCfg extends HawkConfigBase {
	@Id
	protected final int superlabId;
	protected final String coreList;

	private List<Integer> pageOneCores;

	public LaboratorySuperlabCfg() {
		superlabId = 0;
		coreList = ""; // 1_2_3_4_5_6
	}

	@Override
	protected boolean assemble() {
		pageOneCores = Splitter.on("_").splitToList(coreList).stream().map(Integer::valueOf).collect(Collectors.toList());
		return true;
	}

	public int getSuperlabId() {
		return superlabId;
	}

	public String getCoreList() {
		return coreList;
	}

	public List<Integer> getPageOneCores() {
		return pageOneCores;
	}

	public void setPageOneCores(List<Integer> pageOneCores) {
		this.pageOneCores = pageOneCores;
	}

}
