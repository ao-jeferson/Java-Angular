package br.com.antlia.movimento.presentation.rest;

import br.com.antlia.movimento.domain.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.security.authentication.BadCredentialsException;
import org.slf4j.LoggerFactory;
import java.util.*;
@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail validation(MethodArgumentNotValidException ex) {
        ProblemDetail detail=problem(400,"Revise os campos informados.");
        Map<String,String> fields=new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(e -> fields.putIfAbsent(e.getField(),e.getDefaultMessage()));
        detail.setProperty("errors",fields); return detail;
    }
    @ExceptionHandler({IllegalArgumentException.class,HttpMessageNotReadableException.class,MethodArgumentTypeMismatchException.class,MissingServletRequestParameterException.class})
    public ProblemDetail badRequest(Exception ex) {
        return problem(400,ex instanceof IllegalArgumentException ? ex.getMessage() : "Requisição inválida. Confira os campos e tipos esperados.");
    }
    @ExceptionHandler(RecursoNaoEncontradoException.class) public ProblemDetail notFound(Exception ex) { return problem(404,ex.getMessage()); }
    @ExceptionHandler(RegraNegocioException.class) public ProblemDetail invalidRelation(Exception ex) { return problem(422,ex.getMessage()); }
    @ExceptionHandler(ConflitoNumeracaoException.class) public ProblemDetail conflict(Exception ex) { return problem(409,ex.getMessage()); }
    @ExceptionHandler(BadCredentialsException.class) public ProblemDetail unauthorized(Exception ex) { return problem(401,"Usuário ou senha inválidos."); }
    @ExceptionHandler(Exception.class) public ProblemDetail unexpected(Exception ex) {
        String id=UUID.randomUUID().toString();
        LoggerFactory.getLogger(ApiExceptionHandler.class).error("Erro de referência {}",id,ex);
        ProblemDetail detail=problem(500,"Não foi possível concluir a operação."); detail.setProperty("reference",id); return detail;
    }
    private ProblemDetail problem(int status,String message) { return ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(status),message); }
}
