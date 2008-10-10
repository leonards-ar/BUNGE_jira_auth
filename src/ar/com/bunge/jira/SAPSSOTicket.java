/*
 * File name: SAPSSOTicket.java
 * Creation date: Oct 10, 2008 3:51:17 PM
 * Copyright Mindpool
 */
package ar.com.bunge.jira;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

/**
 *
 * @author <a href="mcapurro@gmail.com">Mariano Capurro</a>
 * @version 1.0
 * @since SPM 1.0
 *
 */
public class SAPSSOTicket {
	private static final Logger LOG = Logger.getLogger(SAPSSOTicket.class);	
	
    public static final int ISSUER_CERT_SUBJECT = 0;
    public static final int ISSUER_CERT_ISSUER = 1;
    public static final int ISSUER_CERT_SERIALNO = 2;
		
    private static boolean initialized = false;
    public static String SECLIBRARY ;
    public static String SSO2TICKETLIBRARY = "sapssoext";
    
    public static final String SAP_COOKIE_NAME = "MYSAPSSO2";
    
    static {
        if (System.getProperty("os.name").startsWith("Win"))  {
            SECLIBRARY = "sapsecu.dll";
        } else {
            SECLIBRARY = "libsapsecu.so";
        }
        try {
            System.loadLibrary(SSO2TICKETLIBRARY); 
            LOG.info("SAPSSOEXT loaded.");
        } catch (Throwable ex) {
        	LOG.error("Error during initialization of SSO2TICKET: " + ex.getMessage(), ex);
        }
    }

	/**
	 * 
	 */
	public SAPSSOTicket() {
	}
    
    
    /**
     * Initialization
     * 
     * @param seclib location of ssf-implemenation
     * 
     * @return true/false whether initailisation was ok
     */
    private static native synchronized boolean init(String seclib);

    /**
     * Returns internal version.
     * 
     * @return version
     */
    public static native synchronized String getVersion();
    
    /**
     * eval ticket
     * 
     * @param ticket        the ticket
     * @param pab           location of pab
     * @param pab_password  password for access the pab
     * 
     * @return Object array with:
     *         [0] = (String)user, [1] = (String)sysid, [2] = (String)client , [3] = (byte[])certificate
     *         [4] = (String)portalUser, [5] = (String)authSchema, [6] = validity
     *  
     */
    public static native synchronized Object[] evalLogonTicket(
        String ticket,
        String pab,
        String pab_password)
        throws Exception;
    

    /**
     * Parse certificate
     * @param cert 			Certificate received from evalLogonTicket
     * @param info_id       One of the requst idŽs
     * 
     * @return Info string from certificate
     *  
     */
    public static native synchronized String parseCertificate(
        byte[] cert,
        int info_id);

    /**
     * 
     * @param request
     * @param response
     * @return
     */
    private String getSAPSSOTicket(HttpServletRequest request, HttpServletResponse response) {
    	Cookie[] cookies = request.getCookies();

    	for(int i=0; i < cookies.length; i++) {
    		if(LOG.isDebugEnabled()) {
    			LOG.debug("Found cookie [" + cookies[i].getName() + "]");
    		}
    		if(SAP_COOKIE_NAME.equals(cookies[i].getName())) {
        		if(LOG.isDebugEnabled()) {
        			LOG.debug("Found SAP SSO Ticket cookie with value [" + cookies[i].getValue() + "]");
        		}
    			return cookies[i].getValue();
    		}
    	}

    	if(LOG.isDebugEnabled()) {
			LOG.debug("SAP SSO Ticket cookie not found");
		}
    	return null;
    }
    
    /**
     * 
     * @param ticket
     * @return
     * @throws Exception
     */
    private Ticket parseTicket(String ticket) throws Exception {
    	try {
    		Object elements[] = evalLogonTicket (ticket, "SAPdefault", null);
    		Ticket t = new Ticket();
    		t.setUser((String)elements[0]);
    		t.setIssuingSystemId((String)elements[1]);
    		t.setClient((String)elements[2]);
    		t.setCertificate((byte[])elements[3]);
    		t.setPortalUser((String)elements[4]);
    		return t;
    	} catch(Exception ex) {
    		LOG.error("Error parsin ticket [" + ticket + "] -> " + ex.getMessage(), ex);
    		throw ex;
    	}
    }
    
    /**
     * 
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public String getLoggedUsername(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	String ticket = getSAPSSOTicket(request, response);
    	if(ticket != null && ticket.trim().length() > 0) {
    		if(LOG.isDebugEnabled()) {
            	LOG.debug("Ticket found. Start processing with ticket library version [" + getVersion() + "]");
    		}
    		Ticket t = parseTicket(ticket);
    		LOG.debug("Ticket parsed -> " + t);
    		return t.getUser();
    	} else {
        	LOG.info("Ticket not found. No authenticated user present in session");
    		return null;
    	}
    }
}
