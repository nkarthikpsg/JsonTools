package com.nk.tools.payload;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tools/payload")
public class PayloadController
{
    @Autowired
    PayloadHelper payloadHelper;

    @RequestMapping(value = "clean", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public String cleanPayload(@RequestBody String input)
    {
        String cleanPayload = payloadHelper.getCleanPayload(input);

        return cleanPayload;
    }
}
