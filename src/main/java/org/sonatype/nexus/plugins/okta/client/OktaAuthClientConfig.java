package org.sonatype.nexus.plugins.okta.client;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Named
public class OktaAuthClientConfig
{
	private static final Logger LOG = LoggerFactory.getLogger(OktaAuthClientConfig.class);

	private static final String CFG_FILE = "nexus-okta-auth.properties";

	private static String OKTA_URL_KEY = "okta.org.url";

	private static String OKTA_API_KEY = "okta.api";
	private static String OKTA_API_DEFAULT = "/api/v1";

	private static String MFA_POLL_DELAY_KEY = "mfa.poll.delay";
	private static int MFA_POLL_DELAY_DEFAULT = 3000;
	private static String MFA_POLL_MAXRETRY_KEY = "mfa.poll.maxretry";
	private static int MFA_POLL_MAXRETRY_DEFAULT = 20;

	private Properties configuration;

	@PostConstruct
	public void init()
	{
		configuration = new Properties();

		try (InputStream input = Files.newInputStream(Paths.get(".", "etc", CFG_FILE)))
		{
			configuration.load(input);
		} catch (final IOException e)
		{
			LOG.warn("Error reading '" + CFG_FILE + "' properties. Falling back to default configuration", e);
		}
	}

	public String getOktaOrgUrl()
	{
		return configuration.getProperty(OKTA_URL_KEY);
	}

	public String getOktaApi()
	{
		return configuration.getProperty(OKTA_API_KEY, OKTA_API_DEFAULT);
	}

	public int getMfaPollDelay()
	{
		return propertyAsInt(MFA_POLL_DELAY_KEY, MFA_POLL_DELAY_DEFAULT, 100, 60000);
	}

	public int getMfaPollMaxRetries()
	{
		return propertyAsInt(MFA_POLL_MAXRETRY_KEY, MFA_POLL_MAXRETRY_DEFAULT, 1, 60);
	}

	private int propertyAsInt(String key, int defaultValue, int minValue, int maxValue)
	{
		String delay = configuration.getProperty(key);
		if (delay != null)
		{
			try
			{
				int val = Integer.parseInt(delay);
				if (val < minValue)
				{
					throw new IllegalArgumentException(key + " must be equal or greater than " + minValue);
				}
				if (val > maxValue)
				{
					throw new IllegalArgumentException(key + " must be equal or less than " + maxValue);
				}

				return val;
			} catch (RuntimeException ex)
			{
				LOG.warn("Error reading property '" + key + "'. Falling back to default configuration", ex);
				return defaultValue;
			}
		} else
		{
			return defaultValue;
		}
	}

}
