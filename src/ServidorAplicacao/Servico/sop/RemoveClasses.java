/*
 *
 * Created on 2003/08/15
 */

package ServidorAplicacao.Servico.sop;

/**
 * Servi�o AdicionarTurno.
 * 
 * @author Luis Cruz & Sara Ribeiro
 */
import java.util.List;

import pt.utl.ist.berserk.logic.serviceManager.IService;
import DataBeans.InfoShift;
import Dominio.ITurma;
import Dominio.ITurno;
import Dominio.Turma;
import Dominio.Turno;
import ServidorAplicacao.Servico.exceptions.FenixServiceException;
import ServidorPersistente.ExcepcaoPersistencia;
import ServidorPersistente.ISuportePersistente;
import ServidorPersistente.OJB.SuportePersistenteOJB;

public class RemoveClasses implements IService {

    public Boolean run(InfoShift infoShift, List classOIDs) throws FenixServiceException, ExcepcaoPersistencia {

        boolean result = false;

        ISuportePersistente sp = SuportePersistenteOJB.getInstance();

        ITurno shift = (ITurno) sp.getITurnoPersistente().readByOID(Turno.class,
                infoShift.getIdInternal());

        sp.getITurnoPersistente().simpleLockWrite(shift);

        for (int i = 0; i < classOIDs.size(); i++) {
            ITurma schoolClass = (ITurma) sp.getITurmaPersistente().readByOID(Turma.class,
                    (Integer) classOIDs.get(i));

            shift.getAssociatedClasses().remove(schoolClass);

            sp.getITurmaPersistente().simpleLockWrite(schoolClass);
            schoolClass.getAssociatedShifts().remove(shift);
        }

        result = true;

        return new Boolean(result);
    }

}