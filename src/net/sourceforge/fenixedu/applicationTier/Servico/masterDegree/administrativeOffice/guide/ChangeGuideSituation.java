/**
 * Autores : - Nuno Nunes (nmsn@rnl.ist.utl.pt) - Joana Mota
 * (jccm@rnl.ist.utl.pt)
 */

package net.sourceforge.fenixedu.applicationTier.Servico.masterDegree.administrativeOffice.guide;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.fenixedu.applicationTier.IUserView;
import net.sourceforge.fenixedu.applicationTier.Servico.ExcepcaoInexistente;
import net.sourceforge.fenixedu.applicationTier.Servico.exceptions.ExistingServiceException;
import net.sourceforge.fenixedu.applicationTier.Servico.exceptions.FenixServiceException;
import net.sourceforge.fenixedu.applicationTier.Servico.exceptions.NonValidChangeServiceException;
import net.sourceforge.fenixedu.domain.DocumentType;
import net.sourceforge.fenixedu.domain.DomainFactory;
import net.sourceforge.fenixedu.domain.GuideState;
import net.sourceforge.fenixedu.domain.IExecutionDegree;
import net.sourceforge.fenixedu.domain.IGratuitySituation;
import net.sourceforge.fenixedu.domain.IGuide;
import net.sourceforge.fenixedu.domain.IGuideEntry;
import net.sourceforge.fenixedu.domain.IGuideSituation;
import net.sourceforge.fenixedu.domain.IPerson;
import net.sourceforge.fenixedu.domain.IPersonAccount;
import net.sourceforge.fenixedu.domain.IStudent;
import net.sourceforge.fenixedu.domain.degree.DegreeType;
import net.sourceforge.fenixedu.domain.transactions.IPaymentTransaction;
import net.sourceforge.fenixedu.domain.transactions.PaymentType;
import net.sourceforge.fenixedu.domain.transactions.TransactionType;
import net.sourceforge.fenixedu.persistenceTier.ExcepcaoPersistencia;
import net.sourceforge.fenixedu.persistenceTier.IPersistentGratuitySituation;
import net.sourceforge.fenixedu.persistenceTier.IPersistentPersonAccount;
import net.sourceforge.fenixedu.persistenceTier.IPessoaPersistente;
import net.sourceforge.fenixedu.persistenceTier.ISuportePersistente;
import net.sourceforge.fenixedu.persistenceTier.PersistenceSupportFactory;
import net.sourceforge.fenixedu.persistenceTier.exceptions.ExistingPersistentException;
import net.sourceforge.fenixedu.persistenceTier.transactions.IPersistentInsuranceTransaction;
import net.sourceforge.fenixedu.persistenceTier.transactions.IPersistentTransaction;
import net.sourceforge.fenixedu.util.State;
import pt.utl.ist.berserk.logic.serviceManager.IService;

public class ChangeGuideSituation implements IService {

    public void run(Integer guideNumber, Integer guideYear, Integer guideVersion, Date paymentDate,
            String remarks, String situationOfGuideString, String paymentType, IUserView userView)
            throws ExcepcaoInexistente, FenixServiceException, ExistingPersistentException,
            ExcepcaoPersistencia {

        ISuportePersistente sp = PersistenceSupportFactory.getDefaultPersistenceSupport();
        IGuide guide = sp.getIPersistentGuide().readByNumberAndYearAndVersion(guideNumber, guideYear,
                guideVersion);

        if (guide == null) {
            throw new ExcepcaoInexistente("Unknown Guide !!");
        }
        GuideState situationOfGuide = GuideState.valueOf(situationOfGuideString);

        IPessoaPersistente persistentPerson = sp.getIPessoaPersistente();
        IPerson employeePerson = persistentPerson.lerPessoaPorUsername(userView.getUtilizador());

        IGuideSituation guideSituation = DomainFactory.makeGuideSituation();
        for (Iterator iter = guide.getGuideSituations().iterator(); iter.hasNext();) {
            IGuideSituation guideSituationTemp = (IGuideSituation) iter.next();
            if (guideSituationTemp.getState().equals(new State(State.ACTIVE)))
                guideSituation = guideSituationTemp;

        }

        // check if the change is valid
        if (verifyChangeValidation(guideSituation, situationOfGuide) == false) {
            throw new NonValidChangeServiceException();
        }

        if (situationOfGuide.equals(guideSituation.getSituation())) {
            guideSituation.setRemarks(remarks);
            if (guideSituation.getSituation().equals(GuideState.PAYED)) {
                guide.setPaymentDate(paymentDate);
                guide.setPaymentType(PaymentType.valueOf(paymentType));
            }
            guide.getGuideSituations().add(guideSituation);
        } else {
            // Create The New Situation

            guideSituation.setState(new State(State.INACTIVE));

            IGuideSituation newGuideSituation = DomainFactory.makeGuideSituation();

            Calendar date = Calendar.getInstance();
            newGuideSituation.setDate(date.getTime());
            newGuideSituation.setGuide(guide);
            newGuideSituation.setRemarks(remarks);
            newGuideSituation.setSituation(situationOfGuide);
            newGuideSituation.setState(new State(State.ACTIVE));

            if (situationOfGuide.equals(GuideState.PAYED)) {
                guide.setPaymentDate(paymentDate);
                guide.setPaymentType(PaymentType.valueOf(paymentType));

                // For Transactions Creation
                IPersistentTransaction persistentTransaction = sp.getIPersistentTransaction();
                IPaymentTransaction paymentTransaction = null;
                IGratuitySituation gratuitySituation = null;
                IPersistentPersonAccount persistentPersonAccount = sp.getIPersistentPersonAccount();
                IPersonAccount personAccount = persistentPersonAccount.readByPerson(guide.getPerson()
                        .getIdInternal());

                if (personAccount == null) {
                    personAccount = DomainFactory.makePersonAccount(guide.getPerson());
                }

                IPersistentGratuitySituation persistentGratuitySituation = sp
                        .getIPersistentGratuitySituation();

                // Iterate Guide Entries to create Transactions
                IGuideEntry guideEntry = null;
                Iterator guideEntryIterator = guide.getGuideEntries().iterator();
                while (guideEntryIterator.hasNext()) {

                    guideEntry = (IGuideEntry) guideEntryIterator.next();

                    IPerson studentPerson = guide.getPerson();

                    IStudent student = sp.getIPersistentStudent().readByPersonAndDegreeType(
                            studentPerson.getIdInternal(), DegreeType.MASTER_DEGREE);

                    IExecutionDegree executionDegree = guide.getExecutionDegree();

                    // Write Gratuity Transaction
                    if (guideEntry.getDocumentType().equals(DocumentType.GRATUITY)) {

                        executionDegree = guide.getExecutionDegree();
                        gratuitySituation = persistentGratuitySituation
                                .readGratuitySituationByExecutionDegreeAndStudent(executionDegree
                                        .getIdInternal(), student.getIdInternal());
                        Double value = new Double(guideEntry.getPrice().doubleValue()
                                * guideEntry.getQuantity().intValue());

                        paymentTransaction = DomainFactory.makeGratuityTransaction(value, new Timestamp(
                                Calendar.getInstance().getTimeInMillis()), guideEntry.getDescription(),
                                guide.getPaymentType(), TransactionType.GRATUITY_ADHOC_PAYMENT,
                                Boolean.FALSE, employeePerson, personAccount, guideEntry,
                                gratuitySituation);

                        // Update GratuitySituation
                        Double remainingValue = gratuitySituation.getRemainingValue();

                        gratuitySituation.setRemainingValue(new Double(remainingValue.doubleValue()
                                - paymentTransaction.getValue().doubleValue()));

                    }

                    // Write Insurance Transaction
                    if (guideEntry.getDocumentType().equals(DocumentType.INSURANCE)) {

                        IPersistentInsuranceTransaction insuranceTransactionDAO = sp
                                .getIPersistentInsuranceTransaction();

                        List insuranceTransactionList = insuranceTransactionDAO
                                .readAllNonReimbursedByExecutionYearAndStudent(executionDegree
                                        .getExecutionYear().getIdInternal(), student.getIdInternal());

                        if (insuranceTransactionList.isEmpty() == false) {
                            throw new ExistingServiceException(
                                    "error.message.transaction.insuranceTransactionAlreadyExists");
                        }

                        paymentTransaction = DomainFactory.makeInsuranceTransaction(guideEntry
                                .getPrice(), new Timestamp(Calendar.getInstance().getTimeInMillis()),
                                guideEntry.getDescription(), guide.getPaymentType(),
                                TransactionType.INSURANCE_PAYMENT, Boolean.FALSE, guide.getPerson(),
                                personAccount, guideEntry, executionDegree.getExecutionYear(), student);

                    }

                }

            }

            // Write the new Situation

            guide.getGuideSituations().add(newGuideSituation);

        }

    }

    private boolean verifyChangeValidation(IGuideSituation activeGuideSituation,
            GuideState situationOfGuide) {
        if (activeGuideSituation.equals(GuideState.ANNULLED))
            return false;

        if ((activeGuideSituation.getSituation().equals(GuideState.PAYED))
                && (situationOfGuide.equals(GuideState.NON_PAYED)))
            return false;

        return true;
    }

}