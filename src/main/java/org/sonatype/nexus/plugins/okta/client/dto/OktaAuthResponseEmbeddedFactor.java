package org.sonatype.nexus.plugins.okta.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OktaAuthResponseEmbeddedFactor
{
	private String id;
	private String factorType;
	private OktaAuthResponseLinks links;

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getFactorType()
	{
		return factorType;
	}

	public void setFactorType(String factorType)
	{
		this.factorType = factorType;
	}

	@JsonProperty("_links")
	public OktaAuthResponseLinks getLinks()
	{
		return links;
	}

	public void setLinks(OktaAuthResponseLinks links)
	{
		this.links = links;
	}
}
