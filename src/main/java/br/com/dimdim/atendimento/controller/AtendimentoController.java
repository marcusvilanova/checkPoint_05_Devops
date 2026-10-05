package br.com.dimdim.atendimento.controller;

import br.com.dimdim.atendimento.entity.Atendimento;
import br.com.dimdim.atendimento.entity.StatusAtendimento;
import br.com.dimdim.atendimento.service.AtendimentoService;
import br.com.dimdim.atendimento.service.ClienteSelecionadoInvalidoException;
import br.com.dimdim.atendimento.service.ClienteService;
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
@RequestMapping("/atendimentos")
public class AtendimentoController {
    private final AtendimentoService atendimentos;
    private final ClienteService clientes;

    public AtendimentoController(AtendimentoService atendimentos, ClienteService clientes) {
        this.atendimentos = atendimentos;
        this.clientes = clientes;
    }

    @InitBinder("atendimento")
    public void camposPermitidos(WebDataBinder binder) {
        binder.setAllowedFields("assunto", "descricao", "status", "dataAbertura", "clienteId");
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("atendimentos", atendimentos.listar());
        return "atendimentos/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("atendimento", new AtendimentoForm());
        return formulario(null, model);
    }

    @GetMapping("/{id}")
    public String detalhe(@PathVariable Long id, Model model) {
        model.addAttribute("atendimento", atendimentos.buscar(id));
        return "atendimentos/detalhe";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("atendimento", AtendimentoForm.de(atendimentos.buscar(id)));
        return formulario(id, model);
    }

    @PostMapping
    public String criar(@Valid @ModelAttribute("atendimento") AtendimentoForm atendimento,
                        BindingResult erros, Model model, RedirectAttributes redirect) {
        return salvar(null, atendimento, erros, model, redirect);
    }

    @PostMapping("/{id}")
    public String atualizar(@PathVariable Long id,
                            @Valid @ModelAttribute("atendimento") AtendimentoForm atendimento,
                            BindingResult erros, Model model, RedirectAttributes redirect) {
        atendimentos.buscar(id);
        return salvar(id, atendimento, erros, model, redirect);
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes redirect) {
        atendimentos.excluir(id);
        redirect.addFlashAttribute("mensagem", "Atendimento excluído com sucesso.");
        return "redirect:/atendimentos";
    }

    private String salvar(Long id, AtendimentoForm form, BindingResult erros, Model model,
                          RedirectAttributes redirect) {
        if (erros.hasErrors()) { return formulario(id, model); }
        try {
            Atendimento salvo = atendimentos.salvar(id, form.getAssunto(), form.getDescricao(),
                    form.getStatus(), form.getDataAbertura(), form.getClienteId());
            redirect.addFlashAttribute("mensagem", id == null ? "Atendimento registrado com sucesso."
                    : "Atendimento atualizado com sucesso.");
            return "redirect:/atendimentos/" + salvo.getId();
        } catch (ClienteSelecionadoInvalidoException exception) {
            erros.rejectValue("clienteId", "cliente.inexistente", exception.getMessage());
            return formulario(id, model);
        }
    }

    private String formulario(Long id, Model model) {
        model.addAttribute("registroId", id);
        model.addAttribute("titulo", id == null ? "Registrar atendimento" : "Editar atendimento");
        model.addAttribute("clientes", clientes.listar());
        model.addAttribute("situacoes", StatusAtendimento.values());
        return "atendimentos/formulario";
    }
}
