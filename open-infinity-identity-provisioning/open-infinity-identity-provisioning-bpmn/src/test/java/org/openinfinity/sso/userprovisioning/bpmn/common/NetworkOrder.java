package org.openinfinity.sso.userprovisioning.bpmn.common;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

import javax.validation.constraints.NotNull;

public class NetworkOrder implements Serializable {

	private Long id;

	@NotNull 
	String contractId;
	@NotNull
	String ssn;
	
	

	@NotNull
	String orderType;

	boolean statusFailure;

	public static AtomicLong getIdsequence() {
		return idSequence;
	}

	public Long assignId() {
		this.id = idSequence.incrementAndGet();
		return id;
	}

	private static final AtomicLong idSequence = new AtomicLong();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getContractId() {
		return contractId;
	}

	public void setContractId(String contractId) {
		this.contractId = contractId;
	}

	public String getSsn() {
		return ssn;
	}

	public void setSsn(String ssn) {
		this.ssn = ssn;
	}

	public String getOrderType() {
		return orderType;
	}

	public NetworkOrder(String contractId, String ssn, String orderType,
			boolean statusFailure) {
		super();
		this.contractId = contractId;
		this.ssn = ssn;
		this.orderType = orderType;
		this.statusFailure = statusFailure;
	}

	public void setOrderType(String orderType) {
		this.orderType = orderType;
	}

	public boolean isStatusFailure() {
		return statusFailure;
	}

	public void setStatusFailure(boolean statusFailure) {
		this.statusFailure = statusFailure;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((contractId == null) ? 0 : contractId.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result
				+ ((orderType == null) ? 0 : orderType.hashCode());
		result = prime * result + ((ssn == null) ? 0 : ssn.hashCode());
		result = prime * result + (statusFailure ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NetworkOrder other = (NetworkOrder) obj;
		if (contractId == null) {
			if (other.contractId != null)
				return false;
		} else if (!contractId.equals(other.contractId))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (orderType == null) {
			if (other.orderType != null)
				return false;
		} else if (!orderType.equals(other.orderType))
			return false;
		if (ssn == null) {
			if (other.ssn != null)
				return false;
		} else if (!ssn.equals(other.ssn))
			return false;
		if (statusFailure != other.statusFailure)
			return false;
		return true;
	}

	public NetworkOrder(Long id, String contractId, String ssn,
			String orderType, boolean statusFailure) {
		super();
		this.id = id;
		this.contractId = contractId;
		this.ssn = ssn;
		this.orderType = orderType;
		this.statusFailure = statusFailure;
	}

	public NetworkOrder() {
		super();
	}

	
}