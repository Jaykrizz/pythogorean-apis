# Pythogorean MVP implementation roadmap

Build only what is needed to run a live classroom test: a teacher signs in, sets up a class, adds people and MCQs, prints ArUco cards, points a camera at the room, and watches answers land in realtime.

Work through the phases in order. Do not start a later phase until the previous one is usable.

---

## Product slice (MVP)

A teacher can:

1. Land on a dashboard, log in, and (after login) register teachers, students, and parents.
2. Create a **class**, then **subjects** inside it, then **topics** inside a subject.
3. Add **MCQ questions** (A/B/C/D + marks) on a topic.
4. Issue each student a unique ArUco card. Card **orientation** is the answer (A/B/C/D).
5. Start a live test, capture **many cards in one camera frame**, record each student’s answer, and show results as they arrive.

Student and parent accounts exist so they can be registered and later see scores. Their dashboards stay thin: last session result only.

---

## Out of scope for MVP

- Public self-registration
- Open-ended / multi-correct / image questions
- Homework, attendance, grades beyond one live session
- Multiple cameras, mobile student apps, offline mode
- Analytics, PDF reports, notifications, payments
- Changing ArUco dictionary after cards are issued
- Production hardening (email verify, password reset, SSO)

---

## Repos and ports

| Piece | Repo | Port | MVP job |
|---|---|---|---|
| UI | `pythogorean-ng` | 4200 | Dashboard, forms, camera, live results |
| Domain | `pythogorean-apis` | 8080 | Users, classes, questions, answers, sessions |
| Vision | `pythogorean-vision-layer` | 8000 | Print cards; detect marker id + rotation from a frame |

Angular talks to Spring only (`http://localhost:8080/api`). Spring calls Vision when it needs a card PNG or a frame decoded. The browser never depends on Vision URLs except if we stream the teacher camera locally and send JPEG bytes through Spring.

MySQL is already running via `pythogorean-apis/docker-compose.yml`.

---

## How classes, subjects, and topics link

Use a **strict tree owned by the class**. No many-to-many in MVP.

```
Branch
  └── Class          (e.g. Grade 10-A)
        ├── Subject  (e.g. Physics)     belongs to one class
        │     └── Topic                 belongs to one subject
        │           └── Question        belongs to one topic
        └── Students (enrolled)         each has one marker_id
```

Why this shape:

- A live test is always “this class, this topic, these students.”
- Questions never leak across classes.
- Screens stay a simple drill-down: Class → Subject → Topic → Questions.
- Sharing Physics across 10-A and 10-B is a later copy/clone action, not a join table.

Parents link to students (`parent_student`). A parent is not enrolled in a class.

---

## Answer cards (fixed contract)

Each enrolled student gets one marker id (0–249 with `DICT_4X4_250`). The id is unique in the school so one camera can see the whole room.

The student turns the card so their answer letter is **upright at the top**. Vision reports marker id + top-edge angle. Spring maps angle → letter:

| Top-edge angle | Letter |
|---|---|
| 0° | A |
| 270° | B |
| 180° | C |
| 90° | D |

Allow ±30° tolerance. Ambiguous angles are ignored, not scored.

Printing uses existing `GET /cards/{marker_id}.png?label=Student+Name`.

---

## Auth rules

- Logged-out dashboard shows **Login** only. Hide Register.
- First teacher is **seeded** (SQL on startup). That unlocks the product.
- After login, only `TEACHER` sees **Register**.
- Register creates `TEACHER`, `STUDENT`, or `PARENT` (role chosen on the form).
- Students must be assigned to a class at register time; Spring assigns the next free `marker_id` and can return a card URL.
- Parents must be linked to at least one student.
- JWT on every API except login and health.
- Students/parents cannot create classes, questions, or live sessions.

---

## Live test loop (core)

1. Teacher opens a topic → **Start live session**.
2. UI shows question 1, camera preview, and a roster (waiting / answered / letter / marks).
3. Browser captures frames (~4–8 fps), `POST`s the JPEG to Spring, Spring calls Vision `/detect`.
4. For each detection: marker → student in this class; rotation → letter; upsert that student’s answer for the **current** question (last clear read wins).
5. Push or poll results to the teacher screen immediately.
6. Teacher clicks **Next question** / **End session**.
7. Session summary: per student total marks, per question counts of A/B/C/D.

One session has one current question. Students answering late for a previous question are ignored.

---

## Domain tables (Spring / MySQL)

Keep names boring and stable.

| Table | Purpose |
|---|---|
| `branch` | Seed one row. Teacher belongs to a branch. |
| `app_user` | id, name, email, password_hash, role, branch_id |
| `class_room` | id, branch_id, name, created_by |
| `subject` | id, class_id, name |
| `topic` | id, subject_id, name |
| `question` | id, topic_id, prompt, option_a–d, correct_letter, marks, sort_order |
| `enrollment` | class_id, student_id, marker_id (unique) |
| `parent_student` | parent_id, student_id |
| `live_session` | id, class_id, topic_id, teacher_id, status (`LIVE`/`ENDED`), current_question_id |
| `response` | session_id, question_id, student_id, letter, angle, marks_awarded, updated_at |

`ddl-auto=update` is fine for MVP. No Flyway yet.

---

## UI routes (Angular)

Reuse existing stub folders (`dashboard`, `class-management`, `assessment`, `auth`).

| Route | Who | Screen |
|---|---|---|
| `/` | all | Dashboard (landing). Login if logged out. Teacher home if logged in. |
| `/login` | public | Login |
| `/register` | teacher | Register teacher / student / parent |
| `/classes` | teacher | Class list + create |
| `/classes/:id` | teacher | Subjects, enrollments, print cards |
| `/classes/:id/subjects/:sid/topics/:tid` | teacher | Topic: questions + **Start live test** |
| `/live/:sessionId` | teacher | Camera + current question + live roster |
| `/results/:sessionId` | teacher, later student/parent | Summary |

Logged-in student/parent stay on `/` with a simple “your last result” card. No class editor.

---

## Phases

### Phase 0 — Confirm the stack (half day)

Already in place: MySQL Docker, Spring hello endpoint, Angular login/register UI pointing at `:8080/api`, Vision card PNG.

Do this first:

1. `docker compose up -d` in `pythogorean-apis`.
2. Run Spring and Angular; confirm `GET /api/hello`.
3. Run Vision; confirm `GET /health` and one card PNG.

**Done when:** all three processes run locally.

---

### Phase 1 — Auth, dashboard, teacher-only register

**Spring**

1. Entities: `Branch`, `AppUser`. Seed one branch + one teacher (`teacher@local` / known password).
2. Spring Security + JWT.
3. `POST /api/auth/login` → `{ token, name, email, role }` (matches existing Angular models).
4. `POST /api/auth/register` — **authenticated TEACHER only**. Body: name, email, password, role, plus `classId` for students, `studentIds` for parents. Branch comes from the logged-in teacher.
5. `GET /api/auth/me`.
6. CORS for `http://localhost:4200`.

Student register in this phase may create the user without enrollment if classes do not exist yet; prefer blocking student register until a class exists (Phase 2 can tighten this). For a clean MVP, **implement class create before student register** or do Phase 2 class table in the same PR as student register.

**Angular**

1. `/` is the dashboard. Logged out: product blurb + **Login**. No Register.
2. After teacher login: dashboard actions — Register person, Classes, (disabled until later) Live test.
3. Auth guard: `/register` and class routes require `TEACHER`.
4. Register form: role select (Teacher / Student / Parent). Show class or student pickers when those roles are chosen.
5. Navbar: Login when logged out; Register only for teacher; Sign out.

**Done when:** seeded teacher can log in, register another teacher, and a logged-out visitor cannot open `/register`.

---

### Phase 2 — Classes, subjects, topics, students, cards

**Spring**

1. Tables: `class_room`, `subject`, `topic`, `enrollment`.
2. CRUD (teacher, own branch):
   - `POST/GET /api/classes`
   - `POST/GET /api/classes/{id}/subjects`
   - `POST/GET /api/subjects/{id}/topics`
3. `POST /api/classes/{id}/students` (or register student with `classId`) assigns next `marker_id`.
4. `GET /api/students/{id}/card` — Spring calls Vision, returns PNG (or redirects). Dictionary stored on enrollment.

**Angular**

1. Class list + create.
2. Class detail: subject list, student roster, **Print card** per student.
3. Subject → topic list.
4. After registering a student, show marker id and a card preview.

**Done when:** teacher has a class with at least two students, each with a printable card, and a topic ready for questions.

---

### Phase 3 — MCQ question bank

**Spring**

1. `question` table as above.
2. `POST/GET/PUT/DELETE /api/topics/{id}/questions`.
3. Validate: four options, `correctLetter` in A–D, `marks` > 0.

**Angular**

1. Topic page: list questions, add/edit form (prompt, A–D, correct, marks).
2. **Start live test** button (wires in Phase 4; can be disabled until a session API exists).

**Done when:** a topic has several scored MCQs and the correct letter is stored server-side (never shown on the live student-facing camera view).

---

### Phase 4 — Detect, live session, realtime results (core)

**Vision**

1. `POST /detect` — image bytes in, JSON out:

```json
{
  "detections": [
    { "markerId": 7, "angle": 180 }
  ]
}
```

`angle` is the marker top-edge direction, same contract as the card README. No student names, no letters.

**Spring**

1. `POST /api/topics/{id}/sessions` → create `LIVE` session, set current question to first by `sort_order`.
2. `POST /api/sessions/{id}/next` / `POST /api/sessions/{id}/end`.
3. `POST /api/sessions/{id}/frame` — multipart image → Vision `/detect` → map markers → upsert `response` for current question.
4. `GET /api/sessions/{id}` — current question (without broadcasting the correct letter to the camera view if we later add a projector mode; teacher UI may show it).
5. `GET /api/sessions/{id}/results` — roster: student, letter, marks, answeredAt.
6. Realtime: **poll `GET .../results` every 500–1000ms`** for MVP. Add WebSocket only if polling feels slow.

Scoring: `letter == correct_letter` → full marks, else 0. Last valid detection for that question wins.

**Angular (`/live/:sessionId`)**

1. `getUserMedia` camera on the teacher machine.
2. Draw video; every ~150–250ms grab a canvas JPEG and `POST` `/sessions/{id}/frame`.
3. Show current prompt + options A–D.
4. Roster updates from polling: green when answered, letter chip, running total.
5. Next / End controls.
6. After end, `/results/:sessionId` summary (per student totals + per-question A/B/C/D counts).

**Done when:** two printed cards in one frame, rotated to different letters, both names update on the live roster within about a second, and the summary matches those answers.

---

### Phase 5 — Thin student / parent result view

1. Student dashboard: latest session score for their class.
2. Parent dashboard: same for linked children.
3. No camera, no editing.

**Done when:** a parent registered in Phase 1 can log in and see the child’s last live-test marks.

---

## Suggested build order inside each phase

For every vertical slice: **MySQL entity → API → Angular screen → click through with real HTTP**. Do not batch all backend then all frontend.

Manual check at the end of Phase 4 (the only check that matters):

1. Seed teacher logs in.
2. Registers two students into a class; prints two cards.
3. Adds a topic with two MCQs.
4. Starts live test; two people hold cards in one shot.
5. Roster shows both answers; Next question; End; totals look right.

---

## Defaults to lock now

- One branch, one camera (teacher laptop).
- One live session per teacher at a time.
- Marker dictionary `DICT_4X4_250`, never changed in MVP.
- JWT in `localStorage` (already started).
- Passwords hashed (BCrypt). Seed teacher only in local config.
- No unit-test mandate beyond what you already generate; Phase 4 deserves one Spring test for angle → letter and one for “unknown marker ignored.”

---

## What this file is not

This is the implementation sequence, not a backlog of stretch features. If a request is not in Phases 1–5, it waits until MVP is running in a real classroom.
