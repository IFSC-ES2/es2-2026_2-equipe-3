package br.edu.ifsc.gestao_tcc.dto;

/**
 * Resultado da submissão de uma entrega. {@code criada} é verdadeiro na primeira
 * submissão da etapa (201 Created) e falso em um reenvio (200 OK).
 */
public record SubmissaoEntregaResultado(EntregaResponseDTO entrega, boolean criada) {
}
