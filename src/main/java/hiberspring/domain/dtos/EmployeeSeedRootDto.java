package hiberspring.domain.dtos;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import java.util.List;

import static javax.xml.bind.annotation.XmlAccessType.FIELD;

@XmlRootElement(name = "employees")
@XmlAccessorType(FIELD)
public class EmployeeSeedRootDto {

    @XmlElement(name = "employee")
    List<EmployeeSeedDto> employeeSeedDtos;

    public EmployeeSeedRootDto() {
    }

    public List<EmployeeSeedDto> getEmployeeSeedDtos() {
        return employeeSeedDtos;
    }

    public void setEmployeeSeedDtos(List<EmployeeSeedDto> employeeSeedDtos) {
        this.employeeSeedDtos = employeeSeedDtos;
    }
}
