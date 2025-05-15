package cn.bincker.common.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

@Controller
@RequestMapping("/error")
@Slf4j
public class MyErrorController implements ErrorController {
    private final ErrorAttributes errorAttributes;

    public MyErrorController(ErrorAttributes errorAttributes) {
        this.errorAttributes = errorAttributes;
    }

    private Map<String, Object> getErrorAttributes(HttpServletRequest request) {
        ErrorAttributeOptions options;
        if (log.isDebugEnabled()) {
            options = ErrorAttributeOptions.of(
                    ErrorAttributeOptions.Include.EXCEPTION,
                    ErrorAttributeOptions.Include.STACK_TRACE,
                    ErrorAttributeOptions.Include.MESSAGE,
                    ErrorAttributeOptions.Include.BINDING_ERRORS,
                    ErrorAttributeOptions.Include.STATUS,
                    ErrorAttributeOptions.Include.ERROR,
                    ErrorAttributeOptions.Include.PATH
            );
        }else{
            options = ErrorAttributeOptions.of(
                    ErrorAttributeOptions.Include.MESSAGE,
                    ErrorAttributeOptions.Include.BINDING_ERRORS,
                    ErrorAttributeOptions.Include.STATUS,
                    ErrorAttributeOptions.Include.ERROR,
                    ErrorAttributeOptions.Include.PATH
            );
        }
        return errorAttributes.getErrorAttributes(new ServletWebRequest(request), options);
    }

    @GetMapping
    public ModelAndView error(HttpServletRequest request) {
        var attributes = getErrorAttributes(request);
        return new ModelAndView("error", attributes);
    }
}
