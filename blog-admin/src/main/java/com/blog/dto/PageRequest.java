package com.blog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
@AllArgsConstructor
public class PageRequest extends LinkedHashMap<String, Object> {
    private int pageNo;
    private int pageSize;

    public PageRequest(Map<String, Object> params){
        this.putAll(params);
        this.pageNo = Integer.parseInt(params.get("pageNo").toString());
        this.pageSize = Integer.parseInt(params.get("pageSize").toString());
        this.put("pageSize", pageSize);
        this.put("pageNo",pageNo);
    }
}