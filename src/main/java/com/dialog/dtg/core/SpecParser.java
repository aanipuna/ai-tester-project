package com.dialog.dtg.core;

import com.dialog.dtg.core.model.NormalizedSpec;

public interface SpecParser {

    NormalizedSpec parseFromOpenApi(String sourcePath);

    NormalizedSpec parseFromPostman(String sourcePath);

    NormalizedSpec parseFromManualJson(String sourcePath);
}
