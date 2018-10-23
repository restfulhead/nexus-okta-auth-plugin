package org.sonatype.nexus.plugins.okta;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import org.sonatype.nexus.plugins.okta.client.ApiHttpClientImpl;
import org.sonatype.nexus.plugins.okta.client.OktaAuthClient;
import org.sonatype.nexus.plugins.okta.client.OktaAuthClientConfig;
import org.sonatype.nexus.plugins.okta.client.OktaAuthClientException;
import org.sonatype.nexus.plugins.okta.test.IntegrationTest;

@RunWith(MockitoJUnitRunner.class)
@Category(IntegrationTest.class)
public class OktaAuthClientIntegrationTest
{
	@Rule
	public final ExpectedException exception = ExpectedException.none();

	private OktaAuthClient client;

	private String oktaTestOrgUrl = System.getProperty("oktaTestOrgUrl");
	private String oktaTestUserName = System.getProperty("oktaTestUserName");
	private String oktaTestUserPassword = System.getProperty("oktaTestUserPassword");

	@Before
	public void setup()
	{
		assertNotNull("You must pass in oktaTestOrgUrl as a system property", oktaTestOrgUrl);
		assertNotNull("You must pass in oktaTestUserName as a system property", oktaTestUserName);
		assertNotNull("You must pass in oktaTestUserPassword as a system property", oktaTestUserPassword);

		final OktaAuthClientConfig config = Mockito.mock(OktaAuthClientConfig.class);
		when(config.getOktaOrgUrl()).thenReturn(oktaTestOrgUrl);
		when(config.getOktaApi()).thenReturn("/api/v1");
		when(config.getMfaPollDelay()).thenReturn(3000);
		when(config.getMfaPollMaxRetries()).thenReturn(5);

		this.client = new OktaAuthClient(new ApiHttpClientImpl(), config);
	}

	@Test
	public void shouldNotAuthenticate()
	{
		exception.expect(OktaAuthClientException.class);
		exception.expectMessage("Okta error 'E0000004' - Authentication failed");
		client.authn("test", "test");
	}

	@Test
	public void shouldAuthenticate()
	{
		client.authn(oktaTestUserName, oktaTestUserPassword);
	}
}
