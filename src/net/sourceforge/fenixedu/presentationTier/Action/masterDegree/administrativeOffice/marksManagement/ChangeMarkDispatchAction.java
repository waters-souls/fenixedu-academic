package net.sourceforge.fenixedu.presentationTier.Action.masterDegree.administrativeOffice.marksManagement;

import net.sourceforge.fenixedu.applicationTier.Servico.commons.student.ReadInfoEnrolmentEvaluationByEvaluationOID;

import java.text.DateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sourceforge.fenixedu.applicationTier.IUserView;
import net.sourceforge.fenixedu.applicationTier.Servico.exceptions.ExistingServiceException;
import net.sourceforge.fenixedu.applicationTier.Servico.exceptions.FenixServiceException;
import net.sourceforge.fenixedu.applicationTier.Servico.exceptions.NonExistingServiceException;
import net.sourceforge.fenixedu.applicationTier.Servico.exceptions.NotAuthorizedException;
import net.sourceforge.fenixedu.applicationTier.Servico.gesdis.teacher.ReadTeacherByUsername;
import net.sourceforge.fenixedu.applicationTier.Servico.masterDegree.administrativeOffice.marksManagement.AlterStudentEnrolmentEvaluation;
import net.sourceforge.fenixedu.applicationTier.Servico.masterDegree.administrativeOffice.marksManagement.ReadStudentMarksByCurricularCourse;
import net.sourceforge.fenixedu.dataTransferObject.InfoEnrolment;
import net.sourceforge.fenixedu.dataTransferObject.InfoEnrolmentEvaluation;
import net.sourceforge.fenixedu.dataTransferObject.InfoSiteEnrolmentEvaluation;
import net.sourceforge.fenixedu.dataTransferObject.InfoTeacher;
import net.sourceforge.fenixedu.domain.EnrolmentEvaluation;
import net.sourceforge.fenixedu.domain.RootDomainObject;
import net.sourceforge.fenixedu.domain.Teacher;
import net.sourceforge.fenixedu.domain.curriculum.EnrolmentEvaluationType;
import net.sourceforge.fenixedu.domain.degree.DegreeType;
import net.sourceforge.fenixedu.domain.exceptions.DomainException;
import net.sourceforge.fenixedu.framework.factory.ServiceManagerServiceFactory;
import net.sourceforge.fenixedu.presentationTier.Action.base.FenixDispatchAction;
import net.sourceforge.fenixedu.presentationTier.Action.exceptions.ExistingActionException;
import net.sourceforge.fenixedu.presentationTier.Action.exceptions.FenixActionException;
import net.sourceforge.fenixedu.presentationTier.Action.resourceAllocationManager.utils.SessionConstants;
import net.sourceforge.fenixedu.util.Data;
import net.sourceforge.fenixedu.util.FormataData;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;

import pt.ist.fenixWebFramework.security.UserView;

/**
 * @author Angela 30/06/2003 Modified by Fernanda Quit�rio
 */
public class ChangeMarkDispatchAction extends FenixDispatchAction {
    InfoSiteEnrolmentEvaluation infoSiteEnrolmentEvaluation = null;

    public ActionForward prepareChangeMark(ActionMapping mapping, ActionForm form, HttpServletRequest request,
	    HttpServletResponse response) throws Exception {

	String curricularCourseId = MarksManagementDispatchAction.getFromRequest("courseId", request);
	MarksManagementDispatchAction.getFromRequest("objectCode", request);
	MarksManagementDispatchAction.getFromRequest("degreeId", request);

	List listEnrolmentEvaluation = null;
	IUserView userView = UserView.getUser();
	Object args[] = { userView, Integer.valueOf(curricularCourseId), null };
	try {
	    listEnrolmentEvaluation = (List) ServiceManagerServiceFactory.executeService(
		    "ReadStudentMarksListByCurricularCourse", args);
	} catch (NotAuthorizedException e) {
	    return mapping.findForward("NotAuthorized");
	} catch (NonExistingServiceException e) {
	    addErrorMessage(request, "nonExisting", "error.exception.noStudents");
	    return mapping.findForward("NoStudents");
	} catch (FenixServiceException e) {
	    throw new FenixActionException(e);
	}

	if (listEnrolmentEvaluation.size() == 0) {
	    addErrorMessage(request, "StudentNotEnroled", "error.students.Mark.NotAvailable");
	    return mapping.findForward("NoStudents");
	}

	InfoEnrolment infoEnrolment = (InfoEnrolment) listEnrolmentEvaluation.get(0);
	request.setAttribute("oneInfoEnrollment", infoEnrolment);

	return mapping.findForward("editStudentNumber");
    }

    public ActionForward chooseStudentMarks(ActionMapping mapping, ActionForm form, HttpServletRequest request,
	    HttpServletResponse response) throws Exception {

	String curricularCourseId = MarksManagementDispatchAction.getFromRequest("courseId", request);
	MarksManagementDispatchAction.getFromRequest("objectCode", request);
	MarksManagementDispatchAction.getFromRequest("degreeId", request);
	Integer studentNumber = null;
	try {
	    studentNumber = Integer.valueOf(MarksManagementDispatchAction.getFromRequest("studentNumber", request));
	} catch (NumberFormatException e) {
	    addErrorMessage(request, "StudentNumberRequired", "error.studentNumber.required");
	    return prepareChangeMark(mapping, form, request, response);
	}

	// Get mark student List
	String showMarks = MarksManagementDispatchAction.getFromRequest("showMarks", request);

	List infoSiteEnrolmentEvaluations = null;
	IUserView userView = UserView.getUser();
	try {

	    infoSiteEnrolmentEvaluations = ReadStudentMarksByCurricularCourse.run(Integer.valueOf(curricularCourseId),
		    studentNumber, null, getIntegerFromRequest(request, "enrolmentId"));
	} catch (ExistingServiceException e) {
	    // invalid student number
	    addErrorMessage(request, "StudentNotExist", "error.student.notExist");
	    if (showMarks == null) {
		return prepareChangeMark(mapping, form, request, response);
	    }
	    return mapping.findForward("chooseCurricularCourse");

	} catch (FenixServiceException e) {
	    throw new FenixActionException(e);
	}

	if (infoSiteEnrolmentEvaluations.size() == 0) {
	    addErrorMessage(request, "StudentNotEnroled", "error.student.Enrolment.curricularCourse.invalid", String
		    .valueOf(studentNumber));
	    return prepareChangeMark(mapping, form, request, response);
	}

	if (((InfoSiteEnrolmentEvaluation) infoSiteEnrolmentEvaluations.get(0)).getEnrolmentEvaluations().size() == 0) {
	    addErrorMessage(request, "StudentNotEnroled", "error.student.Mark.NotAvailable", String.valueOf(studentNumber));
	    if (showMarks != null) {
		return mapping.findForward("chooseCurricularCourse");
	    }
	    return prepareChangeMark(mapping, form, request, response);
	}

	InfoEnrolment infoEnrolmentTemp = ((InfoEnrolmentEvaluation) ((InfoSiteEnrolmentEvaluation) infoSiteEnrolmentEvaluations
		.get(0)).getEnrolmentEvaluations().get(0)).getInfoEnrolment();

	InfoEnrolmentEvaluation newEnrolmentEvaluation = null;
	try {

	    newEnrolmentEvaluation = (InfoEnrolmentEvaluation) ReadInfoEnrolmentEvaluationByEvaluationOID.run(userView, studentNumber, DegreeType.MASTER_DEGREE, infoEnrolmentTemp.getIdInternal());
	} catch (ExistingServiceException e) {
	    throw new ExistingActionException(e);
	}

	Locale locale = new Locale("pt", "PT");
	String examDay = null;
	String examMonth = null;
	String examYear = null;
	String gradeAvailableDay = null;
	String gradeAvailableMonth = null;
	String gradeAvailableYear = null;
	if (newEnrolmentEvaluation.getExamDate() != null) {
	    examDay = FormataData.getDay(DateFormat.getDateInstance(DateFormat.SHORT, locale).format(
		    newEnrolmentEvaluation.getExamDate()));
	} else {
	    examDay = "";

	}
	if (newEnrolmentEvaluation.getExamDate() != null) {
	    examMonth = FormataData.getMonth(DateFormat.getDateInstance(DateFormat.SHORT, locale).format(
		    newEnrolmentEvaluation.getExamDate()));
	} else {
	    examMonth = "";
	}
	if (newEnrolmentEvaluation.getExamDate() != null) {
	    examYear = FormataData.getYear(DateFormat.getDateInstance(DateFormat.SHORT, locale).format(
		    newEnrolmentEvaluation.getExamDate()));
	} else {
	    examYear = "";
	}
	if (newEnrolmentEvaluation.getGradeAvailableDate() != null) {
	    gradeAvailableDay = FormataData.getDay(DateFormat.getDateInstance(DateFormat.SHORT, locale).format(
		    newEnrolmentEvaluation.getGradeAvailableDate()));
	} else {
	    gradeAvailableDay = "";
	}
	if (newEnrolmentEvaluation.getGradeAvailableDate() != null) {
	    gradeAvailableMonth = FormataData.getMonth(DateFormat.getDateInstance(DateFormat.SHORT, locale).format(
		    newEnrolmentEvaluation.getGradeAvailableDate()));
	} else {
	    gradeAvailableMonth = "";
	}
	if (newEnrolmentEvaluation.getGradeAvailableDate() != null) {
	    gradeAvailableYear = FormataData.getYear(DateFormat.getDateInstance(DateFormat.SHORT, locale).format(
		    newEnrolmentEvaluation.getGradeAvailableDate()));
	} else {
	    gradeAvailableYear = "";
	}

	DynaActionForm studentNumberForm = (DynaActionForm) form;

	studentNumberForm.set("examDateYear", examYear);

	if (examMonth != "") {
	    int month = Integer.valueOf(examMonth).intValue() - 1;
	    studentNumberForm.set("examDateMonth", String.valueOf(month));
	} else {
	    studentNumberForm.set("examDateMonth", "");
	}
	if (examDay != "") {
	    studentNumberForm.set("examDateDay", new Integer(Integer.parseInt(examDay)).toString());
	} else {
	    studentNumberForm.set("examDateDay", "");
	}

	studentNumberForm.set("gradeAvailableDateYear", gradeAvailableYear);
	if (gradeAvailableMonth != "") {
	    int month = Integer.valueOf(gradeAvailableMonth).intValue() - 1;
	    studentNumberForm.set("gradeAvailableDateMonth", String.valueOf(month));
	} else {
	    studentNumberForm.set("gradeAvailableDateMonth", "");
	}
	if (gradeAvailableDay != "") {
	    studentNumberForm.set("gradeAvailableDateDay", new Integer(Integer.parseInt(gradeAvailableDay)).toString());
	} else {
	    studentNumberForm.set("gradeAvailableDateDay", "");
	}
	studentNumberForm.set("grade", newEnrolmentEvaluation.getGradeValue());
	studentNumberForm.set("observation", newEnrolmentEvaluation.getObservation());
	studentNumberForm.set("enrolmentEvaluationType", String.valueOf(newEnrolmentEvaluation.getEnrolmentEvaluationType()
		.toString()));
	studentNumberForm.set("studentNumber", studentNumber);
	// responsible teacher
	InfoTeacher infoTeacher = null;

	if (newEnrolmentEvaluation.getInfoPersonResponsibleForGrade() != null) {
	    infoTeacher = ReadTeacherByUsername.run(newEnrolmentEvaluation.getInfoPersonResponsibleForGrade().getUsername());
	    studentNumberForm.set("teacherNumber", String.valueOf(infoTeacher.getTeacherNumber()));
	} else {
	    studentNumberForm.set("teacherNumber", "");
	}
	request.setAttribute(SessionConstants.ENROLMENT_EVALUATION_TYPE_LIST, Arrays.asList(EnrolmentEvaluationType
		.getLabelValues(null)));
	request.setAttribute(SessionConstants.MONTH_DAYS_KEY, Data.getMonthDays());
	request.setAttribute(SessionConstants.MONTH_LIST_KEY, Data.getMonths());
	request.setAttribute(SessionConstants.YEARS_KEY, Data.getYears());

	request.setAttribute("lastEnrolmentEavluation", newEnrolmentEvaluation);
	request.setAttribute("infoSiteEnrolmentEvaluation", infoSiteEnrolmentEvaluations);

	return mapping.findForward("studentMarks");
    }

    public ActionForward studentMarkChanged(ActionMapping mapping, ActionForm form, HttpServletRequest request,
	    HttpServletResponse response) throws Exception {
	String curricularCourseId = MarksManagementDispatchAction.getFromRequest("courseId", request);
	MarksManagementDispatchAction.getFromRequest("objectCode", request);
	MarksManagementDispatchAction.getFromRequest("degreeId", request);

	DynaActionForm studentNumberForm = (DynaActionForm) form;
	// get input
	Integer enrolmentEvaluationCode = Integer.valueOf(MarksManagementDispatchAction.getFromRequest("teacherCode", request));
	String grade = MarksManagementDispatchAction.getFromRequest("grade", request);

	String evaluation = MarksManagementDispatchAction.getFromRequest("enrolmentEvaluationType", request);

	EnrolmentEvaluationType enrolmentEvaluationType = EnrolmentEvaluationType.valueOf(evaluation);

	Integer teacherNumber = null;

	try {
	    teacherNumber = Integer.valueOf(MarksManagementDispatchAction.getFromRequest("teacherNumber", request));
	} catch (NumberFormatException e) {
	    addErrorMessage(request, "TeacharNumberRequired", "error.teacherNumber.required");
	    return chooseStudentMarks(mapping, form, request, response);
	}

	String observation = MarksManagementDispatchAction.getFromRequest("observation", request);
	Calendar examDate = Calendar.getInstance();

	String examMonth = MarksManagementDispatchAction.getFromRequest("examDateMonth", request);
	String examYear = MarksManagementDispatchAction.getFromRequest("examDateYear", request);
	String examDay = MarksManagementDispatchAction.getFromRequest("examDateDay", request);

	String gradeAvailableDateMonth = MarksManagementDispatchAction.getFromRequest("gradeAvailableDateMonth", request);
	String gradeAvailableDateYear = MarksManagementDispatchAction.getFromRequest("gradeAvailableDateYear", request);
	String gradeAvailableDateDay = MarksManagementDispatchAction.getFromRequest("gradeAvailableDateDay", request);
	Integer day = null;
	Integer month = null;
	Integer year = null;

	InfoEnrolmentEvaluation infoEnrolmentEvaluation = new InfoEnrolmentEvaluation();

	if ((examDay == null) || (examMonth == null) || (examYear == null) || (examDay.length() == 0)
		|| (examMonth.length() == 0) || (examYear.length() == 0)) {
	    addErrorMessage(request, "TeacharNumberRequired", "error.data.exame.inv�lida");
	    return chooseStudentMarks(mapping, form, request, response);
	}

	day = new Integer((String) studentNumberForm.get("examDateDay"));
	month = new Integer((String) studentNumberForm.get("examDateMonth"));
	year = new Integer((String) studentNumberForm.get("examDateYear"));

	examDate.set(year.intValue(), month.intValue(), day.intValue());
	infoEnrolmentEvaluation.setExamDate(examDate.getTime());

	day = null;
	month = null;
	year = null;
	examDate = Calendar.getInstance();

	if ((gradeAvailableDateDay == null) || (gradeAvailableDateMonth == null) || (gradeAvailableDateYear == null)
		|| (gradeAvailableDateDay.length() == 0) || (gradeAvailableDateMonth.length() == 0)
		|| (gradeAvailableDateYear.length() == 0)) {

	    addErrorMessage(request, "TeacharNumberRequired", "error.data.lan�amento.inv�lida");
	    return chooseStudentMarks(mapping, form, request, response);
	}

	day = new Integer((String) studentNumberForm.get("gradeAvailableDateDay"));
	month = new Integer((String) studentNumberForm.get("gradeAvailableDateMonth"));
	year = new Integer((String) studentNumberForm.get("gradeAvailableDateYear"));

	examDate.set(year.intValue(), month.intValue(), day.intValue());
	infoEnrolmentEvaluation.setGradeAvailableDate(examDate.getTime());

	final InfoTeacher infoTeacher = InfoTeacher.newInfoFromDomain(Teacher.readByNumber(teacherNumber));
	final EnrolmentEvaluation enrolmentEvaluation = RootDomainObject.getInstance().readEnrolmentEvaluationByOID(
		enrolmentEvaluationCode);
	infoEnrolmentEvaluation.setEnrolmentEvaluationType(enrolmentEvaluationType);

	infoEnrolmentEvaluation.setGradeValue(grade);
	infoEnrolmentEvaluation.setObservation(observation);
	infoEnrolmentEvaluation.setInfoEnrolment(InfoEnrolment.newInfoFromDomain(enrolmentEvaluation.getEnrolment()));

	try {
	    IUserView userView = UserView.getUser();

	    AlterStudentEnrolmentEvaluation.run(Integer.valueOf(curricularCourseId), enrolmentEvaluationCode,
		    infoEnrolmentEvaluation, infoTeacher.getTeacherNumber(), userView);
	} catch (DomainException e) {
	    addErrorMessage(request, e.getKey(), e.getKey(), (Object[]) e.getArgs());
	    return chooseStudentMarks(mapping, form, request, response);

	} catch (NonExistingServiceException e) {
	    addErrorMessage(request, "TeacharNumberRequired", "message.non.existing.teacher");
	    return chooseStudentMarks(mapping, form, request, response);
	}

	request.setAttribute("Label.MarkChange", "Registo  Alterado");
	return mapping.findForward("changeStudentMark");
    }
}