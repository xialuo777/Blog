package com.blog.util.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author: zhang
 * @time: 2024-09-14 12:55
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
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