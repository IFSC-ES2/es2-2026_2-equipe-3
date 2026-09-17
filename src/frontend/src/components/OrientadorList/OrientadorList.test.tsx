import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { vi } from 'vitest';
import { OrientadorList } from './index.tsx';
import { listarOrientadores } from '../../utils/orientadorService.ts';
import type { Orientador } from '../../types/Orientador.ts';

vi.mock('../../utils/orientadorService.ts', () => ({
    listarOrientadores: vi.fn(),
}));

describe('OrientadorList', () => {
    const mockOrientadores: Orientador[] = [
        {
            id: 1,
            nome: 'Dr. Adriano Lima',
            email: 'adriano@ifsc.edu.br',
            vagasDisponiveis: 3,
            linhasDePesquisa: ['IA'],
            ativo: true,
        },
        {
            id: 2,
            nome: 'Dra. Maria',
            email: 'Maria@ifsc.edu.br',
            vagasDisponiveis: 1,
            linhasDePesquisa: ['Redes'],
            ativo: false,
        },
    ];

    beforeEach(() => {
        vi.clearAllMocks();
    });

    test('deve exibir o estado de carregamento inicial', () => {
        vi.mocked(listarOrientadores).mockReturnValue(new Promise(() => {}));

        render(<OrientadorList />);

        expect(screen.getByText('Carregando orientadores...')).toBeInTheDocument();
    });

    test('deve carregar e renderizar apenas orientadores ativos na montagem', async () => {
        vi.mocked(listarOrientadores).mockResolvedValue(mockOrientadores);

        render(<OrientadorList />);

        await waitFor(() => {
            expect(screen.queryByText('Carregando orientadores...')).not.toBeInTheDocument();
        });

        expect(screen.getByText('Dr. Adriano Lima')).toBeInTheDocument();
        expect(screen.queryByText('Dra. Inativa')).not.toBeInTheDocument();
    });

    test('deve exibir mensagem de erro se a chamada à API falhar', async () => {
        vi.mocked(listarOrientadores).mockRejectedValue(new Error('Erro de rede'));

        render(<OrientadorList />);

        await waitFor(() => {
            expect(screen.getByText('Erro ao carregar orientadores')).toBeInTheDocument();
        });

        expect(screen.queryByText('Dr. Adriano Lima')).not.toBeInTheDocument();
    });

    test('deve exibir empty state quando não houver orientadores ativos retornados', async () => {
        vi.mocked(listarOrientadores).mockResolvedValue([mockOrientadores[1]]);

        render(<OrientadorList />);

        await waitFor(() => {
            expect(screen.getByText('Nenhum orientador encontrado')).toBeInTheDocument();
        });
    });

    test('deve chamar a API com o termo digitado ao submeter o formulário de filtro', async () => {
        const user = userEvent.setup();
        vi.mocked(listarOrientadores).mockResolvedValue([]);

        render(<OrientadorList />);

        await waitFor(() => {
            expect(screen.queryByText('Carregando orientadores...')).not.toBeInTheDocument();
        });

        const inputArea = screen.getByPlaceholderText('Ex.: Engenharia de Software');
        await user.type(inputArea, 'IA');

        const botaoFiltrar = screen.getByRole('button', { name: 'Filtrar' });
        await user.click(botaoFiltrar);

        expect(vi.mocked(listarOrientadores)).toHaveBeenCalledTimes(2);
        expect(vi.mocked(listarOrientadores)).toHaveBeenLastCalledWith('IA');
    });
});