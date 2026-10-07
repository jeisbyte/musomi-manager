package com.musomi.manager.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Map;

@Controller
public class StudentMarksWebController {

    @GetMapping("/student/marks")
    public String showMarks(Model model) {
        model.addAttribute("pageTitle", "My Marks");
        model.addAttribute("currentPage", "marks");
        model.addAttribute("schoolName", "St. Mary's Secondary School");
        
        List<Map<String, Object>> mathMarks = List.of(
            Map.of("id", "m1", "title", "Mid-term Test", "type", "TEST", "date", "15 Mar 2026", "score", 85.0, "maxScore", 100, "grade", "A"),
            Map.of("id", "m2", "title", "Weekly Quiz", "type", "QUIZ", "date", "22 Mar 2026", "score", 18.0, "maxScore", 20, "grade", "A")
        );
        
        List<Map<String, Object>> englishMarks = List.of(
            Map.of("id", "e1", "title", "Poetry Assignment", "type", "HOMEWORK", "date", "10 Mar 2026", "score", 78.5, "maxScore", 100, "grade", "B")
        );
        
        List<Map<String, Object>> subjectGroups = List.of(
            Map.of("subjectId", "sub-math", "subjectName", "Mathematics", "marks", mathMarks),
            Map.of("subjectId", "sub-eng", "subjectName", "English Language", "marks", englishMarks)
        );
        
        model.addAttribute("subjectGroups", subjectGroups);
        return "student/marks";
    }

    @GetMapping("/student/subjects/{subjectId}")
    public String showSubjectDetail(@PathVariable String subjectId, Model model) {
        model.addAttribute("pageTitle", "Subject Detail");
        model.addAttribute("currentPage", "marks");
        model.addAttribute("schoolName", "St. Mary's Secondary School");
        
        model.addAttribute("subjectName", "Mathematics");
        model.addAttribute("average", "81.5");
        model.addAttribute("grade", "A");
        
        List<Map<String, Object>> marks = List.of(
            Map.of("id", "m1", "title", "Mid-term Test", "type", "TEST", "date", "15 Mar 2026", "score", 85.0, "maxScore", 100, "grade", "A"),
            Map.of("id", "m2", "title", "Weekly Quiz", "type", "QUIZ", "date", "22 Mar 2026", "score", 18.0, "maxScore", 20, "grade", "A")
        );
        model.addAttribute("marks", marks);
        
        return "student/subject-detail";
    }
}
