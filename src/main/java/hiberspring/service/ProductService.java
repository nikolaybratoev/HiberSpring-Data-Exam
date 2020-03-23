package hiberspring.service;

import hiberspring.domain.entities.Product;

import javax.xml.bind.JAXBException;
import java.io.FileNotFoundException;
import java.io.IOException;

public interface ProductService {

    Boolean productsAreImported();

    String readProductsXmlFile() throws IOException;

    String importProducts() throws JAXBException, FileNotFoundException;

    Product getProductByName(String name);
}
