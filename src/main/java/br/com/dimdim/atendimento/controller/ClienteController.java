package br.com.dimdim.atendimento.controller;

import br.com.dimdim.atendimento.entity.Cliente;
import br.com.dimdim.atendimento.service.ClienteService;
import br.com.dimdim.atendimento.service.RegraNegocioException;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/clientes")
public class ClienteController {
    private final ClienteService clientes;

    public ClienteController(ClienteService clientes) { this.clientes = clientes; }

    @InitBinder("cliente")
    public void camposPermitidos(WebDataBinder binder) {
        binder.setAllowedFields("nome", "email", "telefone");
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("clientes", clientes.listar());
        return "clientes/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("cliente", new Cliente());
        return formulario(null, model);
    }

    @GetMapping("/{id}")
    public String detalhe(@PathVariable Long id, Model model) {
        model.addAttribute("cliente", clientes.buscar(id));
        model.addAttribute("atendimentos", clientes.listarAtendimentos(id));
        return "clientes/detalhe";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("cliente", clientes.buscar(id));
        return formulario(id, model);
    }

    @PostMapping
    public String criar(@Valid @ModelAttribute("cliente") Cliente cliente, BindingResult erros,
                        Model model, RedirectAttributes redirect) {
        return salvar(null, cliente, erros, model, redirect);
    }

    @PostMapping("/{id}")
    public String atualizar(@PathVariable Long id, @Valid @ModelAttribute("cliente") Cliente cliente,
                            BindingResult erros, Model model, RedirectAttributes redirect) {
        clientes.buscar(id);
        return salvar(id, cliente, erros, model, redirect);
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            clientes.excluir(id);
            redirect.addFlashAttribute("mensagem", "Cliente excluído com sucesso.");
            return "redirect:/clientes";
        } catch (RegraNegocioException exception) {
            redirect.addFlashAttribute("erro", exception.getMessage());
            return "redirect:/clientes/" + id;
        }
    }

    private String salvar(Long id, Cliente cliente, BindingResult erros, Model model,
                          RedirectAttributes redirect) {
        if (erros.hasErrors()) { return formulario(id, model); }
        Cliente salvo = clientes.salvar(id, cliente);
        redirect.addFlashAttribute("mensagem", id == null ? "Cliente cadastrado com sucesso."
                : "Cliente atualizado com sucesso.");
        return "redirect:/clientes/" + salvo.getId();
    }

    private String formulario(Long id, Model model) {
        model.addAttribute("registroId", id);
        model.addAttribute("titulo", id == null ? "Cadastrar cliente" : "Editar cliente");
        return "clientes/formulario";
    }
}
