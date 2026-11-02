package com.servicehub.graphql;

import com.servicehub.exception.ResourceNotFoundException;
import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

/** Traduz exceções da aplicação em erros GraphQL com tipo semântico (NOT_FOUND, FORBIDDEN...). */
@Component
public class GraphQLExceptionResolver extends DataFetcherExceptionResolverAdapter {

    @Override
    protected GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment env) {
        if (ex instanceof ResourceNotFoundException) {
            return build(env, ErrorType.NOT_FOUND, ex.getMessage());
        }
        if (ex instanceof AccessDeniedException) {
            return build(env, ErrorType.FORBIDDEN, "Você não tem permissão para acessar este recurso.");
        }
        if (ex instanceof AuthenticationException) {
            return build(env, ErrorType.UNAUTHORIZED, "Autenticação necessária.");
        }
        return null;
    }

    private GraphQLError build(DataFetchingEnvironment env, ErrorType type, String message) {
        return GraphqlErrorBuilder.newError(env).errorType(type).message(message).build();
    }
}
