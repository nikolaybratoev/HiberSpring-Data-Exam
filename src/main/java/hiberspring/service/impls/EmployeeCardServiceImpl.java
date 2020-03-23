package hiberspring.service.impls;

import com.google.gson.Gson;
import hiberspring.domain.dtos.EmployeeCardSeedDto;
import hiberspring.domain.entities.EmployeeCard;
import hiberspring.repository.EmployeeCardRepository;
import hiberspring.service.EmployeeCardService;
import hiberspring.util.ValidationUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static hiberspring.common.Constants.*;

@Service
public class EmployeeCardServiceImpl implements EmployeeCardService {

    private final Gson gson;
    private final EmployeeCardRepository employeeCardRepository;
    private final ModelMapper modelMapper;
    private final ValidationUtil validationUtil;

    @Autowired
    public EmployeeCardServiceImpl(Gson gson,
                                   EmployeeCardRepository employeeCardRepository,
                                   ModelMapper modelMapper,
                                   ValidationUtil validationUtil) {
        this.gson = gson;
        this.employeeCardRepository = employeeCardRepository;
        this.modelMapper = modelMapper;
        this.validationUtil = validationUtil;
    }

    @Override
    public Boolean employeeCardsAreImported() {
        return this.employeeCardRepository.count() > 0;
    }

    @Override
    public String readEmployeeCardsJsonFile() throws IOException {
        return Files.readString(Path.of(EMPLOYEE_CARDS_FILE_PATH));
    }

    @Override
    public String importEmployeeCards(String employeeCardsFileContent) throws FileNotFoundException {
        StringBuilder stringBuilder = new StringBuilder();

        EmployeeCardSeedDto[] employeeCardSeedDtos = this.gson
                .fromJson(new FileReader(EMPLOYEE_CARDS_FILE_PATH), EmployeeCardSeedDto[].class);

        Arrays.stream(employeeCardSeedDtos)
                .forEach(employeeCardSeedDto -> {
                    if (this.employeeCardRepository
                            .findFirstByNumber(employeeCardSeedDto.getNumber()) != null) {
                        stringBuilder.append("Already in DB.")
                                .append(System.lineSeparator());
                        return;
                    }

                    if (this.validationUtil.isValid(employeeCardSeedDto)) {
                        EmployeeCard employeeCard = this.modelMapper
                                .map(employeeCardSeedDto, EmployeeCard.class);

                        this.employeeCardRepository.saveAndFlush(employeeCard);

                        stringBuilder.append(String
                                .format(SUCCESSFUL_IMPORT_MESSAGE,
                                        employeeCard.getClass().getSimpleName(),
                                        employeeCard.getNumber()));
                    } else {
                        stringBuilder.append(INCORRECT_DATA_MESSAGE);
                    }

                    stringBuilder.append(System.lineSeparator());
                });

        return stringBuilder.toString().trim();
    }

    @Override
    public EmployeeCard getEmployeeCardByNumber(String number) {
        return this.employeeCardRepository.findFirstByNumber(number);
    }
}
