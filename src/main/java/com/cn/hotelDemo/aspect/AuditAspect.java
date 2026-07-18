package com.cn.hotelDemo.aspect;

import java.lang.reflect.Method;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.cn.hotelDemo.annotation.AuditLogged;
import com.cn.hotelDemo.service.AuditService;

@Aspect
@Component
public class AuditAspect {

    private final AuditService auditService;
    private final ExpressionParser parser = new SpelExpressionParser();

    public AuditAspect(AuditService auditService) {
        this.auditService = auditService;
    }

    @AfterReturning(pointcut = "@annotation(auditLogged)", returning = "result")
    public void logAudit(JoinPoint joinPoint, AuditLogged auditLogged, Object result) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = (authentication != null && authentication.getName() != null) ? authentication.getName() : "anonymous";
        
        String resourceId = resolveSpelExpression(joinPoint, result, auditLogged.resourceIdSpel(), "N/A");
        String action = auditLogged.action().startsWith("#") ? resolveSpelExpression(joinPoint, result, auditLogged.action(), "UNKNOWN") : auditLogged.action();

        auditService.record(
                action,
                username,
                auditLogged.resourceType(),
                resourceId,
                "SUCCESS",
                "Action performed successfully"
        );
    }

    private String resolveSpelExpression(JoinPoint joinPoint, Object result, String spelExpression, String defaultValue) {
        if (spelExpression == null || spelExpression.isEmpty() || spelExpression.equals("''")) {
            return defaultValue;
        }

        EvaluationContext context = new StandardEvaluationContext();
        
        // Add method arguments to context
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] parameterNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();
        if (parameterNames != null) {
            for (int i = 0; i < parameterNames.length; i++) {
                context.setVariable(parameterNames[i], args[i]);
            }
        }
        
        // Add method return value to context
        context.setVariable("result", result);

        try {
            Object evaluatedId = parser.parseExpression(spelExpression).getValue(context);
            return evaluatedId != null ? String.valueOf(evaluatedId) : "N/A";
        } catch (Exception e) {
            return "UNKNOWN";
        }
    }
}
