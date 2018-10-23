package org.sonatype.nexus.plugins.okta.client.dto;

public class OktaAuthRequestVerifyFactor
{
	private String stateToken;

	public OktaAuthRequestVerifyFactor()
	{
		// empty
	}

	public OktaAuthRequestVerifyFactor(String stateToken)
	{
		super();
		this.stateToken = stateToken;
	}

	public String getStateToken()
	{
		return stateToken;
	}

	public void setStateToken(String stateToken)
	{
		this.stateToken = stateToken;
	}

}
