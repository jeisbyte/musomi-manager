---
name: Feature request
about: Propose a new feature or enhancement
title: '[FEAT] '
labels: enhancement
assignees: ''
---

<!--
Before filling this out, check docs/master/MASTER.md section 3 (Version Status)
and section 4 (v1 Scope).

If the feature is NOT in v1, mark it as a later version below.
Do NOT propose v2+ work as v1 work.
-->

## Summary

One sentence describing the feature.

Example: "Allow teachers to duplicate a previous assessment instead of re-entering topics."

---

## Problem

What problem does this solve? Who has it?

Describe the real-world scenario. Be specific about the user and the moment they hit the problem.

Example:
> A teacher creates 3–5 assessments per term per class. Each one requires re-selecting the same topics. With 6 classes, that's 30+ repeated selections per term. It's slow and error-prone.

---

## Proposed solution

How should it work? Describe the behaviour, not the implementation.

Example:
> On the assessment list, add a "Duplicate" action. It opens the create form pre-filled with the same class, stream, subject, term, type, max score, and topics. The teacher only changes the title and date.

---

## User story

As a **[role]**, I want to **[action]** so that **[benefit]**.

Example:
> As a **teacher**, I want to **duplicate a previous assessment** so that **I don't have to re-enter the same topics every time**.

---

## Acceptance criteria

What must be true for this to be considered done?

- [ ] Criterion 1
- [ ] Criterion 2
- [ ] Criterion 3

Example:
- [ ] A "Duplicate" button appears on the assessment list for DRAFT and PUBLISHED assessments.
- [ ] Clicking it opens the create form with all fields pre-filled except title and date.
- [ ] The new assessment is created as DRAFT.
- [ ] Topics are copied from the source assessment.
- [ ] The action is logged in the audit log as `ASSESSMENT_DUPLICATED`.
- [ ] A toast confirms success.
- [ ] Works for both ADMIN and TEACHER roles (teacher must own the source assessment).

---

## Version

Which version does this belong to? See `docs/master/MASTER.md` section 3.

- [ ] v1 — Core Operations (marks + topic tagging + reports)
- [ ] v2a — Daily Usability (offline + notifications + UX)
- [ ] v2b — Wider Access (parents + sharing + messaging)
- [ ] v3 — Insights (analytics on topic data)
- [ ] v4 — Intelligence (AI narratives, recommendations)
- [ ] v5 — Scale & Fairness (multi-school, billing, equity)
- [ ] v6 — Full Vision (career maps, advanced AI)
- [ ] Not sure — needs discussion

**If this is not v1, stop here.** Open it as a discussion or backlog item, not a v1 feature request.

---

## Scope check

Confirm this request does not violate v1 rules from `MASTER.md` section 4:

- [ ] This is not an AI feature (v4+)
- [ ] This is not analytics or charts (v3+)
- [ ] This is not a parent portal (v2b+)
- [ ] This is not offline mode (v2a+)
- [ ] This is not teacher-to-teacher sharing (v2b+)
- [ ] This is not notifications or messaging (v2a+)
- [ ] This is not multi-school management (v5+)
- [ ] This is not custom roles or fine-grained permissions (v5+)
- [ ] This is not equity disaggregation (v5+)
- [ ] This is not self/peer assessment (v5+)
- [ ] This is not career guidance (v6+)
- [ ] This is not two-factor authentication (later)
- [ ] This is not billing (v5+)

If you ticked any of the above, this is a **later-version** feature. Reassign accordingly.

---

## Affected layers

Which parts of the system does this touch?

- [ ] Database — new table or column (`docs/shared/SCHEMA.md`)
- [ ] Backend API — new or changed endpoint (`docs/shared/API_CONTRACT.md`)
- [ ] Backend service — business logic
- [ ] Desktop app (JavaFX) — teacher/admin UI
- [ ] Web app (Thymeleaf) — student portal
- [ ] Reports — PDF generation
- [ ] DevOps — deployment, backup, monitoring
- [ ] Documentation only

---

## Design impact

Does this require UI work?

- [ ] No UI change
- [ ] Follows existing `docs/shared/DESIGN_SYSTEM.md` components
- [ ] Needs a new component (describe below)

If new component:
> Describe it. Include which existing components it's similar to, and why the existing ones don't work.

---

## API impact

Does this need a new or changed endpoint?

- [ ] No API change
- [ ] New endpoint (describe below)
- [ ] Changed endpoint (describe below)

If yes:
```
METHOD /path
Request:  { ... }
Response: { ... }
Errors:   [list from ERROR_CODES.md]
```

---

## Data impact

Does this need a schema change?

- [ ] No schema change
- [ ] New table
- [ ] New column(s) on existing table
- [ ] New index or constraint
- [ ] Migration required

If yes, note it here. The Database Engineer will update `docs/shared/SCHEMA.md` before any migration is written.

---

## Alternatives considered

What other approaches did you consider, and why did you reject them?

Example:
> Could implement as "save as template" but that adds a new concept. Duplication is simpler and matches how teachers already think.

---

## Out of scope

What is explicitly NOT part of this request?

Example:
> Does not include duplicating scores. Does not include duplicating comments. Does not include cross-term duplication.

---

## Priority

- [ ] Critical — blocks pilot school go-live
- [ ] High — needed for v1 success test
- [ ] Medium — improves v1 but not blocking
- [ ] Low — nice to have
- [ ] Backlog — for a later version

---

## Requested by

- **Person / role:**
- **School / stakeholder:** (if from pilot school)
- **Date requested:**

---

## Additional context

Screenshots, sketches, links, references to related issues or docs.

---

## Checklist before submitting

- [ ] I read `docs/master/MASTER.md` section 3 (Version Status) and section 4 (v1 Scope)
- [ ] I checked whether this already exists as an issue
- [ ] I confirmed this belongs to the version I selected
- [ ] I described the problem, not just the solution
- [ ] I listed measurable acceptance criteria
- [ ] I noted which layers this touches
- [ ] I marked what is out of scope