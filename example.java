package com.uhg.optumrx.aop;

import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uhg.optumrx.OWSConstants;
import com.uhg.optumrx.domain.Header;
import com.uhg.optumrx.domain.MedicareMemberDetailRequest;
import com.uhg.optumrx.domain.MedicareMemberDetailResponse;
import com.uhg.optumrx.domain.MemberDetailReadOnlyItemRequest;
import com.uhg.optumrx.domain.MemberDetailResponse;
import com.uhg.optumrx.domain.MemberDetailResult;
import com.uhg.optumrx.domain.MemberLoadRequest;
import com.uhg.optumrx.domain.MemberLoadResponse;
import com.uhg.optumrx.domain.MemberLoadResult;
import com.uhg.optumrx.domain.MemberSearchInfo;
import com.uhg.optumrx.domain.MemberSearchRequest;
import com.uhg.optumrx.domain.MemberSearchResponse;
import com.uhg.optumrx.domain.MemberSearchV2Info;
import com.uhg.optumrx.domain.MemberSearchV2Item;
import com.uhg.optumrx.domain.MemberSearchV2Request;
import com.uhg.optumrx.domain.MemberSearchV2Response;
import com.uhg.optumrx.domain.MemberSearchV2Result;
import com.uhg.optumrx.domain.Result;
import com.uhg.optumrx.domain.accumulator.MedDAccumPhaseDetailRequest;
import com.uhg.optumrx.domain.accumulator.MedDAccumPhaseDetailResult;
import com.uhg.optumrx.domain.benefitinquiry.BenefitInquiryRequest_Hashing;
import com.uhg.optumrx.domain.benefitinquiry.BenefitInquiryResponse;
import com.uhg.optumrx.domain.calltrackinghistory.CallTrackingHistorySearchRequest;
import com.uhg.optumrx.domain.calltrackinghistory.CallTrackingHistorySearchResult;
import com.uhg.optumrx.domain.calltrackinghistory.CallTrackingHistoryUpdateRequest;
import com.uhg.optumrx.domain.calltrackinghistory.CallTrackingHistoryUpdateResult;
import com.uhg.optumrx.domain.claim.ClaimSearchRequest;
import com.uhg.optumrx.domain.claim.ClaimSearchResult;
import com.uhg.optumrx.domain.claim.ClaimSearchServiceRequest;
import com.uhg.optumrx.domain.claim.ClaimSearchV2Result;
import com.uhg.optumrx.domain.claim.ClaimSearchV2ServiceRequest;
import com.uhg.optumrx.domain.copay.MultiPriceTrialAdjudicatorInfo;
import com.uhg.optumrx.domain.copay.MultiPriceTrialAdjudicatorRequest;
import com.uhg.optumrx.domain.copay.MultiPriceTrialAdjudicatorResponse;
import com.uhg.optumrx.domain.copay.MultiPriceTrialAdjudicatorResult;
import com.uhg.optumrx.domain.diagnosissearch.DiagnosisSearchRequest;
import com.uhg.optumrx.domain.diagnosissearch.DiagnosisSearchResult;
import com.uhg.optumrx.domain.drugalternative.DrugAlternativeRequest;
import com.uhg.optumrx.domain.drugalternative.DrugAlternativeResponse;
import com.uhg.optumrx.domain.drugalternative.DrugAlternativeResult;
import com.uhg.optumrx.domain.drugproxy.DrugProxyRequest;
import com.uhg.optumrx.domain.drugproxy.DrugProxyResponse;
import com.uhg.optumrx.domain.drugproxy.DrugProxyResult;
import com.uhg.optumrx.domain.drugsearch.DrugSearchRequest;
import com.uhg.optumrx.domain.drugsearch.DrugSearchResponse;
import com.uhg.optumrx.domain.drugsearch.DrugSearchResult;
import com.uhg.optumrx.domain.healthcheck.AppHealthStatus;
import com.uhg.optumrx.domain.memberplanbenefits.MemberPlanBenefitsRequest;
import com.uhg.optumrx.domain.memberplanbenefits.MemberPlanBenefitsResult;
import com.uhg.optumrx.domain.metadata.SearchInputMetaData;
import com.uhg.optumrx.domain.multipriceadjudicatorv7.MultiPriceTrialAdjudicatorV2Request;
import com.uhg.optumrx.domain.pas.priorauthhistory.PASPriorAuthHistoryResponse;
import com.uhg.optumrx.domain.pas.priorauthhistory.PriorAuthHistoryRequest;
import com.uhg.optumrx.domain.pharmacy.PharmacySearchRequest;
import com.uhg.optumrx.domain.pharmacy.PharmacySearchResponse;
import com.uhg.optumrx.domain.pharmacy.PharmacySearchResult;
import com.uhg.optumrx.domain.pharmacy.PharmacySearchV2Request;
import com.uhg.optumrx.domain.pharmacy.PharmacySearchV2Response;
import com.uhg.optumrx.domain.plan.PlanDetailRequest;
import com.uhg.optumrx.domain.plan.PlanDetailResponse;
import com.uhg.optumrx.domain.plan.PlanDetailResult;
import com.uhg.optumrx.domain.preferredalternatives.InvocationContext;
import com.uhg.optumrx.domain.preferredalternatives.PreferredAlternativesRequest;
import com.uhg.optumrx.domain.preferredalternatives.PreferredAlternativesResponse;
import com.uhg.optumrx.domain.prescriber.PrescriberDetailRequest;
import com.uhg.optumrx.domain.prescriber.PrescriberDetailResponse;
import com.uhg.optumrx.domain.prescriber.PrescriberDetailResult;
import com.uhg.optumrx.domain.prescriber.PrescriberSearchRequest;
import com.uhg.optumrx.domain.prescriber.PrescriberSearchResponse;
import com.uhg.optumrx.domain.prescriber.PrescriberSearchResult;
import com.uhg.optumrx.domain.priorauth.dynamicpa.DynamicPriorAuthRequest;
import com.uhg.optumrx.domain.priorauth.dynamicpa.DynamicPriorAuthResponse;
import com.uhg.optumrx.domain.priorauth.dynamicpa.DynamicPriorAuthResult;
import com.uhg.optumrx.domain.priorauthhistory.PriorAuthHistoryResponse;
import com.uhg.optumrx.domain.productdetail.ProductDetailsRequest;
import com.uhg.optumrx.domain.productdetail.ProductDetailsResponse;
import com.uhg.optumrx.domain.productsearch.ProductSearchRequest;
import com.uhg.optumrx.domain.productsearch.ProductSearchV2Result;
import com.uhg.optumrx.domain.provider.pharmacy.PreferredPharmacyResult;
import com.uhg.optumrx.domain.rxclaimpahistory.PriorAuthHistoryResult;
import com.uhg.optumrx.domain.rxclaimpahistory.RxClaimPAHistoryRequest;
import com.uhg.optumrx.domain.rxclaimpahistory.RxClaimPAHistoryResponse;
import com.uhg.optumrx.domain.trialclaim.MultiPriceTrialAdjudicatorV2Result;
import com.uhg.optumrx.service.CorrelationService;
import com.uhg.optumrx.utility.OWSUtil;
import com.uhg.optumrx.utility.PropertyUtil;

@Aspect
@Component
public class EcsEsbAuditLogAspect {

	private static final Logger logger = LogManager.getLogger(EcsEsbAuditLogAspect.class.getName());
	private static final ObjectMapper mapper = new ObjectMapper();

	@Autowired
	private PropertyUtil propertyUtil;

	@Pointcut("execution(public * com.uhg.optumrx.controller.*.*(..))")
	private void controllerPoint() {}

	@Autowired
	private CorrelationService correlationService;


	@Pointcut("execution(public * com.uhg.optumrx.integration.*.getProduct(..))" +
			"|| execution(public * com.uhg.optumrx.integration.*.get*Response(..))"+
			"|| execution(public * com.uhg.optumrx.integration.*.get*Products(..))"+
			"|| execution(public * com.uhg.optumrx.integration.MemberSearchV2.search(..))"+
			"|| execution(public * com.uhg.optumrx.integration.MemberFinder.getMemberInstance(..))")
	private void integrationLayerPoint() {}

	@Async
	@Around("integrationLayerPoint()")
	public Object integrationLayerLogAround(ProceedingJoinPoint joinPoint) {
		boolean logMessage = false;
		EcsEsbAuditSupplLog ecsEsbAuditSupplLog = new EcsEsbAuditSupplLog();
		
		String className = joinPoint.getSignature().getDeclaringTypeName();
		Object[] args = joinPoint.getArgs();
		ecsEsbAuditSupplLog.setEventIdentifier(OWSUtil.getServiceDetailName(className));

		for (Object object : args) {

			if (object instanceof DrugProxyRequest) {
				DrugProxyRequest request = (DrugProxyRequest) object;
				if (request.getHeader() != null
						&& !request.getHeader().getConsumingApp().equalsIgnoreCase(OWSConstants.HEALTH_CHECK_CONSUMER)
						&& !request.getInternalId().startsWith(OWSConstants.DRUG_PROXY_ID_PREFIX)) {
					logMessage = true;
					ecsEsbAuditSupplLog.setInternalCorrelationId(request.getInternalId());
					ecsEsbAuditSupplLog.setTransactionDomain(request.getHeader().getConsumingApp());
				}
			} else if (object instanceof DrugSearchRequest) {
				DrugSearchRequest request = (DrugSearchRequest) object;
				if (request.getHeader() != null
						&& !request.getHeader().getConsumingApp().equalsIgnoreCase(OWSConstants.HEALTH_CHECK_CONSUMER)
						&& !request.getInternalId().startsWith(OWSConstants.DRUG_SEARCH_ID_PREFIX)) {
					logMessage = true;
					ecsEsbAuditSupplLog.setInternalCorrelationId(request.getInternalId());
					ecsEsbAuditSupplLog.setTransactionDomain(request.getHeader().getConsumingApp());
				}
			} else if (object instanceof MemberSearchInfo) {
				MemberSearchInfo searchInfo = (MemberSearchInfo) object;
				MemberSearchRequest request = searchInfo.getMemberSearchRequest();
				if (request.getHeader() != null
						&& !request.getHeader().getConsumingApp().equalsIgnoreCase(OWSConstants.HEALTH_CHECK_CONSUMER)
						&& !request.getInternalId().startsWith(OWSConstants.MEMBER_SEARCH_ID_PREFIX)) {
					logMessage = true;
					ecsEsbAuditSupplLog.setInternalCorrelationId(request.getInternalId());
					ecsEsbAuditSupplLog.setTransactionDomain(request.getHeader().getConsumingApp());
					if (searchInfo.getMemberSearchRoutingInfo() != null) {
						ecsEsbAuditSupplLog.setInstanceId(searchInfo.getMemberSearchRoutingInfo().getInstanceId());
					}
				}
			} else if (object instanceof MultiPriceTrialAdjudicatorInfo) {
				MultiPriceTrialAdjudicatorInfo adjudicatorInfo = (MultiPriceTrialAdjudicatorInfo)object;
				MultiPriceTrialAdjudicatorRequest request = adjudicatorInfo.getTrialAdjudicatorRequest();
				if (!request.getInternalId().startsWith(OWSConstants.MULTI_PRICE_TRIAL_ADJUDICATOR_ID_PREFIX)) {
					logMessage = true;
					ecsEsbAuditSupplLog.setInternalCorrelationId(request.getInternalId());
					ecsEsbAuditSupplLog.setTransactionDomain(request.getHeader().getConsumingApp());
				}
			} else if (object instanceof PreferredAlternativesRequest) {
				PreferredAlternativesRequest request = (PreferredAlternativesRequest) object;
				if (!request.getInternalId().startsWith(OWSConstants.PREFERRED_ALTERNATIVES_ID_PREFIX)) {
					logMessage = true;
					ecsEsbAuditSupplLog.setInternalCorrelationId(request.getInternalId());
					ecsEsbAuditSupplLog.setTransactionDomain(request.getInvocationContext().getConsumingApp());
				}
			} else if (object instanceof PlanDetailRequest) {
				PlanDetailRequest request = (PlanDetailRequest) object;
				//added variable
				if (request.getHeader() != null
						&& !request.getHeader().getConsumingApp().equalsIgnoreCase(OWSConstants.HEALTH_CHECK_CONSUMER)
						&& !request.getInternalId().startsWith(OWSConstants.PLAN_DETAIL_ID_PREFIX)) {
					logMessage = true;
					ecsEsbAuditSupplLog.setInternalCorrelationId(request.getInternalId());
					ecsEsbAuditSupplLog.setTransactionDomain(request.getHeader().getConsumingApp());
				}
			} else if (object instanceof DrugAlternativeRequest) {
				DrugAlternativeRequest request = (DrugAlternativeRequest) object;
				if (request.getHeader() != null
						&& !request.getHeader().getConsumingApp().equalsIgnoreCase(OWSConstants.HEALTH_CHECK_CONSUMER)
						&& !request.getInternalId().startsWith(OWSConstants.DRUG_ALTERNATIVES_ID_PREFIX)) {
					logMessage = true;
					ecsEsbAuditSupplLog.setInternalCorrelationId(request.getInternalId());
					ecsEsbAuditSupplLog.setTransactionDomain(request.getHeader().getConsumingApp());
				}
			} else if (object instanceof MemberSearchV2Info) {
				MemberSearchV2Info request = (MemberSearchV2Info) object;
				if (request != null 
						&& request.getMemberSearchV2Request() != null
						&& request.getMemberSearchV2Request().getSearchInputMetaData() != null
						&& request.getMemberSearchV2Request().getSearchInputMetaData().getInternalCorrelationId() != null) {
					logMessage = true;
					ecsEsbAuditSupplLog.setInternalCorrelationId(request.getMemberSearchV2Request().getSearchInputMetaData().getInternalCorrelationId());
					ecsEsbAuditSupplLog.setTransactionDomain(request.getMemberSearchV2Request().getSearchInputMetaData().getConsumerAppId());
				}
			} 
		}
		
		// Calculate method execution time
		Object retValue = null;
		Long enter_Service_Timestamp = System.currentTimeMillis();
		ecsEsbAuditSupplLog.setTimestamp(usingDateFormatter(enter_Service_Timestamp));

		try {
			retValue = joinPoint.proceed();
		} catch (Throwable e) {
			String message="";
			if(e.getMessage() != null)
			 message=e.getMessage().length() > 254 ? e.getMessage().substring(0, 254) : e.getMessage();
			 
			ecsEsbAuditSupplLog.setMessage(message);
			logger.error(e.getMessage(), e);
		}

		Long exit_Service_Timestamp = System.currentTimeMillis();
		long method_Execution_Time = exit_Service_Timestamp - enter_Service_Timestamp;
		ecsEsbAuditSupplLog.setTimeduration(method_Execution_Time);

//		EcsEsbReqRespData ecsEsbResponseData = new EcsEsbReqRespData();

		if (logMessage) {
			if(retValue instanceof Exception){
				Exception exception = (Exception) retValue;
				String message="";
				if(exception.getMessage() != null)
				 message=exception.getMessage().length() > 254 ? exception.getMessage().substring(0, 254) : exception.getMessage();
				 
				ecsEsbAuditSupplLog.setMessage(message);
			} else if (retValue instanceof MemberSearchV2Response) {
				MemberSearchV2Response memberSearchV2Response = (MemberSearchV2Response) retValue;
				if(memberSearchV2Response != null && memberSearchV2Response.getMemberSearchItems() != null && !memberSearchV2Response.getMemberSearchItems().isEmpty()
						&& memberSearchV2Response.getMemberSearchItems().get(0) != null) {
					ecsEsbAuditSupplLog.setInstanceId(memberSearchV2Response.getMemberSearchItems().get(0).getInstanceId());
				}
			}

			logAuditDetails(ecsEsbAuditSupplLog);
		}
		
		return retValue;
	}

	@Async
	@Around("controllerPoint()")
	public Object logAroundMethode(ProceedingJoinPoint joinPoint) {
		EcsEsbAuditLog ecsEsbAuditLog = new EcsEsbAuditLog();
		EcsEsbReqRespData ecsEsbRequestData = new EcsEsbReqRespData();
		Object[] args = joinPoint.getArgs();
		ObjectMapper mapper = new ObjectMapper();
		if(RequestContextHolder.getRequestAttributes() != null){
			HttpServletRequest httprequest = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
			String className = joinPoint.getSignature().getDeclaringTypeName();
			String hostName = httprequest.getRemoteHost();

			// Get the service name from the class name.
			ecsEsbAuditLog.setServiceName(OWSUtil.getServiceName(className));
			ecsEsbAuditLog.setHostName(hostName);
		}
		// Get Header ecsEsbAuditLog from controller request
		MemberSearchV2Request memberSearchV2Request=null;
		for (Object object : args) {
			if(!(object instanceof Exception)) {
				ecsEsbRequestData.setMessage(convertObjToMsg(object));
				ecsEsbRequestData.setMessageType(OWSConstants.REQUEST_TYPE);
			}

			if (object instanceof DrugSearchRequest) {
				DrugSearchRequest request = (DrugSearchRequest) object;
				request = (DrugSearchRequest) OWSUtil.trimAllStringFields(request);
				request.setHeader((Header) OWSUtil.trimAllStringFields(request.getHeader()));
				ecsEsbAuditLog.setUserID(request.getHeader() == null ? "" : request.getHeader().getClientId());
				ecsEsbAuditLog.setConsumingApp(request.getHeader() == null ? "" : request.getHeader().getConsumingApp());
				ecsEsbAuditLog.setExternalCorrelationId(request.getHeader() == null ? "" : request.getHeader().getCorrelationId());
				ecsEsbAuditLog.setInternalCorrelationId(request.getInternalId());
			//	ecsEsbAuditLog
				ecsEsbRequestData.setApplicationId(ecsEsbAuditLog.getConsumingApp());
				ecsEsbRequestData.setCorrelationId(request.getHeader() == null ? "" : request.getHeader().getCorrelationId());
				ecsEsbRequestData.setTransactionId(request.getInternalId());
			} else if (object instanceof PharmacySearchRequest) {
				PharmacySearchRequest request = (PharmacySearchRequest) object;
				request = (PharmacySearchRequest) OWSUtil.trimAllStringFields(request);
				request.setHeader((Header) OWSUtil.trimAllStringFields(request.getHeader()));
				ecsEsbAuditLog.setUserID(request.getHeader() == null ? "" :request.getHeader().getClientId());
				ecsEsbAuditLog.setConsumingApp(request.getHeader() == null ? "" :request.getHeader().getConsumingApp());
				ecsEsbAuditLog.setExternalCorrelationId(request.getHeader() == null ? "" : request.getHeader().getCorrelationId());
				ecsEsbAuditLog.setInternalCorrelationId(request.getHeader() == null ? "" :request.getInternalId());
				ecsEsbRequestData.setApplicationId(ecsEsbAuditLog.getConsumingApp());
				ecsEsbRequestData.setCorrelationId(request.getHeader() == null ? "" :request.getHeader().getCorrelationId());
				ecsEsbRequestData.setTransactionId(request.getInternalId());
			} else if (object instanceof ClaimSearchRequest) {
				ClaimSearchRequest request = (ClaimSearchRequest) object;
				request = (ClaimSearchRequest) OWSUtil.trimAllStringFields(request);
				request.setHeader((Header) OWSUtil.trimAllStringFields(request.getHeader()));
				ecsEsbAuditLog.setUserID(request.getHeader() == null ? "" : request.getHeader().getClientId());
				ecsEsbAuditLog.setConsumingApp(request.getHeader() == null ? "" : request.getHeader().getConsumingApp());
				ecsEsbAuditLog.setExternalCorrelationId(request.getHeader() == null ? "" : request.getHeader().getCorrelationId());
				ecsEsbAuditLog.setInternalCorrelationId(request.getInternalId());
				ecsEsbRequestData.setApplicationId(ecsEsbAuditLog.getConsumingApp());
				ecsEsbRequestData.setCorrelationId(request.getHeader() == null ? "" : request.getHeader().getCorrelationId());
				ecsEsbRequestData.setTransactionId(request.getInternalId());

			} else if (object instanceof ClaimSearchServiceRequest) {
				ClaimSearchServiceRequest request = (ClaimSearchServiceRequest) object;
				//request = (ClaimSearchServiceRequest) OWSUtil.trimAllStringFields(request);
				//request.setHeader((Header) OWSUtil.trimAllStringFields(request.getHeader()));
				ecsEsbAuditLog.setUserID(request.getHeader() == null ? "" : request.getHeader().getClientId());
				ecsEsbAuditLog.setConsumingApp(request.getHeader() == null ? "" : request.getHeader().getConsumingApp());
				ecsEsbAuditLog.setExternalCorrelationId(request.getHeader() == null ? "" : request.getHeader().getCorrelationId());
				ecsEsbAuditLog.setInternalCorrelationId(request.getInternalId());
				ecsEsbRequestData.setApplicationId(ecsEsbAuditLog.getConsumingApp());
				ecsEsbRequestData.setCorrelationId(request.getHeader() == null ? "" : request.getHeader().getCorrelationId());
				ecsEsbRequestData.setTransactionId(request.getInternalId());
			} else if (object instanceof PriorAuthHistoryRequest) {
				PriorAuthHistoryRequest request = (PriorAuthHistoryRequest) object;
				request = (PriorAuthHistoryRequest) OWSUtil.trimAllStringFields(request);
				request.setHeader( (com.uhg.optumrx.domain.priorauthhistory.Header) OWSUtil.trimAllStringFields(request.getHeader()));
				ecsEsbAuditLog.setUserID(request.getHeader() == null ? "" : request.getHeader().getClientId());
				ecsEsbAuditLog.setConsumingApp(request.getHeader() == null ? "" : request.getHeader().getConsumingAppID());
				ecsEsbAuditLog.setExternalCorrelationId(request.getHeader() == null ? "" : request.getHeader().getCorrelationId());
				ecsEsbAuditLog.setInternalCorrelationId(request.getInternalId());
				ecsEsbRequestData.setApplicationId(ecsEsbAuditLog.getConsumingApp());
				ecsEsbRequestData.setCorrelationId(request.getHeader() == null ? "" : request.getHeader().getCorrelationId());
				ecsEsbRequestData.setTransactionId(request.getInternalId());
			} else if (object instanceof com.uhg.optumrx.domain.priorauthhistory.PriorAuthHistoryRequest) {
				com.uhg.optumrx.domain.priorauthhistory.PriorAuthHistoryRequest request = (com.uhg.optumrx.domain.priorauthhistory.PriorAuthHistoryRequest) object;
				request = (com.uhg.optumrx.domain.priorauthhistory.PriorAuthHistoryRequest) OWSUtil.trimAllStringFields(request);
				request.setHeader( (com.uhg.optumrx.domain.priorauthhistory.Header) OWSUtil.trimAllStringFields(request.getHeader()));
				ecsEsbAuditLog.setUserID(request.getHeader() == null ? "" : request.getHeader().getClientId());
				ecsEsbAuditLog.setConsumingApp(request.getHeader() == null ? "" : request.getHeader().getConsumingAppID());
				ecsEsbAuditLog.setExternalCorrelationId(request.getHeader() == null ? "" : request.getHeader().getCorrelationId());
				ecsEsbAuditLog.setInternalCorrelationId(request.getInternalId());
				ecsEsbRequestData.setApplicationId(ecsEsbAuditLog.getConsumingApp());
				ecsEsbRequestData.setCorrelationId(request.getHeader() == null ? "" : request.getHeader().getCorrelationId());
				ecsEsbRequestData.setTransactionId(request.getInternalId());
			} else if (object instanceof MemberSearchRequest) {
				MemberSearchRequest request = (MemberSearchRequest) object;
				request = (MemberSearchRequest) OWSUtil.trimAllStringFields(request);
				request.setHeader((Header) OWSUtil.trimAllStringFields(request.getHeader()));
				ecsEsbAuditLog.setUserID(request.getHeader() == null ? "" :request.getHeader().getClientId());
				ecsEsbAuditLog.setConsumingApp(request.getHeader() == null ? "" :request.getHeader().getConsumingApp());
				ecsEsbAuditLog.setExternalCorrelationId(request.getHeader() == null ? "" : request.getHeader().getCorrelationId());
				ecsEsbAuditLog.setInternalCorrelationId(request.getInternalId());
				ecsEsbRequestData.setApplicationId(ecsEsbAuditLog.getConsumingApp());
				ecsEsbRequestData.setCorrelationId(request.getHeader() == null ? "" :request.getHeader().getCorrelationId());
				ecsEsbRequestData.setTransactionId(request.getInternalId());
			} else if (object instanceof MemberDetailReadOnlyItemRequest) {
				MemberDetailReadOnlyItemRequest request = (MemberDetailReadOnlyItemRequest) object;
				request = (MemberDetailReadOnlyItemRequest) OWSUtil.trimAllStringFields(request);
				request.setHeader((Header) OWSUtil.trimAllStringFields(request.getHeader()));
				ecsEsbAuditLog.setUserID(request.getHeader() == null ? "" : request.getHeader().getClientId());
				ecsEsbAuditLog.setConsumingApp(request.getHeader() == null ? "" : request.getHeader().getConsumingApp());
				ecsEsbAuditLog.setExternalCorrelationId(request.getHeader() == null ? "" : request.getHeader().getCorrelationId());
				ecsEsbAuditLog.setInternalCorrelationId(request.getInternalId());
				ecsEsbRequestData.setApplicationId(ecsEsbAuditLog.getConsumingApp());
				ecsEsbRequestData.setCorrelationId(request.getHeader() == null ? "" : request.getHeader().getCorrelationId());
				ecsEsbRequestData.setTransactionId(request.getInternalId());
			} else if (object instanceof BenefitInquiryRequest_Hashing) {
				BenefitInquiryRequest_Hashing request = (BenefitInquiryRequest_Hashing) object;
				request = (BenefitInquiryRequest_Hashing) OWSUtil.trimAllStringFields(request);
				request.setInvocationContext((com.uhg.optumrx.domain.benefitinquiry.InvocationContext) OWSUtil.trimAllStringFields(request.getInvocationContext()));
				if (null != request.getBenefitInquiryRequest() && null != request.getBenefitInquiryRequest().getUserContext()) {
					ecsEsbAuditLog.setUserID(request.getBenefitInquiryRequest().getUserContext().getUserId());
				}
				if (null != request.getInvocationContext()) {
					ecsEsbAuditLog.setConsumingApp(request.getInvocationContext().getConsumingApp());
				}
				if (null != request.getBenefitInquiryRequest() && null != request.getBenefitInquiryRequest().getServiceContext()) {
					ecsEsbAuditLog.setExternalCorrelationId(request.getBenefitInquiryRequest().getServiceContext().getCorrelationId());
					ecsEsbRequestData.setCorrelationId(request.getBenefitInquiryRequest().getServiceContext().getCorrelationId());
				}
				ecsEsbAuditLog.setInternalCorrelationId(request.getInternalId());
				ecsEsbRequestData.setApplicationId(ecsEsbAuditLog.getConsumingApp());
				ecsEsbRequestData.setTransactionId(request.getInternalId());
			} else if (object instanceof PreferredAlternativesRequest) {
				PreferredAlternativesRequest request = (PreferredAlternativesRequest) object;
				request = (PreferredAlternativesRequest) OWSUtil.trimAllStringFields(request);
				request.setInvocationContext( (InvocationContext) OWSUtil.trimAllStringFields(request.getInvocationContext()));
				ecsEsbAuditLog.setUserID(request.getUserContext() == null ? "" : request.getUserContext().getUserId());
				ecsEsbAuditLog.setConsumingApp(request.getInvocationContext() == null ? "" : request.getInvocationContext().getConsumingApp());
				ecsEsbAuditLog.setExternalCorrelationId(request.getServiceContext() == null ? "" : request.getServiceContext().getCorrelationId());
				ecsEsbAuditLog.setInternalCorrelationId(request.getInternalId());
				ecsEsbRequestData.setApplicationId(ecsEsbAuditLog.getConsumingApp());
				ecsEsbRequestData.setCorrelationId(request.getServiceContext() == null ? "" : request.getServiceContext().getCorrelationId());
				ecsEsbRequestData.setTransactionId(request.getInternalId());
			} else if (object instanceof DiagnosisSearchRequest) {
				DiagnosisSearchRequest request = (DiagnosisSearchRequest) object;
				request = (DiagnosisSearchRequest) OWSUtil.trimAllStringFields(request);
				request.setHeader((Header) OWSUtil.trimAllStringFields(request.getHeader()));
				ecsEsbAuditLog.setUserID(request.getHeader().getClientId());
				ecsEsbAuditLog.setConsumingApp(request.getHeader().getConsumingApp());
				ecsEsbAuditLog.setExternalCorrelationId(request.getHeader().getCorrelationId());
				ecsEsbRequestData.setApplicationId(ecsEsbAuditLog.getConsumingApp());
				ecsEsbRequestData.setCorrelationId(request.getHeader().getCorrelationId());
//				ecsEsbRequestData.setTransactionId(request.getHeader() == null ? ""  : request.getInternalId());
			} else if (object instanceof MultiPriceTrialAdjudicatorRequest) {
				MultiPriceTrialAdjudicatorRequest request = (MultiPriceTrialAdjudicatorRequest) object;
				request = (MultiPriceTrialAdjudicatorRequest) OWSUtil.trimAllStringFields(request);
				request.setHeader((Header) OWSUtil.trimAllStringFields(request.getHeader()));
				ecsEsbAuditLog.setUserID(request.getHeader() == null ? "" : request.getHeader().getClientId());
				ecsEsbAuditLog.setConsumingApp(request.getHeader() == null ? "" : request.getHeader().getConsumingApp());
				ecsEsbAuditLog.setExternalCorrelationId(request.getHeader() == null ? "" : request.getHeader().getCorrelationId());
				ecsEsbAuditLog.setInternalCorrelationId(request.getInternalId());
				ecsEsbRequestData.setApplicationId(ecsEsbAuditLog.getConsumingApp());
				ecsEsbRequestData.setCorrelationId(request.getHeader() == null ? "" : request.getHeader().getCorrelationId());
				ecsEsbRequestData.setTransactionId(request.getInternalId());
			} else if (object instanceof PlanDetailRequest) {
				PlanDetailRequest request = (PlanDetailRequest) object;
				request = (PlanDetailRequest) OWSUtil.trimAllStringFields(request);
				request.setHeader((Header) OWSUtil.trimAllStringFields(request.getHeader()));
				ecsEsbAuditLog.setUserID(request.getHeader() == null ? "" : request.getHeader().getClientId());
				ecsEsbAuditLog.setConsumingApp(request.getHeader() == null ? "" : request.getHeader().getConsumingApp());
				ecsEsbAuditLog.setExternalCorrelationId(request.getHeader() == null ? "" : request.getHeader().getCorrelationId());				
				ecsEsbAuditLog.setInternalCorrelationId(request.getInternalId());
				ecsEsbRequestData.setApplicationId(ecsEsbAuditLog.getConsumingApp());
				ecsEsbRequestData.setCorrelationId(request.getHeader() == null ? "" : request.getHeader().getConsumingApp());
				ecsEsbRequestData.setTransactionId(request.getInternalId());

			} else if (object instanceof PrescriberDetailRequest) {
				PrescriberDetailRequest request = (PrescriberDetailRequest) object;
				request = (PrescriberDetailRequest) OWSUtil.trimAllStringFields(request);
				request.setHeader((Header) OWSUtil.trimAllStringFields(request.getHeader()));
				ecsEsbAuditLog.setUserID(request.getHeader() == null ? "" : request.getHeader().getClientId());
				ecsEsbAuditLog.setConsumingApp(request.getHeader() == null ? "" : request.getHeader().getConsumingApp());
				ecsEsbAuditLog.setExternalCorrelationId(request.getHeader() == null ? "" : request.getHeader().getCorrelationId());
				ecsEsbAuditLog.setInternalCorrelationId(request.getInternalId());
				ecsEsbRequestData.setApplicationId(ecsEsbAuditLog.getConsumingApp());
				ecsEsbRequestData.setCorrelationId(request.getHeader() == null ? "" : request.getHeader().getCorrelationId());
				ecsEsbRequestData.setTransactionId(request.getInternalId());
				
			} else if (object instanceof RxClaimPAHistoryRequest) {
				RxClaimPAHistoryRequest request = (RxClaimPAHistoryRequest) object;
				request = (RxClaimPAHistoryRequest) OWSUtil.trimAllStringFields(request);
				request.setHeader( (com.uhg.optumrx.domain.priorauthhistory.Header) OWSUtil.trimAllStringFields(request.getHeader()));
				ecsEsbAuditLog.setUserID(request.getHeader() == null ? "" : request.getHeader().getClientId());
				ecsEsbAuditLog.setConsumingApp(request.getHeader() == null ? "" : request.getHeader().getConsumingAppID());
				ecsEsbAuditLog.setExternalCorrelationId(request.getHeader() == null ? "" : request.getHeader().getCorrelationId());
				ecsEsbAuditLog.setInternalCorrelationId(request.getInternalId());
				ecsEsbRequestData.setApplicationId(ecsEsbAuditLog.getConsumingApp());
				ecsEsbRequestData.setCorrelationId(request.getHeader() == null ? "" : request.getHeader().getCorrelationId());
				ecsEsbRequestData.setTransactionId(request.getInternalId());
			} else if (object instanceof DrugAlternativeRequest) {
				DrugAlternativeRequest request = (DrugAlternativeRequest) object;
				request = (DrugAlternativeRequest) OWSUtil.trimAllStringFields(request);
				request.setHeader((Header) OWSUtil.trimAllStringFields(request.getHeader()));
				ecsEsbAuditLog.setUserID(request.getHeader() == null ? "" : request.getHeader().getClientId());
				ecsEsbAuditLog.setConsumingApp(request.getHeader() == null ? "" : request.getHeader().getConsumingApp());
				ecsEsbAuditLog.setExternalCorrelationId(request.getHeader() == null ? "" : request.getHeader().getCorrelationId());
				ecsEsbAuditLog.setInternalCorrelationId(request.getInternalId());
				ecsEsbRequestData.setApplicationId(ecsEsbAuditLog.getConsumingApp());
				ecsEsbRequestData.setCorrelationId(request.getHeader() == null ? ""  : request.getHeader().getCorrelationId());
				ecsEsbRequestData.setTransactionId(request.getInternalId());
			} else if (object instanceof MemberSearchV2Request) {
				memberSearchV2Request = (MemberSearchV2Request) object;
				//memberSearchV2Request = (MemberSearchV2Request) OWSUtil.trimAllStringFields(memberSearchV2Request);
				//memberSearchV2Request.setSearchInputMetaData( (SearchInputMetaData) OWSUtil.trimAllStringFields(memberSearchV2Request.getSearchInputMetaData()));
				ecsEsbAuditLog.setUserID(memberSearchV2Request.getSearchInputMetaData() == null ? "" : memberSearchV2Request.getSearchInputMetaData().getConsumerAppId());
				ecsEsbAuditLog.setConsumingApp(memberSearchV2Request.getSearchInputMetaData() == null ? "" : memberSearchV2Request.getSearchInputMetaData().getConsumerAppId());
				ecsEsbAuditLog.setInternalCorrelationId(generateCorrelationId(memberSearchV2Request));
				String externalCorrelationId = memberSearchV2Request.getSearchInputMetaData() != null ? memberSearchV2Request.getSearchInputMetaData().getExternalCorrelationId() : "";
				ecsEsbAuditLog.setExternalCorrelationId(externalCorrelationId);
				ecsEsbRequestData.setApplicationId(memberSearchV2Request.getSearchInputMetaData() == null ? "" : memberSearchV2Request.getSearchInputMetaData().getConsumerAppId());
				ecsEsbRequestData.setCorrelationId(ecsEsbAuditLog.getExternalCorrelationId());
				ecsEsbRequestData.setTransactionId(ecsEsbAuditLog.getInternalCorrelationId());
			}else if (object instanceof MedicareMemberDetailRequest) {
				MedicareMemberDetailRequest request = (MedicareMemberDetailRequest) object;
				//request = (MedicareMemberDetailRequest) OWSUtil.trimAllStringFields(request);
				//request.setSearchInputMetaData( (SearchInputMetaData) OWSUtil.trimAllStringFields(request.getSearchInputMetaData()));
				ecsEsbAuditLog.setUserID(request.getSearchInputMetaData() == null ? "" : request.getSearchInputMetaData().getConsumerAppId());
				ecsEsbAuditLog.setConsumingApp(request.getSearchInputMetaData() == null ? "" : request.getSearchInputMetaData().getConsumerAppId());
				ecsEsbAuditLog.setInternalCorrelationId(generateCorrelationId(request));
				String externalCorrelationId = request.getSearchInputMetaData() != null ? request.getSearchInputMetaData().getExternalCorrelationId() : "";
				ecsEsbAuditLog.setExternalCorrelationId(externalCorrelationId);
				ecsEsbRequestData.setApplicationId(request.getSearchInputMetaData() == null ? "" : request.getSearchInputMetaData().getConsumerAppId());
				ecsEsbRequestData.setCorrelationId(ecsEsbAuditLog.getExternalCorrelationId());
				ecsEsbRequestData.setTransactionId(ecsEsbAuditLog.getInternalCorrelationId());
			}else if (object instanceof ClaimSearchV2ServiceRequest) {
				ClaimSearchV2ServiceRequest request = (ClaimSearchV2ServiceRequest) object;
				//request = (ClaimSearchV2ServiceRequest) OWSUtil.trimAllStringFields(request);
				//request.setSearchInputMetaData( (SearchInputMetaData) OWSUtil.trimAllStringFields(request.getSearchInputMetaData()));
				ecsEsbAuditLog.setUserID(request.getSearchInputMetaData() == null ? "" : request.getSearchInputMetaData().getConsumerAppId());
				ecsEsbAuditLog.setConsumingApp(request.getSearchInputMetaData() == null ? "" : request.getSearchInputMetaData().getConsumerAppId());
				ecsEsbAuditLog.setInternalCorrelationId(generateCorrelationId(request));
				String externalCorrelationId = request.getSearchInputMetaData() != null ? request.getSearchInputMetaData().getExternalCorrelationId() : "";
				ecsEsbAuditLog.setExternalCorrelationId(externalCorrelationId);
				ecsEsbRequestData.setApplicationId(request.getSearchInputMetaData() == null ? "" : request.getSearchInputMetaData().getConsumerAppId());
				ecsEsbRequestData.setCorrelationId(ecsEsbAuditLog.getExternalCorrelationId());
				ecsEsbRequestData.setTransactionId(ecsEsbAuditLog.getInternalCorrelationId());
			}else if (object instanceof ProductSearchRequest) {
				ProductSearchRequest request = (ProductSearchRequest) object;
				//request = (ProductSearchRequest) OWSUtil.trimAllStringFields(request);
				//request.setSearchInputMetaData( (SearchInputMetaData) OWSUtil.trimAllStringFields(request.getSearchInputMetaData()));
				ecsEsbAuditLog.setUserID(request.getSearchInputMetaData() == null ? "" : request.getSearchInputMetaData().getConsumerAppId());
				ecsEsbAuditLog.setConsumingApp(request.getSearchInputMetaData() == null ? "" : request.getSearchInputMetaData().getConsumerAppId());
				ecsEsbAuditLog.setInternalCorrelationId(generateCorrelationId(request));
				String externalCorrelationId = request.getSearchInputMetaData() != null ? request.getSearchInputMetaData().getExternalCorrelationId() : "";
				ecsEsbAuditLog.setExternalCorrelationId(externalCorrelationId);
				ecsEsbRequestData.setApplicationId(request.getSearchInputMetaData() == null ? "" : request.getSearchInputMetaData().getConsumerAppId());
				ecsEsbRequestData.setCorrelationId(ecsEsbAuditLog.getExternalCorrelationId());
				ecsEsbRequestData.setTransactionId(ecsEsbAuditLog.getInternalCorrelationId());
			} else if (object instanceof PrescriberSearchRequest) {
				PrescriberSearchRequest request = (PrescriberSearchRequest) object;
				request = (PrescriberSearchRequest) OWSUtil.trimAllStringFields(request);
				request.setHeader((Header) OWSUtil.trimAllStringFields(request.getHeader()));
				ecsEsbAuditLog.setUserID(request.getHeader() == null ? "" : request.getHeader().getClientId());
				ecsEsbAuditLog.setConsumingApp(request.getHeader() == null ? "" : request.getHeader().getConsumingApp());
				ecsEsbAuditLog.setExternalCorrelationId(request.getHeader() == null  ? "" : request.getHeader().getCorrelationId());
				ecsEsbAuditLog.setInternalCorrelationId(request.getInternalId() == null ? "" : request.getInternalId());
				ecsEsbRequestData.setApplicationId(request.getHeader() == null ? "" : request.getHeader().getConsumingApp());
				ecsEsbRequestData.setCorrelationId(request.getHeader() == null ? "" : request.getHeader().getCorrelationId());
				ecsEsbRequestData.setTransactionId(request.getInternalId());
			}
			else if (object instanceof MedDAccumPhaseDetailRequest) {
				MedDAccumPhaseDetailRequest request = (MedDAccumPhaseDetailRequest) object;
				request = (MedDAccumPhaseDetailRequest) OWSUtil.trimAllStringFields(request);
				request.setHeader((Header) OWSUtil.trimAllStringFields(request.getHeader()));
				ecsEsbAuditLog.setUserID(request.getHeader() == null ? "" : request.getHeader().getClientId());
				ecsEsbAuditLog.setConsumingApp(request.getHeader() == null ? "" : request.getHeader().getConsumingApp());
				ecsEsbAuditLog.setExternalCorrelationId(request.getHeader() == null ? "" : request.getHeader().getCorrelationId());
				ecsEsbAuditLog.setInternalCorrelationId(request.getInternalId());
				ecsEsbRequestData.setApplicationId(request.getHeader() == null ? "" : request.getHeader().getConsumingApp());
				ecsEsbRequestData.setCorrelationId(request.getHeader() == null ? "" : request.getHeader().getCorrelationId());
				ecsEsbRequestData.setTransactionId(request.getInternalId());
			}
			else if (object instanceof ProductDetailsRequest) {
				ProductDetailsRequest request = (ProductDetailsRequest) object;
				//request = (ProductDetailsRequest) OWSUtil.trimAllStringFields(request);
				//request.setSearchInputMetaData( (SearchInputMetaData) OWSUtil.trimAllStringFields(request.getSearchInputMetaData()));
				ecsEsbAuditLog.setUserID(request.getSearchInputMetaData() == null ? "" : request.getSearchInputMetaData().getConsumerAppId());
				ecsEsbAuditLog.setConsumingApp(request.getSearchInputMetaData() == null ? "" : request.getSearchInputMetaData().getConsumerAppId());
				ecsEsbAuditLog.setInternalCorrelationId(generateCorrelationId(request));
				String externalCorrelationId = request.getSearchInputMetaData() != null ? request.getSearchInputMetaData().getExternalCorrelationId() : "";
				ecsEsbAuditLog.setExternalCorrelationId(externalCorrelationId);
				ecsEsbRequestData.setApplicationId(request.getSearchInputMetaData().getConsumerAppId());
				ecsEsbRequestData.setCorrelationId(ecsEsbAuditLog.getExternalCorrelationId());
				ecsEsbRequestData.setTransactionId(ecsEsbAuditLog.getInternalCorrelationId());
			} else if (object instanceof MemberPlanBenefitsRequest) {
				MemberPlanBenefitsRequest request = (MemberPlanBenefitsRequest) object;
				//request = (MemberPlanBenefitsRequest) OWSUtil.trimAllStringFields(request);
				//request.setSearchInputMetaData( (SearchInputMetaData) OWSUtil.trimAllStringFields(request.getSearchInputMetaData()));
				ecsEsbAuditLog.setUserID(request.getSearchInputMetaData() == null ? "" : request.getSearchInputMetaData().getConsumerAppId());
				ecsEsbAuditLog.setConsumingApp(request.getSearchInputMetaData() == null ? "" : request.getSearchInputMetaData().getConsumerAppId());
				ecsEsbAuditLog.setInternalCorrelationId(generateCorrelationId(request));
				String externalCorrelationId = request.getSearchInputMetaData() != null ? request.getSearchInputMetaData().getExternalCorrelationId() : "";
				ecsEsbAuditLog.setExternalCorrelationId(externalCorrelationId);
				ecsEsbRequestData.setApplicationId(request.getSearchInputMetaData().getConsumerAppId());
				ecsEsbRequestData.setCorrelationId(ecsEsbAuditLog.getExternalCorrelationId());
				ecsEsbRequestData.setTransactionId(ecsEsbAuditLog.getInternalCorrelationId());
			} else if (object instanceof PriorAuthHistoryRequest) {
				PriorAuthHistoryRequest request = (PriorAuthHistoryRequest) object;
				request = (PriorAuthHistoryRequest) OWSUtil.trimAllStringFields(request);
				request.setHeader( (com.uhg.optumrx.domain.priorauthhistory.Header) OWSUtil.trimAllStringFields(request.getHeader()));
				ecsEsbAuditLog.setUserID(request.getHeader() == null ? "" : request.getHeader().getClientId());
				ecsEsbAuditLog.setConsumingApp(request.getHeader() == null ? "" : request.getHeader().getConsumingAppID());
				ecsEsbAuditLog.setExternalCorrelationId(request.getHeader() == null ? "" : request.getHeader().getCorrelationId());
				ecsEsbAuditLog.setInternalCorrelationId(request.getInternalId());
				ecsEsbRequestData.setApplicationId(request.getHeader() == null ? "" : request.getHeader().getConsumingAppID());
				ecsEsbRequestData.setCorrelationId(request.getHeader() == null ? "" : request.getHeader().getCorrelationId());
				ecsEsbRequestData.setTransactionId(request.getInternalId());
			} else if (object instanceof MemberLoadRequest) {
				MemberLoadRequest request = (MemberLoadRequest) object;
				request = (MemberLoadRequest) OWSUtil.trimAllStringFields(request);
				request.setHeader((Header) OWSUtil.trimAllStringFields(request.getHeader()));
				ecsEsbAuditLog.setUserID(request.getHeader() == null ? "" : request.getHeader().getClientId());
				ecsEsbAuditLog.setConsumingApp(request.getHeader() == null ? "" : request.getHeader().getConsumingApp());
				ecsEsbAuditLog.setExternalCorrelationId(request.getHeader() == null ? "" : request.getHeader().getCorrelationId());
				ecsEsbAuditLog.setInternalCorrelationId(request.getInternalId());
				ecsEsbRequestData.setApplicationId(ecsEsbAuditLog.getConsumingApp());
				ecsEsbRequestData.setCorrelationId(request.getHeader() == null ? "" : request.getHeader().getCorrelationId());
				ecsEsbRequestData.setTransactionId(request.getInternalId());
			}else if (object instanceof PharmacySearchV2Request) {
				PharmacySearchV2Request request = (PharmacySearchV2Request) object;
				//request = (PharmacySearchV2Request) OWSUtil.trimAllStringFields(request);
				//request.setSearchInputMetaData((SearchInputMetaData) OWSUtil.trimAllStringFields(request.getSearchInputMetaData()));
				ecsEsbAuditLog.setUserID(request.getSearchInputMetaData() == null ? "" : request.getSearchInputMetaData().getConsumerAppId());
				ecsEsbAuditLog.setConsumingApp(request.getSearchInputMetaData() == null ? "" : request.getSearchInputMetaData().getConsumerAppId());
				ecsEsbAuditLog.setInternalCorrelationId(generateCorrelationId(request));
				String externalCorrelationId = request.getSearchInputMetaData() != null ? request.getSearchInputMetaData().getExternalCorrelationId() : "";
				ecsEsbAuditLog.setExternalCorrelationId(externalCorrelationId);
				ecsEsbRequestData.setApplicationId(request.getSearchInputMetaData() == null ? "" : request.getSearchInputMetaData().getConsumerAppId());
				ecsEsbRequestData.setCorrelationId(ecsEsbAuditLog.getExternalCorrelationId());
				ecsEsbRequestData.setTransactionId(ecsEsbAuditLog.getInternalCorrelationId());
			}
			else if (object instanceof CallTrackingHistorySearchRequest) {
				CallTrackingHistorySearchRequest request = (CallTrackingHistorySearchRequest) object;
				request = (CallTrackingHistorySearchRequest) OWSUtil.trimAllStringFields(request);
				request.setSearchInputMetaData((SearchInputMetaData) OWSUtil.trimAllStringFields(request.getSearchInputMetaData()));
				ecsEsbAuditLog.setUserID(request.getSearchInputMetaData() == null ? "" : request.getSearchInputMetaData().getConsumerAppId());
				ecsEsbAuditLog.setConsumingApp(request.getSearchInputMetaData() == null ? "" : request.getSearchInputMetaData().getConsumerAppId());
				ecsEsbAuditLog.setInternalCorrelationId(generateCorrelationId(request));
				String externalCorrelationId = request.getSearchInputMetaData() != null ? request.getSearchInputMetaData().getExternalCorrelationId() : "";
				ecsEsbAuditLog.setExternalCorrelationId(externalCorrelationId);
				ecsEsbRequestData.setApplicationId(request.getSearchInputMetaData() == null ? "" : request.getSearchInputMetaData().getConsumerAppId());
				ecsEsbRequestData.setCorrelationId(ecsEsbAuditLog.getExternalCorrelationId());
				ecsEsbRequestData.setTransactionId(ecsEsbAuditLog.getInternalCorrelationId());
			} else if (object instanceof CallTrackingHistoryUpdateRequest) {
				CallTrackingHistoryUpdateRequest request = (CallTrackingHistoryUpdateRequest) object;
				request = (CallTrackingHistoryUpdateRequest) OWSUtil.trimAllStringFields(request);
				request.setSearchInputMetaData((SearchInputMetaData) OWSUtil.trimAllStringFields(request.getSearchInputMetaData()));
				ecsEsbAuditLog.setUserID(request.getSearchInputMetaData() == null ? "" : request.getSearchInputMetaData().getConsumerAppId());
				ecsEsbAuditLog.setConsumingApp(request.getSearchInputMetaData() == null ? "" : request.getSearchInputMetaData().getConsumerAppId());
				ecsEsbAuditLog.setInternalCorrelationId(generateCorrelationId(request));
				String externalCorrelationId = request.getSearchInputMetaData() != null ? request.getSearchInputMetaData().getExternalCorrelationId() : "";
				ecsEsbAuditLog.setExternalCorrelationId(externalCorrelationId);
				ecsEsbRequestData.setApplicationId(request.getSearchInputMetaData() == null ? "" : request.getSearchInputMetaData().getConsumerAppId());
				ecsEsbRequestData.setCorrelationId(ecsEsbAuditLog.getExternalCorrelationId());
				ecsEsbRequestData.setTransactionId(ecsEsbAuditLog.getInternalCorrelationId());
			}
			else if (object instanceof DynamicPriorAuthRequest) {
				DynamicPriorAuthRequest request = (DynamicPriorAuthRequest) object;
				request = (DynamicPriorAuthRequest) OWSUtil.trimAllStringFields(request);
				request.setHeader((Header) OWSUtil.trimAllStringFields(request.getHeader()));
				ecsEsbAuditLog.setUserID(request.getHeader() == null ? "" : request.getHeader().getClientId());
				ecsEsbAuditLog.setConsumingApp(request.getHeader() == null ? "" : request.getHeader().getConsumingApp());
				ecsEsbAuditLog.setExternalCorrelationId(request.getHeader() == null ? "" : request.getHeader().getCorrelationId());
				ecsEsbAuditLog.setInternalCorrelationId(request.getInternalId());
				ecsEsbRequestData.setApplicationId(ecsEsbAuditLog.getConsumingApp());
				ecsEsbRequestData.setCorrelationId(ecsEsbAuditLog.getExternalCorrelationId());
				ecsEsbRequestData.setTransactionId(ecsEsbAuditLog.getInternalCorrelationId());
			}else if (object instanceof MultiPriceTrialAdjudicatorV2Request) {
				MultiPriceTrialAdjudicatorV2Request request = (MultiPriceTrialAdjudicatorV2Request) object;
				//request = (MultiPriceTrialAdjudicatorV2Request) OWSUtil.trimAllStringFields(request);
				//request.setSearchInputMetaData( (SearchInputMetaData) OWSUtil.trimAllStringFields(request.getSearchInputMetaData()));
				ecsEsbAuditLog.setUserID(request.getSearchInputMetaData() == null ? "" : request.getSearchInputMetaData().getConsumerAppId());
				ecsEsbAuditLog.setConsumingApp(request.getSearchInputMetaData() == null ? "" : request.getSearchInputMetaData().getConsumerAppId());
				ecsEsbAuditLog.setInternalCorrelationId(generateCorrelationId(request));
				String externalCorrelationId = request.getSearchInputMetaData() != null ? request.getSearchInputMetaData().getExternalCorrelationId() : "";
				ecsEsbAuditLog.setExternalCorrelationId(externalCorrelationId);
				ecsEsbRequestData.setApplicationId(request.getSearchInputMetaData() == null ? "" : request.getSearchInputMetaData().getConsumerAppId());
				ecsEsbRequestData.setCorrelationId(ecsEsbAuditLog.getExternalCorrelationId());
				ecsEsbRequestData.setTransactionId(ecsEsbAuditLog.getInternalCorrelationId());
			}
			else if (object instanceof DrugProxyRequest) {
				DrugProxyRequest request = (DrugProxyRequest) object;
				request = (DrugProxyRequest) OWSUtil.trimAllStringFields(request);
				request.setHeader((Header) OWSUtil.trimAllStringFields(request.getHeader()));
				ecsEsbAuditLog.setUserID(request.getHeader() == null ? "" : request.getHeader().getClientId());
				ecsEsbAuditLog.setConsumingApp(request.getHeader() == null ? "" : request.getHeader().getConsumingApp());
				ecsEsbAuditLog.setExternalCorrelationId(request.getHeader() == null ? "" : request.getHeader().getCorrelationId());
				ecsEsbAuditLog.setInternalCorrelationId(request.getInternalId());
				ecsEsbRequestData.setApplicationId(ecsEsbAuditLog.getConsumingApp());
				ecsEsbRequestData.setCorrelationId(ecsEsbAuditLog.getExternalCorrelationId());
				ecsEsbRequestData.setTransactionId(ecsEsbAuditLog.getInternalCorrelationId());
			}

		}

		logRequest(ecsEsbRequestData);

		// Calculate method execution time
		Object retValue = null;
		Long enter_Service_Timestamp = System.currentTimeMillis();
		ecsEsbAuditLog.setTimestamp(usingDateFormatter(enter_Service_Timestamp));

		try {
			retValue = joinPoint.proceed();
		} catch (Throwable e) {
			logger.error(e.getMessage());
		}

		Long exit_Service_Timestamp = System.currentTimeMillis();
		long method_Execution_Time = exit_Service_Timestamp - enter_Service_Timestamp;
		ecsEsbAuditLog.setTimeduration(method_Execution_Time);

		EcsEsbReqRespData ecsEsbResponseData = new EcsEsbReqRespData();

		if(!(retValue instanceof Exception)) {
			ecsEsbResponseData.setMessage(convertObjToMsg(retValue));
			ecsEsbResponseData.setMessageType(OWSConstants.RESPONSE_TYPE);
		}
		// get response code and message
		if (retValue instanceof DrugSearchResult) {
			DrugSearchResult result = (DrugSearchResult) retValue;
			ecsEsbAuditLog.setStatus(result.getMessage().getRespCode());
			ecsEsbAuditLog.setMessage(result.getMessage().getRespMessage());
			if (result.getResponse() == null) {
				result.setResponse(new DrugSearchResponse());
			}
			result.getResponse().setCorrelationId(ecsEsbAuditLog.getExternalCorrelationId() == null || ecsEsbAuditLog.getExternalCorrelationId().equals("") ? ecsEsbAuditLog.getInternalCorrelationId() : ecsEsbAuditLog.getExternalCorrelationId());
			
			
			// Sends to the ECS_ESB_REPORTING_EXCHANGE
			String className = joinPoint.getSignature().getDeclaringTypeName();
			if(result.getResponse().getRxLinkReporting() != null){
				result.getResponse().getRxLinkReporting().setEventIdentifier(OWSUtil.getServiceName(className)); 
				result.getResponse().getRxLinkReporting().logData();
			}
			
		} else if (retValue instanceof MemberLoadResult) {
			MemberLoadResult result = (MemberLoadResult) retValue;
			ecsEsbAuditLog.setStatus(result.getMessage().getRespCode());
			ecsEsbAuditLog.setMessage(result.getMessage().getRespMessage());
			if (result.getResponse() == null) {
				result.setResponse(new MemberLoadResponse());
			}
			result.getResponse().setCorrelationId(ecsEsbAuditLog.getExternalCorrelationId() == null || ecsEsbAuditLog.getExternalCorrelationId().equals("") ? ecsEsbAuditLog.getInternalCorrelationId() : ecsEsbAuditLog.getExternalCorrelationId());
		} else if (retValue instanceof PharmacySearchResult) {
			PharmacySearchResult result = (PharmacySearchResult) retValue;
			ecsEsbAuditLog.setStatus(result.getMessage().getRespCode());
			ecsEsbAuditLog.setMessage(result.getMessage().getRespMessage());
			if (result.getResponse() == null) {
				result.setResponse(new PharmacySearchResponse());
			}
			result.getResponse().setCorrelationId(ecsEsbAuditLog.getExternalCorrelationId() == null || ecsEsbAuditLog.getExternalCorrelationId().equals("") ? ecsEsbAuditLog.getInternalCorrelationId() : ecsEsbAuditLog.getExternalCorrelationId());

		} else if (retValue instanceof PreferredPharmacyResult) {
			PreferredPharmacyResult result = (PreferredPharmacyResult) retValue;
			ecsEsbAuditLog.setStatus(result.getMessage().getRespCode());
			ecsEsbAuditLog.setMessage(result.getMessage().getRespMessage());
			result.setCorrelationId(ecsEsbAuditLog.getExternalCorrelationId() == null || ecsEsbAuditLog.getExternalCorrelationId().equals("") ? ecsEsbAuditLog.getInternalCorrelationId() : ecsEsbAuditLog.getExternalCorrelationId());

		} else if (retValue instanceof Result) {
			Result result = (Result) retValue;
			MemberSearchResponse response = new MemberSearchResponse();
			ecsEsbAuditLog.setStatus(result.getMessage().getRespCode());
			ecsEsbAuditLog.setMessage(result.getMessage().getRespMessage());
			if (result.getResponse() == null) {
				result.setResponse(new MemberSearchResponse());
			}
			result.getResponse().setCorrelationId(ecsEsbAuditLog.getExternalCorrelationId() == null || ecsEsbAuditLog.getExternalCorrelationId().equals("") ? ecsEsbAuditLog.getInternalCorrelationId() : ecsEsbAuditLog.getExternalCorrelationId());
		} else if (retValue instanceof ClaimSearchResult) {
			ClaimSearchResult result = (ClaimSearchResult) retValue;
			ecsEsbAuditLog.setStatus(result.getMessage().getRespCode());
			ecsEsbAuditLog.setMessage(result.getMessage().getRespMessage());
			result.setCorrelationId(ecsEsbAuditLog.getExternalCorrelationId() == null || ecsEsbAuditLog.getExternalCorrelationId().equals("") ? ecsEsbAuditLog.getInternalCorrelationId() : ecsEsbAuditLog.getExternalCorrelationId());
		} else if (retValue instanceof MemberDetailResult) {
			MemberDetailResult result = (MemberDetailResult) retValue;
			ecsEsbAuditLog.setStatus(result.getMessage().getRespCode());
			ecsEsbAuditLog.setMessage(result.getMessage().getRespMessage());
			if (result.getResponse() == null) {
				result.setResponse(new MemberDetailResponse());
			}
			result.getResponse().setCorrelationId(ecsEsbAuditLog.getExternalCorrelationId() == null || ecsEsbAuditLog.getExternalCorrelationId().equals("") ? ecsEsbAuditLog.getInternalCorrelationId() : ecsEsbAuditLog.getExternalCorrelationId());
		} else if (retValue instanceof BenefitInquiryResponse) {
			BenefitInquiryResponse result = (BenefitInquiryResponse) retValue;
			if (null != result && null != result.getIntegrationStatus() && null != result.getIntegrationStatus().getEsbStatus()) {
				ecsEsbAuditLog.setStatus(result.getIntegrationStatus().getEsbStatus().getResponseCode());
				ecsEsbAuditLog.setMessage(result.getIntegrationStatus().getEsbStatus().getResponseMessage());
				
				// Sends to the ECS_ESB_REPORTING_EXCHANGE
				String className = joinPoint.getSignature().getDeclaringTypeName();
				if(result.getRxLinkReporting() != null){
					result.getRxLinkReporting().setEventIdentifier(OWSUtil.getServiceName(className)); 
					result.getRxLinkReporting().logData();
				}
			}
		} else if (retValue instanceof PreferredAlternativesResponse) {
			PreferredAlternativesResponse result = (PreferredAlternativesResponse) retValue;
			ecsEsbAuditLog.setStatus(result.getIntegrationStatus().getEsbStatus().getResponseCode());
			ecsEsbAuditLog.setMessage(result.getIntegrationStatus().getEsbStatus().getResponseMessage());
			
			// Sends to the ECS_ESB_REPORTING_EXCHANGE
			String className = joinPoint.getSignature().getDeclaringTypeName();
			if(result.getRxLinkReporting() != null){
				result.getRxLinkReporting().setEventIdentifier(OWSUtil.getServiceName(className)); 
				result.getRxLinkReporting().logData();
			}

		} else if (retValue instanceof DiagnosisSearchResult) {
			DiagnosisSearchResult result = (DiagnosisSearchResult) retValue;
			ecsEsbAuditLog.setStatus(result.getMessage().getResponseCode());
			ecsEsbAuditLog.setMessage(result.getMessage().getResponseMessage());

		} else if (retValue instanceof MultiPriceTrialAdjudicatorResult) {
			MultiPriceTrialAdjudicatorResult result = (MultiPriceTrialAdjudicatorResult) retValue;
			ecsEsbAuditLog.setStatus(result.getMessage().getRespCode());
			ecsEsbAuditLog.setMessage(result.getMessage().getRespMessage());
			if (result.getResponse() == null) {
				result.setResponse(new MultiPriceTrialAdjudicatorResponse());
			}
			result.getResponse().setCorrelationId(ecsEsbAuditLog.getExternalCorrelationId() == null || ecsEsbAuditLog.getExternalCorrelationId().equals("") ? ecsEsbAuditLog.getInternalCorrelationId() : ecsEsbAuditLog.getExternalCorrelationId());
			
			// Sends to the ECS_ESB_REPORTING_EXCHANGE
			String className = joinPoint.getSignature().getDeclaringTypeName();
			if(result.getResponse().getRxLinkReporting() != null){
				result.getResponse().getRxLinkReporting().setEventIdentifier(OWSUtil.getServiceName(className)); 
				result.getResponse().getRxLinkReporting().logData();
			}			

		} else if (retValue instanceof PlanDetailResult) {
			PlanDetailResult result = (PlanDetailResult) retValue;
			ecsEsbAuditLog.setStatus(result.getMessage().getRespCode());
			ecsEsbAuditLog.setMessage(result.getMessage().getRespMessage());
			if (result.getResponse() == null) {
				result.setResponse(new PlanDetailResponse());
			}
			result.getResponse().setCorrelationId(ecsEsbAuditLog.getExternalCorrelationId() == null || ecsEsbAuditLog.getExternalCorrelationId().equals("") ? ecsEsbAuditLog.getInternalCorrelationId() : ecsEsbAuditLog.getExternalCorrelationId());
		} else if (retValue instanceof PriorAuthHistoryResult) {
			PriorAuthHistoryResult result = (PriorAuthHistoryResult) retValue;
			ecsEsbAuditLog.setStatus(result.getMessage().getRespCode());
			ecsEsbAuditLog.setMessage(result.getMessage().getRespMessage());
			if (result.getResponse() == null) {
				result.setResponse(new RxClaimPAHistoryResponse());
			}
			result.getResponse().setCorrelationId(ecsEsbAuditLog.getExternalCorrelationId() == null || ecsEsbAuditLog.getExternalCorrelationId().equals("") ? ecsEsbAuditLog.getInternalCorrelationId() : ecsEsbAuditLog.getExternalCorrelationId());

		} else if (retValue instanceof com.uhg.optumrx.domain.rxclaimpahistory.PriorAuthHistoryResult) {
			com.uhg.optumrx.domain.rxclaimpahistory.PriorAuthHistoryResult result = (com.uhg.optumrx.domain.rxclaimpahistory.PriorAuthHistoryResult) retValue;
			ecsEsbAuditLog.setStatus(result.getMessage().getRespCode());
			ecsEsbAuditLog.setMessage(result.getMessage().getRespMessage());
			if (result.getResponse() == null) {
				result.setResponse(new RxClaimPAHistoryResponse());
			}
			result.getResponse().setCorrelationId(ecsEsbAuditLog.getExternalCorrelationId() == null || ecsEsbAuditLog.getExternalCorrelationId().equals("") ? ecsEsbAuditLog.getInternalCorrelationId() : ecsEsbAuditLog.getExternalCorrelationId());

		} else if (retValue instanceof com.uhg.optumrx.domain.priorauthhistory.PriorAuthHistoryResult) {
			com.uhg.optumrx.domain.priorauthhistory.PriorAuthHistoryResult result = (com.uhg.optumrx.domain.priorauthhistory.PriorAuthHistoryResult) retValue;
			ecsEsbAuditLog.setStatus(result.getMessage().getRespCode());
			ecsEsbAuditLog.setMessage(result.getMessage().getRespMessage());
			if (result.getResponse() == null) {
				result.setResponse(new PriorAuthHistoryResponse());
			}
			result.getResponse().setCorrelationId(ecsEsbAuditLog.getExternalCorrelationId() == null || ecsEsbAuditLog.getExternalCorrelationId().equals("") ? ecsEsbAuditLog.getInternalCorrelationId() : ecsEsbAuditLog.getExternalCorrelationId());

		} else if (retValue instanceof com.uhg.optumrx.domain.pas.priorauthhistory.PriorAuthHistoryResult) {
			com.uhg.optumrx.domain.pas.priorauthhistory.PriorAuthHistoryResult result = (com.uhg.optumrx.domain.pas.priorauthhistory.PriorAuthHistoryResult) retValue;
			ecsEsbAuditLog.setStatus(result.getMessage().getRespCode());
			ecsEsbAuditLog.setMessage(result.getMessage().getRespMessage());
			if (result.getResponse() == null) {
				result.setResponse(new PASPriorAuthHistoryResponse());
			}
			result.getResponse().setCorrelationId(ecsEsbAuditLog.getExternalCorrelationId() == null || ecsEsbAuditLog.getExternalCorrelationId().equals("") ? ecsEsbAuditLog.getInternalCorrelationId() : ecsEsbAuditLog.getExternalCorrelationId());
		} else if (retValue instanceof DrugAlternativeResult) {
			DrugAlternativeResult result = (DrugAlternativeResult) retValue;
			ecsEsbAuditLog.setStatus(result.getMessage().getRespCode());
			ecsEsbAuditLog.setMessage(result.getMessage().getRespMessage());
			if (result.getResponse() == null) {
				result.setResponse(new DrugAlternativeResponse());
			}
			result.getResponse().setCorrelationId(ecsEsbAuditLog.getExternalCorrelationId() == null || ecsEsbAuditLog.getExternalCorrelationId().equals("") ? ecsEsbAuditLog.getInternalCorrelationId() : ecsEsbAuditLog.getExternalCorrelationId());
		} else if (retValue instanceof MemberSearchV2Result) {
			MemberSearchV2Result result = (MemberSearchV2Result) retValue;
			ecsEsbAuditLog.setStatus(result.getSearchOutputMetaData().getRespCode());
			String statusMessage = result.getElapsedTimeMsg() != null ? result.getElapsedTimeMsg().toString() : "";
			if (null !=result.getResponse() && null != result.getResponse().getMemberSearchItems()) {
				for (MemberSearchV2Item item : result.getResponse().getMemberSearchItems()) {
					if (item.getInstanceId() == null) {
						statusMessage = OWSUtil.convertToJson(memberSearchV2Request);
						ecsEsbAuditLog.setStatus(OWSConstants.HTTP_CODE_NO_CONTENT);
						break;
					}
				}
			}
			ecsEsbAuditLog.setMessage(statusMessage.length() > 2000 ? statusMessage.substring(0, 1999) : statusMessage);
		} else if (retValue instanceof MedicareMemberDetailResponse) {
			MedicareMemberDetailResponse result = (MedicareMemberDetailResponse) retValue;
			ecsEsbAuditLog.setStatus(result.getSearchOutputMetaData().getRespCode());
			String statusMsg = result.getSearchOutputMetaData().getRespMessage() != null ? result.getSearchOutputMetaData().getRespMessage().toString() : "";
			ecsEsbAuditLog.setMessage(statusMsg);
		} else if (retValue instanceof ClaimSearchV2Result) {
			ClaimSearchV2Result result = (ClaimSearchV2Result) retValue;
			ecsEsbAuditLog.setStatus(result.getSearchOutputMetaData().getRespCode());
			String statusMsg = result.getSearchOutputMetaData().getRespMessage() != null ? result.getSearchOutputMetaData().getRespMessage().toString() : "";
			ecsEsbAuditLog.setMessage(statusMsg);
		} else if (retValue instanceof ProductDetailsResponse) {
			ProductDetailsResponse result = (ProductDetailsResponse) retValue;
			ecsEsbAuditLog.setStatus(result.getSearchOutputMetaData().getRespCode());
			String statusMsg = result.getSearchOutputMetaData().getRespMessage() != null ? result.getSearchOutputMetaData().getRespMessage().toString() : "";
			statusMsg = result.getSearchOutputMetaData().getElapsedTimeMsg()!=null ? statusMsg+result.getSearchOutputMetaData().getElapsedTimeMsg():statusMsg;
			ecsEsbAuditLog.setMessage(statusMsg);
		} else if (retValue instanceof MemberPlanBenefitsResult) {
			MemberPlanBenefitsResult result = (MemberPlanBenefitsResult) retValue;
			ecsEsbAuditLog.setStatus(result.getSearchOutputMetaData().getRespCode());
			String statusMsg = result.getSearchOutputMetaData().getRespMessage() != null ? result.getSearchOutputMetaData().getRespMessage().toString() : "";
			statusMsg = result.getSearchOutputMetaData().getElapsedTimeMsg()!=null ? statusMsg+":"+result.getSearchOutputMetaData().getElapsedTimeMsg():statusMsg;
			ecsEsbAuditLog.setMessage(statusMsg);
		} else if (retValue instanceof ProductSearchV2Result) {
			ProductSearchV2Result result = (ProductSearchV2Result) retValue;
			ecsEsbAuditLog.setStatus(result.getSearchOutputMetaData().getRespCode());
//			String statusMsg = result.getSearchOutputMetaData().getRespMessage() != null ? result.getSearchOutputMetaData().getRespMessage().toString() : "";
			String statusMsg = result.getProductSearchResponse() != null && result.getProductSearchResponse().getElapsedTimeMsg() != null ? result.getProductSearchResponse().getElapsedTimeMsg().toString() : "";
			ecsEsbAuditLog.setMessage(statusMsg);
		}else if (retValue instanceof PharmacySearchV2Response) {
			PharmacySearchV2Response result = (PharmacySearchV2Response) retValue;
			ecsEsbAuditLog.setStatus(result.getSearchOutputMetaData().getRespCode());
			String statusMsg = result.getSearchOutputMetaData().getRespMessage() != null ? result.getSearchOutputMetaData().getRespMessage().toString() : "";
			statusMsg = result.getSearchOutputMetaData().getElapsedTimeMsg()!=null ? statusMsg+result.getSearchOutputMetaData().getElapsedTimeMsg():statusMsg;
			ecsEsbAuditLog.setMessage(statusMsg);
		}else if (retValue instanceof PrescriberSearchResult) {
			PrescriberSearchResult result = (PrescriberSearchResult) retValue;
			ecsEsbAuditLog.setStatus(result.getMessage().getRespCode());
			ecsEsbAuditLog.setMessage(result.getMessage().getRespMessage());
			if (result.getResponse() == null) {
				result.setResponse(new PrescriberSearchResponse());
			}
			result.getResponse().setCorrelationId(ecsEsbAuditLog.getExternalCorrelationId() == null || ecsEsbAuditLog.getExternalCorrelationId().equals("") ? ecsEsbAuditLog.getInternalCorrelationId() : ecsEsbAuditLog.getExternalCorrelationId());
		}else if (retValue instanceof PrescriberDetailResult) {
			PrescriberDetailResult result = (PrescriberDetailResult) retValue;
			ecsEsbAuditLog.setStatus(result.getMessage().getResponseCode());
			ecsEsbAuditLog.setMessage(result.getMessage().getResponseMessage());
			if (result.getResponse() == null) {
				result.setResponse(new PrescriberDetailResponse());
			}
			result.getResponse().setCorrelationId(ecsEsbAuditLog.getExternalCorrelationId() == null || ecsEsbAuditLog.getExternalCorrelationId().equals("") ? ecsEsbAuditLog.getInternalCorrelationId() : ecsEsbAuditLog.getExternalCorrelationId());
		}
		else if (retValue instanceof MedDAccumPhaseDetailResult) {
			MedDAccumPhaseDetailResult result = (MedDAccumPhaseDetailResult) retValue;
			ecsEsbAuditLog.setStatus(result.getMessage().getRespCode());
			ecsEsbAuditLog.setMessage(result.getMessage().getRespMessage());
		}
		else if (retValue instanceof DynamicPriorAuthResult) {
			DynamicPriorAuthResult result = (DynamicPriorAuthResult) retValue;
			ecsEsbAuditLog.setStatus(result.getMessage().getRespCode());
			ecsEsbAuditLog.setMessage(result.getMessage().getRespMessage());
			if (result.getResponse() == null) {
				result.setResponse(new DynamicPriorAuthResponse());
			}
			result.getResponse().setCorrelationId(ecsEsbAuditLog.getExternalCorrelationId() == null || ecsEsbAuditLog.getExternalCorrelationId().equals("") ? ecsEsbAuditLog.getInternalCorrelationId() : ecsEsbAuditLog.getExternalCorrelationId());
		}
		else if (retValue instanceof DrugProxyResult) {
			DrugProxyResult result = (DrugProxyResult) retValue;
			ecsEsbAuditLog.setStatus(result.getMessage().getRespCode());
			ecsEsbAuditLog.setMessage(result.getMessage().getRespMessage());
			if (result.getResponse() == null) {
				result.setResponse(new DrugProxyResponse());
			}
			result.getResponse().setCorrelationId(ecsEsbAuditLog.getExternalCorrelationId() == null || ecsEsbAuditLog.getExternalCorrelationId().equals("") ? ecsEsbAuditLog.getInternalCorrelationId() : ecsEsbAuditLog.getExternalCorrelationId());
		}
		else if (retValue instanceof ResponseEntity) {
			ResponseEntity responseEntity = (ResponseEntity) retValue;
			if (responseEntity.hasBody() && responseEntity.getBody() instanceof AppHealthStatus) {
				AppHealthStatus response = (AppHealthStatus) responseEntity.getBody();
				ecsEsbAuditLog.setConsumingApp(OWSConstants.HEALTH_CHECK_CONSUMER);
				ecsEsbAuditLog.setInternalId(OWSConstants.HEALTH_CHECK_CONSUMER);
				ecsEsbAuditLog.setInternalCorrelationId(OWSConstants.HEALTH_CHECK_CONSUMER);
				ecsEsbAuditLog.setExternalCorrelationId(OWSConstants.HEALTH_CHECK_CONSUMER);
				ecsEsbAuditLog.setUserID(OWSConstants.HEALTH_CHECK_CONSUMER);
				ecsEsbAuditLog.setStatus(responseEntity.getStatusCode().toString());
				ecsEsbAuditLog.setMessage(Arrays.toString(response.getMessage().toArray(new String[response.getMessage().size()])));
			}
		} 
		else if (retValue instanceof CallTrackingHistorySearchResult) {
			CallTrackingHistorySearchResult result = (CallTrackingHistorySearchResult) retValue;
			ecsEsbAuditLog.setStatus(result.getSearchOutputMetaData().getRespCode());
			ecsEsbAuditLog.setMessage(result.getSearchOutputMetaData().getRespMessage().toString());
		}
		else if (retValue instanceof CallTrackingHistoryUpdateResult) {
			CallTrackingHistoryUpdateResult result = (CallTrackingHistoryUpdateResult) retValue;
			ecsEsbAuditLog.setStatus(result.getUpdateOutputMetaData().getRespCode());
			ecsEsbAuditLog.setMessage(result.getUpdateOutputMetaData().getRespMessage().toString());
		}
		else if (retValue instanceof MultiPriceTrialAdjudicatorV2Result) {
			MultiPriceTrialAdjudicatorV2Result result = (MultiPriceTrialAdjudicatorV2Result) retValue;
			ecsEsbAuditLog.setStatus(result.getSearchOutputMetaData().getRespCode());
			String statusMsg = result.getResponse() != null && result.getResponse().getProviderElapsedTimeMsg() != null ? result.getResponse().getProviderElapsedTimeMsg().toString() : result.getSearchOutputMetaData().getRespMessage().toString();
			ecsEsbAuditLog.setMessage(statusMsg);
		}

		logAuditAndResponse(ecsEsbAuditLog, ecsEsbRequestData, ecsEsbResponseData);
		return retValue;
	}


	private void logRequest(EcsEsbReqRespData ecsEsbRequestData) {
		//  Log only when  LOG_REQUEST_TO_DB indicator is Y. By default value set as Y. Also when BACKOUT_TO_MYSQL_DB value is N, log request to DB2 DB
		if(OWSConstants.YES_IND.equals(propertyUtil.getString("LOG_REQUEST_TO_DB"))
				&& OWSConstants.NO_IND.equals(propertyUtil.getString("BACKOUT_TO_MYSQL_DB"))) {

			// Write the request to DB2 DB in JSON format.
			ecsEsbRequestData.logData();
		}
	}

	private void logAuditAndResponse(EcsEsbAuditLog ecsEsbAuditLog, EcsEsbReqRespData ecsEsbRequestData, EcsEsbReqRespData ecsEsbResponseData) {
		try {
			if(OWSConstants.YES_IND.equals(propertyUtil.getString("BACKOUT_TO_MYSQL_DB"))) {
				logger.info(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(ecsEsbAuditLog));
			} else {

				// Log to summary of transaction to Audit table in DB2
				ecsEsbAuditLog.logData();

				// Log Response in JSON format and only when LOG_RESPONSE_TO_DB indicator is Y. By default value set as Y.
				if(OWSConstants.YES_IND.equals(propertyUtil.getString("LOG_RESPONSE_TO_DB"))) {
					ecsEsbResponseData.setApplicationId(ecsEsbRequestData.getApplicationId());
					ecsEsbResponseData.setTransactionId(ecsEsbRequestData.getTransactionId() == null ? OWSConstants.UNKNOWN : ecsEsbRequestData.getTransactionId());
					ecsEsbResponseData.setCorrelationId(ecsEsbRequestData.getCorrelationId());
					ecsEsbResponseData.logData();
				}
			}
		} catch (JsonGenerationException e) {
			logger.error(e.getMessage(), e);
		} catch (JsonMappingException e) {
			logger.error(e.getMessage(), e);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	private void logAuditDetails(EcsEsbAuditSupplLog ecsEsbAuditSupplLog) {
		try {
				// Log Response in JSON format and only when LOG_RESPONSE_TO_DB indicator is Y. By default value set as Y.
				if(OWSConstants.YES_IND.equals(propertyUtil.getString("LOG_DETAILS_TO_DB"))) {
						ecsEsbAuditSupplLog.logData();
				}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	private String usingDateFormatter(long input) {
		Date date = new Date(input);
		Calendar cal = new GregorianCalendar();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MMM/dd hh:mm:ss a z");
		sdf.setCalendar(cal);
		cal.setTime(date);
		return sdf.format(date);
	}

	private String generateCorrelationId(MemberSearchV2Request memberSearchV2Request) {
		String consumerAppId = memberSearchV2Request.getSearchInputMetaData() != null ? memberSearchV2Request.getSearchInputMetaData().getConsumerAppId() : null;
		String correlationId = correlationService.generateCorrelationId(new java.util.Date(), consumerAppId);
		if(memberSearchV2Request.getSearchInputMetaData() != null) {
			memberSearchV2Request.getSearchInputMetaData().setInternalCorrelationId(correlationId);
		}
		return correlationId;
	}

	private String generateCorrelationId(MedicareMemberDetailRequest medicareMemberDetailRequest) {
		String correlationId = correlationService.generateCorrelationId(new java.util.Date(), medicareMemberDetailRequest.getSearchInputMetaData()!=null?medicareMemberDetailRequest.getSearchInputMetaData().getConsumerAppId():"");
		if(medicareMemberDetailRequest.getSearchInputMetaData() != null) {
			medicareMemberDetailRequest.getSearchInputMetaData().setInternalCorrelationId(correlationId);
		}
		return correlationId;
	}

	private String generateCorrelationId(ClaimSearchV2ServiceRequest claimSearchV2ServiceRequest) {
		String correlationId = correlationService.generateCorrelationId(new java.util.Date(), claimSearchV2ServiceRequest.getSearchInputMetaData()!=null?claimSearchV2ServiceRequest.getSearchInputMetaData().getConsumerAppId():"");
		if(claimSearchV2ServiceRequest.getSearchInputMetaData()!=null)
			claimSearchV2ServiceRequest.getSearchInputMetaData().setInternalCorrelationId(correlationId);
		return correlationId;
	}
	private String generateCorrelationId(ProductDetailsRequest productDetailsRequest) {
		String correlationId = correlationService.generateCorrelationId(new java.util.Date(), productDetailsRequest.getSearchInputMetaData()!=null?productDetailsRequest.getSearchInputMetaData().getConsumerAppId():"");
		if(productDetailsRequest.getSearchInputMetaData()!=null)
			productDetailsRequest.getSearchInputMetaData().setInternalCorrelationId(correlationId);
		return correlationId;
	}

	private String generateCorrelationId(ProductSearchRequest productSearchRequest) {
		String correlationId = correlationService.generateCorrelationId(new java.util.Date(), productSearchRequest.getSearchInputMetaData()!=null?productSearchRequest.getSearchInputMetaData().getConsumerAppId():"");
		if(productSearchRequest.getSearchInputMetaData()!=null)
			productSearchRequest.getSearchInputMetaData().setInternalCorrelationId(correlationId);
		return correlationId;
	}
	
	private String generateCorrelationId(PharmacySearchV2Request pharmacySearchV2Request) {
		String consumerAppId = pharmacySearchV2Request.getSearchInputMetaData() != null ? pharmacySearchV2Request.getSearchInputMetaData().getConsumerAppId() : null;
		String correlationId = correlationService.generateCorrelationId(new java.util.Date(), consumerAppId);
		if(pharmacySearchV2Request.getSearchInputMetaData() != null) {
			pharmacySearchV2Request.getSearchInputMetaData().setInternalCorrelationId(correlationId);
		}
		return correlationId;
	}
	
	private String generateCorrelationId(CallTrackingHistorySearchRequest request) {
		String correlationId = correlationService.generateCorrelationId(new java.util.Date(), request.getSearchInputMetaData()!=null?request.getSearchInputMetaData().getConsumerAppId():"");
		if(request.getSearchInputMetaData()!=null)
			request.getSearchInputMetaData().setInternalCorrelationId(correlationId);
		return correlationId;
	}
	
	private String generateCorrelationId(CallTrackingHistoryUpdateRequest request) {
		String correlationId = correlationService.generateCorrelationId(new java.util.Date(), request.getSearchInputMetaData()!=null?request.getSearchInputMetaData().getConsumerAppId():"");
		if(request.getSearchInputMetaData()!=null)
			request.getSearchInputMetaData().setInternalCorrelationId(correlationId);
		return correlationId;
	}

	private String generateCorrelationId(MemberPlanBenefitsRequest memberPlanBenefitsRequest) {
		String consumerAppId = memberPlanBenefitsRequest.getSearchInputMetaData() != null ? memberPlanBenefitsRequest.getSearchInputMetaData().getConsumerAppId() : null;
		String correlationId = correlationService.generateCorrelationId(new java.util.Date(), consumerAppId);
		if(memberPlanBenefitsRequest.getSearchInputMetaData() != null) {
			memberPlanBenefitsRequest.getSearchInputMetaData().setInternalCorrelationId(correlationId);
		}
		return correlationId;
	}
	
	private String generateCorrelationId(MultiPriceTrialAdjudicatorV2Request request) {
		String consumerAppId = request.getSearchInputMetaData() != null ? request.getSearchInputMetaData().getConsumerAppId() : null;
		String correlationId = correlationService.generateCorrelationId(new java.util.Date(), consumerAppId);
		if(request.getSearchInputMetaData() != null) {
			request.getSearchInputMetaData().setInternalCorrelationId(correlationId);
		}
		return correlationId;
	}
	private String convertObjToMsg(Object object) {
		try {
			return mapper.writeValueAsString(object);
		} catch (IOException ioe) {
			return ioe.getMessage();
		}
	}
}
