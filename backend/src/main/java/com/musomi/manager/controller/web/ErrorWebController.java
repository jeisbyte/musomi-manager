package com.musomi.manager.controller.web;

import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class ErrorWebController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        model.addAttribute("pageTitle", "Error");
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        
        if (status != null) {
            Integer statusCode = Integer.valueOf(status.toString());
            
            if(statusCode == 404) {
                return "errors/404";
            }
            else if(statusCode == 401 || statusCode == 403) {
                return "errors/access-denied";
            }
        }
        return "errors/500";
    }
    
    // Explicit mappings for HTMX redirects
    @GetMapping("/404")
    public String notFound(Model model) {
        model.addAttribute("pageTitle", "Not Found");
        return "errors/404";
    }

    @GetMapping("/500")
    public String serverError(Model model) {
        model.addAttribute("pageTitle", "Server Error");
        return "errors/500";
    }
    
    @GetMapping("/access-denied")
    public String accessDenied(Model model) {
        model.addAttribute("pageTitle", "Access Denied");
        return "errors/access-denied";
    }
}
