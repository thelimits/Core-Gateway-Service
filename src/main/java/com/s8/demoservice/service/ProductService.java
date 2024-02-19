package com.s8.demoservice.service;

import com.s8.demoservice.AppConfiguration;
import com.s8.demoservice.dto.AccountResponseDTO;
import com.s8.demoservice.dto.ProductDTO;
import com.s8.demoservice.dto.ProductDetailResponseDTO;
import com.s8.demoservice.dto.ProductVersionDTO;
import com.s8.demoservice.model.enums.Endpoint;
import com.s8.demoservice.repository.AccountRepository;
import com.s8.demoservice.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductService {

    Logger logger = LoggerFactory.getLogger(ProductService.class);
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private ThoughtMachineApiClient thoughtMachineApiClient;
    @Autowired
    private AppConfiguration configuration;

    @Autowired
    private AccountService accountService;

    public ProductDetailResponseDTO getProductDetail(Map<String, Object> queryParamMap){
        ProductDTO productDTO = new ProductDTO();
        AccountResponseDTO account = accountService.getTMAccountDetail(queryParamMap);
        String productVersionId = account.getProduct_version_id();

        queryParamMap.put("view", "PRODUCT_VERSION_VIEW_INCLUDE_PARAMETERS");
        queryParamMap.put("ids", productVersionId);
        String queryString = StringUtil.convertMapToQueryParamString(queryParamMap);
        
        ResponseEntity<ProductDTO> response = thoughtMachineApiClient.get(getTMProductAsyncApiUrl(), queryString, ProductDTO.class);

        return new ProductDetailResponseDTO(
                Objects.requireNonNull(response.getBody()).getProduct_versions().get(productVersionId)
        );
    }

    private String getTMProductAsyncApiUrl() {
        return configuration.getThoughtMachineApiServer() + Endpoint.PRODUCTBATCH.endPoint;
    }

}
