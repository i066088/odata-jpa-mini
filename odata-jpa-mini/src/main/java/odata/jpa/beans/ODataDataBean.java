package odata.jpa.beans;

public class ODataDataBean {

	private Object data;

	public ODataDataBean() {
	}

	public ODataDataBean(Object data) {
		this.data = data;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}
}
