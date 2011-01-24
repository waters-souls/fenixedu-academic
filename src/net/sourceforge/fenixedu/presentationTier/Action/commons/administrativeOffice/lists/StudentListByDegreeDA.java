package net.sourceforge.fenixedu.presentationTier.Action.commons.administrativeOffice.lists;

import static net.sourceforge.fenixedu.util.StringUtils.EMPTY;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sourceforge.fenixedu.applicationTier.Filtro.exception.FenixFilterException;
import net.sourceforge.fenixedu.applicationTier.Servico.exceptions.FenixServiceException;
import net.sourceforge.fenixedu.commons.CollectionUtils;
import net.sourceforge.fenixedu.dataTransferObject.administrativeOffice.lists.SearchStudentsByDegreeParametersBean;
import net.sourceforge.fenixedu.dataTransferObject.student.RegistrationConclusionBean;
import net.sourceforge.fenixedu.dataTransferObject.student.RegistrationWithStateForExecutionYearBean;
import net.sourceforge.fenixedu.domain.Degree;
import net.sourceforge.fenixedu.domain.DegreeCurricularPlan;
import net.sourceforge.fenixedu.domain.ExecutionDegree;
import net.sourceforge.fenixedu.domain.ExecutionYear;
import net.sourceforge.fenixedu.domain.Person;
import net.sourceforge.fenixedu.domain.StudentCurricularPlan;
import net.sourceforge.fenixedu.domain.degree.DegreeType;
import net.sourceforge.fenixedu.domain.degreeStructure.CycleType;
import net.sourceforge.fenixedu.domain.student.Registration;
import net.sourceforge.fenixedu.domain.student.RegistrationAgreement;
import net.sourceforge.fenixedu.domain.student.StudentStatuteType;
import net.sourceforge.fenixedu.domain.student.registrationStates.RegistrationState;
import net.sourceforge.fenixedu.domain.student.registrationStates.RegistrationStateType;
import net.sourceforge.fenixedu.domain.studentCurriculum.BranchCurriculumGroup;
import net.sourceforge.fenixedu.domain.studentCurriculum.CycleCurriculumGroup;
import net.sourceforge.fenixedu.presentationTier.Action.base.FenixDispatchAction;
import net.sourceforge.fenixedu.util.BundleUtil;
import net.sourceforge.fenixedu.util.StringUtils;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import pt.ist.fenixWebFramework.renderers.utils.RenderUtils;
import pt.utl.ist.fenix.tools.util.excel.StyledExcelSpreadsheet;

/**
 * @author - Shezad Anavarali (shezad@ist.utl.pt)
 * @author - �ngela Almeida (argelina@ist.utl.pt)
 * 
 */

public abstract class StudentListByDegreeDA extends FenixDispatchAction {

    protected static final String RESOURCE_MODULE = "academicAdminOffice";

    private static final String YMD_FORMAT = "yyyy-MM-dd";

    public ActionForward prepareByDegree(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request,
	    HttpServletResponse response) {

	request.setAttribute("searchParametersBean", getOrCreateSearchParametersBean());
	return mapping.findForward("searchRegistrations");
    }

    public ActionForward postBack(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request,
	    HttpServletResponse response) {

	final SearchStudentsByDegreeParametersBean searchParametersBean = getOrCreateSearchParametersBean();
	RenderUtils.invalidateViewState();
	request.setAttribute("searchParametersBean", searchParametersBean);

	return mapping.findForward("searchRegistrations");
    }

    private SearchStudentsByDegreeParametersBean getOrCreateSearchParametersBean() {
	SearchStudentsByDegreeParametersBean bean = getRenderedObject("searchParametersBean");
	if (bean == null) {
	    bean = new SearchStudentsByDegreeParametersBean(getAdministratedDegreeTypes(), getAdministratedDegrees());
	}
	return bean;
    }

    public ActionForward searchByDegree(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request,
	    HttpServletResponse response) throws FenixFilterException, FenixServiceException {

	final SearchStudentsByDegreeParametersBean searchBean = getOrCreateSearchParametersBean();

	final List<RegistrationWithStateForExecutionYearBean> registrations = search(searchBean);

	request.setAttribute("searchParametersBean", searchBean);
	request.setAttribute("studentCurricularPlanList", registrations);

	return mapping.findForward("searchRegistrations");
    }

    private static List<RegistrationWithStateForExecutionYearBean> search(final SearchStudentsByDegreeParametersBean searchbean) {

	final Set<Registration> registrations = new TreeSet<Registration>(Registration.COMPARATOR_BY_NUMBER_AND_ID);

	final Degree chosenDegree = searchbean.getDegree();
	final DegreeType chosenDegreeType = searchbean.getDegreeType();
	final ExecutionYear executionYear = searchbean.getExecutionYear();
	for (final ExecutionDegree executionDegree : executionYear.getExecutionDegreesSet()) {
	    final DegreeCurricularPlan degreeCurricularPlan = executionDegree.getDegreeCurricularPlan();
	    if ((chosenDegreeType != null && degreeCurricularPlan.getDegreeType() != chosenDegreeType)) {
		continue;
	    }
	    if (chosenDegree != null && degreeCurricularPlan.getDegree() != chosenDegree) {
		continue;
	    }
	    if (degreeCurricularPlan.getDegreeType() != DegreeType.EMPTY) {
		if (!searchbean.getAdministratedDegreeTypes().contains(degreeCurricularPlan.getDegreeType())) {
		    continue;
		}
		if (!searchbean.getAdministratedDegrees().contains(degreeCurricularPlan.getDegree())) {
		    continue;
		}
	    }
	    degreeCurricularPlan.getRegistrations(executionYear, registrations);
	}
	return filterResults(searchbean, registrations, executionYear);
    }

    private static List<RegistrationWithStateForExecutionYearBean> filterResults(SearchStudentsByDegreeParametersBean searchBean,
	    final Set<Registration> registrations, final ExecutionYear executionYear) {
	final List<RegistrationWithStateForExecutionYearBean> result = new ArrayList<RegistrationWithStateForExecutionYearBean>();
	for (final Registration registration : registrations) {
	    if (searchBean.hasAnyRegistrationAgreements()
		    && !searchBean.getRegistrationAgreements().contains(registration.getRegistrationAgreement())) {
		continue;
	    }

	    if (searchBean.hasAnyStudentStatuteType() && !hasStudentStatuteType(searchBean, registration)) {
		continue;
	    }

	    final RegistrationState lastRegistrationState = registration.getLastRegistrationState(executionYear);
	    if (lastRegistrationState == null) {
		continue;
	    }
	    if (searchBean.hasAnyRegistrationStateTypes()
		    && !searchBean.getRegistrationStateTypes().contains(lastRegistrationState.getStateType())) {
		continue;
	    }

	    if ((searchBean.isIngressedInChosenYear()) && (registration.getIngressionYear() != executionYear)) {
		continue;
	    }

	    if (searchBean.isConcludedInChosenYear()) {
		CycleType cycleType;
		if (searchBean.getDegreeType() != null) {
		    cycleType = searchBean.getDegreeType().getCycleType();
		} else {
		    cycleType = registration.getCycleType(executionYear);
		}

		RegistrationConclusionBean conclusionBean;
		if (registration.isBolonha()) {
		    conclusionBean = new RegistrationConclusionBean(registration, cycleType);
		} else {
		    conclusionBean = new RegistrationConclusionBean(registration);
		}

		if (!conclusionBean.isConcluded()) {
		    continue;
		}
		if (conclusionBean.getConclusionYear() != executionYear) {
		    continue;
		}
	    }

	    if (searchBean.getActiveEnrolments() && !registration.hasAnyEnrolmentsIn(executionYear)) {
		continue;
	    }

	    if (searchBean.getStandaloneEnrolments() && !registration.hasAnyStandaloneEnrolmentsIn(executionYear)) {
		continue;
	    }

	    if ((searchBean.getRegime() != null) && (registration.getRegimeType(executionYear) != searchBean.getRegime())) {
		continue;
	    }

	    if ((searchBean.getNationality() != null) && (registration.getPerson().getCountry() != searchBean.getNationality())) {
		continue;
	    }

	    if ((searchBean.getIngression() != null) && (registration.getIngression() != searchBean.getIngression())) {
		continue;
	    }

	    result.add(new RegistrationWithStateForExecutionYearBean(registration, lastRegistrationState.getStateType(),
		    executionYear));
	}
	return result;
    }

    static private boolean hasStudentStatuteType(final SearchStudentsByDegreeParametersBean searchBean,
	    final Registration registration) {
	return CollectionUtils.containsAny(searchBean.getStudentStatuteTypes(), registration.getStudent()
		.getStatutesTypesValidOnAnyExecutionSemesterFor(searchBean.getExecutionYear()));
    }

    public ActionForward exportInfoToExcel(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request,
	    HttpServletResponse response) throws FenixServiceException, FenixFilterException {

	final SearchStudentsByDegreeParametersBean searchBean = getOrCreateSearchParametersBean();
	if (searchBean == null) {
	    return null;
	}
	final List<RegistrationWithStateForExecutionYearBean> registrations = search(searchBean);

	try {
	    String filename = getResourceMessage("label.students");

	    Degree degree = searchBean.getDegree();
	    DegreeType degreeType = searchBean.getDegreeType();
	    ExecutionYear executionYear = searchBean.getExecutionYear();
	    if (degree != null) {
		filename += "_" + degree.getNameFor(executionYear).getContent().replace(' ', '_');
	    } else if (degreeType != null) {
		filename += "_" + BundleUtil.getEnumName(degreeType).replace(' ', '_');
	    }
	    filename += "_" + executionYear.getYear();

	    response.setContentType("application/vnd.ms-excel");
	    response.setHeader("Content-disposition", "attachment; filename=" + filename + ".xls");
	    ServletOutputStream writer = response.getOutputStream();

	    final String param = request.getParameter("extendedInfo");
	    boolean extendedInfo = param != null && param.length() > 0 && Boolean.valueOf(param).booleanValue();

	    exportToXls(registrations, writer, searchBean, extendedInfo);
	    writer.flush();
	    response.flushBuffer();
	    return null;

	} catch (IOException e) {
	    throw new FenixServiceException();
	}
    }

    private void exportToXls(List<RegistrationWithStateForExecutionYearBean> registrationList, OutputStream outputStream,
	    SearchStudentsByDegreeParametersBean searchBean, boolean extendedInfo) throws IOException {

	final StyledExcelSpreadsheet spreadsheet = new StyledExcelSpreadsheet(
		getResourceMessage("lists.studentByDegree.unspaced"));
	fillSpreadSheetFilters(searchBean, spreadsheet);
	fillSpreadSheetResults(registrationList, spreadsheet, searchBean.getExecutionYear(), extendedInfo);
	spreadsheet.getWorkbook().write(outputStream);
    }

    private void fillSpreadSheetFilters(SearchStudentsByDegreeParametersBean searchBean, final StyledExcelSpreadsheet spreadsheet) {
	spreadsheet.newHeaderRow();
	if (searchBean.isIngressedInChosenYear()) {
	    spreadsheet.addHeader(getResourceMessage("label.ingressedInChosenYear"));
	}
	spreadsheet.newHeaderRow();
	if (searchBean.isConcludedInChosenYear()) {
	    spreadsheet.addHeader(getResourceMessage("label.concludedInChosenYear"));
	}
	spreadsheet.newHeaderRow();
	if (searchBean.getActiveEnrolments()) {
	    spreadsheet.addHeader(getResourceMessage("label.activeEnrolments.capitalized"));
	}
	spreadsheet.newHeaderRow();
	if (searchBean.getStandaloneEnrolments()) {
	    spreadsheet.addHeader(getResourceMessage("label.withStandaloneEnrolments"));
	}
	spreadsheet.newHeaderRow();
	if (searchBean.getRegime() != null) {
	    spreadsheet.addHeader(getResourceMessage("registration.regime") + ": "
		    + BundleUtil.getEnumName(searchBean.getRegime()));
	}
	spreadsheet.newHeaderRow();
	if (searchBean.getNationality() != null) {
	    spreadsheet.addHeader(getResourceMessage("label.nationality") + ": " + searchBean.getNationality().getName());
	}
	spreadsheet.newHeaderRow();
	if (searchBean.getIngression() != null) {
	    spreadsheet.addHeader(getResourceMessage("label.ingression.short") + ": "
		    + BundleUtil.getEnumName(searchBean.getIngression()));
	}

	spreadsheet.newHeaderRow();
	if (searchBean.hasAnyRegistrationAgreements()) {
	    spreadsheet.addHeader(getResourceMessage("label.registrationAgreement") + ":");
	    for (RegistrationAgreement agreement : searchBean.getRegistrationAgreements()) {
		spreadsheet.addHeader(BundleUtil.getEnumName(agreement));
	    }
	}
	spreadsheet.newHeaderRow();
	if (searchBean.hasAnyRegistrationStateTypes()) {
	    spreadsheet.addHeader(getResourceMessage("label.registrationState") + ":");
	    for (RegistrationStateType state : searchBean.getRegistrationStateTypes()) {
		spreadsheet.addHeader(BundleUtil.getEnumName(state));
	    }
	}
	spreadsheet.newHeaderRow();
	if (searchBean.hasAnyStudentStatuteType()) {
	    spreadsheet.addHeader(getResourceMessage("label.statutes") + ":");
	    for (StudentStatuteType statute : searchBean.getStudentStatuteTypes()) {
		spreadsheet.addHeader(BundleUtil.getEnumName(statute));
	    }
	}
    }

    private void fillSpreadSheetResults(List<RegistrationWithStateForExecutionYearBean> registrations,
	    final StyledExcelSpreadsheet spreadsheet, ExecutionYear executionYear, boolean extendedInfo) {
	spreadsheet.newRow();
	spreadsheet.newRow();
	spreadsheet.addCell(registrations.size() + " " + getResourceMessage("label.students"));

	setHeaders(spreadsheet, extendedInfo);
	for (RegistrationWithStateForExecutionYearBean registrationWithStateForExecutionYearBean : registrations) {

	    final Registration registration = registrationWithStateForExecutionYearBean.getRegistration();
	    spreadsheet.newRow();

	    final Degree degree = registration.getDegree();
	    spreadsheet.addCell(!(StringUtils.isEmpty(degree.getSigla())) ? degree.getSigla() : degree.getNameFor(executionYear)
		    .toString());
	    spreadsheet.addCell(degree.getFilteredName(executionYear));
	    spreadsheet.addCell(registration.getNumber().toString());

	    final Person person = registration.getPerson();
	    spreadsheet.addCell(person.getName());
	    spreadsheet.addCell(person.getDocumentIdNumber());

	    final RegistrationState lastRegistrationState = registration.getLastRegistrationState(executionYear);
	    spreadsheet.addCell(lastRegistrationState.getStateType().getDescription());
	    spreadsheet.addCell(registration.getRegistrationAgreement().getName());

	    if (extendedInfo) {
		spreadsheet.addCell(person.getCountry() == null ? EMPTY : person.getCountry().getName());
		spreadsheet.addCell(person.getDefaultEmailAddress() == null ? EMPTY : person.getDefaultEmailAddress().getValue());
		spreadsheet.addCell(getFullAddress(person));
		spreadsheet.addCell(person.hasDefaultMobilePhone() ? person.getDefaultMobilePhoneNumber() : EMPTY);
		spreadsheet.addCell(person.getGender().toLocalizedString());
		spreadsheet.addCell(person.getDateOfBirthYearMonthDay() == null ? EMPTY : person.getDateOfBirthYearMonthDay()
			.toString(YMD_FORMAT));
		spreadsheet.addCell(registration.getEnrolmentsExecutionYears().size());
		spreadsheet.addCell(registration.getCurricularYear(executionYear));
		spreadsheet.addCell(registration.getEnrolments(executionYear).size());
		spreadsheet.addCell(BundleUtil.getEnumName(registration.getRegimeType(executionYear)));

		fillSpreadSheetPreBolonhaInfo(spreadsheet, registration);

		spreadsheet.addCell(getResourceMessage(registration.getStudent().isSenior(executionYear) ? "label.yes"
			: "label.no"));

		final StudentCurricularPlan studentCurricularPlan = registration.getLastStudentCurricularPlan();

		if (getAdministratedCycleTypes().contains(CycleType.FIRST_CYCLE)) {
		    fillSpreadSheetBolonhaInfo(spreadsheet, registration, studentCurricularPlan.getCycle(CycleType.FIRST_CYCLE));
		}
		if (getAdministratedCycleTypes().contains(CycleType.SECOND_CYCLE)) {
		    fillSpreadSheetBolonhaInfo(spreadsheet, registration, studentCurricularPlan.getCycle(CycleType.SECOND_CYCLE));
		}
		if (getAdministratedCycleTypes().contains(CycleType.THIRD_CYCLE)) {
		    fillSpreadSheetBolonhaInfo(spreadsheet, registration, studentCurricularPlan.getCycle(CycleType.THIRD_CYCLE));
		}

		spreadsheet.addCell(registrationWithStateForExecutionYearBean.getPersonalDataAuthorization());

		addBranchsInformation(spreadsheet, studentCurricularPlan);
	    }
	}
    }

    private String getFullAddress(final Person person) {
	if (person.hasDefaultPhysicalAddress()) {
	    StringBuilder sb = new StringBuilder();

	    if (!StringUtils.isEmpty(person.getDefaultPhysicalAddress().getAddress())) {
		sb.append(person.getDefaultPhysicalAddress().getAddress()).append(" ");
	    }

	    if (!StringUtils.isEmpty(person.getDefaultPhysicalAddress().getArea())) {
		sb.append(person.getDefaultPhysicalAddress().getArea()).append(" ");
	    }

	    if (!StringUtils.isEmpty(person.getDefaultPhysicalAddress().getAreaCode())) {
		sb.append(person.getDefaultPhysicalAddress().getAreaCode()).append(" ");
	    }

	    if (!StringUtils.isEmpty(person.getDefaultPhysicalAddress().getAreaOfAreaCode())) {
		sb.append(person.getDefaultPhysicalAddress().getAreaOfAreaCode()).append(" ");
	    }

	    return StringUtils.isEmpty(sb.toString()) ? EMPTY : sb.toString();
	}

	return EMPTY;
    }

    private void addBranchsInformation(final StyledExcelSpreadsheet spreadsheet, final StudentCurricularPlan studentCurricularPlan) {

	final StringBuilder majorBranches = new StringBuilder();
	final StringBuilder minorBranches = new StringBuilder();

	for (final BranchCurriculumGroup group : studentCurricularPlan.getBranchCurriculumGroups()) {
	    if (group.isMajor()) {
		majorBranches.append(group.getName().toString()).append(",");
	    } else if (group.isMinor()) {
		minorBranches.append(group.getName().toString()).append(",");
	    }
	}

	spreadsheet.addCell(majorBranches.length() > 0 ? majorBranches.deleteCharAt(majorBranches.length() - 1).toString()
		: majorBranches.toString());

	spreadsheet.addCell(minorBranches.length() > 0 ? minorBranches.deleteCharAt(minorBranches.length() - 1).toString()
		: minorBranches.toString());
    }

    private void fillSpreadSheetPreBolonhaInfo(StyledExcelSpreadsheet spreadsheet, Registration registration) {
	if (!registration.isBolonha()) {
	    RegistrationConclusionBean registrationConclusionBean = new RegistrationConclusionBean(registration);
	    fillSpreadSheetRegistrationInfo(spreadsheet, registrationConclusionBean, registration.hasConcluded());
	} else {
	    fillSpreadSheetEmptyCells(spreadsheet);
	}
    }

    private void fillSpreadSheetBolonhaInfo(StyledExcelSpreadsheet spreadsheet, Registration registration,
	    CycleCurriculumGroup cycle) {
	if ((cycle != null) && (!cycle.isExternal())) {
	    RegistrationConclusionBean registrationConclusionBean = new RegistrationConclusionBean(registration, cycle);
	    fillSpreadSheetRegistrationInfo(spreadsheet, registrationConclusionBean, registrationConclusionBean.isConcluded());
	} else {
	    fillSpreadSheetEmptyCells(spreadsheet);
	}
    }

    private void fillSpreadSheetRegistrationInfo(StyledExcelSpreadsheet spreadsheet,
	    RegistrationConclusionBean registrationConclusionBean, boolean isConcluded) {
	spreadsheet.addCell(getResourceMessage("label." + (isConcluded ? "yes" : "no") + ".capitalized"));
	spreadsheet.addCell(isConcluded ? registrationConclusionBean.getConclusionDate().toString(YMD_FORMAT) : EMPTY);
	spreadsheet.addCell(registrationConclusionBean.getAverage().toString());
	spreadsheet.addCell(getResourceMessage("label." + (registrationConclusionBean.isConclusionProcessed() ? "yes" : "no")
		+ ".capitalized"));
    }

    private void fillSpreadSheetEmptyCells(StyledExcelSpreadsheet spreadsheet) {
	spreadsheet.addCell(EMPTY);
	spreadsheet.addCell(EMPTY);
	spreadsheet.addCell(EMPTY);
	spreadsheet.addCell(EMPTY);
    }

    private void setHeaders(final StyledExcelSpreadsheet spreadsheet, final boolean extendedInfo) {
	spreadsheet.newHeaderRow();
	spreadsheet.addHeader(getResourceMessage("label.degree.acronym"));
	spreadsheet.addHeader(getResourceMessage("label.degree.name"));
	spreadsheet.addHeader(getResourceMessage("label.number"));
	spreadsheet.addHeader(getResourceMessage("label.name"));
	spreadsheet.addHeader(getResourceMessage("label.documentIdNumber"));
	spreadsheet.addHeader(getResourceMessage("label.registration.state"));
	spreadsheet.addHeader(getResourceMessage("label.registrationAgreement"));
	if (extendedInfo) {
	    spreadsheet.addHeader(getResourceMessage("label.nationality"));
	    spreadsheet.addHeader(getResourceMessage("label.email"));
	    spreadsheet.addHeader(getResourceMessage("label.person.title.addressInfo"));
	    spreadsheet.addHeader(getResourceMessage("label.person.title.contactInfo"));
	    spreadsheet.addHeader(getResourceMessage("label.gender"));
	    spreadsheet.addHeader(getResourceMessage("label.dateOfBirth"));
	    spreadsheet.addHeader(getResourceMessage("label.registration.enrolments.number.short"));
	    spreadsheet.addHeader(getResourceMessage("curricular.year"));
	    spreadsheet.addHeader(getResourceMessage("label.student.enrolments.number.short"));
	    spreadsheet.addHeader(getResourceMessage("registration.regime"));
	    spreadsheet.addHeader(getResourceMessage("degree.concluded"));
	    spreadsheet.addHeader(getResourceMessage("label.conclusionDate"));
	    spreadsheet.addHeader(getResourceMessage("degree.average"));
	    spreadsheet.addHeader(getResourceMessage("degree.hasConclusionProcess"));
	    spreadsheet.addHeader(getResourceMessage("student.is.senior"));

	    if (getAdministratedCycleTypes().contains(CycleType.FIRST_CYCLE)) {
		spreadsheet.addHeader(getResourceMessage("label.firstCycle.concluded"));
		spreadsheet.addHeader(getResourceMessage("label.firstCycle.conclusionDate"));
		spreadsheet.addHeader(getResourceMessage("label.firstCycle.average"));
		spreadsheet.addHeader(getResourceMessage("label.firstCycle.hasConclusionProcess"));
	    }
	    if (getAdministratedCycleTypes().contains(CycleType.SECOND_CYCLE)) {
		spreadsheet.addHeader(getResourceMessage("label.secondCycle.concluded"));
		spreadsheet.addHeader(getResourceMessage("label.secondCycle.conclusionDate"));
		spreadsheet.addHeader(getResourceMessage("label.secondCycle.average"));
		spreadsheet.addHeader(getResourceMessage("label.secondCycle.hasConclusionProcess"));
	    }
	    if (getAdministratedCycleTypes().contains(CycleType.THIRD_CYCLE)) {
		spreadsheet.addHeader(getResourceMessage("label.thirdCycle.concluded"));
		spreadsheet.addHeader(getResourceMessage("label.thirdCycle.conclusionDate"));
		spreadsheet.addHeader(getResourceMessage("label.thirdCycle.average"));
		spreadsheet.addHeader(getResourceMessage("label.thirdCycle.hasConclusionProcess"));
	    }

	    spreadsheet.addHeader(getResourceMessage("label.studentData.personalDataAuthorization"));

	    spreadsheet.addHeader(getResourceMessage("label.main.branch"));
	    spreadsheet.addHeader(getResourceMessage("label.minor.branch"));
	}
    }

    protected static String getResourceMessage(String key) {
	return BundleUtil.getMessageFromModuleOrApplication(RESOURCE_MODULE, key);
    }

    protected abstract Set<CycleType> getAdministratedCycleTypes();

    protected abstract Set<DegreeType> getAdministratedDegreeTypes();

    protected abstract Set<Degree> getAdministratedDegrees();
}