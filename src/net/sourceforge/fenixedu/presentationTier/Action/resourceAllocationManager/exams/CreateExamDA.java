package net.sourceforge.fenixedu.presentationTier.Action.resourceAllocationManager.exams;

import net.sourceforge.fenixedu.applicationTier.Servico.resourceAllocationManager.exams.ReadAvailableRoomsForExam;

import net.sourceforge.fenixedu.applicationTier.Servico.resourceAllocationManager.exams.ReadExecutionCourseWithAssociatedCurricularCourses;

import net.sourceforge.fenixedu.applicationTier.Servico.resourceAllocationManager.exams.ReadExecutionCourseWithAssociatedCurricularCourses;

import net.sourceforge.fenixedu.applicationTier.Servico.resourceAllocationManager.exams.ReadExecutionCourseWithAssociatedCurricularCourses;

import net.sourceforge.fenixedu.applicationTier.Servico.resourceAllocationManager.exams.ReadExecutionCourseWithAssociatedCurricularCourses;

import net.sourceforge.fenixedu.applicationTier.Servico.resourceAllocationManager.exams.ReadExecutionCourseWithAssociatedCurricularCourses;

import net.sourceforge.fenixedu.applicationTier.Servico.resourceAllocationManager.exams.ReadExecutionCourseWithAssociatedCurricularCourses;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sourceforge.fenixedu.applicationTier.Servico.exceptions.FenixServiceException;
import net.sourceforge.fenixedu.applicationTier.Servico.resourceAllocationManager.ReadRoomByOID;
import net.sourceforge.fenixedu.commons.CollectionUtils;
import net.sourceforge.fenixedu.dataTransferObject.InfoCurricularCourse;
import net.sourceforge.fenixedu.dataTransferObject.InfoCurricularCourseScope;
import net.sourceforge.fenixedu.dataTransferObject.InfoExam;
import net.sourceforge.fenixedu.dataTransferObject.InfoExecutionCourse;
import net.sourceforge.fenixedu.dataTransferObject.InfoRoom;
import net.sourceforge.fenixedu.dataTransferObject.InfoRoomOccupation;
import net.sourceforge.fenixedu.presentationTier.Action.resourceAllocationManager.utils.ServiceUtils;
import net.sourceforge.fenixedu.presentationTier.Action.resourceAllocationManager.utils.SessionConstants;
import net.sourceforge.fenixedu.presentationTier.Action.resourceAllocationManager.utils.Util;
import net.sourceforge.fenixedu.presentationTier.Action.utils.ContextUtils;
import net.sourceforge.fenixedu.util.DiaSemana;
import net.sourceforge.fenixedu.util.HourMinuteSecond;
import net.sourceforge.fenixedu.util.Season;

import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;
import org.apache.struts.util.LabelValueBean;
import org.apache.struts.validator.DynaValidatorForm;
import org.joda.time.YearMonthDay;

/**
 * @author Ana e Ricardo
 */
public class CreateExamDA extends FenixDateAndTimeContextDispatchAction {

    public ActionForward prepare(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
	    throws Exception {
	ContextUtils.setCurricularYearsContext(request);

	Integer executionCourseOID = ((InfoExecutionCourse) request.getAttribute(SessionConstants.EXECUTION_COURSE))
		.getIdInternal();

	InfoExecutionCourse executionCourse;
	try {
	    executionCourse = (InfoExecutionCourse) ReadExecutionCourseWithAssociatedCurricularCourses.run(executionCourseOID);
	} catch (Exception ex) {
	    throw new Exception(ex);
	}
	List executionCourseList = new ArrayList();

	executionCourseList.add(executionCourse);

	request.setAttribute(SessionConstants.EXECUTION_COURSES_LIST, executionCourseList);

	String nextPage = request.getParameter("nextPage");
	request.setAttribute(SessionConstants.NEXT_PAGE, nextPage);

	List examSeasons = Util.getExamSeasons();
	request.setAttribute(SessionConstants.LABLELIST_SEASONS, examSeasons);

	DynaValidatorForm createExamForm = (DynaValidatorForm) form;
	String[] executionCourseIDList = { executionCourse.getIdInternal().toString() };
	createExamForm.set("executionCourses", executionCourseIDList);

	List scopeIDList = new ArrayList();
	Iterator iter1 = executionCourse.getAssociatedInfoCurricularCourses().iterator();
	while (iter1.hasNext()) {
	    Iterator iter2 = ((InfoCurricularCourse) iter1.next()).getInfoScopes().iterator();
	    while (iter2.hasNext()) {
		scopeIDList.add(((InfoCurricularCourseScope) iter2.next()).getIdInternal().toString());
	    }
	}
	String[] scopeIDArray = CollectionUtils.toArrayOfString(scopeIDList);
	createExamForm.set("scopes", scopeIDArray);

	return mapping.findForward("showCreateForm");
    }

    public ActionForward create(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
	    throws Exception {

	DynaValidatorForm createExamForm = (DynaValidatorForm) form;

	// exam season
	Season season = new Season(new Integer((String) createExamForm.get("season")));

	// exam execution course
	List<String> executionCourseIDs = Arrays.asList((String[]) createExamForm.get("executionCourses"));

	// exam date
	Calendar examDate = Calendar.getInstance();
	Integer day = new Integer((String) createExamForm.get("day"));
	Integer month = new Integer((String) createExamForm.get("month"));
	Integer year = new Integer((String) createExamForm.get("year"));
	examDate.set(Calendar.YEAR, year.intValue());
	examDate.set(Calendar.MONTH, month.intValue() - 1);
	examDate.set(Calendar.DAY_OF_MONTH, day.intValue());
	if (examDate.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
	    addErrorMessage(request, "error.sunday", "error.sunday");
	    return prepare(mapping, form, request, response);
	}

	// exam start time
	Calendar examStartTime = Calendar.getInstance();
	Integer startHour = new Integer((String) createExamForm.get("beginningHour"));
	Integer startMinute = new Integer((String) createExamForm.get("beginningMinute"));
	examStartTime.set(Calendar.HOUR_OF_DAY, startHour.intValue());
	examStartTime.set(Calendar.MINUTE, startMinute.intValue());
	examStartTime.set(Calendar.SECOND, 0);

	// exam end time
	Calendar examEndTime = Calendar.getInstance();
	Integer endHour = new Integer((String) createExamForm.get("endHour"));
	Integer endMinute = new Integer((String) createExamForm.get("endMinute"));
	examEndTime.set(Calendar.HOUR_OF_DAY, endHour.intValue());
	examEndTime.set(Calendar.MINUTE, endMinute.intValue());
	examEndTime.set(Calendar.SECOND, 0);
	if (examStartTime.after(examEndTime)) {
	    addErrorMessage(request, "error.timeSwitched", "error.timeSwitched");
	    return prepare(mapping, form, request, response);
	}

	// associated scopes
	List<String> curricularCourseScopeIDs = Arrays.asList((String[]) createExamForm.get("scopes"));
	List<String> curricularCourseContextIDs = new ArrayList<String>();

	// associated rooms
	List<String> roomIDs = Arrays.asList((String[]) createExamForm.get("rooms"));

	// Create an exam with season, examDateAndTime and executionCourse
	Object argsCreateExam[] = { null, examDate.getTime(), examStartTime.getTime(), examEndTime.getTime(), executionCourseIDs,
		curricularCourseScopeIDs, curricularCourseContextIDs, roomIDs, season, null };
	try {
	    ServiceUtils.executeService("CreateWrittenEvaluation", argsCreateExam);
	} catch (FenixServiceException ex) {
	    addErrorMessage(request, "errors", ex.getMessage());
	    return prepare(mapping, form, request, response);
	}

	return mapping.findForward("Sucess");
    }

    public ActionForward associateExecutionCourse(ActionMapping mapping, ActionForm form, HttpServletRequest request,
	    HttpServletResponse response) throws Exception {
	String infoExamId = (String) request.getAttribute(SessionConstants.EXAM_OID);
	if (infoExamId == null) {
	    infoExamId = request.getParameter(SessionConstants.EXAM_OID);
	}
	request.setAttribute(SessionConstants.EXAM_OID, infoExamId);

	ContextUtils.setCurricularYearContext(request);
	ContextUtils.setExecutionDegreeContext(request);
	ContextUtils.setExecutionPeriodContext(request);
	ContextUtils.setCurricularYearsContext(request);

	return mapping.findForward("associateExecutionCourse");
    }

    public ActionForward prepareAfterAssociateExecutionCourse(ActionMapping mapping, ActionForm form, HttpServletRequest request,
	    HttpServletResponse response) throws Exception {
	String infoExamId = (String) request.getAttribute(SessionConstants.EXAM_OID);
	if (infoExamId == null) {
	    infoExamId = request.getParameter(SessionConstants.EXAM_OID);
	}
	request.setAttribute(SessionConstants.EXAM_OID, infoExamId);

	ContextUtils.setCurricularYearContext(request);
	ContextUtils.setExecutionDegreeContext(request);
	ContextUtils.setExecutionPeriodContext(request);
	ContextUtils.setCurricularYearsContext(request);

	DynaValidatorForm createExamForm = (DynaValidatorForm) form;
	String[] executionCourseArray = (String[]) createExamForm.get("executionCourses");

	List executionCourseList = new ArrayList();
	for (int i = 0; i < executionCourseArray.length; i++) {

	    InfoExecutionCourse executionCourse;
	    try {
		executionCourse = (InfoExecutionCourse) ReadExecutionCourseWithAssociatedCurricularCourses.run(new Integer(executionCourseArray[i]));
	    } catch (Exception ex) {
		throw new Exception(ex);
	    }

	    executionCourseList.add(executionCourse);
	}
	request.setAttribute(SessionConstants.EXECUTION_COURSES_LIST, executionCourseList);

	String nextPage = request.getParameter("nextPage");
	request.setAttribute(SessionConstants.NEXT_PAGE, nextPage);

	List examSeasons = Util.getExamSeasons();
	request.setAttribute(SessionConstants.LABLELIST_SEASONS, examSeasons);

	String[] scopeIDArray = (String[]) createExamForm.get("scopes");
	List scopeIDList = CollectionUtils.toList(scopeIDArray);
	InfoExecutionCourse executionCourse = (InfoExecutionCourse) executionCourseList.get(executionCourseList.size() - 1);
	Iterator iter2 = executionCourse.getAssociatedInfoCurricularCourses().iterator();
	while (iter2.hasNext()) {
	    Iterator iter3 = ((InfoCurricularCourse) iter2.next()).getInfoScopes().iterator();
	    while (iter3.hasNext()) {
		scopeIDList.add(((InfoCurricularCourseScope) iter3.next()).getIdInternal().toString());
	    }
	}
	scopeIDArray = CollectionUtils.toArrayOfString(scopeIDList);
	createExamForm.set("scopes", scopeIDArray);

	String[] rooms = (String[]) createExamForm.get("rooms");
	List roomNames = new ArrayList();

	if (rooms != null && rooms.length > 0) {

	    for (int iterRooms = 0; iterRooms < rooms.length; iterRooms++) {

		InfoRoom infoRoom = (InfoRoom) ReadRoomByOID.run(new Integer(rooms[iterRooms]));

		roomNames.add(infoRoom);
	    }
	}

	request.setAttribute("rooms", roomNames);

	return mapping.findForward("showCreateForm");
    }

    public ActionForward prepareAfterDissociateExecutionCourse(ActionMapping mapping, ActionForm form,
	    HttpServletRequest request, HttpServletResponse response) throws Exception {
	String infoExamId = (String) request.getAttribute(SessionConstants.EXAM_OID);
	if (infoExamId == null) {
	    infoExamId = request.getParameter(SessionConstants.EXAM_OID);
	}
	request.setAttribute(SessionConstants.EXAM_OID, infoExamId);

	ContextUtils.setCurricularYearContext(request);
	ContextUtils.setExecutionDegreeContext(request);
	ContextUtils.setExecutionPeriodContext(request);
	ContextUtils.setCurricularYearsContext(request);

	DynaValidatorForm createExamForm = (DynaValidatorForm) form;
	String[] executionCourseArray = (String[]) createExamForm.get("executionCourses");

	List executionCourseList = new ArrayList();
	for (int i = 0; i < executionCourseArray.length; i++) {

	    InfoExecutionCourse executionCourse;
	    try {
		executionCourse = (InfoExecutionCourse) ReadExecutionCourseWithAssociatedCurricularCourses.run(new Integer(executionCourseArray[i]));
	    } catch (Exception ex) {
		throw new Exception(ex);
	    }

	    executionCourseList.add(executionCourse);
	}
	request.setAttribute(SessionConstants.EXECUTION_COURSES_LIST, executionCourseList);

	String nextPage = request.getParameter("nextPage");
	request.setAttribute(SessionConstants.NEXT_PAGE, nextPage);

	List examSeasons = Util.getExamSeasons();
	request.setAttribute(SessionConstants.LABLELIST_SEASONS, examSeasons);

	String[] scopeIDArray = (String[]) createExamForm.get("scopes");

	createExamForm.set("scopes", scopeIDArray);

	String[] rooms = (String[]) createExamForm.get("rooms");
	List roomNames = new ArrayList();

	if (rooms != null && rooms.length > 0) {

	    for (int iterRooms = 0; iterRooms < rooms.length; iterRooms++) {

		InfoRoom infoRoom = (InfoRoom) ReadRoomByOID.run(new Integer(rooms[iterRooms]));

		roomNames.add(infoRoom);
	    }
	}

	request.setAttribute("rooms", roomNames);

	return mapping.findForward("showCreateForm");
    }

    public ActionForward associateRoom(ActionMapping mapping, ActionForm form, HttpServletRequest request,
	    HttpServletResponse response) throws Exception {
	String infoExamId = (String) request.getAttribute(SessionConstants.EXAM_OID);
	if (infoExamId == null) {
	    infoExamId = request.getParameter(SessionConstants.EXAM_OID);
	}
	request.setAttribute(SessionConstants.EXAM_OID, infoExamId);

	ContextUtils.setCurricularYearContext(request);
	ContextUtils.setExecutionCourseContext(request);
	ContextUtils.setExecutionDegreeContext(request);
	ContextUtils.setExecutionPeriodContext(request);
	ContextUtils.setCurricularYearsContext(request);

	DynaValidatorForm examForm = (DynaValidatorForm) form;

	// exam start time
	Calendar examStartTime = Calendar.getInstance();
	Integer startHour = new Integer((String) examForm.get("beginningHour"));
	Integer startMinute = new Integer((String) examForm.get("beginningMinute"));
	examStartTime.set(Calendar.HOUR_OF_DAY, startHour.intValue());
	examStartTime.set(Calendar.MINUTE, startMinute.intValue());
	examStartTime.set(Calendar.SECOND, 0);

	// exam end time
	Calendar examEndTime = Calendar.getInstance();
	Integer endHour = new Integer((String) examForm.get("endHour"));
	Integer endMinute = new Integer((String) examForm.get("endMinute"));
	examEndTime.set(Calendar.HOUR_OF_DAY, endHour.intValue());
	examEndTime.set(Calendar.MINUTE, endMinute.intValue());
	examEndTime.set(Calendar.SECOND, 0);

	if (examStartTime.after(examEndTime)) {
	    addErrorMessage(request, "error.timeSwitched", "error.timeSwitched");
	    return prepare(mapping, form, request, response);
	}

	// exam date
	Calendar examDate = Calendar.getInstance();
	Integer day = new Integer((String) examForm.get("day"));
	Integer month = new Integer((String) examForm.get("month"));
	Integer year = new Integer((String) examForm.get("year"));
	examDate.set(Calendar.YEAR, year.intValue());
	examDate.set(Calendar.MONTH, month.intValue() - 1);
	examDate.set(Calendar.DAY_OF_MONTH, day.intValue());

	if (examDate.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
	    addErrorMessage(request, "error.sunday", "error.sunday");
	    return prepare(mapping, form, request, response);
	}
	// //////////////////
	String[] scopeIDArray = (String[]) examForm.get("scopes");
	request.setAttribute("scopes", scopeIDArray);
	// ///////////////

	return mapping.findForward("associateRoom");
    }

    public ActionForward prepareAfterAssociateRoom(ActionMapping mapping, ActionForm form, HttpServletRequest request,
	    HttpServletResponse response) throws Exception {
	String infoExamId = (String) request.getAttribute(SessionConstants.EXAM_OID);
	if (infoExamId == null) {
	    infoExamId = request.getParameter(SessionConstants.EXAM_OID);
	}
	request.setAttribute(SessionConstants.EXAM_OID, infoExamId);

	ContextUtils.setCurricularYearContext(request);
	ContextUtils.setExecutionDegreeContext(request);
	ContextUtils.setExecutionPeriodContext(request);
	ContextUtils.setCurricularYearsContext(request);

	String executionDegreeOID = (String) request.getAttribute(SessionConstants.EXECUTION_DEGREE_OID);
	request.setAttribute("executionDegreeOID", executionDegreeOID);

	DynaValidatorForm createExamForm = (DynaValidatorForm) form;
	String[] executionCourseArray = (String[]) createExamForm.get("executionCourses");

	List executionCourseList = new ArrayList();
	for (int i = 0; i < executionCourseArray.length; i++) {

	    InfoExecutionCourse executionCourse;
	    try {
		executionCourse = (InfoExecutionCourse) ReadExecutionCourseWithAssociatedCurricularCourses.run(new Integer(executionCourseArray[i]));
	    } catch (Exception ex) {
		throw new Exception(ex);
	    }

	    executionCourseList.add(executionCourse);
	}
	request.setAttribute(SessionConstants.EXECUTION_COURSES_LIST, executionCourseList);

	String nextPage = request.getParameter("nextPage");
	request.setAttribute(SessionConstants.NEXT_PAGE, nextPage);

	List examSeasons = Util.getExamSeasons();
	request.setAttribute(SessionConstants.LABLELIST_SEASONS, examSeasons);

	String[] scopeIDArray = (String[]) createExamForm.get("scopes");
	request.setAttribute("scopes", scopeIDArray);

	String[] rooms = (String[]) createExamForm.get("rooms");
	List roomNames = new ArrayList();

	if (rooms != null && rooms.length > 0) {

	    for (int iterRooms = 0; iterRooms < rooms.length; iterRooms++) {

		InfoRoom infoRoom = (InfoRoom) ReadRoomByOID.run(new Integer(rooms[iterRooms]));

		roomNames.add(infoRoom);
	    }
	}

	request.setAttribute("rooms", roomNames);

	return mapping.findForward("showCreateForm");
    }

    public ActionForward prepareForEdit(ActionMapping mapping, ActionForm form, HttpServletRequest request,
	    HttpServletResponse response) throws Exception {
	Integer infoExamId = (Integer) request.getAttribute(SessionConstants.EXAM_OID);
	request.setAttribute(SessionConstants.EXAM_OID, infoExamId);

	ContextUtils.setCurricularYearsContext(request);

	InfoExam infoExam = (InfoExam) request.getAttribute(SessionConstants.EXAM);
	List executionCourseList = infoExam.getAssociatedExecutionCourse();
	Iterator iter = executionCourseList.iterator();
	List newExecutionCourseList = new ArrayList();
	while (iter.hasNext()) {
	    InfoExecutionCourse element = (InfoExecutionCourse) iter.next();

	    InfoExecutionCourse executionCourse;
	    try {
		executionCourse = (InfoExecutionCourse) ReadExecutionCourseWithAssociatedCurricularCourses.run(element.getIdInternal());
	    } catch (Exception ex) {
		throw new Exception(ex);
	    }
	    newExecutionCourseList.add(executionCourse);

	}

	request.setAttribute(SessionConstants.EXECUTION_COURSES_LIST, newExecutionCourseList);

	String nextPage = request.getParameter("nextPage");
	request.setAttribute(SessionConstants.NEXT_PAGE, nextPage);

	List<LabelValueBean> examSeasons = Util.getExamSeasons();
	request.setAttribute(SessionConstants.LABLELIST_SEASONS, examSeasons);

	DynaValidatorForm createExamForm = (DynaValidatorForm) form;

	String[] executionCourseIDList = new String[executionCourseList.size()];
	for (int i = 0; i < executionCourseList.size(); i++) {
	    executionCourseIDList[i] = ((InfoExecutionCourse) executionCourseList.get(i)).getIdInternal().toString();
	}
	createExamForm.set("executionCourses", executionCourseIDList);

	List<InfoRoomOccupation> roomsList = infoExam.getWrittenEvaluationSpaceOccupations();
	String[] roomsIDArray = new String[roomsList.size()];
	Object[] infoRoomsArray = new Object[roomsList.size()];
	for (int i = 0; i < roomsList.size(); i++) {
	    infoRoomsArray[i] = ((InfoRoomOccupation) roomsList.get(i)).getInfoRoom();
	    roomsIDArray[i] = ((InfoRoom) infoRoomsArray[i]).getIdInternal().toString();
	}
	createExamForm.set("rooms", roomsIDArray);
	request.setAttribute("rooms", infoRoomsArray);

	List scopeList = infoExam.getAssociatedCurricularCourseScope();
	String[] scopeIDArray = new String[scopeList.size()];
	for (int i = 0; i < scopeList.size(); i++) {
	    scopeIDArray[i] = ((InfoCurricularCourseScope) scopeList.get(i)).getIdInternal().toString();
	}
	createExamForm.set("scopes", scopeIDArray);

	createExamForm.set("day", DateFormatUtils.format(infoExam.getDay().getTime(), "dd"));
	createExamForm.set("month", DateFormatUtils.format(infoExam.getDay().getTime(), "MM"));
	createExamForm.set("year", DateFormatUtils.format(infoExam.getDay().getTime(), "yyyy"));
	createExamForm.set("beginningHour", DateFormatUtils.format(infoExam.getBeginning().getTime(), "HH"));
	createExamForm.set("beginningMinute", DateFormatUtils.format(infoExam.getBeginning().getTime(), "mm"));
	createExamForm.set("endHour", DateFormatUtils.format(infoExam.getEnd().getTime(), "HH"));
	createExamForm.set("endMinute", DateFormatUtils.format(infoExam.getEnd().getTime(), "mm"));
	createExamForm.set("season", infoExam.getSeason().getSeason().toString());

	return mapping.findForward("showCreateForm");
    }

    public ActionForward edit(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
	    throws Exception {

	DynaValidatorForm createExamForm = (DynaValidatorForm) form;

	Integer infoExamID = Integer.valueOf(((String) createExamForm.get("exam_oid")));
	String infoExamIdInteger = (String) request.getAttribute(SessionConstants.EXAM_OID);
	if (infoExamIdInteger == null) {
	    infoExamIdInteger = request.getParameter(SessionConstants.EXAM_OID);
	}
	request.setAttribute(SessionConstants.EXAM_OID, infoExamIdInteger);

	// exam season
	Season season = new Season(Integer.valueOf(((String) createExamForm.get("season"))));
	List<String> executionCourseIDs = Arrays.asList((String[]) createExamForm.get("executionCourses"));

	// exam date
	Calendar examDate = Calendar.getInstance();
	Integer day = new Integer((String) createExamForm.get("day"));
	Integer month = new Integer((String) createExamForm.get("month"));
	Integer year = new Integer((String) createExamForm.get("year"));
	examDate.set(Calendar.YEAR, year.intValue());
	examDate.set(Calendar.MONTH, month.intValue() - 1);
	examDate.set(Calendar.DAY_OF_MONTH, day.intValue());
	if (examDate.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
	    addErrorMessage(request, "error.sunday", "error.sunday");
	    return prepare(mapping, form, request, response);
	}

	// exam start time
	Calendar examStartTime = Calendar.getInstance();
	Integer startHour = new Integer((String) createExamForm.get("beginningHour"));
	Integer startMinute = new Integer((String) createExamForm.get("beginningMinute"));
	examStartTime.set(Calendar.HOUR_OF_DAY, startHour.intValue());
	examStartTime.set(Calendar.MINUTE, startMinute.intValue());
	examStartTime.set(Calendar.SECOND, 0);

	// exam end time
	Calendar examEndTime = Calendar.getInstance();
	Integer endHour = new Integer((String) createExamForm.get("endHour"));
	Integer endMinute = new Integer((String) createExamForm.get("endMinute"));
	examEndTime.set(Calendar.HOUR_OF_DAY, endHour.intValue());
	examEndTime.set(Calendar.MINUTE, endMinute.intValue());
	examEndTime.set(Calendar.SECOND, 0);
	if (examStartTime.after(examEndTime)) {
	    addErrorMessage(request, "error.timeSwitched", "error.timeSwitched");
	    return prepare(mapping, form, request, response);
	}

	List<String> scopeIDs = Arrays.asList((String[]) createExamForm.get("scopes"));
	List<String> contextIDs = new ArrayList<String>();
	List<String> roomIDs = Arrays.asList((String[]) createExamForm.get("rooms"));

	Object argsEditExam[] = { null, examDate.getTime(), examStartTime.getTime(), examEndTime.getTime(), executionCourseIDs,
		scopeIDs, contextIDs, roomIDs, infoExamID, season, null };
	try {
	    ServiceUtils.executeService("EditWrittenEvaluation", argsEditExam);
	} catch (FenixServiceException ex) {
	    addErrorMessage(request, "errors", ex.getMessage());
	    return prepare(mapping, form, request, response);
	}

	String date = (String) request.getAttribute(SessionConstants.DATE);
	if (date == null) {
	    date = request.getParameter(SessionConstants.DATE);
	}
	if (date == null) {
	    return mapping.findForward("Sucess");
	}
	return mapping.findForward("sucessSearchByDate");

    }

    public ActionForward dissociateExecutionCourse(ActionMapping mapping, ActionForm form, HttpServletRequest request,
	    HttpServletResponse response) throws Exception {
	DynaValidatorForm dissociateExamForm = (DynaValidatorForm) form;
	String executionCourseToDissociateID = (String) dissociateExamForm.get("executionCourseToDissociateID");
	String[] executionCourseArray = (String[]) dissociateExamForm.get("executionCourses");
	String[] newExecutionCourseArray = new String[executionCourseArray.length - 1];
	boolean depois = false;
	for (int i = 0; i < executionCourseArray.length; i++) {
	    if (depois) {
		newExecutionCourseArray[i - 1] = executionCourseArray[i];
	    } else if (executionCourseToDissociateID.equals(executionCourseArray[i])) {
		depois = true;
	    } else {
		newExecutionCourseArray[i] = executionCourseArray[i];
	    }
	}
	dissociateExamForm.set("executionCourses", newExecutionCourseArray);
	// request.setAttribute("examNewForm", dissociateExamForm);

	return prepareAfterDissociateExecutionCourse(mapping, dissociateExamForm, request, response);

    }

    public ActionForward checkRooms(ActionMapping mapping, ActionForm form, HttpServletRequest request,
	    HttpServletResponse response) throws Exception {

	DynaActionForm examForm = (DynaActionForm) form;
	ContextUtils.setCurricularYearsContext(request);

	String infoExamId = (String) request.getAttribute(SessionConstants.EXAM_OID);
	if (infoExamId == null) {
	    infoExamId = request.getParameter(SessionConstants.EXAM_OID);
	}
	request.setAttribute(SessionConstants.EXAM_OID, infoExamId);

	String[] executionCourseArray = (String[]) examForm.get("executionCourses");

	List executionCourseList = new ArrayList();
	for (int i = 0; i < executionCourseArray.length; i++) {

	    InfoExecutionCourse executionCourse;
	    try {
		executionCourse = (InfoExecutionCourse) ReadExecutionCourseWithAssociatedCurricularCourses.run(new Integer(executionCourseArray[i]));
	    } catch (Exception ex) {
		throw new Exception(ex);
	    }

	    executionCourseList.add(executionCourse);
	}
	request.setAttribute(SessionConstants.EXECUTION_COURSES_LIST, executionCourseList);

	String nextPage = request.getParameter("nextPage");
	request.setAttribute(SessionConstants.NEXT_PAGE, nextPage);

	List examSeasons = Util.getExamSeasons();
	request.setAttribute(SessionConstants.LABLELIST_SEASONS, examSeasons);

	List executionCourseNames = (List) request.getAttribute(SessionConstants.LIST_EXECUTION_COURSE_NAMES);
	request.setAttribute(SessionConstants.LIST_EXECUTION_COURSE_NAMES, executionCourseNames);

	String[] executionCourse = (String[]) examForm.get("executionCourses");
	examForm.set("executionCourses", executionCourse);

	String[] scopeIDArray = (String[]) examForm.get("scopes");
	examForm.set("scopes", scopeIDArray);

	// exam start time
	Calendar examStartTime = Calendar.getInstance();
	Integer startHour = new Integer((String) examForm.get("beginningHour"));
	Integer startMinute = new Integer((String) examForm.get("beginningMinute"));
	examStartTime.set(Calendar.HOUR_OF_DAY, startHour.intValue());
	examStartTime.set(Calendar.MINUTE, startMinute.intValue());
	examStartTime.set(Calendar.SECOND, 0);

	// exam end time
	Calendar examEndTime = Calendar.getInstance();
	Integer endHour = new Integer((String) examForm.get("endHour"));
	Integer endMinute = new Integer((String) examForm.get("endMinute"));
	examEndTime.set(Calendar.HOUR_OF_DAY, endHour.intValue());
	examEndTime.set(Calendar.MINUTE, endMinute.intValue());
	examEndTime.set(Calendar.SECOND, 0);

	if (examStartTime.after(examEndTime)) {
	    addErrorMessage(request, "error.timeSwitched", "error.timeSwitched");
	    return prepare(mapping, form, request, response);
	}

	// exam date
	Calendar examDate = Calendar.getInstance();
	Integer day = new Integer((String) examForm.get("day"));
	Integer month = new Integer((String) examForm.get("month"));
	Integer year = new Integer((String) examForm.get("year"));
	examDate.set(Calendar.YEAR, year.intValue());
	examDate.set(Calendar.MONTH, month.intValue() - 1);
	examDate.set(Calendar.DAY_OF_MONTH, day.intValue());

	int dayOfWeekInt = examDate.get(Calendar.DAY_OF_WEEK);
	DiaSemana dayOfWeek = new DiaSemana(dayOfWeekInt);
	if (dayOfWeekInt == Calendar.SUNDAY) {
	    addErrorMessage(request, "error.sunday", "error.sunday");
	    return prepare(mapping, form, request, response);
	}


	List<InfoRoom> availableInfoRoom = (List<InfoRoom>) ReadAvailableRoomsForExam.run(YearMonthDay.fromCalendarFields(examDate), YearMonthDay.fromCalendarFields(examDate),
		HourMinuteSecond.fromCalendarFields(examStartTime), HourMinuteSecond.fromCalendarFields(examEndTime), dayOfWeek,
		null, null, Boolean.FALSE);

	String[] rooms = (String[]) examForm.get("rooms");
	List<InfoRoom> selectedRooms = new ArrayList<InfoRoom>();
	List<InfoRoom> occupiedSelectedRooms = new ArrayList<InfoRoom>();

	if (rooms != null && rooms.length > 0) {

	    for (int iterRooms = 0; iterRooms < rooms.length; iterRooms++) {

		InfoRoom infoRoom = (InfoRoom) ReadRoomByOID.run(Integer.valueOf(rooms[iterRooms]));

		selectedRooms.add(infoRoom);
	    }

	    for (int iterSR = 0; iterSR < selectedRooms.size(); iterSR++) {
		InfoRoom selectedInfoRoom = (InfoRoom) selectedRooms.get(iterSR);

		boolean infoContida = false;

		for (int iterAIF = 0; iterAIF < availableInfoRoom.size(); iterAIF++) {
		    InfoRoom availInfoRoom = (InfoRoom) availableInfoRoom.get(iterAIF);

		    if (selectedInfoRoom.equals(availInfoRoom)) {
			infoContida = true;
			break;
		    }
		}

		if (!infoContida) {
		    // room occupied
		    occupiedSelectedRooms.add(selectedInfoRoom);
		}
	    }

	    for (int iterOSR = 0; iterOSR < occupiedSelectedRooms.size(); iterOSR++) {
		InfoRoom occupiedInfoRoom = (InfoRoom) occupiedSelectedRooms.get(iterOSR);
		selectedRooms.remove(occupiedInfoRoom);
	    }

	    request.setAttribute("rooms", selectedRooms);
	}

	return mapping.findForward("showCreateForm");
    }

}