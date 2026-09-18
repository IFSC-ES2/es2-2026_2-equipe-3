import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { LinhaPesquisaInput } from "./index.tsx";

describe("LinhaPesquisaInput", () => {
  const onChangeMock = vi.fn();

  beforeEach(() => {
    onChangeMock.mockClear();
  });

  test("deve renderizar o componente com o input e as linhas iniciais", () => {
    render(
      <LinhaPesquisaInput
        linhas={["Inteligência Artificial", "Redes"]}
        onChange={onChangeMock}
      />,
    );

    expect(
      screen.getByPlaceholderText("Ex: Formação de Professores"),
    ).toBeInTheDocument();
    expect(screen.getByText("Inteligência Artificial")).toBeInTheDocument();
    expect(screen.getByText("Redes")).toBeInTheDocument();
  });

  test('deve chamar onChange com a nova linha ao clicar em "Adicionar" e limpar o input', async () => {
    const user = userEvent.setup();
    render(<LinhaPesquisaInput linhas={["IA"]} onChange={onChangeMock} />);

    const input = screen.getByPlaceholderText("Ex: Formação de Professores");
    await user.type(input, "Engenharia de Software");

    const button = screen.getByRole("button", { name: "Adicionar" });
    await user.click(button);

    expect(onChangeMock).toHaveBeenCalledTimes(1);
    expect(onChangeMock).toHaveBeenCalledWith(["IA", "Engenharia de Software"]);
    expect(input).toHaveValue(""); // Verifica se o input foi limpo após adicionar[cite: 19]
  });

  test("deve chamar onChange ao pressionar a tecla Enter", async () => {
    const user = userEvent.setup();
    render(<LinhaPesquisaInput linhas={[]} onChange={onChangeMock} />);

    const input = screen.getByPlaceholderText("Ex: Formação de Professores");
    await user.type(input, "Machine Learning{enter}"); // O Enter aciona o onKeyDown[cite: 19]

    expect(onChangeMock).toHaveBeenCalledWith(["Machine Learning"]);
  });

  test("deve exibir mensagem de erro se a linha tiver menos de 2 caracteres", async () => {
    const user = userEvent.setup();
    render(<LinhaPesquisaInput linhas={[]} onChange={onChangeMock} />);

    const input = screen.getByPlaceholderText("Ex: Formação de Professores");
    await user.type(input, "A");

    const button = screen.getByRole("button", { name: "Adicionar" });
    await user.click(button);

    expect(
      screen.getByText("A linha de pesquisa deve ter entre 2 e 80 caracteres."),
    ).toBeInTheDocument();
    expect(onChangeMock).not.toHaveBeenCalled();
  });

  test("deve exibir mensagem de erro se a linha já existir ignorando maiúsculas/minúsculas", async () => {
    const user = userEvent.setup();
    render(
      <LinhaPesquisaInput
        linhas={["Engenharia de Software"]}
        onChange={onChangeMock}
      />,
    );

    const input = screen.getByPlaceholderText("Ex: Formação de Professores");
    await user.type(input, "ENGENHARIA DE SOFTWARE");

    const button = screen.getByRole("button", { name: "Adicionar" });
    await user.click(button);

    expect(
      screen.getByText("Esta linha de pesquisa já foi adicionada."),
    ).toBeInTheDocument();
    expect(onChangeMock).not.toHaveBeenCalled();
  });

  test("deve limpar a mensagem de erro ao voltar a digitar no input", async () => {
    const user = userEvent.setup();
    render(<LinhaPesquisaInput linhas={[]} onChange={onChangeMock} />);

    const input = screen.getByPlaceholderText("Ex: Formação de Professores");
    await user.type(input, "A{enter}");

    expect(
      screen.getByText("A linha de pesquisa deve ter entre 2 e 80 caracteres."),
    ).toBeInTheDocument();

    await user.type(input, "B");

    expect(
      screen.queryByText(
        "A linha de pesquisa deve ter entre 2 e 80 caracteres.",
      ),
    ).not.toBeInTheDocument();
  });

  test("deve chamar onChange removendo a linha ao clicar no botão de remover", async () => {
    const user = userEvent.setup();
    render(
      <LinhaPesquisaInput
        linhas={["IA", "Redes", "Segurança"]}
        onChange={onChangeMock}
      />,
    );

    const removeButton = screen.getByRole("button", { name: "Remover Redes" });
    await user.click(removeButton);

    expect(onChangeMock).toHaveBeenCalledWith(["IA", "Segurança"]);
  });
});
