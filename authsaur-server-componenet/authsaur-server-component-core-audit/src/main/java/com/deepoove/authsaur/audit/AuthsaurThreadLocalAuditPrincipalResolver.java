package com.deepoove.authsaur.audit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.audit.AuditPrincipalIdProvider;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationCredentialsThreadLocalBinder;
import org.apereo.cas.authentication.AuthenticationTransaction;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.inspektr.common.spi.PrincipalResolver;
import org.aspectj.lang.JoinPoint;

import java.util.Collection;


/**
 * Inspektr PrincipalResolver that gets the value for principal id from Authentication object bound to a current
 * thread of execution.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class AuthsaurThreadLocalAuditPrincipalResolver implements PrincipalResolver {
    private final AuditPrincipalIdProvider auditPrincipalIdProvider;

    @Override
    public String resolveFrom(final JoinPoint auditTarget, final Object returnValue) {
        LOGGER.trace("Resolving principal at audit point [{}]", auditTarget);
        return getCurrentPrincipal(auditTarget, returnValue, null);
    }

    @Override
    public String resolveFrom(final JoinPoint auditTarget, final Exception exception) {
        LOGGER.trace("Resolving principal at audit point [{}] with thrown exception [{}]", auditTarget, exception);
        return getCurrentPrincipal(auditTarget, null, exception);
    }

    @Override
    public String resolve() {
        return UNKNOWN_USER;
    }

    private String getCurrentPrincipal(final JoinPoint auditTarget, final Object returnValue,
                                       final Exception exception) {
        val authn = AuthenticationCredentialsThreadLocalBinder.getCurrentAuthentication();
        String principal = this.auditPrincipalIdProvider.getPrincipalIdFrom(auditTarget, authn, returnValue, exception);
        if (StringUtils.isBlank(principal)) {
            Object[] args = auditTarget.getArgs();
            if (null != args && args.length >= 1 && args[0] instanceof AuthenticationTransaction) {
                Collection<Authentication> authentications = ((AuthenticationTransaction) args[0]).getAuthentications();
                if (CollectionUtils.isNotEmpty(authentications)) {
                    for (Authentication auth : authentications) {
                        principal = auth.getPrincipal().getId();
                        break;
                    }
                }
            }
        }
        final String p = principal;
        String id = FunctionUtils.doIfNull(principal,
                        AuthenticationCredentialsThreadLocalBinder::getCurrentCredentialIdsAsString,
                        () -> p)
                .get();


        return StringUtils.defaultIfBlank(id, UNKNOWN_USER);
    }
}
