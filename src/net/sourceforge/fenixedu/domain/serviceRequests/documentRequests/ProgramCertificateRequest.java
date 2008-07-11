package net.sourceforge.fenixedu.domain.serviceRequests.documentRequests;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import net.sourceforge.fenixedu.domain.Enrolment;
import net.sourceforge.fenixedu.domain.ExecutionYear;
import net.sourceforge.fenixedu.domain.accounting.EventType;
import net.sourceforge.fenixedu.domain.exceptions.DomainException;
import net.sourceforge.fenixedu.domain.student.Registration;
import net.sourceforge.fenixedu.domain.student.RegistrationAgreement;

import org.joda.time.DateTime;

public class ProgramCertificateRequest extends ProgramCertificateRequest_Base {

    static public final List<RegistrationAgreement> FREE_PAYMENT_AGREEMENTS = Arrays.asList(RegistrationAgreement.AFA,
	    RegistrationAgreement.MA);

    protected ProgramCertificateRequest() {
	super();
	setNumberOfPages(0);
    }

    public ProgramCertificateRequest(final Registration registration, DateTime requestDate, final ExecutionYear executionYear,
	    final DocumentPurposeType purposeType, final String otherPurposeTypeDescription,
	    final Collection<Enrolment> enrolments, final Boolean urgentRequest) {
	this();
	super.init(registration, requestDate, executionYear, Boolean.FALSE, purposeType, otherPurposeTypeDescription,
		urgentRequest);
	checkParameters(enrolments);
	super.getEnrolments().addAll(enrolments);
    }

    private void checkParameters(final Collection<Enrolment> enrolments) {
	if (enrolments.isEmpty()) {
	    throw new DomainException("error.CourseLoadRequest.invalid.number.of.enrolments");
	}

	for (final Enrolment enrolment : enrolments) {
	    if (!enrolment.isApproved()) {
		throw new DomainException("error.ProgramCertificateRequest.cannot.add.not.approved.enrolments");
	    }
	    if (!getStudent().hasEnrolments(enrolment)) {
		throw new DomainException("error.ProgramCertificateRequest.enrolment.doesnot.belong.to.student");
	    }
	}
    }

    @Override
    public Integer getNumberOfUnits() {
	return Integer.valueOf(0);
    }

    @Override
    public DocumentRequestType getDocumentRequestType() {
	return DocumentRequestType.PROGRAM_CERTIFICATE;
    }

    @Override
    public String getDocumentTemplateKey() {
	return getClass().getName();
    }

    @Override
    public EventType getEventType() {
	return EventType.PROGRAM_CERTIFICATE_REQUEST;
    }

    @Override
    public void delete() {
	getEnrolments().clear();
	super.delete();
    }

    @Override
    public boolean isFree() {
	if (FREE_PAYMENT_AGREEMENTS.contains(getRegistration().getRegistrationAgreement())) {
	    return true;
	}
	return super.isFree();
    }
}
