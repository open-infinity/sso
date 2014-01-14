package org.openinfinity.sso.userprovisioning.bpmn.common;

import org.openinfinity.core.annotation.AuditTrail;
import org.openinfinity.core.annotation.Log;
import org.openinfinity.core.exception.BusinessViolationException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Qualifier("networkOrderValidationService")
public class NetworkOrderValidationService {

	@Log
	@AuditTrail
	public void validate(NetworkOrder networkOrder) {
		if (networkOrder.getContractId() == null ) {
			throw new BusinessViolationException("Validation of network order failed.");
		}
		if (networkOrder.getOrderType() == null) {
			throw new BusinessViolationException("Validation of network order failed.");
		}
	}
	
}
