package hiberspring.service.impls;

import hiberspring.domain.dtos.EmployeeSeedRootDto;
import hiberspring.domain.entities.Branch;
import hiberspring.domain.entities.Employee;
import hiberspring.domain.entities.EmployeeCard;
import hiberspring.repository.EmployeeRepository;
import hiberspring.service.BranchService;
import hiberspring.service.EmployeeCardService;
import hiberspring.service.EmployeeService;
import hiberspring.util.ValidationUtil;
import hiberspring.util.XmlParser;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import javax.xml.bind.JAXBException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static hiberspring.common.Constants.*;

@Service
@Transactional
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final XmlParser xmlParser;
    private final ModelMapper modelMapper;
    private final ValidationUtil validationUtil;
    private final EmployeeCardService employeeCardService;
    private final BranchService branchService;

    @Autowired
    public EmployeeServiceImpl(EmployeeRepository employeeRepository,
                               XmlParser xmlParser,
                               ModelMapper modelMapper,
                               ValidationUtil validationUtil,
                               EmployeeCardService employeeCardService,
                               BranchService branchService) {
        this.employeeRepository = employeeRepository;
        this.xmlParser = xmlParser;
        this.modelMapper = modelMapper;
        this.validationUtil = validationUtil;
        this.employeeCardService = employeeCardService;
        this.branchService = branchService;
    }

    @Override
    public Boolean employeesAreImported() {
        return this.employeeRepository.count() > 0;
    }

    @Override
    public String readEmployeesXmlFile() throws IOException {
        return Files.readString(Path.of(EMPLOYEES_FILE_PATH));
    }

    @Override
    public String importEmployees() throws JAXBException, FileNotFoundException {
        StringBuilder stringBuilder = new StringBuilder();

        EmployeeSeedRootDto employeeSeedRootDto = this.xmlParser
                .parseXml(EmployeeSeedRootDto.class, EMPLOYEES_FILE_PATH);

        employeeSeedRootDto.getEmployeeSeedDtos()
                .forEach(employeeSeedDto -> {
                    if (this.employeeRepository
                            .findFirstByFirstNameAndLastName(employeeSeedDto
                                    .getFirstName(), employeeSeedDto
                                    .getLastName()) != null) {
                        stringBuilder.append("Already in DB.")
                                .append(System.lineSeparator());
                        return;
                    }

                    if (this.validationUtil.isValid(employeeSeedDto)) {
                        EmployeeCard card = this.employeeCardService
                                .getEmployeeCardByNumber(employeeSeedDto.getCard());

                        Branch branch = this.branchService
                                .getBranchByName(employeeSeedDto.getBranch());

                        if (card != null && branch != null) {
                            Employee employee = this.modelMapper
                                    .map(employeeSeedDto, Employee.class);

                            employee.setCard(card);
                            employee.setBranch(branch);

                            this.employeeRepository.saveAndFlush(employee);

                            stringBuilder.append(String
                                    .format(SUCCESSFUL_IMPORT_MESSAGE,
                                            employee.getClass().getSimpleName(),
                                            employee.getFirstName() + " " + employee.getLastName()));
                        } else {
                            stringBuilder.append(INCORRECT_DATA_MESSAGE);
                        }
                    } else {
                        stringBuilder.append(INCORRECT_DATA_MESSAGE);
                    }

                    stringBuilder.append(System.lineSeparator());
                });

        return stringBuilder.toString().trim();
    }

    @Override
    public String exportProductiveEmployees() {
        StringBuilder stringBuilder = new StringBuilder();

        this.employeeRepository
                .getAllProductiveEmployees()
                .forEach(employee -> {
            stringBuilder.append(String.format("Name: %s\n" +
                    "Position: %s\n" +
                    "Card Number: %s\n" +
                    "-------------------------",
                    employee.getFirstName() + " " + employee.getLastName(),
                    employee.getPosition(),
                    employee.getCard().getNumber()))
                    .append(System.lineSeparator());
        });

        return stringBuilder.toString().trim();
    }

    @Override
    public Employee getEmployeeByFirstAndLastName(String firstName, String lastName) {
        return this.employeeRepository.findFirstByFirstNameAndLastName(firstName, lastName);
    }
}
