import React, { useState } from "react";
import type { KeyboardEvent } from "react";
import "./LinhaPesquisaInput.css";

interface LinhaPesquisaInputProps {
  linhas: string[];
  onChange: (novasLinhas: string[]) => void;
}

export function LinhaPesquisaInput({
  linhas,
  onChange,
}: LinhaPesquisaInputProps) {
  const [inputValue, setInputValue] = useState("");
  const [erro, setErro] = useState("");

  const processarAdicao = (
    e?: React.MouseEvent<HTMLButtonElement> | KeyboardEvent<HTMLInputElement>,
  ) => {
    if (e) e.preventDefault(); // Previne o envio acidental do formulário pai

    const textoFormatado = inputValue.trim();

    if (textoFormatado.length < 2 || textoFormatado.length > 80) {
      setErro("A linha de pesquisa deve ter entre 2 e 80 caracteres.");
      return;
    }

    if (
      linhas.some(
        (linha) => linha.toLowerCase() === textoFormatado.toLowerCase(),
      )
    ) {
      setErro("Esta linha de pesquisa já foi adicionada.");
      return;
    }

    onChange([...linhas, textoFormatado]);
    setInputValue("");
    setErro("");
  };

  const handleKeyDown = (e: KeyboardEvent<HTMLInputElement>) => {
    if (e.key === "Enter") {
      processarAdicao(e);
    }
  };

  const removerLinha = (linhaParaRemover: string) => {
    onChange(linhas.filter((linha) => linha !== linhaParaRemover));
  };

  return (
    <div className="linha-pesquisa-container">
      <div className="input-group">
        <input
          type="text"
          value={inputValue}
          onChange={(e) => {
            setInputValue(e.target.value);
            if (erro) setErro(""); // Remove a mensagem de erro quando o usuário volta a digitar
          }}
          onKeyDown={handleKeyDown}
          placeholder="Ex: Formação de Professores"
          maxLength={80}
        />
        <button type="button" onClick={processarAdicao}>
          Adicionar
        </button>
      </div>

      {erro && <p className="erro-mensagem">{erro}</p>}

      <div className="tags-container">
        {linhas.map((linha, index) => (
          <div key={index} className="tag-pilula">
            <span>{linha}</span>
            <button
              type="button"
              className="btn-remover-tag"
              onClick={() => removerLinha(linha)}
              aria-label={`Remover ${linha}`}
              title={`Remover ${linha}`}
            >
              &times;
            </button>
          </div>
        ))}
      </div>
    </div>
  );
}
