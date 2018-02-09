package com.uhg.optumrx.utility;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uhg.optumrx.OWSConstants;
import com.uhg.optumrx.domain.CommonRoutingInfo;
import com.uhg.optumrx.domain.StrictSimpleDateFormat;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;

public class OWSUtil {

	@Autowired
	private RoutingInfoUtil routingInfoUtil;

	private static final Logger logger = LogManager.getLogger(OWSUtil.class.getName());
	private static ObjectMapper mapper = new ObjectMapper();
	public static Map<String, String> serviceNames = new HashMap<String, String>();

	public static Map<String, String> serviceDetailNames = new HashMap<String, String>();

	static {
		serviceNames.put("com.uhg.optumrx.controller.MemberSearchController", "MemberSearch");
		serviceNames.put("com.uhg.optumrx.controller.MemberDetailController", "MemberDetail");
		serviceNames.put("com.uhg.optumrx.controller.DrugSearchController", "DrugSearch");
		serviceNames.put("com.uhg.optumrx.controller.BenefitInquiryController", "BenefitInquiry");
		serviceNames.put("com.uhg.optumrx.controller.MultiPriceTrialAdjudicatorController", "TrialClaim");
		serviceNames.put("com.uhg.optumrx.controller.PharmacySearchController", "PharmacySearch");
		serviceNames.put("com.uhg.optumrx.controller.PreferredPharmacyController", "PreferredPharmacy");
		serviceNames.put("com.uhg.optumrx.controller.PlanDetailController", "PlanDetail");
		serviceNames.put("com.uhg.optumrx.controller.RxClaimPAHistoryController", "RxClaimPAHistory");
		serviceNames.put("com.uhg.optumrx.controller.PreferredAlternativesController", "PreferredAlternatives");
		serviceNames.put("com.uhg.optumrx.controller.DrugAlternativeController", "DrugAlternative");
		serviceNames.put("com.uhg.optumrx.controller.PasPriorAuthController", "PasPriorAuth");
		serviceNames.put("com.uhg.optumrx.controller.PrescriberSearchController", "PrescriberSearch");
		serviceNames.put("com.uhg.optumrx.controller.MedDAccumPhaseDetailController", "MedDAccumPhaseDetail");
		serviceNames.put("com.uhg.optumrx.controller.HealthCheckController", "HealthCheck");
		serviceNames.put("com.uhg.optumrx.controller.MemberPlanBenefitsController", "MemberPlanBenefits");
		serviceNames.put("com.uhg.optumrx.controller.ProductSearchController", "ProductSearch");
		serviceNames.put("com.uhg.optumrx.controller.ClaimSearchController", "ClaimSearch");
		serviceNames.put("com.uhg.optumrx.controller.MemberLoadController", "MemberLoad");
		serviceNames.put("com.uhg.optumrx.controller.PrescriberDetailController", "PrescriberDetail");
		serviceNames.put("com.uhg.optumrx.controller.PriorAuthHistoryController", "PriorAuthHistory");
		serviceNames.put("com.uhg.optumrx.controller.DynamicPriorAuthController", "DynamicPriorAuth");
		serviceNames.put("com.uhg.optumrx.controller.DrugProxyController", "DrugProxy");
		serviceNames.put("com.uhg.optumrx.controller.MemberSearchV2Controller", "MemberSearchV2");
		serviceNames.put("com.uhg.optumrx.controller.MedicareMemberDetailContoller", "MedicareMemberDetail");
		serviceNames.put("com.uhg.optumrx.controller.CallTrackingHistorySearchController", "CallTrackingHistorySearch");
		serviceNames.put("com.uhg.optumrx.controller.CallTrackingHistoryUpdateController", "CallTrackingHistoryUpdate");
		serviceNames.put("com.uhg.optumrx.controller.ClaimSearchV2Controller", "ClaimSearchV2");
		serviceNames.put("com.uhg.optumrx.controller.MemberPlanBenefitsController", "MemberPlanBenefits");
		serviceNames.put("com.uhg.optumrx.controller.MultiPriceTrialAdjudicatorV2Controller", "TrialAdjudicatorV2");
		serviceNames.put("com.uhg.optumrx.controller.PharmacySearchV2Controller", "PharmacySearchV2");
		serviceNames.put("com.uhg.optumrx.controller.ProductDetailsController", "ProductDetails");
		serviceNames.put("com.uhg.optumrx.controller.ProductSearchV2Controller", "ProductSearchV2");
		serviceNames.put("com.uhg.optumrx.controller.ClaimMedDPhaseController", "ClaimMedDPhase");

		
		serviceDetailNames.put("com.uhg.optumrx.integration.SearchMember", "MemberSearch");
		serviceDetailNames.put("com.uhg.optumrx.integration.DrugSearch", "DrugSearch");
		serviceDetailNames.put("com.uhg.optumrx.integration.MultiPriceAdjudicatorImpl", "TrialClaim");
		serviceDetailNames.put("com.uhg.optumrx.integration.PlanDetail", "PlanDetail");
		serviceDetailNames.put("com.uhg.optumrx.integration.PreferredAlternatives", "PreferredAlternatives");
		serviceDetailNames.put("com.uhg.optumrx.integration.DrugAlternative", "DrugAlternative");
		serviceDetailNames.put("com.uhg.optumrx.integration.DrugProxy", "DrugProxy");
		
		serviceDetailNames.put("com.uhg.optumrx.integration.MemberSearchV2", "MemberSearchV2");
		serviceDetailNames.put("com.uhg.optumrx.integration.MemberFinder", "MemberFinder");
		serviceDetailNames.put("com.uhg.optumrx.integration.MemberFinder", "Raja");

	}

	public static String getServiceName(String name) {
		return serviceNames.get(name);

	}

	public static String getServiceDetailName(String name) {
		return serviceDetailNames.get(name);

	}

	public static Date convertToDate(String paramString1, String paramString2) {
		String DATE_PATTERN = "yyyyMMdd";
		Date localDate = null;
		if ((paramString1 != null) && (!paramString1.equals("00000000"))
				&& ((paramString2.equals(DATE_PATTERN)) || (paramString1.length() == 8))) {
			try {
				StrictSimpleDateFormat localStrictSimpleDateFormat = new StrictSimpleDateFormat(paramString2);
				localStrictSimpleDateFormat.setLenient(false);
				localDate = localStrictSimpleDateFormat.parse(paramString1);
			} catch (ParseException localException) {
				logger.error(localException.getMessage());
			}
		} else if ((paramString1 != null) && (paramString1.length() == 10) && ((paramString2.equals("yyyy-MM-dd")))) {
			try {
				StrictSimpleDateFormat sdf = new StrictSimpleDateFormat(paramString2);
				sdf.setLenient(false);
				localDate = sdf.parse(paramString1);
			} catch (ParseException localException) {
				logger.error(localException.getMessage());
			}
		}
		return localDate;
	}

	public static Object trimAllStringFields(Object object) {
		ObjectUtil util = new ObjectUtil();
		try {
			util.trimAllStrings(object);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return object;
	}

	public static String getSpaceAsNull(String value) {
		if (value != null) {
			value = value.trim();
			if (value.length() == 0) {
				value = null;
			}
		}
		return value;
	}

	public static String trimToEmpty(final String str) {
		return str == null ? "" : str.trim();
	}

	public static String currentDate() {
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		return dateFormat.format(new Date());
	}

	public static Date convertStringToDate(String dateStr, String pattern) throws ParseException {
		StrictSimpleDateFormat localStrictSimpleDateFormat = new StrictSimpleDateFormat(pattern);
		Date formattedDate = localStrictSimpleDateFormat.parse(dateStr);
		return formattedDate;
	}

	public static Date getDateWithoutTime(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}

	// timestamp format: YYYY-MM-DD hh:mm:ss.sss
	public static String getRxServiceReferenceNo(String ipAddress) {
		String timestamp = getSystemTimestamp();
		String ipa = "";
		String hhmmsssss = "";
		if (StringUtils.isNotBlank(ipAddress)) {
			String hostAddress = ipAddress.replace(".", "");
			ipa = hostAddress.length() > 3 ? hostAddress.substring(hostAddress.length() - 3) : hostAddress;
		}
		if (StringUtils.isNotBlank(timestamp) && timestamp.length() >= 23)
			hhmmsssss = timestamp.substring(11, 23).replace(":", "").replace(".", "");
		return (ipa + hhmmsssss);
	}

	public static String getSystemTimestamp() {
		String dateStr = "";
		DateFormat writeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.sss");
		Date date = new Date();
		if (date != null) {
			dateStr = writeFormat.format(date);
		}
		return dateStr;
	}

	public static String convertToJson(Object obj) {
		try {
			return mapper.writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			logger.warn("Unable to convert to JSON" + e.getMessage());
			return obj.toString();
		}
	}

	public static long getDaysRangeFromDates(Date fromDate, Date toDate, TimeUnit timeUnit) {
		if (fromDate == null || toDate == null)
			return 0L;
		long diffInMillies = toDate.getTime() - fromDate.getTime();
		return timeUnit.toDays(diffInMillies);
	}

	public static boolean isAfter(String fromDate, String toDate, String format) {
		StrictSimpleDateFormat localStrictSimpleDateFormat = new StrictSimpleDateFormat(format);
		localStrictSimpleDateFormat.setLenient(false);
		try {
			Date from = localStrictSimpleDateFormat.parse(fromDate);
			Date to = localStrictSimpleDateFormat.parse(toDate);
			return from.after(to);
		} catch (ParseException localException) {
			logger.error(localException.getMessage());
		}
		return false;
	}

	public static int daysBetweenUsingJoda(Date d1, Date d2) {
		return Days.daysBetween(new LocalDate(d1.getTime()), new LocalDate(d2.getTime())).getDays();
	}

	public static boolean isNumeric(String str) {
		if (str == null) {
			return false;
		}
		int sz = str.length();
		for (int i = 0; i < sz; i++) {
			if (Character.isDigit(str.charAt(i)) == false) {
				return false;
			}
		}
		return true;
	}
}
