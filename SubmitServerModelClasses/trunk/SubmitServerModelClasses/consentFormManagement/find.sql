use consent_forms;
select c1.unique_id, c1.course_pk
from consent_by_semester as c1, consent_by_semester as c2
where c1.unique_id = c2.unique_id
and c2.course_pk != c1.course_pk
and c1.given_consent != 'yes'
and c2.given_consent = 'yes'
