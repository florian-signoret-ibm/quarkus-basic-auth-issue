package com.quarkus401;

import org.jboss.logging.Logger;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;

@Provider
@PreMatching
public class SecurityRequestFilter implements ContainerRequestFilter {

    private static final Logger LOG = Logger.getLogger(SecurityRequestFilter.class);

    @Override
    public void filter(ContainerRequestContext containerRequestContext) {

        final String authorization = containerRequestContext.getHeaderString(AUTHORIZATION);
        final String requestInternalId = containerRequestContext.getHeaderString("request_internal_id");

        String principal = "";
        if (containerRequestContext.getSecurityContext() != null) {
            if (containerRequestContext.getSecurityContext().getUserPrincipal() != null) {
                principal = containerRequestContext.getSecurityContext().getUserPrincipal().getName();
            } else {
                principal = "[null]";
            }
        } else {
            principal = "[no-security-context]";
        }

        LOG.info("Received request from '" + (requestInternalId != null ? requestInternalId : "unknown") +
                "' with authorization header: '" + (authorization != null ? authorization : "none") + "'" +
                " and principal '" + principal +"'");
    }
}
