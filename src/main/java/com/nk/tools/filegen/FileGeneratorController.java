package com.nk.tools.filegen;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tools/json")
public class FileGeneratorController
{
    @Autowired
    FileGeneratorHelper fileGeneratorHelper;

    @RequestMapping(value = "generate", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public String generateJson(@RequestBody String inputTemplate)
    {
        String jsonData = fileGeneratorHelper.getJsonData(inputTemplate);

        return jsonData;
    }
}
