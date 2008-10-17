/*
 * File name: SAPSSOAuthenticator.java
 * Creation date: Oct 10, 2008 11:55:41 AM
 * Copyright Mindpool
 */
package ar.com.bunge.jira;

import java.security.Principal;
import java.util.Enumeration;

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
    public Principal getUser(HttpServletRequest request, HttpServletResponse response) {
        Principal user = null;

        try {
            if(request.getSession() != null && request.getSession().getAttribute(DefaultAuthenticator.LOGGED_IN_KEY) != null) {
                LOG.info("Session found. User already logged in");
                user = (Principal) request.getSession().getAttribute(DefaultAuthenticator.LOGGED_IN_KEY);
            } else {
            		// First check HTTP Header and then SSO Ticket
            		String username = getUsernameFromHTTPHeader(request, response);
            		if(username == null) {
            			username = getUsernameFromTicket(request, response);
            		}
            		
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
	
    /**
     * 
     * @param request
     * @param response
     * @return
     */
    private String getUsernameFromHTTPHeader(HttpServletRequest request, HttpServletResponse response) {
    	String headerParam = SAPSSOConfiguration.instance().getUserHttpHeaderParameterName();

    	
    	if(headerParam != null && headerParam.trim().length() > 0) {
    		if(LOG.isDebugEnabled()) {
            	LOG.debug("About to check HTTP Headers [" + getHeadersToPrint(request) + "] for SSO information hold in HTTP Header [" + headerParam + "]");
    		}
    		String value = request.getHeader(headerParam);
    		if(value != null && value.trim().length() > 0) {
    			LOG.debug("Found HTTP Header Parameter [" + headerParam + "] with value [" + value + "]");
    			return value;
    		} else {
    			LOG.info("HTTP Header Parameter [" + headerParam + "] not found in request");
    			return null;
    		}
    	} else {
    		LOG.debug("No SSO HTTP Header configuration found");
    		return null;
    	}
    }

    /**
     * 
     * @return
     */
    private String getHeadersToPrint(HttpServletRequest request) {
    	Object paramName;
    	StringBuffer sb = new StringBuffer();
    	for(Enumeration e = request.getHeaderNames(); e.hasMoreElements(); ) {
    		paramName = e.nextElement();
    		sb.append(paramName + "=" + request.getHeader(paramName.toString()));
    		if(e.hasMoreElements()) {
    			sb.append(", ");
    		}
    	}
    	return sb.toString();
    }
    
    /**
     * 
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    private String getUsernameFromTicket(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	LOG.debug("About to check SSO information SAP SSO Cookie");
    	String username = new SAPSSOTicket().getLoggedUsername(request, response);
    	
    	if(username != null && username.trim().length() > 0) {
    		LOG.debug("Found user [" + username + "] in SAP SSO Ticket");
    		return username;
    	} else {
    		LOG.debug("SAP SSO Ticket not found or not logon user present");
    		return null;
    	}
    }
}