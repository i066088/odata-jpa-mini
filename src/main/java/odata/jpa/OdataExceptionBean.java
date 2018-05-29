package odata.jpa;

import javax.ws.rs.core.Response.Status;

/**
 * JSON bean representing exceptions
 * 
 * @see http://www.odata.org/documentation/odata-version-3-0/json-verbose-format/#representingerrorsinaresponse
 *
 */
public class OdataExceptionBean {

	public static class OdataExceptionErrorBean {
		private String lang = "en-us";
		private String value;

		public OdataExceptionErrorBean() {
		}

		public OdataExceptionErrorBean(String lang, String value) {
			this.lang = lang;
			this.value = value;
		}

		public String getLang() {
			return lang;
		}

		public void setLang(String lang) {
			this.lang = lang;
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}
	}

	private OdataExceptionErrorBean error = new OdataExceptionErrorBean();
	private String code;

	public OdataExceptionBean() {
	}

	public OdataExceptionBean(int status, String errorMsg) {
		this.code = Integer.toString(status);
		this.error.value = errorMsg;
	}

	public OdataExceptionBean(Status status, String errorMsg) {
		this(status.getStatusCode(), errorMsg);
	}

	public OdataExceptionBean(int status, String lang, String errorMsg) {
		this(status, errorMsg);
		this.error.lang = lang;
	}

	public OdataExceptionBean(Status status, String lang, String errorMsg) {
		this(status.getStatusCode(), errorMsg);
		this.error.lang = lang;
	}

	public OdataExceptionErrorBean getError() {
		return error;
	}

	public void setError(OdataExceptionErrorBean error) {
		this.error = error;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

}
