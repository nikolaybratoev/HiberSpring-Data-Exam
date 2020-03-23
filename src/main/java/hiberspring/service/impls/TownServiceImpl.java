package hiberspring.service.impls;

import com.google.gson.Gson;
import hiberspring.domain.dtos.TownSeedDto;
import hiberspring.domain.entities.Town;
import hiberspring.repository.TownRepository;
import hiberspring.service.TownService;
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
public class TownServiceImpl implements TownService {

    private final ModelMapper modelMapper;
    private final TownRepository townRepository;
    private final ValidationUtil validationUtil;
    private final Gson gson;

    @Autowired
    public TownServiceImpl(ModelMapper modelMapper,
                           TownRepository townRepository,
                           ValidationUtil validationUtil,
                           Gson gson) {
        this.modelMapper = modelMapper;
        this.townRepository = townRepository;
        this.validationUtil = validationUtil;
        this.gson = gson;
    }

    @Override
    public Boolean townsAreImported() {
        return this.townRepository.count() > 0;
    }

    @Override
    public String readTownsJsonFile() throws IOException {
        return Files.readString(Path.of(TOWNS_FILE_PATH));
    }

    @Override
    public String importTowns(String townsFileContent) throws FileNotFoundException {
        StringBuilder stringBuilder = new StringBuilder();

        TownSeedDto[] townSeedDtos = this.gson
                .fromJson(new FileReader(TOWNS_FILE_PATH), TownSeedDto[].class);

        Arrays.stream(townSeedDtos)
                .forEach(townSeedDto -> {
                    if (this.townRepository.findFirstByName(townSeedDto.getName()) != null) {
                        stringBuilder.append("Already in DB.")
                                .append(System.lineSeparator());
                        return;
                    }

                    if (this.validationUtil.isValid(townSeedDto)) {
                        Town town = this.modelMapper
                                .map(townSeedDto, Town.class);

                        this.townRepository.saveAndFlush(town);

                        stringBuilder.append(String
                                .format(SUCCESSFUL_IMPORT_MESSAGE,
                                        town.getClass().getSimpleName(),
                                        town.getName()));
                    } else {
                        stringBuilder.append(INCORRECT_DATA_MESSAGE);
                    }

                    stringBuilder.append(System.lineSeparator());
                });

        return stringBuilder.toString().trim();
    }

    @Override
    public Town getTownByName(String name) {
        return this.townRepository.findFirstByName(name);
    }
}
