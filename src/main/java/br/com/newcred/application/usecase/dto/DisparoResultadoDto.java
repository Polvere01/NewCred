package br.com.newcred.application.usecase.dto;

import java.util.List;

public record DisparoResultadoDto(int total,
                                  int enviados,
                                  int falhas,
                                  List<FalhaDto> erros) {
}
