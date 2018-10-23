package org.sonatype.nexus.plugins.okta.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OktaErrorResponse
{
	private String errorCode;
	private String errorSummary;
	private String errorLink;
	private String errorId;

	public OktaErrorResponse()
	{
		// empty
	}

	public String getErrorCode()
	{
		return errorCode;
	}

	public void setErrorCode(final String errorCode)
	{
		this.errorCode = errorCode;
	}

	public String getErrorSummary()
	{
		return errorSummary;
	}

	public void setErrorSummary(final String errorSummary)
	{
		this.errorSummary = errorSummary;
	}

	public String getErrorLink()
	{
		return errorLink;
	}

	public void setErrorLink(final String errorLink)
	{
		this.errorLink = errorLink;
	}

	public String getErrorId()
	{
		return errorId;
	}

	public void setErrorId(final String errorId)
	{
		this.errorId = errorId;
	}

}
