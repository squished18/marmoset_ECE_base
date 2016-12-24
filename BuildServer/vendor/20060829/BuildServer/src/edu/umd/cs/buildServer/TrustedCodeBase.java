/*
 * Copyright (C) 2004-2005, University of Maryland
 * All Rights Reserved
 * Created on Mar 10, 2005
 */
package edu.umd.cs.buildServer;

/**
 * A trusted code base that can be given special
 * permissions in the security policy file.
 */
public class TrustedCodeBase {
	String property;
	String value;
	public TrustedCodeBase(String property, String value) {
		this.property = property;
		this.value = value;
	}
	/**
	 * @return Returns the property.
	 */
	public String getProperty() {
		return property;
	}
	/**
	 * @return Returns the value.
	 */
	public String getValue() {
		return value;
	}
}