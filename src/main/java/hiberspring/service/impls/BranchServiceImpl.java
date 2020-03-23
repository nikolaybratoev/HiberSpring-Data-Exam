package hiberspring.service.impls;

import com.google.gson.Gson;
import hiberspring.domain.dtos.BranchSeedDto;
import hiberspring.domain.entities.Branch;
import hiberspring.domain.entities.Town;
import hiberspring.repository.BranchRepository;
import hiberspring.service.BranchService;
import hiberspring.service.TownService;
import hiberspring.util.ValidationUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static hiberspring.common.Constants.*;

@Service
@Transactional
public class BranchServiceImpl implements BranchService {

    private final ModelMapper modelMapper;
    private final Gson gson;
    private final BranchRepository branchRepository;
    private final ValidationUtil validationUtil;
    private final TownService townService;

    @Autowired
    public BranchServiceImpl(ModelMapper modelMapper,
                             Gson gson,
                             BranchRepository branchRepository,
                             ValidationUtil validationUtil,
                             TownService townService) {
        this.modelMapper = modelMapper;
        this.gson = gson;
        this.branchRepository = branchRepository;
        this.validationUtil = validationUtil;
        this.townService = townService;
    }

    @Override
    public Boolean branchesAreImported() {
        return this.branchRepository.count() > 0;
    }

    @Override
    public String readBranchesJsonFile() throws IOException {
        return Files.readString(Path.of(BRANCHES_FILE_PATH));
    }

    @Override
    public String importBranches(String branchesFileContent) throws FileNotFoundException {
        StringBuilder stringBuilder = new StringBuilder();

        BranchSeedDto[] branchSeedDtos = this.gson
                .fromJson(new FileReader(BRANCHES_FILE_PATH), BranchSeedDto[].class);

        Arrays.stream(branchSeedDtos)
                .forEach(branchSeedDto -> {
                    if (this.branchRepository.findFirstByName(branchSeedDto.getName()) != null) {
                        stringBuilder.append("Already in DB.")
                                .append(System.lineSeparator());
                        return;
                    }

                    if (this.validationUtil.isValid(branchSeedDto)) {
                        Town town = this.townService
                                .getTownByName(branchSeedDto.getTown());

                        Branch branch = this.modelMapper
                                .map(branchSeedDto, Branch.class);

                        branch.setTown(town);

                        this.branchRepository.saveAndFlush(branch);

                        stringBuilder.append(String
                                .format(SUCCESSFUL_IMPORT_MESSAGE,
                                        branch.getClass().getSimpleName(),
                                        branch.getName()));
                    } else {
                        stringBuilder.append(INCORRECT_DATA_MESSAGE);
                    }

                    stringBuilder.append(System.lineSeparator());
                });

        return stringBuilder.toString().trim();
    }

    @Override
    public Branch getBranchByName(String name) {
        return this.branchRepository.findFirstByName(name);
    }
}
