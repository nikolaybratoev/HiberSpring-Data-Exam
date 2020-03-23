package hiberspring.service.impls;

import hiberspring.domain.dtos.ProductSeedRootDto;
import hiberspring.domain.entities.Branch;
import hiberspring.domain.entities.Product;
import hiberspring.repository.ProductRepository;
import hiberspring.service.BranchService;
import hiberspring.service.ProductService;
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

import static hiberspring.common.Constants.*;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    private final XmlParser xmlParser;
    private final ProductRepository productRepository;
    private final ValidationUtil validationUtil;
    private final ModelMapper modelMapper;
    private final BranchService branchService;

    @Autowired
    public ProductServiceImpl(XmlParser xmlParser,
                              ProductRepository productRepository,
                              ValidationUtil validationUtil,
                              ModelMapper modelMapper,
                              BranchService branchService) {
        this.xmlParser = xmlParser;
        this.productRepository = productRepository;
        this.validationUtil = validationUtil;
        this.modelMapper = modelMapper;
        this.branchService = branchService;
    }

    @Override
    public Boolean productsAreImported() {
        return this.productRepository.count() > 0;
    }

    @Override
    public String readProductsXmlFile() throws IOException {
        return Files.readString(Path.of(PRODUCTS_FILE_PATH));
    }

    @Override
    public String importProducts() throws JAXBException, FileNotFoundException {
        StringBuilder stringBuilder = new StringBuilder();

        ProductSeedRootDto productSeedRootDto = this.xmlParser
                .parseXml(ProductSeedRootDto.class, PRODUCTS_FILE_PATH);

        productSeedRootDto.getProductSeedDtos()
                .forEach(productSeedDto -> {
                    if (this.productRepository.findFirstByName(productSeedDto.getName()) != null) {
                        stringBuilder.append("Already in DB.")
                                .append(System.lineSeparator());
                        return;
                    }

                    if (this.validationUtil.isValid(productSeedDto)) {
                        Product product = this.modelMapper
                                .map(productSeedDto, Product.class);

                        Branch branch = this.branchService
                                .getBranchByName(productSeedDto.getBranch());

                        product.setBranch(branch);

                        this.productRepository.saveAndFlush(product);

                        stringBuilder.append(String
                                .format(SUCCESSFUL_IMPORT_MESSAGE,
                                        product.getClass().getSimpleName(),
                                        product.getName()));
                    } else {
                        stringBuilder.append(INCORRECT_DATA_MESSAGE);
                    }

                    stringBuilder.append(System.lineSeparator());
                });

        return stringBuilder.toString().trim();
    }

    @Override
    public Product getProductByName(String name) {
        return this.productRepository.findFirstByName(name);
    }
}
