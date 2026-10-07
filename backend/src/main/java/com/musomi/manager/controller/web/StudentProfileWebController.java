package com.musomi.manager.controller.web;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Controller
public class StudentProfileWebController {

    @GetMapping("/student/profile")
    public String showProfile(Model model) {
        model.addAttribute("pageTitle", "My Profile");
        model.addAttribute("currentPage", "profile");
        model.addAttribute("schoolName", "St. Mary's Secondary School");
        
        model.addAttribute("admissionNumber", "U001/0123");
        model.addAttribute("fullName", "Achieng Sarah");
        model.addAttribute("gender", "Female");
        model.addAttribute("dateOfBirth", "12 May 2010");
        model.addAttribute("className", "S3");
        model.addAttribute("streamName", "Blue");
        model.addAttribute("guardianName", "Odongo Peter");
        model.addAttribute("guardianPhone", "+256 700 123 456");
        
        return "student/profile";
    }
    
    @PostMapping("/student/profile/change-password")
    @ResponseBody
    public String changePassword() {
        // HTMX will process this response to show a toast and reset the form
        return "<form hx-post=\"/student/profile/change-password\" hx-swap=\"outerHTML\" class=\"space-y-4\" id=\"password-form\">" +
               "<div class=\"p-4 rounded-lg bg-emerald-50 border border-emerald-200 text-emerald-800 text-sm font-medium flex items-start gap-3 mb-4\">" +
               "<i data-lucide=\"check-circle\" class=\"w-5 h-5 shrink-0 text-emerald-600 mt-0.5\"></i>" +
               "<span>Password successfully changed.</span></div>" +
               "<div><label class=\"block text-sm font-medium text-slate-700 mb-1\">Current Password</label>" +
               "<input type=\"password\" name=\"current\" required class=\"block w-full rounded-lg border-slate-300 shadow-sm focus:border-brand-700 focus:ring-brand-700 sm:text-sm px-3 py-2 border text-slate-900\"></div>" +
               "<div><label class=\"block text-sm font-medium text-slate-700 mb-1\">New Password</label>" +
               "<input type=\"password\" name=\"new\" required class=\"block w-full rounded-lg border-slate-300 shadow-sm focus:border-brand-700 focus:ring-brand-700 sm:text-sm px-3 py-2 border text-slate-900\"></div>" +
               "<div><label class=\"block text-sm font-medium text-slate-700 mb-1\">Confirm New Password</label>" +
               "<input type=\"password\" name=\"confirm\" required class=\"block w-full rounded-lg border-slate-300 shadow-sm focus:border-brand-700 focus:ring-brand-700 sm:text-sm px-3 py-2 border text-slate-900\"></div>" +
               "<div class=\"pt-2\"><button type=\"submit\" class=\"w-full flex justify-center py-2.5 px-4 border border-transparent rounded-lg shadow-sm text-sm font-medium text-white bg-brand-700 hover:bg-brand-800 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-brand-700\">Change Password</button></div>" +
                "<script>lucide.createIcons();</script>" +
                "</form>";
    }

    @PostMapping("/student/profile/upload-avatar")
    @ResponseBody
    public ResponseEntity<String> uploadAvatar(@RequestParam("photo") MultipartFile file) {
        // Validate content type
        Set<String> allowed = Set.of("image/jpeg", "image/png", "image/webp");
        String contentType = file.getContentType();
        if (contentType == null || !allowed.contains(contentType)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid file type. Only JPG, PNG, and WebP are allowed.");
        }
        // Validate size (5 MB)
        if (file.getSize() > 5L * 1024 * 1024) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("File too large. Maximum allowed size is 5 MB.");
        }

        try {
            // Resolve static/img directory — try classpath resource first, fall back to source tree (dev mode)
            ClassPathResource imgDir = new ClassPathResource("static/img/");
            Path targetDir;
            if (imgDir.exists()) {
                targetDir = Paths.get(imgDir.getURI());
            } else {
                targetDir = Paths.get("backend/src/main/resources/static/img");
            }
            Files.createDirectories(targetDir);

            Path target = targetDir.resolve("student-avatar.jpg");
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }
            return ResponseEntity.ok("/img/student-avatar.jpg");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Upload failed: " + e.getMessage());
        }
    }
}
