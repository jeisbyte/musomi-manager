name: Bug report
about: Report a bug
title: '[BUG] '
labels: bug
---

## Description
A clear description of the bug.

## Steps to reproduce
1.
2.
3.

## Expected
What should happen.

## Actual
What actually happens.

## Environment
- Device:
- OS:
- App version:
- User role:

## Severity
- [ ] Critical (data loss, crash, can't log in, security)
- [ ] High (feature broken, no workaround)
- [ ] Medium (partially works, workaround exists)
- [ ] Low (cosmetic)

## Evidence
Screenshots, logs.
EOF

cat > .github/ISSUE_TEMPLATE/feature_request.md << 'EOF'
---
name: Feature request
about: Suggest a feature
title: '[FEAT] '
labels: enhancement
---

## Description
What feature do you want?

## Why
What problem does it solve?

## Acceptance criteria
- [ ]
- [ ]

## Version
- [ ] v1
- [ ] v2a
- [ ] v2b
- [ ] later
