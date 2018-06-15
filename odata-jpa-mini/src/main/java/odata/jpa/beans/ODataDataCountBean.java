package odata.jpa.beans;

public class ODataDataCountBean extends ODataDataBean {

	private Long count;

	public ODataDataCountBean() {
	}

	public ODataDataCountBean(Object data, Long count) {
		super(data);
		this.count = count;
	}

	public Long getCount() {
		return count;
	}

	public void setCount(Long count) {
		this.count = count;
	}
}
