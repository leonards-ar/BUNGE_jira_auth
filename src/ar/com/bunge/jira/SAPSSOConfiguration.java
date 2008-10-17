/*
 * File name: SAPSSOConfiguration.java
 * Creation date: Oct 16, 2008 10:14:20 AM
 * Copyright Mindpool
 */
package ar.com.bunge.jira;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

/**
 *
 * @author <a href="mcapurro@gmail.com">Mariano Capurro</a>
 * @version 1.0
 * @since SPM 1.0
 *
 */
public class SAPSSOConfiguration {
	
	private static final Logger LOG = Logger.getLogger(SAPSSOConfiguration.class);	
	
	private Properties configuration = new Properties();
	private static final String CONFIG_FILENAME = "sapsso_ext.properties";
	
	private static SAPSSOConfiguration instance = null;
	
	/**
	 * 
	 */
	public SAPSSOConfiguration() {
	}

	/**
	 * 
	 * @return
	 */
	public static SAPSSOConfiguration instance() {
		if(instance == null) {
			instance = new SAPSSOConfiguration();
			instance.load();
		}
		return instance;
	}
	
	/**
	 * 
	 *
	 */
	private void load() {
		InputStream in = null;
		
		try {
			in = getClass().getResourceAsStream("/" + CONFIG_FILENAME);
			if(in != null) {
				this.configuration.load(in);
			} else {
				LOG.error("Properties file " + CONFIG_FILENAME + " not found on CLASSPATH");
			}
		} catch(Exception ex) {
			LOG.error("Could not load configuration from properties file " + CONFIG_FILENAME + ": " + ex.getMessage(), ex);
		}
	}

	/**
	 * 
	 * @param key
	 * @return
	 */
	public String getConfigutationParameter(String key) {
		return configuration.getProperty(key);
	}
	
	/**
	 * 
	 * @return
	 */
	public String getUserHttpHeaderParameterName() {
		return getConfigutationParameter("sap.sso.remote_user_aliasparameter");
	}
	
	/**
	 * 
	 * @return
	 */
	public String getPublicKeyOfIssuingSystemPath() {
		String pab = getConfigutationParameter("sap.sso.pab_path");
		return pab != null && pab.trim().length() > 0 ? pab : SAPSSOTicket.DEFAULT_PAB;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getPSEPassword() {
		String psePwd = getConfigutationParameter("sap.sso.pse_password");
		return psePwd != null && psePwd.trim().length() > 0 ? psePwd : null;
	}

	/**
	 * 
	 * @return
	 */
	public String getSAPSecuLibrary() {
		String lib = getConfigutationParameter("sap.sso.secu_library");
		return lib != null && lib.trim().length() > 0 ? lib : null;
	}
	
	/**
	 * 
	 * @return
	 */
	public List getPrefixToRemove() {
		String values = getConfigutationParameter("sap.sso.prefix_to_remove");
		if(values != null && values.trim().length() > 0) {
			StringTokenizer st = new StringTokenizer(values, "|");
			List prefix = new ArrayList();
			while(st.hasMoreTokens()) {
				prefix.add(st.nextToken());
			}
			return prefix;
		} else {
			return Collections.EMPTY_LIST;
		}
	}
	
}
