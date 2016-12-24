select
cvs_account, students.student_pk, student_registration_pk, employee_num
from student_registration, students
where instructor_capability is null
and student_registration.student_pk = students.student_pk

