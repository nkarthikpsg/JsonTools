package com.nk.tools.filegen;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class FileGeneratorHelper
{
    @Autowired
    ObjectMapper objectMapper;

    private static String replacePattern = "\\{\\{\\$.+\\}\\}";

    private static Pattern pattern1 = Pattern.compile("\\{\\{\\$(.+)\\}\\}");

    private static Pattern pattern2 = Pattern.compile("\\{\\{\\$repeat\\((.+)\\)\\}\\}");

    public String getJsonData(String input)
    {
        String output = null;
        try
        {
            Map<String, Object> inputTemplateMap = objectMapper.readValue(input, Map.class);
            Map<String, Object> outputMap = getValueMap(inputTemplateMap, 1);
            output = objectMapper.writeValueAsString(outputMap);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }

        return output;
    }

    public Map<String, Object> getValueMap(Map<String, Object> input, int index)
    {
        Map<String, Object> outputMap = new LinkedHashMap<>();
        try
        {
            for (Map.Entry<String, Object> entry : input.entrySet())
            {
                String key = entry.getKey();
                Object value = entry.getValue();

                if (value instanceof Map)
                {
                    outputMap.put(key, getValueMap((Map) value, index));
                }
                else if (value instanceof List<?>)
                {
                    List<Map<String, Object>> templateAppliedList = getValueList(index, key,
                            (List<Map<String, Object>>) value);

                    outputMap.put(key, templateAppliedList);
                }
                else
                {
                    outputMap.put(key, getValue(value, index));
                }

            }

            return outputMap;
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private List<Map<String, Object>> getValueList(int index, String key, List<Map<String, Object>> value)
    {
        List<Map<String, Object>> valueList = value;
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map<String, Object> objItem : valueList)
        {
            RepeatInfo repeatInfo = getRepeatInfo(objItem);
            if (repeatInfo == null)
            {
                Map<String, Object> valueMap = getValueMap(objItem, index);
                list.add(valueMap);
            }
            else
            {
                for (int i = repeatInfo.getStart(); i <= repeatInfo.getEnd(); i++)
                {
                    Map<String, Object> valueMap = getValueMap(repeatInfo.getTemplate(), i);
                    list.add(valueMap);
                }
            }
        }
        return list;
    }

    private RepeatInfo getRepeatInfo(Map<String, Object> objItem)
    {
        RepeatInfo repeatInfo = null;
        if (objItem.size() == 1)
        {
            Map.Entry<String, Object> entry = objItem.entrySet().iterator().next();
            String key = entry.getKey();
            Matcher matcher = pattern2.matcher(key);
            if (matcher.find())
            {
                repeatInfo = new RepeatInfo();
                String[] repeats = matcher.group(1).split(",");
                if (repeats.length > 1)
                {
                    repeatInfo.setStart(Integer.parseInt(repeats[0].trim()));
                    repeatInfo.setEnd(Integer.parseInt(repeats[1].trim()));
                }
                else
                {
                    repeatInfo.setEnd(Integer.parseInt(repeats[0].trim()));
                }
                repeatInfo.setTemplate((Map) entry.getValue());
            }
        }

        return repeatInfo;
    }

    private Object getValue(Object value, int index)
    {
        if (value instanceof String)
        {
            value = getSubstitutedValue(value, index);
        }

        return value;
    }

    private Object getSubstitutedValue(Object value, int index)
    {
        Matcher matcher = pattern1.matcher((String) value);
        if (matcher.find())
        {
            String groupValue = matcher.group(1);

            if (groupValue.equals(FileGenConstants.index))
            {
                String valueStr = (String) value;
                value = valueStr.replaceAll(replacePattern, String.valueOf(index));
            }
            else if (groupValue.equals(FileGenConstants.index_num))
            {
                value = Integer.valueOf(index);
            }
            else if (groupValue.equals(FileGenConstants.uid))
            {
                value = UUID.randomUUID().toString();
            }
        }

        return value;
    }
}