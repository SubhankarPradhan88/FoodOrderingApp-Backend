package com.upgrad.FoodOrderingApp.service.entity;

import org.apache.commons.lang3.builder.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Collection;
import java.util.List;
import java.io.Serializable;

@Entity
@Table(name = "customer")
@NamedQueries(
        {
                @NamedQuery(name = "customerByContactNumber", query = "select c from CustomerEntity c where c.contactNumber =:contact_number"),
                @NamedQuery(name = "customerByFirstname", query = "select c from CustomerEntity c where c.firstname =:firstname"),
                @NamedQuery(name = "customerByUuid", query = "select c from CustomerEntity c where c.uuid  =:uuid")
        }
)

public class CustomerEntity implements Serializable {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "uuid")
    @Size(max = 200)
    private String uuid;

    @Column(name = "firstname")
    @Size(max = 30)
    private String firstname;

    @Column(name = "lastname")
    @Size(max = 30)
    private String lastname;

    @Column(name = "email")
    @Size(max = 50)
    private String email;

    @Column(name = "contact_number")
    @Size(max = 30)
    private String contactNumber;

    @Column(name = "password")
    @Size(max = 255)
    @ToStringExclude
    private String password;

    @Column(name = "salt")
    @NotNull
    @Size(max = 255)
    @ToStringExclude
    private String salt;

    @Override
    public boolean equals(Object obj) {
        return new EqualsBuilder().append(this, obj).isEquals();
    }

//    @ManyToMany(mappedBy = "customers")
//    private List<AddressEntity> addresses;
//
//    public List<AddressEntity> getAddresses() {
//        return addresses;
//    }
//    public void setAddresses(List<AddressEntity> addresses) {
//        this.addresses = addresses;
//    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getFirstName() { return firstname; }

    public void setFirstName(String firstname) {
        this.firstname = firstname;
    }

    public String getLastName() {
        return lastname;
    }

    public void setLastName(String lastname) {
        this.lastname = lastname;
    }

    public String getEmailAddress() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactnumber) {
        this.contactNumber = contactnumber;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(this).hashCode();
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    @OneToMany(mappedBy = "customer", fetch = FetchType.EAGER)
    private List<AddressEntity> addresses;
        public List<AddressEntity> getAddresses() {
            return addresses;
        }
}
