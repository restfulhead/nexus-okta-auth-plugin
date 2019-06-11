package org.sonatype.nexus.plugins.okta.client;

import static org.sonatype.nexus.plugins.okta.client.OktaAuthClientExceptionSeverity.INFO;
import static org.sonatype.nexus.plugins.okta.client.OktaAuthClientExceptionSeverity.WARN;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.plugins.okta.client.dto.OktaAuthRequest;
import org.sonatype.nexus.plugins.okta.client.dto.OktaAuthRequestVerifyFactor;
import org.sonatype.nexus.plugins.okta.client.dto.OktaAuthResponse;
import org.sonatype.nexus.plugins.okta.client.dto.OktaAuthResponseEmbeddedFactor;

@Singleton
@Named("OktaAuthClient")
public class OktaAuthClient
{
	private static final Logger LOG = LoggerFactory.getLogger(OktaAuthClient.class);

	private ApiHttpClient client;
	private OktaAuthClientConfig config;

	public OktaAuthClient()
	{
		// empty
	}

	@Inject
	public OktaAuthClient(final OktaAuthClientConfig config)
	{
		this.config = config;
		client = new ApiHttpClientImpl();
	}

	public OktaAuthClient(final ApiHttpClient client, final OktaAuthClientConfig config)
	{
		this.client = client;
		this.config = config;
	}

	public OktaAuthClientConfig getConfig()
	{
		return config;
	}

	public OktaAuthResponse authn(final String username, final String password)
	{

		final OktaAuthRequest requestBody = new OktaAuthRequest(username, password, null);

		final String uri = config.getOktaOrgUrl() + config.getOktaApi() + "/authn";
		OktaAuthResponse response = client.sendPostRequest(uri, requestBody, OktaAuthResponse.class);

		if ("MFA_REQUIRED".equals(response.getStatus()))
		{
			response = handleMfaChallenge(response);
		}

		if ("SUCCESS".equals(response.getStatus()))
		{
			if (response.getSessionToken() == null)
			{
				throw new OktaAuthClientException(WARN,
						"Authentication appears to be successful, but no session token was found.");
			}
			return response;
		}

		throw new OktaAuthClientException(INFO, "Authentication was not successful");
	}

	protected OktaAuthResponse handleMfaChallenge(final OktaAuthResponse response)
	{
		if (response == null || response.getEmbedded() == null || CollectionUtils.isEmpty(response.getEmbedded().getFactors()))
		{
			throw new OktaAuthClientException(INFO,
					"Status indicates that MFA is required, but no second factor config was found in the response. Did you set up MFA correctly?");
		}

		OktaAuthResponseEmbeddedFactor selectedFactor = null;
		for (final OktaAuthResponseEmbeddedFactor factor : response.getEmbedded().getFactors())
		{
			if ("push".equalsIgnoreCase(factor.getFactorType()))
			{
				selectedFactor = factor;
				break;
			}
		}

		if (selectedFactor == null)
		{
			throw new OktaAuthClientException(INFO,
					"No supported factor found. At the moment only the 'push' factor type is supported. Factors found: "
							+ client.asStrOrEmpty(response.getEmbedded().getFactors()));
		}

		return verifyMfa(response.getStateToken(), selectedFactor);
	}

	protected OktaAuthResponse verifyMfa(final String stateToken, final OktaAuthResponseEmbeddedFactor factor)
	{
		if (factor == null || factor.getLinks() == null || factor.getLinks().getVerify() == null
				|| StringUtils.isBlank(factor.getLinks().getVerify().getHref()))
		{
			throw new OktaAuthClientException(WARN, "Expected to find link for verification on factor " + client.asStrOrEmpty(factor));
		}
		final String verifyLink = factor.getLinks().getVerify().getHref();
		final OktaAuthRequestVerifyFactor body = new OktaAuthRequestVerifyFactor(stateToken);
		final OktaAuthResponse verificationResponse = client.sendPostRequest(verifyLink, body, OktaAuthResponse.class);

		if (verificationResponse == null || verificationResponse.getLinks() == null
				|| verificationResponse.getLinks().getNext() == null
				|| StringUtils.isBlank(verificationResponse.getLinks().getNext().getHref()))
		{
			throw new OktaAuthClientException(WARN,
					"Expected to find link to poll for push notification: " + client.asStrOrEmpty(verificationResponse));
		}

		final int pollDelay = config.getMfaPollDelay();
		final int pollMaxRetries = config.getMfaPollMaxRetries();
		return pollForPushNotification(stateToken, verificationResponse.getLinks().getNext().getHref(), pollDelay, pollMaxRetries,
				1);
	}

	protected OktaAuthResponse pollForPushNotification(final String stateToken, final String pollLink, final int delay, final int maxRetries, final int tryNo)
	{
		if (tryNo >= maxRetries)
		{
			throw new OktaAuthClientException(WARN,
					"Still waiting for push notification confirmation after too many retries (" + tryNo + ")");
		}

		try
		{
			Thread.sleep(delay);
		} catch (final InterruptedException e)
		{
			LOG.warn(e.getMessage(), e); // ignore
		}

		final OktaAuthRequestVerifyFactor body = new OktaAuthRequestVerifyFactor(stateToken);
		final OktaAuthResponse response = client.sendPostRequest(pollLink, body, OktaAuthResponse.class);
		if ("SUCCESS".equals(response.getStatus()))
		{
			return response;
		} else if ("WAITING".equals(response.getFactorResult()))
		{
			return pollForPushNotification(stateToken, pollLink, delay, maxRetries, tryNo + 1);
		} else if ("TIMEOUT".equals(response.getFactorResult()))
		{
			throw new OktaAuthClientException(INFO, "Push notification confirmation timed out");
		} else if ("REJECTED".equals(response.getFactorResult()))
		{
			throw new OktaAuthClientException(INFO, "Push notification was rejected");
		} else
		{
			throw new OktaAuthClientException(WARN, "Unexpected verification status: " + response.getFactorResult());
		}
	}


}
