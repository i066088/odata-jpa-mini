package odata.jpa.beans;

public class ODataDataBean {

	private Object data;

	public ODataDataBean() {
	}

	public ODataDataBean(Object value) {
		this.data = value;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}
}
