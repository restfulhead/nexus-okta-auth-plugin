package org.sonatype.nexus.plugins.okta.client;

import org.sonatype.nexus.plugins.okta.client.dto.OktaErrorResponse;

public class OktaAuthClientException extends RuntimeException
{
	private static final long serialVersionUID = 5208195150639479189L;

	private final OktaAuthClientExceptionSeverity severity;

	public OktaAuthClientException(OktaAuthClientExceptionSeverity severity, final OktaErrorResponse response)
	{
		super(String.format("Okta error '%s' - %s", response.getErrorCode(), response.getErrorSummary()));
		this.severity = severity;
	}

	public OktaAuthClientException(OktaAuthClientExceptionSeverity severity, final String message, final Throwable cause)
	{
		super(message, cause);
		this.severity = severity;
	}

	public OktaAuthClientException(OktaAuthClientExceptionSeverity severity, final String message)
	{
		super(message);
		this.severity = severity;
	}

	public OktaAuthClientException(OktaAuthClientExceptionSeverity severity, final Throwable cause)
	{
		super(cause);
		this.severity = severity;
	}

	public OktaAuthClientExceptionSeverity getSeverity()
	{
		return severity;
	}

}
