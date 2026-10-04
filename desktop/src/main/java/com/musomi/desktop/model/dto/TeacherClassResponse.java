package com.musomi.desktop.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for a single teacher class assignment entry returned by GET /teacher/classes.
 *
 * <p>Fields match exactly the response shape defined in API_CONTRACT.md section 12:
 * <pre>{@code
 * {
 *   "classId": 1,
 *   "className": "S3",
 *   "streamId": 1,
 *   "streamName": "Blue",
 *   "subjectId": 1,
 *   "subjectName": "Mathematics",
 *   "studentCount": 42,
 *   "assessmentCount": 3,
 *   "draftCount": 1
 * }
 * }</pre>
 *
 * @param classId         unique class identifier
 * @param className       display name of the class (e.g. "S3")
 * @param streamId        unique stream identifier
 * @param streamName      display name of the stream (e.g. "Blue")
 * @param subjectId       unique subject identifier
 * @param subjectName     display name of the subject (e.g. "Mathematics")
 * @param studentCount    number of students enrolled in this class-stream
 * @param assessmentCount total number of assessments for this class-stream-subject
 * @param draftCount      number of draft (unpublished) assessments
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record TeacherClassResponse(

    @JsonProperty("classId")
    Long classId,

    @JsonProperty("className")
    String className,

    @JsonProperty("streamId")
    Long streamId,

    @JsonProperty("streamName")
    String streamName,

    @JsonProperty("subjectId")
    Long subjectId,

    @JsonProperty("subjectName")
    String subjectName,

    @JsonProperty("studentCount")
    int studentCount,

    @JsonProperty("assessmentCount")
    int assessmentCount,

    @JsonProperty("draftCount")
    int draftCount

) {

    public Long getClassId() { return classId; }
    public String getClassName() { return className; }
    public Long getStreamId() { return streamId; }
    public String getStreamName() { return streamName; }
    public Long getSubjectId() { return subjectId; }
    public String getSubjectName() { return subjectName; }
    public int getStudentCount() { return studentCount; }
    public int getAssessmentCount() { return assessmentCount; }
    public int getDraftCount() { return draftCount; }
}
