package com.nk.tools.payload;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
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
            Map<String, Object> inputMap = objectMapper.readValue(input, Map.class);
            if(inputMap.containsKey("data") && inputMap.containsKey("success"))
            {
                Object data = inputMap.get("data");
                if(data instanceof Map)
                    inputMap = (Map<String, Object>) data;
                if(data instanceof List && ObjectUtils.isNotEmpty(data))
                    inputMap = (Map<String, Object>)((List)data).get(0);
            }
            String template = getTemplate(inputMap.containsKey("PaymentMethod") ? "Payment.json" : "Order_Invoice.json");
            Map<String, Object> templateMap = objectMapper.readValue(template, Map.class);
//            templateMap = convertToTemplate(templateMap);
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

        boolean condition = true;
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
                if(templateValue != null && !Objects.equals(templateValue, obj))
                {
                    condition = false;
                    break;
                }
                output.put(name, obj);
            }
        }

        return !condition ? null : output;
    }

    private String getTemplate(String fileName)
    {
        String query = null;
        Resource resource = resourceLoader.getResource("classpath:/promQueries/" + fileName);
        try
        {
            InputStream file = resource.getInputStream();
            if (file != null)
            {
                query = IOUtils.toString(file, StandardCharsets.UTF_8);
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        return query;
    }

    private Map<String, Object> convertToTemplate(Map<String, Object> templateMap)
    {
        Map<String, Object> template = new HashMap<>();

        addEntries(templateMap.entrySet(), template);

        return template;
    }

    private void addEntries(Set<Map.Entry<String, Object>> entrySet, Map<String, Object> template)
    {
        for (Map.Entry<String, Object> entry : entrySet)
        {
            if (entry.getValue() instanceof Map)
            {
                Map<String, Object> childTemplate = new HashMap<>();
                template.put(entry.getKey(), childTemplate);
                Map<String, Object> childMap = (Map<String, Object>) entry.getValue();
                addEntries(childMap.entrySet(), childTemplate);
            }
            else
            {
                template.put(entry.getKey(), null);
            }
        }
    }

}