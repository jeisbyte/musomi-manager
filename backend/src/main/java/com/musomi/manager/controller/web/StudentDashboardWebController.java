package com.musomi.manager.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

@Controller
public class StudentDashboardWebController {

    @GetMapping("/student/dashboard")
    public String showDashboard(Model model) {
        model.addAttribute("pageTitle", "Dashboard");
        model.addAttribute("currentPage", "dashboard");
        model.addAttribute("schoolName", "St. Mary's Secondary School");
        
        // Mock Student Details
        model.addAttribute("firstName", "Sarah");
        model.addAttribute("className", "S3");
        model.addAttribute("streamName", "Blue");
        
        // Mock Term & Academic Info
        model.addAttribute("currentTerm", "Term 1");
        model.addAttribute("currentYear", "2026");
        
        // Mock Stats
        model.addAttribute("average", "82.5");
        model.addAttribute("overallGrade", "A");
        model.addAttribute("position", 12);
        model.addAttribute("classSize", 42);
        
        // Mock Recent Marks
        List<Map<String, Object>> recentMarks = List.of(
            Map.of("subjectName", "Mathematics", "title", "Mid-term Test", "score", 85.0, "maxScore", 100, "grade", "A", "type", "TEST"),
            Map.of("subjectName", "English Language", "title", "Poetry Assignment", "score", 78.5, "maxScore", 100, "grade", "B", "type", "HOMEWORK"),
            Map.of("subjectName", "Physics", "title", "Lab Experiment", "score", 65.0, "maxScore", 100, "grade", "C", "type", "PRACTICAL")
        );
        model.addAttribute("recentMarks", recentMarks);
        
        return "student/dashboard";
    }
}
