package com.isolate.microservice.loadBalancer;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;


import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Sandun Gunasekara
 * Created on 11/25/2019
 */
@RestController
public class CustomError implements ErrorController {

    private static final String PATH = "error";

    private ErrorAttributes errorAttributes;

    @Autowired
    public CustomError(ErrorAttributes errorAttributes) {
        this.errorAttributes = errorAttributes;
    }

    @RequestMapping(PATH)
    @ResponseBody
    public CustomHttpErrorResponse error(WebRequest request, HttpServletResponse response) {
        return new CustomHttpErrorResponse(response.getStatus(), getErrorAttributes(request));          // return a CustomeErrorResponse type object using request
    }

    public void setErrorAttributes(ErrorAttributes errorAttributes) {
        this.errorAttributes = errorAttributes;
    }

    @Override
    public String getErrorPath() {
        return PATH;
    }

    private Map<String, Object> getErrorAttributes(WebRequest request) {
        return new HashMap<>(this.errorAttributes.getErrorAttributes(request, true));
    }

    static class CustomHttpErrorResponse {

        private Integer status;
        private String path;
        private String errorMessage;
        private String timeStamp;
        private String trace;

        CustomHttpErrorResponse(int status, Map<String, Object> errorAttributes) {
            this.setStatus(status);
            this.setPath((String) errorAttributes.get("path"));                 //Set error attributes from request to object attributes
            this.setErrorMessage((String) errorAttributes.get("message"));
            this.setTimeStamp(errorAttributes.get("timestamp").toString());
            this.setTrace((String) errorAttributes.get("trace"));
        }

        //Getters and setters

        public Integer getStatus() {
            return status;
        }

        public void setStatus(Integer status) {
            this.status = status;
        }

        public String getPath() {
            return path;
        }

        void setPath(String path) {
            this.path = path;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        public String getTimeStamp() {
            return timeStamp;
        }

        void setTimeStamp(String timeStamp) {
            this.timeStamp = timeStamp;
        }

        public String getTrace() {
            return trace;
        }

        void setTrace(String trace) {
            this.trace = trace;
        }
    }
}



