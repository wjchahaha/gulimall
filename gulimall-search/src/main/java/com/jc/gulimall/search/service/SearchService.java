package com.jc.gulimall.search.service;

import com.jc.gulimall.search.vo.SearchParam;
import com.jc.gulimall.search.vo.SearchRespVo;

public interface SearchService {

    SearchRespVo search(SearchParam vo);
}
