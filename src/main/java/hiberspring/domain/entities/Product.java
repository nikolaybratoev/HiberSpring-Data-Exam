package hiberspring.domain.entities;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

import static javax.persistence.CascadeType.ALL;

@Entity
@Table(name = "products")
public class Product extends BaseEntity {

    private String name;
    private int clients;
    private Branch branch;

    public Product() {
    }

    @NotNull
    @Column(name = "name",
            nullable = false,
            unique = true)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @NotNull
    @Positive
    @Min(value = 1)
    @Column(name = "clients")
    public int getClients() {
        return clients;
    }

    public void setClients(int clients) {
        this.clients = clients;
    }

    @ManyToOne(cascade = ALL)
    public Branch getBranch() {
        return branch;
    }

    public void setBranch(Branch branch) {
        this.branch = branch;
    }
}
