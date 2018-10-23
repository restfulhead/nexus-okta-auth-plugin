package org.sonatype.nexus.plugins.okta.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// TODO this should be a map with dynamic links rather than simply listing some selected static links
@JsonIgnoreProperties(ignoreUnknown = true)
public class OktaAuthResponseLinks
{
	private OktaAuthResponseLink verify;
	private OktaAuthResponseLink next;

	public OktaAuthResponseLink getVerify()
	{
		return verify;
	}

	public void setVerify(OktaAuthResponseLink verify)
	{
		this.verify = verify;
	}

	public OktaAuthResponseLink getNext()
	{
		return next;
	}

	public void setNext(OktaAuthResponseLink next)
	{
		this.next = next;
	}

}
