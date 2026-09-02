Teacher
  ↓
Register (role TEACHER, branchId) ✅
  ↓
Login → JWT ✅
  ↓
Create Class ✅   POST /api/classes
  ↓
Add Students ✅   POST /api/classes/{classId}/students
  ↓                GET  /api/classes/{classId}/students
Assign ArUco marker to each student   ← NEXT
  ↓
Create Question
  ↓
Start Assessment
  ↓
Students answer with ArUco cards
  ↓
Record answers
  ↓
Show results
