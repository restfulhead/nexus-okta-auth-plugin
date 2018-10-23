package org.sonatype.nexus.plugins.okta.client.dto;

import java.util.Map;

public class OktaAuthRequest
{
	private String username;
	private String password;
	private Map<String, String> context;

	public OktaAuthRequest()
	{
		// empty
	}

	public OktaAuthRequest(final String username, final String password, final Map<String, String> context)
	{
		super();
		this.username = username;
		this.password = password;
		this.context = context;
	}

	public String getUsername()
	{
		return username;
	}

	public void setUsername(final String username)
	{
		this.username = username;
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword(final String password)
	{
		this.password = password;
	}

	public Map<String, String> getContext()
	{
		return context;
	}

	public void setContext(final Map<String, String> context)
	{
		this.context = context;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((password == null) ? 0 : password.hashCode());
		result = prime * result + ((username == null) ? 0 : username.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		OktaAuthRequest other = (OktaAuthRequest) obj;
		if (password == null)
		{
			if (other.password != null)
				return false;
		} else if (!password.equals(other.password))
			return false;
		if (username == null)
		{
			if (other.username != null)
				return false;
		} else if (!username.equals(other.username))
			return false;
		return true;
	}


}
