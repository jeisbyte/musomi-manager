# PRODUCT-QA.md — Product & QA Specialist Document

---

## 1. Who This Is For

You are the **Product + QA Lead** on Musomi Manager.

You are the bridge between the team and the pilot school. You make sure what we build actually works for real teachers, real students, and real schools.

You work alongside:
- **Backend Lead** — you test their APIs and services
- **JavaFX Developer** — you test the desktop app teachers use
- **Web Developer** — you test the student portal
- **Database + DevOps** — you verify data integrity and deployments
- **Lead / Founder** — you help prioritize and coordinate with the school

You own testing, documentation, and school coordination. You don't write production code.

---

## 2. What You Do in v1

### Product side
- Coordinate with the pilot school (head teacher, DOS, teachers, students)
- Write acceptance criteria for each feature
- Gather feedback from real users
- Prioritize bugs and features with the Lead
- Write user guides (admin, teacher, student)
- Write training slides
- Prepare demo scripts for the school
- Manage the sprint backlog

### QA side
- Test every feature before it ships
- Write test scenarios
- Test edge cases (empty classes, invalid scores, big imports)
- Test on real devices (Windows PC, Android phone, laptop)
- File clear bug reports
- Do regression testing after new changes
- Run acceptance tests against the pilot school
- Verify the "success test" before calling v1 done

**Not in v1:** automated test frameworks (JUnit is written by devs), performance testing at scale, load testing. Those can wait.

---

## 3. Folder Structure

```
docs/
├── 08-team/
│   ├── product-qa/
│   │   ├── test-plan-v1.md
│   │   ├── test-scenarios/
│   │   │   ├── login.md
│   │   │   ├── mark-entry.md
│   │   │   ├── publish.md
│   │   │   ├── reports.md
│   │   │   ├── student-portal.md
│   │   │   ├── import-students.md
│   │   │   └── admin-settings.md
│   │   ├── bug-template.md
│   │   ├── bug-log.md
│   │   ├── regression-checklist.md
│   │   └── pilot-school-notes.md
│   │
│   └── release-notes/
│       ├── v1.0.0.md
│       └── ...
│
└── 07-operations/
    └── training-materials/
        ├── admin-training.md
        ├── teacher-training.md
        ├── student-guide.md
        └── school-onboarding.md
```

---

## 4. The Product Mindset

You represent the user — not the team.

**The team asks:** "Does it work?"
**You ask:** "Does it work for a teacher who has 50 students and 15 minutes before the next class?"

**The team asks:** "Is the API fast?"
**You ask:** "Can a teacher on a slow school connection enter marks without losing them?"

**The team asks:** "Is the feature complete?"
**You ask:** "Would the DOS actually use this, or would they go back to Excel?"

**You are the voice of the pilot school in every sprint.**

---

## 5. The QA Mindset

Every feature has four states. Test all four.

| State | What to test |
|---|---|
| **Happy path** | Everything works as expected |
| **Empty state** | Nothing exists yet — is there a helpful message? |
| **Error state** | Something goes wrong — is the error clear and useful? |
| **Edge case** | Unusual but valid input — does it still work? |

If you only test the happy path, you've tested 25% of the feature.

---

## 6. What "Done" Means for You

Before you approve a feature as done, ask:

1. **Does it work?** — tested on real devices
2. **Is it clear?** — a teacher can use it without asking
3. **Is it fast?** — no waiting, no blocking
4. **Is it safe?** — data isn't lost, permissions are enforced
5. **Is it documented?** — user guide or in-app help exists
6. **Is it what the school asked for?** — matches the acceptance criteria

If any answer is "no", it's not done.

---

## 7. Test Scenarios for v1

These are the critical flows. Test each one before shipping.

### Scenario 1: Admin sets up a school

**Precondition:** Fresh install, no data.

**Steps:**
1. Log in as admin
2. Set school name, upload logo, configure grading scale
3. Create academic year 2026, set as current
4. Create terms (Term 1, Term 2, Term 3), mark Term 1 as current
5. Create class levels (S1, S2, S3, S4, S5, S6)
6. Create classes (S3, S4) with streams (Blue, Red)
7. Create subjects (Math, English, Biology, Chemistry, Physics)
8. Create topics per subject (Algebra, Geometry for Math)
9. Create teacher accounts (10 teachers)
10. Create teacher assignments (teacher × subject × class)
11. Import 500 students from Excel
12. Create student accounts

**Expected:** All steps succeed. Total time under 1 day.

**Common failures:**
- Excel import rejects valid file
- Duplicate admission numbers
- Missing required fields in template

### Scenario 2: Teacher creates an assessment with topic tags

**Precondition:** Admin setup complete, teacher logged in.

**Steps:**
1. Teacher opens "My Classes"
2. Clicks "Create Assessment" for S3 Blue Mathematics
3. Fills in title "Mid-Term Exam"
4. Selects type "EXAM"
5. Sets date and max score 100
6. Tags topics (Algebra, Geometry)
7. Saves

**Expected:** Assessment created as DRAFT. Appears in teacher's assessment list.

**Common failures:**
- Can create assessment without tags
- Topics are for the wrong subject

### Scenario 3: Teacher enters marks for a class

**Precondition:** Assessment exists as DRAFT.

**Steps:**
1. Teacher opens the assessment
2. Grid appears with all S3 Blue students
3. Enters score for each student using keyboard only
4. Presses Tab to move to next cell
5. Presses Enter to commit and move down
6. Enters feedback for a few students
7. Watches "Saved" indicator appear after each commit

**Expected:** Auto-save after every commit. No data loss. Grade updates live. Entered count shows at bottom.

**Common failures:**
- Auto-save fails silently
- Tab doesn't move to next cell
- Score accepted above max

### Scenario 4: Teacher publishes marks

**Precondition:** All students have scores.

**Steps:**
1. Teacher clicks "Publish"
2. Confirmation modal appears: "Publish marks? Students will see them immediately."
3. Teacher confirms
4. Status badge changes from DRAFT to PUBLISHED

**Expected:** Marks visible to students immediately. Students notified (when notifications exist in v2a).

**Common failures:**
- Can publish with empty scores
- No confirmation
- Students can't see marks after publishing

### Scenario 5: Student views marks on phone

**Precondition:** Marks published.

**Steps:**
1. Student opens `https://schoolname.musomi.app` on Android phone
2. Logs in with admission number + password
3. Taps "My Marks"
4. Sees list of published assessments grouped by subject
5. Taps a subject to see details

**Expected:** Page loads in under 2 seconds on 3G. All text readable. Navigation works with thumb.

**Common failures:**
- Page too wide for phone
- Bottom nav overlaps content
- Login rejects valid credentials

### Scenario 6: Student downloads report card

**Precondition:** Report generated by admin.

**Steps:**
1. Student logs in
2. Taps "Reports"
3. Sees list of generated reports
4. Taps download
5. PDF opens in browser viewer

**Expected:** PDF is professional, includes logo, marks, comments, position, signatures.

**Common failures:**
- PDF missing school logo
- Position calculation wrong
- Comments not showing

### Scenario 7: Admin imports students from Excel

**Precondition:** Template downloaded and filled.

**Steps:**
1. Admin opens "Import Students"
2. Uploads Excel file with 500 students
3. System shows preview with validation results
4. Fixes errors (if any)
5. Confirms import
6. Success message shows 500 imported

**Expected:** All valid students imported. Invalid rows highlighted with reason. Time under 60 seconds.

**Common failures:**
- Missing columns not reported
- Duplicate admission numbers not flagged
- Partial import leaves inconsistent state

### Scenario 8: Admin resets a teacher's password

**Precondition:** Teacher exists.

**Steps:**
1. Admin opens user list
2. Selects teacher
3. Clicks "Reset Password"
4. System generates new temp password
5. Admin copies and shares with teacher

**Expected:** New password works. Old password no longer works. Reset is logged in audit log.

**Common failures:**
- Password not generated
- Old password still works
- No audit log entry

---

## 8. Test Devices

Every feature gets tested on:

| Device | OS | Browser | Purpose |
|---|---|---|---|
| Windows PC (main) | Windows 11 | — | Desktop app for teachers |
| Windows PC (old) | Windows 10 | — | Desktop on older hardware |
| Android phone | Android 10+ | Chrome | Student portal |
| Android phone (old) | Android 8 | Chrome | Low-end device |
| iPhone | iOS 14+ | Safari | Student portal |
| Laptop | Windows/macOS | Chrome, Firefox, Edge | Web app |

**If you only test on one device, you've tested nothing.**

---

## 9. Bug Reporting Format

Every bug gets reported in this exact format.

```markdown
## Bug #001

**Title:** Mark entry grid loses data when switching tabs

**Reported by:** [your name]
**Date:** 2026-09-24
**Severity:** Critical / High / Medium / Low

**Environment:**
- Device: Windows 11 laptop
- App version: v1.0.0-beta
- Steps from a fresh login

**Steps to reproduce:**
1. Log in as teacher
2. Open assessment for S3 Blue Mathematics
3. Enter a score in row 3
4. Switch to another window (Alt+Tab)
5. Switch back to the app

**Expected:** Score is saved.

**Actual:** Score disappears.

**Frequency:** Always / Sometimes / Once

**Evidence:**
- Screenshot: [attach]
- Log: [attach if available]

**Impact:**
Teacher loses work. Trust broken. Must fix before pilot.

**Notes:**
Happens with Tab also. Auto-save indicator doesn't appear.
```

**Severity guide:**
- **Critical** — data loss, crash, can't log in, security
- **High** — feature broken, no workaround
- **Medium** — feature partially works, workaround exists
- **Low** — cosmetic, minor annoyance

**Rule:** Critical bugs block the release. High bugs should be fixed before pilot. Medium can be scheduled. Low can wait.

---

## 10. Regression Checklist

After every code change, re-test these. If any breaks, the change broke something.

- [ ] Login works for admin, teacher, student
- [ ] Logout clears session
- [ ] Create assessment with topics
- [ ] Enter marks (50 students in under 10 minutes)
- [ ] Publish marks
- [ ] Student sees published marks
- [ ] Student cannot see other students' marks
- [ ] Generate report card
- [ ] Download report card
- [ ] Excel import works
- [ ] Audit log records key actions
- [ ] App works after server restart

**This list grows as features ship. It never shrinks.**

---

## 11. Pilot School Coordination

You are the main contact with the pilot school.

### Before pilot
- Confirm pilot school (name, head teacher, DOS)
- Confirm term dates
- Schedule setup day
- Schedule teacher training
- Schedule student orientation
- Prepare training materials
- Confirm support contact (you)

### During setup day
- Install server on school network
- Import students
- Create teacher accounts
- Walk through admin setup with the school admin
- Test the whole system end-to-end on-site

### During first week
- Be on call for questions
- Visit school at least twice
- Watch teachers use the system
- Take notes on friction points
- Fix critical bugs immediately

### After each term
- Collect feedback
- Interview 3 teachers, 3 students, 1 admin
- Document what worked and what didn't
- Feed into next version planning

---

## 12. User Documentation

You write these in v1:

### Admin Guide
- How to set up the school
- How to create users
- How to import students
- How to configure grading
- How to generate reports
- How to reset passwords

### Teacher Guide
- How to log in
- How to create an assessment with topics
- How to enter marks (with keyboard shortcuts)
- How to publish marks
- How to add comments

### Student Guide
- How to log in
- How to view marks
- How to download reports
- How to change password

**Rules:**
- Plain English, no jargon
- Step-by-step with screenshots
- One concept per section
- Under 10 pages each
- Test by having a real teacher follow it

---

## 13. Common Prompts for Product/QA AI

**Write test scenarios:**

```
Context: MASTER.md + PRODUCT-QA.md
Feature: [name]
API contract: [paste endpoint]

Write test scenarios covering:
- Happy path
- Empty state
- Error state
- Edge cases

Follow the test scenario format in PRODUCT-QA.md.
```

**Write a bug report:**

```
Context: PRODUCT-QA.md

Bug: [description]
Steps: [steps]
Expected: [expected]
Actual: [actual]

Format as a bug report using the template in PRODUCT-QA.md.
Determine severity based on the impact.
```

**Write user documentation:**

```
Context: MASTER.md + PRODUCT-QA.md
Feature: [name]
Audience: [admin / teacher / student]

Write a step-by-step guide in plain English.
Include screenshots placeholders.
Under 10 pages.
Follow the guide pattern in PRODUCT-QA.md.
```

**Write a regression checklist:**

```
Context: PRODUCT-QA.md

Write a regression checklist for v1.
Cover: all critical flows.
Format as checkboxes.
Follow the pattern in PRODUCT-QA.md.
```

**Write training slides:**

```
Context: MASTER.md + PRODUCT-QA.md
Audience: [admin / teacher / student]
Duration: [minutes]
Topics: [list]

Write slide outlines with:
- Title
- Key points (3-5 per slide)
- Demo notes
- Practice exercise

Follow the training pattern in PRODUCT-QA.md.
```

**Review a feature before release:**

```
Context: PRODUCT-QA.md
Feature: [name]

Review against:
- Acceptance criteria
- Empty state
- Error state
- Edge cases
- Mobile (if web)

Return: pass/fail per criterion, with notes.
```

---

## 14. Definition of Done (Product/QA)

A feature is ready to ship when:

- [ ] All test scenarios pass
- [ ] Empty state works
- [ ] Error state works with clear message
- [ ] Edge cases tested
- [ ] Tested on real devices (desktop and phone)
- [ ] Regression checklist passes
- [ ] User documentation written
- [ ] Screenshots captured for training
- [ ] Pilot school feedback incorporated (if applicable)
- [ ] Bug log has no critical or high bugs open
- [ ] Approved by you and the Lead

---

## 15. What NOT to Do

- Don't test only the happy path — test all four states
- Don't test only on your laptop — test on real devices
- Don't report a bug without steps to reproduce — devs can't fix what they can't find
- Don't report "it's broken" — say what's broken, when, and how
- Don't skip regression testing — you'll ship a regression
- Don't assume the school will adapt — watch them use it, adjust the product
- Don't let developers mark their own work as done — verify it yourself
- Don't ignore low-severity bugs — they accumulate
- Don't write documentation for developers — write it for teachers
- Don't skip the pilot school visits — remote support doesn't catch real problems
- Don't promise features that aren't in v1 — manage expectations
- Don't approve a release with data-loss bugs — ever

---

## 16. Weekly Rhythm

### Monday
- Review bugs from the weekend
- Prioritize with the Lead
- Plan the week's testing

### Tuesday–Thursday
- Test in progress features
- Coordinate with the pilot school
- Write documentation

### Friday
- Regression testing on the week's changes
- Update bug log
- Prepare demo for weekly review
- Update test scenarios

### End of each month
- Full regression pass
- Pilot school update
- Bug log review with the Lead
- Next month's testing plan

---

## 17. What You Approve Before Release

Before any release reaches the pilot school, you confirm:

- [ ] All critical bugs fixed
- [ ] All high bugs fixed or scheduled with a plan
- [ ] Regression checklist passes
- [ ] User documentation updated
- [ ] Training materials updated
- [ ] Release notes written
- [ ] Pilot school notified
- [ ] Support contact confirmed
- [ ] Backup verified (with DevOps)
- [ ] Rollback plan confirmed

**No release ships without your approval.**

---

## 18. Reference Documents

- `MASTER.md` — project context (paste first)
- `docs/02-requirements/v1-requirements.md` — what v1 must do
- `docs/02-requirements/v1-acceptance-criteria.md` — what "done" means
- `docs/specialists/BACKEND.md` — for testing backend features
- `docs/specialists/DESKTOP.md` — for testing the desktop app
- `docs/specialists/WEB.md` — for testing the student portal
- `docs/specialists/DATABASE.md` — for testing data integrity
- `docs/specialists/DEVOPS.md` — for testing deployments

---

## The One-Sentence Summary

**You are the Product + QA Lead on Musomi Manager. You represent the pilot school in every sprint. You test every feature on real devices across all four states (happy, empty, error, edge). You write user documentation in plain English. You coordinate directly with teachers, students, and admin. No release ships without your approval. Paste MASTER.md and PRODUCT-QA.md into every AI session.**

---

**That's PRODUCT-QA.md.**

That's all six specialist documents done:

1. **BACKEND.md** ✅
2. **DESKTOP.md** ✅
3. **WEB.md** ✅
4. **DATABASE.md** ✅
5. **DEVOPS.md** ✅
6. **PRODUCT-QA.md** ✅

---

## What Comes Next

Now the specialist documents exist. But they reference five shared documents that don't exist yet:

| Document | Purpose |
|---|---|
| **API_CONTRACT.md** | Every endpoint, request, response |
| **SCHEMA.md** | Every table, column, relationship |
| **STYLE_GUIDE.md** | Naming rules in more detail |
| **ERROR_CODES.md** | Standard error catalog |
| **DESIGN_SYSTEM.md** | Colors, fonts, components in detail |

These are what every specialist pastes when needed.


