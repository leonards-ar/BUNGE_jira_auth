/*
 * File name: SAPSSOTicket.java
 * Creation date: Oct 10, 2008 3:51:17 PM
 * Copyright Mindpool
 */
package ar.com.bunge.jira;

import java.io.File;
import java.io.FileNotFoundException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.mysap.sso.SSO2Ticket;

/**
 *
 * @author <a href="mcapurro@gmail.com">Mariano Capurro</a>
 * @version 1.0
 * @since SPM 1.0
 *
 */
public class SAPSSOTicket {
	private static final Logger LOG = Logger.getLogger(SAPSSOTicket.class);	
	
	
	public static final String DEFAULT_PAB = "SAPdefault";

    public static String SECLIBRARY = SAPSSOConfiguration.instance().getSAPSecuLibrary();

    
    public static final String SAP_COOKIE_NAME = "MYSAPSSO2";

    static {
    	if(SECLIBRARY == null) {
            if (System.getProperty("os.name").startsWith("Win"))  {
                SECLIBRARY = "sapsecu.dll";
            } else {
                SECLIBRARY = "libsapsecu.so";
            }
    	}
        LOG.debug("SECLIBRARY [" + SECLIBRARY + "] configured");
    }
    
	/**
	 * 
	 */
	public SAPSSOTicket() {
	}
    
    
    /**
     * 
     * @param request
     * @param response
     * @return
     */
    private String getSAPSSOTicket(HttpServletRequest request, HttpServletResponse response) {
    	Cookie[] cookies = request.getCookies();
    	
    	if(cookies != null) {
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
    	}

    	if(LOG.isDebugEnabled()) {
			LOG.debug("SAP SSO Ticket cookie not found");
		}
    	return null;
    }

    /**
     * 
     * @throws Exception
     */
    private void initSAPSSO() throws Exception {
		if( !SSO2Ticket.init(SECLIBRARY)) {
			LOG.error("Could not load library: " + SECLIBRARY);
		} else {
			LOG.debug("SEC library [" + SECLIBRARY + "] loaded");
		}
    }
    
    /**
     * 
     * @param ticket
     * @return
     * @throws Exception
     */
    private Ticket parseTicket(String ticket) throws Exception {
    	try {
    		initSAPSSO();
    		Object elements[] = SSO2Ticket.evalLogonTicket (ticket, getPSEFile(), SAPSSOConfiguration.instance().getPSEPassword());
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
            	LOG.debug("Ticket found. Start processing with ticket library version [" + SSO2Ticket.getVersion() + "]");
    		}
    		Ticket t = parseTicket(ticket);
    		LOG.debug("Ticket parsed -> " + t);
    		return t.getUser();
    	} else {
        	LOG.info("Ticket not found. No authenticated user present in session");
    		return null;
    	}
    }
    
    /**
     * 
     * @return
     */
    private String getPSEFile() {
    	String pseConfig = SAPSSOConfiguration.instance().getPublicKeyOfIssuingSystemPath();
    	if(pseConfig == null || DEFAULT_PAB.equalsIgnoreCase(pseConfig)) {
    		return pseConfig;
    	} else {
    		try {
        		return getFullFilePath(pseConfig);
    		} catch(FileNotFoundException ex) {
    			LOG.error("File " + pseConfig + " not found", ex);
    			return null;
    		}
    	}
    }

    /**
     * 
     * @param filename
     * @return
     * @throws FileNotFoundException
     */
	private String getFullFilePath(String filename) throws FileNotFoundException {
		String path;
		File file = new File(filename);

		if( file.getAbsolutePath().toLowerCase().indexOf(".pse") > 0 ) {
			path = file.getAbsolutePath();
		} else {
			path = file.getAbsolutePath() + ".pse";
		}
		if(!new File(path).exists()) {
			throw new FileNotFoundException("File "+ filename +" does not exists");
		}
		return path;            
	}    
}
