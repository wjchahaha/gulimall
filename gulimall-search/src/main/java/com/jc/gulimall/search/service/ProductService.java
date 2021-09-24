package com.jc.gulimall.search.service;

import com.jc.common.to.SkuEsModel;

import java.io.IOException;
import java.util.List;

public interface ProductService {
    boolean productUp(List<SkuEsModel> skuEsModels) throws IOException;
}
