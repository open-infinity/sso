package org.openinfinity.sso.identityprovisioning.bpmn;

import java.io.Serializable;
import java.util.Date;

public class Task implements Serializable {

	private String id;
	
	private String name;
	
	private String description;
	
	private int priority;

	private String assignee;

	private String owner;
	
	private String processInstanceId;
	
	private String processExecutionId;
	
	private String taskDefinitionKey;
	
	private Date dueDate;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	protected void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	protected void setDescription(String description) {
		this.description = description;
	}

	public int getPriority() {
		return priority;
	}

	protected void setPriority(int priority) {
		this.priority = priority;
	}

	public String getAssignee() {
		return assignee;
	}

	protected void setAssignee(String assignee) {
		this.assignee = assignee;
	}

	public String getOwner() {
		return owner;
	}

	protected void setOwner(String owner) {
		this.owner = owner;
	}

	public String getProcessInstanceId() {
		return processInstanceId;
	}

	protected void setProcessInstanceId(String processInstanceId) {
		this.processInstanceId = processInstanceId;
	}

	public String getProcessExecutionId() {
		return processExecutionId;
	}

	protected void setProcessExecutionId(String processExecutionId) {
		this.processExecutionId = processExecutionId;
	}

	public Date getDueDate() {
		return dueDate;
	}

	protected void setDueDate(Date dueDate) {
		this.dueDate = dueDate;
	}

	protected void setTaskDefinitionKey(String taskDefinitionKey) {
		this.taskDefinitionKey = taskDefinitionKey;
	}

	public String getTaskDefinitionKey() {
		return taskDefinitionKey;
	}
	
}