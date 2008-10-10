/*
 * File name: Ticket.java
 * Creation date: Oct 10, 2008 4:22:03 PM
 * Copyright Mindpool
 */
package ar.com.bunge.jira;

import java.io.Serializable;

/**
 *
 * @author <a href="mcapurro@gmail.com">Mariano Capurro</a>
 * @version 1.0
 * @since SPM 1.0
 *
 */
public class Ticket implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8718637322965131206L;
	
	private String user;
	private String issuingSystemId;
	private String client;
	private String portalUser;
	private byte[] certificate;
	
	/**
	 * 
	 */
	public Ticket() {
	}

	/**
	 * @return the user
	 */
	public String getUser() {
		return user;
	}

	/**
	 * 
	 * @return
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("User: " + getUser() + " - ");
		sb.append("Issuing System ID: " + getIssuingSystemId() + " - ");
		sb.append("Client: " + getClient() + " - ");
		sb.append("Portal User: " + getPortalUser());
		
		return sb.toString();
	}
	
	/**
	 * @param user the user to set
	 */
	public void setUser(String user) {
		this.user = user;
	}

	/**
	 * @return the issuingSystemId
	 */
	public String getIssuingSystemId() {
		return issuingSystemId;
	}

	/**
	 * @param issuingSystemId the issuingSystemId to set
	 */
	public void setIssuingSystemId(String issuingSystemId) {
		this.issuingSystemId = issuingSystemId;
	}

	/**
	 * @return the client
	 */
	public String getClient() {
		return client;
	}

	/**
	 * @param client the client to set
	 */
	public void setClient(String client) {
		this.client = client;
	}

	/**
	 * @return the portalUser
	 */
	public String getPortalUser() {
		return portalUser;
	}

	/**
	 * @param portalUser the portalUser to set
	 */
	public void setPortalUser(String portalUser) {
		this.portalUser = portalUser;
	}

	/**
	 * @return the certificate
	 */
	public byte[] getCertificate() {
		return certificate;
	}

	/**
	 * @param certificate the certificate to set
	 */
	public void setCertificate(byte[] certificate) {
		this.certificate = certificate;
	}

	
}
