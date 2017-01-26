package com.matt.forgehax.asm2.util;

import bspkrs.mmv.*;
import com.google.common.collect.Maps;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * Credits to bspkrs
 */
public class MCPMappingLoader {
    private SrgFile srgFileData;
    private ExcFile excFileData;
    private StaticMethodsFile staticMethods;
    private CsvFile csvFieldData, csvMethodData;
    private ParamCsvFile csvParamData;

    public MCPMappingLoader() throws IOException {
        staticMethods = new StaticMethodsFile(getClass().getResourceAsStream(MappingResources.STATIC_METHODS));
        excFileData = new ExcFile(getClass().getResourceAsStream(MappingResources.JOINED_EXC));
        srgFileData = new SrgFile(getClass().getResourceAsStream(MappingResources.JOINED_SRG), excFileData, staticMethods);

        csvFieldData = new CsvFile(getClass().getResourceAsStream(MappingResources.FIELDS_CSV));
        csvMethodData = new CsvFile(getClass().getResourceAsStream(MappingResources.METHODS_CSV));
        csvParamData = new ParamCsvFile(getClass().getResourceAsStream(MappingResources.PARAMS_CSV));
    }

    public CsvFile getCsvMethodData() {
        return csvMethodData;
    }

    public CsvFile getCsvFieldData() {
        return csvFieldData;
    }

    public SrgFile getSrgFileData() {
        return srgFileData;
    }
}
