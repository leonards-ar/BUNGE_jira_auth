/*
 * File name: SAPSSOAuthenticator.java
 * Creation date: Oct 10, 2008 11:55:41 AM
 * Copyright Mindpool
 */
package ar.com.bunge.jira;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.atlassian.seraph.auth.DefaultAuthenticator;

/**
 *
 * @author <a href="mcapurro@gmail.com">Mariano Capurro</a>
 * @version 1.0
 * @since JIRA AUHT 1.0
 *
 */
public class SAPSSOAuthenticator extends DefaultAuthenticator {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final Logger LOG = Logger.getLogger(SAPSSOAuthenticator.class);	

	/**
	 * 
	 */
	public SAPSSOAuthenticator() {
	}

	/**
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @see com.atlassian.seraph.auth.DefaultAuthenticator#getUser(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
    public Principal getUser(HttpServletRequest request, HttpServletResponse response)
    {
        Principal user = null;

        try {
            if(request.getSession() != null && request.getSession().getAttribute(DefaultAuthenticator.LOGGED_IN_KEY) != null) {
                LOG.info("Session found. User already logged in");
                user = (Principal) request.getSession().getAttribute(DefaultAuthenticator.LOGGED_IN_KEY);
            } else {
            		String username = new SAPSSOTicket().getLoggedUsername(request, response);
                    if (username != null) {
                        user = getUser(username);
                        LOG.info("Logged in via SSO, with user " + user);
                        request.getSession().setAttribute(DefaultAuthenticator.LOGGED_IN_KEY, user);
                        request.getSession().setAttribute(DefaultAuthenticator.LOGGED_OUT_KEY, null);
                    } else {
	                    LOG.info("No SSO ticket found. Redirecting");
	                    //user was not found, or not currently valid
	                    return null;
                    }
            }
        } catch (Exception e) {
            LOG.error("Exception: " + e.getMessage(), e);
        }
        return user;
    }
	
}
