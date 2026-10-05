package com.musomi.manager.repository;

import java.util.List;

import com.musomi.manager.entity.TeacherAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

/** Persists teacher assignments. */
public interface TeacherAssignmentRepository extends JpaRepository<TeacherAssignment, Long> {

    /** Lists all assignments for a school. */
    List<TeacherAssignment> findBySchoolIdOrderByIdAsc(Long schoolId);

    /** Lists a teacher's assignments for a school. */
    List<TeacherAssignment> findBySchoolIdAndTeacherIdOrderByIdAsc(Long schoolId, Long teacherId);
}
