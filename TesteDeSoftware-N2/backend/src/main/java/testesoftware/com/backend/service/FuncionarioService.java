package testesoftware.com.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import testesoftware.com.backend.dto.CadastroFuncionarioRequest;
import testesoftware.com.backend.enums.FuncaoFuncionario;
import testesoftware.com.backend.model.Funcionario;
import testesoftware.com.backend.repository.FuncionarioRepository;

import java.util.List;

/**
 * Camada de serviço responsável por consultas, cadastro e autenticação de Funcionários no PDV.
 */
@Service
public class FuncionarioService {

    private static final int TAMANHO_MINIMO_SENHA = 4;
    private static final String CREDENCIAIS_INVALIDAS = "Credenciais inválidas: e-mail ou senha incorretos.";

    private final FuncionarioRepository funcionarioRepository;

    public FuncionarioService(FuncionarioRepository funcionarioRepository) {
        this.funcionarioRepository = funcionarioRepository;
    }

    public List<Funcionario> listarTodos() {
        return funcionarioRepository.findAll();
    }

    public Funcionario buscarPorId(Long id) {
        return funcionarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Funcionário não encontrado com ID: " + id));
    }

    public List<Funcionario> listarFarmaceuticos() {
        return funcionarioRepository.findByFuncao(FuncaoFuncionario.FARMACEUTICO);
    }

    // CRF obrigatório para farmacêutico; senha mínima de 4 caracteres
    @Transactional
    public Funcionario cadastrar(CadastroFuncionarioRequest dados) {
        String email = dados.email().trim().toLowerCase();
        if (funcionarioRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Já existe um funcionário cadastrado com o e-mail: " + email);
        }

        if (dados.senha().length() < TAMANHO_MINIMO_SENHA) {
            throw new IllegalArgumentException("A senha deve ter no mínimo " + TAMANHO_MINIMO_SENHA + " caracteres.");
        }

        String crf = dados.crf() != null && !dados.crf().isBlank() ? dados.crf().trim() : null;
        if (dados.cargo() == FuncaoFuncionario.FARMACEUTICO && crf == null) {
            throw new IllegalArgumentException("O CRF é obrigatório para funcionários com cargo FARMACEUTICO.");
        }

        String cpf = dados.cpf() != null && !dados.cpf().isBlank() ? dados.cpf().trim() : null;
        if (cpf != null && funcionarioRepository.existsByCpf(cpf)) {
            throw new IllegalArgumentException("Já existe um funcionário cadastrado com o CPF: " + cpf);
        }

        Funcionario funcionario = new Funcionario();
        funcionario.setNome(dados.nome().trim());
        funcionario.setEmail(email);
        funcionario.setSenha(dados.senha());
        funcionario.setFuncao(dados.cargo());
        funcionario.setCrf(dados.cargo() == FuncaoFuncionario.FARMACEUTICO ? crf : null);
        funcionario.setCpf(cpf);
        return funcionarioRepository.save(funcionario);
    }

    // Mesma mensagem para e-mail ou senha inválidos
    public Funcionario autenticar(String email, String senhaInformada) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("O e-mail é obrigatório para autenticação.");
        }
        if (senhaInformada == null || senhaInformada.isBlank()) {
            throw new IllegalArgumentException("A senha é obrigatória para autenticação.");
        }

        Funcionario funcionario = funcionarioRepository.findByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new IllegalArgumentException(CREDENCIAIS_INVALIDAS));

        if (funcionario.getSenha() == null || !funcionario.getSenha().equals(senhaInformada)) {
            throw new IllegalArgumentException(CREDENCIAIS_INVALIDAS);
        }
        return funcionario;
    }

    public boolean isFarmaceutico(Funcionario funcionario) {
        return funcionario != null && funcionario.getFuncao() == FuncaoFuncionario.FARMACEUTICO;
    }

    public boolean isGerente(Funcionario funcionario) {
        return funcionario != null && funcionario.getFuncao() == FuncaoFuncionario.GERENTE;
    }

    public boolean isOperadorCaixa(Funcionario funcionario) {
        return funcionario != null && funcionario.getFuncao() == FuncaoFuncionario.OPERADOR_CAIXA;
    }
}
