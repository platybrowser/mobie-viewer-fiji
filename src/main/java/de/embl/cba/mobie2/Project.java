package de.embl.cba.mobie2;

import java.util.List;

public class Project
{
	private List< String > datasets;
	private String defaultDataset;
	private String specVersion;

	public List< String > getDatasets()
	{
		return datasets;
	}

	public String getDefaultDataset()
	{
		return defaultDataset;
	}

	public String getSpecVersion()
	{
		return specVersion;
	}

	public void setDatasets( List<String> datasets ) { this.datasets = datasets; }

	public void setDefaultDataset( String defaultDataset ) { this.defaultDataset = defaultDataset; }

	public void setSpecVersion( String specVersion ) { this.specVersion = specVersion; }
}
