-- ITA applicants have identity verified via invitation; date_of_birth and ssn_token not always present
ALTER TABLE applicant ALTER COLUMN date_of_birth DROP NOT NULL;
ALTER TABLE applicant ALTER COLUMN ssn_token DROP NOT NULL;
