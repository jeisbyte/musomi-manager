package com.musomi.manager.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

@Controller
public class StudentResultsWebController {

    @GetMapping("/student/results")
    public String showResults(Model model) {
        model.addAttribute("pageTitle", "My Results");
        model.addAttribute("currentPage", "results");
        model.addAttribute("schoolName", "St. Mary's Secondary School");
        
        model.addAttribute("termName", "Term 1");
        model.addAttribute("year", "2026");
        
        List<Map<String, Object>> subjects = List.of(
            Map.of("subjectId", "sub-math", "subjectName", "Mathematics", "average", 81.5, "grade", "A", "assessmentCount", 2),
            Map.of("subjectId", "sub-eng", "subjectName", "English Language", "average", 78.5, "grade", "B", "assessmentCount", 1)
        );
        model.addAttribute("subjects", subjects);
        
        model.addAttribute("overallAverage", 80.0);
        model.addAttribute("overallGrade", "A");
        model.addAttribute("position", 12);
        model.addAttribute("classSize", 42);
        
        return "student/results";
    }
}
