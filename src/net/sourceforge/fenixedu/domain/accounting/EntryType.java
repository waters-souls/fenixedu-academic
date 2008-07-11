package net.sourceforge.fenixedu.domain.accounting;

public enum EntryType {

    TRANSFER,

    ADJUSTMENT,

    SCHOOL_REGISTRATION_CERTIFICATE_REQUEST_FEE,

    ENROLMENT_CERTIFICATE_REQUEST_FEE,

    APPROVEMENT_CERTIFICATE_REQUEST_FEE,

    DEGREE_FINALIZATION_CERTIFICATE_REQUEST_FEE,

    SCHOOL_REGISTRATION_DECLARATION_REQUEST_FEE,

    ENROLMENT_DECLARATION_REQUEST_FEE,

    DIPLOMA_REQUEST_FEE,

    COURSE_LOAD_REQUEST_FEE,
    
    EXTERNAL_COURSE_LOAD_REQUEST_FEE,

    EXAM_DATE_CERTIFICATE_REQUEST_FEE,

    PROGRAM_CERTIFICATE_REQUEST_FEE,
    
    EXTERNAL_PROGRAM_CERTIFICATE_REQUEST_FEE,

    PHOTOCOPY_REQUEST_FEE,

    STUDENT_REINGRESSION_REQUEST_FEE,

    EQUIVALENCE_PLAN_REQUEST_FEE,

    REVISION_EQUIVALENCE_PLAN_REQUEST_FEE,

    COURSE_GROUP_CHANGE_REQUEST_FEE,

    EXTRA_EXAM_REQUEST_FEE,

    FREE_SOLICITATION_ACADEMIC_REQUEST_FEE,

    CANDIDACY_ENROLMENT_FEE,

    GRATUITY_FEE,

    INSURANCE_FEE,

    REGISTRATION_FEE,

    ADMINISTRATIVE_OFFICE_FEE,

    ADMINISTRATIVE_OFFICE_FEE_INSURANCE,

    IMPROVEMENT_OF_APPROVED_ENROLMENT_FEE,

    ENROLMENT_OUT_OF_PERIOD_PENALTY,

    OVER23_INDIVIDUAL_CANDIDACY_FEE,

    SECOND_CYCLE_INDIVIDUAL_CANDIDACY_FEE, 
    
    DEGREE_CANDIDACY_FOR_GRADUATED_PERSON_FEE,
    
    RESIDENCE_FEE;

    public String getName() {
	return name();
    }

    public String getQualifiedName() {
	return EntryType.class.getSimpleName() + "." + name();
    }

    public String getFullyQualifiedName() {
	return EntryType.class.getName() + "." + name();
    }
}
