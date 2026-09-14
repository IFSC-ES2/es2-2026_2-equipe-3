export interface ErroValidacaoCampo {
    campo: string;
    mensagem: string;
}

export interface RespostaErroPadrao {
    timestamp: string;
    status: number;
    erro: string;
    caminho: string;
    detalhes?: ErroValidacaoCampo[] | string;
}