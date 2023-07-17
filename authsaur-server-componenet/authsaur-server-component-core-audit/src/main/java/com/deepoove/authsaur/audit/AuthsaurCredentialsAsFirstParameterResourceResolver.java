package com.deepoove.authsaur.audit;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.AuthenticationTransaction;
import org.apereo.cas.configuration.model.core.web.MessageBundleProperties;
import com.deepoove.authsaur.principal.MinePrincipal;
import com.deepoove.authsaur.principal.PrincipalAttr;
import org.apereo.cas.util.AopUtils;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.flow.authentication.CasWebflowExceptionCatalog;
import org.apereo.cas.web.flow.authentication.CasWebflowExceptionHandler;
import org.apereo.inspektr.audit.AuditTrailManager;
import org.apereo.inspektr.audit.spi.AuditResourceResolver;
import org.aspectj.lang.JoinPoint;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Setter
@RequiredArgsConstructor
@Slf4j
public class AuthsaurCredentialsAsFirstParameterResourceResolver implements AuditResourceResolver {

    final CasWebflowExceptionCatalog errors;
    private AuditTrailManager.AuditFormats auditFormat = AuditTrailManager.AuditFormats.DEFAULT;

    /**
     * Turn the arguments into a list.
     *
     * @param args the args
     * @return the string[]
     */
    private String[] toResources(final Object[] args, Map<String, String> user, String failCode) {
        val object = args[0];
        if (object instanceof AuthenticationTransaction) {
            val transaction = AuthenticationTransaction.class.cast(object);
            Map<String, Object> res = new HashMap<>();
            res.put("credentials", AuditTrailManager.AuditFormats.DEFAULT.serialize(transaction.getCredentials()));
            res.put("user", user);
            res.put("failCode", failCode);
            return new String[]{toResourceString(res)};
        }
        return new String[]{toResourceString(CollectionUtils.wrap(object))};
    }

    private String toResourceString(final Object credential) {
        return auditFormat.serialize(credential);
    }

    @Override
    public String[] resolveFrom(final JoinPoint joinPoint, final Object retval) {
        Map<String, String> user = new HashMap<>();
        if (retval instanceof Authentication) {
            MinePrincipal minePrincipal = new MinePrincipal(((Authentication) retval).getPrincipal());
            user.put(PrincipalAttr.NAME.getAttr(), minePrincipal.getName());
            user.put(PrincipalAttr.USER_ID.getAttr(), minePrincipal.getUserId());
            user.put(PrincipalAttr.AUTH_TYPE.getAttr(), minePrincipal.getAuthType());
            if (null != minePrincipal.getRelatedPrincipal()) {
                user.put("relatedAuthType", minePrincipal.getRelatedPrincipal().getAuthType());
            }
        }
        return toResources(AopUtils.unWrapJoinPoint(joinPoint).getArgs(), user, null);
    }

    @Override
    public String[] resolveFrom(final JoinPoint joinPoint, final Exception e) {
        String messageCode = CasWebflowExceptionHandler.UNKNOWN;
        if (e instanceof AuthenticationException) {
            val values = ((AuthenticationException) e).getHandlerErrors().values().stream().map(
                    Throwable::getClass).collect(Collectors.toList());
            val handlerErrorName = errors.getRegisteredExceptions().stream().filter(values::contains).map(
                    Class::getSimpleName).findFirst().orElseGet(() -> {
                LOGGER.debug("Unable to translate handler errors of the authentication e [{}]. Returning " + "[{}]", e,
                        CasWebflowExceptionHandler.UNKNOWN);
                return CasWebflowExceptionHandler.UNKNOWN;
            });

            messageCode = MessageBundleProperties.DEFAULT_BUNDLE_PREFIX_AUTHN_FAILURE + handlerErrorName;
        }
        return toResources(AopUtils.unWrapJoinPoint(joinPoint).getArgs(), null, messageCode);
    }
}