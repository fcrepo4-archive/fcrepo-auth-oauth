package org.fcrepo.auth.oauth.integration.api.bind;

import javax.xml.bind.annotation.XmlElement;


public abstract class Displayable extends Describable {

	@XmlElement(namespace="http://java.sun.com/xml/ns/javaee", name="display-name")
	String displayName;

	public String displayName() {
		return this.displayName;
	}
}
