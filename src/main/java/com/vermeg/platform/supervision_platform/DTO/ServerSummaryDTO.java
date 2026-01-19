package com.vermeg.platform.supervision_platform.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ServerSummaryDTO {

    private long id;
    private String name;
    private String type;
    private String status;

}
