package hiberspring.domain.dtos;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

import static javax.xml.bind.annotation.XmlAccessType.FIELD;

@XmlRootElement(name = "products")
@XmlAccessorType(FIELD)
public class ProductSeedRootDto {

    @XmlElement(name = "product")
    List<ProductSeedDto> productSeedDtos;

    public ProductSeedRootDto() {
    }

    public List<ProductSeedDto> getProductSeedDtos() {
        return productSeedDtos;
    }

    public void setProductSeedDtos(List<ProductSeedDto> productSeedDtos) {
        this.productSeedDtos = productSeedDtos;
    }
}
