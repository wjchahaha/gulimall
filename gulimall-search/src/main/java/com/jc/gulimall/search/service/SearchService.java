package com.jc.gulimall.search.service;

import com.jc.gulimall.search.vo.SearchParam;
import com.jc.gulimall.search.vo.SearchResult;

public interface SearchService {

    SearchResult search(SearchParam vo);
}
