package com.nk.tools.payload;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class PayloadHelper
{
    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    ResourceLoader resourceLoader;

    public String getCleanPayload(String input)
    {
        try
        {
            Map<String, Object> inputAndTemplateMap = objectMapper.readValue(input, Map.class);

            Map<String, Object> inputMap = (Map) inputAndTemplateMap.get("Input");
            Map<String, Object> templateMap = (Map) inputAndTemplateMap.get("Template");

            if(inputMap.containsKey("data") && inputMap.containsKey("success"))
            {
                Object data = inputMap.get("data");
                if(data instanceof Map)
                    inputMap = (Map<String, Object>) data;
                if(data instanceof List && ObjectUtils.isNotEmpty(data))
                    inputMap = (Map<String, Object>)((List)data).get(0);
            }
            Map<String, Object> outputMap = applyTemplateOnDTOMap(inputMap, templateMap);

            String output = objectMapper.writeValueAsString(outputMap);

            return output;
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public Map<String, Object> applyTemplateOnDTOMap(Map<String, Object> dtoMap, Map<String, Object> template)
    {
        if (template == null || dtoMap == null || dtoMap.isEmpty())
            return dtoMap;

        Map<String, Object> output = new HashMap<>();
        for (Map.Entry<String, Object> entry : template.entrySet())
        {
            String name = entry.getKey();
            Object templateValue = entry.getValue();
            Object obj = dtoMap.get(name);
            if (obj instanceof Map)
            {
                output.put(name, applyTemplateOnDTOMap((Map) obj, (Map) templateValue));
            }
            else if (obj instanceof List<?>)
            {
                List<Map<String, Object>> objList = (List<Map<String, Object>>) obj;
                List<Map<String, Object>> templateAppliedList = new ArrayList<>();
                for (Map<String, Object> objItem : objList)
                {
                    templateAppliedList.add(applyTemplateOnDTOMap(objItem, (Map) templateValue));
                }
                output.put(name, templateAppliedList);
            }
            else
            {
                output.put(name, obj);
            }
        }
        return output;
    }

}