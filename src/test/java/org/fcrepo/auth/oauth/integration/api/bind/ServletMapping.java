
package org.fcrepo.auth.oauth.integration.api.bind;

import javax.xml.bind.annotation.XmlElement;

public class ServletMapping extends UrlMappable {

    @XmlElement(namespace = "http://java.sun.com/xml/ns/javaee",
            name = "servlet-name")
    String servletName;

    public String servletName() {
        return this.servletName;
    }

}
