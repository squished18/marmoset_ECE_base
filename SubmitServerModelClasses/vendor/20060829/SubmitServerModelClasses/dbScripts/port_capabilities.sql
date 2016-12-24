UPDATE
student_registration, capabilities
SET
student_registration.instructor_capability = 'read-only'
WHERE student_registration.student_registration_pk = capabilities.student_registration_pk
AND capabilities.capability = 'read-only';

UPDATE
student_registration, capabilities
SET
student_registration.instructor_capability = 'modify'
WHERE student_registration.student_registration_pk = capabilities.student_registration_pk
AND capabilities.capability = 'modify';