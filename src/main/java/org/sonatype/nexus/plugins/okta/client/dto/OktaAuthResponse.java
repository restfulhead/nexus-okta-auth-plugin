package org.sonatype.nexus.plugins.okta.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OktaAuthResponse
{
	private String stateToken;
	private String status;
	private String factorResult;
	private OktaAuthResponseEmbedded embedded;
	private OktaAuthResponseLinks links;

	private String sessionToken;
	private String expiresAt;

	public String getStateToken()
	{
		return stateToken;
	}

	public void setStateToken(String stateToken)
	{
		this.stateToken = stateToken;
	}

	public String getStatus()
	{
		return status;
	}

	public void setStatus(String status)
	{
		this.status = status;
	}

	public String getFactorResult()
	{
		return factorResult;
	}

	public void setFactorResult(String factorResult)
	{
		this.factorResult = factorResult;
	}

	@JsonProperty("_embedded")
	public OktaAuthResponseEmbedded getEmbedded()
	{
		return embedded;
	}

	public void setEmbedded(OktaAuthResponseEmbedded embedded)
	{
		this.embedded = embedded;
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

	public String getSessionToken()
	{
		return sessionToken;
	}

	public void setSessionToken(String sessionToken)
	{
		this.sessionToken = sessionToken;
	}

	public String getExpiresAt()
	{
		return expiresAt;
	}

	public void setExpiresAt(String expiresAt)
	{
		this.expiresAt = expiresAt;
	}

}
