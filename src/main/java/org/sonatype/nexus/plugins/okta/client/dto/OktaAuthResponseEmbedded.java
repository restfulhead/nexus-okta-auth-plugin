package org.sonatype.nexus.plugins.okta.client.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OktaAuthResponseEmbedded
{
	private List<OktaAuthResponseEmbeddedFactor> factors;

	public List<OktaAuthResponseEmbeddedFactor> getFactors()
	{
		return factors;
	}

	public void setFactors(List<OktaAuthResponseEmbeddedFactor> factors)
	{
		this.factors = factors;
	}

}
