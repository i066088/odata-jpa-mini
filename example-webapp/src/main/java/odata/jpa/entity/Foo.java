/*
* WebTemplate 1.0
* Luca Vercelli 2017
* Released under MIT license 
*/
package odata.jpa.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@Table(name = "FOO")
@XmlRootElement
public class Foo {

	private Long id;
	private String name;
	private Integer height;
	private String address;
	private Double age;
	private Date birthday;
	private Boolean3 nice; // EnumType
	private Bar favouriteBar; // Navigation
	private List<Bar> bars = new ArrayList<>(); // Navigation to Collection

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID")
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name = "NAME", length = 50)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "HEIGHT")
	public Integer getHeight() {
		return height;
	}

	public void setHeight(Integer height) {
		this.height = height;
	}

	@Column(name = "ADDRESS", length = 500)
	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	@Column(name = "AGE")
	public Double getAge() {
		return age;
	}

	public void setAge(Double age) {
		this.age = age;
	}

	@Column(name = "BIRTHDAY")
	@Temporal(value = TemporalType.DATE)
	public Date getBirthday() {
		return birthday;
	}

	public void setBirthday(Date birthday) {
		this.birthday = birthday;
	}

	@Column(name = "NICE")
	@Enumerated(EnumType.STRING)
	public Boolean3 getNice() {
		return nice;
	}

	public void setNice(Boolean3 nice) {
		this.nice = nice;
	}

	public Bar getFavouriteBar() {
		return favouriteBar;
	}

	public void setFavouriteBar(Bar favouriteBar) {
		this.favouriteBar = favouriteBar;
	}

	// FIXME I have to make it EAGER otherwise serialization fails...
	@OneToMany(fetch = FetchType.EAGER)
	public List<Bar> getBars() {
		return bars;
	}

	public void setBars(List<Bar> bars) {
		this.bars = bars;
	}

	@Override
	public String toString() {
		return "Foo " + getName();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((address == null) ? 0 : address.hashCode());
		result = prime * result + ((age == null) ? 0 : age.hashCode());
		result = prime * result + ((bars == null) ? 0 : bars.hashCode());
		result = prime * result + ((birthday == null) ? 0 : birthday.hashCode());
		result = prime * result + ((height == null) ? 0 : height.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((nice == null) ? 0 : nice.hashCode());
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
		Foo other = (Foo) obj;
		if (address == null) {
			if (other.address != null)
				return false;
		} else if (!address.equals(other.address))
			return false;
		if (age == null) {
			if (other.age != null)
				return false;
		} else if (!age.equals(other.age))
			return false;
		if (bars == null) {
			if (other.bars != null)
				return false;
		} else if (!bars.equals(other.bars))
			return false;
		if (birthday == null) {
			if (other.birthday != null)
				return false;
		} else if (!birthday.equals(other.birthday))
			return false;
		if (height == null) {
			if (other.height != null)
				return false;
		} else if (!height.equals(other.height))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (nice != other.nice)
			return false;
		return true;
	}
}
