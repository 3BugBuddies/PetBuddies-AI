package br.com.fiap.petbuddies.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ErrorDto {

    private String code;
    private String message;
}
