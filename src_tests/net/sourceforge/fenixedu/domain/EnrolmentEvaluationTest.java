package net.sourceforge.fenixedu.domain;

import java.util.Date;

import net.sourceforge.fenixedu.domain.curriculum.EnrollmentState;
import net.sourceforge.fenixedu.domain.curriculum.EnrolmentEvaluationType;
import net.sourceforge.fenixedu.domain.curriculum.GradeFactory;
import net.sourceforge.fenixedu.domain.curriculum.IGrade;
import net.sourceforge.fenixedu.util.EnrolmentEvaluationState;


public class EnrolmentEvaluationTest extends DomainTestBase {

	private IEnrolmentEvaluation normalEvaluation;
	private IEnrolmentEvaluation evaluationToClear;
	private IEnrolmentEvaluation evaluationWithoutExamDate;
    
    private IEnrolmentEvaluation enrolmentEvaluationA;
    private IEnrolmentEvaluation enrolmentEvaluationB;
    private IEnrolmentEvaluation enrolmentEvaluationC;
    private IEnrolmentEvaluation enrolmentEvaluationD;
    private IEnrolmentEvaluation enrolmentEvaluationE;
    private IEnrolmentEvaluation enrolmentEvaluationF;
	
	private IPerson newResponsibleFor;
	private String newGrade;
	private Date newAvailableDate;
	private Date newExamDate;
	private String newChecksum;
	
	private IEnrolmentEvaluation aprovedEvaluation;
	private IEnrolmentEvaluation notAprovedEvaluation;
	private IEnrolmentEvaluation notEvaluatedEvaluation;
	
	private IEmployee employee;
	private String observation;
	
	private IEnrolmentEvaluation evaluation;
	private IEnrolmentEvaluation improvementEvaluation;
	private IEnrolmentEvaluation notImprovementEvaluation;
	
	private IExecutionPeriod currentExecutionPeriod;
	private IAttends attendsToDelete;
	private IAttends attendsNotToDelete;
	
		
	private void setUpEdit() {
		
		normalEvaluation = new EnrolmentEvaluation();
		evaluationToClear = new EnrolmentEvaluation();
		evaluationWithoutExamDate = new EnrolmentEvaluation();
		
		newResponsibleFor = new Person();
		newGrade = "20";
		newAvailableDate = new Date();
		newExamDate = new Date();
		newChecksum = "";
	}
    
    private void setUpForGetEnrollmentStateByGradeCase() {
        enrolmentEvaluationA = new EnrolmentEvaluation();
        enrolmentEvaluationA.setGrade(null);
        
        enrolmentEvaluationB = new EnrolmentEvaluation();
        enrolmentEvaluationB.setGrade("");
        
        enrolmentEvaluationC = new EnrolmentEvaluation();
        enrolmentEvaluationC.setGrade("RE");
        
        enrolmentEvaluationD = new EnrolmentEvaluation();
        enrolmentEvaluationD.setGrade("NA");
        
        enrolmentEvaluationE = new EnrolmentEvaluation();
        enrolmentEvaluationE.setGrade("AP");
        
        enrolmentEvaluationF = new EnrolmentEvaluation();
        enrolmentEvaluationF.setGrade("15");
    }
    
    private void setUpForGetGradeWrapperCase() {
        enrolmentEvaluationA = new EnrolmentEvaluation();
        enrolmentEvaluationA.setGrade(null);
        
        enrolmentEvaluationB = new EnrolmentEvaluation();
        enrolmentEvaluationB.setGrade("");
        
        enrolmentEvaluationC = new EnrolmentEvaluation();
        enrolmentEvaluationC.setGrade("RE");
        
        enrolmentEvaluationD = new EnrolmentEvaluation();
        enrolmentEvaluationD.setGrade("NA");
        
        enrolmentEvaluationE = new EnrolmentEvaluation();
        enrolmentEvaluationE.setGrade("AP");
        
        enrolmentEvaluationF = new EnrolmentEvaluation();
        enrolmentEvaluationF.setGrade("15");
    }

	private void setUpConfirmSubmission() {
		aprovedEvaluation = new EnrolmentEvaluation();
		notAprovedEvaluation = new EnrolmentEvaluation();
		notEvaluatedEvaluation = new EnrolmentEvaluation();
		
		IEnrolment aprovedEnrolment = new Enrolment();
		aprovedEnrolment.addEvaluations(aprovedEvaluation);
		
		IEnrolment notAprovedEnrolment = new Enrolment();
		notAprovedEnrolment.addEvaluations(notAprovedEvaluation);
		
		IEnrolment notEvaluatedEnrolment = new Enrolment();
		notEvaluatedEnrolment.addEvaluations(notEvaluatedEvaluation);
		
		aprovedEvaluation.setGrade("20");
		notAprovedEvaluation.setGrade("RE");
		notEvaluatedEvaluation.setGrade("NA");
		
		employee = new Employee();
		aprovedEvaluation.setEmployee(employee);
		notAprovedEvaluation.setEmployee(employee);
		notEvaluatedEvaluation.setEmployee(employee);
		
		observation = "";
	}
	
	
	private void setUpDelete() {
		
		evaluation = new EnrolmentEvaluation();

		IPerson person = new Person();
		IEmployee employee = new Employee();
		IEnrolment enrolment = new Enrolment();
		
		evaluation.setPersonResponsibleForGrade(person);
		evaluation.setEmployee(employee);
		evaluation.setEnrolment(enrolment);
		
		
		improvementEvaluation = new EnrolmentEvaluation();
		notImprovementEvaluation = new EnrolmentEvaluation();
		
		improvementEvaluation.setEnrolment(enrolment);
		notImprovementEvaluation.setEnrolment(enrolment);
		
		improvementEvaluation.setEnrolmentEvaluationState(EnrolmentEvaluationState.TEMPORARY_OBJ);
		improvementEvaluation.setEnrolmentEvaluationType(EnrolmentEvaluationType.IMPROVEMENT);
		
		notImprovementEvaluation.setEnrolmentEvaluationState(EnrolmentEvaluationState.TEMPORARY_OBJ);
		notImprovementEvaluation.setEnrolmentEvaluationType(EnrolmentEvaluationType.NORMAL);
		

		//associated attends
		currentExecutionPeriod = new ExecutionPeriod();
		IExecutionPeriod notCurrentExecutionPeriod = new ExecutionPeriod();
		
		attendsToDelete = new Attends();
		attendsNotToDelete = new Attends();
		
		ICurricularCourse curricularCourse = new CurricularCourse();
		
		IExecutionCourse currentExecutionCourse = new ExecutionCourse();
		IExecutionCourse notCurrentExecutionCourse = new ExecutionCourse();
		
		IStudentCurricularPlan studentCurricularPlan = new StudentCurricularPlan();
		IStudent student = new Student();
		
		enrolment.setCurricularCourse(curricularCourse);
		enrolment.setStudentCurricularPlan(studentCurricularPlan);
		enrolment.addAttends(attendsToDelete);
		enrolment.addAttends(attendsNotToDelete);
		curricularCourse.addAssociatedExecutionCourses(currentExecutionCourse);
		curricularCourse.addAssociatedExecutionCourses(notCurrentExecutionCourse);
		studentCurricularPlan.setStudent(student);
		attendsToDelete.setAluno(student);
		attendsNotToDelete.setAluno(student);
		attendsToDelete.setDisciplinaExecucao(currentExecutionCourse);
		attendsNotToDelete.setDisciplinaExecucao(notCurrentExecutionCourse);
		currentExecutionCourse.setExecutionPeriod(currentExecutionPeriod);
		notCurrentExecutionCourse.setExecutionPeriod(notCurrentExecutionPeriod);
	}

	public void testEdit() {
		
		setUpEdit();
				
		normalEvaluation.edit(newResponsibleFor, newGrade, newAvailableDate, newExamDate, newChecksum);

		assertTrue("Failed to assign personResponsibleForGrade", normalEvaluation.getPersonResponsibleForGrade().equals(newResponsibleFor));
		assertTrue("Failed to assign grade", normalEvaluation.getGrade().equals(newGrade));
		assertTrue("Failed to assign gradeAvailableDate", normalEvaluation.getGradeAvailableDate().equals(newAvailableDate));
		assertTrue("Failed to assign examDate", normalEvaluation.getExamDate().equals(newExamDate));
		assertTrue("Failed to assign checkSum", normalEvaluation.getCheckSum().equals(newChecksum));

		
		evaluationToClear.edit(null, null, null, null, null);
		
		assertFalse("Failed to clear personResponsibleForGrade", evaluationToClear.hasPersonResponsibleForGrade());
		assertNull("Failed to clear grade", evaluationToClear.getGrade());
		assertNull("Failed to clear gradeAvailableDate", evaluationToClear.getGradeAvailableDate());
		assertNull("Failed to clear examDate", evaluationToClear.getExamDate());
		assertNull("Failed to clear checkSum", evaluationToClear.getCheckSum());
		
		
		evaluationWithoutExamDate.edit(newResponsibleFor, newGrade, newAvailableDate, null, newChecksum);
				
		assertTrue("Failed to assign personResponsibleForGrade", evaluationWithoutExamDate.getPersonResponsibleForGrade().equals(newResponsibleFor));
		assertTrue("Failed to assign grade", evaluationWithoutExamDate.getGrade().equals(newGrade));
		assertTrue("Failed to assign gradeAvailableDate", evaluationWithoutExamDate.getGradeAvailableDate().equals(newAvailableDate));
		assertTrue("Failed to assign examDate", evaluationWithoutExamDate.getExamDate().equals(newAvailableDate));
		assertTrue("Failed to assign checkSum", evaluationWithoutExamDate.getCheckSum().equals(newChecksum));
	}
	
	
	public void testConfirmSubmission() {
		
		setUpConfirmSubmission();
				
		aprovedEvaluation.confirmSubmission(employee, observation);
		
		assertCorrectSubmissionConfirmation("Assignment of property failed on submission confirmation", 
				aprovedEvaluation, EnrolmentEvaluationState.FINAL_OBJ, 
				employee, observation, EnrollmentState.APROVED);

		
		notAprovedEvaluation.confirmSubmission(employee, observation);
		
		assertCorrectSubmissionConfirmation("Assignment of property failed on submission confirmation", 
				notAprovedEvaluation, EnrolmentEvaluationState.FINAL_OBJ, 
				employee, observation, EnrollmentState.NOT_APROVED);
		
		
		notEvaluatedEvaluation.confirmSubmission(employee, observation);

		assertCorrectSubmissionConfirmation("Assignment of property failed on submission confirmation", 
				notEvaluatedEvaluation, EnrolmentEvaluationState.FINAL_OBJ, 
				employee, observation, EnrollmentState.NOT_EVALUATED);		
	}
	
	
	public void testDelete() {
		
		setUpDelete();
				
		evaluation.delete();
		
		assertFalse("Failed to dereference personResponsibleForGrade", evaluation.hasPersonResponsibleForGrade());
		assertFalse("Failed to dereference employee", evaluation.hasEmployee());
		assertFalse("Failed to dereference enrolment", evaluation.hasEnrolment());
	}
	
	
//	public void testInsertStudentFinalEvaluationForMasterDegree() {
//		
//		setUpInsertStudentFinalEvaluationForMasterDegree();
//	
//		mdEvaluationToInsert.insertStudentFinalEvaluationForMasterDegree("20", newResponsibleFor, newExamDate);
//		
//		assertTrue(mdEvaluationToInsert.getGrade().equals("20"));
//		assertTrue(mdEvaluationToInsert.getPersonResponsibleForGrade().equals(newResponsibleFor));
//		assertTrue(mdEvaluationToInsert.getExamDate().equals(newExamDate));		
//		
//		mdEvaluationToInsert.insertStudentFinalEvaluationForMasterDegree("", newResponsibleFor, newExamDate);
//		
//		assertNull(mdEvaluationToInsert.getPersonResponsibleForGrade());
//		assertNull(mdEvaluationToInsert.getGrade());
//		assertNull(mdEvaluationToInsert.getGradeAvailableDate());
//		assertNull(mdEvaluationToInsert.getExamDate());
//		assertNull(mdEvaluationToInsert.getCheckSum());
//		
//		try {
//			mdEvaluationToInsert.insertStudentFinalEvaluationForMasterDegree("30", newResponsibleFor, newExamDate);
//			fail("Should not have been inserted.");
//		} catch (DomainException e) {
//			
//		}
//		
//		mdEvaluationToInsert.insertStudentFinalEvaluationForMasterDegree("RE", newResponsibleFor, newExamDate);
//		
//		assertTrue(mdEvaluationToInsert.getGrade().equals("RE"));
//		assertTrue(mdEvaluationToInsert.getPersonResponsibleForGrade().equals(newResponsibleFor));
//		assertTrue(mdEvaluationToInsert.getExamDate().equals(newExamDate));	
//	}
	
	private void assertCorrectSubmissionConfirmation(String errorMessagePrefix, IEnrolmentEvaluation evaluation, EnrolmentEvaluationState state, 
			IEmployee employee, String observation, EnrollmentState enrolmentState) {
		assertTrue(errorMessagePrefix + ": enrolmentEvaluationState", evaluation.getEnrolmentEvaluationState().equals(state));
		assertTrue(errorMessagePrefix + ": employee", evaluation.getEmployee().equals(employee));
		assertTrue(errorMessagePrefix + ": observation", evaluation.getObservation().equals(observation));
		assertTrue(errorMessagePrefix + ": enrolment.enrolmentState", evaluation.getEnrolment().getEnrollmentState().equals(enrolmentState));
	}
    
    public void testGetEnrollmentStateByGrade() {
        setUpForGetEnrollmentStateByGradeCase();
        /*IGrade reprovedGrade = GradeFactory.getInstance().getGrade("RE");
        IGrade notEvaluatedGrade = GradeFactory.getInstance().getGrade("NA");
        IGrade approvedGrade = GradeFactory.getInstance().getGrade("AP");
        IGrade numericGrade = GradeFactory.getInstance().getGrade("15");*/
        
        assertEquals("Grade reproved mismatch", EnrollmentState.NOT_EVALUATED, enrolmentEvaluationA.getEnrollmentStateByGrade());
        assertEquals("Grade reproved mismatch", EnrollmentState.NOT_EVALUATED, enrolmentEvaluationB.getEnrollmentStateByGrade());
        assertEquals("Grade reproved mismatch", EnrollmentState.NOT_APROVED, enrolmentEvaluationC.getEnrollmentStateByGrade());
        assertEquals("Grade not evaluated mismatch", EnrollmentState.NOT_EVALUATED, enrolmentEvaluationD.getEnrollmentStateByGrade());
        assertEquals("Grade approved mismatch", EnrollmentState.APROVED, enrolmentEvaluationE.getEnrollmentStateByGrade());
        assertEquals("Grade numeric mismatch", EnrollmentState.APROVED, enrolmentEvaluationF.getEnrollmentStateByGrade());
    }
    
    public void testGetGradeWrapper() {
        setUpForGetGradeWrapperCase();
        
        IGrade reprovedGrade = GradeFactory.getInstance().getGrade("RE");
        IGrade notEvaluatedGrade = GradeFactory.getInstance().getGrade("NA");
        IGrade approvedGrade = GradeFactory.getInstance().getGrade("AP");
        IGrade numericGrade = GradeFactory.getInstance().getGrade("15");
        
        assertEquals("Grade reproved mismatch", notEvaluatedGrade, enrolmentEvaluationA.getGradeWrapper());
        assertEquals("Grade reproved mismatch", notEvaluatedGrade, enrolmentEvaluationB.getGradeWrapper());
        assertEquals("Grade reproved mismatch", reprovedGrade, enrolmentEvaluationC.getGradeWrapper());
        assertEquals("Grade not evaluated mismatch", notEvaluatedGrade, enrolmentEvaluationD.getGradeWrapper());
        assertEquals("Grade approved mismatch", approvedGrade, enrolmentEvaluationE.getGradeWrapper());
        assertEquals("Grade numeric mismatch", numericGrade, enrolmentEvaluationF.getGradeWrapper());
    }
}
