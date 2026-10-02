# Definition of Done

A task is only "done" when ALL criteria below are met. No exceptions.

## Backend Feature

- [ ] Code follows `BACKEND.md` and `STYLE_GUIDE.md`
- [ ] Unit tests written (≥80% coverage on new code)
- [ ] Integration test written if it crosses layers
- [ ] All error cases handled with standard `ErrorCode` values
- [ ] Audit log entry written for state-changing actions
- [ ] Permission check enforced server-side
- [ ] Input validation via `@Valid`
- [ ] API contract updated in `docs/shared/API_CONTRACT.md` if endpoint changed
- [ ] Javadoc on public service methods
- [ ] No hardcoded values (use config)
- [ ] No secrets in code
- [ ] Reviewed and approved by another team member
- [ ] Merged to `main`
- [ ] CI passes

## Frontend — Desktop (JavaFX)

- [ ] Matches `DESIGN_SYSTEM.md` (colors, fonts, spacing)
- [ ] Works at 1280×720 minimum
- [ ] Keyboard navigation works (Tab, Enter, Escape)
- [ ] Empty state implemented
- [ ] Loading state implemented (spinner after 200ms delay)
- [ ] Error state implemented with plain-language message
- [ ] No blocking calls on FX thread
- [ ] Toast notifications for actions
- [ ] Reviewed by another team member
- [ ] Tested on real Windows PC

## Frontend — Web (Student Portal)

- [ ] Matches `DESIGN_SYSTEM.md`
- [ ] Mobile-first (works at 360px)
- [ ] Works on Chrome, Firefox, Safari, Edge
- [ ] Empty state implemented
- [ ] Loading state implemented
- [ ] Error state implemented
- [ ] Keyboard accessible
- [ ] WCAG 2.1 AA contrast
- [ ] Reviewed by another team member
- [ ] Tested on a real phone

## Database

- [ ] Migration file created with correct naming (`V<n>__description.sql`)
- [ ] Tested on fresh database
- [ ] Tested on existing database (no data loss)
- [ ] Indexes added on FKs and search columns
- [ ] Constraints named per `DATABASE.md` §4
- [ ] `SCHEMA.md` updated to match
- [ ] Reviewed by Backend Lead

## Documentation

- [ ] README updated if needed
- [ ] API contract updated if endpoint changed
- [ ] Javadoc added to public methods
- [ ] Complex logic has inline comments
- [ ] User-facing change noted in CHANGELOG

## Security

- [ ] No secrets in code
- [ ] Input validated
- [ ] Permissions enforced server-side
- [ ] No SQL injection risk
- [ ] No XSS risk
- [ ] CSRF protection on forms

## Testing

- [ ] Manual test scenarios passed
- [ ] Edge cases covered (empty, max, invalid)
- [ ] Bug fixes have a regression test
- [ ] CI pipeline green

## Acceptance

- [ ] Product owner reviewed
- [ ] Demo given if significant
- [ ] No known blockers