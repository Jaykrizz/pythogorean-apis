# Pythogorean Architecture

## Domain Hierarchy

School
├── Branch A
│   ├── Teachers
│   ├── Students
│   ├── Classes
│   │   └── Subjects
│   │       └── Questions
│   └── Subjects
│
└── Branch B
    ├── Teachers
    ├── Students
    ├── Classes
    │   └── Subjects
    │       └── Questions
    └── Subjects


## Domain Relationships

School
  │
  │ 1 → many
  ▼
Branch
  ├── 1 → many → Teachers
  ├── 1 → many → Students
  ├── 1 → many → Classes
  └── 1 → many → Subjects

Class
  ├── many ↔ many → Students
  └── many ↔ many → Subjects

Subject
  └── 1 → many → Questions


## Database Structure

| Table            | Key columns                                           |
|------------------|-------------------------------------------------------|
| `users`          | `id`, `email`, `password_hash`, `role`               |
| `schools`        | `id`, `name`                                          |
| `branches`       | `id`, `school_id`, `name`                             |
| `teachers`       | `id`, `branch_id`, `user_id`                          |
| `classes`        | `id`, `branch_id`, `teacher_id`, `name`               |
| `students`       | `id`, `branch_id`, `name`, `email`, `aruco_marker_id` |
| `class_students` | `class_id`, `student_id`                              |
| `subjects`       | `id`, `branch_id`, `name`                             |
| `class_subjects` | `class_id`, `subject_id`                              |
| `questions`      | `id`, `subject_id`, ...                               |


## Foreign-Key Relationships

schools
  │
  └── branches.school_id
          │
          ├── teachers.branch_id
          │     └── teachers.user_id → users.id
          │
          ├── classes.branch_id
          │     └── classes.teacher_id → teachers.id
          │
          ├── students.branch_id
          │
          └── subjects.branch_id

classes
  ├── class_students.class_id
  │        └── class_students.student_id → students.id
  │
  └── class_subjects.class_id
           └── class_subjects.subject_id → subjects.id
                                      │
                                      └── questions.subject_id