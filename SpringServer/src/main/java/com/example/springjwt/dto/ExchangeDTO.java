package com.example.springjwt.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExchangeDTO {
    private List<AppDTO> apps;
}

