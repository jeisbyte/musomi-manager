# API_CONTRACT.md — Shared Document

**The source of truth for every endpoint. Backend builds it. Frontend calls it.**

---

## 1. How to Use This Document

- **Backend devs:** implement exactly what's described here
- **Frontend devs:** call exactly what's described here
- **Never change an endpoint without updating this file**
- **Never change this file without team agreement**

If the code and this document disagree, this document wins — until someone updates both.

---

## 2. Conventions

### Base URL

```
/api/v1
```

### Authentication

| Client | Method | Header/Cookie |
|---|---|---|
| Desktop app | JWT | `Authorization: Bearer <token>` |
| Student web | Session | `JSESSIONID` cookie (automatic) |

### Content Type

```
Content-Type: application/json
Accept: application/json
```

### Field Naming

camelCase everywhere in JSON:
- `admissionNumber`, `createdAt`, `studentId`

### Dates and Times

- Dates: `YYYY-MM-DD` (e.g. `2026-09-24`)
- Timestamps: ISO 8601 UTC (e.g. `2026-09-24T10:30:00Z`)
- All timestamps stored in UTC

### Money and Decimals

- Marks: decimal with 2 places (e.g. `78.50`)
- Sent as JSON numbers, not strings

### HTTP Status Codes

| Code | Meaning |
|---|---|
| 200 | OK — success |
| 201 | Created — resource created |
| 204 | No Content — success, no body |
| 400 | Bad Request — validation failed |
| 401 | Unauthorized — not logged in or token expired |
| 403 | Forbidden — logged in but not allowed |
| 404 | Not Found — resource doesn't exist |
| 409 | Conflict — duplicate, state conflict |
| 422 | Unprocessable Entity — business rule violated |
| 500 | Server Error — something broke |

---

## 3. Standard Response Shapes

### Success (single resource)

```json
{
  "data": {
    "id": 1,
    "fullName": "Achieng Sarah"
  }
}
```

### Success (list)

```json
{
  "data": [
    { "id": 1, "fullName": "Achieng Sarah" },
    { "id": 2, "fullName": "Akello James" }
  ]
}
```

### Success (paginated)

```json
{
  "data": [ ... ],
  "page": 0,
  "size": 20,
  "total": 145,
  "totalPages": 8
}
```

### Success (no content)

No body, status 204.

### Error

```json
{
  "error": {
    "code": "STUDENT_NOT_FOUND",
    "message": "Student not found.",
    "details": null
  }
}
```

### Error with details

```json
{
  "error": {
    "code": "SCORE_EXCEEDS_MAX",
    "message": "Score cannot exceed 100.",
    "details": {
      "max": 100,
      "entered": 120
    }
  }
}
```

### Validation Error

```json
{
  "error": {
    "code": "VALIDATION_FAILED",
    "message": "One or more fields are invalid.",
    "details": {
      "admissionNumber": "must not be blank",
      "dateOfBirth": "must be in the past"
    }
  }
}
```

---

## 4. Pagination

Query parameters on list endpoints:

```
?page=0&size=20&sort=fullName,asc
```

- `page` — 0-based page number (default 0)
- `size` — items per page (default 20, max 100)
- `sort` — field,direction (default varies per endpoint)

---

## 5. Authentication Endpoints

### POST /auth/login

Log in with username and password.

**Auth:** none

**Request:**
```json
{
  "username": "teacher1",
  "password": "secret123"
}
```

**Response 200:**
```json
{
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresAt": "2026-09-24T18:30:00Z",
    "user": {
      "id": 5,
      "username": "teacher1",
      "fullName": "Mr. Okello",
      "role": "TEACHER",
      "schoolId": 1,
      "schoolName": "St. Mary's Secondary School"
    }
  }
}
```

**Errors:**
- `INVALID_CREDENTIALS` (401)
- `ACCOUNT_LOCKED` (403) — after 5 failed attempts
- `ACCOUNT_INACTIVE` (403)

---

### POST /auth/logout

Invalidate the current session/token.

**Auth:** required

**Response 204** (no body)

---

### GET /auth/me

Get the currently logged-in user.

**Auth:** required

**Response 200:**
```json
{
  "data": {
    "id": 5,
    "username": "teacher1",
    "fullName": "Mr. Okello",
    "role": "TEACHER",
    "schoolId": 1,
    "schoolName": "St. Mary's Secondary School"
  }
}
```

---

### POST /auth/change-password

Change your own password.

**Auth:** required

**Request:**
```json
{
  "currentPassword": "oldpass123",
  "newPassword": "newpass456"
}
```

**Response 204** (no body)

**Errors:**
- `INVALID_CREDENTIALS` (401) — current password wrong
- `WEAK_PASSWORD` (400)

---

## 6. Admin Endpoints — Users

### GET /admin/users

List users with filters.

**Auth:** ADMIN

**Query parameters:**
- `role` — ADMIN / TEACHER / STUDENT (optional)
- `active` — true/false (default true)
- `search` — matches fullName or username
- `page`, `size`, `sort`

**Response 200:**
```json
{
  "data": {
    "data": [
      {
        "id": 5,
        "username": "teacher1",
        "fullName": "Mr. Okello",
        "email": "okello@school.ug",
        "phone": "+256700000000",
        "role": "TEACHER",
        "isActive": true,
        "lastLoginAt": "2026-09-23T08:15:00Z"
      }
    ],
    "page": 0,
    "size": 20,
    "total": 45,
    "totalPages": 3
  }
}
```

---

### POST /admin/users

Create a new user.

**Auth:** ADMIN

**Request:**
```json
{
  "username": "teacher2",
  "password": "initialpass123",
  "fullName": "Ms. Achieng",
  "email": "achieng@school.ug",
  "phone": "+256700000001",
  "role": "TEACHER"
}
```

**Response 201:**
```json
{
  "data": {
    "id": 6,
    "username": "teacher2",
    "fullName": "Ms. Achieng",
    "email": "achieng@school.ug",
    "phone": "+256700000001",
    "role": "TEACHER",
    "isActive": true,
    "createdAt": "2026-02-01T10:00:00Z",
    "lastLoginAt": null
  }
}
```

**Errors:**
- `USERNAME_TAKEN` (409)
- `VALIDATION_FAILED` (400)

---

### GET /admin/users/{id}

**Auth:** ADMIN

**Response 200:**
```json
{
  "data": {
    "id": 5,
    "username": "teacher1",
    "fullName": "Mr. Okello",
    "email": "okello@school.ug",
    "phone": "+256700000000",
    "role": "TEACHER",
    "isActive": true,
    "createdAt": "2026-02-01T10:00:00Z",
    "lastLoginAt": "2026-09-23T08:15:00Z"
  }
}
```

**Errors:**
- `USER_NOT_FOUND` (404)

---

### PUT /admin/users/{id}

Update a user's details (not password).

**Auth:** ADMIN

**Request:**
```json
{
  "fullName": "Mr. John Okello",
  "email": "john.okello@school.ug",
  "phone": "+256700000002"
}
```

**Response 200** (updated user)

---

### DELETE /admin/users/{id}

Deactivate a user (soft delete).

**Auth:** ADMIN

**Response 204**

**Errors:**
- `CANNOT_DEACTIVATE_SELF` (422)

---

### POST /admin/users/{id}/reset-password

Admin-triggered password reset. Generates a temporary password.

**Auth:** ADMIN

**Response 200:**
```json
{
  "data": {
    "temporaryPassword": "Temp9x4Kp2"
  }
}
```

Admin shares this with the user. User must change on next login (future enhancement).

---

## 7. Admin Endpoints — School Structure

### GET /admin/academic-years

**Auth:** ADMIN

**Response 200:**
```json
{
  "data": [
    { "id": 3, "year": 2026, "isCurrent": true, "createdAt": "2026-01-10T08:00:00Z" },
    { "id": 2, "year": 2025, "isCurrent": false, "createdAt": "2025-01-10T08:00:00Z" },
    { "id": 1, "year": 2024, "isCurrent": false, "createdAt": "2024-01-10T08:00:00Z" }
  ]
}
```

---

### POST /admin/academic-years

**Request:**
```json
{ "year": 2027 }
```

**Response 201** (created year)

---

### PUT /admin/academic-years/{id}/current

Set as the current year. Only one can be current.

**Response 200** (updated year)

---

### GET /admin/terms?yearId={id}

**Auth:** ADMIN

**Response 200:**
```json
{
  "data": [
    {
      "id": 1,
      "academicYearId": 3,
      "name": "Term 1",
      "startDate": "2026-02-01",
      "endDate": "2026-05-01",
      "isCurrent": true
    }
  ]
}
```

---

### POST /admin/terms

**Request:**
```json
{
  "academicYearId": 3,
  "name": "Term 2",
  "startDate": "2026-05-15",
  "endDate": "2026-08-15"
}
```

**Response 201** (created term)

---

### PUT /admin/terms/{id}/current

Set as current term.

**Response 200** (updated term)

---

### GET /admin/class-levels

**Response 200:**
```json
{
  "data": [
    { "id": 1, "name": "S1", "sortOrder": 1, "isActive": true },
    { "id": 2, "name": "S2", "sortOrder": 2, "isActive": true },
    { "id": 3, "name": "S3", "sortOrder": 3, "isActive": true }
  ]
}
```

---

### POST /admin/class-levels

**Request:**
```json
{ "name": "S4", "sortOrder": 4 }
```

**Response 201**

---

### GET /admin/streams?classId={id}

**Auth:** ADMIN

**Response 200:**
```json
{
  "data": [
    { "id": 1, "classId": 1, "name": "Blue", "capacity": 50, "isActive": true },
    { "id": 2, "classId": 1, "name": "Red", "capacity": 50, "isActive": true }
  ]
}
```

---

### POST /admin/streams

**Request:**
```json
{
  "classId": 1,
  "name": "Green",
  "capacity": 50
}
```

**Response 201**

---

### GET /admin/classes?yearId={id}

**Response 200:**
```json
{
  "data": [
    {
      "id": 1,
      "name": "S3",
      "classLevelId": 3,
      "academicYearId": 3,
      "classTeacherId": 5,
      "isActive": true,
      "createdAt": "2026-01-10T08:00:00Z"
    }
  ]
}
```

---

### POST /admin/classes

**Request:**
```json
{
  "name": "S4",
  "classLevelId": 4,
  "academicYearId": 3,
  "classTeacherId": 6
}
```

**Response 201**

---

### POST /admin/streams

**Request:**
```json
{
  "classId": 1,
  "name": "Green",
  "capacity": 50
}
```

**Response 201**

---

### GET /admin/subjects

**Response 200:**
```json
{
  "data": [
    {
      "id": 1,
      "code": "MATH",
      "name": "Mathematics",
      "description": null,
      "isCore": true,
      "isActive": true
    }
  ]
}
```

---

### POST /admin/subjects

**Request:**
```json
{
  "code": "ENG",
  "name": "English",
  "description": null,
  "isCore": true
}
```

**Response 201**

---

### GET /admin/subjects/{id}/topics

**Response 200:**
```json
{
  "data": [
    { "id": 1, "subjectId": 1, "name": "Algebra", "description": null, "sortOrder": 1 },
    { "id": 2, "subjectId": 1, "name": "Geometry", "description": null, "sortOrder": 2 }
  ]
}
```

---

### POST /admin/subjects/{id}/topics

**Request:**
```json
{ "subjectId": 1, "name": "Statistics", "description": null, "sortOrder": 3 }
```

**Note:** The subject ID is derived from the path `/admin/subjects/{id}/topics`; the controller binds the path value and overwrites any client-supplied `subjectId`.

**Response 201**

---

### GET /admin/topics?subjectId={id}

**Auth:** ADMIN

**Response 200:**
```json
{
  "data": [
    { "id": 1, "subjectId": 1, "name": "Algebra", "description": null, "sortOrder": 1 },
    { "id": 2, "subjectId": 1, "name": "Geometry", "description": null, "sortOrder": 2 }
  ]
}
```

---

## 8. Admin Endpoints — Students

(Not yet implemented — planned for Phase 3)

### GET /admin/students

**Auth:** ADMIN

**Query parameters:**
- `classId` — filter by class
- `streamId` — filter by stream
- `status` — ACTIVE / INACTIVE
- `search` — matches fullName or admissionNumber
- `page`, `size`, `sort`

**Response 200:**
```json
{
  "data": [
    {
      "id": 1,
      "admissionNumber": "2024/0456",
      "fullName": "Achieng Sarah",
      "gender": "FEMALE",
      "dateOfBirth": "2008-03-15",
      "currentClassId": 1,
      "currentClass": "S3",
      "currentStreamId": 1,
      "currentStream": "Blue",
      "status": "ACTIVE",
      "guardianName": "Mrs. Achieng Mary",
      "guardianPhone": "+256700000003"
    }
  ],
  "page": 0,
  "size": 20,
  "total": 500,
  "totalPages": 25
}
```

---

### POST /admin/students

**Request:**
```json
{
  "admissionNumber": "2024/0459",
  "fullName": "Odongo Peter",
  "gender": "MALE",
  "dateOfBirth": "2008-05-20",
  "currentClassId": 1,
  "currentStreamId": 1,
  "guardian": {
    "name": "Mr. Odongo",
    "relationship": "Father",
    "phone": "+256700000004",
    "email": null
  }
}
```

**Response 201** (created student)

**Errors:**
- `DUPLICATE_ADMISSION_NUMBER` (409)
- `VALIDATION_FAILED` (400)

---

### GET /admin/students/{id}

**Response 200** (full student with guardians)

---

### PUT /admin/students/{id}

**Request:** same as POST (without admissionNumber change)

**Response 200**

---

### DELETE /admin/students/{id}

Soft-delete: sets `isActive = false`.

**Response 204**

---

### POST /admin/students/import

Upload Excel file to import students.

**Auth:** ADMIN

**Content-Type:** `multipart/form-data`

**Request:**
- `file` — the `.xlsx` file

**Response 200:**
```json
{
  "data": {
    "totalRows": 500,
    "validRows": 497,
    "errorRows": 3,
    "errors": [
      { "row": 45, "field": "admissionNumber", "message": "Required" },
      { "row": 102, "field": "admissionNumber", "message": "Duplicate in file" },
      { "row": 233, "field": "gender", "message": "Must be MALE, FEMALE, or OTHER" }
    ],
    "importId": "imp_20260924_001"
  }
}
```

**Note:** This endpoint validates and previews. A second call confirms.

---

### POST /admin/students/import/{importId}/confirm

Confirm the import. Only valid rows are imported.

**Response 200:**
```json
{
  "data": {
    "imported": 497,
    "skipped": 3
  }
}
```

---

### GET /admin/students/import/template

Download the Excel template.

**Response 200:** binary `.xlsx` file

---

## 9. Admin Endpoints — Teacher Assignments

(Not yet implemented — planned for Phase 3)

### GET /admin/assignments?teacherId={id}

**Response 200:**
```json
{
  "data": [
    {
      "id": 1,
      "teacherId": 5,
      "teacherName": "Mr. Okello",
      "subjectId": 1,
      "subjectName": "Mathematics",
      "classId": 1,
      "className": "S3",
      "streamId": 1,
      "streamName": "Blue",
      "academicYearId": 3,
      "academicYear": 2026
    }
  ]
}
```

---

### POST /admin/assignments

**Request:**
```json
{
  "teacherId": 5,
  "subjectId": 1,
  "classId": 1,
  "streamId": 1,
  "academicYearId": 3
}
```

**Response 201**

---

### DELETE /admin/assignments/{id}

**Response 204**

---

## 10. Admin Endpoints — School Settings

(Not yet implemented — planned for Phase 2)

### GET /admin/settings

**Response 200:**
```json
{
  "data": {
    "schoolName": "St. Mary's Secondary School",
    "logoUrl": "/uploads/schools/1/logo.png",
    "address": "P.O. Box 1234, Kampala",
    "phone": "+256414000000",
    "email": "info@stmarys.ac.ug",
    "gradingScale": [
      { "grade": "A", "min": 80, "max": 100 },
      { "grade": "B", "min": 70, "max": 79 },
      { "grade": "C", "min": 60, "max": 69 },
      { "grade": "D", "min": 50, "max": 59 },
      { "grade": "E", "min": 40, "max": 49 },
      { "grade": "F", "min": 0, "max": 39 }
    ],
    "reportHeader": "St. Mary's Secondary School — P.O. Box 1234, Kampala",
    "reportFooter": "Next term begins 15 May 2026",
    "currentTermId": 1,
    "currentAcademicYearId": 3
  }
}
```

---

### PUT /admin/settings

**Request:** same shape as response

**Response 200**

---

### POST /admin/settings/logo

Upload logo.

**Content-Type:** `multipart/form-data`

**Request:**
- `file` — image (PNG/JPG, max 2 MB)

**Response 200:**
```json
{
  "data": { "logoUrl": "/uploads/schools/1/logo.png" }
}
```

---

## 11. Admin Endpoints — Audit Log

(Not yet implemented — planned for Phase 2)

### GET /admin/audit

**Auth:** ADMIN

**Query parameters:**
- `userId`
- `action` — e.g. `SCORE_UPDATED`, `USER_CREATED`
- `entityType` — e.g. `Score`, `Student`
- `from` — ISO date
- `to` — ISO date
- `page`, `size`

**Response 200:**
```json
{
  "data": [
    {
      "id": 12345,
      "userId": 5,
      "userName": "Mr. Okello",
      "action": "SCORE_UPDATED",
      "entityType": "Score",
      "entityId": 789,
      "details": { "oldScore": 75, "newScore": 78.5 },
      "ipAddress": "192.168.1.15",
      "createdAt": "2026-09-24T10:30:00Z"
    }
  ]
}
```

---

## 12. Teacher Endpoints — Classes

(Not yet implemented — planned for Phase 3)

### GET /teacher/classes

Get classes the logged-in teacher is assigned to.

**Auth:** TEACHER

**Response 200:**
```json
{
  "data": [
    {
      "classId": 1,
      "className": "S3",
      "streamId": 1,
      "streamName": "Blue",
      "subjectId": 1,
      "subjectName": "Mathematics",
      "studentCount": 42,
      "assessmentCount": 3,
      "draftCount": 1
    },
    {
      "classId": 1,
      "className": "S3",
      "streamId": 2,
      "streamName": "Red",
      "subjectId": 1,
      "subjectName": "Mathematics",
      "studentCount": 45,
      "assessmentCount": 2,
      "draftCount": 0
    }
  ]
}
```

---

### GET /teacher/classes/{classId}/streams/{streamId}/students

Get the roster for a class-stream.

**Auth:** TEACHER (must teach this class)

**Response 200:**
```json
{
  "data": [
    {
      "id": 1,
      "admissionNumber": "2024/0456",
      "fullName": "Achieng Sarah",
      "gender": "FEMALE"
    }
  ]
}
```

---

## 13. Teacher Endpoints — Assessments

(Not yet implemented — planned for Phase 3)

### GET /teacher/assessments

List assessments by the logged-in teacher.

**Query parameters:**
- `classId`, `streamId`, `subjectId`, `termId`
- `status` — DRAFT / PUBLISHED
- `page`, `size`

**Response 200:**
```json
{
  "data": [
    {
      "id": 123,
      "title": "Mid-Term Exam",
      "type": "EXAM",
      "assessmentDate": "2026-03-15",
      "maxScore": 100,
      "status": "DRAFT",
      "classId": 1,
      "className": "S3",
      "streamName": "Blue",
      "subjectId": 1,
      "subjectName": "Mathematics",
      "termId": 1,
      "topics": [
        { "id": 1, "name": "Algebra" },
        { "id": 2, "name": "Geometry" }
      ],
      "studentCount": 42,
      "enteredCount": 38,
      "createdAt": "2026-03-10T08:00:00Z"
    }
  ]
}
```

---

### POST /teacher/assessments

Create a new assessment.

**Auth:** TEACHER

**Request:**
```json
{
  "classId": 1,
  "streamId": 1,
  "subjectId": 1,
  "termId": 1,
  "title": "Mid-Term Exam",
  "type": "EXAM",
  "assessmentDate": "2026-03-15",
  "maxScore": 100,
  "topicIds": [1, 2]
}
```

**Response 201** (full assessment)

**Errors:**
- `NOT_YOUR_CLASS` (403)
- `ASSESSMENT_TOPIC_REQUIRED` (400)
- `VALIDATION_FAILED` (400)

---

### GET /teacher/assessments/{id}

**Response 200** (full assessment with topics)

---

### PUT /teacher/assessments/{id}

Update an assessment (only if DRAFT).

**Request:** same as POST

**Response 200**

**Errors:**
- `ASSESSMENT_ALREADY_PUBLISHED` (409)

---

### DELETE /teacher/assessments/{id}

Delete a draft assessment.

**Response 204**

**Errors:**
- `ASSESSMENT_ALREADY_PUBLISHED` (409)

---

### POST /teacher/assessments/{id}/publish

Publish the assessment. Makes marks visible to students.

**Response 200:**
```json
{
  "data": {
    "id": 123,
    "status": "PUBLISHED",
    "publishedAt": "2026-03-16T14:22:00Z",
    "affectedStudents": 42
  }
}
```

**Errors:**
- `CANNOT_PUBLISH_EMPTY` (422) — some students have no score

---

## 14. Teacher Endpoints — Mark Entry

(Not yet implemented — planned for Phase 3)

### GET /teacher/assessments/{id}/grid

Load the mark entry grid.

**Response 200:**
```json
{
  "data": {
    "assessment": {
      "id": 123,
      "title": "Mid-Term Exam",
      "type": "EXAM",
      "maxScore": 100,
      "status": "DRAFT",
      "subjectName": "Mathematics",
      "className": "S3",
      "streamName": "Blue",
      "termName": "Term 1",
      "academicYear": 2026,
      "topics": ["Algebra", "Geometry"]
    },
    "students": [
      {
        "id": 1,
        "admissionNumber": "2024/0456",
        "fullName": "Achieng Sarah"
      },
      {
        "id": 2,
        "admissionNumber": "2024/0457",
        "fullName": "Akello James"
      }
    ],
    "scores": [
      {
        "scoreId": 501,
        "studentId": 1,
        "score": 82,
        "feedback": "Excellent work",
        "grade": "B"
      },
      {
        "scoreId": 502,
        "studentId": 2,
        "score": null,
        "feedback": null,
        "grade": null
      }
    ]
  }
}
```

**Note:** `scoreId` may be null for new rows — backend creates on first save.

---

### PUT /teacher/scores/{scoreId}

Update a single score (used for auto-save).

**Auth:** TEACHER (must own the assessment)

**Request:**
```json
{
  "score": 82,
  "feedback": "Excellent work"
}
```

**Response 200:**
```json
{
  "data": {
    "scoreId": 501,
    "score": 82,
    "feedback": "Excellent work",
    "grade": "B",
    "savedAt": "2026-03-16T10:30:00Z"
  }
}
```

**Errors:**
- `SCORE_EXCEEDS_MAX` (400)
- `SCORE_BELOW_MIN` (400)
- `NOT_YOUR_CLASS` (403)

---

### POST /teacher/assessments/{id}/scores/batch

Save multiple scores at once (used for "Save Draft").

**Request:**
```json
{
  "scores": [
    { "scoreId": 501, "studentId": 1, "score": 82, "feedback": "Excellent" },
    { "scoreId": null, "studentId": 2, "score": 45, "feedback": "Needs improvement" },
    { "scoreId": 503, "studentId": 3, "score": 67, "feedback": null }
  ]
}
```

**Response 200:**
```json
{
  "data": {
    "saved": 3,
    "scores": [
      { "scoreId": 501, "studentId": 1, "grade": "B" },
      { "scoreId": 504, "studentId": 2, "grade": "E" },
      { "scoreId": 503, "studentId": 3, "grade": "C" }
    ]
  }
}
```

---

## 15. Teacher Endpoints — Comments

(Not yet implemented — planned for Phase 3)

### GET /teacher/students/{studentId}/comments?termId={id}

Get all comments for a student in a term.

**Response 200:**
```json
{
  "data": [
    {
      "id": 10,
      "studentId": 1,
      "termId": 1,
      "subjectId": 1,
      "subjectName": "Mathematics",
      "commentType": "SUBJECT",
      "commentText": "Good improvement in algebra this term.",
      "writtenBy": 5,
      "writtenByName": "Mr. Okello",
      "writtenAt": "2026-04-15T10:00:00Z"
    },
    {
      "id": 11,
      "studentId": 1,
      "termId": 1,
      "subjectId": null,
      "subjectName": null,
      "commentType": "CLASS_TEACHER",
      "commentText": "Sarah has been a pleasure to teach.",
      "writtenBy": 6,
      "writtenByName": "Ms. Achieng",
      "writtenAt": "2026-04-16T09:00:00Z"
    }
  ]
}
```

---

### POST /teacher/comments

Create or update a comment. If a comment of the same type/subject/term exists, it's updated.

**Request:**
```json
{
  "studentId": 1,
  "termId": 1,
  "subjectId": 1,
  "commentType": "SUBJECT",
  "commentText": "Good improvement in algebra this term."
}
```

**Response 200** (created or updated comment)

**Errors:**
- `NOT_YOUR_CLASS` (403) — for subject comments

---

### DELETE /teacher/comments/{id}

**Response 204**

---

## 16. Report Endpoints

(Not yet implemented — planned for Phase 4)

### POST /reports/generate

Generate report cards for a class-stream-term.

**Auth:** TEACHER or ADMIN

**Request:**
```json
{
  "classId": 1,
  "streamId": 1,
  "termId": 1
}
```

**Response 200:**
```json
{
  "data": {
    "generated": 42,
    "reports": [
      {
        "studentId": 1,
        "studentName": "Achieng Sarah",
        "reportId": 5001,
        "downloadUrl": "/reports/download/5001"
      }
    ]
  }
}
```

**Errors:**
- `NO_MARKS_PUBLISHED` (422) — some students have no published marks

---

### GET /reports/student/{studentId}?termId={id}

List reports for a student.

**Response 200:**
```json
{
  "data": [
    {
      "id": 5001,
      "studentId": 1,
      "termId": 1,
      "termName": "Term 1",
      "academicYear": 2026,
      "generatedAt": "2026-04-20T15:00:00Z",
      "downloadUrl": "/reports/download/5001"
    }
  ]
}
```

---

### GET /reports/download/{reportId}

Download the PDF.

**Auth:** TEACHER, ADMIN, or the STUDENT who owns it

**Response 200:** binary PDF with header `Content-Type: application/pdf`

---

## 17. Student Endpoints (Web)

(Not yet implemented — planned for Phase 4)

These are called by the student web app. Session-based (cookie).

### GET /student/me

**Auth:** STUDENT (session)

**Response 200:**
```json
{
  "data": {
    "id": 1,
    "admissionNumber": "2024/0456",
    "fullName": "Achieng Sarah",
    "firstName": "Sarah",
    "class": "S3",
    "stream": "Blue",
    "schoolName": "St. Mary's Secondary School"
  }
}
```

---

### GET /student/marks

List published marks for the logged-in student.

**Query parameters:**
- `termId` (optional)
- `subjectId` (optional)

**Response 200:**
```json
{
  "data": [
    {
      "id": 501,
      "subjectId": 1,
      "subjectName": "Mathematics",
      "title": "Mid-Term Exam",
      "type": "EXAM",
      "date": "2026-03-15",
      "score": 82,
      "maxScore": 100,
      "grade": "B"
    },
    {
      "id": 502,
      "subjectId": 2,
      "subjectName": "English",
      "title": "Term 1 Test",
      "type": "TEST",
      "date": "2026-03-20",
      "score": 65,
      "maxScore": 100,
      "grade": "C"
    }
  ]
}
```

---

### GET /student/results

Per-subject summary for the current term.

**Response 200:**
```json
{
  "data": {
    "term": { "id": 1, "name": "Term 1", "year": 2026 },
    "subjects": [
      {
        "subjectId": 1,
        "subjectName": "Mathematics",
        "average": 78.5,
        "grade": "B",
        "assessmentCount": 3
      },
      {
        "subjectId": 2,
        "subjectName": "English",
        "average": 65.0,
        "grade": "C",
        "assessmentCount": 2
      }
    ],
    "overallAverage": 71.75,
    "overallGrade": "B",
    "position": 12,
    "classSize": 42
  }
}
```

---

### GET /student/reports

List report cards available to the student.

**Response 200:**
```json
{
  "data": [
    {
      "id": 5001,
      "termId": 1,
      "termName": "Term 1",
      "academicYear": 2026,
      "generatedAt": "2026-04-20T15:00:00Z",
      "downloadUrl": "/reports/download/5001"
    }
  ]
}
```

---

### GET /student/profile

**Response 200:**
```json
{
  "data": {
    "admissionNumber": "2024/0456",
    "fullName": "Achieng Sarah",
    "gender": "FEMALE",
    "dateOfBirth": "2008-03-15",
    "class": "S3",
    "stream": "Blue",
    "guardianName": "Mrs. Achieng Mary",
    "guardianPhone": "+256700000003"
  }
}
```

---

### POST /student/profile/change-password

**Request:**
```json
{
  "currentPassword": "oldpass",
  "newPassword": "newpass123"
}
```

**Response 204**

---

## 18. Error Codes — Full List

```
INVALID_CREDENTIALS
ACCOUNT_LOCKED
ACCOUNT_INACTIVE
SESSION_EXPIRED
WEAK_PASSWORD

USER_NOT_FOUND
USERNAME_TAKEN
CANNOT_DEACTIVATE_SELF

STUDENT_NOT_FOUND
DUPLICATE_ADMISSION_NUMBER
STUDENT_NOT_ENROLLED
STUDENT_INACTIVE

ASSESSMENT_NOT_FOUND
ASSESSMENT_ALREADY_PUBLISHED
ASSESSMENT_NOT_DRAFT
CANNOT_PUBLISH_EMPTY
ASSESSMENT_TOPIC_REQUIRED

SCORE_NOT_FOUND
SCORE_EXCEEDS_MAX
SCORE_BELOW_MIN
SCORE_REQUIRED
FEEDBACK_TOO_LONG

NOT_YOUR_CLASS
NOT_YOUR_SUBJECT
PERMISSION_DENIED
ADMIN_ONLY

INVALID_IMPORT_FILE
IMPORT_VALIDATION_FAILED
IMPORT_DUPLICATE_ROWS
IMPORT_MISSING_COLUMNS

REPORT_NOT_FOUND
REPORT_ALREADY_GENERATED
NO_MARKS_PUBLISHED

VALIDATION_FAILED
INTERNAL_ERROR
SERVICE_UNAVAILABLE
```

Full descriptions in `ERROR_CODES.md`.

---

## 19. Notes for Frontend Devs

**Desktop app:**
- After login, store the JWT and `expiresAt` in `Session`
- Add `Authorization: Bearer <token>` to every subsequent request
- On `401`, redirect to login
- On `403`, show a permission denied message
- Handle `SCORE_EXCEEDS_MAX` with the `details` field for a helpful error

**Student web:**
- Session handled by cookie — no token needed
- CSRF tokens auto-injected by Spring Security into forms
- On `401`, redirect to `/login`
- On `403`, show access denied page

---

## 20. Notes for Backend Devs

- Every endpoint requires `@Valid` on request bodies
- Every service method validates permission server-side
- Every state change writes to `audit_log`
- Every list endpoint supports pagination
- Every entity response is a DTO, never the raw entity
- Every error uses the standard shape
- Never expose `password_hash` in any response

---

## The One-Sentence Summary

**API_CONTRACT.md is the source of truth for every endpoint — path, method, request, response, errors. Backend implements it exactly. Frontend calls it exactly. Nothing changes without updating both.**

---

