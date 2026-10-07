package com.musomi.manager.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

@Controller
public class StudentReportsWebController {

    @GetMapping("/student/reports")
    public String showReports(Model model) {
        model.addAttribute("pageTitle", "Report Cards");
        model.addAttribute("currentPage", "reports");
        model.addAttribute("schoolName", "St. Mary's Secondary School");
        
        List<Map<String, Object>> reports = List.of(
            Map.of("id", "r1", "termName", "Term 1", "academicYear", "2026", "generatedAt", "18 Apr 2026", "downloadUrl", "/reports/download/r1")
        );
        model.addAttribute("reports", reports);
        
        return "student/reports";
    }
}
