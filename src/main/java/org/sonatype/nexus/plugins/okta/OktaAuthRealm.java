package org.sonatype.nexus.plugins.okta;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.shiro.authc.AccountException;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.pam.UnsupportedTokenException;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.eclipse.sisu.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.security.role.RoleIdentifier;
import org.sonatype.nexus.security.user.UserManager;
import org.sonatype.nexus.security.user.UserNotFoundException;

import org.sonatype.nexus.plugins.okta.client.OktaAuthClient;
import org.sonatype.nexus.plugins.okta.client.OktaAuthClientException;
import org.sonatype.nexus.plugins.okta.client.OktaAuthClientExceptionSeverity;

@Singleton
@Named
@Description("Okta Auth Realm")
public class OktaAuthRealm extends AuthorizingRealm
{
	private static final Logger LOG = LoggerFactory.getLogger(OktaAuthRealm.class);
	public static final String NAME = OktaAuthRealm.class.getName();

	private final OktaAuthClient client;
	private UserManager userManager;

	@Inject
	public OktaAuthRealm(final OktaAuthClient client, final UserManager userManager)
	{
		this.client = Objects.requireNonNull(client);
		this.userManager = Objects.requireNonNull(userManager);
	}

	@Override
	protected void onInit()
	{
		super.onInit();
		LOG.info("Okta Auth Realm for {} initialized...", this.client.getConfig().getOktaOrgUrl());
	}

	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(final AuthenticationToken token) throws AuthenticationException
	{
		if (!(token instanceof UsernamePasswordToken))
		{
			throw new UnsupportedTokenException(String.format("Token of type '%s' is not supported. '%s' is required.",
					token.getClass().getName(), UsernamePasswordToken.class.getName()));
		}

		final UsernamePasswordToken t = (UsernamePasswordToken) token;
		String password = new String(t.getPassword());

		LOG.info("Authenticating with Okta for user {}", t.getUsername());

		try
		{
			client.authn(t.getUsername(), password);
			return new SimpleAuthenticationInfo(t.getUsername(), token.getCredentials(), getName());

		} catch (OktaAuthClientException ex)
		{
			if (OktaAuthClientExceptionSeverity.INFO.equals(ex.getSeverity()))
			{
				LOG.info("Authentication for '" + t.getUsername() + "' was not successful: " + ex.getMessage());
			} else if (OktaAuthClientExceptionSeverity.WARN.equals(ex.getSeverity()))
			{
				LOG.warn("Authentication for '" + t.getUsername() + "' was not successful: " + ex.getMessage());
				throw new AccountException(ex.getMessage(), ex);
			} else
			{
				LOG.error("Authentication for '" + t.getUsername() + "' was not successful: " + ex.getMessage());
				throw new AccountException(ex.getMessage(), ex);
			}
		} catch (Exception ex)
		{
			LOG.error("Unexpected authentication error: " + ex.getMessage(), ex);
			throw new AuthenticationException(ex.getMessage(), ex);
		}

		return null;
	}

	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals)
	{
		Object principal = principals.getPrimaryPrincipal();
		if (!(principal instanceof String))
		{
			LOG.error("Expected principal of type String but was " + principal == null ? "null"
					: principal.getClass().getName());
			return null;
		}
		
		Set<String> roles = new HashSet<String>();
		try {
			for (RoleIdentifier roleIdentifier : userManager.getUser((String) principal).getRoles()) {
				roles.add(roleIdentifier.getRoleId());
	        }
		}
		catch (UserNotFoundException e) {
			throw new AuthorizationException("User for principals: " + principals.getPrimaryPrincipal()
	            + " could not be found.", e);
		}
		
		return new SimpleAuthorizationInfo(roles);
	}

	@Override
	public String getName()
	{
		return NAME;
	}
}
