# DESIGN_SYSTEM.md — Shared Document

---

## 1. How to Use This Document

- **JavaFX Developer:** paste alongside DESKTOP.md
- **Web Developer:** paste alongside WEB.md
- **Both must produce screens that look like the same product**
- **Never invent colors, fonts, or spacing outside this document**
- **Never change the design system without team agreement**

A teacher using the desktop app and a student using the web app should both feel like they're inside the same product.

---

## 2. Design Principles

1. **Clarity over decoration** — every element has a purpose
2. **Density where needed, breathing room elsewhere** — mark entry is dense; dashboards are spacious
3. **Consistency above cleverness** — same button, same color, same behaviour, everywhere
4. **Accessible by default** — WCAG 2.1 AA minimum
5. **Fast feels professional** — no long animations, no blocking
6. **Mobile-first for web, keyboard-first for desktop**
7. **Restrained palette** — one primary color, semantic accents

---

## 3. Colors

### Primary palette

| Name | Hex | Tailwind | Use |
|---|---|---|---|
| Primary | `#4F46E5` | `indigo-600` | Primary buttons, active nav, links |
| Primary Hover | `#4338CA` | `indigo-700` | Button hover |
| Primary Light | `#EEF2FF` | `indigo-50` | Selected row, active nav bg |

### Semantic colors

| Name | Hex | Tailwind | Use |
|---|---|---|---|
| Success | `#059669` | `emerald-600` | Published, pass, success |
| Success Light | `#ECFDF5` | `emerald-50` | Success badge bg |
| Warning | `#F59E0B` | `amber-500` | Draft, attention |
| Warning Light | `#FFFBEB` | `amber-50` | Warning badge bg |
| Danger | `#DC2626` | `red-600` | Errors, delete, fail |
| Danger Light | `#FEF2F2` | `red-50` | Error badge bg |
| Info | `#2563EB` | `blue-600` | Info messages |
| Info Light | `#EFF6FF` | `blue-50` | Info badge bg |

### Neutrals

| Name | Hex | Tailwind | Use |
|---|---|---|---|
| Background | `#F8FAFC` | `slate-50` | Page background |
| Surface | `#FFFFFF` | `white` | Cards, panels |
| Border | `#E2E8F0` | `slate-200` | Dividers, inputs |
| Border Strong | `#CBD5E1` | `slate-300` | Emphasized borders |
| Text Primary | `#0F172A` | `slate-900` | Body text |
| Text Secondary | `#475569` | `slate-600` | Secondary text |
| Text Muted | `#64748B` | `slate-500` | Labels, hints |
| Text Disabled | `#94A3B8` | `slate-400` | Disabled states |

### Grade colors

Used in badges and score displays.

| Grade | Text | Background | Hex pair |
|---|---|---|---|
| A | `emerald-700` | `emerald-50` | `#047857` / `#ECFDF5` |
| B | `blue-700` | `blue-50` | `#1D4ED8` / `#EFF6FF` |
| C | `amber-700` | `amber-50` | `#B45309` / `#FFFBEB` |
| D | `orange-700` | `orange-50` | `#C2410C` / `#FFF7ED` |
| E | `red-700` | `red-50` | `#B91C1C` / `#FEF2F2` |
| F | `red-800` | `red-100` | `#991B1B` / `#FEE2E2` |

---

## 4. Typography

### Font families

| Purpose | Font | Weight range |
|---|---|---|
| UI text | Inter | 400, 500, 600, 700 |
| Numbers, marks, tables | JetBrains Mono | 400, 500 |

**Why Inter:** modern, clean, highly legible at small sizes, huge character set.
**Why JetBrains Mono:** monospaced for alignment in tables and mark grids.

**Fallback:**
- Web: `Inter, system-ui, -apple-system, sans-serif`
- Desktop: `Inter, "Segoe UI", sans-serif`
- Mono: `JetBrains Mono, "Courier New", monospace`

### Type scale

| Name | Size | Weight | Line height | Use |
|---|---|---|---|---|
| Display | 32px | 700 | 1.2 | Login page title |
| H1 | 24px | 600 | 1.3 | Page titles |
| H2 | 20px | 600 | 1.35 | Section titles |
| H3 | 18px | 600 | 1.4 | Card titles |
| Body | 16px | 400 | 1.5 | Default text |
| Body Small | 14px | 400 | 1.5 | Labels, secondary text |
| Body Small Med | 14px | 500 | 1.5 | Emphasis in labels |
| Tiny | 12px | 500 | 1.4 | Badges, meta |
| Mono | 14px | 500 | 1.4 | Numbers, marks |

### Rules

- Never use more than 3 weights on one screen
- Headings always 600, never 700 (except Display)
- Body text never smaller than 14px
- Numbers in tables always JetBrains Mono
- Never use ALL CAPS for body text
- Never use italics for emphasis — use weight or color
- Line length: 60–80 characters for body text

---

## 5. Spacing

Use a consistent 4px scale everywhere. No eyeballing.

| Token | Pixels | Tailwind | Use |
|---|---|---|---|
| space-1 | 4px | `p-1`, `m-1` | Micro spacing |
| space-2 | 8px | `p-2`, `m-2` | Small gaps |
| space-3 | 12px | `p-3`, `m-3` | Form gaps |
| space-4 | 16px | `p-4`, `m-4` | Card padding |
| space-5 | 20px | `p-5`, `m-5` | Between sections |
| space-6 | 24px | `p-6`, `m-6` | Card padding (large) |
| space-8 | 32px | `p-8`, `m-8` | Between page sections |
| space-10 | 40px | `p-10`, `m-10` | Page-level spacing |
| space-12 | 48px | `p-12`, `m-12` | Between major blocks |

### Common patterns

- Inside a card: `p-4` or `p-6`
- Between cards: `gap-4`
- Between sections: `mb-6` or `space-y-6`
- Between form fields: `space-y-4`
- Between page and content: `py-6 px-4`

**Rule:** if you're unsure, use the next size up. Whitespace is professional.

---

## 6. Border Radius

| Element | Radius | Tailwind |
|---|---|---|
| Inputs | 8px | `rounded-lg` |
| Buttons | 8px | `rounded-lg` |
| Cards | 12px | `rounded-xl` |
| Modals | 16px | `rounded-2xl` |
| Badges | Full pill | `rounded-full` |
| Avatars | Full circle | `rounded-full` |

**Consistency matters more than exact values.** Same element type = same radius everywhere.

---

## 7. Shadows

Subtle and layered. Never dramatic.

| Name | Use | Tailwind |
|---|---|---|
| shadow-sm | Cards, inputs | `shadow-sm` |
| shadow-md | Dropdowns, hover cards | `shadow-md` |
| shadow-lg | Popovers | `shadow-lg` |
| shadow-xl | Modals | `shadow-xl` |

**Rules:**
- Default cards use `shadow-sm`
- On hover, cards lift to `shadow-md`
- Modals always use `shadow-xl`
- Never use heavy drop shadows
- Never combine shadows with borders unless intentional

---

## 8. Components

### Buttons

**Primary** — the main action on a screen. Only one per screen.

```
Background: Primary (#4F46E5)
Text: White
Padding: 10px 20px (px-5 py-2.5)
Radius: 8px
Font: 14px, weight 600
Hover: Primary Hover (#4338CA)
Focus: 2px ring, primary color
Disabled: 50% opacity
```

**Secondary** — alternative actions.

```
Background: White
Text: Text Primary
Border: 1px solid Border (#E2E8F0)
Padding: 10px 20px
Radius: 8px
Hover: Background becomes slate-50
```

**Ghost** — tertiary actions, like "Cancel" in a modal.

```
Background: transparent
Text: Text Secondary
Hover: Background becomes slate-50
```

**Danger** — destructive actions.

```
Background: Danger (#DC2626)
Text: White
Hover: red-700
```

**Sizes:**
- sm — `px-3 py-1.5 text-sm`
- md — `px-5 py-2.5 text-sm` (default)
- lg — `px-6 py-3 text-base`

**States:** default, hover, focus, active, disabled, loading (spinner replaces text)

### Inputs

```
Height: 40px (default), 48px (large)
Padding: 10px 12px
Border: 1px solid Border (#E2E8F0)
Radius: 8px
Font: 14px, Inter
Background: White
Focus: border primary, 2px ring
Error: border danger, error text below
Disabled: slate-100 bg, slate-400 text
```

**Every input has:**
- A label above (14px, weight 500, text-slate-700)
- Optional help text below (12px, text-slate-500)
- Optional error text below (12px, text-danger)

### Tables

**Used for:** lists of students, marks, users, audit log.

```
Header: bg-slate-50, weight 600, 12px uppercase tracking-wide
Rows: 48px height, 1px bottom border
Hover: bg-slate-50
Selected: bg-indigo-50
Numbers: JetBrains Mono, right-aligned
Actions: right column, ghost buttons
```

- Sticky header on long tables
- Zebra striping optional (only for very dense tables)
- Row height minimum 44px for touch (web)
- Keyboard focus visible on all rows

### Cards

```
Background: White
Radius: 12px
Shadow: shadow-sm
Padding: 24px (p-6)
```

**Anatomy:**
- Optional header: title (H3) + action button
- Body: content
- Optional footer: secondary actions

### Badges

Small pill-shaped labels for state.

| State | Style |
|---|---|
| DRAFT | bg-amber-50, text-amber-700 |
| PUBLISHED | bg-emerald-50, text-emerald-700 |
| FAILED | bg-red-50, text-red-700 |
| PENDING | bg-slate-100, text-slate-700 |
| Active | bg-emerald-50, text-emerald-700 |
| Inactive | bg-slate-100, text-slate-500 |

```
Padding: 4px 10px
Radius: full pill
Font: 12px, weight 600
```

### Modals

```
Backdrop: black at 50% opacity, blurred
Panel: white, radius 16px, max-width 480px
Padding: 24px
Shadow: shadow-xl
```

**Anatomy:**
- Title (H3)
- Body text or content
- Actions at bottom (right-aligned buttons)
- Close X in top-right (optional)

**Behaviour:**
- Escape closes
- Clicking backdrop closes
- Focus trapped inside
- Confirm dialogs for destructive actions

### Toasts

```
Position: top-right (fixed)
Max width: 360px
Radius: 8px
Padding: 12px 16px
Shadow: shadow-lg
Animation: slide-in from right, auto-dismiss in 5s
```

| Type | Style |
|---|---|
| Success | bg-emerald-600, text-white, check icon |
| Error | bg-red-600, text-white, alert icon |
| Info | bg-blue-600, text-white, info icon |
| Warning | bg-amber-500, text-white, warning icon |

### Empty states

Every list has an empty state.

```
Icon: 48px, slate-300
Title: 16px, weight 600, slate-700
Description: 14px, slate-500
Action: primary or secondary button, optional
Padding: py-12
Center-aligned
```

**Example:** "No students yet. [Add Students]"

### Loading states

**Skeletons:** for lists — grey blocks with shimmer.

**Spinners:** for buttons and short operations.

**Rules:**
- Delay showing spinner by 200ms (avoid flicker)
- Never show a blank screen
- Never block the whole UI for a small action

### Avatars

```
Shape: circle
Sizes: sm (32px), md (40px), lg (64px)
Fallback: initials on slate-200 background, text-slate-700
```

### Icons

**Library:** Lucide (both desktop and web).

**Sizes:** 16px (inline), 20px (buttons), 24px (nav), 32px (feature).

**Rules:**
- Always pair icons with text (except on mobile bottom nav)
- Never use icon-only buttons in critical flows
- Icons inherit text color

---

## 9. Layouts

### Desktop (JavaFX)

```
┌──────────────────────────────────────────────────────────┐
│  Top bar (56px)                                          │
├─────────────┬────────────────────────────────────────────┤
│             │                                            │
│  Sidebar    │  Content area                              │
│  (240px)    │                                            │
│             │                                            │
└─────────────┴────────────────────────────────────────────┘
```

- Sidebar collapsible to 64px (icon only)
- Content max width: 1280px, centered
- Content padding: 24px (p-6)
- Minimum window: 1280×720

### Web (Student)

**Mobile (< 768px):**

```
┌───────────────────────┐
│  Top bar (56px)       │
├───────────────────────┤
│                       │
│  Content (fluid)      │
│                       │
├───────────────────────┤
│  Bottom nav (64px)    │
└───────────────────────┘
```

**Desktop (> 768px):**

```
┌──────────────────────────────────────────┐
│  Top bar (56px)                          │
├──────────────────────────────────────────┤
│                                          │
│  Content (max 900px, centered)           │
│                                          │
└──────────────────────────────────────────┘
```

- No sidebar on web (student)
- Content max width: `max-w-3xl` (768px)
- Padding: `px-4 py-6`
- Bottom nav only on mobile

---

## 10. Grade Display

Grades appear in tables, cards, and report cards.

**Rules:**
- Letter shown (A, B, C)
- Color-coded background
- Always monospace when paired with numbers
- Never red for grades C and above (only D, E, F can be red)

**In tables:**

```
Score (mono)    Grade (badge)
82.5            [ B ]
```

**In cards:**

```
[ 82.5 / 100 ]   [ B ]
```

**In report cards:**

```
Marks    Grade
82.5     B
```

---

## 11. Motion

Minimal animation. Only where it helps.

| Animation | Duration | Easing |
|---|---|---|
| Hover lift | 150ms | ease-out |
| Modal enter | 200ms | ease-out |
| Toast slide-in | 250ms | ease-out |
| Button spinner | infinite | linear |

**Never:**
- Animate on page load (feels slow)
- Use bounce or spring easing
- Animate more than 3 elements at once
- Animate for more than 300ms

**Prefers-reduced-motion:** honour it. Disable all transitions if the user's OS requests it.

---

## 12. Accessibility

Every screen must pass WCAG 2.1 AA.

### Color contrast

- Body text on white: at least 4.5:1
- Large text (18px+): at least 3:1
- Focus indicators: visible at 3:1
- All the palette colors above meet this

### Keyboard

- Every interactive element reachable by Tab
- Focus visible on every focused element (2px ring, primary color)
- Escape closes modals and dropdowns
- Enter activates the focused element

### Screen readers

- Every input has an associated label
- Every icon-only button has `aria-label`
- Every table has proper `<thead>`, `<tbody>`, `<th>`
- Meaningful alt text on images

### Forms

- Errors described in text, not just color
- Success messages clear and specific
- Required fields marked with `*` and `aria-required`

### Never rely on color alone

- Don't use only red/green to show state — always pair with text or icon
- Don't use hover-only to reveal important info

---

## 13. Component Reference — Quick Card

Copy this into your prompts when you need AI to build a component.

**Button (primary):**

```html
<button class="px-5 py-2.5 rounded-lg bg-indigo-600 text-white font-semibold text-sm hover:bg-indigo-700 focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed">
  Save
</button>
```

**Input (with label):**

```html
<div class="space-y-1">
  <label for="name" class="block text-sm font-medium text-slate-700">Name</label>
  <input id="name" type="text"
         class="w-full px-3 py-2.5 rounded-lg border border-slate-200 text-sm focus:border-indigo-500 focus:ring-2 focus:ring-indigo-500 focus:ring-opacity-20">
</div>
```

**Card:**

```html
<div class="bg-white rounded-xl shadow-sm p-6">
  <h3 class="text-lg font-semibold text-slate-900">Title</h3>
  <p class="mt-2 text-sm text-slate-600">Content</p>
</div>
```

**Badge (published):**

```html
<span class="inline-block px-2.5 py-0.5 rounded-full bg-emerald-50 text-emerald-700 text-xs font-semibold">
  Published
</span>
```

**Empty state:**

```html
<div class="text-center py-12">
  <i data-lucide="inbox" class="w-12 h-12 text-slate-300 mx-auto mb-3"></i>
  <h3 class="font-semibold text-slate-700">No students yet</h3>
  <p class="text-sm text-slate-500 mt-1">Add your first student to get started.</p>
  <button class="mt-4 px-5 py-2.5 rounded-lg bg-indigo-600 text-white font-semibold text-sm">
    Add Student
  </button>
</div>
```

**Grade badge:**

```html
<!-- A -->
<span class="inline-block px-2 py-0.5 rounded-full bg-emerald-50 text-emerald-700 text-xs font-semibold font-mono">A</span>
<!-- B -->
<span class="inline-block px-2 py-0.5 rounded-full bg-blue-50 text-blue-700 text-xs font-semibold font-mono">B</span>
<!-- C -->
<span class="inline-block px-2 py-0.5 rounded-full bg-amber-50 text-amber-700 text-xs font-semibold font-mono">C</span>
<!-- F -->
<span class="inline-block px-2 py-0.5 rounded-full bg-red-50 text-red-700 text-xs font-semibold font-mono">F</span>
```

**JavaFX equivalent (CSS):**

```css
.btn-primary {
    -fx-background-color: #4F46E5;
    -fx-text-fill: white;
    -fx-background-radius: 8;
    -fx-padding: 10 20;
    -fx-font-weight: 600;
    -fx-font-size: 14px;
    -fx-cursor: hand;
}
.btn-primary:hover { -fx-background-color: #4338CA; }
.btn-primary:disabled { -fx-opacity: 0.5; }

.card {
    -fx-background-color: white;
    -fx-background-radius: 12;
    -fx-padding: 24;
    -fx-effect: dropshadow(gaussian, rgba(15,23,42,0.06), 6, 0, 0, 2);
}

.badge-published {
    -fx-background-color: #ECFDF5;
    -fx-text-fill: #047857;
    -fx-background-radius: 999;
    -fx-padding: 4 10;
    -fx-font-size: 12px;
    -fx-font-weight: 600;
}
```

---

## 14. Common Prompts for Design AI

**Build a screen matching the design system:**

```
Context: MASTER.md + DESIGN_SYSTEM.md + [DESKTOP.md or WEB.md]

Build [component/screen name].
Purpose: [what it does]
Elements: [list]
Follow every rule in DESIGN_SYSTEM.md — colors, fonts, spacing, radius, shadows.
Return [FXML + CSS / Thymeleaf + Tailwind].
```

**Review a screen against the design system:**

```
Context: DESIGN_SYSTEM.md
Screen: [paste code]

Review against the design system.
List every deviation: colors, spacing, radius, fonts, shadows.
Suggest fixes.
```

**Create a new component:**

```
Context: DESIGN_SYSTEM.md

Design a component for [purpose].
Match the existing patterns in DESIGN_SYSTEM.md.
Include: [HTML + Tailwind / JavaFX + CSS]
```

---

## 15. What NOT to Do

- Don't invent colors — use the palette
- Don't use more than 2 font sizes in a single card
- Don't use shadows heavier than `shadow-xl`
- Don't use border radius other than the defined set
- Don't use ALL CAPS in body text
- Don't use red for anything that isn't an error, danger, or failing grade
- Don't animate on page load
- Don't use icon-only buttons in critical flows
- Don't rely on color alone for meaning
- Don't use hover-only interactions on mobile
- Don't use more than one primary button per screen
- Don't put text smaller than 12px anywhere
- Don't design for desktop-only on the web — mobile is the default
- Don't skip empty states
- Don't skip loading states

---

## The One-Sentence Summary

**DESIGN_SYSTEM.md defines the visual language of Musomi Manager — one palette (indigo primary + slate neutrals + semantic accents), two fonts (Inter + JetBrains Mono), a 4px spacing scale, consistent radii, subtle shadows, and a small set of accessible components. JavaFX and web both follow it exactly. If a screen uses colors, spacing, or fonts outside this document, it's wrong.**

---


