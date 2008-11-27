/*
 * Created on 2004/04/21
 */
package net.sourceforge.fenixedu.applicationTier.Servico.student;

import pt.ist.fenixWebFramework.services.Service;

import pt.ist.fenixWebFramework.security.accessControl.Checked;

import net.sourceforge.fenixedu.applicationTier.FenixService;
import net.sourceforge.fenixedu.domain.finalDegreeWork.FinalDegreeWorkGroup;
import net.sourceforge.fenixedu.domain.finalDegreeWork.GroupProposal;

/**
 * @author Luis Cruz
 */
public class ChangePreferenceOrderOfFinalDegreeWorkStudentGroupCandidacy extends FenixService {

    public ChangePreferenceOrderOfFinalDegreeWorkStudentGroupCandidacy() {
	super();
    }

    @Checked("RolePredicates.STUDENT_PREDICATE")
    @Service
    public static boolean run(Integer groupOID, Integer groupProposalOID, Integer orderOfPreference) {
	FinalDegreeWorkGroup group = rootDomainObject.readFinalDegreeWorkGroupByOID(groupOID);
	GroupProposal groupProposal = rootDomainObject.readGroupProposalByOID(groupProposalOID);
	if (group != null && groupProposal != null) {
	    for (int i = 0; i < group.getGroupProposals().size(); i++) {
		GroupProposal otherGroupProposal = group.getGroupProposals().get(i);
		if (otherGroupProposal != null && !groupProposal.getIdInternal().equals(otherGroupProposal.getIdInternal())) {
		    int otherOrderOfPreference = otherGroupProposal.getOrderOfPreference().intValue();
		    if (orderOfPreference.intValue() <= otherOrderOfPreference
			    && groupProposal.getOrderOfPreference().intValue() > otherOrderOfPreference) {
			otherGroupProposal.setOrderOfPreference(new Integer(otherOrderOfPreference + 1));
		    } else if (orderOfPreference.intValue() >= otherOrderOfPreference
			    && groupProposal.getOrderOfPreference().intValue() < otherOrderOfPreference) {
			otherGroupProposal.setOrderOfPreference(new Integer(otherOrderOfPreference - 1));
		    }
		}
	    }
	    groupProposal.setOrderOfPreference(orderOfPreference);
	}
	return true;
    }

}