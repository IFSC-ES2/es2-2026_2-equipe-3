import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { vi } from 'vitest';
import { OrientadorForm } from './index.tsx';
import { cadastrarOrientador, atualizarOrientador, ApiError } from '../../utils/orientadorService.ts';
import type { Orientador } from '../../types/Orientador.ts';

vi.mock('../../utils/orientadorService.ts', () => {
    return {
        cadastrarOrientador: vi.fn(),
        atualizarOrientador: vi.fn(),
        ApiError: class extends Error {
            status: number;
            detalhes: { campo: string; mensagem: string }[];

            constructor(status: number, detalhes: { campo: string; mensagem: string }[]) {
                super('Erro de API');
                this.status = status;
                this.detalhes = detalhes;
            }
        }
    };
});

vi.mock('../LinhaPesquisa', () => ({
    LinhaPesquisaInput: ({ onChange }: { onChange: (linhas: string[]) => void }) => (
        <button
            type="button"
            onClick={() => onChange(['Engenharia de Software'])}
            data-testid="mock-add-linha"
        >
            Adicionar Linha Mock
        </button>
    )
}));

describe('OrientadorForm', () => {
    const mockOnSucesso = vi.fn();
    const mockOnErro = vi.fn();

    beforeEach(() => {
        vi.clearAllMocks();
    });

    test('deve renderizar o formulário vazio para modo de cadastro', () => {
        render(<OrientadorForm onSucesso={mockOnSucesso} onErro={mockOnErro} />);

        expect(screen.getByText('Cadastrar Novo Orientador')).toBeInTheDocument();
        expect(screen.getByRole('button', { name: 'Cadastrar orientador' })).toBeInTheDocument();
        expect(screen.getByLabelText(/nome \*/i)).toHaveValue('');
    });

    test('deve renderizar o formulário preenchido para modo de edição', () => {
        const orientadorExistente: Orientador = {
            id: 1,
            nome: 'Dr. Teste',
            email: 'teste@ifsc.edu.br',
            vagasDisponiveis: 2,
            departamento: 'DAE',
            biografia: 'Bio teste',
            linhasDePesquisa: ['IA'],
            ativo: true
        };

        render(<OrientadorForm orientador={orientadorExistente} onSucesso={mockOnSucesso} onErro={mockOnErro} />);

        expect(screen.getByText('Editar Perfil')).toBeInTheDocument();
        expect(screen.getByRole('button', { name: 'Atualizar perfil' })).toBeInTheDocument();
        expect(screen.getByLabelText(/nome \*/i)).toHaveValue('Dr. Teste');
        expect(screen.getByLabelText(/vagas disponíveis/i)).toHaveValue(2);
    });

    test('deve exibir erros de validação local e bloquear envio se os campos obrigatórios estiverem vazios', async () => {
        const user = userEvent.setup();
        render(<OrientadorForm onSucesso={mockOnSucesso} onErro={mockOnErro} />);

        const botaoSubmit = screen.getByRole('button', { name: 'Cadastrar orientador' });
        await user.click(botaoSubmit);

        expect(screen.getByText('O nome é obrigatório')).toBeInTheDocument();
        expect(screen.getByText('O e-mail é obrigatório')).toBeInTheDocument();
        expect(screen.getByText('Informe ao menos uma linha de pesquisa')).toBeInTheDocument();

        expect(cadastrarOrientador).not.toHaveBeenCalled();
    });

    test('deve limpar o erro do campo ao voltar a digitar nele', async () => {
        const user = userEvent.setup();
        render(<OrientadorForm onSucesso={mockOnSucesso} onErro={mockOnErro} />);

        await user.click(screen.getByRole('button', { name: 'Cadastrar orientador' }));
        expect(screen.getByText('O nome é obrigatório')).toBeInTheDocument();

        const inputNome = screen.getByLabelText(/nome \*/i);
        await user.type(inputNome, 'A');

        expect(screen.queryByText('O nome é obrigatório')).not.toBeInTheDocument();
    });

    test('deve submeter o formulário corretamente no modo de cadastro', async () => {
        const user = userEvent.setup();
        const mockResultado = { id: 1, nome: 'Novo Orientador' };

        vi.mocked(cadastrarOrientador).mockResolvedValue(mockResultado as unknown as Orientador);

        render(<OrientadorForm onSucesso={mockOnSucesso} onErro={mockOnErro} />);

        await user.type(screen.getByLabelText(/nome \*/i), 'Novo Orientador');
        await user.type(screen.getByLabelText(/e-mail \*/i), 'novo@ifsc.edu.br');

        const inputVagas = screen.getByLabelText(/vagas disponíveis/i);
        await user.clear(inputVagas);
        await user.type(inputVagas, '3');

        await user.click(screen.getByTestId('mock-add-linha'));

        await user.click(screen.getByRole('button', { name: 'Cadastrar orientador' }));

        await waitFor(() => {
            expect(cadastrarOrientador).toHaveBeenCalledWith({
                nome: 'Novo Orientador',
                email: 'novo@ifsc.edu.br',
                departamento: '',
                biografia: '',
                vagasDisponiveis: 3,
                linhasDePesquisa: ['Engenharia de Software']
            });
            expect(mockOnSucesso).toHaveBeenCalledWith(mockResultado);
        });

        expect(screen.getByLabelText(/nome \*/i)).toHaveValue('');
    });

    test('deve submeter o formulário corretamente no modo de edição', async () => {
        const user = userEvent.setup();
        const orientadorExistente: Orientador = {
            id: 1,
            nome: 'Dr. Teste',
            email: 'teste@ifsc.edu.br',
            vagasDisponiveis: 2,
            departamento: 'DAE',
            biografia: 'Bio teste',
            linhasDePesquisa: ['IA'],
            ativo: true
        };
        const mockResultado = { ...orientadorExistente, nome: 'Dr. Teste Atualizado' };

        vi.mocked(atualizarOrientador).mockResolvedValue(mockResultado as unknown as Orientador);

        render(<OrientadorForm orientador={orientadorExistente} onSucesso={mockOnSucesso} onErro={mockOnErro} />);

        const inputNome = screen.getByLabelText(/nome \*/i);
        await user.clear(inputNome); // Limpa o valor antigo
        await user.type(inputNome, 'Dr. Teste Atualizado'); // Digita o novo

        await user.click(screen.getByRole('button', { name: 'Atualizar perfil' }));

        await waitFor(() => {
            expect(atualizarOrientador).toHaveBeenCalledWith(1, expect.objectContaining({
                nome: 'Dr. Teste Atualizado',
                email: 'teste@ifsc.edu.br'
            }));
            expect(mockOnSucesso).toHaveBeenCalledWith(mockResultado);
        });
    });

    test('deve processar erros de validação vindos do backend (ApiError 400)', async () => {
        const user = userEvent.setup();
        vi.mocked(cadastrarOrientador).mockRejectedValue(
            // eslint-disable-next-line @typescript-eslint/no-explicit-any
            new ApiError(400, [{ campo: 'email', mensagem: 'E-mail já cadastrado' }] as any)
        );

        render(<OrientadorForm onSucesso={mockOnSucesso} onErro={mockOnErro} />);

        await user.type(screen.getByLabelText(/nome \*/i), 'Fulano');
        await user.type(screen.getByLabelText(/e-mail \*/i), 'duplicado@ifsc.edu.br');
        await user.click(screen.getByTestId('mock-add-linha'));

        await user.click(screen.getByRole('button', { name: 'Cadastrar orientador' }));

        await waitFor(() => {
            expect(screen.getByText('E-mail já cadastrado')).toBeInTheDocument();
            expect(mockOnErro).toHaveBeenCalled();
        });
    });
});