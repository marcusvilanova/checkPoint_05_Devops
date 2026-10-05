package br.com.dimdim.atendimento.controller;

import br.com.dimdim.atendimento.service.RecursoNaoEncontradoException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
public class ErroControllerAdvice {
    private static final Logger LOGGER = LoggerFactory.getLogger(ErroControllerAdvice.class);

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ModelAndView naoEncontrado() {
        return erro(HttpStatus.NOT_FOUND, "Registro não encontrado",
                "O registro solicitado não existe ou já foi excluído.");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ModelAndView parametroInvalido() {
        return erro(HttpStatus.BAD_REQUEST, "Endereço inválido",
                "Confira o endereço solicitado e tente novamente.");
    }

    @ExceptionHandler({NoResourceFoundException.class, NoHandlerFoundException.class})
    public ModelAndView paginaNaoEncontrada() {
        ModelAndView view = new ModelAndView("error/404");
        view.setStatus(HttpStatus.NOT_FOUND);
        return view;
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ModelAndView metodoInvalido() {
        return erro(HttpStatus.METHOD_NOT_ALLOWED, "Operação não permitida neste endereço",
                "Use os links e formulários da aplicação para realizar esta operação.");
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView inesperado(Exception exception, HttpServletRequest request) {
        LOGGER.error("Falha inesperada no atendimento da requisição {}", request.getRequestURI(), exception);
        return erro(HttpStatus.INTERNAL_SERVER_ERROR, "Não foi possível concluir a operação",
                "Tente novamente em alguns instantes. Se o problema persistir, avise a equipe responsável.");
    }

    private ModelAndView erro(HttpStatus status, String titulo, String mensagem) {
        ModelAndView view = new ModelAndView("erro");
        view.setStatus(status);
        view.addObject("codigo", status.value());
        view.addObject("titulo", titulo);
        view.addObject("descricao", mensagem);
        return view;
    }
}
