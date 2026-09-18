import { render, screen } from "@testing-library/react";
import { OrientadorCard } from "./index.tsx";
import type { Orientador } from "../../types/Orientador.ts";

describe("OrientadorCard", () => {
  const orientadorCompleto: Orientador = {
    id: 1,
    nome: "Dr. Adriano Lima",
    email: "adriano.lima@ifsc.edu.br",
    vagasDisponiveis: 3,
    departamento: "DAE - Câmpus São José",
    biografia: "Doutor em Computação com foco em qualidade de software.",
    linhasDePesquisa: ["Engenharia de Software", "Testes Automatizados"],
    ativo: true,
  };

  const orientadorSemOpcionais: Orientador = {
    id: 2,
    nome: "Dra. Maria Silva",
    email: "maria.silva@ifsc.edu.br",
    vagasDisponiveis: 0,
    linhasDePesquisa: [],
    ativo: true,
  };

  test("deve renderizar todas as informações do orientador corretamente", () => {
    render(<OrientadorCard orientador={orientadorCompleto} />);

    expect(screen.getByText("Orientador")).toBeInTheDocument();
    expect(screen.getByText("Dr. Adriano Lima")).toBeInTheDocument();
    expect(screen.getByText("adriano.lima@ifsc.edu.br")).toBeInTheDocument();
    expect(screen.getByText("3 vagas")).toBeInTheDocument();

    expect(screen.getByText("DAE - Câmpus São José")).toBeInTheDocument();
    expect(
      screen.getByText(
        "Doutor em Computação com foco em qualidade de software.",
      ),
    ).toBeInTheDocument();

    expect(screen.getByText("Engenharia de Software")).toBeInTheDocument();
    expect(screen.getByText("Testes Automatizados")).toBeInTheDocument();
  });

  test("não deve renderizar campos de departamento e biografia quando não informados", () => {
    render(<OrientadorCard orientador={orientadorSemOpcionais} />);

    expect(screen.getByText("Dra. Maria Silva")).toBeInTheDocument();
    expect(screen.getByText("0 vagas")).toBeInTheDocument();

    expect(screen.queryByText("DAE - Câmpus São José")).not.toBeInTheDocument();
    expect(
      screen.queryByText(
        "Doutor em Computação com foco em qualidade de software.",
      ),
    ).not.toBeInTheDocument();
  });

  test("deve renderizar corretamente quando a lista de áreas de pesquisa estiver vazia", () => {
    const { container } = render(
      <OrientadorCard orientador={orientadorSemOpcionais} />,
    );

    expect(screen.getByText("Áreas de pesquisa")).toBeInTheDocument();

    const lista = container.querySelector("ul");
    expect(lista?.children.length).toBe(0);
  });
});
