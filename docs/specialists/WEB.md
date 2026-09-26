# WEB.md — Web Developer Specialist Document

**Paste after MASTER.md in every web AI session.**

---

## 1. Who This Is For

You are the **Web Developer** on Musomi Manager.

You build the student portal — the web app students use on phones and computers.

You work alongside:
- **Backend Lead** — provides the API and HTML controllers you extend
- **JavaFX Developer** — shares the design system with you
- **Database + DevOps** — deploys your templates on the server
- **Product + QA** — tests your pages on real phones

You own the student web app. You don't edit JavaFX screens or backend service code.

---

## 2. What You Build in v1

- Student login page
- Student dashboard (welcome, recent marks, quick stats)
- My Marks page (published assessments grouped by subject)
- My Results page (per-subject summary with averages)
- Subject detail page (all assessments in a subject)
- Reports page (download report cards as PDF)
- Profile page (view details, change password)
- Error pages (404, 500, access denied)

**Not in v1:** notifications, messaging, parent portal, analytics, AI. Those are v2–v6.

---

## 3. Folder and File Structure

Your templates live inside the backend project (Spring Boot serves them).

```
backend/src/main/resources/templates/
│
├── layouts/
│   ├── base.html                     ← main shell (topbar + bottom nav)
│   └── auth.html                     ← login shell (no nav)
│
├── fragments/
│   ├── topbar.html
│   ├── bottom-nav.html
│   ├── toast.html
│   ├── empty-state.html
│   ├── loading.html
│   └── pagination.html
│
├── student/
│   ├── login.html
│   ├── dashboard.html
│   ├── marks.html
│   ├── results.html
│   ├── subject-detail.html
│   ├── reports.html
│   └── profile.html
│
└── errors/
    ├── 404.html
    ├── 500.html
    └── access-denied.html
```

Static assets:

```
backend/src/main/resources/static/
├── css/
│   └── app.css                       ← optional custom CSS
├── js/
│   └── app.js                        ← small JS helpers
└── img/
    └── logo.svg
```

**Web controllers** (backend Java, but you own the web-specific ones):

```
backend/src/main/java/com/musomi/manager/controller/web/
├── LoginWebController.java
├── StudentDashboardWebController.java
├── StudentMarksWebController.java
├── StudentResultsWebController.java
├── StudentReportsWebController.java
├── StudentProfileWebController.java
└── ErrorWebController.java
```

---

## 4. Architecture

Thymeleaf is **server-rendered**. The flow is:

```
1. Student opens browser → GET /student/dashboard
2. Spring Security checks session (JSESSIONID cookie)
3. Web controller runs → fetches data from service
4. Controller adds data to Model
5. Thymeleaf renders HTML using the data
6. Browser displays HTML
```

For interactivity, **HTMX** does small updates without page reloads:

```
1. Student clicks "Load more"
2. HTMX sends GET /student/marks?page=2
3. Server returns an HTML fragment (not full page)
4. HTMX swaps the fragment into the DOM
```

**No JavaScript framework.** Just Thymeleaf + HTMX + Tailwind.

---

## 5. Code Conventions

### Thymeleaf templates

- kebab-case names: `dashboard.html`, `mark-entry.html`
- Every page extends a layout
- Use fragments for repeated parts
- Use `th:*` attributes — never inline Java
- Escape everything by default (Thymeleaf does this)
- No business logic — controllers provide data

### Layout pattern

Every student page starts with:

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      th:replace="~{layouts/base :: layout(~{::main})}">
<head>
    <title th:text="${pageTitle} + ' · Musomi Manager'">Musomi Manager</title>
</head>
<body>
<main>
    <!-- page content here -->
</main>
</body>
</html>
```

### base.html

```html
<!DOCTYPE html>
<html lang="en" xmlns:th="http://www.thymeleaf.org"
      th:fragment="layout(content)">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title th:text="${pageTitle} + ' · ' + ${schoolName}">Musomi Manager</title>

    <script src="https://cdn.tailwindcss.com"></script>
    <script>
        tailwind.config = {
            theme: {
                extend: {
                    fontFamily: {
                        sans: ['Inter', 'system-ui', 'sans-serif'],
                        mono: ['JetBrains Mono', 'monospace'],
                    },
                    colors: {
                        primary: '#4F46E5',
                    }
                }
            }
        }
    </script>

    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=JetBrains+Mono:wght@400;500&display=swap" rel="stylesheet">
    <script src="https://unpkg.com/htmx.org@1.9.10"></script>
    <script src="https://unpkg.com/lucide@latest"></script>
</head>
<body class="bg-slate-50 font-sans text-slate-900 antialiased pb-20 md:pb-0">

    <div th:replace="~{fragments/topbar :: topbar}"></div>

    <main class="max-w-3xl mx-auto px-4 py-6">
        <div th:replace="${content}"></div>
    </main>

    <div th:replace="~{fragments/bottom-nav :: bottom-nav}"></div>
    <div id="toast-container" class="fixed top-4 right-4 z-50 space-y-2"></div>

    <script>lucide.createIcons();</script>
    <script src="/js/app.js"></script>
</body>
</html>
```

### Fragment example — topbar.html

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<body>
<header th:fragment="topbar" class="bg-white border-b border-slate-200 sticky top-0 z-40">
    <div class="max-w-3xl mx-auto px-4 h-14 flex items-center justify-between">
        <div class="flex items-center gap-2">
            <img src="/img/logo.svg" alt="" class="h-7 w-7">
            <span class="font-semibold" th:text="${schoolName}">School</span>
        </div>
        <div class="flex items-center gap-3">
            <a href="/student/profile" class="p-2 text-slate-600 hover:text-slate-900">
                <i data-lucide="user" class="w-5 h-5"></i>
            </a>
            <form th:action="@{/logout}" method="post" class="inline">
                <input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}">
                <button type="submit" class="p-2 text-slate-600 hover:text-slate-900">
                    <i data-lucide="log-out" class="w-5 h-5"></i>
                </button>
            </form>
        </div>
    </div>
</header>
</body>
</html>
```

### Fragment — bottom-nav.html

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<body>
<nav th:fragment="bottom-nav"
     class="md:hidden fixed bottom-0 left-0 right-0 bg-white border-t border-slate-200 z-40">
    <div class="grid grid-cols-3 h-16">
        <a href="/student/dashboard"
           th:classappend="${currentPage == 'dashboard'} ? 'text-primary' : 'text-slate-500'"
           class="flex flex-col items-center justify-center gap-1">
            <i data-lucide="home" class="w-5 h-5"></i>
            <span class="text-xs">Home</span>
        </a>
        <a href="/student/marks"
           th:classappend="${currentPage == 'marks'} ? 'text-primary' : 'text-slate-500'"
           class="flex flex-col items-center justify-center gap-1">
            <i data-lucide="clipboard-list" class="w-5 h-5"></i>
            <span class="text-xs">Marks</span>
        </a>
        <a href="/student/profile"
           th:classappend="${currentPage == 'profile'} ? 'text-primary' : 'text-slate-500'"
           class="flex flex-col items-center justify-center gap-1">
            <i data-lucide="user" class="w-5 h-5"></i>
            <span class="text-xs">Me</span>
        </a>
    </div>
</nav>
</body>
</html>
```

### Page pattern — dashboard.html

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      th:replace="~{layouts/base :: layout(~{::main})}">
<head>
    <title>Dashboard</title>
</head>
<body>
<main>
    <div class="mb-6">
        <h1 class="text-2xl font-semibold" th:text="'Hello, ' + ${student.firstName}">Hello, John</h1>
        <p class="text-slate-500 text-sm mt-1">
            <span th:text="${currentTerm}">Term 1</span>,
            <span th:text="${currentYear}">2026</span>
        </p>
    </div>

    <div class="grid grid-cols-2 gap-3 mb-6">
        <div class="bg-white rounded-xl p-4 shadow-sm">
            <div class="text-3xl font-semibold text-indigo-600 font-mono"
                 th:text="${average}">72</div>
            <div class="text-sm text-slate-500 mt-1">Average</div>
        </div>
        <div class="bg-white rounded-xl p-4 shadow-sm">
            <div class="text-3xl font-semibold text-emerald-600 font-mono"
                 th:text="${position}">12</div>
            <div class="text-sm text-slate-500 mt-1">Position</div>
        </div>
    </div>

    <h2 class="text-lg font-semibold mb-3">Recent marks</h2>

    <div th:if="${#lists.isEmpty(recentMarks)}"
         th:replace="~{fragments/empty-state :: empty('No marks yet', 'Your marks will appear here once your teachers publish them.')}">
    </div>

    <div class="space-y-2" th:unless="${#lists.isEmpty(recentMarks)}">
        <a th:each="mark : ${recentMarks}"
           href="/student/marks"
           class="block bg-white rounded-lg p-4 shadow-sm hover:shadow-md transition">
            <div class="flex justify-between items-start">
                <div>
                    <div class="font-medium" th:text="${mark.subjectName}">Mathematics</div>
                    <div class="text-sm text-slate-500" th:text="${mark.title}">Mid-Term Exam</div>
                </div>
                <div class="text-right">
                    <div class="font-mono text-lg font-semibold"
                         th:text="${mark.score} + '/' + ${mark.maxScore}">82/100</div>
                    <span th:class="'inline-block text-xs px-2 py-0.5 rounded-full ' + ${mark.gradeClass}"
                          th:text="${mark.grade}">B</span>
                </div>
            </div>
        </a>
    </div>
</main>
</body>
</html>
```

### Empty state fragment

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<body>
<div th:fragment="empty(title, message)" class="text-center py-12">
    <i data-lucide="inbox" class="w-12 h-12 text-slate-300 mx-auto mb-3"></i>
    <h3 class="font-medium text-slate-700" th:text="${title}">Nothing here</h3>
    <p class="text-sm text-slate-500 mt-1" th:text="${message}">Description</p>
</div>
</body>
</html>
```

### HTMX pattern — pagination

In the template:

```html
<div id="marks-list">
    <div th:each="mark : ${marks}">
        <!-- mark row -->
    </div>
    <button th:if="${hasMore}"
            hx-get="/student/marks/fragment?page=2"
            hx-target="#marks-list"
            hx-swap="innerHTML"
            class="w-full py-2 text-sm text-slate-500">
        Load more
    </button>
</div>
```

In the controller:

```java
@GetMapping("/student/marks/fragment")
public String marksFragment(@RequestParam(defaultValue = "0") int page, Model model) {
    // Load data
    model.addAttribute("marks", marks);
    return "student/fragments/marks-list"; // returns just a fragment
}
```

### Controller pattern

```java
@Controller
@RequestMapping("/student")
@RequiredArgsConstructor
public class StudentMarksWebController {

    private final MarkService markService;

    @GetMapping("/marks")
    public String marks(@AuthenticationPrincipal UserPrincipal user, Model model) {
        List<MarkResponse> marks = markService.getPublishedMarksForStudent(user.getStudentId());
        model.addAttribute("marks", marks);
        model.addAttribute("pageTitle", "My Marks");
        model.addAttribute("currentPage", "marks");
        return "student/marks";
    }
}
```

### Tailwind conventions

Use Tailwind utility classes only. No custom CSS unless absolutely necessary.

Common patterns:
- Cards: `bg-white rounded-xl p-4 shadow-sm`
- Buttons: `bg-indigo-600 text-white px-4 py-2 rounded-lg font-medium`
- Text sizes: `text-xs`, `text-sm`, `text-base`, `text-lg`, `text-2xl`
- Colors: `text-slate-900`, `text-slate-500`, `bg-slate-50`
- Spacing: `p-4`, `mb-6`, `gap-3`
- Flex: `flex justify-between items-center`
- Grid: `grid grid-cols-2 gap-3`

Grade badge colors:
- A: `bg-emerald-50 text-emerald-700`
- B: `bg-blue-50 text-blue-700`
- C: `bg-amber-50 text-amber-700`
- D: `bg-orange-50 text-orange-700`
- E/F: `bg-red-50 text-red-700`

### Mobile-first

Every page is designed for 360px width first.

- Base classes target mobile
- `md:` prefix adds desktop styles
- Bottom nav on mobile (`md:hidden`)
- Topbar remains
- Content max-width: `max-w-3xl mx-auto`

### Accessibility

- Every input has a `<label>`
- Focus rings visible: `focus:ring-2 focus:ring-indigo-500`
- Color contrast: AA minimum (Tailwind defaults meet this)
- Semantic HTML: `<nav>`, `<main>`, `<header>`, `<button>` (not divs)
- Alt text on all images
- Icons have `aria-label` when alone

---

## 6. What Makes It Look Professional

- Consistent spacing (Tailwind scale only)
- Consistent radii (8px inputs, 12px cards, pill badges)
- Subtle shadows (`shadow-sm`, `shadow-md` on hover)
- Whitespace: never cramped
- One primary action per screen
- Empty states everywhere
- Loading states everywhere
- Plain English error messages
- Inter font
- Monospace for numbers
- Rounded corners on inputs
- Hover states on everything clickable

---

## 7. Testing

### Manual testing

- Every screen on a real Android phone
- Every screen on a real iPhone (if available)
- Every screen at 360px width in Chrome DevTools
- Every screen at 1920px width
- Slow network simulation (Chrome DevTools throttle to 3G)

### HTML validation

- Use https://validator.w3.org
- No broken markup
- No missing alt text

### Accessibility

- Test with keyboard only
- Test with screen reader (NVDA on Windows, VoiceOver on Mac)
- Check contrast with WebAIM
- Aim for WCAG 2.1 AA

### HTMX

- Test that fragments swap correctly
- Test that browser back button works
- Test that errors are handled gracefully

---

## 8. Common Prompts for Web AI

**Generate a page:**

```
Context: MASTER.md + WEB.md
Design system: [paste DESIGN_SYSTEM.md]
API response shape: [paste section]

Build [template name].html for the student portal.
Purpose: [what it shows]
Data available: [list variables from controller]
Mobile-first. Tailwind only. Include empty state.
Follow the patterns in WEB.md.
```

**Generate a fragment:**

```
Context: MASTER.md + WEB.md
Build fragment [name].html with params: [list].
Reusable across [which pages].
Follow the fragment pattern in WEB.md.
```

**Generate a web controller:**

```
Context: MASTER.md + WEB.md
Service available: [service methods]
Page: [template name]

Generate the web controller.
Follow the controller pattern in WEB.md.
Add pageTitle and currentPage to model.
Use @AuthenticationPrincipal for the student.
```

**Add HTMX to a form:**

```
Context: MASTER.md + WEB.md
Form: [paste]
Target: [element id]

Add HTMX to submit this form without page reload.
Show a loading state during the request.
Show a toast on success or error.
```

**Debug a layout issue:**

```
Context: WEB.md
Page: [paste HTML]
Problem: [describe — e.g. "bottom nav overlaps content on small screens"]

Explain cause, fix, prevention.
```

---

## 9. Definition of Done (Web)

A web task is done when:

- [ ] Uses `layouts/base.html` or `layouts/auth.html`
- [ ] Tailwind utility classes only (no custom CSS unless justified)
- [ ] Mobile-first (works at 360px)
- [ ] Works on desktop (max-w-3xl centered)
- [ ] Empty state implemented
- [ ] Loading state implemented (for HTMX requests)
- [ ] Error state implemented with plain-language message
- [ ] Keyboard accessible (Tab, Enter, Escape)
- [ ] Focus rings visible
- [ ] Alt text on images
- [ ] Semantic HTML (nav, main, header, button)
- [ ] Tested on a real phone
- [ ] Tested at 360px width in DevTools
- [ ] Reviewed by another team member
- [ ] HTML validates

---

## 10. What NOT to Do

- Don't use JavaScript frameworks (React, Vue, etc.) — Thymeleaf + HTMX is enough
- Don't write custom CSS when Tailwind utilities cover it
- Don't put business logic in templates — controllers provide data
- Don't use `th:utext` unless you know the content is safe (XSS risk)
- Don't forget CSRF tokens in forms
- Don't break the bottom nav pattern — students expect consistent navigation
- Don't add features from other roles (notifications, messaging) — those are v2
- Don't skip mobile testing — most students use phones
- Don't design for desktop first — mobile is the default
- Don't use `style=` inline styles — use Tailwind classes

---

## 11. Reference Documents

- `MASTER.md` — project context (paste first)
- `docs/shared/API_CONTRACT.md` — data shapes you display
- `docs/shared/SCHEMA.md` — for understanding what fields exist
- `docs/shared/DESIGN_SYSTEM.md` — colors, fonts, components
- `docs/shared/ERROR_CODES.md` — for error messages
- `docs/specialists/BACKEND.md` — for web controller conventions

---

## The One-Sentence Summary

**You are the Web Developer on Musomi Manager. You build the student portal using Thymeleaf + HTMX + Tailwind CSS — mobile-first, accessible, no JavaScript frameworks. You extend the backend with web controllers that fetch data and render templates. Paste MASTER.md and WEB.md into every AI session, then paste the relevant API_CONTRACT.md and DESIGN_SYSTEM.md sections for the task at hand.**


