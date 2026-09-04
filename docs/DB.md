# Pythogorean Data Model

This file describes what the code actually builds. Update it in the same commit
as any entity change.

## Domain Hierarchy

School (not modelled yet — Branch is currently the top of the tree)
└── Branch
    ├── Teachers
    └── Classes
        └── Students
            └── Parent (optional)

## Relationships

Branch
  ├── 1 → many → Teacher
  └── 1 → many → Class

Class
  ├── many → 1 → Teacher   (the class owner)
  └── 1 → many → Student

Student
  ├── 1 → 1  → User        (login identity)
  ├── many → 1 → Class     (exactly one, class_id is NOT NULL)
  └── many → 1 → Parent    (optional)

Parent
  ├── 1 → 1  → User
  └── 1 → many → Student

## Tables

| Table      | Key columns                                                        |
|------------|--------------------------------------------------------------------|
| `users`    | `id`, `name`, `email` (unique), `password`, `role`, `created_at`   |
| `branches` | `id`, `name`                                                        |
| `teachers` | `id`, `user_id` (unique), `branch_id`                               |
| `classes`  | `id`, `name`, `branch_id`, `teacher_id`, `created_at`               |
| `students` | `id`, `user_id` (unique), `class_id`, `parent_id`, `aruco_marker_id` (unique) |
| `parents`  | `id`, `user_id` (unique)                                            |
| `aruco_cards` | `id`, `student_id` (unique), `marker_id`, `aruco_dictionary`, `image`, `generated_at` |

`role` is one of TEACHER, STUDENT, PARENT, ADMIN.

## Decisions and their consequences

**One class per student.** `students.class_id` is NOT NULL and there is no
`class_students` join table. A student therefore cannot be enrolled in two
classes and cannot be moved between classes without a direct data change.
This matches a homeroom-teacher model. Revisit if subject teachers ever need
their own rosters.

**Students do not register themselves.** `POST /api/auth/register` rejects role
STUDENT. A student row is only created by their teacher through
`POST /api/classes/{classId}/students`, which creates the `users` row and the
`students` row in one transaction.

**ArUco markers are globally unique and server-assigned.** `students.aruco_marker_id`
carries a unique constraint across the whole database, not per class. The teacher
never chooses the number: the backend takes `max(aruco_marker_id) + 1`, starting
at 1, and returns it so the teacher knows which printed card to hand over.

Global uniqueness is deliberate: the vision layer can send a bare marker ID and
the backend resolves the student without needing to know which class is on
camera. The cost is that every student in the school needs a distinct printed
card.

`max + 1` rather than lowest-free is also deliberate: there is no way to delete a
student yet, so gaps cannot occur, and ascending numbers match the order a
printed card set is handed out. Switch to lowest-free when students can be
removed, or freed cards will be stranded.

**Branch is reached through the class.** Students have no `branch_id` of their
own; their branch is `student.class.branch`. Adding one would only be worth it
if students ever exist outside a class.

**Card images live in their own table, not in `students`.** A `byte[]` column on
an entity is loaded by Hibernate on every query that touches that entity unless
bytecode enhancement is switched on, so a card image on `students` would drag
every image across the wire just to list forty names. `aruco_cards` holds the
PNG, one row per student, and is only read when someone asks to print a card.

The row also records the dictionary the card was generated with. The printed
card a student holds can never change, so the dictionary that produced it is
provenance, not a setting.

## Not modelled yet

`schools`, `subjects`, `topics`, `questions`, `assessments`,
`assessment_questions`, `student_answers`.
