/*
 * 
 * Created on 2003/08/13
 */

package net.sourceforge.fenixedu.applicationTier.Servico.resourceAllocationManager;

import pt.ist.fenixWebFramework.services.Service;

import pt.ist.fenixWebFramework.security.accessControl.Checked;

/**
 * @author Luis Cruz & Sara Ribeiro
 * 
 */
import java.util.List;
import java.util.Set;

import net.sourceforge.fenixedu.applicationTier.FenixService;
import net.sourceforge.fenixedu.dataTransferObject.InfoClass;
import net.sourceforge.fenixedu.dataTransferObject.InfoShift;
import net.sourceforge.fenixedu.domain.SchoolClass;
import net.sourceforge.fenixedu.domain.Shift;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;

public class ReadAvailableShiftsForClass extends FenixService {

    @Checked("RolePredicates.RESOURCE_ALLOCATION_MANAGER_PREDICATE")
    @Service
    public static Object run(InfoClass infoClass) {

	List infoShifts = null;

	SchoolClass schoolClass = rootDomainObject.readSchoolClassByOID(infoClass.getIdInternal());
	Set<Shift> shifts = schoolClass.findAvailableShifts();

	infoShifts = (List) CollectionUtils.collect(shifts, new Transformer() {
	    public Object transform(Object arg0) {
		Shift shift = (Shift) arg0;
		return InfoShift.newInfoFromDomain(shift);
	    }
	});

	return infoShifts;
    }

}