/*
 * Copyright (c) 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openinfinity.sso.identityprovisioning.bpmn;

import java.io.Serializable;
import java.util.Date;

/**
 * Simple task abstraction for holding information about the user task.
 * 
 * @author Ilkka Leinonen
 * @version 1.0.0
 * @since 1.0.0
 */
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