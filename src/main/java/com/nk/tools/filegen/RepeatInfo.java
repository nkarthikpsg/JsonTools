package com.nk.tools.filegen;

import java.util.Map;

public class RepeatInfo
{
    Map<String, Object> template;

    int start = 1;

    int end;

    public int getStart()
    {
        return start;
    }

    public void setStart(int start)
    {
        this.start = start;
    }

    public int getEnd()
    {
        return end;
    }

    public void setEnd(int end)
    {
        this.end = end;
    }

    public Map<String, Object> getTemplate()
    {
        return template;
    }

    public void setTemplate(Map<String, Object> template)
    {
        this.template = template;
    }
}
