package com.musomi.manager.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginWebController {

    @GetMapping("/login")
    public String showLoginForm(
            @RequestParam(name = "error", required = false) String error,
            Model model) {
        
        model.addAttribute("pageTitle", "Student Login");
        // Normally schoolName comes from settings or a generic attribute advice
        model.addAttribute("schoolName", "Musomi Manager"); 
        
        if (error != null) {
            String errorMessage = "Wrong username or password.";
            switch (error) {
                case "ACCOUNT_LOCKED":
                    errorMessage = "Too many failed attempts. Your account is locked. Please contact the school office.";
                    break;
                case "ACCOUNT_INACTIVE":
                    errorMessage = "This account is not active. Please contact the school office.";
                    break;
                case "SESSION_EXPIRED":
                    errorMessage = "Your session expired. Please sign in again.";
                    break;
                case "INVALID_CREDENTIALS":
                default:
                    errorMessage = "Wrong username or password.";
                    break;
            }
            model.addAttribute("errorMessage", errorMessage);
        }
        
        return "student/login";
    }
        
    @org.springframework.web.bind.annotation.PostMapping("/login")
    public String simulateLogin() {
        return "redirect:/student/dashboard";
    }

    @org.springframework.web.bind.annotation.PostMapping("/logout")
    public String simulateLogout() {
        return "redirect:/login";
    }
}
