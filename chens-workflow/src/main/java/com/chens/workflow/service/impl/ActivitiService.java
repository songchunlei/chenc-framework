package com.chens.workflow.service.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chens.core.entity.workflow.WorkFlowRequestParam;
import com.chens.core.entity.workflow.WorkFlowReturn;
import com.chens.core.exception.BaseException;
import com.chens.core.exception.BaseExceptionEnum;
import com.chens.workflow.enums.ConditionEnum;
import com.chens.workflow.enums.WorkFlowEnum;
import com.chens.workflow.enums.WorkFlowGlobals;
import com.chens.workflow.enums.WorkFlowStatusEnum;
import com.chens.workflow.service.IWorkFlowService;
import com.chens.workflow.util.StreamUtils;
import com.chens.workflow.util.XmlActivitiUtil;

/**
 * 流程服务
 *
 * @auther songchunlei@qq.com
 * @create 2018/3/17
 */
@Service
@Transactional
public class ActivitiService implements IWorkFlowService {
	
    @Autowired
    private TaskService taskService;
    @Autowired
    private IdentityService identityService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private HistoryService historyService;
	
	@Override
    @Transactional
    public WorkFlowReturn startWorkflow(WorkFlowRequestParam workFlowRequestParam) {
		 WorkFlowReturn workFlowReturn = new WorkFlowReturn();
    	 String processDefinitionKey = workFlowRequestParam.getProcessDefinitionKey();
         String businessKey = workFlowRequestParam.getBusinessKey();
         Map<String,Object> variables = workFlowRequestParam.getVariables();
         if(variables == null){
             variables = new HashMap<String, Object>();
         }
         String nextUserId = workFlowRequestParam.getNextUserId();//下一个环节处理人 若无默认为流程发起人
         if (StringUtils.isBlank(nextUserId)) {
             nextUserId = workFlowRequestParam.getStartUserId();
         }
         String name = workFlowRequestParam.getVariableName();
         String value = workFlowRequestParam.getVariableValue();
         List<String> list = Arrays.asList(nextUserId.split(","));
         if(StringUtils.isNotBlank(name) && StringUtils.isNotBlank(value)){
             variables.put(name, value);
             variables.put("nextUserId", nextUserId);
             //启动时判断节点是否是会签节点
  /*           if(this.checkStartNextUserTaskIsHuiQian(processDefinitionKey,name, value)){
                 variables.put("assigneeUserIdList", list);
             }else{
            	 if(list.size() > 1){
            		 workFlowReturn.setStartSuccess(false);
            		 workFlowReturn.setMessage("单处理人任务节点只能选择一个处理人");
                	 return workFlowReturn;
            	 }
                 variables.put("nextUserId", nextUserId);
             }*/
         }else{
             if(this.checkActivitiIsHuiQian(processDefinitionKey)){
                 variables.put("assigneeUserIdList", list);
             }else{
            	 if(list.size() > 1){
            		 workFlowReturn.setStartSuccess(false);
            		 workFlowReturn.setMessage("单处理人任务节点只能选择一个处理人");
                	 return workFlowReturn;
            	 }
                 variables.put("nextUserId", nextUserId);
             } 
         }       
        
         /*考虑国际化 这个 应该要存在字典表中 流程状态 1处理中 2完成 3审核不通过 4，驳回修改，5 发起人删除，6 管理员关闭*/
         variables.put(WorkFlowGlobals.BPM_STATUS.toString(), WorkFlowStatusEnum.PROCESSING.getCode());
         //标准变量
         variables.put(WorkFlowGlobals.BPM_DATA_ID.toString(), businessKey);
                  
         
         List<ProcessInstance> piList = runtimeService.createProcessInstanceQuery().processInstanceBusinessKey(workFlowRequestParam.getBusinessKey()).list();
         if(CollectionUtils.isNotEmpty(piList)){
        	 workFlowReturn.setExistFlag("Y");
        	 workFlowReturn.setStartSuccess(false);
        	 workFlowReturn.setMessage("该条业务数据已发起流程，请勿重复发起");
        	 return workFlowReturn;
         }
      
        identityService.setAuthenticatedUserId(workFlowRequestParam.getStartUserId());// 设置流程发起人		
 		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(processDefinitionKey, businessKey, variables);
        if(processInstance == null){
        	throw new BaseException(BaseExceptionEnum.WORKFLOW_START_FAIL);
        }
        workFlowReturn.setStartSuccess(true);
        workFlowReturn.setMessage("流程发起成功");
        workFlowReturn.setData(processInstance.getId());
        return workFlowReturn;
	}
	

	
	   /**
     * 
     *@Description: 判断节点是否为会签节点
     * @param processDefinitionKey
     * @return 参数描述
     * boolean 返回类型
     * @throws 异常说明
     * 
     * @author shenbo
     */
    @Override
	public boolean checkActivitiIsHuiQian(String processDefinitionKey) {
        ProcessDefinition pd = repositoryService.createProcessDefinitionQuery().processDefinitionKey(processDefinitionKey).orderByProcessDefinitionVersion().desc().list().get(0);
        InputStream resourceAsStream = repositoryService.getResourceAsStream(pd.getDeploymentId(), pd.getResourceName());
        String xmlString = StreamUtils.InputStreamTOString(resourceAsStream);
        Map<String,String> startEvent =  XmlActivitiUtil.parseStartXml(xmlString);
        Map<String,String> flow = XmlActivitiUtil.parseXml(xmlString,"sequenceFlow","sourceRef",startEvent.get("id"));
        String taskkey = flow.get("targetRef");
        return XmlActivitiUtil.parseXml(xmlString, taskkey);
    }
	 
    
    /**
     * @Description: 判断节点是否为会签节点
     */
    public boolean checkStartUserTaskIsHuiQian(String processDefinitionKey,String taskkey){
        ProcessDefinition pd = repositoryService.createProcessDefinitionQuery().processDefinitionKey(processDefinitionKey).orderByProcessDefinitionVersion().desc().list().get(0);
        InputStream resourceAsStream = repositoryService.getResourceAsStream(pd.getDeploymentId(), pd.getResourceName());
        String xmlString = StreamUtils.InputStreamTOString(resourceAsStream);
        return XmlActivitiUtil.parseXml(xmlString, taskkey);
    }
	
	
    @Override
    public boolean start() {
        return false;
    }

    @Override
    public boolean complete() {
        return false;
    }



	@Override
	@Transactional
	public WorkFlowReturn completeTask(WorkFlowRequestParam workFlowRequestParam) {
		WorkFlowReturn workFlowReturn = new WorkFlowReturn();
        String taskId = workFlowRequestParam.getTaskId();
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if(task == null){
        	workFlowReturn.setCompleteSuccess(false);
        	workFlowReturn.setMessage("任务不存在");
        	return workFlowReturn;
        }
        String processInstanceId = task.getProcessInstanceId();
        Map<String,Object> variables = workFlowRequestParam.getVariables();
        if (null ==variables ) {
            variables = new HashMap<String,Object>();
        }
        
        //将表单实体类型的字段属性作为流程变量传入
//        Map<String, Object> formObj = paramsVo.getFormObj();        
//       	variables.putAll(formObj);        
        
        String nextUserId = workFlowRequestParam.getNextUserId();
        List<String> list = Arrays.asList(nextUserId.split(","));
        //判断当前节点是否为会签节点
        boolean isHuiqian = false;
        if(this.checkUserTaskIsHuiQian(taskId, "")){
            isHuiqian = true;
        }
        String name = workFlowRequestParam.getVariableName();
        String value = workFlowRequestParam.getVariableValue();
        variables.put(name, value);
        //判断下个节点是否是会签节点
        //暂时写死
        boolean nextNodeIsHuiQian = false;//this.checkNextUserTaskIsHuiQian(taskId, name,value);
        if(nextNodeIsHuiQian){
        	//若果当前节点是会签节点 且是第一个来处理的人 直接塞list 若果不是就拼接 原来的 list
        	if(isHuiqian){
        		Object obj = runtimeService.getVariable(task.getExecutionId(), "nrOfCompletedInstances");
        		Object objAll = runtimeService.getVariable(task.getExecutionId(), "nrOfInstances");
        		int  nrOfCompletedInstances= 0,nrOfInstances = 0;
        		if(obj!=null){
        			nrOfCompletedInstances = (Integer)obj;        			
        		}
        		if(objAll!=null){
        			nrOfInstances = (Integer)objAll;        			
        		}
        		
        		String conditionExp = getCompletionCondition(taskId,"");       		
        		String condition = "";
        		int index = 0;
        		//是否 ==
        		if(conditionExp.indexOf("==") != -1){
        			condition = ConditionEnum.EQ.getCode();
        			index = conditionExp.indexOf("==") + 2;
        		}else if(conditionExp.indexOf(">") != -1){
        			condition = ConditionEnum.GT.getCode();
        			index = conditionExp.indexOf(">") + 1;
        		}else{
        			condition = ConditionEnum.GE.getCode();
        			index = conditionExp.indexOf(">=") + 2;
        		}
        		conditionExp = conditionExp.substring(index, conditionExp.length()-1).trim();      		
        		float exp = Float.parseFloat(conditionExp);        		
        		float now = (float)(nrOfCompletedInstances + 1)/nrOfInstances;       		
        		if(nrOfCompletedInstances == 0){//当前节点为会签  且是第一个处理人
        			runtimeService.setVariable(processInstanceId, WorkFlowEnum.ASSIGNEE_USER_ID_LIST_TEMP.getCode(),list);
        			//这里需要考虑会签节点每次都只选一个人的情况
        			
        			if(StringUtils.equals(condition, ConditionEnum.EQ.getCode())){
        				//相等
        				if(now == exp){
            				runtimeService.setVariable(processInstanceId, WorkFlowEnum.ASSIGNEE_USER_ID_LIST.getCode(),list);
            			}
        			}else if(StringUtils.equals(condition, ConditionEnum.GT.getCode())){
        				//大于
        				if(now > exp){
            				runtimeService.setVariable(processInstanceId, WorkFlowEnum.ASSIGNEE_USER_ID_LIST.getCode(),list);
            			}
        			}else{
        				//大于等于
        				if(now >= exp){
            				runtimeService.setVariable(processInstanceId, WorkFlowEnum.ASSIGNEE_USER_ID_LIST.getCode(),list);
            			}
        			} 
        		}else{//当前是会签 且非第一个处理人 累加上其他人选择的 处理人同时用set去重
        			obj = runtimeService.getVariable(processInstanceId, WorkFlowEnum.ASSIGNEE_USER_ID_LIST_TEMP.getCode());
        			@SuppressWarnings("unchecked")
					List<String> listTemp = (List<String>) obj;
        			Set<String> set = new LinkedHashSet<String>();
        			set.addAll(listTemp);
        			set.addAll(list);
        			List<String> list2 = new ArrayList<String> ();  
        			list2.addAll(set);
        			//临时解决 只有当完成的会签任务 + 1 = 需要完成的总数时候才去替换数组  需要修改为判断通过率 2017-06-14
        			
        			if(StringUtils.equals(condition, ConditionEnum.EQ.getCode())){
        				//相等        				
        				if(now == exp){
            				runtimeService.setVariable(processInstanceId, WorkFlowEnum.ASSIGNEE_USER_ID_LIST.getCode(),list2);
            				runtimeService.setVariable(processInstanceId, WorkFlowEnum.ASSIGNEE_USER_ID_LIST_TEMP.getCode(),new ArrayList<String> ());
            			}else{
            				//若还不是最后一个会签任务 就累加 暂存起来
            				runtimeService.setVariable(processInstanceId, WorkFlowEnum.ASSIGNEE_USER_ID_LIST_TEMP.getCode(),list2);
            			}       				
        			}else if(StringUtils.equals(condition, ConditionEnum.GT.getCode())){
        				//大于        				
        				if(now > exp){
            				runtimeService.setVariable(processInstanceId, WorkFlowEnum.ASSIGNEE_USER_ID_LIST.getCode(),list2);
            				runtimeService.setVariable(processInstanceId, WorkFlowEnum.ASSIGNEE_USER_ID_LIST_TEMP.getCode(),new ArrayList<String> ());
            			}else{
            				//若还不是最后一个会签任务 就累加 暂存起来
            				runtimeService.setVariable(processInstanceId, WorkFlowEnum.ASSIGNEE_USER_ID_LIST_TEMP.getCode(),list2);
            			}
        			}else{
        				//大于等于        				
        				if(now >= exp){
            				runtimeService.setVariable(processInstanceId, WorkFlowEnum.ASSIGNEE_USER_ID_LIST.getCode(),list2);
            				runtimeService.setVariable(processInstanceId, WorkFlowEnum.ASSIGNEE_USER_ID_LIST_TEMP.getCode(),new ArrayList<String> ());
            			}else{
            				//若还不是最后一个会签任务 就累加 暂存起来
            				runtimeService.setVariable(processInstanceId, WorkFlowEnum.ASSIGNEE_USER_ID_LIST_TEMP.getCode(),list2);
            			}
        			} 
        		}
        	}else{
        		variables.put(WorkFlowEnum.ASSIGNEE_USER_ID_LIST.getCode(), list);
        	}
        }else{
        	if(list.size() > 1){
        		workFlowReturn.setCompleteSuccess(false);
        		workFlowReturn.setMessage("下一节点为单处理人任务只能选择一个办理人");
        		return workFlowReturn;
        	}
            variables.put("nextUserId", nextUserId);
        }
       
        try {
            //如果当前节点是会签  且当前这个办理人不同意 ，那么删除其他任务 然后完成这个任务  就直接走不同意的流程 一票否决
            if ("0".equals(value) && isHuiqian) {
                List<Task> taskList = taskService.createTaskQuery().processInstanceId(processInstanceId).list();
                for (Task task1 : taskList) {
                    if (!task1.getId().equals(taskId)) {
                        //删不掉，变通下，其他未完成任务 直接自动完成掉
                        taskService.complete(task1.getId(), variables);
                    }
                }
            }
            taskService.complete(taskId, variables);    
            ProcessInstance pi = runtimeService.createProcessInstanceQuery()//
                            .processInstanceId(processInstanceId)//使用流程实例ID查询
                            .singleResult();		
            //表示已经完成
            if(pi == null){
            	workFlowReturn.setCompleteSuccess(true);
            	workFlowReturn.setFinish(true);
            	workFlowReturn.setMessage("办理成功");
            	return workFlowReturn;
            }
        } catch (Exception e) {
            e.printStackTrace();
        	throw new BaseException(BaseExceptionEnum.WORKFLOW_COMPLETE_FAIL);

        }
        workFlowReturn.setCompleteSuccess(true);
    	workFlowReturn.setFinish(false);
    	workFlowReturn.setMessage("办理成功");
        return workFlowReturn;
    }
	
	  /**
     * 校验下一个节点是否是会签节点
     *@Description: 
     * @param taskId
     * @param string
     * @return 参数描述
     * boolean 返回类型
     * @throws 异常说明
     * 
     * @author shenbo
     */
/*    public boolean checkNextUserTaskIsHuiQian(String taskId, String field,String value) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        String processDefinitionId = task.getProcessDefinitionId();
        ProcessDefinitionEntity processDefinitionEntity = (ProcessDefinitionEntity)repositoryService.getProcessDefinition(processDefinitionId);
        String processInstanceId = task.getProcessInstanceId();
        ProcessInstance pi = runtimeService.createProcessInstanceQuery()//
                    .processInstanceId(processInstanceId).singleResult();
        String activityId = pi.getActivityId();
        if(StringUtils.isBlank(activityId)){
            List<Execution> executions = runtimeService.createExecutionQuery().parentId(pi.getId()).list();
            if(executions != null && executions.size() > 0){
                activityId = executions.get(0).getActivityId();
            }
        }
        ActivityImpl activityImpl = processDefinitionEntity.findActivity(activityId);
        List<PvmTransition> pvmList = activityImpl.getOutgoingTransitions();
        String taskkey =  getNextUserTaskNode(pvmList,field,value);
        
        return checkUserTaskIsHuiQian(taskId,taskkey);
    }*/
	
	public String getCompletionCondition(String taskId ,String taskkey){
    	HistoricTaskInstance hti = historyService.createHistoricTaskInstanceQuery().taskId(taskId).singleResult();
        if ("".equals(taskkey)) {
            taskkey = hti.getTaskDefinitionKey();
        }
        String processkey = hti.getProcessDefinitionId();
        ProcessDefinition pd = repositoryService.getProcessDefinition(processkey);
        InputStream resourceAsStream = repositoryService.getResourceAsStream(pd.getDeploymentId(), pd.getResourceName());
        String xmlString = StreamUtils.InputStreamTOString(resourceAsStream);
        return XmlActivitiUtil.getCompletionCondition(xmlString, taskkey);
    }


	@Override
	public boolean checkUserTaskIsHuiQian(String taskId, String taskkey) {
		HistoricTaskInstance hti = historyService.createHistoricTaskInstanceQuery().taskId(taskId).singleResult();
        if ("".equals(taskkey)) {
            taskkey = hti.getTaskDefinitionKey();
        }
        String processkey = hti.getProcessDefinitionId();
        ProcessDefinition pd = repositoryService.getProcessDefinition(processkey);
        InputStream resourceAsStream = repositoryService.getResourceAsStream(pd.getDeploymentId(), pd.getResourceName());
        String xmlString = StreamUtils.InputStreamTOString(resourceAsStream);
        return XmlActivitiUtil.parseXml(xmlString, taskkey);
	}
}